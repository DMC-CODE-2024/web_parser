package com.glocks.web_parser.model.app;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "search_imei_by_police_mgmt")
@Data
public class SearchImeiByPoliceMgmt implements Serializable {
    private static final long serialVersionUID = -5230207560070120832L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn;

    @Column(name = "modified_on")
    @UpdateTimestamp
    private LocalDateTime modifiedOn;

    @Column(name = "imei1")
    private String imei1;

    @Column(name = "imei2")
    private String imei2;

    @Column(name = "imei3")
    private String imei3;

    @Column(name = "imei4")
    private String imei4;

    @Column(name = "status")
    private String status;

    @Column(name = "remark")
    private String remark;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "transaction_id", unique = true)
    private String transactionId;

    @Column(name = "request_mode")
    private String requestMode;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_record_count")
    private Integer fileRecordCount;

    @Column(name = "count_found_in_lost")
    private Integer countFoundInLost;

    @Column(name = "fail_reason")
    private String failReason;

}
