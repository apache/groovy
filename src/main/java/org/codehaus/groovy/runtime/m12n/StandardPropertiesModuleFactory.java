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
package org.codehaus.groovy.runtime.m12n;

import groovy.lang.GroovyRuntimeException;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

/**
 * This is the standard Groovy module factory. This factory will build a module
 * using the {@link MetaInfExtensionModule} by default, unless a key named
 * "moduleFactory" is found in the properties file. If this is the case, then a new
 * factory is instantiated and used instead of this factory.
 */
public class StandardPropertiesModuleFactory extends PropertiesModuleFactory {
    public static final String MODULE_FACTORY_KEY = "moduleFactory";

    @Override
    @SuppressWarnings("unchecked")
    public ExtensionModule newModule(final Properties properties, final ClassLoader classLoader) {
        String factoryName = properties.getProperty(MODULE_FACTORY_KEY);
        if (factoryName!=null) {
            try {
                Class<? extends PropertiesModuleFactory> factoryClass = (Class<? extends PropertiesModuleFactory>) classLoader.loadClass(factoryName);
                PropertiesModuleFactory delegate = factoryClass.getDeclaredConstructor().newInstance();
                return delegate.newModule(properties, classLoader);
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                throw new GroovyRuntimeException("Unable to load module factory ["+factoryName+"]",e);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new GroovyRuntimeException("Unable to instantiate module factory ["+factoryName+"]",e);
            }
        }
        return MetaInfExtensionModule.newModule(properties, classLoader);
    }

}
