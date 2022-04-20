package com.vastdata.constants;

public enum ImagePullPolicyEnum {
    IF_NOT_PRESENT("IfNotPresent");
    final private String policy;

    ImagePullPolicyEnum(String policy) {
        this.policy = policy;
    }

    public String getPolicy() {
        return policy;
    }
}
