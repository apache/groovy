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

// Differential parity corpus for groovy.indy.cold.reflection (GROOVY-12137),
// driven by ColdReflectionParityTest. Dynamic dispatch throughout (no
// @CompileStatic) so plain calls hit the reflective cold tier when the flag
// is on. Prints a fully deterministic transcript; the test runs it with the
// flag off and on in separate JVMs and asserts the transcripts are
// byte-identical, so any difference is a genuine reflection-vs-MethodHandle
// divergence. Every check returns a stable String (or throws, captured
// stably) — keep all inputs/outputs deterministic (no identity hashes,
// timing, or randomness) when extending it.

// ---- target classes ----
class Prim {
    int i(int x) { x }
    long lng(long x) { x }
    char ch() { (char) 65 }
    boolean bool() { true }
    byte by() { (byte) 7 }
    short sh() { (short) 9 }
    float fl() { 1.5f }
    double db() { 2.5d }
}
class Coerce {
    String s(String x) { x }
    long widen(long x) { x }
    String chr(char c) { String.valueOf(c) }
    String num(Number n) { n.getClass().simpleName + ':' + n }
}
class Vararg {
    String v(String... xs) { xs.length + ':' + xs.join(',') }
    int arr(byte[]... a) { a.length }
    String mix(Object... o) { o.length + ':' + o.collect { it?.getClass()?.simpleName }.join(',') }
}
class Overload {
    String m(String s) { 'S' }
    String m(Integer i) { 'I' }
    String m(Object o) { 'O' }
    String m(int i) { 'prim' }
}
class Ex {
    def boom() { throw new IllegalStateException('boom') }
    def missing() { this.nope() }
    def checked() { throw new java.io.IOException('io') }
}
class Ret {
    void v() { }
    def nul() { null }
    int[] arr() { [1, 2, 3] as int[] }
    Void voidBox() { null }
}
class Vis {
    String pub() { 'pub' }
    protected String prot() { 'prot' }
    private String priv() { 'priv' }
}
class Pogo {
    String greet() { 'OWNER' }
    int echoInt(int x) { x }
}
class Pogo2 {
    int echoInt(int x) { x * 10 }
    String label() { 'P2' }
}
class Intercept implements GroovyInterceptable {
    def invokeMethod(String name, args) { "intercept:$name" }
    String real() { 'real' }
}
class MethMissing {
    def methodMissing(String name, args) { "mm:$name/${args.length}" }
    String real() { 'real' }
}

// ---- harness ----
def out = new StringBuilder()
Closure rec = { String tag, Closure body ->
    String line
    try {
        def r = body()
        line = "$tag | OK | ${r}"
    } catch (Throwable t) {
        line = "$tag | EX | ${t.getClass().name}: ${t.message}"
    }
    out.append(line).append('\n')
}
// helper: value + box-cache identity, deterministic
def boxId = { v, canonical -> "${v}/cached=${v.is(canonical)}" }

// ---- A. primitive return value + box identity (the Method.invoke box axis) ----
rec('A.int.cached')   { def p = new Prim(); boxId(p.i(42),   Integer.valueOf(42)) }
rec('A.int.uncached') { def p = new Prim(); boxId(p.i(9999), Integer.valueOf(9999)) }
rec('A.int.neg')      { def p = new Prim(); boxId(p.i(-128), Integer.valueOf(-128)) }
rec('A.long')         { def p = new Prim(); boxId(p.lng(7L), Long.valueOf(7L)) }
rec('A.long.big')     { def p = new Prim(); boxId(p.lng(99999L), Long.valueOf(99999L)) }
rec('A.char')         { def p = new Prim(); def v = p.ch(); "${(int) v}/cached=${v.is(Character.valueOf((char) 65))}" }
rec('A.bool')         { def p = new Prim(); def v = p.bool(); "${v}/cached=${v.is(Boolean.TRUE)}" }
rec('A.byte')         { def p = new Prim(); boxId(p.by(), Byte.valueOf((byte) 7)) }
rec('A.short')        { def p = new Prim(); boxId(p.sh(), Short.valueOf((short) 9)) }
rec('A.float')        { def p = new Prim(); "${p.fl()}" }
rec('A.double')       { def p = new Prim(); "${p.db()}" }

// ---- B. exceptions ----
rec('B.rte')     { new Ex().boom() }
rec('B.mme')     { new Ex().missing() }
rec('B.checked') { new Ex().checked() }
rec('B.npe')     { def x = null; x.foo() }
rec('B.nosuch')  { new Prim().totallyUnknownMethod() }

// ---- C. argument coercion ----
rec('C.gstring') { def who = 'x'; new Coerce().s("a${who}${1 + 1}b") }
rec('C.widen')   { new Coerce().widen(5) }        // Integer 5 -> long
rec('C.char')    { new Coerce().chr('Z' as char) }
rec('C.num.int') { new Coerce().num(5) }
rec('C.num.bd')  { new Coerce().num(3.14G) }
rec('C.num.big') { new Coerce().num(123456789012345G) }

// ---- D. varargs / arrays ----
rec('D.varg.0')   { new Vararg().v() }
rec('D.varg.1')   { new Vararg().v('a') }
rec('D.varg.3')   { new Vararg().v('a', 'b', 'c') }
rec('D.varg.arr') { new Vararg().arr('ab'.bytes) }        // byte[] into byte[]... (GROOVY-12139)
rec('D.varg.arr2') { new Vararg().arr('ab'.bytes, 'cd'.bytes) }
rec('D.varg.mix') { new Vararg().mix(1, 'x', [2], null) }

// ---- E. overload selection ----
rec('E.ov.string') { new Overload().m('x') }
rec('E.ov.int')    { new Overload().m(5) }
rec('E.ov.integer'){ new Overload().m(Integer.valueOf(5)) }
rec('E.ov.obj')    { new Overload().m([1, 2]) }
rec('E.ov.null')   { new Overload().m(null) }

// ---- F. caller-sensitive: serialization (must stay off the cold tier) ----
rec('F.ser.list') {
    def baos = new ByteArrayOutputStream()
    new ObjectOutputStream(baos).withCloseable { it.writeObject([1, 2, 3]) }
    def r = null
    new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())).withCloseable { r = it.readObject() }
    r.toString()
}

// ---- G. visibility ----
rec('G.pub')  { new Vis().pub() }
rec('G.prot') { new Vis().prot() }
rec('G.priv') { new Vis().priv() }

// ---- H. return-type corners ----
rec('H.void')  { new Ret().v(); 'done' }
rec('H.null')  { new Ret().nul() }
rec('H.array') { def a = new Ret().arr(); "${a.length}:${a[0]},${a[2]}" }
rec('H.voidBox') { new Ret().voidBox() }

// ---- I. receiver kinds (POJO / DGM) ----
rec('I.pojo.substr')  { 'Hello'.substring(1, 3) }
rec('I.pojo.listget') { [10, 20, 30].get(1) }
rec('I.dgm.reverse')  { 'abc'.reverse() }
rec('I.dgm.sum')      { [1, 2, 3, 4].sum() }
rec('I.dgm.collect')  { [1, 2, 3].collect { it * 2 }.toString() }
rec('I.dgm.max')      { [3, 1, 2].max() }

// ---- J. metaclass dynamics (validity invalidation across cold entry) ----
rec('J.emc') {
    def r = new Pogo()
    def a = r.greet()
    Pogo.metaClass.greet = { -> 'MC' }
    def b = new Pogo().greet()
    GroovySystem.metaClassRegistry.removeMetaClass(Pogo)
    def c = new Pogo().greet()
    "$a|$b|$c"
}
rec('J.category') {
    def r = new Pogo()
    def before = r.greet()
    def during = use(PogoCat) { r.greet() }
    def after = r.greet()
    "$before|$during|$after"
}

// ---- K. promotion boundary: many calls crossing the hit-count threshold ----
rec('K.promote') {
    def r = new Pogo()
    long acc = 0
    for (int i = 0; i < 2500; i++) { acc += r.echoInt(i % 100) }
    acc
}
rec('K.promote.poly') {
    def rs = [new Pogo(), new Pogo2()]   // both have echoInt; alternate to stress per-wrapper promotion
    long acc = 0
    for (int i = 0; i < 3000; i++) { acc += rs[i % 2].echoInt(i % 50) }
    acc
}

// ---- N. more breadth: statics, null-into-primitive, chaining, GString coercion ----
rec('N.static.parseInt') { Integer.parseInt('42') }          // Class receiver — cold tier excluded
rec('N.static.valueOf')  { Long.valueOf('123') }
rec('N.null.prim')       { new Prim().i(null) }              // null into primitive param
rec('N.chain')           { 'Hello World'.toLowerCase().split(' ')[1] }
rec('N.chain.dgm')       { [3, 1, 2, 1].unique().sort().join('-') }
rec('N.gstring.coerce')  { new Coerce().widen("${40 + 2}".toInteger()) }
rec('N.ov.char')         { new Overload().m('x' as char) }   // char -> which overload?
rec('N.map.get')         { [a: 1, b: 2].get('b') }
rec('N.string.eq')       { 'abc'.equals('abc') }
rec('N.number.plus')     { 5.plus(3) }                        // operator as method via DGM/number
rec('N.list.add')        { def l = [1, 2]; l.add(3); l.toString() }
rec('N.bool.method')     { Boolean.parseBoolean('TRUE') }

// ---- M. MOP interception (must decline the cold tier) ----
rec('M.interceptable.dyn')  { new Intercept().anything(1, 2) }
rec('M.interceptable.real') { new Intercept().real() }
rec('M.mm.real')    { new MethMissing().real() }
rec('M.mm.missing') { new MethMissing().ghost(1, 2) }

print out.toString()

class PogoCat {
    static String greet(Pogo self) { 'CAT' }
    static int echoInt(Pogo self, int x) { x }
}
