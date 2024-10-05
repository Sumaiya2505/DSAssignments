import java.io.*;
import java.net.*;
import org.json.JSONArray;

public class Client {

    private static LamportClock lamportClockTimeStamp = new LamportClock();

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java Client <host> <port> [ID]");
            return;
        }

        String host = args[0];  // First argument is the host
        String serverPort = args[1]; // Second argument is the port
        String id = args.length > 2 ? args[2] : null; // Optional ID

        try {
            Socket socket = new Socket(host, Integer.parseInt(serverPort));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println("GET " + (id != null ? "/" + id : "") + " HTTP/1.1");
            out.println("Host: " + host);
            out.println("Lamport-Clock: " + lamportClockTimeStamp.getTime());
            out.println();
            out.flush();

            String responseBody;
            while ((responseBody = in.readLine()) != null) {
                if (responseBody.startsWith("Lamport-Clock:")) {
                    int serverClock = Integer.parseInt(responseBody.split(":")[1].trim());
                    lamportClockTimeStamp.updateTime(serverClock);
                } else {
                    System.out.println(responseBody);
                }
            }

            System.out.println("Lamport Clock Timestamp at Client: " + lamportClockTimeStamp.getTime());
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
