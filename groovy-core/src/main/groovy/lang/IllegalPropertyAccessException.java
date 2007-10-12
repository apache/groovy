/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.lang;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * An exception occurred if a dynamic property dispatch fails with a 
 * field not accessible.
 * 
 * @author <a href="mailto:blackdrag@uni.de">Jochen Theodorou</a>
 * @version $Revision$
 */
public class IllegalPropertyAccessException extends MissingPropertyException {
    
    private static String makeMessage(String propertyName, Class clazz, int modifiers, boolean isField) {
        String access = "private";
        if (Modifier.isProtected(modifiers)) access = "protected";
        if (Modifier.isPublic(modifiers)) access = "public";
        String propertyType = "property";
        if (isField) propertyType = "field";
        return  "Can not access the "+access+" "+propertyType+" "+propertyName+" in class "+clazz.getName();
    }
    
    public IllegalPropertyAccessException(String propertyName, Class clazz, int modifiers) {
        super(makeMessage(propertyName,clazz,modifiers,false),propertyName,clazz);
    }
    
    public IllegalPropertyAccessException(Field field, Class clazz) {
        super(makeMessage(field.getName(),clazz,field.getModifiers(),true),field.getName(),clazz);
    }
    
}
