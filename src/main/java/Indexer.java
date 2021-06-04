import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import Models.Website;
public class Indexer {

    MyDatabaseConnection myDatabaseConnection=new MyDatabaseConnection();

    List<String> stopWords = new ArrayList<String>();


    void initializeStopWords(){

        File txt = new File("stopwords2.txt");
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

    void startIndexing() throws JSONException{
        List<Website> websites = myDatabaseConnection.retrieveWebsitesByStatus(2);
        for(Website website :websites){
            startIndexingURL(website);
            
        }
    }


    void startIndexingURL(Website website) throws JSONException{
        try {
            String url = website.getUrl();
            Document doc = Jsoup.connect(url).get();
            
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
                        innerJson.put("url", url);
                        json.put("url", innerJson);
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
                        innerJson.put("url", url);
                        json.put("url", innerJson);
             
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
                        innerJson.put("url", url);
                        json.put("url", innerJson);
                     
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    dict.put(word, json);
                }
            }
            );

            System.out.println("------------Indexed " + url + " and found " + dict.keySet().size() + " words---------");
            List<String> words = new ArrayList<>();
            List<JSONObject> jsonObjects = new ArrayList<>();

            for (String key : dict.keySet()) {
                JSONObject jo= dict.get(key);
                jo = jo.getJSONObject("url");
                words.add(key);
                jsonObjects.add(jo);
            }

            myDatabaseConnection.addWords(words, jsonObjects);


            myDatabaseConnection.updateStatusOfWebsiteBy_Id(website.get_Id(), 3, 2);
            System.out.println("Finished adding them to the DB");
            
      
          
           
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
