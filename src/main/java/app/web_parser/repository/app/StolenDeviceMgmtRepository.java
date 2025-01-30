package app.web_parser.repository.app;

import app.web_parser.model.app.StolenDeviceMgmt;
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
public interface StolenDeviceMgmtRepository extends JpaRepository<StolenDeviceMgmt, Long> {
    Optional<StolenDeviceMgmt> findByRequestId(String requestId);

    Optional<StolenDeviceMgmt> findByLostId(String lostId);

    @Modifying
    @Query("UPDATE StolenDeviceMgmt x SET x.status =:status WHERE x.requestId =:requestId")
    public int updateStatus(String status, String requestId);

    @Modifying
    @Query("UPDATE StolenDeviceMgmt x SET x.status =:status, x.userStatus =:userStatus  WHERE x.requestId =:requestId")
    public int updateUserStatus(String status, String userStatus,String requestId);

    @Query("SELECT COUNT(e) FROM StolenDeviceMgmt e WHERE " +
            "(e.imei1 = :imei OR e.imei2 = :imei OR e.imei3 = :imei OR e.imei4 = :imei) " +
            "AND e.status IN (:statuses)")
    long existsByImeiAndStatusIn(String imei, List<String> statuses);
}
