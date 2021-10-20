package com.bee.rpc.ServerTest;

import com.bee.rpc.api.HelloObject;
import com.bee.rpc.api.SeeYouObject;
import com.bee.rpc.api.SeeYouService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SeeYouServiceImpl implements SeeYouService {


    @Override
    public String seeYou(SeeYouObject object) {
        log.info("接收到：{}", object.getMessage());
        return "这是调用的返回值，id=" + object.getId();
    }
}
