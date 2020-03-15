package cpen221.mp3.wikimediator;

public class WikiRequest {
    private String type;
    private String query;
    private int limit;
    private int hops;
    private String pageTitle;
    private long timeout;
    private String id;
    private String startPage;
    private String stopPage;
    private long time;

    //RI:
    //  1. type must not be null
    //  2. id must be positive
    //  3. time out value, if set by user, must be positive
    //  4. the rest of the instances, depending on the type of request made and methods definitions in WikiMediator, can
    //     be null since the json file input would not specify
    // AF:
    //  AF(type,query,limit,hops,timeout,id) = a request with query/pageTitle/limit/hops/timeout specified and ready to be processed
    //                                         by WikiMediator based on type, and an id set by a counter

    public WikiRequest(String type, String id, String query, String pageTitle, int limit, int hops, long timeout, long time, String startPage, String stopPage){
        this.type = type;
        this.id = id;
        this.query = query;
        this.pageTitle= pageTitle;
        this.limit = limit;
        this.hops = hops;
        this.timeout = timeout;
        this.startPage = startPage;
        this.stopPage = stopPage;
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public String getQuery() {
        return query;
    }

    public int getLimit() {
        return limit;
    }

    public int getHops() {
        return hops;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public long getTimeout() {
        return timeout;
    }

    public String getId() {
        return id;
    }

    public String getStartPage(){ return startPage; }

    public String getStopPage(){ return stopPage; }

    public long getTime(){ return this.time; }
}
