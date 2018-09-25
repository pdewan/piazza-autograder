 package main;

import java.util.Arrays;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONObject;

import piazza.APiazzaClass;
import piazza.APiazzaClassWithDiaries;
import piazza.LoginFailedException;
import piazza.NotLoggedInException;
import piazza.PiazzaClass;

public class Tester {
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
		String classID = config.getString("class_id");
		

//		PiazzaClass comp401 = new APiazzaClass(email, password, classID);
		//List<Map<String, Object>> posts = comp401.getAllPosts();
		
//		for (Map<String, Object> post : posts) {
//			Map<String, Object> top = ((List<Map<String, Object>>)post.get("history")).get(0);
//			System.out.println(top.get("subject"));
//			System.out.println(top.get("content"));
//			System.out.println("==================");
//		}
		
		APiazzaClassWithDiaries comp401p = new APiazzaClassWithDiaries(email, password, classID);
		
//		comp401p.getDiaryGrades();
		comp401p.generateDiaryGradesCSV("/Users/Johnson/desktop/401diaries.csv");
		
		System.out.println("DONE!");
		
//		System.out.println(comp401p.getDiaryGrades().toString());
		
//		List<List<String>> grades = comp401p.getDiaryGrades();
		
//		System.out.println(grades);
//
//		for (List<String> g: grades) {
//			System.out.println(Arrays.toString(g.toArray()));
//		}
		String a = "asd"
;		
	}

}
