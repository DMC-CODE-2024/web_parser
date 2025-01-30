package app.web_parser.model.rep;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "android_uninstallevents", schema = "rep")
public class AndroidUninstallEvents {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "uninstall_date", nullable = false)
    private LocalDate uninstallDate;

    @Column(name = "uninstall_events", nullable = false)
    private int uninstallEvents;

    @Column(name = "notes", length = 100)
    private String notes;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getUninstallDate() {
        return uninstallDate;
    }

    public void setUninstallDate(LocalDate uninstallDate) {
        this.uninstallDate = uninstallDate;
    }

    public int getUninstallEvents() {
        return uninstallEvents;
    }

    public void setUninstallEvents(int uninstallEvents) {
        this.uninstallEvents = uninstallEvents;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
