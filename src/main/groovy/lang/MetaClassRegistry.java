/*
 * Created on Feb 9, 2007
 *
 * Copyright 2007 John G. Wilson
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package groovy.lang;


public interface MetaClassRegistry {
    /*
     * The intialisation part of this is temporary
     * eventually we will support plug in implementations
     */
    final MetaClassRegistry registry  = new MetaClassRegistryImpl();
    
    /*
     * The main function of the Registry
     * If a Metaclass exists then return it
     * otherwise create one, put it in the Registry and return it
     */
    MetaClass getMetaClass(Class theClass);
    
    /*
     * Do we really want these two?
     */
    void setMetaClass(Class theClass, MetaClass theMetaClass);
    void removeMetaClass(Class theClass);
 }
