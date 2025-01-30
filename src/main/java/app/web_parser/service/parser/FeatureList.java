package app.web_parser.service.parser;

import app.web_parser.service.appLoader.AppLoaderFeature;
import app.web_parser.service.parser.BulkIMEI.BulkImeiFeature;
import app.web_parser.service.parser.ListMgmt.ListMgmtFeature;
import app.web_parser.service.parser.TRC.TRCFeature;
import app.web_parser.service.parser.moi.utility.MOIFeature;
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
    @Autowired
    AppLoaderFeature appLoaderFeature;

    public Map<String, FeatureInterface> getFeatures() {
        return Map.of("TRCManagement", trcFeature,
                "ListManagement", listMgmtFeature,
                "BulkIMEICheck", bulkImeiFeature,
                "MOI", moiFeature,
                "appLoader", appLoaderFeature
        );
    }
}

