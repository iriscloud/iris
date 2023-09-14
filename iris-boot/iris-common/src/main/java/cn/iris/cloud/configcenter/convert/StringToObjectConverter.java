package cn.iris.cloud.configcenter.convert;


import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;

/**
 * Convert a String to Object.
 *
 * field name of Object do not begin with "is"
 *
 */
public class StringToObjectConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(StringToObjectConverter.class);

    private final ObjectMapper objectMapper;

    public StringToObjectConverter() {
        objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public Object convert(String source, Type type) {
        try {
            JavaType javaType = objectMapper.constructType(type);
            return objectMapper.readValue(source, javaType);
        } catch (Exception e) {
            LOGGER.warn("String to Object fail ", e);
        }
        return null;
    }


}
