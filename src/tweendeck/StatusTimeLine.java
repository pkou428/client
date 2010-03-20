package tweendeck;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import twitter4j.PagableResponseList;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.User;
import twitter4j.UserList;

class StatusTimeLine{

	/*
	 * タイムラインカラムを管理するクラス
	 * コンストラクタをオーバーロードして取得するTLを選べるようにすべき（All Friendsかリストとか）
	 * */
	class TimeLineColumn{
		JPanel columnPanel;
		JList statusJList;
		long latestStatusId;
		int updateInterval;
		JLabel titleLabel;
		JPopupMenu popup;
		JPanel menuPanel;

		ListEntry selectedItem;		
		DefaultListModel listModel;//カラム内のツイートを管理するオブジェクト
		/*
		 * コンストラクタ
		 * */
		public TimeLineColumn(String title){
			columnPanel = new JPanel();
			columnPanel.setMaximumSize(new Dimension(300,columnPanel.getMaximumSize().height));
			columnPanel.setMinimumSize(new Dimension(300,columnPanel.getMinimumSize().height));
			columnPanel.setPreferredSize(new Dimension(300,210));
			listModel = new DefaultListModel();
			titleLabel = new JLabel(title);
			titleLabel.setPreferredSize(new Dimension(300,40));
			statusJList = new JList(listModel);
			statusJList.setCellRenderer(new TimeLineCellRenderer(iconList));
			menuPanel = new JPanel();
			menuPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		    popup = new JPopupMenu();
			popup.add(new ActionRetweet("Retweet"));
			popup.add(new ActionReply("Reply"));
			//マウスリスナへ登録
			MouseListener mouseListener = new MouseAdapter() {
				@Override
				public void mouseReleased(MouseEvent me) {

					if(SwingUtilities.isRightMouseButton(me)){
						int index = statusJList.locationToIndex(me.getPoint());
						if(index >= 0){
							statusJList.setSelectedIndex(index);
							selectedItem = (ListEntry)listModel.get(statusJList.getSelectedIndex());
							tweetDetail.setDetail(selectedItem);
//							JOptionPane.showMessageDialog(statusJList, "right clicked");
//							tweetForm.addString("@" + selectedItem.getUser().getScreenName());
							showPopup(me, statusJList);
						}
					}else if(SwingUtilities.isLeftMouseButton(me)){
						selectedItem = (ListEntry)listModel.get(statusJList.getSelectedIndex());
						tweetDetail.setDetail(selectedItem);
//						JOptionPane.showMessageDialog(statusJList, "left clicked");
					}
				}
			};
			statusJList.addMouseListener(mouseListener);
			JScrollPane scrollPane = new JScrollPane(statusJList,
					ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, // 垂直バー
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

			SpringLayout layout = new SpringLayout();
			columnPanel.setLayout(layout);
			layout.putConstraint(SpringLayout.NORTH, titleLabel, 5, SpringLayout.NORTH, columnPanel);
			layout.putConstraint(SpringLayout.WEST, titleLabel, 5, SpringLayout.WEST, columnPanel);
			layout.putConstraint(SpringLayout.EAST, titleLabel, -200, SpringLayout.EAST, columnPanel);
			layout.putConstraint(SpringLayout.NORTH, menuPanel, 5, SpringLayout.NORTH, columnPanel);
			layout.putConstraint(SpringLayout.WEST, menuPanel, 5, SpringLayout.WEST, titleLabel);
			layout.putConstraint(SpringLayout.EAST, menuPanel, -5, SpringLayout.EAST, columnPanel);

			layout.putConstraint(SpringLayout.NORTH, scrollPane, 40, SpringLayout.NORTH, columnPanel);
			layout.putConstraint(SpringLayout.WEST, scrollPane, 0, SpringLayout.WEST, columnPanel);
			layout.putConstraint(SpringLayout.EAST, scrollPane, 0, SpringLayout.EAST, columnPanel);
			layout.putConstraint(SpringLayout.SOUTH, scrollPane,0, SpringLayout.SOUTH, columnPanel);
			columnPanel.add(scrollPane);
			columnPanel.add(titleLabel);
			columnPanel.add(menuPanel);
		}
		
		private void showPopup(MouseEvent me, JList list){
			if(me.isPopupTrigger()){
				popup.show(list, me.getX(), me.getY());
			}
		}


		/*
		 * 自動取得用スレッド
		 * 一定時間ごとにupdateTimeLineメソッドを呼び出す
		 * */
		class TimerThread extends Thread{
			boolean running;
			public TimerThread(){
				running = true;
			}
			@Override
			public void run(){
				while(running){
					try{
						Thread.sleep(60000L);
					}
					catch (Exception ex){
						ex.printStackTrace();
					}
					SwingUtilities.invokeLater(new Runnable(){
						public void run(){
							updateTimeLine();
						}
					});
					System.out.println("calling updateTimeLine" + this.hashCode());
				}
			}
			public void stopRunning(){
				running = false;
			}
		}

		/*
		 * カラム用パネルを返す
		 * */
		public JPanel getColumnPanel(){
			return columnPanel;
		}
		public final void startUpdate(){
			
			
			TimerThread timer = new TimerThread();
			reloadTimeLine();
			timer.start();
		}
		public void setTitle(String title){
			titleLabel.setText(title);
		}
		public String getTitle(){
			return titleLabel.getText();
		}
		/*
		 * タイムラインカラムの再表示
		 * */
		private void reloadTimeLine(){

			JButton add = new JButton("add List");
			add.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent ae){
					AddList();
				}
			});
			menuPanel.add(add);
			try{
				ResponseList<Status> statusList = twitter.getFriendsTimeline(new Paging(1, 100));
				for(int i = 0; i < statusList.size(); i++){
					UserData tmp;
					String tmpName = statusList.get(i).getUser().getName();
					if(userList.containsKey(tmpName) == false){
						tmp = new UserData(statusList.get(i), iconList.getImageIcon(statusList.get(i).getUser()));
						userList.put(tmpName, tmp);
					}else{
						System.out.println(tmpName + "'s Data already exists");
						tmp = userList.get(tmpName);
					}
					long tmpId = statusList.get(i).getId();
					tmp.addTweet(tmpId, statusList.get(i).getText());
					listModel.addElement(new ListEntry(tmp, tmpId));
				}
				latestStatusId = statusList.get(0).getId();

			}
			catch (Exception ex){
				ex.printStackTrace();
			}
		}
		
		/*
		 * タイムラインの更新
		 * 自動更新用スレッドと外部からの２つからアクセスされる可能性がアル
		 * */
		/*
		 * API の実行回数制限について(公式マニュアルのコピペ
		 * Twitter の API は、60分間に150回まで実行できる。
		 * この実行回数制限を超えた状態でさらにリクエストを送った場合、HTTPステータスコード 400 が返る。
		 * 認証の必要なもの、不要なものの両方が実行回数制限の対象となる(以前は実行回数制限の対象外であった
		 * public_timeline の取得も、現在は対象となっている)。
		 * 認証の必要なものはユーザID(アカウント)単位で、認証の不要なものはIPアドレス単位で、実行回数のカウントを行なう。
		 * [訳者による注記]
		 * Twitter の運用状況によっては API 制限がより厳しく設定されることがある(60分間に20回まで、など)。
		 * なお、POST系API(発言の投稿、ダイレクトメッセージの送信、指定ユーザのフォロー、お気に入りの登録、など)は、
		 * この実行回数制限には関係なく、何回でも実行できる。
		 * ただし、POST系APIであっても、一定時間当たりの使用回数があまりにも多い場合は、使用制限をすることがある。
		 * この実行回数制限を適用されると都合が悪い場合は、理由を明示の上、Twitter 開発者にコンタクトを取ること。
		 * 納得できる理由が示されれば、当該ユーザを、実行回数制限適用外のスクリーン名のリストに入れる。
		 * (ただし、この実行回数制限適用外のリストに登録されても、1時間に最大2万回のリクエストしか受け付けない)
		 * rate_limit_status という「アカウント関連のAPI」を使うことで、実際の API 制限の実施状況を調べることができる。
		 *      *「発言の投稿(statuses/update)」、「following(friendships/create)」等、1日に実行可能な上限回数が別途決められているものもある
		 *             (詳細は http://help.twitter.com/forums/10711/entries/15364 を参照)
		 *                       - 発言の投稿: 1日最大1000件まで
		 *                       - ダイレクトメッセージの送信: 1日最大1000件まで
		 *                       - following: 具体的な数字は明示されていない(1日最大2000件まで、らしい)
		 * API 実行回数制限の緩和
		 * (2010年1月から) OAuth 経由で新しい API エンドポイント(http://api.twitter.com/ で始まる URL) にアクセスする場合に限って、
		 * API を60分間に350回まで実行することができる
		 *	 (訳者注: 緩和措置導入直後は60分間に450回まで実行可能だったが、2010年1月12日のハイチ地震発生時のサーバー負荷状況を考慮した結果、
		 *				2010年1月24日以降、60分間に350回まで実行可能とすることにした模様。将来的には、60分間に1500回まで実行可能にすることが予告されている)。
		 * なお、この「緩和措置」が有効な状態でも rate_limit_status は、依然として hourly-limit の値として 150 を返す。
		 * 「緩和措置」が有効になっていることを確認するには、API 実行要求(httpリクエスト)に対する応答中のhttpレスポンスヘッダを見ればよい。
		 * httpレスポンスヘッダ中の
		 *         X-RateLimit-Limit:
		 * が、60分間(1時間)に実行可能な回数を示し、
		 *         X-RateLimit-Remaining:
		 * が、あと何回実行可能なのかを示している
		 * */
		synchronized public void updateTimeLine(){
			try{
				ResponseList<Status> statusList = twitter.getFriendsTimeline(new Paging(1, 100, latestStatusId));
				if(statusList.size()>0){
					System.out.println("1");
					for(int i = statusList.size()-1; i >= 0; i--){
						UserData tmp;
						String tmpName = statusList.get(i).getUser().getName();
						if(userList.containsKey(tmpName) == false){
							tmp = new UserData(statusList.get(i), iconList.getImageIcon(statusList.get(i).getUser()));
							userList.put(tmpName, tmp);
						}else{
							System.out.println(tmpName + "'s Data already exists");
							tmp = userList.get(tmpName);
						}
						long tmpId = statusList.get(i).getId();
						tmp.addTweet(tmpId, statusList.get(i).getText());
						listModel.add(0,new ListEntry(tmp, tmpId));
					}
					System.out.println("1.5");
					
					latestStatusId = statusList.get(0).getId();
					UpdateTimeLineThread thread = new UpdateTimeLineThread(statusList);
					thread.start();
				}

			}
			catch(Exception ex){
				System.out.println(ex.getMessage());
			}
		}
		class UpdateTimeLineThread extends Thread{
			Status status;
			ResponseList<Status> statusList;
			public UpdateTimeLineThread(ResponseList<Status> statusList){
				this.statusList = statusList;
			}
			@Override
			public void run(){
				int index = statusJList.getSelectedIndex();
				for(int i = 0; i < listList.size(); i++){
					try{
						listList.get(i).updateTimeLine(statusList);
					}
					catch (Exception ex){
						JOptionPane.showMessageDialog(null, "updateStatus Failed");
					}
				}
				statusJList.setSelectedIndex(statusList.size() + index);
			}
		}

		class ActionRetweet extends AbstractAction{
			public ActionRetweet(String title){
				super(title);
			}
			public void actionPerformed(ActionEvent e) {
				tweetForm.addString("RT @" + selectedItem.getUserData().getScreenName()
						+ ": " + selectedItem.getUserData().getTweetText(selectedItem.getTweetId()));
			}

		}
		class ActionReply extends AbstractAction{
			public ActionReply(String title){
				super(title);
			}
			public void actionPerformed(ActionEvent e) {
				tweetForm.addString("@" + selectedItem.getUserData().getScreenName() + " ");
			}
		}

	}


	/*
	 * リスト用カラム
	 * リスト管理者のScreenNameとlistIDを渡してやって作成
	 * AllFriendsカラムから最新TLを受け取って管理するリストに所属してるメンバーのツイートのみをカラムへ追加していく
	 * */
	class ListColumn extends TimeLineColumn{
		PagableResponseList<User> list;
		String listOwnerName;
		HashMap<String, Integer> listMap;
		int listId;
		
		public ListColumn(String listOwnerName, int listId, String title){
			super(title);
			try{
				long cursor = -1;
				listMap = new HashMap<String, Integer>();
				this.listOwnerName = listOwnerName;
				this.listId = listId;
				do{
					list = twitter.getUserListMembers(listOwnerName, listId, cursor);
					for(int i = 0; i < list.size(); i++){
						listMap.put(list.get(i).getName(), new Integer(list.get(i).getId()));
					}
					cursor = list.getNextCursor();
				}while(cursor != 0);
				JButton remove = new JButton("remove List");
				remove.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent ae){
						deleteThisColumn();
					}
				});
				menuPanel.add(remove);
				/*
				this.columnPanel = new JPanel();
				columnPanel.setMaximumSize(new Dimension(300,columnPanel.getMaximumSize().height));
				columnPanel.setMinimumSize(new Dimension(300,columnPanel.getMinimumSize().height));
				columnPanel.setPreferredSize(new Dimension(280,210));
				listModel = new DefaultListModel();
				statusJList = new JList(listModel);
				statusJList.setCellRenderer(new TimeLineCellRenderer());
				//マウスリスナへ登録
				MouseListener mouseListener = new MouseAdapter() {
					public void mouseClicked(MouseEvent me) {
						Status selectedItem = (Status)listModel.get(statusJList.getSelectedIndex());
						if(SwingUtilities.isRightMouseButton(me)){
							//選択してるリストのオブジェクトなりを取得して
							//テキスト内容を取得できるかテスト
							JOptionPane.showMessageDialog(null, "right clicked");
							tweetForm.addString("@" + selectedItem.getUser().getScreenName());
						}else if(SwingUtilities.isLeftMouseButton(me)){
							tweetDetail.setText(selectedItem.getText());
						}
					}
				};
				statusJList.addMouseListener(mouseListener);
				JScrollPane scrollPane = new JScrollPane(statusJList,
						JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, // 垂直バー
						JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

				SpringLayout layout = new SpringLayout();
				columnPanel.setLayout(layout);
				layout.putConstraint(SpringLayout.NORTH, scrollPane, 0, SpringLayout.NORTH, columnPanel);
				layout.putConstraint(SpringLayout.WEST, scrollPane, 0, SpringLayout.WEST, columnPanel);
				layout.putConstraint(SpringLayout.EAST, scrollPane, 0, SpringLayout.EAST, columnPanel);
				layout.putConstraint(SpringLayout.SOUTH, scrollPane,0, SpringLayout.SOUTH, columnPanel);
				columnPanel.add(scrollPane);
				*/
				reloadTimeLine();
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
		}
		public void updateTimeLine(ResponseList<Status> timeLineList){
			int index = statusJList.getSelectedIndex();
			int counter = 0;
			ArrayList<ListEntry> listEntry = new ArrayList<ListEntry>();
			for(int i = timeLineList.size()-1; i >=0; i--){
				if(listMap.containsKey(timeLineList.get(i).getUser().getName())){
					UserData tmp;
					String tmpName = timeLineList.get(i).getUser().getName();
					if(userList.containsKey(tmpName) == false){
						tmp = new UserData(timeLineList.get(i), iconList.getImageIcon(timeLineList.get(i).getUser()));
						userList.put(tmpName, tmp);
					}else{
//						System.out.println(tmpName + "'s Data already exists");
						tmp = userList.get(tmpName);
					}
					long tmpId = timeLineList.get(i).getId();
					tmp.addTweet(tmpId, timeLineList.get(i).getText());
					listEntry.add(new ListEntry(tmp, tmpId));
					counter++;
				}
			}
			if(index >= 0)
				statusJList.setSelectedIndex(counter + index);
			SwingUtilities.invokeLater(new AddStatusThread(listEntry));
		}
		class AddStatusThread implements Runnable{
			ArrayList<ListEntry> listEntry;
			public AddStatusThread(ArrayList<ListEntry> listEntry){
				this.listEntry = listEntry;
			}
			public void run(){
				System.out.println("thread start");
				for(int i = 0;i < listEntry.size(); i++){
					listModel.add(0,listEntry.get(i));
					System.out.println("added list");
				}
			}
		}
		public void reloadTimeLine(){
			try{
				ResponseList<Status> statusList = twitter.getUserListStatuses(this.listOwnerName, this.listId,new Paging(1, 100));
				for(int i = 0; i < statusList.size(); i++){
					UserData tmp;
					String tmpName = statusList.get(i).getUser().getName();
					if(userList.containsKey(tmpName) == false){
						tmp = new UserData(statusList.get(i), iconList.getImageIcon(statusList.get(i).getUser()));
						userList.put(tmpName, tmp);
					}else{
						tmp = userList.get(tmpName);
					}
					long tmpId = statusList.get(i).getId();
					tmp.addTweet(tmpId, statusList.get(i).getText());
					listModel.addElement(new ListEntry(tmp, tmpId));
				}
			}
			catch (Exception ex){
				ex.printStackTrace();
			}
		}
		private void deleteThisColumn(){
			deleteColumn(this);
		}
	}

	/*
	 * セル描画の設定
	 * 名前とツイートで色変えたい
	 * JTextArea1つだけで色を変えるのは無理くさいからパネル１こ作ってLabelを追加する？
	 * */
	class TimeLineCellRenderer implements ListCellRenderer{

		JTextArea tmp;
		IconList iconList;
		Color evenColor = new Color(230,255,230);

		public TimeLineCellRenderer(IconList iconList){
			tmp = null;
			this.iconList = iconList;
		}

		public Component getListCellRendererComponent(
				JList list,
				Object value,
				int index,
				boolean isSelected,
				boolean cellHasFocus){
			/*
			JPanel tmpPanel = new JPanel();
			tmpPanel.setPreferredSize(new Dimension(290,50));
			ListEntry tmpEntry = (ListEntry)value;
			JLabel tmpIcon = new JLabel();
			UserData tmpUserData = tmpEntry.getUserData();
			tmpIcon.setIcon(tmpUserData.getIcon());
			
//			tmpIcon.setMaximumSize(new Dimension(50,50));
//			tmpIcon.setMinimumSize(new Dimension(50,50));
//			tmpIcon.setSize(new Dimension(50,50));
			
			tmp = new JTextArea(tmpUserData.getUserName() + " >\n" + tmpUserData.getTweetText(tmpEntry.getTweetId()));
//			tmp.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
			tmp.setLineWrap(true);
//			tmp.setPreferredSize(new Dimension(200,50));
			tmp.setRows(3);
			*/
//			System.out.print("Rendering List \"" + list.hashCode() + "\"  ");
			ListEntry tmpEntry = (ListEntry)value;
			JPanel tmp = tmpEntry.getUserData().getTweetPanel(tmpEntry.getTweetId());
			
			if(isSelected){
				tmp.getComponent(0).setBackground(list.getSelectionBackground());

			}else{
				tmp.getComponent(0).setBackground(index%2==0 ? evenColor : list.getBackground());
				tmp.getComponent(0).setForeground(list.getForeground());
			}
			/*
			tmpPanel.setLayout(new FlowLayout());
			tmp.setPreferredSize(new Dimension(220,40));
			tmpPanel.add(tmp);
			tmpPanel.add(tmpIcon);
			*/
			/*
			SpringLayout layout = new SpringLayout();
			tmpPanel.setLayout(layout);
			layout.putConstraint(SpringLayout.NORTH, tmpIcon, 5, SpringLayout.NORTH, tmpPanel);
			layout.putConstraint(SpringLayout.WEST, tmpIcon, 5, SpringLayout.WEST, tmpPanel);
			layout.putConstraint(SpringLayout.EAST, tmpIcon, -5, SpringLayout.EAST, tmpPanel);
			layout.putConstraint(SpringLayout.SOUTH, tmpIcon,-5, SpringLayout.SOUTH, tmpPanel);
			
			layout.putConstraint(SpringLayout.NORTH, tmpIcon, 5, SpringLayout.NORTH, tmpPanel);
			layout.putConstraint(SpringLayout.WEST, tmpIcon, 5, SpringLayout.WEST, tmpPanel);
			layout.putConstraint(SpringLayout.EAST, tmpIcon, -5, SpringLayout.WEST, tmp);
			layout.putConstraint(SpringLayout.SOUTH, tmpIcon,-5, SpringLayout.SOUTH, tmpPanel);
			tmpPanel.add(tmp);
			tmpPanel.add(tmpIcon);
			*/
			return tmp;
		}

	}

	
	JScrollPane timeLineScrollPane;
	ArrayList<TimeLineColumn> columnList;
	JPanel timeLinePanel;//TL表示用
	//JPanel iconPanel;//アイコン表示用
	Twitter twitter;
	TweetForm tweetForm;
	TimeLineColumn allFriends;
	TweetDetail tweetDetail;
	ArrayList<ListColumn> listList;
	IconList iconList;
	FlowLayout layout;
	HashMap<String, UserData> userList;

	/*
	 * コンストラクタ
	 * */
	public StatusTimeLine(Twitter twitter, 
			TweetForm tweetForm,
			TweetDetail tweetDetail,
			IconList iconList,
			HashMap<String, UserData> userList){
		this.tweetForm = tweetForm;
		this.tweetDetail = tweetDetail;
		this.iconList = iconList;
		this.userList = userList;
		timeLinePanel = new JPanel();
		columnList = new ArrayList<TimeLineColumn>();
		this.twitter = twitter;
		allFriends = new TimeLineColumn("All Friends");
		allFriends.startUpdate();
		columnList.add(allFriends);
		listList = new ArrayList<ListColumn>();
		
		for(int i = 0; i < listList.size(); i++){
			columnList.add(listList.get(i));
		}
		
		BoxLayout timeLineLayout = new BoxLayout(timeLinePanel, BoxLayout.X_AXIS);
		timeLinePanel.setLayout(timeLineLayout);
		
		/*
		layout = new FlowLayout();
		timeLinePanel.setLayout(layout);
		*/
		Iterator<TimeLineColumn> ite = columnList.iterator();
		while(ite.hasNext()){
			TimeLineColumn tmp = ite.next();
			timeLinePanel.add(tmp.getColumnPanel());
		}
		timeLineScrollPane = new JScrollPane(timeLinePanel,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

	}
	public void updateAllFriends(){
		allFriends.updateTimeLine();
	}
	/*
	 * タイムライン全体用のパネルを返す
	 * */
	public JScrollPane getTimeLinePanel(){ //parentPanelを返す
		return timeLineScrollPane;
	}
	private void deleteColumn(ListColumn target){
		//Listからのremoveも忘れずに
		System.out.println("before size : " + listList.size());
		listList.remove(target);
		System.out.println("after size : " + listList.size());
		target.getColumnPanel().removeAll();
		timeLinePanel.remove(target.getColumnPanel());
		timeLinePanel.revalidate();
	}
	public void AddList(){
		ArrayList<UserList> list = new ArrayList<UserList>();
		PagableResponseList<UserList> tmp;
		long cursor = -1;
		do{
			try{
				tmp = twitter.getUserLists(twitter.getScreenName(), cursor);
			}catch(Exception ex){
				JOptionPane.showMessageDialog(null, "getUserList failed");
				break;
			}
			list.addAll(tmp);
			cursor = tmp.getNextCursor();
		}while(cursor != 0);
		System.out.println("list.size : " + list.size());
		Object[] arry = list.toArray();
		System.out.println("arry.length : " + arry.length);
		String[] name = new String[arry.length];
		for(int i = 0; i < arry.length; i++){
			name[i] = new String(((UserList)arry[i]).getFullName());
		}
		for(int i = 0; i < arry.length; i++){
			System.out.println(name[i]);
		}
		Object selectedItem = JOptionPane.showInputDialog(
				allFriends.getColumnPanel() , "追加するリストを選択してください" , "adding ListColumn..." ,
				JOptionPane.INFORMATION_MESSAGE , null , name , name[0]);
		for(int i = 0; i < name.length; i++){
			ListColumn tmpColumn = null;
			if(name[i].equals(selectedItem)){
				try{
					tmpColumn = new ListColumn(twitter.getScreenName(),((UserList)arry[i]).getId(),((UserList)arry[i]).getFullName());
				}catch(Exception ex){
					ex.printStackTrace();
				}
				if(tmpColumn != null){
				timeLinePanel.add(tmpColumn.getColumnPanel());
				listList.add(tmpColumn);
				tmpColumn.getColumnPanel().revalidate();
				}
			}
		}

		File iniFile = new File("./tweendeck.ini");

	}
	
}

