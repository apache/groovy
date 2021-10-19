package org.codehaus.groovy.tools;

import junit.framework.TestCase;
import java.util.Map;

public class GrapeUtilTest extends TestCase {

    public void testGetIvyParts1(){
        String allStr = "@M";
        Map<String, Object> ivyParts = GrapeUtil.getIvyParts(allStr);
        assert ivyParts.size() == 3;
    }

    public void testGetIvyParts2(){
        String allStr = "a@";
        Map<String, Object> ivyParts = GrapeUtil.getIvyParts(allStr);
        assert ivyParts.size() == 2;
    }

    public void testGetIvyParts3(){
        String allStr = "@";
        Map<String, Object> ivyParts = GrapeUtil.getIvyParts(allStr);
        assert ivyParts.size() == 2;
    }

    public void testGetIvyParts4(){
        String allStr = ":k:@M";
        Map<String, Object> ivyParts = GrapeUtil.getIvyParts(allStr);
        assert ivyParts.size() == 4;
    }

}
