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
package bugs

import groovy.transform.Internal
import org.codehaus.groovy.classgen.Verifier
import org.junit.jupiter.api.Test

/**
 * GROOVY-9572B: the Verifier-generated {@code __$stMC} field (constant
 * {@link Verifier#STATIC_METACLASS_BOOL}) should carry
 * {@link groovy.transform.Internal} so that tooling (groovydoc per GEP-17)
 * can hide it from generated documentation of subclasses that inherit it.
 */
final class Groovy9572 {

    @Test
    void stMCFieldIsMarkedInternal() {
        def cls = new GroovyClassLoader().parseClass('class Sample9572 {}')
        def field = cls.getDeclaredField(Verifier.STATIC_METACLASS_BOOL)
        assert field != null
        assert field.isAnnotationPresent(Internal)
    }
}
