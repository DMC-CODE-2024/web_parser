package com.glocks.web_parser.model.app;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Column;
import lombok.Data;


@Entity
@Data
@Table(name="exception_list_his")
public class ExceptionListHis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="imsi")
    String imsi;

    @Column(name="msisdn")
    String msisdn;

    @Column(name="imei")
    String imei;

    @Column(name="mode_type")
    String modeType;


    @Column(name="request_type")
    String requestType;

    @Column(name="txn_id")
    String txnId;

    @Column(name="user_id")
    String userId;

    @Column(name="operator_name")
    String operatorName;

    @Column(name="actual_imei")
    String actualImei;

    @Column(name="tac")
    String tac;

    @Column(name="remark")
    String remarks;

    @Column(name="operation")
    int operation;
    @Column(name="source")
    String source;

}
