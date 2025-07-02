package piazza.nlp.redux;

import piazza.nlp.redux.agents.ImageCheckerAgent;
import piazza.nlp.redux.agents.MediatedLLMAgent;
import piazza.nlp.redux.agents.VisibilityCheckerAgent;
import piazza.nlp.redux.general.ADataStoreDiscussionForum;
import piazza.nlp.redux.general.AMixedInitiativeDiscussionForum;
import piazza.nlp.redux.general.DataStoreDiscussionForum;
import piazza.nlp.redux.general.MixedInitiativeDiscussionForum;
import piazza.nlp.redux.piazza.APiazzaForum;
import piazza.nlp.redux.piazza.PiazzaForum;


public class ReduxTestRunner {

	public static void main(String[] args) {
		
		final int DATA_POST_NUMBER = 6;
		
		String email = System.getenv("PIAZZA_EMAIL");
		String password = System.getenv("PIAZZA_PASSWORD");
		String classID = System.getenv("PIAZZA_CLASS_ID");

		PiazzaForum df = new APiazzaForum("TestPiazzaCourse", classID, email, password);
		String dataPostID = df.getPost(DATA_POST_NUMBER).getPostID();
		
		DataStoreDiscussionForum dsdf = new ADataStoreDiscussionForum(df, dataPostID);
		MixedInitiativeDiscussionForum midf = new AMixedInitiativeDiscussionForum(dsdf);
		
		ImageCheckerAgent img = new ImageCheckerAgent();
		VisibilityCheckerAgent vis = new VisibilityCheckerAgent();
		MediatedLLMAgent llm = new MediatedLLMAgent();
		
		//dsdf.overwriteWithDataPost(df.getPost(6).getPostID());
		
		midf.registerAgent(img);
		midf.registerAgent(vis);
		midf.registerAgent(llm);

		//midf.createNewSystemLog();
		//midf.setUp();
		//midf.setUpAgents(midf.getRegisteredAgentNames());
		
		//System.out.println(midf.getRegisteredAgentNames());
		//System.out.println(((PiazzaForum) midf.getDataStoreForum().getForum()).getFeed());
		
		midf.runAllAgents(((PiazzaForum) midf.getDataStoreForum().getForum()).getFeed());
		
	}

}
