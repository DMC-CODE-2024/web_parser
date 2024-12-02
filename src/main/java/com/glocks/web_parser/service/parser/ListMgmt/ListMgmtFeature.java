package com.glocks.web_parser.service.parser.ListMgmt;

import com.glocks.web_parser.model.app.ListDataMgmt;
import com.glocks.web_parser.model.app.WebActionDb;
import com.glocks.web_parser.repository.app.ListDataMgmtRepository;
import com.glocks.web_parser.service.parser.FeatureInterface;
import com.glocks.web_parser.service.parser.ListMgmt.blackList.BlackListSubFeature;
import com.glocks.web_parser.service.parser.ListMgmt.blockedTac.BlockedTacListSubFeature;
import com.glocks.web_parser.service.parser.ListMgmt.exceptionList.ExceptionListSubFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ListMgmtFeature implements FeatureInterface {

    @Autowired
    ExceptionListSubFeature exceptionListSubFeature;
    @Autowired
    BlackListSubFeature blackListSubFeature;
    @Autowired
    BlockedTacListSubFeature blockedTacListSubFeature;
    @Autowired
    ListDataMgmtRepository listDataMgmtRepository;

    @Override
    public void executeInit(WebActionDb wb) {

        ListDataMgmt listDataMgmt = listDataMgmtRepository.findByTransactionId(wb.getTxnId());
        if(wb.getSubFeature().equalsIgnoreCase("Exception_List")) {
            exceptionListSubFeature.delegateInitRequest(wb, listDataMgmt);
        }
        else if(wb.getSubFeature().equalsIgnoreCase(("Black_list"))) {
            blackListSubFeature.delegateInitRequest(wb, listDataMgmt);
        }
        else if(wb.getSubFeature().equalsIgnoreCase("blocked_tac_list")) {
            blockedTacListSubFeature.delegateInitRequest(wb, listDataMgmt);
        }
    }

    @Override
    public void executeProcess(WebActionDb wb) {
        ListDataMgmt listDataMgmt = listDataMgmtRepository.findByTransactionId(wb.getTxnId());
        if(wb.getSubFeature().equalsIgnoreCase("Exception_List")) {
            exceptionListSubFeature.delegateExecuteProcess(wb, listDataMgmt);
        }
        else if(wb.getSubFeature().equalsIgnoreCase(("Black_list"))) {
            blackListSubFeature.delegateExecuteProcess(wb, listDataMgmt);
        }
        else if(wb.getSubFeature().equalsIgnoreCase("blocked_tac_list")) {
            blockedTacListSubFeature.delegateExecuteProcess(wb, listDataMgmt);
        }
    }

    @Override
    public void validateProcess(WebActionDb wb) {

        ListDataMgmt listDataMgmt = listDataMgmtRepository.findByTransactionId(wb.getTxnId());
        if(wb.getSubFeature().equalsIgnoreCase("Exception_List")) {
            exceptionListSubFeature.delegateValidateRequest(wb, listDataMgmt);
        }
        else if(wb.getSubFeature().equalsIgnoreCase(("Black_list"))) {
            blackListSubFeature.delegateValidateRequest(wb, listDataMgmt);
        }
        else if(wb.getSubFeature().equalsIgnoreCase("blocked_tac_list")) {
            blockedTacListSubFeature.delegateValidateRequest(wb, listDataMgmt);
        }

    }

}
