package com.glocks.web_parser.service.parser.ListMgmt.utils;


import com.glocks.web_parser.builder.BlockedTacListBuilder;
import com.glocks.web_parser.builder.BlockedTacListHisBuilder;
import com.glocks.web_parser.config.DbConfigService;
import com.glocks.web_parser.dto.BlockedTacDto;
import com.glocks.web_parser.model.app.BlockedTacList;
import com.glocks.web_parser.model.app.BlockedTacListHis;
import com.glocks.web_parser.model.app.ListDataMgmt;
import com.glocks.web_parser.repository.app.BlockedTacListHisRepository;
import com.glocks.web_parser.repository.app.BlockedTacListRepository;
import com.glocks.web_parser.service.parser.ListMgmt.db.DbClass;
import com.glocks.web_parser.validator.Validation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;

@Service
public class BlockedTacUtils {

    @Autowired
    Validation validation;
    @Autowired
    BlockedTacListRepository blockedTacListRepository;
    @Autowired
    BlockedTacListHisRepository blockedTacListHisRepository;
    @Autowired
    DbConfigService dbConfigService;
    @Autowired
    DbClass dbClass;
    private final Logger logger = LogManager.getLogger(this.getClass());
    public boolean processBlockedTacDelEntry(ListDataMgmt listDataMgmt, BlockedTacDto record, int type, PrintWriter writer) {
        String tac = type == 1 ? listDataMgmt.getTac() : record.getTac();
        boolean tacEmpty = validation.isEmptyAndNull(tac);
        try {
            // search in list if already exists or not.
            if(!tacEmpty) tac = tac.trim();
            BlockedTacList blockedTacList = dbClass.getBlockedTacEntry(tacEmpty, tac);
            // if present write in file and exit.
            if (blockedTacList != null) {
                logger.info("The entry already exists {}", blockedTacList);
                blockedTacListRepository.delete(blockedTacList);
                logger.info("Entry deleted in blocked tac list {}", blockedTacList);
                BlockedTacListHis blockedTacListHisEntity = BlockedTacListHisBuilder.forInsert(blockedTacList, 0, listDataMgmt);
                blockedTacListHisRepository.save(blockedTacListHisEntity);
                logger.info("Entry save in blocked tac list his {}", blockedTacListHisEntity);
//                writer.println(tac + "," + "DELETED");
                writer.println((tacEmpty ? "":tac)+","+dbConfigService.getValue("msgForDeletedInBlockedTac"));

            }
            // if present write in file and exit
            else {
                logger.info("The entry for tac {} does not exist.", tac);
//                writer.println(tac + "," + "NOT_EXIST");
//                writer.println((msisdnEmpty ? "":msisdn)+","+(imsiEmpty ? "":imsi)+","+(imeiEmpty ? "":imei )+","+"NOT_EXIST");
                writer.println((tacEmpty ? "":tac)+","+dbConfigService.getValue("msgForNotExistsInBlockedTac"));
            }
            return true;
        } catch (Exception ex) {
            logger.error("Error while processing the entry for black list, for request {} and action {}, message {}",
                    listDataMgmt.getRequestType(), listDataMgmt.getAction(), ex.getMessage());
//            writer.println(tac +","+"ENTRY_FAILED");
//            writer.println((msisdnEmpty ? "":msisdn)+","+(imsiEmpty ? "":imsi)+","+(imeiEmpty ? "":imei )+","+"NOT_EXIST");
            writer.println((tacEmpty ? "":tac)+","+dbConfigService.getValue("msgForEntryFailedInBlockedTac"));
            return false;

        }
    }

    public boolean processBlockedTacAddEntry(ListDataMgmt listDataMgmt, BlockedTacDto record, int type, PrintWriter writer) {


        String tac = type == 1 ? listDataMgmt.getTac() : record.getTac();
        boolean tacEmpty = validation.isEmptyAndNull(tac);
        try {
            // search in list if already exists or not.
            if(!tacEmpty) tac = tac.trim();

            BlockedTacList blockedTacList = dbClass.getBlockedTacEntry(tacEmpty, tac);

            // if present write in file and exit.
            if (blockedTacList != null) {
                logger.info("The entry already exists {}", blockedTacList);
//                writer.println(tac+","+"ALREADY_EXIST");
//                writer.println((msisdnEmpty ? "":msisdn)+","+(imsiEmpty ? "":imsi)+","+(imeiEmpty ? "":imei )+","+"NOT_EXIST");
                writer.println((tacEmpty ? "":tac)+","+dbConfigService.getValue("msgForAlreadyExistsInBlockedTac"));
            }
            // if not present make entry in table
            else {
                logger.info("The entry for tac {}", tac);
//                String operatorName = operatorSeriesService.getOperatorName(imsi, msisdn);
                blockedTacList = type == 1 ? BlockedTacListBuilder.forInsert(listDataMgmt) : BlockedTacListBuilder.forInsert(listDataMgmt, record);

                logger.info("Entry save in blocked tac list {}",blockedTacList);
                blockedTacListRepository.save(blockedTacList);
                BlockedTacListHis blockedTacListHisEntity = BlockedTacListHisBuilder.forInsert(blockedTacList, 1, listDataMgmt);
                logger.info("Entry save in blocked tac list his {}", blockedTacListHisEntity);
                blockedTacListHisRepository.save(blockedTacListHisEntity);
//                writer.println(tac+","+"ADDED");
//                writer.println((msisdnEmpty ? "":msisdn)+","+(imsiEmpty ? "":imsi)+","+(imeiEmpty ? "":imei )+","+"NOT_EXIST");
                writer.println((tacEmpty ? "":tac)+","+dbConfigService.getValue("msgForAddedInBlockedTac"));
            }
            return true;
        } catch (Exception ex) {
            logger.error("Error while processing the entry for blocked tac list, for request {} and action {}, message {}",
                    listDataMgmt.getRequestType(), listDataMgmt.getAction(), ex.getMessage());
//            writer.println(tac+","+"ENTRY_FAILED");
//            writer.println((msisdnEmpty ? "":msisdn)+","+(imsiEmpty ? "":imsi)+","+(imeiEmpty ? "":imei )+","+"NOT_EXIST");
            writer.println((tacEmpty ? "":tac)+","+dbConfigService.getValue("msgForEntryFailedInBlockedTac"));
            return false;

        }
    }



}
