package app.web_parser.model.app;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name="sys_param")
public class SysParam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(name = "tag")
    String tag;

    @Column(name = "value")
    String value;

}
