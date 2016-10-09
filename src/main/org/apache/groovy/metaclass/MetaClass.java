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

import groovy.lang.MetaMethod;
import org.apache.groovy.internal.metaclass.MetaClassConstant;
import org.apache.groovy.internal.util.ReevaluatingReference;
import org.apache.groovy.lang.annotation.Incubating;

/**
 * A MetaClass within Groovy defines the behaviour of any given Groovy or Java class
 */
@Incubating
public final class MetaClass<T> {
    private final ReevaluatingReference<MetaClassConstant<T>> implRef;

    MetaClass(ReevaluatingReference<MetaClassConstant<T>> implRef) {
        this.implRef = implRef;
    }

    public MetaMethod getMethod(String name, Class[] parameters) {
        return implRef.getPayload().getMethod(name, parameters);
    }
}
