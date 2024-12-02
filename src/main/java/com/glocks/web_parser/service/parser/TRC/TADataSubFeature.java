package com.glocks.web_parser.service.parser.TRC;


import com.glocks.web_parser.alert.AlertService;
import com.glocks.web_parser.config.AppConfig;
import com.glocks.web_parser.config.DbConfigService;
import com.glocks.web_parser.constants.FileType;
import com.glocks.web_parser.constants.ListType;
import com.glocks.web_parser.dto.FileDto;
import com.glocks.web_parser.dto.TrcTaFileDto;
import com.glocks.web_parser.model.app.TrcDataMgmt;
import com.glocks.web_parser.model.app.TrcTypeApprovedData;
import com.glocks.web_parser.model.app.WebActionDb;
import com.glocks.web_parser.repository.app.TrcDataMgmtRepository;
import com.glocks.web_parser.repository.app.TrcTypeApprovedDataRepository;
import com.glocks.web_parser.repository.app.WebActionDbRepository;
import com.glocks.web_parser.service.fileCopy.ListFileManagementService;
import com.glocks.web_parser.service.fileOperations.FileOperations;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;

import static com.glocks.web_parser.constants.Constants.done;

@Service
public class TADataSubFeature {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Autowired
    AppConfig appConfig;
    @Autowired
    WebActionDbRepository webActionDbRepository;
    @Autowired
    TrcDataMgmtRepository trcDataMgmtRepository;
    @Autowired
    AlertService alertService;
    @Autowired
    TrcTypeApprovedDataRepository trcTypeApprovedDataRepository;
    @Autowired
    ListFileManagementService listFileManagementService;
    @Autowired
    FileOperations fileOperations;
    @Autowired
    DbConfigService dbConfigService;

    String sortedFileName = "sortedFile.txt";

    void initProcess(WebActionDb webActionDb) {
        logger.info("Starting the init function for TRC, TA-Data sub feature {}", webActionDb);
        // changing the status in web action db to 2
        webActionDbRepository.updateWebActionStatus(2, webActionDb.getId());
        validateProcess(webActionDb);
    }

    void validateProcess(WebActionDb webActionDb) {

        logger.info("Validating the files.");
        // validating the file recd.
        try {
            TrcDataMgmt trcDataMgmt = trcDataMgmtRepository.findByTransactionId(webActionDb.getTxnId());
            logger.info("The trc data management entry is {}", trcDataMgmt);
            String currentFileName = trcDataMgmt.getFileName();
            String transactionId = trcDataMgmt.getTransactionId();
            String filePath = appConfig.getTaBaseFilePath() + "/" + transactionId + "/" + currentFileName;
            logger.info("File path is {}", filePath);
            String date = webActionDb.getModifiedOn().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            logger.info("Date is {}", date);
            String deltaDeleteFile = appConfig.getTaBaseFilePath() + "/" + trcDataMgmt.getTransactionId() + "/" +
                    "trc_data_ta_dump_del_"+ date+".txt";
            String deltaAddFile = appConfig.getTaBaseFilePath() + "/" + trcDataMgmt.getTransactionId() + "/" +
                    "trc_data_ta_dump_add_"+ date+".txt";
            if(!fileOperations.checkFileExists(filePath)) {
                logger.error("File does not exists {}", filePath);
                alertService.raiseAnAlert(transactionId,"alert6001", "TA", currentFileName, 0);
                updateFailStatus(webActionDb, trcDataMgmt, dbConfigService.getValue("msgForRemarksForInternalErrorInTA"), "alert6001", "TA", currentFileName);
                return ;
            }

            logger.info("{} {}",appConfig.getTaBaseFilePath(), currentFileName);
            FileDto currFile = new FileDto(currentFileName, appConfig.getTaBaseFilePath()+"/"+trcDataMgmt.getTransactionId());
            logger.info("File {} exists on the path {}", currentFileName, appConfig.getTaBaseFilePath() + "/" + transactionId);
            if(!fileValidation(filePath)) {
                logger.info("File Header validation Failed");
                updateFailStatus(webActionDb, trcDataMgmt, dbConfigService.getValue("msgForRemarksForDataFormatErrorInTA"),
                        "alert6002", "TA", currentFileName, currFile.getTotalRecords(), 0, 0, 0);
//                fileOperations.moveFile(currentFileName, currentFileName, appConfig.getTaBaseFilePath() + "/" +
//                        transactionId, appConfig.getTaProcessedBaseFilePath() + "/" + transactionId);
                return ;
            }
            // pick the last successfully processed file
            TrcDataMgmt previousTrcDataMgmt = trcDataMgmtRepository.getFileName(done, "TA");
            String sortedFilePath = appConfig.getTaBaseFilePath() + "/" + transactionId + "/" +currentFileName+"_sorted";
            logger.info("Fetched Last FileName With Done status. Also Sorted file is {}", sortedFilePath);
            // sort the current file
            if(!fileOperations.sortFile(filePath, sortedFilePath)) {
                alertService.raiseAnAlert(transactionId,"alert6003", "while sorting file for TRC TA", currentFileName, 0);
                return ;
            }

            if(previousTrcDataMgmt == null) {
                logger.info("No previous file exists for TA data. Taking file as fresh file {}", currentFileName);
                // copy the contents of current file as it is in add file but sort the file.
                boolean output = fileOperations.copy(currFile, deltaAddFile, deltaDeleteFile);
                if(!output) {
                    alertService.raiseAnAlert(transactionId,"alert6003", "while creating diff file for TRC TA", currentFileName, 0);
                    return ;
                }
            } else {
                // check if previous file exists or not....
                String previousProcessedFilePath = appConfig.getTaBaseFilePath() + "/" +
                        previousTrcDataMgmt.getTransactionId() +"/" + previousTrcDataMgmt.getFileName() + "_sorted";
                logger.info("Previous file name - . Checking if that file exists. {}", previousProcessedFilePath);
                if(!fileOperations.checkFileExists(previousProcessedFilePath)) {
                    logger.error("No previous file exists for TA data.");
                    updateFailStatus(webActionDb, trcDataMgmt, dbConfigService.getValue("msgForRemarksForInternalErrorInTA"),
                            "alert6001", "TA", previousTrcDataMgmt.getFileName(), currFile.getTotalRecords(), 0, 0, 0);
                    return;
                }
                // create diff

                if(fileOperations.createDiffFiles(sortedFilePath, previousProcessedFilePath, deltaDeleteFile, 0)) {
                    alertService.raiseAnAlert(transactionId,"alert6003", "while creating diff file for TRC TA For Type 0", currentFileName, 0);
                    return ;
                }

                if(fileOperations.createDiffFiles(sortedFilePath, previousProcessedFilePath, deltaAddFile, 1)) {
                    alertService.raiseAnAlert(transactionId,"alert6003", "while creating diff file for TRC TA For Type 1", currentFileName, 0);
                    return ;
                }
                logger.info("Diff file creation successful");

            }
//            fileOperations.moveFile(currentFileName, currentFileName, appConfig.getTaBaseFilePath() + "/" +
//                    transactionId, appConfig.getTaProcessedBaseFilePath() + "/" + transactionId);
            // all done updating the entry to 3 in web action db and calling process file functions
            webActionDbRepository.updateWebActionStatus(3, webActionDb.getId());
            listFileManagementService.saveListManagementEntity(transactionId, ListType.OTHERS, FileType.PROCESSED_FILE,
                    appConfig.getTaBaseFilePath() + "/" +
                    transactionId +"/", currentFileName+"_sorted",(long) currFile.getTotalRecords());

            listFileManagementService.saveListManagementEntity(transactionId, ListType.OTHERS, FileType.PROCESSED_FILE,
                    appConfig.getTaBaseFilePath() + "/" +
                    transactionId +"/", "trc_data_ta_dump_del_"+date+".txt", 0L);

            listFileManagementService.saveListManagementEntity(transactionId, ListType.OTHERS, FileType.PROCESSED_FILE,
                    appConfig.getTaBaseFilePath() + "/" +
                    transactionId +"/", "trc_data_ta_dump_add_"+date+".txt", 0L);
            executeProcess(webActionDb);


        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
    }

    void executeProcess(WebActionDb webActionDb) {
        TrcDataMgmt trcDataMgmt = trcDataMgmtRepository.findByTransactionId(webActionDb.getTxnId());
        String transactionId = trcDataMgmt.getTransactionId();
        String date = webActionDb.getModifiedOn().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        FileDto delFile = new FileDto("trc_data_ta_dump_del_"+ date+".txt",
                appConfig.getTaBaseFilePath() + "/"+trcDataMgmt.getTransactionId());
        FileDto addFile = new FileDto("trc_data_ta_dump_add_"+ date+".txt",
                appConfig.getTaBaseFilePath() + "/"+trcDataMgmt.getTransactionId());
        try {

            boolean output1 = fileRead(delFile, 1);
            if(!output1) {
                logger.error("Error in processing delete delta file for TRC TA data.");
                updateFailStatus(webActionDb, trcDataMgmt, dbConfigService.getValue("msgForRemarksForInternalErrorInTA"),
                        "alert6003", "while processing delete detla file for TRC TA", delFile.getFileName(),
                        addFile.getTotalRecords() + delFile.getTotalRecords(),
                        addFile.getSuccessRecords(), delFile.getSuccessRecords(),
                        addFile.getFailedRecords()+delFile.getFailedRecords());
//                fileOperations.moveFile(delFile.getFileName(), delFile.getFileName(), appConfig.getTaBaseFilePath() + "/" +
//                        transactionId, appConfig.getTaProcessedBaseFilePath() + "/" + transactionId);
//                fileOperations.moveFile(addFile.getFileName(), addFile.getFileName(), appConfig.getTaBaseFilePath() + "/" +
//                        transactionId, appConfig.getTaProcessedBaseFilePath() + "/" + transactionId);
                return;
            }
            boolean output2 = fileRead(addFile, 0);
            if(!output2) {
                logger.error("Error in processing add delta file for TRC TA data");
                updateFailStatus(webActionDb, trcDataMgmt, dbConfigService.getValue("msgForRemarksForInternalErrorInTA"),
                        "alert6003", "while processing add delta file for TRC TA", addFile.getFileName(),
                        addFile.getTotalRecords() + delFile.getTotalRecords(),
                        addFile.getSuccessRecords(), delFile.getSuccessRecords(),
                        addFile.getFailedRecords()+delFile.getFailedRecords());
//                fileOperations.moveFile(delFile.getFileName(), delFile.getFileName(), appConfig.getTaBaseFilePath() + "/" +
//                        transactionId, appConfig.getTaProcessedBaseFilePath() + "/" + transactionId);
//                fileOperations.moveFile(addFile.getFileName(), addFile.getFileName(), appConfig.getTaBaseFilePath() + "/" +
//                        transactionId, appConfig.getTaProcessedBaseFilePath() + "/" + transactionId);
                return;
            }
            logger.info("Delete delta file summary for TRC Ta data: {}", delFile);
            logger.info("Add delta file summary for TRC Ta data: {}", addFile);

            updateSuccessStatus(webActionDb, trcDataMgmt, dbConfigService.getValue("msgForRemarksForSuccessInTA"),
                    addFile.getTotalRecords() + delFile.getTotalRecords(),
                    addFile.getSuccessRecords(), delFile.getSuccessRecords(),
                    addFile.getFailedRecords()+delFile.getFailedRecords());
//            fileOperations.moveFile(delFile.getFileName(), delFile.getFileName(), appConfig.getTaBaseFilePath() + "/" +
//                    transactionId, appConfig.getTaProcessedBaseFilePath() + "/" + transactionId);
//            fileOperations.moveFile(addFile.getFileName(), addFile.getFileName(), appConfig.getTaBaseFilePath() + "/" +
//                    transactionId, appConfig.getTaProcessedBaseFilePath() + "/" + transactionId);
        } catch (Exception ex) {
            logger.error("Error in executing the process for delta files for TA data");

            return ;
        }
    }

    boolean fileRead(FileDto fileDto, int request) {
        // read file and process the entries
        int failureCount=0;
        int succesCount=0;
        try(BufferedReader reader = new BufferedReader(new FileReader(fileDto.getFilePath() +"/" + fileDto.getFileName()))) {

            try {
                String record;
                reader.readLine(); // skipping the header
                while ((record = reader.readLine()) != null) {
                    if (record.isEmpty()) {
                        continue;
                    }

                    String[] taDataRecord = record.split(appConfig.getTrcTaFileSeparator(), -1);
                    logger.info("Record length {}", taDataRecord.length);
                    if(taDataRecord.length != 10) {
                        logger.error("The record length is not equal to 10 {}", Arrays.stream(taDataRecord));
                        failureCount++;
                        continue;
                    }

                    TrcTypeApprovedData taData = new TrcTypeApprovedData(taDataRecord);
                    try {
                        if(request == 0) {
                            logger.info("Inserting the entry {}", taData);
                            trcTypeApprovedDataRepository.save(taData);
                        }
                        else {
                            logger.info("Deleting the the entry {}", taData);
                            trcTypeApprovedDataRepository.deleteByModel(taData.getModel());

                        }
                        succesCount++;

                    } catch (Exception ex) {
                        if(request == 0 ) logger.error("The entry failed to save in TA Data, {}", taData);
                        else logger.error("The entry failed to delete in TA Data, {}", taData);
                        logger.error(ex.toString());
                        failureCount++;
                    }
                }
            } catch (Exception ex) {
                logger.error("File processing for file {}, failed due to {}", fileDto.getFileName(), ex.getMessage());
                fileDto.setFailedRecords(failureCount);
                fileDto.setSuccessRecords(succesCount);
                return false;
            }

        } catch (FileNotFoundException ex) {
            logger.error("File processing for file {}, failed due to {}", fileDto.getFileName(), ex.getMessage());
            fileDto.setFailedRecords(failureCount);
            fileDto.setSuccessRecords(succesCount);
            return false;
        } catch (IOException ex) {
            logger.error("File processing for file {}, failed due to {}", fileDto.getFileName(), ex.getMessage());
            fileDto.setFailedRecords(failureCount);
            fileDto.setSuccessRecords(succesCount);
            return false;
        } catch (Exception ex) {
            logger.error("File processing for file {}, failed due to {}", fileDto.getFileName(), ex.getMessage());
            fileDto.setFailedRecords(failureCount);
            fileDto.setSuccessRecords(succesCount);
            return false;
        }
        fileDto.setFailedRecords(failureCount);
        fileDto.setSuccessRecords(succesCount);
        return true;
    }

    void updateFailStatus(WebActionDb webActionDb, TrcDataMgmt trcDataMgmt, String remarks, String alertId,
                          String type, String fileName) {
        boolean commonStorage = appConfig.isCommonStorage();
        logger.info("isCommonStorage available : {}", commonStorage);
        if (commonStorage && Objects.nonNull(trcDataMgmt.getTransactionId())) {
            webActionDbRepository.updateWebActionStatus(5, webActionDb.getId());
            trcDataMgmtRepository.updateTrcDataMgmtStatus("FAIL", LocalDateTime.now(), remarks, trcDataMgmt.getId());
            // alertService.raiseAnAlert(webActionDb.getTxnId(),alertId, type, fileName, 0);
        }
    }
    void updateFailStatus(WebActionDb webActionDb, TrcDataMgmt trcDataMgmt, String remarks, String alertId,
                          String type, String fileName,
                          long totalCount, long addCount, long deleteCount, long failureCount) {
        webActionDbRepository.updateWebActionStatus(5, webActionDb.getId());
        trcDataMgmtRepository.updateTrcDataMgmtStatus("FAIL", LocalDateTime.now(), remarks,trcDataMgmt.getId(),
                totalCount, addCount, deleteCount, failureCount);
        alertService.raiseAnAlert(webActionDb.getTxnId(),alertId, type, fileName + " with transaction id " + webActionDb.getTxnId(), 0);
    }

    void updateSuccessStatus(WebActionDb webActionDb, TrcDataMgmt trcDataMgmt, String remarks) {
        webActionDbRepository.updateWebActionStatus(4, webActionDb.getId());
        trcDataMgmtRepository.updateTrcDataMgmtStatus("DONE", LocalDateTime.now(), remarks,trcDataMgmt.getId());
    }

    void updateSuccessStatus(WebActionDb webActionDb, TrcDataMgmt trcDataMgmt, String remarks, long totalCount,
                             long addCount, long deleteCount, long failureCount) {
        webActionDbRepository.updateWebActionStatus(4, webActionDb.getId());
        trcDataMgmtRepository.updateTrcDataMgmtStatus("DONE", LocalDateTime.now(), remarks,trcDataMgmt.getId(),
                totalCount, addCount, deleteCount, failureCount);
    }

    boolean fileValidation(String fileName) {
        File file = new File(fileName);
        try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String headers = reader.readLine();
            String[] header = headers.split(appConfig.getTrcTaFileSeparator(), -1);
            if(header.length != 10) {
                return false;
            }
            TrcTaFileDto trcTaFileDto = new TrcTaFileDto(header);
            if (trcTaFileDto.getNo().equalsIgnoreCase("no") &&
                    trcTaFileDto.getCompany().trim().equalsIgnoreCase("company") &&
                    trcTaFileDto.getTrademark().trim().equalsIgnoreCase("trademark") &&
                    trcTaFileDto.getProductName().trim().equalsIgnoreCase("product name") &&
                    trcTaFileDto.getModel().trim().equalsIgnoreCase("model") &&
                    trcTaFileDto.getCountryOfManufacture().trim().equalsIgnoreCase("Country Of Manufacture") &&
                    trcTaFileDto.getCompanyId().trim().equalsIgnoreCase("Company Id") &&
                    trcTaFileDto.getCommercialName().trim().equalsIgnoreCase("Commercial Name") &&
                    trcTaFileDto.getTrcIdentifier().trim().equalsIgnoreCase("trc identifier") &&
                    trcTaFileDto.getApprovedDate().trim().equalsIgnoreCase("approved date")
            ) {
                return true;
            }
            reader.close();
            logger.error("The header of the file is not correct");
            return false;
        } catch (Exception ex) {
            logger.error("Exception while reading the file {} {}", fileName, ex.getMessage());
            return false;
        }
    }




}
