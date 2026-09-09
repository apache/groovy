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
package org.codehaus.groovy.reflection.android;

/**
 * Detects whether the runtime is Android's ART (or its Dalvik predecessor),
 * where classes cannot be defined at run time and parts of the JDK class
 * library are absent.
 * <p>
 * The VM name is the primary signal: Android reports {@code java.vm.name} as
 * {@code "Dalvik"} on every release to date. A JVM merely carrying Android
 * classes on its class path, as Robolectric tests do, is not Android and keeps
 * the JVM behaviour. The framework's {@code Activity} class is consulted only
 * when the property cannot be read (GROOVY-12384).
 */
public abstract class AndroidSupport {
    private static final boolean IS_ANDROID = detect();

    private static boolean detect() {
        String vmName;
        try {
            vmName = System.getProperty("java.vm.name");
        } catch (SecurityException e) {
            vmName = null;
        }
        if (vmName != null) {
            return vmName.startsWith("Dalvik");
        }
        try {
            Class.forName("android.app.Activity", false, AndroidSupport.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isRunningAndroid() {
        return IS_ANDROID;
    }

}
