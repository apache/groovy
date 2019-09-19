import groovy.test.GroovyTestCase

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
class SaferScriptingASTTransformsTest extends GroovyTestCase {
    void testThreadInterrupt() {
        assertScript '''import groovy.transform.ThreadInterrupt
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer

// tag::threadinterrupt_shell_setup[]
def config = new CompilerConfiguration()
config.addCompilationCustomizers(
        new ASTTransformationCustomizer(ThreadInterrupt)
)
def binding = new Binding(i:0)
def shell = new GroovyShell(binding,config)
// end::threadinterrupt_shell_setup[]

def userCode = """
// tag::threadinterrupt_infiniteloop[]
while (true) {
    i++
}
// end::threadinterrupt_infiniteloop[]
/*
// tag::threadinterrupt_infiniteloop_equiv[]
while (true) {
    if (Thread.currentThread().interrupted) {
        throw new InterruptedException('The current thread has been interrupted.')
    }
    i++
}
// end::threadinterrupt_infiniteloop_equiv[]
*/
"""

// tag::threadinterrupt_control[]
def t = Thread.start {
    shell.evaluate(userCode)
}
t.join(1000) // give at most 1000ms for the script to complete
if (t.alive) {
    t.interrupt()
}
// end::threadinterrupt_control[]
assert binding.i > 0'''
    }

    void testThreadInterruptThrown() {

        assertScript '''import groovy.transform.ThreadInterrupt
import groovy.transform.InheritConstructors
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer


// tag::threadinterrupt_thrown[]
class BadException extends Exception {
    BadException(String message) { super(message) }
}

def config = new CompilerConfiguration()
config.addCompilationCustomizers(
        new ASTTransformationCustomizer(thrown:BadException, ThreadInterrupt)
)
def binding = new Binding(i:0)
def shell = new GroovyShell(this.class.classLoader,binding,config)

def userCode = """
try {
    while (true) {
        i++
    }
} catch (BadException e) {
    i = -1
}
"""

def t = Thread.start {
    shell.evaluate(userCode)
}
t.join(1000) // give at most 1s for the script to complete
assert binding.i > 0
if (t.alive) {
    t.interrupt()
}
Thread.sleep(500)
assert binding.i == -1'''
// end::threadinterrupt_thrown[]
   }

    void testTimedInterrupt() {
        assertScript '''import groovy.transform.TimedInterrupt
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer

// tag::timedinterrupt_shell_setup[]
def config = new CompilerConfiguration()
config.addCompilationCustomizers(
        new ASTTransformationCustomizer(value:1, TimedInterrupt)
)
def binding = new Binding(result:0)
def shell = new GroovyShell(this.class.classLoader, binding,config)
// end::timedinterrupt_shell_setup[]

def userCode = """
// tag::timedinterrupt_fib[]
def fib(int n) { n<2?n:fib(n-1)+fib(n-2) }

result = fib(600)
// end::timedinterrupt_fib[]
/*
// tag::timedinterrupt_fib_equiv[]
def fib(int n) {
    if (System.nanoTime()>_expireDate) {
        throw new TimeoutException('Timeout exceeded')
    }
    n<2?n:fib(n-1)+fib(n-2)
}
// end::timedinterrupt_fib_equiv[]
*/
"""

// tag::timedinterrupt_control[]
def t = Thread.start {
    shell.evaluate(userCode)
}
t.join(5000)
assert !t.alive
// end::timedinterrupt_control[]
assert binding.result == 0'''
    }

    void testTimedInterruptDuration() {
        assertScript '''import groovy.transform.TimedInterrupt

import java.util.concurrent.TimeUnit

// tag::timedinterrupt_duration[]
@TimedInterrupt(value=500L, unit= TimeUnit.MILLISECONDS, applyToAllClasses = false)
class Slow {
    def fib(n) { n<2?n:fib(n-1)+fib(n-2) }
}
def result
def t = Thread.start {
    result = new Slow().fib(500)
}
t.join(5000)
assert result == null
assert !t.alive
// end::timedinterrupt_duration[]
'''
    }

    void testTimedInterruptThrown() {
        assertScript '''import groovy.transform.InheritConstructors
import groovy.transform.TimedInterrupt

import java.util.concurrent.TimeUnit

@InheritConstructors class TooLongException extends Exception {}

// tag::timedinterrupt_thrown[]
@TimedInterrupt(thrown=TooLongException, applyToAllClasses = false, value=1L)
class Slow {
    def fib(n) { Thread.sleep(100); n<2?n:fib(n-1)+fib(n-2) }
}
def result
def t = Thread.start {
    try {
        result = new Slow().fib(50)
    } catch (TooLongException e) {
        result = -1
    }
}
t.join(5000)
assert result == -1
// end::timedinterrupt_thrown[]
'''
    }

    void testConditionalInterrupt() {
        assertScript '''import groovy.transform.ConditionalInterrupt
// tag::conditionalinterrupt_quotaclass[]
class Quotas {
    static def quotas = [:].withDefault { 10 }
    static boolean disallow(String userName) {
        println "Checking quota for $userName"
        (quotas[userName]--)<0
    }
}
// end::conditionalinterrupt_quotaclass[]

// tag::conditionalinterrupt[]
@ConditionalInterrupt({Quotas.disallow('user')})
class UserCode {
    void doSomething() {
        int i=0
        while (true) {
            println "Consuming resources ${++i}"
        }
    }
}
// end::conditionalinterrupt[]

// tag::conditionalinterrupt_assert[]
assert Quotas.quotas['user'] == 10
def t = Thread.start {
    new UserCode().doSomething()
}
t.join(5000)
assert !t.alive
assert Quotas.quotas['user'] < 0
// end::conditionalinterrupt_assert[]
'''
    }

    void testConditionalInterruptInjected() {
        assertScript '''import groovy.transform.ConditionalInterrupt
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer

class Quotas {
    static def quotas = [:].withDefault { 10 }
    static boolean disallow(String userName) {
        println "Checking quota for $userName"
        (quotas[userName]--)<0
    }
}

// tag::conditionalinterrupt_injected[]
def config = new CompilerConfiguration()
def checkExpression = new ClosureExpression(
        Parameter.EMPTY_ARRAY,
        new ExpressionStatement(
                new MethodCallExpression(new ClassExpression(ClassHelper.make(Quotas)), 'disallow', new ConstantExpression('user'))
        )
)
config.addCompilationCustomizers(
        new ASTTransformationCustomizer(value: checkExpression, ConditionalInterrupt)
)

def shell = new GroovyShell(this.class.classLoader,new Binding(),config)

def userCode = """
        int i=0
        while (true) {
            println "Consuming resources \\${++i}"
        }
"""

assert Quotas.quotas['user'] == 10
def t = Thread.start {
    shell.evaluate(userCode)
}
t.join(5000)
assert !t.alive
assert Quotas.quotas['user'] < 0
// end::conditionalinterrupt_injected[]
'''
    }
    void testConditionalInterruptThrown() {
        assertScript '''import groovy.transform.ConditionalInterrupt
import groovy.transform.InheritConstructors
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer

@InheritConstructors
class QuotaExceededException extends Exception {}

class Quotas {
    static def quotas = [:].withDefault { 10 }
    static boolean disallow(String userName) {
        println "Checking quota for $userName"
        (quotas[userName]--)<0
    }
}

def userCode = """
        int i=0
        while (true) {
            println "Consuming resources \\${++i}"
        }
"""
def config = new CompilerConfiguration()
def checkExpression = new ClosureExpression(
        Parameter.EMPTY_ARRAY,
        new ExpressionStatement(
                new MethodCallExpression(new ClassExpression(ClassHelper.make(Quotas)), 'disallow', new ConstantExpression('user'))
        )
)
def shell = new GroovyShell(this.class.classLoader,new Binding(),config)

// tag::conditionalinterrupt_thrown[]
config.addCompilationCustomizers(
        new ASTTransformationCustomizer(thrown: QuotaExceededException,value: checkExpression, ConditionalInterrupt)
)
assert Quotas.quotas['user'] == 10
def t = Thread.start {
    try {
        shell.evaluate(userCode)
    } catch (QuotaExceededException) {
        Quotas.quotas['user'] = 'Quota exceeded'
    }
}
t.join(5000)
assert !t.alive
assert Quotas.quotas['user'] == 'Quota exceeded'
// end::conditionalinterrupt_thrown[]
'''
    }

}
