package com.glocks.web_parser.repository.app;

import com.glocks.web_parser.model.app.HlrDump;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HlrDumpRepository extends JpaRepository<HlrDump, Integer> {

    @Query("select u.imsi from HlrDump u where u.msisdn= :msisdn")
    String findImsi(@Param("msisdn") String msisdn);
}
