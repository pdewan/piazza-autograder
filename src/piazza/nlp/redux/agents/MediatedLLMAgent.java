package piazza.nlp.redux.agents;

import piazza.nlp.redux.general.AgentAction;
import piazza.nlp.redux.general.DataStoreDiscussionForum;
import piazza.nlp.redux.general.ForumPost;

public class MediatedLLMAgent extends AnAbstractForumAgent implements ForumAgent {
	
	final protected static String DEFAULT_NAME = "Mediated LLM Agent";
	final protected static String DESCRIPTION = ""; // TODO
	
	public MediatedLLMAgent(String agentName) {
		this.name = agentName;
		this.description = DESCRIPTION;
	}
	
	public MediatedLLMAgent() {
		this(DEFAULT_NAME);
	}

	@Override
	public AgentAction processPost(DataStoreDiscussionForum dataStoreForum, ForumPost post) {

		// TODO
		
		
		
		
		
		return null;
		
	}
	
}
