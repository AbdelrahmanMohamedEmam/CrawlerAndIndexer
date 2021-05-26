import java.io.File;
import java.io.IOException;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class Indexer {

    MySQLConnection mySQLConnection=new MySQLConnection();


    void startIndexing(){
        try {
            File in = new File("C:/Users/Ziadkamal/Desktop/Senior-1/APT/CrawlerAndIndexer/CrawlerAndIndexer/testHtml.html");
            Document doc = Jsoup.parse(in, null);
            //Title
            Elements titleElements = doc.select("title");

            //Headings
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
            

            //Plain Text
            Elements plainTextElements = doc.select("p");
            doc.select("li").forEach((e)->{
                headingsElements.add(e);
            });
            doc.select("pre").forEach((e)->{
                headingsElements.add(e);
            });
           
            
            JSONObject item = new JSONObject();
            // String jsonString = new JSONObject()
            //       .put("JSON1", "Hello World!")
            //       .put("JSON2", "Hello my World!")
            //       .put("JSON3", new JSONObject().put("key1", "value1"))
            //       .toString();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
 
}
