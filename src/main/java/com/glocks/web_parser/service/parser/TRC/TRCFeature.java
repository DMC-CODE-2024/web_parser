package com.glocks.web_parser.service.parser.TRC;

import com.glocks.web_parser.model.app.WebActionDb;
import com.glocks.web_parser.service.parser.FeatureInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TRCFeature implements FeatureInterface {

    @Autowired
    TADataSubFeature taDataSubFeature;
    @Autowired
    QADataSubFeature qaDataSubFeature;
    @Autowired
    LMDataSubFeature lmDataSubFeature;
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Override
    public void executeInit(WebActionDb wb) {
        logger.info("Starting the init function for TRC");
        if(wb.getSubFeature().equalsIgnoreCase("TA")) {
            taDataSubFeature.initProcess(wb);
        }
        else if(wb.getSubFeature().equalsIgnoreCase("QA")) {
            qaDataSubFeature.initProcess(wb);
        }
        else if(wb.getSubFeature().equalsIgnoreCase("LM")) {
            lmDataSubFeature.initProcess(wb);
        }


    }
    @Override
    public void executeProcess(WebActionDb wb) {
         // Mark Done
        if(wb.getSubFeature().equalsIgnoreCase("TA")) {
            taDataSubFeature.executeProcess(wb);
        }
        else if(wb.getSubFeature().equalsIgnoreCase("QA")) {
            qaDataSubFeature.executeProcess(wb);
        }
        else if(wb.getSubFeature().equalsIgnoreCase("LM")) {
            lmDataSubFeature.executeProcess(wb);
        }
    }

    @Override
    public void validateProcess(WebActionDb wb) {
        if(wb.getSubFeature().equalsIgnoreCase("TA")) {
            taDataSubFeature.validateProcess(wb);
        }
        else if(wb.getSubFeature().equalsIgnoreCase("QA")) {
            qaDataSubFeature.validateProcess(wb);
        }
        else if(wb.getSubFeature().equalsIgnoreCase("LM")) {
            lmDataSubFeature.validateProcess(wb);
        }

    }
}
