package piazza.nlp.redux.general;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import piazza.nlp.redux.general.DiscussionForum.EditorType;
import piazza.nlp.redux.general.ForumPost.PostType;
import piazza.nlp.redux.general.ForumPost.PostVisibility;

public class ADataStoreDiscussionForum implements DataStoreDiscussionForum {

	// TODO: support multiple data posts -- have a hashmap of data post numbers and customizable names
	//   this would require some persistent memory: maybe save as part of log and then search for log based on post title??
	
	protected DiscussionForum forum;
	protected String dataPostID;
	
	public ADataStoreDiscussionForum(DiscussionForum baseForum) {
		this.forum = baseForum;
	}
	
	
	
	/* DataStoreDiscussionForum METHODS */
	
	@Override
	public DiscussionForum getForum() {
		return this.forum;
	}

	// TODO: more code reuse between createNewDataPost() and overwriteWithDataPost()
	
	@Override
	public String createNewDataPost() {
		String dataPostSubject = "Data Post for Mixed-Initiative Agents";
		String dataPostInitialBody = "{}";
		String[] dataPostTags = {"automated"};
		List<String> dataPostTagList = new ArrayList<>(Arrays.asList(dataPostTags)); // TODO: what tags?
		String postID = this.forum.createPost(dataPostSubject, dataPostInitialBody, PostType.NOTE, PostVisibility.PRIVATE, dataPostTagList, EditorType.PLAIN_TEXT);
		this.dataPostID = postID;
		return postID;
	}

	@Override
	public String overwriteWithDataPost(String postID) {		
		String dataPostSubject = "Data Post for Mixed-Initiative Agents";
		String dataPostInitialBody = "{}";
		String[] dataPostTags = {"automated"};
		List<String> dataPostTagList = new ArrayList<>(Arrays.asList(dataPostTags)); // TODO: what tags?
		this.forum.updatePost(postID, dataPostSubject, dataPostInitialBody, PostType.NOTE, PostVisibility.PRIVATE, dataPostTagList, EditorType.PLAIN_TEXT);
		this.dataPostID = postID;
		return postID;
	}

	@Override
	public ForumPost getDataPost() {
		return this.forum.getPost(dataPostID);
	}
	
	
	// registers a new element with the given name, type, and value -- returns false if element already exists
	@Override
	public boolean registerData(String dataName, Class dataType, Object dataValue) {
		
		if (this.isRegistered(dataName))
			return false;
		
		JSONObject data = this.getJSONData();
		JSONObject newElement = new JSONObject()
				.put("type", dataType.getName())
				.put("value", dataValue);
		
		data.put(dataName, newElement);
		
		// TODO: check if this works properly
		
		this.updateJSONData(data);
		return true;
		
	}

	// checks if an element with the given name has been registered in the data store
	@Override
	public boolean isRegistered(String dataName) {
		return this.getJSONData().containsKey(dataName); // could be .has(dataName)
	}

	// returns registered type for dataName or null if dataName is not registered
	@Override
	public Class getDataType(String dataName) {
		
		if (this.isRegistered(dataName)) {
			JSONObject element = this.getJSONData().getJSONObject(dataName);
			String classString = element.getString("type");
			Class classType = Class.forName(classString);
			return classType;
		}
		
		return null;		

	}
	
	// returns registered value for dataName or null if dataName is not registered
	@Override
	public Object getDataValue(String dataName) {

		if (this.isRegistered(dataName)) {
			JSONObject element = this.getJSONData().getJSONObject(dataName);
			String classString = element.getString("type");
			Class classType = Class.forName(classString);
			Object value = element.get("value"); // this method does internal type conversion, could potentially cause issues (e.g. detecting int vs long)
			return classType.cast(value);
		}
		
		return null;
		
	}

	// // returns false if does not already exist
	@Override
	public boolean updateData(String dataName, Object dataValue) {

		if (this.isRegistered(dataName)) {
			
			JSONObject data = this.getJSONData();
			JSONObject element = data.getJSONObject(dataName);
			element.remove("value");
			element.put("value", dataValue);
			
			// TODO: check if this works properly
			
			this.updateJSONData(data);
			return true;
			
		}
		
		return false;
		
	}

	
	
	/* HELPER METHODS */
	protected JSONObject getJSONData() {
		return new JSONObject(this.getDataPost().getBody());
	}
	
	protected void updateJSONData(JSONObject data) {

		// TODO: simplify this if we develop update methods with less parameters
		ForumPost dataPost = this.getDataPost();
		this.forum.updatePost(this.dataPostID, dataPost.getSubject(), data.toString(), dataPost.getType(), dataPost.getVisibility(), dataPost.getTags(), EditorType.PLAIN_TEXT);
		
	}

	
}
