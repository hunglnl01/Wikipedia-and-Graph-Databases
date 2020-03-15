package cpen221.mp3;

import com.google.gson.Gson;
import cpen221.mp3.wikimediator.WikiRequest;
import cpen221.mp3.wikimediator.WikiResponseInt;
import cpen221.mp3.wikimediator.WikiResponseList;
import cpen221.mp3.wikimediator.WikiResponseString;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class wikiSupporterTests {
    @Test
    public void wikiResponseIntTest(){
        WikiResponseInt responseInt = new WikiResponseInt("1");
        responseInt.setResponse(1000);
        responseInt.setStatus("success");

        Gson json = new Gson();
        String returnResponse = json.toJson(responseInt);
        Assert.assertEquals("{\"id\":\"1\",\"status\":\"success\",\"response\":1000}",returnResponse);
    }

    @Test
    public void wikiResponseStringTest(){
        WikiResponseString responseString = new WikiResponseString("1");
        responseString.setResponse("This is the response");
        responseString.setStatus("success");

        Gson json = new Gson();
        String returnResponse = json.toJson(responseString);
        Assert.assertEquals("{\"id\":\"1\",\"status\":\"success\",\"response\":\"This is the response\"}",returnResponse);
    }

    @Test
    public void wikiResponseListTest(){
        WikiResponseList responseList = new WikiResponseList("1");
        List<String> returnList = new ArrayList<>();
        returnList.add("this is return element 1");
        responseList.setResponse(returnList);
        responseList.setStatus("success");

        Gson json = new Gson();
        String returnResponse = json.toJson(responseList);
        Assert.assertEquals("{\"id\":\"1\",\"status\":\"success\",\"response\":[\"this is return element 1\"]}",returnResponse);
    }

    @Test
    public void getTimeTest(){
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
        WikiRequest wikiRequest = new WikiRequest(type, id, query, pageTitle, limit, hops, timeout, time, startPage,stopPage);

        Assert.assertEquals(-1,wikiRequest.getTime());
    }
}
