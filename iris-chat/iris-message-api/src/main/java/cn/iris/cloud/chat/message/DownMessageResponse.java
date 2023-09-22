package cn.iris.cloud.chat.message;

import java.util.UUID;

/**
 * DownMessageResponse
 * @author wuhao
 * @description: DownMessageResponse
 * @createTime 2023/08/25 00:00:00
 */

public class DownMessageResponse extends BaseMessage {
	public DownMessageResponse() {
		super(UUID.randomUUID().toString());
	}

	public DownMessageResponse(String tid) {
		super(tid);
	}
}
