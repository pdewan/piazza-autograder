 package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONObject;

import piazza.APiazzaClass;
import piazza.APiazzaClassWithDiaries;
import piazza.APiazzaClassWithDiaries_2;
import piazza.APiazzaClassWithDiaries_3;
import piazza.APiazzaClassWithDiaries_TA;
import piazza.APiazzaClassWithDiaries_Yicheng;
import piazza.LoginFailedException;
import piazza.NotLoggedInException;
import piazza.PiazzaClass;

public class Tester {
	
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
	
	public static void main(String[] argv) throws ClientProtocolException, IOException, LoginFailedException, NotLoggedInException {
		
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
		String classID = "lljvl3218jw2r4"; //config.getString("class_id");
		// 991: k9zvvl9ubao6xa, 524: lljvl3218jw2r4
		
		//(for example, this post is @_)
		String outputFilePath = "C:\\Users\\Mason\\Documents\\COMP524\\diaries";
		//String inputFilePath = "/Path/To/Where/File/Is/Saved";
		String contactName = "Mason Laney";
		String fullRegradeNote = "Note that this is a full regrade of all diary entries that currently exist";
		
		APiazzaClassWithDiaries_3 comp991 = new APiazzaClassWithDiaries_3(email, password, classID, contactName, fullRegradeNote);
		
		// Set the method to the desired operation. See the Method enum declaration for details
		comp991.setMethod(Method.FULL_REGRADE);
		//comp991.generateDiaryGrades(outputFilePath);
		comp991.generateDiaryGrades(outputFilePath);
		
		
		System.out.println("DONE!");
		
		configReader.close();
	}

}
