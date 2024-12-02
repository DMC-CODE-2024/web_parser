package com.glocks.web_parser.model.app;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "search_imei_detail_by_police")
@Setter
@Getter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchImeiDetailByPolice implements Serializable {
    private static final long serialVersionUID = -2772314216410962795L;
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

    @Column(name = "imei")
    private String imei;

    @Column(name = "lost_date_time")
    private LocalDateTime lostDateTime;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "request_id")
    private String requestId;

    @Column(name = "device_owner_name")
    private String deviceOwnerName;

    @Column(name = "device_owner_address")
    private String deviceOwnerAddress;

    @Column(name = "contact_number")
    private String contactNumber;

    @Column(name = "device_owner_national_id")
    private String deviceOwnerNationalId;

    @Column(name = "device_lost_police_station")
    private String deviceLostPoliceStation;

    @Column(name = "request_mode")
    private String requestMode;

}
