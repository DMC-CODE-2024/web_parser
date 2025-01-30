package app.web_parser.service.parser.ListMgmt.blockedTac;

import app.web_parser.model.app.ListDataMgmt;
import app.web_parser.model.app.WebActionDb;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static app.web_parser.constants.ListMgmtConstants.*;

@Service
public class BlockedTacListSubFeature {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Autowired
    BlockedTacSingleDel blockedTacSingleDel;
    @Autowired
    BlockedTacSingleAdd blockedTacSingleAdd;
    @Autowired
    BlockedTacBulkAdd blockedTacBulkAdd;
    @Autowired
    BlockedTacBulkDel blockedTacBulkDel;

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
            return blockedTacSingleAdd;
        }
        else if(requestType.equalsIgnoreCase(listMgmtSingleRequestMode) && action.equalsIgnoreCase(listMgmtActionDel)) {
            return blockedTacSingleDel;
        }
        else if(requestType.equalsIgnoreCase(listMgmtBulkRequestMode) && action.equalsIgnoreCase(listMgmtActionAdd)) {
            return blockedTacBulkAdd;

        }
        else if(requestType.equalsIgnoreCase(listMgmtBulkRequestMode) && action.equalsIgnoreCase(listMgmtActionDel)) {
            return blockedTacBulkDel;
        }
        else return null;
    }

}
