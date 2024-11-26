import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * This class implements the Paxos consensus algorithm.
 * Paxos is a distributed consensus protocol used to agree on a single value among distributed systems.
 */
public class Paxos {
    /**
     * The total number of councillors participating in the Paxos protocol.
     * This is the quorum size for the system.
     */
    public static final int TOTAL_COUNCILLORS = 9;

    /**
     * The majority of councillors required to reach consensus.
     * Majority is calculated as (TOTAL_COUNCILLORS / 2) + 1.
     */
    public static final int MAJORITY = TOTAL_COUNCILLORS / 2 + 1;

    /**
     * The main method to initialize councillors and simulate the Paxos protocol.
     */
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(TOTAL_COUNCILLORS);
        List<Councillor> councillors = new ArrayList<>();

//        Initializing the councillors with unique names and ports
        for (int i = 1; i <= TOTAL_COUNCILLORS; i++) {
            int id = i;
            Councillor councillor = new Councillor("M" + id, 5000 + id);
            councillors.add(councillor);
            executor.execute(() -> councillor.start());
        }

//         Simulate two proposers initiating voting simultaneously
        Proposer proposer1 = new Proposer("M1", councillors);
        Proposer proposer2 = new Proposer("M8", councillors);

        Thread thread1 = new Thread(() -> proposer1.initiateVoting("M1 for President"));
        Thread thread2 = new Thread(() -> proposer2.initiateVoting("M8 for President"));

//         Start  the proposer threads with a delay to simulate simultaneous proposals
        thread1.start();
        try {
//            Delay to simulate overlapping proposals
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        thread2.start();
//        Shutdown the executor to gracefully terminate the councillors
        executor.shutdown();
    }
}

/**
 * Represents a councillor participating in the Paxos protocol.
 * Each councillor listens for messages from proposers and participates in the three phases:
 * 1. Prepare phase: Responds with a promise if no other proposal has been accepted.
 * 2. Accept phase: Accepts the proposal if it matches the promised one.
 * 3. Decide phase: Commits to the proposal and concludes consensus.
 */
class Councillor implements Runnable {
//     Unique identifier for the councillor
    public final String name;
//     Port number for communication
    public final int port;
//     Socket for listening to requests
    private ServerSocket serverSocket;
//     Proposal already accepted
    private String acceptedProposal = null;
//     Proposal being evaluated
    private String currentProposal = null;
//    For simulating delays and connectivity
    private final Random random = new Random();
//    Simulates the councillor's connectivity state
    public boolean isConnected = true;

    public Councillor(String name, int port)
    {
        this.name = name;
        this.port = port;
    }

    /**
     * Starts the councillor by opening a server socket and waiting for connections.
     * Each connection is handled in a separate thread.
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
     * Handles incoming requests from the proposers.
     * Processes messages for each phase of the Paxos protocol.
     *
     * @param socket The socket connected to the proposer.
     */
    private void handleRequest(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

//             Simulate councillor-specific behavior (e.g., delays, offline state)
            simulateBehavior();

            if (!isConnected)
            {
//                If the councillor is offline, drop the connection
                System.out.println(name + " is offline.");
                return;
            }
//          Read the incoming message
            String message = in.readLine();
            synchronized (this)
            { // Ensure thread-safe access to state variables
                if (message.startsWith("PREPARE"))
                {
                    // Handle Prepare phase: Respond with a promise
                    String proposal = message.split(" ")[1];
                    if (acceptedProposal == null)
                    {
                        currentProposal = proposal;
                        out.println("PROMISE " + proposal);
                    }
                    else
                    {
                        out.println("PROMISE " + acceptedProposal);
                    }
                }
                else if (message.startsWith("ACCEPT"))
                {
                    // Handle Accept phase: Accept the proposal if it matches the promise
                    String proposal = message.split(" ")[1];
                    if (proposal.equals(currentProposal))
                    {
                        acceptedProposal = proposal;
                        out.println("ACCEPTED " + proposal);
                    }
                    else
                    {
                        out.println("REJECTED " + proposal);
                    }
                } else if (message.startsWith("DECIDE"))
                {
                    // Handle Decide phase: Commit to the proposal
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
     * Simulates councillor-specific behavior such as delays or being offline.
     * Behavior varies based on the councillor's name.
     */
    private void simulateBehavior() {
        switch (name) {
            case "M1": // Always responsive, no delay
                break;
            case "M2": // Occasionally offline with delays
                isConnected = random.nextBoolean();
                sleep(isConnected ? 200 : 2000);
                break;
            case "M3": // Frequently online, occasionally offline
                isConnected = random.nextInt(10) > 2;
                break;
            default: // Randomized delays for other councillors
                sleep(random.nextInt(500) + 100);
                break;
        }
    }

    /**
     * Utility method to pause execution for a specified duration.
     *
     * @param milliseconds Time in milliseconds to sleep.
     */
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
 * Represents a proposer in the Paxos protocol.
 * A proposer initiates the voting process and coordinates with councillors
 * to reach consensus on a proposed value.
 */
class Proposer {
    private final String name;               // Unique identifier for the proposer
    private final List<Councillor> councillors; // List of councillors in the system
    private final int majority = Paxos.MAJORITY; // Majority threshold

    public Proposer(String name, List<Councillor> councillors) {
        this.name = name;
        this.councillors = councillors;
    }

    /**
     * Sends a message to a councillor and returns the response.
     *
     * @param councillor The councillor to communicate with.
     * @param message    The message to send.
     * @return The response from the councillor.
     */
    private String sendMessage(Councillor councillor, String message) {
        try (Socket socket = new Socket("localhost", councillor.port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out.println(message);
            return in.readLine(); // Read the response
        } catch (IOException e) {
            System.out.println(councillor.name + " is offline.");
            return null;
        }
    }

    /**
     * Initiates the Paxos voting process for a given proposal.
     * Executes the three phases: Prepare, Accept, and Decide.
     *
     * @param proposal The value to be proposed for consensus.
     */
    public void initiateVoting(String proposal) {
        System.out.println(name + " initiating voting with proposal: " + proposal);

        // Phase 1: Prepare
        List<Councillor> promises = new ArrayList<>();
        for (Councillor councillor : councillors) {
            String response = sendMessage(councillor, "PREPARE " + proposal);
            if (response != null && response.startsWith("PROMISE")) {
                promises.add(councillor);
            }
        }

        if (promises.size() >= majority) {
            // Phase 2: Accept
            List<Councillor> acceptances = new ArrayList<>();
            for (Councillor councillor : promises) {
                String response = sendMessage(councillor, "ACCEPT " + proposal);
                if (response != null && response.startsWith("ACCEPTED")) {
                    acceptances.add(councillor);
                }
            }

            if (acceptances.size() >= majority) {
                // Phase 3: Decide
                for (Councillor councillor : acceptances) {
                    sendMessage(councillor, "DECIDE " + proposal);
                }
                System.out.println(name + " successfully decided on proposal: " + proposal);
            } else {
                System.out.println(name + " failed to get majority acceptance for: " + proposal);
            }
        } else {
            System.out.println(name + " failed to get majority promises for: " + proposal);
        }
    }
}
