/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.util;

import groovy.lang.Closure;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * List decorator that will trigger PropertyChangeEvents when a value changes.<br>
 * An optional Closure may be specified and will work as a filter, if it returns true the property
 * will trigger an event (if the value indeed changed), otherwise it won't. The Closure may receive
 * 1 or 2 parameters, the single one being the value, the other one both the key and value, for
 * example:
 * <pre>
 * // skip all properties whose value is a closure
 * def map = new ObservableList( {!(it instanceof Closure)} )
 *
 * // skip all properties whose name matches a regex
 * def map = new ObservableList( { name, value -&gt; !(name =&tilde; /[A-Z+]/) } )
 * </pre>
 * The current implementation will trigger specialized events in the following scenarios, you need
 * not register a different listener as those events extend from PropertyChangeEvent
 * <ul>
 * <li>ObservableList.ElementAddedEvent - a new element is added to the list</li>
 * <li>ObservableList.ElementRemovedEvent - an element is removed from the list</li>
 * <li>ObservableList.ElementUpdatedEvent - an element changes value (same as regular
 * PropertyChangeEvent)</li>
 * <li>ObservableList.ElementClearedEvent - all elements have been removed from the list</li>
 * <li>ObservableList.MultiElementAddedEvent - triggered by calling list.addAll()</li>
 * <li>ObservableList.MultiElementRemovedEvent - triggered by calling
 * list.removeAll()/list.retainAll()</li>
 * </ul>
 * <p>
 * <strong>Bound properties</strong>
 * <ul>
 * <li><tt>content</tt> - read-only.</li>
 * <li><tt>size</tt> - read-only.</li>
 * </ul>
 */
public class ObservableList implements List {
    private final List delegate;
    private final PropertyChangeSupport pcs;
    private final Closure test;

    /** Bound property name for list size changes. */
    public static final String SIZE_PROPERTY = "size";
    /** Bound property name for list content changes. */
    public static final String CONTENT_PROPERTY = "content";

    /** Creates an observable list backed by an {@link ArrayList}. */
    public ObservableList() {
        this(new ArrayList(), null);
    }

    /**
     * Creates an observable list backed by the supplied delegate.
     *
     * @param delegate the backing list
     */
    public ObservableList(List delegate) {
        this(delegate, null);
    }

    /**
     * Creates an observable list backed by an {@link ArrayList}.
     *
     * @param test optional event filter
     */
    public ObservableList(Closure test) {
        this(new ArrayList(), test);
    }

    /**
     * Creates an observable list backed by the supplied delegate.
     *
     * @param delegate the backing list
     * @param test optional event filter
     */
    public ObservableList(List delegate, Closure test) {
        this.delegate = delegate;
        this.test = test;
        pcs = new PropertyChangeSupport(this);
    }

    /**
     * Returns an unmodifiable snapshot view of the backing list.
     *
     * @return the list content
     */
    public List getContent() {
        return Collections.unmodifiableList(delegate);
    }

    /**
     * Returns the mutable backing list.
     *
     * @return the delegate list
     */
    protected List getDelegateList() {
        return delegate;
    }

    /**
     * Returns the optional event filter closure.
     *
     * @return the event filter, or {@code null}
     */
    protected Closure getTest() {
        return test;
    }

    /** Fires a single-element added event. */
    protected void fireElementAddedEvent(int index, Object element) {
        fireElementEvent(new ElementAddedEvent(this, element, index));
    }

    /** Fires a multi-element added event. */
    protected void fireMultiElementAddedEvent(int index, List values) {
        fireElementEvent(new MultiElementAddedEvent(this, index, values));
    }

    /** Fires a cleared event containing removed values. */
    protected void fireElementClearedEvent(List values) {
        fireElementEvent(new ElementClearedEvent(this, values));
    }

    /** Fires a single-element removed event. */
    protected void fireElementRemovedEvent(int index, Object element) {
        fireElementEvent(new ElementRemovedEvent(this, element, index));
    }

    /** Fires a multi-element removed event. */
    protected void fireMultiElementRemovedEvent(List values) {
        fireElementEvent(new MultiElementRemovedEvent(this, values));
    }

    /** Fires a single-element updated event. */
    protected void fireElementUpdatedEvent(int index, Object oldValue, Object newValue) {
        fireElementEvent(new ElementUpdatedEvent(this, oldValue, newValue, index));
    }

    /** Publishes an element event to registered listeners. */
    protected void fireElementEvent(ElementEvent event) {
        pcs.firePropertyChange(event);
    }

    /** Fires the bound size change event. */
    protected void fireSizeChangedEvent(int oldValue, int newValue) {
        pcs.firePropertyChange(new PropertyChangeEvent(this, SIZE_PROPERTY, oldValue, newValue));
    }

    /** {@inheritDoc} */
    @Override
    public void add(int index, Object element) {
        int oldSize = size();
        delegate.add(index, element);
        fireAddWithTest(element, index, oldSize);
    }

    /** {@inheritDoc} */
    @Override
    public boolean add(Object o) {
        int oldSize = size();
        boolean success = delegate.add(o);
        if (success) {
            fireAddWithTest(o, oldSize, oldSize);
        }
        return success;
    }

    private void fireAddWithTest(Object element, int index, int oldSize) {
        if (test != null) {
            Object result = test.call(element);
            if (result instanceof Boolean && (Boolean) result) {
                fireElementAddedEvent(index, element);
                fireSizeChangedEvent(oldSize, size());
            }
        } else {
            fireElementAddedEvent(index, element);
            fireSizeChangedEvent(oldSize, size());
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean addAll(Collection c) {
        return addAll(size(), c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean addAll(int index, Collection c) {
        int oldSize = size();
        boolean success = delegate.addAll(index, c);

        if (success && c != null) {
            List values = new ArrayList();
            for (Object element : c) {
                if (test != null) {
                    Object result = test.call(element);
                    if (result instanceof Boolean && (Boolean) result) {
                        values.add(element);
                    }
                } else {
                    values.add(element);
                }
            }
            if (!values.isEmpty()) {
                fireMultiElementAddedEvent(index, values);
                fireSizeChangedEvent(oldSize, size());
            }
        }

        return success;
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        int oldSize = size();
        List values = new ArrayList(delegate);
        delegate.clear();
        if (!values.isEmpty()) {
            fireElementClearedEvent(values);
        }
        fireSizeChangedEvent(oldSize, size());
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsAll(Collection c) {
        return delegate.containsAll(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    /** {@inheritDoc} */
    @Override
    public Object get(int index) {
        return delegate.get(index);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public int indexOf(Object o) {
        return delegate.indexOf(o);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public Iterator iterator() {
        return new ObservableIterator(delegate.iterator());
    }

    /** {@inheritDoc} */
    @Override
    public int lastIndexOf(Object o) {
        return delegate.lastIndexOf(o);
    }

    /** {@inheritDoc} */
    @Override
    public ListIterator listIterator() {
        return new ObservableListIterator(delegate.listIterator(), 0);
    }

    /** {@inheritDoc} */
    @Override
    public ListIterator listIterator(int index) {
        return new ObservableListIterator(delegate.listIterator(index), index);
    }

    /** {@inheritDoc} */
    @Override
    public Object remove(int index) {
        int oldSize = size();
        Object element = delegate.remove(index);
        fireElementRemovedEvent(index, element);
        fireSizeChangedEvent(oldSize, size());
        return element;
    }

    /** {@inheritDoc} */
    @Override
    public boolean remove(Object o) {
        int oldSize = size();
        int index = delegate.indexOf(o);
        boolean success = delegate.remove(o);
        if (success) {
            fireElementRemovedEvent(index, o);
            fireSizeChangedEvent(oldSize, size());
        }
        return success;
    }

    /** {@inheritDoc} */
    @Override
    public boolean removeAll(Collection c) {
        if (c == null) {
            return false;
        }

        List values = new ArrayList();
        // GROOVY-7783 use Sets for O(1) performance for contains
        Set delegateSet = new HashSet<Object>(delegate);
        if (!(c instanceof Set)) {
            c = new HashSet<Object>(c);
        }
        for (Object element : c) {
            if (delegateSet.contains(element)) {
                values.add(element);
            }
        }

        int oldSize = size();
        boolean success = delegate.removeAll(c);
        if (success && !values.isEmpty()) {
            fireMultiElementRemovedEvent(values);
            fireSizeChangedEvent(oldSize, size());
        }

        return success;
    }

    /** {@inheritDoc} */
    @Override
    public boolean retainAll(Collection c) {
        if (c == null) {
            return false;
        }

        List values = new ArrayList();
        // GROOVY-7783 use Set for O(1) performance for contains
        if (!(c instanceof Set)) {
            c = new HashSet<Object>(c);
        }
        for (Object element : delegate) {
            if (!c.contains(element)) {
                values.add(element);
            }
        }

        int oldSize = size();
        boolean success = delegate.retainAll(c);
        if (success && !values.isEmpty()) {
            fireMultiElementRemovedEvent(values);
            fireSizeChangedEvent(oldSize, size());
        }

        return success;
    }

    /** {@inheritDoc} */
    @Override
    public Object set(int index, Object element) {
        Object oldValue = delegate.set(index, element);
        if (test != null) {
            Object result = test.call(element);
            if (result instanceof Boolean && (Boolean) result) {
                fireElementUpdatedEvent(index, oldValue, element);
            }
        } else {
            fireElementUpdatedEvent(index, oldValue, element);
        }
        return oldValue;
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        return delegate.size();
    }

    /**
     * Returns the current list size as a bound property value.
     *
     * @return the current list size
     */
    public int getSize() {
        return size();
    }

    /** {@inheritDoc} */
    @Override
    public List subList(int fromIndex, int toIndex) {
        return delegate.subList(fromIndex, toIndex);
    }

    /** {@inheritDoc} */
    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    /** {@inheritDoc} */
    @Override
    public Object[] toArray(Object[] a) {
        return delegate.toArray(a);
    }

    /**
     * Iterator wrapper that reports removals as observable list events.
     */
    protected class ObservableIterator implements Iterator {
        private final Iterator iterDelegate;
        /** Current list index for iterator-driven updates. */
        protected int cursor = -1 ;

        /**
         * Creates an observable iterator around the supplied delegate.
         *
         * @param iterDelegate the backing iterator
         */
        public ObservableIterator(Iterator iterDelegate) {
            this.iterDelegate = iterDelegate;
        }

        /**
         * Returns the wrapped iterator.
         *
         * @return the backing iterator
         */
        public Iterator getDelegate() {
            return iterDelegate;
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            return iterDelegate.hasNext();
        }

        /** {@inheritDoc} */
        @Override
        public Object next() {
            cursor++;
            return iterDelegate.next();
        }

        /** {@inheritDoc} */
        @Override
        public void remove() {
            int oldSize = ObservableList.this.size();
            Object element = ObservableList.this.get(cursor);
            iterDelegate.remove();
            fireElementRemovedEvent(cursor, element);
            fireSizeChangedEvent(oldSize, size());
            cursor--;
        }
    }

    /**
     * List iterator wrapper that routes mutations through {@link ObservableList}.
     */
    protected class ObservableListIterator extends ObservableIterator implements ListIterator {
        /**
         * Creates an observable list iterator.
         *
         * @param iterDelegate the backing list iterator
         * @param index the starting index
         */
        public ObservableListIterator(ListIterator iterDelegate, int index) {
            super(iterDelegate);
            cursor = index - 1;
        }

        /**
         * Returns the wrapped list iterator.
         *
         * @return the backing list iterator
         */
        public ListIterator getListIterator() {
            return (ListIterator) getDelegate();
        }

        /** {@inheritDoc} */
        @Override
        public void add(Object o) {
            ObservableList.this.add(o);
            cursor++;
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasPrevious() {
            return getListIterator().hasPrevious();
        }

        /** {@inheritDoc} */
        @Override
        public int nextIndex() {
            return getListIterator().nextIndex();
        }

        /** {@inheritDoc} */
        @Override
        public Object previous() {
            return getListIterator().previous();
        }

        /** {@inheritDoc} */
        @Override
        public int previousIndex() {
            return getListIterator().previousIndex();
        }

        /** {@inheritDoc} */
        @Override
        public void set(Object o) {
            ObservableList.this.set(cursor, o);
        }
    }

    // observable interface

    /**
     * Registers a listener for all observable list events.
     *
     * @param listener the listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    /**
     * Registers a listener for a named bound property.
     *
     * @param propertyName the property to observe
     * @param listener the listener to add
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Returns listeners registered for all properties.
     *
     * @return the registered listeners
     */
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners();
    }

    /**
     * Returns listeners registered for a named property.
     *
     * @param propertyName the observed property name
     * @return the registered listeners
     */
    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return pcs.getPropertyChangeListeners(propertyName);
    }

    /**
     * Removes a listener registered for all properties.
     *
     * @param listener the listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    /**
     * Removes a listener registered for a named property.
     *
     * @param propertyName the observed property name
     * @param listener the listener to remove
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    /**
     * Reports whether listeners are registered for a named property.
     *
     * @param propertyName the property name to inspect
     * @return {@code true} if listeners are registered
     */
    public boolean hasListeners(String propertyName) {
        return pcs.hasListeners(propertyName);
    }

    /** Enumerates the specialized list change events. */
    public enum ChangeType {
        /** A single element was added. */
        ADDED,
        /** A single element was updated. */
        UPDATED,
        /** A single element was removed. */
        REMOVED,
        /** The list was cleared. */
        CLEARED,
        /** Multiple elements were added. */
        MULTI_ADD,
        /** Multiple elements were removed. */
        MULTI_REMOVE,
        /** No specialized change type applies. */
        NONE;

        /** Placeholder old value for aggregated events. */
        public static final Object oldValue = new Object[0];
        /** Placeholder new value for aggregated events. */
        public static final Object newValue = new Object[0];

        /**
         * Resolves an enum constant from its ordinal.
         *
         * @param ordinal the serialized ordinal value
         * @return the matching change type
         */
        public static ChangeType resolve(int ordinal) {
            return switch (ordinal) {
                case 0 -> ADDED;
                case 2 -> REMOVED;
                case 3 -> CLEARED;
                case 4 -> MULTI_ADD;
                case 5 -> MULTI_REMOVE;
                case 6 -> NONE;
                default -> UPDATED;
            };
        }
    }

    /**
     * Base event type for observable list content changes.
     */
    public abstract static class ElementEvent extends PropertyChangeEvent {

        @Serial
        private static final long serialVersionUID = -218253929030274352L;
        private final ChangeType type;
        private final int index;

        /**
         * Creates an element event.
         *
         * @param source the event source
         * @param oldValue the previous value payload
         * @param newValue the new value payload
         * @param index the affected index
         * @param type the specialized change type
         */
        public ElementEvent(Object source, Object oldValue, Object newValue, int index, ChangeType type) {
            super(source, ObservableList.CONTENT_PROPERTY, oldValue, newValue);
            this.type = type;
            this.index = index;
        }

        /**
         * Returns the affected index.
         *
         * @return the list index associated with the event
         */
        public int getIndex() {
            return index;
        }

        /**
         * Returns the specialized event type ordinal.
         *
         * @return the change type ordinal
         */
        public int getType() {
            return type.ordinal();
        }

        /**
         * Returns the specialized event type.
         *
         * @return the change type
         */
        public ChangeType getChangeType() {
            return type;
        }

        /**
         * Returns the specialized event type name.
         *
         * @return the change type name
         */
        public String getTypeAsString() {
            return type.name().toUpperCase();
        }
    }

    /** Event fired when one element is added. */
    public static class ElementAddedEvent extends ElementEvent {
        @Serial
        private static final long serialVersionUID = -6594847306176480596L;

        /**
         * Creates an added-element event.
         *
         * @param source the event source
         * @param newValue the added element
         * @param index the insertion index
         */
        public ElementAddedEvent(Object source, Object newValue, int index) {
            super(source, null, newValue, index, ChangeType.ADDED);
        }
    }

    /** Event fired when one element is updated. */
    public static class ElementUpdatedEvent extends ElementEvent {
        @Serial
        private static final long serialVersionUID = 1116018076124047485L;

        /**
         * Creates an updated-element event.
         *
         * @param source the event source
         * @param oldValue the previous element value
         * @param newValue the new element value
         * @param index the affected index
         */
        public ElementUpdatedEvent(Object source, Object oldValue, Object newValue, int index) {
            super(source, oldValue, newValue, index, ChangeType.UPDATED);
        }
    }

    /** Event fired when one element is removed. */
    public static class ElementRemovedEvent extends ElementEvent {
        @Serial
        private static final long serialVersionUID = 9017470261231004168L;

        /**
         * Creates a removed-element event.
         *
         * @param source the event source
         * @param value the removed element
         * @param index the removal index
         */
        public ElementRemovedEvent(Object source, Object value, int index) {
            super(source, value, null, index, ChangeType.REMOVED);
        }
    }

    /** Event fired when the list is cleared. */
    public static class ElementClearedEvent extends ElementEvent {
        @Serial
        private static final long serialVersionUID = -2754983590419383972L;
        private List values = new ArrayList();

        /**
         * Creates a cleared-list event.
         *
         * @param source the event source
         * @param values the removed elements
         */
        public ElementClearedEvent(Object source, List values) {
            super(source, ChangeType.oldValue, ChangeType.newValue, 0, ChangeType.CLEARED);
            if (values != null) {
                this.values.addAll(values);
            }
        }

        /**
         * Returns the removed elements.
         *
         * @return an unmodifiable view of removed elements
         */
        public List getValues() {
            return Collections.unmodifiableList(values);
        }
    }

    /** Event fired when multiple elements are added. */
    public static class MultiElementAddedEvent extends ElementEvent {
        @Serial
        private static final long serialVersionUID = 443060557109693114L;
        private List values = new ArrayList();

        /**
         * Creates a multi-add event.
         *
         * @param source the event source
         * @param index the first insertion index
         * @param values the added elements
         */
        public MultiElementAddedEvent(Object source, int index, List values) {
            super(source, ChangeType.oldValue, ChangeType.newValue, index, ChangeType.MULTI_ADD);
            if (values != null) {
                this.values.addAll(values);
            }
        }

        /**
         * Returns the added elements.
         *
         * @return an unmodifiable view of added elements
         */
        public List getValues() {
            return Collections.unmodifiableList(values);
        }
    }

    /** Event fired when multiple elements are removed. */
    public static class MultiElementRemovedEvent extends ElementEvent {
        @Serial
        private static final long serialVersionUID = 2590238951081945868L;
        private List values = new ArrayList();

        /**
         * Creates a multi-remove event.
         *
         * @param source the event source
         * @param values the removed elements
         */
        public MultiElementRemovedEvent(Object source, List values) {
            super(source, ChangeType.oldValue, ChangeType.newValue, 0, ChangeType.MULTI_REMOVE);
            if (values != null) {
                this.values.addAll(values);
            }
        }

        /**
         * Returns the removed elements.
         *
         * @return an unmodifiable view of removed elements
         */
        public List getValues() {
            return Collections.unmodifiableList(values);
        }
    }
}
