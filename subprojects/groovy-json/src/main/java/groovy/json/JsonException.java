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
package groovy.json;

/**
 * <code>JsonException</code> is the exception thrown by the JSON builder and slurper classes,
 * whenever a problem occurs when creating or parsing JSON data structures.
 *
 * @since 1.8.0
 */
public class JsonException extends RuntimeException {
    public JsonException() {
        super();
    }

    public JsonException(String s) {
        super(s);
    }

    public JsonException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public JsonException(Throwable throwable) {
        super(throwable);
    }

}
