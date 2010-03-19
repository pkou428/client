package tweendeck;

import java.awt.Desktop;
import java.awt.FlowLayout;
import java.net.URL;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpringLayout;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent.EventType;

import twitter4j.Status;
/*
 * 詳細表示部
 * 
 * */
class TweetDetail{
	JPanel tweetDetailPanel;
	JEditorPane tweetDetailPane;
	IconList iconList;
	ImageIcon icon;
	JLabel iconLabel;
	HashMap<String, UserData> userList;
	
	class HyperlinkHandler implements HyperlinkListener{
		public void hyperlinkUpdate(HyperlinkEvent e) {
			if (e.getEventType() == EventType.ACTIVATED) {	//クリックされた時
				URL url = e.getURL();

				//デフォルトのブラウザーを使ってリンク先を表示
				Desktop dp = Desktop.getDesktop();
				try {
					dp.browse(url.toURI());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	
	public TweetDetail(IconList iconList, HashMap<String, UserData> userList){
		tweetDetailPanel = new JPanel();
		tweetDetailPane = new JEditorPane("text/html","Detail here!!");
		tweetDetailPane.setEditable(false);
		tweetDetailPane.addHyperlinkListener(new HyperlinkHandler());
		iconLabel = new JLabel();
		this.userList = userList;
		this.iconList = iconList;
		JScrollPane scrollPane = new JScrollPane(tweetDetailPane,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		tweetDetailPanel.setLayout(new FlowLayout());
		
		SpringLayout layout = new SpringLayout();
		tweetDetailPanel.setLayout(layout);
		layout.putConstraint(SpringLayout.NORTH, scrollPane, 0, SpringLayout.NORTH, tweetDetailPanel);
		layout.putConstraint(SpringLayout.WEST, scrollPane, 50, SpringLayout.WEST, tweetDetailPanel);
		layout.putConstraint(SpringLayout.EAST, scrollPane, 0, SpringLayout.EAST, tweetDetailPanel);
		layout.putConstraint(SpringLayout.SOUTH, scrollPane,0, SpringLayout.SOUTH, tweetDetailPanel);
		layout.putConstraint(SpringLayout.NORTH, iconLabel, 0, SpringLayout.NORTH, tweetDetailPanel);
		layout.putConstraint(SpringLayout.WEST, iconLabel, 0, SpringLayout.WEST, tweetDetailPanel);
		layout.putConstraint(SpringLayout.EAST, iconLabel, 0, SpringLayout.WEST, scrollPane);
		layout.putConstraint(SpringLayout.SOUTH, iconLabel,0, SpringLayout.SOUTH, tweetDetailPanel);
		
		tweetDetailPanel.add(iconLabel);
		tweetDetailPanel.add(scrollPane);
	}
	public JPanel getTweetDetailPanel(){
		return tweetDetailPanel;
	}
	
	public void setDetail(ListEntry entry){
		iconLabel.setIcon(entry.getUserData().getIcon());
		tweetDetailPane.setText(convertHyperLink(entry.getUserData().getTweetText(entry.getTweetId())));
	}
	private String convertHyperLink(String arg){
		Pattern conv = Pattern.compile("(http://|https://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+",
			    Pattern.CASE_INSENSITIVE);
		Matcher matcher = conv.matcher(arg);
		return matcher.replaceAll("<a href=\"$0\">$0</a>");
	}
}