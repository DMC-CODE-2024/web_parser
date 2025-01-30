package app.web_parser.builder;

import app.web_parser.model.app.BlackList;
import app.web_parser.model.app.BlackListHis;
import app.web_parser.model.app.ListDataMgmt;
import lombok.Builder;
import org.springframework.stereotype.Component;

@Component
@Builder
public class BlackListHisBuilder {

    public static BlackListHis forInsert(BlackList blackList, int operation, ListDataMgmt listDataMgmt) {
        BlackListHis blackListHis = new BlackListHis();
        blackListHis.setImei(blackList.getImei());
        blackListHis.setImsi(blackList.getImsi());
        blackListHis.setMsisdn(blackList.getMsisdn());
        blackListHis.setOperation(operation);
        blackListHis.setRemarks(blackList.getRemarks());
        blackListHis.setActualImei(blackList.getActualImei());
//
        blackListHis.setModeType(blackList.getModeType());
        blackListHis.setOperatorName(blackList.getOperatorName());

        blackListHis.setRequestType(blackList.getRequestType());
        blackListHis.setTxnId(listDataMgmt.getTransactionId());
        blackListHis.setUserId(listDataMgmt.getUserId());

        blackListHis.setTac(blackList.getTac());
        blackListHis.setSource(blackList.getSource());
        return blackListHis;
    }
}

