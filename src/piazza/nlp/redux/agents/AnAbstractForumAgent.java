package piazza.nlp.redux.agents;

public abstract class AnAbstractForumAgent implements ForumAgent {

	protected String name;
	protected String description;
	
	@Override
	public String getAgentName() {
		return this.name;
	}

	@Override
	public void setAgentName(String newName) {
		this.name = newName;
	}

	@Override
	public String getAgentDescription() {
		return this.description;
	}

}

