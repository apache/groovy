/*
 * Copyright 2003-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.groovy.metaclass;

import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;
import groovy.lang.MetaClassImpl;
import groovy.lang.MetaClassRegistry;
import org.apache.groovy.lang.annotation.Incubating;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A Realm is the representation of a metaclass layer in a tree of realm objects.
 */
@Incubating
public final class Realm {
    public final static Realm ROOT = new Realm("Root", null);

    private final String name;
    private final Realm parent;
    private final ClassValue<MetaClass> cv = new ClassValue() {
        @Override
        protected MetaClass computeValue(Class type) {
            return new MetaClassImpl(type);
        }
    };

    private Realm(String name, Realm parent) {
        this.name = name;
        this.parent = parent;
    }

    public Realm createRealm(String name) {
        Objects.requireNonNull(name, "missing realm name");
        return new Realm(name, this);
    }

    @Override
    public String toString() {
        return "Realm{" +
                "name='" + name + '\'' +
                ", parent=" + parent +
                '}';
    }

    public MetaClass getMetaClass(Class<?> theClass) {
        return cv.get(theClass);
    }
}
