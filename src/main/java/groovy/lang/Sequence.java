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
package groovy.lang;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a sequence of zero or more objects of a given type.
 * The type can be omitted in which case any type of object can be added.
 */
public class Sequence extends ArrayList implements GroovyObject {

    private static final long serialVersionUID = 5697409354934589471L;
    private transient MetaClass metaClass = InvokerHelper.getMetaClass(getClass());
    private final Class type;
    private int hashCode;

    public Sequence() {
        this(null);
    }

    public Sequence(Class type) {
        this.type = type;
    }

    public Sequence(Class type, List content) {
        super(content.size());
        this.type = type;
        addAll(content);
    }

    /**
     * Sets the contents of this sequence to that
     * of the given collection.
     */
    public void set(Collection collection) {
        checkCollectionType(collection);
        clear();
        addAll(collection);
    }

    @Override
    public boolean equals(Object that) {
        if (that instanceof Sequence) {
            return equals((Sequence) that);
        }
        return false;
    }

    public boolean equals(Sequence that) {
        if (size() == that.size()) {
            for (int i = 0; i < size(); i++) {
                if (!DefaultTypeTransformation.compareEqual(this.get(i), that.get(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            for (int i = 0; i < size(); i++) {
                Object value = get(i);
                int hash = (value != null) ? value.hashCode() : 0xbabe;
                hashCode ^= hash;
            }
            if (hashCode == 0) {
                hashCode = 0xbabe;
            }
        }
        return hashCode;
    }

    public int minimumSize() {
        return 0;
    }

    /**
     * @return the type of the elements in the sequence or null if there is no
     * type constraint on this sequence
     */
    public Class type() {
        return type;
    }

    @Override
    public void add(int index, Object element) {
        checkType(element);
        hashCode = 0;
        super.add(index, element);
    }

    @Override
    public boolean add(Object element) {
        checkType(element);
        hashCode = 0;
        return super.add(element);
    }

    @Override
    public boolean addAll(Collection c) {
        checkCollectionType(c);
        hashCode = 0;
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection c) {
        checkCollectionType(c);
        hashCode = 0;
        return super.addAll(index, c);
    }

    @Override
    public void clear() {
        hashCode = 0;
        super.clear();
    }

    @Override
    public Object remove(int index) {
        hashCode = 0;
        return super.remove(index);
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        hashCode = 0;
        super.removeRange(fromIndex, toIndex);
    }

    @Override
    public Object set(int index, Object element) {
        hashCode = 0;
        return super.set(index, element);
    }

    // GroovyObject interface
    //-------------------------------------------------------------------------
    @Override
    public Object invokeMethod(String name, Object args) {
        try {
            return getMetaClass().invokeMethod(this, name, args);
        } catch (MissingMethodException e) {
            // let's apply the method to each item in the collection
            List answer = new ArrayList(size());
            for (Object element : this) {
                Object value = InvokerHelper.invokeMethod(element, name, args);
                answer.add(value);
            }
            return answer;
        }
    }

    @Override
    public Object getProperty(String property) {
        return getMetaClass().getProperty(this, property);
    }

    @Override
    public void setProperty(String property, Object newValue) {
        getMetaClass().setProperty(this, property, newValue);
    }

    @Override
    public MetaClass getMetaClass() {
        return metaClass;
    }

    @Override
    public void setMetaClass(MetaClass metaClass) {
        this.metaClass = metaClass;
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * Checks that each member of the given collection are of the correct
     * type
     */
    protected void checkCollectionType(Collection c) {
        if (type != null) {
            for (Object element : c) {
                checkType(element);
            }
        }
    }

    /**
     * Checks that the given object instance is of the correct type
     * otherwise a runtime exception is thrown
     */
    protected void checkType(Object object) {
        if (object == null) {
            throw new NullPointerException("Sequences cannot contain null, use a List instead");
        }
        if (type != null) {
            if (!type.isInstance(object)) {
                throw new IllegalArgumentException(
                        "Invalid type of argument for sequence of type: "
                                + type.getName()
                                + " cannot add object: "
                                + object);
            }
        }
    }
}
