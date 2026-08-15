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
 * Covariant {@code doCall} so javac emits a bridge. {@code CallOverride} must
 * skip that bridge and cache the most-derived declaration.
 */
class ObjectDoCallClosure extends Closure<Object> {

    ObjectDoCallClosure() {
        super(null);
    }

    public Object doCall(final Object value) {
        return value;
    }
}

public final class StringDoCallClosure extends ObjectDoCallClosure {

    public StringDoCallClosure() {
        super();
    }

    @Override
    public String doCall(final Object value) {
        return String.valueOf(value);
    }
}
