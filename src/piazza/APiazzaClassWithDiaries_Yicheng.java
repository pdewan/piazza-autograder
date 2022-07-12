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

public class APiazzaClassWithDiaries_Yicheng extends APiazzaClass {
	
	private Pattern DATE_WITH_YEAR_PTRN = Pattern.compile("(.*)([0-9]+/[0-9]+/[0-9]+)(.*)");
	private Pattern DATE_WITHOUT_YEAR_PTRN = Pattern.compile("(.*)([0-9]+/[0-9]+)(.*)");
	private Pattern GRADE_DATE = Pattern.compile(".*?Date:\\s*([0-9]+/[0-9]+).*", Pattern.CASE_INSENSITIVE);
	private Pattern GRADE_DIARY = Pattern.compile(".*?(diaries|diary entries).*(=.*?([0-9]+).*|(n/a))", Pattern.CASE_INSENSITIVE);
	private Pattern GRADE_QA = Pattern.compile(".*?(Q/A|questions?\\sanswered|answered\\squestions?).*(=.*?([0-9]+).*|(n/a))", Pattern.CASE_INSENSITIVE);
	private Pattern GRADE_NOTES = Pattern.compile(".*?Notes:\\s*(.*)", Pattern.CASE_INSENSITIVE);

	// this is the map of all the diaries, the keys are the user's names and the values are the
	// post objects containing the diaries
	private Map<String, Map<String, Object>> diaries = new HashMap<String, Map<String, Object>>();
	private Instant lastUpdateTime = null;
	
	public APiazzaClassWithDiaries_Yicheng(String email, String password, String classID)
			throws ClientProtocolException, IOException, LoginFailedException, NotLoggedInException {
		super(email, password, classID);
		this.updateAllDiaries();
		// TODO Auto-generated constructor stub
	}
	
	public void updateAllDiaries() throws ClientProtocolException, NotLoggedInException, IOException { // populates the diaries private variable
		// do updates here
		for (Map<String, Object> post : this.getAllPosts()) {
//			Map<String, Object> top = ((List<Map<String, Object>>)(post.get("history"))).get(0);//(List<Map<String, Object>>) (post.get("history")).get(0);
			Map<String, String> top = ((List<Map<String, String>>)post.get("history")).get(0);
			String subject = top.get("subject").toLowerCase();
			if (subject.contains("diary")) {
				//System.out.println(subject);
				if (subject.lastIndexOf(' ') != -1) {
					String name = subject.toLowerCase().substring(0, subject.lastIndexOf(' '));
					this.diaries.put(name, post);
				}
			}
		}
		this.lastUpdateTime = Instant.now();
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
		//System.out.println(diary.get("history"));
		@SuppressWarnings("unchecked")
		String diary_content = ((List<Map<String, String>>)diary.get("history")).get(0).get("content");
		String aid = this.getAuthorId(diary);
		if (aid.equals("")) { return null; }
		String authorname = this.getUserName(aid);
		String email = this.getUserEmail(aid);
		
		int totalDiaryGrade = 0;
		int totalQAGrade = 0;
		
		List<List<String>> grades = new ArrayList<List<String>>();
		
		for (Map<String, String> reply : (List<Map<String, String>>)diary.get("children")) {
//			System.out.println(reply);
			String subject = reply.get("subject");
			if (subject == null)
				continue;
			subject = subject.replaceAll("<p>", "<SPLIT>");
			subject = subject.replaceAll("br", "<SPLIT>");
			String[] lines = subject.split("<SPLIT>");
			
			String date = null;
			String diaryGrade = null;
			String QAGrade = null;
			String comments = null;
			
			String graderId = reply.get("uid");
			String graderName = this.getUserName(graderId);
			
	
			
			for (String line : lines) {
				line = line.replace("&#43;", "");
				line = line.replace("&amp;", "/");
				
				if (date == null)
					date = this.getNthGroupIfMatch(this.GRADE_DATE, line, 1);
				if (diaryGrade == null) { // b/c of potential 'n/a' in grades
					Matcher m = this.GRADE_DIARY.matcher(line);
					
					if (m.find()) {
//						System.out.println(m.group());
						if (m.group(2).toLowerCase().contains("n/a")) {
							diaryGrade = "0";
						}
						if (m.groupCount() > 3)
							diaryGrade = m.group(3);
					}

				}
				if (QAGrade == null) {
					Matcher m = this.GRADE_QA.matcher(line);
					
					if (m.find()) {
						if (m.groupCount() > 3 && m.group(3) != null) {
//							System.out.print("Group 3:");
//							System.out.println(m.group(3));
							if (m.group(2).toLowerCase().equals("n/a")) {
								QAGrade = "0";
							}
							else
								QAGrade = m.group(3);
						}
					}
				}
				if (comments == null)
					comments = this.getNthGroupIfMatch(this.GRADE_NOTES, line, 1);
				
				
//					System.out.println(line);
//					System.out.println(date);
//					System.out.println(diaryGrade);
//					System.out.println(QAGrade);
//					System.out.println(comments);
//					System.out.println("===================");
			}
			
			if (comments != null)
				comments = comments.replaceAll("</p>", "");
			if (diaryGrade != null || QAGrade != null) {
				diaryGrade = (diaryGrade == null) ? "0" : diaryGrade;
				QAGrade = (QAGrade == null) ? "0" : QAGrade;
//				String graderId = reply.get("uid");
//				String graderName = this.getUserName(graderId);
				String graderEmail = this.getUserEmail(graderId);
				
				date = reply.get("updated").split("T")[0];
				
				if (!((String)this.getUser(graderId).get("role")).equals("student")) {
					grades.add(new ArrayList<String>(Arrays.asList(email, authorname,
							diaryGrade, QAGrade, graderName, graderEmail, date, comments, diary_content))); // XXX change last field back to diary_content
				}
				
				totalDiaryGrade += Integer.parseInt(diaryGrade);
				totalQAGrade += Integer.parseInt(QAGrade);
			}
		}
		
		System.out.println(name);
		System.out.println(totalDiaryGrade);
		System.out.println(totalQAGrade);
		System.out.println("---------");
		
		return grades;
	}
	
	public List<List<String>> getDiaryGrades() throws ClientProtocolException, NotLoggedInException, IOException {
		List<List<String>> grades = new ArrayList<List<String>>();
//		List<List<String>> g2 = this.get_grades("gavin maddock");
		
//		System.out.println("HI");

		for (String name : this.diaries.keySet()) {
			List<List<String>> g = this.get_grades(name);
			grades.addAll(g);
		}
		return grades;
	}
	
	public void generateDiaryGradesCSV(String path) throws IOException, NotLoggedInException {
		BufferedWriter br = new BufferedWriter(new FileWriter(path));
		
		List<List<String>> grades = this.getDiaryGrades();
		
		for (List<String> g : grades) {
			for (String s : g) {
				if (s != null) {
					s = s.replaceAll(",", ";");
					br.write("\""+ s + "\"");
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