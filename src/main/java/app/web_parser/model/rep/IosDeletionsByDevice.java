package app.web_parser.model.rep;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "ios_deletionsbydevice", schema = "rep")
public class IosDeletionsByDevice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "device_name", length = 200, nullable = false)
    private String deviceName;

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

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
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

