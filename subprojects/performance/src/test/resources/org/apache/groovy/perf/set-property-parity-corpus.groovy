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

// Differential parity corpus for groovy.indy.setproperty (GROOVY-12138),
// driven by SetPropertyParityTest. Dynamic property writes throughout (no
// @CompileStatic) so each becomes an invokedynamic setProperty site when the
// flag is on and a ScriptBytecodeAdapter.setProperty call when off. Prints a
// deterministic transcript; the test compiles+runs it in separate JVMs with
// the flag on and off and asserts the transcripts are byte-identical (the
// non-fast-path cases fall back to the exact classic adapter, so behaviour is
// preserved by construction). Keep all inputs/outputs deterministic when
// extending it.

class Bean {
    String name
    int count
    long total
    public double raw               // public field, no setter
    private String hidden = 'h'
    String getHidden() { hidden }
    static String shared = 's'
    Boolean flag
}
class Fluent {
    String label
    Fluent setLabel(String l) { this.label = 'set:' + l; return this }   // non-void setter
}
class Custom {
    def stored = [:]
    void setProperty(String name, Object value) { stored[name] = "custom:$value" }
}

def out = new StringBuilder()
Closure rec = { String tag, Closure body ->
    String line
    try {
        line = "$tag | OK | ${body()}"
    } catch (Throwable t) {
        line = "$tag | EX | ${t.getClass().name}: ${t.message}"
    }
    out.append(line).append('\n')
}

// setter / field writes, repeated to cross the promotion threshold
rec('setter.string') { def b = new Bean(); 300.times { i -> b.name = "n$i" }; b.name }
rec('setter.int')    { def b = new Bean(); 300.times { i -> b.count = i }; b.count }
rec('field.public')  { def b = new Bean(); 50.times { i -> b.raw = i + 0.5d }; b.raw }
// coercion (needs the adapter path): Integer -> long, GString -> String
rec('coerce.long')   { def b = new Bean(); b.total = 5; b.total }
rec('coerce.gstr')   { def b = new Bean(); def who = 'world'; b.name = "hi $who"; b.name.class.simpleName + ':' + b.name }
// alternating value types at one call site (re-selection)
rec('alttypes')      { def b = new Bean(); def r = []; ['a', 1, 'b', 2].each { v -> b.name = "$v"; r << b.name }; r.toString() }
// custom setProperty interception (must decline the fast path)
rec('custom.set')    { def c = new Custom(); 30.times { c.answer = 42 }; c.stored.answer }
// static property via Class receiver
rec('static.prop')   { Bean.shared = 'updated'; Bean.shared }
// metaclass change after caching
rec('emc') {
    def b0 = new Bean(); 60.times { b0.name = 'x' }
    Bean.metaClass.setName = { String v -> delegate.@name = 'MC:' + v }
    def b1 = new Bean(); b1.name = 'y'
    def afterEmc = b1.name
    GroovySystem.metaClassRegistry.removeMetaClass(Bean)
    def b2 = new Bean(); b2.name = 'z'
    "$afterEmc|${b2.name}"
}
// read-only / missing property errors
rec('missing')  { def b = new Bean(); b.nonexistent = 1; 'no-error' }
rec('readonly') { def b = new Bean(); b.hidden = 'nope'; 'no-error' }
// null and Boolean property
rec('null.bool') { def b = new Bean(); b.flag = true; def a = b.flag; b.flag = null; "$a|${b.flag}" }
rec('null.str')  { def b = new Bean(); b.name = null; b.name }
// non-void (fluent) setter
rec('fluent') { def f = new Fluent(); 25.times { f.label = 'L' }; f.label }
// maps and expando
rec('map')     { def m = [:]; 40.times { i -> m.k = i }; m.k }
rec('expando') { def e = new Expando(); 40.times { i -> e.dyn = i }; e.dyn }

print out.toString()
