package com.glocks.web_parser.service.parser.moi.loststolen;

import com.glocks.web_parser.config.AppConfig;
import com.glocks.web_parser.model.app.StolenDeviceDetail;
import com.glocks.web_parser.model.app.StolenDeviceMgmt;
import com.glocks.web_parser.model.app.WebActionDb;
import com.glocks.web_parser.repository.app.*;
import com.glocks.web_parser.service.parser.moi.utility.*;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class MOILostStolenService {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final AppConfig appConfig;
    private final MOIService moiService;
    private final StolenDeviceDetailRepository stolenDeviceDetailRepository;
    private final Notification notificationForPendingVerification;

    public void fileProcess(WebActionDb webActionDb, StolenDeviceMgmt stolenDeviceMgmt, String uploadedFileName, String uploadedFilePath, int greyListDuration) {
        try (BufferedReader reader = new BufferedReader(new FileReader(uploadedFilePath))) {
            String record;
            IMEISeriesModel imeiSeriesModel = new IMEISeriesModel();
            String[] split;
            boolean headerSkipped = false;
            while ((record = reader.readLine()) != null) {
                if (!record.trim().isEmpty()) {
                    if (!headerSkipped) {
                        headerSkipped = true;
                    } else {
                        split = record.split(appConfig.getMoiFileSeparator(), -1);
                        imeiSeriesModel.setImeiSeries(split, "STOLEN");
                        List<String> imeiList = moiService.imeiSeries.apply(imeiSeriesModel);
                        if (!imeiList.isEmpty()) imeiList.forEach(imei -> {
                            if (!moiService.isNumericAndValid.test(imei)) {
                                logger.info("Invalid IMEI found");
                            } else {
                                this.recordProcess(imei, stolenDeviceMgmt, stolenDeviceMgmt.getDeviceLostDateTime(), "Bulk", greyListDuration, webActionDb);
                            }
                        });
                    }
                }
            }
            if (Objects.nonNull(stolenDeviceMgmt.getDeviceOwnerNationality())) {
                String channel = stolenDeviceMgmt.getDeviceOwnerNationality().equals("0") ? "SMS" : "EMAIL";
                notificationForPendingVerification.sendNotification(webActionDb, stolenDeviceMgmt, channel, uploadedFilePath, ConfigurableParameter.MOI_VERIFICATION_DONE_MSG.getValue());
                logger.info("notification sent to {} mode user , 0:Cambodian 1:Non-cambodian", stolenDeviceMgmt.getDeviceOwnerNationality());
            }
        } catch (Exception exception) {
            logger.error("Exception in processing the file " + exception.getMessage());
            ExceptionModel exceptionModel = ExceptionModel.builder()
                    .error(exception.getMessage())
                    .transactionId(stolenDeviceMgmt.getRequestId())
                    .subFeature(webActionDb.getSubFeature())
                    .build();
            moiService.exception(exceptionModel);
        }
    }

    public void recordProcess(String imei, StolenDeviceMgmt stolenDeviceMgmt, String deviceLostDateTime, String mode, int greyListDuration, WebActionDb webActionDb) {
        if (greyListDuration == 0) moiService.greyListDurationIsZero(imei, mode, stolenDeviceMgmt);
        else if (greyListDuration > 0)
            moiService.greyListDurationGreaterThanZero(greyListDuration, imei, mode, stolenDeviceMgmt);
        else logger.info("GREY_LIST_DURATION tag invalid value {}", greyListDuration);

        if (moiService.isDateFormatValid(deviceLostDateTime)) {
            moiService.imeiPairDetail(deviceLostDateTime, mode, imei);
        }
        lostDeviceDetailAction(imei, stolenDeviceMgmt, mode, webActionDb);
    }

    public void lostDeviceDetailAction(String imei, StolenDeviceMgmt stolenDeviceMgmt, String mode, WebActionDb webActionDb) {
        if (mode.equalsIgnoreCase("SINGLE")) {
            try {
                StolenDeviceDetail stolenDeviceDetail = StolenDeviceDetail.builder().imei(imei).deviceModel(stolenDeviceMgmt.getDeviceModel()).deviceBrand(stolenDeviceMgmt.getDeviceBrand()).contactNumber(stolenDeviceMgmt.getContactNumber()).requestId(stolenDeviceMgmt.getRequestId()).status("Done").requestType("Stolen").build();
                StolenDeviceDetail save = moiService.save(stolenDeviceDetail, stolenDeviceDetailRepository::save);
                if (save != null) {
                    logger.info("Save operation for imei {} in stolen_device_detail", imei);
                } else {
                    logger.info("Failed to save record for imei {} in stolen_device_detail", imei);
                }
            } catch (DataIntegrityViolationException e) {
                logger.info("IMEI {} already exist in stolen_device_detail", e.getMessage());
                ExceptionModel exceptionModel = ExceptionModel.builder()
                        .error(e.getMessage())
                        .transactionId(stolenDeviceMgmt.getRequestId())
                        .subFeature(webActionDb.getSubFeature())
                        .build();
                moiService.exception(exceptionModel);

            }

        }
        if (mode.equalsIgnoreCase("BULK")) {
            if (stolenDeviceDetailRepository.updateStatus("Done", imei) > 0) {
                logger.info("Record updated for imei {} in stolen_device_detail", imei);
            } else {
                logger.info("Failed to update record for imei {} in stolen_device_detail", imei);
            }
        }
    }
}