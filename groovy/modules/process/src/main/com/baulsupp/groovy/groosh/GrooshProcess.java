package com.baulsupp.groovy.groosh;

import java.io.File;
import java.io.IOException;

import com.baulsupp.process.FileStreams;
import com.baulsupp.process.Sink;
import com.baulsupp.process.Source;
import com.baulsupp.process.StandardStreams;
import com.baulsupp.process.StringStreams;

// TODO class should not be reentrant 
// that is if output is already set, don't let it be done twice.
public abstract class GrooshProcess {  
  protected abstract Sink getSink();
  
  protected abstract Source getSource();
  
  public String toStringOut() throws IOException {
    StringStreams.StringSink sink = StringStreams.stringSink();

    getSource().connect(sink);
    start();
    
    return sink.toString();
  }

  // TODO should this be asynchronous, would be less obvious though!
  public void toFile(File f) throws IOException {
    Sink sink = FileStreams.sink(f, false);
  
    getSource().connect(sink);
    start();
    
    waitForExit();
  }
  
  // needs to be asynchronous so they can continue the chain
  public GrooshProcess pipeTo(GrooshProcess process) throws IOException {
    getSource().connect(process.getSink());
    
    start();
    
    // return other process so chaining is possible
    return process;
  }

  // TODO should this be asynchronous, would be less obvious though!
  public void toStdOut() throws IOException {
    Sink sink = StandardStreams.stdout();
  
    getSource().connect(sink);
    start();
    
    waitForExit();
  }
  
  public GrooshProcess fromStdIn() throws IOException {
    Source source = StandardStreams.stdin();
    
    source.connect(getSink());  
    
    return this;
  }
  
  public GrooshProcess fromString(String s) throws IOException {
    Source source = StringStreams.stringSource(s);
    
    source.connect(getSink());  
    
    return this;
  }
  
  public abstract void start() throws IOException;
  
  public abstract void waitForExit() throws IOException;
}
