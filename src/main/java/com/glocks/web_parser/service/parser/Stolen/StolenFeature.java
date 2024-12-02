package com.glocks.web_parser.service.parser.Stolen;

import com.glocks.web_parser.model.app.WebActionDb;
import com.glocks.web_parser.service.parser.FeatureInterface;

public class StolenFeature implements FeatureInterface {
    @Override
    public void executeInit(WebActionDb wb) {
        System.out.println("This is stolen init flow");

    }
    @Override
    public void executeProcess(WebActionDb wb) {
        System.out.println("This is stolen process flow");

    }

    @Override
    public void validateProcess(WebActionDb wb) {

    }
}
