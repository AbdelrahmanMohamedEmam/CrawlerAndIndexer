import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
public class Indexer {

    MySQLConnection mySQLConnection=new MySQLConnection();
    // Stemmer myStemmer  = new Stemmer();
    List<String> stopWords = new ArrayList<String>();


    void initializeStopWords(){

        File txt = new File("C:/Users/Ziadkamal/Desktop/Senior-1/APT/CrawlerAndIndexer/CrawlerAndIndexer/stopwords.txt");
        Scanner scan;
        try {
            scan = new Scanner(txt);
            while (scan.hasNextLine()) {
                stopWords.add(scan.nextLine());
            }
            scan.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
      

    }

    Indexer(){
        initializeStopWords();
    }


    void startIndexing(){
        try {
           
            File in = new File("C:/Users/Ziadkamal/Desktop/Senior-1/APT/CrawlerAndIndexer/CrawlerAndIndexer/testHtml.html");
            Document doc = Jsoup.parse(in, null);
            
            //Title
            List<String> titles = getStemmedTitles(doc);
            
            //Headings
            List<String> headings = getStemmedHeadings(doc);

            //Plain Text
            List<String> plainText = getStemmedPlainText(doc);


            Hashtable<String,JSONObject> dict = new Hashtable<>();

            titles.forEach((word)->{
                if(dict.get(word) != null){

                    int tf;
                    int df;
                    int titleFrequency;
                    try {

                        tf = dict.get(word).getJSONObject("url").getInt("TF")+1;
                        titleFrequency = dict.get(word).getJSONObject("url").getInt("titleFrequency")+1;
                        dict.get(word).getJSONObject("url").put("TF", tf);
                        dict.get(word).getJSONObject("url").put("titleFrequency", titleFrequency);

                    } catch (JSONException e) {
                        
                        e.printStackTrace();
                    }
                
                }else{
                    JSONObject innerJson = new JSONObject();
                    JSONObject json = new JSONObject();
                    try {
                        innerJson.put("TF", 1);
                        innerJson.put("headingsFrequency", 0);
                        innerJson.put("titleFrequency", 1);            
                        innerJson.put("plainTextFrequency", 0);
                        json.put("url",innerJson);
                        json.put("DF", 1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    dict.put(word, json);
                }
            }
            );

            headings.forEach((word)->{
                if(dict.get(word) != null){

                    int tf;
                    int headingsFrequency;
                    try {
                        
                        tf = dict.get(word).getJSONObject("url").getInt("TF")+1;
                        headingsFrequency = dict.get(word).getJSONObject("url").getInt("headingsFrequency")+1;
                        dict.get(word).getJSONObject("url").put("TF", tf);
                        dict.get(word).getJSONObject("url").put("headingsFrequency", headingsFrequency);

                    } catch (JSONException e) {
                        
                        e.printStackTrace();
                    }
                
                }else{
                    JSONObject json = new JSONObject();
                    JSONObject innerJson = new JSONObject();
                    try {
                        innerJson.put("TF", 1);
                        innerJson.put("headingsFrequency", 1);
                        innerJson.put("titleFrequency", 0);            
                        innerJson.put("plainTextFrequency", 0);
                        json.put("url", innerJson);
                        json.put("DF", 1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    dict.put(word, json);
                }
            }
            );

            plainText.forEach((word)->{
                if(dict.get(word) != null){

                    int tf;
                    int plainTextFrequency;
                    try {
                        
                        tf = dict.get(word).getJSONObject("url").getInt("TF")+1;
                        plainTextFrequency = dict.get(word).getJSONObject("url").getInt("plainTextFrequency")+1;
                        dict.get(word).getJSONObject("url").put("TF", tf);
                        dict.get(word).getJSONObject("url").put("plainTextFrequency", plainTextFrequency);

                    } catch (JSONException e) {
                        
                        e.printStackTrace();
                    }
                
                }else{
                    JSONObject json = new JSONObject();
                    JSONObject innerJson = new JSONObject();
                    try {
                        innerJson.put("TF", 1);
                        innerJson.put("headingsFrequency", 0);
                        innerJson.put("titleFrequency", 0);            
                        innerJson.put("plainTextFrequency", 1);
                        json.put("url", innerJson);
                        json.put("DF", 1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    dict.put(word, json);
                }
            }
            );

            
          

            System.out.println(new JSONObject(dict));

      
          
           
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    List<String> getStemmedTitles(Document doc){
        Elements titleElements = doc.select("title");

        String[] titleWords = titleElements.text().split(" ");
        List<String> titleWordsProcessed= new ArrayList<String>();
        List<String> stemmedTitles= new ArrayList<String>();
        for(String word:titleWords){
            word = word.replaceAll("[^A-Za-z]", "");
            if(word!=""  && !stopWords.contains(word)){
                titleWordsProcessed.add(word);
            }
        }

        titleWordsProcessed.forEach((word)->{
            word = Stemmer.stem(word);
            stemmedTitles.add(word);
          
        });

        return stemmedTitles;
    }

    
    List<String> getStemmedHeadings(Document doc){
        Elements headingsElements = doc.select("h1");
            doc.select("h2").forEach((e)->{
                headingsElements.add(e);
            });
            doc.select("h3").forEach((e)->{
                headingsElements.add(e);
            });
            doc.select("h4").forEach((e)->{
                headingsElements.add(e);
            });
            doc.select("h5").forEach((e)->{
                headingsElements.add(e);
            });
            doc.select("h6").forEach((e)->{
                headingsElements.add(e);
            });

        String[] headingsWords = headingsElements.text().split(" ");
        List<String> headingsWordsProcessed= new ArrayList<String>();
        List<String> stemmedHeadings= new ArrayList<String>();
        for(String word:headingsWords){
            word = word.replaceAll("[^A-Za-z]", "");
            if(word!=""  && !stopWords.contains(word)){
                headingsWordsProcessed.add(word);
            }
        }

        headingsWordsProcessed.forEach((word)->{
            word = Stemmer.stem(word);
            stemmedHeadings.add(word);
        });

        return stemmedHeadings;
    }


    
    List<String> getStemmedPlainText(Document doc){            
        Elements plainTextElements = doc.select("p");
        doc.select("li").forEach((e)->{
            plainTextElements.add(e);
        });
        doc.select("pre").forEach((e)->{
            plainTextElements.add(e);
        });

        String[] plaingTextWords = plainTextElements.text().split(" ");
        List<String> plainTextWordsProcessed= new ArrayList<String>();
        List<String> stemmedPlainText= new ArrayList<String>();
        for(String word:plaingTextWords){
            word = word.replaceAll("[^A-Za-z]", "");
            if(word!="" && !stopWords.contains(word)){
                plainTextWordsProcessed.add(word);
            }
        }

        plainTextWordsProcessed.forEach((word)->{
            word = Stemmer.stem(word);
            stemmedPlainText.add(word);
        });

        return stemmedPlainText;
    }


 
}