package app.web_parser.model.app;


import app.web_parser.constants.ListType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "list_file_mgmt")
public class ListFileManagement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="created_on")
    private LocalDateTime createdOn;

    @Column(name="modified_on")
    private LocalDateTime modifiedOn;

    @Column(name="file_name")
    private String fileName;

    @Column(name="file_path")
    private String filePath;

    @Column(name="source_server")
    private String sourceServer;

    @Column(name="destination_path")
    private String destinationPath;

    @Column(name="destination_server")
    private String destinationServer;

    @Column(name="list_type")
    @Enumerated(EnumType.STRING)
    private ListType listType;

    @Column(name="operator_name")
    private String operatorName;

    @Column(name="file_type")
    private Integer fileType;

    @Column(name="file_state")
    private Integer fileState;

    @Column(name="record_count")
    private Long recordCount;

    @Column(name="copy_status")
    private Integer copyStatus;
}
