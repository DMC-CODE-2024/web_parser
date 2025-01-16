package com.glocks.web_parser.builder;

import com.glocks.web_parser.model.app.BlockedTacList;
import com.glocks.web_parser.model.app.BlockedTacListHis;
import com.glocks.web_parser.model.app.ListDataMgmt;
import lombok.Builder;
import org.springframework.stereotype.Component;

@Component
@Builder
public class BlockedTacListHisBuilder {

    public static BlockedTacListHis forInsert(BlockedTacList blockedTacList, int operation, ListDataMgmt listDataMgmt) {
        BlockedTacListHis blockedTacListHis = new BlockedTacListHis();
        blockedTacListHis.setOperation(operation);
        blockedTacListHis.setRemarks(blockedTacList.getRemarks());//
        blockedTacListHis.setModeType(blockedTacList.getModeType());
        blockedTacListHis.setRequestType(blockedTacList.getRequestType());
        blockedTacListHis.setTxnId(listDataMgmt.getTransactionId());
        blockedTacListHis.setUserId(listDataMgmt.getUserId());
        blockedTacListHis.setTac(blockedTacList.getTac());
        blockedTacListHis.setSource(blockedTacList.getSource());
        return blockedTacListHis;
    }
}
