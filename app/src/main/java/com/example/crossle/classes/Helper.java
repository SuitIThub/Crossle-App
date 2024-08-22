package com.example.crossle.classes;

import org.json.JSONObject;

import java.util.ArrayList;

public class Helper {
    public static ArrayList<JSONObject> getDummyJson() {
        ArrayList<String> json_string = new ArrayList();
        json_string.add(
                "{" +
                        "    \"id\": 0," +
                        "    \"question\": \"Arsch\"," +
                        "    \"position\": \"0:0\"," +
                        "    \"slots\": [\"1:0\", \"2:0\", \"3:0\"]," +
                        "    \"length\": 3," +
                        "    \"linked\": [6, 9]," +
                        "    \"answers\": []" +
                        "}"
        );
        json_string.add(
                "{" +
                        "    \"id\": 1," +
                        "    \"question\": \"Schachtel\"," +
                        "    \"position\": \"4:0\"," +
                        "    \"slots\": [\"4:1\", \"4:2\", \"4:3\"]," +
                        "    \"length\": 3," +
                        "    \"linked\": [4]," +
                        "    \"answers\": []" +
                        "}"
        );
        json_string.add(
                "{" +
                        "    \"id\": 2," +
                        "    \"question\": \"Computer Gerät\"," +
                        "    \"position\": \"4:4\"," +
                        "    \"slots\": [\"3:4\", \"2:4\", \"1:4\", \"0:4\"]," +
                        "    \"length\": 4," +
                        "    \"linked\": [4, 5, 9]," +
                        "    \"answers\": []" +
                        "}"
        );
        json_string.add(
                "{" +
                        "    \"id\": 3," +
                        "    \"question\": \"Haus\"," +
                        "    \"position\": \"0:0\"," +
                        "    \"slots\": [\"0:1\", \"0:2\", \"0:3\", \"0:4\"]," +
                        "    \"length\": 3," +
                        "    \"linked\": [3, 7]," +
                        "    \"answers\": []" +
                        "}"
        );
        json_string.add(
                "{" +
                        "    \"id\": 4," +
                        "    \"question\": \"Cyberpunk Gang\"," +
                        "    \"position\": \"1:3\"," +
                        "    \"slots\": [\"2:3\", \"3:3\", \"4:3\"]," +
                        "    \"length\": 3," +
                        "    \"linked\": [1, 5, 8]," +
                        "    \"answers\": []" +
                        "}"
        );
        json_string.add(
                "{" +
                        "    \"id\": 5," +
                        "    \"question\": \"OM\"," +
                        "    \"position\": \"3:2\"," +
                        "    \"slots\": [\"3:3\", \"3:4\"]," +
                        "    \"length\": 2," +
                        "    \"linked\": [2, 4]," +
                        "    \"answers\": []" +
                        "}"
        );
        json_string.add(
                "{" +
                        "    \"id\": 6," +
                        "    \"question\": \"US\"," +
                        "    \"position\": \"3:2\"," +
                        "    \"slots\": [\"3:1\", \"3:0\"]," +
                        "    \"length\": 2," +
                        "    \"linked\": [0]," +
                        "    \"answers\": []" +
                        "}"
        );
        json_string.add(
                "{" +
                        "    \"id\": 7," +
                        "    \"question\": \"USA\"," +
                        "    \"position\": \"3:2\"," +
                        "    \"slots\": [\"2:2\", \"1:2\", \"0:2\"]," +
                        "    \"length\": 3," +
                        "    \"linked\": [3, 8, 9]," +
                        "    \"answers\": []" +
                        "}"
        );
        json_string.add(
                "{" +
                        "    \"id\": 8," +
                        "    \"question\": \"UMA\"," +
                        "    \"position\": \"2:1\"," +
                        "    \"slots\": [\"2:2\", \"2:3\", \"2:4\"]," +
                        "    \"length\": 3," +
                        "    \"linked\": [7, 4, 2]," +
                        "    \"answers\": []" +
                        "}"
        );
        json_string.add(
                "{" +
                        "    \"id\": 9," +
                        "    \"question\": \"SAA\"," +
                        "    \"position\": \"1:3\"," +
                        "    \"slots\": [\"1:2\", \"1:1\", \"1:0\"]," +
                        "    \"length\": 3," +
                        "    \"linked\": [7, 0]," +
                        "    \"answers\": []" +
                        "}"
        );
        try {
            ArrayList<JSONObject> jsons = new ArrayList();
            for (String json : json_string) {
                jsons.add(new JSONObject(json));
            }
            return jsons;
        } catch (Exception e) {
            return null;
        }
    }
}
