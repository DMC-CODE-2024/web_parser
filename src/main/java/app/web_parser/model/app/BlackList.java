package app.web_parser.model.app;

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
@Table(name = "black_list")
public class BlackList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_on")
    LocalDateTime createdOn;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modified_on")
    LocalDateTime modifiedOn;

    @Column(name = "imsi")
    String imsi;

    @Column(name = "msisdn")
    String msisdn;

    @Column(name = "imei")
    String imei;

    @Column(name = "mode_type")
    String modeType;


    @Column(name = "request_type")
    String requestType;

    @Column(name = "txn_id")
    String txnId;

    @Column(name = "user_id")
    String userId;

    @Column(name = "operator_name")
    String operatorName;

    @Column(name = "actual_imei")
    String actualImei;

    @Column(name = "tac")
    String tac;

    @Column(name = "remark")
    String remarks;

    @Column(name = "source")
    String source;
}
