package app.web_parser.service.parser.moi.imeisearchrecovery;

import app.web_parser.alert.AlertService;
import app.web_parser.config.AppConfig;
import app.web_parser.config.DbConfigService;
import app.web_parser.model.app.SearchImeiByPoliceMgmt;
import app.web_parser.model.app.WebActionDb;
import app.web_parser.repository.app.WebActionDbRepository;
import app.web_parser.service.fileOperations.FileOperations;
import app.web_parser.service.parser.moi.utility.*;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class IMEISearchRecoveryBulkRequest implements RequestTypeHandler<SearchImeiByPoliceMgmt> {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final MOIService moiService;
    private final WebActionDbRepository webActionDbRepository;
    private final AppConfig appConfig;
    private final FileOperations fileOperations;
    private final AlertService alertService;
    private final IMEISearchRecoveryService imeiSearchRecoveryService;
    private final DbConfigService dbConfigService;
    static int successCount = 0, recordCount = 0;
    Map<String, String> map = new HashMap<>();

    @Override
    public void executeInitProcess(WebActionDb webActionDb, SearchImeiByPoliceMgmt searchImeiByPoliceMgmt) {
        executeValidateProcess(webActionDb, searchImeiByPoliceMgmt);
    }

    @Override
    public void executeValidateProcess(WebActionDb webActionDb, SearchImeiByPoliceMgmt searchImeiByPoliceMgmt) {
        String uploadedFileName = searchImeiByPoliceMgmt.getFileName();
        String transactionId = webActionDb.getTxnId();
        String moiFilePath = appConfig.getMoiFilePath();
        String uploadedFilePath = moiFilePath + "/" + transactionId + "/" + uploadedFileName;
        logger.info("Uploaded file path is {}", uploadedFilePath);
        if (!fileOperations.checkFileExists(uploadedFilePath)) {
            logger.error("Uploaded file does not exists in path {} for transactionId {}", uploadedFilePath, transactionId);
            alertService.raiseAnAlert(transactionId, ConfigurableParameter.FILE_MISSING_ALERT.getValue(), webActionDb.getSubFeature(), transactionId, 0);
            moiService.commonStorageAvailable(webActionDb, transactionId);
            return;
        }

        if (!moiService.areHeadersValid(uploadedFilePath, "DEFAULT", 4)) {
            //  dbConfigService.getValue("error_invalid_imei") TBD
            moiService.updateStatusAndCountFoundInLost("Fail", 0, transactionId, dbConfigService.getValue("header_invalid"));
            logger.info("updated record with status as Fail and count_found_in _lost as 0 for Txn ID {}", transactionId);
            webActionDbRepository.updateWebActionStatus(5, webActionDb.getId());
            return;
        }
        map.put("uploadedFileName", uploadedFileName);
        map.put("transactionId", transactionId);
        map.put("uploadedFilePath", uploadedFilePath);
        map.put("moiFilePath", moiFilePath);
        successCount = 0;
        executeProcess(webActionDb, searchImeiByPoliceMgmt);
        /*} catch (Exception e) {
            ExceptionModel exceptionModel = ExceptionModel.builder()
                    .error(e.getMessage())
                    .transactionId(searchImeiByPoliceMgmt.getTransactionId())
                    .subFeature(webActionDb.getSubFeature())
                    .build();
            moiService.exception(exceptionModel);
        }*/
    }

    @Override
    public void executeProcess(WebActionDb webActionDb, SearchImeiByPoliceMgmt searchImeiByPoliceMgmt) {
        recordCount = 0;
        String transactionId = map.get("transactionId");
        String moiPath = map.get("moiFilePath");
        String uploadedFilePath = map.get("uploadedFilePath");
        String processedFileName = transactionId + ".csv";
        String processedFilePath = moiPath + "/" + transactionId + "/" + processedFileName;
        logger.info("Processed file path is {}", processedFilePath);
        PrintWriter printWriter = moiService.file(processedFilePath);
        try (BufferedReader reader = new BufferedReader(new FileReader(uploadedFilePath))) {
            String record;
            String[] header;
            IMEISeriesModel imeiSeriesModel = new IMEISeriesModel();
            String[] split;
            boolean headerSkipped = false;
            while ((record = reader.readLine()) != null) {
                if (!record.isBlank()) {
                    if (!headerSkipped) {
                        header = record.split(appConfig.getMoiFileSeparator(), -1);
                        printWriter.println(moiService.joiner(header, ",Reason"));
                        headerSkipped = true;
                    } else {
                        ++recordCount;
                        split = record.split(appConfig.getListMgmtFileSeparator(), -1);
                        imeiSeriesModel.setImeiSeries(split, "DEFAULT");
                        List<String> imeiList = moiService.imeiSeries.apply(imeiSeriesModel);
                        boolean isImeiValid = false;
                        for (String imei : imeiList) {
                            isImeiValid = moiService.isNumericAndValid.test(imei);
                        }
                        if (!isImeiValid) {
                            printWriter.println(moiService.joiner(split, "," + dbConfigService.getValue("invalid_imei") + ""));
                            logger.info("Invalid IMEI format");
                        } else {
                            boolean multipleIMEIExist = moiService.isMultipleIMEIExist(imeiSeriesModel);
                            if (multipleIMEIExist) {
                                if (!imeiSearchRecoveryService.isBrandAndModelGenuine(webActionDb, imeiSeriesModel, transactionId)) {
                                    printWriter.println(moiService.joiner(split, "," + dbConfigService.getValue("device_mismatch_error") + ""));
                                    continue;
                                }
                            }
                            int count = imeiSearchRecoveryService.actionAtRecord(imeiSeriesModel, webActionDb, webActionDb.getTxnId(), printWriter, "Bulk", split);
                            successCount += count;
                        }
                    }
                }
            }
            printWriter.close();
            logger.info("recordCount {} and successCount {} in Bulk Request ", recordCount, successCount);
            moiService.updateCountFoundInLostAndRecordCount("Done", successCount, transactionId, null, recordCount);
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
        } catch (Exception ex) {
            moiService.updateStatusAndCountFoundInLost("Fail", 0, transactionId, dbConfigService.getValue("error_msg"));
            moiService.webActionDbOperation(5, webActionDb.getId());
            ExceptionModel exceptionModel = ExceptionModel.builder()
                    .error(ex.getMessage())
                    .transactionId(transactionId)
                    .subFeature(webActionDb.getSubFeature())
                    .build();
            moiService.exception(exceptionModel);
        }

    }
}
