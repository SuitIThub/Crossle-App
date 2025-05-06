package com.example.crossle.LocalDBManager;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Connection;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.crossle.classes.Helper;

public class LocalDBManager implements LocalDBManagerInterface {
    // db parameters
    static final String DB_URL = "jdbc:sqlite:LocalPuzzle.db";
    Connection localDBConnection = null;

    // puzzle params
    public int currentPuzzleID = 0;

    // Constructor
    public LocalDBManager() {
        currentPuzzleID = 0;
        connectToLocalDB();
        selectAndSetAsCurrentHighestPuzzleID();
    }

    public void connectToLocalDB() {
        try {
            if (localDBConnection == null || localDBConnection.isClosed()) {
                localDBConnection = DriverManager.getConnection(DB_URL);
                // Should no longer be necessery, because it should be enabled by default in the
                // sqlite-jdbc driver
                // Statement aForeignKeyEnabler = localDBConnection.createStatement();
                // boolean aForeignKeyBool = aForeignKeyEnabler.execute("PRAGMA foreign_keys =
                // ON");
                // if (aForeignKeyBool)
                // System.out.println("FK enabled");

                // System.out.println("Connected to DB");
            }
        } catch (SQLException e) {
            System.out.println("Error! while connecting to DB");
            System.out.println(e.getMessage());
        }
    }

    public void disconnectFromLocalDB() {
        try {
            if (localDBConnection != null && !localDBConnection.isClosed()) {
                localDBConnection.close();
                // System.out.println("Disconnected from DB");
            }
        } catch (SQLException ex) {
            System.out.println("Error! while closing DB connection");
            System.out.println(ex.getMessage());
        }
    }

    // This method is used to start other methods like delete a specific table only
    // once
    @Override
    public void TemporaryMethodToStartOtherMethodsOneTime() {
    }

    // Writes a given Matrix to the local DB and returns the ID of the new entry
    @Override
    public int writePuzzleToLocalDB(String[][] theMatrixToWrite) {
        try{
            connectToLocalDB();
            return insertValuesIntoTable(theMatrixToWrite);
        }finally{
            disconnectFromLocalDB();
        }
    }

    // Writes a given JSONObject with a specified puzzleId to the local DB and
    // returns the ID of the new entry
    @Override
    public int writeQuestionToLocalDB(int thePuzzleId, JSONObject theJSONObjectToWrite) {
        try{
            connectToLocalDB();
            currentPuzzleID = thePuzzleId;
            return insertValuesIntoTable(theJSONObjectToWrite);

        }finally{
            disconnectFromLocalDB();
        }
    }

    // Reads a question from the local DB by ID and returns it as a JSONObject
    @Override
    public JSONObject readQuestionFromLocalDBByIDasJSONObject(int theQestionID) {
        try {
            connectToLocalDB();
            JSONObject aReturnJsonObject = new JSONObject();
            aReturnJsonObject.put("id", Integer.parseInt(getQuestionValueFromColumnInLocalDBbyID(theQestionID, "qID")));
            aReturnJsonObject.put("question", getQuestionValueFromColumnInLocalDBbyID(theQestionID, "question"));
            aReturnJsonObject.put("length",
                    Integer.parseInt(getQuestionValueFromColumnInLocalDBbyID(theQestionID, "length")));
            aReturnJsonObject.put("slots", getQuestionValueFromColumnInLocalDBbyID(theQestionID, "slots").split(";"));
            aReturnJsonObject.put("linked",
                    Helper.parseIntArray(getQuestionValueFromColumnInLocalDBbyID(theQestionID, "linked").split(";")));
            aReturnJsonObject.put("answers", getQuestionValueFromColumnInLocalDBbyID(theQestionID, "answers").split(";"));

            return aReturnJsonObject;
        } catch (Exception e) {
            System.out.println("Error! while fetching question from table questions: \n");
            System.out.println(e.getMessage());
            return null;
        } finally {
            disconnectFromLocalDB();
        }
    }

    // Reads a puzzle from the local DB by ID and returns it as a 2D-Array
    @Override
    public String[][] readPuzzleFromLocalDBByIDasMatrix(int thePuzzleID) {
        String aSQLSelectStatement = "SELECT grid FROM puzzles WHERE pID=" + thePuzzleID + ";";
        String aPuzzleGridString = "";

        // Get GridString from DB
        try {
            connectToLocalDB();
            ResultSet rs = executeSQLStatement(aSQLSelectStatement);
            while (rs.next()) {
                aPuzzleGridString = "" + rs.getString(1);
            }
            System.out.println("Got Question Value from grid in puzzle " + thePuzzleID + ": " + aPuzzleGridString);
        } catch (SQLException e) {
            System.out.println("Error! while fetching grid from table puzzles: \n"+ aSQLSelectStatement);
            System.out.println(e.getMessage());
        } finally {
            disconnectFromLocalDB();
        }

        // Turn GRidString into 2D-Array to Retrun
        String[] aPuzzleGridRows = aPuzzleGridString.split("[|]");

        String[][] aReturnPuzzleGrid = new String[aPuzzleGridRows.length][];

        for (int i = 0; i < aPuzzleGridRows.length; i++) {
            aReturnPuzzleGrid[i] = aPuzzleGridRows[i].split(";");
        }
        return aReturnPuzzleGrid;
    }

    // Reads the answer of a puzzle from the local DB by ID and returns it as a
    // String
    @Override
    public String readPuzzleAnswerFromLocalDBByIDasString(int thePuzzleID) {
        String aSQLSelectStatement = "SELECT solution_word FROM puzzles WHERE pID=" + thePuzzleID + ";";
        String aPuzzleAnswer = "";

        // Get Answer from DB
        try {
            connectToLocalDB();
            ResultSet rs = executeSQLStatement(aSQLSelectStatement);
            while (rs.next()) {
                aPuzzleAnswer = "" + rs.getString(1);
            }
            System.out.println("Got Question Value from solution_word in puzzle " + thePuzzleID + ": " + aPuzzleAnswer);
        } catch (SQLException e) {
            System.out.println("Error! while fetching value from table puzzles: \n"+ aSQLSelectStatement);
            System.out.println(e.getMessage());
        }
        finally{
            disconnectFromLocalDB();
        }
        return aPuzzleAnswer;
    }

    // Gets a value from a specific column in the questions table by ID
    private String getQuestionValueFromColumnInLocalDBbyID(int theQuestionID, String theColumnName) {
        // Init Variables
        String aReturnValue = "";
        String aSQLSelectStatement = "SELECT " + theColumnName + " FROM questions WHERE qID=" + theQuestionID + ";";

        // Get Value from DB
        try {
            ResultSet rs = executeSQLStatement(aSQLSelectStatement);
            while (rs.next()) {
                aReturnValue = "" + rs.getString(1);
            }
            System.out.println("Got Question Value from " + theColumnName + ": " + aReturnValue);
        } catch (SQLException e) {
            System.out.println("Error! while fetching value from table questions: \n"+ aSQLSelectStatement);
            System.out.println(e.getMessage());
        }
        return aReturnValue;
    }

    // Inserts a given value into the local DB and returns the ID of the new entry
    private <T> Integer insertValuesIntoTable(T theInsertValues) {
        // Init Variables
        boolean isQuestion = false;
        Integer aReturnId = 0;
        String aSQLCheckIfExistsStatement = "";
        String aSQLGetIdStatement = "";
        String aSQLInsertStatement = "";
        String aSQLEditStatement = "";
        String anInsertString = "";

        try {

            // Check if theInsertValues is a JSONObject or a 2D-Array
            if (theInsertValues instanceof JSONObject) {
                JSONObject aValues = (JSONObject) theInsertValues;
                isQuestion = true;

                // Check if the question already exists
                aSQLCheckIfExistsStatement = "SELECT qID FROM questions WHERE " +
                        "question='" + aValues.getString("question") + "' AND " +
                        "length=" + aValues.getInt("length") + ";";

                // Inserting values and checking if they already exist
                aSQLInsertStatement = "INSERT INTO questions (question, length, slots, answers, linked, puzzles_pID) " +
                        "SELECT '" + aValues.getString("question") + "', '" +
                        aValues.getInt("length") + "', '" +
                        Helper.join(Helper.jsonArrayToArrayList(aValues.getJSONArray("slots")).toArray(), ";") + "', '" +
                        Helper.join(Helper.jsonArrayToArrayList(aValues.getJSONArray("answers")).toArray(), ";") + "', '" +
                        Helper.join(Helper.jsonArrayToArrayList(aValues.getJSONArray("linked")).toArray(), ";") + "', " +
                        currentPuzzleID + " " +
                        "WHERE NOT EXISTS (SELECT 1 FROM questions WHERE " +
                        "question='" + aValues.getString("question") + "' AND " +
                        "length=" + aValues.getInt("length") + " AND " +
                        "puzzles_pID=" + currentPuzzleID + ");";

                // Getting the created ID
                aSQLGetIdStatement = "SELECT qID FROM questions " +
                        "WHERE question='" + aValues.getString("question") + "' AND " +
                        "length=" + aValues.getInt("length") + " AND " +
                        "puzzles_pID=" + currentPuzzleID + ";";

            } else if (theInsertValues instanceof String[][]) {
                String[][] aValues = (String[][]) theInsertValues;
                // Turn 2D-Array into a String
                anInsertString = "";
                for (int i = 0; i < aValues.length; i++) {
                    for (int j = 0; j < aValues[i].length; j++) {
                        anInsertString += aValues[i][j] + ";";
                    }
                    if (i != aValues.length - 1)
                        anInsertString += "|";
                }

                // Check if the puzzle already exists
                aSQLCheckIfExistsStatement = "SELECT pID FROM puzzles WHERE " +
                        "pID='" + currentPuzzleID + "';";

                // SQLStatement to insert grid and possible solution word into the local DB if
                // it does not already exists
                aSQLInsertStatement = "INSERT INTO puzzles (grid, solution_word, lastUsed) " +
                        "SELECT '" + anInsertString + "', " +
                        "'', " +
                        "0 " +
                        // "WHERE NOT EXISTS (SELECT 1 FROM puzzles WHERE " +
                        // "pID=" + currentPuzzleID +
                        ";";
                aSQLGetIdStatement = "SELECT MAX(pID) FROM puzzles;";
            }

        } catch (JSONException e) {
            System.out.println("Error! while Inserting: \n" + theInsertValues);
            System.out.println(e.getMessage());
        }

        try {
            // Check if the value already exists
            ResultSet ExistsResultSet = executeSQLStatement(aSQLCheckIfExistsStatement);

            // If the value already exists edit the existing entry
            if (ExistsResultSet.next()) {
                // Editing Question
                if (isQuestion) {
                    JSONObject aValues = (JSONObject) theInsertValues;

                    // asking for ID
                    ResultSet rs = executeSQLStatement(aSQLGetIdStatement);
                    while (rs.next()) {
                        aReturnId = rs.getInt(1);
                    }

                    // Editing the existing entry
                    aSQLEditStatement = "UPDATE questions SET answers='"
                            + Helper.join(Helper.jsonArrayToArrayList(aValues.getJSONArray("answers")).toArray(), ";") +
                            "', linked='"
                            + Helper.join(Helper.jsonArrayToArrayList(aValues.getJSONArray("linked")).toArray(), ";") +
                            "', puzzles_pID=" + currentPuzzleID +
                            " WHERE question='" + aValues.getString("question") +
                            "' AND length=" + aValues.getInt("length") +
                            " AND puzzles_pID=" + currentPuzzleID + ";";

                    executeSQLStatement(aSQLEditStatement);
                }
                // Editing Puzzle
                else {

                    // asking for ID
                    ResultSet rs = executeSQLStatement(aSQLGetIdStatement);
                    while (rs.next()) {
                        aReturnId = rs.getInt(1);
                    }

                    // Editing the existing entry
                    aSQLEditStatement = "UPDATE puzzles SET grid='"
                            + anInsertString +
                            "' WHERE pID=" + currentPuzzleID + ";";

                    executeSQLStatement(aSQLEditStatement);
                }

            } else {
                // Inserting values
                executeSQLStatement(aSQLInsertStatement);

                // asking for ID
                ResultSet rs = executeSQLStatement(aSQLGetIdStatement);
                while (rs.next()) {
                    aReturnId = rs.getInt(1);
                }
            }
            System.out.println("The Insert Statement was used. Resulting Entry ID: " + aReturnId);
        } catch (SQLException e) {
            System.out.println("Error! while Inserting: \n" + aSQLInsertStatement + "\n" + aSQLGetIdStatement);
            System.out.println(e.getMessage());
        } catch (JSONException e) {
            System.out.println("Error! while Inserting: \n" + theInsertValues);
            System.out.println(e.getMessage());
        }
        return aReturnId;
    }

    private ResultSet executeSQLStatement(String theSQLStatement) {
        // System.out.println("The Statement to use: \n" + theSQLStatement);
        ResultSet aReturnResultSet = null;
        // boolean shouldHandleConnect = false;
        try {
            // shouldHandleConnect = localDBConnection == null || localDBConnection.isClosed();
            // if(shouldHandleConnect) connectToLocalDB();

            Statement aStatement = localDBConnection.createStatement();
            aStatement.execute(theSQLStatement);
            aReturnResultSet = aStatement.getResultSet();
            System.out.println("Statemant used: \n" + theSQLStatement);
        } catch (SQLException e) {
            System.out.println("Error! while using Statment: \n" + theSQLStatement);
            System.out.println(e.getMessage());
        }
        // finally {
        //     if(shouldHandleConnect) disconnectFromLocalDB();
        // }
        return aReturnResultSet;
    }

    public int selectAndSetAsCurrentHighestPuzzleID() {
        int aReturnId = 0;
        String aSQLSelectStatement = "SELECT MAX(pID) FROM puzzles;";
        try {
            connectToLocalDB();
            ResultSet rs = executeSQLStatement(aSQLSelectStatement);
            while (rs.next()) {
                aReturnId = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            disconnectFromLocalDB();
        }
        currentPuzzleID = aReturnId;
        return aReturnId;
    }

    // Initiates/Creates empty puzzle and question tables
    private void initLocalDBWithTables() {
        String CreatePuzzleTable = "CREATE TABLE IF NOT EXISTS puzzles(" +
                "pID INTEGER PRIMARY KEY, " +
                "lastUsed BOOLEAN NOT NULL CHECK (lastUsed IN (0, 1)), " +
                "grid VARCHAR, " +
                "solution_word VARCHAR);";
        String CreateQuestionsTable = "CREATE TABLE IF NOT EXISTS questions(" +
                "qID INTEGER PRIMARY KEY, " +
                "question VARCHAR, " +
                "length INTEGER, " +
                "slots VARCHAR, " +
                "answers VARCHAR, " +
                "linked VARCHAR, " +
                "puzzles_pID INTEGER, " +
                "FOREIGN KEY (puzzles_pID) REFERENCES puzzles(pID));";
        // String CreateGridPositionsTable = "CREATE TABLE IF NOT EXISTS gridpositions(" +
        //         "puzzles_pID INTEGER, " +
        //         "questions_qID INTEGER, " +
        //         "gridposition VARCHAR, " +
        //         "FOREIGN KEY (puzzles_pID) REFERENCES puzzles(pID), " +
        //         "FOREIGN KEY (questions_qID) REFERENCES questions(qID), " +
        //         "CONSTRAINT PK_gridposition PRIMARY KEY (puzzles_pID, questions_qID));";

        try {
            executeSQLStatement(CreatePuzzleTable);
            executeSQLStatement(CreateQuestionsTable);
            // executeSQLStatement(CreateGridPositionsTable);
            System.out.println("Finished Executing Create Tables");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // Deletes a given Table
    private void deleteLocalDBTable(String theTableToDelete) {
        String DeleteTable = "DROP TABLE " + theTableToDelete + ";";
        try {
            executeSQLStatement(DeleteTable);
            System.out.println("table " + theTableToDelete + " has been deleted permanently.");
        } catch (Exception e) {
            System.out.println("Error! during table drop: \n" + theTableToDelete);
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void ResetAllTablesInLocalDB(){
        deleteLocalDBTable("questions");
        deleteLocalDBTable("puzzles");
        // deleteLocalDBTable("gridpositions");
        initLocalDBWithTables();
    }
}