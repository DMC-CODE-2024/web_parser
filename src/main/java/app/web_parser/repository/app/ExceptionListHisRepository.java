package app.web_parser.repository.app;


import app.web_parser.model.app.ExceptionListHis;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
@Transactional(rollbackOn = {SQLException.class})
public interface ExceptionListHisRepository extends JpaRepository<ExceptionListHis, Integer> {
    @Modifying
    @Query("UPDATE ExceptionListHis x SET x.source =:source WHERE x.imei =:imei")
    public int updateSource(String source, String imei);
}
