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
// import org.codehaus.groovy.runtime.InvokerHelper;

// /**
//  * Represents a {a, b -&gt; a + b} closure
//  *
//  * @author Vaclav Pech
//  */
// public final class SumClosure extends Closure {
//     private static final long serialVersionUID = 209099114666842715L;

//     private static final SumClosure ourInstance = new SumClosure();

//     public static SumClosure getInstance() {
//         return ourInstance;
//     }

//     private SumClosure() {
//         super(null);
//     }

//     @Override
//     public Object call(final Object[] args) {
//         return InvokerHelper.invokeMethod(args[0], "plus", args[1]);
//     }
// }
