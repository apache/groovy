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
 * Intended mode to use for sealed classes when using the {@code @Sealed} annotation (or {@code sealed} keyword).
 *
 * @since 4.0.0
 * @see Sealed
 */
public enum SealedMode {
    /**
     * Indicate the sealed nature using annotations.
     * This allows sealed hierarchies in earlier JDKs but for integration purposes won't be recognised by Java.
     */
    EMULATE,

    /**
     * Produce Java-like code with sealed nature indicated by "native" bytecode information (JEP 360/397/409).
     * Produces a compile-time error when used on earlier JDKs.
     */
    NATIVE,

    /**
     * Produce native sealed classes when compiling for a suitable target bytecode (JDK17+)
     * and automatically emulate the concept on earlier JDKs.
     */
    AUTO
}
