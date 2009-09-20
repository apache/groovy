package org.codehaus.groovy.ant.vm5;

import java.util.ArrayList;
import java.util.List;

public class IncorrectGenericsUsage {
    private ArrayList<String> x = new ArrayList<String>();
    public void doIt ( final List<?> z ) {
        x = (ArrayList)z ;
    }
}