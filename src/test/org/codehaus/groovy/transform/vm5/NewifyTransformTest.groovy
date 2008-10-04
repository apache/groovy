/*
 * Copyright 2008 the original author or authors.
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
package org.codehaus.groovy.transform.vm5

/**
 * @author Paul King
 */
class NewifyTransformTest extends GroovyShellTestCase {

    void testNewify() {
        def main = evaluate("""
              @Newify() class Main {
                  def field1 = Integer.new(42)
                  @Newify(Integer)
                  def field2 = Integer(43)
              }
              new Main()
        """)

        assertEquals main.field1, 42
        assertEquals main.field2, 43
    }
}