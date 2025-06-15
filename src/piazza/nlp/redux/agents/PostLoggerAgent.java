package piazza.nlp.redux.agents;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;

import piazza.nlp.redux.exceptions.AnonymousDataAccessException;
import piazza.nlp.redux.general.AgentAction;
import piazza.nlp.redux.general.DataStoreDiscussionForum;
import piazza.nlp.redux.general.ForumPost;

public class PostLoggerAgent extends AnAbstractForumAgent implements ForumAgent {

	final protected static String DEFAULT_NAME = "Post Logger Agent";
	final protected static String DESCRIPTION = "Logs each post with our MongoDB database for tracking student difficulty.";
	
	private String mongoDBEndpoint;
	private String mongoDBPassword;
	
	public PostLoggerAgent(String agentName, String mongoDBEndpoint, String mongoDBPassword) {
		super(agentName, DESCRIPTION);
		this.mongoDBEndpoint = mongoDBEndpoint;
		this.mongoDBPassword = mongoDBPassword;
	}
	
	public PostLoggerAgent(String mongoDBEndpoint, String mongoDBPassword) {
		this(DEFAULT_NAME, mongoDBEndpoint, mongoDBPassword);
	}

	@Override
	public AgentAction processPost(DataStoreDiscussionForum dataStoreForum, ForumPost post, List<AgentAction> pastActions) {

		// TODO: test values
		String courseID = "Mediated-Agent-Test"; // TODO: we have the Piazza course ID, but want to be able to unify it with things like "Fall2025-Comp533"
		String machineID = "mlaney@cs.unc.edu"; // TODO: add getUserEmail() to PiazzaSession and do forum.getSession().getUserEmail();
		String sessionID = "example_exercise";
		
		String postID = post.getPostID();
		String postURL = post.getURL();
		String logType = dataStoreForum.getForum().getPlatformName();
		String authorName;
		try {
			authorName = dataStoreForum.getForum().getUser(post.getAuthorID()).getName();
		} catch (AnonymousDataAccessException e1) {
			e1.printStackTrace();
			authorName = "Anonymous";
		}
		JSONObject postData = new JSONObject(post.getAllData());
		
		try {
			
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost httpPost = new HttpPost(mongoDBEndpoint);
            
            // set headers
            httpPost.setHeader("Content-Type", "application/json");

            // construct the JSON request body
            String jsonBody = new JSONObject()
            		.put("body", new JSONObject()
            				.put("password", mongoDBPassword)
            				.put("course_id", courseID)
            				//.put("created_timestamp", mongoDBPassword) // will be created automatically?
            				.put("log", new JSONObject()
            						.put("author_name", authorName)
            						.put("post_url", postURL)
            						.put("post_data", postData)
            						)
            				.put("log_id", postID)
            				.put("log_type", logType)
            				.put("machine_id", machineID)
            				.put("session_id", sessionID)
            		).toString();
            
            // TODO: for debugging
            System.out.println(jsonBody);
            
            // set the request entity
            StringEntity requestEntity = new StringEntity(jsonBody);
            httpPost.setEntity(requestEntity);

            // execute the request
            HttpResponse response = httpClient.execute(httpPost);

            // handle the response (e.g. print the status code and response body)
            int statusCode = response.getStatusLine().getStatusCode();
            System.out.println("Status Code: " + statusCode);

      
            // You can read the response body using response.getEntity().getContent()
            // and convert it to a String or process it as needed.
            System.out.println(response.getEntity().getContent());
            // TODO
            
            
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		// TODO: how to log this? include the agent name and the response code from the request?
		return null;
		
	}
	
}
