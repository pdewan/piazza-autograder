package piazza.nlp.redux.tools;

public class AnAbstractForumTool implements ForumTool {

	protected String name;
	protected String description;
	
	public AnAbstractForumTool(String name, String description) {
		this.name = name;
		this.description = description;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "{" + this.getName() + "}";
	}
	
	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String newName) {
		this.name = newName;
	}

	@Override
	public String getDescription() {
		return this.description;
	}
	
}
