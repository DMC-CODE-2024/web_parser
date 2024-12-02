package com.glocks.web_parser.service.parser.ListMgmt.blockedTac;

import com.glocks.web_parser.config.AppConfig;
import com.glocks.web_parser.config.DbConfigService;
import com.glocks.web_parser.constants.FileType;
import com.glocks.web_parser.constants.ListType;
import com.glocks.web_parser.model.app.ListDataMgmt;
import com.glocks.web_parser.model.app.WebActionDb;
import com.glocks.web_parser.repository.app.WebActionDbRepository;
import com.glocks.web_parser.service.fileCopy.ListFileManagementService;
import com.glocks.web_parser.service.fileOperations.FileOperations;
import com.glocks.web_parser.service.operatorSeries.OperatorSeriesService;
import com.glocks.web_parser.service.parser.ListMgmt.CommonFunctions;
import com.glocks.web_parser.service.parser.ListMgmt.utils.BlockedTacUtils;
import com.glocks.web_parser.validator.Validation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.PrintWriter;

@Service
public class BlockedTacSingleAdd implements IRequestTypeAction {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Autowired
    WebActionDbRepository webActionDbRepository;
    @Autowired
    Validation validation;
    @Autowired
    AppConfig appConfig;
    @Autowired
    ListFileManagementService listFileManagementService;
    @Autowired
    FileOperations fileOperations;
    @Autowired
    OperatorSeriesService operatorSeriesService;
    @Autowired
    CommonFunctions commonFunctions;
    @Autowired
    DbConfigService dbConfigService;
    @Autowired
    BlockedTacUtils blockedTacUtils;

    @Override
    public  void executeInitProcess(WebActionDb webActionDb, ListDataMgmt listDataMgmt) {
        logger.info("Starting the init process for blocked tac list, for request {} and action {}",
                listDataMgmt.getRequestMode(), listDataMgmt.getAction());

        webActionDbRepository.updateWebActionStatus(2, webActionDb.getId());
        executeValidateProcess(webActionDb, listDataMgmt);

    }

    @Override
    public void executeValidateProcess(WebActionDb webActionDb, ListDataMgmt listDataMgmt) {
        logger.info("Starting the validate process for blocked tac list, for request {} and action {}",
                listDataMgmt.getRequestMode(), listDataMgmt.getAction());

        // single and add
        try {

            String tac = listDataMgmt.getTac();
            fileOperations.createDirectory(appConfig.getListMgmtFilePath() + "/" + listDataMgmt.getTransactionId() + "/");
            // check all should be null or empty
            String validateOutput = commonFunctions.validateEntry(tac);

            if (validateOutput.equalsIgnoreCase("")) {
                logger.info("The entry is valid, it will be processed");
            } else {
                File outFile = new File(appConfig.getListMgmtFilePath() + "/" + listDataMgmt.getTransactionId() + "/" + listDataMgmt.getTransactionId() + ".csv");
                PrintWriter writer = new PrintWriter(outFile);
                logger.info("The entry failed the validation, with reason {}", validateOutput);
                writer.println("TAC,Reason"); // print header in file
                writer.println((tac==null?"":tac) + "," + dbConfigService.getValue(validateOutput));
                commonFunctions.updateFailStatus(webActionDb, listDataMgmt, 1, 0, 1);
                writer.close();
                listFileManagementService.saveListManagementEntity(listDataMgmt.getTransactionId(), ListType.OTHERS, FileType.SINGLE,
                        appConfig.getListMgmtFilePath() + "/" + listDataMgmt.getTransactionId() + "/",
                        listDataMgmt.getTransactionId() + ".csv", 1L);
                return;
            }
            webActionDbRepository.updateWebActionStatus(3, webActionDb.getId());
            executeProcess(webActionDb, listDataMgmt);
        } catch (Exception ex) {
            logger.error("Exception in validating the entry {} with message {}", listDataMgmt, ex.getMessage());
            commonFunctions.updateFailStatus(webActionDb, listDataMgmt, 1, 0, 1);
        }
    }

    public void executeProcess(WebActionDb webActionDb, ListDataMgmt listDataMgmt) {
        try {
            operatorSeriesService.fillOperatorSeriesHash();
            File outFile = new File(appConfig.getListMgmtFilePath() + "/" + listDataMgmt.getTransactionId() + "/" + listDataMgmt.getTransactionId()+ ".csv");
            PrintWriter writer = new PrintWriter(outFile);
            writer.println("TAC,Reason");
            boolean status = blockedTacUtils.processBlockedTacAddEntry(listDataMgmt, null, 1, writer);
            writer.close();
            listFileManagementService.saveListManagementEntity(listDataMgmt.getTransactionId(), ListType.OTHERS, FileType.SINGLE,
                    appConfig.getListMgmtFilePath() + "/" + listDataMgmt.getTransactionId() + "/",
                    listDataMgmt.getTransactionId() + ".csv", 1L);
            if(status) {
                commonFunctions.updateSuccessStatus(webActionDb, listDataMgmt, 1, 1, 0);
            } else commonFunctions.updateFailStatus(webActionDb, listDataMgmt, 1, 0, 1);
        } catch (Exception ex) {
            logger.error("Error while processing the entry for blocked tac list, for request {} and action {}",
                    listDataMgmt.getRequestType(), listDataMgmt.getAction());
            commonFunctions.updateFailStatus(webActionDb, listDataMgmt, 1, 0, 1);
        }
    }



}
