// GPars - Groovy Parallel Systems
//
// Copyright © 2008-10  The original author or authors
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

package groovyx.gpars.csp

public interface JCSPCopy {

    /**
     * The interface <code>JCSPCopy</code> is used to define a <code>copy</code> method
     * that is used to make a deep copy of an object.  In parallel systems an <code>object</code>
     * is used to encapsulate data only.  When an <code>object</code> is communicated from one process
     * to another then an object reference  is passed from the outputting process to the inputting one,
     * if the processes are on the same processor.  This would mean that two processes could modify the
     * same object in parallel, which of course is very dangerous.  One of the processes has to make
     * a copy of the object if they are both to work on the same object at the same time.  This can be
     * achieved by always declaring new objects for each iteration of a process, which is wasteful of
     * memory, or by copying the object.  The interface <code>JCSPCopy</code> gives the basic
     * definition of such a <code>copy</code> method.
     *
     * If an <code>object</code> is communicated over a network channel there is no need to make
     * a copy as the <code>object</code> has to implement <code>Serializable</code> and a copy
     * is made by the underlying system.  A processor cannot access the memory space of
     * another processor.
     * <p>Company: Napier University</p>
     *
     * @author Jon Kerridge, Ken Barclay, John Savage
     * @version 1.0
     *
     */
    public abstract copy()
}

