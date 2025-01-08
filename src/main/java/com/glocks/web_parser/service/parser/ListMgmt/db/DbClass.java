package com.glocks.web_parser.service.parser.ListMgmt.db;

import com.glocks.web_parser.model.app.BlackList;
import com.glocks.web_parser.model.app.BlockedTacList;
import com.glocks.web_parser.model.app.ExceptionList;
import com.glocks.web_parser.model.app.GreyList;
import com.glocks.web_parser.repository.app.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DbClass {
    private final Logger logger = LogManager.getLogger(this.getClass());
    @Autowired
    BlackListRepository blackListRepository;

    @Autowired
    ExceptionListRepository exceptionListRepository;

    @Autowired
    BlockedTacListRepository blockedTacListRepository;

    @Autowired
    GreyListRepository greyListRepository;

    @Autowired
    ExceptionListHisRepository exceptionListHisRepository;

    @Autowired
    BlackListHisRepository blackListHisRepository;

    @Autowired
    GreyListHisRepository greyListHisRepository;

    public GreyList getGreyListEntry(boolean imsiEmpty, boolean imeiEmpty, String imei,
                                     String imsi) {
        imei = getIMEI(imei);
        GreyList greyList = null;
        if (!imeiEmpty && !imsiEmpty) {  // both imei and imsi is present
            greyList = greyListRepository.findGreyListByImeiAndImsi(imei, imsi);
        } else if (!imeiEmpty && imsiEmpty) { // imsi is empty
            greyList = greyListRepository.findGreyListByImei(imei);
        } else if (imeiEmpty && !imsiEmpty) { // imei is empty
            greyList = greyListRepository.findGreyListByImsi(imsi);
        }
        return greyList;

    }

    public BlackList getBlackListEntry(boolean imsiEmpty, boolean imeiEmpty, String imei,
                                       String imsi) {
        imei = getIMEI(imei);
        BlackList blackList = null;
        if (!imeiEmpty && !imsiEmpty) {  // both imei and imsi is present
            blackList = blackListRepository.findBlackListByImeiAndImsi(imei, imsi);
        } else if (!imeiEmpty && imsiEmpty) { // imsi is empty
            blackList = blackListRepository.findBlackListByImei(imei);
        } else if (imeiEmpty && !imsiEmpty) { // imei is empty
            blackList = blackListRepository.findBlackListByImsi(imsi);
        }
        return blackList;

    }

    public ExceptionList getExceptionListEntry(boolean imsiEmpty, boolean imeiEmpty, String imei,
                                               String imsi) {
/*

        when u match, match the value. if any value is missing, then consider that value as null while search

        so if imei, imsi is given, the combination is imei, imsi, null (for msisdn) and if new request only has same imei,
        then the combination is imei, null(for imsi), null (for msisdn),, if we match this,
                the this will not match and entry will happen
*/

                imei = getIMEI(imei);
        ExceptionList exceptionList = null;
        if (!imeiEmpty && !imsiEmpty) { // imei and imsi is present
            exceptionList = exceptionListRepository.findExceptionListByImeiAndImsi(imei, imsi);
        } else if (!imeiEmpty && imsiEmpty) { // imsi is missing
            exceptionList = exceptionListRepository.findExceptionListByImei(imei);
        } else if (imeiEmpty && !imsiEmpty) { // imei is missing
            exceptionList = exceptionListRepository.findExceptionListByImsi(imsi);
        }
        return exceptionList;
    }

    public BlockedTacList getBlockedTacEntry(boolean tacEmpty, String tac) {
        BlockedTacList blockedTacList = null;
        if (!tacEmpty) {
            blockedTacList = blockedTacListRepository.findBlockedTacListByTac(tac);
        }
        return blockedTacList;
    }


    public String getIMEI(String imei) {
        return Stream.of(imei).filter(Objects::nonNull).map(x -> imei.substring(0, 14)).collect(Collectors.joining());
    }

    public String remove(String source) {
        if (Objects.nonNull(source)) {
            return Arrays.stream(source.split(",")).filter(element -> !element.equals("EIRSAdmin")).collect(Collectors.joining(","));
        } else logger.info("No source value {} found", source);
        return null;
    }

    public void updateSource(String source, String imei, String repo) {
        logger.info("Updated {} with source {} for imei {}", repo, source, imei);
        int rowAffected = 0;
        switch (repo) {
            case "BLACK_LIST" -> rowAffected = blackListRepository.updateSource(source, imei);
            case "BLACK_LIST_HIS" -> rowAffected = blackListHisRepository.updateSource(source, imei);
  /*          case "GREY_LIST" -> rowAffected = greyListRepository.updateSource(source, imei);
            case "GREY_LIST_HIS" -> rowAffected = greyListHisRepository.updateSource(source, imei);*/
            case "EXCEPTION_LIST" -> rowAffected = exceptionListRepository.updateSource(source, imei);
            case "EXCEPTION_LIST_HIS" -> rowAffected = exceptionListHisRepository.updateSource(source, imei);
        }
        if (rowAffected == 1) {
            logger.info("Removed EIRSAdmin and updated source value for {}", repo);
        } else {
            logger.info("Failed to update source value for {}", repo);
        }
    }


}
