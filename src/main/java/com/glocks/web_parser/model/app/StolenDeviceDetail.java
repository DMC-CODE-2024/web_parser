package com.glocks.web_parser.model.app;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "lost_device_detail")
@Setter
@Getter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class StolenDeviceDetail implements Serializable {
    private static final long serialVersionUID = -9193873433253773828L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn;

    @Column(name = "modified_on")
    @UpdateTimestamp
    private LocalDateTime modifiedOn;

    @Column(name = "contact_number")
    private String contactNumber;

    @Column(name = "imei", unique = true)
    private String imei;

    @Column(name = "request_id")
    private String requestId;

    @Column(name = "device_brand")
    private String deviceBrand;

    @Column(name = "device_model")
    private String deviceModel;

    @Column(name = "request_type")
    private String requestType;

    @Column(name = "status")
    private String status;

    @Column(name = "lost_stolen_request_id")
    private String lostStolenRequestId;


    @PrePersist
    public void setDefaultValues() {
        if (this.contactNumber == null || this.contactNumber.isEmpty()) {
            this.contactNumber = "0000-1111";
        }
    }


}

