package piazza.nlp.redux;

import java.util.Map;

import org.json.JSONObject;

public interface DataStoreDiscussionForum extends DiscussionForum {
	
	// should these be Map<String, Object> instead of JSONObject?
//	public JSONObject getData(String dataName);
//    public void storeData(String dataName, JSONObject dataValue);
    
	public int createNewDataPost(); // returns the post number
	public int overwriteWithDataPost(int postNumber);
	
	// changes from EICS model: allows for separate data posts -- read entry now takes the data post number
}
