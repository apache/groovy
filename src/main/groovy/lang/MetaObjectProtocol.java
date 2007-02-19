/*
 * Created on Feb 12, 2007
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

public interface MetaObjectProtocol {
    Class getTheClass();
    Object invokeConstructor(Object[] arguments);
    Object invokeMethod(Object object, String methodName, Object[] arguments);
    Object invokeStaticMethod(Object object, String methodName, Object[] arguments);
    Object getProperty(Object object, String property);
    void setProperty(Object object, String property, Object newValue);
    Object getAttribute(Object object, String attribute);
    void setAttribute(Object object, String attribute, Object newValue);
}
