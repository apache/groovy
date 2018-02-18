package org.apache.groovy.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static org.apache.groovy.util.Maps.of;
import static org.junit.Assert.*;

public class MapsTest {

    @Test
    public void inverse() {
        Map<String, Integer> map = Maps.of("a", 1, "b", 2, "c", 3);
        Map<Integer, String> inversedMap = Maps.inverse(map);

        Assert.assertEquals(map.size(), inversedMap.size());
        for (Map.Entry<Integer, String> entry : inversedMap.entrySet()) {
            Assert.assertEquals(map.get(entry.getValue()), entry.getKey());
        }

        try {
            Maps.inverse(Maps.of("a", 1, "b", 2, "c", 2));
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("duplicated key found: 2"));
        }

        Map<Integer, String> inversedMap2 = Maps.inverse(Maps.of("a", 1, "b", 2, "c", 2), true);
        Assert.assertEquals(2, inversedMap2.size());
        Assert.assertEquals("a", inversedMap2.get(1));
        Assert.assertEquals("c", inversedMap2.get(2));
    }
}