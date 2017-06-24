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
package org.codehaus.groovy.vmplugin;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CompileUnit;

import java.lang.reflect.Method;

/**
 * Factory class to get functionality based on the VM version.
 * The usage of this class is not for public use, only for the
 * runtime.
 * @author Jochen Theodorou
 * @deprecated use {@link org.apache.groovy.internal.vmplugin.VMPluginFactory}
 */
@Deprecated
public class VMPluginFactory {

    private static final VMPlugin plugin =
            new LegacyPluginAdapter(org.apache.groovy.internal.vmplugin.VMPluginFactory.getPlugin());

    public static VMPlugin getPlugin() {
        return plugin;
    }

    private static class LegacyPluginAdapter implements VMPlugin {

        private final org.apache.groovy.internal.vmplugin.VMPlugin adaptee;

        LegacyPluginAdapter(org.apache.groovy.internal.vmplugin.VMPlugin adaptee) {
            this.adaptee = adaptee;
        }

        @Override
        public void setAdditionalClassInformation(ClassNode c) {
            adaptee.setAdditionalClassInformation(c);
        }

        @Override
        public Class[] getPluginDefaultGroovyMethods() {
            return adaptee.getPluginDefaultGroovyMethods();
        }

        @Override
        public Class[] getPluginStaticGroovyMethods() {
            return adaptee.getPluginStaticGroovyMethods();
        }

        @Override
        public void configureAnnotationNodeFromDefinition(AnnotationNode definition, AnnotationNode root) {
            adaptee.configureAnnotationNodeFromDefinition(definition, root);
        }

        @Override
        public void configureAnnotation(AnnotationNode an) {
            adaptee.configureAnnotation(an);
        }

        @Override
        public void configureClassNode(CompileUnit compileUnit, ClassNode classNode) {
            adaptee.configureClassNode(compileUnit, classNode);
        }

        @Override
        public void invalidateCallSites() {
            adaptee.invalidateCallSites();
        }

        @Override
        public Object getInvokeSpecialHandle(Method m, Object receiver) {
            return adaptee.getInvokeSpecialHandle(m, receiver);
        }

        @Override
        public Object invokeHandle(Object handle, Object[] args) throws Throwable {
            return adaptee.invokeHandle(handle, args);
        }

        @Override
        public int getVersion() {
            return adaptee.getVersion();
        }
    }
}
