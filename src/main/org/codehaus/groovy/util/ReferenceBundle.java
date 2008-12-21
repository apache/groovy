/*  

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package org.codehaus.groovy.util;

import java.lang.ref.ReferenceQueue;

public class ReferenceBundle{
    private ReferenceManager manager;
    private ReferenceType type;
    public ReferenceBundle(ReferenceManager manager, ReferenceType type){
        this.manager = manager;
        this.type = type;
    }
    public ReferenceType getType() {
        return type;
    }
    public ReferenceManager getManager() {
        return manager;
    }      
    
    private final static ReferenceBundle softReferences, weakReferences;
    static {
        ReferenceQueue queue = new ReferenceQueue();
        ReferenceManager callBack = ReferenceManager.createCallBackedManager(queue);
        ReferenceManager manager  = ReferenceManager.createThresholdedIdlingManager(queue, callBack, 5000);
        softReferences = new ReferenceBundle(manager, ReferenceType.SOFT);
        weakReferences = new ReferenceBundle(manager, ReferenceType.WEAK);
    }

    public static ReferenceBundle getSoftBundle() {
        return softReferences;
    }
    
    public static ReferenceBundle getWeakBundle() {
        return weakReferences;
    }
}