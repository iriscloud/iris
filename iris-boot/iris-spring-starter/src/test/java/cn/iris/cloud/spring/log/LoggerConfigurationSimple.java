package cn.iris.cloud.spring.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wuhao
 * @description: LoggerConfigurationSimple
 * @createTime 2023/08/29 22:39:00
 */

public class LoggerConfigurationSimple {
	private static final Logger LOGGER = LoggerFactory.getLogger(LoggerConfigurationSimple.class);

	public static void main(String[] args) {
		LOGGER.info("simple log begin info");
		LOGGER.error("simple log begin error");
		LOGGER.info("simple log begin info");
		LOGGER.info("simple log begin info");
		LOGGER.info("simple log begin info");
		LOGGER.info("simple log begin info");
		LOGGER.info("simple log begin info");
		LoggerConfiguration loggerConfiguration = new LoggerConfiguration();
		LOGGER.info("simple log refresh info complete");
		LOGGER.info("simple log refresh info complete");
		LOGGER.info("simple log refresh info complete");
		LOGGER.info("simple log refresh info complete");
		LOGGER.info("simple log refresh info complete");
		LOGGER.info("simple log refresh info complete");
		LOGGER.error("simple log refresh error complete");
	}
}
