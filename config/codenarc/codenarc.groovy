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
ruleset {    
    ruleset('rulesets/naming.xml') {
        exclude 'PackageName' {
            regex = '^[a-z]([a-zA-Z\\.0-9])*\\b'
        } 

        'ClassName' {
            regex = '^[A-Z]([a-zA-Z0-9$_])*\\b'
            doNotApplyToClassNames='$Temp,fileNameFinderTest,rayMain'
        }
        'FieldName' {
            regex = '^[a-z]([a-zA-Z0-9$])*\\b'
            finalRegex = '^[a-z]([a-zA-Z0-9$])*\\b'
            staticFinalRegex = '^[A-Z]([A-Z0-9$_])*\\b|^serialVersionUID\\b'
            staticRegex = '^[A-Z]([A-Z0-9$_])*\\b'
            doNotApplyToClassNames='Entity,AstNodeToScriptVisitor,LookAndFeelHelper,SwingBuilder,Console,' +    
                        'JavadocAssertionTestBuilder,JavadocAssertionTestSuite,Main,Groovysh,Parser,' + 
                        'AliasTargetProxyCommand,WorkerThread,Cheddar,CategoryTestPropertyCategory,' + 
                        'GroovyClosureMethodsTest,SingletonBugPrivate,SingletonBugProtected,' + 
                        'GroovyInnerEnumBug$MyEnum,GroovyInnerEnumBug,' + 
                        'CategoryTestHelperPropertyReplacer,PrimitiveTypeFieldTest,I3830,Cheese,' + 
                        'SingletonBugTest,ClosureWithStaticVariablesBug,Groovy1018_Bug,Groovy3830Bug,Groovy4386_Bug,' + 
                        'GroovyInnerEnumBug,GroovySwingTestCase,GpathSyntaxTestSupport,MixedMarkupTestSupport,' + 
                        'TraversalTestSupport,CallClosureFieldAsMethodTest'
        }
        'PropertyName' {
            regex = '^[a-z]([a-zA-Z0-9$])*\\b'
            finalRegex = '^[a-z]([a-zA-Z0-9$])*\\b'
            staticFinalRegex = '^[A-Z]([A-Z0-9$_])*\\b|^serialVersionUID\\b'
            staticRegex = '^[A-Z]([A-Z0-9$_])*\\b'
            doNotApplyToClassNames='groovy.inspect.swingui.AstNodeToScriptVisitor,groovy.inspect.swingui.ScriptToTreeNodeAdapter,' +
                'groovy.swing.factory.TitledBorderFactory,groovy.ui.Console,groovy.ui.OutputTransforms,' +
                'org.codehaus.groovy.tools.shell.commands.HistoryCommand,org.codehaus.groovy.tools.shell.commands.PurgeCommand,' +
                'org.codehaus.groovy.tools.shell.commands.RecordCommand,org.codehaus.groovy.tools.shell.commands.ShadowCommand,' +
                'org.codehaus.groovy.tools.shell.commands.ShowCommand,org.codehaus.groovy.tools.shell.ComplexCommandSupport,' + 
                'groovy.bugs.StaticPropertyFoo,groovy.bugs.Groovy3135Bug,groovy.util.XmlParserTest,' +
                'org.codehaus.groovy.runtime.PerInstanceMetaClassTest,groovy.StaticImportChild,' +
                'gls.scope.VariablePrecedenceTest,groovy.bugs.Groovy3069Bug,groovy.StaticImportTarget,' +
                'gls.scope.VariablePrecedenceTest,groovy.bugs.Groovy3069Bug,groovy.mock.interceptor.Baz,' +
                'groovy.bugs.Groovy2706Bug,groovy.sql.TestHelper,org.codehaus.groovy.runtime.WriterAppendTest,' +
                'groovy.bugs.Groovy3135Bug,groovy.mock.interceptor.Baz,groovy.ReadLineTest,' +
                'groovy.bugs.TedsClosureBug,groovy.tree.ClosureClassLoaderBug,groovy.tree.NestedClosureBugTest,' +
                'groovy.tree.SmallTreeTest,groovy.ReadLineTest,groovy.bugs.Groovy3135Bug,' +
                'org.codehaus.groovy.runtime.DateGDKTest,groovy.sql.PersonDTO,groovy.bugs.One,' +
                'groovy.bugs.Two,org.codehaus.groovy.runtime.FileAppendTest,org.codehaus.groovy.runtime.WriterAppendTest,' +
                'org.codehaus.groovy.runtime.FileAppendTest,org.codehaus.groovy.runtime.WriterAppendTest,groovy.sql.PersonDTO,' +
                'groovy.bugs.Groovy3135Bug,Sphere,groovy.sql.PersonDTO,groovy.bugs.Groovy3135Bug,' +
                'gls.enums.GrooyColors3693,groovy.sql.PersonDTO,groovy.bugs.HasStaticFieldSomeClass,groovy.PrintTest,' +
                'groovy.bugs.StaticClosurePropertyBug,groovy.bugs.Groovy3311Bug,groovy.StaticImportParent,' +
                'org.codehaus.groovy.transform.TestTransform,org.codehaus.groovy.classgen.asm.InstructionSequenceHelperClassTest,' +
                'groovy.bugs.Groovy3511Bug,groovy.bugs.Groovy3135Bug,groovy.bugs.Groovy2556Bug,' +
                'groovy.mock.interceptor.Baz,org.codehaus.groovy.ast.builder.AstBuilderFromCodeTest,' +
                'groovy.bugs.UseStaticInClosureBug,groovy.bugs.Groovy2556Bug,' +
                'groovy.ReadLineTest,org.codehaus.groovy.runtime.FileAppendTest,org.codehaus.groovy.runtime.WriterAppendTest,' +
                'org.codehaus.groovy.transform.TestTransform,groovy.bugs.ClosureWithStaticVariablesBug'
        }
        'VariableName' {
            regex = '^[a-z]([a-zA-Z0-9$])*\\b'
            finalRegex = '^[a-z]([a-zA-Z0-9$])*\\b'
            doNotApplyToClassNames='groovy.GroovyClosureMethodsTest,groovy.RegularExpressionsTest,groovy.NumberMathTest,' + 
                        'groovy.NumberMathTest,groovy.NumberMathTest,groovy.NumberMathTest,groovy.NumberMathTest,' + 
                        'groovy.NumberMathTest,groovy.NumberMathTest,groovy.NumberMathTest,groovy.NumberMathTest,' + 
                        'groovy.NumberMathTest,groovy.RegularExpressionsTest,' + 
                        'groovy.NumberMathTest,groovy.NumberMathTest,groovy.NumberMathTest,groovy.NumberMathTest,' + 
                        'groovy.RegularExpressionsTest,groovy.NumberMathTest,groovy.NumberMathTest,' + 
                        'groovy.NumberMathTest,groovy.ValidNameTest,groovy.ValidNameTest,' + 
                        'org.codehaus.groovy.runtime.PerInstanceMetaClassTest,' + 
                        'groovy.NumberMathTest,groovy.NumberMathTest,groovy.NumberMathTest,groovy.NumberMathTest,' + 
                        'groovy.NumberMathTest,groovy.NumberMathTest,groovy.NumberMathTest,groovy.NumberMathTest,' + 
                        'gls.scope.NameResolvingTest,groovy.util.AntTest,groovy.sql.SqlCacheTest,' + 
                        'groovy.operator.NegateListsTest,org.codehaus.groovy.reflection.WeakMapTest,' + 
                        'groovy.RegularExpressionsTest,gls.scope.NameResolvingTest'
        }

        'MethodName' {
            doNotApplyToClassNames='groovy.swing.SwingBuilder,groovy.$Temp,groovy.bugs.Groovy4119Bug,' + 
                        'groovy.lang.MetaClassRegistryTest,groovy.lang.MixinTest,' + 
                        'groovy.swing.TitledBorderFactoryJustificationTest'
            doNotApplyToFilesMatching='.*spectralnorm\\.groovy'
        }
        'ParameterName' {
            doNotApplyToClassNames='groovy.bugs.MyDelegatingMetaClass'
            doNotApplyToFilesMatching='.*spectralnorm\\.groovy'
        }
        'ConfusingMethodName'  {
            doNotApplyToClassNames='Groovy1059Foo'
        }
        exclude 'FactoryMethodName' // many violations
    }
    ruleset('rulesets/unused.xml') {
        'UnusedVariable' {
            doNotApplyToClassNames = 'SourceBaseTestCase,SAXTest,groovy.ForLoopTest,groovy.bugs.Groovy3894Bug,' +
                'ExpandoMetaClassTest,ExceptionTest,JSR223Test,' +
                'groovy.util.GroovyShellTestCase,org.codehaus.groovy.tools.shell.ShellRunner,' +
                'groovy.bugs.Bytecode7Bug,groovy.mock.interceptor.HalfMockTest,' +
                'groovy.mock.interceptor.MockSingleCallTest,groovy.mock.interceptor.StubSingleCallTest,' +
                'groovy.operator.TernaryOperatorsTest,groovy.swing.SwingBuilderTableTest,' +
                'groovy.swing.SwingBuilderTest,groovy.util.BuilderSupportTest,' +
                'groovy.util.GroovyScriptEngineTest,groovy.util.ObservableMapTest,' +
                'groovy.xml.NamespaceNodeGPathTest,groovy.bugs.ClassGeneratorFixesTest,' +
                'groovy.bugs.ClosureWithBitwiseDefaultParamTest,groovy.bugs.ConstructorThisCallBug,' +
                'groovy.bugs.InconsistentStackHeightBug,groovy.bugs.InterfaceImplBug,' +
                'groovy.bugs.TedsClosureBug,org.codehaus.groovy.ast.builder.AstBuilderFromCodeTest,' +
                'SubscriptTest,UnsafeNavigationTest,' +
                'GStringTest,GeneratorTest,GroovyClosureMethodsTest,' +
                'GroovyMethodsTest,ImmutableModificationTest,LittleClosureTest,' +
                'NumberMathTest,OptionalReturnTest,OverridePropertyGetterTest,' +
                'PrivateVariableAccessFromAnotherInstanceTest,PropertyTest,' +
                'gls.annotations.closures.CallOnOwner,gls.annotations.closures.JavaCompatibility,' +
                'gls.annotations.closures.CallOnThisObject,gls.annotations.closures.JavaCompatibilityParameterized,' +
                'gls.annotations.closures.UnqualifiedCall,groovy.CompileOrderTest,' +
                'gls.annotations.closures.ClosureWithParameters,EscapedUnicodeTest,' +
                'groovy.bugs.Groovy2365Base,groovy.bugs.Groovy249_Bug,' +
                'groovy.bugs.Groovy3139Bug,groovy.bugs.Get2,groovy.bugs.Groovy3511Bug,' +
                'org.codehaus.groovy.tools.LoaderConfigurationTest'
        }

        'UnusedPrivateField' {
            doNotApplyToClassNames='gls.annotations.closures.CallOnOwner,gls.annotations.closures.CallOnThisObject,' +
                'gls.annotations.closures.UnqualifiedCall,gls.annotations.closures.ClosureWithParameters,' + 
                'gls.annotations.closures.JavaCompatibility,gls.annotations.closures.JavaCompatibilityParameterized,' +
                'groovy.Foo,groovy.Singlet'
        }

        'UnusedPrivateMethod'  {
            doNotApplyToClassNames='org.codehaus.groovy.ast.builder.AstBuilder,org.codehaus.groovy.ast.builder.AstSpecificationCompiler,' + 
                'StringMethodName,Foo' 
        }

        'UnusedPrivateMethodParameter' {
            doNotApplyToClassNames='CurryFoo4170,AssertionRenderingTest'
        }

        'UnusedObject'  {
            doNotApplyToClassNames='groovy.ui.OutputTransforms,org.codehaus.groovy.ast.builder.AstSpecificationCompiler,groovy.lang.GroovyCodeSourceTest'        
        }
    }

    ruleset('rulesets/imports.xml') {
        'UnnecessaryGroovyImport' {
            doNotApplyToFileNames='JListProperties.groovy,GridBagFactory.groovy,Groovy558_616_Bug.groovy'
        }
        'DuplicateImport' {
            doNotApplyToFileNames='StaticImportTest.groovy,'
        }
        exclude 'ImportFromSamePackage'   // too many to worry about, review later
        exclude 'MisorderedStaticImports'   // too many to worry about, opposite to IDEA default
    }

    ruleset('rulesets/logging.xml') {
        exclude 'SystemOutPrint'  // too many to worry about, review later
        exclude 'SystemErrPrint'  // too many to worry about, review later
        'Println' {
            doNotApplyToFileNames='genArrayAccess.groovy,genArrayUtil.groovy,genDgmMath.groovy,genMathModification.groovy,'
        }
    }
    ruleset('rulesets/braces.xml') {
        exclude 'ForStatementBraces' // for statements without braces seems acceptable in our coding standards
        exclude 'IfStatementBraces' // if statements without braces seems acceptable in our coding standards
        exclude 'WhileStatementBraces' // while statements without braces seems acceptable in our coding standards
        exclude 'ElseBlockBraces'   // else statements without braces seems acceptable in our coding standards
    }
    ruleset('rulesets/basic.xml') {
        'DeadCode' {
            doNotApplyToClassNames='ThrowTest'
        }
    }
    ruleset('rulesets/size.xml') {
        exclude 'CyclomaticComplexity'  // too many to worry about, review later
        exclude 'AbcMetric'  // too many to worry about, review later
        exclude 'MethodSize'  // too many to worry about, review later
        exclude 'MethodCount'  // too many to worry about, review later
        exclude 'ClassSize'  // too many to worry about, review later
        exclude 'MethodSizeCount'  // too many to worry about, review later
        exclude 'NestedBlockDepth'  // too many to worry about, review later
    }
    ruleset('rulesets/junit.xml') {
        exclude 'JUnitStyleAssertions'      // too many to worry about, review later
        exclude 'JUnitTestMethodWithoutAssert'  // too many to worry about, review later
        exclude 'JUnitLostTest' //we do not use the @Test annotation, so it is o.k. to use JUnit 4 classes and have test* Methods without this annotation
    }

    ruleset('rulesets/concurrency.xml') {
        exclude 'SynchronizedMethod'    // OK within Groovy
        exclude 'WaitOutsideOfWhileLoop'   // too many to worry about, review later
        exclude 'SynchronizedOnThis'   // too many to worry about, review later
    }

    ruleset('rulesets/unnecessary.xml') {
        'UnnecessaryInstantiationToGetClass'  {
             doNotApplyToClassNames='SpreadDotTest'
        } 

        'ConsecutiveStringConcatenation' {
            doNotApplyToClassNames='groovy.DynamicMemberTest,groovy.StaticImportTest,groovy.bugs.Groovy675_Bug,groovy.bugs.MorgansBug,' + 
                        'groovy.bugs.WriteOnlyPropertyBug,groovy.operator.StringOperatorsTest,groovy.swing.SwingBuilderTest,' + 
                        'org.codehaus.groovy.runtime.powerassert.EvaluationTest,groovy.json.JsonLexerTest'
        }

        'ConsecutiveLiteralAppends' {
            doNotApplyToClassNames='groovy.bugs.ByteIndexBug'
        }

        exclude 'UnnecessaryPackageReference' // failing for all scripts
    }
    ruleset('rulesets/dry.xml') {
        exclude 'DuplicateNumberLiteral'    // too many to worry about, review later
        exclude 'DuplicateStringLiteral'    // too many to worry about, review later
    }
    ruleset('rulesets/design.xml') {
        'CloseWithoutCloseable' { 
            doNotApplyToClassNames='Log4jInterceptingAppender'
        }
        exclude 'EmptyMethodInAbstractClass'      // too many to worry about, review later
        exclude 'ImplementationAsType'      // too many to worry about, review later
        exclude 'AbstractClassWithoutAbstractMethod'     // too many to worry about, review later
        exclude 'ConstantsOnlyInterface'    // too many to worry about, review later
        exclude 'FinalClassWithProtectedMember'    // too many to worry about, review later
    }
    ruleset('rulesets/exceptions.xml') {
        exclude 'ThrowRuntimeException'    // too many to worry about, review later
        exclude 'ThrowException'    // too many to worry about, review later
        exclude 'CatchThrowable'    // too many to worry about, review later
        exclude 'CatchException'    // too many to worry about, review later
        exclude 'CatchRuntimeException'    // too many to worry about, review later
        exclude 'CatchNullPointerException'    // too many to worry about, review later
        exclude 'ReturnNullFromCatchBlock'    // too many to worry about, review later
        exclude 'ThrowNullPointerException'    // too many to worry about, review later
        exclude 'CatchIllegalMonitorStateException'    // too many to worry about, review later
        exclude 'CatchError'   // too many to worry about, review later
        exclude 'ExceptionExtendsError'   // too many to worry about, review later
        exclude 'ThrowError'   // too many to worry about, review later
    }
}
