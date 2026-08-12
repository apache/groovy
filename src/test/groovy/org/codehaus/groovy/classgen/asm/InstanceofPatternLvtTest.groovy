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
package org.codehaus.groovy.classgen.asm

import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodNode

/**
 * GROOVY-12242: checks that the LocalVariableTable debug ranges emitted for
 * {@code instanceof} pattern variables agree with flow scoping. A pattern
 * variable may be visible over several disjoint bytecode regions (e.g. the
 * condition plus the then-arm, then again after the if), so entries are
 * checked by coverage properties rather than by count:
 * <ul>
 *   <li>no entry for the pattern name covers a region where flow scoping
 *       says the name is dead (negative), and</li>
 *   <li>every region where the name is live is covered by some entry
 *       (positive), so debuggers keep showing the variable there.</li>
 * </ul>
 */
final class InstanceofPatternLvtTest extends AbstractBytecodeTestCase {

    private MethodNode treeMethod(final String name) {
        assert classBytes != null : 'no class bytes captured'
        def cn = new ClassNode()
        new ClassReader(classBytes).accept(cn, 0)
        def mn = cn.methods.find { it.name == name }
        assert mn != null : "method '$name' not found; methods: ${cn.methods*.name}"
        mn
    }

    /** LVT entries for {@code name} as maps of desc/slot/start/end (instruction indices). */
    private static List<Map> entriesFor(final MethodNode mn, final String name) {
        (mn.localVariables ?: []).findAll { it.name == name }.collect { lv ->
            [desc: lv.desc, slot: lv.index,
             start: mn.instructions.indexOf(lv.start),
             end: mn.instructions.indexOf(lv.end)]
        }
    }

    private static boolean covers(final Map entry, final int idx) {
        entry.start <= idx && idx < entry.end
    }

    private static int ldcIndex(final MethodNode mn, final Object cst) {
        for (int i = 0; i < mn.instructions.size(); i += 1) {
            def insn = mn.instructions.get(i)
            if (insn instanceof LdcInsnNode && insn.cst == cst) return i
        }
        -1
    }

    private static int marker(final MethodNode mn, final String cst) {
        int idx = ldcIndex(mn, cst)
        assert idx != -1 : "marker '$cst' not found"
        idx
    }

    private static String describe(final MethodNode mn) {
        (mn.localVariables ?: []).collect { lv ->
            "LVT ${lv.name} ${lv.desc} slot=${lv.index} range=[${mn.instructions.indexOf(lv.start)}, ${mn.instructions.indexOf(lv.end)})"
        }.join('\n')
    }

    private void assertNoneCover(final MethodNode mn, final List<Map> entries, final String markerText) {
        int idx = marker(mn, markerText)
        entries.each { e ->
            assert !covers(e, idx) :
                "entry ${e} covers dead-region marker '$markerText' at $idx\n${describe(mn)}"
        }
    }

    private void assertSomeCover(final MethodNode mn, final List<Map> entries, final String markerText) {
        int idx = marker(mn, markerText)
        assert entries.any { covers(it, idx) } :
            "no entry covers live-region marker '$markerText' at $idx; entries: ${entries}\n${describe(mn)}"
    }

    // case 1: pattern variable dead in the else-arm, live in the then-arm
    @Test
    void testPatternVariableRangeExcludesElseArm() {
        compile method: 'm', '''
            def m(o) {
                if (o instanceof String s) {
                    print 'in-then'
                    print s
                } else {
                    print 'in-else'
                }
            }
            m('x')
        '''
        def mn = treeMethod('m')
        def entries = entriesFor(mn, 's')
        assert !entries.empty : "no LVT entries for s\n${describe(mn)}"
        assertNoneCover(mn, entries, 'in-else')
        assertSomeCover(mn, entries, 'in-then')
    }

    // case 2: redeclared local in else must not overlap the pattern variable's entries
    @Test
    void testRedeclaredLocalRangeDoesNotOverlapPattern() {
        compile method: 'm', '''
            def m(o) {
                if (o instanceof String s) {
                    print 'in-then'
                    print s
                } else {
                    def s = 'local'
                    print 'else-after-decl'
                    print s
                }
            }
            m('x')
        '''
        def mn = treeMethod('m')
        def entries = entriesFor(mn, 's')
        def pattern = entries.findAll { it.desc == 'Ljava/lang/String;' }
        def redeclared = entries.findAll { it.desc != 'Ljava/lang/String;' }
        assert !pattern.empty && !redeclared.empty : "expected both s variables in LVT\n${describe(mn)}"
        assert pattern*.slot.unique() != redeclared*.slot.unique() : 'expected distinct slots'
        pattern.each { p ->
            redeclared.each { r ->
                boolean overlap = p.start < r.end && r.start < p.end
                assert !overlap : "pattern entry ${p} overlaps redeclared entry ${r}\n${describe(mn)}"
            }
        }
        assertNoneCover(mn, pattern, 'else-after-decl')
        assertSomeCover(mn, redeclared, 'else-after-decl')
        assertSomeCover(mn, pattern, 'in-then')
    }

    // case 3: negated pattern with abrupt then — dead in the then-arm, live after the if
    @Test
    void testNegatedPatternRangeExcludesThenArm() {
        compile method: 'm', '''
            def m(o) {
                if (!(o instanceof String s)) {
                    print 'in-then'
                    return
                }
                print 'after-if'
                print s
            }
            m('x')
        '''
        def mn = treeMethod('m')
        def entries = entriesFor(mn, 's')
        assert !entries.empty : "no LVT entries for s\n${describe(mn)}"
        assertNoneCover(mn, entries, 'in-then')
        assertSomeCover(mn, entries, 'after-if')
    }

    // case 4: no-binding shape (`instanceof s || cond`) — dead in both arms
    @Test
    void testOrShapePatternRangeCoversNeitherArm() {
        compile method: 'm', '''
            def m(o, c) {
                if (o instanceof String s || c) {
                    print 'in-then'
                } else {
                    print 'in-else'
                }
            }
            m('x', true)
        '''
        def mn = treeMethod('m')
        def entries = entriesFor(mn, 's')
        assert !entries.empty : "no LVT entries for s\n${describe(mn)}"
        assertNoneCover(mn, entries, 'in-then')
        assertNoneCover(mn, entries, 'in-else')
    }
}
