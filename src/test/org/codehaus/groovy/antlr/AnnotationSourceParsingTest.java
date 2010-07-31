package org.codehaus.groovy.antlr;

import java.io.StringReader;

/**
 * Parsing annotations.
 *
 * @author <a href='mailto:the[dot]mindstorm[at]gmail[dot]com'>Alex Popescu</a>
 */
public class AnnotationSourceParsingTest extends SourceParserTest {
    public void testMultiLineAttributes() {
        StringReader reader = new StringReader(
                "class OtherSection\n"
                        + "{\n"
                        + "    @CollectionOfElements\n"
                        + "    @JoinTable\n"
                        + "    (\n"
                        + "        table=@Table(name=\"gaga\"),\n"
                        + "        joinColumns = @JoinColumn(name=\"BoyId\")\n"
                        + "    )\n"
                        + "    @Column(name=\"favoritepoupon\", \n"
                        + "nullable=false)\n"
                        + "    Set<String> questions = new HashSet<String> ()\n\n"
                        + "}");
        parse("testMultiLineAttributes", reader);
    }
}
