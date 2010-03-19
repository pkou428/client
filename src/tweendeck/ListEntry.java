package tweendeck;

public class ListEntry {
	UserData userData;
	long tweetId;
	
	public ListEntry(UserData userData, long tweetId){
		this.userData = userData;
		this.tweetId = tweetId;
	}
	public UserData getUserData(){
		return this.userData;
	}
	public long getTweetId(){
		return this.tweetId;
	}
}
