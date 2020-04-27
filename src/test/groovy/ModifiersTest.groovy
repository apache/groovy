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
package groovy

import gls.CompilableTestSupport

class ModifiersTest extends CompilableTestSupport {

	public void testInterface() {
		// control
		shouldCompile("interface X {}")
		// erroneous
		shouldNotCompile("synchronized interface X {}")
	}

	public void testClass() {
		// control
		shouldCompile("public class X {}")
		shouldCompile """
            class X {
                private class Y {}
            }
        """
		// erroneous
		shouldNotCompile("public private class X {}")
		shouldNotCompile("synchronized class X {}")
		shouldNotCompile("private class X {}")
	}

	public void testMethodsShouldOnlyHaveOneVisibility() {
		// control
		shouldCompile("class X { private method() {} }")
		// erroneous
		shouldNotCompile("class X { private public method() {} }")
	}

	public void testFinalMethodParametersShouldNotBeModified() {
		// control
		shouldCompile("class X { private method(x) { x = 1 } }")
		// erroneous
		shouldNotCompile("class X { private method(final x) { x = 1 } }")
	}

	public void testMethodsShouldNotBeVolatile() {
		// control
		shouldCompile("class X { def method() {} }")
		// erroneous
		shouldNotCompile("class X { volatile method() {} }")
	}

	public void testInterfaceMethodsShouldNotBeSynchronizedNativeStrictfp() {
		// control
		shouldCompile("interface X { def method() }")
		// erroneous
		shouldNotCompile("interface X { native method() }")
		shouldNotCompile("interface X { synchronized method() }")
		shouldNotCompile("interface X { strictfp method() }")
	}

	public void testVariableInClass() {
		// control
		shouldCompile("class X { protected name }")
		// erroneous
		shouldNotCompile("class X { protected private name }")
	}

	public void testVariableInScript() {
		// control
		shouldCompile("def name")
		shouldCompile("var name")
		shouldCompile("val name")
		shouldCompile("let name")
		shouldCompile("String name")
		shouldCompile("final name")
		// erroneous
		shouldNotCompile("final val name")
		shouldNotCompile("final let name")
		shouldNotCompile("abstract name")
		shouldNotCompile("native name")
		shouldNotCompile("private name")
		shouldNotCompile("protected name")
		shouldNotCompile("public name")
		shouldNotCompile("static name")
		shouldNotCompile("strictfp name")
		shouldNotCompile("synchronized name")
		shouldNotCompile("transient name")
		shouldNotCompile("volatile name")
		shouldNotCompile("private protected name")
	}

	public void testInvalidModifiersOnConstructor() {
		// control
		shouldCompile("class Foo { Foo() {}}")
		// erroneous
		shouldNotCompile("class Foo { static Foo() {}}")
		shouldNotCompile("class Foo { final Foo() {}}")
		shouldNotCompile("class Foo { abstract Foo() {}}")
		shouldNotCompile("class Foo { native Foo() {}}")
	}

	public void testMethodsShouldNotAllowValOrLet() {
		shouldNotCompile("class X { let method() {} }")
		shouldNotCompile("class X { val method() {} }")
	}
}
