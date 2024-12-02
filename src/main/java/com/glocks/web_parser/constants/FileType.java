package com.glocks.web_parser.constants;

public enum FileType {
    BULK(0, "bulk"), SINGLE(1, "single"), FULL_DUMP(2, "full dump"), PROCESSED_FILE(3, "processed file");
    Integer index;
    private String value;

    FileType(Integer index, String value) {
        this.index = index;
        this.value = value;
    }

    public Integer getIndex() {
        return this.index;
    }

    public String getValue() {
        return value;
    }

    public static FileType getByIndex(Integer index) {
        for (FileType fileType : FileType.values()) {
            if (fileType.getIndex() == index) {
                return fileType;
            }
        }
        return null;
    }
}
