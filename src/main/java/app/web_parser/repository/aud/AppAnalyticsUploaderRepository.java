package app.web_parser.repository.aud;

import app.web_parser.model.aud.AppAnalyticsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.sql.SQLException;

@Repository
@Transactional(rollbackOn = {SQLException.class})
public interface AppAnalyticsUploaderRepository extends JpaRepository<AppAnalyticsEntity, Long>, JpaSpecificationExecutor<AppAnalyticsEntity> {

    AppAnalyticsEntity findByTransactionId(String transactionId);

    @Modifying
    @Transactional
    @Query("UPDATE AppAnalyticsEntity a SET a.insertCount = :insertCount, a.status = :status , a.reason = :reason WHERE a.transactionId = :transactionId ")
    void updateCountOfRecordsandStatusandReason(String transactionId, int insertCount, String status, String reason);

    @Modifying
    @Transactional
    @Query("UPDATE AppAnalyticsEntity a SET a.insertCount = :insertCount, a.status = :status WHERE a.transactionId = :transactionId ")
    void updateCountOfRecordsandStatus(String transactionId, int insertCount, String status);
}