package com.glocks.web_parser.model.app;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Column;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name="eirs_list_mgmt")
public class ListDataMgmt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="created_on")
    LocalDateTime createdOn;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="modified_on")
    LocalDateTime modifiedOn;

    @Column(name="msisdn")
    String msisdn;

    @Column(name="imei")
    String imei;

    @Column(name="imsi")
    String imsi;

    @Column(name="tac")
    String tac;

    @Column(name="file_name")
    String fileName;

    @Column(name="request_mode")
    String requestMode;

    @Column(name="remark")
    String remarks;

    @Column(name="status")
    String status;

    @Column(name="category")
    String category;

    @Column(name="transaction_id")
    String transactionId;

    @Column(name="user_id")
    String userId;

    @Column(name="quantity")
    String quantity;

    @Column(name="action")
    String action;

    @Column(name="request_type")
    String requestType;

    @Column(name="total_count")
    long totalCount;

    @Column(name="success_count")
    long successCount;

    @Column(name="failure_count")
    long failureCount;

}
