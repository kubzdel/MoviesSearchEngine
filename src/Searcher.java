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
        String movieTitle = "The Conjuring"; // tutaj tytuł filmu dla wyszukiwarki
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
        String director = indexSearcher.doc(doc.doc).get(Constants.director);
        updateDirectorScore(director);


       // documentScore = sortByValues(documentScore);

        ArrayList<SimilarDocument>similarDocuments = new ArrayList<>();
        for (Integer id : documentScore.keySet()) {
            String n = indexSearcher.doc(id).get(Constants.title);
            float titleScore = documentScore.get(id)[0];
            float castScore = documentScore.get(id)[1];
            float plotScore = documentScore.get(id)[2];
            float directorScore = documentScore.get(id)[3];
            similarDocuments.add(new SimilarDocument(id,n,titleScore,castScore,directorScore,plotScore));
        }
        Collections.sort(similarDocuments);
        System.out.println("RANKING");
        int size = 5;
        if(similarDocuments.size()<size+1)
            size = similarDocuments.size()-1;
        for(int i =0;i<=size;i++){
            System.out.println(i+1+". "+ similarDocuments.get(i).title+ " SCORE: "+similarDocuments.get(i).totalScore);
            System.out.print("TitleScore: "+similarDocuments.get(i).titleScore);
            System.out.print(" DirectorScore: "+similarDocuments.get(i).directorScore);
            System.out.print(" CastScore: "+similarDocuments.get(i).castScore);
            System.out.print(" PlotScore: "+similarDocuments.get(i).plotScore);
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
               float k[] = new float[]{doc.score*Constants.titleWeight,0,0,0};
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
                float k[] = new float[]{0,doc.score*Constants.castWeight,0,0};
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
        if(plot.length()>2) {
            QueryParser qp = new QueryParser(Constants.plot, analyzer);
            plot = plot.replace("]", "");
            if (plot.length() > 6000)
                plot = plot.substring(0, 6000);
            Query q = qp.parse(QueryParser.escape(plot));
            for (ScoreDoc doc : indexSearcher.search(q, Constants.top_docs).scoreDocs) {
                if (!documentScore.containsKey(doc.doc)) {
                    float k[] = new float[]{0, 0, doc.score * Constants.plotWeight, 0};
                    documentScore.put(doc.doc, k);
                } else {
                    float k[] = documentScore.get(doc.doc);
                    k[2] = doc.score * Constants.plotWeight;
                    documentScore.put(doc.doc, k);
                }

            }
        }
    }

    private static void updateDirectorScore(String director) throws ParseException, IOException {
        QueryParser qp = new QueryParser(Constants.plot, analyzer);
        Query q = qp.parse(director);
        for(ScoreDoc doc:indexSearcher.search(q,Constants.top_docs).scoreDocs)
        {
            if(!documentScore.containsKey(doc.doc)){
                float k[] = new float[]{0,0,0,doc.score*Constants.directorWeight};
                documentScore.put(doc.doc,k);
            }
            else{
                float k[] = documentScore.get(doc.doc);
                k[3]=doc.score*Constants.directorWeight;
                documentScore.put(doc.doc,k);
            }

        }
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
