/*
 * Copyright 2010-2011 the original author or authors.
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
    ruleset('rulesets/imports.xml') {

        exclude 'DuplicateImport'   // too many to worry about, review later
        exclude 'UnnecessaryGroovyImport'   // too many to worry about, review later
        exclude 'UnnecessaryGroovyImportRule'   // too many to worry about, review later
        exclude 'ImportFromSamePackage'   // too many to worry about, review later
        exclude 'UnusedImport'   // too many to worry about, review later
    }

    ruleset('rulesets/naming.xml') {
        exclude 'PackageName' {
            regex = '^[a-z]([a-zA-Z\\.0-9])*\\b'
        } 

        exclude 'ClassName'  // TODO: regexs are correct, but need exclude list
//        'ClassName' {
//            regex = '^[A-Z]([a-zA-Z0-9$_])*\\b'
//        }
        exclude 'FieldName'  // TODO: regexs are correct, but need exclude list
//        'FieldName' {
//            regex = '^[a-z]([a-zA-Z0-9$])*\\b'
//            finalRegex = '^[a-z]([a-zA-Z0-9$])*\\b'
//            staticFinalRegex = '^[A-Z]([A-Z0-9$_])*\\b|^serialVersionUID\\b'
//            staticRegex = '^[A-Z]([A-Z0-9$_])*\\b'
//        }
        exclude 'PropertyName'  // TODO: regexs are correct, but need exclude list
//        'PropertyName' {
//            regex = '^[a-z]([a-zA-Z0-9$])*\\b'
//            finalRegex = '^[a-z]([a-zA-Z0-9$])*\\b'
//            staticFinalRegex = '^[A-Z]([A-Z0-9$_])*\\b|^serialVersionUID\\b'
//            staticRegex = '^[A-Z]([A-Z0-9$_])*\\b'
//        }
        exclude 'VariableName'
//        'VariableName' {
//            regex = '^[a-z]([a-zA-Z0-9$])*\\b'
//            finalRegex = '^[a-z]([a-zA-Z0-9$])*\\b'
//        }
        exclude 'MethodName' // TODO: regexs are correct, but need exclude list
        exclude 'ParameterName' // figure out correct regex
        exclude 'ConfusingMethodName'  // too many to worry about, review later
    }
    ruleset('rulesets/grails.xml')
    ruleset('rulesets/logging.xml') {
        exclude 'LoggerForDifferentClass'   // BUG in CodeNarc 0.13, add exclude for SwingBuilder only

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
        exclude 'ConsecutiveStringConcatenation'  // defect in CodeNarc 0.13    

        exclude 'ConstantTernaryExpression'    // too many to worry about, review later
        exclude 'GStringAsMapKey'    // too many to worry about, review later
        exclude 'SimpleDateFormatMissingLocale'    // too many to worry about, review later
        exclude 'ReturnsNullInsteadOfEmptyCollection'    // too many to worry about, review later
        exclude 'ExplicitGarbageCollection'    // too many to worry about, review later
        exclude 'DoubleNegative'    // too many to worry about, review later
        exclude 'ThrowExceptionFromFinallyBlock'    // too many to worry about, review later
        exclude 'InvertedIfElse'    // too many to worry about, review later
        exclude 'InvertedIfElse'    // too many to worry about, review later
        exclude 'ExplicitCallToOrMethod'    // too many to worry about, review later
        exclude 'ExplicitCallToPowerMethod'    // too many to worry about, review later
        exclude 'ConsecutiveLiteralAppends'    // too many to worry about, review later
        exclude 'DeadCode'    // too many to worry about, review later
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

        exclude 'UseAssertTrueInsteadOfAssertEquals'    // defect in CodeNarc 0.13
    }

    ruleset('rulesets/concurrency.xml') {
        exclude 'SynchronizedMethod'    // OK within Groovy
        exclude 'WaitOutsideOfWhileLoop'   // too many to worry about, review later
        exclude 'SynchronizedOnThis'   // too many to worry about, review later
    }

    ruleset('rulesets/unnecessary.xml') {
        exclude 'UnnecessaryObjectReferences'   // CodeNarc 0.12 has a bug

        exclude 'UnnecessaryBooleanInstantiation'  // too many to worry about, review later
        exclude 'UnnecessaryNullCheckBeforeInstanceOf'  // too many to worry about, review later
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
        exclude 'UnnecessaryInstantiationToGetClass'   // too many to worry about, review later
        exclude 'UnnecessaryNullCheck'   // too many to worry about, review later
        exclude 'UnnecessaryStringInstantiation' // too many to worry about, review later
        exclude 'UnnecessaryOverridingMethod' // too many to worry about, review later
        exclude 'UnnecessaryCallForLastElement' // too many to worry about, review later
    }
    ruleset('rulesets/dry.xml') {
        exclude 'DuplicateNumberLiteral'    // BUG in CodeNarc 0.13. No way to exclude a float. 
        //'DuplicateNumberLiteral' {
        //    ignoreNumbers = '0,1,2,3,4,5,6,9,10,11,12,16,24,33,34,48,55,77,97,100,123,456,10.0,1.0,0.0,2.0,-1,27.0'
        //}            
        exclude 'DuplicateStringLiteralRule'    // too many to worry about, review later
        exclude 'DuplicateStringLiteral'    // too many to worry about, review later
    }
    ruleset('rulesets/design.xml') {
        exclude 'CloseWithoutCloseable'   // too many to worry about, review later
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
    ruleset('rulesets/unused.xml') {
        exclude 'UnusedObject'  // too many to worry about, review later
        exclude 'UnusedVariableRule'  // too many to worry about, review later
        exclude 'UnusedVariable'  // too many to worry about, review later
        exclude 'UnusedPrivateField'  // too many to worry about, review later
        exclude 'UnusedPrivateMethod'  // too many to worry about, review later
        exclude 'UnusedPrivateMethodParameter'  // too many to worry about, review later
    }
}

