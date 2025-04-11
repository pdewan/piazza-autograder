package piazza.nlp.redux;

import java.util.List;
import java.util.Map;

public class APiazzaPostPreview implements PiazzaPost {

	protected Map<String, Object> postData;
	
	public APiazzaPostPreview(Map<String, Object> postData) {
		this.postData = postData;
	}
	
	public Map<String, Object> getAllData() {
		return this.postData;
	}
	
	// TODO: getters for individual properties
	//	either have instance variables for them and set in constructor
	// 	or have getters access specific elements of postData
	
	public String getID() {
		return (String) this.getAllData().get("id");
	}

	@Override
	public List<String> getTags() {
		// TODO Auto-generated method stub
		return null;
	}
	
	// TODO: get full post? but this needs access to a PiazzaForum object
	// public APiazzaPost getFullPost() {}
	
}
