package com.glocks.web_parser.model.app;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "duplicate_device_detail")
public class DuplicateDeviceDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_on")
    LocalDateTime createdOn;

    /*
        @UpdateTimestamp
        @Temporal(TemporalType.TIMESTAMP)
        @Column(name = "modified_on")
        LocalDateTime modifiedOn;
    */
    @Column(name = "imei")
    String imei;
    @Column(name = "msisdn")
    String msisdn;

    @Column(name = "status")
    String status;

}
