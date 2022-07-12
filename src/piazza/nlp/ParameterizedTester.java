 package piazza.nlp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
	
	public static void main(String[] args) throws ClientProtocolException, IOException, LoginFailedException, NotLoggedInException {
		if (args.length != 2) {
			System.out.println("Please input the regular and incomplete posts files as arguments");
			System.exit(-1);
		}
		String aPostsFile = args[0];
		String anIncompletePostsFile = args[1];
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
		String classID = config.getString("class_id");
		
		
//		String outputFilePath = "/Users/gubow/COMP 691H/524posts.csv";
		String outputFilePath = aPostsFile;

		//String inputFilePath = "/Path/To/Where/File/Is/Saved";
		String contactName = "one of the TAs";
		String fullRegradeNote = "Note that this is a full regrade of all diary entries that currently exist";
		
		//APiazzaDiaryPD comp524 = new APiazzaDiaryPD(email, password, classID, contactName, fullRegradeNote);
		APiazzaClassRecursivePosts comp524 = new APiazzaClassRecursivePosts(email, password, classID, anIncompletePostsFile);
		List<Map<String, Object>> allPosts;
		// comp533.updateAllDiaries(outputFilePath);
		// Set the method to the desired operation. See the Method enum declaration for details
		//comp524.setMethod(Method.READ_FROM_FILE);
		comp524.getAllPosts();
		System.out.println("DONE!");
		
		//System.out.println(onePost);
		
		configReader.close();
	}

}
