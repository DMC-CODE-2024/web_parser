package com.glocks.web_parser.model.app;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name="notification")
public class SmsNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name="msisdn")
    String msisdn;
    @Column(name="message")
    String message;
    @Column(name="channel_type")
    String channel;
    @Column(name="msg_lang")
    String msgLang;
    @Column(name="email")
    String email;
    @Column(name="feature_name")
    String featureName;
    @Column(name="feature_txn_id")
    String featureTxnId;
    @Column(name="sub_feature")
    String subFeature;


}
