package com.glocks.web_parser.model.app;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name="blocked_tac_list")
public class BlockedTacList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="created_on")
    LocalDateTime createdOn;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="modified_on")
    LocalDateTime modifiedOn;

    @Column(name="tac")
    String tac;

    @Column(name="source")
    String source;

    @Column(name="remark")
    String remarks;

    @Column(name="mode_type")
    String modeType;

    @Column(name="request_type")
    String requestType;

    @Column(name="txn_id")
    String txnId;

    @Column(name="user_id")
    String userId;
}
