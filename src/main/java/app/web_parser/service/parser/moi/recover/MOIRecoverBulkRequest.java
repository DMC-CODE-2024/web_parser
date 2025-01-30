package app.web_parser.service.parser.moi.recover;

import app.web_parser.alert.AlertService;
import app.web_parser.config.AppConfig;
import app.web_parser.model.app.StolenDeviceMgmt;
import app.web_parser.model.app.WebActionDb;
import app.web_parser.repository.app.WebActionDbRepository;
import app.web_parser.service.fileOperations.FileOperations;
import app.web_parser.service.parser.moi.utility.ConfigurableParameter;
import app.web_parser.service.parser.moi.utility.ExceptionModel;
import app.web_parser.service.parser.moi.utility.MOIService;
import app.web_parser.service.parser.moi.utility.RequestTypeHandler;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MOIRecoverBulkRequest implements RequestTypeHandler<StolenDeviceMgmt> {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final MOIService moiService;
    private final WebActionDbRepository webActionDbRepository;
    private final AppConfig appConfig;
    private final FileOperations fileOperations;
    private final MOIRecoverService moiRecoverService;
    private final AlertService alertService;
    Map<String, String> map = new HashMap<>();

    @Override
    public void executeInitProcess(WebActionDb webActionDb, StolenDeviceMgmt stolenDeviceMgmt) {
        executeValidateProcess(webActionDb, stolenDeviceMgmt);
    }

    @Override
    public void executeValidateProcess(WebActionDb webActionDb, StolenDeviceMgmt stolenDeviceMgmt) {
        String uploadedFileName = stolenDeviceMgmt.getFileName();
        String transactionId = webActionDb.getTxnId();
        String uploadedFilePath = appConfig.getMoiFilePath() + "/" + transactionId + "/" + uploadedFileName;
        logger.info("Uploaded file path is {}", uploadedFilePath);
        if (!fileOperations.checkFileExists(uploadedFilePath)) {
            logger.error("Uploaded file does not exists in path {} for lost ID {}", uploadedFilePath, transactionId);
            alertService.raiseAnAlert(transactionId, ConfigurableParameter.FILE_MISSING_ALERT.getValue(), webActionDb.getSubFeature(), transactionId, 0);
            moiService.commonStorageAvailable(webActionDb, transactionId);
            return;
        }
        /*try {*/
        if (!moiService.areHeadersValid(uploadedFilePath, "RECOVER", 1)) {
            moiService.updateStatusAsFailInLostDeviceMgmt(webActionDb, transactionId);
            return;
        }
        map.put("uploadedFileName", uploadedFileName);
        map.put("transactionId", transactionId);
        map.put("uploadedFilePath", uploadedFilePath);
        executeProcess(webActionDb, stolenDeviceMgmt);
      /*  } catch (Exception e) {
            ExceptionModel exceptionModel = ExceptionModel.builder()
                    .error(e.getMessage())
                    .transactionId(transactionId)
                    .subFeature(webActionDb.getSubFeature())
                    .build();
            moiService.exception(exceptionModel);
        }*/
    }


    @Override
    public void executeProcess(WebActionDb webActionDb, StolenDeviceMgmt stolenDeviceMgmt) {
        try {
            moiRecoverService.fileProcessing(map.get("uploadedFilePath"), stolenDeviceMgmt, webActionDb);
            moiService.updateStatusInLostDeviceMgmt("Done", stolenDeviceMgmt.getLostId());
            moiService.webActionDbOperation(4, webActionDb.getId());
        } catch (DataAccessException e) {
            Throwable rootCause = e.getMostSpecificCause();
            if (rootCause instanceof SQLException) {
                SQLException sqlException = (SQLException) rootCause;
                if (sqlException.getErrorCode() == 1146) {
                    ExceptionModel exceptionModel = ExceptionModel.builder().error(moiService.extractTableNameFromMessage(sqlException.getMessage())).transactionId(null).subFeature(null).build();
                    logger.info("{} table not found", moiService.extractTableNameFromMessage(sqlException.getMessage()));
                    moiService.exception(exceptionModel);
                }
            }
        } catch (Exception e) {
            logger.error("Exception occured inside isCommonStorage method {}", e.getMessage());
            ExceptionModel exceptionModel = ExceptionModel.builder().error(e.getMessage()).transactionId(webActionDb.getTxnId()).subFeature(webActionDb.getSubFeature()).build();
            moiService.exception(exceptionModel);
        }
    }

}
