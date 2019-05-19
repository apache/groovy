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
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.io.Serializable;
import java.util.Comparator;

/**
 * A Comparator which uses a closure to compare 2 values being equal
 */
public class ClosureComparator<T> implements Comparator<T>, Serializable {

    private static final long serialVersionUID = -4593521535656429522L;
    Closure closure;

    public ClosureComparator(Closure closure) {
        this.closure = closure;
    }

    public int compare(T object1, T object2) {
        Object value = closure.call(object1, object2);
        return DefaultTypeTransformation.intUnbox(value);
    }
}
