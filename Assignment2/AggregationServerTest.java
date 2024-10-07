import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class AggregationServerTest {

    private AggregationServer server;
    private LamportClock clock;

    @BeforeEach
    public void setUp() {
        server = new AggregationServer();
        server.lamportClock = new LamportClock();
        server.weatherData = new JSONArray();
        server.contentServerValue = new HashMap<>();
    }

    // Test getData() method
    @Test
    public void testGetDataWithValidData() {
        // Add mock weather data
        JSONObject mockData = new JSONObject();
        mockData.put("id", "server1");
        mockData.put("temperature", 25);
        server.weatherData.put(mockData);
        server.contentServerValue.put("server1", System.currentTimeMillis());

        // Fetch data
        JSONArray result = AggregationServer.getData();

        // Check if the data was retrieved correctly
        assertEquals(1, result.length());
        assertEquals("server1", result.getJSONObject(0).getString("id"));
    }

    // Test getData() when data is expired
    @Test
    public void testGetDataWithExpiredData() {
        // Add expired data
        JSONObject mockData = new JSONObject();
        mockData.put("id", "server1");
        mockData.put("temperature", 25);
        server.weatherData.put(mockData);
        server.contentServerValue.put("server1", System.currentTimeMillis() - AggregationServer.TIME_LIMIT - 1);

        // Fetch data
        JSONArray result = AggregationServer.getData();

        // Check if the data was removed
        assertEquals(0, result.length());
    }

    // Test getById() with a valid ID
    @Test
    public void testGetByIdValid() {
        JSONObject mockData = new JSONObject();
        mockData.put("id", "server1");
        mockData.put("temperature", 25);
        server.weatherData.put(mockData);

        // Fetch data by ID
        JSONObject result = AggregationServer.getById("server1");

        // Check if correct data is returned
        assertNotNull(result);
        assertEquals("server1", result.getString("id"));
    }

    // Test getById() with an invalid ID
    @Test
    public void testGetByIdInvalid() {
        JSONObject mockData = new JSONObject();
        mockData.put("id", "server1");
        mockData.put("temperature", 25);
        server.weatherData.put(mockData);

        // Fetch data with a non-existent ID
        JSONObject result = AggregationServer.getById("server2");

        // Result should be null
        assertNull(result);
    }


    // Test handling GET request and retrieving weather data
    @Test
    public void testHandleGetRequest() throws IOException {
        // Add mock weather data
        JSONObject mockData = new JSONObject();
        mockData.put("id", "server1");
        mockData.put("temperature", 25);
        server.weatherData.put(mockData);
        server.contentServerValue.put("server1", System.currentTimeMillis());

        // Mock
    }
}