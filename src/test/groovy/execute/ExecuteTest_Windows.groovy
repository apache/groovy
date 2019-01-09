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
package groovy.execute

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import static org.junit.Assume.assumeTrue

/**
 * Test to ensure that the execute mechanism works fine on Windows systems.
 * <p>
 * These test are a bit trivial but at least they are here :-)
 */
@RunWith(JUnit4)
class ExecuteTest_Windows extends GroovyTestCase {

  private static final boolean windows = System.properties['os.name'].toLowerCase().contains('windows')

  @Before
  void assumeUnixOrSolaris() {
    assumeTrue('Test requires Windows.', windows)
  }

  @Test
  void testCmdEchoOneArray() {
    def process = ( [ "cmd.exe" , "/c" , "echo 1" ] as String[] ).execute()
    process.waitFor()
    assert process.in.text.trim() == "1"
  }

  @Test
  void testCmdEchoOneList() {
    def process = [ "cmd.exe" , "/c" , "echo 1" ].execute ( )
    process.waitFor()
    assert process.in.text.trim() == "1"
  }

  @Test
  void testCmdDate() {
    def process = "cmd.exe /c date.exe /t".execute()
    process.waitFor()
    def theDate = process.in.text.trim()
    def minLen = 8
      // dk: the length depends on the locale settings and usually contains two digits for
      //     each day/month/year where the separation char may differ. This test may fail for
      //     locales with even shorter date representations. As soon as this happens, please
      //     adapt the minLen value.
    assert theDate.size() >= minLen, "Expected '$theDate' to be at least $minLen chars long"
  }

  @Test
  void testEchoOneArray() {
    try {
      def process = ( [ "echo 1" ] as String[] ).execute()
      process.waitFor()
      fail ( "Should have thrown java.io.IOException: echo 1: not found" )
    }
    catch ( IOException ioe ) { }
  }

  @Test
  void testEchoOneList() {
    try {
      def process = [ "echo 1" ].execute()
      process.waitFor()
      fail ( "Should have thrown java.io.IOException: echo 1: not found" )
    }
    catch ( IOException ioe ) { }
  }
}