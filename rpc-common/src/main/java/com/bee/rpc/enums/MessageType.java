package com.bee.rpc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author ziyang
 */
@AllArgsConstructor
@Getter
public enum MessageType {

    REQUEST_Message((byte) 0),
    RESPONSE_Message((byte) 1);

    private final byte code;

}
