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
package groovy.json;

import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class used as delegate of closures representing JSON objects.
 *
 * @since 1.8.0
 */
public class JsonDelegate extends GroovyObjectSupport {

    private final Map<String, Object> content = new LinkedHashMap<String, Object>();

    /**
     * Intercepts calls for setting a key and value for a JSON object
     *
     * @param name the key name
     * @param args the value associated with the key
     */
    @Override
    public Object invokeMethod(String name, Object args) {
        Object val = null;
        if (args != null && Object[].class.isAssignableFrom(args.getClass())) {
            Object[] arr = (Object[]) args;

            if (arr.length == 1) {
                val = arr[0];
            } else if (isIterableOrArrayAndClosure(arr)) {
                Closure<?> closure = (Closure<?>) arr[1];
                Iterator<?> iterator = (arr[0] instanceof Iterable) ?
                        ((Iterable) arr[0]).iterator() : Arrays.asList((Object[])arr[0]).iterator();
                List<Object> list = new ArrayList<Object>();
                while (iterator.hasNext()) {
                    list.add(curryDelegateAndGetContent(closure, iterator.next()));
                }
                val = list;
            } else {
                val = Arrays.asList(arr);
            }
        }
        content.put(name, val);

        return val;
    }

    private static boolean isIterableOrArrayAndClosure(Object[] args) {
        if (args.length != 2 || !(args[1] instanceof Closure)) {
            return false;
        }
        return ((args[0] instanceof Iterable) || (args[0] != null && args[0].getClass().isArray()));
    }

    /**
     * Factory method for creating <code>JsonDelegate</code>s from closures.
     *
     * @param c closure representing JSON objects
     * @return an instance of <code>JsonDelegate</code>
     */
    public static Map<String, Object> cloneDelegateAndGetContent(Closure<?> c) {
        JsonDelegate delegate = new JsonDelegate();
        Closure<?> cloned = (Closure<?>) c.clone();
        cloned.setDelegate(delegate);
        cloned.setResolveStrategy(Closure.DELEGATE_FIRST);
        cloned.call();

        return delegate.getContent();
    }

    /**
     * Factory method for creating <code>JsonDelegate</code>s from closures currying an object
     * argument.
     *
     * @param c closure representing JSON objects
     * @param o an object curried to the closure
     * @return an instance of <code>JsonDelegate</code>
     */
    public static Map<String, Object> curryDelegateAndGetContent(Closure<?> c, Object o) {
        JsonDelegate delegate = new JsonDelegate();
        Closure<?> curried = c.curry(o);
        curried.setDelegate(delegate);
        curried.setResolveStrategy(Closure.DELEGATE_FIRST);
        curried.call();

        return delegate.getContent();
    }

    public Map<String, Object> getContent() {
        return content;
    }

}
