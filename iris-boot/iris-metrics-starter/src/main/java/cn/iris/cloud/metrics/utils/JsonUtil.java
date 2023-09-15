package cn.iris.cloud.metrics.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.InputStream;

/**
 * JsonUtil jackson序列化
 *
 * @author wuhao
 */
public class JsonUtil {
	private static final ObjectMapper defaultObjectMapper = new ObjectMapper();
	private static ObjectMapper objectMapper = null;

	private JsonUtil() {
	}

	private static ObjectMapper mapper() {
		return objectMapper == null ? defaultObjectMapper : objectMapper;
	}

	public static JsonNode toJson(Object var0) {
		return mapper().valueToTree(var0);
	}

	public static <A> A fromJson(JsonNode var0, Class<A> var1) throws JsonProcessingException {
		return mapper().treeToValue(var0, var1);
	}

	public static ObjectNode newObject() {
		return mapper().createObjectNode();
	}

	public static String stringify(JsonNode var0) {
		return var0.toString();
	}

	public static JsonNode parse(String var0) throws JsonProcessingException {
		return mapper().readValue(var0, JsonNode.class);
	}

	public static JsonNode parse(InputStream var0) throws IOException {
		return mapper().readValue(var0, JsonNode.class);
	}

	public static void setObjectMapper(ObjectMapper var0) {
		objectMapper = var0;
	}
}
