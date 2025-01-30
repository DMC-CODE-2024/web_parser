package app.web_parser.model.app;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "mobile_device_repository")
@Data
public class MobileDeviceRepository implements Serializable {
    private static final long serialVersionUID = 7484467909153119352L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "brand_name")
    private String brandName = "";

    @CreationTimestamp
    @Column(name = "created_on", columnDefinition = "timestamp DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdOn;

    @UpdateTimestamp
    @Column(name = "modified_on", columnDefinition = "timestamp DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime modifiedOn;

    @Column(name = "device_id", length = 8, columnDefinition = "varchar(8) DEFAULT '0'", unique = true)
    private String deviceId;

    @Column(name = "model_name")
    private String modelName;

    @Column(name = "device_type")
    private String deviceType;

    @Column(name = "is_type_approved")
    private int isTypeApproved;

}

