package cpen221.mp3.wikimediator;

import java.util.ArrayList;
import java.util.List;

public class getReversePath extends Thread {
    private WikiMediator goodWiki = new WikiMediator(1);
    List<String> theReversePath = new ArrayList<>();
    private String startPage;
    private String stopPage;

    //Representation Invariant:
    //  startPage and stopPage != null
    //  startPage and stopPage are not empty strings

    //Abstraction Function:
    //  AF(startPage, stopPage) = a thread(task) of finding the path from a Wikipedia link startPage to another link stopPage but in a
    //                            reversed order, with startPage being the wikipedia page that the path should begin at, stopPage being the
    //                            destination page, and theReversePath being an array that stores all links from the startPage to the destination
    //                            link stopPage, and goodWiki being a Wikimediator provides methods to help find the path.


    getReversePath(String startPage, String stopPage) {
        this.startPage = startPage;
        this.stopPage = stopPage;
    }

    public void run() {
        theReversePath = ReversePath(startPage,stopPage);
    }

    /**
     * Find and return a list of pages that goes from the startPage to stopPage
     * in reverse order
     * @param startPage wikipedia page where the path begins
     * @param stopPage wikipedia page where the path ends up at
     * @return a reverse list of links that path from startPage to stopPage
     */
    private List<String> ReversePath(String startPage, String stopPage){
        List<String> returnList = new ArrayList<>();
        //base case
        if(startPage.equals(stopPage)){
            returnList.add(startPage);
            return returnList;
        }

        //how many hops needed
        int counter = 1;
        List<String> HowManyHops = goodWiki.getConnectedPages(startPage, counter);
        while(!HowManyHops.contains(stopPage)){
            counter ++;
            HowManyHops = goodWiki.getConnectedPages(startPage,counter);
        }

        //now find the path with recursion
        for(String neighborLink: goodWiki.getConnectedPages(startPage,1)){
            List<String> FindWithInSpecifiedCounter = goodWiki.getConnectedPages(neighborLink, counter-1);
            if(FindWithInSpecifiedCounter.contains(stopPage)){
                returnList.addAll(ReversePath(neighborLink, stopPage));
                returnList.add(startPage);
                break;
            }
        }
        return returnList;
    }
}
