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
package org.apache.groovy.groovysh.commands

/**
 * Tests for the {@code /types} command.
 */
class TypesTest extends SystemTestSupport {
    void testImport() {
        def names = ['C', 'I', 'T', 'R', 'E', 'A']
        system.execute('/types')
        def out = printer.output.join()
        names.each{ name -> assert !out.contains(name) }
        system.execute('class C {}')
        system.execute('interface I {}')
        system.execute('trait T {}')
        system.execute('record R() {}')
        system.execute('enum E {}')
        system.execute('@interface A {}')
        system.execute('/types')
        out = printer.output.join()
        names.each{ name -> assert out.contains(name) }
        assert engine.types.keySet() == ['C', 'I', 'T', 'R', 'E', 'A'] as Set
        system.execute('/types -d C')
        system.execute('/types -d R')
        assert engine.types.keySet() == ['I', 'T', 'E', 'A'] as Set
    }
}
