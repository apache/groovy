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
package org.codehaus.groovy.transform.stc;

import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.DefaultGroovyStaticMethods;
import org.codehaus.groovy.runtime.m12n.ExtensionModule;
import org.codehaus.groovy.vmplugin.VMPluginFactory;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl.EXTENSION_DISABLE_PROPERTY;

/**
 * This class is used to make extension methods lookup faster. Basically, it will only
 * collect the list of extension methods (see {@link ExtensionModule} if the list of
 * extension modules has changed. It avoids recomputing the whole list each time we perform
 * a method lookup.
 */
public class ExtensionMethodCache extends AbstractExtensionMethodCache {
    public static final ExtensionMethodCache INSTANCE = new ExtensionMethodCache();

    private ExtensionMethodCache() {}

    @Override
    protected void addAdditionalClassesToScan(Set<Class> instanceExtClasses, Set<Class> staticExtClasses) {
        Collections.addAll(instanceExtClasses, DefaultGroovyMethods.DGM_LIKE_CLASSES);
        Collections.addAll(instanceExtClasses, DefaultGroovyMethods.ADDITIONAL_CLASSES);
        staticExtClasses.add(DefaultGroovyStaticMethods.class);

        instanceExtClasses.add(StaticTypeCheckingSupport.ObjectArrayStaticTypesHelper.class);
        instanceExtClasses.add(StaticTypeCheckingSupport.BooleanArrayStaticTypesHelper.class);
        instanceExtClasses.add(StaticTypeCheckingSupport.CharArrayStaticTypesHelper.class);
        instanceExtClasses.add(StaticTypeCheckingSupport.ByteArrayStaticTypesHelper.class);
        instanceExtClasses.add(StaticTypeCheckingSupport.ShortArrayStaticTypesHelper.class);
        instanceExtClasses.add(StaticTypeCheckingSupport.IntArrayStaticTypesHelper.class);
        instanceExtClasses.add(StaticTypeCheckingSupport.LongArrayStaticTypesHelper.class);
        instanceExtClasses.add(StaticTypeCheckingSupport.FloatArrayStaticTypesHelper.class);
        instanceExtClasses.add(StaticTypeCheckingSupport.DoubleArrayStaticTypesHelper.class);

        Collections.addAll(instanceExtClasses, VMPluginFactory.getPlugin().getPluginDefaultGroovyMethods());
        Collections.addAll(staticExtClasses, VMPluginFactory.getPlugin().getPluginStaticGroovyMethods());
    }

    @Override
    protected String getDisablePropertyName() {
        return EXTENSION_DISABLE_PROPERTY;
    }

    @Override
    protected Predicate<MethodNode> getMethodFilter() {
        return m -> !m.getAnnotations(StaticTypeCheckingSupport.Deprecated_TYPE).isEmpty();
    }

    @Override
    protected Function<MethodNode, String> getMethodMapper() {
        return m -> m.getParameters()[0].getType().getName();
    }
}
