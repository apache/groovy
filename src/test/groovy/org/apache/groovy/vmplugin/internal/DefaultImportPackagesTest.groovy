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
package org.apache.groovy.vmplugin.internal

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertSame
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.Assertions.assertTrue

final class DefaultImportPackagesTest {

    private static final String[] PRECEDENCE = ['a.', 'b.', 'c.']

    @Test
    void orderedDropsUnknownPackagesAndFollowsPrecedence() {
        assertTrue(DefaultImportPackages.ordered(null, PRECEDENCE).isEmpty())
        assertTrue(DefaultImportPackages.ordered(Set.of(), PRECEDENCE).isEmpty())
        assertTrue(DefaultImportPackages.ordered(List.of('a.'), null).isEmpty())
        assertTrue(DefaultImportPackages.ordered(List.of('a.'), new String[0]).isEmpty())
        assertTrue(DefaultImportPackages.ordered(List.of('z.'), PRECEDENCE).isEmpty())

        Set<String> one = DefaultImportPackages.ordered(List.of('c.', 'c.'), PRECEDENCE)
        assertEquals(['c.'], one.toList())
        assertThrows(UnsupportedOperationException) { one.add('a.') }

        Set<String> many = DefaultImportPackages.ordered(List.of('c.', 'a.', 'z.', 'a.'), PRECEDENCE)
        assertEquals(['a.', 'c.'], many.toList())
        assertThrows(UnsupportedOperationException) { many.add('b.') }
    }

    @Test
    void includingKeepsTheSameInstanceWhenThePackageIsAlreadyPresent() {
        Set<String> existing = DefaultImportPackages.ordered(List.of('c.', 'a.'), PRECEDENCE)
        assertSame(existing, DefaultImportPackages.including(existing, 'a.', PRECEDENCE))

        Set<String> added = DefaultImportPackages.including(null, 'b.', PRECEDENCE)
        assertEquals(['b.'], added.toList())
        assertThrows(UnsupportedOperationException) { added.add('a.') }

        Set<String> union = DefaultImportPackages.including(Collections.singleton('c.'), 'a.', PRECEDENCE)
        assertEquals(['a.', 'c.'], union.toList())

        Set<String> foreign = DefaultImportPackages.including(Collections.singleton('z.'), 'b.', PRECEDENCE)
        assertEquals(['b.'], foreign.toList())

        Set<String> dropped = DefaultImportPackages.including(null, 'z.', PRECEDENCE)
        assertTrue(dropped.isEmpty())
        assertThrows(UnsupportedOperationException) { dropped.add('a.') }

        Set<String> fromEmpty = DefaultImportPackages.including(Set.of(), 'a.', PRECEDENCE)
        assertEquals(['a.'], fromEmpty.toList())
    }

    @Test
    void mergeUnionsInPrecedenceOrderAndIgnoresEmptyAdditions() {
        Map<String, Set<String>> target = new LinkedHashMap<>()
        target.put('Keep', Collections.singleton('a.'))
        Set<String> kept = target.get('Keep')

        DefaultImportPackages.merge(target, null, PRECEDENCE)
        DefaultImportPackages.merge(target, Map.of(), PRECEDENCE)
        assertSame(kept, target.get('Keep'))
        assertEquals(1, target.size())

        Map<String, Set<String>> skipped = new LinkedHashMap<>()
        skipped.put('Nope', Set.of('z.'))
        skipped.put('Nil', null)
        DefaultImportPackages.merge(target, skipped, PRECEDENCE)
        assertFalse(target.containsKey('Nope'))
        assertFalse(target.containsKey('Nil'))

        DefaultImportPackages.merge(target, Map.of('S', Set.of('c.')), PRECEDENCE)
        assertEquals(['c.'], target.get('S').toList())

        Set<String> both = DefaultImportPackages.ordered(List.of('a.', 'b.'), PRECEDENCE)
        target.put('Name', both)
        DefaultImportPackages.merge(target, Map.of('Name', Set.of('a.')), PRECEDENCE)
        assertSame(both, target.get('Name'))

        target.put('U', Collections.singleton('c.'))
        DefaultImportPackages.merge(target, Map.of('U', Set.of('a.')), PRECEDENCE)
        assertEquals(['a.', 'c.'], target.get('U').toList())
        assertThrows(UnsupportedOperationException) { target.get('U').add('b.') }
    }
}
