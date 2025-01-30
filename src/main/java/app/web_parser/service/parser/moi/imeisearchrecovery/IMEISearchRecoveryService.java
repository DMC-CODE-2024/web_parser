package app.web_parser.service.parser.moi.imeisearchrecovery;

import app.web_parser.config.DbConfigService;
import app.web_parser.model.app.SearchImeiDetailByPolice;
import app.web_parser.model.app.StolenDeviceDetail;
import app.web_parser.model.app.StolenDeviceMgmt;
import app.web_parser.model.app.WebActionDb;
import app.web_parser.repository.app.StolenDeviceMgmtRepository;
import app.web_parser.repository.app.SearchImeiDetailByPoliceRepository;
import app.web_parser.repository.app.WebActionDbRepository;
import app.web_parser.service.parser.moi.utility.ExceptionModel;
import app.web_parser.service.parser.moi.utility.IMEISeriesModel;
import app.web_parser.service.parser.moi.utility.MOIService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@RequiredArgsConstructor
public class IMEISearchRecoveryService {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final MOIService moiService;
    private final WebActionDbRepository webActionDbRepository;
    private final StolenDeviceMgmtRepository stolenDeviceMgmtRepository;
    public static Map<String, String> requestIdMap = new HashMap<>();
    private final SearchImeiDetailByPoliceRepository searchImeiDetailByPoliceRepository;
    private final DbConfigService dbConfigService;
    public boolean isBrandAndModelGenuine(WebActionDb webActionDb, IMEISeriesModel imeiSeriesModel, String transactionId) {
        List<String> list = moiService.tacList(imeiSeriesModel);
        if (list.isEmpty()) {
            return false;
        }
        if (!moiService.isBrandAndModelValid(list)) {
            moiService.updateStatusAndCountFoundInLost("Fail", 0, transactionId, dbConfigService.getValue("device_mismatch_error"));
            webActionDbRepository.updateWebActionStatus(5, webActionDb.getId());
            return false;
        }
        return true;
    }

    public void isLostDeviceDetailEmpty(WebActionDb webActionDb, String transactionId, int count) {
        logger.info("No record found for txn ID {} in stolen_device_detail", transactionId);
        moiService.updateStatusAndCountFoundInLost("Done", count, transactionId, dbConfigService.getValue("imei_not_found"));
        webActionDbRepository.updateWebActionStatus(4, webActionDb.getId());

    }

    public void isCopiedRecordLostDeviceMgmtToSearchIMEIDetailByPolice(WebActionDb webActionDb, StolenDeviceMgmt lostDeviceMgmt, String requestId, String mode, String[] split, PrintWriter printWriter, String txnId, String imei, List<String> imeiList) {
        logger.info("requestIdMap {}", requestIdMap);
        String deviceLostDateTime = lostDeviceMgmt.getDeviceLostDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime lostDateTime = LocalDateTime.parse(deviceLostDateTime, formatter);
        String contactNumber = lostDeviceMgmt.getDeviceOwnerNationality().equalsIgnoreCase("1") ?  lostDeviceMgmt.getDeviceOwnerEmail() : lostDeviceMgmt.getContactNumberForOtp();
        SearchImeiDetailByPolice searchImeiDetailByPolice = SearchImeiDetailByPolice.builder().imei(imei).lostDateTime(lostDateTime).createdBy(lostDeviceMgmt.getCreatedBy()).transactionId(txnId).requestId(lostDeviceMgmt.getRequestId()).deviceOwnerName(lostDeviceMgmt.getDeviceOwnerName()).deviceOwnerAddress(lostDeviceMgmt.getDeviceOwnerAddress()).contactNumber(contactNumber).deviceOwnerNationalId(lostDeviceMgmt.getDeviceOwnerNationalID()).deviceLostPoliceStation(lostDeviceMgmt.getPoliceStation()).requestMode(mode).build();
        try {
            logger.info("---------- SearchImeiDetailByPolice payload ---------- {}", searchImeiDetailByPolice);
            searchImeiDetailByPoliceRepository.save(searchImeiDetailByPolice);
            logger.info("Record saved in search_imei_detail_by_police");
            if (mode.equalsIgnoreCase("BULK")) {
                logger.info("---- BULK REQUEST ----");
                if (requestIdMap.get(requestId) == null) {
                    requestIdMap.put(requestId, lostDeviceMgmt.getRequestId());
                    printWriter.println(moiService.joiner(split, "," + dbConfigService.getValue("imei_found") + ""));
                }
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
        } catch (Exception e) {
            logger.info("exception occurred {}", e.getMessage());
            ExceptionModel exceptionModel = ExceptionModel.builder()
                    .error(e.getMessage())
                    .transactionId(requestId)
                    .subFeature(webActionDb.getSubFeature())
                    .build();
            moiService.exception(exceptionModel);
        }
    }

    public int actionAtRecord(IMEISeriesModel imeiSeriesModel, WebActionDb webActionDb, String transactionId, PrintWriter printWriter, String mode, String[] split) {
        int successCountInIMEISearchRecoveryService = 0;

        List<String> imeiList = moiService.imeiSeries.apply(imeiSeriesModel);
        if (!imeiList.isEmpty()) {
            try {
                for (String imei : imeiList) {
                    Optional<StolenDeviceDetail> lostDeviceDetailOptional = moiService.findByImeiAndStatusIgnoreCaseAndRequestTypeIgnoreCaseIn(imei);
                    if (lostDeviceDetailOptional.isPresent()) {
                        StolenDeviceDetail lostDeviceDetail = lostDeviceDetailOptional.get();
                        logger.info("StolenDeviceDetail response based {} on IMEI  {}", imei, lostDeviceDetail);
                        String requestId = lostDeviceDetail.getRequestId();
                        Optional<StolenDeviceMgmt> byRequestId = stolenDeviceMgmtRepository.findByRequestId(requestId);
                        if (byRequestId.isPresent()) {
                            ++successCountInIMEISearchRecoveryService;
                            StolenDeviceMgmt lostDeviceMgmt = byRequestId.get();
                            logger.info("StolenDeviceMgmt response {}", lostDeviceMgmt);
                            isCopiedRecordLostDeviceMgmtToSearchIMEIDetailByPolice(webActionDb, lostDeviceMgmt, requestId, mode, split, printWriter, transactionId, imei, imeiList);
                        }
                    } else {
                        logger.info("No record found for IMEI {} in StolenDeviceDetail", imei);
                        if (mode.equalsIgnoreCase("BULK")) {
                            printWriter.println(moiService.joiner(split, "," + dbConfigService.getValue("imei_not_found") + ""));
                            break;
                        }
                    }
                }
                updateStatus(mode, transactionId, successCountInIMEISearchRecoveryService, split, printWriter);
            } catch (DataAccessException e) {
                Throwable rootCause = e.getMostSpecificCause();
                if (rootCause instanceof SQLException) {
                    SQLException sqlException = (SQLException) rootCause;
                    if (sqlException.getErrorCode() == 1146) {
                        ExceptionModel exceptionModel = ExceptionModel.builder()
                                .error(moiService.extractTableNameFromMessage(sqlException.getMessage()))
                                .transactionId(null)
                                .subFeature(webActionDb.getSubFeature())
                                .build();
                        logger.info("{} table not found", moiService.extractTableNameFromMessage(sqlException.getMessage()));
                        moiService.exception(exceptionModel);
                    }
                }
            } catch (RuntimeException e) {
                moiService.updateStatusAndCountFoundInLost("Fail", 0, transactionId, dbConfigService.getValue("error_msg"));
                webActionDbRepository.updateWebActionStatus(5, webActionDb.getId());
                ExceptionModel exceptionModel = ExceptionModel.builder()
                        .error(e.getMessage())
                        .transactionId(transactionId)
                        .subFeature(webActionDb.getSubFeature())
                        .build();
                moiService.exception(exceptionModel);
            }
        }
        logger.info("successCountInIMEISearchRecoveryService {}", successCountInIMEISearchRecoveryService);
        return successCountInIMEISearchRecoveryService;
    }

    public void updateStatus(String mode, String transactionId, int count, String[] split, PrintWriter printWriter) {
        switch (mode) {
            case "Single" -> {
                if (count == 0) {
                    moiService.updateStatusAndCountFoundInLost("Done", 0, transactionId,dbConfigService.getValue("imei_not_found"));
                    logger.info("No IMEI found for Txn ID {}", transactionId);
                } else if (count > 0) {
                    moiService.updateCountFoundInLostAndRecordCount("Done", count, transactionId, null, 1);
                    logger.info("Updated record with count_found_in _lost as {} for Txn ID {}", count, transactionId);
                }
            }
        }
    }
}
