package app.web_parser.builder;


import app.web_parser.dto.ListMgmtDto;
import app.web_parser.model.app.ExceptionList;
import app.web_parser.model.app.ListDataMgmt;
import org.apache.commons.lang3.StringUtils;
import lombok.Builder;
import org.springframework.stereotype.Component;

//import static com.gl.eirs.simchange.constants.Constants.remarks;

@Component
@Builder
public class ExceptionListBuilder {

    public static ExceptionList forInsert(ListDataMgmt listDataMgmt, String operatorName) {
        var actualImei = StringUtils.isBlank(listDataMgmt.getImei())
                ? "" : listDataMgmt.getImei();
        var imei = actualImei.length() > 14 ? actualImei.substring(0, 14) : actualImei;
        ExceptionList exceptionList = new ExceptionList();
        exceptionList.setImei(imei);
        exceptionList.setImsi(listDataMgmt.getImsi());
        exceptionList.setMsisdn(listDataMgmt.getMsisdn());
        exceptionList.setRemarks(listDataMgmt.getRemarks());
        exceptionList.setActualImei(actualImei);
        exceptionList.setModeType(listDataMgmt.getRequestMode());
        exceptionList.setOperatorName(operatorName);
//        exceptionList.setOperatorId(exceptionList.getOperatorId());
        exceptionList.setRequestType(listDataMgmt.getCategory());
        exceptionList.setTxnId(listDataMgmt.getTransactionId());
        exceptionList.setUserId(listDataMgmt.getUserId());
//        exceptionList.setUserType(listDataMgmt.getUserType());
        exceptionList.setTac(((listDataMgmt.getImei() == null) || (listDataMgmt.getImei().equalsIgnoreCase(""))) ? null : listDataMgmt.getImei().length() > 8 ? listDataMgmt.getImei().substring(0, 8) : listDataMgmt.getImei());
        exceptionList.setSource("EIRSAdmin");
        return exceptionList;
    }

    public static ExceptionList forInsert(ListDataMgmt listDataMgmt, ListMgmtDto listMgmtDto, String operatorName) {
        var actualImei = StringUtils.isBlank(listMgmtDto.getImei())
                ? "" : listMgmtDto.getImei();
        var imei = actualImei.length() > 14 ? actualImei.substring(0, 14) : actualImei;
        ExceptionList exceptionList = new ExceptionList();
        exceptionList.setImei(imei);
        exceptionList.setImsi(listMgmtDto.getImsi());
        exceptionList.setMsisdn(listMgmtDto.getMsisdn());
        exceptionList.setRemarks(listDataMgmt.getRemarks());
        exceptionList.setActualImei(actualImei);
        exceptionList.setModeType(listDataMgmt.getRequestMode());
        exceptionList.setOperatorName(operatorName);
//        exceptionList.setOperatorId(exceptionList.getOperatorId());
        exceptionList.setRequestType(listDataMgmt.getCategory());
        exceptionList.setTxnId(listDataMgmt.getTransactionId());
        exceptionList.setUserId(listDataMgmt.getUserId());
//        exceptionList.setUserType(listDataMgmt.getUserType());
        exceptionList.setTac(imei.length() > 8 ? imei.substring(0, 8) : imei);
        exceptionList.setSource("EIRSAdmin");
        return exceptionList;
    }

}
