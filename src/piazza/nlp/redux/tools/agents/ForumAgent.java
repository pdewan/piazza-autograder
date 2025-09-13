package piazza.nlp.redux.tools.agents;

import java.util.List;

import piazza.nlp.redux.actions.AgentAction;
import piazza.nlp.redux.general.DataStoreDiscussionForum;
import piazza.nlp.redux.general.ForumPost;
import piazza.nlp.redux.tools.ForumTool;

public interface ForumAgent extends ForumTool {
		
	public void setUp(DataStoreDiscussionForum dataStoreForum);
	public AgentAction processPost(DataStoreDiscussionForum dataStoreForum, ForumPost post, List<? extends AgentAction> pastActions); // runs the agent on the given post
	
	// NOTE: just added list of past actions to processPost, to handle chained order of agents

}
