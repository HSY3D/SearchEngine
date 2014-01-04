import java.util.*;
import java.io.*;

// This class implements a google-like search engine
public class searchEngine {

	//GLOBAL VARIABLES
	public HashMap<String, LinkedList<String>> wordIndex; // this will contain a set of pairs (String, LinkedList of Strings).
	public directedGraph internet; // this is our internet graph.

	/*
	 * CONSTRUCTOR: initializes everything to empty data structures. It also sets the location of the internet files.
	 */
	searchEngine() {
		// Below is the directory that contains all the internet files
		htmlParsing.internetFilesLocation = "internetFiles";
		wordIndex = new HashMap<String, LinkedList<String> > ();		
		internet = new directedGraph();				
	}

	/*
	 * Returns a String description of a searchEngine(non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString () {
		return "WORDINDEX::\n" + wordIndex + "\nINTERNET::\n" + internet;
	}

	/*
	 * TRAVERSE INTERNET: Traverses the internet. Updates the wordIndex.
	 */
	void traverseInternet(String url) throws Exception {
		try {
			//VARIABLES
			LinkedList<String> links = new LinkedList<String>();
			LinkedList<String> words = new LinkedList<String>();


			//ADD VERTEX
			if(!internet.vertices.containsKey(url)) {
				internet.addVertex(url);
			}

			//GET WORDS ON URL PAGE
			words = htmlParsing.getContent(url);
			Iterator<String> w = words.iterator();

			/*
			 * UPDATE WORDINDEX: Iterate through the 'words' list. Then if the HashTable 'wordIndex' does not 
			 * contains the word 's' then create a new LinkedList 'urls' and add the current 'url' to the the
			 * list. Then add key:s,urls:url_1,...url_n to the 'wordIndex'. If the HashTable does contain the
			 * word 's', then append the current 'url' to the LinkedList 'urls'.
			 */
			while(w.hasNext()) {
				String s = w.next();
				LinkedList<String> urls = new LinkedList<String>();

				if(!wordIndex.containsKey(s)) {
					urls.addLast(url);
					wordIndex.put(s, urls);
				} else {
					urls = wordIndex.get(s);
					urls.addLast(url);
					wordIndex.put(s, urls);
				}
			}

			//GET LINKS(EDGES)
			links = htmlParsing.getLinks(url);
			Iterator<String> i = links.iterator();

			//SET VISITED
			if(!internet.getVisited(url)) {
				internet.setVisited(url, true);
			}    	

			//ADD EDGES
			while(i.hasNext()) {
				String s = i.next();
				internet.addEdge(url, s);
				if(!internet.getVisited(s)) {
					traverseInternet(s);
				}
			}
		} catch (Exception e) {
			System.out.println("Tried to access the following web site more than once." + e);
			e.printStackTrace();
		}
	}

	/* 
	 * COMPUTE PAGE RANKS: This computes the pageRanks for every vertex in the internet graph.
	 */
	void computePageRanks() {
		//VARIABLES
		double damping = 0.5; //damning factor used to calculate the pageRank.
		double pageRank = 1;

		//GET VERTICES
		LinkedList<String> vertices = internet.getVertices();
		Iterator<String> v = vertices.iterator(); //an iterator for the vertices.

		//SET PAGE RANK TO 1
		for(String vertex: vertices) {
			internet.setPageRank(vertex, 1);
		}

		//ITERATE TILL CONVERGENCE: in this case 100 times.
		for(int i = 0; i < 100; i++) {
			while(v.hasNext()) {
				//SET PAGE RANK FRACTION AND CURRENT VERTEX
				String currentVertex = v.next();
				double pageRankFraction = 0;

				//GET EDGES INTO
				LinkedList<String> edgesInto = internet.getEdgesInto(currentVertex);

				//PAGE RANK CALCULATION
				for(String edgeInto: edgesInto) {
					pageRankFraction += (internet.getPageRank(edgeInto)/internet.getOutDegree(edgeInto));
				}

				//SET PAGE RANK
				pageRank = (1-damping) + damping * pageRankFraction;
				internet.setPageRank(currentVertex, pageRank);
			}
		}
	}


	/*
	 * GET BEST URL: returns the URL of the page with the high page-rank containing the query word. returns the String "No website contains the query"
	 * if no web site contains the query. Start by obtaining the list of URLs containing the query word. Then return the URL with the highest pageRank.
	 */
	String getBestURL(String query) {
		//VARIABLE
		String result = null;
		String webpage = null;
		String nextWebpage = null; 
		double highestPageRank = 0;
		double nextPageRank = 0;

		/*
		 * FIND THE HIGHEST PAGE RANK: if there are entries for the query, return "No website query." if there is an index for the query then iterate through the
		 * list of webpages, and compare all the page ranks till the highest page rank is found.
		 */
		if(!wordIndex.containsKey(query)) {
			return result = "No website contians the query.";
		} else {
			//GET THE QUERY WEBPAGES
			LinkedList<String> webpages = wordIndex.get(query);
			Iterator<String> i = webpages.iterator();

			//RE-DECLARE VARIABLES
			webpage = i.next(); //any first webpage in the list
			highestPageRank = internet.getPageRank(webpage); //rank of the first webpage
			result = webpage; //the current query result

			//SORT THROUGH TO FIND THE HIGHEST RANK
			while(i.hasNext()) {
				//VARIABLES
				nextWebpage = i.next(); 
				nextPageRank = internet.getPageRank(nextWebpage);

				//IF THE RANK OF THE NEXT WEBPAGE IS GREATER THAN THE LAST RETURN THE NEXT WEBPAGE
				if(highestPageRank < nextPageRank) {
					highestPageRank = nextPageRank;
					result = nextWebpage;
				}
			}
		}
		return result;
	}

	/*
	 * MAIN METHOD
	 */
	public static void main(String args[]) throws Exception{		
		searchEngine mySearchEngine = new searchEngine();

		//mySearchEngine.traverseInternet("http://www.cs.mcgill.ca/");
		mySearchEngine.traverseInternet("http://www.youtube.com/channel/HCtnHdj3df7iM");
		mySearchEngine.computePageRanks();
		System.out.println(mySearchEngine);

		BufferedReader stndin = new BufferedReader(new InputStreamReader(System.in));
		String query;
		do {
			System.out.print("Enter query: ");
			query = stndin.readLine();
			if ( query != null && query.length() > 0 ) {
				System.out.println("Best site = " + mySearchEngine.getBestURL(query));
			}
		} while (query!=null && query.length()>0);				
	}
}
