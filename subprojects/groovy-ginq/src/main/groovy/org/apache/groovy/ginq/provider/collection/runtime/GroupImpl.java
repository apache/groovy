package org.apache.groovy.ginq.provider.collection.runtime;

import java.util.stream.Stream;

/**
 * Represents group implementation
 *
 * @param <T> the type of element
 * @since 4.0.0
 */
class GroupImpl<T> extends QueryableCollection<T> implements Group<T> {
    private static final long serialVersionUID = 5737735821215711785L;

    GroupImpl(Stream<T> sourceStream) {
        super(sourceStream);
    }
}
