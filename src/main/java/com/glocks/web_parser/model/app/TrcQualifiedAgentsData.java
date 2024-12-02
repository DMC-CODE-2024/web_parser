package com.glocks.web_parser.model.app;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Entity
@Table(name="trc_qualified_agent_data")
public class TrcQualifiedAgentsData {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
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

    @Column(name="company_name")
    String companyName;

    @Column(name = "company_id")
    String companyId;

    @Column(name="phone_number")
    String phoneNumber;

    @Column(name="email")
    String email;

    @Column(name = "expiry_date")
    String expiryDate;


    public TrcQualifiedAgentsData(String[] taDataRecord) {
        this.no= Integer.parseInt(taDataRecord[0].trim());
        this.companyName=taDataRecord[1].trim();
        this.companyId = taDataRecord[2].trim();
        this.phoneNumber = taDataRecord[3].trim();
        this.email = taDataRecord[4].trim();
        this.expiryDate = stringToDate(taDataRecord[5].trim());
    }

    public String stringToDate(String inputDate) {
        return LocalDate.parse(inputDate, DateTimeFormatter.ofPattern("dd-MMM-yy"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
    public TrcQualifiedAgentsData() {

    }
}
