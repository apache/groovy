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
package org.apache.groovy.internal.runtime.invoke;

import groovy.lang.MissingMethodException;

import java.io.IOException;

/**
 * Java fixtures with exact modifiers for {@link InvokerFactory} / {@link DirectInvoker} tests.
 * Groovy sources default to public, which would collapse the modifier table.
 */
public class DirectInvokerSubjects {

    public String ping() {
        return "pong";
    }

    public static String staticPing() {
        return "static-pong";
    }

    public void noop() {
    }

    public int add(final int a, final int b) {
        return a + b;
    }

    public boolean flag(final boolean v) {
        return v;
    }

    public String echo(final String s) {
        return s;
    }

    public String join(final String a, final String... rest) {
        if (rest == null || rest.length == 0) {
            return a;
        }
        return a + rest[0];
    }

    public String boomRuntime() {
        throw new IllegalStateException("runtime-boom");
    }

    public String boomError() {
        throw new AssertionError("error-boom");
    }

    public String boomChecked() throws IOException {
        throw new IOException("checked-boom");
    }

    public String boomMme() {
        throw new MissingMethodException("missing", DirectInvokerSubjects.class, null);
    }

    private String secret() {
        return "secret";
    }

    private static String staticSecret() {
        return "static-secret";
    }

    protected String protectedPing() {
        return "protected-pong";
    }

    String packagePing() {
        return "package-pong";
    }

    public interface Defaults {
        default String greet() {
            return "hello";
        }

        private String hidden() {
            return "hidden-iface";
        }

        static String staticGreet() {
            return "static-hello";
        }
    }

    public static final class DefaultsImpl implements Defaults {
    }

    public abstract static class AbstractHost {
        public abstract String abs();
    }

    static final class PackageHost {
        public String visible() {
            return "pkg-visible";
        }
    }
}
