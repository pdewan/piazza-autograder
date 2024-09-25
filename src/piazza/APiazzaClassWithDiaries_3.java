package piazza;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

import main.Tester.Method;

public class APiazzaClassWithDiaries_3 extends APiazzaClass {

	private Pattern GRADE_MY_QA = Pattern.compile("My.*?=\\s(\\d+)",Pattern.CASE_INSENSITIVE);
	private Pattern GRADE_CLASS_QA = Pattern.compile("Class.*?=\\s(\\d+)",Pattern.CASE_INSENSITIVE);
	private Pattern GRADE_DATE = Pattern.compile(".*?Date:\\s*([0-9]+)/([0-9]+).*", Pattern.CASE_INSENSITIVE);
	private Pattern DIARY_DATE = Pattern.compile(".*?([0-9]+/[0-9]+)",Pattern.CASE_INSENSITIVE);
	private Pattern DIARY_DATE1 = Pattern.compile(".*?([0-9]+)/([0-9]+).*",Pattern.CASE_INSENSITIVE);
	private Pattern GRADE_NOTES = Pattern.compile(".*?Notes.*?(.*)", Pattern.CASE_INSENSITIVE);
	private Pattern GRADE_NOTES1 = Pattern.compile(".*?Notes.*?\n(.*)", Pattern.CASE_INSENSITIVE);
	private Pattern DIARY_GRADE = Pattern.compile(".*?Total diary grade up to [0-9]+/[0-9]+ is: ([0-9]+)",Pattern.CASE_INSENSITIVE);
	private Pattern GRADING_PERIOD = Pattern.compile("Grading period: ([0-9]+/[0-9]+) - ([0-9]+/[0-9]+)"); 

	// this is the map of all the diaries, the keys are the user's names and the
	// values are the post objects containing the diaries
	private Map<String, Map<String, Object>> diaries = new HashMap<String, Map<String, Object>>();
	private Instant lastUpdateTime = null;
    private Map<String, String> cids = new HashMap<>();
    private Map<String, String> final_grades = new HashMap<>();
    private int counter = 0;
    
    // Constants to keep track of information needed for grades and auto-posted followups on Piazza
    private final String START_DATE = "startDateInCurrentPeriod";
    private final String END_DATE = "endDateInCurrentPeriod";
    private final String MY_QA_COUNT = "myQACount";
    private final String MY_QA_GRADE = "myQAGrade";
    private final String CLASS_QA_COUNT = "classQACount";
    private final String CLASS_QA_GRADE = "classQAGrade";
    private final String TOTAL_GRADE = "totalGrade";
    private final String NO_START_DATE = "startDateNotSpecified";
    
    // This variable is set to determine what kind of operation the class should execute in terms of generating
    // Piazza posts and generating .csv files
    private Method method = Method.UPDATE_CSV_FROM_PIAZZA;
    
    private String currentYear;
    private String contactName;
    private String fullRegradeNote;
    
	public APiazzaClassWithDiaries_3(String email, String password, String classID, String contactName, String fullRegradeNote)
			throws ClientProtocolException, IOException, LoginFailedException, NotLoggedInException {
			super(email, password, classID);
			currentYear = Integer.toString(LocalDate.now().getYear());
			this.contactName = contactName;
			this.fullRegradeNote = fullRegradeNote;
	}

	// populate the diaries variable
	public void updateAllDiaries() throws ClientProtocolException, NotLoggedInException, IOException { 
		for (Map<String, Object> post : this.getAllPosts()) {
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			//System.out.println(post);
			
			@SuppressWarnings("unchecked")
			Map<String, String> top = ((List<Map<String, String>>) post.get("history")).get(0);
			String content = top.get("content").toLowerCase();
			
			if (content.contains("diary") || content.contains("Diary")) {

				System.out.println("DIARY:");
				System.out.println(post);
				
				String cid = (String) post.get("id");
				String uid = this.getMap().get(cid);
				//System.out.println("cid: " + cid);
				
				Map<String, Object> user = this.getUser(uid);
				if(user == null) continue;
				String role = (String)user.get("role");
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				// TODO: uncomment!!
				if(!role.equals("student")) continue;
				String name = this.getUserName(uid);
				if(!content.contains("Instructor") && !content.contains("instructor")) continue;
//					String name = content.toLowerCase().substring(startIndex, findIndex);
					//this.cids.put(name, (String) post.get("id"));
					this.cids.put(name,cid);
					this.diaries.put(name, post);
					counter++;
					//System.out.println(name);
				}
		}
		this.lastUpdateTime = Instant.now();
		System.out.println("---------\n"+counter+"\n----------");
	}
	
	private String getNthGroupIfMatch(Pattern pat, String input, int n) {
		Matcher m = pat.matcher(input);
		
		if (m.find()) {
			if (n > m.groupCount()) {
				return null;
			}
			return m.group(n);
		}
		else {
//			System.out.println("This pattern could not be found" + pat.toString() + "in " + input);
			return null;
		}
	}
	
	// Converts month and day into an ADate object for use of comparing dates
	private ADate convertMoD(String month, String day) {
		return new MyDate(Integer.parseInt(month), Integer.parseInt(day));
	}
	
	// Returns an end date depending on if an end date exists or not for the current grading period
	private String getEndDate(Map<ADate, Map<String, String>> followupGrades, ADate mostRecentDate) {
		Map<ADate, Map<String, String>> followupGradesCopy = new HashMap<ADate, Map<String,String>>(followupGrades);
		Map<String, String> gradeMap = followupGradesCopy.get(mostRecentDate);
		String endDate = gradeMap.get(END_DATE);
		if (endDate == null) { // We have to find the end date of the previous grading period
			while (endDate == null) {
				followupGradesCopy.remove(mostRecentDate);
				endDate = followupGradesCopy.get(getMostRecentDate(followupGradesCopy)).get(END_DATE);
			}
		}
		return endDate;
	}
	
	// Gets the most recent date from a map of followupGrades
	private ADate getMostRecentDate(Map<ADate, Map<String, String>> followupGrades) {
		ADate mostRecentDate = new MyDate(0, 0);
		for (Map.Entry<ADate, Map<String, String>> followup : followupGrades.entrySet()) {
			if (followup.getKey().compareDate(mostRecentDate) > 0) {
				mostRecentDate = followup.getKey();
			}
		}
		return mostRecentDate;
	}
	
	// Parses the diary post and calculates the grade for the student specified
	private IndividualGrade get_grades(String name) throws ClientProtocolException, NotLoggedInException, IOException {
		Map<String, Object> diary = this.diaries.get(name);
 		String aid = this.getAuthorId(diary);
		if (aid.equals("")) { return null; }
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		String authorname = this.getUserName(aid);
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		String email = this.getUserEmail(aid);
		
		@SuppressWarnings("unchecked")
		String diary_content = ((List<Map<String, String>>) diary.get("history")).get(0).get("content");
		
		return generateGrades(diary, aid, authorname, email, name, diary_content);
	}
	
	private IndividualGrade getGradeFromFile(String filePath) {
		String diary_content = "";
		try {
			diary_content = new String(Files.readAllBytes(Paths.get(filePath)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	    return generateGrades(null, "", "", "", "", diary_content);
		
	}
	
	private IndividualGrade generateGrades(Map<String, Object> diary, String aid, String authorname, String email,
											String name, String diary_content) {
		
		Map<String, String> date_comment = new HashMap<>();  // key: TA_date --> value: TA_note

		List<String> individualGradeSummary = new ArrayList<String>();
		List<List<String>> individualGradeDetailed = new ArrayList<>();
		Map<String, String> gradeMap = new HashMap<String, String>();
		String earliestDiaryDate = null;
		// Used to hold dates and their respective grading components from the grade followups
		Map<ADate, Map<String, String>> followupGrades = new HashMap<>();
		// These variables will hold the cumulative grade total from the grades recorded within the followup posts
		int myQATotFromPrevFollowups = 0;
		int classQATotFromPrevFollowups = 0;
		
		// We want to trick the grader into doing a full regrade by not processing any Piazza followups
		// Making it think that it hasn't graded this diary before
		if (method == Method.REGULAR_GRADING_WITH_CSV || method == Method.UPDATE_CSV_FROM_PIAZZA) {
			// Checks for grade followups from previous gradings
			for (Map<String, String> reply : (List<Map<String, String>>)diary.get("children")) {
				String subject = reply.get("subject");
				if (subject == null)
					continue;
				subject = subject.replaceAll("<p>", "<SPLIT>");
				subject = subject.replaceAll("br", "<SPLIT>");
				String[] lines = subject.split("<SPLIT>");
				String month = null;
				String day = null;
				
				// Extracting the month and day from the graded followups to find the most recent one
				month = this.getNthGroupIfMatch(this.GRADE_DATE, subject, 1);
				day = this.getNthGroupIfMatch(this.GRADE_DATE, subject, 2);
				// Get the cumulative grade from the most recent grading followup
				String grade = this.getNthGroupIfMatch(this.DIARY_GRADE, subject, 1);
				String gradingPeriodStart = this.getNthGroupIfMatch(this.GRADING_PERIOD, subject, 1);
				String gradingPeriodEnd = this.getNthGroupIfMatch(this.GRADING_PERIOD, subject, 2);
				String myQAGrade = this.getNthGroupIfMatch(this.GRADE_MY_QA, subject, 1);
				String classQAGrade = this.getNthGroupIfMatch(this.GRADE_CLASS_QA, subject, 1);
				
				if (earliestDiaryDate == null) {
					// Initialize earliestDiaryDate based on the first follow-up post
					earliestDiaryDate = gradingPeriodStart;
				} else if (gradingPeriodStart.equals(earliestDiaryDate)) {
					// If a future post regrades the first diary, that means it is a full regrade
					// in which case we need to reset the grade counts for myQA and classQA
					myQATotFromPrevFollowups = 0;
					classQATotFromPrevFollowups = 0;
				}
				if (month != null && day != null && grade != null) {
					myQATotFromPrevFollowups += Integer.parseInt(myQAGrade);
					classQATotFromPrevFollowups += Integer.parseInt(classQAGrade);
					Map<String, String> followupMap = new HashMap<String, String>();
					if (gradingPeriodStart == null) { // There may be no grading period if there were no new diary posts since the last grading period
						followupMap.put(START_DATE, NO_START_DATE);
					} else {
						followupMap.put(START_DATE, gradingPeriodStart);
					}
					followupMap.put(END_DATE, gradingPeriodEnd);
					//followupMap.put(MY_QA_GRADE, Integer.toString(myQATotFromPrevFollowups));
					//followupMap.put(CLASS_QA_GRADE, Integer.toString(classQATotFromPrevFollowups));
					followupMap.put(TOTAL_GRADE, grade);
					followupGrades.put(convertMoD(month, day), followupMap);
					//followupGrades.put(convertMoD(month, day), Integer.parseInt(grade));
				}
			}
		}

		// Get the most recent grade
		ADate mostRecentDate = getMostRecentDate(followupGrades); // Will return MyDate(0,0) if no followup grades exist yet for this student
		int mostRecentGrade = 0; // Initialized to 0 because there is no previous followup for the first ever followup
		// Initialize start date to NO_START_DATE in the case that there have been no new diary posts since the last grading period
		gradeMap.put(START_DATE, NO_START_DATE);
		
		// The mostRecentGrade is the one associated with the mostRecentDate
		if (followupGrades.containsKey(mostRecentDate)) {
			mostRecentGrade = Integer.parseInt(followupGrades.get(mostRecentDate).get(TOTAL_GRADE));
		}
		
		// If the method is UPDATE_CSV_FROM_PIAZZA, we can just pull data from the followups and return from generateGrades() early
		if (method == Method.UPDATE_CSV_FROM_PIAZZA) {
			String endDate = "";
			// In the case that followupGrades do not exist yet for this student, supply default values for gradeMap and endDate
			if (followupGrades.size() == 0) {
				gradeMap = new HashMap<String, String>();
				gradeMap.put(TOTAL_GRADE, "0");
				gradeMap.put(START_DATE, NO_START_DATE);
				endDate = "NA";
			} else {
				gradeMap = followupGrades.get(mostRecentDate);
				endDate = getEndDate(followupGrades, mostRecentDate);
			}			
			individualGradeSummary = new ArrayList<String>(Arrays.asList(email, authorname, generateGradingPeriod(gradeMap), 
														Integer.toString(myQATotFromPrevFollowups),
														Integer.toString(classQATotFromPrevFollowups), gradeMap.get(TOTAL_GRADE),
														"n/a", "n/a", currentYear+"-"+endDate.replace('/', '-'), diary_content.replaceAll("\n", " ")));
			// Note that individualGradedDetailed and gradeMap are empty at this point, as they are not needed for UPDATE_CSV_FROM_PIAZZA
			return new AnIndividualGrade(individualGradeSummary, individualGradeDetailed, gradeMap);
		}
		
		
		/* Check the actual content of the diary post for regular grading
		 * Specifically we only care about content since the last time we graded the diary post
		 * to prevent students from adding in posts after the previous grading period has already passed
		 */
		if(diary_content.contains("diary") || diary_content.contains("Diary")) {
			
			Matcher m = this.DIARY_DATE.matcher(diary_content);
			while(m.find()) {
				String date1 = m.group(1);
				diary_content = diary_content.replaceAll(date1, "<date_breaker>" + date1);		
			}
			
			String[] date_separate = diary_content.split("<date_breaker>");
			
			List<String> each_date_grade = new ArrayList<>();
			String post_date = null;
			
			// Keep track of summation counts
			int myQACountTotal = 0;
			int myQAGradeTotal = 0;
			int classQACountTotal = 0;
			int classQAGradeTotal = 0;
			
			// Flag for indicating the date right after the last date recorded from the most recent grading period
			boolean isFirstDateInCurrentPeriod = true;
			/* Flag for indicating whether a particular diary entry should be included in the followup post,
			 * as the grade in the followup post only includes entries within the current grading period date range
			 */
			boolean shouldIncludeInFollowup = false;
			
			for(String each_date : date_separate) {
				int my_QAcount = 0;          
				int class_QAcount = 0;
				String date_match = getNthGroupIfMatch(this.DIARY_DATE, each_date, 1);
				if(date_match == null) continue;
				
				String month = null;
				String day = null;

				// Extracting the month and day from the diary entries
				month = this.getNthGroupIfMatch(this.DIARY_DATE1, each_date, 1);
				day = this.getNthGroupIfMatch(this.DIARY_DATE1, each_date, 2);
				
				shouldIncludeInFollowup = (month != null && day != null && convertMoD(month, day).compareDate(mostRecentDate) <= 0) ? false : true;
				
				if (shouldIncludeInFollowup && isFirstDateInCurrentPeriod) {
					gradeMap.put(START_DATE, date_match);
					isFirstDateInCurrentPeriod = false;
				}
				
				// Main parsing of the diary entry
				each_date = each_date.replaceAll("&#43;", "");
				each_date = each_date.replaceAll("&amp;", "&");
				each_date = each_date.replaceAll("My Q&A", "<breaker>My Q&A");
				each_date = each_date.replaceAll("my Q&A", "<breaker>my Q&A");          // in case of lower case
				each_date = each_date.replaceAll("Class Q&A", "<breaker>Class Q&A");
				each_date = each_date.replaceAll("class Q&A", "<breaker>class Q&A");
				
				String each_date_content = each_date;
				each_date_content = each_date_content.replaceAll("\n", "<newline>");
				String[] content_arr = each_date.split("<breaker>");
				for(String line : content_arr) {
					if(line.contains("My Q&A") || line.contains("my Q&A")) {
						line = line.replaceAll("<li>", "<p>");
						line = line.replaceAll("br", "<p>");
						line = line.replaceAll("\n", "<p>");
						String[] split = line.split("<p");
						for(String l : split) {
							line = line.replaceAll("<li>", "<p>");
							if(l.contains("I:") || l.contains("instructor") || l.contains("Instructor")
									|| l.contains("professor")|| l.contains("Professor")) my_QAcount++;
						}
					}
					if(line.contains("Class Q&A") || line.contains("class Q&A")) {
						line = line.replaceAll("\n", "<p>");
						line = line.replaceAll("br", "<p>");
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
				final_grades.put(name, Integer.toString(total_grade));
				
				if (shouldIncludeInFollowup) {
					myQACountTotal += my_QAcount;
					myQAGradeTotal += myQA_grade;
					classQACountTotal += class_QAcount;
					classQAGradeTotal += classQA_grade;					
				}
				
				each_date_grade = new ArrayList<String>(Arrays.asList(email, authorname, Integer.toString(myQA_grade),
												 Integer.toString(classQA_grade), "n/a", "n/a", currentYear+"-"+post_date, each_date_content));
				individualGradeDetailed.add(each_date_grade);
			}
			
			// End date in current grading period will be the last dated diary entry
			String endDateInCurrentPeriod = "";
			// Check for index out of bounds here in case a student has zero diary entries
			if (date_separate.length > 0) {
				endDateInCurrentPeriod = getNthGroupIfMatch(this.DIARY_DATE, date_separate[date_separate.length - 1], 1);
			}
			gradeMap.put(END_DATE, endDateInCurrentPeriod);
			
			// Populate the gradeMap with grading information for this student
			gradeMap.put(MY_QA_COUNT, Integer.toString(myQACountTotal));
			gradeMap.put(MY_QA_GRADE, Integer.toString(myQAGradeTotal));
			gradeMap.put(CLASS_QA_COUNT, Integer.toString(classQACountTotal));
			gradeMap.put(CLASS_QA_GRADE, Integer.toString(classQAGradeTotal));
			gradeMap.put(TOTAL_GRADE, Integer.toString(myQAGradeTotal + classQAGradeTotal + mostRecentGrade));
			
			/* The summary includes the cumulative grade totals for My Q&A and Class Q&A,
			 * but the grade followup post requires data specific only to this current grading period,
			 * so we only modify what we write as the My Q&A and Class Q&A grades to individualGradeSummary and
			 * leave gradeMap untouched so that the autopost() method can use it with less work involved
			 */
			int cumulativeMyQAGrade = myQATotFromPrevFollowups + myQAGradeTotal;
			int cumulativeClassQAGrade = classQATotFromPrevFollowups + classQAGradeTotal;
			
			individualGradeSummary = new ArrayList<String>(Arrays.asList(email, authorname, generateGradingPeriod(gradeMap), 
													Integer.toString(cumulativeMyQAGrade), Integer.toString(cumulativeClassQAGrade),
													gradeMap.get(TOTAL_GRADE), "n/a", "n/a", currentYear+"-"+post_date));
			
			int size = date_comment.size();
			for (int i = 0; i < individualGradeDetailed.size(); i++) {
				if (i < size) {
					for (Map.Entry<String, String> entry : date_comment.entrySet()) {
						//String temp_date = entry.getKey();
						String comment = entry.getValue();
						int len = individualGradeDetailed.get(i).size();
						//String date = "2020-"+temp_date;
						//individual_grade.get(i).add(len-1, date);
						individualGradeDetailed.get(i).add(len-1, comment);
					}
				} else {
					int len = individualGradeDetailed.get(i).size();
					individualGradeDetailed.get(i).add(len-1, "no TA comment");
				}
			}
		}
		
		System.out.println("Name: " + authorname);
		System.out.println("---------");
		System.out.println(individualGradeDetailed);
		return new AnIndividualGrade(individualGradeSummary, individualGradeDetailed, gradeMap);
	}
	

	// get a list of diary grades for every student, each with two pieces of info: Name and total grade
	public List<IndividualGrade> getDiaryGrades() throws ClientProtocolException, NotLoggedInException, IOException {
		List<IndividualGrade> grades = new ArrayList<IndividualGrade>();
		Map<String, IndividualGrade> gradesList = new HashMap<String, IndividualGrade>();
		for (String name : this.diaries.keySet()) {
			//if (!name.equals("Yifan Xu")) continue;
			//System.out.println(name);
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			IndividualGrade g = this.get_grades(name);
			grades.add(g);
			gradesList.put(name, g);
		}
		if (method == Method.REGULAR_GRADING_WITH_CSV || method == Method.FULL_REGRADE) {
			System.out.println("GRADESLIST TO BE POSTED");
			System.out.println(gradesList);
			this.autoPost(gradesList);			
		}
		return grades;
	}	
	
	public void setMethod(Method method) {
		this.method = method;
	}
	
	public void generateDiaryGrades(String path) throws IOException, NotLoggedInException {
		// Assume that the path ends in the file extension .csv
		// Want to write two files: one is the summary, one is the detailed version
		LocalDate ld = LocalDate.now();
		String date = ld.getMonthValue() + "-" + ld.getDayOfMonth() + "-" + ld.getYear();
		
		String summaryPath = path.substring(0, path.length() - 4) + "_" + date + "_summary.csv";
		String detailedPath = path.substring(0, path.length() - 4) + "_" + date + "_detailed.csv";
		
		if (method == Method.READ_FROM_FILE) {
			IndividualGrade grade = this.getGradeFromFile(path);
			Map<String, String> gradeMap = grade.getMapRepresentation();
			System.out.println("Grading period: " + generateGradingPeriod(gradeMap) + "\n"
					+ "My Q&A: " + gradeMap.get(MY_QA_COUNT) + "*5 = " + gradeMap.get(MY_QA_GRADE) + "\n"
					+ "Class Q&A: " + gradeMap.get(CLASS_QA_COUNT) + "*5 = " + gradeMap.get(CLASS_QA_GRADE) + "\n"
					+ "Total diary grade up to " + date + " is: " + gradeMap.get(TOTAL_GRADE) + "\n"
					);
			return;
		}
		
		// Fetch the diary posts from Piazza
		this.updateAllDiaries();
		
		//ML
//		System.out.println("TEST ALL DIARIES");
//		System.out.println(this.diaries);
		
		// Get a list of individual grades
		List<IndividualGrade> grades = this.getDiaryGrades();
		System.out.println("INDIVIDUAL GRADES:");
		System.out.println(grades);
		
		// The variable used for generating the summary .csv
		List<List<String>> summary_string =  new ArrayList<List<String>>();
		// Populate it based on the string representations of the individual grades
		for (IndividualGrade grade : grades) {
			summary_string.add(grade.getSummaryStringRepresentation());
		}
		BufferedWriter brSummary = new BufferedWriter(new FileWriter(summaryPath));
		
		brSummary.write("Student Email, Student Name, Most Recent Grading Period, My Q&A Total, Class Q&A Total, Total Grade, Grading TA, TA's Email, Last Post Date, Diary Text\n");
		
		System.out.println(">>>>>>>>>>>>>>>>>>>>");
		System.out.println(grades.size());
		
		String s = "";
		for (List<String> g : summary_string) {
			for (int i = 0; i < g.size() - 1; i++) { // g.size() - 1 because we don't want an ending comma to generate an unneeded blank column
				s = g.get(i);
				if (s != null) {
					s = s.replaceAll(",", " ");
					brSummary.write("\"" + s + "\"");
				}
				brSummary.write(", ");
			}
			s = g.get(g.size() - 1); // write the last element
			s = s.replaceAll(",", " ");
			brSummary.write("\"" + s + "\"");
			brSummary.write("\n");
		}
		brSummary.close();
		
		if (method == Method.REGULAR_GRADING_WITH_CSV || method == Method.FULL_REGRADE) {
			// The variable used for generating the detailed .csv
			List<List<String>> detailed_string =  new ArrayList<List<String>>();
			// Populate it based on the string representations of the individual grades
			for (IndividualGrade grade : grades) {
				detailed_string.addAll(grade.getDetailedStringRepresentation());
			}
			BufferedWriter brDetailed = new BufferedWriter(new FileWriter(detailedPath));
			
			brDetailed.write("Student Email, Student Name, My Q&A, Class Q&A, Grading TA, TA's Email, Post Date, Comment, Diary Content\n");

			System.out.println(">>>>>>>>>>>>>>>>>>>>");
			System.out.println(grades.size());
			for (List<String> g : detailed_string) {
				for (int i = 0; i < g.size() - 1; i++) { // g.size() - 1 because we don't want an ending comma to generate an unneeded blank column
					s = g.get(i);
					if (s != null) {
						s = s.replaceAll(",", " ");
						brDetailed.write("\"" + s + "\"");
					}
					brDetailed.write(", ");
				}
				s = g.get(g.size() - 1); // write the last element
				s = s.replaceAll(",", " ");
				brDetailed.write("\"" + s + "\"");
				brDetailed.write("\n");
			}
			brDetailed.close();
		}		
	}

	public Instant getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void autoPost(Map<String, IndividualGrade> gradesList) throws ClientProtocolException, NotLoggedInException, IOException {
		for(String name : cids.keySet()) {
			
			try {
				Thread.sleep(10000); // NOTE: had to increase this from 1000 to 10000 - Mason
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			
		//String name = "k55k48z93ve740";
			
			String cid = cids.get(name);
			//System.out.println("cid with name: " + cid);
//			if (!cid.equals("k5hbqf88zka2ft")) continue;
			Map<String, String> grades = gradesList.get(name).getMapRepresentation();
			String date = LocalDate.now().getMonthValue() + "/" + LocalDate.now().getDayOfMonth();
			String post = "<p>Date: " + date + "<br>"
					+ "Grading period: " + generateGradingPeriod(grades) + "<br>"
					+ "My Q&A: " + grades.get(MY_QA_COUNT) + "*5 = " + grades.get(MY_QA_GRADE) + "<br>"
					//+ "Class Q&A: " + grades.get(CLASS_QA_COUNT) + "*5 = " + grades.get(CLASS_QA_GRADE) + "<br>"
					+ "Total diary grade up to " + date + " is: " + grades.get(TOTAL_GRADE) + "<br>"
					+ "If you have any questions regarding your grade, please talk to " + contactName;
			if (method == Method.FULL_REGRADE) {
				post += "<br>" + fullRegradeNote;
			}
			post += "</p>";
			
			System.out.println("ABOUT TO CREATE FOLLOWUP:");
			System.out.println("name: " + name);
			System.out.println(cid);
			System.out.println(post);
			//TODO:
			this.createFollowup(cid, post);
			System.out.println("FOLLOWUP CREATED FOR " + name);
			System.out.println();
		}
	}
	
	private String generateGradingPeriod(Map<String, String> grades) {
		if (grades.get(START_DATE).equals(NO_START_DATE)) { // There is no start date
			return "N/A - No new diary posts since last grading period";
		} else {
			return grades.get(START_DATE) + " - " + grades.get(END_DATE);
		}
	}
	
}
