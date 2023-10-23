package metalupo.hw2;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.codecs.simpletext.SimpleTextCodec;

public class App {    
    public static void main( String[] args ) {
        String docsPath = "C:\\Users\\Fero\\Desktop\\Homework2\\documenti";
        String indexPath = "C:\\Users\\Fero\\Desktop\\Homework2\\indici";

        try {
            Directory dir = FSDirectory.open(Paths.get(indexPath));

            CharArraySet stopWords = new CharArraySet(Arrays.asList(
                // Preposizioni
                "di", "a", "da", "in", "con", "su", "per", "tra", "fra",
                // Articoli determinativi
                "il", "lo", "la", "i", "gli", "le",
                // Articoli indeterminativi
                "un", "uno", "una"
            ), true);

            StandardAnalyzer analyzer = new StandardAnalyzer(stopWords);

            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setCodec(new SimpleTextCodec());

            IndexWriter writer = new IndexWriter(dir, iwc);
            
            // Registra l'orario di inizio
            long startTime = System.nanoTime();

            File docsDirectory = new File(docsPath);
            for (File file : docsDirectory.listFiles()) {
                indexDocs(writer, file);
            }
            writer.commit();

            // Registra l'orario di fine e calcola il tempo trascorso
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000_000; // Converti in millisecondi
            
            System.out.println("Tempo di indicizzazione: " + duration + " millisecondi");
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void indexDocs(final IndexWriter writer, File file) throws IOException {
        if (file.canRead() && file.getName().endsWith(".txt")) {
            try {
                Document doc = new Document();

                String titleWithoutExtension = file.getName().replace(".txt", "");
                doc.add(new TextField("titolo", titleWithoutExtension, Field.Store.YES));

                String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
                doc.add(new TextField("contenuto", content, Field.Store.YES));

                writer.addDocument(doc);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
