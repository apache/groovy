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
package org.apache.groovy.ginq.provider.collection.runtime

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.stc.POJO

/**
 * Represents identity for window value
 *
 * @param <T> the type of window value
 * @since 4.0.0
 */
@PackageScope
@POJO
@CompileStatic
class Identity<T> {
    private T object

    Identity(T object) {
        this.object = object
    }

    @Override
    boolean equals(Object o) {
        if (this === o) return true
        if (o !instanceof Identity) return false
        Identity<?> identity = (Identity<?>) o

        if (object instanceof NamedTuple && identity.object instanceof NamedTuple) {
            NamedTuple namedTuple = (NamedTuple) object
            NamedTuple otherNamedRecord = (NamedTuple) identity.object
            if (namedTuple.size() == otherNamedRecord.size()) {
                for (int i = 0; i < namedTuple.size(); i++) {
                    if (namedTuple.get(i) !== otherNamedRecord.get(i)) {
                        return false
                    }
                }
                return true
            }
        }

        return object === identity.object
    }

    @Override
    int hashCode() {
        return Objects.hash(object)
    }
}
