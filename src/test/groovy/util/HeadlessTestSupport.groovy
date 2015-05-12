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
package groovy.util

import java.lang.reflect.Constructor

public abstract class HeadlessTestSupport {
    private static boolean headless;

    /**
     * A boolean indicating if we are running in headless mode.
     * Check this flag if you believe your test may make use of AWT/Swing
     * features, then simply return rather than running your test.
     *
     * @return true if running in headless mode
     */
    public static boolean isHeadless() {
        return headless
    }

    /**
     * Alias for isHeadless().
     *
     * @return true if running in headless mode
     */
    public static boolean getHeadless() {
        return isHeadless()
    }

    static {
        try {
            final Class jframe = Class.forName("javax.swing.JFrame")
            final Constructor constructor = jframe.getConstructor([String] as Class[])
            constructor.newInstance(["testing"] as String[])
            headless = false
        } catch (Throwable t) {
            // any exception means headless
            headless = true
        }
    }

}
