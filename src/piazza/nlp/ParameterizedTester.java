 package piazza.nlp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONObject;

import piazza.LoginFailedException;
import piazza.NotLoggedInException;
import piazza.PiazzaClass;

public class ParameterizedTester {
	
	/* Regular grading involves grading the newest set of diary entries within the current grading period by
	 * posting a followup post to each students' diary and generating summary and detailed .csv files
	 * Update csv is used mainly in the case where there were manual changes made to the diary grades from directly and manually
	 * changing student diary grades from within the followup post on Piazza. In this case, only an updated summary .csv file is generated
	 * Full regrade is used in the case of regrading all existing diary entries, regardless of whether they are in the current grading period or not
	 * A followup post is made with a note specifying the full regrade, and summary and detailed .csv files are generated
	 * Read from file is used to output Q&A scores to System.out from a text file containing the diary content of a single student
	 * The file path of the input file should be passed into generateDiaryGrades()
	 */
	public enum Method {
		REGULAR_GRADING_WITH_CSV, UPDATE_CSV_FROM_PIAZZA, FULL_REGRADE, READ_FROM_FILE;
	}
	public static void processSessionPosts(
			String anOutDirectory, int aStartCid, 
			int anEndCid, long aStartTime, 
			long anEndTime, String ... aTags ) throws ClientProtocolException, NotLoggedInException, IOException, LoginFailedException {
		
		APiazzaClassRecursivePosts aLoggedInClass = loginToPiazzaClass();
//		aLoggedInClass.writeAllPosts(anOutDirectory, aStartCid, anEndCid, aStartTime, anEndTime, aTags);

//		String aSubject = "TestSubject";
//		String aContent = "TestContent";
//		String[] aRecipients = new String[] {"i588uvywlmn3b5"};
//		String[] aPostTags = new String[] {"hw1"};
		
	//	id	:	l5mo3pe7wm84dr
		
//		String id = "l5mo3pe7wm84dr";
		String id = "l5hurqwc7pcce";
//		
		aLoggedInClass.createFollowup(id, "auto follow up " + System.currentTimeMillis());
//		
//		aLoggedInClass.createPost(aSubject, aContent, Arrays.asList(aPostTags), Arrays.asList(aRecipients));

		//i588uvywlmn3b5,h68jepo6q4z3bk

//		BufferedReader configReader = new BufferedReader(new FileReader("config.json"));
//		
//		String text = "";
//		String line = configReader.readLine();
//		
//		while (line != null) {
//			text = text + line;
//			line = configReader.readLine();
//		}
//		
//		JSONObject config = new JSONObject(text);
//		String email = config.getString("email");
//		String password = config.getString("password");
//		String classID = config.getString("class_id");
//		
//		
////		String outputFilePath = "/Users/gubow/COMP 691H/524posts.csv";
//
//		//String inputFilePath = "/Path/To/Where/File/Is/Saved";
//		
//		
//		//APiazzaDiaryPD comp524 = new APiazzaDiaryPD(email, password, classID, contactName, fullRegradeNote);
//		APiazzaClassRecursivePosts aClass = new APiazzaClassRecursivePosts(email, password, classID, anOutDir);
//		aClass.writeAllPosts(aStartCid, anEndCid, aStartTime, anEndTime, aTags);
//
//		// comp533.updateAllDiaries(outputFilePath);
//		// Set the method to the desired operation. See the Method enum declaration for details
//		//comp524.setMethod(Method.READ_FROM_FILE);
//
//
//		System.out.println("DONE!");
//		
//		//System.out.println(onePost);
//		
//		configReader.close();
	}
	static String classID;
	public static APiazzaClassRecursivePosts loginToPiazzaClass(
			) throws ClientProtocolException, NotLoggedInException, IOException, LoginFailedException {

		BufferedReader configReader = new BufferedReader(new FileReader("config.json"));
		
		String text = "";
		String line = configReader.readLine();
		
		while (line != null) {
			text = text + line;
			line = configReader.readLine();
		}
		
		JSONObject config = new JSONObject(text);
		String email = config.getString("email");
		String password = config.getString("password");
		classID = config.getString("class_id");
		configReader.close();

		
//		String outputFilePath = "/Users/gubow/COMP 691H/524posts.csv";

		//String inputFilePath = "/Path/To/Where/File/Is/Saved";
		
		
		//APiazzaDiaryPD comp524 = new APiazzaDiaryPD(email, password, classID, contactName, fullRegradeNote);
		APiazzaClassRecursivePosts aClass = new APiazzaClassRecursivePosts(email, password, classID);
		return aClass;
		//	?aClass.writeAllPosts(aStartCid, anEndCid, aStartTime, anEndTime, aTags);

		// comp533.updateAllDiaries(outputFilePath);
		// Set the method to the desired operation. See the Method enum declaration for details
		//comp524.setMethod(Method.READ_FROM_FILE);


//		System.out.println("DONE!");
		
		//System.out.println(onePost);
		
	}

	public static void main(String[] args) throws ClientProtocolException, IOException, LoginFailedException, NotLoggedInException {
		if (args.length < 1) {
			System.out.println("Please input the folder to which the files should be written");
			System.exit(-1);
		}
		processSessionPosts(args[0], 0, Integer.MAX_VALUE, 0,Long.MAX_VALUE);
//		String[] aTags = new String [args.length - 1];
//		for (int i= 0; i <= aTags.length; i++) {
//			aTags[i] = args[i+1];
//		}
//		String anOutDir = args[0];
//		BufferedReader configReader = new BufferedReader(new FileReader("config.json"));
//		
//		String text = "";
//		String line = configReader.readLine();
//		
//		while (line != null) {
//			text = text + line;
//			line = configReader.readLine();
//		}
//		
//		JSONObject config = new JSONObject(text);
//		String email = config.getString("email");
//		String password = config.getString("password");
//		String classID = config.getString("class_id");
//		
//		
////		String outputFilePath = "/Users/gubow/COMP 691H/524posts.csv";
//
//		//String inputFilePath = "/Path/To/Where/File/Is/Saved";
//		
//		
//		//APiazzaDiaryPD comp524 = new APiazzaDiaryPD(email, password, classID, contactName, fullRegradeNote);
//		APiazzaClassRecursivePosts aClass = new APiazzaClassRecursivePosts(email, password, classID, anOutDir);
//		List<Map<String, Object>> allPosts;
//		// comp533.updateAllDiaries(outputFilePath);
//		// Set the method to the desired operation. See the Method enum declaration for details
//		//comp524.setMethod(Method.READ_FROM_FILE);
//		aClass.writeAllPosts(0, aTags);
//		System.out.println("DONE!");
//		
//		//System.out.println(onePost);
//		
//		configReader.close();
	}

}
