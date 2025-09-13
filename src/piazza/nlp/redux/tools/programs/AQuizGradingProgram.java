package piazza.nlp.redux.tools.programs;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import piazza.nlp.AGPTClass;
import piazza.nlp.redux.general.DataStoreDiscussionForum;
import piazza.nlp.redux.general.DiscussionForum;
import piazza.nlp.redux.general.ForumPost;
import piazza.nlp.redux.general.DiscussionForum.EditorType;
import piazza.nlp.redux.general.ForumUser;
import piazza.nlp.redux.piazza.PiazzaForum;
import piazza.nlp.redux.general.ForumPost.PostType;
import piazza.nlp.redux.general.ForumPost.PostVisibility;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AQuizGradingProgram extends AnAbstractForumProgram implements ForumProgram {

	final private static String DEFAULT_FEEDBACK_POST_TEMPLATE = "Hi [STUDENT_NAME],\r\n\r\nThis post will be used for providing scores and feedback on your answers to questions on the Google Form quizzes. For each question, a followup to this post will be created containing the question, your answer, and an AI-generated score with feedback. **The scores and feedback may be incorrect, so if you would like to challenge them, please reply to the followup with an explanation of why your answer should be getting more points.** If we agree with your case, we will adjust the scores accordingly. In this way, we can explore the potential of AI-based grading while working together to catch errors and ensuring that a human instructor always has the final say.\r\n\r\nLet us know if you have any questions.\r\n\r\nBest,\r\nMason";
	final private static String INDIVIDUAL_FEEDBACK_INSTRUCTIONS = "See post @[RUBRIC_NUMBER] for the grading rubric. If you believe the AI system made a mistake or you disagree with its assigned score and feedback, please reply to this followup comment with an explanation of your case.";

	protected AGPTClass gpt; // TODO: support for other LLMs
	
	public AQuizGradingProgram(String name, String description) {
		super(name, description);
		
		String apiKey = System.getenv("OPENAI_API_KEY");
		String defaultModel = System.getenv("DEFAULT_GPT_MODEL");			
		this.gpt = new AGPTClass(apiKey, defaultModel);
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
		
		this.parseCSV("test_files\\File-Systems.csv");
		
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
	public String createEnhancedRubric(DataStoreDiscussionForum dataStoreForum, String quizIdentifier, String responsesFilepath, String solutionsFilepath) {
		
		// determine if an initial human-created rubric exists
		DiscussionForum forum = dataStoreForum.getForum();
		String rubricID = fetchRubric(forum, quizIdentifier);
		
		// if no initial rubric exists, create one
		List<String> rubricTags = new ArrayList();
		rubricTags.add("automated_grading_rubric");
		if (rubricID == null) {
			rubricID = forum.createPost(quizIdentifier + " Quiz Rubric", "No initial rubric specified.", PostType.NOTE, PostVisibility.PRIVATE, rubricTags, EditorType.MARKDOWN);
		}
		
		// enhance the rubric using an LLM
		String initialRubric = forum.getPost(rubricID).getBody();
		String enhancedRubric = initialRubric + " ENHANCED!!"; // TODO: temp line for testing
				
			/*
		
			TODO
			
				do we need to call parseRubric() to separate out the rubric item for each question?
					or can GPT handle doing all questions in the quiz at once?
		
			 */
		
		// save the enhanced rubric to Piazza
		forum.updatePost(rubricID, quizIdentifier + " Quiz Rubric", enhancedRubric, PostType.NOTE, PostVisibility.PRIVATE, rubricTags, EditorType.MARKDOWN);
		
		// return the ID of the created/identified rubric post
		return rubricID;
		
	}
	
	
	/* STEP 1.5 (manual): check over enhanced rubric on Piazza, make changes if necessary */
	
	
	/* STEP 2 (automated): generate feedback to students' responses and post privately */
	// TODO: parameter for spreadsheet of responses
	public void createPrivateFeedback(DataStoreDiscussionForum dataStoreForum, String quizIdentifier) {
		
		// fetch the enhanced rubric
		DiscussionForum forum = dataStoreForum.getForum();
		String rubricID = fetchRubric(forum, quizIdentifier);
		String rubricText = forum.getPost(rubricID).getBody();
		
		// fetch the individual feedback posts
		Map<String, String> individualFeedbackPostIDs = (Map<String, String>) dataStoreForum.getDataValue("individualFeedbackPosts");
		
		/*
		
		TODO: extract individual question rubrics from quiz rubric using parseRubric()
		
		*/
		
		// iterate through each student in the Piazza course
		Map<String, String> studentIDMap = retrieveStudentIDsFromEmails(forum, "unc.edu");
		for (String studentID : studentIDMap.keySet()) {
			
			String individualFeedbackPostID = individualFeedbackPostIDs.get(studentID);
			String studentUniversityID = studentIDMap.get(studentID);
			
			/*
			
			TODO: access students' responses from the spreadsheet using their university ID
			TODO: grade responses using LLM
					per question? per student? how are we batching this?
					make sure it considers all the different aspects mentioned in the gold standard solutions equally
			
			*/

			// create instructor-only followup with feedback
			String individualFeedback; // TODO: = formatIndividualFeedback();
			forum.createFollowup(individualFeedbackPostID, individualFeedback, EditorType.MARKDOWN, true);

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
			
			/*
			
			TODO: find the follows that correspond to this quiz
				then, for each followup:
					copy the body
					delete it
					create a new public followup
			
			*/
			
		}
		
		// change the rubric visibility from private to public
		String rubricID = fetchRubric(forum, quizIdentifier);
		ForumPost rubric = forum.getPost(rubricID);
		forum.updatePost(rubricID, rubric.getSubject(), rubric.getBody(), PostType.NOTE, PostVisibility.PUBLIC, rubric.getTags(), EditorType.MARKDOWN);
		
	}
	
	/* STEP 3.5 (manual): students view the feedback and can argue it, instructors can modify scores if necessary */
	
	
	/* STEP 4 (automated): retrieve the (potentially modified) feedback scores from Piazza, export as CSV in Canvas format */
	/*		also exports a log of followup interactions, and deletes the feedback followups */
	// TODO: parameter for spreadsheet of responses
	public String exportGrades(DataStoreDiscussionForum dataStoreForum, String quizIdentifier) {
		
	
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
		
		return null; // TODO: return path of exported file (or directory?)
		
	}
	
	
	/* STEP 4.5 (manual): upload the exported scores to Canvas */
	
	
	
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
	
	// given the rubric text for a quiz, extract the rubric item for each question
	protected List<String> parseRubric(String rubricText) {
		
		/*
		
		TODO: split on some sort of token pattern
			need to make sure GPT adheres to this after enhancement
		
		*/
		
		return null;
		
	}
	
	// find the corresponding university ID for each student in the Piazza course
	protected Map<String, String> retrieveStudentIDsFromEmails(DiscussionForum forum, String universityDomain) {
		
		Map<String, String> studentIDMap = new HashMap<String, String>();
		
		for (ForumUser u : forum.getAllUsers()) {
			List<String> emails = u.getEmails();
			for (String e : emails) {
				if (e.contains(universityDomain)) {
					String studentUniversityID = e.split("@")[0];
					studentIDMap.put(u.getID(), studentUniversityID);
					break;					
				}
			}
		}
		
		return studentIDMap;
		
	}
	
	// format feedback for a student into text for a followup
	protected String formatIndividualFeedback(String quizID, int questionID, String studentAnswer, double assignedScore, double maxScore, String answerFeedback, int rubricPostNumber) {
				
		/*
	
		TODO: INCORPERATE TEXT OF CORRESPONDING PRIOR QUESTION

		*/
		
		String followupText =
			"Quiz: " + quizID + "\n"
			+ "Question: " + questionID + "\n"
			+ "Your Answer: " + studentAnswer + "\n"
			+ "AI Score: " + assignedScore + "/" + maxScore + "\n"
			+ "AI Feedback: " + answerFeedback + "\n---\n"
			+ INDIVIDUAL_FEEDBACK_INSTRUCTIONS.replace("[RUBRIC_NUMBER]", String.valueOf(rubricPostNumber));
		
		return followupText;
		
	}
	
	// given followup text, parse out the individual feedback items
	protected Map<String, Object> parseIndividualFeedback(String followupText) {
		
        Map<String, Object> items = new HashMap<>();
        
        // TODO: make sure AI response is a single line.
        
        String[] lines = followupText.split("\n");
		for (String l : lines) {
			
			String[] parts = l.split(":", 2);
            if (parts.length == 2) {
            
            	String key = parts[0].trim();
            	String value = parts[1].trim();
            	
            	if (key.equals("AI Score") || key.equals("Instructor Score"))
            		items.put(key, Double.valueOf(value.split("/")[0]));
            	else if (key.equals("Question"))
            		items.put(key, Integer.valueOf(value));            		
            	else
            		items.put(key, value);
            	
            }
            
		}
        		
        return items;
		
	}
	
	
	
	
	
	// ---------------------------------------------------
	
	
	

	
	// TODO: clean this up
	protected void parseCSV(String filepath) {
		
		try (Reader reader = new FileReader(filepath);
			@SuppressWarnings("deprecation")
			CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {
			
			for (CSVRecord csvRecord : csvParser) {
				
				System.out.println("\n\n\n\n");
				Map<String, String> recordMap = csvRecord.toMap();
				System.out.println(recordMap.keySet());
//				System.out.println(recordMap.get("Username"));
				
				
//				for (String v : csvRecord.toList()) {
//					System.out.println(v);
//				}
				
//				String timestamp = csvRecord.get("Timestamp");
//				String username = csvRecord.get("Username");
//				String totalScore = csvRecord.get("Total score");
//				String anonymousID = csvRecord.get("Anonymous ID");
//				
//				// TODO: make this work for all quizzes
//				
//				// test prints
//				System.out.println("Timestamp: " + timestamp);
//				System.out.println("Username: " + username);
//				System.out.println("Total Score: " + totalScore);
//				System.out.println("Anonymous ID: " + anonymousID);	
				
//				System.out.println("Record values: " + csvRecord.toList());
				
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	// try to get them to mark as resolved if they think it looks good
	
	// current behavior: only emails that get set is from instructor to student when they reply to reply
		// currently not sending email when followup gets made		
	
}
