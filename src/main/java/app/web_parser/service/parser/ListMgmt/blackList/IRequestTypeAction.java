package app.web_parser.service.parser.ListMgmt.blackList;

import app.web_parser.model.app.ListDataMgmt;
import app.web_parser.model.app.WebActionDb;

public interface IRequestTypeAction {

    void executeInitProcess(WebActionDb webActionDb, ListDataMgmt listDataMgmt);
    void executeValidateProcess(WebActionDb webActionDb, ListDataMgmt listDataMgmt);

    void executeProcess(WebActionDb webActionDb, ListDataMgmt listDataMgmt);
}
