import java.io.*;
import java.net.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class ContentServer {

    private static LamportClock lamportClock = new LamportClock();

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java ContentServer <portNumber> <locationOfWeather-sample-data.txt> <ContentServerID>");
            return;
        }

        String serverPort = args[0];
        String weatherDataFileLocation = args[1];
        String contentServerIdNumber = args[2];

        try {
            // Read weather data from the file
            JSONArray weatherArray = new JSONArray();
            BufferedReader reader = new BufferedReader(new FileReader(weatherDataFileLocation));
            StringBuilder record = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    continue; // Skip empty lines
                }
                if (line.startsWith("id:")) {
                    if (record.length() > 0) {
                        // Convert the previous record to a JSONObject
                        weatherArray.put(parseWeatherData(record.toString()));
                        record.setLength(0); // Reset the record
                    }
                }
                record.append(line).append("\n"); // Append the line to the current record
            }

            // Add the last record if exists
            if (record.length() > 0) {
                weatherArray.put(parseWeatherData(record.toString()));
            }

            reader.close();

            // Parse port number
            int portNumber;
            try {
                portNumber = Integer.parseInt(serverPort);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number: " + serverPort);
                return;
            }

            // Start the Content Server
            System.out.println("Content Server Started with ID: " + contentServerIdNumber);

            // Connect to the Aggregation Server
            Socket socket = new Socket("localhost", portNumber);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Prepare JSON data for sending
            String json = weatherArray.toString();
            out.println("PUT /weather.json HTTP/1.1");
            out.println("Host: localhost");
            out.println("Content-Type: application/json");
            out.println("Content-Length: " + json.length());
            out.println("Lamport-Clock: " + lamportClock.getTime());
            out.println("Content-Server-ID: " + contentServerIdNumber);
            out.println();
            out.println(json);
            out.flush();

            // Read the server response
            String responseBody;
            while ((responseBody = in.readLine()) != null) {
                if (responseBody.startsWith("Lamport-Clock:")) {
                    int serverClock = Integer.parseInt(responseBody.split(":")[1].trim());
                    lamportClock.updateTime(serverClock);
                } else {
                    System.out.println(responseBody);
                }
            }

            System.out.println("Lamport Clock Timestamp at Content Server: " + lamportClock.getTime());
            socket.close();
        } catch (FileNotFoundException e) {
            System.err.println("Weather data file not found: " + weatherDataFileLocation);
        } catch (IOException e) {
            System.err.println("I/O error occurred: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to parse weather data from a formatted string
    private static JSONObject parseWeatherData(String data) {
        JSONObject weatherData = new JSONObject();
        String[] lines = data.split("\n");
        for (String line : lines) {
            String[] parts = line.split(":");
            if (parts.length == 2) {
                String key = parts[0].trim();
                String value = parts[1].trim().replaceAll(",", ""); // Remove trailing commas
                // Convert numerical values to their appropriate types
                switch (key) {
                    case "id":
                    case "name":
                    case "state":
                    case "time_zone":
                    case "cloud":
                    case "wind_dir":
                        weatherData.put(key, value);
                        break;
                    case "lat":
                    case "lon":
                    case "air_temp":
                    case "apparent_t":
                    case "dewpt":
                    case "press":
                    case "rel_hum":
                    case "wind_spd_kmh":
                    case "wind_spd_kt":
                        try {
                            weatherData.put(key, Double.parseDouble(value)); // Try parsing the value
                        } catch (NumberFormatException e) {
                            System.err.println("Could not parse double for key: " + key + " with value: " + value);
                        }
                        break;
                    case "local_date_time":
                    case "local_date_time_full":
                        weatherData.put(key, value);
                        break;
                    default:
                        System.err.println("Unknown field: " + key);
                }
            } else {
                System.err.println("Invalid line in weather data: " + line);
            }
        }
        return weatherData;
    }

}


