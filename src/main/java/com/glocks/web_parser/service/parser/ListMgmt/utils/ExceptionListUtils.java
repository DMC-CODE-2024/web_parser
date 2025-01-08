package com.glocks.web_parser.service.parser.ListMgmt.utils;

import com.glocks.web_parser.builder.ExceptionListBuilder;
import com.glocks.web_parser.builder.ExceptionListHisBuilder;
import com.glocks.web_parser.config.AppConfig;
import com.glocks.web_parser.config.DbConfigService;
import com.glocks.web_parser.dto.ListMgmtDto;
import com.glocks.web_parser.model.app.*;
import com.glocks.web_parser.repository.app.ExceptionListHisRepository;
import com.glocks.web_parser.repository.app.ExceptionListRepository;
import com.glocks.web_parser.repository.app.ListDataMgmtRepository;
import com.glocks.web_parser.service.hlr.HlrService;
import com.glocks.web_parser.service.operatorSeries.OperatorSeriesService;
import com.glocks.web_parser.service.parser.ListMgmt.db.DbClass;
import com.glocks.web_parser.validator.Validation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.util.Objects;

@Service
public class ExceptionListUtils {

    @Autowired
    Validation validation;

    @Autowired
    ListDataMgmtRepository listDataMgmtRepository;
    @Autowired
    AppConfig appConfig;
    @Autowired
    HlrService hlrService;
    @Autowired
    ExceptionListRepository exceptionListRepository;
    @Autowired
    ExceptionListHisRepository exceptionListHisRepository;
    @Autowired
    DbConfigService dbConfigService;
    @Autowired
    OperatorSeriesService operatorSeriesService;
    @Autowired
    DbClass dbClass;

    private final Logger logger = LogManager.getLogger(this.getClass());

    public boolean processExceptionSingleAddEntry(ListDataMgmt listDataMgmt, ListMgmtDto record, int type, PrintWriter writer) {
        String imsi = type == 1 ? listDataMgmt.getImsi() : record.getImsi().trim();
        String imei = type == 1 ? listDataMgmt.getImei() : record.getImei();
        String msisdn = type == 1 ? listDataMgmt.getMsisdn() : record.getMsisdn();
        boolean imsiEmpty = validation.isEmptyAndNull(imsi);
        boolean msisdnEmpty = validation.isEmptyAndNull(msisdn);
        boolean imeiEmpty = validation.isEmptyAndNull(imei);
        boolean filled = false;

        try {
            // search in list if already exists or not.

            if (!imsiEmpty) imsi = imsi.trim();
            if (!imeiEmpty) imei = imei.trim();
            if (!msisdnEmpty) msisdn = msisdn.trim();
            if (imsiEmpty && !msisdnEmpty) {
                imsi = hlrService.popluateImsi(msisdn);
                if (validation.isEmptyAndNull(imsi)) {
                    logger.error("The entry is failed.");
                    writer.println((msisdnEmpty ? "" : msisdn) + "," + (imsiEmpty ? "" : imsi) + "," + (imeiEmpty ? "" : imei) + "," + dbConfigService.getValue("msgForEntryFailedInExceptionList"));
                    return false;
                }
                filled = true;
                imsiEmpty = false;
            }
            ExceptionList exceptionList = dbClass.getExceptionListEntry(imsiEmpty, imeiEmpty, imei, imsi);
            // if present write in file and exit.
            if (exceptionList != null) {
                logger.info("The entry already exists {}", exceptionList);
                String source = dbClass.remove(exceptionList.getSource());
                if (!source.equalsIgnoreCase("EIRSAdmin")) {
                    dbClass.updateSource(source + ",EIRSAdmin", exceptionList.getImei(), "EXCEPTION_LIST");
                   // dbClass.updateSource(source + ",EIRSAdmin", exceptionList.getImei(), "EXCEPTION_LIST_HIS");
                }
//                writer.println(msisdn+","+imsi+","+imei+","+"ALREADY_EXIST");
                writer.println((msisdnEmpty ? "" : msisdn) + "," + (imsiEmpty ? "" : imsi) + "," + (imeiEmpty ? "" : imei) + "," + dbConfigService.getValue("msgForAlreadyExistsInExceptionList"));
            }
            // if not present make entry in table
            else {
                logger.info("The entry for msisdn {}, imsi {} and imei {} does not exist.", msisdn, imsi, imei);
                String operatorName = operatorSeriesService.getOperatorName(imsiEmpty, msisdnEmpty, imsi, msisdn);
                if (validation.isEmptyAndNull(operatorName) && (!imsiEmpty || !msisdnEmpty)) { // operator name not found if imsi or msisdn is present.
                    logger.info("The operator name from operator series is not found.");
                    logger.error("The entry is failed.");
                    writer.println((msisdnEmpty ? "" : msisdn) + "," + (imsiEmpty ? "" : imsi) + "," + (imeiEmpty ? "" : imei) + "," + dbConfigService.getValue("msgForEntryFailedInBlackList"));
                    return false;
                }
                if (filled && type == 1) {
                    listDataMgmt.setImsi(imsi);
                } else if (filled && type == 0) record.setImsi(imsi);
                // search in black list or grey list is present then don't add
                BlackList blackList = dbClass.getBlackListEntry(imsiEmpty, imeiEmpty, imei, imsi);

                if (blackList != null) {
                    logger.info("The entry already exists in black list {}", blackList);
                    writer.println((msisdnEmpty ? "" : msisdn) + "," + (imsiEmpty ? "" : imsi) + "," + (imeiEmpty ? "" : imei) + "," + dbConfigService.getValue("msgForAlreadyExistsInBlackList"));
                    return false;
                }
                GreyList greyList = dbClass.getGreyListEntry(imsiEmpty, imeiEmpty, imei, imsi);
                if (greyList != null) {
                    logger.info("The entry already exists in grey list {}", greyList);
                    writer.println((msisdnEmpty ? "" : msisdn) + "," + (imsiEmpty ? "" : imsi) + "," + (imeiEmpty ? "" : imei) + "," + dbConfigService.getValue("msgForAlreadyExistsInGreyList"));
                    return false;
                }
                exceptionList = type == 1 ? ExceptionListBuilder.forInsert(listDataMgmt, operatorName) : ExceptionListBuilder.forInsert(listDataMgmt, record, operatorName);
                logger.info("Entry save in exception list {}", exceptionList);
                exceptionListRepository.save(exceptionList);
                ExceptionListHis exceptionListHisEntity = ExceptionListHisBuilder.forInsert(exceptionList, 1, listDataMgmt);
                logger.info("Entry save in exception list his {}", exceptionListHisEntity);
                exceptionListHisRepository.save(exceptionListHisEntity);
//                writer.println(msisdn+","+imsi+","+imei+","+"ADDED");
                writer.println((msisdnEmpty ? "" : msisdn) + "," + (imsiEmpty ? "" : imsi) + "," + (imeiEmpty ? "" : imei) + "," + dbConfigService.getValue("msgForAddedInExceptionList"));
            }
            return true;
        } catch (Exception ex) {
            logger.error("Error while processing the entry for exception list, for request {} and action {}, message {}", listDataMgmt.getRequestType(), listDataMgmt.getAction(), ex.getMessage());
//            writer.println(msisdn+","+imsi+","+imei+","+"ENTRY_FAILED");
            writer.println((msisdnEmpty ? "" : msisdn) + "," + (imsiEmpty ? "" : imsi) + "," + (imeiEmpty ? "" : imei) + "," + dbConfigService.getValue("msgForEntryFailedInExceptionList"));

            return false;
        }
    }

    public boolean processExceptionSingleDelEntry(ListDataMgmt listDataMgmt, ListMgmtDto record, int type, PrintWriter writer) {
        String imsi = type == 1 ? listDataMgmt.getImsi() : record.getImsi().trim();
        String imei = type == 1 ? listDataMgmt.getImei() : record.getImei();
        String msisdn = type == 1 ? listDataMgmt.getMsisdn() : record.getMsisdn();
        boolean imsiEmpty = validation.isEmptyAndNull(imsi);
        boolean msisdnEmpty = validation.isEmptyAndNull(msisdn);
        boolean imeiEmpty = validation.isEmptyAndNull(imei);
        boolean filled = false;

        try {
            // search in list if already exists or not.

            if (!imsiEmpty) imsi = imsi.trim();
            if (!imeiEmpty) imei = imei.trim();
            if (!msisdnEmpty) msisdn = msisdn.trim();
            if (imsiEmpty && !msisdnEmpty) {
                imsi = hlrService.popluateImsi(msisdn);
                if (validation.isEmptyAndNull(imsi)) {
                    logger.error("The entry is failed.");
                    writer.println((msisdnEmpty ? "" : msisdn) + "," + (imsiEmpty ? "" : imsi) + "," + (imeiEmpty ? "" : imei) + "," + dbConfigService.getValue("msgForEntryFailedInExceptionList"));
//                    return false;
                }
                filled = true;
                imsiEmpty = false;

            }
//            getExceptionListEntry()
            ExceptionList exceptionList = dbClass.getExceptionListEntry(imsiEmpty, imeiEmpty, imei, imsi);
            // if present write in file and exit.
            if (exceptionList != null) {
                logger.info("The entry exists {}", exceptionList);
                exceptionListRepository.delete(exceptionList);
                logger.info("Entry deleted in exception list {}", exceptionList);
                ExceptionListHis exceptionListHisEntity = ExceptionListHisBuilder.forInsert(exceptionList, 0, listDataMgmt);
                logger.info("Entry save in exception list his {}", exceptionListHisEntity);
                exceptionListHisRepository.save(exceptionListHisEntity);
//                writer.println(msisdn + "," + imsi + "," + imei + "," + "DELETED");
                writer.println((msisdnEmpty ? "" : msisdn) + "," + (imsiEmpty ? "" : imsi) + "," + (imeiEmpty ? "" : imei) + "," + dbConfigService.getValue("msgForDeletedInExceptionList"));
            }
            // if present write in file and exit
            else {
                logger.info("The entry for msisdn {}, imsi {} and imei {} does not exist.", msisdn, imsi, imei);
//                writer.println(msisdn + "," + imsi + "," + imei + "," + "NOT_EXIST");
                writer.println((msisdnEmpty ? "" : msisdn) + "," + (imsiEmpty ? "" : imsi) + "," + (imeiEmpty ? "" : imei) + "," + dbConfigService.getValue("msgForNotExistsInExceptionList"));


            }
            return true;
        } catch (Exception ex) {
            logger.error("Error while processing the entry for exception list, for request {} and action {}, message {}", listDataMgmt.getRequestType(), listDataMgmt.getAction(), ex.getMessage());
//            writer.println(msisdn+","+imsi+","+imei+","+"ENTRY_FAILED");
            writer.println((msisdnEmpty ? "" : msisdn) + "," + (imsiEmpty ? "" : imsi) + "," + (imeiEmpty ? "" : imei) + "," + dbConfigService.getValue("msgForEntryFailedInExceptionList"));

            return false;

        }
    }


}
