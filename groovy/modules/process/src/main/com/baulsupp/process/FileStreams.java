package com.baulsupp.process;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class FileStreams {
  public static class FileSource extends Source {
    private File file;
    private FileInputStream is;

    public FileSource(File f) throws FileNotFoundException {
      this.file = f;  
      this.is = new FileInputStream(f);
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
  
  public static Source source(File file) throws FileNotFoundException {
    return new FileSource(file);
  }
  
  public static class FileSink extends Sink {
    private File file;
    private FileOutputStream os;

    public FileSink(File f, boolean append) throws FileNotFoundException {
      this.file = f;  
      this.os = new FileOutputStream(f, append);
    }
    
    public OutputStream getStream() {
      return os;
    }

    public boolean providesStream() {
      return true;
    }
  }
  
  public static Sink sink(File file, boolean append) throws FileNotFoundException {
    return new FileSink(file, append);
  }
}
