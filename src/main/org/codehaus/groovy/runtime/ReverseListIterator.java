package org.codehaus.groovy.runtime;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A reverse iterator over a list. Utilizes the {@link ListIterator}
 * obtained from the provided {@link List} and converts it to an
 * {@link Iterator} that efficiently traverses the <code>List</code> in
 * reverse. The fail-fast semantics of this iterator are the same as the
 * semantics of the underlying <code>ListIterator</code>.
 *
 * @author Mike Dillon
 */
public class ReverseListIterator implements Iterator {
    private ListIterator delegate;

    /**
     * Constructs a new <code>ReverseListIterator</code> for the provided list.
     * @param list the list to iterate over in reverse
     */
    public ReverseListIterator(List list) {
        this.delegate = list.listIterator(list.size());
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNext() {
        return delegate.hasPrevious();
    }

    /**
     * {@inheritDoc}
     */
    public Object next() {
        return delegate.previous();
    }

    /**
     * {@inheritDoc}
     */
    public void remove() {
        delegate.remove();
    }
}
