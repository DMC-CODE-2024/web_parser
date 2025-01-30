package app.web_parser.repository.app;

import app.web_parser.model.app.SearchImeiByPoliceMgmt;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Optional;

@Repository
@Transactional(rollbackOn = {SQLException.class})
public interface SearchImeiByPoliceMgmtRepository extends JpaRepository<SearchImeiByPoliceMgmt, Long>, JpaSpecificationExecutor<SearchImeiByPoliceMgmt> {
    Optional<SearchImeiByPoliceMgmt> findByTransactionId(String txnId);

    @Modifying
    @Query("UPDATE SearchImeiByPoliceMgmt x SET x.status =:status, x.failReason =:failReason, x.countFoundInLost =:count WHERE x.transactionId =:transactionId")
    public int updateStatus(String status, String failReason,int count,String transactionId);

    @Modifying
    @Query("UPDATE SearchImeiByPoliceMgmt x  SET x.countFoundInLost =:count WHERE x.transactionId =:transactionId")
    public int updateFailCount(int count, String transactionId);


    @Modifying
    @Query("UPDATE SearchImeiByPoliceMgmt x SET x.status =:status, x.failReason =:failReason, x.countFoundInLost =:count, x.fileRecordCount =:fileRecordCount WHERE x.transactionId =:transactionId")
    public int updateStatusAndRecordCount(String status, String failReason, int count, Integer fileRecordCount, String transactionId);



}
