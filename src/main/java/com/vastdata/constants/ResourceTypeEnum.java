package com.vastdata.constants;

public enum ResourceTypeEnum {
    DEPLOYMENT("Deployment"),
    SERVICE("Service"),
    ;

    private String type;

    ResourceTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
