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
 * Intended mode to use when generating constructors to emulate default parameter values when using the {@link TupleConstructor} annotation.
 *
 * @since 4.0.0
 * @see TupleConstructor
 */
public enum DefaultsMode {
    /**
     * Produce a single constructor corresponding to the complete list of properties/fields of the class being compiled.
     */
    OFF,

    /**
     * Produce multiple constructors as required to handle any mandatory and optional arguments.
     * An argument is optional if the respective property/field has an explicit initial value.
     * A property/field without an initial value is deemed mandatory.
     */
    AUTO,

    /**
     * Produce multiple constructors as required from all parameters through to the no-arg constructor.
     * Defaults will be set according to any explicit initial values or the default value otherwise.
     */
    ON
}
