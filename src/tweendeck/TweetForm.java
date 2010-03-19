package tweendeck;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import twitter4j.Twitter;

/*
 * ツイートフォーム
 * */
class TweetForm{
	JPanel		tweetFormPanel;
	JTextField	tweetFormField;
	StatusTimeLine statusTimeLine;
	Twitter twitter;
	
	public void setStatusTimeLine(StatusTimeLine arg){
		statusTimeLine = arg;
	}
	public TweetForm(Twitter twitter){
		this.twitter = twitter;
		tweetFormPanel = new JPanel();
		tweetFormField = new JTextField();
		SpringLayout layout = new SpringLayout();
		tweetFormPanel.setLayout(layout);
		layout.putConstraint(SpringLayout.NORTH, tweetFormField, 0, SpringLayout.NORTH, tweetFormPanel);
		layout.putConstraint(SpringLayout.WEST, tweetFormField, 0, SpringLayout.WEST, tweetFormPanel);
		layout.putConstraint(SpringLayout.EAST, tweetFormField, 0, SpringLayout.EAST, tweetFormPanel);
		layout.putConstraint(SpringLayout.SOUTH, tweetFormField,0, SpringLayout.SOUTH, tweetFormPanel);
		tweetFormPanel.add(tweetFormField);
		tweetFormField.addKeyListener(new KeyAdapter(){
			@Override
			public void keyPressed(KeyEvent ke){
				if(tweetFormField.getText().length() <= 140){
					tweetFormField.setForeground(Color.BLACK);
					if(ke.isControlDown() && ke.getKeyCode() == 10){
						sendTweet();
					}
				}else{
					tweetFormField.setForeground(Color.RED);
				}
			}
		});
	}

	/*
	 * JTextFieldを返す
	 * */
	public JPanel getTweetFormPanel(){
		return tweetFormPanel;
	}
	
	/*
	 * 文字列を受け取りtweetFormに追加
	 * */
	public void addString(String arg){
		tweetFormField.setText(tweetFormField.getText() + arg + " ");
		if(tweetFormField.getText().length() > 140)
			tweetFormField.setForeground(Color.RED);
	}
	
	/*
	 * ツイートを送信
	 * */
	private void sendTweet(){
		SendTweetThread sendThread = new SendTweetThread();
		sendThread.start();
		System.out.println("sendTweet done?");

	}
	class SendTweetThread extends Thread{
		boolean flag;
		
		public SendTweetThread(){
			flag = false;
		}
		
		@Override
		public void run(){
			tweetFormField.setEditable(false);
			try{
				twitter.updateStatus(tweetFormField.getText());
			}
			catch (Exception ex){
				flag = true;
			}
			tweetFormField.setEditable(true);
			SwingUtilities.invokeLater(new Runnable(){
				public void run(){
					if(flag){
						JOptionPane.showMessageDialog(null, "updateStatus Failed");
					}else{
						tweetFormField.setText("");
						statusTimeLine.updateAllFriends();
					}
				}
			});
		}
	}
}


