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
package groovy.typecheckers

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail

final class SqlInjectionCheckerTest {

    private static final String PREAMBLE = 'groovy.sql.Sql sql = null\nString title = "x"\nboolean cond = true\n'

    private static GroovyShell shell

    @BeforeAll
    static void setUp() {
        shell = new GroovyShell(configured())
    }

    private static CompilerConfiguration configured() {
        new CompilerConfiguration().tap {
            def customizer = new ASTTransformationCustomizer(groovy.transform.TypeChecked)
            customizer.annotationParameters = [extensions: 'groovy.typecheckers.SqlInjectionChecker']
            addCompilationCustomizers(customizer)
        }
    }

    // A quoted interpolation reaching a Sql sink through a local must be a compile error.
    private static void assertRejected(String body) {
        def err = shouldFail shell, PREAMBLE + body
        assert err.message.contains('SQL injection')
    }

    // A safe (or irrelevant) usage must type-check cleanly; parse compiles without running (no DB needed).
    private static void assertAccepted(String body) {
        shell.parse(PREAMBLE + body)
    }

    // Compile through type checking and return the collected warning messages.
    // The source carries its own @TypeChecked(extensions=...), so no global customizer is applied.
    private static List<String> warningsFor(String source) {
        def cu = new CompilationUnit(new CompilerConfiguration())
        cu.addSource('SqlInjectionWarn.groovy', source)
        cu.compile(Phases.CLASS_GENERATION)
        (cu.errorCollector.warnings ?: []).collect { it.message }
    }

    // === dangerous: quoted interpolation flowing into a Sql sink (compile error) ===

    @Test
    void testDirectGString() {
        assertRejected "sql.rows(\"select * from Book where title = '\${title}'\")"
    }

    @Test
    void testAssignmentCoercion() {
        // GString coerced to String before the call — invisible to the runtime guard
        assertRejected "String query = \"select * from Book where title = '\${title}'\"\nsql.rows(query)"
    }

    @Test
    void testToStringCoercion() {
        assertRejected "sql.rows(\"select * from Book where title = '\${title}'\".toString())"
    }

    @Test
    void testAsStringCoercion() {
        assertRejected "sql.rows(\"select * from Book where title = '\${title}'\" as String)"
    }

    @Test
    void testDoubleQuotedInterpolation() {
        assertRejected "sql.execute(\"select * from Book where title = \\\"\${title}\\\"\")"
    }

    @Test
    void testOtherSinkMethods() {
        assertRejected "sql.firstRow(\"select * from Book where title = '\${title}'\")"
        assertRejected "sql.eachRow(\"select * from Book where title = '\${title}'\") { }"
        assertRejected "sql.execute(\"select * from Book where title = '\${title}'\")"
    }

    @Test
    void testAliasChain() {
        assertRejected "String a = \"select * from Book where title = '\${title}'\"\nString b = a\nsql.rows(b)"
    }

    @Test
    void testReassignmentToDangerousIsFlagged() {
        // false-negative guard: safe declaration, then reassigned to a dangerous value
        assertRejected "String q = \"select * from Book where title = \$title\"\n" +
            "q = \"select * from Book where title = '\${title}'\"\nsql.rows(q)"
    }

    @Test
    void testDangerousInOneBranchIsFlaggedAfterMerge() {
        assertRejected "String q = \"select * from Book where id = 1\"\n" +
            "if (cond) { q = \"select * from Book where title = '\${title}'\" }\nsql.rows(q)"
    }

    // === safe: value bound as a parameter, reassigned away, or not a Sql sink ===

    @Test
    void testUnquotedInterpolationIsBound() {
        assertAccepted "sql.rows(\"select * from Book where title = \$title\")"
    }

    @Test
    void testUnquotedInterpolationViaVariable() {
        assertAccepted "String query = \"select * from Book where title = \$title\"\nsql.rows(query)"
    }

    @Test
    void testReassignmentToSafeIsNotFlagged() {
        // false-positive guard: dangerous declaration, then reassigned to a safe value before the sink
        assertAccepted "String q = \"select * from Book where title = '\${title}'\"\n" +
            "q = \"select * from Book where id = 1\"\nsql.rows(q)"
    }

    @Test
    void testPlainConstantStringNotFlagged() {
        assertAccepted "sql.execute(\"delete from Book where title = 'a fixed literal'\")"
    }

    @Test
    void testQuotedInterpolationOnNonSqlReceiverNotFlagged() {
        assertAccepted '''
            class NotSql { List rows(String query) { [] } }
            def helper = new NotSql()
            helper.rows("select * from Book where title = '${title}'")
        '''
    }

    // === field taint is a warning, not an error ===

    @Test
    void testFieldFlatteningIsWarningNotError() {
        def source = '''
            import groovy.transform.TypeChecked
            class Dao {
                String query = "select * from Book where title = '${title}'"
                String title = 'x'
                @TypeChecked(extensions='groovy.typecheckers.SqlInjectionChecker')
                void run(groovy.sql.Sql sql) { sql.rows(query) }
            }
        '''
        // compiles (no error), but a warning is emitted
        def warnings = warningsFor(source)
        assert warnings.any { it.contains('SQL injection') && it.contains('warning only') }
    }

    // === suppression ===

    @Test
    void testSuppressWarningsOnMethodSilencesCheck() {
        assertAccepted '''
            class Dao {
                String title = 'x'
                @SuppressWarnings('groovy.sql.injection')
                void run(groovy.sql.Sql sql) { sql.rows("select * from Book where title = '${title}'") }
            }
        '''
    }
}
