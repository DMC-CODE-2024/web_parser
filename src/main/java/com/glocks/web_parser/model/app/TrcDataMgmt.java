package com.glocks.web_parser.model.app;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name="trc_data_mgmt")
public class TrcDataMgmt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="created_on")
    LocalDateTime createdOn;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="modified_on")
    LocalDateTime modifiedOn;

    @Column(name="file_name")
    String fileName;

    @Column(name="status")
    String status;

    @Column(name="transaction_id")
    String transactionId;

    @Column(name="user_id")
    String userId;

    @Column(name="request_type")
    String requestType;

    @Column(name="processed_remark")
    String processedRemarks;

    @Column(name="add_count")
    long addCount;

    @Column(name="delete_count")
    long deleteCount;

    @Column(name="failure_count")
    long failureCount;

    @Column(name="total_count")
    long totalCount;
}
