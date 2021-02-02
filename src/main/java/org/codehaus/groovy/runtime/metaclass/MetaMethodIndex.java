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
package org.codehaus.groovy.runtime.metaclass;

import groovy.lang.MetaMethod;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.GeneratedMetaMethod;
import org.codehaus.groovy.util.FastArray;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

public class MetaMethodIndex {
    public final Map<Class, Header> methodHeaders = new LinkedHashMap<>(32);

    public static class Header {
        public Entry head;
               Class cls;
        public Class subclass;
        public int clsHashCode31;

        public Header(final Class cls) {
            this(cls, null);
        }

        public Header(final Class cls, final Class subclass) {
            this.cls = cls;
            this.subclass = subclass;
            this.clsHashCode31 = 31 * cls.hashCode();
        }
    }

    public static class CacheEntry {
        public final Class[] params;
        public final MetaMethod method;

        public CacheEntry(final Class[] params, final MetaMethod method) {
            this.params = params;
            this.method = method;
        }
    }

    public static class Entry {
        public int hash;
        public Class cls;
        public String name;
        public Entry nextHashEntry, nextClassEntry;
        public Object methods, methodsForSuper, staticMethods;
        public CacheEntry cachedMethod, cachedMethodForSuper, cachedStaticMethod;

        @Override
        public String toString() {
            return "[" + name + ", " + cls.getName() + "]";
        }
    }

    public interface EntryIterator {
        boolean hasNext();
        Entry next();
    }

    //--------------------------------------------------------------------------

    public MetaMethodIndex(final CachedClass theCachedClass) {
        init(DEFAULT_CAPACITY);

        CachedClass last = null;
        if (!theCachedClass.isInterface()) {
            for (CachedClass c = theCachedClass; c != null; c = c.getCachedSuperClass()) {
                methodHeaders.put(c.getTheClass(), new Header(c.getTheClass(), last == null ? null : last.getTheClass()));
                last = c;
            }
        } else {
            methodHeaders.put(Object.class, new Header(Object.class, theCachedClass.getTheClass()));
        }
    }

    protected Entry[] table;

    protected static final int DEFAULT_CAPACITY = 32;
    protected static final int MINIMUM_CAPACITY = 4;
    protected static final int MAXIMUM_CAPACITY = 1 << 28;

    protected int size;
    protected transient int threshold;

    public static int hash(int h) {
        h += ~(h << 9);
        h ^= (h >>> 14);
        h += (h << 4);
        h ^= (h >>> 10);
        return h;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        Object[] tab = table;
        Arrays.fill(tab, null);
        size = 0;
    }

    public void init(final int initCapacity) {
        threshold = (initCapacity * 6) / 8;
        table = new Entry[initCapacity];
    }

    public void resize(final int newLength) {
        Entry[] oldTable = table;
        Entry[] newTable = new Entry[newLength];

        for (Entry entry : oldTable) {
            for (Entry e = entry, next; e != null; e = next) {
                next = e.nextHashEntry;

                int index = e.hash & (newLength - 1);
                e.nextHashEntry = newTable[index];
                newTable[index] = e;
            }
        }

        table = newTable;
        threshold = (6 * newLength) / 8;
    }

    public Entry[] getTable() {
        return table;
    }

    public EntryIterator getEntrySetIterator() {
        return new EntryIterator() {
            Entry next; // next entry to return
            int index;  // current slot

            {
                Entry[] t = table;
                int i = t.length;
                Entry n = null;
                if (size != 0) { // advance to first entry
                    while (i > 0 && (n = t[--i]) == null) {
                    }
                }
                next = n;
                index = i;
            }

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public Entry next() {
                return nextEntry();
            }

            Entry nextEntry() {
                Entry e = next;
                if (e == null)
                    throw new NoSuchElementException();

                Entry n = e.nextHashEntry;
                Entry[] t = table;
                int i = index;
                while (n == null && i > 0)
                    n = t[--i];
                index = i;
                next = n;
                return e;
            }
        };
    }

    public final Entry getMethods(final Class cls, final String name) {
        int h = hash(31 * cls.hashCode() + name.hashCode());
        for (Entry e = table[h & (table.length - 1)]; e != null; e = e.nextHashEntry) {
            if (e.hash == h && cls == e.cls && Objects.equals(e.name, name)) {
                return e;
            }
        }
        return null;
    }

    public Entry getOrPutMethods(final String name, final Header header) {
        final Class cls = header.cls;
        int h = hash(header.clsHashCode31 + name.hashCode());
        final Entry[] t = table;
        final int index = h & (t.length - 1);
        for (Entry e = t[index]; e != null; e = e.nextHashEntry) {
            if (e.hash == h && cls == e.cls && Objects.equals(e.name, name)) {
                return e;
            }
        }
        Entry entry = new Entry();
        entry.nextHashEntry = t[index];
        entry.hash = h;
        entry.name = name;
        entry.cls = cls;
        t[index] = entry;

        entry.nextClassEntry = header.head;
        header.head = entry;

        if (++size == threshold)
            resize(2 * t.length);

        return entry;
    }

    public Header getHeader(final Class cls) {
        return methodHeaders.computeIfAbsent(cls, k -> new Header(cls));
    }

    public void copyNonPrivateMethods(final Class from, final Class to) {
        copyNonPrivateMethods(getHeader(from), getHeader(to));
    }

    public void copyNonPrivateMethods(final Header from, final Header to) {
        for (Entry e = from.head; e != null; e = e.nextClassEntry) {
            copyNonPrivateMethods(e, to);
        }
    }

    public void copyAllMethodsToSuper(final Header from, final Header to) {
        for (Entry e = from.head; e != null; e = e.nextClassEntry) {
            copyAllMethodsToSuper(e, to);
        }
    }

    public void copyNonPrivateMethodsFromSuper(final Header from) {
        for (Entry e = from.head; e != null; e = e.nextClassEntry) {
            copyNonPrivateMethodsFromSuper(e);
        }
    }

    private void copyNonPrivateMethods(final Entry from, final Header to) {
        Object oldListOrMethod = from.methods;
        if (oldListOrMethod instanceof FastArray) {
            FastArray oldList = (FastArray) oldListOrMethod;
            Entry e = null;
            final int n = oldList.size();
            Object[] array = oldList.getArray();
            for (int i = 0; i != n; i += 1) {
                MetaMethod method = (MetaMethod) array[i];
                if (method.isPrivate()) continue;
                if (e == null)
                    e = getOrPutMethods(from.name, to);
                e.methods = addMethodToList(e.methods, method);
            }
        } else {
            MetaMethod method = (MetaMethod) oldListOrMethod;
            if (!method.isPrivate()) {
                Entry e = getOrPutMethods(from.name, to);
                e.methods = addMethodToList(e.methods, method);
            }
        }
    }

    private void copyAllMethodsToSuper(final Entry from, final Header to) {
        Object oldListOrMethod = from.methods;
        if (oldListOrMethod instanceof FastArray) {
            FastArray oldList = (FastArray) oldListOrMethod;
            Entry e = null;
            final int n = oldList.size();
            Object[] array = oldList.getArray();
            for (int i = 0; i != n; i += 1) {
                MetaMethod method = (MetaMethod) array[i];
                if (e == null)
                    e = getOrPutMethods(from.name, to);
                e.methodsForSuper = addMethodToList(e.methodsForSuper, method);
            }
        } else {
            MetaMethod method = (MetaMethod) oldListOrMethod;
            Entry e = getOrPutMethods(from.name, to);
            e.methodsForSuper = addMethodToList(e.methodsForSuper, method);
        }
    }

    private void copyNonPrivateMethodsFromSuper(final Entry e) {
        Object oldListOrMethod = e.methodsForSuper;
        if (oldListOrMethod == null) {
            return;
        }

        if (oldListOrMethod instanceof FastArray) {
            FastArray oldList = (FastArray) oldListOrMethod;
            final int n = oldList.size();
            Object[] array = oldList.getArray();
            for (int i = 0; i != n; i += 1) {
                MetaMethod method = (MetaMethod) array[i];
                if (method.isPrivate()) continue;
                e.methods = addMethodToList(e.methods, method);
            }
        } else {
            MetaMethod method = (MetaMethod) oldListOrMethod;
            if (!method.isPrivate()) {
                e.methods = addMethodToList(e.methods, method);
            }
        }
    }

    public void copyNonPrivateMethodsDown(final Class from, final Class to) {
        copyNonPrivateNonNewMetaMethods(getHeader(from), getHeader(to));
    }

    public void copyNonPrivateNonNewMetaMethods(final Header from, final Header to) {
        for (Entry e = from.head; e != null; e = e.nextClassEntry) {
            copyNonPrivateNonNewMetaMethods(e, to);
        }
    }

    private void copyNonPrivateNonNewMetaMethods(final Entry from, final Header to) {
        Object oldListOrMethod = from.methods;
        if (oldListOrMethod == null) {
            return;
        }

        if (oldListOrMethod instanceof FastArray) {
            FastArray oldList = (FastArray) oldListOrMethod;
            Entry e = null;
            final int n = oldList.size();
            Object[] array = oldList.getArray();
            for (int i = 0; i != n; i += 1) {
                MetaMethod method = (MetaMethod) array[i];
                if (method instanceof NewMetaMethod || method.isPrivate()) continue;
                if (e == null)
                    e = getOrPutMethods(from.name, to);
                e.methods = addMethodToList(e.methods, method);
            }
        } else {
            MetaMethod method = (MetaMethod) oldListOrMethod;
            if (method instanceof NewMetaMethod || method.isPrivate()) return;
            Entry e = getOrPutMethods(from.name, to);
            e.methods = addMethodToList(e.methods, method);
        }
    }

    public Object addMethodToList(final Object o, final MetaMethod toIndex) {
        if (o == null) {
            return toIndex;
        }

        if (o instanceof MetaMethod) {
            final MetaMethod inIndex = (MetaMethod) o;
            if (!isMatchingMethod(inIndex, toIndex)) {
                return new FastArray(new Object[]{inIndex, toIndex});
            }

            if (inIndex != toIndex && isOverridden(inIndex, toIndex)) {
                return toIndex;
            }
            return inIndex;
        }

        if (o instanceof FastArray) {
            final FastArray index = (FastArray) o;
            int found = findMatchingMethod(index, toIndex);
            if (found == -1) {
                index.add(toIndex);
            } else {
                final MetaMethod inIndex = (MetaMethod) index.get(found);
                if (inIndex != toIndex && isOverridden(inIndex, toIndex)) {
                    index.set(found, toIndex);
                }
            }
        }

        return o;
    }

    /**
     * TODO
     * <p>
     * Note: private methods from parent classes are not handled here, but when
     * doing the multi-method connection step, methods of the parent class will
     * be overwritten with methods of a subclass and in that case private methods
     * should be kept.
     */
    private static boolean isOverridden(final MetaMethod inIndex, final MetaMethod toIndex) {
        // do not overwrite private methods
        if (inIndex.isPrivate()) return false;

        CachedClass inIndexDC = inIndex.getDeclaringClass();
        CachedClass toIndexDC = toIndex.getDeclaringClass();
        if (inIndexDC == toIndexDC) {
            return isNonRealMethod(toIndex);
        }

        // interface vs instance method; be careful...
        if (!inIndex.isStatic() && !toIndex.isStatic()
                && inIndexDC.isInterface() != toIndexDC.isInterface()) {
            // this is the old logic created for GROOVY-2391 and GROOVY-7879, which was labeled as "do not overwrite interface methods with instance methods"
            return (isNonRealMethod(inIndex) || !inIndexDC.isInterface() || toIndexDC.isInterface()) && !toIndexDC.isAssignableFrom(inIndexDC.getTheClass());
        }

        // prefer most-specific or most-recent for type disjunction
        return inIndexDC.isAssignableFrom(toIndexDC.getTheClass());
    }

    private static boolean isNonRealMethod(final MetaMethod method) {
        return method instanceof NewInstanceMetaMethod
            || method instanceof NewStaticMetaMethod
            || method instanceof ClosureMetaMethod
            || method instanceof GeneratedMetaMethod
            || method instanceof ClosureStaticMetaMethod
            || method instanceof MixinInstanceMetaMethod
            || method instanceof ClosureMetaMethod.AnonymousMetaMethod;
    }

    private static boolean isMatchingMethod(final MetaMethod method1, final MetaMethod method2) {
        if (method1 == method2) return true;
        CachedClass[] params1 = method1.getParameterTypes();
        CachedClass[] params2 = method2.getParameterTypes();
        if (params1.length != params2.length) return false;

        for (int i = 0, n = params1.length; i < n; i += 1) {
            if (params1[i] != params2[i]) {
                return false;
            }
        }
        return true;
    }

    private static int findMatchingMethod(final FastArray list, final MetaMethod method) {
        final int n = list.size();
        Object[] data = list.getArray();
        for (int i = 0; i != n; i += 1) {
            MetaMethod aMethod = (MetaMethod) data[i];
            if (isMatchingMethod(aMethod, method)) {
                return i;
            }
        }
        return -1;
    }

    public void copyMethodsToSuper() {
        for (Entry e : table) {
            for (; e != null; e = e.nextHashEntry) {
                if (e.methods instanceof FastArray) {
                    e.methodsForSuper = ((FastArray) e.methods).copy();
                } else {
                    e.methodsForSuper = e.methods;
                }
            }
        }
    }

    public void copy(final Class c, final Header index) {
        copy(getHeader(c), index);
    }

    public void copy(final Header from, final Header to) {
        for (Entry e = from.head; e != null; e = e.nextClassEntry) {
            copyAllMethods(e, to);
        }
    }

    private void copyAllMethods(final Entry from, final Header to) {
        Object oldListOrMethod = from.methods;
        if (oldListOrMethod instanceof FastArray) {
            FastArray oldList = (FastArray) oldListOrMethod;
            Entry e = null;
            final int n = oldList.size();
            Object[] array = oldList.getArray();
            for (int i = 0; i != n; i += 1) {
                MetaMethod method = (MetaMethod) array[i];
                if (e == null)
                    e = getOrPutMethods(from.name, to);
                e.methods = addMethodToList(e.methods, method);
            }
        } else {
            MetaMethod method = (MetaMethod) oldListOrMethod;
            if (!method.isPrivate()) {
                Entry e = getOrPutMethods(from.name, to);
                e.methods = addMethodToList(e.methods, method);
            }
        }
    }

    public void clearCaches() {
        for (Entry e : table) {
            for (; e != null; e = e.nextHashEntry) {
                e.cachedMethod = e.cachedMethodForSuper = e.cachedStaticMethod = null;
            }
        }
    }

    public void clearCaches(String name) {
        for (Entry e : table) {
            for (; e != null; e = e.nextHashEntry) {
                if (e.name.equals(name)) {
                    e.cachedMethod = e.cachedMethodForSuper = e.cachedStaticMethod = null;
                }
            }
        }
    }
}
