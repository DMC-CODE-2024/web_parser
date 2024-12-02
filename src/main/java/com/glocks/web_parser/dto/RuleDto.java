package com.glocks.web_parser.dto;


import lombok.Data;

@Data
public class RuleDto {

    long id;
    String name;
    String output;
    String graceAction;
    String postGraceAction;
    String failedRuleActionGrace;
    String failedRuleActionPostGrace;
    String ruleMessage;

    public RuleDto(long id, String name, String output,
                   String graceAction, String postGraceAction, String failedRuleActionGrace,
                   String failedRuleActionPostGrace, String ruleMessage) {
        this.id = id;
        this.name = name;
        this.output = output;
        this.graceAction = graceAction;
        this.postGraceAction = postGraceAction;
        this.failedRuleActionGrace = failedRuleActionGrace;
        this.failedRuleActionPostGrace = failedRuleActionPostGrace;
        this.ruleMessage = ruleMessage;
    }
}
