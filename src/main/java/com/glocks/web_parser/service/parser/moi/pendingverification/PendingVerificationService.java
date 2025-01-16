package com.glocks.web_parser.service.parser.moi.pendingverification;

import com.glocks.web_parser.config.AppConfig;
import com.glocks.web_parser.config.DbConfigService;
import com.glocks.web_parser.model.app.StolenDeviceDetail;
import com.glocks.web_parser.model.app.StolenDeviceMgmt;
import com.glocks.web_parser.model.app.WebActionDb;
import com.glocks.web_parser.repository.app.*;
import com.glocks.web_parser.service.parser.moi.utility.*;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.*;

@Component
@RequiredArgsConstructor
public class PendingVerificationService {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final AppConfig appConfig;
    private final MDRRepository mdrRepository;
    private final MOIService moiService;
    private final StolenDeviceMgmtRepository stolenDeviceMgmtRepository;
    private final StolenDeviceDetailRepository stolenDeviceDetailRepository;
    private final EirsInvalidImeiRepository eirsInvalidImeiRepository;
    private final DuplicateDeviceDetailRepository duplicateDeviceDetailRepository;
    private final WebActionDbRepository webActionDbRepository;
    private final Notification notificationForPendingVerification;
    private final DbConfigService dbConfigService;
    static int failCount = 0;
    Set<StolenDeviceDetail> set = new LinkedHashSet<>();

    public boolean pendingVerificationFileValidation(String filePath, StolenDeviceMgmt stolenDeviceMgmt, PrintWriter printWriter, String state) {
        failCount = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String record;
            String[] header;
            boolean headerSkipped = false;
            IMEISeriesModel imeiSeriesModel = new IMEISeriesModel();
            String[] split;
            while ((record = reader.readLine()) != null) {
                if (!record.isBlank()) {
                    if (!headerSkipped) {
                        header = record.split(appConfig.getMoiFileSeparator(), -1);
                        headerSkipped = true;
                        if (state.equalsIgnoreCase("VERIFICATION_STAGE_INIT"))
                            printWriter.println(moiService.joiner(header, ",Status,Reason"));
                    } else {
                        split = record.split(appConfig.getListMgmtFileSeparator(), -1);
                        imeiSeriesModel.setImeiSeries(split, "STOLEN");
                        List<String> imeiList = moiService.imeiSeries.apply(imeiSeriesModel);
                        if (!imeiList.isEmpty()) {
                            List<String> list = moiService.tacList(imeiSeriesModel);
                            if (!list.isEmpty()) {
                                if (moiService.isBrandAndModelValid(list)) {
                                    action(split, printWriter, state, imeiList, stolenDeviceMgmt, imeiSeriesModel);
                                } else {
                                    printWriter.println(moiService.joiner(split, "," + dbConfigService.getValue("state_fail") + "," + dbConfigService.getValue("gsma_non_compliant") + ""));
                                    failCount++;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            logger.error("Exception in processing the file {}", exception.getMessage());
            failCount++;
        }
        logger.info("failCount {}", failCount);
        return failCount == 0;
    }


    public void validFile(WebActionDb webActionDb, String uploadedFilePath, StolenDeviceMgmt stolenDeviceMgmt, PrintWriter printWriter, String uploadedFileName, String state) {
        try {
            this.pendingVerificationFileValidation(uploadedFilePath, stolenDeviceMgmt, printWriter, state);
            printWriter.close();
            if (!set.isEmpty()) this.payload(set);
            moiService.updateStatusInLostDeviceMgmt("VERIFY_MOI", stolenDeviceMgmt.getRequestId());
            moiService.webActionDbOperation(4, webActionDb.getId());
        } catch (DataAccessException e) {
            Throwable rootCause = e.getMostSpecificCause();
            if (rootCause instanceof SQLException) {
                SQLException sqlException = (SQLException) rootCause;
                if (sqlException.getErrorCode() == 1146) {
                    ExceptionModel exceptionModel = ExceptionModel.builder()
                            .error(moiService.extractTableNameFromMessage(sqlException.getMessage()))
                            .transactionId(null)
                            .subFeature(null)
                            .build();
                    logger.info("{} table not found", moiService.extractTableNameFromMessage(sqlException.getMessage()));
                    moiService.exception(exceptionModel);
                }
            }
        } catch (RuntimeException e) {
            ExceptionModel exceptionModel = ExceptionModel.builder()
                    .error(e.getMessage())
                    .transactionId(webActionDb.getTxnId())
                    .subFeature(webActionDb.getSubFeature())
                    .build();
            moiService.exception(exceptionModel);
        }
    }

    public void invalidFile(WebActionDb webActionDb, String uploadedFilePath, StolenDeviceMgmt stolenDeviceMgmt, PrintWriter printWriter, String uploadedFileName, String state) {
        try {
            printWriter.close();
            moiService.updateStatusInLostDeviceMgmt("Fail", stolenDeviceMgmt.getRequestId());
            moiService.webActionDbOperation(4, webActionDb.getId());
            if (Objects.nonNull(stolenDeviceMgmt.getDeviceOwnerNationality())) {
                String channel = stolenDeviceMgmt.getDeviceOwnerNationality().equalsIgnoreCase("0") ? "SMS" : "EMAIL";
                String eirsResponseParamTag = channel.equals("SMS") ? ConfigurableParameter.MOI_PENDING_VERIFICATION_MSG.getValue() : ConfigurableParameter.MOI_PENDING_VERIFICATION_MSG_EMAIL.getValue();
                notificationForPendingVerification.sendNotification(webActionDb, stolenDeviceMgmt, channel, uploadedFilePath, eirsResponseParamTag);
                logger.info("notification sent to {} mode user , 0:Cambodian 1:Non-cambodian", stolenDeviceMgmt.getDeviceOwnerNationality());
            }
        } catch (DataAccessException e) {
            Throwable rootCause = e.getMostSpecificCause();
            if (rootCause instanceof SQLException) {
                SQLException sqlException = (SQLException) rootCause;
                if (sqlException.getErrorCode() == 1146) {
                    ExceptionModel exceptionModel = ExceptionModel.builder()
                            .error(moiService.extractTableNameFromMessage(sqlException.getMessage()))
                            .transactionId(null)
                            .subFeature(null)
                            .build();
                    logger.info("{} table not found", moiService.extractTableNameFromMessage(sqlException.getMessage()));
                    moiService.exception(exceptionModel);
                }
            }
        } catch (RuntimeException e) {
            ExceptionModel exceptionModel = ExceptionModel.builder()
                    .error(e.getMessage())
                    .transactionId(webActionDb.getTxnId())
                    .subFeature(webActionDb.getSubFeature())
                    .build();
            moiService.exception(exceptionModel);
        }
    }

    public void action(String[] split, PrintWriter printWriter, String state, List<String> imeiList, StolenDeviceMgmt stolenDeviceMgmt, IMEISeriesModel imeiSeriesModel) {
        boolean isImeiListValid = true;
        for (String imei : imeiList) {
            if (!moiService.isNumericAndValid.test(imei)) {
                logger.info("Invalid IMEI {} found", imei);
                printWriter.println(moiService.joiner(split, "," + dbConfigService.getValue("state_fail") + "," + dbConfigService.getValue("invalid_imei") + ""));
            } else {
                if (state.equalsIgnoreCase("VERIFICATION_STAGE_INIT")) {
                    if (!isConditionFulfil(imei, printWriter, split)) {
                        isImeiListValid = false;
                        break;
                    }
                } else if (state.equalsIgnoreCase("VERIFICATION_STAGE_DONE")) {
                    StolenDeviceDetail lostDeviceDetail = StolenDeviceDetail.builder().imei(imei).requestId(stolenDeviceMgmt.getRequestId()).contactNumber(imeiSeriesModel.getContactNumber()).deviceBrand(imeiSeriesModel.getBrand()).deviceModel(imeiSeriesModel.getModel()).requestType(stolenDeviceMgmt.getRequestType()).status("PENDING_VERIFICATION").build();
                    set.add(lostDeviceDetail);
                }
            }
        }
        if (state.equalsIgnoreCase("VERIFICATION_STAGE_INIT")) {
            logger.info("isImeiListValid {}", isImeiListValid);
            if (isImeiListValid) printSuccessRecord(split, printWriter);
        }
    }

    public Boolean isImeiExistInStolenDeviceMgmt(String value) {
        List<String> listStatus = List.of("INIT", "VERIFY_MOI");
        return stolenDeviceMgmtRepository.existsByImeiAndStatusIn(value, listStatus) > 0;
    }


    public boolean isConditionFulfil(String imei, PrintWriter printWriter, String[] split) {
        String imeiValue = moiService.getIMEI(imei);

 /*       boolean isImeiValid = moiService.isNumericAndValid.test(imei);
        if (!isImeiValid) {
            printWriter.println(moiService.joiner(split, "," + dbConfigService.getValue("state_fail") + "," + dbConfigService.getValue("invalid_imei") + ""));
            ++failCount;
            return false;
        }

        String tacFromIMEI = moiService.getTacFromIMEI(imei);
//      Not GSMA compliant
        if (!mdrRepository.existsByDeviceId(tacFromIMEI)) {
            printWriter.println(moiService.joiner(split, "," + dbConfigService.getValue("state_fail") + "," + dbConfigService.getValue("gsma_non_compliant") + ""));

            ++failCount;
            return false;
        }*/

//     Is IMEI present in lost_device_mgmt
        logger.info("GSMA check passed for IMEI {} ✓", imei);
        if (isImeiExistInStolenDeviceMgmt(imei)) {
            printWriter.println(moiService.joiner(split, "," + dbConfigService.getValue("state_fail") + "," + dbConfigService.getValue("exist_in_lost") + ""));
            ++failCount;
            logger.info("IMEI {} found in lostDeviceMgmt", imei);
            return false;
        }

//     Is IMEI present in stolen_device_detail
        logger.info("No IMEI {} found in stolen device mgmt ✓", imei);
        if (stolenDeviceDetailRepository.existsByImei(imei)) {
            printWriter.println(moiService.joiner(split, "," + dbConfigService.getValue("state_fail") + "," + dbConfigService.getValue("exist_in_lost") + ""));
            ++failCount;
            logger.info("IMEI {} found in stolenDeviceDetail", imei);
            return false;
        }
//     Is IMEI present in eirs_invalid_imei
        logger.info("No IMEI {} found in stolen_device_detail ✓", imei);
        if (eirsInvalidImeiRepository.existsByImei(imeiValue)) {
            printWriter.println(moiService.joiner(split, "," + dbConfigService.getValue("state_fail") + "," + dbConfigService.getValue("invalid_imei") + ""));
            logger.info("IMEI {} found in eirsInvalidImei", imeiValue);
            ++failCount;
            return false;
        }
//     Is IMEI present in duplicate_device_detail
        logger.info("No IMEI {} found in eirs_invalid_imei ✓", imeiValue);
        if (duplicateDeviceDetailRepository.existsByImei(imeiValue)) {
            logger.info("IMEI {} found in duplicate_device_detail ✓", imeiValue);
            if (duplicateDeviceDetailRepository.existsByImeiAndMsisdnIsNull(imeiValue)) {
                logger.info("Phone number not provided for IMEI {}", imeiValue);
                printWriter.println(moiService.joiner(split, "," + dbConfigService.getValue("state_fail") + "," + dbConfigService.getValue("duplicate_imei") + ""));
                ++failCount;
                return false;
            } else {
                if (!duplicateDeviceDetailRepository.existsByImeiAndStatusIgnoreCaseEquals(imeiValue, "ORIGINAL")) {
                    logger.info("Phone number exist for IMEI {} but status is not equals to ORIGINAL ✓", imeiValue);
                    printWriter.println(moiService.joiner(split, "," + dbConfigService.getValue("state_fail") + "," + dbConfigService.getValue("duplicate_imei") + ""));
                    ++failCount;
                    return false;
                }
            }
        }
        return true;
    }

    public void payload(Set<StolenDeviceDetail> stolenDeviceDetailSet) {
        try {
            logger.info("stolenDeviceDetail payload after successful verification{}", stolenDeviceDetailSet);
            stolenDeviceDetailRepository.saveAll(stolenDeviceDetailSet);
        } catch (DataIntegrityViolationException e) {
            logger.info("IMEI {} already exist in stolen_device_detail", e.getMessage());
        }
    }

    public void printSuccessRecord(String[] split, PrintWriter printWriter) {
        printWriter.println(moiService.joiner(split, "," + dbConfigService.getValue("state_success") + "," + dbConfigService.getValue("imei_success") + ""));
    }
}
