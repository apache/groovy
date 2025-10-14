// GPars - Groovy Parallel Systems
//
// Copyright © 2014  The original author or authors
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

package groovyx.gpars.streams;

import java.util.function.Consumer;

import groovy.lang.Closure;

/**
 *  A wrapper around a Groovy Closure to create a Java BinaryOperator suitable for use in a Streams reduction.
 *
 *  @author Russel Winder
 */
public class ClosureConsumer<T> implements Consumer<T> {

  private final Closure<?> code;

  public ClosureConsumer(final Closure<?> code) {
    this.code = code;
  }

  @Override
  public void accept(final T a) {
    final Object[] args = {a};
    code.call(args);
  }

}
