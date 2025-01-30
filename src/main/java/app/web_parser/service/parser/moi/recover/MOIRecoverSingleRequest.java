package app.web_parser.service.parser.moi.recover;

import app.web_parser.model.app.StolenDeviceMgmt;
import app.web_parser.model.app.WebActionDb;
import app.web_parser.repository.app.WebActionDbRepository;
import app.web_parser.service.parser.moi.utility.ExceptionModel;
import app.web_parser.service.parser.moi.utility.IMEISeriesModel;
import app.web_parser.service.parser.moi.utility.MOIService;
import app.web_parser.service.parser.moi.utility.RequestTypeHandler;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MOIRecoverSingleRequest implements RequestTypeHandler<StolenDeviceMgmt> {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final MOIService moiService;
    private final WebActionDbRepository webActionDbRepository;
    private final MOIRecoverService moiRecoverService;

    List<String> imeiList = new ArrayList<>();

    @Override
    public void executeInitProcess(WebActionDb webActionDb, StolenDeviceMgmt stolenDeviceMgmt) {
        executeValidateProcess(webActionDb, stolenDeviceMgmt);
    }

    @Override
    public void executeValidateProcess(WebActionDb webActionDb, StolenDeviceMgmt stolenDeviceMgmt) {
        IMEISeriesModel imeiSeriesModel = new IMEISeriesModel();
        BeanUtils.copyProperties(stolenDeviceMgmt, imeiSeriesModel);
        imeiList = moiService.imeiSeries.apply(imeiSeriesModel);
        if (imeiList.isEmpty()) {
            logger.info("No IMEI found for txn id {}", webActionDb.getTxnId());
            return;
        }
        executeProcess(webActionDb, stolenDeviceMgmt);
    }

    @Override
    public void executeProcess(WebActionDb webActionDb, StolenDeviceMgmt stolenDeviceMgmt) {
        try {
            moiRecoverService.actionAtRecord(webActionDb, stolenDeviceMgmt, imeiList, "Single");
            moiService.updateStatusInLostDeviceMgmt("Done", stolenDeviceMgmt.getRequestId());
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