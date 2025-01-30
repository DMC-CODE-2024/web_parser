package app.web_parser.service.parser.moi.loststolen;

import app.web_parser.alert.AlertService;
import app.web_parser.config.AppConfig;
import app.web_parser.model.app.StolenDeviceMgmt;
import app.web_parser.model.app.WebActionDb;
import app.web_parser.repository.app.WebActionDbRepository;
import app.web_parser.service.fileOperations.FileOperations;
import app.web_parser.service.parser.moi.pendingverification.PendingVerificationRequest;
import app.web_parser.service.parser.moi.utility.ConfigurableParameter;
import app.web_parser.service.parser.moi.utility.ExceptionModel;
import app.web_parser.service.parser.moi.utility.MOIService;
import app.web_parser.service.parser.moi.utility.RequestTypeHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MOILostStolenBulkRequest implements RequestTypeHandler<StolenDeviceMgmt> {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final WebActionDbRepository webActionDbRepository;
    private final MOIService moiService;
    private final FileOperations fileOperations;
    Map<String, String> map = new HashMap<>();
    private final AlertService alertService;
    private final MOILostStolenService moiLostStolenService;
    private final AppConfig appConfig;
    private final PendingVerificationRequest pendingVerificationRequest;

    public MOILostStolenBulkRequest(WebActionDbRepository webActionDbRepository, MOIService moiService, FileOperations fileOperations, AlertService alertService, MOILostStolenService moiLostStolenService, AppConfig appConfig, PendingVerificationRequest pendingVerificationRequest) {
        this.webActionDbRepository = webActionDbRepository;
        this.moiService = moiService;
        this.fileOperations = fileOperations;
        this.alertService = alertService;
        this.moiLostStolenService = moiLostStolenService;
        this.appConfig = appConfig;
        this.pendingVerificationRequest = pendingVerificationRequest;
    }

    @Override
    public void executeInitProcess(WebActionDb webActionDb, StolenDeviceMgmt stolenDeviceMgmt) {
        executeValidateProcess(webActionDb, stolenDeviceMgmt);
    }

    @Override
    public void executeValidateProcess(WebActionDb webActionDb, StolenDeviceMgmt stolenDeviceMgmt) {
        boolean isGreyListDurationValueValid = false;
        String uploadedFileName = stolenDeviceMgmt.getFileName();
        String transactionId = stolenDeviceMgmt.getRequestId();
        String moiFilePath = appConfig.getMoiFilePath();
        String uploadedFilePath = moiFilePath + "/" + transactionId + "/" + uploadedFileName;
        logger.info("Uploaded file path is {}", uploadedFilePath);
        if (!fileOperations.checkFileExists(uploadedFilePath)) {
            logger.error("File is missing in path {} for transaction ID  {}", uploadedFilePath, transactionId);
            alertService.raiseAnAlert(transactionId, ConfigurableParameter.FILE_MISSING_ALERT.getValue(), webActionDb.getSubFeature(), transactionId, 0);
            moiService.commonStorageAvailable(webActionDb, transactionId);
            return;
        }


        if (!moiService.areHeadersValid(uploadedFilePath, "STOLEN", 9)) {
            moiService.updateStatusAsFailInLostDeviceMgmt(webActionDb, transactionId);
            return;
        }
        Integer.parseInt(moiService.greyListDuration());
        isGreyListDurationValueValid = true;
        /*} catch (Exception e) {
            logger.info("Invalid GREY_LIST_DURATION value");
            ExceptionModel exceptionModel = ExceptionModel.builder()
                    .error(e.getMessage())
                    .transactionId(stolenDeviceMgmt.getRequestId())
                    .subFeature(webActionDb.getSubFeature())
                    .build();
            moiService.exception(exceptionModel);
        }*/
        if (isGreyListDurationValueValid) {
            map.put("uploadedFileName", uploadedFileName);
            map.put("transactionId", transactionId);
            map.put("uploadedFilePath", uploadedFilePath);
            map.put("moiFilePath", moiFilePath);
            executeProcess(webActionDb, stolenDeviceMgmt);
        }
    }

    @Override
    public void executeProcess(WebActionDb webActionDb, StolenDeviceMgmt stolenDeviceMgmt) {
        logger.info("--------------------------STOLEN BULK PROCESS STARTED------------------");
        logger.info("-----------------------------------------------------------------------");

        String transactionId = map.get("transactionId");
        String processedFilePath = map.get("moiFilePath") + "/" + transactionId + "/" + transactionId + ".csv";
        logger.info("Processed file path is {}", processedFilePath);
        moiLostStolenService.fileProcess(webActionDb, stolenDeviceMgmt, map.get("uploadedFileName"), map.get("uploadedFilePath"), Integer.parseInt(moiService.greyListDuration()));
        try {
            moiService.updateStatusInLostDeviceMgmt("Done", stolenDeviceMgmt.getRequestId());
            moiService.webActionDbOperation(4, webActionDb.getId());
        } catch (Exception e) {
            ExceptionModel exceptionModel = ExceptionModel.builder()
                    .error(e.getMessage())
                    .transactionId(stolenDeviceMgmt.getRequestId())
                    .subFeature(webActionDb.getSubFeature())
                    .build();
            moiService.exception(exceptionModel);
        }
    }
}