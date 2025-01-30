package app.web_parser.builder;


import app.web_parser.dto.ListMgmtDto;
import app.web_parser.model.app.BlackList;
import app.web_parser.model.app.ListDataMgmt;
import org.apache.commons.lang3.StringUtils;
import lombok.Builder;
import org.springframework.stereotype.Component;

@Component
@Builder
public class BlackListBuilder {


    public static BlackList forInsert(ListDataMgmt listDataMgmt, String operatorName) {
        BlackList blackList = new BlackList();
        var actualImei = StringUtils.isBlank(listDataMgmt.getImei())
                ? "" : listDataMgmt.getImei();
        var imei = actualImei.length() > 14 ? actualImei.substring(0, 14) : actualImei;
        blackList.setImei(imei);
        blackList.setImsi(listDataMgmt.getImsi());
        blackList.setMsisdn(listDataMgmt.getMsisdn());
        blackList.setRemarks(listDataMgmt.getRemarks());
        blackList.setActualImei(actualImei);
        blackList.setModeType(listDataMgmt.getRequestMode());
        blackList.setOperatorName(operatorName);
//        exceptionList.setOperatorId(exceptionList.getOperatorId());
        blackList.setRequestType(listDataMgmt.getCategory());
        blackList.setTxnId(listDataMgmt.getTransactionId());
        blackList.setUserId(listDataMgmt.getUserId());
//        exceptionList.setUserType(listDataMgmt.getUserType());
        blackList.setTac(((listDataMgmt.getImei() == null) || (listDataMgmt.getImei().equalsIgnoreCase(""))) ? null : listDataMgmt.getImei().length() > 8 ? listDataMgmt.getImei().substring(0, 8) : listDataMgmt.getImei());
        blackList.setSource("EIRSAdmin");
        return blackList;
    }

    public static BlackList forInsert(ListDataMgmt listDataMgmt, ListMgmtDto listMgmtDto, String operatorName) {
        BlackList blackList = new BlackList();
        var actualImei = StringUtils.isBlank(listMgmtDto.getImei())
                ? "" : listMgmtDto.getImei();
        var imei = actualImei.length() > 14 ? actualImei.substring(0, 14) : actualImei;
        blackList.setImei(imei);
        blackList.setImsi(listMgmtDto.getImsi());
        blackList.setMsisdn(listMgmtDto.getMsisdn());
        blackList.setRemarks(listDataMgmt.getRemarks());
        blackList.setActualImei(actualImei);
        blackList.setModeType(listDataMgmt.getRequestMode());
        blackList.setOperatorName(operatorName);
//        exceptionList.setOperatorId(exceptionList.getOperatorId());
        blackList.setRequestType(listDataMgmt.getCategory());
        blackList.setTxnId(listDataMgmt.getTransactionId());
        blackList.setUserId(listDataMgmt.getUserId());
//        exceptionList.setUserType(listDataMgmt.getUserType());
        blackList.setTac(imei.length() > 8 ? imei.substring(0, 8) : imei);
        blackList.setSource("EIRSAdmin");
        return blackList;
    }
}
