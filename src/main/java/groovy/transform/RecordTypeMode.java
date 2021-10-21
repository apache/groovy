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
package groovy.transform;

/**
 * Intended mode to use for records when using the {@code @RecordType} annotation (or {@code record} keyword).
 *
 * @since 4.0.0
 * @see RecordType
 */
public enum RecordTypeMode {
    /**
     * Produce a record-like class.
     */
    EMULATE,

    /**
     * Produce a Java-like "native" record (JEP 359/384/395).
     */
    NATIVE,

    /**
     * Produce native records when compiling for a suitable target bytecode (JDK16+).
     */
    AUTO
}
