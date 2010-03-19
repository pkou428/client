package tweendeck;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.WeakHashMap;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import twitter4j.Status;
import twitter4j.User;



class UserData{
	String userName;
	String screenName;
	HashMap<Long, String> tweetList;
	WeakHashMap<Long, JPanel> panelList;
	ImageIcon icon;

	public UserData(Status status, ImageIcon icon){
		User user = status.getUser();
		this.screenName = user.getScreenName();
		this.userName = user.getName();
		this.tweetList = new HashMap<Long, String>();
		this.panelList = new WeakHashMap<Long, JPanel>();
		this.icon = icon;
	}
	public String getScreenName(){
		return this.screenName;
	}
	public String getTweetText(long id){
		return tweetList.get(id);
	}
	public String getUserName(){
		return this.userName;
	}
	public void addTweet(long id, String tweet){
		panelList.put(id, createTweetPanel(tweet));
		tweetList.put(id, tweet);
		
	}
	public ImageIcon getIcon(){
		return this.icon;
	}
	private JPanel createTweetPanel(String tweetText){
		JPanel tmpPanel = new JPanel();
		tmpPanel.setPreferredSize(new Dimension(290,50));
		JLabel tmpIcon = new JLabel();
		tmpIcon.setIcon(icon);
		/*
		tmpIcon.setMaximumSize(new Dimension(50,50));
		tmpIcon.setMinimumSize(new Dimension(50,50));
		tmpIcon.setSize(new Dimension(50,50));
		*/
		JTextArea tmp = new JTextArea(userName + " >\n" + tweetText);
		tmp.setFocusable(true);
		tmp.setEditable(false);
//		tmp.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		tmp.setLineWrap(true);
//		tmp.setPreferredSize(new Dimension(200,50));
		/*
		tmp.setRows(3);
		if(isSelected){
			tmp.setBackground(list.getSelectionBackground());

		}else{
			tmp.setBackground(index%2==0 ? evenColor : list.getBackground());
			tmp.setForeground(list.getForeground());
		}
		*/
		tmpPanel.setLayout(new FlowLayout());
		tmp.setPreferredSize(new Dimension(220,50));
		tmpPanel.add(tmp);
		tmpPanel.add(tmpIcon);
//		System.out.println("created " + userName + "'s Panel. TweetText : " + tweetText);
		return tmpPanel;
	}
	public JPanel getTweetPanel(long id){
//		System.out.println("returning " + userName + "'s Panel. TweetID : " + id);
//		if(this.screenName.matches("junkdiver")){
//			System.out.println("number of panelList : " + panelList.size());
//			if(!panelList.containsKey(id)){
//				System.out.println(id + "'s panel doesn't exist");
//				panelList.put(id, createTweetPanel(tweetList.get(id)));
//			}else{
//				System.out.println(id + "'s panel exists");
//			}
//			return panelList.get(id);
//		}else{
//			if(!panelList.containsKey(id)){
//				panelList.put(id, createTweetPanel(tweetList.get(id)));
//			}
			return panelList.get(id);
//		}
	}
}
