package piazza.nlp;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import piazza.nlp.redux.exceptions.LoginFailedException;
import piazza.nlp.redux.exceptions.NotLoggedInException;

public class SynchronousMediatedGPTRunner {

	static APiazzaClassRecursivePostsMLSynchronous loggedInClass;
	static String outDir = "./diaries";
	
	public static void main (String[] args) {
		
		int data_post_number = 21;
		boolean resetLog = false;
		
		try {
			
			loggedInClass = ParameterizedTester.loginToPiazzaClassFromEnvVarSynchronous();
			loggedInClass.setUpTool(Integer.toString(data_post_number), false);
		
		} catch (NotLoggedInException | IOException | LoginFailedException e) {
		
			e.printStackTrace();
		
		}
		
		while (true) {
			
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
			LocalDateTime now = LocalDateTime.now();  
			System.out.println();
			System.out.println(dtf.format(now));
			System.out.println("PROCESS POSTS CALLED");
			System.out.println();
			
			int numNewPosts = -1;
			try {
				numNewPosts = loggedInClass.processNewPosts(resetLog);
			} catch (IOException | NotLoggedInException e) {
				e.printStackTrace();
			}
			
			System.out.println();
			System.out.println("PROCESS POSTS FINISHED");
			System.out.println("Number of new posts: " + numNewPosts);
			System.out.println(dtf.format(now));
			System.out.println();
			
		}
		
	}
	
}
