package com.glocks.web_parser.service.parser.moi.loststolen;

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
public class MOILostStolenSubFeature {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final MOILostStolenSingleRequest moiLostStolenSingleRequest;
    private final MOILostStolenBulkRequest moiLostStolenBulkRequest;

    public void delegateInitRequest(WebActionDb webActionDb, StolenDeviceMgmt stolenDeviceMgmt) {
        RequestTypeHandler requestTypeHandler = checkType(stolenDeviceMgmt);
        if (requestTypeHandler != null) {
            logger.info("Executed -- {} -- operation", stolenDeviceMgmt.getRequestMode());
            requestTypeHandler.executeInitProcess(webActionDb, stolenDeviceMgmt);
        } else logger.info("Invalid request mode");
    }

    public RequestTypeHandler checkType(StolenDeviceMgmt stolenDeviceMgmt) {
        String requestType = stolenDeviceMgmt.getRequestMode();
        return requestType.equalsIgnoreCase(ConfigurableParameter.SINGLE.getValue()) ? moiLostStolenSingleRequest : requestType.equalsIgnoreCase(ConfigurableParameter.BULK.getValue()) ? moiLostStolenBulkRequest : null;
    }
}