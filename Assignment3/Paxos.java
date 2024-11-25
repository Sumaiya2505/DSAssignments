import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * This class implements the Paxos consensus algorithm.
 * This is the main class which class the councillor class and proposer class
 */
public class Paxos
{
    /**
     * The total number of councillors participating in the Paxos protocol
     */
    public static final int TOTAL_COUNCILLORS = 9;

    /**
     * The majority of the councilllors required to win the election.
     */
    public static final int MAJORITY = TOTAL_COUNCILLORS / 2 + 1;

    /**
     * The main method to implement the Paxos protocol.
     */
    public static void main(String[] args)
    {
        ExecutorService executor = Executors.newFixedThreadPool(TOTAL_COUNCILLORS);
        List<Councillor> councillors = new ArrayList<>();

        /**
         * Initializing the number of councillors.
         */
        for (int i = 1; i <= TOTAL_COUNCILLORS; i++) {
            int id = i;
            Councillor councillor = new Councillor("M" + id, 5000 + id);
            councillors.add(councillor);
            executor.execute(() -> councillor.start());
        }

//        Proposer proposer1 = new Proposer("M1", councillors);
//        proposer1.initiateVoting("M1 for President");
//        Proposer proposer2 = new Proposer("M8", councillors);
//        proposer2.initiateVoting("M8 for President");

        /**
         * Simulating two proposers initating vote simultaneously.
         */
         Proposer proposer1 = new Proposer("M1", councillors);
         Proposer proposer2 = new Proposer("M8", councillors);
        new Thread(() -> proposer1.initiateVoting("M1 for President"));
        new Thread(() -> proposer2.initiateVoting("M8 for President"));

        Thread thread1 = new Thread(() -> proposer1.initiateVoting("M1 for President"));
        Thread thread2 = new Thread(() -> proposer2.initiateVoting("M8 for President"));
        thread1.start();

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        thread2.start();
        executor.shutdown();
    }
}

/**
 * This class represents a Councillor in the Paxos protocol.
 * Each Councillor listens for messages and participates in the protocol phases.
 */
class Councillor implements Runnable
{
    public final String name;
    public final int port;
    private ServerSocket serverSocket;
    private String acceptedProposal = null;
    private String currentProposal = null;
    private final Random random = new Random();
    public boolean isConnected = true; // Simulates M2/M3 connectivity state

    public Councillor(String name, int port) {
        this.name = name;
        this.port = port;
    }

    /**
     * Establishes the communication via sockets.
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println(name + " is ready on port " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> handleRequest(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles incoming requests from proposers.
     *
     * @param socket Socket for the incoming connection.
     */
    private void handleRequest(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
//            Simulate behaviour specific delays and connectivity.
            simulateBehavior();

            if (!isConnected) {
                // Drop the connection
                System.out.println(name + " is offline.");
                return;
            }
            String message = in.readLine();
            synchronized (this) { // Ensure thread-safe access
                if (message.startsWith("PREPARE")) {
                    String proposal = message.split(" ")[1];
                    if (acceptedProposal == null) {
                        currentProposal = proposal;
                        out.println("PROMISE " + proposal);
                    } else {
                        out.println("PROMISE " + acceptedProposal);
                    }
                } else if (message.startsWith("ACCEPT")) {
                    String proposal = message.split(" ")[1];
                    if (proposal.equals(currentProposal)) {
                        acceptedProposal = proposal;
                        out.println("ACCEPTED " + proposal);
                    } else {
                        out.println("REJECTED " + proposal);
                    }
                } else if (message.startsWith("DECIDE")) {
                    String proposal = message.split(" ")[1];
                    acceptedProposal = proposal;
                    System.out.println(name + " decided on: " + proposal);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Simulates specific behavior for councillors based on their name.
     */
    private void simulateBehavior() {
        switch (name) {
            case "M1":// Always responsive, no delay
                break;
            case "M2":// Delayed responses unless in connected state
                isConnected = random.nextBoolean(); // Randomly decide connectivity
                if (isConnected) {
                    sleep(200); // Small delay
                } else {
                    sleep(2000); // Large delay or timeout
                }
                break;
            case "M3":// Occasionally goes offline
                isConnected = random.nextInt(10) > 2; // 80% chance of being online
                break;
            default:// Randomized delays for M4-M9
                sleep(random.nextInt(500) + 100); // Random delay between 100ms and 600ms
                break;
        }
    }

    private void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run() {
        start();
    }
}

/**
 * This class represents a Proposer in the Paxos protocol.
 * Proposers initiate voting with a proposal and coordinate with councillors.
 */
class Proposer
{
    private final String name;
    private final List<Councillor> councillors;
    private final int majority = Paxos.MAJORITY;

    /**
     * Creates a Proposer with the specified name and councillors.
     *
     * @param name        Name of the proposer.
     * @param councillors List of councillors to communicate with.
     */
    public Proposer(String name, List<Councillor> councillors)
    {
        this.name = name;
        this.councillors = councillors;
    }


    private String sendMessage(Councillor councillor, String message) {
        try (Socket socket = new Socket("localhost", councillor.port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out.println(message);
            String response = in.readLine();
            return response != null ? response : "NO_RESPONSE";
        } catch (IOException e) {
            System.out.println(councillor.name + " is offline.");
            return "OFFLINE";
        }
    }

    /**
     * Initiates the Paxos voting process for a given proposal.
     *
     * @param proposal The proposal to be voted on.
     */
    public void initiateVoting(String proposal)
    {
        System.out.println(name + " initiating voting with proposal: " + proposal);

//        Phase 1: Prepare
        List<Councillor> promises = new ArrayList<>();
        for (Councillor councillor : councillors) {
            String response = sendMessage(councillor, "PREPARE " + proposal);
            if (response.startsWith("PROMISE")) {
                promises.add(councillor);
            }
        }

        if (promises.size() >= majority) {
//            Phase 2: Accept
            List<Councillor> acceptances = new ArrayList<>();
            for (Councillor councillor : promises) {
                String response = sendMessage(councillor, "ACCEPT " + proposal);
                if (response.startsWith("ACCEPTED")) {
                    acceptances.add(councillor);
                }
            }

            if (acceptances.size() >= majority) {
//                 Phase 3: Decide
                for (Councillor councillor : acceptances) {
                    sendMessage(councillor, "DECIDE " + proposal);
                }
                System.out.println(name + " successfully decided on proposal: " + proposal);
            } else {
                System.out.println(name + " failed to get majority acceptances.");
            }
        } else {
            System.out.println(name + " failed to get majority promises.");
        }
    }
}


