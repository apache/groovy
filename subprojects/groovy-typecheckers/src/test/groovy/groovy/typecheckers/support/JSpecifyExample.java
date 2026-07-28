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
package groovy.typecheckers.support;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Precompiled Java class using JSpecify's TYPE_USE nullness annotations, consumed by
 * {@code NullCheckerTest} to verify that type-use annotations are ingested when reading
 * compiled classes (GROOVY-12206). JSpecify annotations target {@code TYPE_USE} only,
 * so they end up in type-annotation bytecode attributes rather than on the declarations.
 */
public class JSpecifyExample {
    public static @Nullable String findValue(String key) {
        return "example".equals(key) ? "value" : null;
    }

    public static @NonNull String requireValue(@NonNull String value) {
        return value;
    }
}
