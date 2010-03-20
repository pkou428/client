package tweendeck;

import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Properties;

import twitter4j.Twitter;
import javax.swing.*;

public class TweenDeck extends JFrame{
	Twitter twitter;
	LoginForm loginForm;
	
	TweenDeck(){
		File iniFile = new File("./tweendeck.ini");
		String loginName;
		String loginPassword;
		if(iniFile.exists()){
			Properties prop = new Properties();

			try{
				prop.load(new FileInputStream(iniFile));
				loginName = prop.getProperty("name");
				loginPassword = prop.getProperty("password");
				if(loginName != null && loginPassword != null){
					System.out.println("from iniFile : " + loginName + loginPassword);
					twitter = new Twitter(loginName,loginPassword);
					twitter.verifyCredentials();
				}else{
					do{
						System.out.println("ini file exists.but id or password doesn't exist");
						loginForm = new LoginForm();

						loginName = loginForm.getUserName();
						loginPassword = loginForm.getPassword();
						System.out.println("from loginform : " + loginName + loginPassword);
						try{
							twitter = new Twitter(loginName,loginPassword);
							twitter.verifyCredentials();
						}catch(Exception ex){
							JOptionPane.showMessageDialog(null, "Login failed.");
							continue;
						}
						break;
					}while(true);
				}
			}catch(Exception ex){
				JOptionPane.showMessageDialog(null, "Login failed. Ini File's something wrong.");
				System.exit(-1);
			}

		}else{
			do{
				loginForm = new LoginForm();

				loginName = loginForm.getUserName();
				loginPassword = loginForm.getPassword();
				System.out.println("from loginform : " + loginName + loginPassword);
				try{
					twitter = new Twitter(loginName,loginPassword);
					twitter.verifyCredentials();
				}catch(Exception ex){
					JOptionPane.showMessageDialog(null, "Login failed.");
					continue;
				}
				break;
			}while(true);
			if(loginForm.getCheckState()){
				BufferedWriter configWriter;
				try{
					if(iniFile.createNewFile()){
						configWriter = new BufferedWriter(new FileWriter(iniFile));
						configWriter.write("name=" + loginForm.getUserName() + "\n");
						configWriter.newLine();
						configWriter.write("password=" + loginForm.getPassword());
						configWriter.close();
					}else{
						JOptionPane.showMessageDialog(null, "Creating ini File failed");
						System.out.println("exiting");
						System.exit(-1);
					}
				}catch(Exception ex){
					ex.printStackTrace();
				}
				/*
				if(iniFile.exists()){
					System.out.println("file exists");
				}
				third
				*/
			}
		}
		JPanel p = new JPanel();
		SpringLayout layout = new SpringLayout();
		p.setLayout(layout);
		TweetForm	tweetForm = new TweetForm(twitter);
		IconList iconList = new IconList();
		HashMap<String, UserData> userList = new HashMap<String, UserData>();
		TweetDetail	tweetDetail = new TweetDetail(iconList, userList);
		
		/*
		tweetForm.setMinimumSize(new Dimension(tweetForm.getMinimumSize().width,40));
		tweetForm.setMaximumSize(new Dimension(tweetForm.getMaximumSize().width,40));
		tweetForm.setPreferredSize(new Dimension(400,40));
		*/
		
		StatusTimeLine tl = new StatusTimeLine(twitter, tweetForm, tweetDetail, iconList, userList);
		tweetForm.setStatusTimeLine(tl);
		
		/*
		 * タイムラインパネルのレイアウト
		 * */
		layout.putConstraint(SpringLayout.NORTH, tl.getTimeLinePanel(), 5, SpringLayout.NORTH, p);
		layout.putConstraint(SpringLayout.WEST, tl.getTimeLinePanel(), 5, SpringLayout.WEST, p);
		layout.putConstraint(SpringLayout.EAST, tl.getTimeLinePanel(), -5, SpringLayout.EAST, p);
		layout.putConstraint(SpringLayout.SOUTH, tl.getTimeLinePanel(), -90, SpringLayout.SOUTH, p);
		
		/*
		 * ツイート表示パネルのレイアウト
		 * */
		layout.putConstraint(SpringLayout.NORTH, tweetDetail.getTweetDetailPanel(), 5, SpringLayout.SOUTH,tl.getTimeLinePanel());
		layout.putConstraint(SpringLayout.WEST, tweetDetail.getTweetDetailPanel(), 5, SpringLayout.WEST, p);
		layout.putConstraint(SpringLayout.EAST, tweetDetail.getTweetDetailPanel(), -5, SpringLayout.EAST, p);
		layout.putConstraint(SpringLayout.SOUTH, tweetDetail.getTweetDetailPanel(), -40, SpringLayout.SOUTH, p);
		
		/*
		 * ツイート投稿フォームのレイアウト
		 * */
		layout.putConstraint(SpringLayout.NORTH, tweetForm.getTweetFormPanel(), 5, SpringLayout.SOUTH, tweetDetail.getTweetDetailPanel());
		layout.putConstraint(SpringLayout.WEST, tweetForm.getTweetFormPanel(), 5, SpringLayout.WEST, p);
		layout.putConstraint(SpringLayout.EAST, tweetForm.getTweetFormPanel(), -5, SpringLayout.EAST, p);
		layout.putConstraint(SpringLayout.SOUTH, tweetForm.getTweetFormPanel(), -5, SpringLayout.SOUTH, p);
		p.add(tl.getTimeLinePanel());
		p.add(tweetForm.getTweetFormPanel());
		p.add(tweetDetail.getTweetDetailPanel());
		this.getContentPane().add(p);
//		  JProgressBar progressBar = new JProgressBar(0, 100);
//		JOptionPane.showMessageDialog(null, progressBar	);
		
	}
	public static void main(String args[]){
		TweenDeck frame = new TweenDeck();
		frame.setTitle("TW full cache");
		frame.setSize(400,400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}

