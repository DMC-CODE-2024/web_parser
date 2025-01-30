package app.web_parser.service.parser.moi.utility;

import app.web_parser.model.app.WebActionDb;

public interface RequestTypeHandler<T> {
    void executeInitProcess(WebActionDb webActionDb, T t);

    void executeValidateProcess(WebActionDb webActionDb, T t);

    void executeProcess(WebActionDb webActionDb, T t);
}
