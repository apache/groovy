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
import org.codehaus.groovy.util.SingleKeyHashMap;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;

public class MetaMethodIndex {
    public SingleKeyHashMap methodHeaders = new SingleKeyHashMap();

    public static class Header {
        public Entry head;
        Class cls;
        public int clsHashCode31;
        public Class subclass;

        public Header(Class cls) {
            this (cls, null);
        }

        public Header(Class cls, Class subclass) {
            this.cls = cls;
            this.subclass = subclass;
            this.clsHashCode31 = 31 * cls.hashCode();
        }
    }

    public static class CacheEntry {
        public final Class [] params;
        public final MetaMethod method;

        public CacheEntry(final Class[] params, final MetaMethod method) {
            this.params = params;
            this.method = method;
        }
    }

    public static class Entry {
        public int hash;

        public Entry nextHashEntry, nextClassEntry;

        public String name;
        public Class cls;

        public Object methods, methodsForSuper, staticMethods;

        public CacheEntry cachedMethod, cachedMethodForSuper, cachedStaticMethod;

        public String toString () {
            return "[" + name + ", " + cls.getName() + "]";
        }
    }

    public MetaMethodIndex(CachedClass theCachedClass) {
        init(DEFAULT_CAPACITY);

        CachedClass last = null;
        if (!theCachedClass.isInterface()) {
            for (CachedClass c = theCachedClass; c != null; c = c.getCachedSuperClass()) {
              final SingleKeyHashMap.Entry e = methodHeaders.getOrPut(c.getTheClass());
              e.value = new Header (c.getTheClass(), last == null ? null : last.getTheClass());
              last = c;
            }
        }
        else {
            final SingleKeyHashMap.Entry e = methodHeaders.getOrPut(Object.class);
            e.value = new Header (Object.class, theCachedClass.getTheClass());
        }
    }

    protected Entry table[];

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

    public void init(int initCapacity) {
        threshold = (initCapacity * 6) / 8;
        table = new Entry[initCapacity];
    }

    public void resize(int newLength) {
        Entry[] oldTable = table;
        int oldLength = table.length;

        Entry[] newTable = new Entry[newLength];

        for (int j = 0; j < oldLength; j++) {

            for (Entry e = oldTable[j]; e != null;) {
                Entry next = e.nextHashEntry;
                int index = e.hash & (newLength - 1);

                e.nextHashEntry = newTable[index];
                newTable[index] = e;

                e = next;
            }
        }

        table = newTable;
        threshold = (6 * newLength) / 8;
    }

    public interface EntryIterator {
        boolean hasNext();

        Entry next();
    }


    public Entry[] getTable() {
        return table;
    }

    public EntryIterator getEntrySetIterator() {
        return new EntryIterator() {
            Entry next;    // next entry to return
            int index;        // current slot

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

            public boolean hasNext() {
                return next != null;
            }

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

    public final Entry getMethods(Class cls, String name) {
        int h = hash(31 * cls.hashCode() + name.hashCode());
        Entry e = table[h & (table.length - 1)];
        for (; e != null; e = e.nextHashEntry)
            if (e.hash == h && cls == e.cls && Objects.equals(e.name, name) )
                return e;

        return null;
    }

    public Entry getOrPutMethods(String name, Header header) {
        final Class cls = header.cls;
        int h = hash(header.clsHashCode31 + name.hashCode());
        final Entry[] t = table;
        final int index = h & (t.length - 1);
        Entry e = t[index];
        for (; e != null; e = e.nextHashEntry)
            if (e.hash == h && cls == e.cls && Objects.equals(e.name, name) )
                return e;

        Entry entry = new Entry();
        entry.nextHashEntry = t[index];
        entry.hash = h;
        entry.name = name.intern();
        entry.cls = cls;
        t[index] = entry;

        entry.nextClassEntry = header.head;
        header.head = entry;

        if (++size == threshold)
            resize(2 * t.length);

        return entry;
    }

    public Header getHeader(Class cls) {
        Header header;
        final SingleKeyHashMap.Entry head = methodHeaders.getOrPut(cls);
        if (head.value == null) {
            head.value = new Header(cls);
        }
        header = (Header) head.value;
        return header;
    }

    public void copyNonPrivateMethods(Class from, Class to) {
        copyNonPrivateMethods(getHeader(from), getHeader(to));
    }

    public void copyNonPrivateMethods(Header from, Header to) {
        for (Entry e = from.head; e != null; e = e.nextClassEntry)
            copyNonPrivateMethods(e, to);
    }

    public void copyAllMethodsToSuper(Header from, Header to) {
        for (Entry e = from.head; e != null; e = e.nextClassEntry)
            copyAllMethodsToSuper(e, to);
    }

    public void copyNonPrivateMethodsFromSuper(Header from) {
        for (Entry e = from.head; e != null; e = e.nextClassEntry)
            copyNonPrivateMethodsFromSuper(e);
    }

    private void copyNonPrivateMethods(Entry from, Header to) {
        Object oldListOrMethod = from.methods;
        if (oldListOrMethod instanceof FastArray) {
            FastArray oldList = (FastArray) oldListOrMethod;
            Entry e = null;
            int len1 = oldList.size();
            Object list[] = oldList.getArray();
            for (int j = 0; j != len1; ++j) {
                MetaMethod method = (MetaMethod) list[j];
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

    private void copyAllMethodsToSuper(Entry from, Header to) {
        Object oldListOrMethod = from.methods;
        if (oldListOrMethod instanceof FastArray) {
            FastArray oldList = (FastArray) oldListOrMethod;
            Entry e = null;
            int len1 = oldList.size();
            Object list[] = oldList.getArray();
            for (int j = 0; j != len1; ++j) {
                MetaMethod method = (MetaMethod) list[j];
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

    private void copyNonPrivateMethodsFromSuper(Entry e) {
        Object oldListOrMethod = e.methodsForSuper;
        if (oldListOrMethod == null)
          return;

        if (oldListOrMethod instanceof FastArray) {
            FastArray oldList = (FastArray) oldListOrMethod;
            int len1 = oldList.size();
            Object list[] = oldList.getArray();
            for (int j = 0; j != len1; ++j) {
                MetaMethod method = (MetaMethod) list[j];
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

    public void copyNonPrivateMethodsDown(Class from, Class to) {
        copyNonPrivateNonNewMetaMethods(getHeader(from), getHeader(to));
    }

    public void copyNonPrivateNonNewMetaMethods(Header from, Header to) {
        for (Entry e = from.head; e != null; e = e.nextClassEntry)
            copyNonPrivateNonNewMetaMethods(e, to);
    }

    private void copyNonPrivateNonNewMetaMethods(Entry from, Header to) {
        Object oldListOrMethod = from.methods;
        if (oldListOrMethod == null)
          return;

        if (oldListOrMethod instanceof FastArray) {
            FastArray oldList = (FastArray) oldListOrMethod;
            Entry e = null;
            int len1 = oldList.size();
            Object list[] = oldList.getArray();
            for (int j = 0; j != len1; ++j) {
                MetaMethod method = (MetaMethod) list[j];
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

    public Object addMethodToList(Object o, MetaMethod method) {
        if (o == null) {
            return method;
        }

        if (o instanceof MetaMethod) {
            MetaMethod match = (MetaMethod) o;
            if (!isMatchingMethod(match, method)) {
                FastArray list = new FastArray(2);
                list.add(match);
                list.add(method);
                return list;
            } else {
                if (match.isPrivate()
                        || (!isNonRealMethod(match)
                            && match.getDeclaringClass().isInterface()
                            && !method.getDeclaringClass().isInterface()
                            && !method.isStatic())) {
                    // do not overwrite interface methods with instance methods
                    // do not overwrite private methods
                    // Note: private methods from parent classes are not shown here,
                    // but when doing the multimethod connection step, we overwrite
                    // methods of the parent class with methods of a subclass and
                    // in that case we want to keep the private methods
                } else {
                    CachedClass methodC = method.getDeclaringClass();
                    CachedClass matchC = match.getDeclaringClass();
                    if (methodC == matchC) {
                        if (isNonRealMethod(method)) {
                            return method;
                        }
                    } else if (!methodC.isAssignableFrom(matchC.getTheClass())) {
                        return method;
                    }
                }
            }
            return o;
        }

        if (o instanceof FastArray) {
            FastArray list = (FastArray) o;
            int found = findMatchingMethod(list, method);

            if (found == -1) {
                list.add(method);
            } else {
                MetaMethod match = (MetaMethod) list.get(found);
                if (match==method) return o;
                if (match.isPrivate()
                        || (!isNonRealMethod(match)
                            && match.getDeclaringClass().isInterface()
                            && !method.getDeclaringClass().isInterface()
                            && !method.isStatic())) {
                    // do not overwrite interface methods with instance methods
                    // do not overwrite private methods
                    // Note: private methods from parent classes are not shown here,
                    // but when doing the multimethod connection step, we overwrite
                    // methods of the parent class with methods of a subclass and
                    // in that case we want to keep the private methods
                } else {
                    CachedClass  methodC = method.getDeclaringClass();
                    CachedClass matchC = match.getDeclaringClass();
                    if (methodC == matchC) {
                        if (isNonRealMethod(method)) {
                            list.set(found, method);
                        }
                    } else if (!methodC.isAssignableFrom(matchC.getTheClass())) {
                        list.set(found, method);
                    }
                }
            }
        }

        return o;
    }

    private static boolean isNonRealMethod(MetaMethod method) {
        return method instanceof NewInstanceMetaMethod ||
                method instanceof NewStaticMetaMethod ||
                method instanceof ClosureMetaMethod ||
                method instanceof GeneratedMetaMethod ||
                method instanceof ClosureStaticMetaMethod ||
                method instanceof MixinInstanceMetaMethod ||
                method instanceof ClosureMetaMethod.AnonymousMetaMethod;
    }

    private static boolean isMatchingMethod(MetaMethod aMethod, MetaMethod method) {
        if (aMethod==method) return true;
        CachedClass[] params1 = aMethod.getParameterTypes();
        CachedClass[] params2 = method.getParameterTypes();
        if (params1.length != params2.length) {
            return false;
        }

        boolean matches = true;
        for (int i = 0; i < params1.length; i++) {
            if (params1[i] != params2[i]) {
                matches = false;
                break;
            }
        }
        return matches;
    }

    private static int findMatchingMethod(FastArray list, MetaMethod method) {
        int len = list.size();
        Object data[] = list.getArray();
        for (int j = 0; j != len; ++j) {
            MetaMethod aMethod = (MetaMethod) data[j];
            if (isMatchingMethod(aMethod, method))
                return j;
        }
        return -1;
    }

    public void copyMethodsToSuper() {
        Entry[] table = this.table;
        int length = table.length;

        for (Entry entry : table) {
            for (Entry e = entry; e != null; e = e.nextHashEntry) {
                if (e.methods instanceof FastArray)
                    e.methodsForSuper = ((FastArray) e.methods).copy();
                else
                    e.methodsForSuper = e.methods;
            }
        }

    }

    public void copy(Class c, Header index) {
        copy(getHeader(c), index);
    }

    public void copy(Header from, Header to) {
        for (Entry e = from.head; e != null; e = e.nextClassEntry)
            copyAllMethods(e, to);
    }

    private void copyAllMethods(Entry from, Header to) {
        Object oldListOrMethod = from.methods;
        if (oldListOrMethod instanceof FastArray) {
            FastArray oldList = (FastArray) oldListOrMethod;
            Entry e = null;
            int len1 = oldList.size();
            Object list[] = oldList.getArray();
            for (int j = 0; j != len1; ++j) {
                MetaMethod method = (MetaMethod) list[j];
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
        for (int i = 0; i != table.length; ++i )
          for (Entry e = table [i]; e != null; e = e.nextHashEntry ) {
              e.cachedMethod = e.cachedMethodForSuper = e.cachedStaticMethod = null;
          }
    }

    public void clearCaches(String name) {
        for (int i = 0; i != table.length; ++i )
          for (Entry e = table [i]; e != null; e = e.nextHashEntry ) {
              if (e.name.equals(name)) {
                  e.cachedMethod = e.cachedMethodForSuper = e.cachedStaticMethod = null;
              }
          }
    }
}
