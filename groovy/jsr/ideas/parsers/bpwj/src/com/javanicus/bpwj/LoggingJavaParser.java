package com.javanicus.bpwj;

import sjm.parse.Alternation;
import sjm.parse.Sequence;

import java.io.PrintStream;

public class LoggingJavaParser extends JavaParser {
    public static String lastIn;
    public static String lastOut;
    public static int level = 0;
    public static PrintStream out;

    public Alternation alternation(String name) {
        return new LoggingAlternation(name);
    }

    public Sequence sequence(String name) {
        return new LoggingTrackSequence(name);
    }
}
