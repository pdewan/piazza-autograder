package piazza.nlp.redux;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import piazza.nlp.redux.general.ADataStoreDiscussionForum;
import piazza.nlp.redux.general.AMixedInitiativeDiscussionForum;
import piazza.nlp.redux.general.DataStoreDiscussionForum;
import piazza.nlp.redux.general.MixedInitiativeDiscussionForum;
import piazza.nlp.redux.piazza.APiazzaForum;
import piazza.nlp.redux.piazza.PiazzaForum;
import piazza.nlp.redux.tools.agents.ImageCheckerAgent;
import piazza.nlp.redux.tools.agents.MediatedLLMAgent;
import piazza.nlp.redux.tools.agents.VisibilityCheckerAgent;
import piazza.nlp.redux.tools.programs.AQuizGradingProgram;


public class ReduxTestRunner {

	public static void main(String[] args) {		
		
		final int DATA_POST_NUMBER = 40; // TEST
//		final int DATA_POST_NUMBER = 33; // COMP89
		
		String email = System.getenv("PIAZZA_EMAIL");
		String password = System.getenv("PIAZZA_PASSWORD");
		String classID = System.getenv("PIAZZA_CLASS_ID");
//		String classID = "mdrjw1sfh122nw"; // TODO

		PiazzaForum df = new APiazzaForum("TestPiazzaCourse", classID, email, password);
		String dataPostID = df.getPost(DATA_POST_NUMBER).getPostID();
		
		DataStoreDiscussionForum dsdf = new ADataStoreDiscussionForum(df, dataPostID);
		MixedInitiativeDiscussionForum midf = new AMixedInitiativeDiscussionForum(dsdf);
		
//		System.out.println(df.getPost(239).getAllData());
//		df.deletePost("mgfvyj0lqkj53p");
		
//		System.out.println("SYSTEM LOG: " + midf.getSystemLog());
//		System.out.println(midf.getDataStoreForum().getForum().getPost("mcu3v28kdwc7oc").getPostNumber());
//		System.out.println(df.getPost(179).getPostID());		

//		System.exit(0);
		
		
//		ImageCheckerAgent img = new ImageCheckerAgent();
//		VisibilityCheckerAgent vis = new VisibilityCheckerAgent();
//		MediatedLLMAgent llm = new MediatedLLMAgent();
//		
////		dsdf.overwriteWithDataPost(df.getPost(6).getPostID());
//		
//
//		midf.registerAgent(img);
//		midf.registerAgent(vis);
//		midf.registerAgent(llm);
//
////		midf.resetSystemLog();
////		midf.createNewSystemLog();
//		//midf.setUp();
//		//midf.setUpAgents(midf.getRegisteredAgentNames());
//		
//		//System.out.println(midf.getRegisteredAgentNames());
//		//System.out.println(((PiazzaForum) midf.getDataStoreForum().getForum()).getFeed());
//		
//		midf.runAllAgents(((PiazzaForum) midf.getDataStoreForum().getForum()).getFeed());
		
		AQuizGradingProgram qgp = new AQuizGradingProgram("Quiz Grader Test", "A program that grades Google Forms responses.");
//		qgp.parseCSV("test_files\\File-Systems.csv");
//		qgp.createFeedbackPosts(dsdf);
//		
//		qgp.testbed(dsdf);

//		qgp.setUp(dsdf);
//		qgp.initializeIndividualFeedbackPosts(dsdf, false);
		
		List<String[]> quizzesToGrade = new ArrayList();
		quizzesToGrade.add(new String[] {"HW3 Caching", "grades\\hw3\\quizzes\\Caching.csv", "grades\\hw3\\solutions\\Caching Quiz Solutions.csv"});
		quizzesToGrade.add(new String[] {"HW3 Docker", "grades\\hw3\\quizzes\\Docker.csv", "grades\\hw3\\solutions\\Docker Quiz Solutions.csv"});
		quizzesToGrade.add(new String[] {"HW3 Client-Server", "grades\\hw3\\quizzes\\Client Server.csv", "grades\\hw3\\solutions\\Client-Server Quiz Solutions.csv"});
		quizzesToGrade.add(new String[] {"HW3 Bash", "grades\\hw3\\quizzes\\Bash.csv", "grades\\hw3\\solutions\\Bash Quiz Solutions.csv"});
		quizzesToGrade.add(new String[] {"HW3 Second Exam Practice", "grades\\hw3\\quizzes\\Second Exam Practice.csv", "grades\\hw3\\solutions\\Second Exam Practice Solutions.csv"});

//		quizzesToGrade.add(new String[] {"CW2 Command Anatomy", "grades\\cw2\\quizzes\\Anatomy of a Command.csv", "grades\\cw2\\solutions\\Anatomy of a Command Solutions.csv"});
//		quizzesToGrade.add(new String[] {"CW3 Basic Bash Application", "grades\\cw3\\quizzes\\BasicBashApplication.csv", "grades\\cw3\\solutions\\Basic Bash Application Solutions.csv"});
//		quizzesToGrade.add(new String[] {"CW3 Bash RNA Analysis", "grades\\cw3\\quizzes\\RNA-Bash.csv", "grades\\cw3\\solutions\\Bash RNA Analysis Solutions.csv"});
		
//		quizzesToGrade.add(new String[] {"CW4 Variables", "grades\\cw4\\quizzes\\Variables.csv", "grades\\cw4\\solutions\\Variables Solutions.csv"});
//		quizzesToGrade.add(new String[] {"CW4 PATH", "grades\\cw4\\quizzes\\PATH.csv", "grades\\cw4\\solutions\\PATH Solutions.csv"});
//		quizzesToGrade.add(new String[] {"CW4 Parameters", "grades\\cw4\\quizzes\\Parameters Formal and Actual.csv", "grades\\cw4\\solutions\\Parameters Solutions.csv"});

		

//		for (String[] quizTriple : quizzesToGrade)
//			qgp.createEnhancedRubric(dsdf, quizTriple[0], quizTriple[1], quizTriple[2], false);
		
//		for (String[] quizTriple : quizzesToGrade)
//			qgp.createPrivateFeedback(dsdf, quizTriple[0], quizTriple[1], false);

//		for (String[] quizTriple : quizzesToGrade)
//			qgp.makeFeedbackVisible(dsdf, quizTriple[0]);
		
		for (String[] quizTriple : quizzesToGrade)
			qgp.exportGradedSubmissions(dsdf, quizTriple[0], quizTriple[1], "grades\\hw3\\exports", false);
		
//		qgp.createEnhancedRubric(dsdf, "File-Systems", "grades\\hw1\\quizzes\\File-Systems.csv", "grades\\hw1\\solutions\\File-Systems Solutions.csv", true);
//		qgp.createPrivateFeedback(dsdf, "File-Systems", "grades\\hw1\\quizzes\\File-Systems.csv", true);
//		qgp.makeFeedbackVisible(dsdf, "File-Systems");
//		qgp.exportGrades(dsdf, "File-Systems");
		
//		df.createFollowup(postID, body, editor, instructorOnly)
		
//		try {
//			System.out.println(qgp.readQuizGrades("grades\\hw1\\quizzes\\File-Systems.csv", true).toString(2));
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
//		Map<String, Object> quizMockupData = df.getPost(234).getAllData();
//		for (String key : quizMockupData.keySet()) {
//			System.out.println(key);
//			// data, children, history
//		}
//		
//		List<Map<String, Object>> children = (List<Map<String, Object>>) quizMockupData.get("children");
//		System.out.println("\n\nCHILDREN:\n\n");
//		
//		for (Map<String, Object> c : children) {
//			
//			System.out.println(c.keySet());
//			
//			System.out.println("\n\nSUB CHILDREN:\n\n");
//			List<Map<String, Object>> subChildren = (List<Map<String, Object>>) c.get("children");
//			for (Map<String, Object> sc : subChildren) {
//				System.out.println(sc.keySet());
//
//			}
//
//						
//		}
//		
//		// FOLLOWUP KEYS: anon, folders, data, no_upvotes, subject, created, bucket_order, bucket_name, type, tag_good, uid, children, tag_good_arr, no_answer, id, updated, config
//		// REPLY KEYS:    anon, folders, data, subject, created, bucket_order, bucket_name, type, tag_good, uid, children, tag_good_arr, id, updated, config
//		
//		System.out.println("\n\nHISTORY:\n\n");
//		System.out.println(quizMockupData.get("history"));
//		
//		System.out.println("\n\nCHANGE LOG:\n\n");
//		System.out.println(quizMockupData.get("change_log"));
//		
		
		
	}

}
