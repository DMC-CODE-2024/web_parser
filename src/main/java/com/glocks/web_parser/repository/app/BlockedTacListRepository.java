package com.glocks.web_parser.repository.app;

import com.glocks.web_parser.model.app.BlockedTacList;
import org.springframework.data.jpa.repository.JpaRepository;


public interface BlockedTacListRepository extends JpaRepository<BlockedTacList, Integer> {


    BlockedTacList findBlockedTacListByTac(String tac);
}
