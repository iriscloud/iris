/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.iris.cloud.common;

import cn.iris.cloud.common.constants.CommonConstants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class BaseServiceMetadataTest {

	@Test
	public void test() {
		BaseServiceMetadata baseServiceMetadata = new BaseServiceMetadata();
		baseServiceMetadata.setGroup("group1");
		baseServiceMetadata.setServiceInterfaceName("cn.iris.cloud.common.TestInterface");
		baseServiceMetadata.setVersion("1.0.0");
		baseServiceMetadata.setServiceKey(BaseServiceMetadata.buildServiceKey("cn.iris.cloud.common.TestInterface", "group1", "1.0.0"));

		assertEquals(baseServiceMetadata.getGroup(), "group1");
		assertEquals(baseServiceMetadata.getServiceInterfaceName(), "cn.iris.cloud.common.TestInterface");
		assertEquals(baseServiceMetadata.getVersion(), "1.0.0");
		assertEquals(baseServiceMetadata.getServiceKey(), "group1/cn.iris.cloud.common.TestInterface:1.0.0");
		assertEquals(baseServiceMetadata.getDisplayServiceKey(), "cn.iris.cloud.common.TestInterface:1.0.0");

		baseServiceMetadata.setServiceKey(BaseServiceMetadata.buildServiceKey("cn.iris.cloud.common.TestInterface", null, null));
		assertEquals(baseServiceMetadata.getServiceKey(), "cn.iris.cloud.common.TestInterface");
		baseServiceMetadata.setServiceKey(BaseServiceMetadata.buildServiceKey("cn.iris.cloud.common.TestInterface", "", ""));
		assertEquals(baseServiceMetadata.getServiceKey(), "cn.iris.cloud.common.TestInterface");


		baseServiceMetadata.setVersion("2.0.0");
		baseServiceMetadata.generateServiceKey();
		assertEquals(baseServiceMetadata.getServiceKey(), "group1/cn.iris.cloud.common.TestInterface:2.0.0");

		assertEquals(BaseServiceMetadata.versionFromServiceKey("group1/cn.iris.cloud.common.TestInterface:1.0.0"), "1.0.0");
		assertEquals(BaseServiceMetadata.groupFromServiceKey("group1/cn.iris.cloud.common.TestInterface:1.0.0"), "group1");
		assertEquals(BaseServiceMetadata.interfaceFromServiceKey("group1/cn.iris.cloud.common.TestInterface:1.0.0"), "cn.iris.cloud.common.TestInterface");

		assertEquals(CommonConstants.DEFAULT_VERSION, BaseServiceMetadata.versionFromServiceKey(""));
		assertNull(BaseServiceMetadata.groupFromServiceKey(""));
		assertEquals(BaseServiceMetadata.interfaceFromServiceKey(""), "");

		assertEquals(BaseServiceMetadata.revertDisplayServiceKey("cn.iris.cloud.common.TestInterface:1.0.0").getDisplayServiceKey(),
				"cn.iris.cloud.common.TestInterface:1.0.0");
		assertEquals(BaseServiceMetadata.revertDisplayServiceKey("cn.iris.cloud.common.TestInterface").getDisplayServiceKey(),
				"cn.iris.cloud.common.TestInterface:null");
		assertEquals(BaseServiceMetadata.revertDisplayServiceKey(null).getDisplayServiceKey(), "null:null");
		assertEquals(BaseServiceMetadata.revertDisplayServiceKey("cn.iris.cloud.common.TestInterface:1.0.0:1").getDisplayServiceKey(), "null:null");
	}
}
