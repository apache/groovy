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
package org.apache.groovy.mcp.internal;

import java.util.Map;

/**
 * A JSON-RPC failure. The code is a protocol code, not an HTTP status.
 * Tool mistakes use {@link ToolFailure} instead, so the model can read them.
 */
public final class ProtocolException extends RuntimeException {

    private final int code;

    private final Map<String, Object> data;

    /**
     * @param code JSON-RPC error code
     * @param message short message safe to return to the client
     */
    public ProtocolException(final int code, final String message) {
        this(code, message, null);
    }

    /**
     * @param code JSON-RPC error code
     * @param message short message safe to return to the client
     * @param data optional JSON-RPC {@code error.data}, or {@code null}
     */
    public ProtocolException(final int code, final String message, final Map<String, Object> data) {
        super(message == null ? "" : message);
        this.code = code;
        this.data = data;
    }

    /**
     * @return the JSON-RPC error code
     */
    public int code() {
        return code;
    }

    /**
     * @return optional {@code error.data}, or {@code null} when the error has none
     */
    public Map<String, Object> data() {
        return data;
    }
}
