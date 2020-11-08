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
 * Represents source record, which is constructed by {@code from}/joins clauses
 *
 * @since 4.0.0
 */
@PackageScope
@POJO
@CompileStatic
class SourceRecord<T> implements Serializable {
    private static final String ALL = '*'
    private final T sourceRecord
    private final List<String> aliasList
    private Map<String, Object> sourceRecordCache

    SourceRecord(T sourceRecord, List<String> aliasList) {
        this.sourceRecord = sourceRecord
        this.aliasList = aliasList
    }

    def getAt(String name) {
        if (null == sourceRecordCache) sourceRecordCache = new HashMap<>(4)

        return sourceRecordCache.computeIfAbsent(name, n -> findSourceRecordByName(n))
    }

    def get(String name) {
        return getAt(name)
    }

    private findSourceRecordByName(String name) {
        if (ALL == name) return sourceRecord

        if (!aliasList.contains(name)) {
            throw new GroovyRuntimeException("Failed to find data source by the alias: $name")
        }

        def accessPath = sourceRecord

        if (accessPath !instanceof Tuple2) return accessPath

        for (int i = aliasList.size() - 1; i >= 0; i--) {
            String alias = aliasList.get(i)
            if (name == alias) {
                if (accessPath instanceof Tuple2) {
                    return accessPath.get(1)
                } else {
                    return accessPath
                }
            } else if (accessPath instanceof Tuple2) {
                accessPath = accessPath.get(0)
            }
        }

        return accessPath
    }
}