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
package org.codehaus.groovy.runtime.typehandling;

public class EqualityTestAbstractClass implements EqualityTestInterface {

    private final int id;
    private final String value;

    public EqualityTestAbstractClass(int id, String value) {
        this.id = id;
        this.value = value;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public int compareTo(EqualityTestInterface other) {
        if (other == null) {
            return 1;
        }
        if (getValue() == null) {
            if (other.getValue() == null) {
                return 0;
            }
            return -1;
        }
        if (other.getValue() == null) {
            return 1;
        }
        return getValue().compareTo(other.getValue());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof EqualityTestInterface) {
            EqualityTestInterface castedOther = (EqualityTestInterface) other;
            return getId() == castedOther.getId();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getId();
    }

}
