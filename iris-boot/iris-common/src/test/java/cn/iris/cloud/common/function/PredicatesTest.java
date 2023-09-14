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
package cn.iris.cloud.common.function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * {@link Predicates} Test
 *
 * @since 2.7.5
 */
public class PredicatesTest {

	@Test
	public void testAlwaysTrue() {
		Assertions.assertTrue(Predicates.alwaysTrue().test(null));
	}

	@Test
	public void testAlwaysFalse() {
		Assertions.assertFalse(Predicates.alwaysFalse().test(null));
	}

	@Test
	public void testAnd() {
		Assertions.assertTrue(Predicates.and(Predicates.alwaysTrue(), Predicates.alwaysTrue(), Predicates.alwaysTrue()).test(null));
		Assertions.assertFalse(Predicates.and(Predicates.alwaysFalse(), Predicates.alwaysFalse(), Predicates.alwaysFalse()).test(null));
		Assertions.assertFalse(Predicates.and(Predicates.alwaysTrue(), Predicates.alwaysFalse(), Predicates.alwaysFalse()).test(null));
		Assertions.assertFalse(Predicates.and(Predicates.alwaysTrue(), Predicates.alwaysTrue(), Predicates.alwaysFalse()).test(null));
	}

	@Test
	public void testOr() {
		Assertions.assertTrue(Predicates.or(Predicates.alwaysTrue(), Predicates.alwaysTrue(), Predicates.alwaysTrue()).test(null));
		Assertions.assertTrue(Predicates.or(Predicates.alwaysTrue(), Predicates.alwaysTrue(), Predicates.alwaysFalse()).test(null));
		Assertions.assertTrue(Predicates.or(Predicates.alwaysTrue(), Predicates.alwaysFalse(), Predicates.alwaysFalse()).test(null));
		Assertions.assertFalse(Predicates.or(Predicates.alwaysFalse(), Predicates.alwaysFalse(), Predicates.alwaysFalse()).test(null));
	}
}
