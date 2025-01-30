package app.web_parser.model.app;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "lost_device_mgmt")
@Data
public class StolenDeviceMgmt implements Serializable {
    private static final long serialVersionUID = -9193873433253773828L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_dob")
    private String ownerDOB;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_on")
    LocalDateTime createdOn;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modified_on")
    LocalDateTime modifiedOn;

    @Column(name = "contact_number")
    private String contactNumber;


    @Column(name = "imei1")
    private String imei1;

    @Column(name = "imei2")
    private String imei2;

    @Column(name = "imei3")
    private String imei3;

    @Column(name = "imei4")
    private String imei4;
    @Column(name = "device_lost_date_time")
    private String deviceLostDateTime;

    @Column(name = "device_brand")
    private String deviceBrand;

    @Column(name = "device_model")
    private String deviceModel;

    @Column(name = "device_owner_email")
    private String deviceOwnerEmail;

    /*
        @Column(name = "device_purchase_invoice_url")
        private String devicePurchaseInvoiceUrl;

        @Column(name = "device_owner_national_id_url")
        private String deviceOwnerNationalIdUrl;
    */
    @Column(name = "device_owner_national_id")
    private String deviceOwnerNationalID;
    @Column(name = "device_owner_address")
    private String deviceOwnerAddress;

    @Column(name = "device_owner_name")
    private String deviceOwnerName;

    @Column(name = "device_owner_nationality")
    private String deviceOwnerNationality;

    @Column(name = "contact_number_for_otp")
    private String contactNumberForOtp;

   /* @Column(name = "otp")
    private String otp;

    @Column(name = "fir_copy_url")
    private String firCopyUrl;

*/
   @Column(name = "remark")
   private String remark;
    @Column(name = "status")
    private String status;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "request_type")
    private String requestType;

    @Column(name = "request_id")
    private String requestId;

    @Column(name = "request_mode")
    private String requestMode;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_record_count")
    private String fileRecordCount;

    /*@Column(name = "mobile_invoice_bill")
    private String mobileInvoiceBill;

    @Column(name = "device_owner_address2")
    private String deviceOwnerAddress2;

    @Column(name = "recovery_reason")
    private String recoveryReason;

    @Column(name = "device_lost_province_city")
    private String deviceLostProvinceCity;

    @Column(name = "device_lost_district")
    private String deviceLostDistrict;

    @Column(name = "device_lost_commune")
    private String deviceLostCommune;

    @Column(name = "owner_passport_number")
    private String ownerPassportNumber;

    @Column(name = "email_for_otp")
    private String emailForOtp;

    @Column(name = "category")
    private String category;

    @Column(name = "user_status")
    private String userStatus;

*/
    @Column(name = "user_status")
    private String userStatus;

    @Column(name = "police_station")
    private String policeStation;

    @Column(name = "email_for_otp")
    private String emailForOtp;

    @Column(name = "language")
    private String language;

    @Column(name = "lost_id")
    private String lostId;
/*
    @Column(name = "serial_number")
    private String serialNumber;


    @Column(name = "incident_detail")
    private String incidentDetail;

    @Column(name = "device_type")
    private String deviceType;

    @Column(name = "otp_retry_count")
    private int otpRetryCount;
*/


    public void setImei(String[] record) {
        for (int i = 0; i < record.length; i++) {
            switch (i) {
                case 0 -> this.imei1 = recordIndexWise(record[i]);
                case 1 -> this.imei2 = recordIndexWise(record[i]);
                case 2 -> this.imei3 = recordIndexWise(record[i]);
                case 3 -> this.imei4 = recordIndexWise(record[i]);
            }
        }
    }

    public String recordIndexWise(String input) {
        return Objects.nonNull(input) ? input.strip() : null;
    }

    public boolean areAllFieldsEmpty() {
        return imei1.isEmpty() && imei2.isEmpty() && imei3.isEmpty() && imei4.isEmpty();
    }
}
