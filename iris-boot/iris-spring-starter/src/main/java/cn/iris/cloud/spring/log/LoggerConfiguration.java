package cn.iris.cloud.spring.log;


import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.xml.XmlConfiguration;
import org.aspectj.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

/**
 * LoggerConfiguration
 *
 * @author wuhao
 */
public class LoggerConfiguration {
	private static final Logger LOGGER = LoggerFactory.getLogger(LoggerConfiguration.class);

	/**
	 * 服务名称
	 */
	private final String serverName;

	/**
	 * 初始化
	 */
	public LoggerConfiguration() {

		this.serverName = "app";
		// 注册namespace 及 listener
		String content = "";
		try {
			content = FileUtil.readAsString(new File("log4j2.xml"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		refresh(content);
	}


	/**
	 * 刷新日志配置
	 */
	private void refresh(String config) {
		try {
			if (StringUtils.isEmpty(config)) {
				return;
			}
			// 替换日志文件名称
			if (StringUtils.isEmpty(serverName)) {
				config = config.replace("${server.name}", "server.name");
			} else {
				config = config.replace("${server.name}", this.serverName);
			}

			InputStream is = new ByteArrayInputStream(config.getBytes());
			// 构造新配置并应用到LoggerContext
			LoggerContext ctx = (LoggerContext) LogManager.getContext(false);

			ConfigurationSource source = new ConfigurationSource(is);
			XmlConfiguration xmlConfiguration = new XmlConfiguration(ctx, source);
			ctx.start(xmlConfiguration);
			LOGGER.error("logging refresh success...");
		} catch (Exception e) {
			LOGGER.error("logging refresh error.", e);
		}
	}
}
