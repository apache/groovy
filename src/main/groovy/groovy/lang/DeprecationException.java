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
package groovy.lang;

/**
 * Use this exception to mark a method implementation as being deprecated.
 *
 * Use the message to indicate the recommended way of calling the desired functionality.
 * Make throwing this exception the only line in the method implementation, i.e. unlike
 * the JavaDoc deprecated feature there is no relay to the new implementation but an early
 * and deliberate halt of execution ("fail early").
 *
 * This exception is supposed to be used in the SNAPSHOT releases only. Before release, all
 * references to this exception should be resolved and the according methods removed.
 */
public class DeprecationException extends RuntimeException {

    private static final long serialVersionUID = 8828016729085737697L;

    public DeprecationException(String message) {
        super(message);
    }

    public DeprecationException(String message, Throwable cause) {
        super(message, cause);
    }
}
