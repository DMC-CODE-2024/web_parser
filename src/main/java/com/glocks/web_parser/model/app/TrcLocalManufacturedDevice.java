package com.glocks.web_parser.model.app;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name="trc_local_manufactured_device_data")
public class    TrcLocalManufacturedDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="created_on")
    LocalDateTime createdOn;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="modified_on")
    LocalDateTime modifiedOn;

    @Column(name="imei")
    String imei;

    @Column(name="actual_imei")
    String actualImei;

    @Column(name="tac")
    String tac;

    @Column(name="serial_number")
    String serialNumber;

    @Column(name="manufacturer_id")
    String manufacturerId;

    @Column(name="manufacturer_name")
    String manufacturerName;

    @Column(name="manufacturing_date")
    String manufactureringDate;


    public TrcLocalManufacturedDevice(String[] taDataRecord) {
        this.imei= taDataRecord[0].trim().substring(0,14);
        this.serialNumber=taDataRecord[1].trim();
        this.manufacturerId=taDataRecord[2].trim();
        this.manufacturerName=taDataRecord[3].trim();
        this.manufactureringDate=taDataRecord[4].trim();
        this.actualImei=taDataRecord[0].trim();
        this.tac=taDataRecord[0].trim().substring(0,8);
    }

    public TrcLocalManufacturedDevice() {

    }




}
