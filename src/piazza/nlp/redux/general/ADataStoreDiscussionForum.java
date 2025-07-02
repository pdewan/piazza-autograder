package piazza.nlp.redux.general;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;
import org.json.JSONArray;
import org.apache.commons.text.StringEscapeUtils; // NOTE: need to add 'commons-text-1.13.1.jar' to project from https://commons.apache.org/text/download_text.cgi
													// also need to upgrade to 'commons-lang3-3.17.0.jar'

import piazza.nlp.redux.general.DiscussionForum.EditorType;
import piazza.nlp.redux.general.ForumPost.PostType;
import piazza.nlp.redux.general.ForumPost.PostVisibility;

public class ADataStoreDiscussionForum implements DataStoreDiscussionForum {

	// TODO: support multiple data posts -- have a hashmap of data post numbers and customizable names
	//   this would require some persistent memory: maybe save as part of log and then search for log based on post title??
	
	protected DiscussionForum forum;
	protected String dataPostID;
	
	public ADataStoreDiscussionForum(DiscussionForum baseForum, String dataPostID) {
		this.forum = baseForum;
		if (dataPostID == null)
			this.createNewDataPost();
		else
			this.dataPostID = dataPostID;
	}
	
	
	
	/* DataStoreDiscussionForum METHODS */
	
	@Override
	public DiscussionForum getForum() {
		return this.forum;
	}

	// TODO: more code reuse between createNewDataPost() and overwriteWithDataPost()
	
	@Override
	public String createNewDataPost() {
		String dataPostSubject = "Mixed-Initiative Agent Data";
		String dataPostInitialBody = "{}";
		String[] dataPostTags = {"automated"};
		List<String> dataPostTagList = new ArrayList<>(Arrays.asList(dataPostTags)); // TODO: what tags?
		String postID = this.forum.createPost(dataPostSubject, dataPostInitialBody, PostType.NOTE, PostVisibility.PRIVATE, dataPostTagList, EditorType.PLAIN_TEXT);
		this.dataPostID = postID;
		return postID;
	}

	@Override
	public String overwriteWithDataPost(String postID) {		
		String dataPostSubject = "Mixed-Initiative Agent Data";
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
		return this.getJSONData().has(dataName);
	}

	// returns registered type for dataName or null if dataName is not registered
	@Override
	public Class getDataType(String dataName) {
		
		if (this.isRegistered(dataName)) {
			JSONObject element = this.getJSONData().getJSONObject(dataName);
			String classString = element.getString("type");
			Class classType;
			try {
				classType = Class.forName(classString);
				return classType;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		return null;		

	}
	
	// returns registered value for dataName or null if dataName is not registered
	@Override
	public Object getDataValue(String dataName) {

		System.out.println("Name: " + dataName + "\tRegistered: " + this.isRegistered(dataName));
		
		if (this.isRegistered(dataName)) {
			
			JSONObject element = this.getJSONData().getJSONObject(dataName);
			Object value = element.get("value"); // this method does internal type conversion, could potentially cause issues (e.g. detecting int vs long)
			String classString = element.getString("type");
			Class classType;
			
			try {
				System.out.println("Name: " + dataName + "\tType: " + classString + "\tValue: " + value);
				
				classType = Class.forName(classString);
				if (classType.isArray()) {
				
		            Class<?> componentType = classType.getComponentType();
		            List<Object> arrList = new ArrayList<>();
		            JSONArray arrVals = (JSONArray) value;
		            for (int i = 0; i < (arrVals.length()); i++) {
		            	arrList.add(componentType.cast(arrVals.get(i)));
		            }
		            
		            Object resultArray = Array.newInstance(componentType, arrList.size());
		            for (int i = 0; i < arrList.size(); i++) {
		                Array.set(resultArray, i, arrList.get(i));
		            }
		            
		            return resultArray;
					
				} else {
					return classType.cast(value);
				}
				
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
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
		String jsonBody = this.getDataPost().getBody();
		
		//System.out.println(jsonBody);
		String formattedJSON = StringEscapeUtils.unescapeHtml4(jsonBody);
		//System.out.println(formattedJSON);
		
		JSONObject data = new JSONObject(formattedJSON);
		
		System.out.println(data.keySet());
		System.out.println(data.has("automatedSuggestionDisclaimerID"));
		
		return data;
	}
	
	protected void updateJSONData(JSONObject data) {

		// TODO: simplify this if we develop update methods with less parameters
		ForumPost dataPost = this.getDataPost();
		this.forum.updatePost(this.dataPostID, dataPost.getSubject(), data.toString(), dataPost.getType(), dataPost.getVisibility(), dataPost.getTags(), EditorType.PLAIN_TEXT);
		
	}

	
}
