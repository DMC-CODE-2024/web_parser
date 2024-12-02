package com.glocks.web_parser.model.app;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "imei_pair_detail_his")
@Data
public class ImeiPairDetailHis implements Serializable {
    private static final long serialVersionUID = -11529872387504297L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_on")
    LocalDateTime createdOn;

/*    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modified_on")
    LocalDateTime modifiedOn;*/

    @Column(name = "file_name")
    private String fileName;

    @Enumerated(EnumType.STRING)
    @Column(name = "gsma_status")
    private GsmaStatus gsmaStatus; // Enum for INVALID, VALID

    @Column(name = "pairing_date")
    private LocalDateTime pairingDate;

    @Column(name = "record_time")
    private LocalDateTime recordTime;

    @Column(name = "msisdn")
    private String msisdn;

    @Column(name = "imei")
    private String imei;

    @Column(name = "imsi")
    private String imsi;

    @Column(name = "operator")
    private String operator;

    @Column(name = "allowed_days", nullable = true, columnDefinition = "int default 60")
    private Integer allowedDays;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "pair_mode")
    private String pairMode;

    @Column(name = "actual_imei")
    private String actualImei;

 /*   @Column(name = "request_id")
    private String requestId;

    @Column(name = "txn_id")
    private String txnId;
*/
    @Column(name = "action")
    private String action;

    @Column(name = "action_remark")
    private String actionRemark;

}
