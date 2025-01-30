package app.web_parser.service.parser.moi.utility;

import app.web_parser.model.app.StolenDeviceMgmt;
import app.web_parser.model.app.SearchImeiByPoliceMgmt;
import app.web_parser.model.app.WebActionDb;
import app.web_parser.service.parser.moi.imeisearchrecovery.IMEISearchRecoverySubFeature;
import app.web_parser.service.parser.moi.loststolen.MOILostStolenSubFeature;
import app.web_parser.service.parser.moi.pendingverification.PendingVerificationFeature;
import app.web_parser.service.parser.moi.recover.MOIRecoverSubFeature;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

@Component
public class MOIFeatureExist {
    private static IMEISearchRecoverySubFeature imeiSearchRecoverySubFeature;
    private static MOIRecoverSubFeature moiRecoverSubFeature;
    private static MOILostStolenSubFeature moiLostStolenSubFeature;
    private static PendingVerificationFeature pendingVerificationFeature;

    public MOIFeatureExist(IMEISearchRecoverySubFeature imeiSearchRecoverySubFeature, MOIRecoverSubFeature moiRecoverSubFeature, MOILostStolenSubFeature moiLostStolenSubFeature, PendingVerificationFeature pendingVerificationFeature) {
        this.imeiSearchRecoverySubFeature = imeiSearchRecoverySubFeature;
        this.moiRecoverSubFeature = moiRecoverSubFeature;
        this.moiLostStolenSubFeature = moiLostStolenSubFeature;
        this.pendingVerificationFeature = pendingVerificationFeature;
    }

    static BiConsumer<WebActionDb, Object> task(String columnName) {
        Map<String, BiConsumer<WebActionDb, Object>> map = new HashMap<>();
        if (Objects.nonNull(columnName) && !columnName.isBlank()) {
            map.put("IMEI_SEARCH_RECOVERY", (x, y) -> imeiSearchRecoverySubFeature.delegateInitRequest(x, (SearchImeiByPoliceMgmt) y));
            map.put("RECOVER", (x, y) -> moiRecoverSubFeature.delegateInitRequest(x, (StolenDeviceMgmt) y));
            map.put("LOST/STOLEN", (x, y) -> moiLostStolenSubFeature.delegateInitRequest(x, (StolenDeviceMgmt) y));
            map.put("LOST", (x, y) -> moiLostStolenSubFeature.delegateInitRequest(x, (StolenDeviceMgmt) y));
            map.put("STOLEN", (x, y) -> moiLostStolenSubFeature.delegateInitRequest(x, (StolenDeviceMgmt) y));
            map.put("PENDING_VERIFICATION", (x, y) -> pendingVerificationFeature.delegateInitRequest(x, (StolenDeviceMgmt) y));
        }
        return map.get(columnName);
    }
}
