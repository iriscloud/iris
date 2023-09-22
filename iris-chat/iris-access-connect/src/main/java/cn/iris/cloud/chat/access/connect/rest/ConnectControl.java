package cn.iris.cloud.chat.access.connect.rest;

import cn.iris.cloud.chat.message.UpMessageRequest;
import cn.iris.cloud.chat.message.UpMessageService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
/**
 * ConnectControl
 * @author wuhao
 * @description: ConnectControl
 * @createTime 2023/08/26 00:00:00
 */
@RestController
public class ConnectControl {
	@DubboReference(protocol = "dubbo", check = false)
	private UpMessageService routeService;
	@GetMapping("/check")
	@ResponseBody
	public String check() {
		return "OK";
	}

	@GetMapping("/test")
	@ResponseBody
	public String test() {
		routeService.processMessage(new UpMessageRequest());
		return "OK";

	}


}
