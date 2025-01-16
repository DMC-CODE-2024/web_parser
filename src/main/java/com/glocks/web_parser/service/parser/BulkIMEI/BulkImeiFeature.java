package com.glocks.web_parser.service.parser.BulkIMEI;

import com.glocks.web_parser.model.app.BulkCheckImeiMgmt;
import com.glocks.web_parser.model.app.WebActionDb;
import com.glocks.web_parser.repository.app.BulkCheckImeiMgmtRepository;
import com.glocks.web_parser.service.parser.BulkIMEI.CheckImei.CheckImeiSubFeature;
import com.glocks.web_parser.service.parser.FeatureInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class BulkImeiFeature implements FeatureInterface {

    @Autowired
    BulkCheckImeiMgmtRepository bulkCheckImeiMgmtRepository;
    @Autowired
    CheckImeiSubFeature checkImeiSubFeature;
    public void executeInit(WebActionDb webActionDb) {
        BulkCheckImeiMgmt bulkCheckImeiMgmt = bulkCheckImeiMgmtRepository.findByTransactionId(webActionDb.getTxnId());
        if(webActionDb.getSubFeature().equalsIgnoreCase("CHECK_IMEI")) {
            checkImeiSubFeature.executeInitProcess(webActionDb, bulkCheckImeiMgmt);
        }
    }

    public void executeProcess(WebActionDb webActionDb) {
        BulkCheckImeiMgmt bulkCheckImeiMgmt = bulkCheckImeiMgmtRepository.findByTransactionId(webActionDb.getTxnId());
        if(webActionDb.getSubFeature().equalsIgnoreCase("CHECK_IMEI")) {
            checkImeiSubFeature.executeProcess(webActionDb, bulkCheckImeiMgmt);
        }
    }

    public void validateProcess(WebActionDb webActionDb) {

        BulkCheckImeiMgmt bulkCheckImeiMgmt = bulkCheckImeiMgmtRepository.findByTransactionId(webActionDb.getTxnId());
        if(webActionDb.getSubFeature().equalsIgnoreCase("CHECK_IMEI")) {
            checkImeiSubFeature.executeValidateProcess(webActionDb, bulkCheckImeiMgmt);
        }

    }

}
