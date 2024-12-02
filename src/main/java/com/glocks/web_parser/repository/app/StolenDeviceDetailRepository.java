package com.glocks.web_parser.repository.app;

import com.glocks.web_parser.model.app.StolenDeviceDetail;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional(rollbackOn = {SQLException.class})
public interface StolenDeviceDetailRepository extends JpaRepository<StolenDeviceDetail, Long> {
    Boolean existsByImeiAndStatusIgnoreCaseAndRequestTypeIgnoreCaseIn(String imei, String status, List<String> requestTypes);

    @Query("SELECT x.requestId FROM StolenDeviceDetail x WHERE x.imei =:imei")
    Optional<String> findStolenDeviceDetailByImei(String imei);

    Optional<StolenDeviceDetail> findByImeiAndStatusIgnoreCaseAndRequestTypeIgnoreCaseIn(String imei, String status, List<String> requestType);

    int deleteByImeiAndRequestTypeIgnoreCaseIn(String imei, List<String> status);

    Boolean existsByImei(String imei);

    @Modifying
    @Query("UPDATE StolenDeviceDetail x SET x.status =:status WHERE x.imei =:imei")
    public int updateStatus(String status, String imei);
}
