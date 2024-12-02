package com.glocks.web_parser.service.parser.moi.pendingverification;

import com.glocks.web_parser.model.app.StolenDeviceMgmt;
import com.glocks.web_parser.model.app.WebActionDb;
import com.glocks.web_parser.service.parser.moi.utility.RequestTypeHandler;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PendingVerificationFeature {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final PendingVerificationRequest pendingVerificationRequest;

    public void delegateInitRequest(WebActionDb webActionDb, StolenDeviceMgmt stolenDeviceMgmt) {
        RequestTypeHandler requestTypeHandler = checkType(stolenDeviceMgmt);
        requestTypeHandler.executeInitProcess(webActionDb, stolenDeviceMgmt);

    }

    public RequestTypeHandler checkType(StolenDeviceMgmt stolenDeviceMgmt) {
        RequestTypeHandler requestTypeSelection = pendingVerificationRequest;
        return requestTypeSelection;
    }
}
