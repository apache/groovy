import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.queryParser.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.FSDirectory

/**
 * Searcher: searches a Lucene index for a query passed as an argument
 *
 * @author Jeremy Rayner <groovy@ross-rayner.com>
 * based on examples in the wonderful 'Lucene in Action' book
 * by Erik Hatcher and Otis Gospodnetic ( http://www.lucenebook.com )
 *
 * requires a lucene-1.x.x.jar from http://lucene.apache.org
 */

if (args.size() != 2) {
    throw new Exception("Usage: groovy -cp lucene-1.4.3.jar Searcher <index dir> <query>")
}
def indexDir = new File(args[0]) // Index directory create by Indexer
def q = args[1] // Query string

if (!indexDir.exists() || !indexDir.directory) {
    throw new Exception("$indexDir does not exist or is not a directory")
}

def fsDir = FSDirectory.getDirectory(indexDir, false)
def is = new IndexSearcher(fsDir) // Open index

def query = QueryParser.parse(q, "contents", new StandardAnalyzer()) // Parse query
def start = new Date().time
def hits = is.search(query) // Search index
def end = new Date().time

println "Found ${hits.length()} document(s) (in ${end - start} milliseconds) that matched query '$q':"

for ( i in 0 ..< hits.length() ) {
    println(hits.doc(i)["filename"]) // Retrieve matching document and display filename
}
