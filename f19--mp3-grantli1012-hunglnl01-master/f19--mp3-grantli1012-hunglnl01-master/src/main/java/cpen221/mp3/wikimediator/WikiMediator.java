package cpen221.mp3.wikimediator;

import cpen221.mp3.cache.Cache;
import cpen221.mp3.cache.Cacheable;
import fastily.jwiki.core.Wiki;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class WikiMediator {
    //search history
    private ArrayList<String> searchRecord;
    //time record for search
    private HashMap<Integer,searchTime> time;
    //request id and time record
    private HashMap<Integer,Long> request;
    private int count;
    //creating cache that stores up to 256 pages with life length of 12 hours
    private Cache<wikiCacheable> cache;
    //getting wiki
    private Wiki wiki;
    private int mediatorId;

    //Representation Invariant:
    //  1. Valued stored in time and request map cannot be negative
    //  2. Each key (representing request id determined by count) in request map must be positive
    //

    //Abstraction Function:
    //  AF(mediatorID) = A mediator with id of mediatorID that contains a list of search records, a map recording each search
    //                   and the search time, a map recording search id and time, a counter integer used to determine id for
    //                   each search, and a cache used to store/access wiki pages, and a wiki object used to conduct search.

    //ThreadSafe argument:
    //  This class is thread-safe because it is using thread-safe ExecutorService
    //  - ExecutorService is thread-safe type
    //  - Only getPath uses a single thread, so it it thread safe since there are no 2 threads altering data from the same field
    //    so there is no racing

    public WikiMediator(int mediatorId){
        this.wiki = new Wiki("en.wikipedia.org");
        wiki.enableLogging(false);
        this.cache = new Cache<>(256,43200);
        this.searchRecord = new ArrayList<>();
        this.time = new HashMap<>();
        this.request = new HashMap<>();
        this.count = 0;
        this.mediatorId = mediatorId;
    }

    public int getID(){
        return this.mediatorId;
    }
    public ArrayList<String> getSearchRecord(){
        return searchRecord;
    }

    private class searchTime {
        private long time;
        private String titleSearched;

        //Representation Invariant:
        //  1. time must be a positive integer
        //  2. titleSearched must be the title used to make the search at the time associated
        //Abstraction Function:
        //  AF(titleSearched) = a object representing the title used to make a wiki search and the time when the search is made

        public searchTime(String titleSearched){
            this.titleSearched = titleSearched;
            this.time = System.currentTimeMillis();
        }

        public long getTime(){
            return this.time;
        }
        public String getTitleSearched(){
            return this.titleSearched;
        }
    }


    private class wikiCacheable implements Cacheable {
        public String text;
        private String id;

        //Representation Invariant:
        //  The id must be equal to the title of the page text passed in
        //Abstraction Function:
        //  AF(text,id) = a object that can be put into cache, containing a title as id and the page text that's related to the id

        public wikiCacheable(String text, String id) {
            this.text = text;
            this.id = id;
        }

        @Override
        public String id() {
            return this.id;
        }
    }

    /**
     * Given a query, return up to limit page titles that match the query string
     * (per Wikipedia's search service).
     *
     * @param query The string to search for
     * @param limit Biggest number of page titles to return
     * @return A list of page titles that matches the query
     */
    public List<String> simpleSearch(String query, int limit){
        request.put(count,System.currentTimeMillis());

        ArrayList<String> target = new ArrayList<>();
        target.addAll(wiki.search(query,limit));

        ///
        searchRecord.add(query);
        time.put(count,new searchTime(query));
        ///
        count++;
        return target;
    }

    /**
     * Given a pageTitle, return the text associated with the Wikipedia page
     * that matches pageTitle.
     *
     * @param pageTitle The title of the page to look for
     * @return Text associated to the wikipedia page that matched the pageTitle
     */
    public String getPage(String pageTitle){
        request.put(count,System.currentTimeMillis());
        String target;
        try{
            target = cache.get(pageTitle).toString();
            cache.touch(pageTitle);
        }catch(IllegalArgumentException e){
            target = wiki.getPageText(pageTitle);
            wikiCacheable page = new wikiCacheable(target,pageTitle);
            cache.put(page);
        }
        ///
        searchRecord.add(pageTitle);
        time.put(count, new searchTime(pageTitle));
        count++;
        ///
        return target;
    }

    /**
     * Return a list of page titles that can be reached by following up to hops links
     * starting with the page specified by pageTitle.
     *
     * @param pageTitle The title of the page to search hops links from
     * @param hops The max number of links to follow from starting page
     * @return A list of page title that can be reached within hops starting from pageTitle
     */
    public List<String> getConnectedPages(String pageTitle, int hops){
        request.put(count++,System.currentTimeMillis());

        List<String> target = new ArrayList<>();
        int count = 0;
        if(count==hops){
            target.add(pageTitle);
        }
        else{
            List<String> neighbors = wiki.getLinksOnPage(pageTitle);
            count++;
            for(String neighbor:neighbors){
                target.addAll(this.getConnectedPages(neighbor,hops-count));
            }
        }
        return target.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Return the most common Strings used in simpleSearch and getPage requests, with items
     * being sorted in non-increasing count order. When many requests have been made,
     * return only limit items.
     *
     * @param limit The max number of commonly used strings can return
     * @return A list of most commonly used strings in simpleSearch and getPage
     */

    public List<String> zeitgeist(int limit){
        request.put(count++,System.currentTimeMillis());

        Map<String, Long> map = searchRecord.stream().collect(Collectors.groupingBy(w -> w,Collectors.counting()));
        List<Map.Entry<String, Long>> limited = map.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(limit).collect(Collectors.toList());
        List<String> target = new ArrayList<>();
        for(int i=0;i<limited.size();i++){
            target.add(limited.get(i).getKey());
        }
        return target;
    }



    /**
     * Similar to zeitgeist(), but returns the most frequent requests made in
     * the last 30 seconds.
     *
     * @param limit The max number of strings can return
     * @return A list of most frequently searched strings in simpleSearch and getPage
     *         in the last 30 seconds.
     */
    public List<String> trending(int limit){
        request.put(count++,System.currentTimeMillis());

        long current = System.currentTimeMillis();
        List<String> last30sRecord = new ArrayList<>();

        for(Integer i:time.keySet()){
            if(current-time.get(i).getTime()<=30000){
                last30sRecord.add(time.get(i).getTitleSearched());
            }
        }

        Map<String, Long> map = last30sRecord.stream().collect(Collectors.groupingBy(w -> w,Collectors.counting()));
        List<Map.Entry<String, Long>> limited = map.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(limit).collect(Collectors.toList());
        List<String> target = new ArrayList<>();
        for(int i=0;i<limited.size();i++){
            target.add(limited.get(i).getKey());
        }

        return target;
    }

    /**
     * Return the maximum number of requests seen in any 30-second window.
     * The request count is to include all requests made through the listed 6
     * methods in this class.
     *
     * @return Maximum number of requests seen in any 30-second window
     */
    public int peakLoad30s(){
        request.put(count++,System.currentTimeMillis());

        //gist: sliding window technique
        int i=0;
        int j=0;
        int peak = 0;
        Set<Integer> set = new HashSet<>();

        while (j<request.size()){
            if(request.get(j)-request.get(i)<30000){
                set.add(j++);
                peak = Math.max(peak,set.size());
            }
            else{
                set.remove(i++);
            }
        }

        return peak;
    }

    /**
     * Find and return a path from the startPage to stopPage by
     * following the links between two pages. If the process of finding the path
     * exceeds the time limit which is 4 minutes, return an empty string. If the
     * startPage and stopPage are equal, return a List of only startPage
     *
     * @param startPage is the title of the page which the path starts off with
     * @param stopPage is the title of the destination page which you end up at
     * @return a list of of strings that contains all title pages of the path from
     * startPage to stopPage
     */
    public List<String> getPath(String startPage, String stopPage){
        getReversePath thread = new getReversePath(startPage, stopPage);
        ExecutorService newPool = Executors.newSingleThreadExecutor();
        try {
            newPool.submit(thread).get(240, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
        List<String> returnList = thread.theReversePath;
        newPool.shutdown();

        Collections.reverse(returnList);
        return returnList;
    }

    /**
     * return a list of strings that contains all results which satisfies with the
     * criteria specified by the user through the use of query. Return an empty list
     * for queries, that satisfy the grammar but does not yield meaningful results.
     * If queries are not valid, throw InvalidQueryException
     *
     * @param query is string that represents client request
     * @return a list of strings that represents the result of the query
     */
    public List<String> executeQuery(String query){
        List<String> returnList = new ArrayList<>();
        returnList.add("THIS IS TASK 3B");
        return returnList;
    }

}
