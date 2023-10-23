package metalupo.hw2;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.document.Document;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;

public class SearchApp {
    public static void main(String[] args) {
        String indexPath = "C:\\Users\\Fero\\Desktop\\Homework2\\indici";
        try {
            Directory dir = FSDirectory.open(Paths.get(indexPath));
            IndexReader reader = DirectoryReader.open(dir);
            IndexSearcher searcher = new IndexSearcher(reader);

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Inserisci la tua query (es. titolo:<termine>, titolo:\"phrase query\", contenuto:<termine> o contenuto:\"phrase query\"):");
            String inputQuery = br.readLine();

            Query query = parseQuery(inputQuery);
            TopDocs results = searcher.search(query, 10);

            System.out.println("\nCorrispondenze trovate:");
            printLine();
            for (ScoreDoc sd : results.scoreDocs) {
                Document doc = searcher.doc(sd.doc);
                String title = highlightMatch(doc.get("titolo"), inputQuery);
                String contentSnippet = getSnippet(doc.get("contenuto"), inputQuery);
                System.out.println("| Nome file: " + title + ".txt");
                System.out.println("| Contenuto: " + contentSnippet);
                printLine();
            }

            reader.close();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private static Query parseQuery(String inputQuery) throws ParseException {
        try (StandardAnalyzer analyzer = new StandardAnalyzer()) {
            QueryParser parser;

            if (inputQuery.startsWith("titolo:")) {
                String term = inputQuery.substring(7).trim();
                parser = new QueryParser("titolo", analyzer);
                return parser.parse(QueryParser.escape(term));
            } else if (inputQuery.startsWith("contenuto:")) {
                String term = inputQuery.substring(10).trim();
                if (term.startsWith("\"") && term.endsWith("\"")) {
                    parser = new QueryParser("contenuto", analyzer);
                    return parser.parse(term);
                } else {
                    parser = new QueryParser("contenuto", analyzer);
                    return parser.parse(QueryParser.escape(term));
                }
            } else {
                throw new ParseException("La query deve iniziare con 'titolo:' o 'contenuto:'.");
            }
        }
    }

    private static String getSnippet(String content, String query) {
        int snippetLength = 100;
        String lowerCaseContent = content.toLowerCase();
        String lowerCaseQuery = query.toLowerCase().replace("titolo:", "").replace("contenuto:", "").trim();

        int index = lowerCaseContent.indexOf(lowerCaseQuery);
        if (index == -1) return content.substring(0, Math.min(content.length(), snippetLength)) + "...";

        int start = Math.max(0, index - snippetLength / 2);
        int end = Math.min(content.length(), index + snippetLength / 2);
        
        String snippet;
        if (start > 0) {
            snippet = "..." + content.substring(start, end);
        } else {
            snippet = content.substring(start, end);
        }
        
        if (end < content.length()) {
            snippet += "...";
        }

        return snippet.replace(lowerCaseQuery, "{" + lowerCaseQuery + "}");
    }

    private static String highlightMatch(String text, String query) {
        String lowerCaseText = text.toLowerCase();
        String lowerCaseQuery = query.toLowerCase().replace("titolo:", "").replace("contenuto:", "").trim();
        return lowerCaseText.contains(lowerCaseQuery) ? text.replace(lowerCaseQuery, "{" + lowerCaseQuery + "}") : text;
    }

    private static void printLine() {
        System.out.println("+----------------------------------------------+");
    }
}
