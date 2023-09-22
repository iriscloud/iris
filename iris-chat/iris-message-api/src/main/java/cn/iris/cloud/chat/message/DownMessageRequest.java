package cn.iris.cloud.chat.message;

import java.util.UUID;

/**
 * DownMessageRequest
 * @author wuhao
 * @description: DownMessageRequest
 * @createTime 2023/08/26 00:00:00
 */

public class DownMessageRequest extends BaseMessage {

	public DownMessageRequest() {
		super(UUID.randomUUID().toString());
	}

	public DownMessageRequest(String tid) {
		super(tid);
	}
}
