// GPars - Groovy Parallel Systems
//
// Copyright © 2008–2011, 2014  The original author or authors
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

// package groovyx.gpars.pa

// // TODO: delete
// //import groovyx.gpars.extra166y.ParallelArrayWithMapping

// /**
//  * The ParallelArray wrapper used after the map() operation
//  */
// final class MappedPAWrapper<T> extends AbstractPAWrapper {

//     def MappedPAWrapper(final ParallelArrayWithMapping pa) {
//         super(pa)
//     }

//     /**
//      * Filters concurrently elements in the collection based on the outcome of the supplied function on each of the elements.
//      * @param A closure indicating whether to propagate the given element into the filtered collection
//      * @return A collection holding the allowed values
//      */
//     public final AbstractPAWrapper filter(final Closure cl) {
//         new PAWrapper(pa.all().withFilter(new ClosurePredicate(new CallClosure(cl))))
//     }
// }
