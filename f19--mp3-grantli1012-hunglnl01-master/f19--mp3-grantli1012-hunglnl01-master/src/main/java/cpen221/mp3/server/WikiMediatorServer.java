package cpen221.mp3.server;

import org.json.simple.JSONArray;

import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class WikiMediatorServer {
    JSONArray toLocalArray = new JSONArray();
    //Representation Invariant:
    //  ToLocalArray is an JSONArray that must store all requests from client
    //  under a form of JSON strings

    //Abstraction Function:
    //  AF(port, n) = a server program that has a thread-pool of size n to execute n requests concurrently. This thread-
    //                pool will execute a list of ServerConnection threads, where each ServerConnection thread is a single thread of
    //                handling one client connection. This server is in charged

    //ThreadSafe argument:
    //  This class is thread-safe because it is using thread-safe ExecutorService to perform multiple threads at the same time
    //  - ExecutorService is thread-safe type
    //  - All threads work on different fields
    //  - When adding requests to the toLocalArray, different threads can have same requests, but the Array expects to store all of them
    //    so there would be no racing

    /**
     * Start a server at a given port number, with the ability to process
     * up to n requests concurrently.
     *
     * @param port the port number to bind the server to
     * @param n the number of concurrent requests the server can handle
     */
    public WikiMediatorServer(int port, int n) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            ExecutorService pool = Executors.newFixedThreadPool(n);

            while (true) {
                Socket s = serverSocket.accept();
                ServerConnection sc = new ServerConnection(s,this);
                pool.submit(sc);
                FileWriter file = null;

                try {
                    file = new FileWriter("local/statistics.json");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    assert file != null;
                    file.append(toLocalArray.toJSONString());
                    file.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}
