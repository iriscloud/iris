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
package cn.iris.cloud.common.extension.support;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ActivateComparatorTest {

	@Test
	public void testActivateComparator() {
		Filter1 f1 = new Filter1();
		Filter2 f2 = new Filter2();
		Filter3 f3 = new Filter3();
		Filter4 f4 = new Filter4();
		List<Class> filters = new ArrayList<>();
		filters.add(f1.getClass());
		filters.add(f2.getClass());
		filters.add(f3.getClass());
		filters.add(f4.getClass());

		Collections.sort(filters, ActivateComparator.COMPARATOR);

		Assertions.assertEquals(f4.getClass(), filters.get(0));
		Assertions.assertEquals(f3.getClass(), filters.get(1));
		Assertions.assertEquals(f2.getClass(), filters.get(2));
		Assertions.assertEquals(f1.getClass(), filters.get(3));
	}
}
