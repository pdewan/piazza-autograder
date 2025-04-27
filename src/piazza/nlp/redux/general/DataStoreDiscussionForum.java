package piazza.nlp.redux.general;

public interface DataStoreDiscussionForum {
	
	public DiscussionForum getForum(); // NOTE: changed from IS-A to HAS-A between DataStoreDiscussionForum and DiscussionForum
	
	public boolean registerData(String dataName, Class dataType, Object dataValue); // returns false if already exists
	public boolean isRegistered(String dataName); // checks if an element with the given name has been registered in the data store
	public Object getDataValue(String dataName);
	public Class getDataClass(String dataName); // returns registered type for dataName or null if dataName is not registered
	public boolean updateData(String dataName, Object dataValue); // returns false if does not already exist
    
	public int createNewDataPost(); // returns the post number -- maybe make this set an environment var
	public int overwriteWithDataPost(int postNumber); // maybe in the main code, check if the environment var it set and use this if so
	public ForumPost getDataPost();
	
	// NOT DOING NOW: too complicated bc how do agents get these numbers? changes from EICS model: allows for separate data posts -- read entry now takes the data post number
}
