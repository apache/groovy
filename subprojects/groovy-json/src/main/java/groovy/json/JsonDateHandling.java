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

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;

/**
 * What a slurper makes of a string in a full ISO-8601 or JSON-date form.
 * <p>
 * This applies to {@link JsonParserType#INDEX_OVERLAY} and {@link JsonParserType#LAX}.
 * The other two parser types have no date handling and return the string whatever is
 * chosen here. A date carrying no time, such as {@code "2026-09-04"}, is left as a
 * {@code String} by every parser type.
 *
 * @since 6.0.0
 */
public enum JsonDateHandling {

    /**
     * Leave it as a {@code String}, as it appears in the document.
     */
    STRING,

    /**
     * Convert to a {@link Date}. This is the default, and what a slurper has
     * always done. A {@code Date} is an instant, so an offset in the source is not
     * preserved.
     */
    UTIL_DATE,

    /**
     * Convert to a {@link Instant}, which like {@link Date} is a point
     * on the timeline and so does not preserve an offset in the source.
     */
    INSTANT,

    /**
     * Convert to a {@link OffsetDateTime}, the only choice here that keeps the
     * offset the document carried.
     */
    OFFSET_DATE_TIME
}
