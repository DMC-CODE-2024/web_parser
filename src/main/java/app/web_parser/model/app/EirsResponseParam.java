package app.web_parser.model.app;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "eirs_response_param")
public class EirsResponseParam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "language")
    String language;

    @Column(name = "tag")

    private String tag;
    @Column(name = "value")
    String value;
    @Column(name = "feature_name")
    String featureName;

    @Column(name = "description")
    String description;
    @Column(name = "subject")
    String subject;


}
