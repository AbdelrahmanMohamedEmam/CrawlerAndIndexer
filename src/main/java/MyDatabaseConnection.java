import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.json.JsonObject;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import Models.Website;

public class MyDatabaseConnection {
   
    private MongoClient mongoClient = null;

    public void connectToMySQLDatabase() throws Exception {
        try {
  
            if (mongoClient == null){
                mongoClient = MongoClients.create("mongodb+srv://rootUser:webcrawler_1@cluster0.gsdmf.mongodb.net/CrawlerAndIndexer?w=majority");

                MongoDatabase db= mongoClient.getDatabase("CrawlerAndIndexer");
                db.getCollection("Indexer");
                db.getCollection("Crawler");
                
            }
            
        } catch (Exception e) {
            throw e;
        }
    }
    public boolean createWebsite(String url, int status) {
        boolean result = false;
        try {
            connectToMySQLDatabase();
            MongoDatabase mDatabase = mongoClient.getDatabase("CrawlerAndIndexer");
            MongoCollection<Document> crawlerCollection = mDatabase.getCollection("Crawler");

            if (crawlerCollection.countDocuments(Filters.eq("url", url))==0) {
                Document myDoc = new Document();
                myDoc.put("url", url);
                myDoc.put("status", status);
                crawlerCollection.insertOne(myDoc);
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return result;

    }

    public ArrayList<Website> retrieveWebsitesByStatus(int status) {
        try {
            connectToMySQLDatabase();
            Bson filter = Filters.eq("status", status);
            MongoDatabase mDatabase = mongoClient.getDatabase("CrawlerAndIndexer");
            MongoCollection<Document> crawlerCollection = mDatabase.getCollection("Crawler");
            FindIterable<Document> websites = crawlerCollection.find(filter);
            ArrayList<Website> uncrawledSites = new ArrayList<Website>();
            for (Document doc : websites){
                Website temp = new Website();
                String id = doc.getObjectId("_id").toString();
                temp.set_Id(id);
                temp.setStatus(doc.getInteger("status"));
                temp.setUrl(doc.getString("url"));
                uncrawledSites.add(temp);
            }

            return uncrawledSites;
        } catch (Exception e) {
            System.out.println(e.toString());

        }
        return null;

    }

    public ArrayList<Website> retreiveWebsiteByUrl(String url) {
        try {
            connectToMySQLDatabase();
            Bson filter = Filters.eq("url", url);
            MongoDatabase mDatabase = mongoClient.getDatabase("CrawlerAndIndexer");
            MongoCollection<Document> crawlerCollection = mDatabase.getCollection("Crawler");
            FindIterable<Document> websites = crawlerCollection.find(filter);
            ArrayList<Website> uncrawledSites = new ArrayList<Website>();
            for (Document doc : websites){
                Website temp = new Website();
                String id = doc.getObjectId("_id").toString();
                temp.set_Id(id);
                temp.setStatus(doc.getInteger("status"));
                temp.setUrl(doc.getString("url"));
                uncrawledSites.add(temp);
            }
            
            return uncrawledSites;
        } catch (Exception e) {
            System.out.println(e.toString());

        }
        return null;
    }


    public boolean updateStatusOfWebsiteBy_Id(String id, int status) {
       
        try {
            connectToMySQLDatabase();
            MongoDatabase mDatabase = mongoClient.getDatabase("CrawlerAndIndexer");
            MongoCollection<Document> crawlerCollection = mDatabase.getCollection("Crawler");
            Bson queryFilter = Filters.eq("_id", new ObjectId(id));
            Bson updateFilter = Updates.set("status", status);
            Document result = crawlerCollection.findOneAndUpdate(queryFilter, updateFilter);
            if(result==null){
                return false;
            }else{
                return true;
            }
        } catch (Exception e) {
            System.out.println(e.toString());
            return false;
        }
    }



    public void addWord(String word,JSONObject jsonObject) {
        try{
            connectToMySQLDatabase();
            MongoDatabase mDatabase = mongoClient.getDatabase("CrawlerAndIndexer");
            MongoCollection<Document> indexerCollection = mDatabase.getCollection("Indexer");
            
            Object o = BasicDBObject.parse(jsonObject.toString());
            DBObject dbObj = (DBObject) o;
            Bson queryFilter = Filters.eq("word", word);
            List<DBObject> tempList = new ArrayList<DBObject>();
            tempList.add(dbObj);
            Bson updateFilter = Updates.addEachToSet("links", tempList);
            Bson update2 = Updates.inc("df", 1);
            Bson updates = Updates.combine(updateFilter,update2);
            
            Document result = indexerCollection.findOneAndUpdate(queryFilter, updates);
            if(result==null){
              
                Document myDoc = new Document();
            
                myDoc.put("word", word);
                myDoc.put("df", 1);
                List<JsonObject> linksArray = new ArrayList<JsonObject>();
                JsonObject jo = new JsonObject(jsonObject.toString());
                linksArray.add(jo);
                myDoc.append("links",  linksArray);
                indexerCollection.insertOne(myDoc);
            }
            else{
                System.out.println(result.toJson().toString());
            }
           
        }catch(Exception e){
            System.out.println(e.toString());
        }
       
    }
}