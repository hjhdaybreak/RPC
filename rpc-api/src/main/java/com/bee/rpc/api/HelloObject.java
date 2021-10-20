package com.bee.rpc.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 测试用api的实体
 *
 * @author
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HelloObject {

    private Integer id;
    private String message;

}
