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
package org.apache.groovy.mcp;

import java.nio.file.Path;
import java.util.List;

/**
 * Launch limits for one MCP server. The classpath is fixed here so a
 * model cannot load jars. A {@code null} root allows in-memory buffers
 * only. The server does not scan the root.
 *
 * @param root workspace jail, or {@code null}
 * @param classpath compile classpath entries; not scanned as sources
 * @param maxMessageBytes maximum UTF-8 bytes of one stdio message
 * @param maxSourceChars maximum UTF-16 length of {@code text}, and maximum file bytes of {@code path}
 * @param maxResultChars maximum JSON length of one tool or resource payload
 */
public record McpServerConfig(Path root, List<String> classpath, int maxMessageBytes, int maxSourceChars,
                              int maxResultChars) {

    /** Default stdio message cap, in bytes. */
    public static final int DEFAULT_MAX_MESSAGE_BYTES = 2 * 1024 * 1024;

    /** Default source cap. See {@link #maxSourceChars()}. */
    public static final int DEFAULT_MAX_SOURCE_CHARS = 512 * 1024;

    /** Default JSON payload cap, in characters. */
    public static final int DEFAULT_MAX_RESULT_CHARS = 512 * 1024;

    /**
     * Validates limits and copies the classpath.
     */
    public McpServerConfig {
        classpath = classpath == null ? List.of() : List.copyOf(classpath);
        if (root != null) {
            root = root.toAbsolutePath().normalize();
        }
        if (maxMessageBytes < 1 || maxSourceChars < 1 || maxResultChars < 1) {
            throw new IllegalArgumentException("limits must be positive");
        }
    }

    /**
     * In-memory buffers only, with the default caps.
     *
     * @return a config with no root and no classpath
     */
    public static McpServerConfig defaults() {
        return new McpServerConfig(null, List.of(), DEFAULT_MAX_MESSAGE_BYTES, DEFAULT_MAX_SOURCE_CHARS,
                DEFAULT_MAX_RESULT_CHARS);
    }

    /**
     * Default caps, with an optional root and classpath.
     *
     * @param root workspace jail, or {@code null}
     * @param classpath compile classpath, or {@code null} for none
     * @return the config
     */
    public McpServerConfig(final Path root, final List<String> classpath) {
        this(root, classpath, DEFAULT_MAX_MESSAGE_BYTES, DEFAULT_MAX_SOURCE_CHARS, DEFAULT_MAX_RESULT_CHARS);
    }
}
