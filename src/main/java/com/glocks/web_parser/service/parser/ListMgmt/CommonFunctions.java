package com.glocks.web_parser.service.parser.ListMgmt;


import com.glocks.web_parser.config.AppConfig;
import com.glocks.web_parser.constants.ConfigFlag;
import com.glocks.web_parser.model.app.ListDataMgmt;
import com.glocks.web_parser.model.app.WebActionDb;
import com.glocks.web_parser.repository.app.ListDataMgmtRepository;
import com.glocks.web_parser.repository.app.WebActionDbRepository;
import com.glocks.web_parser.service.operatorSeries.OperatorSeriesService;
import com.glocks.web_parser.validator.Validation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class CommonFunctions {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Autowired
    WebActionDbRepository webActionDbRepository;
    @Autowired
    Validation validation;
    @Autowired
    ListDataMgmtRepository listDataMgmtRepository;
    @Autowired
    AppConfig appConfig;

    @Autowired
    OperatorSeriesService operatorSeriesService;


    public void updateFailStatus(WebActionDb webActionDb, ListDataMgmt listDataMgmt) {
        boolean commonStorage = appConfig.isCommonStorage();
        logger.info("isCommonStorage available : {}", commonStorage);
        if (commonStorage && Objects.nonNull(listDataMgmt.getTransactionId())) {
            webActionDbRepository.updateWebActionStatus(5, webActionDb.getId());
            listDataMgmtRepository.updateListDataMgmtStatus("Fail", LocalDateTime.now(), listDataMgmt.getId());
        }
//        alertService.raiseAnAlert(alertId, type, fileName, 0);
    }

    public void updateFailStatus(WebActionDb webActionDb, ListDataMgmt listDataMgmt, long totalCount,
                                 long successCount, long failureCount) {
        webActionDbRepository.updateWebActionStatus(5, webActionDb.getId());
        listDataMgmtRepository.updateListDataMgmtStatus("Fail", LocalDateTime.now(), listDataMgmt.getId(),
                totalCount, successCount, failureCount);
//        alertService.raiseAnAlert(alertId, type, fileName, 0);
    }

    public void updateSuccessStatus(WebActionDb webActionDb, ListDataMgmt listDataMgmt) {
        webActionDbRepository.updateWebActionStatus(4, webActionDb.getId());
        listDataMgmtRepository.updateListDataMgmtStatus("Done", LocalDateTime.now(), listDataMgmt.getId());
    }

    public void updateSuccessStatus(WebActionDb webActionDb, ListDataMgmt listDataMgmt, long totalCount,
                                    long successCount, long failureCount) {
        webActionDbRepository.updateWebActionStatus(4, webActionDb.getId());
        listDataMgmtRepository.updateListDataMgmtStatus("Done", LocalDateTime.now(),
                listDataMgmt.getId(), totalCount, successCount, failureCount);
    }

    public String validateEntry(String imsi, String imei, String msisdn, String[] msisdnPrefix, String[] imsiPrefix) {

        boolean imsiEmpty = validation.isEmptyAndNull(imsi);
        boolean msisdnEmpty = validation.isEmptyAndNull(msisdn);
        boolean imeiEmpty = validation.isEmptyAndNull(imei);

        if (imsiEmpty && msisdnEmpty && imeiEmpty) {
            return ConfigFlag.msgForNullIMEIIMSIMSISDNInList.name();
        }
        if (!imeiEmpty) {
            if (validation.isLengthMore(imei, 16)) {  //validation.isLengthLess(imei, 1 4) ||  //2307
                return ConfigFlag.msgForLengthValidationIMEIInList.name();
            }
            if (!validation.isAlphaNumeric(imei)) {
                return ConfigFlag.msgForNonNumericIMEIInList.name();
            }
        }
        if (!imsiEmpty) {
            for (int i = 0; i < imsiPrefix.length; i++) {

                if (!imsi.startsWith(imsiPrefix[i])) {
                    return ConfigFlag.msgForPrefixIMSIInList.name();
                }
//                if(imsi.isBlank() || !imsi.matches("\\d+")) {
//                    flagImsiNull = true;
//                }
            }
//            if(!validation.isPrefix(imsi, "456")) {
//                return "IMSI does not starts with prefix 456";
//            }
            if (!validation.isNumeric(imsi)) {
                return ConfigFlag.msgForNonNumericIMSIInList.name();
            }
        }
        if (!msisdnEmpty) {
            for (int i = 0; i < msisdnPrefix.length; i++) {

                if (!msisdn.startsWith(msisdnPrefix[i])) {
                    return ConfigFlag.msgForPrefixMSISDNInList.name();
                }
//                if(imsi.isBlank() || !imsi.matches("\\d+")) {
//                    flagImsiNull = true;
//                }
            }
//            if(!validation.isPrefix(msisdn, "855")) {
//                return "MSISDN does not starts with prefix 855";
//            }
            if (!validation.isNumeric(msisdn)) {
                return ConfigFlag.msgForNonNumericMSISDNInList.name();
            }
            if (!operatorSeriesService.validLengthMsisdn(msisdn)) {
                return ConfigFlag.msgForLengthValidationMSISDNInList.name();
            }

        }
        return "";
    }

    public String validateEntry(String tac) {
        boolean tacEmpty = validation.isEmptyAndNull(tac);
        if (tacEmpty) {
            return ConfigFlag.msgForNullTACInList.name();
        } else {

            if (!validation.isLengthEqual(tac, 8)) {
                return ConfigFlag.msgForLengthValidationTACInList.name();
            }
            if (!validation.isNumeric(tac)) {
                return ConfigFlag.msgForNonNumericTACInList.name();
            }
        }
        return "";
    }

}
