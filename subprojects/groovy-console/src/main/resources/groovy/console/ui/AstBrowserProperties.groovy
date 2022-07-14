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
package groovy.console.ui

org {
    codehaus {
        groovy {
            ast {
                ClassNode           = "ClassNode - \$expression.name"
                InnerClassNode      = "InnerClassNode - \$expression.name"
                ConstructorNode     = "ConstructorNode - \$expression.name(\${expression.parameters*.type*.toString(false).join(', ')})"
                MethodNode          = "MethodNode - \$expression.name(\${expression.parameters*.type*.toString(false).join(', ')})"
                FieldNode           = "FieldNode - \${expression.type.toString(false)} \$expression.name\${expression.initialValueExpression ? ' = ' + expression.initialValueExpression.text : ''}"
                PropertyNode        = "PropertyNode - \${expression.field?.type.toString(false)} \${expression.field?.name}\${expression.field?.initialValueExpression ? ' = ' + expression.field?.initialValueExpression.text : ''}"
                RecordComponentNode = "RecordComponentNode - \${expression.type.toString(false)} \$expression.name"
                AnnotationNode      = "AnnotationNode - \${expression.classNode?.name}"
                Parameter           = "Parameter - \${expression.type.toString(false)} \$expression.name\${expression.initialExpression ? ' = ' + expression.initialExpression.text : ''}"
                DynamicVariable     = "DynamicVariable - \$expression.name"

                stmt {
                    BlockStatement      = "BlockStatement - (\${expression.statements ? expression.statements.size() : 0})"
                    ExpressionStatement = "ExpressionStatement - \${expression?.expression.getClass().simpleName}"
                    ReturnStatement     = "ReturnStatement - \$expression.text"
                    TryCatchStatement   = "TryCatchStatement - \${expression.catchStatements?.size() ?: 0} catch, \${expression.finallyStatement ? 1 : 0} finally"
                    CatchStatement      = "CatchStatement - \$expression.exceptionType]"
                }
                expr {
                    ConstructorCallExpression = "ConstructorCall - \$expression.text"
                    SpreadExpression          = "Spread - \$expression.text"
                    ArgumentListExpression    = "ArgumentList - \$expression.text"
                    MethodCallExpression      = "MethodCall - \$expression.text"
                    GStringExpression         = "GString - \$expression.text"
                    AttributeExpression       = "Attribute - \$expression.text"
                    DeclarationExpression     = "Declaration - \$expression.text"
                    VariableExpression        = "Variable - \${expression.type.toString(false)} \$expression.name"
                    ConstantExpression        = "Constant - \${expression.type.toString(false)} \$expression.value"
                    BinaryExpression          = "Binary - \$expression.text"
                    ClassExpression           = "Class - \$expression.text"
                    BooleanExpression         = "Boolean - \$expression.text"
                    ArrayExpression           = "Array - \$expression.text"
                    ListExpression            = "List - \$expression.text"
                    TupleExpression           = "Tuple - \$expression.text"
                    FieldExpression           = "Field - \$expression.text"
                    PropertyExpression        = "Property - \$expression.propertyAsString"
                    NotExpression             = "Not - \$expression.text"
                    CastExpression            = "Cast - \$expression.text"
                }
            }
        }
    }
}
