package com.glocks.web_parser.service.parser.moi.imeisearchrecovery;

import com.glocks.web_parser.model.app.SearchImeiByPoliceMgmt;
import com.glocks.web_parser.model.app.WebActionDb;
import com.glocks.web_parser.service.parser.moi.utility.ConfigurableParameter;
import com.glocks.web_parser.service.parser.moi.utility.RequestTypeHandler;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IMEISearchRecoverySubFeature {

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final IMEISearchRecoverySingleRequest imeiSearchRecoverySingleRequest;
    private final IMEISearchRecoveryBulkRequest imeiSearchRecoveryBulkRequest;

    public void delegateInitRequest(WebActionDb webActionDb, SearchImeiByPoliceMgmt searchImeiByPoliceMgmt) {
        RequestTypeHandler requestTypeHandler = checkType(searchImeiByPoliceMgmt);
        if (requestTypeHandler != null)
            requestTypeHandler.executeInitProcess(webActionDb, searchImeiByPoliceMgmt);
        else logger.info("Invalid request mode");
    }

    public RequestTypeHandler checkType(SearchImeiByPoliceMgmt searchImeiByPoliceMgmt) {
        String requestType = searchImeiByPoliceMgmt.getRequestMode();
        RequestTypeHandler requestTypeSelection = requestType.equalsIgnoreCase(ConfigurableParameter.SINGLE.getValue()) ? imeiSearchRecoverySingleRequest : requestType.equalsIgnoreCase(ConfigurableParameter.BULK.getValue()) ? imeiSearchRecoveryBulkRequest : null;
        logger.info("Executed -- {} -- operation", requestType);
        return requestTypeSelection;
    }
}