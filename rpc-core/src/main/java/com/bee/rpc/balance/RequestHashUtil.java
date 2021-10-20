package com.bee.rpc.balance;





import com.bee.rpc.api.HelloObject;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class RequestHashUtil {


    public static int getHashCode(Object[] parameters) {
        if (Objects.equals(parameters[0].getClass(), HelloObject.class)) {
            HelloObject parameter = (HelloObject) parameters[0];
            return HashAlgorithmUtils.FNV1_32_HASH.getHash(parameter.getMessage());
        }
        log.info("没有匹配的参数类型！！！！！");
        return -1;
    }

}
