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

import java.util.Properties;

/**
 * This is the base class for classes responsible for creating modules
 * using properties.
 *
 * @since 2.0.0
 */
public abstract class PropertiesModuleFactory {
    public static final String MODULE_NAME_KEY = "moduleName";
    public static final String MODULE_VERSION_KEY = "moduleVersion";

    public abstract ExtensionModule newModule(Properties properties, final ClassLoader classLoader);
}
