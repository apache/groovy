package org.apache.groovy.ginq.provider.collection.runtime;

import java.util.stream.Stream;

/**
 * Represents group
 *
 * @param <T> the type of element
 * @since 4.0.0
 */
public interface Group<T> extends Queryable<T> {
    /**
     * Factory method to create {@link Group} instance
     *
     * @param sourceStream the source stream
     * @param <T> the type of element
     * @return the {@link Group} instance
     * @since 4.0.0
     */
    static <T> Group<T> of(Stream<T> sourceStream) {
        return new GroupImpl<>(sourceStream);
    }
}
