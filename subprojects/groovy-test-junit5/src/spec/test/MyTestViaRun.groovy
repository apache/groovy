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
import groovy.test.GroovyTestCase

class MyTestViaRun extends GroovyTestCase {
  void testJunit5ViaRun() {
    new GroovyShell().run '''
//@Grab('org.junit.jupiter:junit-jupiter-params:5.2.0')
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.stream.Stream
import static org.junit.jupiter.api.Assertions.assertTrue
import static org.junit.jupiter.api.DynamicTest.dynamicTest
import java.util.logging.ConsoleHandler
import java.util.logging.Level
import java.util.logging.Logger
import org.junit.platform.launcher.listeners.LoggingListener

// tag::junit5_test_part1[]
class MyTest {
// end::junit5_test_part1[]
  // tag::junit5_test_part3[]
  @BeforeAll
  static void init() {
    def logger = Logger.getLogger(LoggingListener.name)
    logger.level = Level.FINE
    logger.addHandler(new ConsoleHandler(level: Level.FINE))
  }
  // end::junit5_test_part3[]

// tag::junit5_test_part2[]
  @Test
  void streamSum() {
    assertTrue(Stream.of(1, 2, 3)
      .mapToInt(i -> i)
      .sum() > 5, () -> "Sum should be greater than 5")
  }

  @RepeatedTest(value=2, name = "{displayName} {currentRepetition}/{totalRepetitions}")
  void streamSumRepeated() {
    assert Stream.of(1, 2, 3).mapToInt(i -> i).sum() == 6
  }

  private boolean isPalindrome(s) { s == s.reverse()  }

  @ParameterizedTest                                                              // <1>
  @ValueSource(strings = [ "racecar", "radar", "able was I ere I saw elba" ])
  void palindromes(String candidate) {
    assert isPalindrome(candidate)
  }

  @TestFactory
  def dynamicTestCollection() {[
    dynamicTest("Add test") { -> assert 1 + 1 == 2 },
    dynamicTest("Multiply Test", () -> { assert 2 * 3 == 6 })
  ]}
}
// end::junit5_test_part2[]
''', "MyTest", []
  }
/*
// tag::junit5_test_output[]
JUnit5 launcher: passed=8, failed=0, skipped=0, time=246ms
// end::junit5_test_output[]
*/
}
