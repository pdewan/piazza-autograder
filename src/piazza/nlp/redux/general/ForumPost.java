package piazza.nlp.redux.general;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ForumPost {
	
	public enum PostType {
		QUESTION, NOTE; // TODO: others? (announcement, poll, etc.)
	}
	
	public enum PostVisibility {
		PRIVATE, PUBLIC;
	}
	
	public Map<String, Object> getAllData();
	public String getPostID();
	public String getCourseID();
	public String getSubject();
	public String getBody();
	public String getAuthorID();
	public PostType getType();
	public PostVisibility getVisibility();
	public Date getDateCreated();
	public Date getDateUpdated();
	public List<String> getTags();
	public String getURL();

}
