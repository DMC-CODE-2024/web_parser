package com.glocks.web_parser.model.app;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name="trc_type_approved_data")
public class TrcTypeApprovedData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="created_on")
    LocalDateTime createdOn;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)

    @Column(name="modified_on")
    LocalDateTime modifiedOn;

    @Column(name="no")
    int no;

    @Column(name="company")
    String company;

    @Column(name = "company_id")
    String companyId;


    @Column(name="trademark")
    String trademark;

    @Column(name="product_name")
    String productName;

    @Column(name = "commercial_name")
    String commercialName;

    @Column(name="model")
    String model;
    @Column(name = "country_of_manufacture")
    String countryOfManufacture;

    @Column(name="trc_identifier")
    String trcIdentifier;

    @Column(name="approved_date")
    String approvedDate;


    public TrcTypeApprovedData(String[] taDataRecord) {
        this.no= Integer.parseInt(taDataRecord[0].trim());
        this.company=taDataRecord[1].trim();
        this.companyId = taDataRecord[2].trim();
        this.trademark = taDataRecord[3].trim();
        this.productName = taDataRecord[4].trim();
        this.commercialName = taDataRecord[5].trim();
        this.model = taDataRecord[6].trim();
        this.countryOfManufacture = taDataRecord[7].trim();
        this.trcIdentifier=taDataRecord[8].trim();
        this.approvedDate = taDataRecord[9].trim();
    }

    public TrcTypeApprovedData() {

    }
}
