package app.web_parser.builder;

import app.web_parser.dto.BlockedTacDto;
import app.web_parser.model.app.BlockedTacList;
import app.web_parser.model.app.ListDataMgmt;
import lombok.Builder;
import org.springframework.stereotype.Component;


@Component
@Builder
public class BlockedTacListBuilder {


    public static BlockedTacList forInsert(ListDataMgmt listDataMgmt) {
        BlockedTacList blockedTacList = new BlockedTacList();
        blockedTacList.setTac(listDataMgmt.getTac());
        blockedTacList.setRemarks(listDataMgmt.getRemarks());
        blockedTacList.setModeType(listDataMgmt.getRequestMode());
        blockedTacList.setRequestType(listDataMgmt.getCategory());
        blockedTacList.setTxnId(listDataMgmt.getTransactionId());
        blockedTacList.setUserId(listDataMgmt.getUserId());
        blockedTacList.setSource("EIRSAdmin");
        return blockedTacList;
    }

    public static BlockedTacList forInsert(ListDataMgmt listDataMgmt, BlockedTacDto blockedTacDto) {
        BlockedTacList blockedTacList = new BlockedTacList();
        blockedTacList.setRemarks(listDataMgmt.getRemarks());
        blockedTacList.setModeType(listDataMgmt.getRequestMode());
        blockedTacList.setRequestType(listDataMgmt.getCategory());
        blockedTacList.setTxnId(listDataMgmt.getTransactionId());
        blockedTacList.setUserId(listDataMgmt.getUserId());
        blockedTacList.setTac(blockedTacDto.getTac());
        blockedTacList.setSource("EIRSAdmin");
        return blockedTacList;
    }


}




