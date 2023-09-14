package cn.iris.cloud.configcenter.configmanager;

import cn.iris.cloud.configcenter.dynamic.ConfigChange;
import cn.iris.cloud.configcenter.dynamic.ConfigChangeType;
import cn.iris.cloud.configcenter.dynamic.ConfigChangedEvent;
import cn.iris.cloud.configcenter.dynamic.ConfigurationListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MemConfigManager
 *
 * @author wuhao
 */
public class MemConfigManager implements ConfigManager {

    private ConcurrentHashMap<String, String> configCache = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, ConfigurationListener> configListeners = new ConcurrentHashMap<>();

    @Override
    public Set<String> keySet(String namespace) {
        return configCache.keySet();
    }

    @Override
    public String getConfig(String namespace, String key) {
        return configCache.get(key);
    }

    /**
     * watchConfigs
     *
     * @param namespace
     * @param watcher
     */

    @Override
    public void watchConfigs(String namespace, ConfigurationListener watcher) {
        configListeners.put(namespace, watcher);
    }

    /**
     * watchConfigs
     *
     * @param namespace
     * @param watcher
     */

    @Override
    public void watchConfigs(String namespace, ConfigurationListener watcher, Set<String> keys) {
        configListeners.put(namespace, watcher);
    }

    /**
     * unwatchConfigs
     *
     * @param namespace
     * @param watcher
     */

    @Override
    public void unwatchConfigs(String namespace, ConfigurationListener watcher) {
        configListeners.put(namespace, watcher);
    }

    /**
     * 添加变更，仅mem使用
     *
     * @param namespace
     * @param key
     * @param value
     * @return
     */
    public void addOrModifyProperty(String namespace, String key, String value) {
        configCache.put(key, value);
        ConfigChangedEvent configChangedEvent = getConfigChangedEvent(namespace, key, value);
        ConfigurationListener listener = configListeners.get(namespace);
        if (listener != null) {
            listener.process(configChangedEvent);
        }
    }

    /**
     * 获取变更通知
     *
     * @param namespace
     * @param key
     * @param value
     * @return
     */
    private ConfigChangedEvent getConfigChangedEvent(String namespace, String key, String value) {
        String oldValue = configCache.get(key);
        ConfigChange configChange = new ConfigChange(namespace, key, oldValue, value, ConfigChangeType.MODIFIED);
        Map<String, ConfigChange> configChangeMap = new HashMap<>();
        configChangeMap.put(key, configChange);
        return new ConfigChangedEvent(namespace, configChangeMap);
    }

}
