package com.example.crossle.OnlineDBManager;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class MongoDBConnection {
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static MongoCollection<Document> collection;
    private final OnlineJsonReader jsonReader = new OnlineJsonReader();
    private static final String CONNECTION_STRING = "mongodb+srv://dbUser:crossle@cluster0.aceoy.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";

    /**
     * Retrieves a JSON object with answers from the MongoDB collection.
     * If the JSON object does not exist, it will be created by the API.
     *
     * @param jsonObject The JSON object containing the question and possible answer
     *                   length.
     * @return The JSON object with answers from the MongoDB collection.
     * @throws JSONException If there is an error processing the JSON object.
     */
    public JSONObject getJsonWithAnswers(JSONObject jsonObject) throws JSONException {
        String question = jsonReader.getQuestions(jsonObject);
        int answerLength = jsonReader.getPossibleAnswerLength(jsonObject);
        getMongoClient();
        if (JsonObjectAlreadyExist(question, answerLength)) {
            System.out.println("Json was found in MongoDB.");
            return GetJsonFromMongoDB(jsonObject);
        }
        System.out.println("Json needs to be created by API.");
        JSONObject newJsonObject = CreateNewJsonInMongoDB(jsonObject);
        return newJsonObject;
    }

    private void getMongoClient() {
        if (mongoClient != null) {
            return;
        }
        disableMongoLogger();
        try {
            mongoClient = MongoClients.create(CONNECTION_STRING);
            database = mongoClient.getDatabase("Crossle");
            collection = database.getCollection("Questions");
            System.out.println("Connected to MongoDB successfully.");

        } catch (Exception e) {
            System.err.println("Connection failed. " + e.getMessage());
        }
    }

    private void disableMongoLogger() {
        Logger mongoLogger = (Logger) LoggerFactory.getLogger("org.mongodb.driver");
        mongoLogger.setLevel(Level.OFF);
    }

    private boolean JsonObjectAlreadyExist(String question, int length) throws JSONException {
        Document query = new Document("$and", List.of(
                new Document("question", question),
                new Document("length", length)));

        Document found = collection.find(query).first();

        return found != null;
    }

    private JSONObject GetJsonFromMongoDB(JSONObject jsonObject) throws JSONException {
        String question = jsonReader.getQuestions(jsonObject);
        int length = jsonReader.getPossibleAnswerLength(jsonObject);
        try {
            Document query = new Document("$and", List.of(
                    new Document("question", question),
                    new Document("length", length)));

            // Find the document matching the query
            Document found = collection.find(query).first();

            if (found == null) {
                System.err.println("Matching document cannot be null.");
                return null;
            }

            System.out.println("Matching document found.");
            return new JSONObject(found.toJson());
        } catch (JSONException e) {
            System.err.println("Error retrieving Json: " + e.getMessage());
            return null;
        }
    }

    private JSONObject CreateNewJsonInMongoDB(JSONObject jsonObject) {
        CompletableFuture<JSONObject> future = new CompletableFuture<>();

        try {
            JsonObjectAnswerEditor.JsonObjectCallback callback = new JsonObjectAnswerEditor.JsonObjectCallback() {
                @Override
                public void onResult(JSONObject jsonObjectFromApi) {
                    try {
                        Document document = Document.parse(jsonObjectFromApi.toString());
                        collection.insertOne(document);
                        System.out.println("Document has been inserted into MongoDB.");
                        future.complete(jsonObjectFromApi);
                    } catch (Exception e) {
                        System.err.println("Error inserting document into MongoDB: " + e.getMessage());
                        future.completeExceptionally(e);
                    }
                }

                @Override
                public void onError(Exception e) {
                    System.err.println("Error updating Json object: " + e.getMessage());
                    future.completeExceptionally(e);
                }
            };

            JsonObjectAnswerEditor.AddAnswerToJson(jsonObject, callback);

            return future.get(); // Wait for the callback to complete and get the result
        } catch (JSONException | InterruptedException | ExecutionException e) {
            System.err.println("Error creating new Json: " + e.getMessage());
            return null;
        }
    }
}