package piazza.nlp.redux.piazza;

import java.util.List;
import java.util.Map;

public abstract class AnAbstractPiazzaPost implements PiazzaPost {

	protected Map<String, Object> postData;
	protected String classID;
	
	public AnAbstractPiazzaPost(Map<String, Object> postData, String classID) {
		this.postData = postData;
		this.classID = classID;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "{@" + this.getPostNumber() + "}";
	}
	
	
	
	/* ForumPost METHODS */
	
	@Override
	public Map<String, Object> getAllData() {
		return this.postData;
	}
	
	@Override
	public String getPostID() {
		return (String) this.postData.get("id");
	}
	
	@Override
	public String getCourseID() {
		return this.classID;
	}

	@Override
	public PostType getType() {
		String type = (String) this.postData.get("type");
		if (type.equals("question"))
			return PostType.QUESTION;
		else if (type.equals("note"))
			return PostType.NOTE;
		else if (type.equals("poll"))
			return PostType.POLL;
		else
			return null;
	}
	
	@Override
	public PostVisibility getVisibility() {
		return ((String) this.postData.get("status")).equals("private") ? PostVisibility.PRIVATE : PostVisibility.PUBLIC;
	}
	
	@Override
	public List<String> getTags() {
		// NOTE: this.postData.get("folders") will give just poster-assigned tags without tags like "instructor-note" and "pin"
		// 	not sure if that's what we want here or not -- could make a separate method for that?
		return (List<String>) this.postData.get("tags");
	}
	
	@Override
	public String getURL() {
		return "https://piazza.com/class/" + this.getCourseID() + "/post/" + this.getPostNumber();
	}
	
	
	
	/* PiazzaPost METHODS */
	
	@Override
	public int getPostNumber() {
		return (int) this.postData.get("nr");
	}
	
	
	
	/* NOTES */

	// to determine if a post is anonymous, can use latestElement.get("anon") (can be either "no", "stud", or "full")
	// if a base post number is @281, the post numbers of followups are @281_f1 if normal, or @281_if1 if instructor private
	
}

