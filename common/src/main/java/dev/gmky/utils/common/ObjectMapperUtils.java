package dev.gmky.utils.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Utility wrapper around a configured Jackson {@link com.fasterxml.jackson.databind.ObjectMapper}
 * for JSON serialization and deserialization.
 *
 * <p>Provides thread-safe static helpers to convert objects to JSON strings and parse JSON into
 * objects. The underlying {@code ObjectMapper} ignores unknown properties, registers the
 * Java Time module and disables writing dates as timestamps.
 *
 * <p>On serialization errors methods log the failure and return an empty string; on
 * deserialization errors they log the failure and return {@code null}.
 *
 * @see com.fasterxml.jackson.databind.ObjectMapper
 */
public class ObjectMapperUtils {
    private static final String BLANK_STR = "";
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectMapperUtils.class);
    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Private constructor to prevent instantiation of this utility class.
     *
     * <p>Always throws {@link IllegalStateException} when called.
     */
    public ObjectMapperUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Convert the given object to a JSON string.
     *
     * @param object the object to serialize; may be {@code null}
     * @return the JSON string representation of {@code object}, or an empty string if
     *         {@code object} is {@code null} or serialization fails
     */
    public static String toJson(Object object) {
        try {
            if (Objects.isNull(object)) {
                LOGGER.warn("Provided object is null");
                return BLANK_STR;
            }
            return MAPPER.writeValueAsString(object);
        } catch (Exception e) {
            LOGGER.error("Failed to serialize object", e);
            return BLANK_STR;
        }
    }

    /**
     * Convert the given JSON string to an instance of the specified class.
     *
     * @param json the JSON string to deserialize; may be {@code null} or blank
     * @param clazz the target type to deserialize into
     * @param <T> the target type
     * @return an instance of {@code clazz} populated from {@code json}, or {@code null} if
     *         {@code json} is {@code null}/blank or deserialization fails
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            if (Objects.isNull(json) || json.isBlank()) {
                LOGGER.warn("Provided json is null");
                return null;
            }
            return MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            LOGGER.error("Failed to deserialize object", e);
            return null;
        }
    }

    /**
     * Convert the given JSON string to an instance of the specified generic type.
     *
     * <p>Uses Jackson's {@link com.fasterxml.jackson.core.type.TypeReference} to support
     * deserialization of parameterized types (for example {@code List<YourType>} or {@code Map<String,YourType>}).
     *
     * @param json the JSON string to deserialize; may be {@code null} or blank
     * @param typeReference the {@code TypeReference} describing the target generic type
     * @param <T> the target type
     * @return an instance of the target type populated from {@code json}, or {@code null} if
     *         {@code json} is {@code null}/blank or deserialization fails; failures are logged
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            if (Objects.isNull(json) || json.isBlank()) {
                LOGGER.warn("Provided json is null");
                return null;
            }
            return MAPPER.readValue(json, typeReference);
        } catch (Exception e) {
            LOGGER.error("Failed to deserialize object", e);
            return null;
        }
    }
}
