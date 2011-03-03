package org.codehaus.groovy

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilationUnit.PrimaryClassNodeOperation
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.ast.ClassNode

/**
 * Before Groovy 1.8, the structure of closure's inner classes
 * was a bit different than it is now in 1.8+.
 *
 * This test checks that closure inner classes are direct child of their enclosing class,
 * instead of being child of the outermost class.
 *
 * @author Guillaume Laforge
 */
class ClosureAndInnerClassNodeStructureTest extends GroovyTestCase {

    void testStructure() {
        def cu = new CompilationUnit()
        cu.addSource("t.groovy", '''
            exec {                               // t$_run_closure1
                def d = {                        // t$_run_closure1_closure3
                    def o = new Object() {       // t$1
                        void run() {             //
                            def f = {}           // t$_1_run_closure1
                        }                        //
                    }                            //
                    def e = {}                   // t$_run_closure1_closure3_closure4
                }                                //
            }                                    //
            def g = {}                           // t$_run_closure2
        ''')

        def classNodes = [:]

        cu.addPhaseOperation(new PrimaryClassNodeOperation() {
            void call(SourceUnit source, GeneratorContext context, ClassNode cn) {
                def recurse = { ClassNode node ->
                    classNodes[node.name] = node
                    for (icn in node.innerClasses) {
                        classNodes[icn.name] == icn
                        call(icn)
                    }
                }
                recurse(cn)
            }
        }, Phases.CLASS_GENERATION)

        cu.compile(Phases.CLASS_GENERATION)

        def assertParentOf = { String child ->
            [isClass: { String parent ->
                assert classNodes[child].outerClass.name == parent
            }]
        }

        assertParentOf 't$1'                               isClass 't'
        assertParentOf 't$_1_run_closure1'                 isClass 't$1'
        assertParentOf 't$_run_closure1'                   isClass 't'
        assertParentOf 't$_run_closure2'                   isClass 't'
        assertParentOf 't$_run_closure1_closure3'          isClass 't$_run_closure1'
        assertParentOf 't$_run_closure1_closure3_closure4' isClass 't$_run_closure1_closure3'
    }
}
