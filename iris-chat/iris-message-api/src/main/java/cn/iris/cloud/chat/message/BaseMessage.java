package cn.iris.cloud.chat.message;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BaseMessage
 * @author wuhao
 * @description: BaseMessage
 * @createTime 2023/08/26 00:00:00
 */


public class BaseMessage implements Serializable {
	private String tid;
	private String topic;
	private String from;
	private String to;
	private long time;
	private Map<String, String> headers = new ConcurrentHashMap<>();
	private String content;

	public BaseMessage(String tid) {
		this.tid = tid;
	}

	public String getTid() {
		return tid;
	}

	public void setTid(String tid) {
		this.tid = tid;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
