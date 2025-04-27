package piazza.nlp.redux.piazza;

import java.util.Map;

import piazza.nlp.redux.general.ForumUser;

public interface PiazzaUser extends ForumUser {

	public boolean getPublished();
	public int getAdminPermission();
	public Map<String, Object> getEndorser();
	
}
