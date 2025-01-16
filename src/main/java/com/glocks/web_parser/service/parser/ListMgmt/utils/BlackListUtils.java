package com.glocks.web_parser.service.parser.ListMgmt.utils;


import com.glocks.web_parser.builder.BlackListBuilder;
import com.glocks.web_parser.builder.BlackListHisBuilder;
import com.glocks.web_parser.config.AppConfig;
import com.glocks.web_parser.config.DbConfigService;
import com.glocks.web_parser.dto.ListMgmtDto;
import com.glocks.web_parser.model.app.*;
import com.glocks.web_parser.repository.app.BlackListHisRepository;
import com.glocks.web_parser.repository.app.BlackListRepository;
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

@Service
public class BlackListUtils {

    @Autowired
    Validation validation;

    @Autowired
    ListDataMgmtRepository listDataMgmtRepository;
    @Autowired
    AppConfig appConfig;
    @Autowired
    HlrService hlrService;
    @Autowired
    BlackListRepository blackListRepository;
    @Autowired
    BlackListHisRepository blackListHisRepository;
    @Autowired
    DbConfigService dbConfigService;
    @Autowired
    OperatorSeriesService operatorSeriesService;
    @Autowired
    DbClass dbClass;
    private final Logger logger = LogManager.getLogger(this.getClass());

    public boolean processBlackSingleAddEntry(ListDataMgmt listDataMgmt, ListMgmtDto record, int type, PrintWriter writer) {
        String imsi = type == 1 ? listDataMgmt.getImsi() : record.getImsi().trim();
        String imei = type == 1 ? listDataMgmt.getImei() : record.getImei();
        String msisdn = type == 1 ? listDataMgmt.getMsisdn() : record.getMsisdn();
        boolean imsiEmpty = validation.isEmptyAndNull(imsi);
        boolean msisdnEmpty = validation.isEmptyAndNull(msisdn);
        boolean imeiEmpty = validation.isEmptyAndNull(imei);
        boolean filled = true;
//        String populatedImsi = "";
        try {
            // search in list if already exists or not.
            if (!imsiEmpty) imsi = imsi.trim();
            if (!imeiEmpty) imei = imei.trim();
            if (!msisdnEmpty) msisdn = msisdn.trim();
            if (imsiEmpty && !msisdnEmpty) {
                imsi = hlrService.popluateImsi(msisdn);
                if (validation.isEmptyAndNull(imsi)) {
                    logger.error("The entry is failed.");
                    writer.println((msisdnEmpty ? "" : msisdn) + "," + (imsiEmpty ? "" : imsi) + "," + (imeiEmpty ? "" : imei) + "," +
                            dbConfigService.getValue("msgForEntryFailedInBlackList"));
                    return false;
                }
                filled = true;
                imsiEmpty = false;
            }

            BlackList blackList = dbClass.getBlackListEntry(imsiEmpty, imeiEmpty, imei, imsi);
            // if present write in file and exit.
            if (blackList != null) {
                logger.info("The entry already exists {}", blackList);
                String source = dbClass.remove(blackList.getSource());
                if (!source.equalsIgnoreCase("EIRSAdmin")) {
                    dbClass.updateSource(source + ",EIRSAdmin", blackList.getImei(), "BLACK_LIST");
                 //   dbClass.updateSource(source + ",EIRSAdmin", blackList.getImei(), "BLACK_LIST_HIS");
                }
//                writer.println(msisdn+","+imsi+","+imei+","+"ALREADY_EXIST");
                writer.println((msisdnEmpty ? "" : msisdn) + "," + (imsiEmpty ? "" : imsi) + "," + (imeiEmpty ? "" : imei) + "," + dbConfigService.getValue("msgForAlreadyExistsInBlackList"));


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
                // check if imsi is already present in exception list and grey list then don't add.
                ExceptionList exceptionList = dbClass.getExceptionListEntry(imsiEmpty, imeiEmpty, imei, imsi);

                if (exceptionList != null) {
                    logger.info("The entry already exists in exception list {}", exceptionList);
                    writer.println((msisdnEmpty ? "" : msisdn) + "," + (imsiEmpty ? "" : imsi) + "," + (imeiEmpty ? "" : imei) + "," + dbConfigService.getValue("msgForAlreadyExistsInExceptionList"));
                    return false;
                }
                GreyList greyList = dbClass.getGreyListEntry(imsiEmpty, imeiEmpty, imei, imsi);
                if (greyList != null) {
                    logger.info("The entry already exists in grey list {}", greyList);
                    writer.println((msisdnEmpty ? "" : msisdn) + "," + (imsiEmpty ? "" : imsi) + "," + (imeiEmpty ? "" : imei) + "," + dbConfigService.getValue("msgForAlreadyExistsInGreyList"));
                    return false;
                }
                blackList = type == 1 ? BlackListBuilder.forInsert(listDataMgmt, operatorName) : BlackListBuilder.forInsert(listDataMgmt, record, operatorName);

                logger.info("Entry save in black list {}", blackList);
                blackListRepository.save(blackList);
                BlackListHis blackListHisEntity = BlackListHisBuilder.forInsert(blackList, 1, listDataMgmt);
                logger.info("Entry save in black list his {}", blackListHisEntity);
                blackListHisRepository.save(blackListHisEntity);
//                writer.println(msisdn+","+imsi+","+imei+","+"ADDED");
                writer.println((msisdnEmpty ? "" : msisdn) + "," + (imsiEmpty ? "" : imsi) + "," + (imeiEmpty ? "" : imei) + "," + dbConfigService.getValue("msgForAddedInExceptionList"));

            }
            return true;
        } catch (Exception ex) {
            logger.error("Error while processing the entry for black list, for request {} and action {}, message {}",
                    listDataMgmt.getRequestType(), listDataMgmt.getAction(), ex.getMessage());
//            writer.println(msisdn+","+imsi+","+imei+","+"ENTRY_FAILED");
            writer.println((msisdnEmpty ? "" : msisdn) + "," + (imsiEmpty ? "" : imsi) + "," + (imeiEmpty ? "" : imei) + "," + dbConfigService.getValue("msgForEntryFailedInBlackList"));

            return false;
        }
    }

    public boolean processBlackSingleDelEntry(ListDataMgmt listDataMgmt, ListMgmtDto record, int type, PrintWriter writer) {
        String imsi = type == 1 ? listDataMgmt.getImsi() : record.getImsi();
        String imei = type == 1 ? listDataMgmt.getImei() : record.getImei();
        String msisdn = type == 1 ? listDataMgmt.getMsisdn() : record.getMsisdn();
        boolean imsiEmpty = validation.isEmptyAndNull(imsi);
        boolean msisdnEmpty = validation.isEmptyAndNull(msisdn);
        boolean imeiEmpty = validation.isEmptyAndNull(imei);
        boolean filled = false;
//        String populatedImsi = "";
        try {
            // search in list if already exists or not.
            if (!imsiEmpty) imsi = imsi.trim();
            if (!imeiEmpty) imei = imei.trim();
            if (!msisdnEmpty) msisdn = msisdn.trim();
            // check if imsi is populated in case of only msisdn in request.
            if (imsiEmpty && !msisdnEmpty) {
                imsi = hlrService.popluateImsi(msisdn);
                if (validation.isEmptyAndNull(imsi)) {
                    logger.error("The entry is failed.");
                    writer.println((msisdnEmpty ? "" : msisdn) + "," + (imsiEmpty ? "" : imsi) + "," + (imeiEmpty ? "" : imei) + "," +
                            dbConfigService.getValue("msgForEntryFailedInBlackList"));
                    return false;
                }
                filled = true;
                imsiEmpty = false;
            }

            BlackList blackList = dbClass.getBlackListEntry(imsiEmpty, imeiEmpty, imei, imsi);
            // if present write in file and exit.
            if (blackList != null) {
                logger.info("The entry already exists {}", blackList);
                String operatorName = operatorSeriesService.getOperatorName(imsiEmpty, msisdnEmpty, imsi, msisdn);
                logger.info("Entry deleted in black list {}", blackList);
                blackListRepository.delete(blackList);
                BlackListHis blackListHisEntity = BlackListHisBuilder.forInsert(blackList, 0, listDataMgmt);
                logger.info("Entry save in black list his {}", blackListHisEntity);
                blackListHisRepository.save(blackListHisEntity);
//                writer.println(msisdn + "," + imsi + "," + imei + "," + "DELETED");
                writer.println((msisdnEmpty ? "" : msisdn) + "," + (imsiEmpty ? "" : imsi) + "," + (imeiEmpty ? "" : imei) + "," + dbConfigService.getValue("msgForDeletedInBlackList"));

            }
            // if present write in file and exit
            else {
                logger.info("The entry for msisdn {}, imsi {} and imei {} does not exist.", msisdn, imsi, imei);
//                writer.println(msisdn + "," + imsi + "," + imei + "," + "NOT_EXIST");
                writer.println((msisdnEmpty ? "" : msisdn) + "," + (imsiEmpty ? "" : imsi) + "," + (imeiEmpty ? "" : imei) + "," + dbConfigService.getValue("msgForNotExistsInBlackList"));

            }
            return true;
        } catch (Exception ex) {
            logger.error("Error while processing the entry for black list, for request {} and action {}, message {}",
                    listDataMgmt.getRequestType(), listDataMgmt.getAction(), ex.getMessage());
//            writer.println(msisdn+","+imsi+","+imei+","+"ENTRY_FAILED");
            writer.println((msisdnEmpty ? "" : msisdn) + "," + (imsiEmpty ? "" : imsi) + "," + (imeiEmpty ? "" : imei) + "," + dbConfigService.getValue("msgForEntryFailedInBlackList"));

            return false;

        }
    }


}
