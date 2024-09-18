package piazza.nlp;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONObject;

import piazza.LoginFailedException;
import piazza.NotLoggedInException;

public class ParameterizedTesterDriverML {
	
	static APiazzaClassRecursivePostsML loggedInClass;
	static String outDir = "C:\\Users\\Mason\\Documents\\COMP 524\\diaries";

	public static void printJSON(JSONObject obj) throws ClientProtocolException, NotLoggedInException, IOException {
		System.out.println();
		System.out.println(obj.toString(2));
		System.out.println();
	}
	
	public static JSONObject getPosts() throws ClientProtocolException, NotLoggedInException, IOException {
		return loggedInClass.getAllPostsRecursive(0, Integer.MAX_VALUE, 0, Long.MAX_VALUE);
	}
	
	public static void writePosts() throws ClientProtocolException, NotLoggedInException, IOException {
		loggedInClass.writeAllPosts(outDir, 0, Integer.MAX_VALUE, 0,Long.MAX_VALUE);		
	}
	
	public static void createTestPost() throws ClientProtocolException, NotLoggedInException, IOException {
		String aSubject = "AutoSubject" + System.currentTimeMillis();
		String aContent = "AutoContent";
		String aRecipients = "i588uvywlmn3b5,h68jepo6q4z3bk";
		String[] aPostTags = new String[] {"hw2"};
		createPost(aSubject, aContent, aRecipients, aPostTags);
	}
	
	public static void createPost(String subject, String content, String recipients, String[] tags) throws ClientProtocolException, NotLoggedInException, IOException {
		//loggedInClass.createPost(subject, content, Arrays.asList(tags), recipients);
		
		/*
		{
		  "method": "content.create",
		  "params": {
		    "nid": "k9zvvl9ubao6xa",
		    "type": "note",
		    "subject": "New test post",
		    "content": "Creating a new test post",
		    "editor": "rte",
		    "anonymous": "no",
		    "client_time": "9/28/2023, 5:17:08 PM",
		    "status": "active",
		    "config": {},
		    "folders": [
		      "hw1",
		      "hw2"
		    ]
		  }
		}
		*/
		
	}
	
	public static void deletePost(String subject, String content, String recipients, String[] tags) throws ClientProtocolException, NotLoggedInException, IOException {
		
		/*
			{
			  "method": "content.delete",
			  "params": {
			    "cid": "ln3oeojkm84ym"
			  }
			}		
		*/
	
	}

	public static void createDraftPost() throws ClientProtocolException, NotLoggedInException, IOException {
		/*
		{
		  "method": "network.save_draft",
		  "params": {
		    "nid": "k9zvvl9ubao6xa",
		    "draft": {
		      "content": "Creating a new test post",
		      "editorType": "rte",
		      "selectedPrivateUsers": {},
		      "folders": [],
		      "btn": {
		        "post_type_note": true,
		        "post_type_poll": false,
		        "post_type_question": false,
		        "class_live": false,
		        "entire_group": true,
		        "class_subgroup": false,
		        "individual_members": false,
		        "selections_allowed_one": true,
		        "selections_allowed_more": false,
		        "poll_close_date_after": true,
		        "poll_close_date_never": 0,
		        "show_poll_results_before_member_votes": true,
		        "show_poll_results_after_member_votes": false,
		        "show_poll_results_after_poll_closes": false,
		        "show_poll_results_keep_private": false,
		        "revotes_no": true,
		        "revotes_yes": false,
		        "poll_anonymity_everyone": true,
		        "poll_anonymity_nobody": false,
		        "poll_anonymity_students": false,
		        "publish_later": false,
		        "publish_now": true,
		        "posting_options_bypass_email": false,
		        "notify_mobile_update_input": false,
		        "must_read": 0,
		        "must_read_manual": true,
		        "must_read_to_post": false,
		        "must_read_expire": null
		      },
		      "txt": {
		        "subgroup_dropdown": "instr_k9zvvl9ubao6xa",
		        "post_summary": "New test post",
		        "input_poll_explanation": "",
		        "poll_close_date_menu": 48,
		        "new_post_anonymity": "no"
		      },
		      "questions": [
		        {
		          "answers": [],
		          "explanation": ""
		        }
		      ]
		    }
		  }
		}
		*/
	}
	
	public static void createDraftReply() throws ClientProtocolException, NotLoggedInException, IOException {
		
		// to delete, same but with null as body]
		
		/* 
	 	{
		  "method": "content.auto_save",
		  "params": {
		    "network_id": "k9zvvl9ubao6xa",
		    "cid": "lms7mrlybnq1uc",
		    "type": "followup",
		    "revision": 0,
		    "editor": "rte",
		    "body": "test reply"
		  }
		}
		*/
	}
	
	public static void deleteDraft() throws ClientProtocolException, NotLoggedInException, IOException {
		/*
			{
			  "method": "network.delete_draft",
			  "params": {
			    "id": "ln3o1roq3xc142",
			    "nid": "k9zvvl9ubao6xa"
			  }
			}
		*/
	}
	
	
	
	public static void main (String[] args) {

		try {
			
			// 2 hours in seconds: 7200
			
			loggedInClass = ParameterizedTester.loginToPiazzaClassFromEnvVar();

			//System.out.println(loggedInClass.getIDFromNumber("6"));
			loggedInClass.setUpTool("6", true);
			
			//System.out.println(loggedInClass.getInstructorIDs());
			//System.out.println(loggedInClass.processNewPosts(false));
			//System.out.println(loggedInClass.checkOfficeHourRequests());
			
//			List<Map<String, Object>> feed = loggedInClass.getFeed(999999, 0);
//			for (Map<String, Object> item : feed) {
//				String id = (String) item.get("id");
//				String nr = String.valueOf(item.get("nr"));
//				System.out.println(id);
//				System.out.println(nr);
//			}
			//loggedInClass.checkOfficeHourRequests();
			//System.out.println(loggedInClass.getPostFromNumber("351"));
			//loggedInClass.getGptPrompt();
			//loggedInClass.processNewPosts(false);
			//System.out.println(loggedInClass.getGptPrompt());
			//loggedInClass.resetLog();
			//{"ProcessedPosts":[]}
			
//			JSONArray logArr = new JSONArray();
//			JSONObject logObj = new JSONObject();
//			logObj.put("ProcessedPosts", logArr);
//			String jsonStr = logObj.toString();
//			System.out.println(jsonStr);
			
			
			//Date testBeginningDate = new Date(0);
			//DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
			//Date testBeginningDate = new Date(0);;
//			try {
//				testBeginningDate = df.parse("Wed Nov 29 06:31:59 EST 2023");
//			} catch (ParseException e) {
//				e.printStackTrace();
//			}
			
			//loggedInClass.getOfficeHoursReqests(testBeginningDate);

			
			
			//loggedInClass.getNewOfficeHoursReqests("C:\\Users\\Mason\\Documents\\COMP 524\\office hours requests\\timelog.txt");
			
			/*
			final String TA_MASON_ID = "lljvnbpqdze3xm";
			final String STUDENT_MASON_ID = "jzk5vujhfp6pa";
			
			
    		String aSubject = "Subject line";
    		String aContent = "body text";
    		String[] aRecipients = {TA_MASON_ID, STUDENT_MASON_ID};
    		String[] aPostTags = new String[] {"hw1", "hw2"};
			//loggedInClass.createDraftNote(aSubject, aContent, Arrays.asList(aPostTags), Arrays.asList(aRecipients), "individual");
 			
			
			String postID = "ln3n9af88yl4h6";
			//String postID = "lms7mrlybnq1uc"; // this one works, followup
			String content = "test from eclipse";
			loggedInClass.createDraftReply(postID, content);
			*/

			/*
			String postID = "ln3n9af88yl4h6";
			String replyMessage = "replying to your anonymous comment from Eclipse";
			loggedInClass.createTaggedCommentReply(postID, replyMessage);
			*/
			
			//System.out.println(loggedInClass.getAllUsers());
			//System.out.println(loggedInClass.createPrivateInstructorThread(TA_MASON_ID));
			//System.out.println(loggedInClass.createPrivateInstructorThread(STUDENT_MASON_ID));

			//System.out.println(loggedInClass.getPrivateInstructorThread(STUDENT_MASON_ID));
			
			
			

		} catch (IOException | LoginFailedException | NotLoggedInException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
		}
	}
	
}
