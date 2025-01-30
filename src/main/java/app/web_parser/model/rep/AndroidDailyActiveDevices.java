package app.web_parser.model.rep;

import jakarta.persistence.*;
import java.time.LocalDate;

;

@Entity
@Table(name = "android_dailyactivedevices", schema = "rep")
public class AndroidDailyActiveDevices {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "active_date", nullable = false)
    private LocalDate activeDate;

    @Column(name = "active_devices_count", nullable = false)
    private int activeDevicesCount;

    @Column(name = "notes", length = 100)
    private String notes;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getActiveDate() {
        return activeDate;
    }

    public void setActiveDate(LocalDate activeDate) {
        this.activeDate = activeDate;
    }

    public int getActiveDevicesCount() {
        return activeDevicesCount;
    }

    public void setActiveDevicesCount(int activeDevicesCount) {
        this.activeDevicesCount = activeDevicesCount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
