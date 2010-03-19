package tweendeck;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class LoginForm extends Thread implements ActionListener{
	  JLabel labelName;
	  JLabel labelPass;
	  JTextField textName;
	  JPasswordField passField;
	  JButton okButton;
	  JButton cancelButton;
	  JCheckBox saveOrNot;
	  
	  JDialog dialog;

	  public LoginForm(){
		  JPanel panelOne = new JPanel();
		  labelName = new JLabel("Name");
		  textName = new JTextField(15);
		  panelOne.add(labelName);
		  panelOne.add(textName);

		  JPanel panelTwo = new JPanel();
		  labelPass = new JLabel("Password");
		  passField = new JPasswordField(15);
		  panelTwo.add(labelPass);
		  panelTwo.add(passField);

		  JPanel panelThree = new JPanel();
		  okButton = new JButton("OK");
		  cancelButton = new JButton("Cancel");
		  okButton.addActionListener(this);
		  cancelButton.addActionListener(this);
		  saveOrNot = new JCheckBox("éüâÒà»ç~ì¸óÕÇè»ó™Ç∑ÇÈ");
		  panelThree.add(saveOrNot);
		  panelThree.add(okButton);
		  panelThree.add(cancelButton);

		  dialog = new JDialog();
		  dialog.setResizable(false);
		  dialog.getContentPane().add(panelOne);
		  dialog.getContentPane().add(panelTwo);
		  dialog.getContentPane().add(panelThree);
		  JTextArea statusText = new JTextArea("progress...");
		  dialog.getContentPane().add(statusText);
		  dialog.setTitle("Login to Twitter");
		  dialog.getContentPane().setLayout(new FlowLayout(FlowLayout.RIGHT));
		  dialog.setSize(350, 180);
		  dialog.setLocationRelativeTo(null); // âÊñ ÇÃíÜâõÇ…îzíu
		  dialog.setModal(true);
		  dialog.setVisible(true);
	      
	}
	  public void actionPerformed(ActionEvent e) {
		  if (e.getSource() == okButton) {
			  dialog.dispose();
		  } else if (e.getSource() == cancelButton) {
			  System.exit(0);
		  }
	  }

	  public String getUserName() {
		  return textName.getText();
	  }

	  public String getPassword() {
		  return String.valueOf(passField.getPassword());
	  }
	  public boolean getCheckState(){
		  return saveOrNot.isSelected();
	  }
	  public void terminateDialog(){
		  dialog.dispose();
	  }
}
