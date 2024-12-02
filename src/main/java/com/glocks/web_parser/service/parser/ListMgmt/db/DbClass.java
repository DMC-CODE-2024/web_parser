package com.glocks.web_parser.service.parser.ListMgmt.db;

import com.glocks.web_parser.model.app.BlackList;
import com.glocks.web_parser.model.app.BlockedTacList;
import com.glocks.web_parser.model.app.ExceptionList;
import com.glocks.web_parser.model.app.GreyList;
import com.glocks.web_parser.repository.app.BlackListRepository;
import com.glocks.web_parser.repository.app.BlockedTacListRepository;
import com.glocks.web_parser.repository.app.ExceptionListRepository;
import com.glocks.web_parser.repository.app.GreyListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DbClass {

    @Autowired
    BlackListRepository blackListRepository;

    @Autowired
    ExceptionListRepository exceptionListRepository;

    @Autowired
    BlockedTacListRepository blockedTacListRepository;

    @Autowired
    GreyListRepository greyListRepository;

    public GreyList getGreyListEntry(boolean imsiEmpty, boolean imeiEmpty, String imei,
                                     String imsi) {
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

        ExceptionList exceptionList = null;
        if (!imeiEmpty && !imsiEmpty) { // imei and imsi is present
            exceptionList = exceptionListRepository.findExceptionListByImeiAndImsi(imei, imsi);
        } else if (!imeiEmpty && imsiEmpty) { // imsi is missing
            exceptionList = exceptionListRepository.findExceptionListByImei(imei);
        }
        else if (imeiEmpty && !imsiEmpty) { // imei is missing
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
}
