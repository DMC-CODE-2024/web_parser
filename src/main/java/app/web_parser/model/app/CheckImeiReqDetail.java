package app.web_parser.model.app;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "check_imei_req_detail")
public class CheckImeiReqDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_on")
    LocalDateTime createdOn;

    @Column(name = "language")
    String language;

    @Column(name = "imei")
    String imei;

    @Column(name = "compliance_status")
    String complianceStatus;

    @Column(name = "channel")
    String channel;

    @Column(name = "request_id")
    String requestId;

    @Column(name = "fail_process_description")
    String failProcessDescription;

    @Column(name = "request_process_status")
    String requestProcessStatus;

    @Column(name = "imei_process_status")
    String imeiProcessStatus;

    @Column(name = "header_browser")
    String headerBrowser;

    @Column(name = "header_public_ip")
    String headerPublicIp;

    @Column(name = "brand_name")
    String brandName;

    @Column(name = "model_name")
    String modelName;

    @Column(name = "manufacturer")
    String manufacturer;

    @Column(name = "marketing_name")
    String marketingName;

    @Column(name = "device_type")
    String deviceType;


}
