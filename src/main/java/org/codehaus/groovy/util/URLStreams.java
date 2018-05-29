package org.codehaus.groovy.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class URLStreams {
    private URLStreams() {

    }

    /**
     * Opens an {@link InputStream} reading from the given URL without
     * caching the stream. This prevents file descriptor leaks when reading
     * from file system URLs.
     *
     * @param url the URL to connect to
     * @return an input stream reading from the URL connection
     */
    public static InputStream openUncachedStream(URL url) throws IOException {
        URLConnection urlConnection = url.openConnection();
        urlConnection.setUseCaches(false);
        return urlConnection.getInputStream();
    }
}
