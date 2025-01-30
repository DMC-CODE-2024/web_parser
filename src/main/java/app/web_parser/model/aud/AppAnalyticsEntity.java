package app.web_parser.model.aud;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_analytics_uploader_audit", schema = "aud")
public class AppAnalyticsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "uploaded_by")
    private String uploadedBy;

    @Column(name = "uploaded_on")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime uploadedOn;

    @Column(name = "os_type")
    private String osType;

    @Column(name = "report_type")
    private String reportType;

    @Column(name = "insert_count")
    private int insertCount;

    @Column(name = "source_file_name")
    private String sourceFileName;

    @Column(name = "status")
    private String status;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "reason")
    private String reason;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public LocalDateTime getUploadedOn() {
        return uploadedOn;
    }

    public void setUploadedOn(LocalDateTime uploadedOn) {
        this.uploadedOn = uploadedOn;
    }

    public String getOsType() {
        return osType;
    }

    public void setOsType(String osType) {
        this.osType = osType;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public int getInsertCount() {
        return insertCount;
    }

    public void setInsertCount(int insertCount) {
        this.insertCount = insertCount;
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    public void setSourceFileName(String sourceFileName) {
        this.sourceFileName = sourceFileName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
    @Override
    public String toString() {
        return "AppAnalyticsEntity{" +
                "id=" + id +
                ", uploadedBy='" + uploadedBy + '\'' +
                ", uploadedOn=" + uploadedOn +
                ", osType='" + osType + '\'' +
                ", reportType='" + reportType + '\'' +
                ", insertCount=" + insertCount +
                ", sourceFileName='" + sourceFileName + '\'' +
                ", status='" + status + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", reason='" + reason + '\'' +
                '}';
    }
}
