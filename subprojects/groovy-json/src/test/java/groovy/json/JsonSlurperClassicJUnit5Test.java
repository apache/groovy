/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.json;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for JsonSlurperClassic class.
 */
class JsonSlurperClassicJUnit5Test {

    private JsonSlurperClassic slurper;

    @BeforeEach
    void setUp() {
        slurper = new JsonSlurperClassic();
    }

    // parseText tests
    @Test
    void testParseTextWithNull() {
        assertThrows(IllegalArgumentException.class, () -> slurper.parseText(null));
    }

    @Test
    void testParseTextWithEmptyString() {
        assertThrows(IllegalArgumentException.class, () -> slurper.parseText(""));
    }

    @Test
    void testParseTextSimpleObject() {
        Map result = (Map) slurper.parseText("{\"name\":\"John\"}");
        assertEquals("John", result.get("name"));
    }

    @Test
    void testParseTextSimpleArray() {
        List result = (List) slurper.parseText("[1, 2, 3]");
        assertEquals(3, result.size());
        assertEquals(1, result.get(0));
    }

    @Test
    void testParseTextNestedObject() {
        String json = "{\"person\":{\"name\":\"John\",\"age\":30}}";
        Map result = (Map) slurper.parseText(json);
        Map person = (Map) result.get("person");
        assertEquals("John", person.get("name"));
        assertEquals(30, person.get("age"));
    }

    @Test
    void testParseTextNestedArray() {
        String json = "{\"matrix\":[[1,2],[3,4]]}";
        Map result = (Map) slurper.parseText(json);
        List matrix = (List) result.get("matrix");
        assertEquals(2, matrix.size());
        assertEquals(List.of(1, 2), matrix.get(0));
    }

    @Test
    void testParseTextWithAllTypes() {
        String json = "{\"string\":\"hello\",\"number\":42,\"float\":3.14,\"bool\":true,\"null\":null}";
        Map result = (Map) slurper.parseText(json);
        assertEquals("hello", result.get("string"));
        assertEquals(42, result.get("number"));
        // JsonSlurperClassic uses BigDecimal for decimals
        assertEquals(new java.math.BigDecimal("3.14"), result.get("float"));
        assertEquals(true, result.get("bool"));
        assertNull(result.get("null"));
    }

    @Test
    void testParseTextEmptyObject() {
        Map result = (Map) slurper.parseText("{}");
        assertTrue(result.isEmpty());
    }

    @Test
    void testParseTextEmptyArray() {
        List result = (List) slurper.parseText("[]");
        assertTrue(result.isEmpty());
    }

    @Test
    void testParseTextArrayOfObjects() {
        String json = "[{\"id\":1},{\"id\":2}]";
        List result = (List) slurper.parseText(json);
        assertEquals(2, result.size());
        assertEquals(1, ((Map) result.get(0)).get("id"));
    }

    @Test
    void testParseTextWithEscapedCharacters() {
        String json = "{\"message\":\"Hello\\nWorld\"}";
        Map result = (Map) slurper.parseText(json);
        assertEquals("Hello\nWorld", result.get("message"));
    }

    @Test
    void testParseTextWithUnicode() {
        String json = "{\"greeting\":\"Hello \\u4e16\\u754c\"}";
        Map result = (Map) slurper.parseText(json);
        assertEquals("Hello 世界", result.get("greeting"));
    }

    @Test
    void testParseTextInvalidJson() {
        assertThrows(JsonException.class, () -> slurper.parseText("not json"));
    }

    @Test
    void testParseTextInvalidStartToken() {
        assertThrows(JsonException.class, () -> slurper.parseText("\"just a string\""));
    }

    // parse(Reader) tests
    @Test
    void testParseReader() {
        StringReader reader = new StringReader("{\"key\":\"value\"}");
        Map result = (Map) slurper.parse(reader);
        assertEquals("value", result.get("key"));
    }

    @Test
    void testParseReaderArray() {
        StringReader reader = new StringReader("[1, 2, 3]");
        List result = (List) slurper.parse(reader);
        assertEquals(3, result.size());
    }

    // parse(File) tests
    @Test
    void testParseFile(@TempDir Path tempDir) throws Exception {
        Path jsonFile = tempDir.resolve("test.json");
        Files.writeString(jsonFile, "{\"name\":\"test\"}");
        
        Map result = (Map) slurper.parse(jsonFile.toFile());
        assertEquals("test", result.get("name"));
    }

    @Test
    void testParseFileWithCharset(@TempDir Path tempDir) throws Exception {
        Path jsonFile = tempDir.resolve("test.json");
        Files.write(jsonFile, "{\"name\":\"test\"}".getBytes(StandardCharsets.UTF_8));
        
        Map result = (Map) slurper.parse(jsonFile.toFile(), "UTF-8");
        assertEquals("test", result.get("name"));
    }

    @Test
    void testParseFileNonExistent() {
        File nonExistent = new File("non_existent_file.json");
        assertThrows(JsonException.class, () -> slurper.parse(nonExistent));
    }

    // Complex JSON tests
    @Test
    void testParseComplexStructure() {
        String json = """
            {
                "users": [
                    {"name": "Alice", "age": 25, "active": true},
                    {"name": "Bob", "age": 30, "active": false}
                ],
                "metadata": {
                    "total": 2,
                    "page": 1
                }
            }
            """;
        Map result = (Map) slurper.parseText(json);
        
        List users = (List) result.get("users");
        assertEquals(2, users.size());
        
        Map alice = (Map) users.get(0);
        assertEquals("Alice", alice.get("name"));
        assertEquals(25, alice.get("age"));
        assertEquals(true, alice.get("active"));
        
        Map metadata = (Map) result.get("metadata");
        assertEquals(2, metadata.get("total"));
    }

    @Test
    void testParseNegativeNumbers() {
        String json = "{\"value\":-42,\"float\":-3.14}";
        Map result = (Map) slurper.parseText(json);
        assertEquals(-42, result.get("value"));
        // JsonSlurperClassic uses BigDecimal for decimals
        assertEquals(new java.math.BigDecimal("-3.14"), result.get("float"));
    }

    @Test
    void testParseScientificNotation() {
        String json = "{\"value\":1.23e10}";
        Map result = (Map) slurper.parseText(json);
        // JsonSlurperClassic uses BigDecimal for decimal notation
        assertEquals(new java.math.BigDecimal("1.23E+10"), result.get("value"));
    }

    @Test
    void testParseBooleans() {
        String json = "{\"yes\":true,\"no\":false}";
        Map result = (Map) slurper.parseText(json);
        assertEquals(true, result.get("yes"));
        assertEquals(false, result.get("no"));
    }

    @Test
    void testParseMixedArray() {
        String json = "[1, \"two\", true, null, {\"key\":\"value\"}]";
        List result = (List) slurper.parseText(json);
        assertEquals(5, result.size());
        assertEquals(1, result.get(0));
        assertEquals("two", result.get(1));
        assertEquals(true, result.get(2));
        assertNull(result.get(3));
        assertTrue(result.get(4) instanceof Map);
    }

    @Test
    void testParseWithTrailingCommaInObject() {
        // JsonSlurperClassic appears to be lenient with trailing commas
        Map result = (Map) slurper.parseText("{\"key\":\"value\",}");
        assertEquals("value", result.get("key"));
    }

    @Test
    void testParseWithTrailingCommaInArray() {
        // JsonSlurperClassic appears to be lenient with trailing commas
        List result = (List) slurper.parseText("[1, 2, 3,]");
        assertEquals(3, result.size());
    }

    @Test
    void testParseDeepNesting() {
        String json = "{\"a\":{\"b\":{\"c\":{\"d\":{\"e\":\"deep\"}}}}}";
        Map result = (Map) slurper.parseText(json);
        Map a = (Map) result.get("a");
        Map b = (Map) a.get("b");
        Map c = (Map) b.get("c");
        Map d = (Map) c.get("d");
        assertEquals("deep", d.get("e"));
    }

    @Test
    void testParseDeepArrayNesting() {
        String json = "[[[[\"deep\"]]]]";
        List result = (List) slurper.parseText(json);
        List l1 = (List) result.get(0);
        List l2 = (List) l1.get(0);
        List l3 = (List) l2.get(0);
        assertEquals("deep", l3.get(0));
    }

    @Test
    void testParseWhitespace() {
        String json = "  {  \"key\"  :  \"value\"  }  ";
        Map result = (Map) slurper.parseText(json);
        assertEquals("value", result.get("key"));
    }

    @Test
    void testParseMultilineJson() {
        String json = """
            {
                "key": "value"
            }
            """;
        Map result = (Map) slurper.parseText(json);
        assertEquals("value", result.get("key"));
    }

    @Test
    void testParseLargeNumbers() {
        String json = "{\"big\":9999999999999999999}";
        Map result = (Map) slurper.parseText(json);
        assertNotNull(result.get("big"));
    }

    @Test
    void testParseZero() {
        String json = "{\"zero\":0}";
        Map result = (Map) slurper.parseText(json);
        assertEquals(0, result.get("zero"));
    }

    @Test
    void testParseEmptyStringValue() {
        String json = "{\"empty\":\"\"}";
        Map result = (Map) slurper.parseText(json);
        assertEquals("", result.get("empty"));
    }

    @Test
    void testParseSpecialCharactersInString() {
        String json = "{\"special\":\"tab\\there\\nnewline\"}";
        Map result = (Map) slurper.parseText(json);
        assertEquals("tab\there\nnewline", result.get("special"));
    }

    @Test
    void testParseQuoteInString() {
        String json = "{\"quote\":\"say \\\"hello\\\"\"}";
        Map result = (Map) slurper.parseText(json);
        assertEquals("say \"hello\"", result.get("quote"));
    }

    @Test
    void testParseBackslashInString() {
        String json = "{\"path\":\"C:\\\\Users\\\\test\"}";
        Map result = (Map) slurper.parseText(json);
        assertEquals("C:\\Users\\test", result.get("path"));
    }

    @Test
    void testParseSlashInString() {
        String json = "{\"url\":\"http:\\/\\/example.com\"}";
        Map result = (Map) slurper.parseText(json);
        assertEquals("http://example.com", result.get("url"));
    }
}
