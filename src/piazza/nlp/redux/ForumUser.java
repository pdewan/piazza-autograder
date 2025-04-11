package piazza.nlp.redux;

import java.util.List;

public interface ForumUser {

	public String getID();
	public String getName();
	public List<String> getEmails();
	public String getRole();
	public boolean getAdmin();
	
}
