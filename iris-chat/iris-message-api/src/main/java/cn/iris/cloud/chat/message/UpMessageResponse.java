package cn.iris.cloud.chat.message;

import java.util.UUID;

/**
 * UpMessageResponse
 * @author wuhao
 * @description: UpMessageResponse
 * @createTime 2023/08/26 00:00:00
 */

public class UpMessageResponse extends BaseMessage {
	public UpMessageResponse() {
		super(UUID.randomUUID().toString());
	}

	public UpMessageResponse(String tid) {
		super(tid);
	}
}
