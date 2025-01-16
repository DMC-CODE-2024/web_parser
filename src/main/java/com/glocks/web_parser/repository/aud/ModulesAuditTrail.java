package com.glocks.web_parser.repository.aud;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "modules_audit_trail")
@Data
public class ModulesAuditTrail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_on")
    LocalDateTime createdOn;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modified_on")
    LocalDateTime modifiedOn;

    @Column(name = "execution_time")
    private String executionTime;

    @Column(name = "status_code")
    private int statusCode;

    @Column(name = "status")
    private String status;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "module_name")
    private String moduleName;

    @Column(name = "feature_name")
    private String featureName;

    @Column(name = "action")
    private String action;

    @Column(name = "count")
    private long count;

    @Column(name = "info")
    private String info;

    @Column(name = "server_name")
    private String serverName;

    @Column(name = "count2")
    private int count2;

    @Column(name = "failure_count")
    private int failureCount;

}