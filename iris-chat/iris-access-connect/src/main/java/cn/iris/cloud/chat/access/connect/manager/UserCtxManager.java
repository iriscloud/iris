package cn.iris.cloud.chat.access.connect.manager;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author wuhao
 * @description: UserCtxManager
 * @createTime 2023/08/27 00:10:00
 */

public class UserCtxManager {

	private static final Cache<String, ConcurrentMap<String, ChannelHandlerContext>> ONLINE_MAP = Caffeine
		.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).maximumSize(100000).build();

	public static void putUser(String userId, String device, ChannelHandlerContext context){
		ConcurrentMap<String, ChannelHandlerContext> map = ONLINE_MAP.get(userId,
			s -> new ConcurrentHashMap<>());
		map.put("default", context);
	}
	public static ChannelHandlerContext getUser(String userId, String device){
		ConcurrentMap<String, ChannelHandlerContext> map = ONLINE_MAP.get(userId,
			s -> new ConcurrentHashMap<>());
		return map.get("default");
	}
}
