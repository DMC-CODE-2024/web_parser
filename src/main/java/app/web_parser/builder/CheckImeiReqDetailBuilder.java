package app.web_parser.builder;


import app.web_parser.model.app.BulkCheckImeiMgmt;
import app.web_parser.model.app.CheckImeiReqDetail;
import lombok.Builder;
import org.springframework.stereotype.Component;

@Component
@Builder
public class CheckImeiReqDetailBuilder {

    public static CheckImeiReqDetail forInsert(String imei, BulkCheckImeiMgmt bulkCheckImeiMgmt) {
        CheckImeiReqDetail checkImeiReqDetail = new CheckImeiReqDetail();
        checkImeiReqDetail.setImei(imei);
        checkImeiReqDetail.setLanguage(bulkCheckImeiMgmt.getLanguage());
        checkImeiReqDetail.setChannel("WEB_BULK");
        checkImeiReqDetail.setRequestId(bulkCheckImeiMgmt.getTransactionId());
        return checkImeiReqDetail;
    }
}
