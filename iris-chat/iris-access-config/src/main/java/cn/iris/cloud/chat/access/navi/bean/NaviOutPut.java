package cn.iris.cloud.chat.access.navi.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wuhao
 * @description: NaviOutPut
 * @createTime 2023/03/19 23:28:00
 */

public class NaviOutPut {

	private String version = "1.0";
	private List<AccessAddr> addrs = new ArrayList<>();

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public List<AccessAddr> getAddrs() {
		return addrs;
	}

	public void setAddrs(List<AccessAddr> addrs) {
		this.addrs = addrs;
	}
}
