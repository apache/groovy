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
package groovy.transform;

import groovy.lang.Closure;
import org.codehaus.groovy.ast.ClassNode;

/**
 * Java doesn't allow you to have null as an attribute value. It wants you to indicate what you really
 * mean by null, so that is what we do here - as ugly as it is.
 */
public final class Undefined {
    private Undefined() {}
    public static final String STRING = "<DummyUndefinedMarkerString-DoNotUse>";
    public static final class CLASS {}
    public static final class CLOSURE_CLASS extends Closure {
        public CLOSURE_CLASS(Object owner) { super(owner); }
    }
    public static final class EXCEPTION extends RuntimeException {
        private static final long serialVersionUID = -3960500360386581172L;
    }
    public static boolean isUndefined(String other) { return STRING.equals(other); }
    public static boolean isUndefined(ClassNode other) { return CLASS.class.getName().equals(other.getName()); }
    public static boolean isUndefinedException(ClassNode other) { return EXCEPTION.class.getName().equals(other.getName()); }
}
