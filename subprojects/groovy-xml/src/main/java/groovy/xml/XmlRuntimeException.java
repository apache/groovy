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
package groovy.xml;

import groovy.lang.GroovyRuntimeException;
import org.apache.groovy.lang.annotation.Incubating;

import java.io.Serial;

/**
 * Represents a runtime exception that occurred when parsing or building XML.
 *
 * @since 6.0.0
 */
@Incubating
public class XmlRuntimeException extends GroovyRuntimeException {
    @Serial
    private static final long serialVersionUID = 7858720024082487492L;

    /**
     * Creates a new exception with a detail message.
     *
     * @param msg the detail message
     */
    public XmlRuntimeException(String msg) {
        super(msg);
    }

    /**
     * Creates a new exception wrapping the supplied cause.
     *
     * @param cause the underlying parsing or building failure
     */
    public XmlRuntimeException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new exception with a detail message and cause.
     *
     * @param msg the detail message
     * @param cause the underlying parsing or building failure
     */
    public XmlRuntimeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
