package com.baulsupp.groovy.groosh;

import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;

import groovy.lang.Closure;

public class LineClosureProcess extends StreamClosureProcess {
  public LineClosureProcess(Closure closure) {
    super(closure);
  }

  protected void process(final InputStream is, final OutputStream os) throws IOException {
    BufferedReader ris = new BufferedReader(new InputStreamReader(is));
    Writer wos = new PrintWriter(new OutputStreamWriter(os, "ISO-8859-1"));
    
    String line;
    
    List l = new ArrayList();
    
    while ((line = ris.readLine()) != null) {
      l.clear();
      l.add(line);
      l.add(wos);
      closure.call(l);
      wos.flush();
    }
  }
}
