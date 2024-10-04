import java.io.*;
import java.net.*;
import java.util.*;
import org.json.JSONObject;
import org.json.JSONArray;

// AggregrationServer handles the PUT request from the ContentServer and also handles the GET request from the Client.
// If the request is other than GET or PUT it sends the response as BAD request.
// Removes the old data that is more than 30 seconds.
public class AggregationServer {

//      Initializing the LamportClock instance.
        private static LamportClock lamportClock;
//      Declaring the time limit that is thirty seconds ,after thirty seconds the AgrregationServer would remove the old Data.
        private static final int TIME_LIMIT = 30000;
//      Declaring the port NUmber to establish the connection.
        private static final int PORT_NUMBER = 4040;
//      HashMap to store the values sent by the contentserver.
        private static Map<String, Long> contentServerValue;
//        A JSONArray that is ,it is an array of JSON objects, to store the weather data.
        private static JSONArray weatherData;

        public static void main(String[] args) throws IOException
        {
            int port = args.length > 0 ? Integer.parseInt(args[0]) : PORT_NUMBER;
//      HashMap to store the values sent by the contentserver.
            contentServerValue = new HashMap<>();
//      A JSONArray that is ,it is an array of JSON objects, to store the weather data.
            weatherData = new JSONArray();
//      Creating the LamportClock instance.
            lamportClock = new LamportClock();
//      Starting  the server socket to establish the connection
            ServerSocket serverSocket = new ServerSocket(port);
//      Initiating the content server for communication.
            System.out.println("Aggregation server started on port number " + port);
//      Establishing the connection with the client.
            while (true) {
                Socket client = serverSocket.accept();
                new HandlingClient(client).start();
            }
        }

//      Handling the weather data.
        private static synchronized JSONArray getData()
        {
//      Increment the timestamp.
        lamportClock.increaseTime();
//        Removing the old data.
        long currentTime = System.currentTimeMillis();
        for (int i = 0; i < weatherData.length(); i++)
        {
            JSONObject object = weatherData.getJSONObject(i);
            String contentServerId = object.getString("id");
            if (currentTime - contentServerValue.getOrDefault(contentServerId, 0L) > TIME_LIMIT) {
                weatherData.remove(i);
                i--;
            }
        }
        return weatherData;
        }

//     Handling the updates on the weatherdata
    private static synchronized void updateData(JSONObject newEntry, String contentServerId, int receivedClock) {
//             Update Lamport clock based on received clock  timestamp and increment it.
            lamportClock.updateTime(receivedClock);
//           Adding the value in the hashmap.
            contentServerValue.put(contentServerId, System.currentTimeMillis());

//            Removing the  old data for the same content server
            for (int i = 0; i < weatherData.length(); i++)
            {
                JSONObject object = weatherData.getJSONObject(i);
                if (object.getString("id").equals(contentServerId))
                {
                    weatherData.remove(i);
                    break;
                }
            }

//            Adding the new entry.
            weatherData.put(newEntry);
        }

//      Handling client ,creating socket for the clients.
        private static class HandlingClient extends Thread
        {
            private Socket socket;

            public HandlingClient(Socket socket)
            {
                this.socket = socket;
            }

// Handles all the requests from both the Content Server and the Client.
            public void run()
            {
//                Reading the value sent through the socket.
                try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true))
                    {
                    String input;
//                    Costructing the proper JSON format.
                    StringBuilder request = new StringBuilder();
                    while ((input = in.readLine()) != null && !input.isEmpty()) {
                        request.append(input).append("\n");
                    }

//                  Parsing the request
                    String[] lines = request.toString().split("\n");
                    String[] parts = lines[0].split(" ");
                    String method = parts[0];
                    String path = parts[1];

                    if ("GET".equalsIgnoreCase(method))
                    {
                        handleGetRequest(out);
                    }
                    else if ("PUT".equalsIgnoreCase(method)) {
                        handlePutRequest(in, out, lines);
                    }
                    else
                    {
                        out.println("HTTP/1.1 400 Bad Request");
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }

//          Handling the Get Request.
            private void handleGetRequest(PrintWriter out)
            {
                JSONArray currentWeatherData = getData();
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: application/json");
                out.println("Lamport-Clock: " + lamportClock.getTime());
                out.println();
                out.println(currentWeatherData.toString());
            }


//          Handling the Put Request.
            private void handlePutRequest(BufferedReader in, PrintWriter out, String[] requestLines) throws IOException
            {
                int clientClockTime = 0;
//              Reading the content length and body
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

                try {
                    JSONObject newData = new JSONObject(jsonData);
//                    String contentServerId = newData.getString("id");
                    updateData(newData, contentServerId, clientClockTime);
                    out.println("HTTP/1.1 200 OK");
                    out.println("Lamport-Clock: " + lamportClock.getTime());
                } catch (Exception e) {
                    out.println("HTTP/1.1 500 Internal Server Error");
                }
            }


        }
}



