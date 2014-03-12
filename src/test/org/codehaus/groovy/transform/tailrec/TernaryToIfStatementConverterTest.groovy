package org.codehaus.groovy.transform.tailrec

import org.codehaus.groovy.ast.builder.AstAssert
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.stmt.IfStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.junit.Test

/**
 * @author Johannes Link
 */
class TernaryToIfStatementConverterTest {

    @Test
    public void simpleTernary() {
        ReturnStatement statement = new AstBuilder().buildFromSpec {
            returnStatement {
                ternary {
                    booleanExpression {
                        constant true
                    }
                    constant 1
                    constant 2
                }
            }
        }[0]

        IfStatement expected = new AstBuilder().buildFromSpec {
            ifStatement {
                booleanExpression {
                    constant true
                }
                returnStatement {
                    constant 1
                }
                returnStatement {
                    constant 2
                }
            }
        }[0]

        def ifStatement = new TernaryToIfStatementConverter().convert(statement)

        AstAssert.assertSyntaxTree([expected], [ifStatement])
    }

}
