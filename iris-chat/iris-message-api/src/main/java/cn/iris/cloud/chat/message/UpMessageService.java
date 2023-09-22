package cn.iris.cloud.chat.message;

/**
 * UpMessageService
 * @author wuhao
 * @description: UpMessageService
 * @createTime 2023/08/26 00:00:00
 */

public interface UpMessageService {
	/**
	 * processMessage
	 * @param request
	 */
	UpMessageResponse processMessage(UpMessageRequest request);
}
