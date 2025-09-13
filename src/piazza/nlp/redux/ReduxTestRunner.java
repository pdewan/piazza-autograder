package piazza.nlp.redux;

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
		
		final int DATA_POST_NUMBER = 40;
		
		
		String email = System.getenv("PIAZZA_EMAIL");
		String password = System.getenv("PIAZZA_PASSWORD");
		String classID = System.getenv("PIAZZA_CLASS_ID");

		PiazzaForum df = new APiazzaForum("TestPiazzaCourse", classID, email, password);
		String dataPostID = df.getPost(DATA_POST_NUMBER).getPostID();
		
		DataStoreDiscussionForum dsdf = new ADataStoreDiscussionForum(df, dataPostID);
		MixedInitiativeDiscussionForum midf = new AMixedInitiativeDiscussionForum(dsdf);
		
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
		
		qgp.testbed(dsdf);
		
		
		
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
