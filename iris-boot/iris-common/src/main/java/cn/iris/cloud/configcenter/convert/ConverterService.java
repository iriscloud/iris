package cn.iris.cloud.configcenter.convert;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * ConverterService
 *
 **/
public class ConverterService {

    // CopyOnWrite
    private Map<CacheKey, Converter<String, ?>> converters = new HashMap<>();

    private StringToObjectConverter defaultConverter = new StringToObjectConverter();

    private static class ConverterServiceHolder {
        public static ConverterService holder = new ConverterService();
    }

    public static ConverterService getInstance() {
        return ConverterServiceHolder.holder;
    }

    private ConverterService() {
        addConverter(new StringToBooleanConverter());
        addConverter(new StringToCurrencyConverter());
        addConverter(new StringToCharacterConverter());
        addConverter(new StringToCharsetConverter());
        addConverter(new StringToUUIDConverter());
    }

    private <T> void addConverter(Converter<String, T> converter) {
        Map<CacheKey, Converter<String, ?>> converters = new HashMap<>(this.converters);
        final Type[] ifaces = converter.getClass().getGenericInterfaces();
        final String cotype = Converter.class.getName() + "<";
        for (Type iface : ifaces) {
            if (iface.getTypeName().startsWith(cotype)) {
                Type[] params = ((ParameterizedType) iface).getActualTypeArguments();
                converters.put(new CacheKey(params[0], params[1]), converter);
            }
        }
        this.converters = converters;
    }

    @SuppressWarnings("unchecked")
    public <T> T convert(Class<T> tClass, String source) {
        Converter<String, ?> converter = getConverter(tClass);
        return converter == null ? null : (T) converter.convert(source);
    }

    @SuppressWarnings("unchecked")
    public <T> T convert(Class<T> tClass, Type type, String source) {
        Converter<String, ?> converter = getConverter(tClass);
        return converter == null
            ? (T) defaultConverter.convert(source, type)
            : (T) converter.convert(source);
    }

    private Converter<String, ?> getConverter(Class tClass) {
        CacheKey key = new CacheKey(String.class, tClass);
        return this.converters.get(key);
    }

    private static final class CacheKey implements Comparable<CacheKey> {

        private final String key;

        public CacheKey(Type sourceType, Type targetType) {
            this.key = sourceType.getTypeName() + "-" + targetType.getTypeName();
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof CacheKey)) {
                return false;
            }

            CacheKey otherKey = (CacheKey) other;
            return this.key.equals(otherKey.key);
        }

        @Override
        public int hashCode() {
            return this.key.hashCode();
        }

        @Override
        public String toString() {
            return "ConverterCacheKey [" + this.key + "]";
        }

        @Override
        public int compareTo(CacheKey other) {
            return other == null ? 1 : this.key.compareTo(other.key);
        }
    }
}
