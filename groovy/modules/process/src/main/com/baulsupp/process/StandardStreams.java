package com.baulsupp.process;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

// TODO don't let stdout, stderr be closed
public class StandardStreams {
  public static class InSource extends Source {
    public void connect(Sink sink) {
      InputStream is = new FileInputStream(FileDescriptor.in);
      
      if (sink.providesStream()) {
        // TODO feels better if this is line based, rather than fixed buffer size based
        // TODO handle result
        IOUtil.pumpAsync(is, sink.getStream());
      } else if (sink.receivesStream()) {
        sink.setStream(is);  
      } else {
        throw new UnsupportedOperationException("sink type unknown");  
      }
    }
  }
  
  public static Source stdin() {
    return new InSource();
  }
  
  public static class ErrSink extends Sink {
    public OutputStream getStream() {
      return new FileOutputStream(FileDescriptor.err) {
        public void close() throws IOException {
          // ignore close
          flush();  
        }  
      };
    }

    public boolean providesStream() {
      return true;
    }
  }
  
  public static Sink stderr() {
    return new ErrSink();
  }
  
  public static class OutSink extends Sink {
    public OutputStream getStream() {
      return new FileOutputStream(FileDescriptor.out) {
        public void close() throws IOException {
          // ignore close
          flush();  
        }  
      };
    }

    public boolean providesStream() {
      return true;
    }
  }
  
  public static Sink stdout() {
    return new OutSink();
  }

}
