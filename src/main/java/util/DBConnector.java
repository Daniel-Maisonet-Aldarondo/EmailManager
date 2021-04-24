package util;

import com.mongodb.*;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;


import java.util.Arrays;
import java.util.List;

public class DBConnector {
    private static MongoClient mongo;
    private static MongoDatabase database;
    private static MongoCredential credential;



    public DBConnector() {
//        credential = MongoCredential.createCredential("root", "emailDB", "password".toCharArray());
//        MongoClientURI uri = new MongoClientURI("mongodb://root:password@localhost/?authSource=db1");
//        mongo = new MongoClient(uri);

//        mongo = new MongoClient(new ServerAddress("localhost", 27017), Arrays.asList(credential));
        mongo=  new MongoClient();
        database = mongo.getDatabase("emailDB");

    }

    public void updateBackups(List<Email> emails) {
        MongoCollection<Document> collection = database.getCollection("backups");
        for(Email email: emails) {
            collection.insertOne(new Document("email", email.toString()));
        }
    }

//    private static Document DBObject createDBObject(Email email) {
//        Document document = new Document();
//        document.append("email", email.toString());
//        return document;
//    }

    public void displayBackups() {
        MongoCollection<Document> collection = database.getCollection("backups");
        for (Document document : collection.find()) {
            System.out.println(document);
        }
    }


}
