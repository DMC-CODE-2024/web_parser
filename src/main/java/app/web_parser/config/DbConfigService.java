package app.web_parser.config;


import app.web_parser.model.app.EirsResponseParam;
import app.web_parser.repository.app.EirsResponseParamRepository;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class DbConfigService {

    private final Logger logger = LogManager.getLogger(this.getClass());
    @Autowired
    EirsResponseParamRepository eirsResponseParamRepository;
    @Autowired
    AppConfig appConfig;
    private Map<String, String> configFlagHM = new ConcurrentHashMap<>();

    @PostConstruct
    public void myInit() {
        loadAllConfig();
    }

    //    @Override
    public void loadAllConfig() {
        List<String> modules = appConfig.getEirsResponseParamList();
        logger.info("going to fetch record based on {} in eirs_response_param", modules);
        List<EirsResponseParam> fullConfigFlag = eirsResponseParamRepository.findByFeatureNameIn(modules);
        for (EirsResponseParam configFlagElement : fullConfigFlag) {
            if (configFlagElement.getTag() != null && configFlagElement.getValue() != null) {
                configFlagHM.put(configFlagElement.getTag(), configFlagElement.getValue());
                logger.info("Filled Config tag:{} value:{}", configFlagElement.getTag(), configFlagElement.getValue());
            }
        }
        logger.info("Config flag data load count : {}", configFlagHM.size());
    }

    public String getValue(String tag) {
        String t = configFlagHM.get(tag);
        return t;
    }


}
