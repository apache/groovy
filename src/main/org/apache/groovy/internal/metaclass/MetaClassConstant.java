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

package org.apache.groovy.internal.metaclass;

import groovy.lang.MetaClassImpl;
import groovy.lang.MetaMethod;
import org.apache.groovy.lang.annotation.Incubating;

import java.lang.invoke.SwitchPoint;

/**
 * The one and only implementation of a meta class.
 * INTERNAL USE ONLY.
 */
@Incubating
public final class MetaClassConstant<T> {
    private final SwitchPoint switchPoint = new SwitchPoint();
    //TODO Joche: replace with real implementation
    private final MetaClassImpl impl;

    public MetaClassConstant(Class<T> clazz) {
        impl = new MetaClassImpl(clazz);
    }

    public SwitchPoint getSwitchPoint() {
        return switchPoint;
    }

    // TODO Jochen: replace with new MetaMethod
    public MetaMethod getMethod(String name, Class[] parameters) {
        return impl.pickMethod(name, parameters);
    }
}
