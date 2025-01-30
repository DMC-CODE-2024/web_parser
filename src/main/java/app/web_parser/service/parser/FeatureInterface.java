package app.web_parser.service.parser;

import app.web_parser.model.app.WebActionDb;

public interface FeatureInterface {

    public void executeInit(WebActionDb wb);
    public void executeProcess(WebActionDb wb);
    public void validateProcess(WebActionDb wb);


}
