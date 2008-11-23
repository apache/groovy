/*
$Id$

Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

Redistribution and use of this software and associated documentation
("Software"), with or without modification, are permitted provided
that the following conditions are met:

1. Redistributions of source code must retain copyright
statements and notices.  Redistributions must also contain a
copy of this document.

2. Redistributions in binary form must reproduce the
above copyright notice, this list of conditions and the
following disclaimer in the documentation and/or other
materials provided with the distribution.

3. The name "groovy" must not be used to endorse or promote
products derived from this Software without prior written
permission of The Codehaus.  For written permission,
please contact info@codehaus.org.

4. Products derived from this Software may not be called "groovy"
nor may "groovy" appear in their names without prior written
permission of The Codehaus. "groovy" is a registered
trademark of The Codehaus.

5. Due credit should be given to The Codehaus -
http://groovy.codehaus.org/

THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
OF THE POSSIBILITY OF SUCH DAMAGE.

*/

package groovy.xml;

import org.codehaus.groovy.classgen.TestSupport;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;

/**
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public abstract class TestXmlSupport extends TestSupport {

    protected void dump(Node node) throws IOException {
        XmlUtil.serialize((Element) node, System.out);
    }

    protected SAXBuilder createSAXBuilder() throws IOException {
        return new SAXBuilder(new LoggingDefaultHandler());
    }

    private static class LoggingDefaultHandler extends DefaultHandler {
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            System.out.println("Start Element: " + localName);
        }
        public void endElement(String uri, String localName, String qName) throws SAXException {
            System.out.println("End Element: " + localName);
        }
        public void characters(char ch[], int start, int length) throws SAXException {
            System.out.println("Characters: " + new String(ch).substring(start, start + length - 1));
        }
    }
}
