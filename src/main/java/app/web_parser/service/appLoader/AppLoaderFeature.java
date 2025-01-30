package app.web_parser.service.appLoader;


import app.web_parser.model.app.WebActionDb;
import app.web_parser.repository.app.WebActionDbRepository;
import app.web_parser.service.parser.FeatureInterface;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AppLoaderFeature implements FeatureInterface {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final WebActionDbRepository webActionDbRepository;
    private final FileProcessorService fileProcessorService;

    public void action(WebActionDb wb, String subFeature) throws Exception {
        String txnId = wb.getTxnId();
        logger.info("Txn ID [{}]", txnId);
        fileProcessorService.processFile(wb, subFeature);
    }

    @Override
    public void executeInit(WebActionDb wb) {
        if (Objects.nonNull(wb.getSubFeature().trim())) {
            String subFeatureName = wb.getSubFeature().trim().toUpperCase();
            logger.info("Initialization started for appLoader sub feature : {}", subFeatureName);
            try {
                this.action(wb, subFeatureName);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void executeProcess(WebActionDb wb) {
        String subFeature = new StringBuilder(wb.getSubFeature()).toString();
        try {
            this.action(wb, subFeature);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void validateProcess(WebActionDb wb) {
        String subFeature = new StringBuilder(wb.getSubFeature()).toString();
        try {
            this.action(wb, subFeature);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
