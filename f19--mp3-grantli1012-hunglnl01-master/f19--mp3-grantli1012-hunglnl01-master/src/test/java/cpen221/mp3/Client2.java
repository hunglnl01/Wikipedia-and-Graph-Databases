package cpen221.mp3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client2 {
    private Socket s;
    private DataInputStream din;
    private DataOutputStream dout;

    public static void main(String[] args){
        new Client2();
    }
    Client2() {
        try {
            s = new Socket("localhost", 3333);
            din = new DataInputStream(s.getInputStream());
            dout = new DataOutputStream(s.getOutputStream());

            listenForInput();
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    private void listenForInput() {
        int count = 0;
        while(count<4){
            String input;
            if(count == 0) {
                input =  "{\"id\": \"1\", \"type\": \"getConnectedPages\", \"pageTitle\": \"Talk:Huntercombe\", \"hops\":\"1\", \"limit\": \"12\", \"timeout\": \"1\"}";
            }
            else if(count == 1 ){
                input = "{\"id\": \"2\", \"type\": \"getConnectedPages\", \"pageTitle\": \"Barack Obama\", \"hops\":\"4\", \"limit\": \"12\", \"timeout\": \"6\"}";
            }
            else if(count == 2 ){
                input = "{\"id\": \"3\", \"type\": \"getConnectedPages\", \"pageTitle\": \"Barack Obama\", \"hops\":\"5\", \"limit\": \"12\", \"timeout\": \"10\"}";
            }
            else{
                input = "{\"id\": \"4\", \"type\": \"getPage\", \"pageTitle\": \"Talk:Huntercombe\", \"hops\":\"3\", \"limit\": \"12\", \"timeout\": \"10\"}";
            }

            count ++;
            try {
                dout.writeUTF(input);
                while(din.available()==0){
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                String reply = din.readUTF();
                System.out.println(reply);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }

        try {
            din.close();
            dout.close();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
