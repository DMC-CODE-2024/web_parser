package app.web_parser.model.rep;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "android_devicestorelistingimpressions", schema = "rep")
public class AndroidDeviceStoreListingImpressions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "listing_date", nullable = false)
    private LocalDate listingDate;

    @Column(name = "devices_listing_count", nullable = false)
    private int devicesListingCount;

    @Column(name = "notes", length = 100)
    private String notes;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getListingDate() {
        return listingDate;
    }

    public void setListingDate(LocalDate listingDate) {
        this.listingDate = listingDate;
    }

    public int getDevicesListingCount() {
        return devicesListingCount;
    }

    public void setDevicesListingCount(int devicesListingCount) {
        this.devicesListingCount = devicesListingCount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

