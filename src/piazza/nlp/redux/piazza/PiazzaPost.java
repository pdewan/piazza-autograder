package piazza.nlp.redux.piazza;

import piazza.nlp.redux.general.ForumPost;

public interface PiazzaPost extends ForumPost {

	public int getPostNumber();
	public int getRevisionNumber();
	
}
