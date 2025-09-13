package piazza.nlp.redux.tools.programs;

import piazza.nlp.redux.tools.AnAbstractForumTool;

public abstract class AnAbstractForumProgram extends AnAbstractForumTool implements ForumProgram {

	public AnAbstractForumProgram(String name, String description) {
		super(name, description);
	}

}
