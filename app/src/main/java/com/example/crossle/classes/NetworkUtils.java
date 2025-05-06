package com.example.crossle.classes;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import com.example.crossle.CamActivity;

public class NetworkUtils {

    private static final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static void sendImageAndJsonToServerAsync(Bitmap bitmap, JSONObject json, String serverAddress, int port, NetworkCallback callback, Context context) {
        executorService.execute(() -> {
            try {
                JSONObject response = sendImageAndJsonToServer(bitmap, json, serverAddress, port, context);
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onResponse(response);
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (context instanceof CamActivity) {
                        ((CamActivity) context).revertUIState("Error sending data");
                    }
                });
            }
        });
    }

    public interface NetworkCallback {
        void onResponse(JSONObject response);
    }

    public static JSONObject sendImageAndJsonToServer(Bitmap bitmap, JSONObject json, String serverAddress, int port, Context context) {
        try {
            // Convert Bitmap to Byte Array
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
            byte[] imageBytes = byteStream.toByteArray();

            // Convert JSON to Bytes
            String jsonString = json.toString();
            byte[] jsonBytes = jsonString.getBytes("UTF-8");

            // Establish a Socket Connection
            Socket socket = new Socket(serverAddress, port);
            OutputStream outputStream = socket.getOutputStream();

            // Send Image Size and Data
            outputStream.write(intToBytes(imageBytes.length)); // Send image length first
            outputStream.write(imageBytes); // Send image data

            // Send JSON Size and Data
            outputStream.write(intToBytes(jsonBytes.length)); // Send JSON length
            outputStream.write(jsonBytes); // Send JSON data

            // Read Responses from Server
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());

            JSONObject data = null;

            while (true) {
                // Read the size of the incoming JSON message
                int responseSize = inputStream.readInt();

                // Read the JSON message bytes
                byte[] responseBytes = new byte[responseSize];
                inputStream.readFully(responseBytes);

                // Convert the bytes to a JSON string
                String responseString = new String(responseBytes, "UTF-8");
                responseString = responseString.replaceAll("'", "\\\"");
                // Parse the JSON string into a JSONObject
                JSONObject responseJson = new JSONObject(responseString);

                // Handle the response based on its type
                String type = responseJson.getString("type");
                if ("status_update".equals(type)) {
                    Log.i("TCP Client", "Status update: " + responseJson.toString());
                    String message = responseJson.getString("message");
                    int percentage = responseJson.getInt("percentage");

                    // Update the progress in CamActivity
                    ((CamActivity) context).updateProgress(message, percentage);
                } else if ("finished".equals(type)) {
                    Log.i("TCP Client", "Final response: " + responseJson.toString());
                    data = responseJson.getJSONObject("data");
                    break; // Exit loop on "finished"
                } else if ("error".equals(type)) {
                    Log.e("TCP Client", "Error response: " + responseJson.toString());
                    ((CamActivity) context).revertUIState("Error: " + responseJson.getString("message"));
                    break;
                }
            }

            // Close the Socket
            outputStream.close();
            socket.close();
            if (data != null)
                ((CamActivity) context).openPuzzle(data);
        } catch (EOFException e) {
            Log.e("TCP Client", "Connection closed by server", e);
            ((CamActivity) context).revertUIState("Connection closed by server");
        } catch (IOException | JSONException e) {
            Log.e("TCP Client", "Error sending data", e);
            ((CamActivity) context).revertUIState("Error sending data");
        } catch (Exception e) {
            Log.e("TCP Client", "Unknown error", e);
            ((CamActivity) context).revertUIState("Unknown error");
        }

        return null;
    }

    private static byte[] intToBytes(int value) {
        return new byte[] {
                (byte)(value >> 24),
                (byte)(value >> 16),
                (byte)(value >> 8),
                (byte)value
        };
    }
}