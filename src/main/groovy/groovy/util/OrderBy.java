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
import org.codehaus.groovy.runtime.NumberAwareComparator;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A helper class for sorting objects via a closure to return the field
 * or operation on which to sort.
 */
public class OrderBy<T> implements Comparator<T>, Serializable {

    private static final long serialVersionUID = 8385130064804116654L;
    private final List<Closure> closures;
    private boolean equalityCheck;
    private final NumberAwareComparator<Object> numberAwareComparator = new NumberAwareComparator<Object>();

    public OrderBy() {
        this(new ArrayList<Closure>(), false);
    }

    public OrderBy(boolean equalityCheck) {
        this(new ArrayList<Closure>(), equalityCheck);
    }

    public OrderBy(Closure closure) {
        this(closure, false);
    }

    public OrderBy(Closure closure, boolean equalityCheck) {
        this(new ArrayList<Closure>(), equalityCheck);
        closures.add(closure);
    }

    public OrderBy(List<Closure> closures) {
        this(closures, false);
    }

    public OrderBy(List<Closure> closures, boolean equalityCheck) {
        this.equalityCheck = equalityCheck;
        this.closures = closures;
    }

    public void add(Closure closure) {
        closures.add(closure);
    }

    public int compare(T object1, T object2) {
        for (Closure closure : closures) {
            Object value1 = closure.call(object1);
            Object value2 = closure.call(object2);
            int result;
            if (!equalityCheck || (value1 instanceof Comparable && value2 instanceof Comparable)) {
                result = numberAwareComparator.compare(value1, value2);
            } else {
                result = DefaultTypeTransformation.compareEqual(value1, value2) ? 0 : -1;
            }
            if (result == 0) continue;
            return result;
        }
        return 0;
    }

    public boolean isEqualityCheck() {
        return equalityCheck;
    }

    public void setEqualityCheck(boolean equalityCheck) {
        this.equalityCheck = equalityCheck;
    }
}
