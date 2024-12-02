package com.glocks.web_parser.service.parser.Recovery;

import com.glocks.web_parser.model.app.WebActionDb;
import com.glocks.web_parser.service.parser.FeatureInterface;

public class RecoveryFeature implements FeatureInterface {
    @Override
    public void executeInit(WebActionDb wb) {
        System.out.println("This is Recovery init flow");
    }
    @Override
    public void executeProcess(WebActionDb wb) {
        System.out.println("This is Recovery process flow");

    }

    @Override
    public void validateProcess(WebActionDb wb) {

    }
}
