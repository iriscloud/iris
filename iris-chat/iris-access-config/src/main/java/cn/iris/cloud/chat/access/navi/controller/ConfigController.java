package cn.iris.cloud.chat.access.navi.controller;

import cn.iris.cloud.chat.access.navi.bean.AccessAddr;
import cn.iris.cloud.chat.access.navi.bean.NaviOutPut;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wuhao
 * @description: NaviControllers
 * @createTime 2023/03/19 23:31:00
 */

@RestController
public class ConfigController {

	@RequestMapping("/config/get")
	public NaviOutPut navi(){
		NaviOutPut naviOutPut = new NaviOutPut();
		naviOutPut.getAddrs().add(new AccessAddr());
		return naviOutPut;
	}
}
