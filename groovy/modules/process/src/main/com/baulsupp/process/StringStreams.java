package com.baulsupp.process;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import EDU.oswego.cs.dl.util.concurrent.FutureResult;

public class StringStreams {
  public static class StringSink extends Sink {
    private ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private FutureResult result = new FutureResult();
  
    public boolean receivesStream() {
      return true;
    }

    public void setStream(InputStream is) {
      result = IOUtil.pumpAsync(is, baos);
    }
  
    public String toString() {
      try {
        result.get();
      } catch (Exception e) {
        // TODO handle better
        throw new RuntimeException(e);
      }
    
      try {
        return baos.toString("ISO-8859-1");
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException(e);
      } 
    }
  }
  
  public static StringSink stringSink() {
    return new StringSink();  
  }
  
  public static class StringSource extends Source {
    private InputStream is;

    public StringSource(String s) {
      byte[] buffy;
      
      try {
        buffy = s.getBytes("ISO-8859-1");
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException(e);
      }
      
      this.is = new ByteArrayInputStream(buffy);
    }
    
    public void connect(Sink sink) {      
      if (sink.providesStream()) {
        // TODO handle result
        IOUtil.pumpAsync(is, sink.getStream());
      } else if (sink.receivesStream()) {
        sink.setStream(is);  
      } else {
        throw new UnsupportedOperationException("sink type unknown");  
      }
    }
  }
  
  public static StringSource stringSource(String s) {
    return new StringSource(s);  
  }
}
