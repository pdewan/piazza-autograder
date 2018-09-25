package piazza;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;

public class APiazzaClassWithDiaries extends APiazzaClass {

	private Pattern GRADE_MY_QA = Pattern.compile("(My\\sQ&A).*= .*?\\+?(\\d+)",Pattern.CASE_INSENSITIVE);
	private Pattern GRADE_CLASS_QA = Pattern.compile("(Class\\sQ&A).*=.*?\\+?(\\d+)",Pattern.CASE_INSENSITIVE);
	//private Pattern GRADE_NOTES = Pattern.compile(".*?Notes:\\s*(.*)", Pattern.CASE_INSENSITIVE);

	// this is the map of all the diaries, the keys are the user's names and the
	// values are the
	// post objects containing the diaries
	private Map<String, Map<String, Object>> diaries = new HashMap<String, Map<String, Object>>();
	private Instant lastUpdateTime = null;

	
	public APiazzaClassWithDiaries(String email, String password, String classID)
			throws ClientProtocolException, IOException, LoginFailedException, NotLoggedInException {
		super(email, password, classID);
		this.updateAllDiaries();
	}

	// populate the diaries variable
	public void updateAllDiaries() throws ClientProtocolException, NotLoggedInException, IOException { 
		
		BufferedWriter br = new BufferedWriter(new FileWriter("/Users/Johnson/desktop/test.txt"));
		int count = 0;
		for (Map<String, Object> post : this.getAllPosts()) {
			@SuppressWarnings("unchecked")
			Map<String, String> top = ((List<Map<String, String>>) post.get("history")).get(0);
			String content = top.get("content").toLowerCase();
			
			if (content.contains("diary")) {
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
					count++;
					//br.write(content);
//					br.write(Integer.toString(startIndex));
//					br.write("\n\n");
//					br.write(Integer.toString(findIndex));
//					br.write("\n\n");
					String name = content.toLowerCase().substring(startIndex, findIndex);
					//String name = content.toLowerCase().substring(0,1);
//				    br.write(name);
					br.write(name);
				    br.write("\n\n\n");
					br.write(content);
					br.write("\n\n\n");
					
					this.diaries.put(name, post);
					//System.out.println(name);
				}
			}
		}
		br.write(Integer.toString(count));
		br.close();
		this.lastUpdateTime = Instant.now();
	}

	private List<String> get_grades(String name)
			throws ClientProtocolException, NotLoggedInException, IOException {
		Map<String, Object> diary = this.diaries.get(name);
		@SuppressWarnings("unchecked")
		String diary_content = ((List<Map<String, String>>) diary.get("history")).get(0).get("content");
		
		int count = 0;    //count the number of occurrence of word "instruction or Instruction"
		
		if(diary_content.contains("diary") || diary_content.contains("Diary")) {
			diary_content = diary_content.replaceAll("</p>", "<SPLIT>");
			String[] content_arr = diary_content.split("<SPLIT>");
			for(String line : content_arr) {
				if(line.contains("instructor") || line.contains("Instructor")) count++;
			}
		}
		int total_grade = 5*count;;
		
		
		String aid = this.getAuthorId(diary);
		if (aid.equals("")) {
			return null;
		}
		String authorname = this.getUserName(aid);
		String email = this.getUserEmail(aid);

		int totalDiaryGrade = 0;
		int totalQAGrade = 0;

		List<String> grades = new ArrayList<String>();
		
//		for (Map<String, String> reply : (List<Map<String, String>>) diary.get("children")) {
////			System.out.println(reply);
//			String subject = reply.get("subject");
//			
//			if (subject == null || !subject.contains("Date"))
//				continue;
//			subject = subject.replaceAll("<p>", "<SPLIT>");
//			subject = subject.replaceAll("br", "<SPLIT>");
//			String[] lines = subject.split("<SPLIT>");
//
//			String date = null;
//			String diaryGrade = null;
//			String QAGrade = null;
//			String comments = null;
//
//			String graderId = reply.get("uid");
//			String graderName = this.getUserName(graderId);
//
//			for (String line : lines) {
//				line = line.replace("&#43;", "+");
//				line = line.replace("&amp;", "&");
//				
//				
//				
//				
//				Matcher m = this.GRADE_MY_QA.matcher(line);
//				if(m.find()) {
//					diaryGrade = m.group(2);
//				}
//				
//				Matcher n = this.GRADE_CLASS_QA.matcher(line);
//				if(n.find()) {
//					QAGrade = n.group(2);
//				}
//				
//			}
//
//			if (comments != null) comments = comments.replaceAll("</p>", "");
//			
//			
//			if (diaryGrade != null || QAGrade != null) {
//				diaryGrade = (diaryGrade == null) ? "0" : diaryGrade;
//				QAGrade = (QAGrade == null) ? "0" : QAGrade;
////				String graderId = reply.get("uid");
////				String graderName = this.getUserName(graderId); 
//				String graderEmail = this.getUserEmail(graderId);
//
//				date = reply.get("updated").split("T")[0];
//				
//
//				if (!((String) this.getUser(graderId).get("role")).equals("student")) {
//					
//					totalDiaryGrade += Integer.parseInt(diaryGrade);
//					totalQAGrade += Integer.parseInt(QAGrade);
//					
////					grades.add(new ArrayList<String>(Arrays.asList(email, authorname, diaryGrade, QAGrade, graderName,
////							graderEmail, date, comments, diary_content))); 
//					
//					grades  = new ArrayList<String>(Arrays.asList(authorname,total_grade+""));
//				}
//
//			} 
//		}
		grades  = new ArrayList<String>(Arrays.asList(authorname,total_grade+""));
		
		System.out.println("Name: " + authorname);
		System.out.println("Count: " + count);
		System.out.println("Total Grade: " + total_grade);
    	System.out.println("My Q&A Grade: " + totalDiaryGrade);
		System.out.println("Class O&A Grade: " + totalQAGrade);
		System.out.println("---------");
		return grades;
	}
	

	// get a list of diary grades for every student, each with two pieces of info: Name and total grade
	public List<List<String>> getDiaryGrades() throws ClientProtocolException, NotLoggedInException, IOException {
		List<List<String>> grades = new ArrayList<List<String>>();
		for (String name : this.diaries.keySet()) {
			System.out.println(name);
			List<String> g = this.get_grades(name);
			System.out.println(g.toString());
			grades.add(g);
		}
		
		//System.out.println(grades.toString());
		return grades;
	}

	
	
	public void generateDiaryGradesCSV(String path) throws IOException, NotLoggedInException {
		BufferedWriter br = new BufferedWriter(new FileWriter(path));
		
		List<List<String>> grades = this.getDiaryGrades();
		br.write("Name, Total Grade\n");

		System.out.println(">>>>>>>>>>>>>>>>>>>>");
		for (List<String> g : grades) {
			for (String s : g) {
				System.out.print(s+" ");
			}
			System.out.print("\n");
		}
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
}
