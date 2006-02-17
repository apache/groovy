/* Copyright 2004-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.grails.web.binding;

import org.apache.commons.lang.StringUtils;

import java.beans.PropertyEditorSupport;
import java.util.Currency;

/**
 * A Property editor for converting instances of java.util.Currency
 * 
 * @author Graeme Rocher
 * @since 14-Feb-2006
 */
public class CurrencyEditor extends PropertyEditorSupport {
    public void setAsText(String text) throws IllegalArgumentException {
        if(StringUtils.isBlank(text)) {
            setValue(null);
        }
        try {
            setValue(Currency.getInstance(text));
        } catch (IllegalArgumentException e) {
            // ignore and just set to null
            setValue(null);
        }
    }

    public String getAsText() {
        Currency c = (Currency)getValue();
        if(c == null) {
            return "";
        }
        else {
            return c.getCurrencyCode();
        }
    }
}
