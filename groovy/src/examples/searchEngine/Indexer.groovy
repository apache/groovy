import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.index.IndexWriter

/**
 * Indexer: traverses a file system and indexes .txt files
 *
 * @author Jeremy Rayner <groovy@ross-rayner.com>
 * based on examples in the wonderful 'Lucene in Action' book
 * by Erik Hatcher and Otis Gospodnetic ( http://www.lucenebook.com )
 *
 * requires a lucene-1.x.x.jar from http://lucene.apache.org
 */

if (args.size() != 2 ) {
    throw new Exception("Usage: groovy -cp lucene-1.4.3.jar Indexer <index dir> <data dir>")
}
def indexDir = new File(args[0]) // Create Lucene index in this directory
def dataDir = new File(args[1]) // Index files in this directory

def start = new Date().time
def numIndexed = index(indexDir, dataDir)
def end = new Date().time

println "Indexing $numIndexed files took ${end - start} milliseconds"

def index(indexDir, dataDir) {
    if (!dataDir.exists() || !dataDir.directory) {
        throw new IOException("$dataDir does not exist or is not a directory")
    }
    def writer = new IndexWriter(indexDir, new StandardAnalyzer(), true) // Create Lucene index
    writer.useCompoundFile = false

    dataDir.eachFileRecurse {
        if (it.name =~ /.txt$/) { // Index .txt files only
            indexFile(writer,it)
        }
    }
    def numIndexed = writer.docCount()
    writer.optimize()
    writer.close() // Close index
    return numIndexed
}

void indexFile(writer, f) {
    if (f.hidden || !f.exists() || !f.canRead() || f.directory) { return }

    println "Indexing $f.canonicalPath"
    def doc = new Document()

    // Construct a Field that is tokenized and indexed, but is not stored in the index verbatim.
    doc.add(Field.Text("contents", new FileReader(f)))

    // Construct a Field that is not tokenized, but is indexed and stored.
    doc.add(Field.Keyword("filename",f.canonicalPath))

    writer.addDocument(doc) // Add document to Lucene index
}