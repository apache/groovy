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

public enum AnnotationCollectorMode {
    /**
     * Annotations from the annotation collection will always be inserted. After all transforms have been run, it will
     * be an error if multiple annotations (excluding those with SOURCE retention) exist.
     */
    DUPLICATE,

    /**
     * Annotations from the collector will be added and any existing annotations with the same name will be removed.
     */
    PREFER_COLLECTOR,

    /**
     * Annotations from the collector will be ignored if any existing annotations with the same name are found.
     */
    PREFER_EXPLICIT,

    /**
     * Annotations from the collector will be added and any existing annotations with the same name will be removed but any new parameters found within existing annotations will be merged into the added annotation.
     */
    PREFER_COLLECTOR_MERGED,

    /**
     * Annotations from the collector will be ignored if any existing annotations with the same name are found but any new parameters on the collector annotation will be added to existing annotations.
     */
    PREFER_EXPLICIT_MERGED
}
