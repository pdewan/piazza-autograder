package piazza.nlp.redux;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import piazza.PiazzaSession;

public interface PiazzaForum extends DiscussionForum {
	
	public Post getPost(int postNumber); // get post from number, instead of ID
	public List<APiazzaPostPreview> getFeed(); // get post headers from the feed
	// TODO

	public PiazzaSession getForumSession(); // TODO: make ForumSession interface and put this in DiscussionForum?
	public PiazzaSession swapForumSession(PiazzaSession newSession); // returns the current session
	
}
