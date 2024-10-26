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
@CompileStatic
@PackageScope
@POJO
class NamedTuple<E> extends Tuple<E> {
    private static final long serialVersionUID = -5067092453136522209L
    private final Map<String, E> data

    NamedTuple(List<E> elementList, List<String> nameList) {
        super(elementList as E[])

        final int nameListSize = nameList.size()
        final int elementListSize = elementList.size()

        if (nameListSize != elementListSize) {
            throw new IllegalArgumentException("elements(size: $elementListSize) and names(size: $nameListSize) should have the same size")
        }
        if (nameListSize != new HashSet<>(nameList).size()) {
            throw new IllegalArgumentException("names should be unique: $nameList")
        }

        data = new LinkedHashMap<>((int) (nameListSize / 0.75) + 1)

        for (int i = 0; i < nameListSize; i += 1) {
            data.put(nameList.get(i), elementList.get(i))
        }
    }

    def get(String name) {
        return getAt(name)
    }

    def getAt(String name) {
        return data.get(name)
    }

    boolean exists(String name) {
        return data.containsKey(name)
    }

    List<String> getNameList() {
        return Collections.unmodifiableList(data.keySet().toList())
    }

    @Override
    String toString() {
        StringJoiner sj = new StringJoiner(', ', '(', ')')
        for (String name : getNameList()) {
            sj.add(name + ':' + this[name])
        }
        sj.toString()
    }
}
