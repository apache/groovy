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
package org.codehaus.groovy.vmplugin.v5;

import org.apache.groovy.internal.vmplugin.VMPluginFactory;
import org.apache.groovy.internal.vmplugin.VMPluginBase;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.tools.GenericsUtils;

import java.lang.reflect.Method;

/**
 * Java 5 based functions
 *
 * @author Jochen Theodorou
 * @deprecated retained for compatibility
 */
@Deprecated
public class Java5 extends VMPluginBase {

    @Deprecated
    public static GenericsType configureTypeVariableDefinition(ClassNode base, ClassNode[] cBounds) {
        return GenericsUtils.configureTypeVariableDefinition(base, cBounds);
    }

    @Deprecated
    public static ClassNode configureTypeVariableReference(String name) {
        return GenericsUtils.configureTypeVariableReference(name);
    }

    @Deprecated
    public static void configureAnnotationFromDefinition(AnnotationNode definition, AnnotationNode root) {
        VMPluginFactory.getPlugin().configureAnnotationNodeFromDefinition(definition, root);
    }

    @Override
    public void invalidateCallSites() {}

    @Override
    public Object getInvokeSpecialHandle(Method m, Object receiver){
        throw new GroovyBugError("getInvokeSpecialHandle requires at least JDK 7 wot private access to Lookup");
    }

    @Override
    public Object invokeHandle(Object handle, Object[] args) throws Throwable {
        throw new GroovyBugError("invokeHandle requires at least JDK 7");
    }

    @Override
    public int getVersion() {
        return 5;
    }

}
