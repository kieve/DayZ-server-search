import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ThreadedQuery {
    private static final int THREADS = 64;
    
    private final String[]        g_searchTerms;
    private final Queue<Server>   g_servers;
    private final List<String>    g_results;
    private static CountDownLatch g_doneSignal;
    private ExecutorService       g_execSvc = Executors.newFixedThreadPool(THREADS);
    
    public ThreadedQuery(Collection<Server> servers, String[] players) {
        g_searchTerms = players;
        g_servers = new LinkedList<Server>(servers);
        g_results = Collections.synchronizedList(new ArrayList<String>());
        g_doneSignal = new CountDownLatch(servers.size());
    }
    
    public List<String> start() {
        int ammount = g_servers.size();
        System.out.println(ammount);
        List<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
        for (int i = 0; i < ammount; i++) {
            tasks.add(Executors.callable(new Task(g_servers.poll())));
        }
        try {
            g_execSvc.invokeAll(tasks);
            g_execSvc.shutdown();
            g_doneSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        return g_results;
    }
    
    private class Task implements Runnable {
        private final Server g_server;
        
        public Task(Server server) {
            g_server = server;
        }

        @Override
        public void run() {
            boolean endEarly = false;
            
            if (g_server == null) { endEarly = true; }
            String players = null;
            if (!endEarly) {
                try {
                    players = Query.getServerInfo(g_server);
                } catch (Exception e) {
                    System.out.println("OOPS " + e);
                }
            }
            
            if (players == null) { endEarly = true; }
            if (!endEarly) {
                for (String s: g_searchTerms) {
                    if (players.contains(s)) {
                        g_results.add(s + " could be in:\n" + g_server.getHostname() + "\n\n"
                                + players + "\n");
                    }
                }
            }
            g_doneSignal.countDown();
        }
    }
}
