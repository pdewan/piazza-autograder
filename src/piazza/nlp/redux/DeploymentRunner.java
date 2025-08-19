package piazza.nlp.redux;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Date;

import piazza.nlp.redux.agents.ImageCheckerAgent;
import piazza.nlp.redux.agents.MediatedLLMAgent;
import piazza.nlp.redux.agents.VisibilityCheckerAgent;
import piazza.nlp.redux.general.ADataStoreDiscussionForum;
import piazza.nlp.redux.general.AMixedInitiativeDiscussionForum;
import piazza.nlp.redux.general.DataStoreDiscussionForum;
import piazza.nlp.redux.general.MixedInitiativeDiscussionForum;
import piazza.nlp.redux.piazza.APiazzaForum;
import piazza.nlp.redux.piazza.PiazzaForum;

public class DeploymentRunner {
	public static void main(String[] args) {
		
		final boolean FULL_SETUP = false;
		final int DATA_POST_NUMBER = 7;
		final int PROCESS_INTERVAL = 15; // in minutes
	
		String email = System.getenv("PIAZZA_EMAIL");
		String password = System.getenv("PIAZZA_PASSWORD");
		String classID = System.getenv("PIAZZA_CLASS_ID");
		
		PiazzaForum df = new APiazzaForum("COMP301 SSII25", classID, email, password);
		String dataPostID = df.getPost(DATA_POST_NUMBER).getPostID();
	
		DataStoreDiscussionForum dsdf = new ADataStoreDiscussionForum(df, dataPostID);
		
		if (FULL_SETUP)
			dsdf.overwriteWithDataPost(df.getPost(DATA_POST_NUMBER).getPostID());
		
		MixedInitiativeDiscussionForum midf = new AMixedInitiativeDiscussionForum(dsdf);

		VisibilityCheckerAgent vis = new VisibilityCheckerAgent();
		ImageCheckerAgent img = new ImageCheckerAgent();
		MediatedLLMAgent llm = new MediatedLLMAgent();

		midf.registerAgent(img);
		midf.registerAgent(vis);
		midf.registerAgent(llm);
		
		if (FULL_SETUP) {
			midf.createNewSystemLog(true);
			midf.setUp();
			midf.setUpAgents(midf.getRegisteredAgentNames());
		}
		
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        
        Runnable processFeed = () -> {
        	try {
        		System.out.println("Began processing Piazza feed at " + new Date());
        		midf.runAllAgents(((PiazzaForum) midf.getDataStoreForum().getForum()).getFeed());
        		System.out.println("Finished processing Piazza feed at " + new Date());
        	} catch (Throwable t) {
        		System.out.println("UNCAUGHT EXCEPTION IN PROCESSING: " + t.getMessage());
                t.printStackTrace();
        	}
        };
        
        scheduler.scheduleAtFixedRate(processFeed, 0, PROCESS_INTERVAL, TimeUnit.MINUTES);		
	
	}
}
