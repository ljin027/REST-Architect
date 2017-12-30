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
import TM1Diagnostic.REST.TM1Server;
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

	public TabFolder folders;

	private TabItem activeTab;
	private TabItem connectionTab;
	private ServerConnectorComposite serverConnectorComposite;
	private ServerExplorerComposite activeServerExplorerComposite;

	private MenuItem viewMenuItem;
	private MenuItem loggingPaneMenuItem;
	private MenuItem modelConfigPaneMenuItem;
	private MenuItem showControlObjectsMenuItem;

	public boolean HTTPDEBUG;

	public Wrarchitect() {
		super();

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
		// displayInitAdminServerTree();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	protected void createContents() {
		shell = new Shell();
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

		MenuItem connectionsMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		connectionsMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				AdminServerConnections connectionoptions = new AdminServerConnections(shell);
				if (connectionoptions.open()) {
					serverConnectorComposite.refreshFullExplorerHierarchy();
				}
			}
		});
		connectionsMenuItem.setText("TM1Connections \tF1");
		connectionsMenuItem.setAccelerator(SWT.F1);
		
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

		MenuItem searchMenuItem = new MenuItem(menu, SWT.CASCADE);
		searchMenuItem.setText("&Search");

		Menu searchMenu = new Menu(shell, SWT.DROP_DOWN);
		searchMenuItem.setMenu(searchMenu);

		MenuItem findMenuItem = new MenuItem(searchMenu, SWT.PUSH);
		findMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (activeServerExplorerComposite == null) return;
				activeServerExplorerComposite.openSearch();
			}
		});
		findMenuItem.setText("&Find \tCtrl+F");
		findMenuItem.setAccelerator(SWT.CTRL + 'F');

		MenuItem findNextMenuItem = new MenuItem(searchMenu, SWT.PUSH);
		findNextMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				System.out.println("Finding Next...");
			}
		});
		findNextMenuItem.setText("Find Next \tF3");
		findNextMenuItem.setAccelerator(SWT.F3);

		MenuItem findPreviousMenuItem = new MenuItem(searchMenu, SWT.PUSH);
		findPreviousMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				System.out.println("Finding Previous...");
			}
		});
		findPreviousMenuItem.setText("Find Previous \tShift-F3");
		findPreviousMenuItem.setAccelerator(SWT.SHIFT + SWT.F3);

		viewMenuItem = new MenuItem(menu, SWT.CASCADE);

		viewMenuItem.setText("View");

		Menu viewMenu = new Menu(viewMenuItem);
		viewMenuItem.setMenu(viewMenu);

		loggingPaneMenuItem = new MenuItem(viewMenu, SWT.CHECK);
		loggingPaneMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (activeServerExplorerComposite.serverMessageTabIsOpen) {
					activeServerExplorerComposite.closeLoggingPane();
				} else {
					activeServerExplorerComposite.openLoggingPane();
				}
			}
		});
		loggingPaneMenuItem.setText("Logging");
		loggingPaneMenuItem.setEnabled(false);

		/*

		modelConfigPaneMenuItem.setText("Model Configuration");
		modelConfigPaneMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (activeServerExplorerComposite.modelConfigurationTabIsOpen) {
					activeServerExplorerComposite.closeModelConfigurationPane();
				} else {
					activeServerExplorerComposite.openModelConfigurationPane();
				}
			}
		});
		modelConfigPaneMenuItem.setEnabled(false);
		*/

		/*showControlObjectsMenuItem = new MenuItem(viewMenu, SWT.CHECK);
		showControlObjectsMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (activeServerExplorerComposite.showControlObjects) {
					activeServerExplorerComposite.showControlObjects = false;
				} else {
					activeServerExplorerComposite.showControlObjects = true;
				}
				activeServerExplorerComposite.updateTM1ServerNode();
			}
		});
		showControlObjectsMenuItem.setText("Show Control Objects");
		showControlObjectsMenuItem.setEnabled(false);
		*/

		MenuItem refreshMenuItem = new MenuItem(viewMenu, SWT.NONE);
		// mntmRefresh.setEnabled(false);
		refreshMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				activeTab = folders.getSelection()[0];
				if (activeTab.getControl() instanceof ServerConnectorComposite) {
					// System.out.println("Refreshing Server Connector...");
					serverConnectorComposite.refreshFullExplorerHierarchy();
				} else if (activeTab.getControl() instanceof ServerExplorerComposite) {
					// System.out.println("Refreshing Server Explorer...");
					ServerExplorerComposite s = (ServerExplorerComposite) activeTab.getControl();
					s.updateTM1ServerNode();
				}
			}
		});
		refreshMenuItem.setText("Refresh \tF5");
		refreshMenuItem.setAccelerator(SWT.F5);

		viewMenu.addMenuListener(new MenuListener() {
			@Override
			public void menuHidden(MenuEvent arg0) {
			}

			@Override
			public void menuShown(MenuEvent arg0) {
				if (activeServerExplorerComposite != null) {
					loggingPaneMenuItem.setSelection(activeServerExplorerComposite.serverMessageTabIsOpen);
					modelConfigPaneMenuItem.setSelection(activeServerExplorerComposite.modelConfigurationTabIsOpen);
					showControlObjectsMenuItem.setSelection(activeServerExplorerComposite.showControlObjects);
				}
			}
		});

		/*MenuItem optionsMenuItem = new MenuItem(menu, SWT.CASCADE);
		optionsMenuItem.setText("&Server Options");

		Menu optionsMenu = new Menu(optionsMenuItem);
		optionsMenuItem.setMenu(optionsMenu);

		disconnectMenuItem = new MenuItem(optionsMenu, SWT.NONE);
		disconnectMenuItem.setText("Disconnect");
		disconnectMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				activeServerExplorerComposite.disconnectServer();
				TabItem t = folders.getSelection()[0];
				folders.setSelection(0);
				t.dispose();
				activeServerExplorerComposite = null;
				serverConnectorComposite.refreshFullExplorerHierarchy();
			}
		});
		disconnectMenuItem.setEnabled(false);

		exportMenuItem = new MenuItem(optionsMenu, SWT.NONE);
		exportMenuItem.setText("Transfer");
		exportMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				activeServerExplorerComposite.exportTM1Objects();
			}
		});
		exportMenuItem.setEnabled(false);

		transactionQueryMenuItem = new MenuItem(optionsMenu, SWT.NONE);
		transactionQueryMenuItem.setText("Transaction Query");
		transactionQueryMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				activeServerExplorerComposite.openTransactionQueryViewer();
			}
		});
		transactionQueryMenuItem.setEnabled(false);

		sandboxMenuItem = new MenuItem(optionsMenu, SWT.NONE);
		sandboxMenuItem.setText("Sandboxes");
		sandboxMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				activeServerExplorerComposite.openSandboxViewer();
			}
		});
		sandboxMenuItem.setEnabled(false);
		
		*/

		folders = new TabFolder(shell, SWT.NONE);
		folders.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		/*
		folders.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent event) {
				TabItem selectedTab = folders.getSelection()[0];
				if (selectedTab.getControl() instanceof Composite_ServerExplorer) {
					setActiveServerExplorerComposite((Composite_ServerExplorer) selectedTab.getControl());
				} else {
					setActiveServerExplorerComposite(null);
				}
			}
		});
		*/

		serverConnectorComposite = new ServerConnectorComposite(folders);
		serverConnectorComposite.setFolders(folders);
		//serverConnectorComposite.setActiveTab(activeTab);
		//serverConnectorComposite.setConnectorTab(connectionTab);
		serverConnectorComposite.setWrarchitectMainWindow(this);
		serverConnectorComposite.setLayout(new GridLayout(1, false));
		

		
		connectionTab = new TabItem(folders, SWT.NONE);
		connectionTab.setText("Connections");
		connectionTab.setControl(serverConnectorComposite);
		activeTab = connectionTab;

	}

	/*
	public void setActiveServerExplorerComposite(Composite_ServerExplorer activeServerExplorerComposite) {
		this.activeServerExplorerComposite = activeServerExplorerComposite;
		if (activeServerExplorerComposite == null) {
			loggingPaneMenuItem.setEnabled(false);
			//modelConfigPaneMenuItem.setEnabled(false);
			//showControlObjectsMenuItem.setEnabled(false);
			//exportMenuItem.setEnabled(false);
			//sandboxMenuItem.setEnabled(false);
			//transactionQueryMenuItem.setEnabled(false);
			//disconnectMenuItem.setEnabled(false);

		} else {
			loggingPaneMenuItem.setEnabled(true);
			//modelConfigPaneMenuItem.setEnabled(true);
			//showControlObjectsMenuItem.setEnabled(true);
			exportMenuItem.setEnabled(true);
			sandboxMenuItem.setEnabled(true);
			transactionQueryMenuItem.setEnabled(true);
			disconnectMenuItem.setEnabled(true);
		}
	}
	*/
}
