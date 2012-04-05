package battlecode.serial;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ExtensibleMetadata implements Serializable {

    private static final long serialVersionUID = -8982623693074338715L;

    private final Map<String, Object> data;

    public ExtensibleMetadata() {
        this(null);
    }

    public ExtensibleMetadata(Map<String, Object> data) {
        this.data = new LinkedHashMap<String, Object>();
        if (data != null)
            this.data.putAll(data);
    }

    /**
     * @param key          the key whose associated value is to be returned
     * @param defaultValue the value to return if there's no value in the map for
     *                     {@code key}
     * @return the value associated with key if it exists, else
     *         {@code defaultValue}
     */
    public Object get(String key, Object defaultValue) {
        Object result = data.get(key);
        if (result == null)
            return defaultValue;
        else
            return result;
    }

    public Object put(String key, Object value) {
        return data.put(key, value);
    }

    public Set<String> keySet() {
        return data.keySet();
    }
}
