package TM1Diagnostic.UI;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

import javax.swing.tree.TreeNode;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;

import TM1Diagnostic.Credential;
import TM1Diagnostic.REST.TM1Admin;
import TM1Diagnostic.REST.TM1Application;
import TM1Diagnostic.REST.TM1Blob;
import TM1Diagnostic.REST.TM1Chore;
import TM1Diagnostic.REST.TM1Cube;
import TM1Diagnostic.REST.TM1Dimension;
import TM1Diagnostic.REST.TM1Folder;
import TM1Diagnostic.REST.TM1Hierarchy;
import TM1Diagnostic.REST.TM1Object;
import TM1Diagnostic.REST.TM1ObjectReference;
import TM1Diagnostic.REST.TM1Process;
import TM1Diagnostic.REST.TM1RestException;
import TM1Diagnostic.REST.TM1Server;
import TM1Diagnostic.REST.TM1ServerStub;
import TM1Diagnostic.REST.TM1Subset;
import TM1Diagnostic.REST.TM1View;

import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public class Wrarchitect {

	private static Image ADMINICON;
	private static Image CONNECTEDICON;
	private static Image DISCONNECTEDICON;

	static public int SERVER = 0;
	static public int CUBE = 1;
	static public int DIMENSION = 2;
	static public int PROCESS = 3;
	static public int CHORE = 4;
	static public int VIEW = 5;
	static public int HIERARCHY = 6;
	static public int SUBSET = 7;
	static public int ELEMENT = 8;
	static public int APPLICATION = 9;
	static public int CELL = 10;
	static public int BLOB = 11;
	static public int FOLDER = 12;
	static public int REFERENCE = 13;
	static public int DOCUMENT = 14;

	static public int PUBLIC = 0;
	static public int PRIVATE = 1;

	protected Shell shell;
	private Display display;
	
	private MenuItem connectionMenuItem;
	private Menu connectionsMenu;

	public TabFolder folders;
	
	private List<TM1Admin> adminservers;

	//private TabItem activeTab;
	//private TabItem connectionTab;
	//private ServerConnectorComposite serverConnectorComposite;
	//private ServerExplorerComposite activeServerExplorerComposite;
	//private MenuItem modelConfigPaneMenuItem;
	//private MenuItem showControlObjectsMenuItem;

	public boolean HTTPDEBUG;

	public Wrarchitect() {
		super();
		adminservers = new ArrayList<TM1Admin>();
		ADMINICON = new Image(display, ".//images//icon_admin.gif");
		CONNECTEDICON = new Image(display, ".//images//icon_connected.gif");
		DISCONNECTEDICON = new Image(display, ".//images//icon_disconnected.gif");
	}

	public static void main(String[] args) {
		try {
			Wrarchitect window = new Wrarchitect();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void open() {
		display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	protected void createContents() {
		shell = new Shell();
		shell.setSize(624, 375);
		shell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent arg0) {
				System.exit(0);
			}
		});
		shell.setMaximized(true);
		shell.setText("Wrarchitect");

		GridLayout gl_shlWrachitect = new GridLayout(1, false);
		shell.setLayout(gl_shlWrachitect);

		Menu menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);

		MenuItem fileMenuItem = new MenuItem(menu, SWT.CASCADE);
		fileMenuItem.setText("&File");

		Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
		fileMenuItem.setMenu(fileMenu);

		MenuItem propertiesMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		propertiesMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				ClientProperties clientProperties = new ClientProperties(shell);
				if (clientProperties.open()) {

				}
			}
		});
		propertiesMenuItem.setText("Configuration");

		MenuItem closeMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		closeMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				shell.close();
			}
		});
		closeMenuItem.setText("Close \tF4");
		closeMenuItem.setAccelerator(SWT.F4);

		connectionMenuItem = new MenuItem(menu, SWT.CASCADE);
		connectionMenuItem.setText("Connections");
		
		connectionsMenu = new Menu(connectionMenuItem);
		connectionsMenu.addMenuListener(new MenuAdapter() {
			@Override
			public void menuShown(MenuEvent e) {
				showConnectionMenu();
			}
		});
		connectionMenuItem.setMenu(connectionsMenu);


		folders = new TabFolder(shell, SWT.NONE);
		folders.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		displayInitAdminServerTree();
	}

	public void displayInitAdminServerTree() {
		adminservers.clear();
		String filename = ".\\connections\\adminhosts";
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String line = br.readLine();
			while (line != null) {
				String[] tokens = line.split(":");
				TM1Admin adminserver = new TM1Admin(tokens[0], Integer.parseInt(tokens[1]));
				adminserver.readTM1ServersFromServer();
				adminservers.add(adminserver);
				line = br.readLine();
			}
			br.close();
		} catch (Exception ex) {
			System.out.println("Error reading adminhosts from file");
			System.out.println(ex.getMessage());
		}

		filename = ".\\connections\\ibmcloudservers";
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String line = br.readLine();
			while (line != null) {
				String[] tokens = line.split(":");
				TM1Admin adminserver = new TM1Admin(tokens[0]);
				for (int i = 1; i < tokens.length; i++) {
					// TM1Server cloudTM1Server = new TM1Server(tokens[0], tokens[i]);
					TM1ServerStub cloudTM1ServerStub = new TM1ServerStub(tokens[i], tokens[0]);
					adminserver.addCloudServer(cloudTM1ServerStub);
				}
				adminservers.add(adminserver);
				line = br.readLine();
			}
			br.close();
		} catch (Exception ex) {
			System.out.println("Error reading adminhosts from file");
			System.out.println(ex.getMessage());
		}

		
	}
	
	public void refreshFullExplorerHierarchy() {
		String filename = ".\\connections\\adminhosts";
		List<TM1Admin> foundadminservers = new ArrayList<TM1Admin>();
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String line = br.readLine();
			while (line != null) {
				String[] tokens = line.split(":");
				TM1Admin foundadminserver = new TM1Admin(tokens[0], Integer.parseInt(tokens[1]));
				foundadminservers.add(foundadminserver);
				line = br.readLine();
			}
			br.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		filename = ".\\connections\\ibmcloudservers";
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String line = br.readLine();
			while (line != null) {
				System.out.println(line);
				String[] tokens = line.split(":");
				TM1Admin adminserver = new TM1Admin(tokens[0]);
				for (int i = 1; i < tokens.length; i++) {
					TM1ServerStub cloudTM1ServerStub = new TM1ServerStub(tokens[i], tokens[0]);
					adminserver.addCloudServer(cloudTM1ServerStub);
				}
				adminservers.add(adminserver);
				line = br.readLine();
			}
			br.close();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}

		boolean found;
		for (int i = 0; i < foundadminservers.size(); i++) {
			found = false;
			for (int j = 0; j < adminservers.size(); j++) {
				if (foundadminservers.get(i).hostname.equals(adminservers.get(j).hostname))
					found = true;
			}
			if (!found)
				adminservers.add(foundadminservers.get(i));
		}

		boolean stillhas;
		for (int i = 0; i < adminservers.size(); i++) {
			stillhas = false;
			for (int j = 0; j < foundadminservers.size(); j++) {
				if (adminservers.get(i).hostname.equals(foundadminservers.get(j).hostname))
					stillhas = true;
			}
			if (!stillhas)
				adminservers.remove(i);
		}
	}
	
	public void infoMessage(String message) {
		MessageBox m = new MessageBox(shell, SWT.ICON_INFORMATION);
		m.setMessage(message);
		m.open();
	}

	public void errorMessage(String errMessage) {
		MessageBox m = new MessageBox(shell, SWT.ICON_ERROR);
		m.setMessage(errMessage);
		m.open();
	}

	public void errorMessage(String code, String message) {
		MessageBox m = new MessageBox(shell, SWT.ICON_ERROR);
		m.setMessage("Error code " + code + "\n" + message);
		m.open();
	}

	/*
	public void checkForReconnect(TreeItem serverNode) {
		TM1Server tm1server = (TM1Server) serverNode.getData();
		if (!tm1server.isAuthenticated()) {
			infoMessage("Disconnected from " + tm1server.getName());
			connectToTM1ServerAs();
			this.refreshTM1AdminHostTree(serverNode.getParentItem());
		}
	}
	*/

	public void connectToServer(TM1ServerStub tm1ServerStub) {
		try {
			// System.out.println("=> connectToTM1ServerAs");
			//TreeItem selectednode = connectionsTree.getSelection()[0];
			//TM1ServerStub tm1ServerStub = (TM1ServerStub) (connectionsTree.getSelection()[0].getData());
			
			TM1Server tm1Server;
			if (tm1ServerStub.isCloud) {
				tm1Server = new TM1Server(tm1ServerStub.hostname, tm1ServerStub.name);
			} else {
				tm1Server = new TM1Server(tm1ServerStub.hostname, tm1ServerStub.name, tm1ServerStub.port, tm1ServerStub.useSSL);
			}
			tm1Server.readSecurityMode();
			// System.out.println("Read Security Mode OK");
			if (!tm1Server.isAuthenticated()) {
				// System.out.println("Not Authenticated Yet");
				while (!tm1Server.isAuthenticated()) {
					// System.out.println("Still Not Authenticated");
					ConnectAs dialog = new ConnectAs(shell, tm1Server);
					if (dialog.open()) {
						Credential c = dialog.getcredential();
						// System.out.println("Connecting as " + c.username);
						if (tm1Server.authenticate(c)) {
							TabItem serverExplorerTab = new TabItem(folders, SWT.CLOSE);
							serverExplorerTab.setText(tm1Server.getAdminHostName() + ":" + tm1Server.getName());
							ServerExplorerComposite serverExplorerComposite = new ServerExplorerComposite(folders, tm1Server);
							serverExplorerTab.setControl(serverExplorerComposite);
							serverExplorerComposite.setLayout(new GridLayout(1, false));
							serverExplorerComposite.updateTM1ServerNode();
							folders.setSelection(folders.getItemCount() - 1);
						} else {
							MessageBox m = new MessageBox(shell, SWT.ERROR | SWT.OK);
							m.setText("Error");
							m.setMessage("Failed to authenticate. Invalid credentials");
							m.open();
						}
					} else {
						break;
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			if (ex instanceof TM1RestException) {
				TM1RestException restException = (TM1RestException) ex;
				MessageBox m = new MessageBox(shell, SWT.ERROR | SWT.OK);
				m.setText("Error");
				m.setMessage("Failed to connect to server: " + restException.getErrorCode());
				if (m.open() == SWT.OK) {
					connectToServer(tm1ServerStub);
				}
			} else {
				MessageBox m = new MessageBox(shell, SWT.ERROR | SWT.OK);
				m.setText("Error");
				m.setMessage(ex.getMessage());
			}
		}
	}

	/*
	public boolean disconnectFromTM1Server() {
		TreeItem selectednode = connectionsTree.getSelection()[0];
		TM1ServerStub tm1ServerStub = (TM1ServerStub) selectednode.getData();
		if (tm1ServerStub.isAuthenticated) {
			connectionsTree.getSelection()[0].setImage(DISCONNECTEDICON);
			tm1ServerStub.setUnAuthenticated();
			return true;
		}
		return false;
	}
	*/

	public void setFolders(TabFolder folders) {
		this.folders = folders;
	}
	
	private void showConnectionMenu() {
		for (MenuItem item : connectionsMenu.getItems()) {
		    item.dispose();
		}
		
		MenuItem adminServersMenuItem = new MenuItem(connectionsMenu, SWT.PUSH);
		adminServersMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				AdminServerConnections connectionoptions = new AdminServerConnections(shell);
				if (connectionoptions.open()) {
					// serverConnectorComposite.refreshFullExplorerHierarchy();
				}
			}
		});
		adminServersMenuItem.setText("Admin Servers\tF1");
		adminServersMenuItem.setAccelerator(SWT.F1);
		
		new MenuItem(connectionsMenu, SWT.SEPARATOR);
		
		for (int i=0; i<adminservers.size(); i++) {
			TM1Admin adminServer = adminservers.get(i);
			MenuItem adminServerMenuItem = new MenuItem(connectionsMenu, SWT.CASCADE);
			adminServerMenuItem.setText(adminServer.hostname);
			Menu m = new Menu(adminServerMenuItem);
			adminServerMenuItem.setMenu(m);
			for (int j = 0; j < adminServer.getservers().size(); j++) {
				TM1ServerStub tm1ServerStub = adminServer.getserver(j);
				final MenuItem serverMenuItem = new MenuItem(m, SWT.CASCADE);
				if (tm1ServerStub.isRestEnabled()) {
					serverMenuItem.setText(tm1ServerStub.name);
				} else {
					serverMenuItem.setText(tm1ServerStub.name + " (HTTP Disabled)");
				}
				serverMenuItem.setImage(DISCONNECTEDICON);
				serverMenuItem.setData(tm1ServerStub);
				serverMenuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent arg0) {
						TM1ServerStub tm1ServerStub = (TM1ServerStub)serverMenuItem.getData();
						connectToServer(tm1ServerStub);
					}
				});
				
		
			}
		}

	}
}
