package cn.iris.cloud.event;

import cn.iris.cloud.common.extension.SPI;
import cn.iris.cloud.common.lang.Prioritized;
import cn.iris.cloud.common.utils.ReflectUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * The {@link Event cloud Event} Listener that is based on Java standard {@link java.util.EventListener} interface supports
 * the generic {@link Event}.
 * <p>
 * The {@link #onEvent(Event) handle method} will be notified when the matched-type {@link Event cloud Event} is
 * published, whose priority could be changed by {@link #getPriority()} method.
 *
 * @param <E> the concrete class of {@link Event cloud Event}
 * @see Event
 * @see java.util.EventListener
 * @since 2.7.5
 */
@SPI
@FunctionalInterface
public interface EventListener<E extends Event> extends java.util.EventListener, Prioritized {

	/**
	 * Handle a {@link Event cloud Event} when it's be published
	 *
	 * @param event a {@link Event cloud Event}
	 */
	void onEvent(E event);

	/**
	 * The priority of {@link EventListener current listener}.
	 *
	 * @return the value is more greater, the priority is more lower.
	 * {@link Integer#MIN_VALUE} indicates the highest priority. The default value is {@link Integer#MAX_VALUE}.
	 * The comparison rule , refer to {@link #compareTo}.
	 */
	default int getPriority() {
		return NORMAL_PRIORITY;
	}

	/**
	 * Find the {@link Class type} {@link Event cloud event} from the specified {@link EventListener cloud event listener}
	 *
	 * @param listener the {@link Class class} of {@link EventListener cloud event listener}
	 * @return <code>null</code> if not found
	 */
	static Class<? extends Event> findEventType(EventListener<?> listener) {
		return findEventType(listener.getClass());
	}

	/**
	 * Find the {@link Class type} {@link Event cloud event} from the specified {@link EventListener cloud event listener}
	 *
	 * @param listenerClass the {@link Class class} of {@link EventListener cloud event listener}
	 * @return <code>null</code> if not found
	 */
	static Class<? extends Event> findEventType(Class<?> listenerClass) {
		Class<? extends Event> eventType = null;

		if (listenerClass != null && EventListener.class.isAssignableFrom(listenerClass)) {
			eventType = ReflectUtils.findParameterizedTypes(listenerClass)
					.stream()
					.map(EventListener::findEventType)
					.filter(Objects::nonNull)
					.findAny()
					.orElse((Class) findEventType(listenerClass.getSuperclass()));
		}

		return eventType;
	}

	/**
	 * Find the type {@link Event cloud event} from the specified {@link ParameterizedType} presents
	 * a class of {@link EventListener cloud event listener}
	 *
	 * @param parameterizedType the {@link ParameterizedType} presents a class of {@link EventListener cloud event listener}
	 * @return <code>null</code> if not found
	 */
	static Class<? extends Event> findEventType(ParameterizedType parameterizedType) {
		Class<? extends Event> eventType = null;

		Type rawType = parameterizedType.getRawType();
		if ((rawType instanceof Class) && EventListener.class.isAssignableFrom((Class) rawType)) {
			Type[] typeArguments = parameterizedType.getActualTypeArguments();
			for (Type typeArgument : typeArguments) {
				if (typeArgument instanceof Class) {
					Class argumentClass = (Class) typeArgument;
					if (Event.class.isAssignableFrom(argumentClass)) {
						eventType = argumentClass;
						break;
					}
				}
			}
		}

		return eventType;
	}
}