package app.web_parser.model.app;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name="active_msisdn_list")
public class HlrDump {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    long id;

    @Column(name="imsi")
    String imsi;

    @Column(name="msisdn")
    String msisdn;

}
