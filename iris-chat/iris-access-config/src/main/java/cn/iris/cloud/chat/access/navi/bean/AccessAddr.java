package cn.iris.cloud.chat.access.navi.bean;

/**
 * @author wuhao
 * @description: AccessAddr
 * @createTime 2023/03/19 23:29:00
 */

public class AccessAddr {
	private String addr = "127.0.0.1:1883";
	private String type = "1";
	private String index = "1";

	public String getAddr() {
		return addr;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}
}
