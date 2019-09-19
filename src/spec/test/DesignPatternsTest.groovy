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
import gls.CompilableTestSupport

class DesignPatternsTest extends CompilableTestSupport {

    void testAbstractFactory() {
        shouldCompile '''
            // tag::abstract_factory_example1[]
            class TwoupMessages {
                def welcome = 'Welcome to the twoup game, you start with $1000'
                def done = 'Sorry, you have no money left, goodbye'
            }

            class TwoupInputConverter {
                def convert(input) { input.toInteger() }
            }

            class TwoupControl {
                private money = 1000
                private random = new Random()
                private tossWasHead() {
                    def next = random.nextInt()
                    return next % 2 == 0
                }
                def moreTurns() {
                    if (money > 0) {
                        println "You have $money, how much would you like to bet?"
                        return true
                    }

                    false
                }
                def play(amount) {
                    def coin1 = tossWasHead()
                    def coin2 = tossWasHead()
                    if (coin1 && coin2) {
                        money += amount
                        println 'You win'
                    } else if (!coin1 && !coin2) {
                        money -= amount
                        println 'You lose'
                    } else {
                        println 'Draw'
                    }
                }
            }
            // end::abstract_factory_example1[]

            // tag::abstract_factory_example2[]
            class GuessGameMessages {
                def welcome = 'Welcome to the guessing game, my secret number is between 1 and 100'
                def done = 'Correct'
            }

            class GuessGameInputConverter {
                def convert(input) { input.toInteger() }
            }

            class GuessGameControl {
                private lower = 1
                private upper = 100
                private guess = new Random().nextInt(upper - lower) + lower
                def moreTurns() {
                    def done = (lower == guess || upper == guess)
                    if (!done) {
                        println "Enter a number between $lower and $upper"
                    }

                    !done
                }
                def play(nextGuess) {
                    if (nextGuess <= guess) {
                        lower = [lower, nextGuess].max()
                    }
                    if (nextGuess >= guess) {
                        upper = [upper, nextGuess].min()
                    }
                }
            }
            // end::abstract_factory_example2[]

            // tag::abstract_factory_code[]
            def guessFactory = [messages: GuessGameMessages, control: GuessGameControl, converter: GuessGameInputConverter]
            def twoupFactory = [messages: TwoupMessages, control: TwoupControl, converter: TwoupInputConverter]

            class GameFactory {
                def static factory
                def static getMessages() { return factory.messages.newInstance() }
                def static getControl() { return factory.control.newInstance() }
                def static getConverter() { return factory.converter.newInstance() }
            }
            // end::abstract_factory_code[]

            // tag::abstract_factory_usage[]
            GameFactory.factory = twoupFactory
            def messages = GameFactory.messages
            def control = GameFactory.control
            def converter = GameFactory.converter
            println messages.welcome
            def reader = new BufferedReader(new InputStreamReader(System.in))
            while (control.moreTurns()) {
                def input = reader.readLine().trim()
                control.play(converter.convert(input))
            }
            println messages.done
            // end::abstract_factory_usage[]
        '''
    }

    void testAdapterDelegation() {
        shouldCompile '''
            // tag::adapter_delegation_classes[]
            class SquarePeg {
                def width
            }

            class RoundPeg {
                def radius
            }

            class RoundHole {
                def radius
                def pegFits(peg) {
                    peg.radius <= radius
                }
                String toString() { "RoundHole with radius $radius" }
            }
            // end::adapter_delegation_classes[]

            // tag::adapter_delegation_code[]
            class SquarePegAdapter {
                def peg
                def getRadius() {
                    Math.sqrt(((peg.width / 2) ** 2) * 2)
                }
                String toString() {
                    "SquarePegAdapter with peg width $peg.width (and notional radius $radius)"
                }
            }
            // end::adapter_delegation_code[]

            // tag::adapter_delegation_usage[]
            def hole = new RoundHole(radius: 4.0)
            (4..7).each { w ->
                def peg = new SquarePegAdapter(peg: new SquarePeg(width: w))
                if (hole.pegFits(peg)) {
                    println "peg $peg fits in hole $hole"
                } else {
                    println "peg $peg does not fit in hole $hole"
                }
            }
            // end::adapter_delegation_usage[]
        '''
    }

    void testAdapterInheritanceClosuresExpandoMetaClass() {
        shouldCompile '''
            // tag::adapter_inheritance_classes[]
            class SquarePeg {
                def width
            }

            class RoundPeg {
                def radius
            }

            class RoundHole {
                def radius
                def pegFits(peg) {
                    peg.radius <= radius
                }
                String toString() { "RoundHole with radius $radius" }
            }
            // end::adapter_inheritance_classes[]

            // tag::adapter_inheritance_code[]
            class SquarePegAdapter extends SquarePeg {
                def getRadius() {
                    Math.sqrt(((width / 2) ** 2) * 2)
                }
                String toString() {
                    "SquarePegAdapter with width $width (and notional radius $radius)"
                }
            }
            // end::adapter_inheritance_code[]

            // tag::adapter_inheritance_usage[]
            def hole = new RoundHole(radius: 4.0)
            (4..7).each { w ->
                def peg = new SquarePegAdapter(width: w)
                if (hole.pegFits(peg)) {
                    println "peg $peg fits in hole $hole"
                } else {
                    println "peg $peg does not fit in hole $hole"
                }
            }
            // end::adapter_inheritance_usage[]

            // tag::adapter_closure_interface[]
            interface RoundThing {
                def getRadius()
            }
            // end::adapter_closure_interface[]

            // tag::adapter_closure_define[]
            def adapter = {
                p -> [getRadius: { Math.sqrt(((p.width / 2) ** 2) * 2) }] as RoundThing
            }
            // end::adapter_closure_define[]

            def isoHelper = {
                // tag::adapter_closure_usage[]
                def peg = new SquarePeg(width: 4)
                if (hole.pegFits(adapter(peg))) {
                    // ... as before
                }
                // end::adapter_closure_usage[]
            }

            def isoHelper2 = {
                // tag::adapter_expando_meta_class[]
                def peg = new SquarePeg(width: 4)
                peg.metaClass.radius = Math.sqrt(((peg.width / 2) ** 2) * 2)
                // end::adapter_expando_meta_class[]
            }
        '''
    }

    void testBouncerNullCheck() {
        shouldCompile '''
            // tag::bouncer_null_check[]
            class NullChecker {
                static check(name, arg) {
                    if (arg == null) {
                        throw new IllegalArgumentException(name + ' is null')
                    }
                }
            }
            // end::bouncer_null_check[]

            class Test1 {
                // tag::bouncer_null_check_usage[]
                void doStuff(String name, Object value) {
                    NullChecker.check('name', name)
                    NullChecker.check('value', value)
                    // do stuff
                }
                // end::bouncer_null_check_usage[]
            }

            class Test2 {
                // tag::bouncer_null_check_usage_groovy_way[]
                void doStuff(String name, Object value) {
                    assert name != null, 'name should not be null'
                    assert value != null, 'value should not be null'
                    // do stuff
                }
                // end::bouncer_null_check_usage_groovy_way[]
            }
        '''
    }

    void testBouncerValidation() {
        shouldCompile '''
            // tag::bouncer_validation[]
            class NumberChecker {
                static final String NUMBER_PATTERN = "\\\\d+(\\\\.\\\\d+(E-?\\\\d+)?)?"
                static isNumber(str) {
                    if (!str ==~ NUMBER_PATTERN) {
                        throw new IllegalArgumentException("Argument '$str' must be a number")
                    }
                }
                static isNotZero(number) {
                    if (number == 0) {
                        throw new IllegalArgumentException('Argument must not be 0')
                    }
                }
            }
            // end::bouncer_validation[]

            // tag::bouncer_validation_usage[]
            def stringDivide(String dividendStr, String divisorStr) {
                NumberChecker.isNumber(dividendStr)
                NumberChecker.isNumber(divisorStr)
                def dividend = dividendStr.toDouble()
                def divisor = divisorStr.toDouble()
                NumberChecker.isNotZero(divisor)
                dividend / divisor
            }

            println stringDivide('1.2E2', '3.0')
            // => 40.0
            // end::bouncer_validation_usage[]

            class Test {
                // tag::bouncer_validation_usage_groovy_way[]
                def stringDivide(String dividendStr, String divisorStr) {
                    assert dividendStr =~ NumberChecker.NUMBER_PATTERN
                    assert divisorStr =~ NumberChecker.NUMBER_PATTERN
                    def dividend = dividendStr.toDouble()
                    def divisor = divisorStr.toDouble()
                    assert divisor != 0, 'Divisor must not be 0'
                    dividend / divisor
                }
                // end::bouncer_validation_usage_groovy_way[]
            }
        '''
    }

    void testChainOfResponsibility() {
        shouldCompile '''
            // tag::chain_of_responsibility[]
            class UnixLister {
                private nextInLine
                UnixLister(next) { nextInLine = next }
                def listFiles(dir) {
                    if (System.getProperty('os.name') == 'Linux') {
                        println "ls $dir".execute().text
                    } else {
                        nextInLine.listFiles(dir)
                    }
                }
            }

            class WindowsLister {
                private nextInLine
                WindowsLister(next) { nextInLine = next }
                def listFiles(dir) {
                    if (System.getProperty('os.name') == 'Windows XP') {
                        println "cmd.exe /c dir $dir".execute().text
                    } else {
                        nextInLine.listFiles(dir)
                    }
                }
            }

            class DefaultLister {
                def listFiles(dir) {
                    new File(dir).eachFile { f -> println f }
                }
            }

            def lister = new UnixLister(new WindowsLister(new DefaultLister()))

            lister.listFiles('Downloads')
            // end::chain_of_responsibility[]
        '''
    }

    void testCompositeCode() {
        shouldCompile '''
            // tag::composite_code[]
            abstract class Component {
                def name
                def toString(indent) {
                    ("-" * indent) + name
                }
            }
                
            class Composite extends Component {
                private children = []
                def toString(indent) {
                    def s = super.toString(indent)
                    children.each { child ->
                        s += "\\n" + child.toString(indent + 1)
                    }
                    s
                }
                def leftShift(component) {
                    children << component
                }
            }

            class Leaf extends Component { }

            def root = new Composite(name: "root")
            root << new Leaf(name: "leaf A")
            def comp = new Composite(name: "comp B")
            root << comp
            root << new Leaf(name: "leaf C")
            comp << new Leaf(name: "leaf B1")
            comp << new Leaf(name: "leaf B2")
            println root.toString(0)
            // end::composite_code[]
        '''
    }

    void testDecoratorLogger() {
        shouldCompile '''
            // tag::decorator_logger_class[]
            class Logger {
                def log(String message) {
                    println message
                }
            }
            // end::decorator_logger_class[]

            // tag::decorator_traditional_classes[]
            class TimeStampingLogger extends Logger {
                private Logger logger
                TimeStampingLogger(logger) {
                    this.logger = logger
                }
                def log(String message) {
                    def now = Calendar.instance
                    logger.log("$now.time: $message")
                }
            }

            class UpperLogger extends Logger {
                private Logger logger
                UpperLogger(logger) {
                    this.logger = logger
                }
                def log(String message) {
                    logger.log(message.toUpperCase())
                }
            }
            // end::decorator_traditional_classes[]

            // tag::decorator_dynamic_behaviour_class[]
            class GenericLowerDecorator {
                private delegate
                GenericLowerDecorator(delegate) {
                    this.delegate = delegate
                }
                def invokeMethod(String name, args) {
                    def newargs = args.collect { arg ->
                        if (arg instanceof String) {
                            return arg.toLowerCase()
                        } else {
                            return arg
                        }
                    }
                    delegate.invokeMethod(name, newargs)
                }
            }
            // end::decorator_dynamic_behaviour_class[]

            def separateHelper = {
                // tag::decorator_traditional_usage[]
                def logger = new UpperLogger(new TimeStampingLogger(new Logger()))
                logger.log("G'day Mate")
                // => Tue May 22 07:13:50 EST 2007: G'DAY MATE
                // end::decorator_traditional_usage[]

                // tag::decorator_traditional_usage2[]
                logger = new TimeStampingLogger(new UpperLogger(new Logger()))
                logger.log('Hi There')
                // => TUE MAY 22 07:13:50 EST 2007: HI THERE
                // end::decorator_traditional_usage2[]

                // tag::decorator_dynamic_behaviour_usage[]
                logger = new GenericLowerDecorator(new TimeStampingLogger(new Logger()))
                logger.log('IMPORTANT Message')
                // => Tue May 22 07:27:18 EST 2007: important message
                // end::decorator_dynamic_behaviour_usage[]
            }

            // tag::decorator_runtime_behaviour[]
            // current mechanism to enable ExpandoMetaClass
            GroovySystem.metaClassRegistry.metaClassCreationHandle = new ExpandoMetaClassCreationHandle()

            def logger = new Logger()
            logger.metaClass.log = { String m -> println 'message: ' + m.toUpperCase() }
            logger.log('x')
            // => message: X
            // end::decorator_runtime_behaviour[]
        '''
    }

    void testDecoratorCalc() {
        shouldCompile '''
            // tag::decorator_calc_class[]
            class Calc {
                def add(a, b) { a + b }
            }
            // end::decorator_calc_class[]

            // tag::decorator_tracing_decorator[]
            class TracingDecorator {
                private delegate
                TracingDecorator(delegate) {
                    this.delegate = delegate
                }
                def invokeMethod(String name, args) {
                    println "Calling $name$args"
                    def before = System.currentTimeMillis()
                    def result = delegate.invokeMethod(name, args)
                    println "Got $result in ${System.currentTimeMillis()-before} ms"
                    result
                }
            }
            // end::decorator_tracing_decorator[]

            // tag::decorator_tracing_decorator_usage[]
            def tracedCalc = new TracingDecorator(new Calc())
            assert 15 == tracedCalc.add(3, 12)
            // end::decorator_tracing_decorator_usage[]

            // tag::decorator_interceptor[]
            class TimingInterceptor extends TracingInterceptor {
                private beforeTime
                def beforeInvoke(object, String methodName, Object[] arguments) {
                    super.beforeInvoke(object, methodName, arguments)
                    beforeTime = System.currentTimeMillis()
                }
                Object afterInvoke(Object object, String methodName, Object[] arguments, Object result) {
                    super.afterInvoke(object, methodName, arguments, result)
                    def duration = System.currentTimeMillis() - beforeTime
                    writer.write("Duration: $duration ms\\n")
                    writer.flush()
                    result
                }
            }
            // end::decorator_interceptor[]

            // tag::decorator_interceptor_usage[]
            def proxy = ProxyMetaClass.getInstance(Calc)
            proxy.interceptor = new TimingInterceptor()
            proxy.use {
                assert 7 == new Calc().add(1, 6)
            }
            // end::decorator_interceptor_usage[]
        '''
    }

    void testDecoratorSql() {
        shouldCompile '''
            @Grab('org.codehaus.groovy:groovy-sql:2.1.6')
            import groovy.sql.Sql
            import java.lang.reflect.InvocationHandler
            import java.sql.Connection

            // tag::decorator_reflect_proxy[]
            protected Sql getGroovySql() {
                final Connection con = session.connection()
                def invoker = { object, method, args ->
                    if (method.name == "close") {
                        log.debug("ignoring call to Connection.close() for use by groovy.sql.Sql")
                    } else {
                        log.trace("delegating $method")
                        return con.invokeMethod(method.name, args)
                    }
                } as InvocationHandler;
                def proxy = Proxy.newProxyInstance( getClass().getClassLoader(), [Connection] as Class[], invoker )
                return new Sql(proxy)
            }
            // end::decorator_reflect_proxy[]
        '''
    }

    void testDecoratorSpring() {
        shouldCompile '''
            // tag::decorator_spring_calc[]
            interface Calc {
                def add(a, b)
            }
            // end::decorator_spring_calc[]

            // tag::decorator_spring_calc_impl[]
            class CalcImpl implements Calc {
                def add(a, b) { a + b }
            }
            // end::decorator_spring_calc_impl[]

            // tag::decorator_spring_context[]
            @Grab('org.springframework:spring-context:3.2.2.RELEASE')
            import org.springframework.context.support.ClassPathXmlApplicationContext

            def ctx = new ClassPathXmlApplicationContext('beans.xml')
            def calc = ctx.getBean('calc')
            println calc.add(3, 25)
            // end::decorator_spring_context[]
        '''
    }

    void testDecoratorGpars() {
        try {
            shouldCompile '''
                // tag::decorator_gpars[]
                @Grab('org.codehaus.gpars:gpars:0.10')
                import static groovyx.gpars.GParsPool.withPool

                interface Document {
                    void print()
                    String getText()
                }

                class DocumentImpl implements Document {
                    def document
                    void print() { println document }
                    String getText() { document }
                }

                def words(String text) {
                    text.replaceAll('[^a-zA-Z]', ' ').trim().split("\\\\s+")*.toLowerCase()
                }

                def avgWordLength = {
                    def words = words(it.text)
                    sprintf "Avg Word Length: %4.2f", words*.size().sum() / words.size()
                }
                def modeWord = {
                    def wordGroups = words(it.text).groupBy {it}.collectEntries { k, v -> [k, v.size()] }
                    def maxSize = wordGroups*.value.max()
                    def maxWords = wordGroups.findAll { it.value == maxSize }
                    "Mode Word(s): ${maxWords*.key.join(', ')} ($maxSize occurrences)"
                }
                def wordCount = { d -> "Word Count: " + words(d.text).size() }

                def asyncDecorator(Document d, Closure c) {
                    ProxyGenerator.INSTANCE.instantiateDelegate([print: {
                        withPool {
                            def result = c.callAsync(d)
                            d.print()
                            println result.get()
                        }
                    }], [Document], d)
                }

                Document d = asyncDecorator(asyncDecorator(asyncDecorator(
                        new DocumentImpl(document:"This is the file with the words in it\\n\\t\\nDo you see the words?\\n"),
                //        new DocumentImpl(document: new File('AsyncDecorator.groovy').text),
                        wordCount), modeWord), avgWordLength)
                d.print()
                // end::decorator_gpars[]
            '''
        } catch (UnsupportedClassVersionError e) {
            // running on an older JDK
        }
    }

    void testDelegationExpandoMetaClass() {
        shouldCompile '''
            // tag::delegation_delegator[]
            class Delegator {
                private targetClass
                private delegate
                Delegator(targetClass, delegate) {
                    this.targetClass = targetClass
                    this.delegate = delegate
                }
                def delegate(String methodName) {
                    delegate(methodName, methodName)
                }
                def delegate(String methodName, String asMethodName) {
                    targetClass.metaClass."$asMethodName" = delegate.&"$methodName"
                }
                def delegateAll(String[] names) {
                    names.each { delegate(it) }
                }
                def delegateAll(Map names) {
                    names.each { k, v -> delegate(k, v) }
                }
                def delegateAll() {
                    delegate.class.methods*.name.each { delegate(it) }
                }
            }
            // end::delegation_delegator[]

            // tag::delegation_classes[]
            class Person {
                String name
            }

            class MortgageLender {
                def borrowAmount(amount) {
                   "borrow \\$$amount"
                }
                def borrowFor(thing) {
                   "buy \\$thing"
                }
            }

            def lender = new MortgageLender()

            def delegator = new Delegator(Person, lender)
            // end::delegation_classes[]

            // tag::delegation_usage[]
            delegator.delegate 'borrowFor'
            delegator.delegate 'borrowAmount', 'getMoney'

            def p = new Person()

            println p.borrowFor('present')   // => buy present
            println p.getMoney(50)
            // end::delegation_usage[]

            // tag::delegation_usage2[]
            delegator.delegateAll 'borrowFor', 'borrowAmount'
            // end::delegation_usage2[]

            // tag::delegation_usage3[]
            delegator.delegateAll()
            // end::delegation_usage3[]

            // tag::delegation_usage4[]
            delegator.delegateAll borrowAmount:'getMoney', borrowFor:'getThing'
            // end::delegation_usage4[]
        '''
    }

    void testDelegationAnnotation() {
        shouldCompile '''
            // tag::delegation_annotation[]
            class Person {
                def name
                @Delegate MortgageLender mortgageLender = new MortgageLender()
            }

            class MortgageLender {
                def borrowAmount(amount) {
                   "borrow \\$$amount"
                }
                def borrowFor(thing) {
                   "buy $thing"
                }
            }

            def p = new Person()

            assert "buy present" == p.borrowFor('present')
            assert "borrow \\$50" == p.borrowAmount(50)
            // end::delegation_annotation[]
        '''
    }

    void testFlyweight() {
        shouldCompile '''
            // tag::flyweight_boeing797[]
            class Boeing797 {
                def wingspan = '80.8 m'
                def capacity = 1000
                def speed = '1046 km/h'
                def range = '14400 km'
                // ...
            }
            // end::flyweight_boeing797[]

            // tag::flyweight_airbus380[]
            class Airbus380 {
                def wingspan = '79.8 m'
                def capacity = 555
                def speed = '912 km/h'
                def range = '10370 km'
                // ...
            }
            // end::flyweight_airbus380[]

            // tag::flyweight_factory[]
            class FlyweightFactory {
                static instances = [797: new Boeing797(), 380: new Airbus380()]
            }

            class Aircraft {
                private type         // instrinsic state
                private assetNumber  // extrinsic state
                private bought       // extrinsic state
                Aircraft(typeCode, assetNumber, bought) {
                    type = FlyweightFactory.instances[typeCode]
                    this.assetNumber = assetNumber
                    this.bought = bought
                }
                def describe() {
                    println """
                    Asset Number: $assetNumber
                    Capacity: $type.capacity people
                    Speed: $type.speed
                    Range: $type.range
                    Bought: $bought
                    """
                }
            }

            def fleet = [
                new Aircraft(380, 1001, '10-May-2007'),
                new Aircraft(380, 1002, '10-Nov-2007'),
                new Aircraft(797, 1003, '10-May-2008'),
                new Aircraft(797, 1004, '10-Nov-2008')
            ]

            fleet.each { p -> p.describe() }
            // end::flyweight_factory[]
        '''
    }

    void testIterator() {
        shouldCompile '''
            // tag::iterator_example[]
            def printAll(container) {
                for (item in container) { println item }
            }

            def numbers = [ 1,2,3,4 ]
            def months = [ Mar:31, Apr:30, May:31 ]
            def colors = [ java.awt.Color.BLACK, java.awt.Color.WHITE ]
            printAll numbers
            printAll months
            printAll colors
            // end::iterator_example[]

            // tag::iterator_example2[]
            colors.eachWithIndex { item, pos ->
                println "Position $pos contains '$item'"
            }
            // end::iterator_example2[]
        '''
    }

    void testLoanMyResource() {
        shouldCompile '''
            // tag::loan_my_resource_example[]
            def f = new File('junk.txt')
            f.withPrintWriter { pw ->
                pw.println(new Date())
                pw.println(this.class.name)
            }
            println f.size()
            // => 42
            // end::loan_my_resource_example[]

            // tag::loan_my_resource_example2[]
            f.eachLine { line ->
                println line
            }
            // =>
            // Mon Jun 18 22:38:17 EST 2007
            // RunPattern
            // end::loan_my_resource_example2[]

            // tag::loan_my_resource_example3[]
            def reader = f.newReader()
            reader.splitEachLine(' ') { wordList ->
                println wordList
            }
            reader.close()
            // =>
            // [ "Mon", "Jun", "18", "22:38:17", "EST", "2007" ]
            // [ "RunPattern" ]
            // end::loan_my_resource_example3[]

            // tag::loan_my_resource_example4[]
            def withListOfWordsForEachLine(File f, Closure c) {
                def r = f.newReader()
                try {
                    r.splitEachLine(' ', c)
                } finally {
                    r?.close()
                }
            }
            // end::loan_my_resource_example4[]

            // tag::loan_my_resource_example5[]
            withListOfWordsForEachLine(f) { wordList ->
                println wordList    
            }
            // =>
            // [ "Mon", "Jun", "18", "22:38:17", "EST", "2007" ]
            // [ "RunPattern" ]
            // end::loan_my_resource_example5[]
        '''
    }

    void testNullObjectSimpleExample() {
        shouldCompile '''
            // tag::null_object_simple_example[]
            class Job {
                def salary
            }

            class Person {
                def name
                def Job job
            }

            def people = [
                new Person(name: 'Tom', job: new Job(salary: 1000)),
                new Person(name: 'Dick', job: new Job(salary: 1200)),
            ]

            def biggestSalary = people.collect { p -> p.job.salary }.max()
            println biggestSalary
            // end::null_object_simple_example[]

            // tag::null_object_simple_example2[]
            people << new Person(name: 'Harry')
            // end::null_object_simple_example2[]

            // tag::null_object_simple_example3[]
            class NullJob extends Job { def salary = 0 }

            people << new Person(name: 'Harry', job: new NullJob())
            biggestSalary = people.collect { p -> p.job.salary }.max()
            println biggestSalary
            // end::null_object_simple_example3[]

            // tag::null_object_simple_example4[]
            people << new Person(name:'Harry')
            biggestSalary = people.collect { p -> p.job?.salary }.max()
            println biggestSalary
            // end::null_object_simple_example4[]
        '''
    }

    void testNullObjectTreeExample() {
        shouldCompile '''
            // tag::null_object_tree_example[]
            class NullHandlingTree {
                def left, right, value
                
                def size() {
                    1 + (left ? left.size() : 0) + (right ? right.size() : 0)
                }

                def sum() {
                   value + (left ? left.sum() : 0) + (right ? right.sum() : 0)
                }

                def product() {
                   value * (left ? left.product() : 1) * (right ? right.product() : 1)
                }
            }

            def root = new NullHandlingTree(
                value: 2,
                left: new NullHandlingTree(
                    value: 3,
                    right: new NullHandlingTree(value: 4),
                    left: new NullHandlingTree(value: 5)
                )
            )

            println root.size()
            println root.sum()
            println root.product()
            // end::null_object_tree_example[]
        '''
    }

    void testNullObjectTreeExample2() {
        shouldCompile '''
            // tag::null_object_tree_example2[]
            class Tree {
                def left = new NullTree(), right = new NullTree(), value
                
                def size() {
                    1 + left.size() + right.size()
                }

                def sum() {
                   value + left.sum() + right.sum()
                }

                def product() {
                   value * left.product() * right.product()
                }
            }

            class NullTree {
                def size() { 0 }
                def sum() { 0 }
                def product() { 1 }
            }

            def root = new Tree(
                value: 2,
                left: new Tree(
                    value: 3,
                    right: new Tree(value: 4),
                    left: new Tree(value: 5)
                )
            )

            println root.size()
            println root.sum()
            println root.product()
            // end::null_object_tree_example2[]
        '''
    }

    void testPimpMyLibraryExample() {
        shouldCompile '''
            // tag::pimp_my_library_example[]
            class EnhancedInteger {
                static boolean greaterThanAll(Integer self, Object[] others) {
                    greaterThanAll(self, others)
                }
                static boolean greaterThanAll(Integer self, others) {
                    others.every { self > it }
                }
            }
            // end::pimp_my_library_example[]

            // tag::pimp_my_library_example2[]
            use(EnhancedInteger) {
                assert 4.greaterThanAll(1, 2, 3)
                assert !5.greaterThanAll(2, 4, 6)
                assert 5.greaterThanAll(-4..4)
                assert 5.greaterThanAll([])
                assert !5.greaterThanAll([4, 5])
            }
            // end::pimp_my_library_example2[]
        '''
    }

    void testProxyClient() {
        shouldCompile '''
            // tag::proxy_client[]
            class AccumulatorProxy {
                def accumulate(args) {
                    def result
                    def s = new Socket("localhost", 54321)
                    s.withObjectStreams { ois, oos ->
                        oos << args
                        result = ois.readObject()
                    }
                    s.close()
                    return result
                }
            }

            println new AccumulatorProxy().accumulate([1, 2, 3, 4, 5, 6, 7, 8, 9, 10])
            // => 55
            // end::proxy_client[]
        '''
    }

    void testProxyServer() {
        shouldCompile '''
            // tag::proxy_server[]
            class Accumulator {
                def accumulate(args) {
                    args.inject(0) { total, arg -> total += arg }
                }
            }

            def port = 54321
            def accumulator = new Accumulator()
            def server = new ServerSocket(port)
            println "Starting server on port $port"
            while(true) {
                server.accept() { socket ->
                    socket.withObjectStreams { ois, oos ->
                        def args = ois.readObject()
                        oos << accumulator.accumulate(args)
                    }
                }
            }
            // end::proxy_server[]
        '''
    }

    void testSingletonVoteCollector() {
        shouldCompile '''
            // tag::singleton_vote_collector[]
            class VoteCollector {
                def votes = 0
                private static final INSTANCE = new VoteCollector()
                static getInstance() { return INSTANCE }
                private VoteCollector() { }
                def display() { println "Collector:${hashCode()}, Votes:$votes" }
            }
            // end::singleton_vote_collector[]

            // tag::singleton_vote_collector_usage[]
            def collector = VoteCollector.instance
            collector.display()
            collector.votes++
            collector = null

            Thread.start{
                def collector2 = VoteCollector.instance
                collector2.display()
                collector2.votes++
                collector2 = null
            }.join()

            def collector3 = VoteCollector.instance
            collector3.display()
            // end::singleton_vote_collector_usage[]
        '''
    }

    void testSingletonMetaProgramming() {
        shouldCompile '''
            // tag::singleton_meta_programming_classes[]
            class Calculator {
                private total = 0
                def add(a, b) { total++; a + b }
                def getTotalCalculations() { 'Total Calculations: ' + total }
                String toString() { 'Calc: ' + hashCode() }
            }

            class Client {
                def calc = new Calculator()
                def executeCalc(a, b) { calc.add(a, b) }
                String toString() { 'Client: ' + hashCode() }
            }
            // end::singleton_meta_programming_classes[]

            // tag::singleton_meta_programming_meta_class[]
            class CalculatorMetaClass extends MetaClassImpl {
                private static final INSTANCE = new Calculator()
                CalculatorMetaClass() { super(Calculator) }
                def invokeConstructor(Object[] arguments) { return INSTANCE }
            }

            def registry = GroovySystem.metaClassRegistry
            registry.setMetaClass(Calculator, new CalculatorMetaClass())
            // end::singleton_meta_programming_meta_class[]

            // tag::singleton_meta_programming_usage[]
            def client = new Client()
            assert 3 == client.executeCalc(1, 2)
            println "$client, $client.calc, $client.calc.totalCalculations"

            client = new Client()
            assert 4 == client.executeCalc(2, 2)
            println "$client, $client.calc, $client.calc.totalCalculations"
            // end::singleton_meta_programming_usage[]
        '''
    }

    void testSingletonGuice() {
        shouldCompile '''
            // tag::singleton_guice[]
            @Grapes([@Grab('aopalliance:aopalliance:1.0'), @Grab('com.google.code.guice:guice:1.0')])
            import com.google.inject.*

            interface Calculator {
                def add(a, b)
            }

            class CalculatorImpl implements Calculator {
                private total = 0
                def add(a, b) { total++; a + b }
                def getTotalCalculations() { 'Total Calculations: ' + total }
                String toString() { 'Calc: ' + hashCode() }
            }

            class Client {
                @Inject Calculator calc
                def executeCalc(a, b) { calc.add(a, b) }
                String toString() { 'Client: ' + hashCode() }
            }

            def injector = Guice.createInjector (
                [configure: { binding ->
                    binding.bind(Calculator)
                           .to(CalculatorImpl)
                           .asEagerSingleton() } ] as Module
            )

            def client = injector.getInstance(Client)
            assert 3 == client.executeCalc(1, 2)
            println "$client, $client.calc, $client.calc.totalCalculations"

            client = injector.getInstance(Client)
            assert 4 == client.executeCalc(2, 2)
            println "$client, $client.calc, $client.calc.totalCalculations"
            // end::singleton_guice[]
        '''
    }

    void testSingletonGuice2() {
        shouldCompile '''
            // tag::singleton_guice2[]
            @Grapes([@Grab('aopalliance:aopalliance:1.0'), @Grab('com.google.code.guice:guice:1.0')])
            import com.google.inject.*

            @ImplementedBy(CalculatorImpl)
            interface Calculator {
                // as before ...
            }

            @Singleton
            class CalculatorImpl implements Calculator {
                // as before ...
            }

            class Client {
                // as before ...
            }

            def injector = Guice.createInjector()

            // ...
            // end::singleton_guice2[]
        '''
    }

    void testSingletonSpring() {
        shouldCompile '''
            // tag::singleton_spring[]
            @Grapes([@Grab('org.springframework:spring-core:3.2.2.RELEASE'), @Grab('org.springframework:spring-beans:3.2.2.RELEASE')])
            import org.springframework.beans.factory.support.*

            interface Calculator {
                def add(a, b)
            }

            class CalculatorImpl implements Calculator {
                private total = 0
                def add(a, b) { total++; a + b }
                def getTotalCalculations() { 'Total Calculations: ' + total }
                String toString() { 'Calc: ' + hashCode() }
            }

            class Client {
                Client(Calculator calc) { this.calc = calc }
                def calc
                def executeCalc(a, b) { calc.add(a, b) }
                String toString() { 'Client: ' + hashCode() }
            }

            // Here we 'wire' up our dependencies through the API. Alternatively,
            // we could use XML-based configuration or the Grails Bean Builder DSL.
            def factory = new DefaultListableBeanFactory()
            factory.registerBeanDefinition('calc', new RootBeanDefinition(CalculatorImpl))
            def beanDef = new RootBeanDefinition(Client, false)
            beanDef.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_AUTODETECT)
            factory.registerBeanDefinition('client', beanDef)

            def client = factory.getBean('client')
            assert 3 == client.executeCalc(1, 2)
            println "$client, $client.calc, $client.calc.totalCalculations"

            client = factory.getBean('client')
            assert 4 == client.executeCalc(2, 2)
            println "$client, $client.calc, $client.calc.totalCalculations"
            // end::singleton_spring[]
        '''
    }

    void testStateExample() {
        shouldCompile '''
            // tag::state_example[]
            class Client {
                def context = new Context()
                def connect() {
                    context.state.connect()
                }
                def disconnect() {
                    context.state.disconnect()
                }
                def send_message(message) {
                    context.state.send_message(message)
                }
                def receive_message() {
                    context.state.receive_message()
                }
            }

            class Context {
                def state = new Offline(this)
            }

            class ClientState {
                def context
                ClientState(context) {
                    this.context = context
                    inform()
                }
            }

            class Offline extends ClientState {
                Offline(context) {
                    super(context)
                }
                def inform() {
                    println "offline"
                }
                def connect() {
                    context.state = new Online(context)
                }
                def disconnect() {
                    println "error: not connected"
                }
                def send_message(message) {
                    println "error: not connected"
                }
                def receive_message() {
                    println "error: not connected"
                }
            }

            class Online extends ClientState {
                Online(context) {
                    super(context)
                }
                def inform() {
                    println "connected"
                }
                def connect() {
                    println "error: already connected"
                }
                def disconnect() {
                    context.state = new Offline(context)
                }
                def send_message(message) {
                    println "\"$message\" sent"
                }
                def receive_message() {
                    println "message received"
                }
            }

            client = new Client()
            client.send_message("Hello")
            client.connect()
            client.send_message("Hello")
            client.connect()
            client.receive_message()
            client.disconnect()
            // end::state_example[]
        '''
    }

    void testStateVariation1() {
        shouldCompile '''
            // tag::state_variation1_interface[]
            interface State {
                def connect()
                def disconnect()
                def send_message(message)
                def receive_message()
            }
            // end::state_variation1_interface[]
        '''
    }

    void testStateVariation2() {
        shouldCompile '''
            interface State { }
            
            // tag::state_variation1_impl[]
            class Client implements State {
              // ... as before ... 
            }

            class Online implements State {
              // ... as before ... 
            }

            class Offline implements State {
              // ... as before ... 
            }
            // end::state_variation1_impl[]
        '''
    }

    void testStateVariation3() {
        shouldCompile '''
            // tag::state_variation2_classes[]
            abstract class InstanceProvider {
                static def registry = GroovySystem.metaClassRegistry
                static def create(objectClass, param) {
                    registry.getMetaClass(objectClass).invokeConstructor([param] as Object[])
                }
            }

            abstract class Context {
                private context
                protected setContext(context) {
                    this.context = context
                }
                def invokeMethod(String name, Object arg) {
                    context.invokeMethod(name, arg)
                }
                def startFrom(initialState) {
                    setContext(InstanceProvider.create(initialState, this))
                }
            }

            abstract class State {
                private client

                State(client) { this.client = client }

                def transitionTo(nextState) {
                    client.setContext(InstanceProvider.create(nextState, client))
                }
            }
            // end::state_variation2_classes[]

            // tag::state_variation2_impl[]
            class Client extends Context {
                Client() {
                    startFrom(Offline)
                }
            }

            class Offline extends State {
                Offline(client) {
                    super(client)
                    println "offline"
                }
                def connect() {
                    transitionTo(Online)
                }
                def disconnect() {
                    println "error: not connected"
                }
                def send_message(message) {
                    println "error: not connected"
                }
                def receive_message() {
                    println "error: not connected"
                }
            }

            class Online extends State {
                Online(client) {
                    super(client)
                    println "connected"
                }
                def connect() {
                    println "error: already connected"
                }
                def disconnect() {
                    transitionTo(Offline)
                }
                def send_message(message) {
                    println "\"$message\" sent"
                }
                def receive_message() {
                    println "message received"
                }
            }

            client = new Client()
            client.send_message("Hello")
            client.connect()
            client.send_message("Hello")
            client.connect()
            client.receive_message()
            client.disconnect()
            // end::state_variation2_impl[]
        '''
    }

    void testStateVariation4() {
        shouldCompile '''
            // tag::state_variation31[]
            class Grammar {
                def fsm

                def event
                def fromState
                def toState

                Grammar(a_fsm) {
                    fsm = a_fsm
                }

                def on(a_event) {
                    event = a_event
                    this
                }

                def on(a_event, a_transitioner) {
                    on(a_event)
                    a_transitioner.delegate = this
                    a_transitioner.call()
                    this
                }

                def from(a_fromState) {
                    fromState = a_fromState
                    this
                }

                def to(a_toState) {
                    assert a_toState, "Invalid toState: $a_toState"
                    toState = a_toState
                    fsm.registerTransition(this)
                    this
                }

                def isValid() {
                    event && fromState && toState
                }

                public String toString() {
                    "$event: $fromState=>$toState"
                }
            }
            // end::state_variation31[]

            // tag::state_variation32[]
            class FiniteStateMachine {
                def transitions = [:]

                def initialState
                def currentState

                FiniteStateMachine(a_initialState) {
                    assert a_initialState, "You need to provide an initial state"
                    initialState = a_initialState
                    currentState = a_initialState
                }

                def record() {
                    Grammar.newInstance(this)
                }

                def reset() {
                    currentState = initialState
                }

                def isState(a_state) {
                    currentState == a_state
                }

                def registerTransition(a_grammar) {
                    assert a_grammar.isValid(), "Invalid transition ($a_grammar)"
                    def transition
                    def event = a_grammar.event
                    def fromState = a_grammar.fromState
                    def toState = a_grammar.toState

                    if (!transitions[event]) {
                        transitions[event] = [:]
                    }

                    transition = transitions[event]
                    assert !transition[fromState], "Duplicate fromState $fromState for transition $a_grammar"
                    transition[fromState] = toState
                }

                def fire(a_event) {
                    assert currentState, "Invalid current state '$currentState': passed into constructor"
                    assert transitions.containsKey(a_event), "Invalid event '$a_event', should be one of ${transitions.keySet()}"
                    def transition = transitions[a_event]
                    def nextState = transition[currentState]
                    assert nextState, "There is no transition from '$currentState' to any other state"
                    currentState = nextState
                    currentState
                }
            }
            // end::state_variation32[]
            import groovy.test.GroovyTestCase
            // tag::state_variation33[]
            class StatePatternDslTest extends GroovyTestCase {
                private fsm

                protected void setUp() {
                    fsm = FiniteStateMachine.newInstance('offline')
                    def recorder = fsm.record()
                    recorder.on('connect').from('offline').to('online')
                    recorder.on('disconnect').from('online').to('offline')
                    recorder.on('send_message').from('online').to('online')
                    recorder.on('receive_message').from('online').to('online')
                }

                void testInitialState() {
                    assert fsm.isState('offline')
                }

                void testOfflineState() {
                    shouldFail{
                        fsm.fire('send_message')
                    }
                    shouldFail{
                        fsm.fire('receive_message')
                    }
                    shouldFail{
                        fsm.fire('disconnect')
                    }
                    assert 'online' == fsm.fire('connect')
                }

                void testOnlineState() {
                    fsm.fire('connect')
                    fsm.fire('send_message')
                    fsm.fire('receive_message')
                    shouldFail{
                        fsm.fire('connect')
                    }
                    assert 'offline' == fsm.fire('disconnect')
                }
            }
            // end::state_variation33[]
        '''
    }

    void testStrategyTraditional() {
        shouldCompile '''
            // tag::strategy_traditional[]
            interface Calc {
                def execute(n, m)
            }

            class CalcByMult implements Calc {
                def execute(n, m) { n * m }
            }

            class CalcByManyAdds implements Calc {
                def execute(n, m) {
                    def result = 0
                    n.times{
                        result += m
                    }

                    result
                }
            }

            def sampleData = [
                [3, 4, 12],
                [5, -5, -25]
            ]

            Calc[] multiplicationStrategies = [
                new CalcByMult(),
                new CalcByManyAdds()
            ]

            sampleData.each{ data ->
                multiplicationStrategies.each { calc ->
                    assert data[2] == calc.execute(data[0], data[1])
                }
            }
            // end::strategy_traditional[]
        '''
    }

    void testStrategyGroovyWay() {
        shouldCompile '''
            // tag::strategy_groovy_way[]
            def multiplicationStrategies = [
                { n, m -> n * m },
                { n, m -> def result = 0; n.times{ result += m }; result }
            ]

            def sampleData = [
                [3, 4, 12],
                [5, -5, -25]
            ]

            sampleData.each{ data ->
                multiplicationStrategies.each { calc ->
                    assert data[2] == calc(data[0], data[1])
                }
            }
            // end::strategy_groovy_way[]
        '''
    }

    void testTemplateMethod() {
        shouldCompile '''
            // tag::template_method_example[]
            abstract class Accumulator {
                protected initial
                abstract doAccumulate(total, v)
                def accumulate(values) {
                    def total = initial
                    values.each { v -> total = doAccumulate(total, v) }
                    total
                }
            }

            class Sum extends Accumulator {
                def Sum() { initial = 0 }
                def doAccumulate(total, v) { total + v }
            }

            class Product extends Accumulator {
                def Product() { initial = 1 }
                def doAccumulate(total, v) { total * v }
            }

            println new Sum().accumulate([1,2,3,4])
            println new Product().accumulate([1,2,3,4])
            // end::template_method_example[]
        '''
    }

    void testTemplateMethod2() {
        shouldCompile '''
            // tag::template_method_example2[]
            Closure addAll = { total, item -> total += item }
            def accumulated = [1, 2, 3, 4].inject(0, addAll)
            println accumulated    // => 10
            // end::template_method_example2[]

            // tag::template_method_example3[]
            accumulated = [ "1", "2", "3", "4" ].inject("", addAll)
            println accumulated    // => "1234"
            // end::template_method_example3[]

            // tag::template_method_example4[]
            Closure multAll = { total, item -> total *= item }
            accumulated = [1, 2, 3, 4].inject(1, multAll)
            println accumulated    // => 24
            // end::template_method_example4[]
        '''
    }

    void testVisitorSimpleExample() {
        shouldCompile '''
            // tag::visitor_simple_example[]
            abstract class Shape { }

            class Rectangle extends Shape {
                def x, y, width, height

                Rectangle(x, y, width, height) {
                    this.x = x; this.y = y; this.width = width; this.height = height
                }

                def union(rect) {
                    if (!rect) return this
                    def minx = [rect.x, x].min()
                    def maxx = [rect.x + width, x + width].max()
                    def miny = [rect.y, y].min()
                    def maxy = [rect.y + height, y + height].max()
                    new Rectangle(minx, miny, maxx - minx, maxy - miny)
                }

                def accept(visitor) {
                    visitor.visit_rectangle(this)
                }
            }

            class Line extends Shape {
                def x1, y1, x2, y2

                Line(x1, y1, x2, y2) {
                    this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2
                }

                def accept(visitor){
                    visitor.visit_line(this)
                }
            }

            class Group extends Shape {
                def shapes = []
                def add(shape) { shapes += shape }
                def remove(shape) { shapes -= shape }
                def accept(visitor) {
                    visitor.visit_group(this)
                }
            }

            class BoundingRectangleVisitor {
                def bounds

                def visit_rectangle(rectangle) {
                    if (bounds)
                        bounds = bounds.union(rectangle)
                    else
                        bounds = rectangle
                }

                def visit_line(line) {
                    def line_bounds = new Rectangle(line.x1, line.y1, line.x2-line.y1, line.x2-line.y2)
                    if (bounds)
                        bounds = bounds.union(line_bounds)
                    else
                        bounds = line_bounds
                }

                def visit_group(group) {
                    group.shapes.each { shape -> shape.accept(this) }
                }
            }

            def group = new Group()
            group.add(new Rectangle(100, 40, 10, 5))
            group.add(new Rectangle(100, 70, 10, 5))
            group.add(new Line(90, 30, 60, 5))
            def visitor = new BoundingRectangleVisitor()
            group.accept(visitor)
            bounding_box = visitor.bounds
            println bounding_box.dump()
            // end::visitor_simple_example[]
        '''
    }

    void testVisitorSimpleExample2() {
        shouldCompile '''
            // tag::visitor_simple_example2[]
            abstract class Shape {
                def accept(Closure yield) { yield(this) }
            }

            class Rectangle extends Shape {
                def x, y, w, h
                def bounds() { this }
                def union(rect) {
                    if (!rect) return this
                    def minx = [ rect.x, x ].min()
                    def maxx = [ rect.x + w, x + w ].max()
                    def miny = [ rect.y, y ].min()
                    def maxy = [ rect.y + h, y + h ].max()
                    new Rectangle(x:minx, y:miny, w:maxx - minx, h:maxy - miny)
                }
            }

            class Line extends Shape {
                def x1, y1, x2, y2
                def bounds() {
                    new Rectangle(x:[x1, x2].min(), y:[y1, y2].min(), w:(x2 - x1).abs(), h:(y2 - y1).abs())
                }
            }

            class Group {
                def shapes = []
                def leftShift(shape) { shapes += shape }
                def accept(Closure yield) { shapes.each{it.accept(yield)} }
            }

            def group = new Group()
            group << new Rectangle(x:100, y:40, w:10, h:5)
            group << new Rectangle(x:100, y:70, w:10, h:5)
            group << new Line(x1:90, y1:30, x2:60, y2:5)
            def bounds
            group.accept{ bounds = it.bounds().union(bounds) }
            println bounds.dump()
            // end::visitor_simple_example2[]
        '''
    }

    void testVisitorAdvancedExample() {
        shouldCompile '''
            // tag::visitor_advanced_example[]
            interface Visitor {
                void visit(NodeType1 n1)
                void visit(NodeType2 n2)
            }

            interface Visitable {
                void accept(Visitor visitor)
            }

            class NodeType1 implements Visitable {
                Visitable[] children = new Visitable[0]
                void accept(Visitor visitor) {
                    visitor.visit(this)
                    for(int i = 0; i < children.length; ++i) {
                        children[i].accept(visitor)
                    }
                }
            }

            class NodeType2 implements Visitable {
                Visitable[] children = new Visitable[0]
                void accept(Visitor visitor) {
                    visitor.visit(this)
                    for(int i = 0; i < children.length; ++i) {
                        children[i].accept(visitor)
                    }
                }
            }

            class NodeType1Counter implements Visitor {
                int count = 0
                void visit(NodeType1 n1) {
                    count++
                }
                void visit(NodeType2 n2){}
            }
            // end::visitor_advanced_example[]

            // tag::visitor_advanced_example2[]
            NodeType1 root = new NodeType1()
            root.children = new Visitable[2]
            root.children[0] = new NodeType1()
            root.children[1] = new NodeType2()
            // end::visitor_advanced_example2[]
        '''
    }

    void testVisitorAdvancedExample3() {
        shouldCompile '''
            // tag::visitor_advanced_example3[]
            interface Visitor {
                void visit(NodeType1 n1)
                void visit(NodeType2 n2)
            }

            class DefaultVisitor implements Visitor{
                void visit(NodeType1 n1) {
                    for(int i = 0; i < n1.children.length; ++i) {
                        n1.children[i].accept(this)
                    }
                }
                void visit(NodeType2 n2) {
                    for(int i = 0; i < n2.children.length; ++i) {
                        n2.children[i].accept(this)
                    }
                }
            }

            interface Visitable {
                void accept(Visitor visitor)
            }

            class NodeType1 implements Visitable {
                Visitable[] children = new Visitable[0]
                void accept(Visitor visitor) {
                    visitor.visit(this)
                }
            }

            class NodeType2 implements Visitable {
                Visitable[] children = new Visitable[0];
                void accept(Visitor visitor) {
                    visitor.visit(this)
                }
            }

            class NodeType1Counter extends DefaultVisitor {
                int count = 0
                void visit(NodeType1 n1) {
                    count++
                    super.visit(n1)
                }
            }
            // end::visitor_advanced_example3[]
        '''
    }

    void testVisitorAdvancedExample4() {
        shouldCompile '''
            // tag::visitor_advanced_example4[]
            class DefaultVisitor {
                void visit(NodeType1 n1) {
                    n1.children.each { visit(it) }
                }
                void visit(NodeType2 n2) {
                    n2.children.each { visit(it) }
                }
                void visit(Visitable v) { }
            }

            interface Visitable { }

            class NodeType1 implements Visitable {
                Visitable[] children = []
            }

            class NodeType2 implements Visitable {
                Visitable[] children = []
            }

            class NodeType1Counter extends DefaultVisitor {
                int count = 0
                void visit(NodeType1 n1) {
                    count++
                    super.visit(n1)
                }
            }
            // end::visitor_advanced_example4[]
        '''
    }

    void testVisitorAdvancedExample5() {
        shouldCompile '''
            // tag::visitor_advanced_example5[]
            class DefaultVisitor {
                void visit(Visitable v) {
                    doIteraton(v)
                }
                void doIteraton(Visitable v) {
                    v.children.each {
                        visit(it)
                    }
                }
            }

            interface Visitable {
                Visitable[] getChildren()
            }

            class NodeType1 implements Visitable {
                Visitable[] children = []
            }

            class NodeType2 implements Visitable {
                Visitable[] children = []
            }

            class NodeType1Counter extends DefaultVisitor {
                int count = 0
                void visit(NodeType1 n1) {
                    count++
                    super.visit(n1)
                }
            }
            // end::visitor_advanced_example5[]
        '''
    }
}
