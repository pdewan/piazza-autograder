package piazza.nlp.redux.tools.agents;

import piazza.nlp.redux.tools.AnAbstractForumTool;

public abstract class AnAbstractForumAgent extends AnAbstractForumTool implements ForumAgent {

	public AnAbstractForumAgent(String name, String description) {
		super(name, description);
	}
	
}

