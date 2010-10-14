/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.runtime.callsite;

import groovy.lang.GroovyObject;


public interface CallSite {
    CallSiteArray getArray();
    int getIndex();
    String getName();

    Object getProperty(Object receiver) throws Throwable;
    Object callGetPropertySafe (Object receiver) throws Throwable;
    Object callGetProperty (Object receiver) throws Throwable;
    Object callGroovyObjectGetProperty (Object receiver) throws Throwable;
    Object callGroovyObjectGetPropertySafe (Object receiver) throws Throwable;

    Object call (Object receiver, Object[] args) throws Throwable;
    Object call (Object receiver) throws Throwable;
    Object call (Object receiver, Object arg1) throws Throwable;
    Object call (Object receiver, Object arg1, Object arg2) throws Throwable;
    Object call (Object receiver, Object arg1, Object arg2, Object arg3) throws Throwable;
    Object call (Object receiver, Object arg1, Object arg2, Object arg3, Object arg4) throws Throwable;

    Object callSafe (Object receiver, Object[] args) throws Throwable;
    Object callSafe (Object receiver) throws Throwable;
    Object callSafe (Object receiver, Object arg1) throws Throwable;
    Object callSafe (Object receiver, Object arg1, Object arg2) throws Throwable;
    Object callSafe (Object receiver, Object arg1, Object arg2, Object arg3) throws Throwable;
    Object callSafe (Object receiver, Object arg1, Object arg2, Object arg3, Object arg4) throws Throwable;

    Object callCurrent (GroovyObject receiver, Object [] args) throws Throwable;
    Object callCurrent (GroovyObject receiver) throws Throwable;
    Object callCurrent (GroovyObject receiver, Object arg1) throws Throwable;
    Object callCurrent (GroovyObject receiver, Object arg1, Object arg2) throws Throwable;
    Object callCurrent (GroovyObject receiver, Object arg1, Object arg2, Object arg3) throws Throwable;
    Object callCurrent (GroovyObject receiver, Object arg1, Object arg2, Object arg3, Object arg4) throws Throwable;

    Object callStatic (Class receiver, Object [] args) throws Throwable;
    Object callStatic (Class receiver) throws Throwable;
    Object callStatic (Class receiver, Object arg1) throws Throwable;
    Object callStatic (Class receiver, Object arg1, Object arg2) throws Throwable;
    Object callStatic (Class receiver, Object arg1, Object arg2, Object arg3) throws Throwable;
    Object callStatic (Class receiver, Object arg1, Object arg2, Object arg3, Object arg4) throws Throwable;

    Object callConstructor (Object receiver, Object [] args) throws Throwable;
    Object callConstructor (Object receiver) throws Throwable;
    Object callConstructor (Object receiver, Object arg1) throws Throwable;
    Object callConstructor (Object receiver, Object arg1, Object arg2) throws Throwable;
    Object callConstructor (Object receiver, Object arg1, Object arg2, Object arg3) throws Throwable;
    Object callConstructor (Object receiver, Object arg1, Object arg2, Object arg3, Object arg4) throws Throwable;
}
