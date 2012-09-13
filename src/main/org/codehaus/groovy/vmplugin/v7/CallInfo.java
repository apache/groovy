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
package org.codehaus.groovy.vmplugin.v7;

import groovy.lang.MetaMethod;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;

/**
 * This class is used to store internal information during MethodHandle creation.
 * This class is for internal use only and must not be used from outside 
 * groovy-core and its indy classes.
 * @author <a href="mailto:blackdrag@gmx.org">Jochen "blackdrag" Theodorou</a>
 */
public class CallInfo {
    public Object[] args;
    public MetaMethod method;
    public MethodType targetType,currentType;
    public String name;
    public MethodHandle handle;
    public boolean useMetaClass = false;
    public MutableCallSite callSite;
    public Class sender;
    public boolean isVargs;
    public boolean safeNavigation, safeNavigationOrig;
    public boolean thisCall;
    public Class selector;
    public boolean catchException = true;
    public int callID;
    public boolean beanConstructor;
}