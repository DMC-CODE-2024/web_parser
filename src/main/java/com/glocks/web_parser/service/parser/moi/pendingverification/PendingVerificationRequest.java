package com.glocks.web_parser.service.parser.moi.pendingverification;

import com.glocks.web_parser.alert.AlertService;
import com.glocks.web_parser.config.AppConfig;
import com.glocks.web_parser.model.app.StolenDeviceMgmt;
import com.glocks.web_parser.model.app.WebActionDb;
import com.glocks.web_parser.repository.app.WebActionDbRepository;
import com.glocks.web_parser.service.fileOperations.FileOperations;
import com.glocks.web_parser.service.parser.moi.loststolen.MOILostStolenService;
import com.glocks.web_parser.service.parser.moi.utility.ConfigurableParameter;
import com.glocks.web_parser.service.parser.moi.utility.MOIService;
import com.glocks.web_parser.service.parser.moi.utility.RequestTypeHandler;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PendingVerificationRequest implements RequestTypeHandler<StolenDeviceMgmt> {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final AppConfig appConfig;
    private final FileOperations fileOperations;
    private final WebActionDbRepository webActionDbRepository;
    private final PendingVerificationService pendingVerificationService;
    private final MOILostStolenService moiLostStolenService;
    private final MOIService moiService;
    private final AlertService alertService;
    Map<String, String> map = new HashMap<>();
    boolean verificationStatus = false;

    @Override
    public void executeInitProcess(WebActionDb webActionDb, StolenDeviceMgmt stolenDeviceMgmt) {
        executeValidateProcess(webActionDb, stolenDeviceMgmt);
    }

    @Override
    public void executeValidateProcess(WebActionDb webActionDb, StolenDeviceMgmt stolenDeviceMgmt) {
        String uploadedFileName = stolenDeviceMgmt.getFileName();
        String transactionId = stolenDeviceMgmt.getRequestId();
        String moiFilePath = appConfig.getMoiFilePath();
        String uploadedFilePath = moiFilePath + "/" + transactionId + "/" + uploadedFileName;
        logger.info("Uploaded file path is {}", uploadedFilePath);
        if (!fileOperations.checkFileExists(uploadedFilePath)) {
            logger.error("Uploaded file does not exists in path {} for lost ID {}", uploadedFilePath, transactionId);
            alertService.raiseAnAlert(transactionId, ConfigurableParameter.FILE_MISSING_ALERT.getValue(), webActionDb.getSubFeature(), transactionId, 0);
            moiService.commonStorageAvailable(webActionDb, transactionId);
            return;
        }

        if (!moiService.areHeadersValid(uploadedFilePath, "STOLEN", 9)) {
            moiService.updateStatusAsFailInLostDeviceMgmt(webActionDb, transactionId);
            return;
        }
        map.put("uploadedFileName", uploadedFileName);
        map.put("transactionId", transactionId);
        map.put("uploadedFilePath", uploadedFilePath);
        map.put("moiFilePath", moiFilePath);
        executeProcess(webActionDb, stolenDeviceMgmt);
       /* } catch (Exception e) {
            ExceptionModel exceptionModel = ExceptionModel.builder().error(e.getMessage()).transactionId(stolenDeviceMgmt.getRequestId()).subFeature(webActionDb.getSubFeature()).build();
            moiService.exception(exceptionModel);
        }*/
    }

    @Override
    public void executeProcess(WebActionDb webActionDb, StolenDeviceMgmt stolenDeviceMgmt) {
        String transactionId = map.get("transactionId");
        String processedFilePath = map.get("moiFilePath") + "/" + transactionId + "/" + transactionId + ".csv";
        logger.info("Processed file path is {}", processedFilePath);
        PrintWriter printWriter = moiService.file(processedFilePath);
        logger.info("----PENDING VERIFICATION PROCESS STARTED---");
        verificationStatus = pendingVerificationService.pendingVerificationFileValidation(map.get("uploadedFilePath"), stolenDeviceMgmt, printWriter, ConfigurableParameter.PENDING_VERIFICATION_STAGE_INIT.getValue());
        logger.info("PENDING VERIFICATION PROCESS status {}", verificationStatus);
        if (verificationStatus) {
            pendingVerificationService.validFile(webActionDb, map.get("uploadedFilePath"), stolenDeviceMgmt, printWriter, map.get("uploadedFileName"), ConfigurableParameter.PENDING_VERIFICATION_STAGE_DONE.getValue());
        } else {
            pendingVerificationService.invalidFile(webActionDb, map.get("uploadedFilePath"), stolenDeviceMgmt, printWriter, map.get("uploadedFileName"), ConfigurableParameter.PENDING_VERIFICATION_STAGE_INIT.getValue());
        }
    }
}