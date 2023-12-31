package cn.iris.cloud.common.extension.support;

import cn.iris.cloud.common.extension.Activate;
import cn.iris.cloud.common.extension.SPI;

import java.util.Comparator;

/**
 * OrderComparator
 */
public class WrapperComparator implements Comparator<Object> {

	public static final Comparator<Object> COMPARATOR = new WrapperComparator();

	@Override
	public int compare(Object o1, Object o2) {
		if (o1 == null && o2 == null) {
			return 0;
		}
		if (o1 == null) {
			return -1;
		}
		if (o2 == null) {
			return 1;
		}
		if (o1.equals(o2)) {
			return 0;
		}

		Class clazz1 = (Class) o1;
		Class clazz2 = (Class) o2;

		Class<?> inf = findSpi(clazz1);

		OrderInfo a1 = parseOrder(clazz1);
		OrderInfo a2 = parseOrder(clazz2);

		// never return 0 even if n1 equals n2, otherwise, o1 and o2 will override each other in collection like HashSet
		return a1.order > a2.order ? 1 : -1;
	}

	private Class<?> findSpi(Class clazz) {
		if (clazz.getInterfaces().length == 0) {
			return null;
		}

		for (Class<?> intf : clazz.getInterfaces()) {
			if (intf.isAnnotationPresent(SPI.class)) {
				return intf;
			} else {
				Class result = findSpi(intf);
				if (result != null) {
					return result;
				}
			}
		}

		return null;
	}

	private OrderInfo parseOrder(Class<?> clazz) {
		OrderInfo info = new OrderInfo();
		if (clazz.isAnnotationPresent(Activate.class)) {
			Activate activate = clazz.getAnnotation(Activate.class);
			info.order = activate.order();
		}
		return info;
	}

	private static class OrderInfo {
		private int order;
	}
}