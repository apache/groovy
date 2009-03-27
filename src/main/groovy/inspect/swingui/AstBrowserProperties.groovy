/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package groovy.inspect.swingui


org {
	codehaus {
		groovy {
			ast {
                ClassNode           = "ClassNode - \$expression.name"
                ConstructorNode     = "ConstructorNode - \$expression.name"
                MethodNode          = "MethodNode - \$expression.name"
                FieldNode           = "FieldNode - \$expression.name : \$expression.type"
                PropertyNode        = "PropertyNode - \${expression.field?.name} : \${expression.field?.type}"
                AnnotationNode      = "AnnotationNode - \${expression.classNode?.name}"

                stmt {
					BlockStatement      = "BlockStatement"
					ExpressionStatement = "ExpressionStatement"
				}
				expr {
					DeclarationExpression   = "Declaration - \$expression.text"
					VariableExpression      = "Variable - \$expression.name : \$expression.type"
					ConstantExpression      = "Constant - \$expression.value : \$expression.type"
					BinaryExpression        = "Binary - \$expression.text"
					ClassExpression         = "Class - \$expression.text"
				}
			}
		}
	}
}