package app.web_parser.service.appLoader;

import app.web_parser.model.app.WebActionDb;
import app.web_parser.service.parser.FeatureInterface;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppLoaderBulkRequest implements FeatureInterface {
    private final Logger logger = LogManager.getLogger(this.getClass());


    @Override
    public void executeInit(WebActionDb wb) {

    }

    @Override
    public void executeProcess(WebActionDb wb) {

    }

    @Override
    public void validateProcess(WebActionDb wb) {

    }
}
