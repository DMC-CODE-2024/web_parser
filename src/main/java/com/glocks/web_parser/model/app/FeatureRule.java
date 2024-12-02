package com.glocks.web_parser.model.app;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name="feature_rule")
public class FeatureRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="created_on")
    LocalDateTime createdOn;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="modified_on")
    LocalDateTime modifiedOn;

    @Column(name="feature")
    String feature;

    @Column(name="grace_action")
    String graceAction;

    @Column(name="name")
    String name;

    @Column(name="post_grace_action")
    String postGraceAction;

    @Column(name="rule_order")
    String ruleOrder;

    @Column(name="user_type")
    String userType;

    @Column(name="failed_rule_action_grace")
    String failedRuleActionGrace;

    @Column(name="failed_rule_action_post_grace")
    String failedRuleActionPostGrace;

    @Column(name="output")
    String output;
    @Column(name="rule_message")
    String ruleMessage;

//
}
