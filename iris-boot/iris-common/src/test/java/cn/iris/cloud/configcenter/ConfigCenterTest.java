package cn.iris.cloud.configcenter;

import cn.iris.cloud.configcenter.configmanager.MemConfigManager;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

class ConfigCenterTest {
    @Test
    void testDefaultNamespaceSingleConfig() {
        try (MockedStatic<ConfigCenterFactory> configMock = Mockito.mockStatic(ConfigCenterFactory.class)) {
            MemConfigManager memConfigManager = new MemConfigManager();
            configMock.when(() -> ConfigCenterFactory.getInstance(anyString())).thenReturn(new ConfigCenter(memConfigManager));
            memConfigManager.addOrModifyProperty("application", "key0", "value0");
            assertEquals("value0", ConfigCenter.getInstance().getConfig("key0"));
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail();
        }
    }

    @Test
    void testMultipleConfigsWithoutPrefix() {
		try (MockedStatic<ConfigCenterFactory> configMock = Mockito.mockStatic(ConfigCenterFactory.class)) {
			MemConfigManager memConfigManager = new MemConfigManager();
			configMock.when(() -> ConfigCenterFactory.getInstance(anyString())).thenReturn(new ConfigCenter(memConfigManager));
            memConfigManager.addOrModifyProperty("application", "key0", "value0");
            memConfigManager.addOrModifyProperty("application", "prefix.key1", "value1");
            memConfigManager.addOrModifyProperty("application", "key2", "value2");
            memConfigManager.addOrModifyProperty("application", "prefix.key3", "value3");
            Map<String, String> configs = ConfigCenter.getInstance()
                    .getConfigs(null, null, Sets.newHashSet("key0", "key1", "key2", "key3"));
            assertEquals(2, configs.size());
            assertTrue(configs.containsKey("key0"));
            assertTrue(configs.containsKey("key2"));
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail();
        }

    }

    @Test
    void testMultipleConfigsWithPrefix() {
		try (MockedStatic<ConfigCenterFactory> configMock = Mockito.mockStatic(ConfigCenterFactory.class)) {
			MemConfigManager memConfigManager = new MemConfigManager();
			configMock.when(() -> ConfigCenterFactory.getInstance(anyString())).thenReturn(new ConfigCenter(memConfigManager));
            memConfigManager.addOrModifyProperty("application", "key0", "value0");
            memConfigManager.addOrModifyProperty("application", "prefix.key1", "value1");
            memConfigManager.addOrModifyProperty("application", "key2", "value2");
            memConfigManager.addOrModifyProperty("application", "prefix.key3", "value3");
            Map<String, String> configs = ConfigCenter.getInstance()
                    .getConfigs(null, "prefix", Sets.newHashSet("key0", "key1", "key2", "key3"));

            assertEquals(2, configs.size());
            assertTrue(configs.containsKey("key1"));
            assertTrue(configs.containsKey("key3"));
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail();
        }


    }

    @Test
    void testFullLevelConfig() {
		try (MockedStatic<ConfigCenterFactory> configMock = Mockito.mockStatic(ConfigCenterFactory.class)) {
			MemConfigManager memConfigManager = new MemConfigManager();
			configMock.when(() -> ConfigCenterFactory.getInstance(anyString())).thenReturn(new ConfigCenter(memConfigManager));
            memConfigManager.addOrModifyProperty("group", "key1", "value1");
            memConfigManager.addOrModifyProperty("group", "key2", "value1");
            memConfigManager.addOrModifyProperty("group", "key3", "value1");
            memConfigManager.addOrModifyProperty("group.IS1", "key2", "value2");
            memConfigManager.addOrModifyProperty("group.IS1", "key3", "value2");
            memConfigManager.addOrModifyProperty("group.SandBox", "key3", "value3");
            assertEquals("value1", ConfigCenter.getInstance().getConfig("group", "key1"));
            assertEquals("value2", ConfigCenter.getInstance().getConfig("group", "key2"));
            assertEquals("value3", ConfigCenter.getInstance().getConfig("group", "key3"));
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail();
        }

    }

    @Test
    void testWatchNamespace() {
		try (MockedStatic<ConfigCenterFactory> configMock = Mockito.mockStatic(ConfigCenterFactory.class)) {
			MemConfigManager memConfigManager = new MemConfigManager();
			configMock.when(() -> ConfigCenterFactory.getInstance(anyString())).thenReturn(new ConfigCenter(memConfigManager));
            final CountDownLatch changed = new CountDownLatch(1);
            ConfigCenter.getInstance().watchConfigs("namespace.not.pre.exists", e -> {
                assertEquals("value", e.getChange("key").getNewValue());
                changed.countDown();
            });
            memConfigManager.addOrModifyProperty("namespace.not.pre.exists", "key", "value");
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> changed.await());
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail();
        }

    }
}