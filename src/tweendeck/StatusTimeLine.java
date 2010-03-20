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
	 * �^�C�����C���J�������Ǘ�����N���X
	 * �R���X�g���N�^���I�[�o�[���[�h���Ď擾����TL��I�ׂ�悤�ɂ��ׂ��iAll Friends�����X�g�Ƃ��j
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
		DefaultListModel listModel;//�J�������̃c�C�[�g���Ǘ�����I�u�W�F�N�g
		/*
		 * �R���X�g���N�^
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
			//�}�E�X���X�i�֓o�^
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
					ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, // �����o�[
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
		 * �����擾�p�X���b�h
		 * ��莞�Ԃ��Ƃ�updateTimeLine���\�b�h���Ăяo��
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
		 * �J�����p�p�l����Ԃ�
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
		 * �^�C�����C���J�����̍ĕ\��
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
		 * �^�C�����C���̍X�V
		 * �����X�V�p�X���b�h�ƊO������̂Q����A�N�Z�X�����\�����A��
		 * */
		/*
		 * API �̎��s�񐔐����ɂ���(�����}�j���A���̃R�s�y
		 * Twitter �� API �́A60���Ԃ�150��܂Ŏ��s�ł���B
		 * ���̎��s�񐔐����𒴂�����Ԃł���Ƀ��N�G�X�g�𑗂����ꍇ�AHTTP�X�e�[�^�X�R�[�h 400 ���Ԃ�B
		 * �F�؂̕K�v�Ȃ��́A�s�v�Ȃ��̗̂��������s�񐔐����̑ΏۂƂȂ�(�ȑO�͎��s�񐔐����̑ΏۊO�ł�����
		 * public_timeline �̎擾���A���݂͑ΏۂƂȂ��Ă���)�B
		 * �F�؂̕K�v�Ȃ��̂̓��[�UID(�A�J�E���g)�P�ʂŁA�F�؂̕s�v�Ȃ��̂�IP�A�h���X�P�ʂŁA���s�񐔂̃J�E���g���s�Ȃ��B
		 * [��҂ɂ�钍�L]
		 * Twitter �̉^�p�󋵂ɂ���Ă� API ��������茵�����ݒ肳��邱�Ƃ�����(60���Ԃ�20��܂ŁA�Ȃ�)�B
		 * �Ȃ��APOST�nAPI(�����̓��e�A�_�C���N�g���b�Z�[�W�̑��M�A�w�胆�[�U�̃t�H���[�A���C�ɓ���̓o�^�A�Ȃ�)�́A
		 * ���̎��s�񐔐����ɂ͊֌W�Ȃ��A����ł����s�ł���B
		 * �������APOST�nAPI�ł����Ă��A��莞�ԓ�����̎g�p�񐔂����܂�ɂ������ꍇ�́A�g�p���������邱�Ƃ�����B
		 * ���̎��s�񐔐�����K�p�����Ɠs���������ꍇ�́A���R�𖾎��̏�ATwitter �J���҂ɃR���^�N�g����邱�ƁB
		 * �[���ł��闝�R���������΁A���Y���[�U���A���s�񐔐����K�p�O�̃X�N���[�����̃��X�g�ɓ����B
		 * (�������A���̎��s�񐔐����K�p�O�̃��X�g�ɓo�^����Ă��A1���Ԃɍő�2����̃��N�G�X�g�����󂯕t���Ȃ�)
		 * rate_limit_status �Ƃ����u�A�J�E���g�֘A��API�v���g�����ƂŁA���ۂ� API �����̎��{�󋵂𒲂ׂ邱�Ƃ��ł���B
		 *      *�u�����̓��e(statuses/update)�v�A�ufollowing(friendships/create)�v���A1���Ɏ��s�\�ȏ���񐔂��ʓr���߂��Ă�����̂�����
		 *             (�ڍׂ� http://help.twitter.com/forums/10711/entries/15364 ���Q��)
		 *                       - �����̓��e: 1���ő�1000���܂�
		 *                       - �_�C���N�g���b�Z�[�W�̑��M: 1���ő�1000���܂�
		 *                       - following: ��̓I�Ȑ����͖�������Ă��Ȃ�(1���ő�2000���܂ŁA�炵��)
		 * API ���s�񐔐����̊ɘa
		 * (2010�N1������) OAuth �o�R�ŐV���� API �G���h�|�C���g(http://api.twitter.com/ �Ŏn�܂� URL) �ɃA�N�Z�X����ꍇ�Ɍ����āA
		 * API ��60���Ԃ�350��܂Ŏ��s���邱�Ƃ��ł���
		 *	 (��Ғ�: �ɘa�[�u���������60���Ԃ�450��܂Ŏ��s�\���������A2010�N1��12���̃n�C�`�n�k�������̃T�[�o�[���׏󋵂��l���������ʁA
		 *				2010�N1��24���ȍ~�A60���Ԃ�350��܂Ŏ��s�\�Ƃ��邱�Ƃɂ����͗l�B�����I�ɂ́A60���Ԃ�1500��܂Ŏ��s�\�ɂ��邱�Ƃ��\������Ă���)�B
		 * �Ȃ��A���́u�ɘa�[�u�v���L���ȏ�Ԃł� rate_limit_status �́A�ˑR�Ƃ��� hourly-limit �̒l�Ƃ��� 150 ��Ԃ��B
		 * �u�ɘa�[�u�v���L���ɂȂ��Ă��邱�Ƃ��m�F����ɂ́AAPI ���s�v��(http���N�G�X�g)�ɑ΂��鉞������http���X�|���X�w�b�_������΂悢�B
		 * http���X�|���X�w�b�_����
		 *         X-RateLimit-Limit:
		 * ���A60����(1����)�Ɏ��s�\�ȉ񐔂������A
		 *         X-RateLimit-Remaining:
		 * ���A���Ɖ�����s�\�Ȃ̂��������Ă���
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
	 * ���X�g�p�J����
	 * ���X�g�Ǘ��҂�ScreenName��listID��n���Ă���č쐬
	 * AllFriends�J��������ŐVTL���󂯎���ĊǗ����郊�X�g�ɏ������Ă郁���o�[�̃c�C�[�g�݂̂��J�����֒ǉ����Ă���
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
				//�}�E�X���X�i�֓o�^
				MouseListener mouseListener = new MouseAdapter() {
					public void mouseClicked(MouseEvent me) {
						Status selectedItem = (Status)listModel.get(statusJList.getSelectedIndex());
						if(SwingUtilities.isRightMouseButton(me)){
							//�I�����Ă郊�X�g�̃I�u�W�F�N�g�Ȃ���擾����
							//�e�L�X�g���e���擾�ł��邩�e�X�g
							JOptionPane.showMessageDialog(null, "right clicked");
							tweetForm.addString("@" + selectedItem.getUser().getScreenName());
						}else if(SwingUtilities.isLeftMouseButton(me)){
							tweetDetail.setText(selectedItem.getText());
						}
					}
				};
				statusJList.addMouseListener(mouseListener);
				JScrollPane scrollPane = new JScrollPane(statusJList,
						JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, // �����o�[
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
	 * �Z���`��̐ݒ�
	 * ���O�ƃc�C�[�g�ŐF�ς�����
	 * JTextArea1�����ŐF��ς���͖̂�������������p�l���P�������Label��ǉ�����H
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
	JPanel timeLinePanel;//TL�\���p
	//JPanel iconPanel;//�A�C�R���\���p
	Twitter twitter;
	TweetForm tweetForm;
	TimeLineColumn allFriends;
	TweetDetail tweetDetail;
	ArrayList<ListColumn> listList;
	IconList iconList;
	FlowLayout layout;
	HashMap<String, UserData> userList;

	/*
	 * �R���X�g���N�^
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
	 * �^�C�����C���S�̗p�̃p�l����Ԃ�
	 * */
	public JScrollPane getTimeLinePanel(){ //parentPanel��Ԃ�
		return timeLineScrollPane;
	}
	private void deleteColumn(ListColumn target){
		//List�����remove���Y�ꂸ��
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
				allFriends.getColumnPanel() , "�ǉ����郊�X�g��I�����Ă�������" , "adding ListColumn..." ,
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

