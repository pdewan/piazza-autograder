package piazza;

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

public class APiazzaClassWithDiaries_TA extends APiazzaClass {

	private Pattern GRADE_MY_QA = Pattern.compile("(My\\sQ&A).*= .*?\\+?(\\d+)",Pattern.CASE_INSENSITIVE);
	private Pattern GRADE_CLASS_QA = Pattern.compile("(Class\\sQ&A).*=.*?\\+?(\\d+)",Pattern.CASE_INSENSITIVE);
	private Pattern GRADE_DATE = Pattern.compile(".*?Date:\\s*([0-9]+/[0-9]+).*", Pattern.CASE_INSENSITIVE);

	// this is the map of all the diaries, the keys are the user's names and the
	// values are the
	// post objects containing the diaries
	private Map<String, Map<String, Object>> diaries = new HashMap<String, Map<String, Object>>();
	private Instant lastUpdateTime = null;
    private Map<String, String> cids = new HashMap<>();
    private Map<String, String> final_grades = new HashMap<>();
	
	public APiazzaClassWithDiaries_TA(String email, String password, String classID)
			throws ClientProtocolException, IOException, LoginFailedException, NotLoggedInException {
		super(email, password, classID);
		this.updateAllDiaries();
	}

	// populate the diaries variable
	public void updateAllDiaries() throws ClientProtocolException, NotLoggedInException, IOException { 
		
		int count = 0;
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
					count++;	
					String name = content.toLowerCase().substring(startIndex, findIndex);
					this.cids.put(name, (String) post.get("id"));
					this.diaries.put(name, post);
				}
			}
		}
		this.lastUpdateTime = Instant.now();
	}

	@SuppressWarnings("unchecked")
	private List<String> get_grades(String name) throws ClientProtocolException, NotLoggedInException, IOException {
		Map<String, Object> diary = this.diaries.get(name);
		
		String diary_content = ((List<Map<String, String>>)diary.get("history")).get(0).get("content");
		
		String aid = this.getAuthorId(diary);
		if (aid.equals("")) { return null; }
		String authorname = this.getUserName(aid);
		String email = this.getUserEmail(aid);
	

		List<String> grades = new ArrayList<String>();
	
		
		//String date = null;
		int my_QAGrade = 0;
		int class_QAGrade = 0;
		String comments = null;
		
		for (Map<String, Object> reply : (List<Map<String, Object>>) diary.get("children")) {
			
			// If TAs post in a "answer box" 
			if(reply.get("history") != null) {
				List<Map<String, String>> sub_children = (List<Map<String, String>>) reply.get("history");
				String subject2;
				Map<String, String> content = sub_children.get(0);
					if(content.get("content") != null) {
						subject2 = content.get("content");
						subject2 = subject2.replaceAll("<p>", "<SPLIT>");
						String[] lines = subject2.split("<SPLIT>");
						for (String line : lines) {
							line = line.replace("&#43;", "+");
							line = line.replace("&amp;", "&");
							
							Matcher m = this.GRADE_MY_QA.matcher(line);
							if(m.find()) {
								my_QAGrade += Integer.parseInt(m.group(2));
							}
							
							Matcher n = this.GRADE_CLASS_QA.matcher(line);
							if(n.find()) {
								class_QAGrade += Integer.parseInt(n.group(2));
							}
						
						}
					}
			}
			
			
			// If TAs post in a "followup box" and also update in the same box
			if(reply.get("children") != null) {
				List<Map<String, String>> sub_children = (List<Map<String, String>>) reply.get("children");
				String subject1;
				//System.out.println(subject1);
				for(Map<String, String> sub : sub_children) {
					if(sub.get("subject") != null) {
						subject1 = sub.get("subject");
						subject1 = subject1.replaceAll("<p>", "<SPLIT>");
						//subject1 = subject1.replaceAll("br", "<SPLIT>");
						//subject1 = subject1.replaceAll("\n", "<SPLIT>");
						String[] lines = subject1.split("<SPLIT>");
						for (String line : lines) {
							line = line.replace("&#43;", "+");
							line = line.replace("&amp;", "&");

							Matcher m = this.GRADE_MY_QA.matcher(line);
							if(m.find()) {
								my_QAGrade += Integer.parseInt(m.group(2));
							}
							
							Matcher n = this.GRADE_CLASS_QA.matcher(line);
							if(n.find()) {
								class_QAGrade += Integer.parseInt(n.group(2));
							}
						
						}
					}
				}
			}
			
			// If TAs post in a "followup box" and update in another "followup box"
			String subject = (String) reply.get("subject");
			
			if (subject == null || !subject.contains("Date")) continue;
			
			subject = subject.replaceAll("<p>", "<SPLIT>");
			String[] lines = subject.split("<SPLIT>");

			for (String line : lines) {
				line = line.replace("&#43;", "+");
				line = line.replace("&amp;", "&");
				
				
				Matcher m = this.GRADE_MY_QA.matcher(line);
				if(m.find()) {
					my_QAGrade += Integer.parseInt(m.group(2));
				}
				
				Matcher n = this.GRADE_CLASS_QA.matcher(line);
				if(n.find()) {
					class_QAGrade += Integer.parseInt(n.group(2));
				}
				
			}

			if (comments != null) comments = comments.replaceAll("</p>", "");
			
			
//				String graderId = reply.get("uid");
//				String graderName = this.getUserName(graderId); 
				//String graderEmail = this.getUserEmail(graderId);

				String s1 = (String) reply.get("updated");
				//date = s1.split("T")[0];
				

				//if (!((String) this.getUser(graderId).get("role")).equals("student")) {
					
					//totalDiaryGrade += Integer.parseInt(diaryGrade);
					//totalQAGrade += Integer.parseInt(QAGrade);
					
//					grades.add(new ArrayList<String>(Arrays.asList(email, authorname, diaryGrade, QAGrade, graderName,
//							graderEmail, date, comments, diary_content))); 		
		
		}
		grades  = new ArrayList<String>(Arrays.asList(email, authorname, class_QAGrade+"", my_QAGrade+"", diary_content));
		
		// line breaker
		//grades  = new ArrayList<String>(Arrays.asList(authorname,total_grade+""));
		
		System.out.println("Name: " + authorname);
		System.out.println("grade: " + class_QAGrade+my_QAGrade+"");
//		System.out.println("Count: " + count);
//		System.out.println("Total Grade: " + total_grade);
//    	System.out.println("My Q&A Grade: " + totalDiaryGrade);
//		System.out.println("Class O&A Grade: " + totalQAGrade);
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
		br.write("Email, Name, class Q&A, My Q&A\n");

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

	public void autoPost() throws ClientProtocolException, NotLoggedInException, IOException {
		for(String name : cids.keySet()) {
			String cid = cids.get(name);
			String grade = final_grades.get(name);
			String date = LocalDate.now().toString();
			String post = "<p>Your diary grade up to " + date +" is:  " + grade + "\n" + "if you have any question on your grading, please talk to one of LAs</p>";
			this.createFollowup(cid, post);
		}
	}
	
}
