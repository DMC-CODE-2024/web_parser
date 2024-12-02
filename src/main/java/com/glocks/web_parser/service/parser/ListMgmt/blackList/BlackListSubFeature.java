package com.glocks.web_parser.service.parser.ListMgmt.blackList;

import com.glocks.web_parser.model.app.ListDataMgmt;
import com.glocks.web_parser.model.app.WebActionDb;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.glocks.web_parser.constants.ListMgmtConstants.*;

@Service
public class BlackListSubFeature {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Autowired
    BlackSingleDel blackSingleDel;
    @Autowired
    BlackSingleAdd blackSingleAdd;
    @Autowired
    BlackBulkAdd blackBulkAdd;
    @Autowired
    BlackBulkDel blackBulkDel;

    public void delegateInitRequest(WebActionDb webActionDb, ListDataMgmt listDataMgmt) {

        IRequestTypeAction requestTypeAction = checkType(listDataMgmt);
        requestTypeAction.executeInitProcess(webActionDb, listDataMgmt);
    }

    public void delegateValidateRequest(WebActionDb webActionDb, ListDataMgmt listDataMgmt) {
        IRequestTypeAction requestTypeAction = checkType(listDataMgmt);
        requestTypeAction.executeValidateProcess(webActionDb, listDataMgmt);
    }

    public void delegateExecuteProcess(WebActionDb webActionDb, ListDataMgmt listDataMgmt) {
        IRequestTypeAction requestTypeAction = checkType(listDataMgmt);
        requestTypeAction.executeProcess(webActionDb, listDataMgmt);
    }
    public IRequestTypeAction checkType(ListDataMgmt listDataMgmt) {
        String requestType = listDataMgmt.getRequestMode();
        String action = listDataMgmt.getAction();
        if(requestType.equalsIgnoreCase(listMgmtSingleRequestMode) && action.equalsIgnoreCase(listMgmtActionAdd)) {
            return blackSingleAdd;
        }
        else if(requestType.equalsIgnoreCase(listMgmtSingleRequestMode) && action.equalsIgnoreCase(listMgmtActionDel)) {
            return blackSingleDel;
        }
        else if(requestType.equalsIgnoreCase(listMgmtBulkRequestMode) && action.equalsIgnoreCase(listMgmtActionAdd)) {
            return blackBulkAdd;

        }
        else if(requestType.equalsIgnoreCase(listMgmtBulkRequestMode) && action.equalsIgnoreCase(listMgmtActionDel)) {
            return blackBulkDel;
        }
        else return null;
    }

}
