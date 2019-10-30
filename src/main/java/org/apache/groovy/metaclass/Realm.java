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

package org.apache.groovy.metaclass;

import org.apache.groovy.internal.metaclass.MetaClassConstant;
import org.apache.groovy.internal.util.Function;
import org.apache.groovy.internal.util.ReevaluatingReference;
import org.apache.groovy.internal.util.Supplier;
import org.apache.groovy.lang.annotation.Incubating;

import java.lang.invoke.SwitchPoint;
import java.util.Objects;

/**
 * A Realm is the representation of a metaclass layer in a tree of realm objects.
 */
@Incubating
public final class Realm {
    private static final Realm ROOT = new Realm("ROOT", null);

    private final String name;
    private final Realm parent;
    private final ClassValue<MetaClassConstant<?>> cv = new ClassValue<MetaClassConstant<?>>() {
        @Override
        @SuppressWarnings("unchecked")
        protected MetaClassConstant<?> computeValue(Class<?> type) {
            return new MetaClassConstant(type);
        }
    };

    private Realm(String name, Realm parent) {
        this.name = name;
        this.parent = parent;
    }

    public static Realm newRealm(String name, Realm parent) {
        Objects.requireNonNull(name, "missing realm name");
        if (parent == null) {
            return new Realm(name, ROOT);
        } else {
            return new Realm(name, parent);
        }
    }

    @Override
    public String toString() {
        return "Realm{" +
                "name='" + name + '\'' +
                ", parent=" + parent +
                '}';
    }

    public <T> MetaClass<T> getMetaClass(final Class<T> theClass) {
        Supplier<MetaClassConstant<T>> valueSupplier = () -> (MetaClassConstant<T>) cv.get(theClass);
        Function<MetaClassConstant<T>, SwitchPoint> validationSupplier = MetaClassConstant::getSwitchPoint;
        ReevaluatingReference<MetaClassConstant<T>> ref = new ReevaluatingReference<>(
                MetaClassConstant.class,
                valueSupplier,
                validationSupplier
        );
        return new MetaClass<>(ref);
    }
}
