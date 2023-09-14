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
package cn.iris.cloud.common.extension;

import cn.iris.cloud.common.URL;
import cn.iris.cloud.common.constants.CommonConstants;
import cn.iris.cloud.common.extension.activate.ActivateExt1;
import cn.iris.cloud.common.extension.activate.ActivateWrapperExt1;
import cn.iris.cloud.common.extension.activate.impl.*;
import cn.iris.cloud.common.extension.ext1.SimpleExt;
import cn.iris.cloud.common.extension.ext1.impl.SimpleExtImpl1;
import cn.iris.cloud.common.extension.ext1.impl.SimpleExtImpl2;
import cn.iris.cloud.common.extension.ext2.Ext2;
import cn.iris.cloud.common.extension.ext6_wrap.WrappedExt;
import cn.iris.cloud.common.extension.ext6_wrap.impl.Ext5Wrapper1;
import cn.iris.cloud.common.extension.ext6_wrap.impl.Ext5Wrapper2;
import cn.iris.cloud.common.extension.ext7.InitErrorExt;
import cn.iris.cloud.common.extension.ext8_add.AddExt1;
import cn.iris.cloud.common.extension.ext8_add.AddExt2;
import cn.iris.cloud.common.extension.ext8_add.AddExt3;
import cn.iris.cloud.common.extension.ext8_add.AddExt4;
import cn.iris.cloud.common.extension.ext8_add.impl.*;
import cn.iris.cloud.common.extension.ext9_empty.Ext9Empty;
import cn.iris.cloud.common.extension.ext9_empty.impl.Ext9EmptyImpl;
import cn.iris.cloud.common.extension.injection.InjectExt;
import cn.iris.cloud.common.extension.injection.impl.InjectExtImpl;
import cn.iris.cloud.common.lang.Prioritized;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;

class ExtensionLoaderTest {

	@Test
	void test_getExtensionLoader_Null() throws Exception {
		try {
			ExtensionLoader.getExtensionLoader(null);
			fail();
		} catch (IllegalArgumentException expected) {
			assertThat(expected.getMessage(),
					containsString("Extension type == null"));
		}
	}

	interface INoMetaInfo {

	}

	@Test
	void test_loadOrByDefaultFactory_FactoryNull() {
		try {
			assertNull(ExtensionLoader.loadOrByDefaultFactory(INoMetaInfo.class, "noMetaInfo1", null));
		} catch (Exception e) {
			fail(e);
		}

	}

	@Test
	void test_loadOrByDefaultFactory_FactoryReturnNull() {
		try {
			assertNull(ExtensionLoader.loadOrByDefaultFactory(INoMetaInfo.class, "noMetaInfo2", () -> null));
		} catch (Exception e) {
			fail(e);
		}

	}


	@Test
	void test_loadOrByDefaultFactory_Factory() {
		try {
			assertNotNull(
					ExtensionLoader.loadOrByDefaultFactory(INoMetaInfo.class, "noMetaInfo3", () -> new INoMetaInfo() {
					}));
		} catch (Exception e) {
			fail(e);
		}

	}

	@Test
	void test_getExtensionLoader_NotInterface() throws Exception {
		try {
			ExtensionLoader.getExtensionLoader(ExtensionLoaderTest.class);
			fail();
		} catch (IllegalArgumentException expected) {
			assertThat(expected.getMessage(),
					containsString(
							"Extension type (class cn.iris.cloud.common.extension.ExtensionLoaderTest) is not an interface"));
		}
	}

	@Test
	void test_getExtensionLoader_NotSpiAnnotation() throws Exception {
		try {
			ExtensionLoader.getExtensionLoader(NoSpiExt.class);
			fail();
		} catch (IllegalArgumentException expected) {
			assertThat(expected.getMessage(),
					allOf(containsString("cn.iris.cloud.common.extension.NoSpiExt"),
							containsString("is not an extension"),
							containsString("NOT annotated with @SPI")));
		}
	}

	@Test
	void test_getDefaultExtension() throws Exception {
		SimpleExt ext = ExtensionLoader.getExtensionLoader(SimpleExt.class).getDefaultExtension();
		assertThat(ext, instanceOf(SimpleExtImpl1.class));

		String name = ExtensionLoader.getExtensionLoader(SimpleExt.class).getDefaultExtensionName();
		assertEquals("impl1", name);
	}


	@Test
	void test_getDefaultExtension_NULL() throws Exception {
		Ext2 ext = ExtensionLoader.getExtensionLoader(Ext2.class).getDefaultExtension();
		assertNull(ext);

		String name = ExtensionLoader.getExtensionLoader(Ext2.class).getDefaultExtensionName();
		assertNull(name);
	}

	@Test
	void test_getExtension() throws Exception {
		assertTrue(ExtensionLoader.getExtensionLoader(SimpleExt.class).getExtension("impl1") instanceof SimpleExtImpl1);
		assertTrue(ExtensionLoader.getExtensionLoader(SimpleExt.class).getExtension("impl2") instanceof SimpleExtImpl2);
	}

	@Test
	void test_getExtension_WithWrapper() throws Exception {
		WrappedExt impl1 = ExtensionLoader.getExtensionLoader(WrappedExt.class).getExtension("impl1");
		assertThat(impl1, anyOf(instanceOf(Ext5Wrapper1.class), instanceOf(Ext5Wrapper2.class)));

		WrappedExt impl2 = ExtensionLoader.getExtensionLoader(WrappedExt.class).getExtension("impl2");
		assertThat(impl2, anyOf(instanceOf(Ext5Wrapper1.class), instanceOf(Ext5Wrapper2.class)));

		URL url = new URL("p1", "1.2.3.4", 1010, "path1");
		int echoCount1 = Ext5Wrapper1.echoCount.get();
		int echoCount2 = Ext5Wrapper2.echoCount.get();

		assertEquals("Ext5Impl1-echo", impl1.echo(url, "ha"));
		assertEquals(echoCount1 + 1, Ext5Wrapper1.echoCount.get());
		assertEquals(echoCount2 + 1, Ext5Wrapper2.echoCount.get());
	}

	@Test
	void test_getActivateExtension_WithWrapper() throws Exception {
		URL url = URL.valueOf("test://localhost/test");
		List<ActivateWrapperExt1> list = ExtensionLoader.getExtensionLoader(ActivateWrapperExt1.class)
				.getActivateExtension(url, new String[]{}, "order");
		assertEquals(2, list.size());
	}

	@Test
	void test_getExtension_ExceptionNoExtension() throws Exception {
		try {
			ExtensionLoader.getExtensionLoader(SimpleExt.class).getExtension("XXX");
			fail();
		} catch (IllegalStateException expected) {
			assertThat(expected.getMessage(),
					containsString("No such extension cn.iris.cloud.common.extension.ext1.SimpleExt by name XXX"));
		}
	}

	@Test
	void test_getExtension_ExceptionNoExtension_WrapperNotAffactName() throws Exception {
		try {
			ExtensionLoader.getExtensionLoader(WrappedExt.class).getExtension("XXX");
			fail();
		} catch (IllegalStateException expected) {
			assertThat(expected.getMessage(),
					containsString("No such extension cn.iris.cloud.common.extension.ext6_wrap.WrappedExt by name XXX"));
		}
	}

	@Test
	void test_getExtension_ExceptionNullArg() throws Exception {
		try {
			ExtensionLoader.getExtensionLoader(SimpleExt.class).getExtension(null);
			fail();
		} catch (IllegalArgumentException expected) {
			assertThat(expected.getMessage(), containsString("Extension name == null"));
		}
	}

	@Test
	void test_hasExtension() throws Exception {
		Assertions.assertTrue(ExtensionLoader.getExtensionLoader(SimpleExt.class).hasExtension("impl1"));
		Assertions.assertFalse(ExtensionLoader.getExtensionLoader(SimpleExt.class).hasExtension("impl1,impl2"));
		Assertions.assertFalse(ExtensionLoader.getExtensionLoader(SimpleExt.class).hasExtension("xxx"));

		try {
			ExtensionLoader.getExtensionLoader(SimpleExt.class).hasExtension(null);
			fail();
		} catch (IllegalArgumentException expected) {
			assertThat(expected.getMessage(), containsString("Extension name == null"));
		}
	}

	@Test
	void test_hasExtension_wrapperIsNotExt() throws Exception {
		Assertions.assertTrue(ExtensionLoader.getExtensionLoader(WrappedExt.class).hasExtension("impl1"));
		Assertions.assertFalse(ExtensionLoader.getExtensionLoader(WrappedExt.class).hasExtension("impl1,impl2"));
		Assertions.assertFalse(ExtensionLoader.getExtensionLoader(WrappedExt.class).hasExtension("xxx"));

		Assertions.assertFalse(ExtensionLoader.getExtensionLoader(WrappedExt.class).hasExtension("wrapper1"));

		try {
			ExtensionLoader.getExtensionLoader(WrappedExt.class).hasExtension(null);
			fail();
		} catch (IllegalArgumentException expected) {
			assertThat(expected.getMessage(), containsString("Extension name == null"));
		}
	}

	@Test
	void test_getSupportedExtensions() throws Exception {
		Set<String> exts = ExtensionLoader.getExtensionLoader(SimpleExt.class).getSupportedExtensions();

		Set<String> expected = new HashSet<String>();
		expected.add("impl1");
		expected.add("impl2");
		expected.add("impl3");

		assertEquals(expected, exts);
	}

	@Test
	void test_getSupportedExtensions_wrapperIsNotExt() throws Exception {
		Set<String> exts = ExtensionLoader.getExtensionLoader(WrappedExt.class).getSupportedExtensions();

		Set<String> expected = new HashSet<String>();
		expected.add("impl1");
		expected.add("impl2");

		assertEquals(expected, exts);
	}

	@Test
	void test_AddExtension() throws Exception {
		try {
			ExtensionLoader.getExtensionLoader(AddExt1.class).getExtension("Manual1");
			fail();
		} catch (IllegalStateException expected) {
			assertThat(expected.getMessage(),
					containsString("No such extension cn.iris.cloud.common.extension.ext8_add.AddExt1 by name Manual1, no related exception was found, please check whether related SPI module is missing."));
		}

		ExtensionLoader.getExtensionLoader(AddExt1.class).addExtension("Manual1", AddExt1_ManualAdd1.class);
		AddExt1 ext = ExtensionLoader.getExtensionLoader(AddExt1.class).getExtension("Manual1");

		assertThat(ext, instanceOf(AddExt1_ManualAdd1.class));
		Assertions.assertEquals("Manual1", ExtensionLoader.getExtensionLoader(AddExt1.class).getExtensionName(AddExt1_ManualAdd1.class));
		ExtensionLoader.resetExtensionLoader(AddExt1.class);
	}

	@Test
	void test_AddExtension_NoExtend() throws Exception {
//        ExtensionLoader.getExtensionLoader(Ext9Empty.class).getSupportedExtensions();
		ExtensionLoader.getExtensionLoader(Ext9Empty.class).addExtension("ext9", Ext9EmptyImpl.class);
		Ext9Empty ext = ExtensionLoader.getExtensionLoader(Ext9Empty.class).getExtension("ext9");

		assertThat(ext, instanceOf(Ext9Empty.class));
		Assertions.assertEquals("ext9", ExtensionLoader.getExtensionLoader(Ext9Empty.class).getExtensionName(Ext9EmptyImpl.class));
	}

	@Test
	void test_AddExtension_ExceptionWhenExistedExtension() throws Exception {
		SimpleExt ext = ExtensionLoader.getExtensionLoader(SimpleExt.class).getExtension("impl1");

		try {
			ExtensionLoader.getExtensionLoader(AddExt1.class).addExtension("impl1", AddExt1_ManualAdd1.class);
			fail();
		} catch (IllegalStateException expected) {
			assertThat(expected.getMessage(), containsString(
					"Extension name impl1 already exists (Extension interface cn.iris.cloud.common.extension.ext8_add.AddExt1)!"));
		}
	}

	@Test
	void test_AddExtension_Adaptive() throws Exception {
		ExtensionLoader<AddExt2> loader = ExtensionLoader.getExtensionLoader(AddExt2.class);
		loader.addExtension(null, AddExt2_ManualAdaptive.class);

		AddExt2 adaptive = loader.getAdaptiveExtension();
		assertTrue(adaptive instanceof AddExt2_ManualAdaptive);
	}

	@Test
	void test_AddExtension_Adaptive_ExceptionWhenExistedAdaptive() throws Exception {
		ExtensionLoader<AddExt1> loader = ExtensionLoader.getExtensionLoader(AddExt1.class);

		loader.getAdaptiveExtension();

		try {
			loader.addExtension(null, AddExt1_ManualAdaptive.class);
			fail();
		} catch (IllegalStateException expected) {
			assertThat(expected.getMessage(), containsString(
					"Adaptive Extension already exists (Extension interface cn.iris.cloud.common.extension.ext8_add.AddExt1)!"));
		}
	}

	@Test
	void test_replaceExtension() throws Exception {
		try {
			ExtensionLoader.getExtensionLoader(AddExt1.class).getExtension("Manual2");
			fail();
		} catch (IllegalStateException expected) {
			assertThat(expected.getMessage(),
					containsString("No such extension cn.iris.cloud.common.extension.ext8_add.AddExt1 by name Manual"));
		}

		{
			AddExt1 ext = ExtensionLoader.getExtensionLoader(AddExt1.class).getExtension("impl1");

			assertThat(ext, instanceOf(AddExt1Impl1.class));
			Assertions.assertEquals("impl1", ExtensionLoader.getExtensionLoader(AddExt1.class).getExtensionName(AddExt1Impl1.class));
		}
		{
			ExtensionLoader.getExtensionLoader(AddExt1.class).replaceExtension("impl1", AddExt1_ManualAdd2.class);
			AddExt1 ext = ExtensionLoader.getExtensionLoader(AddExt1.class).getExtension("impl1");

			assertThat(ext, instanceOf(AddExt1_ManualAdd2.class));
			Assertions.assertEquals("impl1", ExtensionLoader.getExtensionLoader(AddExt1.class).getExtensionName(AddExt1_ManualAdd2.class));
		}
		ExtensionLoader.resetExtensionLoader(AddExt1.class);
	}

	@Test
	void test_replaceExtension_Adaptive() throws Exception {
		ExtensionLoader<AddExt3> loader = ExtensionLoader.getExtensionLoader(AddExt3.class);

		AddExt3 adaptive = loader.getAdaptiveExtension();
		assertFalse(adaptive instanceof AddExt3_ManualAdaptive);

		loader.replaceExtension(null, AddExt3_ManualAdaptive.class);

		adaptive = loader.getAdaptiveExtension();
		assertTrue(adaptive instanceof AddExt3_ManualAdaptive);
		ExtensionLoader.resetExtensionLoader(AddExt3.class);
	}

	@Test
	void test_replaceExtension_ExceptionWhenNotExistedExtension() throws Exception {
		AddExt1 ext = ExtensionLoader.getExtensionLoader(AddExt1.class).getExtension("impl1");

		try {
			ExtensionLoader.getExtensionLoader(AddExt1.class).replaceExtension("NotExistedExtension", AddExt1_ManualAdd1.class);
			fail();
		} catch (IllegalStateException expected) {
			assertThat(expected.getMessage(), containsString(
					"Extension name NotExistedExtension doesn't exist (Extension interface cn.iris.cloud.common.extension.ext8_add.AddExt1)"));
		}
	}

	@Test
	void test_replaceExtension_Adaptive_ExceptionWhenNotExistedExtension() throws Exception {
		ExtensionLoader<AddExt4> loader = ExtensionLoader.getExtensionLoader(AddExt4.class);

		try {
			loader.replaceExtension(null, AddExt4_ManualAdaptive.class);
			fail();
		} catch (IllegalStateException expected) {
			assertThat(expected.getMessage(), containsString(
					"Adaptive Extension doesn't exist (Extension interface cn.iris.cloud.common.extension.ext8_add.AddExt4)"));
		}
	}

	@Test
	void test_InitError() throws Exception {
		ExtensionLoader<InitErrorExt> loader = ExtensionLoader.getExtensionLoader(InitErrorExt.class);

		loader.getExtension("ok");

		try {
			loader.getExtension("error");
			fail();
		} catch (IllegalStateException expected) {
			assertThat(expected.getMessage(), containsString("Failed to load extension class"));
		}
	}

	@Test
	void testLoadActivateExtension() throws Exception {
		// test default
		URL url = URL.valueOf("test://localhost/test");
		List<ActivateExt1> list = ExtensionLoader.getExtensionLoader(ActivateExt1.class)
				.getActivateExtension(url, new String[]{}, "default_group");
		Assertions.assertEquals(1, list.size());
		Assertions.assertSame(list.get(0).getClass(), ActivateExt1Impl1.class);

		// test group
		url = url.addParameter(CommonConstants.GROUP_KEY, "group1");
		list = ExtensionLoader.getExtensionLoader(ActivateExt1.class)
				.getActivateExtension(url, new String[]{}, "group1");
		Assertions.assertEquals(1, list.size());
		Assertions.assertSame(list.get(0).getClass(), GroupActivateExtImpl.class);

		// test value
		url = url.removeParameter(CommonConstants.GROUP_KEY);
		url = url.addParameter(CommonConstants.GROUP_KEY, "value");
		url = url.addParameter("value", "value");
		list = ExtensionLoader.getExtensionLoader(ActivateExt1.class)
				.getActivateExtension(url, new String[]{}, "value");
		Assertions.assertEquals(1, list.size());
		Assertions.assertSame(list.get(0).getClass(), ValueActivateExtImpl.class);

		// test order
		url = URL.valueOf("test://localhost/test");
		url = url.addParameter(CommonConstants.GROUP_KEY, "order");
		list = ExtensionLoader.getExtensionLoader(ActivateExt1.class)
				.getActivateExtension(url, new String[]{}, "order");
		Assertions.assertEquals(2, list.size());
		Assertions.assertSame(list.get(0).getClass(), OrderActivateExtImpl1.class);
		Assertions.assertSame(list.get(1).getClass(), OrderActivateExtImpl2.class);
	}

	@Test
	void testLoadDefaultActivateExtension() throws Exception {
		// test default
		URL url = URL.valueOf("test://localhost/test?ext=order1,default");
		List<ActivateExt1> list = ExtensionLoader.getExtensionLoader(ActivateExt1.class)
				.getActivateExtension(url, "ext", "default_group");
		Assertions.assertEquals(2, list.size());
		Assertions.assertSame(list.get(0).getClass(), OrderActivateExtImpl1.class);
		Assertions.assertSame(list.get(1).getClass(), ActivateExt1Impl1.class);

		url = URL.valueOf("test://localhost/test?ext=default,order1");
		list = ExtensionLoader.getExtensionLoader(ActivateExt1.class)
				.getActivateExtension(url, "ext", "default_group");
		Assertions.assertEquals(2, list.size());
		Assertions.assertSame(list.get(0).getClass(), ActivateExt1Impl1.class);
		Assertions.assertSame(list.get(1).getClass(), OrderActivateExtImpl1.class);
	}

	@Test
	void testInjectExtension() {
		// test default
		InjectExt injectExt = ExtensionLoader.getExtensionLoader(InjectExt.class).getExtension("injection");
		InjectExtImpl injectExtImpl = (InjectExtImpl) injectExt;
		Assertions.assertNotNull(injectExtImpl.getSimpleExt());
		Assertions.assertNull(injectExtImpl.getSimpleExt1());
		Assertions.assertNull(injectExtImpl.getGenericType());
	}

	@Test
	void testGetOrDefaultExtension() {
		ExtensionLoader<InjectExt> loader = ExtensionLoader.getExtensionLoader(InjectExt.class);
		InjectExt injectExt = loader.getOrDefaultExtension("non-exists");
		assertEquals(InjectExtImpl.class, injectExt.getClass());
		assertEquals(InjectExtImpl.class, loader.getOrDefaultExtension("injection").getClass());
	}

	@Test
	void testGetSupported() {
		ExtensionLoader<InjectExt> loader = ExtensionLoader.getExtensionLoader(InjectExt.class);
		assertEquals(1, loader.getSupportedExtensions().size());
		assertEquals(Collections.singleton("injection"), loader.getSupportedExtensions());
	}


	/**
	 * @since 2.7.7
	 */
	@Test
	void testGetLoadingStrategies() {
		List<LoadingStrategy> strategies = ExtensionLoader.getLoadingStrategies();

		assertEquals(4, strategies.size());

		int i = 0;

		LoadingStrategy loadingStrategy = strategies.get(i++);
		assertEquals(IrisInternalLoadingStrategy.class, loadingStrategy.getClass());
		assertEquals(Prioritized.MAX_PRIORITY, loadingStrategy.getPriority());

		loadingStrategy = strategies.get(i++);
		assertEquals(IrisExternalLoadingStrategy.class, loadingStrategy.getClass());
		assertEquals(Prioritized.MAX_PRIORITY + 1, loadingStrategy.getPriority());

		loadingStrategy = strategies.get(i++);
		assertEquals(IrisLoadingStrategy.class, loadingStrategy.getClass());
		assertEquals(Prioritized.NORMAL_PRIORITY, loadingStrategy.getPriority());

		loadingStrategy = strategies.get(i++);
		assertEquals(ServicesLoadingStrategy.class, loadingStrategy.getClass());
		assertEquals(Prioritized.MIN_PRIORITY, loadingStrategy.getPriority());
	}

	@Test
	void testLoadOrByDefaultFactory() {
		LoaderFactory4Test loaderFactory4Test = ExtensionLoader.loadOrByDefaultFactory(LoaderFactory4Test.class,
				"nop4loaderTest", () -> new LoaderFactoryPlusImpl());
		assertEquals(LoaderFactoryNopImpl.class, loaderFactory4Test.getClass());
		assertEquals(0, loaderFactory4Test.getKey());
		assertEquals(0, LoaderFactoryPlusImpl.key.get());

	}


}

@SPI
interface LoaderFactory4Test {

	public int getKey();
}

class LoaderFactoryNopImpl implements LoaderFactory4Test {

	public LoaderFactoryNopImpl() {
	}


	@Override
	public int getKey() {
		return 0;
	}
}

class LoaderFactoryPlusImpl implements LoaderFactory4Test {

	static final AtomicInteger key = new AtomicInteger(0);

	public LoaderFactoryPlusImpl() {
		key.incrementAndGet();
	}

	@Override
	public int getKey() {
		return key.get();
	}
}