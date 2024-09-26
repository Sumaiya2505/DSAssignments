import java.io.*;
import java.net.*;
import org.json.JSONArray;

//Client sends the GET request to the AggregationServer in order to fetch the weather data.
public class Client {

//    Creating the instance of LamportClock in order use the methods.
    private static LamportClock lamportClockTimeStamp = new LamportClock();

    public static void main(String[] args)
    {
//        To establish the connection on the specified port number use the following format.
        if (args.length < 1)
        {
            System.out.println("java Client portNumber");
            return;
        }

        String serverAddress = args[0];
//        Using try-catch block to handle the exceptions.
        try {

//          Establishing the connection using Sockets.
//          Declaring the portNumber  and the hostname to establish the connection with the AggregationServer.
            Socket socket = new Socket(serverAddress.split(":")[0], Integer.parseInt(serverAddress.split(":")[1]));
            System.out.println("Client Connected");
//          Creating an object for BufferedReader to read the input text data from socket connection.
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//          Sending the data via the Sockets enabling the autoflush as true,verifies that the data is sent immediately to the Server.
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
//            Handling the GET Request
            out.println("GET /weather.json HTTP/1.1");
            out.println();
//            Ensuring that the request is sent.
            out.flush();
//          Constructing the JSON.
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null)
            {
                if (line.startsWith("Lamport-Clock:"))
                {
                    int serverClock = Integer.parseInt(line.split(":")[1].trim());
                    lamportClockTimeStamp.updateTime(serverClock);
                }
                response.append(line).append("\n");
            }
//          Extracting the JSON response body from the response.
//          Splitting the String to get the body
            String jsonResponse = response.toString().split("\n\n", 2)[1];
            JSONArray weatherData = new JSONArray(jsonResponse);

//          Pretty-print the JSON array with indentation.
//          four spaces of indentation
            System.out.println(weatherData.toString(4));
            System.out.println("Lamport Clock at GET Client: " + lamportClockTimeStamp.getTime());
            socket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}

