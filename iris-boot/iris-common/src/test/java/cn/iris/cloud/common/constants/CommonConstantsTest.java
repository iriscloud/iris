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
package cn.iris.cloud.common.constants;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * {@link CommonConstants} Test-Cases
 *
 * @since 2.7.8
 */
public class CommonConstantsTest {

	@Test
	public void test() {
		Assertions.assertEquals(',', CommonConstants.COMMA_SEPARATOR_CHAR);
		Assertions.assertEquals("composite", CommonConstants.COMPOSITE_METADATA_STORAGE_TYPE);
		Assertions.assertEquals("service-name-mapping.properties-path", CommonConstants.SERVICE_NAME_MAPPING_PROPERTIES_FILE_KEY);
		Assertions.assertEquals("META-INF/iris/service-name-mapping.properties", CommonConstants.DEFAULT_SERVICE_NAME_MAPPING_PROPERTIES_PATH);
	}
}
