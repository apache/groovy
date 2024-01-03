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
package groovy.typecheckers

import java.lang.annotation.Documented
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * If this annotation is attached to a {@link Formatter#format}-like method,
 * then when type-checking calls to the method, it will be a candidate method
 * checked by {@link FormatStringChecker}.
 *
 * The first argument must be of type {@link Locale} or {@link String}.
 * If the first argument is of type {@code Locale}, the second argument must be of type {@code String}.
 * The String argument is treated as a format string containing zero or more embedded
 * format specifiers. The format specifiers determine how the remaining arguments will
 * be used within the resulting output.
 *
 * The {@code FormatStringChecker} ensures that the format string is valid and the
 * remaining arguments are compatible with the embedded format specifiers.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.METHOD, ElementType.CONSTRUCTOR])
@interface FormatMethod {

}
