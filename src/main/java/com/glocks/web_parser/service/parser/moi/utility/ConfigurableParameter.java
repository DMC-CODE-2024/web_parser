package com.glocks.web_parser.service.parser.moi.utility;

import lombok.Getter;

@Getter
public enum ConfigurableParameter {
    SINGLE("SINGLE"),

    BULK("BULK"),

    FILE_MISSING_ALERT("Alertt8005"),
    GLOBAL_EXCEPTION_ALERT("Alert8008"),
    TABLE_MISSING_ALERT("Alert8002"),

    PENDING_VERIFICATION_STAGE_INIT("VERIFICATION_STAGE_INIT"),

    PENDING_VERIFICATION_STAGE_DONE("VERIFICATION_STAGE_DONE"),

    STOLEN_NOTIFICATION("STOLEN"),

    MOI_PENDING_VERIFICATION_MSG("MOI_PENDING_VERIFICATION_MSG"),
    MOI_VERIFICATION_DONE_MSG("MOI_VERIFICATION_DONE_MSG"),
    ERROR_MSG_500("Please try after some time");

    private String value;

    private ConfigurableParameter(String value) {
        this.value = value;
    }
}
