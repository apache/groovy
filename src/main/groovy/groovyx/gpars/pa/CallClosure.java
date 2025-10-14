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

// package groovyx.gpars.pa;

// import groovy.lang.Closure;

// /**
//  * A call-forwarding closure
//  *
//  * @author Vaclav Pech
//  */
// public final class CallClosure<V> extends Closure<V> {
//     private final Closure<V> target;
//     private static final long serialVersionUID = 209099114666842715L;

//     public CallClosure(final Closure<V> target) {
//         super(target.getOwner());
//         this.target = target;
//     }

//     @Override
//     public V call(final Object[] args) {
//         return target.call(args);
//     }

//     @Override
//     public V call() {
//         return target.call();
//     }

//     @Override
//     public V call(final Object arguments) {
//         return target.call(arguments);
//     }

//     @SuppressWarnings({"EmptyMethod"})
//     @Override
//     public Object clone() {
//         return super.clone();
//     }
// }
