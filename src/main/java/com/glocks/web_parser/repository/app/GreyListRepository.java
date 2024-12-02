package com.glocks.web_parser.repository.app;

import com.glocks.web_parser.model.app.GreyList;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
@Transactional(rollbackOn = {SQLException.class})
public interface GreyListRepository extends JpaRepository<GreyList, Long> {

    GreyList findGreyListByImeiAndImsi(String imei, String imsi);

    GreyList findGreyListByImsi(String imsi);

    GreyList findGreyListByImei(String imei);

    @Modifying
    @Query("UPDATE GreyList x SET x.source =:source WHERE x.imei =:imei")
    public int updateSource(String source, String imei);
}
