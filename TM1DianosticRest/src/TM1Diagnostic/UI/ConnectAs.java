package TM1Diagnostic.UI;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

//import org.apache.commons.codec.binary.Base64;


import TM1Diagnostic.Credential;
import TM1Diagnostic.REST.TM1Server;

import org.eclipse.swt.widgets.Text;
import org.jasypt.util.password.BasicPasswordEncryptor;
import org.jasypt.util.text.StrongTextEncryptor;

public class ConnectAs extends Dialog {

	static String encryptionPassword = "123456";

	protected boolean result;
	protected Shell shell;
	private TM1Server tm1server;
	
	private Table credentialsTable;
	private TableColumn user_column;
	private TableColumn namespace_column;
	private TableColumn connectColumn;
	
	private String path = ".//connections//";
	private boolean isCam;

	private List<Credential> credentials;
	private Credential connectionCredential;

	/**
	 * @wbp.parser.constructor
	 */
	public ConnectAs(Shell parent, TM1Server tm1server) {
		super(parent, SWT.DIALOG_TRIM);
		setText("SWT Dialog");
		this.tm1server = tm1server;
		if (tm1server.getsecuritymode().equals("CAM")){
			this.isCam = true; 
		} else {
			this.isCam = false; 
		}
		credentials = new ArrayList<Credential>();
	}

	public Credential getcredential() {
		return connectionCredential;
	}

	public boolean open() {
		createContents();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	private void createContents() {
		shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.RESIZE);
		shell.setSize(546, 439);
		shell.setText("Connect " + tm1server.getAdminHostName() + ":" + tm1server.getName());
		shell.setLayout(new GridLayout(1, false));

		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new FillLayout(SWT.HORIZONTAL));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		credentialsTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		credentialsTable.setHeaderVisible(true);

		credentialsTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (credentialsTable.getSelectionCount() > 0) {
				}
			}
		});
		credentialsTable.setLinesVisible(true);
		

		user_column = new TableColumn(credentialsTable, SWT.NONE);
		user_column.setWidth(159);
		user_column.setText("User");
		Menu menu = new Menu(credentialsTable);
		credentialsTable.setMenu(menu);

		credentialsTable.addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(Event event) {
				columnResize();
			}
		});

		MenuItem removecredentials_menuitem = new MenuItem(menu, SWT.NONE);
		removecredentials_menuitem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (credentialsTable.getSelectionCount() > 0) {
					credentialsTable.remove(credentialsTable.getSelectionIndex());
					writeCredentialsToFile();
				}
			}
		});
		removecredentials_menuitem.setText("Remove");

		namespace_column = new TableColumn(credentialsTable, SWT.NONE);
		namespace_column.setWidth(151);
		namespace_column.setText("Namespace");

		connectColumn = new TableColumn(credentialsTable, SWT.NONE);
		connectColumn.setWidth(197);
		


		Composite composite_1 = new Composite(shell, SWT.NONE);
		composite_1.setLayout(new FillLayout(SWT.HORIZONTAL));
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1));

		Button add_button = new Button(composite_1, SWT.NONE);
		add_button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				AddUser addUser = new AddUser(shell, credentials, isCam);
				if (addUser.open()){
					writeCredentialsToFile();
					updateCredentialsTable();
				}
			}
		});
		add_button.setText("Add");

		Button removeButton = new Button(composite_1, SWT.NONE);
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				for (int i=0; i<credentialsTable.getSelectionCount(); i++){
					credentials.remove((Credential)credentialsTable.getSelection()[i].getData());
				}
				writeCredentialsToFile();
				updateCredentialsTable();
			}
		});
		removeButton.setText("Remove");

		Button closeButton = new Button(composite_1, SWT.NONE);
		closeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}
		});
		closeButton.setText("Close");

		readCredentialsFromFile();
		updateCredentialsTable();
	}

	private boolean readCredentialsFromFile() {
		credentials.clear();
		//credentialsTable.clearAll();
		String adminserverdirectoryname = path + tm1server.getAdminHostName();
		String serverfilename = adminserverdirectoryname + "//" + tm1server.getName();
		try {
			File file = new File(serverfilename);
			if (file.exists()) {
				FileReader fr = new FileReader(serverfilename);
				BufferedReader br = new BufferedReader(fr);
				String line = br.readLine();
				while (line != null) {
					String[] tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
					StrongTextEncryptor textEncryptor = new StrongTextEncryptor();
					textEncryptor.setPassword(encryptionPassword);
					if (tokens.length == 3) {
						String password = textEncryptor.decrypt(tokens[1].replace("\"", ""));
						Credential credential = new Credential(tokens[0].replace("\"", ""), password, tokens[2].replace("\"", ""));
						credentials.add(credential);
						//TableItem t = new TableItem(credentialsTable, SWT.NONE);
						//t.setText(0, tokens[0].replace("\"", ""));
						//t.setText(1, tokens[2].replace("\"", ""));
						//t.setData(credential);
					} else if (tokens.length == 2) {
						String password = textEncryptor.decrypt(tokens[1].replace("\"", ""));
						Credential credential = new Credential(tokens[0].replace("\"", ""), password);
						credentials.add(credential);
						//TableItem t = new TableItem(credentialsTable, SWT.NONE);
						//t.setText(0, tokens[0].replace("\"", ""));
						//t.setData(credential);
					} else {
						// WTF
					}
					line = br.readLine();
				}
				br.close();
				fr.close();
			}

		} catch (Exception e) {
			System.out.println("File access exception: " + serverfilename);
			return false;
		}
		return true;
	}

	private boolean writeCredentialsToFile() {
		String adminserverdirectoryname = path + tm1server.getAdminHostName();
		File adminserverdirrectory = new File(adminserverdirectoryname);
		if (!adminserverdirrectory.exists()) {
			try {
				adminserverdirrectory.mkdir();
			} catch (Exception ex) {
				System.out.println("Error: " + ex.getMessage());
				return false;
			}
		}

		String serverfilename = adminserverdirectoryname + "//" + tm1server.getName();
		try {
			FileWriter fw = new FileWriter(serverfilename);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter pw = new PrintWriter(bw);

			for (int i = 0; i < credentials.size(); i++) {
				Credential c = credentials.get(i);
				StrongTextEncryptor textEncryptor = new StrongTextEncryptor();
				textEncryptor.setPassword(encryptionPassword);
				String myEncryptedText = textEncryptor.encrypt(c.getpassword());
				if (c.getsecuritymode().equals("CAM")) {
					pw.println("\"" + c.getusername() + "\",\"" + myEncryptedText + "\",\"" + c.getnamespace() + "\"");
				} else if (c.getsecuritymode().equals("BASIC")) {
					pw.println("\"" + c.getusername() + "\",\"" + myEncryptedText + "\"");
				} 
			}
			pw.close();
			bw.close();
			fw.close();
		} catch (Exception ex) {
			System.out.println("Error: " + ex.getMessage());
			return false;
		}
		return true;
	}
	
	private void updateCredentialsTable(){
		//System.out.println("updateCredentialsTable");
		credentialsTable.removeAll();
		for (int i=0; i<credentials.size(); i++){
			TableItem t = new TableItem(credentialsTable, SWT.NONE);
			t.setText(0, credentials.get(i).getusername());
			t.setText(1, credentials.get(i).getnamespace());
			
			TableEditor editor = new TableEditor(credentialsTable);
			Button button = new Button(credentialsTable, SWT.NONE);
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					Button b = (Button)e.getSource();
					connectionCredential = (Credential)b.getData();
					result = true;
					shell.close();
					//System.out.println("Connected - " + b.getText());
				}
			});
			button.pack();
			button.setText("Connect");
			button.setData(credentials.get(i));
			editor.minimumWidth = connectColumn.getWidth();
		    editor.horizontalAlignment = SWT.FILL;
			editor.setEditor(button, t, 2);

			t.setData(credentials.get(i));
		}
		columnResize();
	}
	
	private void columnResize(){
		int columnCount = credentialsTable.getColumnCount();
		if (columnCount == 0) return;
		Rectangle area = credentialsTable.getClientArea();
		int totalAreaWdith = area.width;
		int lineWidth = credentialsTable.getGridLineWidth();
		int totalGridLineWidth = (columnCount - 1) * lineWidth;
		int totalColumnWidth = 0;
		for (TableColumn column : credentialsTable.getColumns()) {
			totalColumnWidth = totalColumnWidth + column.getWidth();
		}
		int diff = totalAreaWdith - (totalColumnWidth + totalGridLineWidth);
		TableColumn lastCol = credentialsTable.getColumns()[columnCount - 1];
		lastCol.setWidth(diff + lastCol.getWidth());
	}

}
