package cpen221.mp3.server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cpen221.mp3.wikimediator.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import org.json.simple.JSONObject;

public class ServerConnection extends Thread {

    private Socket socket;
    private DataInputStream din;
    private DataOutputStream dout;
    private WikiMediatorServer server;

    //Representation Invariant:
    //  socket must be connected to the server socket

    //Abstraction Function:
    //  AF(socket, server) = a thread(task) of handling one incoming client connection, executing requests coming from
    //                       one client connection to a specific socket, with the socket being connected to the
    //                       server socket of a chosen server. din is the data input stream read at the given socket. dout
    //                       is the data output stream to write response back to the client.

    /**
     * Start a ServerConnection with a given server and a given socket
     * @param socket the socket connected to the server
     * @param server is the server in which the connection connected to
     */
    public ServerConnection(Socket socket, WikiMediatorServer server){
        super("SCThread");
        this.socket = socket;
        this.server = server;
    }

    /**
     * handling one request that comes from 1 client at a time
     */
    public void run() {
        WikiMediator mediator = new WikiMediator(1);
        while(true) {
            boolean existLoop = false;

            try {
                din = new DataInputStream(socket.getInputStream());
                dout = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            long start = System.currentTimeMillis();
            while (true) {
                try {
                    if (!(din.available() == 0)) break;

                    long current = System.currentTimeMillis();
                    //if cannot read anything from the connect for the next 4 secs, terminate!

                    if (current-start > 4000){
                        existLoop = true;
                        break;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if(existLoop){
                break;
            }
            //convert requests from json-formatted string to a List<WikiRequest>
            String dataIn = "";
            //start time initiated here before anything retrieving data started
            long startTime = System.currentTimeMillis();

            try {
                dataIn = din.readUTF();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            JsonParser parser = new JsonParser();
            List<WikiRequest> requestList = new ArrayList<>();
            JsonElement json = parser.parse(dataIn);


            if (json.isJsonObject()) {
                JSONObject toLocal = new JSONObject();
                JsonObject req = json.getAsJsonObject();
                String id = "";
                String type = "";
                String query = "";
                String pageTitle = "";
                String startPage = "";
                String stopPage = "";
                int limit = Integer.MAX_VALUE;
                int hops = Integer.MAX_VALUE;
                long timeout = -1;
                long time = -1;
                try {
                    id = req.get("id").getAsString();
                    toLocal.put("id", id);
                } catch (Exception e) {
                }
                try {
                    type = req.get("type").getAsString();
                    toLocal.put("type",type);
                } catch (Exception e) {
                }
                try {
                    query = req.get("query").getAsString();
                    toLocal.put("query",query);
                } catch (Exception e) {
                }
                try {
                    pageTitle = req.get("pageTitle").getAsString();
                    toLocal.put("pageTitle",pageTitle);
                } catch (Exception e) {
                }
                try {
                    startPage = req.get("startPage").getAsString();
                    toLocal.put("startPage", startPage);
                } catch (Exception e) {
                }
                try {
                    stopPage = req.get("stopPage").getAsString();
                    toLocal.put("stopPage", stopPage);
                } catch (Exception e) {
                }
                try {
                    limit = Integer.parseInt(req.get("limit").getAsString());
                    toLocal.put("limit",limit);
                } catch (Exception e) {
                }
                try {
                    hops = Integer.parseInt(req.get("hops").getAsString());
                    toLocal.put("hops",hops);
                } catch (Exception e) {
                }
                try {
                    timeout = Integer.parseInt(req.get("timeout").getAsString());
                    toLocal.put("timeout",timeout);
                } catch (Exception e) {
                }
                try {
                    time = System.currentTimeMillis();
                    toLocal.put("time", System.currentTimeMillis());
                } catch (Exception e) {
                }

                requestList.add(new WikiRequest(type, id, query, pageTitle, limit, hops, timeout, time, startPage,stopPage));
                FileWriter file = null;
                try {
                    file = new FileWriter("local/statistics.json");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    server.toLocalArray.add(toLocal);
                    file.append(server.toLocalArray.toJSONString());
                    file.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }



            //execute each request one by one
            for (WikiRequest request : requestList) {
                //if there is a time out specified
                if (request.getTimeout() > 0) {
                    Runnable task = new Runnable() {
                        @Override
                        public void run() {
                            List<String> responseList;
                            String responseString;
                            int responseInt;
                            if (request.getType().equals("getPage")) {
                                responseString = executingRequestOfGetPage(request, mediator);
                                writeSuccessResponseForString(request, responseString);
                            } else if (request.getType().equals("peakLoad30s")) {
                                responseInt = executingRequestOfPeekLoad30s(mediator);
                                writeSuccessResponseForInt(request, responseInt);
                            } else {
                                responseList = executingRequestThatReturnList(request, mediator);
                                writeSuccessResponseForList(request, responseList);
                            }
                        }
                    };

                    ExecutorService newPool = Executors.newSingleThreadExecutor();
                    try {
                        newPool.submit(task).get(request.getTimeout(), TimeUnit.SECONDS);
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(request.getTimeout());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    newPool.shutdown();

                    long currentTime = System.currentTimeMillis();
                    if (currentTime > request.getTimeout() * 1000 + startTime) {
                        writeFailedResponse(request);
                    }

                    //if there is no timeout specified
                } else {
                    Runnable task = new Runnable() {
                        @Override
                        public void run() {
                            List<String> responseList;
                            String responseString;
                            int responseInt;
                            if (request.getType().equals("getPage")) {
                                responseString = executingRequestOfGetPage(request, mediator);
                                writeSuccessResponseForString(request, responseString);
                            } else if (request.getType().equals("peakLoad30s")) {
                                responseInt = executingRequestOfPeekLoad30s(mediator);
                                writeSuccessResponseForInt(request, responseInt);
                            } else {
                                responseList = executingRequestThatReturnList(request, mediator);
                                writeSuccessResponseForList(request, responseList);
                            }
                        }
                    };
                    ExecutorService newPool = Executors.newSingleThreadExecutor();
                    newPool.submit(task);
                }
            }
        }

        try {
            din.close();
            dout.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Execute one request and get the return the resulting list. This method is used when the request's type
     * is simpleSearch, getConnectedPages, zeigeist, and trending
     *
     * @param request request being executed
     * @param mediator a mediator in which request is executed
     * @return a list of String of the result
     */
    private List<String> executingRequestThatReturnList(WikiRequest request, WikiMediator mediator) {
        List<String> responseList = new ArrayList<>();
        if (request.getType().equals("simpleSearch")) {
            responseList = mediator.simpleSearch(request.getQuery(), request.getLimit());
        } else if (request.getType().equals("getConnectedPages")) {
            responseList = mediator.getConnectedPages(request.getPageTitle(), request.getHops());
        } else if (request.getType().equals("zeitgeist")) {
            responseList = mediator.zeitgeist(request.getLimit());
        } else if (request.getType().equals("trending")) {
            responseList = mediator.trending(request.getLimit());
        } else if (request.getType().equals("getPath")) {
            responseList = mediator.getPath(request.getStartPage(),request.getStopPage());
        } else if (request.getType().equals("executeQuery")) {
            responseList = mediator.executeQuery(request.getQuery());
        }
        return responseList;
    }

    /**
     * Execute requests that call for the use of getPage
     *
     * @param request request being executed
     * @param mediator a mediator in which request is executed
     * @return a string of result
     */
    private String executingRequestOfGetPage(WikiRequest request, WikiMediator mediator) {
        return mediator.getPage(request.getPageTitle());
    }

    /**
     * Execute requests that call for the use of PeekLoad30s
     *
     * @param mediator a mediator in which request is executed
     * @return an integer of result
     */
    private int executingRequestOfPeekLoad30s(WikiMediator mediator) {
        return mediator.peakLoad30s();
    }

    /**
     * Response to the the Client if the request is successfully executed and the request yields a list of string
     *
     * @param request request being executed
     * @param responseList response will contain this list
     */
    private void writeSuccessResponseForList(WikiRequest request, List responseList){
        WikiResponseList wikiResponse = new WikiResponseList(request.getId());
        wikiResponse.setStatus("success");
        wikiResponse.setResponse(responseList);
        Gson json = new Gson();
        String returnResponse = json.toJson(wikiResponse);
        try {
            dout.writeUTF(returnResponse);
            dout.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Response to the the Client if the request is successfully executed and the request yields an integer
     *
     * @param request request being executed
     * @param responseInt response will contain this integer
     */
    private void writeSuccessResponseForInt(WikiRequest request, int responseInt){
        WikiResponseInt wikiResponse = new WikiResponseInt(request.getId());
        wikiResponse.setStatus("success");
        wikiResponse.setResponse(responseInt);
        Gson json = new Gson();
        String returnResponse = json.toJson(wikiResponse);
        try {
            dout.writeUTF(returnResponse);
            dout.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Response to the the Client if the request is successfully executed and the request yields a String
     *
     * @param request request being executed
     * @param responseString response will contain this string
     */
    private void writeSuccessResponseForString(WikiRequest request, String responseString){
        WikiResponseString wikiResponse = new WikiResponseString(request.getId());
        wikiResponse.setStatus("success");
        wikiResponse.setResponse(responseString);
        Gson json = new Gson();
        String returnResponse = json.toJson(wikiResponse);
        try {
            dout.writeUTF(returnResponse);
            dout.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Response to the Client if the request is failed
     *
     * @param request request being executed
     */
    private void writeFailedResponse(WikiRequest request){
        WikiResponseString wikiResponse = new WikiResponseString(request.getId());
        wikiResponse.setStatus("failed");

        wikiResponse.setResponse("Operation timed out");
        Gson json = new Gson();
        String returnResponse = json.toJson(wikiResponse);
        try {
            dout.writeUTF(returnResponse);
            dout.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
