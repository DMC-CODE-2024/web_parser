package com.glocks.web_parser.service.parser.moi.recover;

import com.glocks.web_parser.model.app.StolenDeviceMgmt;
import com.glocks.web_parser.model.app.WebActionDb;
import com.glocks.web_parser.service.parser.moi.utility.ConfigurableParameter;
import com.glocks.web_parser.service.parser.moi.utility.RequestTypeHandler;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MOIRecoverSubFeature {

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final MOIRecoverSingleRequest moiRecoverSingleRequest;
    private final MOIRecoverBulkRequest moiRecoverBulkRequest;

    public void delegateInitRequest(WebActionDb webActionDb, StolenDeviceMgmt stolenDeviceMgmt) {
        logger.info("LostDeviceMgmt response based on request ID {} : {}", stolenDeviceMgmt.getRequestId(), stolenDeviceMgmt);
        RequestTypeHandler requestTypeHandler = checkType(stolenDeviceMgmt);
        if (requestTypeHandler != null)
            requestTypeHandler.executeInitProcess(webActionDb, stolenDeviceMgmt);
        else logger.info("Invalid request mode");
    }

    public RequestTypeHandler checkType(StolenDeviceMgmt stolenDeviceMgmt) {
        String requestType = stolenDeviceMgmt.getRequestMode();
        RequestTypeHandler requestTypeSelection = requestType.equalsIgnoreCase(ConfigurableParameter.SINGLE.getValue()) ? moiRecoverSingleRequest : requestType.equalsIgnoreCase(ConfigurableParameter.BULK.getValue()) ? moiRecoverBulkRequest : null;
        logger.info("executed {} operation", requestType);
        return requestTypeSelection;
    }
}