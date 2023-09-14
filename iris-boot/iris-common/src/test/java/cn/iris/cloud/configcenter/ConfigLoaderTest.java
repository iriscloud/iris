package cn.iris.cloud.configcenter;

import cn.iris.cloud.configcenter.annotation.ConfigurationProperties;
import cn.iris.cloud.configcenter.configmanager.MemConfigManager;
import cn.iris.cloud.configcenter.dynamic.ConfigChangedEvent;
import cn.iris.cloud.configcenter.dynamic.ConfigurationListener;
import cn.iris.cloud.configcenter.utils.ConfigException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.concurrent.Semaphore;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

class ConfigLoaderTest {

    @Test
    void testConfigBeanWithoutPrefix() {
		try (MockedStatic<ConfigCenterFactory> configMock = Mockito.mockStatic(ConfigCenterFactory.class)) {
			MemConfigManager memConfigManager = new MemConfigManager();
			configMock.when(() -> ConfigCenterFactory.getInstance(anyString())).thenReturn(new ConfigCenter(memConfigManager));
            memConfigManager.addOrModifyProperty("application", "key0", "value0");
            memConfigManager.addOrModifyProperty("application", "prefix.key1", "value1");
            memConfigManager.addOrModifyProperty("application", "key2", "value2");
            memConfigManager.addOrModifyProperty("application", "prefix.key3", "value3");
            BeanWithoutPrefix bean = ConfigLoader.getInstance().getConfig(BeanWithoutPrefix.class);
            assertNotNull(bean);
            assertEquals("value0", bean.key0);
            assertEquals("value2", bean.key2);
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail();
        }

    }

    @Test
    void testConfigBeanWithPrefix() {
		try (MockedStatic<ConfigCenterFactory> configMock = Mockito.mockStatic(ConfigCenterFactory.class)) {
			MemConfigManager memConfigManager = new MemConfigManager();
			configMock.when(() -> ConfigCenterFactory.getInstance(anyString())).thenReturn(new ConfigCenter(memConfigManager));
            memConfigManager.addOrModifyProperty("application", "key0", "value0");
            memConfigManager.addOrModifyProperty("application", "prefix.key1", "value1");
            memConfigManager.addOrModifyProperty("application", "key2", "value2");
            memConfigManager.addOrModifyProperty("application", "prefix.key3", "value3");
            BeanWithPrefix bean = ConfigLoader.getInstance().getConfig(BeanWithPrefix.class);
            assertNotNull(bean);
            assertEquals("value1", bean.key1);
            assertEquals("value3", bean.key3);
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail();
        }

    }

    @Test
    void testConfigBeanWithNamespace() {
		try (MockedStatic<ConfigCenterFactory> configMock = Mockito.mockStatic(ConfigCenterFactory.class)) {
			MemConfigManager memConfigManager = new MemConfigManager();
			configMock.when(() -> ConfigCenterFactory.getInstance(anyString())).thenReturn(new ConfigCenter(memConfigManager));
            memConfigManager.addOrModifyProperty("namespace", "someKey", "someValue");
            BeanWithNamespace bean = ConfigLoader.getInstance().getConfig(BeanWithNamespace.class);
            assertNotNull(bean);
            assertEquals("someValue", bean.someKey);
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail();
        }

    }

    @Test
    void testConfigBeanWithIgnored() {
		try (MockedStatic<ConfigCenterFactory> configMock = Mockito.mockStatic(ConfigCenterFactory.class)) {
			MemConfigManager memConfigManager = new MemConfigManager();
			configMock.when(() -> ConfigCenterFactory.getInstance(anyString())).thenReturn(new ConfigCenter(memConfigManager));
            memConfigManager.addOrModifyProperty("ignore", "notme", "ignore me");
            memConfigManager.addOrModifyProperty("ignore", "neither", "ignore me");
            memConfigManager.addOrModifyProperty("ignore", "ignored", "ignore me");
            memConfigManager.addOrModifyProperty("ignore", "interested", "important");
            BeanWithIgnored bean = ConfigLoader.getInstance().getConfig(BeanWithIgnored.class);
            assertEquals("0", BeanWithIgnored.notme);
            assertEquals("1", bean.neither);
//            assertEquals("2", bean.ignored);
            assertEquals("important", bean.interested);
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail();
        }

    }

    @Test
    void testConfigBeanWithoutAnnotation() {
		try (MockedStatic<ConfigCenterFactory> configMock = Mockito.mockStatic(ConfigCenterFactory.class)) {
			MemConfigManager memConfigManager = new MemConfigManager();
			configMock.when(() -> ConfigCenterFactory.getInstance(anyString())).thenReturn(new ConfigCenter(memConfigManager));
            memConfigManager.addOrModifyProperty("application", "key0", "value0");
            memConfigManager.addOrModifyProperty("application", "prefix.key1", "value1");
            memConfigManager.addOrModifyProperty("application", "key2", "value2");
            memConfigManager.addOrModifyProperty("application", "prefix.key3", "value3");
            assertThrows(ConfigException.class, () -> {
                ConfigLoader.getInstance().getConfig(BeanWithoutAnnotation.class);
            });

            BeanWithoutAnnotation bean = ConfigLoader.getInstance().getConfig(BeanWithoutAnnotation.class, null, null);
            assertNotNull(bean);
            assertEquals("value0", bean.key0);
            assertEquals("value2", bean.key2);
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail();
        }

    }

    @Test
    void testConfigBeanWithInheritance() {
		try (MockedStatic<ConfigCenterFactory> configMock = Mockito.mockStatic(ConfigCenterFactory.class)) {
			MemConfigManager memConfigManager = new MemConfigManager();
			configMock.when(() -> ConfigCenterFactory.getInstance(anyString())).thenReturn(new ConfigCenter(memConfigManager));
            memConfigManager.addOrModifyProperty("application", "key0", "value0");
            memConfigManager.addOrModifyProperty("application", "prefix.key1", "value1");
            memConfigManager.addOrModifyProperty("application", "key2", "value2");
            memConfigManager.addOrModifyProperty("application", "prefix.key3", "value3");
            memConfigManager.addOrModifyProperty("application", "mine", "gotcha");
            BeanWithInheritance bean = ConfigLoader.getInstance().getConfig(BeanWithInheritance.class, null, null);
            assertNotNull(bean);
            assertEquals("value0", bean.key0);
            assertEquals("gotcha", bean.mine);
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail();
        }

    }

    @Test
    void testConfigBeanSinglton() {
		try (MockedStatic<ConfigCenterFactory> configMock = Mockito.mockStatic(ConfigCenterFactory.class)) {
			MemConfigManager memConfigManager = new MemConfigManager();
			configMock.when(() -> ConfigCenterFactory.getInstance(anyString())).thenReturn(new ConfigCenter(memConfigManager));
            memConfigManager.addOrModifyProperty("application", "key0", "value0");
            memConfigManager.addOrModifyProperty("application", "prefix.key1", "value1");
            memConfigManager.addOrModifyProperty("application", "key2", "value2");
            memConfigManager.addOrModifyProperty("application", "prefix.key3", "value3");
            memConfigManager.addOrModifyProperty("application", "mine", "gotcha");
            assertEquals(ConfigLoader.getInstance().getConfig(BeanWithPrefix.class),
                    ConfigLoader.getInstance().getConfig(BeanWithPrefix.class));
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail();
        }

    }

    @Test
    void testConfigBeanMultipleInstances() {
		try (MockedStatic<ConfigCenterFactory> configMock = Mockito.mockStatic(ConfigCenterFactory.class)) {
			MemConfigManager memConfigManager = new MemConfigManager();
			configMock.when(() -> ConfigCenterFactory.getInstance(anyString())).thenReturn(new ConfigCenter(memConfigManager));
            memConfigManager.addOrModifyProperty("application", "key0", "value0");
            memConfigManager.addOrModifyProperty("application", "prefix.key1", "value1");
            memConfigManager.addOrModifyProperty("application", "key2", "value2");
            memConfigManager.addOrModifyProperty("application", "prefix.key3", "value3");
            memConfigManager.addOrModifyProperty("application", "mine", "gotcha");
            assertNotEquals(ConfigLoader.getInstance().getConfig(BeanWithoutAnnotation.class, "ns1", "pf1"),
                    ConfigLoader.getInstance().getConfig(BeanWithoutAnnotation.class, "ns1", "pf2"));
            assertNotEquals(ConfigLoader.getInstance().getConfig(BeanWithoutAnnotation.class, "ns1", "pf1"),
                    ConfigLoader.getInstance().getConfig(BeanWithoutAnnotation.class, "ns2", "pf1"));
            assertNotEquals(ConfigLoader.getInstance().getConfig(BeanWithoutAnnotation.class, "ns1", "pf1"),
                    ConfigLoader.getInstance().getConfig(BeanWithoutAnnotation.class, "ns2", "pf2"));
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail();
        }

    }



    @ConfigurationProperties
    static class BeanWithoutPrefix {
        public String key0;
        public String key2;
    }

    @ConfigurationProperties(prefix = "prefix")
    static class BeanWithPrefix {
        public String key1;
        public String key3;
    }

    @ConfigurationProperties(namespace = "namespace")
    static class BeanWithNamespace {
        public String someKey;
    }

    @ConfigurationProperties(namespace = "ignore")
    static class BeanWithIgnored {
        static String notme = "0";
        final String neither = "1";

        public String ignored = "2";
        public String interested = "3";
    }

    static class BeanWithoutAnnotation {
        public String key0;
        public String key2;
    }

    static class BeanWithInheritance extends BeanWithoutPrefix {
        public String mine;
    }

    @ConfigurationProperties
    static class BeanNotifiable implements ConfigurationListener {

        final Semaphore changes = new Semaphore(0);
        public String key0;

        @Override
        public void process(ConfigChangedEvent event) {
            this.changes.release();
        }
    }

}