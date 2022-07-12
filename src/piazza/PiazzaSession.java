package piazza;

import java.io.IOException;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONObject;

public interface PiazzaSession {
	public void login(String username, String password) throws ClientProtocolException, IOException, LoginFailedException; // logins the user
	public Map<String, Object> getResp(String data, String APIEndpt) throws NotLoggedInException, ClientProtocolException, IOException;
	public Map<String, Object> piazzaAPICall(String method, JSONObject params, String APIEndpt) throws ClientProtocolException, NotLoggedInException, IOException;
}
