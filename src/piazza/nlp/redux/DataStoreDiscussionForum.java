package piazza.nlp.redux;

import java.util.Map;

import org.json.JSONObject;

public interface DataStoreDiscussionForum extends DiscussionForum {
	
	// should these be Map<String, Object> instead of JSONObject?
	public JSONObject getData(String dataName);
    public void storeData(String dataName, JSONObject dataValue);
    
}
