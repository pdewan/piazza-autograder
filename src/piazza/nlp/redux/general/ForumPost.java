package piazza.nlp.redux.general;

import java.util.Date;
import java.util.List;
import java.util.Map;

import piazza.nlp.redux.exceptions.AnonymousDataAccessException;

public interface ForumPost {
	
	public enum PostType {
		QUESTION, NOTE, POLL; // TODO: others? (announcement, etc.)
	}
	
	public enum PostVisibility {
		PRIVATE, PUBLIC;
	}
	
	public Map<String, Object> getAllData();
	public String getPostID();
	public String getCourseID(); // TODO: change to getClassID()?
	public String getSubject();
	public String getBody();
	public String getAuthorID() throws AnonymousDataAccessException; // TODO: is this how to handle this?
	public PostType getType();
	public PostVisibility getVisibility();
	public Date getDateCreated(); // TODO: change to Instant object instead of Date?
	public Date getDateUpdated(); // TODO: change to Instant object instead of Date?
	public List<String> getTags();
	public String getURL();
	
	// TODO: getResponses? or are they too platform-specific for that to be useful?

}
