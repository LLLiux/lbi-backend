package com.lin.lbi.manager;

import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class AIManager {

    public static final Long AIMODEL_ID = 1752174806868402177L;

    @Resource
    private YuCongMingClient client;

    public String doChat(String message) {
        DevChatRequest request = new DevChatRequest();
        request.setModelId(AIMODEL_ID);
        request.setMessage(message);

        BaseResponse<DevChatResponse> response = client.doChat(request);
        return response.getData().getContent();
    }
}
