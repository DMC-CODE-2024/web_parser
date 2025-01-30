package app.web_parser.model.app;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name="operator_series")
public class OperatorSeries {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="created_on")
    LocalDateTime createdOn;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="modified_on")
    LocalDateTime modifiedOn;

    @Column(name="series_start")
    int seriesStart;

    @Column(name="series_end")
    int seriesEnd;

    @Column(name="series_type")
    String seriesType;

    @Column(name="operator_name")
    String operatorName;

    @Column(name="length")
    String length;


}
