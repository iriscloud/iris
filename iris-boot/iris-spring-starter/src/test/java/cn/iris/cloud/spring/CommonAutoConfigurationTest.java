package cn.iris.cloud.spring;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * CommonAutoConfigurationTest
 *
 * @author wuhao
 */

public class CommonAutoConfigurationTest {
	@Test
	void testFilter() {
		try {
			CommonAutoConfiguration commonAutoConfiguration = new CommonAutoConfiguration();
		} catch (Exception e) {
			e.printStackTrace();
			Assertions.fail();
		}
	}
}
