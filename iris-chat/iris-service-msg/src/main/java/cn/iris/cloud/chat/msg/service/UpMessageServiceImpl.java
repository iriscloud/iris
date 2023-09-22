package cn.iris.cloud.chat.msg.service;


import cn.iris.cloud.chat.message.*;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * UpMessageServiceImpl
 * @author wuhao
 * @description: UpMessageServiceImpl
 * @createTime 2023/08/26 00:00:00
 */
@Service(interfaceClass = UpMessageService.class)
@Component
public class UpMessageServiceImpl implements UpMessageService {
	private static final Logger LOGGER = LoggerFactory.getLogger(UpMessageServiceImpl.class);
	@DubboReference(protocol = "dubbo", check = false)
	private DownMessageService routeService;

	@Override
	public UpMessageResponse processMessage(UpMessageRequest messageRequest) {
		LOGGER.info("UpMessage:{}:{}", messageRequest.getTid(), messageRequest.getFrom());
		DownMessageRequest downMessageRequest = toDownMessageRequest(messageRequest);
		routeService.processMessage(downMessageRequest);
		UpMessageResponse response = new UpMessageResponse();
		return response;
	}

	private DownMessageRequest toDownMessageRequest(UpMessageRequest messageRequest){
		DownMessageRequest downMessageRequest = new DownMessageRequest();
		downMessageRequest.setContent("欢迎使用iris");
		downMessageRequest.setTo(messageRequest.getFrom());
		downMessageRequest.setTopic(messageRequest.getTopic());
		return downMessageRequest;
	}
}