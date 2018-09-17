package piazza;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;

public interface PiazzaClass {
	public List<Map<String, Object>> getFeed(int limit, int offset) throws ClientProtocolException, NotLoggedInException, IOException;
	public Map<String, Object> getUser(String uid) throws ClientProtocolException, NotLoggedInException, IOException;
	public Map<String, Object> getPost(String cid) throws ClientProtocolException, NotLoggedInException, IOException;
	public List<Map<String, Object>> getAllPosts() throws ClientProtocolException, NotLoggedInException, IOException;
	public String getAuthorId(Map<String, Object> post); // get author id of the post
	public String getUserName(String uid) throws ClientProtocolException, NotLoggedInException, IOException;
	public String getUserEmail(String uid) throws ClientProtocolException, NotLoggedInException, IOException;
}
