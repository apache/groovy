package com.baulsupp.process;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DevNull {
  public static class NullSink extends Sink {
    public OutputStream getStream() {
      return new OutputStream() {
        public void write(int b) throws IOException {
          // do nothing
        }
      };
    }
    
    public void setStream(final InputStream is) {
      // TODO handle result/exception?
      IOUtil.pumpAsync(is, getStream());
    }

    public boolean providesStream() {
      return true;
    }

    public boolean receivesStream() {
      return true;
    }
  }
  
  public static class NullSource extends Source {
    public void connect(Sink sink) {
      if (sink.providesStream()) {
        try {
          sink.getStream().close();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }  
      } else if (sink.receivesStream()) {
        sink.setStream(new ByteArrayInputStream(new byte[0]));  
      } else {
        throw new UnsupportedOperationException("sink type unknown");  
      }
    }
  }
  
  public static Sink createSink() {
    return new NullSink();
  }
  
  public static Source createSource() {
    return new NullSource();
  }
}
