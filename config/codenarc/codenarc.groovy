/*
 * Copyright 2010-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

        try {
            'UnusedPrivateMethodParameter' {
                doNotApplyToClassNames='CurryFoo4170,AssertionRenderingTest'
            } 
        } catch(AssertionError e) { /* only exists in 0.13 */ }

        'UnusedObject'  {
            doNotApplyToClassNames='groovy.ui.OutputTransforms,org.codehaus.groovy.ast.builder.AstSpecificationCompiler,groovy.lang.GroovyCodeSourceTest'        
        }
    }

    ruleset('rulesets/grails.xml')

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
        try {
            'LoggerForDifferentClass' {
                doNotApplyToClassNames='' // BUG in CodeNarc 0.13, add exclude for SwingBuilder only
            }
        } catch (AssertionError e) {
            // only in CodeNarc 0.13        
        }   

        exclude 'Println'  // too many to worry about, review later
        exclude 'SystemOutPrint'  // too many to worry about, review later
        exclude 'SystemErrPrint'    // too many to worry about, review later
        exclude 'PrintStackTrace'      // too many to worry about, review later
    }
    ruleset('rulesets/braces.xml') {
        exclude 'ForStatementBraces' // for statements without braces seems acceptable in our coding standards
        exclude 'IfStatementBraces' // if statements without braces seems acceptable in our coding standards
        exclude 'WhileStatementBraces' // while statements without braces seems acceptable in our coding standards
        exclude 'ElseBlockBraces'   // else statements without braces seems acceptable in our coding standards
    }
    ruleset('rulesets/basic.xml') {
    /* not available with gradle 0.9.2
        'ConsecutiveStringConcatenation' {
            doNotApplyToClassNames='groovy.DynamicMemberTest,groovy.StaticImportTest,groovy.bugs.Groovy675_Bug,groovy.bugs.MorgansBug,' + 
                        'groovy.bugs.WriteOnlyPropertyBug,groovy.operator.StringOperatorsTest,groovy.swing.SwingBuilderTest,' + 
                        'org.codehaus.groovy.runtime.powerassert.EvaluationTest' 
        } 
    */
        'DeadCode' {
            doNotApplyToClassNames='ThrowTest'
        }
    /* not available with gradle 0.9.2
        'ConsecutiveLiteralAppends' {
            doNotApplyToClassNames='groovy.bugs.ByteIndexBug'
        }
    */

        exclude 'ConstantTernaryExpression'    // too many to worry about, review later
        exclude 'GStringAsMapKey'    // too many to worry about, review later
        exclude 'SimpleDateFormatMissingLocale'    // too many to worry about, review later
        exclude 'ReturnsNullInsteadOfEmptyCollection'    // too many to worry about, review later
        exclude 'ExplicitGarbageCollection'    // too many to worry about, review later
        exclude 'DoubleNegative'    // too many to worry about, review later
        exclude 'ThrowExceptionFromFinallyBlock'    // too many to worry about, review later
        exclude 'InvertedIfElse'    // too many to worry about, review later
        exclude 'ExplicitCallToOrMethod'    // too many to worry about, review later
        exclude 'ExplicitCallToPowerMethod'    // too many to worry about, review later
        exclude 'CloneableWithoutClone'    // too many to worry about, review later
        exclude 'ConfusingTernary'    // too many to worry about, review later
        exclude 'ExplicitCallToEqualsMethod'    // too many to worry about, review later
        exclude 'ConstantIfExpression'    // too many to worry about, review later
        exclude 'ExplicitLinkedListInstantiation'    // too many to worry about, review later
        exclude 'ExplicitArrayListInstantiation'    // too many to worry about, review later
        exclude 'ExplicitStackInstantiation'    // too many to worry about, review later
        exclude 'ExplicitHashMapInstantiation'    // too many to worry about, review later
        exclude 'ExplicitTreeSetInstantiation'    // too many to worry about, review later
        exclude 'ExplicitCallToCompareToMethod'    // too many to worry about, review later
        exclude 'ExplicitCallToPlusMethod'    // too many to worry about, review later
        exclude 'ExplicitCallToLeftShiftMethod'    // too many to worry about, review later
        exclude 'ExplicitCallToAndMethod'    // too many to worry about, review later
        exclude 'ExplicitCallToGetAtMethod'    // too many to worry about, review later
        exclude 'ExplicitCallToMinusMethod'    // too many to worry about, review later
        exclude 'EmptyMethod'      // too many to worry about, review later
        exclude 'EmptyElseBlock'      // too many to worry about, review later
        exclude 'EmptyCatchBlock'      // too many to worry about, review later
        exclude 'EmptyCatchBlockRule'      // too many to worry about, review later
        exclude 'ReturnFromFinallyBlock'      // too many to worry about, review later
        exclude 'ReturnsNullInsteadOfEmptyArray'      // too many to worry about, review later
        exclude 'SerializableClassMustDefineSerialVersionUID'  // too many to worry about, review later
        exclude 'EmptyIfStatement'  // too many to worry about, review later
        exclude 'EmptyWhileStatement'  // too many to worry about, review later
        exclude 'EmptySynchronizedStatement'  // too many to worry about, review later
        exclude 'EqualsAndHashCode' // too many to worry about, review later
        exclude 'EmptyTryBlock' // too many to worry about, review later
        exclude 'EmptyFinallyBlock' // too many to worry about, review later
        exclude 'BrokenOddnessCheck' // too many to worry about, review later
        exclude 'AddEmptyString' // too many to worry about, review later

    }
    ruleset('rulesets/size.xml') {
        exclude 'CyclomaticComplexity'  // too many to worry about, review later
        exclude 'AbcComplexity'  // too many to worry about, review later
        exclude 'MethodSize'  // too many to worry about, review later
        exclude 'MethodCount'  // too many to worry about, review later
        exclude 'ClassSize'  // too many to worry about, review later
        exclude 'MethodSizeCount'  // too many to worry about, review later
        exclude 'NestedBlockDepth'  // too many to worry about, review later
    }
    ruleset('rulesets/junit.xml') {
        exclude 'UseAssertTrueInsteadOfAssertEquals'    // defect in CodeNarc 0.13

        exclude 'ChainedTest'  // too many to worry about, review later
        exclude 'UnnecessaryFail'  // too many to worry about, review later
        exclude 'JUnitUnnecessarySetUp'  // too many to worry about, review later
        exclude 'JUnitPublicNonTestMethod'  // too many to worry about, review later
        exclude 'JUnitStyleAssertions'      // too many to worry about, review later
        exclude 'JUnitTestMethodWithoutAssert'  // too many to worry about, review later
        exclude 'JUnitFailWithoutMessage'  // too many to worry about, review later
        exclude 'CoupledTestCase'   // too many to worry about, review later
        exclude 'JUnitSetUpCallsSuper'   // too many to worry about, review later
        exclude 'UseAssertEqualsInsteadOfAssertTrue'  // too many to worry about, review later
        exclude 'JUnitTearDownCallsSuper'  // too many to worry about, review later
        exclude 'UseAssertNullInsteadOfAssertEquals'   // too many to worry about, review later
        exclude 'JUnitAssertAlwaysFails'   // too many to worry about, review later
    }

    ruleset('rulesets/concurrency.xml') {
        exclude 'SynchronizedMethod'    // OK within Groovy
        exclude 'WaitOutsideOfWhileLoop'   // too many to worry about, review later
        exclude 'SynchronizedOnThis'   // too many to worry about, review later
    }

    ruleset('rulesets/unnecessary.xml') {
        exclude 'UnnecessaryObjectReferences'   // CodeNarc 0.12 has a bug

        'UnnecessaryInstantiationToGetClass'  {
             doNotApplyToClassNames='SpreadDotTest'
        } 

        exclude 'UnnecessaryNullCheck'   // too many to worry about, review later
        exclude 'UnnecessaryBooleanInstantiation'  // too many to worry about, review later
        exclude 'UnnecessaryReturnKeywordRule'  // too many to worry about, review later
        exclude 'UnnecessaryReturnKeyword'  // too many to worry about, review later
        exclude 'UnnecessaryGetter'    // too many to worry about, review later
        exclude 'UnnecessaryGString'    // too many to worry about, review later
        exclude 'UnnecessarySemicolon'  // too many to worry about, review later
        exclude 'UnnecessaryPublicModifier' // too many to worry about, review later
        exclude 'UnnecessaryDefInMethodDeclaration' // too many to worry about, review later
        exclude 'UnnecessaryBigDecimalInstantiation'    // too many to worry about, review later
        exclude 'UnnecessaryFloatInstantiation'    // too many to worry about, review later
        exclude 'UnnecessaryIntegerInstantiation'    // too many to worry about, review later
        exclude 'UnnecessaryLongInstantiation'    // too many to worry about, review later
        exclude 'UnnecessaryDoubleInstantiation'    // too many to worry about, review later
        exclude 'UnnecessaryBigIntegerInstantiation'    // too many to worry about, review later
        exclude 'UnnecessaryCollectCall'   // too many to worry about, review later
        exclude 'UnnecessaryConstructor'   // too many to worry about, review later
        exclude 'UnnecessaryBooleanExpression'   // too many to worry about, review later
        exclude 'UnnecessaryStringInstantiation' // too many to worry about, review later
        exclude 'UnnecessaryOverridingMethod' // too many to worry about, review later
        exclude 'UnnecessaryCallForLastElement' // too many to worry about, review later
        exclude 'UnnecessaryPackageReference' // failing for all scripts
    }
    ruleset('rulesets/dry.xml') {
        //exclude 'DuplicateNumberLiteral'    // BUG in CodeNarc 0.13. No way to exclude a float. 
        'DuplicateNumberLiteral' {
            ignoreNumbers = '-1,-1.0,-1000,0,1,2,3,3.0,4,5,5.0,7,8,8.0,6,9,10,11,12,12.2,15,15.0,16,20,22,24,33,34,40,42,48,55,77,81,97,99,100,111,119,123,200,255,256,456,10.0,1.0,0.0,2.0,27.0,999,1100,1200,1300,2222111'
        }            
        exclude 'DuplicateStringLiteralRule'    // too many to worry about, review later
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

