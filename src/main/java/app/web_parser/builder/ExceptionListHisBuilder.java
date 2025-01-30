package app.web_parser.builder;


import app.web_parser.model.app.ExceptionList;
import app.web_parser.model.app.ExceptionListHis;
import app.web_parser.model.app.ListDataMgmt;
import lombok.Builder;
import org.springframework.stereotype.Component;

//import static com.gl.eirs.simchange.constants.Constants.remarks;

@Component
@Builder
public class ExceptionListHisBuilder {

    public static ExceptionListHis forInsert(ExceptionList exceptionList, int operation, ListDataMgmt listDataMgmt) {
        ExceptionListHis exceptionListHis = new ExceptionListHis();
        exceptionListHis.setImei(exceptionList.getImei());
        exceptionListHis.setImsi(exceptionList.getImsi());
        exceptionListHis.setMsisdn(exceptionList.getMsisdn());
        exceptionListHis.setOperation(operation);
        exceptionListHis.setRemarks(exceptionList.getRemarks());
        exceptionListHis.setActualImei(exceptionList.getActualImei());
//
        exceptionListHis.setModeType(exceptionList.getModeType());
        exceptionListHis.setOperatorName(exceptionList.getOperatorName());

        exceptionListHis.setRequestType(exceptionList.getRequestType());
        exceptionListHis.setTxnId(listDataMgmt.getTransactionId());
        exceptionListHis.setUserId(listDataMgmt.getUserId());

        exceptionListHis.setTac(exceptionList.getTac());
        exceptionListHis.setSource(exceptionList.getSource());
        return exceptionListHis;
    }
}
