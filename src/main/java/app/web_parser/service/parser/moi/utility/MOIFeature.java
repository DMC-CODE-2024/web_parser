package app.web_parser.service.parser.moi.utility;

import app.web_parser.model.app.WebActionDb;
import app.web_parser.repository.app.WebActionDbRepository;
import app.web_parser.service.parser.FeatureInterface;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;


@Component
@RequiredArgsConstructor
public class MOIFeature implements FeatureInterface {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final WebActionDbRepository webActionDbRepository;
    private final MOIService moiService;

    public void action(WebActionDb wb, String subFeature) {
        String txnId = wb.getTxnId();
        logger.info("Txn ID [{}]", txnId);
        BiConsumer<WebActionDb, Object> consumer = MOIFeatureExist.task(subFeature);
        if (Objects.nonNull(consumer)) {
            Optional<?> optional = Optional.empty();
            switch (subFeature) {
                case "IMEI_SEARCH_RECOVERY" -> optional = moiService.findByTxnId(txnId);
                case "STOLEN", "LOST", "LOST/STOLEN", "PENDING_VERIFICATION" ->
                        optional = moiService.findByRequestId(txnId);
                case "RECOVER" -> optional = moiService.findByRequestId(txnId);
            }
            optional.ifPresentOrElse(result -> {
                consumer.accept(wb, result);
            }, () -> {
                logger.info("No Txn ID {} found for furthure processing for subFeature {}", txnId, subFeature);
                webActionDbRepository.updateWebActionStatus(4, wb.getId());
                logger.info("Updated status as Done in web_action_db for txn id {} in subFeature {}", txnId, subFeature);
            });
        }
    }

    @Override
    public void executeInit(WebActionDb wb) {
        if (Objects.nonNull(wb.getSubFeature().trim())) {
            String subFeatureName = wb.getSubFeature().trim().toUpperCase();
            logger.info("Initialization started for MOI sub feature : {}", subFeatureName);
            this.action(wb, subFeatureName);
        }
    }

    @Override
    public void executeProcess(WebActionDb wb) {
        String subFeature = new StringBuilder(wb.getSubFeature()).toString();
        this.action(wb, subFeature);
    }

    @Override
    public void validateProcess(WebActionDb wb) {
        String subFeature = new StringBuilder(wb.getSubFeature()).toString();
        this.action(wb, subFeature);
    }
}
