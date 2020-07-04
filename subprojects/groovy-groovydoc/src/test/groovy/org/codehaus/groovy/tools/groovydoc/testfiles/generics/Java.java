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
package org.codehaus.groovy.tools.groovydoc.testfiles.generics;

/**
 * Generic class.
 *
 * @param <N> Doc.
 */
public abstract class Java<N extends Number & Comparable<? extends Number>> {
    /**
     * Generic method.
     *
     * @param <A> Doc.
     * @param <B> Doc.
     * @param a Doc.
     * @param b Doc.
     * @return Doc.
     */
    public static <A, B> int compare(Class<A> a, Class<B> b) {
        return 0;
    }
}
