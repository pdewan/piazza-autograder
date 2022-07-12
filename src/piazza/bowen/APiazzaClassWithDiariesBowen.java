package piazza.bowen;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;

import piazza.LoginFailedException;
import piazza.NotLoggedInException;

public class APiazzaClassWithDiariesBowen extends APiazzaClassBowen {

	private Pattern GRADE_MY_QA = Pattern.compile("(My\\sQ&A).*= .*?\\+?(\\d+)",Pattern.CASE_INSENSITIVE);
	private Pattern GRADE_CLASS_QA = Pattern.compile("(Class\\sQ&A).*=.*?\\+?(\\d+)",Pattern.CASE_INSENSITIVE);
	private Pattern GRADE_DATE = Pattern.compile(".*?Date:\\s*([0-9]+/[0-9]+).*", Pattern.CASE_INSENSITIVE);
	private Pattern DIARY_DATE = Pattern.compile(".*?([0-9]+/[0-9]+).*",Pattern.CASE_INSENSITIVE);
	private Pattern DIARY_DATE1 = Pattern.compile(".*?([0-9]+)/([0-9]+).*",Pattern.CASE_INSENSITIVE);
	private Pattern GRADE_NOTES = Pattern.compile(".*?Notes.*?(.*)", Pattern.CASE_INSENSITIVE);
	private Pattern GRADE_NOTES1 = Pattern.compile(".*?Notes.*?\n(.*)", Pattern.CASE_INSENSITIVE);

	//private Pattern GRADE_NOTES = Pattern.compile(".*?Notes:\\s*(.*)", Pattern.CASE_INSENSITIVE);

	// this is the map of all the diaries, the keys are the user's names and the
	// values are the
	// post objects containing the diaries
	private Map<String, Map<String, Object>> diaries = new HashMap<String, Map<String, Object>>();
	private Instant lastUpdateTime = null;
    private Map<String, String> cids = new HashMap<>();
    private Map<String, String> final_grades = new HashMap<>();
    private int counter = 0;
	
	public APiazzaClassWithDiariesBowen(String email, String password, String classID)
			throws ClientProtocolException, IOException, LoginFailedException, NotLoggedInException {
		super(email, password, classID);
		this.updateAllDiaries();
	}

	// populate the diaries variable
	public void updateAllDiaries() throws ClientProtocolException, NotLoggedInException, IOException { 
		
		for (Map<String, Object> post : this.getAllPosts()) {
			
			@SuppressWarnings("unchecked")
			Map<String, String> top = ((List<Map<String, String>>) post.get("history")).get(0);
			String content = top.get("content").toLowerCase();
			if (content.contains("diary") || content.contains("Diary")) {
				if (content.indexOf(',') != -1) {
					int findIndex = content.indexOf("Diary");
					findIndex = findIndex==-1? content.indexOf("diary"):findIndex;
					int startIndex = findIndex;
					if(content.charAt(findIndex-2)==',') {
						for(int i = findIndex-3; i>=0; i--) {
							char c = content.charAt(i);
							if(c == ',') {
								startIndex = i+1;
								break;
							}
						}
					}else {
						continue;
					}
					String name = content.toLowerCase().substring(startIndex, findIndex);
					this.cids.put(name, (String) post.get("id"));
					counter++;
					this.diaries.put(name, post);
					System.out.println("-----------");
					System.out.println(name);
					System.out.println("-----------");
				}
			}
		}
		this.lastUpdateTime = Instant.now();
		System.out.println("---------\n"+counter+"\n----------");
	}
	
	private String getNthGroupIfMatch(Pattern pat, String input, int n) {
		Matcher m = pat.matcher(input);
		
		if (m.find()) {
			if (n > m.groupCount())
				return null;
			//System.out.println(m.groupCount());
			return m.group(n);
		}
		else {
			return null;
		}
	}
	

	private List<List<String>> get_grades(String name) throws ClientProtocolException, NotLoggedInException, IOException {
		Map<String, Object> diary = this.diaries.get(name);
		List<List<String>> individual_grade = new ArrayList<>();
		
 		String aid = this.getAuthorId(diary);
		if (aid.equals("")) { return null; }
		String authorname = this.getUserName(aid);
		String email = this.getUserEmail(aid);
		
		@SuppressWarnings("unchecked")
		String diary_content = ((List<Map<String, String>>) diary.get("history")).get(0).get("content");
		
		//int my_QAcount = 0;          //count the number of occurrence of word "instruction or Instruction" in my Q&A
		//int class_QAcount = 0;		 //count the number of occurrence of word "instruction or Instruction" in class Q&A
		Map<String, String> date_comment = new HashMap<>();  // key: TA_date --> value: TA_note

		
		for (Map<String, String> reply : (List<Map<String, String>>)diary.get("children")) {
			String subject = reply.get("subject");
			if (subject == null)
				continue;
			subject = subject.replaceAll("<p>", "<SPLIT>");
			subject = subject.replaceAll("br", "<SPLIT>");
			String[] lines = subject.split("<SPLIT>");
			String month = null;
			String date = null;
			String comment = null;
			month = this.getNthGroupIfMatch(this.GRADE_DATE, subject, 1);
			date = this.getNthGroupIfMatch(this.GRADE_DATE, subject, 2);
			comment = this.getNthGroupIfMatch(this.GRADE_NOTES1, subject, 1);
			comment += this.getNthGroupIfMatch(this.GRADE_NOTES, subject, 1);
			if(month!= null && comment != null) date_comment.put(month+"-"+date, comment);
//			for (String line : lines) {
//				
//				line = line.replace("&#43;", "");
//				line = line.replace("&amp;", "/");
//				if(date == null) date = this.getNthGroupIfMatch(this.GRADE_DATE, line, 1);
//				if(comment == null) comment = this.getNthGroupIfMatch(this.GRADE_NOTES, line, 1);
//				if(date != null && comment != null) {
//					date_comment.put(date, comment);
//					date = null;
//					comment = null;
//				}
//			}
		}
		
		if(diary_content.contains("diary") || diary_content.contains("Diary")) {
			
			Matcher m = this.DIARY_DATE.matcher(diary_content);
			while(m.find()) {
				String date1 = m.group(1);
				diary_content = diary_content.replaceAll(date1, "<date_breaker>" + date1);		
			}
			
			String[] date_seperate = diary_content.split("<date_breaker>");
			
			List<String> each_date_grade = new ArrayList<>();
			String post_date = null;
			
			for(String each_date : date_seperate) {
				int my_QAcount = 0;          
				int class_QAcount = 0;
				String date_match = getNthGroupIfMatch(this.DIARY_DATE, each_date, 1);
				if(date_match == null) continue;
				each_date = each_date.replaceAll("&#43;", "");
				each_date = each_date.replaceAll("&amp;", "&");
				each_date = each_date.replaceAll("My Q&A", "<breaker>My Q&A");
				each_date = each_date.replaceAll("my Q&A", "<breaker>my Q&A");          // in case of lower case
				each_date = each_date.replaceAll("Class Q&A", "<breaker>Class Q&A");
				each_date = each_date.replaceAll("class Q&A", "<breaker>class Q&A");
				String each_date_content = each_date;
				String[] content_arr = each_date.split("<breaker>");
				for(String line : content_arr) {
					if(line.contains("My Q&A") || line.contains("my Q&A")) {
						line = line.replaceAll("<li>", "<p>");
						String[] split = line.split("<p");
						for(String l : split) {
							line = line.replaceAll("<li>", "<p>");
							if(l.contains("I:") || l.contains("instructor") || l.contains("Instructor")
									|| l.contains("professor")|| l.contains("Professor")) my_QAcount++;
						}
					}
					if(line.contains("Class Q&A") || line.contains("class Q&A")) {
						String[] split = line.split("<p");
						for(String l : split) {
							if(l.contains("I:") || l.contains("instructor") || l.contains("Instructor")
									|| l.contains("professor")|| l.contains("Professor")) class_QAcount++;
						}
					}
					String temp_date = null;
					String temp_date_1 = this.getNthGroupIfMatch(this.DIARY_DATE1, line, 1);
					String temp_date_2 = this.getNthGroupIfMatch(this.DIARY_DATE1, line, 2);
					if(temp_date_1 != null && temp_date_2 != null) temp_date = temp_date_1 + "-" + temp_date_2;
					if(post_date == null && temp_date != null) post_date = temp_date;
					if(temp_date != null && temp_date != post_date) post_date = temp_date;
				}
				
				if(post_date == null) continue;
				int myQA_grade = 5*my_QAcount;
				int classQA_grade = 5*class_QAcount;
				int total_grade = myQA_grade + classQA_grade;
				final_grades.put(name, ""+total_grade);
				
				each_date_grade = new ArrayList<String>(Arrays.asList(email, authorname, classQA_grade+"", myQA_grade+"",
												 "n/a", "n/a", "2018-"+post_date, each_date_content));
				individual_grade.add(each_date_grade);		
			}
			
			
			int size = date_comment.size();
			for(int i = 0; i < individual_grade.size(); i++) {
				if(i < size) {
					for(Map.Entry<String, String> entry : date_comment.entrySet()) {
						String temp_date = entry.getKey();
						String comment = entry.getValue();
						int len = individual_grade.get(i).size();
						String date = "2018-"+temp_date;
						//individual_grade.get(i).add(len-1, date);
						individual_grade.get(i).add(len-1, comment);
					}
				}else {
					int len = individual_grade.get(i).size();
					//individual_grade.get(i).add(len-1, "no TA post");
					individual_grade.get(i).add(len-1, "no TA comment");
				}
			}
		}
		
		
		System.out.println("Name: " + authorname);
		//int total = my_QAcount+class_QAcount;
		//System.out.println("Count: " + total);
//		System.out.println("Total Grade: " + total_grade);
//    	System.out.println("My Q&A Grade: " + myQA_grade);
//		System.out.println("Class O&A Grade: " + classQA_grade);
		System.out.println("---------");
		return individual_grade;
	}
	

	// get a list of diary grades for every student, each with two pieces of info: Name and total grade
	public List<List<String>> getDiaryGrades() throws ClientProtocolException, NotLoggedInException, IOException {
		List<List<String>> grades = new ArrayList<List<String>>();
		for (String name : this.diaries.keySet()) {
			
			System.out.println(name);
			List<List<String>> g = this.get_grades(name);
			//System.out.println(g.toString());
			grades.addAll(g);
		}
		
		//System.out.println(grades.toString());
		return grades;
	}

	
	
	public void generateDiaryGradesCSV(String path) throws IOException, NotLoggedInException {
		BufferedWriter br = new BufferedWriter(new FileWriter(path));
		
		List<List<String>> grades = this.getDiaryGrades();
		br.write("Student Email, Student Name, Class Q&A, My Q&A, Grading TA, TA's Email, Post Date, Comment, UUID\n");

		System.out.println(grades.size());
		for (List<String> g : grades) {
			for (String s : g) {
				if (s != null) {
					//s = s.replaceAll(",", ";");
					br.write("\"" + s + "\"");
				}
				br.write(", ");
			}
			br.write("\n");
		}

		br.close();
	}

	public Instant getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void autoPost() throws ClientProtocolException, NotLoggedInException, IOException {
		for(String name : cids.keySet()) {
			String cid = cids.get(name);
			String grade = final_grades.get(name);
			String date = LocalDate.now().toString();
			String post = "<p>Your diary grade up to " + date +" is:  " + grade + "\n" + "if you have any question on your grading, please talk to one of LAs</p>";
			//this.createFollowup(cid, post);
		}
	}
	
}
