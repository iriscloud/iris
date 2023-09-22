package cn.iris.cloud.chat.message;

import java.io.Serializable;
import java.util.UUID;

/**
 * UpMessageRequest
 * @author wuhao
 * @description: UpMessageRequest
 * @createTime 2023/08/26 00:00:00
 */

public class UpMessageRequest extends BaseMessage {

	public UpMessageRequest() {
		super(UUID.randomUUID().toString());
	}

	public UpMessageRequest(String tid) {
		super(tid);
	}
}
