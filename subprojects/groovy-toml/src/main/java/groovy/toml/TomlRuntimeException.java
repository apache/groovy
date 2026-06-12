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
package groovy.toml;

import groovy.lang.GroovyRuntimeException;
import org.apache.groovy.lang.annotation.Incubating;

import java.io.Serial;

/**
 * Represents a runtime exception raised while parsing or building TOML.
 *
 * @since 4.0.0
 */
@Incubating
public class TomlRuntimeException extends GroovyRuntimeException {
    @Serial
    private static final long serialVersionUID = -5723737489247290350L;

    /**
     * Creates an exception with a detail message.
     *
     * @param msg the description of the failure
     */
    public TomlRuntimeException(String msg) {
        super(msg);
    }

    /**
     * Creates an exception that wraps the underlying cause.
     *
     * @param cause the original failure
     */
    public TomlRuntimeException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates an exception with both a detail message and an underlying cause.
     *
     * @param msg the description of the failure
     * @param cause the original failure
     */
    public TomlRuntimeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
