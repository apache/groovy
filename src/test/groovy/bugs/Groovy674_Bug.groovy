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
package bugs

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.fail

/**
 *  Test to ensure all the right exceptions are thrown for all the right/wrong combinations of
 *  parentheses and no parameters for print and println.
 */
class Groovy674_Bug {
  @Test
  void testTopLevelPrintParenthesesNoParameter ( ) {
    try { ( new GroovyShell ( ) ).evaluate ( "print ( )" ) }
    catch ( GroovyRuntimeException gre ) { return }
    fail ( "Should have thrown GroovyRuntimeException" ) ;
  }
  @Test
  void testTopLevelPrintlnParenthesesNoParameter ( ) {
    assertScript ( "println ( )" )
  }
  @Test
  void testClosurePrintParenthesesNoParameter ( ) {
    try { ( new GroovyShell ( ) ).evaluate ( "[ 1 , 2 , 3 , 4 , 5 ].each { print ( ) }" ) }
    catch ( GroovyRuntimeException gre ) { return }
    fail ( "Should have thrown GroovyRuntimeException" ) ;
  }
  @Test
  void testClosurePrintlnParenthesesNoParameter ( ) {
    assertScript ( "[ 1 , 2 , 3 , 4 , 5 ].each { println ( ) }" )
  }
  @Test
  void testTopLevelPrintNoParenthesesParameter ( ) { assertScript ( "print ( '' )" ) }
  @Test
  void testTopLevelPrintlnNoParenthesesParameter ( ) { assertScript ( "println ( '' )" ) }
  @Test
  void testClosurePrintNoParenthesesParameter ( ) { assertScript ( "[ 1 , 2 , 3 , 4 , 5 ].each { print ( '' ) }" ) }
  @Test
  void testClosurePrintlnNoParenthesesParameter ( ) { assertScript ( "[ 1 , 2 , 3 , 4 , 5 ].each { println ( '' ) }" ) }
  @Test
  void testTopLevelPrintParenthesesParameter ( ) { assertScript ( "print ''" ) }
  @Test
  void testTopLevelPrintlnParenthesesParameter ( ) { assertScript ( "println ''" ) }
  @Test
  void testClosurePrintParenthesesParameter ( ) { assertScript ( "[ 1 , 2 , 3 , 4 , 5 ].each { print '' }" ) }
  @Test
  void testClosurePrintlnParenthesesParameter ( ) { assertScript ( "[ 1 , 2 , 3 , 4 , 5 ].each { println '' }" ) }
  @Test
  void testTopLevelPrintProperty ( ) {
    try { ( new GroovyShell ( ) ).evaluate ( "print" ) }
    catch ( MissingPropertyException mpe ) { return ; }
    fail ( "Should have thrown MissingPropertyException" ) ;
  }
  @Test
  void testTopLevelPrintlnProperty  ( ) {
    try { ( new GroovyShell ( ) ).evaluate ( "println" ) }
    catch ( MissingPropertyException mpe ) { return ; }
    fail ( "Should have thrown MissingPropertyException" ) ;
  }
  @Test
  void testInClosurePrintProperty  ( ) {
    try { ( new GroovyShell ( ) ).evaluate ( "[ 1 , 2 , 3 , 4 , 5 ].each { print }" ) }
    catch ( MissingPropertyException mpe ) { return ; }
    fail ( "Should have thrown MissingPropertyException" ) ;
  }
  @Test
  void testInClosurePrintlnProperty  ( ) {
    try { ( new GroovyShell ( ) ).evaluate ( "[ 1 , 2 , 3 , 4 , 5 ].each { println }" ) }
    catch ( MissingPropertyException mpe ) { return ; }
    fail ( "Should have thrown MissingPropertyException" ) ;
  }
}
