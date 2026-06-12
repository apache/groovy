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
package groovy.junit6.plugin;

/**
 * Delegate for closure predicates supplied to {@link ExpectedToFail}, exposing
 * the thrown exception under three convenient names: {@code ex} (the
 * {@link Throwable} itself), {@code message} (its message), and {@code cause}
 * (its cause, possibly {@code null}).
 *
 * @since 6.0.0
 */
public class ExpectedToFailContext {

    private final Throwable ex;
    private final String message;
    private final Throwable cause;

    public ExpectedToFailContext(Throwable thrown) {
        this.ex = thrown;
        this.message = thrown.getMessage();
        this.cause = thrown.getCause();
    }

    public Throwable getEx() {
        return ex;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getCause() {
        return cause;
    }
}
