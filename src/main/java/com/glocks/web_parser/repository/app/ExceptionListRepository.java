package com.glocks.web_parser.repository.app;

import com.glocks.web_parser.model.app.ExceptionList;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
@Transactional(rollbackOn = {SQLException.class})
public interface ExceptionListRepository extends JpaRepository<ExceptionList, Integer> {

    ExceptionList findExceptionListByImeiAndMsisdnAndImsi(String imei, String msisdn, String imsi);

    ExceptionList findExceptionListByImeiAndImsi(String imei, String imsi);

    ExceptionList findExceptionListByImeiAndMsisdn(String imei, String msisdn);

    ExceptionList findExceptionListByImsiAndMsisdn(String imsi, String msisdn);

    ExceptionList findExceptionListByImsi(String imsi);

    ExceptionList findExceptionListByImei(String imei);

    ExceptionList findExceptionListByMsisdn(String msisdn);

    @Modifying
    @Query("UPDATE ExceptionList x SET x.source =:source WHERE x.imei =:imei")
    public int updateSource(String source, String imei);
}
