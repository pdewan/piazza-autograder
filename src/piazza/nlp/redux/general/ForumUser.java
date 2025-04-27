package piazza.nlp.redux.general;

import java.util.List;
import java.util.Map;

public interface ForumUser {

	public Map<String, Object> getAllData();
	public String getID();
	public String getName();
	public List<String> getEmails();
	public String getRole(); // TODO: make this enum? what are the options?
	public boolean getAdmin();
	
}
