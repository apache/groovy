package org.apache.groovy.ginq.provider.collection.runtime;

import java.util.stream.Stream;

/**
 * Represents group implementation
 *
 * @param <T> the type of element
 * @since 4.0.0
 */
class GroupImpl<T> extends QueryableCollection<T> implements Group<T> {
    GroupImpl(Iterable<T> sourceIterable) {
        super(sourceIterable);
    }

    GroupImpl(Stream<T> sourceStream) {
        super(sourceStream);
    }
}
