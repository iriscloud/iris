package cn.iris.cloud.boot.simple;


import cn.iris.cloud.boot.simple.api.MessageRequest;
import cn.iris.cloud.boot.simple.api.MessageResponse;
import cn.iris.cloud.boot.simple.api.MessageService;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.stereotype.Component;

@Service(interfaceClass = MessageService.class)
@Component
public class MessageServiceDefault implements MessageService {

	@Override
	public MessageResponse send(MessageRequest messageRequest) {
		MessageResponse response = new MessageResponse();
		System.out.println("sendMessage Rev Tid:" + messageRequest.getTid());
		return response;
	}
}