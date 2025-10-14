// GPars - Groovy Parallel Systems
//
// Copyright © 2008-2012  The original author or authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package groovyx.gpars

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode

/**
 * Enhances objects by being mixed-in either within a GParsPool.withPool() block or after enhancement by
 * the ParallelEnhancer through the makeConcurrent() method.
 * It overrides the iterative methods, like each, collect and such to delegate to eachParallel, collectParallel
 * and other parallel iterative methods.
 * The collections returned from collect(), findAll() and grep() are again mixed with a TransparentParallel instance,
 * so their iterative methods are transparently parallel as well.
 *
 * Author: Vaclav Pech, Dierk Koenig
 * Date: Oct 30, 2009
 */
final class TransparentParallel {
    private boolean concurrencyActive = true;

    public def final each(Closure yield) { if (concurrencyActive) this.eachParallel(yield); else (mixedIn[Object] as Object).each(yield) }

    public def final eachWithIndex(Closure yield) { if (concurrencyActive) this.eachWithIndexParallel(yield); else (mixedIn[Object] as Object).eachWithIndex(yield) }

    public def final collect(Closure yield) { if (concurrencyActive) this.collectParallel(yield).makeConcurrent(); else (mixedIn[Object] as Object).collect(yield) }

    public def final collectMany(Closure yield) { if (concurrencyActive) this.collectManyParallel(yield).makeConcurrent(); else (mixedIn[Object] as Object).collectMany(yield) }

    public def final find(Closure yield) { if (concurrencyActive) this.findParallel(yield); else (mixedIn[Object] as Object).find(yield) }

    public def final findAny(Closure yield) { if (concurrencyActive) this.findAnyParallel(yield); else (mixedIn[Object] as Object).find(yield) }

    public def final findAll(Closure yield) { if (concurrencyActive) this.findAllParallel(yield).makeConcurrent(); else (mixedIn[Object] as Object).findAll(yield) }

    public def final grep(filter) { if (concurrencyActive) this.grepParallel(filter).makeConcurrent(); else (mixedIn[Object] as Object).grep(filter) }

    public def final split(Closure yield) { if (concurrencyActive) this.splitParallel(yield).makeConcurrent(); else (mixedIn[Object] as Object).split(yield) }

    public def final count(filter) { if (concurrencyActive) this.countParallel(filter); else (mixedIn[Object] as Object).count(filter) }

    public def final every(Closure yield) { if (concurrencyActive) this.everyParallel(yield); else (mixedIn[Object] as Object).every(yield) }

    public def final any(Closure yield) { if (concurrencyActive) this.anyParallel(yield); else (mixedIn[Object] as Object).any(yield) }

    public def final groupBy(Closure yield) { if (concurrencyActive) this.groupByParallel(yield); else (mixedIn[Object] as Object).groupBy(yield) }

    public def final min(Closure yield) { if (concurrencyActive) this.minParallel(yield); else (mixedIn[Object] as Object).min(yield) }

    public def final min() { if (concurrencyActive) this.minParallel(); else (mixedIn[Object] as Object).min() }

    public def final max(Closure yield) { if (concurrencyActive) this.maxParallel(yield); else (mixedIn[Object] as Object).max(yield) }

    public def final max() { if (concurrencyActive) this.maxParallel(); else (mixedIn[Object] as Object).max() }

    public def final sum() { if (concurrencyActive) this.sumParallel(); else (mixedIn[Object] as Object).sum() }

//    @Deprecated
//    public def final fold(Closure yield) { if (concurrencyActive) this.foldParallel(yield); else throw new UnsupportedOperationException('The inject() operation is not supported by collections in sequential mode') }
//
//    @Deprecated
//    public def final fold(seed, Closure yield) { if (concurrencyActive) this.foldParallel(seed, yield); else throw new UnsupportedOperationException('The inject() operation is not supported by collections in sequential mode') }

    public def final inject(Closure yield) { if (concurrencyActive) this.injectParallel(yield); else throw new UnsupportedOperationException('The inject() operation is not supported by collections in sequential mode') }

    public def final inject(seed, Closure yield) { if (concurrencyActive) this.injectParallel(seed, yield); else throw new UnsupportedOperationException('The inject() operation is not supported by collections in sequential mode') }

    /**
     * Indicates, whether the iterative methods like each() or collect() have been made parallel.
     * Always true once a collection is enhanced through the makeConcurrent() method.
     * The concurrencyActive flag then indicates, whether the enhanced collection has concurrent or the original sequential semantics
     */
    @CompileStatic(value = TypeCheckingMode.PASS)
    public def boolean isConcurrent() { return true }

    /**
     * Turns concurrency on and off as needed
     * @param flag True, if the collection processing methods should have parallel semantics, false if they should call the original sequential implementation
     */
    @CompileStatic(value = TypeCheckingMode.PASS)
    void setConcurrencyActive(boolean flag) {
        concurrencyActive = flag
    }

    /**
     * Indicates, whether the iterative methods like each() or collect() should have a concurrent or a sequential semantics.
     */
    @CompileStatic(value = TypeCheckingMode.PASS)
    public boolean isConcurrencyActive() {
        concurrencyActive
    }
}
