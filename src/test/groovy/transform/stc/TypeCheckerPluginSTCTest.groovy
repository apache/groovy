/*
 * Copyright 2003-2010 the original author or authors.
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
package groovy.transform.stc

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import groovy.transform.TypeChecked
import org.codehaus.groovy.transform.stc.TypeCheckerPluginFactory
import org.codehaus.groovy.transform.stc.TypeCheckerPlugin
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.DynamicVariable
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.MethodNode
import org.objectweb.asm.Opcodes
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.PropertyNode

/**
 * Unit tests for static type checking : plugins.
 *
 * @author Cedric Champeau
 */
class TypeCheckerPluginSTCTest extends StaticTypeCheckingTestCase {
    private Binding binding
    protected void setUp() {
        super.setUp();
        binding = new Binding()
        config = new CompilerConfiguration()
        config.addCompilationCustomizers(new ASTTransformationCustomizer(TypeChecked, pluginFactory: MySTCPluginFactory))
        configure()
        shell = new GroovyShell(binding, config)
    }
    
    static class MySTCPluginFactory implements TypeCheckerPluginFactory {
        TypeCheckerPlugin getTypeCheckerPlugin(final ClassNode node) {
            new DynamicVariablePlugin()
        }
    }
    
    static class DynamicVariablePlugin extends TypeCheckerPlugin {
        ClassNode resolveDynamicVariableType(DynamicVariable variable) {
            if ("mystring"==variable.name) {
                return ClassHelper.STRING_TYPE
            }

            super.resolveDynamicVariableType(variable)
        }

        @Override
        List<MethodNode> findMethod(final ClassNode receiver, final String name, final ClassNode... args) {
            if ('toDate'.equals(name)) {
                return [
                        new MethodNode(name, Opcodes.ACC_PUBLIC, ClassHelper.make(Date), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null)
                ]
            }
            super.findMethod(receiver, name, args)
        }

        @Override
        PropertyNode resolveProperty(final ClassNode receiver, final String propertyName) {
            if ('barProperty'.equals(propertyName)) {
                return new PropertyNode(propertyName, Opcodes.ACC_PUBLIC, ClassHelper.STRING_TYPE, receiver, null, null, null)
            }

            super.resolveProperty(receiver, propertyName)
        }


    }
    
    void testDynamicVariableResolvedWithPlugin() {
        binding['mystring'] = 'test'
        assertScript '''
            mystring.toUpperCase()
        '''
    }
    
    void testDynamicVariableNotResolvedWithPlugin() {
        shouldFailWithMessages '''
            foo
        ''', 'The variable [foo] is undeclared.'
    }
    
    void testResolveUnknownMethodOnDeclaredVariable() {
        assertScript '''
            String foo = 'myVariable'
            try {
                Date d = foo.toDate() // toDate doesn't exist on String, nor in DGM, but will be resolved by the plugin
            } catch (MissingMethodException e) {
                // catched because at runtime, method will not exist. For testing purposes.
            }
        '''
    }

    void testResolveUnknownProperty() {
        assertScript '''
            try {
                'foo'.barProperty.toUpperCase()
            } catch (MissingPropertyException e) {
                // catched because at runtime, property will not exist. For testing purposes.
            }
        '''
    }
    
}
