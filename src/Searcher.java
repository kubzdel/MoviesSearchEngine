import opennlp.tools.parser.Cons;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class Searcher
{

    public static HashMap<Integer,float[]> documentScore;
    public static  IndexSearcher indexSearcher;
    public static Analyzer analyzer;

    public static void main(String args[]) throws IOException, ParseException {
        // Load the previously generated index (DONE)
        IndexReader reader = getIndexReader();
        assert reader != null;

        // Construct index searcher (DONE)
        indexSearcher = new IndexSearcher(reader);
        // Standard analyzer - might be helpful
        analyzer = new StandardAnalyzer();

        documentScore = new HashMap<>();
        String movieTitle = "Friday the 13th"; // tutaj tytuł filmu dla wyszukiwarki
        //  String actorName = "Anna Breuer Nikolas Jürgens Marvin Grone";

        QueryParser qp = new QueryParser(Constants.title, analyzer);
        //   qp.setDefaultOperator(QueryParser.Operator.AND);
        Query q = qp.parse(movieTitle);
        getTopPage(indexSearcher, q);

        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printResultsForQuery(IndexSearcher indexSearcher, Query q) throws IOException {

        ScoreDoc docs[] = (indexSearcher.search(q,Constants.top_docs).scoreDocs);
        // --------------------------------
       for(ScoreDoc doc:indexSearcher.search(q,Constants.top_docs).scoreDocs)
        {
            String score = Float.toString(doc.score);
            String name = indexSearcher.doc(doc.doc).get(Constants.title);
            String id = indexSearcher.doc(doc.doc).get("id");
            String cast = indexSearcher.doc(doc.doc).get(Constants.cast);
            String size = indexSearcher.doc(doc.doc).get("storedSize");
            String plot = indexSearcher.doc(doc.doc).get(Constants.plot);
         //   System.out.println(score+ " "+name+" "+id + " " + size+ "  "+cast+ "Fabuła: "+plot);
        }
    }

    private static void getTopPage(IndexSearcher indexSearcher, Query q) throws IOException, ParseException {

    // --------------------------------
    for(ScoreDoc doc:indexSearcher.search(q,1).scoreDocs)
    {
        String name = indexSearcher.doc(doc.doc).get(Constants.title);
        updateTitleScore(name);
        String cast = indexSearcher.doc(doc.doc).get(Constants.cast);
        updateCastScore(cast);
        String plot = indexSearcher.doc(doc.doc).get(Constants.plot);
        updatePlotScore(plot);

       // documentScore = sortByValues(documentScore);

        for (Integer id : documentScore.keySet()) {
            String n = indexSearcher.doc(id).get(Constants.title);
            System.out.println(n);
            System.out.print("TitleScore "+documentScore.get(id)[0]+' ');
            System.out.print("CastScore "+documentScore.get(id)[1]+' ');
            System.out.print("PlotScore "+documentScore.get(id)[2]+' ');
            System.out.println();
        }
     //   System.out.println(score+ " "+name+" "+id + " " + size+ "  "+cast+ "Fabuła: "+plot);
    }
}

   private static void updateTitleScore(String title) throws ParseException, IOException {
       QueryParser qp = new QueryParser(Constants.title, analyzer);
       Query q = qp.parse(title);
       for(ScoreDoc doc:indexSearcher.search(q,Constants.top_docs).scoreDocs)
       {
           if(!documentScore.containsKey(doc.doc)){
               float k[] = new float[]{doc.score*Constants.titleWeight,0,0};
               documentScore.put(doc.doc,k);
           }
           else{
               float k[] = documentScore.get(doc.doc);
               k[0]=doc.score*Constants.titleWeight;
               documentScore.put(doc.doc,k);
           }

       }
   }

    private static void updateCastScore(String cast) throws ParseException, IOException {
        QueryParser qp = new QueryParser(Constants.cast, analyzer);
        Query q = qp.parse(QueryParser.escape(cast));
        for(ScoreDoc doc:indexSearcher.search(q,Constants.top_docs).scoreDocs)
        {
            if(!documentScore.containsKey(doc.doc)){
                float k[] = new float[]{0,doc.score*Constants.castWeight,0};
                documentScore.put(doc.doc,k);
            }
            else{
                float k[] = documentScore.get(doc.doc);
                k[1]=doc.score*Constants.castWeight;
                documentScore.put(doc.doc,k);
            }

        }
    }

    private static void updatePlotScore(String plot) throws ParseException, IOException {
        QueryParser qp = new QueryParser(Constants.plot, analyzer);
        plot = plot.replace("]", "");
        Query q = qp.parse(QueryParser.escape(plot.substring(0,6000)));
        for(ScoreDoc doc:indexSearcher.search(q,Constants.top_docs).scoreDocs)
        {
            if(!documentScore.containsKey(doc.doc)){
                float k[] = new float[]{0,0,doc.score*Constants.plotWeight};
                documentScore.put(doc.doc,k);
            }
            else{
                float k[] = documentScore.get(doc.doc);
                k[2]=doc.score*Constants.plotWeight;
                documentScore.put(doc.doc,k);
            }

        }
    }

    private static HashMap sortByValues(HashMap map) {
        List list = new LinkedList(map.entrySet());
        // Defined Custom Comparator here
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o2)).getValue())
                        .compareTo(((Map.Entry) (o1)).getValue());
            }

        });

        HashMap sortedHashMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }

    private static IndexReader getIndexReader()
    {
        try
        {
            Directory dir = FSDirectory.open(Paths.get(Constants.index_dir));
            return DirectoryReader.open(dir);

        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }




}
