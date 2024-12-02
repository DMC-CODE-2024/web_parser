package com.glocks.web_parser.repository.app;

import com.glocks.web_parser.model.app.ListDataMgmt;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ListDataMgmtRepository extends JpaRepository<ListDataMgmt, Integer> {


    ListDataMgmt findByTransactionId (String transactionId);

    @Transactional
    @Modifying
    @Query("update ListDataMgmt u set u.status=:status, u.modifiedOn=:modifiedOn, u.remarks= :remarks where u.id=:id")
    void updateListDataMgmtStatus(@Param("status") String status, @Param("modifiedOn") LocalDateTime modifiedOn,
                                 @Param("remarks") String remarks, @Param("id") long id);

    @Transactional
    @Modifying
    @Query("update ListDataMgmt u set u.status=:status, u.modifiedOn=:modifiedOn where u.id=:id")
    void updateListDataMgmtStatus(@Param("status") String status, @Param("modifiedOn") LocalDateTime modifiedOn,
                                  @Param("id") long id);

    @Transactional
    @Modifying
    @Query("update ListDataMgmt u set u.status=:status, u.modifiedOn=:modifiedOn, u.totalCount=:totalCount, " +
            "u.successCount=:successCount, u.failureCount=:failureCount where u.id=:id")
    void updateListDataMgmtStatus(@Param("status") String status, @Param("modifiedOn") LocalDateTime modifiedOn,
                                  @Param("id") long id, @Param("totalCount") long totalCount,
                                  @Param("successCount") long successCount,
                                  @Param("failureCount") long failureCount);

}
