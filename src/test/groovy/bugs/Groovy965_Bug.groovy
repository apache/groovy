/*
 * Copyright 2003-2015 the original author or authors.
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
package groovy.bugs

/**
 * A test case to ensure that Groovy can compile class names and variable names with non-ASCII
 * characters and that non-ASCII characters in Strings do the right thing.
 * <p>
 * Unfortunately, we cannot actually have this test in the Subversion store since it
 * requires having an encoding.  Java internally uses UTF-16.  Most Linux/UNIX/Mac OS X users
 * use UTF-8 (or if they haven't caught up yet ISO-8859-{1..15], in Europe anyway).  Windows
 * internally is UTF-16 LE but it appears that the Europe region defaults to ISO-8859-1 which
 * is very silly.  All in all we cannot guarantee an encoding so we cannot have the tests.
 * <p>
 * If anyone spots any errors in the rationale or finds a way to fix things please update at will.
 *
 * @suthor Russel Winder
 */
class Groovy965_Bug extends GroovyTestCase {
  /* void test to avoid assertion failure because of the lack of test method in the class */
  void testVoid() {}
  /*
  void testUnicodeVariableNamesAndStrings ( ) {
    def âøñè = 'âøñè'
    assertEquals ( 'âøñè' , âøñè )
  }
  void testUnicodeMëthødName ( ) { }
  void testUnicodeClassName ( ) {
    def object = new Bläh ( ) 
    assert true
  }
  */
}

/*
class Bläh { }
*/
