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

import java.lang.management.ManagementFactory;

/**
 * Defines new Groovy methods which appear on normal JDK 10
 * classes inside the Groovy environment.
 *
 * @since 4.0.0
 */
public class PluginDefaultGroovyMethods {
    /**
     * Get the pid of the current Java process
     *
     * @param self
     * @return the pid
     * @since 4.0.0
     */
    public static String getPid(Runtime self) {
        return String.valueOf(ManagementFactory.getRuntimeMXBean().getPid());
    }
}
