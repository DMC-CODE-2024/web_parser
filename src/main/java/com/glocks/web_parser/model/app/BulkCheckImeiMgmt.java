package com.glocks.web_parser.model.app;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name="bulk_check_imei_mgmt")
public class BulkCheckImeiMgmt {

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
    @Column(name="file_name")
    String fileName;

    @Column(name="status")
    String status;

    @Column(name="contact_number")
    String contactNumber;

    @Column(name="transaction_id")
    String transactionId;

    @Column(name="email")
    String email;

    @Column(name="otp")
    String otp;

    @Column(name="language")
    String language;
    @Column(name="total_count")
    long totalCount;

    @Column(name="success_count")
    long successCount;

    @Column(name="failure_count")
    long failureCount;

}
