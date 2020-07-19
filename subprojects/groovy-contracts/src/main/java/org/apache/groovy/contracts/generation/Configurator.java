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
package org.apache.groovy.contracts.generation;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>Handles {@code -ea} and {@code -da} runtime input arguments for enabling and
 * disabling contract elements.</p>
 */
public final class Configurator {

    public static final String DISABLED_ASSERTIONS = "-da";
    public static final String ENABLED_ASSERTIONS = "-ea";

    public static final String PACKAGE_PREFIX = ":";
    public static final String ENABLE_PACKAGE_ASSERTIONS = ENABLED_ASSERTIONS + PACKAGE_PREFIX;
    public static final String DISABLE_PACKAGE_ASSERTIONS = DISABLED_ASSERTIONS + PACKAGE_PREFIX;
    public static final String PACKAGE_POSTFIX = "...";

    private static Map<String, Boolean> assertionConfiguration;

    static {
        initAssertionConfiguration();
    }

    private static void initAssertionConfiguration() {

        assertionConfiguration = new HashMap<String, Boolean>();
        // per default assertion are enabled (Groovy like)
        assertionConfiguration.put(null, Boolean.TRUE);

        RuntimeMXBean runtimemxBean = ManagementFactory.getRuntimeMXBean();
        for (String arg : runtimemxBean.getInputArguments()) {
            if (DISABLED_ASSERTIONS.equals(arg)) {
                assertionConfiguration.put(null, Boolean.FALSE);

            } else if (arg.startsWith(ENABLE_PACKAGE_ASSERTIONS) && arg.endsWith(PACKAGE_POSTFIX)) {
                final String packageName = arg.substring(ENABLE_PACKAGE_ASSERTIONS.length(), arg.length() - PACKAGE_POSTFIX.length());
                assertionConfiguration.put(packageName, Boolean.TRUE);

            } else if (arg.startsWith(DISABLE_PACKAGE_ASSERTIONS) && arg.endsWith(PACKAGE_POSTFIX)) {
                final String packageName = arg.substring(DISABLE_PACKAGE_ASSERTIONS.length(), arg.length() - PACKAGE_POSTFIX.length());

                assertionConfiguration.put(packageName, Boolean.FALSE);
            } else if (arg.startsWith(ENABLE_PACKAGE_ASSERTIONS)) {
                final String className = arg.substring(ENABLE_PACKAGE_ASSERTIONS.length(), arg.length());
                assertionConfiguration.put(className, Boolean.TRUE);

            } else if (arg.startsWith(DISABLE_PACKAGE_ASSERTIONS)) {
                final String className = arg.substring(DISABLE_PACKAGE_ASSERTIONS.length(), arg.length());

                assertionConfiguration.put(className, Boolean.FALSE);
            }
        }
    }

    /**
     * This static method is used within generated code to check whether assertions have been disabled for the current class or not.
     *
     * @param className the class name to look up in the assertion configuration
     * @return whether assertion checking is enabled or not
     */
    public static boolean checkAssertionsEnabled(final String className) {
        return internalMethod(className);
    }

    private static boolean internalMethod(String className) {
        if (className == null || className.length() == 0) return false;

        if (assertionConfiguration.containsKey(className)) return assertionConfiguration.get(className);
        if (className.lastIndexOf('.') < 0) return assertionConfiguration.get(null);

        String packageName = className.substring(0, className.lastIndexOf('.'));

        while (!assertionConfiguration.containsKey(packageName)) {
            int dotIndex = packageName.lastIndexOf('.');
            if (dotIndex < 0) return assertionConfiguration.get(null);

            packageName = packageName.substring(0, dotIndex);
        }

        if (assertionConfiguration.containsKey(packageName)) return assertionConfiguration.get(packageName);

        return assertionConfiguration.get(null);
    }
}
