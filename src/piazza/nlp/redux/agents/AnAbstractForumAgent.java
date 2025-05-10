package piazza.nlp.redux.agents;

public abstract class AnAbstractForumAgent implements ForumAgent {

	protected String name;
	protected String description;
	
	public AnAbstractForumAgent(String agentName, String agentDescription) {
		this.name = agentName;
		this.description = agentDescription;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "{" + this.getAgentName() + "}";
	}
	
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

