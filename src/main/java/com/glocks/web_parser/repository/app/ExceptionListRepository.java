package com.glocks.web_parser.repository.app;

import com.glocks.web_parser.model.app.ExceptionList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExceptionListRepository extends JpaRepository<ExceptionList, Integer> {

    ExceptionList findExceptionListByImeiAndMsisdnAndImsi(String imei, String msisdn, String imsi);

    ExceptionList findExceptionListByImeiAndImsi(String imei, String imsi);

    ExceptionList findExceptionListByImeiAndMsisdn(String imei, String msisdn);

    ExceptionList findExceptionListByImsiAndMsisdn(String imsi, String msisdn);

    ExceptionList findExceptionListByImsi(String imsi);

    ExceptionList findExceptionListByImei(String imei);

    ExceptionList findExceptionListByMsisdn(String msisdn);
}
