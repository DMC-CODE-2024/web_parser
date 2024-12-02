package com.glocks.web_parser.service.parser.moi.utility;

import com.glocks.web_parser.model.app.WebActionDb;

public interface RequestTypeHandler<T> {
    void executeInitProcess(WebActionDb webActionDb, T t);

    void executeValidateProcess(WebActionDb webActionDb, T t);

    void executeProcess(WebActionDb webActionDb, T t);
}
