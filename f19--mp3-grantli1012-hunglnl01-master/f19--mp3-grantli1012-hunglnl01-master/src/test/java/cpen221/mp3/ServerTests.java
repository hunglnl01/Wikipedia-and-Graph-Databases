package cpen221.mp3;
import cpen221.mp3.server.WikiMediatorServer;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerTests {
    @Test
    public void ServerAndClientRunningTest(){
        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        new WikiMediatorServer(3333, 3);
                    } catch (Exception ignored) {
                    }
                }
            }
        });

        Thread client1Thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        new Client1();
                    } catch (Exception ignored) {
                    }
                }
            }
        });

        Thread client2Thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        new Client2();
                    } catch (Exception ignored) {
                    }
                }
            }
        });

        ExecutorService newpool = Executors.newFixedThreadPool(3);
        newpool.submit(serverThread);
        newpool.submit(client1Thread);
        newpool.submit(client2Thread);
    }
}
