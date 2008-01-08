/*
 * $Id$version Mar 8, 2004 1:36:31 AM $user Exp $
 * 
 * Copyright 2003 (C) Sam Pullara. All Rights Reserved.
 * 
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met: 1. Redistributions of source code must retain
 * copyright statements and notices. Redistributions must also contain a copy
 * of this document. 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the distribution. 3.
 * The name "groovy" must not be used to endorse or promote products derived
 * from this Software without prior written permission of The Codehaus. For
 * written permission, please contact info@codehaus.org. 4. Products derived
 * from this Software may not be called "groovy" nor may "groovy" appear in
 * their names without prior written permission of The Codehaus. "groovy" is a
 * registered trademark of The Codehaus. 5. Due credit should be given to The
 * Codehaus - http://groovy.codehaus.org/
 * 
 * THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 *  
 */
package groovy.text;

import junit.framework.TestCase;
import org.codehaus.groovy.control.CompilationFailedException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author sam
 */
public class TemplateTest extends TestCase {

    public void testMixedTemplateText() throws CompilationFailedException, ClassNotFoundException, IOException {
        Template template1 = new SimpleTemplateEngine().createTemplate("<%= \"test\" %> of expr and <% test = 1 %>${test} script.");
        assertEquals("test of expr and 1 script.", template1.make().toString());

        Template template2 = new GStringTemplateEngine().createTemplate("<%= \"test\" %> of expr and <% test = 1 %>${test} script.");
        assertEquals("test of expr and 1 script.", template2.make().toString());

    }

    public void testBinding() throws CompilationFailedException, ClassNotFoundException, IOException {
        Map binding = new HashMap();
        binding.put("sam", "pullara");

        Template template1 = new SimpleTemplateEngine().createTemplate("<%= sam %><% print sam %>");
        assertEquals("pullarapullara", template1.make(binding).toString());

        Template template2 = new GStringTemplateEngine().createTemplate("<%= sam %><% out << sam %>");
        assertEquals("pullarapullara", template2.make(binding).toString());

        Template template3 = new GStringTemplateEngine().createTemplate("<%= sam + \" \" + sam %><% out << sam %>");
        assertEquals("pullara pullarapullara", template3.make(binding).toString());
    }

}
