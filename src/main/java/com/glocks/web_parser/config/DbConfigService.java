package com.glocks.web_parser.config;



import com.glocks.web_parser.constants.ConfigFlag;
import com.glocks.web_parser.model.app.EirsResponseParam;
import com.glocks.web_parser.model.app.SysParam;
import com.glocks.web_parser.repository.app.EirsResponseParamRepository;
import com.glocks.web_parser.repository.app.SysParamRepository;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class DbConfigService {

    private final Logger logger = LogManager.getLogger(this.getClass());
    @Autowired
    EirsResponseParamRepository eirsResponseParamRepository;

    private Map<String, String> configFlagHM = new ConcurrentHashMap<>();


    @PostConstruct
    public void myInit() {
        loadAllConfig();
    }

//    @Override
    public void loadAllConfig() {
        List<String> modules = new ArrayList<>();
        modules.add("TRC Management"); modules.add("List Management"); modules.add("Bulk Check IMEI");
        List<EirsResponseParam> fullConfigFlag = eirsResponseParamRepository.findByFeatureNameIn(modules);
        for (EirsResponseParam configFlagElement : fullConfigFlag) {
            configFlagHM.put(configFlagElement.getTag(), configFlagElement.getValue());
            logger.info("Filled Config tag:{} value:{}", configFlagElement.getTag(), configFlagElement.getValue());
        }
        logger.info("Config flag data load count : {}", configFlagHM.size());
    }

    public String getValue(String tag) {
        String t = configFlagHM.get(tag);
        return t;
    }



}
