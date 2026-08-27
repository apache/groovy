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
package org.codehaus.groovy.control.messages;

/**
 * A format-neutral view of a {@link Message}: what it says and, when known, the
 * source and position it applies to. {@link org.codehaus.groovy.control.ErrorFormat}
 * renders its non-default layouts from this, so a new layout needs no change to the
 * message classes (GROOVY-12312).
 *
 * @param file   the source unit name, or {@code null} when the message has none
 * @param line   the 1-based line, or {@code -1} when the message has no position
 * @param column the 1-based column, or {@code -1} when unknown
 * @param text   the message text, which may span several lines
 * @since 6.0.0
 */
public record Diagnostic(String file, int line, int column, String text) {
}
