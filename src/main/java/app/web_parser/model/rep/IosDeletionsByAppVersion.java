package app.web_parser.model.rep;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "ios_deletionsbyappversion", schema = "rep")
public class IosDeletionsByAppVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "version", length = 200, nullable = false)
    private String version;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "event_total", nullable = false)
    private int eventTotal;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public int getEventTotal() {
        return eventTotal;
    }

    public void setEventTotal(int eventTotal) {
        this.eventTotal = eventTotal;
    }
}
