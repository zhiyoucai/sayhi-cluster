package com.vastdata;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SayhiClusterSpec {
    // 副本数量
    private Integer size;

    // 镜像
    private String image;
}
