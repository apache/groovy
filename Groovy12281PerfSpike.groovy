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

/*
 * GROOVY-12281 indicative perf spike (manual, not CI; treat as indicative —
 * the JMH suite remains the heavyweight gate). Measures the global
 * ClassValue mode's cost on the paths soft mode touches:
 *   - micro: ClassInfo.getClassInfo hot loop (the extra SoftReference deref)
 *   - macro: dynamic dispatch loops (String-heavy and POGO)
 *
 * Run per mode, fresh JVM each, idle machine, e.g.:
 *   for m in true soft false; do
 *     java -Xms512m -Xmx512m -cp build/libs/groovy-6.0.0-SNAPSHOT.jar \
 *       -Dgroovy.use.classvalue=$m groovy.ui.GroovyMain Groovy12281PerfSpike.groovy
 *   done
 */

import groovy.transform.CompileStatic
import org.codehaus.groovy.reflection.ClassInfo

@CompileStatic
class Micro {
    static long hotLoop(int iters) {
        long acc = 0
        for (int i = 0; i < iters; i++) {
            acc += System.identityHashCode(ClassInfo.getClassInfo(String))
        }
        acc
    }

    static long mixedLoop(int iters) {
        long acc = 0
        Class[] keys = [String, Integer, ArrayList, LinkedHashMap, Micro] as Class[]
        for (int i = 0; i < iters; i++) {
            acc += System.identityHashCode(ClassInfo.getClassInfo(keys[i % keys.length]))
        }
        acc
    }
}

class Pogo {
    int value
    def bump(int n) { value += n; value }
}

def stringLoop = { int iters ->
    def s = 'abcdef'
    def acc = 0
    for (int i = 0; i < iters; i++) {
        acc += s.reverse().size() + "x${i & 7}".size()
    }
    acc
}

def pogoLoop = { int iters ->
    def p = new Pogo()
    def acc = 0
    for (int i = 0; i < iters; i++) {
        acc += p.bump(1) - p.value + i
    }
    acc
}

static List<Double> medianTimes(int rounds, int iters, Closure work) {
    def times = []
    for (int r = 0; r < rounds; r++) {
        def t0 = System.nanoTime()
        work(iters)
        times << (System.nanoTime() - t0) / (double) iters
    }
    times.sort()
    times
}

def mode = System.getProperty('groovy.use.classvalue', 'true')
int microIters = 20_000_000
int macroIters = 200_000
int rounds = 7

// warmup
Micro.hotLoop(microIters); Micro.mixedLoop(microIters)
stringLoop(macroIters); pogoLoop(macroIters)

def report = { String label, List<Double> t ->
    printf('%s mode=%s median=%.2f ns/op (min=%.2f max=%.2f)%n',
            label, mode, t[t.size().intdiv(2)], t.first(), t.last())
}

report('micro.getClassInfo(String) ', medianTimes(rounds, microIters, Micro.&hotLoop))
report('micro.getClassInfo(mixed)  ', medianTimes(rounds, microIters, Micro.&mixedLoop))
report('macro.dispatch(String-heavy)', medianTimes(rounds, macroIters, stringLoop))
report('macro.dispatch(POGO)        ', medianTimes(rounds, macroIters, pogoLoop))
