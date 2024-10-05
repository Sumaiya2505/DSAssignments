import java.io.*;
import java.net.*;
import java.util.*;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class AggregationServer {

    private static LamportClock lamportClock;
    private static final int TIME_LIMIT = 3000;
    private static final int PORT_NUMBER = 4040;
    private static Map<String, Long> contentServerValue;
    private static JSONArray weatherData;

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : PORT_NUMBER;
        contentServerValue = new HashMap<>();
        weatherData = new JSONArray();
        lamportClock = new LamportClock();
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Aggregation server started on port number " + port);
        while (true) {
            Socket client = serverSocket.accept();
            new HandlingClient(client).start();
        }
    }

    private static synchronized JSONArray getData() {
        lamportClock.increaseTime();
        long currentTime = System.currentTimeMillis();
        for (int i = 0; i < weatherData.length(); i++) {
            JSONObject object = weatherData.getJSONObject(i);
            String contentServerId = object.getString("id");
            if (currentTime - contentServerValue.getOrDefault(contentServerId, 0L) > TIME_LIMIT) {
                weatherData.remove(i);
                i--;
            }
        }
        return weatherData;
    }

    private static synchronized JSONObject getById(String id) {
        for (int i = 0; i < weatherData.length(); i++) {
            JSONObject object = weatherData.getJSONObject(i);
            if (object.getString("id").equals(id)) {
                return object;
            }
        }
        return null; // Return null if the ID is not found
    }

    private static class HandlingClient extends Thread {
        private Socket socket;

        public HandlingClient(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                String input;
                StringBuilder request = new StringBuilder();
                while ((input = in.readLine()) != null && !input.isEmpty()) {
                    request.append(input).append("\n");
                }
                String[] lines = request.toString().split("\n");
                String[] parts = lines[0].split(" ");
                String method = parts[0];
                String path = parts[1];
                String id = path.equals("/") ? null : path.substring(1);
                if ("GET".equalsIgnoreCase(method)) {
                    if (id != null) { // Handling GET by ID
                        handleGetByIdRequest(out, id);
                    } else {
                        handleGetRequest(out);
                    }
                }
                else if ("PUT".equalsIgnoreCase(method)) {
                    handlePutRequest(in, out, lines);
                } else {
                    out.println("HTTP/1.1 400 Bad Request");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private static void handleGetRequest(PrintWriter out) {
            JSONArray currentWeatherData = getData();
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: application/json");
            out.println("Lamport-Clock: " + lamportClock.getTime());
            out.println();
            out.println(currentWeatherData.toString());
        }

        private static void handleGetByIdRequest(PrintWriter out, String id) {
            JSONObject data = getById(id);
            if (data != null) {
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: application/json");
                out.println("Lamport-Clock: " + lamportClock.getTime());
                out.println();
                out.println(data.toString());
            } else {
                out.println("HTTP/1.1 404 Not Found");
            }
        }

        private static void handlePutRequest(BufferedReader in, PrintWriter out, String[] requestLines) throws IOException {
            int clientClockTime = 0;
            int contentLength = 0;
            String contentServerId = "";

            for (String line : requestLines) {
                if (line.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(line.split(":")[1].trim());
                } else if (line.startsWith("Lamport-Clock:")) {
                    clientClockTime = Integer.parseInt(line.split(":")[1].trim());
                } else if (line.startsWith("Content-Server-ID:")) {
                    contentServerId = line.split(":")[1].trim();
                }
            }

            char[] body = new char[contentLength];
            in.read(body, 0, contentLength);
            String jsonData = new String(body);
            System.out.println("Received JSON: " + jsonData); // Log incoming JSON

            try {
                JSONArray newDataArray = new JSONArray(jsonData); // Change this line to JSONArray
                for (int i = 0; i < newDataArray.length(); i++) {
                    JSONObject newData = newDataArray.getJSONObject(i);
                    updateData(newData, contentServerId, clientClockTime); // Update data for each JSONObject
                }
                out.println("HTTP/1.1 200 OK");
                out.println("Lamport-Clock: " + lamportClock.getTime());
            } catch (JSONException e) {
                System.err.println("Error parsing JSON: " + e.getMessage());
                out.println("HTTP/1.1 500 Internal Server Error");
            }
        }

        private static synchronized void updateData(JSONObject newEntry, String contentServerId, int receivedClock) {
            lamportClock.updateTime(receivedClock);
            contentServerValue.put(contentServerId, System.currentTimeMillis());

            // Remove old data for the content server ID if it exists
            for (int i = 0; i < weatherData.length(); i++) {
                JSONObject object = weatherData.getJSONObject(i);
                if (object.getString("id").equals(contentServerId)) {
                    weatherData.remove(i);
                    break;
                }
            }
            weatherData.put(newEntry); // Add the new data entry
        }

    }
}
