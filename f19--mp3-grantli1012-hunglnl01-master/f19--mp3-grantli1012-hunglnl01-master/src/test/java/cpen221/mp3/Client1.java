package cpen221.mp3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client1 {
    List<String> replyList = new ArrayList<>();
    private Socket s;
    private DataInputStream din;
    private DataOutputStream dout;

    public static void main(String[] args){
        new Client1();
    }
    Client1() {
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
        while(count<8){
            String input;
            if(count == 0) {
                input =  "{\"id\": \"1\", \"type\": \"getConnectedPages\", \"pageTitle\": \"Talk:Huntercombe\", \"hops\":\"1\", \"limit\": \"12\", \"timeout\": \"5\"}";
            }
            else if(count == 1 ){
                input = "{\"id\": \"2\", \"type\": \"simpleSearch\", \"query\": \"Barack Obama\", \"limit\": \"12\"}";
            }
            else if(count == 2 ){
                input = "{\"id\": \"3\", \"type\": \"getPage\", \"pageTitle\": \"Talk:Huntercombe\", \"hops\":\"3\", \"limit\": \"12\", \"timeout\": \"10\"}";
            }
            else if(count == 3 ){
                input = "{\"id\": \"4\", \"type\": \"zeitgeist\", \"query\": \"Barack Obama\", \"limit\": \"12\"}";
            }
            else if(count == 4 ){
                input = "{\"id\": \"5\", \"type\": \"trending\", \"limit\": \"12\", \"timeout\": \"6\"}";
            }
            else if(count == 5 ){
                input = "{\"id\": \"6\", \"type\": \"getPath\", \"startPage\": \"Lunari\", \"stopPage\": \"Wikipedia:Stub\", \"timeout\": \"6\"}";
            }
            else if(count == 6 ){
                input = "{\"id\": \"7\", \"type\": \"executeQuery\", \"query\": \"anything\", \"timeout\": \"6\"}";
            }
            else{
                input = "{\"id\": \"8\", \"type\": \"peakLoad30s\"}";
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
                replyList.add(reply);
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
