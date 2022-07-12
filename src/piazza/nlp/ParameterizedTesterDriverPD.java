package piazza.nlp;

import java.io.IOException;

import piazza.LoginFailedException;
import piazza.NotLoggedInException;

public class ParameterizedTesterDriverPD {
	public static void main (String[] args) {
		String aPostsFile = "C:\\Users\\dewan\\Downloads\\PiazzaOutput\\allposts.csv";
		String anIncompletePostsFile = "C:\\Users\\dewan\\Downloads\\PiazzaOutput\\incompleteposts.json";
	    String[] myArgs = {aPostsFile, anIncompletePostsFile};
	    try {
			ParameterizedTester.main(myArgs);
		} catch (IOException | LoginFailedException | NotLoggedInException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
 
}
