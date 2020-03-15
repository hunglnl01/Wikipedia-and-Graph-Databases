package cpen221.mp3;

import cpen221.mp3.server.WikiMediatorServer;

public class TestingServer {
    public static void main(String[] args) {
        new WikiMediatorServer(3333, 3);
    }
}
