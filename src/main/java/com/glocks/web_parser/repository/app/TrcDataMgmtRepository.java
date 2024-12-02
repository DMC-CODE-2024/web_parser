package com.glocks.web_parser.repository.app;

import com.glocks.web_parser.model.app.TrcDataMgmt;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface TrcDataMgmtRepository extends JpaRepository<TrcDataMgmt, Long> {

//    @Query("select u from TrcDataMgmt u where u.transactionId=:transactionId")
    TrcDataMgmt findByTransactionId (String transactionId);

    @Transactional
    @Modifying
    @Query("update TrcDataMgmt u set u.status=:status, u.modifiedOn=:modifiedOn, u.processedRemarks= :processedRemarks where u.id=:id")
    void updateTrcDataMgmtStatus(@Param("status") String status, @Param("modifiedOn")LocalDateTime modifiedOn,
                                 @Param("processedRemarks") String processedRemarks, @Param("id") long id);

    @Query("select u from TrcDataMgmt u where u.status like :status and u.requestType=:requestType order by u.id desc limit 1")
    TrcDataMgmt getFileName(@Param("status") String status, @Param("requestType") String requestType);

    @Transactional
    @Modifying
    @Query("update TrcDataMgmt u set u.status=:status, u.modifiedOn=:modifiedOn, u.processedRemarks= :processedRemarks, " +
            " u.totalCount= :totalCount, u.addCount= :addCount,  u.deleteCount= :deleteCount, u.failureCount= :failureCount " +
            " where u.id=:id")
    void updateTrcDataMgmtStatus(@Param("status") String status, @Param("modifiedOn")LocalDateTime modifiedOn,
                                 @Param("processedRemarks") String processedRemarks, @Param("id") long id,
                                 @Param("totalCount") long totalCount, @Param("addCount") long addCount,
                                 @Param("deleteCount") long deleteCount, @Param("failureCount") long failureCount
                                 );
}
