package com.example.crossle.LocalDBManager;
import org.json.JSONObject;

public interface LocalDBManagerInterface {
    /**
     * inserts or updates the given JSONObject to the local DB and returns the ID of the entry
     * May not check for correct formating
     * @param theJSONObjectToWrite, @param thePuzzleId
     * @return ID of the entry
     */
    public int writeQuestionToLocalDB(int thePuzzleId, JSONObject theJSONObjectToWrite);
    /**
     * inserts or updates the given two dimensional String Array (Matrix) to the local DB and returns the ID of the entry
     * May not check for correct formating
     * @param theMatrixToWrite
     * @return ID of the entry
     */
    public int writePuzzleToLocalDB(String[][] theMatrixToWrite);
    /**
     * returns the ID assosiatied local DB entry as a JSONObject
     * @param theQuestionID
     * @return question as a JSONObject
     */
    public JSONObject readQuestionFromLocalDBByIDasJSONObject(int theQuestionID);
    /**
     * returns the ID assosiatied local DB entry as a String array
     * @param thePuzzleID
     * @return puzzle grid as a String array
     */
    public String[][] readPuzzleFromLocalDBByIDasMatrix(int thePuzzleID);

    /**
     * returns the current answer of the puzzle with the given ID as a String
     * @param thePuzzleID
     * @return answer as a String
     */
    public String readPuzzleAnswerFromLocalDBByIDasString(int thePuzzleID);

    public void TemporaryMethodToStartOtherMethodsOneTime();
    public void ResetAllTablesInLocalDB();
}