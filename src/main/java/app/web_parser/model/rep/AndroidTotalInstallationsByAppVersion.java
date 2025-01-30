package app.web_parser.model.rep;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "android_totalinstallationsbyappversion", schema = "rep")
public class AndroidTotalInstallationsByAppVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "install_version", length = 200, nullable = false)
    private String installVersion;

    @Column(name = "install_date", nullable = false)
    private LocalDate installDate;

    @Column(name = "install_count", nullable = false)
    private int installCount;

    @Column(name = "notes", length = 100)
    private String notes;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getInstallVersion() {
        return installVersion;
    }

    public void setInstallVersion(String installVersion) {
        this.installVersion = installVersion;
    }

    public LocalDate getInstallDate() {
        return installDate;
    }

    public void setInstallDate(LocalDate installDate) {
        this.installDate = installDate;
    }

    public int getInstallCount() {
        return installCount;
    }

    public void setInstallCount(int installCount) {
        this.installCount = installCount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

