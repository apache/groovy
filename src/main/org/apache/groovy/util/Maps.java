package org.apache.groovy.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Map utilities.
 */
public abstract class Maps {
    public static Map of(Object... args) {
        int length = args.length;

        if (0 != length % 2) {
            throw new IllegalArgumentException("the length of arguments should be a power of 2");
        }

        Map map = new LinkedHashMap();

        for (int i = 0, n = length / 2; i < n; i++) {
            int index = i * 2;

            map.put(args[index], args[index + 1]);
        }

        return Collections.unmodifiableMap(map);
    }
}
