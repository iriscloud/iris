package cn.iris.cloud.chat.message;

/**
 * @author wuhao
 * @description: DownMessageService
 * @createTime 2023/08/26 00:00:00
 */

public interface DownMessageService {
	/**
	 * processMessage
	 * @param request
	 */
	DownMessageResponse processMessage(DownMessageRequest request);
}
