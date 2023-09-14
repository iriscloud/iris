package cn.iris.cloud.common.extension;

import cn.iris.cloud.common.URL;
import cn.iris.cloud.common.compiler.Compiler;
import cn.iris.cloud.common.constants.CommonConstants;
import cn.iris.cloud.common.context.Lifecycle;
import cn.iris.cloud.common.extension.support.ActivateComparator;
import cn.iris.cloud.common.extension.support.WrapperComparator;
import cn.iris.cloud.common.lang.Prioritized;
import cn.iris.cloud.common.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static java.util.Collections.sort;
import static java.util.ServiceLoader.load;
import static java.util.stream.StreamSupport.stream;

/**
 * {@code IrisBootstrap} and this class are at present designed to be singleton or static (by itself totally static or
 * uses some static fields). So the instances returned from them are of process or classloader scope. If you want to
 * support multiple cloud servers in a single process, you may need to refactor these three classes.
 * <p>
 * Load cloud extensions
 * <ul>
 * <li>auto inject dependency extension</li>
 * <li>auto wrap extension in wrapper</li>
 * <li>default extension is an adaptive instance</li>
 * </ul>
 *
 * @see <a href="http://java.sun.com/j2se/1.5.0/docs/guide/jar/jar.html#Service%20Provider">Service Provider in Java
 * 5</a>
 * @see SPI
 * @see Adaptive
 * @see Activate
 */
public class ExtensionLoader<T> {

	private static Logger logger = LoggerFactory.getLogger(ExtensionLoader.class);

	public static final String DEFAULT = "default";

	private static final Pattern NAME_SEPARATOR = Pattern.compile("\\s*[,]+\\s*");

	private static final ConcurrentMap<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>(64);

	private static final ConcurrentMap<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>(64);

	private final Class<?> type;

	private final ExtensionFactory objectFactory;

	private final ConcurrentMap<Class<?>, String> cachedNames = new ConcurrentHashMap<>();

	private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();

	private final Map<String, Object> cachedActivates = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();
	private final Holder<Object> cachedAdaptiveInstance = new Holder<>();
	private volatile Class<?> cachedAdaptiveClass = null;
	private String cachedDefaultName;
	private volatile Throwable createAdaptiveInstanceError;

	private Set<Class<?>> cachedWrapperClasses;

	private Map<String, IllegalStateException> exceptions = new ConcurrentHashMap<>();

	private static volatile LoadingStrategy[] strategies = loadLoadingStrategies();

	public static void setLoadingStrategies(LoadingStrategy... strategies) {
		if (ArrayUtils.isNotEmpty(strategies)) {
			ExtensionLoader.strategies = strategies;
		}
	}

	/**
	 * Load all {@link Prioritized prioritized} {@link LoadingStrategy Loading Strategies} via {@link ServiceLoader}
	 *
	 * @return non-null
	 * @since 2.7.7
	 */
	private static LoadingStrategy[] loadLoadingStrategies() {
		return stream(load(LoadingStrategy.class, ClassUtils.getClassLoader()).spliterator(), false).sorted()
				.toArray(LoadingStrategy[]::new);
	}

	/**
	 * Get all {@link LoadingStrategy Loading Strategies}
	 *
	 * @return non-null
	 * @see LoadingStrategy
	 * @see Prioritized
	 * @since 2.7.7
	 */
	public static List<LoadingStrategy> getLoadingStrategies() {
		return asList(strategies);
	}

	private ExtensionLoader(Class<?> type) {
		this.type = type;
		this.objectFactory = (type == ExtensionFactory.class ? null
				: ExtensionLoader.getExtensionLoader(ExtensionFactory.class).getAdaptiveExtension());
	}

	private static <T> boolean withExtensionAnnotation(Class<T> type) {
		return type.isAnnotationPresent(SPI.class);
	}

	@SuppressWarnings("unchecked")
	public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
		if (type == null) {
			throw new IllegalArgumentException("Extension type == null");
		}
		if (!type.isInterface()) {
			throw new IllegalArgumentException("Extension type (" + type + ") is not an interface!");
		}
		if (!withExtensionAnnotation(type)) {
			throw new IllegalArgumentException("Extension type (" + type
					+ ") is not an extension, because it is NOT annotated with @" + SPI.class.getSimpleName() + "!");
		}

		ExtensionLoader<T> loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
		if (loader == null) {
			EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<T>(type));
			loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
		}
		return loader;
	}

	public static <I> I loadOrByDefaultFactory(Class<I> clazz, String name, Supplier<I> defaultIfNullFactory) {
		I impl = null;
		try {
			impl =
					ExtensionLoader.getExtensionLoader(clazz).getExtension(StringUtils.isEmpty(name) ? DEFAULT : name);

			if (impl != null) {
				return impl;
			} else {
				return defaultIfNullFactory != null ? defaultIfNullFactory.get() : null;
			}
		} catch (RuntimeException e) {
			logger.info("will use defaultValue, load:{}\tbyName:[{}]\tdefault:{}\tcause:{}", clazz, name,
					defaultIfNullFactory, e.getMessage());
		} catch (Exception e) {
			logger.error("will use defaultValue, load:{}\tbyName:[{}]\tdefaultIsNull:{}\terror:{}", clazz, name,
					defaultIfNullFactory, e.getMessage());
		}
		return Optional.ofNullable(impl)
				.orElseGet(() -> (defaultIfNullFactory != null ? defaultIfNullFactory.get() : null));

	}

	// For testing purposes only
	public static void resetExtensionLoader(Class type) {
		ExtensionLoader loader = EXTENSION_LOADERS.get(type);
		if (loader != null) {
			// Remove all instances associated with this loader as well
			Map<String, Class<?>> classes = loader.getExtensionClasses();
			for (Map.Entry<String, Class<?>> entry : classes.entrySet()) {
				EXTENSION_INSTANCES.remove(entry.getValue());
			}
			classes.clear();
			EXTENSION_LOADERS.remove(type);
		}
	}

	public static void destroyAll() {
		EXTENSION_INSTANCES.forEach((_type, instance) -> {
			if (instance instanceof Lifecycle) {
				Lifecycle lifecycle = (Lifecycle) instance;
				try {
					lifecycle.destroy();
				} catch (Exception e) {
					logger.error("Error destroying extension " + lifecycle, e);
				}
			}
		});
	}

	private static ClassLoader findClassLoader() {
		return ClassUtils.getClassLoader(ExtensionLoader.class);
	}

	public String getExtensionName(T extensionInstance) {
		return this.getExtensionName(extensionInstance.getClass());
	}

	public String getExtensionName(Class<?> extensionClass) {
		this.getExtensionClasses();// load class
		return this.cachedNames.get(extensionClass);
	}

	/**
	 * This is equivalent to {@code getActivateExtension(url, key, null)}
	 *
	 * @param url url
	 * @param key url parameter key which used to get extension point names
	 * @return extension list which are activated.
	 * @see #getActivateExtension(URL, String, String)
	 */
	public List<T> getActivateExtension(URL url, String key) {
		return this.getActivateExtension(url, key, null);
	}

	/**
	 * This is equivalent to {@code getActivateExtension(url, values, null)}
	 *
	 * @param url    url
	 * @param values extension point names
	 * @return extension list which are activated
	 * @see #getActivateExtension(URL, String[], String)
	 */
	public List<T> getActivateExtension(URL url, String[] values) {
		return this.getActivateExtension(url, values, null);
	}

	/**
	 * This is equivalent to {@code getActivateExtension(url, url.getParameter(key).split(","), null)}
	 *
	 * @param url   url
	 * @param key   url parameter key which used to get extension point names
	 * @param group group
	 * @return extension list which are activated.
	 * @see #getActivateExtension(URL, String[], String)
	 */
	public List<T> getActivateExtension(URL url, String key, String group) {
		String value = url.getParameter(key);
		return this.getActivateExtension(url,
				StringUtils.isEmpty(value) ? null : CommonConstants.COMMA_SPLIT_PATTERN.split(value), group);
	}

	/**
	 * Get activate extensions.
	 *
	 * @param url    url
	 * @param values extension point names
	 * @param group  group
	 * @return extension list which are activated
	 * @see Activate
	 */
	public List<T> getActivateExtension(URL url, String[] values, String group) {
		List<T> activateExtensions = new ArrayList<>();
		// solve the bug of using @SPI's wrapper method to report a null pointer exception.
		TreeMap<Class, T> activateExtensionsMap = new TreeMap<>(ActivateComparator.COMPARATOR);
		List<String> names = values == null ? new ArrayList<>(0) : asList(values);
		if (!names.contains(CommonConstants.REMOVE_VALUE_PREFIX + CommonConstants.DEFAULT_KEY)) {
			this.getExtensionClasses();
			for (Map.Entry<String, Object> entry : this.cachedActivates.entrySet()) {
				String name = entry.getKey();
				Object activate = entry.getValue();

				String[] activateGroup, activateValue;

				if (activate instanceof Activate) {
					activateGroup = ((Activate) activate).group();
					activateValue = ((Activate) activate).value();
				} else {
					continue;
				}
				if (this.isMatchGroup(group, activateGroup) && !names.contains(name)
						&& !names.contains(CommonConstants.REMOVE_VALUE_PREFIX + name)
						&& this.isActive(activateValue, url)) {
					activateExtensionsMap.put(this.getExtensionClass(name), this.getExtension(name));
				}
			}
			if (!activateExtensionsMap.isEmpty()) {
				activateExtensions.addAll(activateExtensionsMap.values());
			}
		}
		List<T> loadedExtensions = new ArrayList<>();
		for (int i = 0; i < names.size(); i++) {
			String name = names.get(i);
			if (!name.startsWith(CommonConstants.REMOVE_VALUE_PREFIX)
					&& !names.contains(CommonConstants.REMOVE_VALUE_PREFIX + name)) {
				if (CommonConstants.DEFAULT_KEY.equals(name)) {
					if (!loadedExtensions.isEmpty()) {
						activateExtensions.addAll(0, loadedExtensions);
						loadedExtensions.clear();
					}
				} else {
					loadedExtensions.add(this.getExtension(name));
				}
			}
		}
		if (!loadedExtensions.isEmpty()) {
			activateExtensions.addAll(loadedExtensions);
		}
		return activateExtensions;
	}

	private boolean isMatchGroup(String group, String[] groups) {
		if (StringUtils.isEmpty(group)) {
			return true;
		}
		if (groups != null && groups.length > 0) {
			for (String g : groups) {
				if (group.equals(g)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isActive(String[] keys, URL url) {
		if (keys.length == 0) {
			return true;
		}
		for (String key : keys) {
			// @Active(value="key1:value1, key2:value2")
			String keyValue = null;
			if (key.contains(":")) {
				String[] arr = key.split(":");
				key = arr[0];
				keyValue = arr[1];
			}

			for (Map.Entry<String, String> entry : url.getParameters().entrySet()) {
				String k = entry.getKey();
				String v = entry.getValue();
				if ((k.equals(key) || k.endsWith("." + key))
						&& ((keyValue != null && keyValue.equals(v)) || (keyValue == null && ConfigUtils.isNotEmpty(v)))) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Get extension's instance. Return <code>null</code> if extension is not found or is not initialized. Pls. note
	 * that this method will not trigger extension load.
	 * <p>
	 * In order to trigger extension load, call {@link #getExtension(String)} instead.
	 *
	 * @see #getExtension(String)
	 */
	@SuppressWarnings("unchecked")
	public T getLoadedExtension(String name) {
		if (StringUtils.isEmpty(name)) {
			throw new IllegalArgumentException("Extension name == null");
		}
		Holder<Object> holder = this.getOrCreateHolder(name);
		return (T) holder.get();
	}

	private Holder<Object> getOrCreateHolder(String name) {
		Holder<Object> holder = this.cachedInstances.get(name);
		if (holder == null) {
			this.cachedInstances.putIfAbsent(name, new Holder<>());
			holder = this.cachedInstances.get(name);
		}
		return holder;
	}

	/**
	 * Return the list of extensions which are already loaded.
	 * <p>
	 * Usually {@link #getSupportedExtensions()} should be called in order to get all extensions.
	 *
	 * @see #getSupportedExtensions()
	 */
	public Set<String> getLoadedExtensions() {
		return Collections.unmodifiableSet(new TreeSet<>(this.cachedInstances.keySet()));
	}

	public List<T> getLoadedExtensionInstances() {
		List<T> instances = new ArrayList<>();
		this.cachedInstances.values().forEach(holder -> instances.add((T) holder.get()));
		return instances;
	}

	public Object getLoadedAdaptiveExtensionInstances() {
		return this.cachedAdaptiveInstance.get();
	}

	/**
	 * Find the extension with the given name. If the specified name is not found, then {@link IllegalStateException}
	 * will be thrown.
	 */
	@SuppressWarnings("unchecked")
	public T getExtension(String name) {
		return this.getExtension(name, true);
	}

	public T getExtension(String name, boolean wrap) {
		if (StringUtils.isEmpty(name)) {
			throw new IllegalArgumentException("Extension name == null");
		}
		if ("true".equals(name)) {
			return this.getDefaultExtension();
		}
		final Holder<Object> holder = this.getOrCreateHolder(name);
		Object instance = holder.get();
		if (instance == null) {
			synchronized (holder) {
				instance = holder.get();
				if (instance == null) {
					instance = this.createExtension(name, wrap);
					holder.set(instance);
				}
			}
		}
		return (T) instance;
	}

	/**
	 * Get the extension by specified name if found, or {@link #getDefaultExtension() returns the default one}
	 *
	 * @param name the name of extension
	 * @return non-null
	 */
	public T getOrDefaultExtension(String name) {
		return this.containsExtension(name) ? this.getExtension(name) : this.getDefaultExtension();
	}

	/**
	 * Return default extension, return <code>null</code> if it's not configured.
	 */
	public T getDefaultExtension() {
		this.getExtensionClasses();
		if (StringUtils.isBlank(this.cachedDefaultName) || "true".equals(this.cachedDefaultName)) {
			return null;
		}
		return this.getExtension(this.cachedDefaultName);
	}

	public boolean hasExtension(String name) {
		if (StringUtils.isEmpty(name)) {
			throw new IllegalArgumentException("Extension name == null");
		}
		Class<?> c = this.getExtensionClass(name);
		return c != null;
	}

	public Set<String> getSupportedExtensions() {
		Map<String, Class<?>> clazzes = this.getExtensionClasses();
		return Collections.unmodifiableSet(new TreeSet<>(clazzes.keySet()));
	}

	public Set<T> getSupportedExtensionInstances() {
		List<T> instances = new LinkedList<>();
		Set<String> supportedExtensions = this.getSupportedExtensions();
		if (CollectionUtils.isNotEmpty(supportedExtensions)) {
			for (String name : supportedExtensions) {
				instances.add(this.getExtension(name));
			}
		}
		// sort the Prioritized instances
		sort(instances, Prioritized.COMPARATOR);
		return new LinkedHashSet<>(instances);
	}

	/**
	 * Return default extension name, return <code>null</code> if not configured.
	 */
	public String getDefaultExtensionName() {
		this.getExtensionClasses();
		return this.cachedDefaultName;
	}

	/**
	 * Register new extension via API
	 *
	 * @param name  extension name
	 * @param clazz extension class
	 * @throws IllegalStateException when extension with the same name has already been registered.
	 */
	public void addExtension(String name, Class<?> clazz) {
		this.getExtensionClasses(); // load classes

		if (!this.type.isAssignableFrom(clazz)) {
			throw new IllegalStateException("Input type " + clazz + " doesn't implement the Extension " + this.type);
		}
		if (clazz.isInterface()) {
			throw new IllegalStateException("Input type " + clazz + " can't be interface!");
		}

		if (!clazz.isAnnotationPresent(Adaptive.class)) {
			if (StringUtils.isBlank(name)) {
				throw new IllegalStateException("Extension name is blank (Extension " + this.type + ")!");
			}
			if (this.cachedClasses.get().containsKey(name)) {
				throw new IllegalStateException(
						"Extension name " + name + " already exists (Extension " + this.type + ")!");
			}

			this.cachedNames.put(clazz, name);
			this.cachedClasses.get().put(name, clazz);
		} else {
			if (this.cachedAdaptiveClass != null) {
				throw new IllegalStateException("Adaptive Extension already exists (Extension " + this.type + ")!");
			}

			this.cachedAdaptiveClass = clazz;
		}
	}

	/**
	 * Replace the existing extension via API
	 *
	 * @param name  extension name
	 * @param clazz extension class
	 * @throws IllegalStateException when extension to be placed doesn't exist
	 * @deprecated not recommended any longer, and use only when test
	 */
	@Deprecated
	public void replaceExtension(String name, Class<?> clazz) {
		this.getExtensionClasses(); // load classes

		if (!this.type.isAssignableFrom(clazz)) {
			throw new IllegalStateException("Input type " + clazz + " doesn't implement Extension " + this.type);
		}
		if (clazz.isInterface()) {
			throw new IllegalStateException("Input type " + clazz + " can't be interface!");
		}

		if (!clazz.isAnnotationPresent(Adaptive.class)) {
			if (StringUtils.isBlank(name)) {
				throw new IllegalStateException("Extension name is blank (Extension " + this.type + ")!");
			}
			if (!this.cachedClasses.get().containsKey(name)) {
				throw new IllegalStateException(
						"Extension name " + name + " doesn't exist (Extension " + this.type + ")!");
			}

			this.cachedNames.put(clazz, name);
			this.cachedClasses.get().put(name, clazz);
			this.cachedInstances.remove(name);
		} else {
			if (this.cachedAdaptiveClass == null) {
				throw new IllegalStateException("Adaptive Extension doesn't exist (Extension " + this.type + ")!");
			}

			this.cachedAdaptiveClass = clazz;
			this.cachedAdaptiveInstance.set(null);
		}
	}

	@SuppressWarnings("unchecked")
	public T getAdaptiveExtension() {
		Object instance = this.cachedAdaptiveInstance.get();
		if (instance == null) {
			if (this.createAdaptiveInstanceError != null) {
				throw new IllegalStateException(
						"Failed to create adaptive instance: " + this.createAdaptiveInstanceError.toString(),
						this.createAdaptiveInstanceError);
			}

			synchronized (this.cachedAdaptiveInstance) {
				instance = this.cachedAdaptiveInstance.get();
				if (instance == null) {
					try {
						instance = this.createAdaptiveExtension();
						this.cachedAdaptiveInstance.set(instance);
					} catch (Throwable t) {
						this.createAdaptiveInstanceError = t;
						throw new IllegalStateException("Failed to create adaptive instance: " + t.toString(), t);
					}
				}
			}
		}

		return (T) instance;
	}

	private IllegalStateException findException(String name) {
		StringBuilder buf = new StringBuilder("No such extension " + this.type.getName() + " by name " + name);

		int i = 1;
		for (Map.Entry<String, IllegalStateException> entry : this.exceptions.entrySet()) {
			if (entry.getKey().toLowerCase().startsWith(name.toLowerCase())) {
				if (i == 1) {
					buf.append(", possible causes: ");
				}
				buf.append("\r\n(");
				buf.append(i++);
				buf.append(") ");
				buf.append(entry.getKey());
				buf.append(":\r\n");
				buf.append(StringUtils.toString(entry.getValue()));
			}
		}

		if (i == 1) {
			buf.append(", no related exception was found, please check whether related SPI module is missing.");
		}
		return new IllegalStateException(buf.toString());
	}

	@SuppressWarnings("unchecked")
	private T createExtension(String name, boolean wrap) {
		Class<?> clazz = this.getExtensionClasses().get(name);
		if (clazz == null) {
			throw this.findException(name);
		}
		try {
			T instance = (T) EXTENSION_INSTANCES.get(clazz);
			if (instance == null) {
				EXTENSION_INSTANCES.putIfAbsent(clazz, clazz.getDeclaredConstructor().newInstance());
				instance = (T) EXTENSION_INSTANCES.get(clazz);
			}
			this.injectExtension(instance);

			if (wrap) {

				List<Class<?>> wrapperClassesList = new ArrayList<>();
				if (this.cachedWrapperClasses != null) {
					wrapperClassesList.addAll(this.cachedWrapperClasses);
					wrapperClassesList.sort(WrapperComparator.COMPARATOR);
					Collections.reverse(wrapperClassesList);
				}

				if (CollectionUtils.isNotEmpty(wrapperClassesList)) {
					for (Class<?> wrapperClass : wrapperClassesList) {
						Wrapper wrapper = wrapperClass.getAnnotation(Wrapper.class);
						if (wrapper == null || (ArrayUtils.contains(wrapper.matches(), name)
								&& !ArrayUtils.contains(wrapper.mismatches(), name))) {
							instance =
									this.injectExtension((T) wrapperClass.getConstructor(this.type).newInstance(instance));
						}
					}
				}
			}

			this.initExtension(instance);
			return instance;
		} catch (Throwable t) {
			throw new IllegalStateException("Extension instance (name: " + name + ", class: " + this.type
					+ ") couldn't be instantiated: " + t.getMessage(), t);
		}
	}

	private boolean containsExtension(String name) {
		return this.getExtensionClasses().containsKey(name);
	}

	private T injectExtension(T instance) {

		if (this.objectFactory == null) {
			return instance;
		}

		try {
			for (Method method : instance.getClass().getMethods()) {
				if (!this.isSetter(method)) {
					continue;
				}
				/**
				 * Check {@link DisableInject} to see if we need auto injection for this property
				 */
				if (method.getAnnotation(DisableInject.class) != null) {
					continue;
				}
				Class<?> pt = method.getParameterTypes()[0];
				if (ReflectUtils.isPrimitives(pt)) {
					continue;
				}

				try {
					String property = this.getSetterProperty(method);
					Object object = this.objectFactory.getExtension(pt, property);
					if (object != null) {
						method.invoke(instance, object);
					}
				} catch (Exception e) {
					logger.error("Failed to inject via method " + method.getName() + " of interface "
							+ this.type.getName() + ": " + e.getMessage(), e);
				}

			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return instance;
	}

	private void initExtension(T instance) {
		if (instance instanceof Lifecycle) {
			Lifecycle lifecycle = (Lifecycle) instance;
			lifecycle.initialize();
		}
	}

	/**
	 * get properties name for setter, for instance: setVersion, return "version"
	 * <p>
	 * return "", if setter name with length less than 3
	 */
	private String getSetterProperty(Method method) {
		return method.getName().length() > 3
				? method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4) : "";
	}

	/**
	 * return true if and only if:
	 * <p>
	 * 1, public
	 * <p>
	 * 2, name starts with "set"
	 * <p>
	 * 3, only has one parameter
	 */
	private boolean isSetter(Method method) {
		return method.getName().startsWith("set") && method.getParameterTypes().length == 1
				&& Modifier.isPublic(method.getModifiers());
	}

	private Class<?> getExtensionClass(String name) {
		if (this.type == null) {
			throw new IllegalArgumentException("Extension type == null");
		}
		if (name == null) {
			throw new IllegalArgumentException("Extension name == null");
		}
		return this.getExtensionClasses().get(name);
	}

	private Map<String, Class<?>> getExtensionClasses() {
		Map<String, Class<?>> classes = this.cachedClasses.get();
		if (classes == null) {
			synchronized (this.cachedClasses) {
				classes = this.cachedClasses.get();
				if (classes == null) {
					classes = this.loadExtensionClasses();
					this.cachedClasses.set(classes);
				}
			}
		}
		return classes;
	}

	/**
	 * synchronized in getExtensionClasses
	 */
	private Map<String, Class<?>> loadExtensionClasses() {
		this.cacheDefaultExtensionName();

		Map<String, Class<?>> extensionClasses = new HashMap<>();

		for (LoadingStrategy strategy : strategies) {
			this.loadDirectory(extensionClasses, strategy.directory(), this.type.getName(),
					strategy.preferExtensionClassLoader(), strategy.overridden(), strategy.excludedPackages());
			this.loadDirectory(extensionClasses, strategy.directory(),
					this.type.getName().replace("org.apache", "com.alibaba"), strategy.preferExtensionClassLoader(),
					strategy.overridden(), strategy.excludedPackages());
		}

		return extensionClasses;
	}

	/**
	 * extract and cache default extension name if exists
	 */
	private void cacheDefaultExtensionName() {
		final SPI defaultAnnotation = this.type.getAnnotation(SPI.class);
		if (defaultAnnotation == null) {
			return;
		}

		String value = defaultAnnotation.value();
		if ((value = value.trim()).length() > 0) {
			String[] names = NAME_SEPARATOR.split(value);
			if (names.length > 1) {
				throw new IllegalStateException("More than 1 default extension name on extension " + this.type.getName()
						+ ": " + Arrays.toString(names));
			}
			if (names.length == 1) {
				this.cachedDefaultName = names[0];
			}
		}
	}

	private void loadDirectory(Map<String, Class<?>> extensionClasses, String dir, String type) {
		this.loadDirectory(extensionClasses, dir, type, false, false);
	}

	private void loadDirectory(Map<String, Class<?>> extensionClasses, String dir, String type,
							   boolean extensionLoaderClassLoaderFirst, boolean overridden, String... excludedPackages) {
		String fileName = dir + type;
		try {
			Enumeration<java.net.URL> urls = null;
			ClassLoader classLoader = findClassLoader();

			// try to load from ExtensionLoader's ClassLoader first
			if (extensionLoaderClassLoaderFirst) {
				ClassLoader extensionLoaderClassLoader = ExtensionLoader.class.getClassLoader();
				if (ClassLoader.getSystemClassLoader() != extensionLoaderClassLoader) {
					urls = extensionLoaderClassLoader.getResources(fileName);
				}
			}

			if (urls == null || !urls.hasMoreElements()) {
				if (classLoader != null) {
					urls = classLoader.getResources(fileName);
				} else {
					urls = ClassLoader.getSystemResources(fileName);
				}
			}

			if (urls != null) {
				while (urls.hasMoreElements()) {
					java.net.URL resourceURL = urls.nextElement();
					this.loadResource(extensionClasses, classLoader, resourceURL, overridden, excludedPackages);
				}
			}
		} catch (Throwable t) {
			logger.error("Exception occurred when loading extension class (interface: " + type + ", description file: "
					+ fileName + ").", t);
		}
	}

	private void loadResource(Map<String, Class<?>> extensionClasses, ClassLoader classLoader, java.net.URL resourceURL,
							  boolean overridden, String... excludedPackages) {
		try {
			try (BufferedReader reader =
						 new BufferedReader(new InputStreamReader(resourceURL.openStream(), StandardCharsets.UTF_8))) {
				String line;
				String clazz = null;
				while ((line = reader.readLine()) != null) {
					final int ci = line.indexOf('#');
					if (ci >= 0) {
						line = line.substring(0, ci);
					}
					line = line.trim();
					if (line.length() > 0) {
						try {
							String name = null;
							int i = line.indexOf('=');
							if (i > 0) {
								name = line.substring(0, i).trim();
								clazz = line.substring(i + 1).trim();
							} else {
								clazz = line;
							}
							if (StringUtils.isNotEmpty(clazz) && !this.isExcluded(clazz, excludedPackages)) {
								this.loadClass(extensionClasses, resourceURL, Class.forName(clazz, true, classLoader),
										name, overridden);
							}
						} catch (Throwable t) {
							IllegalStateException e = new IllegalStateException(
									"Failed to load extension class (interface: " + this.type + ", class line: " + line
											+ ") in " + resourceURL + ", cause: " + t.getMessage(),
									t);
							this.exceptions.put(line, e);
						}
					}
				}
			}
		} catch (Throwable t) {
			logger.error("Exception occurred when loading extension class (interface: " + this.type + ", class file: "
					+ resourceURL + ") in " + resourceURL, t);
		}
	}

	private boolean isExcluded(String className, String... excludedPackages) {
		if (excludedPackages != null) {
			for (String excludePackage : excludedPackages) {
				if (className.startsWith(excludePackage + ".")) {
					return true;
				}
			}
		}
		return false;
	}

	private void loadClass(Map<String, Class<?>> extensionClasses, java.net.URL resourceURL, Class<?> clazz,
						   String name, boolean overridden) throws NoSuchMethodException {
		if (!this.type.isAssignableFrom(clazz)) {
			throw new IllegalStateException("Error occurred when loading extension class (interface: " + this.type
					+ ", class line: " + clazz.getName() + "), class " + clazz.getName() + " is not subtype of interface.");
		}
		if (clazz.isAnnotationPresent(Adaptive.class)) {
			this.cacheAdaptiveClass(clazz, overridden);
		} else if (this.isWrapperClass(clazz)) {
			this.cacheWrapperClass(clazz);
		} else {
			clazz.getConstructor();
			if (StringUtils.isEmpty(name)) {
				name = this.findAnnotationName(clazz);
				if (name.length() == 0) {
					throw new IllegalStateException(
							"No such extension name for the class " + clazz.getName() + " in the config " + resourceURL);
				}
			}

			String[] names = NAME_SEPARATOR.split(name);
			if (ArrayUtils.isNotEmpty(names)) {
				this.cacheActivateClass(clazz, names[0]);
				for (String n : names) {
					this.cacheName(clazz, n);
					this.saveInExtensionClass(extensionClasses, clazz, n, overridden);
				}
			}
		}
	}

	/**
	 * cache name
	 */
	private void cacheName(Class<?> clazz, String name) {
		if (!this.cachedNames.containsKey(clazz)) {
			this.cachedNames.put(clazz, name);
		}
	}

	/**
	 * put clazz in extensionClasses
	 */
	private void saveInExtensionClass(Map<String, Class<?>> extensionClasses, Class<?> clazz, String name,
									  boolean overridden) {
		Class<?> c = extensionClasses.get(name);
		if (c == null || overridden) {
			extensionClasses.put(name, clazz);
		} else if (c != clazz) {
			String duplicateMsg = "Duplicate extension " + this.type.getName() + " name " + name + " on " + c.getName()
					+ " and " + clazz.getName();
			logger.error(duplicateMsg);
			throw new IllegalStateException(duplicateMsg);
		}
	}

	/**
	 * cache Activate class which is annotated with <code>Activate</code>
	 * <p>
	 * for compatibility, also cache class with old alibaba Activate annotation
	 */
	private void cacheActivateClass(Class<?> clazz, String name) {
		Activate activate = clazz.getAnnotation(Activate.class);
		if (activate != null) {
			this.cachedActivates.put(name, activate);
		}
	}

	/**
	 * cache Adaptive class which is annotated with <code>Adaptive</code>
	 */
	private void cacheAdaptiveClass(Class<?> clazz, boolean overridden) {
		if (this.cachedAdaptiveClass == null || overridden) {
			this.cachedAdaptiveClass = clazz;
		} else if (!this.cachedAdaptiveClass.equals(clazz)) {
			throw new IllegalStateException(
					"More than 1 adaptive class found: " + this.cachedAdaptiveClass.getName() + ", " + clazz.getName());
		}
	}

	/**
	 * cache wrapper class
	 * <p>
	 * like: ProtocolFilterWrapper, ProtocolListenerWrapper
	 */
	private void cacheWrapperClass(Class<?> clazz) {
		if (this.cachedWrapperClasses == null) {
			this.cachedWrapperClasses = new ConcurrentHashSet<>();
		}
		this.cachedWrapperClasses.add(clazz);
	}

	/**
	 * test if clazz is a wrapper class
	 * <p>
	 * which has Constructor with given class type as its only argument
	 */
	private boolean isWrapperClass(Class<?> clazz) {
		try {
			clazz.getConstructor(this.type);
			return true;
		} catch (NoSuchMethodException e) {
			return false;
		}
	}

	@SuppressWarnings("deprecation")
	private String findAnnotationName(Class<?> clazz) {
		String name = clazz.getSimpleName();
		if (name.endsWith(this.type.getSimpleName())) {
			name = name.substring(0, name.length() - this.type.getSimpleName().length());
		}
		return name.toLowerCase();
	}

	@SuppressWarnings("unchecked")
	private T createAdaptiveExtension() {
		try {
			return this.injectExtension((T) this.getAdaptiveExtensionClass().newInstance());
		} catch (Exception e) {
			throw new IllegalStateException(
					"Can't create adaptive extension " + this.type + ", cause: " + e.getMessage(), e);
		}
	}

	private Class<?> getAdaptiveExtensionClass() {
		this.getExtensionClasses();
		if (this.cachedAdaptiveClass != null) {
			return this.cachedAdaptiveClass;
		}
		return this.cachedAdaptiveClass = this.createAdaptiveExtensionClass();
	}

	private Class<?> createAdaptiveExtensionClass() {
		String code = new AdaptiveClassCodeGenerator(this.type, this.cachedDefaultName).generate();
		ClassLoader classLoader = findClassLoader();
		Compiler compiler = ExtensionLoader.getExtensionLoader(Compiler.class).getAdaptiveExtension();
		return compiler.compile(code, classLoader);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + "[" + this.type.getName() + "]";
	}

}
