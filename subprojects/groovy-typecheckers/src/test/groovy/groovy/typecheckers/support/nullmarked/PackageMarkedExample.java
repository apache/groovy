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
package groovy.typecheckers.support.nullmarked;

import org.jspecify.annotations.Nullable;

/**
 * Precompiled Java class in a package annotated with JSpecify's {@code @NullMarked}
 * (see {@code package-info.java}). None of the members below carry their own nullness
 * annotations, so their unannotated {@code String} types default to non-null; only
 * {@link #find(String)} opts back out with an explicit {@code @Nullable} return.
 * Consumed by {@code NullCheckerTest} for GROOVY-12207.
 */
public class PackageMarkedExample {
    // parameter is non-null by the package default
    public static String greet(String name) {
        return "hi " + name;
    }

    // explicit @Nullable return overrides the package default
    public static @Nullable String find(String key) {
        return "example".equals(key) ? "value" : null;
    }
}
