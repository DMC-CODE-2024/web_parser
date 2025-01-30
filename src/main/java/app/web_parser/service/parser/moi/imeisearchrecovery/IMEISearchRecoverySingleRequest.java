package app.web_parser.service.parser.moi.imeisearchrecovery;

import app.web_parser.config.DbConfigService;
import app.web_parser.model.app.SearchImeiByPoliceMgmt;
import app.web_parser.model.app.WebActionDb;
import app.web_parser.repository.app.WebActionDbRepository;
import app.web_parser.service.parser.moi.utility.IMEISeriesModel;
import app.web_parser.service.parser.moi.utility.MOIService;
import app.web_parser.service.parser.moi.utility.RequestTypeHandler;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IMEISearchRecoverySingleRequest implements RequestTypeHandler<SearchImeiByPoliceMgmt> {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final MOIService moiService;
    private final IMEISearchRecoveryService imeiSearchRecoveryService;
    private final WebActionDbRepository webActionDbRepository;
    private final DbConfigService dbConfigService;
    IMEISeriesModel imeiSeriesModel = new IMEISeriesModel();

    @Override
    public void executeInitProcess(WebActionDb webActionDb, SearchImeiByPoliceMgmt searchImeiByPoliceMgmt) {
        executeValidateProcess(webActionDb, searchImeiByPoliceMgmt);
    }

    @Override
    public void executeValidateProcess(WebActionDb webActionDb, SearchImeiByPoliceMgmt searchImeiByPoliceMgmt) {
        BeanUtils.copyProperties(searchImeiByPoliceMgmt, imeiSeriesModel);
        String transactionId = searchImeiByPoliceMgmt.getTransactionId();
        boolean multipleIMEIExist = moiService.isMultipleIMEIExist(imeiSeriesModel);
        if (multipleIMEIExist) {
            if (!imeiSearchRecoveryService.isBrandAndModelGenuine(webActionDb, imeiSeriesModel, transactionId)) {
                moiService.updateStatusAndCountFoundInLost("Fail", 0, transactionId, dbConfigService.getValue("device_mismatch_error"));
                webActionDbRepository.updateWebActionStatus(4, webActionDb.getId());
                return;
            }
        }
        executeProcess(webActionDb, searchImeiByPoliceMgmt);
    }

    @Override
    public void executeProcess(WebActionDb webActionDb, SearchImeiByPoliceMgmt searchImeiByPoliceMgmt) {
        String txnId = webActionDb.getTxnId();
        imeiSearchRecoveryService.actionAtRecord(imeiSeriesModel, webActionDb, txnId, null, "Single", null);
        moiService.webActionDbOperation(4, webActionDb.getId());
    }
}