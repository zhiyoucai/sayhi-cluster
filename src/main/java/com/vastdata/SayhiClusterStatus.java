package com.vastdata;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 状态
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SayhiClusterStatus {
    // 这里的状态可以定义成这种简单的形式，也可以按照自己的想法去定义，以实际业务为准
    public enum State {
        UNKNOWN,
        PROCESSED
    }
    private SayhiClusterStatus.State state = State.UNKNOWN;
}
