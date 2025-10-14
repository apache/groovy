// GPars - Groovy Parallel Systems
//
// Copyright © 2008–2012, 2014  The original author or authors
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

package groovyx.gpars.pa

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode

/**
 * The default ParallelArray wrapper class
 */
@CompileStatic(value = TypeCheckingMode.PASS)
final class PAWrapper<T> extends AbstractPAWrapper {
    def PAWrapper(final pa) { super(pa) }
}
