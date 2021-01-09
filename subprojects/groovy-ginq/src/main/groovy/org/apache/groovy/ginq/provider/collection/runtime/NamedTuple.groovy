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
 * Immutable named list to represent list result of GINQ
 *
 * @since 4.0.0
 */
@PackageScope
@POJO
@CompileStatic
class NamedTuple<E> extends Tuple<E> {
    private final Map<String, E> data = new LinkedHashMap<>()

    NamedTuple(List<E> elementList, List<String> nameList) {
        super(elementList as E[])

        int nameListSize = nameList.size()
        if (nameListSize != new ArrayList<>(nameList).unique().size()) {
            throw new IllegalArgumentException("names should be unique: $nameList")
        }

        for (int i = 0, n = nameListSize; i < n; i++) {
            data.put(nameList.get(i), elementList.get(i))
        }
    }

    def getAt(String name) {
        return data.get(name)
    }

    def get(String name) {
        return getAt(name)
    }

    boolean exists(String name) {
        return data.containsKey(name)
    }

    List<String> getNameList() {
        return Collections.unmodifiableList(data.keySet().toList())
    }

    @Override
    String toString() {
        '(' + nameList.withIndex()
                .collect((String n, int i) -> { "${n}:${this[i]}" })
                .join(', ') + ')'
    }
}
