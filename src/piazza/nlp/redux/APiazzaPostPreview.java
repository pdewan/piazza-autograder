package piazza.nlp.redux;

import java.util.Map;

public class APiazzaPostPreview implements PiazzaPost {

	protected Map<String, Object> postData;
	
	public APiazzaPostPreview(Map<String, Object> postData) {
		this.postData = postData;
	}
	
	public Map<String, Object> getFullData() {
		return this.postData;
	}
	
	// TODO: getters for individual properties
	//	either have instance variables for them and set in constructor
	// 	or have getters access specific elements of postData
	
}
