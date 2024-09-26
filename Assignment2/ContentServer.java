import java.io.*;
import java.net.*;
import org.json.JSONObject;

//Content Server which handles PUT request on the Aggregation Server.It PUT's the value on the AggregationServer
// if the request is not sent in the proper format it may throw 500 internal server error
//if the request is sent proper format with proper connection established it may send the response as 200 (OK) Success.
public class ContentServer {

//   Creating an instance of LamportClock to use the  methods such as increaseTime,updateTime.
    private static LamportClock lamportClock = new LamportClock();

    public static void main(String[] args)
    {
//            To run the file on the specified port number use this format.
            if (args.length < 2) {
                System.out.println("java ContentServer portNumber locationOfWeather-sample-data.txt");
                return;
            }
//          Assigning the port number and location of the Weather-sample-data file.
            String serverPort = args[0];
            String weatherDataFileLocation = args[1];
//USing try-catch block to handle the exceptions.
            try {
//                Creating an instance of the JSONObject for parsing the text and constructing JSON
                JSONObject weatherSampleData = new JSONObject();
                BufferedReader reader = new BufferedReader(new FileReader(weatherDataFileLocation));
                String line;
//                Constructing JSON Object
                while ((line = reader.readLine()) != null) {
                    String[] words = line.split(":");
                    if (words.length == 2) {
                        weatherSampleData.put(words[0].trim(), words[1].trim());
                    }
                }
                reader.close();
//                Handling case where only portNumber is given assuming localhost
                String hostName = "localhost"; // Default host
                int portNumber;
//                If the serverAddress contains a colon, split host and port
                if (serverPort.contains(":"))
                {
//              Segreagating and decalring the portNumber and hostName
                    String[] address = serverPort.split(":");
                    hostName = address[0];
                    portNumber = Integer.parseInt(address[1]);
                }
                else {
//                    If there is no colon, assume it as the port Number
                    portNumber = Integer.parseInt(serverPort);
                }
//          Establishing the connection using Sockets
//          Declaring the portNumber  and the hostname to establish the connection with the AggregationServer
                Socket socket = new Socket(hostName, portNumber);
//          Initiating the Content Server for Communication.
                System.out.println("Content Server Started");
//          Sending the data via the Sockets enabling the autoflush as true,verifies that the data is sent immediately to the Server.
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
//          Creating an object for BufferedReader to read the input text data from socket connection.
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//          Constructing JSON formatted String.
                String json = weatherSampleData.toString();
//          Handling the PUT request.
                out.println("PUT /weather.json HTTP/1.1");
//          Adding the host header.
                out.println("Host: " + hostName);
//          Setting the  content type.
                out.println("Content-Type: application/json");
//          Setting the content length header.
                out.println("Content-Length: " + json.length());
//          Lamport Clock timestamp.
                out.println("Lamport-Clock: " + lamportClock.getTime());
                out.println();
//          Sending the JSON body.
                out.println(json);
//          Ensures that the request is sent.
                out.flush();
                String responseBody;
//          Reading the data from the BufferedReader.
                while ((responseBody = in.readLine()) != null) {
                    if (responseBody.startsWith("Lamport-Clock:")) {
                        int serverClock = Integer.parseInt(responseBody.split(":")[1].trim());
//                        Updating the timestamp based on the value on the server.
                        lamportClock.updateTime(serverClock);
                    }
//                    Printing the response the API call (PUT request).
                    System.out.println(responseBody);
                }
//                System.out.println("weather data: " + json);
//                Value of the LamportClock at ContentServer.
                System.out.println("Lamport Clock TimeStamp at Content Server: " + lamportClock.getTime());
//                Closing the Connection.
                socket.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
}



