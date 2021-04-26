package util;

import com.mongodb.*;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.Document;


import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DBConnector {
    private static MongoClient mongo;
    private static MongoDatabase database;
    private static MongoCredential credential;



    public DBConnector() {
        mongo=  new MongoClient();
        database = mongo.getDatabase("emailDB");
    }

    public void updateBackups(List<Email> emails) {
        MongoCollection<Document> collection = database.getCollection("backups");
        collection.createIndex(Indexes.ascending("date"), new IndexOptions().expireAfter(1L, TimeUnit.DAYS));
        for(Email email: emails) {
            collection.insertOne(new Document("email", email.toString()));
        }
    }


    public void displayBackups() {
        MongoCollection<Document> collection = database.getCollection("backups");
        for (Document document : collection.find()) {
            System.out.println(document);
        }
    }


}
