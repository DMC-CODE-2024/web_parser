package com.glocks.web_parser.service.parser;

import com.glocks.web_parser.service.parser.BulkIMEI.BulkImeiFeature;
import com.glocks.web_parser.service.parser.ListMgmt.ListMgmtFeature;
import com.glocks.web_parser.service.parser.Recovery.RecoveryFeature;
import com.glocks.web_parser.service.parser.moi.utility.MOIFeature;
import com.glocks.web_parser.service.parser.TRC.TRCFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class FeatureList {

    @Autowired
    TRCFeature trcFeature;
    @Autowired
    ListMgmtFeature listMgmtFeature;
    @Autowired
    BulkImeiFeature bulkImeiFeature;
    @Autowired
    MOIFeature moiFeature;

    public Map<String, FeatureInterface> getFeatures() {
        return Map.of("TRCManagement", trcFeature,
                "ListManagement", listMgmtFeature,
                "BulkIMEICheck", bulkImeiFeature,
                "Recovery", new RecoveryFeature(),
                "MOI", moiFeature
        );
    }
}

