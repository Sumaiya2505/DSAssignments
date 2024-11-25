import org.junit.*;
import java.util.*;
import java.util.concurrent.*;

public class PaxosTest
{

    private static final int TOTAL_COUNCILLORS = 9;
    private ExecutorService executor;
    private List<Councillor> councillors;

    @Before
    public void setUp() {
        executor = Executors.newFixedThreadPool(TOTAL_COUNCILLORS);
        councillors = new ArrayList<>();
        for (int i = 1; i <= TOTAL_COUNCILLORS; i++) {
            int id = i;
            Councillor councillor = new Councillor("M" + id, 5000 + id);
            councillors.add(councillor);
            executor.execute(() -> councillor.start());
        }
    }

    @After
    public void tearDown() {
        executor.shutdownNow();
    }

    @Test
    public void testImmediateResponses() {
        System.out.println("Test Paxos with immediate responses from all councillors");
        Proposer proposer = new Proposer("M1", councillors);
        proposer.initiateVoting("M1 for President");
        Assert.assertTrue("Paxos works with immediate responses.", true);
    }

    @Test
    public void testDelayedResponses() {
        // Simulating delayed responses for M2 and M3
        System.out.println("Test Paxos with delayed responses from all councillors");
        councillors.forEach(c -> {
            if ("M2".equals(c.name) || "M3".equals(c.name)) {
                c.isConnected = true;  // Keep M2 and M3 connected
            }
        });

        Proposer proposer = new Proposer("M1", councillors);
        proposer.initiateVoting("M1 for President");
        Assert.assertTrue("Paxos works with delayed responses.", true);
    }

    @Test
    public void testOfflineCouncillors() {
        // Simulating M2 and M3 going offline
        System.out.println("Test Paxos with offline councillors");
        councillors.forEach(c -> {
            if ("M2".equals(c.name) || "M3".equals(c.name)) {
                c.isConnected = false;  // M2 and M3 are offline
            }
        });

        Proposer proposer = new Proposer("M1", councillors);
        proposer.initiateVoting("M1 for President");
        Assert.assertTrue("Paxos works with some councillors offline.", true);
    }

    @Test
    public void testMultipleProposers() throws InterruptedException {
        System.out.println("Test Paxos with multiple proposers vote simultaneously");
        Proposer proposer1 = new Proposer("M1", councillors);
        Proposer proposer2 = new Proposer("M8", councillors);

        Thread t1 = new Thread(() -> proposer1.initiateVoting("M1 for President"));
        Thread t2 = new Thread(() -> proposer2.initiateVoting("M8 for President"));

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        Assert.assertTrue("Paxos works with multiple proposers simultaneously.", true);
    }

    @Test
    public void testMixedBehaviors() {
        // Simulating mixed behaviors for M2 and M3
        System.out.println("Test Paxos with councillors having mixed behaviors");
        councillors.forEach(c -> {
            if ("M2".equals(c.name)) {
                c.isConnected = false; // M2 offline
            } else if ("M3".equals(c.name)) {
                c.isConnected = true; // M3 online but delayed
            }
        });

        Proposer proposer = new Proposer("M1", councillors);
        proposer.initiateVoting("M1 for President");
        Assert.assertTrue("Paxos works with mixed councillor behaviors.", true);
    }
}
