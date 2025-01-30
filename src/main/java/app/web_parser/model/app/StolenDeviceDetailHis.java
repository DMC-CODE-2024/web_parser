package app.web_parser.model.app;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "lost_device_detail_his")
@Data
@Builder
public class StolenDeviceDetailHis implements Serializable {
    private static final long serialVersionUID = -8978714783467931014L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn;

    @Column(name = "modified_on")
    @UpdateTimestamp
    private LocalDateTime modifiedOn;
    @Column(name = "imei", unique = true)
    private String imei;
    @Column(name = "request_type")
    private String requestType;
    @Column(name = "request_id")
    private String requestId;
    @Column(name = "operation")
    int operation;


/*    @Column(name = "contact_number")
    private String contactNumber;

    @Column(name = "device_brand")
    private String deviceBrand;

    @Column(name = "device_model")
    private String deviceModel;

    @Column(name = "status")
    private String status;

    @Column(name = "lost_stolen_request_id")
    private String lostStolenRequestId;*/

}

