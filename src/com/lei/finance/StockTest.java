package com.lei.finance;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.safs.IndependantLog;
import org.safs.SAFSException;
import org.safs.sockets.DebugListener;
import org.safs.tools.mail.Mailer;
import org.safs.tools.mail.Mailer.Protocol;

import com.lei.GetText;
import com.lei.natives.WebProxyDetector;
import com.lei.util.StringUtil;

public class StockTest extends JFrame implements ActionListener, ItemListener{

	private static final long serialVersionUID = 1L;
	//workers will contain a pair(stockId,aRetrieveWorker)
	private HashMap<String,RetrieveWorker> workers;
	public String resourceBundleName = ""+GetText.DEFAULT_BUNDLE_NAME;

	public static final int	APPLICATION_PANEL_WIDTH 				= 900;
	public static final int	APPLICATION_PANEL_HEIGHT 				= 400;

	/**
	 * This url query will return a string info containing stock's "last price", "change", "quantity" etc.
	 * Such as: // [ { "id": "689364" ,"t" : "600108" ,"e" : "SHA" ,"l" : "6.51" ,"l_fix" : "6.51" ,"l_cur" : "CN¥6.51" ,"s": "0" ,"ltt":"3:00PM GMT+8" ,"lt" : "Sep 18, 3:00PM GMT+8" ,"lt_dts" : "2015-09-18T15:00:04Z" ,"c" : "+0.07" ,"c_fix" : "0.07" ,"cp" : "1.09" ,"cp_fix" : "1.09" ,"ccol" : "chg" ,"pcls_fix" : "6.44" } ]
	 */
	public static final String KEY_STOCK_QUERY_URL_PREFIX				= "http://www.google.com/finance/info?q=";
	/**
	 * This url query will return a whole html document containing the stock's detail information.
	 */
	public static final String KEY_STOCK_INFO_URL_PREFIX				= "http://www.google.com/finance?q=";

	public static final String KEY_APPLICATION_TITLE		= "key.application.title";
	public static final String KEY_LABEL_INTERVAL_TIME		= "key.label.interval";
	public static final String KEY_LABEL_ALERT_PERCENT		= "key.label.alert.percent";
	public static final String KEY_LABEL_STOCK_NUMBER		= "key.label.stock.number";
	public static final String KEY_LABEL_ADDED_STOCK_NUMBER	= "key.label.added.stock.number";

	public static final String KEY_BUTTON_ADD				= "key.button.add";
	public static final String KEY_BUTTON_REMOVE			= "key.button.remove";
	public static final String KEY_BUTTON_PAUSE				= "key.button.pause";
	public static final String KEY_BUTTON_RESUME			= "key.button.resume";
	public static final String KEY_BUTTON_RESET				= "key.button.reset";
	public static final String KEY_BUTTON_CLEAR				= "key.button.clear";
	public static final String KEY_BUTTON_SEND_EMAIL		= "key.button.send.email";

	private GetText text;

	private JPanel panelInput;
	private JPanel panelButton;
	private JScrollPane panelInformation;

	private JLabel labelIntervalTime;
	private JLabel labelAlertPercent;
	private JLabel labelStockNumber;
	private JLabel labelAddedStockNumber;

	//
	private JTextField textfieldIntervalTime;

	private JTextField textfieldAlertPercent;

	private JTextField textfieldStockNumber;

	private JComboBox  comboboxAddedStockNumber;

	//buttonAdd is used to add a new stock to our hashmap workers
	private JButton buttonAdd;
	//buttonRemove is used to remove a added stock from our hashmap workers
	private JButton buttonRemove;
	//buttonPause is used to pause a stock from being queried, RetrieveWorker will wait
	//after this button is clicked, the resume button should be enabled, this button and remove and reset button should be disabled.
	private JButton buttonPause;
	//buttonResume is used to resume a stock to query, RetrieveWorker will be notified and wake up from wait status
	//after this button is clicked, the pause, remove and reset button should be enabled, this button should be disabled.
	private JButton buttonResume;
	//buttonReset will update the query parameters of a stock, for example: intervalTime, alertPercent
	private JButton buttonReset;
	//buttonClear will clear the stock information on text area textAreaStockInfo
	private JButton buttonClear;
	//buttonSendEmail will enable worker to send a email if the stock price is beyond the alert price.
	private JButton buttonSendEmail;

	private JTextArea textAreaStockInfo;

	public void clearTextAreaStockInfo() {
		if(textAreaStockInfo!=null){
			textAreaStockInfo.setText("");
		}
	}
	public void appendTextAreaStockInfo(String info) {
		if(textAreaStockInfo!=null){
			textAreaStockInfo.append(info+"\n");
		}
	}

	public String getSelectedStockNumber() {
		String selectedStockNumber = null;

		if(comboboxAddedStockNumber!=null){
			selectedStockNumber = String.valueOf(comboboxAddedStockNumber.getSelectedItem());
		}
		return selectedStockNumber;
	}

	public StockTest(){
		workers = new HashMap<String,RetrieveWorker>();
		this.initialResourceReader();
		this.initialComponents();
		this.initialMisc();
		this.setPreferredSize(new Dimension(APPLICATION_PANEL_WIDTH,APPLICATION_PANEL_HEIGHT));
		this.pack();
		this.setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	private void initialMisc(){
		try {
			String mailServer = System.getProperty(PROPERTY_MAILSERVERHOST);
			String mailServerPort = System.getProperty(PROPERTY_MAILSERVERPORT);
			String mailServerPotocol = System.getProperty(PROPERTY_MAILSERVERPOTOCOL);
			String user = System.getProperty(PROPERTY_MAILAUTHUSER);
			String pass = System.getProperty(PROPERTY_MAILAUTHPASS);

			if(mailServer==null){
				mailer = new Mailer();
			}else{
				int port = -1;
				Protocol protocol = null;

				if(mailServerPort==null){
					port = Mailer.DEFAULT_PORT;
				}else{
					port = Integer.parseInt(mailServerPort);
				}
				if(mailServerPotocol==null){
					protocol = Mailer.DEFAULT_PROTOCOL;
				}else{
					protocol = Protocol.get(mailServerPotocol);
				}

				if(user!=null && pass!=null){
					mailer = new Mailer(mailServer, port, protocol, user, pass);
				}else{
					mailer = new Mailer(mailServer, port, protocol);
				}
			}

			//mailer.setSender("yourmail@company.com");
		} catch (Exception e) {
			IndependantLog.error("fail to initialize the Mailer, due to "+StringUtil.debugmsg(e));
		}
	}

	private void initialResourceReader(){
//		text = new GetText(resourceBundleName,Locale.getDefault());
		text = new GetText(resourceBundleName,Locale.ENGLISH);
	}

	private void initialComponents(){
		this.setTitle(text.text(StockTest.KEY_APPLICATION_TITLE));
		GridLayout layoutPanel = new GridLayout(3,1);
		this.setLayout(layoutPanel);
//        GridBagLayout gridbagLayout = new GridBagLayout();
//        this.setLayout(gridbagLayout);
//        GridBagConstraints constraints = new GridBagConstraints();


//		int inputPanelWidth=400, inputPanelHeight=30;
		panelInput = new JPanel();
		panelInput.setPreferredSize(new Dimension(APPLICATION_PANEL_WIDTH,APPLICATION_PANEL_HEIGHT*4/20));
//		constraints.gridheight=4;
//		constraints.gridwidth=GridBagConstraints.REMAINDER;
//		constraints.fill = GridBagConstraints.BOTH;
//		gridbagLayout.setConstraints(panelInput, constraints);
		GridLayout layout = new GridLayout(4,2);
		panelInput.setLayout(layout);
//		panelInput.setSize(new Dimension(inputPanelWidth,inputPanelHeight));
		labelIntervalTime = new JLabel(text.text(StockTest.KEY_LABEL_INTERVAL_TIME));
		labelAlertPercent = new JLabel(text.text(StockTest.KEY_LABEL_ALERT_PERCENT));
		labelStockNumber = new JLabel(text.text(StockTest.KEY_LABEL_STOCK_NUMBER));
		labelAddedStockNumber = new JLabel(text.text(StockTest.KEY_LABEL_ADDED_STOCK_NUMBER));
		textfieldIntervalTime = new JTextField(String.valueOf(RetrieveWorker.DEFAULT_INTERVAL_SECOND));
		textfieldAlertPercent = new JTextField(String.valueOf(RetrieveWorker.DEFAULT_ALERT_PERCENT));
		textfieldStockNumber = new JTextField();
		comboboxAddedStockNumber = new JComboBox();
		comboboxAddedStockNumber.addItemListener(this);
		panelInput.add(labelStockNumber);
		panelInput.add(textfieldStockNumber);
		panelInput.add(labelIntervalTime);
		panelInput.add(textfieldIntervalTime);
		panelInput.add(labelAlertPercent);
		panelInput.add(textfieldAlertPercent);
		panelInput.add(labelAddedStockNumber);
		panelInput.add(comboboxAddedStockNumber);
		this.add(panelInput);

		panelButton = new JPanel();
		panelButton.setPreferredSize(new Dimension(APPLICATION_PANEL_WIDTH,APPLICATION_PANEL_HEIGHT/20));
//		constraints.gridheight=1;
//		constraints.gridwidth=GridBagConstraints.REMAINDER;
//		gridbagLayout.setConstraints(panelButton, constraints);
		buttonAdd = new JButton(text.text(StockTest.KEY_BUTTON_ADD));
		buttonRemove = new JButton(text.text(StockTest.KEY_BUTTON_REMOVE));
		buttonPause = new JButton(text.text(StockTest.KEY_BUTTON_PAUSE));
		buttonResume = new JButton(text.text(StockTest.KEY_BUTTON_RESUME));
		buttonReset = new JButton(text.text(StockTest.KEY_BUTTON_RESET));
		buttonClear = new JButton(text.text(StockTest.KEY_BUTTON_CLEAR));
		buttonSendEmail = new JButton(text.text(StockTest.KEY_BUTTON_SEND_EMAIL));
		buttonAdd.addActionListener(this);
		buttonRemove.addActionListener(this);
		buttonPause.addActionListener(this);
		buttonResume.addActionListener(this);
		buttonReset.addActionListener(this);
		buttonClear.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				textAreaStockInfo.setText("");
			}
		});
		buttonSendEmail.addActionListener(this);
		buttonRemove.setEnabled(false);
		buttonPause.setEnabled(false);
		buttonResume.setEnabled(false);
		buttonReset.setEnabled(false);
		panelButton.add(buttonAdd);
		panelButton.add(buttonRemove);
		panelButton.add(buttonPause);
		panelButton.add(buttonResume);
		panelButton.add(buttonReset);
		panelButton.add(buttonClear);
		panelButton.add(buttonSendEmail);
		this.add(panelButton);

		panelInformation = new JScrollPane();
		panelInformation.setPreferredSize(new Dimension(APPLICATION_PANEL_WIDTH,APPLICATION_PANEL_HEIGHT*15/20));
//		constraints.gridheight=GridBagConstraints.REMAINDER;
//		constraints.gridwidth=GridBagConstraints.REMAINDER;
//		gridbagLayout.setConstraints(panelInformation, constraints);
		textAreaStockInfo = new JTextArea();
		textAreaStockInfo.setEditable(false);
		panelInformation.setViewportView(textAreaStockInfo);
		this.add(panelInformation);
	}

	public void actionPerformed(ActionEvent e) {
		String debugmsg = this.getClass().getName()+".actionPerformed(): ";
		Object source = e.getSource();
		String stockID = null;
		Integer intervalTime = StringUtil.convertToInteger(this.textfieldIntervalTime.getText().trim());
		Double alertPercent = StringUtil.convertToDouble(this.textfieldAlertPercent.getText().trim());

		if(source.equals(buttonAdd)){
			//If Add a stock, then we read the stock ID from textfieldStockNumber
			stockID = this.textfieldStockNumber.getText().trim();
		}else{
			//Otherwise we read the stock ID from comboboxAddedStockNumber
			Object selectedItem = this.comboboxAddedStockNumber.getSelectedItem();
			if(selectedItem!=null)
				stockID = selectedItem.toString().trim();
		}

//		System.out.println("The selected number is "+stockID);
		if(stockID==null || stockID.equals("") || stockID.length()!=6){
			System.err.println(debugmsg+"The stockID is wrong, please check.");
			return;
		}

		if(source.equals(buttonAdd) || source.equals(buttonReset)){
			if(stockID==null || stockID.equals("") || stockID.length()!=6){
				System.err.println(debugmsg+"The stockID is wrong, please check.");
				return;
			}
			String url = StockTest.KEY_STOCK_QUERY_URL_PREFIX+stockID;
			RetrieveWorker worker = new RetrieveWorker(url,intervalTime,alertPercent);
			worker.setStockTest(this);
			this.addOrResetWorker(stockID, worker);
		}else if(source.equals(buttonRemove)){
			this.removeWorker(stockID);
		}else if(source.equals(buttonPause)){
			this.pauseWorker(stockID);
		}else if(source.equals(buttonResume)){
			this.resumeWorker(stockID);
		}else if(source.equals(buttonSendEmail)){
			this.enableWorkerToSendEmail(stockID);
		}
	}

	public void itemStateChanged(ItemEvent e) {
		String debugmsg = this.getClass().getName()+".itemStateChanged(): ";
		Object source = e.getSource();
		Object selectedItem = this.comboboxAddedStockNumber.getSelectedItem();
		if(selectedItem==null){
			System.err.println(debugmsg+"No selected stockID is null, please check.");
			return;
		}
		String stockID = selectedItem.toString().trim();

		if(stockID==null || stockID.equals("") || stockID.length()!=6){
			System.err.println(debugmsg+"The stockID is wrong, please check.");
			return;
		}

		if(source.equals(comboboxAddedStockNumber)){
			//Update the state of buttons.
			updateButtonState(stockID);
			//Update also the query parameters like 'interval time', 'alert percent' etc.
			updateTextFileds(stockID);
		}
	}

	public void addOrResetWorker(String stockID,RetrieveWorker worker){
		RetrieveWorker storedWorker = workers.get(stockID);
		if(storedWorker==null){
			workers.put(stockID, worker);
			worker.start();
			//We need to update the UI of combobox comboboxAddedStockNumber
//			this.comboboxAddedStockNumber.removeAllItems();
//			Iterator<String> iter = workers.keySet().iterator();
//			while(iter.hasNext()){
//				comboboxAddedStockNumber.addItem(iter.next());
//			}
			comboboxAddedStockNumber.addItem(stockID);
			comboboxAddedStockNumber.setSelectedItem(stockID);
			comboboxAddedStockNumber.updateUI();
			//Clear the textfield textfieldStockNumber
			this.textfieldStockNumber.setText("");
		}else{
			//If this stock has been already added, we will try to update it.
			storedWorker.setAlertPercent(worker.getAlertPercent());
			storedWorker.setIntervalTime(worker.getIntervalTime());
		}
	}

	public void pauseWorker(String stockID){
		String debugmsg = this.getClass().getName()+".pauseWorker(): ";
		RetrieveWorker worker = workers.get(stockID);
		if(worker==null){
			System.err.println(debugmsg+"The stock has not been added: "+stockID);
		}else{
			worker.setPleaseWait(true);
			updateButtonState(stockID);
		}
	}

	public void resumeWorker(String stockID){
		String debugmsg = this.getClass().getName()+".resumeWorker(): ";
		RetrieveWorker worker = workers.get(stockID);
		if(worker==null){
			System.err.println(debugmsg+"The stock has not been added: "+stockID);
		}else{
			synchronized(worker){
				worker.setPleaseWait(false);
				worker.notifyAll();
			}
			updateButtonState(stockID);
		}
	}

	public void removeWorker(String stockID){
		String debugmsg = this.getClass().getName()+".removeWorker(): ";
		RetrieveWorker worker = workers.get(stockID);
		if(worker==null){
			System.err.println(debugmsg+"The stock has not been added: "+stockID);
		}else{
			//Stop the thread
			synchronized(worker){
				worker.setPleaseStop(true);
			}
			//Remove it from the cache map workers
			workers.remove(stockID);
			//Remove also this ID from the combo box comboboxAddedStockNumber
			comboboxAddedStockNumber.removeItem(stockID);
			comboboxAddedStockNumber.updateUI();

			//If no worker exists, we should disable all buttons except 'Add' button
			if(workers.size()==0){
				updateButtonState(null);
			}
		}
	}

	public void enableWorkerToSendEmail(String stockID){
		String debugmsg = this.getClass().getName()+".enableWorkerToSendEmail(): ";
		RetrieveWorker worker = workers.get(stockID);
		if(worker==null){
			System.err.println(debugmsg+"The stock has not been added: "+stockID);
		}else{
			//Stop the thread
			synchronized(worker){
				worker.setMailSended(false);
			}
			updateButtonState(stockID);
		}
	}

	public void updateButtonState(String stockID){
		String debugmsg = this.getClass().getName()+".updateButtonState(): ";

		//If the stockID is null, we should disable all buttons except 'Add' button
		if(stockID==null){
			this.buttonResume.setEnabled(false);
			this.buttonPause.setEnabled(false);
			this.buttonRemove.setEnabled(false);
			this.buttonReset.setEnabled(false);
			this.buttonSendEmail.setEnabled(false);
			return;
		}

		RetrieveWorker worker = workers.get(stockID);
		if(worker==null){
			System.err.println(debugmsg+"The stock has not been added: "+stockID);
		}else{
			if(worker.isPleaseWait()){
				this.buttonResume.setEnabled(true);
				this.buttonPause.setEnabled(false);
				this.buttonRemove.setEnabled(false);
				this.buttonReset.setEnabled(false);
			}else{
				this.buttonResume.setEnabled(false);
				this.buttonPause.setEnabled(true);
				this.buttonRemove.setEnabled(true);
				this.buttonReset.setEnabled(true);
			}

			if(worker.isMailSended()){
				this.buttonSendEmail.setEnabled(true);
			}else{
				this.buttonSendEmail.setEnabled(false);
			}
		}
	}

	public void updateTextFileds(String stockID){
		String debugmsg = this.getClass().getName()+".updateTextFileds(): ";

		RetrieveWorker worker = workers.get(stockID);
		if(worker==null){
			System.err.println(debugmsg+"The stock has not been added: "+stockID);
		}else{
			this.textfieldIntervalTime.setText(String.valueOf(worker.getIntervalTime()));
			this.textfieldAlertPercent.setText(String.valueOf(worker.getAlertPercent()));
//			this.textAreaStockInfo.append(worker.getStockInfo()+"\n");
		}
	}

	private static void processArgs(String[] args){
		getSysProperty(PROPERTY_HTTPPROXYHOST,WebProxyDetector.INSTANCE.getProxyServerName());
		getSysProperty(PROPERTY_HTTPPROXYPORT, String.valueOf(WebProxyDetector.INSTANCE.getProxyServerPort()));

		getSysProperty(PROPERTY_MAILSERVERHOST, getConfig(PROPERTY_MAILSERVERHOST));
		getSysProperty(PROPERTY_MAILSERVERPORT, getConfig(PROPERTY_MAILSERVERPORT));
		getSysProperty(PROPERTY_MAILSERVERPOTOCOL, getConfig(PROPERTY_MAILSERVERPOTOCOL));
		getSysProperty(PROPERTY_MAILRECIPIENTS, getConfig(PROPERTY_MAILRECIPIENTS));
		getSysProperty(PROPERTY_MAILAUTHUSER, getConfig(PROPERTY_MAILAUTHUSER));
		getSysProperty(PROPERTY_MAILAUTHPASS, getConfig(PROPERTY_MAILAUTHPASS));
	}

	private static String getConfig(String key){
		try{
			return config.getString(key).trim();
		}catch(Exception e){
			IndependantLog.error("Fail to get config value for key '"+key+"'");
			return null;
		}
	}

	private static String getSysProperty(String name, String defaultValue){
		String value = System.getProperty(name);
		if(value==null && defaultValue!=null){
			value = defaultValue;
			System.setProperty(name, value);
		}
		if(value==null){
			IndependantLog.warn("The value for property '"+name+"' is null!");
		}else{
			IndependantLog.info(name+": "+value);
		}
		return value;
	}

	public static final String PROPERTY_HTTPPROXYHOST 		= "http.proxyHost";
	public static final String PROPERTY_HTTPPROXYPORT 		= "http.proxyPort";
	public static final String PROPERTY_MAILSERVERHOST 		= "key.mail.server.host";
	public static final String PROPERTY_MAILSERVERPORT		= "key.mail.server.port";
	public static final String PROPERTY_MAILSERVERPOTOCOL	= "key.mail.server.protocol";
	public static final String PROPERTY_MAILRECIPIENTS		= "key.mail.recipients";
	public static final String PROPERTY_MAILAUTHUSER		= "key.mail.auth.user";
	public static final String PROPERTY_MAILAUTHPASS		= "key.mail.auth.pass";

	private static final ResourceBundle config = ResourceBundle.getBundle("Config");

	private Mailer mailer = null;

	public Mailer getMailer() {
		return mailer;
	}
	public void setMailer(Mailer mailer) {
		this.mailer = mailer;
	}
	public void sendMail(List<String> recipients, String subject, String body) throws SAFSException{
		mailer.send(recipients, subject, body);
	}
	public void sendMail(String subject, String body){
		try{
			List<String> recipients = new ArrayList<String>();
			String[] receivers = System.getProperty(PROPERTY_MAILRECIPIENTS).split(",");
			for(String receiver: receivers) recipients.add(receiver.trim());
			mailer.send(recipients, subject, body);
		}catch(Exception e){
			IndependantLog.error("Fail to send message, due to "+StringUtil.debugmsg(e));
		}
	}

	/**
	 *
	 * It accept the following system properties:
	 * <ul>
	 * <li>-Dhttp.proxyHost
	 * <li>-Dhttp.proxyPort
	 * <li>-Dkey.mail.server.host
	 * <li>-Dkey.mail.server.port
	 * <li>-Dkey.mail.server.protocol
	 * <li>-Dkey.mail.recipients
	 * <li>-Dkey.mail.auth.user
	 * <li>-Dkey.mail.auth.pass
	 * </ul>
	 *
	 * @param args
	 */
	public static void main(String[] args){
		IndependantLog.setDebugListener(new DebugListener(){
			public String getListenerName() {
				return "StockTest";
			}

			public void onReceiveDebug(String message) {
				System.out.println(message);
			}
		});

		processArgs(args);

		new StockTest();
	}


}
