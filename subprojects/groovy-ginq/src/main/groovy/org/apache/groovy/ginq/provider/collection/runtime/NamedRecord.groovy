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
import groovy.transform.stc.POJO

/**
 * Represents named record, which is constructed by clauses excluding {@code from} and joins
 *
 * @since 4.0.0
 */
@POJO
@CompileStatic
class NamedRecord<E, T> extends NamedTuple<E> {
    private SourceRecord<T> sourceRecord
    private final List<String> aliasList

    NamedRecord(List<E> elementList, List<String> nameList, List<String> aliasList = Collections.emptyList()) {
        super(elementList, nameList)
        this.aliasList = aliasList
    }

    @Override
    def getAt(String name) {
        if (exists(name)) {
            def value = super.getAt(name)
            return value
        }

        return sourceRecord.get(name)
    }

    List<String> getAliasList() {
        return Collections.unmodifiableList(aliasList)
    }

    NamedRecord<E, T> sourceRecord(T sr) {
        this.sourceRecord = new SourceRecord<>(sr, aliasList)
        return this
    }
}
