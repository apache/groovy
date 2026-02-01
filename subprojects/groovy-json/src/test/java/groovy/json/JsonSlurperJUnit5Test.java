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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for JsonSlurper class.
 */
class JsonSlurperJUnit5Test {

    @TempDir
    Path tempDir;

    @Test
    void testParseTextSimpleObject() {
        JsonSlurper slurper = new JsonSlurper();
        Object result = slurper.parseText("{\"name\":\"John\",\"age\":30}");
        
        assertNotNull(result);
        assertTrue(result instanceof Map);
        Map<?, ?> map = (Map<?, ?>) result;
        assertEquals("John", map.get("name"));
        assertEquals(30, map.get("age"));
    }

    @Test
    void testParseTextSimpleArray() {
        JsonSlurper slurper = new JsonSlurper();
        Object result = slurper.parseText("[1, 2, 3, 4, 5]");
        
        assertNotNull(result);
        assertTrue(result instanceof List);
        List<?> list = (List<?>) result;
        assertEquals(5, list.size());
        assertEquals(1, list.get(0));
        assertEquals(5, list.get(4));
    }

    @Test
    void testParseTextNestedObject() {
        JsonSlurper slurper = new JsonSlurper();
        Object result = slurper.parseText("{\"person\":{\"name\":\"Jane\",\"address\":{\"city\":\"NYC\"}}}");
        
        assertNotNull(result);
        Map<?, ?> map = (Map<?, ?>) result;
        Map<?, ?> person = (Map<?, ?>) map.get("person");
        assertEquals("Jane", person.get("name"));
        Map<?, ?> address = (Map<?, ?>) person.get("address");
        assertEquals("NYC", address.get("city"));
    }

    @Test
    void testParseTextWithNull() {
        JsonSlurper slurper = new JsonSlurper();
        Object result = slurper.parseText("{\"value\":null}");
        
        Map<?, ?> map = (Map<?, ?>) result;
        assertNull(map.get("value"));
    }

    @Test
    void testParseTextWithBoolean() {
        JsonSlurper slurper = new JsonSlurper();
        Object result = slurper.parseText("{\"active\":true,\"deleted\":false}");
        
        Map<?, ?> map = (Map<?, ?>) result;
        assertEquals(true, map.get("active"));
        assertEquals(false, map.get("deleted"));
    }

    @Test
    void testParseTextWithFloat() {
        JsonSlurper slurper = new JsonSlurper();
        Object result = slurper.parseText("{\"price\":19.99}");
        
        Map<?, ?> map = (Map<?, ?>) result;
        Object price = map.get("price");
        assertTrue(price instanceof Number);
        assertEquals(19.99, ((Number) price).doubleValue(), 0.001);
    }

    @Test
    void testParseTextEmptyObject() {
        JsonSlurper slurper = new JsonSlurper();
        Object result = slurper.parseText("{}");
        
        assertTrue(result instanceof Map);
        Map<?, ?> map = (Map<?, ?>) result;
        assertTrue(map.isEmpty());
    }

    @Test
    void testParseTextEmptyArray() {
        JsonSlurper slurper = new JsonSlurper();
        Object result = slurper.parseText("[]");
        
        assertTrue(result instanceof List);
        List<?> list = (List<?>) result;
        assertTrue(list.isEmpty());
    }

    @Test
    void testParseTextNullThrowsException() {
        JsonSlurper slurper = new JsonSlurper();
        assertThrows(IllegalArgumentException.class, () -> slurper.parseText(null));
    }

    @Test
    void testParseTextEmptyStringThrowsException() {
        JsonSlurper slurper = new JsonSlurper();
        assertThrows(IllegalArgumentException.class, () -> slurper.parseText(""));
    }

    @Test
    void testParseReader() {
        JsonSlurper slurper = new JsonSlurper();
        Reader reader = new StringReader("{\"test\":123}");
        Object result = slurper.parse(reader);
        
        assertNotNull(result);
        Map<?, ?> map = (Map<?, ?>) result;
        assertEquals(123, map.get("test"));
    }

    @Test
    void testParseReaderNullThrowsException() {
        JsonSlurper slurper = new JsonSlurper();
        assertThrows(IllegalArgumentException.class, () -> slurper.parse((Reader) null));
    }

    @Test
    void testParseInputStream() {
        JsonSlurper slurper = new JsonSlurper();
        InputStream is = new ByteArrayInputStream("{\"key\":\"value\"}".getBytes(StandardCharsets.UTF_8));
        Object result = slurper.parse(is);
        
        assertNotNull(result);
        Map<?, ?> map = (Map<?, ?>) result;
        assertEquals("value", map.get("key"));
    }

    @Test
    void testParseInputStreamWithCharset() {
        JsonSlurper slurper = new JsonSlurper();
        InputStream is = new ByteArrayInputStream("{\"key\":\"value\"}".getBytes(StandardCharsets.UTF_8));
        Object result = slurper.parse(is, "UTF-8");
        
        assertNotNull(result);
        Map<?, ?> map = (Map<?, ?>) result;
        assertEquals("value", map.get("key"));
    }

    @Test
    void testParseFile() throws IOException {
        JsonSlurper slurper = new JsonSlurper();
        Path jsonFile = tempDir.resolve("test.json");
        Files.writeString(jsonFile, "{\"file\":\"test\"}");
        
        Object result = slurper.parse(jsonFile.toFile());
        
        assertNotNull(result);
        Map<?, ?> map = (Map<?, ?>) result;
        assertEquals("test", map.get("file"));
    }

    @Test
    void testParseFileWithCharset() throws IOException {
        JsonSlurper slurper = new JsonSlurper();
        Path jsonFile = tempDir.resolve("test2.json");
        Files.writeString(jsonFile, "{\"file\":\"test2\"}");
        
        Object result = slurper.parse(jsonFile.toFile(), "UTF-8");
        
        assertNotNull(result);
        Map<?, ?> map = (Map<?, ?>) result;
        assertEquals("test2", map.get("file"));
    }

    @Test
    void testParsePath() throws IOException {
        JsonSlurper slurper = new JsonSlurper();
        Path jsonFile = tempDir.resolve("test3.json");
        Files.writeString(jsonFile, "{\"path\":\"testing\"}");
        
        Object result = slurper.parse(jsonFile);
        
        assertNotNull(result);
        Map<?, ?> map = (Map<?, ?>) result;
        assertEquals("testing", map.get("path"));
    }

    @Test
    void testGetAndSetType() {
        JsonSlurper slurper = new JsonSlurper();
        
        assertEquals(JsonParserType.CHAR_BUFFER, slurper.getType());
        
        slurper.setType(JsonParserType.INDEX_OVERLAY);
        assertEquals(JsonParserType.INDEX_OVERLAY, slurper.getType());
        
        slurper.setType(JsonParserType.LAX);
        assertEquals(JsonParserType.LAX, slurper.getType());
    }

    @Test
    void testGetAndSetChop() {
        JsonSlurper slurper = new JsonSlurper();
        
        assertFalse(slurper.isChop());
        
        slurper.setChop(true);
        assertTrue(slurper.isChop());
        
        slurper.setChop(false);
        assertFalse(slurper.isChop());
    }

    @Test
    void testGetAndSetLazyChop() {
        JsonSlurper slurper = new JsonSlurper();
        
        assertTrue(slurper.isLazyChop());
        
        slurper.setLazyChop(false);
        assertFalse(slurper.isLazyChop());
        
        slurper.setLazyChop(true);
        assertTrue(slurper.isLazyChop());
    }

    @Test
    void testGetAndSetCheckDates() {
        JsonSlurper slurper = new JsonSlurper();
        
        assertTrue(slurper.isCheckDates());
        
        slurper.setCheckDates(false);
        assertFalse(slurper.isCheckDates());
        
        slurper.setCheckDates(true);
        assertTrue(slurper.isCheckDates());
    }

    @Test
    void testGetAndSetMaxSizeForInMemory() {
        JsonSlurper slurper = new JsonSlurper();
        
        assertEquals(2000000, slurper.getMaxSizeForInMemory());
        
        slurper.setMaxSizeForInMemory(1000000);
        assertEquals(1000000, slurper.getMaxSizeForInMemory());
    }

    @Test
    void testFluentAPI() {
        JsonSlurper slurper = new JsonSlurper()
            .setType(JsonParserType.INDEX_OVERLAY)
            .setChop(true)
            .setLazyChop(false)
            .setCheckDates(false)
            .setMaxSizeForInMemory(500000);
        
        assertEquals(JsonParserType.INDEX_OVERLAY, slurper.getType());
        assertTrue(slurper.isChop());
        assertFalse(slurper.isLazyChop());
        assertFalse(slurper.isCheckDates());
        assertEquals(500000, slurper.getMaxSizeForInMemory());
    }

    @Test
    void testParseWithIndexOverlayType() {
        JsonSlurper slurper = new JsonSlurper().setType(JsonParserType.INDEX_OVERLAY);
        Object result = slurper.parseText("{\"type\":\"overlay\"}");
        
        Map<?, ?> map = (Map<?, ?>) result;
        assertEquals("overlay", map.get("type"));
    }

    @Test
    void testParseWithLaxType() {
        JsonSlurper slurper = new JsonSlurper().setType(JsonParserType.LAX);
        Object result = slurper.parseText("{\"type\":\"lax\"}");
        
        Map<?, ?> map = (Map<?, ?>) result;
        assertEquals("lax", map.get("type"));
    }

    @Test
    void testParseWithCharacterSourceType() {
        JsonSlurper slurper = new JsonSlurper().setType(JsonParserType.CHARACTER_SOURCE);
        Object result = slurper.parseText("{\"type\":\"source\"}");
        
        Map<?, ?> map = (Map<?, ?>) result;
        assertEquals("source", map.get("type"));
    }

    @Test
    void testParseStringWithEscapedCharacters() {
        JsonSlurper slurper = new JsonSlurper();
        Object result = slurper.parseText("{\"text\":\"line1\\nline2\\ttab\"}");
        
        Map<?, ?> map = (Map<?, ?>) result;
        String text = (String) map.get("text");
        assertTrue(text.contains("\n"));
        assertTrue(text.contains("\t"));
    }

    @Test
    void testParseStringWithUnicode() {
        JsonSlurper slurper = new JsonSlurper();
        Object result = slurper.parseText("{\"text\":\"Hello \\u0041\"}");
        
        Map<?, ?> map = (Map<?, ?>) result;
        assertEquals("Hello A", map.get("text"));
    }

    @Test
    void testParseLargeNumber() {
        JsonSlurper slurper = new JsonSlurper();
        Object result = slurper.parseText("{\"big\":9999999999999999999}");
        
        Map<?, ?> map = (Map<?, ?>) result;
        assertNotNull(map.get("big"));
    }

    @Test
    void testParseNegativeNumber() {
        JsonSlurper slurper = new JsonSlurper();
        Object result = slurper.parseText("{\"negative\":-42}");
        
        Map<?, ?> map = (Map<?, ?>) result;
        assertEquals(-42, map.get("negative"));
    }

    @Test
    void testParseScientificNotation() {
        JsonSlurper slurper = new JsonSlurper();
        Object result = slurper.parseText("{\"sci\":1.5e10}");
        
        Map<?, ?> map = (Map<?, ?>) result;
        Object sci = map.get("sci");
        assertTrue(sci instanceof Number);
        assertEquals(1.5e10, ((Number) sci).doubleValue(), 1e5);
    }

    @Test
    void testParseComplexStructure() {
        JsonSlurper slurper = new JsonSlurper();
        String json = "{\"users\":[{\"name\":\"Alice\",\"roles\":[\"admin\",\"user\"]},{\"name\":\"Bob\",\"roles\":[\"user\"]}]}";
        Object result = slurper.parseText(json);
        
        Map<?, ?> map = (Map<?, ?>) result;
        List<?> users = (List<?>) map.get("users");
        assertEquals(2, users.size());
        
        Map<?, ?> alice = (Map<?, ?>) users.get(0);
        assertEquals("Alice", alice.get("name"));
        List<?> aliceRoles = (List<?>) alice.get("roles");
        assertEquals(2, aliceRoles.size());
    }

    @Test
    void testJsonParserTypeValues() {
        JsonParserType[] types = JsonParserType.values();
        assertTrue(types.length >= 4);
        
        assertEquals(JsonParserType.CHAR_BUFFER, JsonParserType.valueOf("CHAR_BUFFER"));
        assertEquals(JsonParserType.INDEX_OVERLAY, JsonParserType.valueOf("INDEX_OVERLAY"));
        assertEquals(JsonParserType.LAX, JsonParserType.valueOf("LAX"));
        assertEquals(JsonParserType.CHARACTER_SOURCE, JsonParserType.valueOf("CHARACTER_SOURCE"));
    }
}
