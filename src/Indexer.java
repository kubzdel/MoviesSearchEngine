import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.Span;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Indexer
{
    TokenNameFinderModel model = null;
    InputStream tokenStream = null;
    Tokenizer tokenizer = null;
    private POSTaggerME posTaggerME;
    private TokenizerME tokenizerME;

    public static void main(String args[]) throws IOException {
        Indexer indexer = new Indexer();
        indexer.indexDocuments();
    }



    private void indexDocuments() throws IOException {
        // REMOVE PREVIOUSLY GENERATED INDEX (DONE)
        try
        {
            FileUtils.deleteDirectory(new File(Constants.index_dir));
        } catch (IOException ignored)
        {
        }
        try {
            tokenStream = new FileInputStream(new File(Constants.TOKENIZER_MODEL));

            model = new TokenNameFinderModel(
                    new File(Constants.NAME_MODEL));
            POSModel posModel = new POSModel(new File(Constants.POS_MODEL));
            posTaggerME = new POSTaggerME(posModel);
            TokenizerModel tokenModel = new TokenizerModel(tokenStream);
            tokenizer = new TokenizerME(tokenModel);

            File file1 = new File(Constants.TOKENIZER_MODEL);
            TokenizerModel tokenizerModel = new TokenizerModel(file1);
            tokenizerME = new TokenizerME(tokenizerModel);
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // LOAD HTML DOCUMENTS (TODO)
        ArrayList<Document> documents = getHTMLDocuments();


        // CONSTRUCT INDEX (TODO)
        // - Firstly, create Analyzer object (StandardAnalyzer).
        //   (An Analyzer builds TokenStreams, which analyze text.
        //   It thus represents a policy for extracting index terms from text.)
        // - Then, create IndexWriterConfig object that uses standard analyzer
        // - Construct IndexWriter (you can use FSDirectory.open and Paths.get + Constants.index_dir
        // - Add documents to the index
        // - Commit and close the index.

        // ----------------------------------
        Analyzer analyzer= new StandardAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(FSDirectory.open(Paths.get(Constants.index_dir)),indexWriterConfig);
        writer.addDocuments(documents);
        writer.commit();
        writer.close();
        // ----------------------------------

    }


    private ArrayList<Document> getHTMLDocuments()
    {
        // This method is finished. Find getHTMLDocument
        File dir = new File("pages");
        File[] files = dir.listFiles();
        if (files != null)
        {
            ArrayList<Document> htmls = new ArrayList<>(files.length);
            for (int id = 0; id < files.length; id++)
            {
                System.out.println("Loading "+ files[id].getName());
                // TODO finish getHTML document
                htmls.add(getHTMLDocument("pages/" + files[id].getName(), id));
            }
            return htmls;
        }
        return null;
    }

    private Document getHTMLDocument(String path, int id)
    {
        File file = new File(path);
        Document document = new Document();

        /*Expert: directly create a field for a document.
        Most users should use one of the sugar subclasses:

        TextField: Reader or String indexed for full-text search
        StringField: String indexed verbatim as a single token
        IntPoint: int indexed for exact/range queries.
        LongPoint: long indexed for exact/range queries.
        FloatPoint: float indexed for exact/range queries.
        DoublePoint: double indexed for exact/range queries.
        SortedDocValuesField: byte[] indexed column-wise for sorting/faceting
        SortedSetDocValuesField: SortedSet<byte[]> indexed column-wise for sorting/faceting
        NumericDocValuesField: long indexed column-wise for sorting/faceting
        SortedNumericDocValuesField: SortedSet<long> indexed column-wise for sorting/faceting
        StoredField: Stored-only value for retrieving in summary results

        A field is a section of a Document.
        Each field has three parts: name, type and value.
        Values may be text (String, Reader or pre-analyzed TokenStream),
        binary (byte[]), or numeric (a Number). Fields are optionally
        stored in the index, so that they may be returned with hits on the document.
        */

        // STORED + INDEXED = field is searchable and results may be highlighted
        // (e.g., document's title may be presented because original content is stored)

        // STORED but not INDEXED = field is not searchable
        // but some metadata may be derived (e.g., document's id)

        // INDEXED but not STORED = field is searchable but results may not be highlighted

        // For the following, it is suggested to use TextField and StoredField

        // TODO create a field that is stored but not indexed
        // and contains document's id
        Field idField = new StoredField("id",id);
        // ----------------------------------
       
        // ----------------------------------

        // TODO create a field that is indexed but not stored
        // and contains document's content
        // for this purpose, extract text from the document
        // using Tika ( use getTextFromHTMLFile() <- this method is finished )
        // ----------------------------------
       
        // ----------------------------------
        String content = getTextFromHTMLFile(file);
        Field contentField = new TextField("content",content,Field.Store.NO);


        // TODO create a field that is stored and indexed
        // and contains file name
        // ----------------------------------

        String title = getTitleFromContent(content); //tytuł z HTMLa
        Field nameField = new TextField(Constants.title,title,Field.Store.YES);

        String cast = getCastFromContent(content); //obsada z HTMLa
        Field castField = new TextField(Constants.cast,cast,Field.Store.YES);


        String plot = getPlotFromContent(content); //fabuła z HTMLa
        Field plotField = new TextField(Constants.plot,plot,Field.Store.YES);
        // ----------------------------------

        // TODO create an INT field (IntPoint) that is indexed
        // and contains file size (bytes, .length())
        // ----------------------------------

        Field intField = new IntPoint("size",(int)file.length());

        // ----------------------------------
        // //TODO IntPoint is not stored but we want to a file size
        // ... so add another field (StoredField) that stores file size
        // ----------------------------------

        Field sizeField = new StoredField("storedSize",file.length());
        // ----------------------------------

        // TODO add fields to the document object
        // ----------------------------------

        document.add(idField);
        document.add(contentField);
        document.add(nameField);
        document.add(castField);
        document.add(plotField);
        document.add(intField);
        document.add(sizeField);

        // ----------------------------------


        return document;

    }

    // (DONE)
    private String getTextFromHTMLFile(File file)
    {
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        FileInputStream inputStream;
        try
        {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
            return null;
        }

        ParseContext pContext = new ParseContext();

        //Html parser
        HtmlParser htmlparser = new HtmlParser();
        try
        {
            htmlparser.parse(inputStream, handler, metadata, pContext);
        } catch (IOException | SAXException | TikaException e)
        {
            e.printStackTrace();
        }

        return handler.toString();
    }

    private String getTitleFromContent(String source) {
        int i = (source.indexOf("name"));
        String title="none";
        if (i != -1) {
            String tmpText = source.substring(i);
            if((tmpText.indexOf("=")+2)<tmpText.indexOf("\n"))
            title = tmpText.substring(tmpText.indexOf("=")+2,tmpText.indexOf("\n"));
        }
        return title;
    }

    private String getCastFromContent(String source) {
        String cast = "";
        int begin = (source.indexOf("starring"));
        if (begin != -1) {
            String tmpText = source.substring(begin + 16);
            int end = (tmpText.indexOf('='));
            if (end != -1) {
                String textToProceed = tmpText.substring(0, end);


                NameFinderME finder = new NameFinderME(model);

                // Split the sentence into tokens
                String[] tokens = tokenizer.tokenize(textToProceed);

                // Find the names in the tokens and return Span objects
                Span[] nameSpans = finder.find(tokens);

                List<String> people = new ArrayList<String>();
                String[] spanns = Span.spansToStrings(nameSpans, tokens);
                for (int i = 0; i < spanns.length; i++) {
                    cast=cast+ spanns[i]+' ';
                }
                if(cast.length()>2)
                cast = cast.substring(0,cast.length()-2);


            }
        }
        return cast;
    }

    private String getPlotFromContent(String source){
        StringBuilder builder = new StringBuilder();
        int i = source.indexOf("== Plot ==");
        if( i != -1) {
            i += 10;
        }else if( (i = source.indexOf("==Plot==")) != -1){
            i += 8;
        }else if((i = source.indexOf("== Synopsis ==")) != -1){
            i += 14;
        }else if((i = source.indexOf("==Synopsis==")) != -1){
            i += 12;
        }
        if(i != -1){
            int j = source.indexOf("== Cast ==");
            j = (j != -1) ? j : source.length();
            String plot = source.substring(i, j);
            String[] plotArray = tokenizerME.tokenize(plot);
            String[] posTags = posTaggerME.tag(plotArray);
            for(int w = 0; w < plotArray.length; w++){
//                System.out.println(plotArray[w] + " " + posTags[w]);
                if((posTags[w].charAt(0) == 'V' || posTags[w].charAt(0) == 'N') && builder.indexOf(plotArray[w]) == -1){
                    builder.append(plotArray[w] +" ");
                }
            }
        }

        return builder.toString();
    }





}
