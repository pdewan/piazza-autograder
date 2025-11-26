package piazza.nlp.redux.tools.programs;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.RFC4180Parser;

import piazza.nlp.AGPTClass;
import piazza.nlp.redux.general.DataStoreDiscussionForum;
import piazza.nlp.redux.general.DiscussionForum;
import piazza.nlp.redux.general.ForumPost;
import piazza.nlp.redux.general.DiscussionForum.EditorType;
import piazza.nlp.redux.general.ForumUser;
import piazza.nlp.redux.piazza.PiazzaForum;
import piazza.nlp.redux.tools.AGoogleFormQuizParser;
import piazza.nlp.redux.tools.programs.intakeCSV.ParsedScore;
import piazza.nlp.redux.tools.programs.intakeCSV.QAEntry;
import piazza.nlp.redux.tools.programs.intakeCSV.QCols;
import piazza.nlp.redux.tools.programs.intakeCSV.QuestionBlock;
import piazza.nlp.redux.general.ForumPost.PostType;
import piazza.nlp.redux.general.ForumPost.PostVisibility;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AQuizGradingProgram extends AnAbstractForumProgram implements ForumProgram {

	final private static String DEFAULT_FEEDBACK_POST_TEMPLATE = "Hi [STUDENT_NAME],\r\n\r\nThis post will be used for providing scores and feedback on your answers to questions on the Google Form quizzes. For each question, a followup to this post will be created containing the question, your answer, and an AI-generated score with feedback. **The scores and feedback may be incorrect, so if you would like to challenge them, please reply to the followup with an explanation of why your answer should be getting more points.** If we agree with your case, we will adjust the scores accordingly. In this way, we can explore the potential of AI-based grading while working together to catch errors and ensuring that a human instructor always has the final say.\r\n\r\nLet us know if you have any questions.\r\n\r\nBest,\r\nMason";
	final private static String INDIVIDUAL_FEEDBACK_INSTRUCTIONS = "See post @[RUBRIC_NUMBER] for the grading rubric. If you believe the AI system made a mistake or you disagree with its assigned score and feedback, please reply to this followup comment with an explanation of your case.";

	final private static String RUBRIC_CREATION_PROMPT = "You are an expert TA for an undergraduate Computer Science course tasked with constructing a structured grading rubric for the free-response QUESTION below. To assist with this task, you will be provided with an INSTRUCTOR ANSWER to the question composed by one of the course instructors, and the STUDENT ANSWERS that were given by students in the course.\r\n" + 
			"\r\n" + 
			"You may also be provided with an INITIAL RUBRIC written by the course instructors. The INITIAL RUBRIC was written for the entire assignment, and may or may not apply to this particular question. Only consider the contents of the INITIAL RUBRIC if they are relevant to the QUESTION below.\r\n" + 
			"\r\n" + 
			"You should output your answer in STRICT JSON matching the following schema:\r\n" + 
			"{\r\n" + 
			"  \"criteria\": [\r\n" + 
			"    {\"id\": \"C1\", \"description\": <holistic description of the core concept required >, \"points\": <number>},\r\n" + 
			"    {\"id\": \"C2\", \"description\": <holistic description of the core concept required >, \"points\": <number>}\r\n" + 
			"    // ...\r\n" + 
			"  ],\r\n" + 
			"  \"notes\": <optional brief guidance, 1-2 sentences>\r\n" + 
			"}\r\n" + 
			"\r\n" + 
			"Your criteria should be holistic and focus on the core concepts of the QUESTION. As long as the student demonstrates a fundamental understanding of the main idea (as shown in the INSTRUCTOR ANSWER), they should receive full points.\r\n" + 
			"\r\n" + 
			"Avoid creating criteria that deduct points for minor omissions or slightly incorrect terminology, as long as the core concept is correct. Aim to use fewer criteria (ideally one, two, or three) that capture the main goal of the question. For example, a single criterion like \"Demonstrates understanding of [Core Concept]\" for all the points may be sufficient for simpler questions that are designed to test students' understanding of a single idea.\r\n" + 
			"\r\n" + 
			"The rubric should be synthesized based on the core idea of the INSTRUCTOR ANSWER. You will be provided with the MAX POINTS for the QUESTION, and the sum of your criterion points MUST equal MAX POINTS.\r\n" + 
			"\r\n" + 
			"The QUESTION, MAX POINTS, INSTRUCTOR ANSWER, STUDENT ANSWERS, and INITIAL RUBRIC are below.\r\n" + 
			"\r\n" + 
			"QUESTION: [QUESTION_TEXT]\r\n" + 
			"\r\n" + 
			"MAX POINTS: [MAX_POINTS]\r\n" + 
			"\r\n" + 
			"INSTRUCTOR ANSWER: [INSTRUCTOR_ANSWER]\r\n" + 
			"\r\n" + 
			"STUDENT ANSWERS: [STUDENT_ANSWERS]\r\n" + 
			"\r\n" + 
			"INITIAL RUBRIC: [INITIAL_RUBRIC]\r\n" + 
			"\r\n" + 
			"Your RUBRIC in JSON:";
	final private static String EC_RUBRIC_CREATION_PROMPT = "You are an expert TA for an undergraduate Computer Science course tasked with constructing a structured grading rubric for the extra-credit free-response QUESTION below. To assist with this task, you will be provided with the STUDENT ANSWERS that were given by students in response to this question. Since this is an open-ended question for extra credit, students should be scored relative to their classmates. Thus, you should analyze the different STUDENT ANSWERS to determine what the most insightful and high-effort answers are. You should engineer your rubric such that the best answers receive full credit, while the rest of the answers receive partial credit on a relative scale.\r\n" + 
			"\r\n" + 
			"You may also be provided with an INITIAL RUBRIC written by the course instructors. The INITIAL RUBRIC was written for the entire assignment, and may or may not apply to this particular question. Only consider the contents of the INITIAL RUBRIC if they are relevant to the QUESTION below. \r\n" + 
			"\r\n" + 
			"You should output your answer in STRICT JSON matching the following schema:\r\n" + 
			"{\r\n" + 
			"  \"criteria\": [\r\n" + 
			"    {\"id\": \"C1\", \"description\": [brief, observable criterion], \"points\": <number>},\r\n" + 
			"    {\"id\": \"C2\", \"description\": [brief, observable criterion], \"points\": <number>}\r\n" + 
			"    // ...\r\n" + 
			"  ],\r\n" + 
			"  \"notes\": <optional brief guidance, 1-2 sentences>\r\n" + 
			"}\r\n" + 
			"\r\n" + 
			"Keep your descriptions short, specific, and observable. The rubric should be synthesized based on a combination of the INITIAL RUBRIC (if present), the STUDENT ANSWERS, and your own understanding. You will be provided with the MAX POINTS for the QUESTION, and the sum of your criterion points MUST equal MAX POINTS.\r\n" + 
			"\r\n" + 
			"The QUESTION, MAX POINTS, STUDENT ANSWERS, and INITIAL RUBRIC are below.\r\n" + 
			"\r\n" + 
			"QUESTION: [QUESTION_TEXT]\r\n" + 
			"\r\n" + 
			"MAX POINTS: [MAX_POINTS]\r\n" + 
			"\r\n" + 
			"STUDENT ANSWERS: [STUDENT_ANSWERS]\r\n" + 
			"\r\n" + 
			"INITIAL RUBRIC: [INITIAL_RUBRIC] \r\n" + 
			"\r\n" + 
			"Your RUBRIC in JSON:";
	final private static String QUESTION_GRADING_PROMPT = "You are an expert TA for an undergraduate Computer Science course tasked with grading the STUDENT ANSWER to the given free-response QUESTION using the attached RUBRIC. Your grade should consist of a numeric score between 0 and MAX POINTS as well as a short piece of written feedback. You are grading with a structured rubric, and must consider all of the criteria given in the rubric. Pay attention to the question-specific notes in the rubric.\r\n" + 
			"\r\n" + 
			"IMPORTANT GRADING PHILOSOPHY: Your primary goal is to assess if the STUDENT ANSWER demonstrates a holistic understanding of the core concept from the QUESTION. The RUBRIC criteria are a guide, but you should be lax and grade generously if the student's answer is conceptually correct, even if it doesn't perfectly match every specific detail in the rubric or uses different phrasing. Prioritize the student's main idea over minor omissions.\r\n" + 
			"\r\n" + 
			"You should output your answer in STRICT JSON matching the following schema:\r\n" + 
			"{\r\n" + 
			"     \"score\": <float>, \r\n" + 
			"     \"feedback\": \"1-2 lines\"\r\n" + 
			"}\r\n" + 
			"\r\n" + 
			"Note that 'score' MUST be between 0 and MAX POINTS. 'feedback' should reference the criterion IDs provided in the RUBRIC (e.g. C1, C2). You may award half credit for criteria that are partially fulfilled in the STUDENT ANSWER.\r\n" + 
			"\r\n" + 
			"The QUESTION, MAX POINTS, RUBRIC, and STUDENT ANSWER are below.\r\n" + 
			"\r\n" + 
			"QUESTION: [QUESTION_TEXT]\r\n" + 
			"\r\n" + 
			"MAX POINTS: [MAX_POINTS]\r\n" + 
			"\r\n" + 
			"RUBRIC: [RUBRIC_TEXT]\r\n" + 
			"\r\n" + 
			"STUDENT ANSWER: [STUDENT_ANSWER]\r\n" + 
			"\r\n" + 
			"Your GRADE in JSON:";
//	final private static String EC_QUESTION_GRADING_PROMPT = ""; // TODO
	
	protected AGPTClass gpt; // TODO: support for other LLMs
	protected AGoogleFormQuizParser quizParser;
	
	public AQuizGradingProgram(String name, String description) {
		super(name, description);
		
		String apiKey = System.getenv("OPENAI_API_KEY");
		String defaultModel = System.getenv("DEFAULT_GPT_MODEL");			
		this.gpt = new AGPTClass(apiKey, defaultModel);
		this.quizParser = new AGoogleFormQuizParser();
	}
	
	// add links to the rubric creation prompts and question grading prompt in the data store
	public void setUp(DataStoreDiscussionForum dataStoreForum) {
		
		DiscussionForum forum = dataStoreForum.getForum();
		List<String> promptPostTags = new ArrayList<String>();
		promptPostTags.add("automated");
		promptPostTags.add("agent_data");
		
		// add default rubric creation prompt to data store
		if (dataStoreForum.isRegistered("quizRubricCreationPromptID")) {
			String quizRubricCreationPromptID = (String) dataStoreForum.getDataValue("quizRubricCreationPromptID");
			forum.updatePost(quizRubricCreationPromptID, "Quiz Rubric Creation Prompt", RUBRIC_CREATION_PROMPT, PostType.NOTE, PostVisibility.PRIVATE, promptPostTags, EditorType.PLAIN_TEXT);
		} else {
			String quizRubricCreationPromptID = forum.createPost("Quiz Rubric Creation Prompt", RUBRIC_CREATION_PROMPT, PostType.NOTE, PostVisibility.PRIVATE, promptPostTags, EditorType.PLAIN_TEXT);
			dataStoreForum.registerData("quizRubricCreationPromptID", String.class, quizRubricCreationPromptID);
		}
		
		// add extra credit rubric creation prompt to data store
		if (dataStoreForum.isRegistered("quizECRubricCreationPromptID")) {
			String quizECRubricCreationPromptID = (String) dataStoreForum.getDataValue("quizECRubricCreationPromptID");
			forum.updatePost(quizECRubricCreationPromptID, "Quiz EC Rubric Creation Prompt", EC_RUBRIC_CREATION_PROMPT, PostType.NOTE, PostVisibility.PRIVATE, promptPostTags, EditorType.PLAIN_TEXT);
		} else {
			String quizECRubricCreationPromptID = forum.createPost("Quiz EC Rubric Creation Prompt", EC_RUBRIC_CREATION_PROMPT, PostType.NOTE, PostVisibility.PRIVATE, promptPostTags, EditorType.PLAIN_TEXT);
			dataStoreForum.registerData("quizECRubricCreationPromptID", String.class, quizECRubricCreationPromptID);
		}
		
		// add question grading prompt to data store
		if (dataStoreForum.isRegistered("quizQuestionGradingPromptID")) {
			String quizQuestionGradingPromptID = (String) dataStoreForum.getDataValue("quizQuestionGradingPromptID");
			forum.updatePost(quizQuestionGradingPromptID, "Quiz Question Grading Prompt", QUESTION_GRADING_PROMPT, PostType.NOTE, PostVisibility.PRIVATE, promptPostTags, EditorType.PLAIN_TEXT);
		} else {
			String quizQuestionGradingPromptID = forum.createPost("Quiz Question Grading Prompt", QUESTION_GRADING_PROMPT, PostType.NOTE, PostVisibility.PRIVATE, promptPostTags, EditorType.PLAIN_TEXT);
			dataStoreForum.registerData("quizQuestionGradingPromptID", String.class, quizQuestionGradingPromptID);
		}
		
	}
	
	
	
	// TODO: delete
	public void testbed(DataStoreDiscussionForum dataStoreForum) {
		
//		System.out.println(this.retrieveStudentIDsFromEmails(dataStoreForum.getForum(), "unc.edu"));
		
//		this.initializeIndividualFeedbackPosts(dataStoreForum, false);
//		System.out.println(dataStoreForum.getDataValue("individualFeedbackPosts"));
//		createEnhancedRubric(dataStoreForum, "File-Systems", "", "");
		
//		String followupTest = formatFeedback("File-Systems", 1, "This is true implicitly, as file separators are added between directories in the absolute name.", 5, 5, "This would be feedback!", 17);
//		
//		System.out.println(followupTest);
//		
//		Map<String, Object> parsedTest = parseFeedback(followupTest);
//		
//		System.out.println(parsedTest);
//		System.out.println(parsedTest.get("Your Answer"));
//		System.out.println();
		
//		DiscussionForum forum = dataStoreForum.getForum();
//		String fileSystemRubricID = fetchRubric(forum, "Discovery1");
//		System.out.println(forum.getPost(fileSystemRubricID).getBody());
		
//		this.parseCSV("test_files\\File-Systems.csv");
		
//		this.createEnhancedRubric( dataStoreForum, "File-Systems", "grades\\hw1\\quizzes\\File-Systems.csv", "grades\\hw1\\solutions\\File-Systems Solutions.csv");
		
		
		// "HW3 Caching", "grades\\hw3\\quizzes\\Caching.csv", "grades\\hw3\\solutions\\Caching Quiz Solutions.csv"
		JSONObject quizSubmissions = new JSONObject();
		try {
			quizSubmissions = quizParser.readQuizGrades("grades\\hw3\\quizzes\\Caching.csv", false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println(quizSubmissions);
		
		
	}
	
	
	// TODO: required tags: "individual_feedback", "automated_grading_rubric"

	// TODO: QuizGradingProgram INTERFACE

	
	
	
	
	
	/* STEP 0 (automated): create/collect individual feedback posts and add them to the data store (only needs to happen once per course) */
	// TODO: instead of parameterized, make this detect whether student-created posts exist?
	// TODO: method to create individual posts for people who join the class late? would need to store it in data store as well
	public void initializeIndividualFeedbackPosts(DataStoreDiscussionForum dataStoreForum, boolean postsAlreadyExist) {
		
		// if posts are already registered in the data store, do nothing
		if (dataStoreForum.isRegistered("individualFeedbackPosts")) {
			return;
		}
		
		// create a map to hold each student's individual feedback post
		Map<String, String> individualFeedbackPosts = new HashMap<String, String>();
		DiscussionForum forum = dataStoreForum.getForum();
		
		// if individual feedback posts were manually created by students, identify them
		if (postsAlreadyExist) {
			
			/*
			
			TODO
		
			 */
			
		}
		
		// otherwise, create the individaul feedback posts automatically
		// NOTE: not all forum platforms support this functionality
		else {
			
			String feedbackPostSubject;
			String feedbackPostBody;
			List<String> feedbackPostTags = new ArrayList();
			feedbackPostTags.add("individual_feedback");
			
			// for each student, create a private post with the feedback instructions
			for (ForumUser u : forum.getAllUsers()) {
				if (!u.getAdmin()) {
					
					feedbackPostSubject = "Quiz Feedback - " + u.getName();
					feedbackPostBody = DEFAULT_FEEDBACK_POST_TEMPLATE.replace("[STUDENT_NAME]", u.getName()).replace("[INSTRUCTOR_NAME", "INSTRUCTOR_GOES_HERE");
					
					// TODO: find a way to do this that's not hard-coded based on Piazza
						// maybe an interface for individual private post functionality?
				
					String studentID = u.getID();
					String feedbackPostID = ((PiazzaForum) forum).createIndividualPost(feedbackPostSubject, feedbackPostBody, PostType.NOTE, studentID, feedbackPostTags, EditorType.MARKDOWN);
					individualFeedbackPosts.put(studentID, feedbackPostID);
					
				}
			}
			
		}
		
		// add feedback posts to the data store
		dataStoreForum.registerData("individualFeedbackPosts", HashMap.class, individualFeedbackPosts);
		
	}
	
	
	/* STEP 0.5 (manual): post an initial rubric to Piazza */
	
	
	/* STEP 1 (automated): create an LLM-enhanced version of our rubric */
	public String createEnhancedRubric(DataStoreDiscussionForum dataStoreForum, String quizIdentifier, String responsesFilepath, String solutionsFilepath, boolean parseIDFromEmail) {
		
		// determine if an initial human-created rubric exists
		DiscussionForum forum = dataStoreForum.getForum();
		String rubricID = fetchRubric(forum, quizIdentifier);
		
		// if no initial rubric exists, create one
		List<String> rubricTags = new ArrayList();
		rubricTags.add("automated_grading_rubric");
		if (rubricID == null) {
			rubricID = forum.createPost(quizIdentifier + " Quiz Rubric", "No initial rubric specified.", PostType.NOTE, PostVisibility.PRIVATE, rubricTags, EditorType.MARKDOWN);
		}
		
		// fetch the rubric creation prompt templates and initial rubric
		String rubricCreationPromptID = (String) dataStoreForum.getDataValue("quizRubricCreationPromptID");
		String rubricCreationPrompt = forum.getPost(rubricCreationPromptID).getBody();
		String ecRubricCreationPromptID = (String) dataStoreForum.getDataValue("quizECRubricCreationPromptID");
		String ecRubricCreationPrompt = forum.getPost(ecRubricCreationPromptID).getBody();
		String initialRubric = forum.getPost(rubricID).getBody();

		// fetch the instructor response
		JSONObject instructorSolutions = new JSONObject();
		try {
			instructorSolutions = quizParser.readInstructorSolutions(solutionsFilepath); // TODO: better exception handling?
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// fetch the student responses
		JSONObject quizSubmissions = new JSONObject();
		try {
			quizSubmissions = quizParser.readQuizGrades(responsesFilepath, parseIDFromEmail); // TODO: better exception handling?
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// construct a rubric for each question in the instructor solution list
		JSONObject rubricJSON = new JSONObject();
		
		int questionNumber = 1; // TODO: this number is meaningless because keySet does not keep the original order.
		
//		System.out.println(quizSubmissions.keySet());
//		System.exit(0);
//		
		System.out.println(instructorSolutions.keySet());
		
		for (String question : quizSubmissions.keySet()) {
		
			JSONObject questionData = quizSubmissions.getJSONObject(question);
			JSONObject studentSubmissions = questionData.getJSONObject("studentSubmissions");
			Integer maxScore = questionData.getInt("maxScore");
			String questionType = questionData.getString("questionType");
			boolean extraCredit = questionData.getBoolean("extraCredit");
			
//			System.out.println(question);
			
			// if the question is listed in the instructor solutions, construct a rubric for it
			String questionKey = question.split(" -- Explain your")[0].split(" -- Justify your")[0].strip();
			if (instructorSolutions.has(questionKey)) {
				
			// if the question has not been auto-graded by Google Forms, construct a rubric for it
			// TODO: do we ever need to have it look at other types of questions?
//			if (questionType.equals("FreeResponse")) {
				
				// create a list of student answers
				List<String> studentAnswers = new ArrayList<String>();
				for (String user : studentSubmissions.keySet()) {
					String answer = studentSubmissions.getJSONObject(user).getString("answer"); // also 'feedback' and 'score'
					studentAnswers.add(answer);
				}
				
				String filledPrompt;
				
				// if the question is extra credit, use an alternate prompt
				if (extraCredit) {

					filledPrompt = ecRubricCreationPrompt
							.replace("[QUESTION_TEXT]", question)
							.replace("[MAX_POINTS]", String.valueOf(maxScore))
							.replace("[STUDENT_ANSWERS]", studentAnswers.toString())
							.replace("[INITIAL_RUBRIC]", initialRubric);
					
				}
				
				// otherwise, use the standard prompt
				else {
					
					//String questionKey = question.split(" -- Explain your")[0].strip();
					
//					System.out.println();
//					System.out.println("questionKey: " + questionKey);
//					System.out.println("instructorSolutions keys: " + instructorSolutions.keySet());
//					System.out.println();
					
					filledPrompt = rubricCreationPrompt
							.replace("[QUESTION_TEXT]", question)
							.replace("[MAX_POINTS]", String.valueOf(maxScore))
							.replace("[INSTRUCTOR_ANSWER]", instructorSolutions.getString(questionKey))
							.replace("[STUDENT_ANSWERS]", studentAnswers.toString())
							.replace("[INITIAL_RUBRIC]", initialRubric);
					
				}
				
				// enhance the rubric using an LLM
				String enhancedRubric = null;
				try {
					
					// TODO: look into how to do the proper JSON stuff: https://platform.openai.com/docs/guides/structured-outputs?format=without-parse
					
					enhancedRubric = gpt.makeCallWithBackoff(filledPrompt, false);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				System.out.println(enhancedRubric);
				
				// add the enhanced rubric to the JSON rubric for the whole quiz
				JSONObject questionEntry = new JSONObject(enhancedRubric);
				questionEntry.put("question number", questionNumber);
				questionEntry.put("max score", maxScore);
				rubricJSON.put(question, questionEntry);
				
			}
			
			questionNumber++;
			
		}
		
		// save the enhanced rubric JSON to Piazza
		// TODO: figure out a way to actually pretty-print this? the Piazza API flattens it out so there's no indentation.
			// potentially wrap each indented line with <p style="padding-left: 40px;"></p> in plain text mode
		forum.updatePost(rubricID, quizIdentifier + " Quiz Rubric", rubricJSON.toString(4), PostType.NOTE, PostVisibility.PRIVATE, rubricTags, EditorType.PLAIN_TEXT);
		
		// return the ID of the created/identified rubric post
		return rubricID;
		
	}
	
	
	/* STEP 1.5 (manual): check over enhanced rubric on Piazza, make changes if necessary */
	
	
	/* STEP 2 (automated): generate feedback to students' responses and post privately */
	public void createPrivateFeedback(DataStoreDiscussionForum dataStoreForum, String quizIdentifier, String responsesFilepath, boolean parseIDFromEmail) {
		
		// fetch the student responses
		JSONObject quizSubmissions = new JSONObject();
		try {
			quizSubmissions = quizParser.readQuizGrades(responsesFilepath, parseIDFromEmail);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// fetch the enhanced rubric
		DiscussionForum forum = dataStoreForum.getForum();
		String rubricID = fetchRubric(forum, quizIdentifier);
		ForumPost rubricPost = forum.getPost(rubricID);
		int rubricPostNumber = rubricPost.getPostNumber();
		
//		System.out.println(rubricPost.getBody());
	
		JSONObject quizRubric = new JSONObject(StringEscapeUtils.unescapeHtml4(rubricPost.getBody()));
		
		// fetch the rubric creation prompt templates and initial rubric
		String quizQuestionGradingPromptID = (String) dataStoreForum.getDataValue("quizQuestionGradingPromptID");
		String questionGradingPrompt = forum.getPost(quizQuestionGradingPromptID).getBody();
		
		// fetch the individual feedback posts
		Map<String, String> individualFeedbackPostIDs = (Map<String, String>) dataStoreForum.getDataValue("individualFeedbackPosts");
		
		// iterate through each student in the Piazza course
		Map<String, String> studentIDMap = retrieveStudentIDsFromEmails(forum, "unc.edu");
		for (String studentID : studentIDMap.keySet()) {
			
//			System.out.println("STUDENT ID: " + studentID);
			
			String individualFeedbackPostID = individualFeedbackPostIDs.get(studentID);
			String studentUniversityID = studentIDMap.get(studentID);
			
			// iterate through each question in the quiz
			for (String question : quizRubric.keySet()) {
				
				
				// TODO: iterate through these in order?
				System.out.println("Question: " + question);
				
				System.out.println(quizSubmissions.keySet());
				System.out.println();
				System.out.println();
				System.out.println();
				
				
				// get the rubric for this specific question
				JSONObject questionRubric = quizRubric.getJSONObject(question);
				
				// determine the question type
				JSONObject questionData = quizSubmissions.getJSONObject(question);
				String questionType = questionData.getString("questionType");
				
				// if the student has no answer for this question, skip it
				JSONObject studentSubmissions = questionData.getJSONObject("studentSubmissions");
				if (!studentSubmissions.has(studentUniversityID))
					continue;
				
				// otherwise, get the student's answer
				String answer = studentSubmissions.getJSONObject(studentUniversityID).getString("answer");
				String score = studentSubmissions.getJSONObject(studentUniversityID).getString("score");
				
				System.out.println(question);
				System.out.println(score);
				System.out.println();
				
				// if the question has not been auto-graded by Google Forms, grade it using the LLM
//				if (questionType.equals("FreeResponse")) { // TODO: do we ever need to have it look at other types of questions?
				if (score.equals("--")) {			
					
					// get the question info
					int questionNumber = questionRubric.getInt("question number");
					double maxScore = questionRubric.getInt("max score");
					
					// add relevant info to the prompt
					String filledPrompt = questionGradingPrompt
							.replace("[RUBRIC_TEXT]", questionRubric.toString())
							.replace("[STUDENT_ANSWER]", answer);
					
					// grade using the LLM
					String response = null;
					try {
						response = gpt.makeCallWithBackoff(filledPrompt, false);
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					// get the score and feedback from the LLM response
					JSONObject llmGrade = new JSONObject(response);
					double assignedScore = llmGrade.getDouble("score");
					String answerFeedback = llmGrade.getString("feedback");
					
					// create instructor-only followup with feedback
					String individualFeedback = formatIndividualFeedback(quizIdentifier, questionNumber, question, answer, assignedScore, maxScore, answerFeedback, rubricPostNumber);
					forum.createFollowup(individualFeedbackPostID, individualFeedback, EditorType.MARKDOWN, true);
				
					// TODO: seems to work but errors out after all are done sayinf "Error message: Missing content id"
					
				}
			}
		}
	}
	
	
	/* STEP 2.5 (manual): check over LLM feedback on Piazza, make changes if necessary */
	
	
	/* STEP 3 (automated): post feedback publicly to Piazza, make rubric post visible to students */
	public void makeFeedbackVisible(DataStoreDiscussionForum dataStoreForum, String quizIdentifier) {
		
		// fetch the feedback posts for each student
		DiscussionForum forum = dataStoreForum.getForum();
		Map<String, String> individualFeedbackPostIDs = (Map<String, String>) dataStoreForum.getDataValue("individualFeedbackPosts");

		for (String studentID : individualFeedbackPostIDs.keySet()) {
			
			ForumPost individualFeedbackPost = forum.getPost(individualFeedbackPostIDs.get(studentID));
			Map<String, Object> individualFeedbackPostData = individualFeedbackPost.getAllData();
			List<Map<String, Object>> postChildren = (List<Map<String, Object>>) individualFeedbackPostData.get("children");
			
			// look through all the children of the individual feedback post
			for (Map<String, Object> c : postChildren) {
				
				Map<String, Object> config = (Map<String, Object>) c.getOrDefault("config", new HashMap());
				if (c.get("type").equals("followup") && (Boolean) config.getOrDefault("ionly", false)) {
					
					// get the text of the followup
					String currentContent = null;
					if (c.containsKey("history")) {
						List<Map<String, Object>> childHistory = (List<Map<String, Object>>) c.get("history");
						currentContent = (String) childHistory.get(childHistory.size()-1).get("content");
					} else if (c.containsKey("subject")) {
						currentContent = (String) c.get("subject");
					}
					
					// get the ID of the followup
					String followupID = null;
					if (c.containsKey("cid")) {
						followupID = (String) c.get("cid");
					} else if (c.containsKey("id")) {
						followupID = (String) c.get("id");
					}
					
					// if the followup corresponds to this quiz, copy the body into a public followup and delete the private one
					if (currentContent.contains("**Quiz:** " + quizIdentifier) || currentContent.contains("Quiz: " + quizIdentifier)) {
						forum.createFollowup(individualFeedbackPost.getPostID(), currentContent, EditorType.MARKDOWN, false); // MARKDOWN?
						forum.deletePost(followupID);
					}
				
				}
			}
			
		}
		
		// change the rubric visibility from private to public
		String rubricID = fetchRubric(forum, quizIdentifier);
		ForumPost rubric = forum.getPost(rubricID);
		forum.updatePost(rubricID, rubric.getSubject(), rubric.getBody(), PostType.NOTE, PostVisibility.PUBLIC, rubric.getTags(), EditorType.MARKDOWN);
		
	}
	
	/* STEP 3.5 (manual): students view the feedback and can argue it, instructors can modify scores if necessary */
	
	
	/* STEP 4 (automated): retrieve the (potentially modified) feedback scores from Piazza, export as CSV in Canvas format */
	/*		also exports a log of followup interactions, and deletes the feedback followups (if desired) */
	// TODO: parameter for spreadsheet of responses
//	public String exportGrades(DataStoreDiscussionForum dataStoreForum, String quizIdentifier, Map<Date, Double> submissionDateMultipliers) {
		
		// if no map of due dates and score multipliers is provided, create an empty map
//		if (submissionDateMultipliers == null) {
//			submissionDateMultipliers = new HashMap<Date, Double>();
//		}
		//date parameter, if present then will add other row to the spreadsheet, otherwise will keep the same as is
		
		
		
		
		
		// TODO: dead with submissionMultipliers
		
		
		
		// iterate through all of the questions in the spreadsheet
		
		// if there is a feedback post for a question, use that
		
		// otherwise, use what was in the spreadsheet
		
		// make sure to add up for total column
		
		
		//deleteFollowupsAfterExporting
		
		
		
		// TODO: also export spreadsheet in CLEANED formatting
		
		/*
		
		TODO:
		
			make an empty map of scores
			
			make an empty log of followup interactions

			for each student:
			
				get their individual feedback post
				
				for each question:
					
					find followup with the corresponding identifier
					
					extract (potentially updated) score from followup
					
					store this score in the map
					
					store followup text in log
					
					delete followup
		
			export scores as csv file in Canvas format
				including non-essay question scores from spreadsheet
			
			export log of the followup interactions that occurred
		
		*/
		
//		return null; // TODO: return path of exported file (or directory?)
//		
//	}
	
	
	
	/*
	{anon=no, folders=[], data=null, no_upvotes=0, subject=Quiz: HW3 Caching
	Question Text: 12. In the absolute name problem, explain the reasoning for giving preference to cache entries for nodes at higher-levels in the tree. Let us call this the Higher-Level policy.
	Your Answer: The higher-level nodes have the most descendants. If we assume that all nodes are equally likely to be accessed, the nodes with the most descendants provide the most opportunities to speed up accesses by using their cached values.
	AI Score: 5.0/5.0
	AI Feedback: Strong explanation that higher-level nodes have more descendants, so their caches benefit many lookups; noting equal-likelihood supports the argument (C1). Well done.
	---
	See post @255 for the grading rubric. If you believe the AI system made a mistake or you disagree with its assigned score and feedback, please reply to this followup comment with an explanation of your case., created=2025-11-12T22:16:10Z, bucket_order=3, bucket_name=Today, type=followup, tag_good=[], uid=lljvnbpqdze3xm, children=[{anon=no, folders=[], data=null, subject=I DISAGREE WITH THIS!!, created=2025-11-18T20:04:10Z, bucket_order=3, bucket_name=Today, type=feedback, tag_good=[], uid=lljvnbpqdze3xm, children=[], tag_good_arr=[], id=mi504v6gogg5x6, updated=2025-11-18T20:04:10Z, config={editor=plain, ionly=true}}, {anon=no, folders=[], data=null, subject=why do you disagree?, created=2025-11-18T20:04:17Z, bucket_order=3, bucket_name=Today, type=feedback, tag_good=[], uid=lljvnbpqdze3xm, children=[], tag_good_arr=[], id=mi50516auqe692, updated=2025-11-18T20:04:17Z, config={editor=plain, ionly=true}}], tag_good_arr=[], no_answer=1, id=mhwk7io4rt456y, updated=2025-11-18T20:04:17Z, config={editor=md, ionly=true}}

	 */
	
	// children=[{anon=no, folders=[], data=null, subject=I DISAGREE WITH THIS!!, created=2025-11-18T20:04:10Z, bucket_order=3, bucket_name=Today, type=feedback, tag_good=[], uid=lljvnbpqdze3xm, children=[], tag_good_arr=[], id=mi504v6gogg5x6, updated=2025-11-18T20:04:10Z, config={editor=plain, ionly=true}}, {anon=no, folders=[], data=null, subject=why do you disagree?, created=2025-11-18T20:04:17Z, bucket_order=3, bucket_name=Today, type=feedback, tag_good=[], uid=lljvnbpqdze3xm, children=[], tag_good_arr=[], id=mi50516auqe692, updated=2025-11-18T20:04:17Z, config={editor=plain, ionly=true}}]
	
	
	
	// TODO: create parseIDFromEmail version, or just scrap that altogether?
	
	
	
	/* STEP 4 (automated): retrieve the (potentially modified) feedback scores from Piazza, export as CSV in Google Forms format */
	/*		also exports a log of followup interactions, and optionally deletes the feedback followups */
	public String exportGradedSubmissions(DataStoreDiscussionForum dataStoreForum, String quizIdentifier, String responsesFilepath, String exportDirectory, boolean deleteFollowupsAfterExporting) {
		
		// create an empty JSON object for the log file
		String logFilename = quizIdentifier + "_followup_log.json";
		JSONObject followupLog = new JSONObject();
		
		// create a map for the grade updates
		Map<String, Map<String, Object>> allStudentGradingData = new HashMap<>();
		
		// fetch the feedback posts for each student
		DiscussionForum forum = dataStoreForum.getForum();
		Map<String, String> individualFeedbackPostIDs = (Map<String, String>) dataStoreForum.getDataValue("individualFeedbackPosts");
		Map<String, String> studentIDMap = retrieveStudentIDsFromEmails(forum, "unc.edu");
		
		for (String studentID : individualFeedbackPostIDs.keySet()) {
			
			// fetch the student's feedback
			String studentUniversityID = studentIDMap.get(studentID);
			ForumPost individualFeedbackPost = forum.getPost(individualFeedbackPostIDs.get(studentID));
			Map<String, Object> individualFeedbackPostData = individualFeedbackPost.getAllData();
			List<Map<String, Object>> postChildren = (List<Map<String, Object>>) individualFeedbackPostData.get("children");
			
			// initialize an array of entries for the log
			JSONArray studentEntries = new JSONArray();
			followupLog.put(studentUniversityID, studentEntries);
			
			// look through all the children of the individual feedback post
			for (Map<String, Object> c : postChildren) {
				
				if (c.get("type").equals("followup")) {
					
					// get the text of the followup
					String currentContent = null;
					if (c.containsKey("history")) {
						List<Map<String, Object>> childHistory = (List<Map<String, Object>>) c.get("history");
						currentContent = (String) childHistory.get(childHistory.size()-1).get("content");
					} else if (c.containsKey("subject")) {
						currentContent = (String) c.get("subject");
					}
					
					// if the followup corresponds to this quiz, add it to the log and delete it
					if (currentContent.contains("**Quiz:** " + quizIdentifier) || currentContent.contains("Quiz: " + quizIdentifier)) {
						
						// collect the followup information to be logged
						Map<String, Object> followupData = parseIndividualFeedback(currentContent);
						JSONObject logEntry = new JSONObject(followupData);
						logEntry.put("User", forum.getUser((String) c.get("uid")).getName());
						logEntry.put("Last Updated", c.get("updated"));
						
						// add any replies to the log
						List<Map<String, Object>> followupChilren = (List<Map<String, Object>>) c.get("children");
						JSONArray replies = new JSONArray();
						for (Map<String, Object> r : followupChilren) {
							JSONObject reply = new JSONObject();
							reply.put("User", forum.getUser((String) r.get("uid")).getName());
							reply.put("Text", r.get("subject"));
							reply.put("Last Updated", r.get("updated"));
							replies.put(reply);
						}
						logEntry.put("Replies", replies);
						
						// update the log object and write it to the log file
						studentEntries.put(logEntry);
						followupLog.put(studentUniversityID, studentEntries);
						try (FileWriter file = new FileWriter(exportDirectory + "/" + logFilename)) {
						    file.write(followupLog.toString(4)); // Use toString(4) for pretty printing (indentation of 4)
						    file.flush();
						} catch (IOException e) {
							System.err.println("Error writing to log file: " + e.getMessage());
						}			
						
						// extract grading info from the followup
						System.out.println("\nFOLLOWUP: " + followupData);
						double score = (double) followupData.getOrDefault("Instructor Score", followupData.get("AI Score"));
						String feedback = (String) followupData.getOrDefault("Instructor Feedback", followupData.get("AI Feedback"));
						String questionText = (String) followupData.get("Question Text");
						Map<String, Object> grades = new HashMap<>();
						grades.put("Score", score);
						grades.put("Feedback", feedback);
						
						// add grading info to the main map (creating inner maps if needed)
						allStudentGradingData
							.computeIfAbsent(studentUniversityID, k -> new HashMap<>())
							.put(questionText, grades);
						
						// if specified, delete the feedback followup
						if (deleteFollowupsAfterExporting) {
							
							// get the ID of the followup
							String followupID = c.containsKey("cid") ? (String) c.get("cid") : (String) c.get("id");
							forum.deletePost(followupID);
			
						}

					}
				}
			}

		}
		
		// export graded submissions
		String exportFilename = exportDirectory + "/" + quizIdentifier + "_graded_responses.csv";
		AGoogleFormQuizParser.updateGradesCSV(responsesFilepath, exportFilename, "Onyen", allStudentGradingData);
		return exportFilename;

	}
	
		
	// STEP 4.25 (automated): given a (graded) spreadsheet of submissions, creates a spreadsheet in the Canvas gradebook import format, and a "cleaned" spreadsheet of grades for easy analysis
	public String convertSubmissionsToGradebook(String quizIdentifier, String gradedResponsesFilepath, Map<Date, Double> submissionDateMultipliers, boolean parseIDFromEmail) {
	
		
		// extra credit
		
		
		// TODO: do cleaned spreadsheet combined for all quizzes in an assignment?
		
		
		
		return null;
		
	}
	
	
	
	
	
	
	
	/* STEP 4.5 (manual): upload the exported scores to Canvas */
	
	
	
	/* HELPER CLASSES */
	
//    public static class QAEntry {
//        public final String answer;
//        public final String score;   // keep as string to preserve "--" or partials
//        public final String feedback;
//        public QAEntry(String a, String s, String f) {
//            this.answer = a; this.score = s; this.feedback = f;
//        }
//    }
//
//    public static class QuestionBlock {
//        public String questionType; // "MCQ" or "FreeResponse"
//        public Double maxScore;     // lifted to question level
//        public final Map<String, QAEntry> studentSubmissions = new LinkedHashMap<>();
//    }
//
//    static class QCols {
//        final String key; final int ans, score, fb;
//        QCols(String key, int ans, int score, int fb) { this.key = key; this.ans = ans; this.score = score; this.fb = fb; }
//    }
//    
//    private static class ParsedScore {
//        final String leftScore; // as shown to the grader (e.g., "--" or "3")
//        final Double maxScore;  // numeric, lifted to question level
//        ParsedScore(String leftScore, Double maxScore) {
//            this.leftScore = leftScore;
//            this.maxScore = maxScore;
//        }
//    }
    
    
    
    /* HELPER METHODS */
    
	// find the rubric for a given quiz and return the post ID
	protected String fetchRubric(DiscussionForum forum, String quizIdentifier) {
		
		List<ForumPost> searchResults = forum.searchPosts(quizIdentifier + " Quiz Rubric");
		for (ForumPost p : searchResults) {
			
			String postTitle = p.getSubject();
			List<String> postTags = p.getTags();
			
			if (postTitle.contains(quizIdentifier) && postTags.contains("automated_grading_rubric")) {
				return p.getPostID();
			}
			
		}
		
		return null;
		
	}
	
	// find the corresponding university ID for each student in the Piazza course
	protected Map<String, String> retrieveStudentIDsFromEmails(DiscussionForum forum, String universityDomain) {
		
		Map<String, String> studentIDMap = new HashMap<String, String>();
		
		for (ForumUser u : forum.getAllUsers()) {
			List<String> emails = u.getEmails();
			for (String e : emails) {
				if (e.contains(universityDomain) && !u.getAdmin()) {
					String studentUniversityID = e.split("@")[0];
					studentIDMap.put(u.getID(), studentUniversityID);
					break;					
				}
			}
		}
		
//		System.out.print(studentIDMap);
		
		return studentIDMap;
		
	}
	
	// format feedback for a student into text for a followup
	protected String formatIndividualFeedback(String quizID, int questionID, String questionText, String studentAnswer, double assignedScore, double maxScore, String answerFeedback, int rubricPostNumber) {
				
		/*
	
		TODO: INCORPERATE TEXT OF CORRESPONDING PRIOR QUESTION

		*/
		
		int maxAnswerLength = 4096;
		if (studentAnswer.length() > maxAnswerLength) {
			studentAnswer = studentAnswer.substring(0, maxAnswerLength);
		}
		
		String followupText =
			"Quiz: " + quizID + "\n"
//			+ "Question Number: " + questionID + "\n"
			+ "Question Text: " + questionText + "\n"
			+ "Your Answer: " + studentAnswer + "\n"
			+ "AI Score: " + assignedScore + "/" + maxScore + "\n"
			+ "AI Feedback: " + answerFeedback + "\n---\n"
			+ INDIVIDUAL_FEEDBACK_INSTRUCTIONS.replace("[RUBRIC_NUMBER]", String.valueOf(rubricPostNumber));
		
		return followupText;
		
	}
	
	
	
	// given followup text, parse out the individual feedback items
	protected Map<String, Object> parseIndividualFeedback(String followupText) {
		
		// trim instructions from the end
		followupText = followupText.split("\\n---\\nSee post")[0];
		
		// define the possible keys
		Map<String, Object> items = new HashMap<>();
		List<String> keys = new ArrayList<>(Arrays.asList("Quiz", "Question Number", "Question Text", "Your Answer", "AI Score", "AI Feedback", "Instructor Score", "Instructor Feedback"));
		
		// iterate through the list in reverse order, parsing out the keys
		// TODO: make this more robust so the order doesn't matter
		Collections.reverse(keys);
		for (String k : keys) {
			if (followupText.contains(k + ":")) {
			
				String[] splitText = followupText.split(k + ":");
				String key = k;
				String value = splitText[1].trim();
				followupText = splitText[0];
				
				if (key.equals("AI Score") || key.equals("Instructor Score"))
            		items.put(key, Double.valueOf(value.split("/")[0]));
            	else if (key.equals("Question Number"))
            		items.put(key, Integer.valueOf(value));            		
            	else
            		items.put(key, value);
				
			}
		}
		
		return items;
			
	}
	
	
	
	
	
	// ---------------------------------------------------
	
	
	// TODO: clean this up
//	protected void parseCSV(String filepath) {
//		
//		try (Reader reader = new FileReader(filepath);
//			@SuppressWarnings("deprecation")
//			CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {
//			
//			for (CSVRecord csvRecord : csvParser) {
//				
//				System.out.println("\n\n\n\n");
//				Map<String, String> recordMap = csvRecord.toMap();
//				System.out.println(recordMap.keySet());
////				System.out.println(recordMap.get("Username"));
//				
//				
////				for (String v : csvRecord.toList()) {
////					System.out.println(v);
////				}
//				
////				String timestamp = csvRecord.get("Timestamp");
////				String username = csvRecord.get("Username");
////				String totalScore = csvRecord.get("Total score");
////				String anonymousID = csvRecord.get("Anonymous ID");
////				
////				// TODO: make this work for all quizzes
////				
////				// test prints
////				System.out.println("Timestamp: " + timestamp);
////				System.out.println("Username: " + username);
////				System.out.println("Total Score: " + totalScore);
////				System.out.println("Anonymous ID: " + anonymousID);	
//				
////				System.out.println("Record values: " + csvRecord.toList());
//				
//			}
//			
//			
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//	}

	// try to get them to mark as resolved if they think it looks good
	
	// current behavior: only emails that get set is from instructor to student when they reply to reply
		// currently not sending email when followup gets made		
	
}
