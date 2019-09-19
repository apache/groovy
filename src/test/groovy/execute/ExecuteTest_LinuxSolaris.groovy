#! /usr/bin/env groovy

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

import groovy.test.GroovyTestCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import static org.junit.Assume.assumeTrue

/**
 * Test to ensure that the execute mechanism works fine on *nix-like systems.  For these OSs we
 * can effectively guarantee the existence of some programs that we can run.  Assume the search
 * path is partway reasonable so we can access sh and echo.
 * <p>
 * These test are a bit trivial but at least they are here :-)
 */
@RunWith(JUnit4)
class ExecuteTest_LinuxSolaris extends GroovyTestCase {

  private static final boolean linuxOrSolaris = System.getProperty('os.name').contains('Linux') ||
          System.getProperty('os.name').contains('sunos')

  @Before
  void assumeUnixOrSolaris() {
    assumeTrue('Test requires Linux or Solaris.', linuxOrSolaris)
  }

  @Test
  void testShellEchoOneArray ( ) {
    def process = ( [ "sh" , "-c" , "echo 1" ] as String[] ).execute ( )
    process.waitFor ( )
    assert process.in.text.trim ( ) == "1"
  }

  @Test
  void testShellEchoOneList ( ) {
    def process = [ "sh" , "-c" , "echo 1" ].execute ( )
    process.waitFor ( )
    assert process.in.text.trim ( ) == "1"
  }

  @Test
  void testEchoOneArray ( ) {
    try {
      def process = ( [ "echo 1" ] as String[] ).execute ( )
      process.waitFor ( )
       fail ( "Should have thrown java.io.IOException: echo 1: not found" )
    }
    catch ( IOException ioe ) { }
  }

  @Test
  void testEchoOneList ( ) {
    try {
      def process = [ "echo 1" ].execute ( )
      process.waitFor ( )
       fail ( "Should have thrown java.io.IOException: echo 1: not found" )
    }
    catch ( IOException ioe ) { }
  }

  @Test
  void testEchoOneScalar ( ) {
    def process = "echo 1".execute ( )
    process.waitFor ( )
    assert process.in.text.trim ( ) == "1"
  }

  @Test
  void testEchoArray ( ) {
    def process = ( [ "echo" , "1" ] as String[] ).execute ( )
    process.waitFor ( )
    assert process.in.text.trim ( ) == "1"
   }

  @Test
  void testEchoList ( ) {
    def process = [ "echo" , "1" ].execute ( )
    process.waitFor ( )
    assert process.in.text.trim ( ) == "1"
   }
}
