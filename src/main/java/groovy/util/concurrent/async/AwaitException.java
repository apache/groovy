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
package groovy.util.concurrent.async;

/**
 * Exception thrown when attempting to wait for the result of a promise
 * that aborted by throwing an exception. This exception can be
 * inspected using the {@link #getCause()} method.
 *
 * @see Promise
 * @since 6.0.0
 */
public class AwaitException extends RuntimeException {
    public AwaitException() {
    }

    public AwaitException(String message) {
        super(message);
    }

    public AwaitException(Throwable cause) {
        super(cause);
    }

    public AwaitException(String message, Throwable cause) {
        super(message, cause);
    }
}
