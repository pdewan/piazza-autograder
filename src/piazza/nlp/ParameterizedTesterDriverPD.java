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

import piazza.LoginFailedException;
import piazza.NotLoggedInException;

public class ParameterizedTesterDriverPD {
	static APiazzaClassRecursivePosts loggedInClass;
	static  String outDir = "C:\\Users\\dewan\\Downloads\\PiazzaOutput\\Comp301ss22";

//	feed_groups	:	i588uvywlmn3b5,h68jepo6q4z3bk
	public static void writePosts() throws ClientProtocolException, NotLoggedInException, IOException {
		loggedInClass.writeAllPosts(outDir, 0, Integer.MAX_VALUE, 0,Long.MAX_VALUE);		
	}
	public static void updatePost() throws ClientProtocolException, NotLoggedInException, IOException {
//		String anExistingID = "l5s7ag4zea40w";	
		String anExistingID = "l5uj1iv368q19x"; //"l5wmbry02vz5h7";	//l5uj1iv368q19x l5wmbry02vz5h7
		Map<String, Object> aPost = loggedInClass.getPost(anExistingID );
		int aHistorySize = (Integer) aPost.get("history_size");
  		List<Map<String, Object>> historyList = (List<Map<String, Object>>) aPost.get("history");

		Map<String, Object> aLatestElement = loggedInClass.getLatestElement(aPost);
		Map<String, Object> aChange = new HashMap();
		String anOriginalSubject = (String) aLatestElement.get("subject");
		String anOriginalContent = (String) aLatestElement.get("content");
		String aNewSubject = anOriginalSubject + "updated on" + new Date(System.currentTimeMillis());
		String aNewContent = anOriginalContent + "updated on" +  new Date(System.currentTimeMillis());
		loggedInClass.updatePost(anExistingID, aNewSubject, aNewContent, aHistorySize + 1);
	}
	public static void updateFollowup() throws ClientProtocolException, NotLoggedInException, IOException {
//		String anExistingID = "l5s7ag4zea40w";	
		String anExistingID = "l5wmbry02vz5h7";	// l5wmbry02vz5h7
		Map<String, Object> aPost = loggedInClass.getPost(anExistingID );
		List<Map<String, Object>> aChildren = (List<Map<String, Object>>) aPost.get("children");
		Map<String, Object> aChild = aChildren.get(0);
		String aContent = (String) aChild.get("subject");
		String aNewContent = aContent + " updated " + new Date(System.currentTimeMillis());
//		Map<String, Object> aLatestElement = loggedInClass.getLatestElement(aPost);
//		
//		String anOriginalSubject = (String) aLatestElement.get("subject");
//		String anOriginalContent = (String) aLatestElement.get("content");
//		String aNewSubject = anOriginalSubject + new Date(System.currentTimeMillis());
//		String aNewContent = anOriginalContent +  new Date(System.currentTimeMillis());
		loggedInClass.updateFollowup(anExistingID, aNewContent);
	}
	public static void createFollowup() throws ClientProtocolException, NotLoggedInException, IOException {
		String anExistingID = "l5uj1iv368q19x";	
		String aContent = "AutoFollowupContent" + new Date(System.currentTimeMillis());
		loggedInClass.createFollowup(anExistingID,  aContent);
	}
	//l5qdpkng17v3k0
	//l5mo3pe7wm84dr
	public static void markAsDuplicate() throws ClientProtocolException, NotLoggedInException, IOException {
	
		String aDuplicate = "l5qdpkng17v3k0";	// l5wmbry02vz5h7St
		String aNewPostId = "l5mo3pe7wm84dr";
		loggedInClass.markPostAsDuplicate(aNewPostId, aDuplicate);
		

	}
	public static void createPost() throws ClientProtocolException, NotLoggedInException, IOException {
		String aSubject = "AutoSubject" + System.currentTimeMillis();
		String aContent = "AutoContent";
		String aRecipients = "i588uvywlmn3b5,h68jepo6q4z3bk";
//		feed_groups	:	i588uvywlmn3b5,h68jepo6q4z3bk
//		String[] aPostTags = new String[] {"hw1"};
		String[] aPostTags = new String[] {"hw2"};
		loggedInClass.createPost(aSubject, aContent, Arrays.asList(aPostTags), aRecipients);
	}
	public static void main (String[] args) {
//	    String[] myArgs = {anOutDir};
//    	DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssz");
//    	DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
//    	DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssz");
//
//
////        String aDataString = "2022-07-12T07:29:25Z";
//        String aDataString = "2022-07-14T07:54:07Z";
//
////        String aDataString = "2022-07-12";
//
//    	try {
//			Date aDate = df.parse(aDataString.replace('T', ' ').replace("Z", "UTC"));
//			String anESTString = aDate.toString();
//		} catch (ParseException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}

	    try {
			loggedInClass = ParameterizedTester.loginToPiazzaClass();
//			updateFollowup();
//			updatePost();
//			createPost();
//			createFollowup();
//			markAsDuplicate();

			writePosts();

//			
//	    	//	id	:	l5mo3pe7wm84dr
//	    		
////	    		String id = "l5mo3pe7wm84dr";
//	    		String diary_id = "l5hurqwc7pcce";
//	    		//l5mo2o1e38x3ry
////	    		String dummy_question_id = "l5mo3pe7wm84dr";
//	    		String updated_post_id = "l5mo2o1e38x3ry";
////
//	    		Map<String, Object> aPost = aLoggedInClass.getPost(updated_post_id );
//	    		Map<String, Object> aLatestElement = aLoggedInClass.getLatestElement(aPost);
//	    		Map<String, Object> aChange = new HashMap();
//	    		String anOriginalSubject = (String) aLatestElement.get("subject");
//	    		String anOriginalContent = (String) aLatestElement.get("content");
//	    		aChange.put("cid", updated_post_id);
//
//	    		Date aDate = new Date(System.currentTimeMillis());
//	    		String aDateString = aDate.toString();
//	    		aChange.put("subject", anOriginalSubject + aDateString );
//	    		aChange.put("content", anOriginalContent + aDateString );
////	    		aLoggedInClass.updatePost(aChange);
//
//
////	    		String aContentQuestion = aLoggedInClass.getLatestContent(aPost);
////	    		String anUpdatedContent = aContentQuestion + "\n" + "update at " + new Date (System.currentTimeMillis());
////	    		aLoggedInClass.updatePost(dummy_question_id, anUpdatedContent);
//
//	    		
////	    		
////	    		aLoggedInClass.createFollowup(id, "auto follow up " + System.currentTimeMillis());
////	    		
//
//
////			ParameterizedTester.main(myArgs);
//	    		String aSubject = "TestSubject";
//	    		String aContent = "TestContent";
//	    		String[] aRecipients = new String[] {"i588uvywlmn3b5"};
////	    		String[] aPostTags = new String[] {"hw1"};
//				String[] aPostTags = new String[] {"hw1", "instructor-note"};
//
//    		aLoggedInClass.createPost(aSubject, aContent, Arrays.asList(aPostTags), Arrays.asList(aRecipients));
//
//			//l5nxlawp2qd3ek
////			aLoggedInClass.createInstructorAnswer("l5nxlawp2qd3ek", "dummy instructor answer  " + System.currentTimeMillis());
////			l5mo2o1e38x3ry
////			ParameterizedTester.processSessionPosts(aPostsFile, 0, Integer.MAX_VALUE, 0, Long.MAX_VALUE);

		} catch (IOException | LoginFailedException | NotLoggedInException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
 
}
