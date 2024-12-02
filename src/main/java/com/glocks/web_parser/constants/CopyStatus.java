package com.glocks.web_parser.constants;

public enum CopyStatus {
    NEW(0), COPIED(1);
    Integer index;

    CopyStatus(Integer index) {
        this.index = index;
    }

    public Integer getIndex() {
        return this.index;
    }
}
