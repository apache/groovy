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
package org.apache.groovy.ginq.provider.sql.runtime;

import groovy.sql.GroovyRowResult;
import org.apache.groovy.lang.annotation.Incubating;

import java.util.Map;

/**
 * One source row rebuilt from a mixed-mode join. Column values live in the
 * map under the names reported for that source. The row ordinal used to
 * re-align a pushed join is kept beside the map, so a real column cannot
 * be overwritten by it and a column of any name stays readable.
 *
 * @since 7.0.0
 */
@Incubating
public final class MixedSourceRow extends GroovyRowResult {
    private final int ordinal;

    /**
     * @param columns column name to value, in select order
     * @param ordinal the output-row index shared by every alias of this joined row
     */
    public MixedSourceRow(Map<String, Object> columns, int ordinal) {
        super(columns);
        this.ordinal = ordinal;
    }

    /**
     * The join ordinal. Not a column, and not looked up by name.
     *
     * @return the output-row index
     */
    public int mixedOrdinal() {
        return ordinal;
    }
}
