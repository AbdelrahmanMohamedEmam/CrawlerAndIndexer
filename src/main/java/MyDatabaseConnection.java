import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.InsertManyResult;

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

            if (mongoClient == null) {
                mongoClient = MongoClients.create(
                        "mongodb+srv://rootUser:webcrawler_1@cluster0.gsdmf.mongodb.net/CrawlerAndIndexer?w=majority");

                MongoDatabase db = mongoClient.getDatabase("CrawlerAndIndexer");
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

            if (crawlerCollection.countDocuments(Filters.eq("url", url)) == 0) {
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

    public boolean createWebsites(LinkedList<String> url, int status) {
        try {
            connectToMySQLDatabase();
            MongoDatabase mDatabase = mongoClient.getDatabase("CrawlerAndIndexer");
            MongoCollection<Document> crawlerCollection = mDatabase.getCollection("Crawler");
            List<Document> docs = new ArrayList<Document>();
            for (int i = 0; i < url.size(); i++) {
                if (crawlerCollection.countDocuments(Filters.eq("url", url)) == 0) {
                    Document myDoc = new Document();
                    myDoc.put("url", url.get(i));
                    myDoc.put("status", status);
                    docs.add(myDoc);
                }
            }
            crawlerCollection.insertMany(docs);
            return true;
        } catch (Exception e) {
            System.out.println(e.toString());
            return false;
        }
    }

    public synchronized LinkedList<Website> retreiveUncrawledWebsite(int status) {
        try {
            connectToMySQLDatabase();
            Bson filter = Filters.eq("status", status);
            MongoDatabase mDatabase = mongoClient.getDatabase("CrawlerAndIndexer");
            MongoCollection<Document> crawlerCollection = mDatabase.getCollection("Crawler");
            FindIterable<Document> websites = crawlerCollection.find(filter);
            LinkedList<Website> uncrawledSites = new LinkedList<Website>();
            for (Document doc : websites) {
                Website temp = new Website();
                String id = doc.getObjectId("_id").toString();
                temp.set_Id(id);
                temp.setStatus(doc.getInteger("status"));
                temp.setUrl(doc.getString("url"));
                uncrawledSites.add(temp);
                Bson queryFilter = Filters.eq("_id", new ObjectId(id));
                Bson updateFilter = Updates.set("status", 1);
                crawlerCollection.findOneAndUpdate(queryFilter, updateFilter);
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
            for (Document doc : websites) {
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

    public synchronized boolean updateStatusOfWebsiteBy_Id(String id, int status, int threadNumber) {

        try {
            System.out.println("I am thread:" + threadNumber + "and i acquired the lock");
            connectToMySQLDatabase();
            MongoDatabase mDatabase = mongoClient.getDatabase("CrawlerAndIndexer");
            MongoCollection<Document> crawlerCollection = mDatabase.getCollection("Crawler");
            Bson queryFilter = Filters.eq("_id", new ObjectId(id));
            Bson updateFilter = Updates.set("status", status);
            Document result = crawlerCollection.findOneAndUpdate(queryFilter, updateFilter);
            if (result == null) {
                System.out.println("I am thread:" + threadNumber + "and i released the lock");
                return false;
            } else {
                System.out.println("I am thread:" + threadNumber + "and i released the lock");
                return true;
            }
        } catch (Exception e) {
            System.out.println(e.toString());
            System.out.println("I am thread:" + threadNumber + "and i released the lock");
            return false;
        }
    }

    public void addWord(String word, JSONObject jsonObject) {
        try {
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
            Bson updates = Updates.combine(updateFilter, update2);

            Document result = indexerCollection.findOneAndUpdate(queryFilter, updates);
            if (result == null) {

                Document myDoc = new Document();

                myDoc.put("word", word);
                myDoc.put("df", 1);
                List<JsonObject> linksArray = new ArrayList<JsonObject>();
                JsonObject jo = new JsonObject(jsonObject.toString());
                linksArray.add(jo);
                myDoc.append("links", linksArray);
                indexerCollection.insertOne(myDoc);
            } else {
                System.out.println(result.toJson().toString());
            }

        } catch (Exception e) {
            System.out.println(e.toString());
        }

    }
}