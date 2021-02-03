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
package org.codehaus.groovy.vmplugin.v10;

import org.codehaus.groovy.vmplugin.v9.Java9;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Additional Java 10 based functions will be added here as needed.
 */
public class Java10 extends Java9 {
    private final Class<?>[] PLUGIN_DGM;

    public Java10() {
        super();
        List<Class<?>> dgmClasses = new ArrayList<>();
        Collections.addAll(dgmClasses, super.getPluginDefaultGroovyMethods());
        dgmClasses.add(PluginDefaultGroovyMethods.class);
        PLUGIN_DGM = dgmClasses.toArray(new Class<?>[0]);
    }

    @Override
    public Class<?>[] getPluginDefaultGroovyMethods() {
        return PLUGIN_DGM;
    }
}
