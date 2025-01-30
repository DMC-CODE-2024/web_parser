package app.web_parser.repository.app;

import app.web_parser.model.app.BlockedTacList;
import org.springframework.data.jpa.repository.JpaRepository;


public interface BlockedTacListRepository extends JpaRepository<BlockedTacList, Integer> {


    BlockedTacList findBlockedTacListByTac(String tac);
}
