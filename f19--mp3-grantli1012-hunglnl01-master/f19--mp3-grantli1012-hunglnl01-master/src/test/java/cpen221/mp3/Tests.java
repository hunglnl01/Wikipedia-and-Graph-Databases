package cpen221.mp3;

import cpen221.mp3.wikimediator.WikiMediator;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import cpen221.mp3.cache.Cache;
import cpen221.mp3.cache.Cacheable;

import static org.junit.Assert.assertEquals;

public class Tests {
    @Test
    public void testingCacheDefaultContructor(){
        //default timeout is 1 hour
        //default size is 32 objects
        Cache cache = new Cache();

        class objectCacheable implements Cacheable {
            public String content;
            public objectCacheable(String content) {
                this.content = content ;
            }
            @Override
            public String id() {
                return "";
            }
        }

        cache.put(new objectCacheable("element"));
        assertEquals(1, cache.size());
        try{
            Thread.sleep(1500);
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
        //after 1.5s, default should still contains the element
        assertEquals(1, cache.size());
    }

    @Test
    public void testingCacheContructor() {
        //3 seconds
        //maxItems: 10
        Cache cache1 = new Cache(10, 3);

        class stringCacheable implements Cacheable {
            public String content;

            public stringCacheable(String content) {
                this.content = content ;
            }

            @Override
            public String id() {
                return "";
            }
        }

        cache1.put(new stringCacheable("string1"));
        assertEquals(1, cache1.size());
        try{
            Thread.sleep(3050);
        }catch (InterruptedException e) {
            e.printStackTrace();
        }

        //after the timeout, cache should no longer contains the expired item
        assertEquals(0, cache1.size());
    }

    @Test
    public void testingCachput() {
        //3 seconds
        //maxItems: 2
        Cache cache1 = new Cache(2,3);

        class string1 implements Cacheable {
            public String content;
            public string1(String content) {
                this.content = content ;
            }
            @Override
            public String id() {
                return "1";
            }
        }
        class string2 implements Cacheable {
            public String content;
            public string2(String content) {
                this.content = content ;
            }
            @Override
            public String id() {
                return "2";
            }
        }
        class string3 implements Cacheable {
            public String content;
            public string3(String content) {
                this.content = content ;
            }
            @Override
            public String id() {
                return "3";
            }
        }

        string1 object1 = new string1("string1");
        string2 object2 = new string2("string2");
        string3 object3 = new string3("string3");

        cache1.put(object1);
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        cache1.put(object2);
        cache1.put(object3);

        //cache should only contains 2 items which is object2 and object3
        assertEquals(object2, cache1.get("2"));
        assertEquals(object3, cache1.get("3"));
        assertEquals(2,cache1.size());
    }

    @Test
    public void testingCacheGet(){
        Cache cache1 = new Cache(10, 3);

        class stringCacheable implements Cacheable {
            public String content;
            public stringCacheable(String content) {
                this.content = content ;
            }
            @Override
            public String id() {
                return "123";
            }
        }

        stringCacheable newItem = new stringCacheable("string1");

        cache1.put(newItem);
        assertEquals(newItem, cache1.get("123"));
    }

    @Test
    public void testingCacheTouch() {
        //3 seconds timeout
        Cache cache1 = new Cache(10, 3);

        class stringCacheable implements Cacheable {
            public String content;
            public stringCacheable(String content) {
                this.content = content ;
            }
            @Override
            public String id() {
                return "id";
            }
        }

        cache1.put(new stringCacheable("string1"));
        assertEquals(1, cache1.size());
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        cache1.touch("id");
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //after the timeout, cache should still contains 1 item because it was touched
        assertEquals(1, cache1.size());
    }

    @Test
    public void testingCacheUpdate() {
        //3 seconds timeout
        Cache cache1 = new Cache(10, 3);

        class stringCacheable implements Cacheable {
            public String content;
            public stringCacheable(String content) {
                this.content = content ;
            }
            @Override
            public String id() {
                return "id";
            }
        }
        stringCacheable object = new stringCacheable("string1");
        cache1.put(object);
        stringCacheable updateObject = new stringCacheable("updateString");
        cache1.update(updateObject);

        assertEquals(updateObject, cache1.get("id"));
        assertEquals(1,cache1.size());
    }





    private WikiMediator mediator1 = new WikiMediator(1);

    @Test
    public void mediatorConstructorTest(){

        Assert.assertEquals(1,mediator1.getID());

        System.out.println(mediator1.getPage("Barack Obama"));
    }

    @Test
    public void simpleSearchTest1(){
        List<String> actual = mediator1.simpleSearch("Barack Obama",5);
        Assert.assertTrue(mediator1.getSearchRecord().contains("Barack Obama"));
        Assert.assertEquals(5,actual.size());

    }

    @Test
    public void simpleSearchTest2(){
        System.out.println(mediator1.simpleSearch("Unicorn",5));
        Assert.assertTrue(mediator1.getSearchRecord().contains("Unicorn"));
    }

    @Test
    public void getPageTest1(){
      System.out.println(mediator1.getPage("Unicorn"));
      System.out.println(mediator1.getPage("Unicorn"));
    }


    @Test
    public void getConnectedPageTest1(){
        List<String> list = mediator1.getConnectedPages("Lusong District",0);
        Assert.assertEquals(1,list.size());
    }

    @Test
    public void getConnectedPageTest2(){
        List<String> list = mediator1.getConnectedPages("You County",1);
        System.out.println(list.size());
        Assert.assertEquals(203, list.size());

        //I found these two pages, which only have 33 and 19 links, check them out!
        List<String> list1 = mediator1.getConnectedPages("Talk:Pylon Field",1);
        System.out.println(list1.size());
        Assert.assertEquals(33, list1.size());

        List<String> list2 = mediator1.getConnectedPages("Talk:Huntercombe",1);
        List<String> list3 = mediator1.getConnectedPages("Luigi Lunari",1);
        List<String> list4 = mediator1.getConnectedPages("Ricardo Lunari",1);
        System.out.println(list2.size());
        System.out.println(list2);
        System.out.println(list3);
        System.out.println(list4);
        Assert.assertEquals(19, list2.size());

    }

    @Test
    public void zeitgeistTest(){
        mediator1.simpleSearch("A",1);
        mediator1.simpleSearch("A",1);
        mediator1.simpleSearch("A",1);
        mediator1.simpleSearch("A",1);
        mediator1.simpleSearch("B",1);
        mediator1.simpleSearch("B",1);
        mediator1.simpleSearch("B",1);
        mediator1.simpleSearch("C",1);
        mediator1.simpleSearch("C",1);
        mediator1.simpleSearch("D",1);
        List<String> expected = new ArrayList<>();
        expected.add("A");
        expected.add("B");
        expected.add("C");
        List<String> actual = mediator1.zeitgeist(3);
        Assert.assertEquals(expected,actual);
    }

    @Test
    public void trendingTest(){
        mediator1.simpleSearch("A",1);
        mediator1.simpleSearch("A",1);
        mediator1.simpleSearch("A",1);
        mediator1.simpleSearch("A",1);
        mediator1.simpleSearch("B",1);
        mediator1.simpleSearch("B",1);
        mediator1.simpleSearch("B",1);
        mediator1.simpleSearch("C",1);
        mediator1.simpleSearch("C",1);
        mediator1.simpleSearch("D",1);
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mediator1.simpleSearch("A",1);
        mediator1.simpleSearch("B",1);
        mediator1.simpleSearch("B",1);
        mediator1.simpleSearch("C",1);
        mediator1.simpleSearch("C",1);
        mediator1.simpleSearch("C",1);
        mediator1.simpleSearch("C",1);
        mediator1.simpleSearch("D",1);
        mediator1.simpleSearch("D",1);
        mediator1.simpleSearch("D",1);
        List<String> expected = new ArrayList<>();
        expected.add("C");
        expected.add("D");
        List<String> actual = mediator1.trending(2);
        Assert.assertEquals(expected,actual);
    }

    @Test
    public void peakWindowTest1(){
        Assert.assertEquals(1,mediator1.peakLoad30s());
    }

    @Test
    public void peakWindowTest2(){
        mediator1.simpleSearch("C",1);
        mediator1.simpleSearch("C",1);
        mediator1.simpleSearch("D",1);
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mediator1.getConnectedPages("A",0);
        mediator1.getPage("A");
        mediator1.simpleSearch("C",1);
        mediator1.simpleSearch("C",1);
        mediator1.simpleSearch("D",1);
        mediator1.getID();      //this is to test that only the 6 specified method count as a request
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mediator1.simpleSearch("C",1);
        mediator1.simpleSearch("C",1);
        mediator1.simpleSearch("D",1);


        Assert.assertEquals(5,mediator1.peakLoad30s());
    }

    @Test
    public void getPath1(){
        List<String> expectedList = new ArrayList<>();
        expectedList.add("A");
        Assert.assertEquals(expectedList,mediator1.getPath("A","A"));
    }

    @Test
    public void getPath2(){
        List<String> expectedList = new ArrayList<>();
        expectedList.add("Talk:Huntercombe");
        expectedList.add("United Kingdom");
        Assert.assertEquals(expectedList,mediator1.getPath("Talk:Huntercombe","United Kingdom"));
    }

    @Test
    public void getPath3(){
        List<String> expectedList = new ArrayList<>();
        expectedList.add("Lunari");
        expectedList.add("Luigi Lunari");
        Assert.assertEquals(expectedList,mediator1.getPath("Lunari","Luigi Lunari"));
    }

    @Test
    public void getPath4(){
        List<String> expectedList = new ArrayList<>();
        expectedList.add("Lunari");
        expectedList.add("Luigi Lunari");
        expectedList.add("Wikipedia:Stub");
        Assert.assertEquals(expectedList,mediator1.getPath("Lunari","Wikipedia:Stub"));
    }

    @Test
    public void executeQuerTest(){
        List<String> expectedList = new ArrayList<>();
        expectedList.add("THIS IS TASK 3B");
        Assert.assertEquals(mediator1.executeQuery("this is for coverage only, we did not implement part 3B"),expectedList);
    }
}
