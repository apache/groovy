package groovy.util

import java.util.concurrent.ConcurrentHashMap

/**
 *
 * @author Guillaume Laforge
 */
class GroovyScriptEngineReloadingTest extends GroovyTestCase {
    GroovyScriptEngine gse;

    void setUp() {
        MapFileSystem.instance.registerMapFileSystem()
        gse = new GroovyScriptEngine([MapUrlConnection.URL_SCHEME] as String[])
        gse.config.minimumRecompilationInterval = 0
    }

    void testIsSourceNewer() {
        Binding binding = new Binding()
        int val = 0
        binding.setVariable("val", val)
        MapFileSystem.instance.modFile("s_1", "val = 1", new Date().time)
        gse.run("s_1", binding)

        assert binding.getVariable("val") == 1

        sleep 1000

        MapFileSystem.instance.modFile("s_1", "val = 2", new Date().time)
        gse.run("s_1", binding)

        assert binding.getVariable("val") == 2
    }
}

class MapFileEntry {
    String content
    long lutime

    public MapFileEntry(String content, long lutime) {
        this.content = content
        this.lutime = lutime
    }
}

@Singleton
class MapFileSystem {

    public final ConcurrentHashMap<String, MapFileEntry> fileCache = new ConcurrentHashMap<String, MapFileEntry>()
    private boolean registered = false

    void registerMapFileSystem() {
        if (!registered) {
            try {
                URL.setURLStreamHandlerFactory(new MapUrlFactory())
                registered = true
            } catch (Error e) { }
        }
    }

    void modFile(String name, String content, long lutime) {
        if (fileCache.containsKey(name)) {
            MapFileEntry sce = fileCache.get(name)
            sce.content = content
            sce.lutime = lutime
        } else {
            fileCache.put(name, new MapFileEntry(content, lutime))
        }
    }

    String getFilesrc(String name) {
        return fileCache.get(name).content
    }

    boolean fileExists(String name) {
        return fileCache.containsKey(name)
    }
}

class MapUrlHandler extends URLStreamHandler {

    MapUrlHandler() {
        super()
    }

    protected URLConnection openConnection(URL u) throws IOException {
        return new MapUrlConnection(u)
    }

    protected void parseURL(URL u, String spec, int start, int limit) {
        super.parseURL(u, spec, start, limit)
    }
}

class MapUrlConnection extends URLConnection {
    String getContentEncoding() {
        return CHARSET
    }

    Object getContent() throws IOException {
        return super.content
    }

    public static final String CHARSET = "UTF-8"
    public static final String URL_HOST = "local"
    public static final String URL_SCHEME = "map://" + URL_HOST + "/"

    public static final String PROTOCOL = "map"

    private String name

    InputStream getInputStream() throws IOException {
        // System.out.println(name+"\t"+MapFileSystem.fileCache.get(name).content);
        if (MapFileSystem.instance.fileCache.containsKey(name)) {
            String content = MapFileSystem.instance.fileCache.get(name).content
            return new ByteArrayInputStream(content.getBytes(CHARSET))
        } else {
            throw new IOException("file not found" + name)
        }
    }

    long getLastModified() {
        long lastmodified = 0
        if (MapFileSystem.instance.fileCache.containsKey(name)) {
            lastmodified = MapFileSystem.instance.fileCache.get(name).lutime
        }
        // System.out.println(name+"\t"+lastmodified);
        return lastmodified
    }

    URL getURL() {
        return super.getURL()
    }

    MapUrlConnection(URL url) {
        super(url)
        name = url.getFile()
        if (name.startsWith("/")) {
            name = name.substring(1)
        }
    }

    void connect() throws IOException { }
}

class MapUrlFactory implements URLStreamHandlerFactory {

    MapUrlFactory() {
        super()
    }

    URLStreamHandler createURLStreamHandler(String protocol) {
        if (MapUrlConnection.PROTOCOL.equals(protocol)) {
            return new MapUrlHandler()
        } else {
            return null
        }
    }
}
