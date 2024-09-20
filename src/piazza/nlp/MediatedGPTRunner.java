package piazza.nlp;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONObject;
import piazza.LoginFailedException;
import piazza.NotLoggedInException;

public class MediatedGPTRunner {

	static APiazzaClassRecursivePostsML loggedInClass;
	static String outDir = "./diaries";
	
	static long currPollingRate, maxPollingRate, minPollingRate;
	static boolean resetLog;
	
	private static ScheduledExecutorService scheduledExecutorService;
	private static ScheduledFuture<?> futureTask;
	private static Runnable myTask;
	
	
	// drafts a GPT reply for all unread posts that were tagged as needing help, returns how many of those posts there were
	public static int processPosts() throws IOException, NotLoggedInException, LoginFailedException {

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
		LocalDateTime now = LocalDateTime.now();  
		  
		System.out.println();
		System.out.println("PROCESS POSTS CALLED");
		System.out.println("POLLING RATE: " + currPollingRate + " SECONDS");
		System.out.println(dtf.format(now));
		System.out.println();
		
		return loggedInClass.processNewPosts(resetLog);
		
	}
	
	
	// set up a fixed-rate scheduler to run processPosts()
	public static void createScheduler() {
		
		myTask = new Runnable() {
	        @Override
	        public void run() {
	        	try {
	        		dynamicallyPoll();
				} catch (IOException | NotLoggedInException | LoginFailedException e) {
					e.printStackTrace();
				}
	        }
	    };
	    
		//scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
	    scheduledExecutorService = Executors.newScheduledThreadPool(5);
	    futureTask = scheduledExecutorService.scheduleAtFixedRate(myTask, 0, currPollingRate, TimeUnit.SECONDS);
	
	}
	
	
	// change the interval of the scheduler
	public static void changeInterval(long newPollingRate) {
		
	    if (newPollingRate > 0) {
	 
	    	long initialDelay = 0;
	    	
	    	if (futureTask != null) {
	    		
	    		long remainingTime = futureTask.getDelay(TimeUnit.SECONDS);
	    		System.out.println("Remaining time: " + remainingTime);
	    		long elapsedTime = currPollingRate - remainingTime;	
	    		initialDelay = Math.max(0, newPollingRate - elapsedTime);
	    	
	    		futureTask.cancel(true);
	    		
	    	}
	    	System.out.println("Initial delay: " + initialDelay);
	        
	    	currPollingRate = newPollingRate;
	        futureTask = scheduledExecutorService.scheduleAtFixedRate(myTask, 0, initialDelay, TimeUnit.SECONDS);
	    }
	    
	}
	
	
	// repeatedly process posts using a time interval that changed based on recent activity
	// returns whether the interval was changed
	public static boolean dynamicallyPoll() throws IOException, NotLoggedInException, LoginFailedException {
		
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
		LocalDateTime now = LocalDateTime.now();  
		System.out.println(dtf.format(now));
	
		
		int numNewPosts = processPosts();
		System.out.println();
		System.out.println("PROCESS POSTS FINISHED");
		System.out.println(dtf.format(now));
		System.out.println();
		
		int upperBound = 5;
		int lowerBound = 1;
		
		// TODO: This dynamic polling stuff is not working right, so currently just running it every 30 min
		
//		// if there were greater than upperBound number of new help-needed posts, shorten the time interval
//		if ((numNewPosts > upperBound) && (currPollingRate / 2 > minPollingRate)) {
//			
//			// exponential decrease
//			long newInterval = currPollingRate / 2;
//			System.out.println("SHORTENING POLLING RATE TO " + newInterval);
//			changeInterval(newInterval);
//			return true;
//			
//		}
//		
//		// if there were less than lowerBound number of new help-needed posts, lengthen the time interval
//		if ((numNewPosts < lowerBound) && (currPollingRate * 2 <= maxPollingRate)) {
//			
//			// exponential increase
//			long newInterval = currPollingRate * 2;
//			System.out.println("LENGTHENING POLLING RATE TO " + newInterval);
//			changeInterval(newInterval);
//
//			return true;
//			
//		}
		
		return false;
		
	}
	
	
	public static void main (String[] args) {

		// NOTE: The dynamic polling stuff is not working right, so currently just running it every 30 min
		
		// ML 992 id: m0mymncloco2ty
		// F24 524 id: lzk5x8ctkej770
		
		// user variables
		int data_post_number = 6;
		currPollingRate = 1800; // initial polling rate in seconds
		maxPollingRate = currPollingRate * 10;
		minPollingRate = 900; // minimum of 15 min
		resetLog = false; 	// whether to wipe the log of processed posts clean, re-running the tool
						 	// for all help-needed posts (normally this should be false in actual use)
		

		try {
			
			loggedInClass = ParameterizedTester.loginToPiazzaClassFromEnvVar();
			loggedInClass.setUpTool(Integer.toString(data_post_number), false);
		
		} catch (NotLoggedInException | IOException | LoginFailedException e) {
		
			e.printStackTrace();
		
		}
		
		// set up schedulers
		createScheduler();
			
	}
	
	
}
