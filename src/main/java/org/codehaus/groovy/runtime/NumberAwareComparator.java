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
package org.codehaus.groovy.runtime;

import groovy.lang.GroovyRuntimeException;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Compares two objects using Groovy's friendly comparison algorithm, i.e.
 * handles nulls gracefully (nul being less than everything else) and
 * performs numeric type coercion if required.
 */
public class NumberAwareComparator<T> implements Comparator<T>, Serializable {
    private static final long serialVersionUID = 9017657289076651660L;

    public int compare(T o1, T o2) {
        try {
            return DefaultTypeTransformation.compareTo(o1, o2);
        } catch (ClassCastException | IllegalArgumentException | GroovyRuntimeException cce) {
            /* ignore */
        }
        // since the object does not have a valid compareTo method
        // we compare using the hashcodes. null cases are handled by
        // DefaultTypeTransformation.compareTo
        // This is not exactly a mathematical valid approach, since we compare object
        // that cannot be compared. To avoid strange side effects we do a pseudo order
        // using hashcodes, but without equality. Since then an x and y with the same
        // hashcodes will behave different depending on if we compare x with y or
        // x with y, the result might be unstable as well. Setting x and y to equal
        // may mean the removal of x or y in a sorting operation, which we don't want.
        int x1 = o1.hashCode();
        int x2 = o2.hashCode();
        if (x1 == x2 && o1.equals(o2)) return 0;
        if (x1 > x2) return 1;
        return -1;
    }
}
