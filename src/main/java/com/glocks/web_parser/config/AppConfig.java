package com.glocks.web_parser.config;


import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
public class AppConfig {

    @Value("${ta.base.file.path}")
    String taBaseFilePath;

    @Value("${eirs.alert.url}")
    String alertUrl;

    @Value("${trc.ta.file.separator}")
    String trcTaFileSeparator;

    @Value("${qa.base.file.path}")
    String qaBaseFilePath;

    @Value("${trc.qa.file.separator}")
    String trcQaFileSeparator;

    @Value("${local.manufacturer.base.file.path}")
    String localManufacturerBaseFilePath;

    @Value("${trc.local.manufacturer.file.separator}")
    String trcLocalManufacturerFileSeparator;

    @Value("${list.mgmt.file.path}")
    String listMgmtFilePath;

    @Value("${list.mgmt.file.separator}")
    String listMgmtFileSeparator;

    @Value("${bulk.check.imei.file.path}")
    String bulkCheckImeiFilePath;

    @Value("${email.url}")
    String emailUrl;

    @Value("${eirs.filecopy.url}")
    String fileCopyUrl;

    @Value("#{'${copy.destination.server.name}'.split(',')}")
    private List<String> copyDestinationServerName;

    @Value("#{'${copy.destination.server.path}'.split(',')}")
    private List<String> copyDestinationServerPath;

    @Value("${source.server.name}")
    String sourceServerName;

    @Value("#{'${feature.list}'.split(',')}")
    List<String> featureList;

    @Value("${virtualIp}")
    String virtualIp;

    @Value("${web.parser.sleep.time}")
    String webParserSleepTime;
    @Value("${eirs.notification.url}")
    String notificationUrl;

    @Value("${web.parser.query.gap}")
    Integer webParserQueryGap;

    @Value("${moi.file.path}")
    private String moiFilePath;
    @Value("${moi.file.separator}")
    String moiFileSeparator;
    @Value("${common_storage_flag}")
    boolean isCommonStorage;

    @Value("${stolenFeatureName}")
    private String stolenFeatureName;


    @Value("${trc.feature}")
    private String trcFeatureName;

    @Value("${listMgmt.feature}")
    private String listMgmtFeatureName;

    @Value("${moi.feature}")
    private String moiFeatureName;

    @Value("${bulkCheckIMEIFeatureName}")
    private String bulkCheckIMEIFeatureName;

    public static String bulkCheckIMEIFeatureNameStatic;

    @PostConstruct
    public void init() {
        bulkCheckIMEIFeatureNameStatic = bulkCheckIMEIFeatureName;
    }
}
