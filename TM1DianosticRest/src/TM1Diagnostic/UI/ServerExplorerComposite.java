package TM1Diagnostic.UI;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipOutputStream;

import org.apache.http.client.ClientProtocolException;
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.OrderedJSONObject;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;

import TM1Diagnostic.ChoreWorker;
import TM1Diagnostic.Credential;
import TM1Diagnostic.SearchResult;
import TM1Diagnostic.ZipUtils;
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
import TM1Diagnostic.REST.TM1Subset;
import TM1Diagnostic.REST.TM1View;

import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Table;

public class ServerExplorerComposite extends Composite {

	static private Image FOLDERICON;
	static private Image ADMINICON;
	static private Image CONNECTEDICON;
	static private Image DISCONNECTEDICON;
	static private Image APPICON;
	static private Image DIMICON;
	static private Image CUBEICON;
	static private Image PROICON;
	static private Image CHOICON;
	static private Image VIEWICON;
	static private Image SUBICON;
	static private Image HIERICON;
	static private Image RULEICON;
	static private Image EXCELICON;
	static private Image PRIVVIEWICON;
	static private Image PRIVSUBICON;
	static private Image BLOBICON;
	static private Image SECURITY_ICON;


	static private Image SAVE;
	static private Image SAVEAS;
	static private Image EXECUTE;

	static private int SERVER = 0;
	static private int CUBE = 1;
	static private int DIMENSION = 2;
	static private int PROCESS = 3;
	static private int CHORE = 4;
	static private int VIEW = 5;
	static private int HIERARCHY = 6;
	static private int SUBSET = 7;
	static private int ELEMENT = 8;
	static private int APPLICATION = 9;
	static private int CELL = 10;
	static private int BLOB = 11;
	static private int FOLDER = 12;
	static private int REFERENCE = 13;
	static private int DOCUMENT = 14;

	static private int PUBLIC = 0;
	static private int PRIVATE = 1;

	protected Display display;
	protected Shell shell;

	private Wrarchitect wrarchitectMainWindow;
	private UI_SearchResult searchResult;

	private TM1Server tm1server;

	private Tree explorerTree;
	private TreeItem applicationListNode;
	private TreeItem cubeListNode;
	private TreeItem dimensionListNode;
	private TreeItem processListNode;
	private TreeItem choreListNode;
	private TreeItem blobListNode;

	private Menu serverExplorerContextMenu;

	private SashForm loggingSashForm;
	private TabFolder serverLoggingTabs;
	private SashForm modelConfigurationSashForm;

	private int keepAliveInterval;

	private Text connectionDetailsLabel;


	private TabItem messageLogTab;
	private TabItem threadLogTab;
	private TabItem httpTraceTab;

	private Table serverMessageLogTable;
	//private Text messageLogRowCountText;
	private int messageLogRowCount;

	private Runnable keepAliveTimer;

	private Table threadMonitorTable;
	private Button threadMonitorFilterIdleCheckButton;

	public boolean showControlObjects;
	public boolean keepAlive;
	public boolean serverMessageTabIsOpen;
	private int[] loggingSashWeight;
	public boolean modelConfigurationTabIsOpen;
	private int[] modelConfigurationSashWeight;

	//private Button threadMonitorFileLogCheckButton;
	private boolean logThreadsToFile;
	private Label messageLogRowCountLabel;


	private Text sessionIDText;

	private Composite composite_1;
	private Button controlObjectsButton;
	private Button loggersButton;
	private Button keepAliveCheckButton;
	private Button modelConfigurationButton;
	private Button transactionQueryButton;
	private Button btnNewButton;
	private Button disconnectButton;
	private Button btnImport;

	public ServerExplorerComposite(Composite parent, TM1Server tm1server) {
		super(parent, SWT.EMBEDDED);
		display = parent.getDisplay();
		this.tm1server = tm1server;
		onOpen();
	}

	private void onOpen() {
		shell = this.getShell();
		setLayout(new GridLayout(1, false));
		searchResult = new UI_SearchResult(shell, this, tm1server);
		modelConfigurationTabIsOpen = true;
		serverMessageTabIsOpen = true;
		showControlObjects = false;
		logThreadsToFile = false;
		keepAlive = false;

		keepAliveInterval = 60;

		SAVE = new Image(this.getDisplay(), ".\\images\\action_save.gif");
		SAVEAS = new Image(this.getDisplay(), ".\\images\\action_save_as.gif");
		EXECUTE = new Image(this.getDisplay(), ".\\images\\action_run.gif");

		FOLDERICON = new Image(display, ".\\images\\icon_folder.gif");
		ADMINICON = new Image(display, ".\\images\\icon_admin.gif");
		CONNECTEDICON = new Image(display, ".\\images\\icon_connected.gif");
		DISCONNECTEDICON = new Image(display, ".\\images\\icon_disconnected.gif");
		APPICON = new Image(display, ".\\images\\icon_app.gif");
		DIMICON = new Image(display, ".\\images\\icon_dimension.gif");
		CUBEICON = new Image(display, ".\\images\\icon_cube.gif");
		PROICON = new Image(display, ".\\images\\icon_process.gif");
		CHOICON = new Image(display, ".\\images\\icon_chore.gif");
		VIEWICON = new Image(display, ".\\images\\icon_view.gif");
		SUBICON = new Image(display, ".\\images\\icon_subset.gif");
		HIERICON = new Image(display, ".\\images\\icon_hierarchy.gif");
		RULEICON = new Image(display, ".\\images\\icon_rules.gif");
		EXCELICON = new Image(display, ".\\images\\icon_excel.gif");
		PRIVVIEWICON = new Image(display, ".\\images\\icon_views_private.gif");
		PRIVSUBICON = new Image(display, ".\\images\\icon_subset.gif");
		BLOBICON = new Image(display, ".\\images\\icon_table.gif");
		SECURITY_ICON = new Image(display, ".\\images\\key.gif");

		composite_1 = new Composite(this, SWT.NONE);
		composite_1.setLayout(new GridLayout(7, false));
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		keepAliveCheckButton = new Button(composite_1, SWT.CHECK);
		keepAliveCheckButton.setText("Keep Alive");
		keepAliveCheckButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (keepAliveCheckButton.getSelection()){
					startKeepAlive();
				} else {
					stopKeepAlive();
				}
			}
		});

		controlObjectsButton = new Button(composite_1, SWT.CHECK);
		controlObjectsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				toggleControlObjects();
			}
		});
		controlObjectsButton.setToolTipText("Enable/Disable Control Objects");
		controlObjectsButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		controlObjectsButton.setText("Control Objects");
		
		btnImport = new Button(composite_1, SWT.NONE);
		btnImport.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openExportImportEditor();
			}
		});
		GridData gd_btnImport = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnImport.widthHint = 120;
		btnImport.setLayoutData(gd_btnImport);
		btnImport.setText("Import");

		transactionQueryButton = new Button(composite_1, SWT.NONE);
		transactionQueryButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				openTransactionQueryViewer();
			}
		});
		GridData gd_transactionQueryButton = new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1);
		gd_transactionQueryButton.widthHint = 120;
		transactionQueryButton.setLayoutData(gd_transactionQueryButton);
		transactionQueryButton.setText("Transactions");
		transactionQueryButton.setToolTipText("Transaction Query Tool");

		btnNewButton = new Button(composite_1, SWT.NONE);
		GridData gd_btnNewButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnNewButton.widthHint = 120;
		btnNewButton.setLayoutData(gd_btnNewButton);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				openCellSecurityReport();
			}
		});
		btnNewButton.setText("Cell Security");

		modelConfigurationButton = new Button(composite_1, SWT.NONE);
		GridData gd_modelConfigurationButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_modelConfigurationButton.widthHint = 120;
		modelConfigurationButton.setLayoutData(gd_modelConfigurationButton);
		modelConfigurationButton.setToolTipText("TM1 Model Configuration");
		modelConfigurationButton.setText("Model Config");
		modelConfigurationButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				openModelConfigWindow();
			}
		});

		loggersButton = new Button(composite_1, SWT.NONE);
		loggersButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				openLoggerConfigWindow();
			}
		});
		GridData gd_loggersButton = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_loggersButton.widthHint = 120;
		loggersButton.setLayoutData(gd_loggersButton);
		loggersButton.setToolTipText("TM1 Model Logging Configuration");
		loggersButton.setText(" Logging Config");

		loggingSashForm = new SashForm(this, SWT.VERTICAL);
		GridData gd_loggingSashForm = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_loggingSashForm.heightHint = 200;
		loggingSashForm.setLayoutData(gd_loggingSashForm);

		loggingSashWeight = new int[] { 2, 1 };

		Composite treeComposite = new Composite(loggingSashForm, SWT.NONE);
		treeComposite.setLayout(new GridLayout(1, false));

		modelConfigurationSashForm = new SashForm(treeComposite, SWT.NONE);
		modelConfigurationSashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		modelConfigurationSashWeight = new int[] { 1, 3 };

		explorerTree = new Tree(modelConfigurationSashForm, SWT.BORDER | SWT.MULTI);
		explorerTree.setBounds(0, 0, 89, 89);

		explorerTree.addListener(SWT.MouseDoubleClick, new Listener() {
			public void handleEvent(Event event) {
				if (explorerTree.getSelectionCount() == 0) {
					// Nothing is selected so do nothing
				} else if (explorerTree.getSelection()[0].getData() instanceof TM1Server) {
					TM1Server tm1server = (TM1Server) explorerTree.getSelection()[0].getData();
					if (!tm1server.isAuthenticated()) {
						connectToTM1ServerAs();
					}
				} else if (explorerTree.getSelection()[0].getData() instanceof TM1Cube) {

				} else if (explorerTree.getSelection()[0].getData() instanceof TM1View) {
					TM1View view = (TM1View) explorerTree.getSelection()[0].getData();
					TM1Cube cube = (TM1Cube) view.getParent();
					openViewEditor(cube, view);
				} else if (explorerTree.getSelection()[0].getData() instanceof TM1Dimension) {

				} else if (explorerTree.getSelection()[0].getData() instanceof TM1Hierarchy) {
					TM1Hierarchy hierarchy = (TM1Hierarchy) explorerTree.getSelection()[0].getData();
					openHierarchyEditor(hierarchy);
				} else if (explorerTree.getSelection()[0].getData() instanceof TM1Subset) {
					TM1Subset subset = (TM1Subset) explorerTree.getSelection()[0].getData();
					openSubsetEditor(subset);
				} else if (explorerTree.getSelection()[0].getData() instanceof TM1Process) {
					TM1Process process = (TM1Process) explorerTree.getSelection()[0].getData();
					openProcessEditor(process);
				} else if (explorerTree.getSelection()[0].getData() instanceof TM1Chore) {
					TM1Chore chore = (TM1Chore) explorerTree.getSelection()[0].getData();
					openChoreEditor(chore);
				} else if (explorerTree.getSelection()[0].getData() instanceof TM1Blob) {
					TM1Blob blob = (TM1Blob) explorerTree.getSelection()[0].getData();
					openBlobEditor(blob);

				} else if (explorerTree.getSelection()[0].getData() instanceof String) {
					String s_node = (String) explorerTree.getSelection()[0].getData();
					if (s_node.equals("Rules")) {
						TM1Cube cube = (TM1Cube) explorerTree.getSelection()[0].getParentItem().getData();
						openRuleEditor(cube);
					}
				}
			}
		});

		explorerTree.addTreeListener(new TreeListener() {
			public void treeExpanded(TreeEvent event) {
				TreeItem node = (TreeItem) event.item;
				Object o = node.getData();
				if (o instanceof TM1Cube) {
					TM1Cube cube = (TM1Cube) o;
					cube.expandedInExplorerTree = true;
					updateCubeNode(node);
				} else if (o instanceof TM1Dimension) {
					try {
						//System.out.println("Start expand");
						TM1Dimension dimension = (TM1Dimension) o;
						dimension.expandedInExplorerTree = true;
						updateDimensionNode(node);
						//System.out.println("Finish expand");
					} catch (Exception ex){
						//System.out.println("Expand Exception");
						exception(ex);
					} 

				} else if (o instanceof TM1Hierarchy) {
					TM1Hierarchy hierarchy = (TM1Hierarchy) o;
					hierarchy.expandedInExplorerTree = true;
				} else if (o instanceof TM1Subset) {
				} else if (o instanceof TM1View) {
				} else if (o instanceof String) {
					String nodename = (String) o;
					if (nodename.equals("Applications")) {
						tm1server.expandapplications();
					} else if (nodename.equals("Cubes")) {
						tm1server.expandcubes();
					} else if (nodename.equals("Dimensions")) {
						if (node.getParentItem() == null) {
							tm1server.expanddimensions();
						} else if (node.getParentItem().getData() instanceof TM1Cube) {
							TM1Cube cube = (TM1Cube) node.getParentItem().getData();
							cube.dimensionsExpandedInServerExplorer = true;
						}
					} else if (nodename.equals("Processes")) {
						tm1server.expandprocesses();
					} else if (nodename.equals("Chores")) {
						tm1server.expandchores();
					} else if (nodename.equals("Hierarchies")) {
						TreeItem dimensionNode = node.getParentItem();
						TM1Dimension dimension = (TM1Dimension) dimensionNode.getData();
						dimension.heirarchiesExpandedInServerExplorer = true;
					} else if (nodename.equals("Subsets")) {
						TreeItem hierarchyNode = node.getParentItem();
						TM1Hierarchy hierarchy = (TM1Hierarchy) hierarchyNode.getData();
						hierarchy.subsetsExpandedInServerExplorer = true;
					} else if (nodename.equals("Views")) {
						TreeItem cubeNode = node.getParentItem();
						TM1Cube cube = (TM1Cube) cubeNode.getData();
						cube.viewsExpandedInServerExplorer = true;
					} else {
						//

					}
				}
			}

			public void treeCollapsed(TreeEvent event) {
				TreeItem node = (TreeItem) event.item;
				Object o = node.getData();
				if (o instanceof TM1Cube) {
					TM1Cube cube = (TM1Cube) o;
					cube.expandedInExplorerTree = false;
				} else if (o instanceof TM1Dimension) {
					TM1Dimension dimension = (TM1Dimension) o;
					dimension.expandedInExplorerTree = false;
				} else if (o instanceof TM1Hierarchy) {
					TM1Hierarchy hierarchy = (TM1Hierarchy) o;
					hierarchy.expandedInExplorerTree = false;
				} else if (node.getData() instanceof String) {
					String nodename = (String) node.getData();
					if (nodename.equals("Applications")) {
						tm1server.collapseapplications();
					} else if (nodename.equals("Cubes")) {
						tm1server.collapsecubes();
					} else if (nodename.equals("Dimensions")) {
						if (node.getParentItem() == null) {
							tm1server.expanddimensions();
						} else if (node.getParentItem().getData() instanceof TM1Cube) {
							TM1Cube cube = (TM1Cube) node.getParentItem().getData();
							cube.dimensionsExpandedInServerExplorer = false;
						}
					} else if (nodename.equals("Hierarchies")) {
						TreeItem dimensionNode = node.getParentItem();
						TM1Dimension dimension = (TM1Dimension) dimensionNode.getData();
						dimension.heirarchiesExpandedInServerExplorer = false;
					} else if (nodename.equals("Processes")) {
						tm1server.collapseprocesses();
					} else if (nodename.equals("Chores")) {
						tm1server.collapsechores();
					} else if (nodename.equals("Hierarchies")) {
						TreeItem dimensionNode = node.getParentItem();
						TM1Dimension dimension = (TM1Dimension) dimensionNode.getData();
						dimension.heirarchiesExpandedInServerExplorer = false;
					} else if (nodename.equals("Subsets")) {
						TreeItem hierarchyNode = node.getParentItem();
						TM1Hierarchy hierarchy = (TM1Hierarchy) hierarchyNode.getData();
						hierarchy.subsetsExpandedInServerExplorer = false;
					} else if (nodename.equals("Views")) {
						TreeItem cubeNode = node.getParentItem();
						TM1Cube cube = (TM1Cube) cubeNode.getData();
						cube.viewsExpandedInServerExplorer = false;
					} else {

					}
				}

			}
		});

		serverExplorerContextMenu = new Menu(explorerTree);
		explorerTree.setMenu(serverExplorerContextMenu);
		modelConfigurationSashForm.setWeights(new int[] {1});
		serverExplorerContextMenu.addMenuListener(new MenuAdapter() {
			public void menuShown(MenuEvent e) {
				MenuItem[] items = serverExplorerContextMenu.getItems();
				for (int i = 0; i < items.length; i++) {
					items[i].dispose();
				}
				if (explorerTree.getSelectionCount() == 0) {
					// Nothing selected
				} else if (explorerTree.getSelectionCount() == 1) {
					TreeItem selectedItem = explorerTree.getSelection()[0];
					Object o = explorerTree.getSelection()[0].getData();
					if (o instanceof TM1Server) {
					} else if (o instanceof String) {
						String nodeText = (String) o;
						if (nodeText.equals("Cubes")) {
							showCubesMenu();
						} else if (nodeText.equals("Dimensions")) {
							showDimensionsMenu();
						} else if (nodeText.equals("Subsets") || nodeText.equals("Private Subsets")) {
							showSubsetsMenu(selectedItem);
						} else if (nodeText.equals("Views") || nodeText.equals("Private Views")) {
							showViewsMenu(selectedItem);
						} else if (nodeText.equals("Rules")) {
							showRulesMenu(selectedItem);
						} else if (nodeText.equals("Processes")) {
							showProcessesMenu();
						} else if (nodeText.equals("Chores")) {
							showChoresMenu();
						} else if (nodeText.equals("Blobs")) {
							showBlobsMenu();
						} else {
							// Unknown item selected
						}
					} else if (o instanceof TM1Cube) {
						showCubeMenu(selectedItem);
					} else if (o instanceof TM1View) {
						showViewMenu(selectedItem);
					} else if (o instanceof TM1Blob) {
						showBlobMenu(selectedItem);
					} else if (o instanceof TM1Hierarchy) {
						showHierarchyMenu(selectedItem);
					} else if (o instanceof TM1Dimension) {
						showDimensionMenu(selectedItem);
					} else if (o instanceof TM1Subset) {
						showSubsetMenu(selectedItem);
					} else if (o instanceof TM1Process) {
						showProcessMenu(selectedItem);
					} else if (o instanceof TM1Chore) {
						showChoreMenu(selectedItem);
					}
				} else {
					// Multi Select Menu
					showMultiSelectMenu();
				}
			}
		});
		// sashForm_1.setWeights(new int[] { 1, 0 });

		serverLoggingTabs = new TabFolder(loggingSashForm, SWT.BORDER);

		httpTraceTab = new TabItem(serverLoggingTabs, SWT.NONE);
		httpTraceTab.setText("HTTP Trace");
		RestTraceComposite compositeHttpTrace = new RestTraceComposite(serverLoggingTabs, this, tm1server);
		httpTraceTab.setControl(compositeHttpTrace);
		tm1server.setHttpTraceDisplay(compositeHttpTrace.httpTraceStyledText);

		messageLogTab = new TabItem(serverLoggingTabs, SWT.NONE);
		ServerMessageLogComposite compositeServerMessageLog = new ServerMessageLogComposite(serverLoggingTabs, this, tm1server);
		messageLogTab.setControl(compositeServerMessageLog);
		messageLogTab.setText("Message Log");

		threadLogTab = new TabItem(serverLoggingTabs, SWT.NONE);
		try {
			ThreadMonitorComposite compositeThreadLog = new ThreadMonitorComposite(serverLoggingTabs, this, tm1server);
			threadLogTab.setControl(compositeThreadLog);
			threadLogTab.setText("Thread Monitor");
		} catch (TM1RestException ex){
			exception(ex);
		}

		Composite composite = new Composite(this, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		connectionDetailsLabel = new Text(composite, SWT.READ_ONLY);
		connectionDetailsLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		connectionDetailsLabel.setText("Admin Host:  Server Name:  Version: ");

		sessionIDText = new Text(composite, SWT.READ_ONLY);
		sessionIDText.setText("Session ID:");
		sessionIDText.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));

		sessionIDText.setText("TM1SessionID: " + tm1server.getSessionID());
		try {
			connectionDetailsLabel.setText("Host: " + tm1server.getAdminHostName() + "Model: " + tm1server.getName() + "Version: " + tm1server.readVersionFromServer());
			
			disconnectButton = new Button(composite, SWT.NONE);
			disconnectButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					disconnectServer();
				}
			});
			GridData gd_disconnectButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gd_disconnectButton.widthHint = 120;
			disconnectButton.setLayoutData(gd_disconnectButton);
			disconnectButton.setText("Disconnect");
		} catch (TM1RestException | URISyntaxException | IOException | JSONException ex) {
			// TODO Auto-generated catch block
			exception(ex);
		}
	}



	public void updateTM1ServerNode()  {
		System.out.println("=> updateTM1ServerNode");
		explorerTree.removeAll();

		explorerTree.setRedraw(false);
		TreeItem appnode = new TreeItem(explorerTree, SWT.NONE);
		appnode.setText("Applications");
		appnode.setImage(APPICON);
		applicationListNode = appnode;
		updateApplicationsListNode(appnode);

		TreeItem cubesnode = new TreeItem(explorerTree, SWT.NONE);
		cubesnode.setText("Cubes");
		cubesnode.setImage(CUBEICON);
		cubesnode.setData("Cubes");
		cubeListNode = cubesnode;
		updateCubeListNode(cubesnode);
		if (tm1server.cubesexpanded()) {
			cubesnode.setExpanded(true);
		}

		TreeItem dimsnode = new TreeItem(explorerTree, SWT.NONE);
		dimsnode.setText("Dimensions");
		dimsnode.setImage(DIMICON);
		dimsnode.setData("Dimensions");
		dimensionListNode = dimsnode;
		updateDimensionListNode(dimsnode);
		if (tm1server.dimensionsexpanded()) {
			dimsnode.setExpanded(true);
		}

		TreeItem processesnode = new TreeItem(explorerTree, SWT.NONE);
		processesnode.setText("Processes");
		processesnode.setImage(PROICON);
		processesnode.setData("Processes");
		processListNode = processesnode;
		updateProcessListNode();
		if (tm1server.processesexpanded()) {
			processesnode.setExpanded(true);
		}

		TreeItem choresnode = new TreeItem(explorerTree, SWT.NONE);
		choresnode.setText("Chores");
		choresnode.setImage(CHOICON);
		choresnode.setData("Chores");
		choreListNode = choresnode;
		updateChoreListNode(choresnode);
		if (tm1server.choresexpanded()) {
			choresnode.setExpanded(true);
		}

		TreeItem blobsnode = new TreeItem(explorerTree, SWT.NONE);
		blobsnode.setText("Blobs");
		blobsnode.setImage(BLOBICON);
		blobsnode.setData("Blobs");
		blobListNode = blobsnode;
		updateBlobListNode();

		explorerTree.setRedraw(true);
	}

	public void removeTM1ServerTree(TreeItem servernode) {
		servernode.removeAll();
	}

	public TreeItem getDimensionTreeItemParent(TreeItem node) {
		if (node.getParentItem().getData() instanceof TM1Dimension)
			return node.getParentItem();
		return getDimensionTreeItemParent(node.getParentItem());
	}

	public void expandNode(TreeItem node) {
		node.setExpanded(true);
		for (int i = 0; i < node.getItemCount(); i++) {
			expandNode(node.getItem(i));
		}
	}

	public void updateCubeListNode(TreeItem cubesnode) {
		cubesnode.removeAll();
		try {
			tm1server.readCubesFromServer();
			int count = tm1server.cubeCount();
			for (int i = 0; i < count; i++) {
				TM1Cube cube = tm1server.getCube(i);
				if (!showControlObjects && cube.displayName.startsWith("}")) {
				} else {
					TreeItem cubenode = new TreeItem(cubesnode, SWT.NONE);
					cubenode.setText(cube.displayName);
					cubenode.setImage(CUBEICON);
					cubenode.setData(cube);
					if (cube.expandedInExplorerTree) {
						updateCubeNode(cubenode);
						cubenode.setExpanded(true);
					} else {
						TreeItem cubeChildNode = new TreeItem(cubenode, SWT.NONE);
						cubeChildNode.setText("");
					}
				}
			}
			cubesnode.setText("Cubes");

		} catch (TM1RestException | URISyntaxException | IOException | JSONException ex){
			exception(ex);
		}
	}

	public void updateCubeNode(TreeItem cubenode) {
		try {
			if (!(cubenode.getData() instanceof TM1Cube)) return;
			TM1Cube cube = (TM1Cube) cubenode.getData();
			cubenode.removeAll();

			cube.readCubeViewsFromServer();

			TreeItem viewsnode = new TreeItem(cubenode, SWT.NONE);
			viewsnode.setText("Views");
			viewsnode.setData("Views");
			viewsnode.setImage(VIEWICON);
			updateViewListNode(viewsnode);
			if (cube.viewsExpandedInServerExplorer) {
				viewsnode.setExpanded(true);
			}

			TreeItem dimensionsnode = new TreeItem(cubenode, SWT.NONE);
			dimensionsnode.setText("Dimensions");
			dimensionsnode.setData("Dimensions");
			dimensionsnode.setImage(DIMICON);
			updateCubeDimensionListNode(dimensionsnode);
			if (cube.dimensionsExpandedInServerExplorer) {
				dimensionsnode.setExpanded(true);
			}

			if (cube.checkServerForRules()) {
				TreeItem rulenode = new TreeItem(cubenode, SWT.NONE);
				rulenode.setText("Rules");
				rulenode.setData("Rules");
				rulenode.setImage(RULEICON);
			}

			if (cube.checkServerForCellSecurity()){
				TM1Cube cellSecurityCube = cube.getCellSecurityCube();
				TreeItem cellSecurityCubeNode = new TreeItem(cubenode, SWT.NONE);
				cellSecurityCubeNode.setText(cellSecurityCube.displayName);
				cellSecurityCubeNode.setData(cellSecurityCube);
				cellSecurityCubeNode.setImage(CUBEICON);
				TreeItem cellSecurityCubeChildNode = new TreeItem(cellSecurityCubeNode, SWT.NONE);
			}
		} catch (TM1RestException | URISyntaxException | IOException ex){
			exception(ex);
		}

	}

	public void updateViewListNode(TreeItem viewsnode) {
		TreeItem cubenode = viewsnode.getParentItem();
		TM1Cube cube = (TM1Cube) cubenode.getData();
		viewsnode.removeAll();

		int privateViewCount = cube.privateViewCount();
		for (int i = 0; i < privateViewCount; i++) {
			TM1View view = cube.getPrivateView(i);
			TreeItem viewnode = new TreeItem(viewsnode, SWT.NONE);
			viewnode.setImage(PRIVVIEWICON);
			viewnode.setText(view.displayName);
			viewnode.setData(view);
		}

		int viewcount = cube.viewCount();
		for (int i = 0; i < viewcount; i++) {
			TM1View view = cube.getview(i);
			TreeItem viewnode = new TreeItem(viewsnode, SWT.NONE);
			viewnode.setImage(VIEWICON);
			viewnode.setText(view.displayName);
			viewnode.setData(view);
		}
		viewsnode.setText("Views");
	}

	public void updateDimensionListNode(TreeItem dimsnode) {
		dimsnode.removeAll();
		try {
			tm1server.readDimensionsFromServer();
			int count = tm1server.dimensionCount();
			for (int i = 0; i < count; i++) {
				TM1Dimension dimension = tm1server.getDimension(i);
				String dimensionname = dimension.displayName;
				if (!showControlObjects && dimensionname.startsWith("}")) {
				} else {
					TreeItem dimensionnode = new TreeItem(dimsnode, SWT.NONE);
					dimensionnode.setImage(DIMICON);
					dimensionnode.setData(dimension);
					dimensionnode.setText(dimensionname);

					if (dimension.expandedInExplorerTree) {
						updateDimensionNode(dimensionnode);
						dimensionnode.setExpanded(true);
					} else {
						TreeItem dimensionChildNode = new TreeItem(dimensionnode, SWT.NONE);
						dimensionChildNode.setText("");
					}
				}
			}
		} catch (TM1RestException | URISyntaxException | IOException | JSONException ex){
			exception(ex);
		}
		dimsnode.setText("Dimensions");
	}

	public void updateCubeDimensionListNode(TreeItem dimsnode) {
		dimsnode.removeAll();

		TreeItem cubeNode = dimsnode.getParentItem();
		TM1Cube cube = (TM1Cube) cubeNode.getData();
		try {
			cube.readCubeDimensionsFromServer();
			int count = cube.dimensionCount();
			for (int i = 0; i < count; i++) {
				TM1Dimension dimension = cube.getDimension(i);
				String dimensionname = dimension.displayName;
				TreeItem dimensionnode = new TreeItem(dimsnode, SWT.NONE);
				dimensionnode.setImage(DIMICON);
				dimensionnode.setData(dimension);
				dimensionnode.setText(dimensionname);

				if (dimension.expandedInExplorerTree) {
					updateDimensionNode(dimensionnode);
					dimensionnode.setExpanded(true);
				} else {
					TreeItem dimensionChildNode = new TreeItem(dimensionnode, SWT.NONE);
					dimensionChildNode.setText("");
				}
			}
		} catch (TM1RestException | URISyntaxException | IOException | JSONException ex){
			exception(ex);
		}
		// dimsnode.setText("Dimensions");
	}

	public void updateDimensionNode(TreeItem dimnode) throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		System.out.println("Start => updateDimensionNode " + dimnode.getText());
		if (!(dimnode.getData() instanceof TM1Dimension)) return;
		TM1Dimension dimension = (TM1Dimension) dimnode.getData();
		dimnode.removeAll();
		updateHierarchyListNode(dimnode);
		if (dimension.checkServerForElementAttributes()){
			TM1Cube attributeCube = dimension.getElementAttributeCube();
			TreeItem elementAttributesNode = new TreeItem(dimnode, SWT.NONE);
			elementAttributesNode.setText(attributeCube.displayName);
			elementAttributesNode.setData(attributeCube);
			elementAttributesNode.setImage(CUBEICON);
			TreeItem elementAttributesChildNode = new TreeItem(elementAttributesNode, SWT.NONE);
		}
		if (dimension.checkServerForElementSecurity()){
			TM1Cube securityCube = dimension.getElementSecurityCube();
			TreeItem elementSecurityNode = new TreeItem(dimnode, SWT.NONE);
			elementSecurityNode.setText(securityCube.displayName);
			elementSecurityNode.setData(securityCube);
			elementSecurityNode.setImage(SECURITY_ICON);
			TreeItem elementAttributesChildNode = new TreeItem(elementSecurityNode, SWT.NONE);
		}
		//System.out.println("Fin => updateDimensionNode " + dimnode.getText());

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

	public void reconnect() {
		if (!tm1server.isAuthenticated()) {
			ConnectAs dialog = new ConnectAs(shell, tm1server);
			if (dialog.open()) {
				Credential c = dialog.getcredential();
				try {
					if (tm1server.authenticate(c)) {
						updateTM1ServerNode();
						return;
					} else {
						MessageBox m = new MessageBox(shell, SWT.ERROR);
						m.setText("Error Connecting to TM1 Server");
						m.setMessage("!");
						m.open();
					}
				} catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException | IOException | TM1RestException ex) {
					//exception(ex);
				}
			}
		}
	}
	
	public void updateHierarchyListNode(TreeItem dimensionnode) throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		TM1Dimension dimension = (TM1Dimension) (dimensionnode.getData());
		dimensionnode.removeAll();
		dimension.readHierarchiesFromServer();
		int hierarchycount = dimension.hierarchyCount();
		for (int i = 0; i < hierarchycount; i++) {
			TM1Hierarchy hierarchy = dimension.getHeirarchy(i);
			String hierarchyname = hierarchy.displayName;
			TreeItem hierarchynode = new TreeItem(dimensionnode, SWT.NONE);
			hierarchynode.setText(hierarchyname);
			hierarchynode.setImage(HIERICON);
			hierarchynode.setData(hierarchy);
			if (!hierarchy.readSubsetsFromServer()) {
				reconnect();
				return;
			}
			updateSubsetListNode(hierarchynode);
			if (hierarchy.subsetsExpandedInServerExplorer) {
				hierarchynode.setExpanded(true);
			}
			if (hierarchy.expandedInExplorerTree) {
				hierarchynode.setExpanded(true);
			}
		}
	}

	public void updateSubsetListNode(TreeItem hierarchynode) {
		TM1Hierarchy hierarchy = (TM1Hierarchy) (hierarchynode.getData());
		hierarchynode.removeAll();
		int privateSubsetCount = hierarchy.privateSubsetCount();
		for (int i = 0; i < privateSubsetCount; i++) {
			TM1Subset subset = hierarchy.getPrivateSubset(i);
			String subsetname = subset.displayName;
			TreeItem subsetnode = new TreeItem(hierarchynode, SWT.NONE);
			subsetnode.setText(subsetname);
			subsetnode.setImage(SUBICON);
			subsetnode.setData(subset);
		}

		int subsetcount = hierarchy.subsetCount();
		for (int i = 0; i < subsetcount; i++) {
			TM1Subset subset = hierarchy.getSubset(i);
			String subsetname = subset.displayName;
			TreeItem subsetnode = new TreeItem(hierarchynode, SWT.NONE);
			subsetnode.setText(subsetname);
			subsetnode.setImage(PRIVSUBICON);
			subsetnode.setData(subset);
		}
	}

	public void updateProcessListNode() {
		try {
			processListNode.removeAll();
			tm1server.readProcessesFromServer();
			int count = tm1server.processCount();
			for (int i = 0; i < count; i++) {
				TM1Process process = tm1server.getProcess(i);
				String processname = process.displayName;
				if (!showControlObjects && processname.startsWith("}")) {
				} else {
					TreeItem processnode = new TreeItem(processListNode, SWT.NONE);
					processnode.setText(processname);
					processnode.setImage(PROICON);
					processnode.setData(process);
				}
			}
			processListNode.setText("Procesess");
		} catch (TM1RestException | URISyntaxException | IOException | JSONException ex){
			exception(ex);
		}
	}

	public void updateChoreListNode(TreeItem choreListNode) {
		try {
			choreListNode.removeAll();
			tm1server.readChoresFromServer();
			int chorecount = tm1server.choreCount();
			choreListNode.setText("Chores");
			for (int i = 0; i < chorecount; i++) {
				TM1Chore chore = tm1server.getChore(i);
				String chorename = chore.displayName;
				if (chore.active) {
					chorename = chorename + " (A)";
				}
				TreeItem chorenode = new TreeItem(choreListNode, SWT.NONE);
				chorenode.setText(chorename);
				chorenode.setImage(CHOICON);
				chorenode.setData(chore);
			}

		} catch (TM1RestException | URISyntaxException | IOException | JSONException ex){
			exception(ex);
		}
	}

	public void updateBlobListNode() {
		try {
			blobListNode.removeAll();
			tm1server.readBlobsFromServer();
			int blobcount = tm1server.getBlobCount();
			blobListNode.setText("Blobs");
			for (int i = 0; i < blobcount; i++) {
				TM1Blob blob = tm1server.getBlob(i);
				String blobname = blob.displayName;
				TreeItem blobnode = new TreeItem(blobListNode, SWT.NONE);
				blobnode.setText(blobname);
				blobnode.setImage(BLOBICON);
				blobnode.setData(blob);
			}
		} catch (TM1RestException | URISyntaxException | IOException | JSONException ex){
			exception(ex);
		}
	}

	public void updateApplicationsListNode(TreeItem applicationsnode) {
		try {
			applicationsnode.removeAll();
			tm1server.readFoldersFromServer();
			int applicationcount = tm1server.getFoldersCount();
			applicationsnode.setText("Applications");
			for (int i = 0; i < applicationcount; i++) {
				TM1Folder folder = tm1server.getFolder(i);
				String foldername = folder.displayName;
				TreeItem appnode = new TreeItem(applicationsnode, SWT.NONE);
				appnode.setText(foldername);
				appnode.setImage(FOLDERICON);
				appnode.setData(folder);
				updateApplicationNode(appnode);
			}
		} catch (TM1RestException | URISyntaxException | IOException | JSONException ex){
			exception(ex);
		}
	}

	public void updateApplicationNode(TreeItem applicationNode) {
		applicationNode.removeAll();
		TM1Folder tm1folder = (TM1Folder) applicationNode.getData();
		for (int i = 0; i < tm1folder.getFolderCount(); i++) {
			TM1Folder folder = tm1folder.getFolder(i);
			TreeItem childNode = new TreeItem(applicationNode, SWT.NONE);
			childNode.setText(folder.displayName);
			childNode.setImage(FOLDERICON);
			childNode.setData(folder);
			updateApplicationNode(childNode);
		}
		for (int i = 0; i < tm1folder.getReferenceCount(); i++) {
			TM1ObjectReference reference = tm1folder.getReference(i);
			TreeItem childNode = new TreeItem(applicationNode, SWT.NONE);
			childNode.setText(reference.displayName);
			if (reference.referenceType == CUBE) {
				childNode.setImage(CUBEICON);
			} else if (reference.referenceType == PROCESS) {
				childNode.setImage(PROICON);
			} else if (reference.referenceType == VIEW) {
				childNode.setImage(VIEWICON);
			} else if (reference.referenceType == DOCUMENT) {
				childNode.setImage(EXCELICON);
			}
			childNode.setData(reference);
		}
	}

	private void executeProcess(TM1Process process) {
		try {
			process.readProcessFromServer();
			UI_ProcessParameterPrompt p = new UI_ProcessParameterPrompt(shell, process, this);
			p.open();
		} catch (TM1RestException | URISyntaxException | IOException | JSONException ex){
			errorMessage(tm1server.getErrorMessage());
			exception(ex);
		}
	}

	public boolean connectToTM1ServerAs() {
		try {
			TreeItem selectednode = explorerTree.getSelection()[0];
			TM1Server tm1server = (TM1Server) (explorerTree.getSelection()[0].getData());
			if (tm1server.readSecurityMode()) {
				if (!tm1server.isAuthenticated()) {
					while (!tm1server.isAuthenticated()) {
						ConnectAs dialog = new ConnectAs(shell, tm1server);
						if (dialog.open()) {
							Credential c = dialog.getcredential();
							if (tm1server.authenticate(c)) {
								selectednode.setText(tm1server.getName() + " (" + tm1server.displayuser() + ")");
								selectednode.setImage(CONNECTEDICON);
								return true;
							} else {
								errorMessage(tm1server.getErrorMessage());
							}
						} else {
							return false;
						}
					}
				}
			}
			return false;
		} catch (Exception ex){
			exception(ex);
			return false;
		}
	}

	private void executeChore(TM1Chore chore) {
		ChoreWorker worker = new ChoreWorker(chore, this, true);
		worker.run();
	}

	private void showMultiSelectMenu() {
		MenuItem deleteMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		deleteMenuItem.setText("Delete Objects");
		deleteMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (deleteSelectedObjects() > 0) {
					updateTM1ServerNode();
				}
			}
		});

		MenuItem exportMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		exportMenuItem.setText("Export");
		exportMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				exportQuickSelectedObjects();
			}
		});
	}

	private void showCubesMenu() {
		MenuItem newCubeMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		newCubeMenuItem.setText("Create Cube");
		newCubeMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TreeItem cubesNode = explorerTree.getSelection()[0];
				CreateCube createCubeDialog = new CreateCube(shell, tm1server);
				try {
					if (createCubeDialog.open()) updateCubeListNode(cubesNode);
				} catch (TM1RestException | URISyntaxException | IOException ex){
					exception(ex);
				}
			}
		});

		MenuItem cubeSecurityMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		cubeSecurityMenuItem.setText("Cube Security");
		cubeSecurityMenuItem.setEnabled(false);

		MenuItem refreshCubesMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		refreshCubesMenuItem.setText("Refresh");
		refreshCubesMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TreeItem cubesnode = explorerTree.getSelection()[0];
				updateCubeListNode(cubesnode);
			}
		});
	}

	private void showDimensionsMenu()  {
		MenuItem newDimensionMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		newDimensionMenuItem.setText("Create Dimension");
		newDimensionMenuItem.setEnabled(true);
		newDimensionMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				createNewDimension();
			}
		});

		MenuItem dimensionSecurityMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		dimensionSecurityMenuItem.setText("Security");
		dimensionSecurityMenuItem.setEnabled(true);

		MenuItem refreshDimensionsMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		refreshDimensionsMenuItem.setText("Refresh");
		refreshDimensionsMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TreeItem dimensionsnode = explorerTree.getSelection()[0];

				updateDimensionListNode(dimensionsnode);
			}
		});
	}

	private void showHierarchiesMenu(TreeItem selectedItem) {
		MenuItem newHierarchyMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		newHierarchyMenuItem.setText("Create Hierarchy");

		MenuItem refreshHierarchiesMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		refreshHierarchiesMenuItem.setText("Refresh");
		refreshHierarchiesMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TreeItem t = explorerTree.getSelection()[0];
				try {
					updateHierarchyListNode(t);
				} catch (TM1RestException | URISyntaxException | IOException | JSONException ex) {
					// TODO Auto-generated catch block
					exception(ex);
				}
			}
		});
	}

	private void showSubsetsMenu(TreeItem selectedItem) {
		MenuItem newSubsetMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		newSubsetMenuItem.setText("New Subset");
		newSubsetMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TM1Hierarchy hierarchy = (TM1Hierarchy) (explorerTree.getSelection()[0].getParentItem().getData());
				try {
					if (!hierarchy.checkServerForPrivateSubset("Default")) {
						// System.out.println("Default private subset not found");
						hierarchy.readSubsetAllFromServer();
						TM1Subset subset = hierarchy.getSubsetAll();
						openSubsetEditor(subset);
					} else {
						// System.out.println("Default private subset found");
						TM1Subset subset = hierarchy.getPrivateSubsetByName("Default");
						openSubsetEditor(subset);
					}
				} catch (TM1RestException | URISyntaxException | IOException | JSONException ex){
					exception(ex);
				}
			}
		});
	}

	private void showViewsMenu(TreeItem selectedItem) {
		MenuItem newViewMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		newViewMenuItem.setText("New View");
		newViewMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TreeItem cubeNode = explorerTree.getSelection()[0].getParentItem();
				TM1Cube cube = (TM1Cube) cubeNode.getData();
				try {
					if (!cube.checkServerForDefaultView()) {
						cube.createDefaultPrivateView();
						openViewEditor(cube);
					} else {
						openViewEditor(cube);
					}
				} catch (TM1RestException | URISyntaxException | IOException | JSONException ex){
					exception(ex);
				}
			}
		});
	}

	private void showRulesMenu(TreeItem selectedItem) {
		MenuItem editRulesMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		editRulesMenuItem.setText("Edit Rules");
		editRulesMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TM1Cube cube = (TM1Cube) explorerTree.getSelection()[0].getParentItem().getData();
				openRuleEditor(cube);
			}
		});
	}

	private void showProcessesMenu() {
		MenuItem newProcessMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		newProcessMenuItem.setText("Create Process");
		newProcessMenuItem.setEnabled(true);
		newProcessMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				openProcessEditor(tm1server);
			}
		});

		MenuItem processSecurityMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		processSecurityMenuItem.setText("Processes Security");
		processSecurityMenuItem.setEnabled(false);

		MenuItem processesRefreshMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		processesRefreshMenuItem.setText("Refresh");
		processesRefreshMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				updateProcessListNode();
			}
		});
	}

	private void showChoresMenu() {
		MenuItem newChoreMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		newChoreMenuItem.setText("Create Chore");
		newChoreMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				openChoreEditor();
			}
		});

		MenuItem choresRefreshMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		choresRefreshMenuItem.setText("Refresh");
		choresRefreshMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TreeItem choresnode = explorerTree.getSelection()[0];
				updateChoreListNode(choresnode);
			}
		});
	}

	private void showCubeMenu(TreeItem selectedItem) {
		try {
			TM1Cube cube = (TM1Cube) selectedItem.getData();

			MenuItem cubePropertiesMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
			cubePropertiesMenuItem.setText("Cube Properties");
			cubePropertiesMenuItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					TM1Cube cube = (TM1Cube) explorerTree.getSelection()[0].getData();
					CubeProperties cubeproperties = new CubeProperties(shell, cube);
					cubeproperties.open();
				}
			});

			MenuItem mdxQuertMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
			mdxQuertMenuItem.setText("MDX Query");
			mdxQuertMenuItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					TM1Cube cube = (TM1Cube) explorerTree.getSelection()[0].getData();
					UI_CubeMDXViewer ui = new UI_CubeMDXViewer(shell, cube);
					ui.open();
				}
			});

			MenuItem cellSecurityMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
			try {
				if (cube.checkServerForCellSecurity()) {
					cellSecurityMenuItem.setText("Update Cell Security");
				} else {
					cellSecurityMenuItem.setText("Create Cell Security Cube");
				}
			} catch (TM1RestException | URISyntaxException | IOException ex){
				exception(ex);
			}

			if (!cube.checkServerForRules()) {
				MenuItem editRulesMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
				editRulesMenuItem.setText("Create Rules");
				editRulesMenuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						try {
							TM1Cube cube = (TM1Cube) explorerTree.getSelection()[0].getData();
							TM1Server tm1server = cube.getServer();
							cube.writeRulesToServer("#");
							updateCubeNode(explorerTree.getSelection()[0]);
						} catch (JSONException | TM1RestException | URISyntaxException | IOException ex) {
							exception(ex);
						}
					}
				});
			} else {
				MenuItem editRulesMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
				editRulesMenuItem.setText("Create Rules");
				editRulesMenuItem.setEnabled(false);
			}

			MenuItem ruleDependantCubeMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
			ruleDependantCubeMenuItem.setText("Rule Target Cubes");
			ruleDependantCubeMenuItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					ruleTargetCube();
				}
			});

			MenuItem ruleSourceCubeMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
			ruleSourceCubeMenuItem.setText("Rule Source Cubes");
			ruleSourceCubeMenuItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					ruleSourceCube();
				}
			});

			MenuItem deleteMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
			deleteMenuItem.setText("Delete");
			deleteMenuItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					if (deleteSelectedObjects() > 0) {
						TreeItem cubeListNode = explorerTree.getSelection()[0].getParentItem();
						updateCubeListNode(cubeListNode);
					}
				}
			});

			MenuItem exportMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
			exportMenuItem.setText("Export");
			exportMenuItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					exportQuickSelectedObjects();
				}
			});
		} catch (TM1RestException | URISyntaxException | IOException ex){
			exception(ex);
		}

	}

	private void showViewMenu(TreeItem selectedItem) {
		TM1View view = (TM1View) selectedItem.getData();

		MenuItem viewSecurityMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		if (view.security == PRIVATE) {
			viewSecurityMenuItem.setText("Set Public");
		} else if (view.security == PUBLIC) {
			viewSecurityMenuItem.setText("Set Private");
		}

		viewSecurityMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				try {
					TM1View view = (TM1View) explorerTree.getSelection()[0].getData();
					if (view.security == PRIVATE) {
						view.writeViewToServer(view.displayName, false);
						explorerTree.getSelection()[0].setImage(VIEWICON);
					} else if (view.security == PUBLIC) {
						view.writeViewToServer(view.displayName, true);
						explorerTree.getSelection()[0].setImage(PRIVVIEWICON);
					}
				} catch (JSONException | TM1RestException | URISyntaxException | IOException ex) {
					// TODO Auto-generated catch block
					exception(ex);
				}
			}
		});

		MenuItem editViewMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		editViewMenuItem.setText("Edit");
		editViewMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TM1View view = (TM1View) explorerTree.getSelection()[0].getData();
				TM1Cube cube = (TM1Cube) explorerTree.getSelection()[0].getParentItem().getParentItem().getData();
				openViewEditor(cube, view);
			}
		});

		MenuItem deleteMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		deleteMenuItem.setText("Delete");
		deleteMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (deleteSelectedObjects() > 0) {
					TreeItem cubeNode = explorerTree.getSelection()[0].getParentItem().getParentItem();
					updateCubeNode(cubeNode);
				}
			}
		});

		MenuItem exportMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		exportMenuItem.setText("Export");
		exportMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				exportQuickSelectedObjects();
			}
		});
	}

	private void showBlobsMenu() {
		MenuItem viewBlobMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		viewBlobMenuItem.setText("Create Blob");
		viewBlobMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				openBlobEditor(null);
			}
		});
	}

	private void showBlobMenu(TreeItem selectedItem) {
		MenuItem viewBlobMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		viewBlobMenuItem.setText("Edit");
		viewBlobMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TM1Blob blob = (TM1Blob) explorerTree.getSelection()[0].getData();
				openBlobEditor(blob);
			}
		});

		MenuItem deleteMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		deleteMenuItem.setText("Delete");
		deleteMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (deleteSelectedObjects() > 0) {
					updateBlobListNode();
				}
			}
		});
	}

	private void showHierarchyMenu(TreeItem selectedItem) {
		MenuItem editHierarchyMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		editHierarchyMenuItem.setText("Edit");
		editHierarchyMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TM1Hierarchy hierarchy = (TM1Hierarchy) explorerTree.getSelection()[0].getData();
				openHierarchyEditor(hierarchy);
			}
		});

		MenuItem duplicateHierarchyMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		duplicateHierarchyMenuItem.setText("Duplicate");
		duplicateHierarchyMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TM1Hierarchy hierarchy = (TM1Hierarchy) explorerTree.getSelection()[0].getData();

				UI_NamePrompt namePrompt = new UI_NamePrompt(shell, "Hierarchy Name", "");
				if (namePrompt.open()) {
					String newHierarchyName = namePrompt.getobjectname();
					TM1Dimension dimension = (TM1Dimension) hierarchy.getParent();
					TM1Server tm1server = dimension.getServer();
					try {
						if (!dimension.checkForHierarchy(newHierarchyName)) {
							hierarchy.saveHierarchyAs(newHierarchyName);
							TreeItem dimensionNode = getDimensionTreeItemParent(explorerTree.getSelection()[0]);
							updateDimensionNode(dimensionNode);
						} else {
							errorMessage(tm1server.getErrorMessage());
						}
					} catch (TM1RestException | URISyntaxException | IOException | JSONException ex){
						exception(ex);
					}
				}
			}
		});

		MenuItem deleteMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		deleteMenuItem.setText("Delete");
		deleteMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (deleteSelectedObjects() > 0) {
					TreeItem hierarchyNode = explorerTree.getSelection()[0].getParentItem();
					try {
						updateHierarchyListNode(hierarchyNode);
					} catch (TM1RestException | URISyntaxException | IOException | JSONException e) {
						// TODO Auto-generated catch block
						exception(e);
					}
				}
			}
		});

		MenuItem exportMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		exportMenuItem.setText("Export");
		exportMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				exportQuickSelectedObjects();
			}
		});
	}

	private void showDimensionMenu(TreeItem selectedItem) {

		MenuItem createHierarchyMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		createHierarchyMenuItem.setText("Create Hierarchy");
		createHierarchyMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TM1Dimension dimension = (TM1Dimension) explorerTree.getSelection()[0].getData();
				createNewHierarchy(dimension);
			}
		});
		
		MenuItem findParentCubesMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		findParentCubesMenuItem.setText("List Parent Cubes");
		findParentCubesMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				try {
					TM1Dimension dimension = (TM1Dimension) explorerTree.getSelection()[0].getData();
					List<String> parentCubes;
					parentCubes = dimension.findParentCubes();
					String message = "";
					for (int i = 0; i < parentCubes.size(); i++) {
						message = message.concat(parentCubes.get(i) + "\n");
					}
					infoMessage(message);
				} catch (TM1RestException | URISyntaxException | IOException | JSONException ex) {
					exception(ex);
				}
			}
		});

		MenuItem editElementAttributesMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		try {
			TM1Dimension dimension = (TM1Dimension) explorerTree.getSelection()[0].getData();
			if (!dimension.checkServerForElementAttributes()) {
				editElementAttributesMenuItem.setText("Create Element Attributes");
			} else {
				editElementAttributesMenuItem.setText("Edit Element Attributes");
			}
		} catch (TM1RestException | URISyntaxException | IOException ex){
			exception(ex);
		}

		MenuItem editElementSecurityMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		try {
			TM1Dimension dimension = (TM1Dimension) explorerTree.getSelection()[0].getData();
			if (!dimension.checkServerForElementSecurity()) {
				editElementSecurityMenuItem.setText("Create Element Security Cube");
			} else {
				editElementSecurityMenuItem.setText("Edit Element Security");
			}
		} catch (TM1RestException | URISyntaxException | IOException ex){
			exception(ex);
		}

		MenuItem deleteMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		deleteMenuItem.setText("Delete");
		deleteMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (deleteSelectedObjects() > 0) {
					TreeItem dimensionNode = explorerTree.getSelection()[0].getParentItem();
					updateDimensionListNode(dimensionNode);
				}
			}
		});

		MenuItem exportMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		exportMenuItem.setText("Export");
		exportMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				exportQuickSelectedObjects();
			}
		});
	}

	private void showSubsetMenu(TreeItem selectedItem) {
		TM1Subset subset = (TM1Subset) explorerTree.getSelection()[0].getData();

		MenuItem editSubsetMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		editSubsetMenuItem.setText("Edit");
		editSubsetMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TM1Subset subset = (TM1Subset) explorerTree.getSelection()[0].getData();
				openSubsetEditor(subset);
			}
		});

		MenuItem findParentViewsMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		findParentViewsMenuItem.setText("List Parent Views");
		findParentViewsMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				try {
					TM1Subset subset = (TM1Subset) explorerTree.getSelection()[0].getData();
					List<String> parentViews;
					parentViews = subset.findParentViews();
					String message = "";
					for (int i = 0; i < parentViews.size(); i++) {
						message = message.concat(parentViews.get(i) + "\n");
					}
					infoMessage(message);
				} catch (TM1RestException | URISyntaxException | IOException | JSONException ex) {
					exception(ex);
				}
			}
		});

		MenuItem viewSecurityMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		if (subset.security == PRIVATE) {
			viewSecurityMenuItem.setText("Set Public");
			viewSecurityMenuItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					// TODO
				}
			});
		} else if (subset.security == PUBLIC) {
			viewSecurityMenuItem.setText("Set Private");
			viewSecurityMenuItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					// TODO
				}
			});
		}

		MenuItem deleteMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		deleteMenuItem.setText("Delete");
		deleteMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (deleteSelectedObjects() > 0) {
					TreeItem hierarchyNode = explorerTree.getSelection()[0].getParentItem();
					try {
						updateHierarchyListNode(hierarchyNode);
					} catch (TM1RestException | URISyntaxException | IOException | JSONException e) {
						// TODO Auto-generated catch block
						exception(e);
					}
				}
			}
		});

		MenuItem exportMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		exportMenuItem.setText("Export");
		exportMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				exportQuickSelectedObjects();
			}
		});
	}

	private void showProcessMenu(TreeItem selectedItem) {
		TM1Process process = (TM1Process) explorerTree.getSelection()[0].getData();

		MenuItem editProcessMenuIteam = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		editProcessMenuIteam.setText("Edit");
		editProcessMenuIteam.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TM1Process process = (TM1Process) explorerTree.getSelection()[0].getData();
				openProcessEditor(process);
			}
		});

		MenuItem processSecurityMenuItem = new MenuItem(serverExplorerContextMenu, SWT.CHECK);
		processSecurityMenuItem.setText("Security Access");
		try {
			if (process.checkServerForSecurityAccess()) {
				processSecurityMenuItem.setSelection(true);
			} else {
				processSecurityMenuItem.setSelection(false);
			}
		} catch (TM1RestException | URISyntaxException | IOException | JSONException ex) {
			exception(ex);
		}
		processSecurityMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				try {
					TM1Process process = (TM1Process) explorerTree.getSelection()[0].getData();
					if (process.checkServerForSecurityAccess()) {
						process.writeSecurityAccessToServer(false);
					} else {
						process.writeSecurityAccessToServer(true);
					}
				} catch (TM1RestException | URISyntaxException | IOException | JSONException ex) {
					exception(ex);
				}
			}
		});

		MenuItem processExecuteMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		processExecuteMenuItem.setText("Execute");
		processExecuteMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TM1Process process = (TM1Process) explorerTree.getSelection()[0].getData();
				executeProcess(process);
			}
		});

		MenuItem processDuplicateMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		processDuplicateMenuItem.setText("Duplicate");
		processDuplicateMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TM1Process process = (TM1Process) explorerTree.getSelection()[0].getData();
				UI_NamePrompt nameprompt = new UI_NamePrompt(shell, "Process Name", "");
				if (nameprompt.open()) {
					String newprocessname = nameprompt.getobjectname();
					try {
						if (process.duplicate(newprocessname)) {
							infoMessage("Process duplicated");
							updateProcessListNode();
						} else {
							errorMessage(tm1server.getErrorMessage());
						}
					} catch (TM1RestException | URISyntaxException | IOException | JSONException ex){
						exception(ex);
					}
				}
			}
		});

		MenuItem processSecurityAdminMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		processSecurityAdminMenuItem.setText("Security Admin");
		processSecurityAdminMenuItem.setEnabled(false);

		MenuItem deleteMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		deleteMenuItem.setText("Delete");
		deleteMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (deleteSelectedObjects() > 0) {
					updateProcessListNode();
				}
			}
		});

		MenuItem exportMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		exportMenuItem.setText("Export");
		exportMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				exportQuickSelectedObjects();
			}
		});

		MenuItem findDependantMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		findDependantMenuItem.setText("Parent Processes");
		findDependantMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				searchParentProcesses();
			}
		});

	}

	private void showChoreMenu(TreeItem selectedItem) {
		TM1Chore chore = (TM1Chore) selectedItem.getData();

		MenuItem editProcessMenuIteam = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		editProcessMenuIteam.setText("Edit");
		editProcessMenuIteam.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TM1Chore chore = (TM1Chore) explorerTree.getSelection()[0].getData();
				openChoreEditor(chore);
			}
		});

		MenuItem activateChoreMenuIteam = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		activateChoreMenuIteam.setText("Activate");
		activateChoreMenuIteam.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TreeItem selectedItem = explorerTree.getSelection()[0];
				TM1Chore chore = (TM1Chore) selectedItem.getData();
				try {
					chore.activate();
					updateChoreListNode(selectedItem.getParentItem());
				} catch (TM1RestException | URISyntaxException | IOException ex) {
					// TODO Auto-generated catch block
					exception(ex);
				}
			}
		});
		if (chore.isActive()) {
			activateChoreMenuIteam.setEnabled(false);
		}

		MenuItem deactivateChoreMenuIteam = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		deactivateChoreMenuIteam.setText("Deactivate");
		deactivateChoreMenuIteam.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TreeItem selectedItem = explorerTree.getSelection()[0];
				TM1Chore chore = (TM1Chore) selectedItem.getData();
				try {
					chore.deactivate();
					updateChoreListNode(selectedItem.getParentItem());
				} catch (TM1RestException | URISyntaxException | IOException ex) {
					// TODO Auto-generated catch block
					exception(ex);
				}
			}
		});
		if (!chore.isActive()) {
			deactivateChoreMenuIteam.setEnabled(false);
		}

		MenuItem executeChoreMenuIteam = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		executeChoreMenuIteam.setText("Execute");
		executeChoreMenuIteam.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TM1Chore chore = (TM1Chore) explorerTree.getSelection()[0].getData();
				executeChore(chore);
			}
		});

		MenuItem exportMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		exportMenuItem.setText("Export");
		exportMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				exportQuickSelectedObjects();
			}
		});

		MenuItem deleteMenuItem = new MenuItem(serverExplorerContextMenu, SWT.NONE);
		deleteMenuItem.setText("Delete");
		deleteMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (deleteSelectedObjects() > 0) {
					TreeItem choreListNode = explorerTree.getSelection()[0].getParentItem();
					updateChoreListNode(choreListNode);
				}
			}
		});
	}

	/*private void expandNode(TreeItem node, int level) {
		node.setExpanded(true);
		if (level - 1 < 1)
			return;
		for (int i = 0; i < node.getItemCount(); i++) {
			TreeItem childNode = node.getItem(i);
			expandNode(childNode, level - 1);
		}
	}*/

	public TreeItem treeContains(Tree t, String s) {
		for (int i = 0; i < t.getItemCount(); i++) {
			TreeItem node = t.getItem(i);
			if (s.startsWith(node.getText())) {
				return treeContains(node, s);
			}
		}
		return null;
	}

	public TreeItem treeContains(TreeItem t, String s) {
		for (int i = 0; i < t.getItemCount(); i++) {
			String searchnodename = t.getItem(i).getText();
			String p = s.substring(s.lastIndexOf('.'), s.length());
			if (p.equals(searchnodename)) {
				return t.getItem(i);
			}
			if (s.startsWith(searchnodename)) {
				return treeContains(t.getItem(i), s);
			}
		}
		return t;
	}

	public void readMessageLogEntriesFromServer() {
		try {
			serverMessageLogTable.removeAll();
			String request = "MessageLogEntries/$count";
			String query = "";
			tm1server.get(request);
			int rowCount = Integer.parseInt(tm1server.response);
			int skip = rowCount - messageLogRowCount;
			if (skip < 0)
				skip = 0;
			request = "MessageLogEntries";
			query = "$skip=" + skip;
			tm1server.get(request, query);
			OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
			JSONArray ja = jresponse.getJSONArray("value");
			for (int i = ja.length() - 1; i > 0; i--) {
				String id = String.valueOf(ja.getJSONObject(i).getLong("ThreadID"));
				String timestamp = ja.getJSONObject(i).getString("TimeStamp");
				String logger = ja.getJSONObject(i).getString("Logger");
				String level = ja.getJSONObject(i).getString("Level");
				String message = ja.getJSONObject(i).getString("Message");
				TableItem tableItem = new TableItem(serverMessageLogTable, SWT.NONE);
				String[] tableItemText = { id, timestamp, logger, level, message };
				tableItem.setText(tableItemText);
			}
			for (TableColumn tc : serverMessageLogTable.getColumns())
				tc.pack();
			serverMessageLogTable.redraw();
			messageLogRowCountLabel.setText("Showing last " + messageLogRowCount + " out of " + rowCount + " lines");
		} catch (TM1RestException | URISyntaxException | IOException | JSONException ex) {
			exception(ex);
		}
	}

	public void downloadLogFile(String filename, int rows) {
		try {
			String request = "MessageLogEntries/$count";
			tm1server.get(request);
			int skiprows = 0;
			if (rows != 0) {
				int logrowcount = Integer.parseInt(tm1server.response.toString());
				skiprows = logrowcount - rows;
			}
			request = "MessageLogEntries";
			String query = "$orderby=TimeStamp asc&$skip=" + skiprows;
			tm1server.get(request, query);
			OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
			JSONArray jlines = jresponse.getJSONArray("value");
			FileWriter fw = new FileWriter(filename);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter pw = new PrintWriter(bw);
			for (int i = 0; i < jlines.length(); i++) {
				String id = String.valueOf(jlines.getJSONObject(i).getLong("ThreadID"));
				String timestamp = jlines.getJSONObject(i).getString("TimeStamp");
				String logger = jlines.getJSONObject(i).getString("Logger");
				String level = jlines.getJSONObject(i).getString("Level");
				String message = jlines.getJSONObject(i).getString("Message");
				pw.format("%-18s %-30s %-28s %-10s %-60s", id, timestamp, logger, level, message);
				pw.println();
			}
			pw.close();
			bw.close();
			fw.close();
			errorMessage("Log file download complete");

		} catch (TM1RestException | URISyntaxException | IOException | JSONException ex) {
			exception(ex);
		}
	}

	public void readThreadsFromServer() {
		try {
			String request = "Threads";
			tm1server.get(request);
			threadMonitorTable.setRedraw(false);
			OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
			JSONArray threadJSONArray = jresponse.getJSONArray("value");
			threadMonitorTable.removeAll();
			if (logThreadsToFile) {
				File dir = new File(".//logs//" + tm1server.getAdminHostName() + "//" + tm1server.getName());
				if (!dir.exists())
					dir.mkdirs();
				File file = new File(".//logs//" + tm1server.getAdminHostName() + "//" + tm1server.getName() + "//threads.log");
				// System.out.println(file.toString());
				if (!file.exists())
					file.createNewFile();
				FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter pw = new PrintWriter(bw);
				pw.println("Thread, Type, Name, Content, State, Function, ObjectType, ObjectName, rLocks, ixLocks, wLocks, Wait, Elapsed");
				for (int i = 0; i < threadJSONArray.length(); i++) {
					OrderedJSONObject line = (OrderedJSONObject) threadJSONArray.get(i);
					String threadId = "";
					String type = "";
					String name = "";
					String context = "";
					String state = "";
					String function = "";
					String objectType = "";
					String objectName = "";
					String rLocks = "";
					String ixLocks = "";
					String wLocks = "";
					String wait = "";
					String elapsed = "";

					if (line.has("ID"))
						threadId = String.valueOf(line.getLong("ID"));
					if (line.has("ThreadID"))
						threadId = String.valueOf(line.getLong("ThreadID"));
					if (line.has("Type"))
						type = line.getString("Type");
					if (line.has("Name"))
						name = line.getString("Name");
					if (line.has("Context"))
						context = line.getString("Context");
					if (line.has("State"))
						state = line.getString("State");
					if (line.has("Function"))
						function = line.getString("Function");
					if (line.has("ObjectType"))
						objectType = line.getString("ObjectType");
					if (line.has("ObjectName"))
						objectName = line.getString("ObjectName");
					if (line.has("RLocks"))
						rLocks = Integer.toString(line.getInt("RLocks"));
					if (line.has("IXLocks"))
						ixLocks = Integer.toString(line.getInt("IXLocks"));
					if (line.has("WLocks"))
						wLocks = Integer.toString(line.getInt("WLocks"));
					if (line.has("ElapsedTime"))
						elapsed = String.valueOf(countSeconds(line.getString("ElapsedTime")));
					if (line.has("WaitTime"))
						wait = String.valueOf(countSeconds(line.getString("WaitTime")));

					TableItem tableItem = new TableItem(threadMonitorTable, 1);
					tableItem.setText(0, threadId);
					tableItem.setText(1, type);
					tableItem.setText(2, name);
					tableItem.setText(3, context);
					tableItem.setText(4, state);
					tableItem.setText(5, function);
					tableItem.setText(6, objectType);
					tableItem.setText(7, objectName);
					tableItem.setText(8, rLocks);
					tableItem.setText(9, ixLocks);
					tableItem.setText(10, wLocks);
					tableItem.setText(11, elapsed);
					tableItem.setText(12, wait);
					pw.println(threadId + ", " + type + ", " + name + ", " + context + ", " + state + ", " + function + ", " + objectType + ", " + objectName + ", " + rLocks + ", " + ixLocks + ", " + wLocks + ", " + elapsed + ", " + wait);

				}
				pw.close();
				bw.close();
				fw.close();
			} else {
				for (int i = 0; i < threadJSONArray.length(); i++) {
					OrderedJSONObject line = (OrderedJSONObject) threadJSONArray.get(i);
					String threadId = "";
					String type = "";
					String name = "";
					String context = "";
					String state = "";
					String function = "";
					String objectType = "";
					String objectName = "";
					String rLocks = "";
					String ixLocks = "";
					String wLocks = "";
					String wait = "";
					String elapsed = "";

					if (line.has("ID"))
						threadId = String.valueOf(line.getLong("ID"));
					if (line.has("ThreadID"))
						threadId = String.valueOf(line.getLong("ThreadID"));
					if (line.has("Type"))
						type = line.getString("Type");
					if (line.has("Name"))
						name = line.getString("Name");
					if (line.has("Context"))
						context = line.getString("Context");
					if (line.has("State"))
						state = line.getString("State");
					if (line.has("Function"))
						function = line.getString("Function");
					if (line.has("ObjectType"))
						objectType = line.getString("ObjectType");
					if (line.has("ObjectName"))
						objectName = line.getString("ObjectName");
					if (line.has("RLocks"))
						rLocks = Integer.toString(line.getInt("RLocks"));
					if (line.has("IXLocks"))
						ixLocks = Integer.toString(line.getInt("IXLocks"));
					if (line.has("WLocks"))
						wLocks = Integer.toString(line.getInt("WLocks"));
					if (line.has("ElapsedTime"))
						elapsed = String.valueOf(countSeconds(line.getString("ElapsedTime")));
					if (line.has("WaitTime"))
						wait = String.valueOf(countSeconds(line.getString("WaitTime")));

					if (state.equals("Idle") && threadMonitorFilterIdleCheckButton.getSelection()) {

					} else {
						TableItem tableItem = new TableItem(threadMonitorTable, 1);
						tableItem.setText(0, threadId);
						tableItem.setText(1, type);
						tableItem.setText(2, name);
						tableItem.setText(3, context);
						tableItem.setText(4, state);
						tableItem.setText(5, function);
						tableItem.setText(6, objectType);
						tableItem.setText(7, objectName);
						tableItem.setText(8, rLocks);
						tableItem.setText(9, ixLocks);
						tableItem.setText(10, wLocks);
						tableItem.setText(11, elapsed);
						tableItem.setText(12, wait);
					}
				}
			}
			for (TableColumn tc : threadMonitorTable.getColumns())
				tc.pack();
			threadMonitorTable.setRedraw(true);
		} catch (TM1RestException | JSONException | IOException | URISyntaxException ex) {
			exception(ex);
		}
	}

	public boolean cancelthread(long threadid) {
		try {
			String request = "Threads(" + threadid + ")/tm1.CancelOperation";
			OrderedJSONObject payload = new OrderedJSONObject();
			tm1server.post(request, payload);
			return true;
		} catch (TM1RestException | URISyntaxException | IOException ex) {
			exception(ex);
			return false;
		}
	}

	public int countSeconds(String s) {
		int secs = Integer.parseInt(s.substring(10, 12));
		int mins = Integer.parseInt(s.substring(7, 9));
		int hours = Integer.parseInt(s.substring(4, 6));
		int days = Integer.parseInt(s.substring(1, 2));
		return (days * 86400) + (hours * 3600) + (mins * 60) + secs;
	}

	public void stopKeepAliveTimer() {
		display.timerExec(-1, keepAliveTimer);
	}

	public Button getFilterIdleCheckButton() {
		return threadMonitorFilterIdleCheckButton;
	}

	public void openLoggingPane() {
		serverMessageTabIsOpen = true;
		loggingSashForm.setWeights(loggingSashWeight);
	}

	public void closeLoggingPane() {
		serverMessageTabIsOpen = false;
		loggingSashForm.setWeights(new int[] { 1, 0 });
	}

	public void openModelConfigurationPane() {
		modelConfigurationTabIsOpen = true;
		modelConfigurationSashForm.setWeights(modelConfigurationSashWeight);
	}

	public void closeModelConfigurationPane() {
		modelConfigurationTabIsOpen = false;
		modelConfigurationSashForm.setWeights(new int[] { 1, 0 });
	}

	protected Label getMessageLogRowCountLabel() {
		return messageLogRowCountLabel;
	}

	public void exportTM1Objects() {
		ExportImportEditor exportDialog = new ExportImportEditor(shell, tm1server);
		try {
			if (exportDialog.open()) {
				// refresh ?
			}
		} catch (TM1RestException ex) {
			exception(ex);
		}
	}

	public int deleteSelectedObjects() {
		try {
			TreeItem[] selectedItems = explorerTree.getSelection();
			List<TM1Server> tm1servers = new ArrayList<TM1Server>();
			List<TM1Subset> deleleSubsets = new ArrayList<TM1Subset>();
			List<TM1View> deleteViews = new ArrayList<TM1View>();
			List<TM1Dimension> deleteDimensions = new ArrayList<TM1Dimension>();
			List<TM1Hierarchy> deleteHierachies = new ArrayList<TM1Hierarchy>();
			List<TM1Cube> deleteCubes = new ArrayList<TM1Cube>();
			List<TM1Process> deleteProcesses = new ArrayList<TM1Process>();
			List<TM1Chore> deleteChores = new ArrayList<TM1Chore>();
			List<TM1Blob> deleteBlobs = new ArrayList<TM1Blob>();

			for (int i = 0; i < selectedItems.length; i++) {
				Object o = selectedItems[i].getData();
				if (o instanceof TM1Object) {
					TM1Object tm1o = (TM1Object) o;
					TM1Server s = tm1o.getServer();
					tm1servers.add(s);
				}

				if (o instanceof TM1Subset) {
					TM1Subset subset = (TM1Subset) o;
					deleleSubsets.add(subset);
				} else if (o instanceof TM1View) {
					TM1View view = (TM1View) o;
					deleteViews.add(view);
				} else if (o instanceof TM1Cube) {
					TM1Cube cube = (TM1Cube) o;
					deleteCubes.add(cube);
				} else if (o instanceof TM1Dimension) {
					TM1Dimension dimension = (TM1Dimension) o;
					deleteDimensions.add(dimension);
				} else if (o instanceof TM1Hierarchy) {
					TM1Hierarchy hierarchy = (TM1Hierarchy) o;
					deleteHierachies.add(hierarchy);
				} else if (o instanceof TM1Process) {
					TM1Process process = (TM1Process) o;
					deleteProcesses.add(process);
				} else if (o instanceof TM1Chore) {
					TM1Chore chore = (TM1Chore) o;
					deleteChores.add(chore);
				} else if (o instanceof TM1Blob) {
					TM1Blob blob = (TM1Blob) o;
					deleteBlobs.add(blob);
				}
			}

			int deleteCount = 0;
			int failedCount = 0;
			String noDeleteList = "";
			for (int i = 0; i < deleleSubsets.size(); i++) {
				TM1Subset subset = deleleSubsets.get(i);
				if (subset.remove()) {
					deleteCount++;
				} else {
					TM1Server tm1server = subset.getServer();
					String error = tm1server.getErrorMessage();
					noDeleteList = noDeleteList.concat("Subset: " + subset.displayName + "(" + error + ")\n");
					failedCount++;
				}
			}

			for (int i = 0; i < deleteHierachies.size(); i++) {
				TM1Hierarchy hierarchy = deleteHierachies.get(i);
				hierarchy.remove();
				deleteCount++;

				//TM1Server tm1server = hierarchy.getServer();
				//String error = tm1server.getErrorMessage();
				//noDeleteList = noDeleteList.concat("Hierarchy: " + hierarchy.displayName + "(" + error + ")\n");
				//failedCount++;

			}
			for (int i = 0; i < deleteDimensions.size(); i++) {
				TM1Dimension dimension = deleteDimensions.get(i);
				dimension.remove();
				deleteCount++;

				//TM1Server tm1server = dimension.getServer();
				//String error = tm1server.getErrorMessage();
				//noDeleteList = noDeleteList.concat("Dimension: " + dimension.displayName + "(" + error + ")\n");
				//failedCount++;

			}
			for (int i = 0; i < deleteViews.size(); i++) {
				TM1View view = deleteViews.get(i);
				view.remove();
				deleteCount++;

				//TM1Server tm1server = view.getServer();
				//String error = tm1server.getErrorMessage();
				//noDeleteList = noDeleteList.concat("View: " + view.displayName + "(" + error + ")\n");
				//failedCount++;

			}
			for (int i = 0; i < deleteCubes.size(); i++) {
				TM1Cube cube = deleteCubes.get(i);
				cube.remove();
				deleteCount++;

				//TM1Server tm1server = cube.getServer();
				//String error = tm1server.getErrorMessage();
				//noDeleteList = noDeleteList.concat("Cube: " + cube.displayName + "(" + error + ")\n");
				//failedCount++;

			}
			for (int i = 0; i < deleteProcesses.size(); i++) {
				TM1Process process = deleteProcesses.get(i);
				process.remove();
				deleteCount++;

				//TM1Server tm1server = process.getServer();
				//String error = tm1server.getErrorMessage();
				//noDeleteList = noDeleteList.concat("Process: " + process.displayName + "(" + error + ")\n");
				//failedCount++;

			}
			for (int i = 0; i < deleteChores.size(); i++) {
				TM1Chore chore = deleteChores.get(i);
				chore.remove();
				deleteCount++;

				//TM1Server tm1server = chore.getServer();
				//String error = tm1server.getErrorMessage();
				//noDeleteList = noDeleteList.concat("Chore: " + chore.displayName + "(" + error + ")\n");
				//failedCount++;

			}

			for (int i = 0; i < deleteBlobs.size(); i++) {
				TM1Blob blob = deleteBlobs.get(i);
				blob.remove();
				deleteCount++;

				//TM1Server tm1server = blob.getServer();
				//String error = tm1server.getErrorMessage();
				//noDeleteList = noDeleteList.concat("Blob: " + blob.displayName + "\n(" + error + ")\n");
				//failedCount++;
			}

			if (noDeleteList.equals("")) {
				infoMessage("Deleted " + deleteCount + " objects");
			} else {
				String errMessage = "";
				errMessage = errMessage.concat("Deleted " + deleteCount + " objects\n\n");
				errMessage = errMessage.concat("Failed to delete " + failedCount + " objects\n");
				errMessage = errMessage.concat(noDeleteList);
				errorMessage(errMessage);
			}
			return deleteCount;
		} catch (Exception ex){
			exception(ex);
			return 0;
		}

	}

	public void exportQuickSelectedObjects() {
		System.out.println("Function -> exportQuickSelectedObjects");
		try {
			TreeItem[] selectedItems = explorerTree.getSelection();
			List<TM1Object> tm1objects = new ArrayList<TM1Object>();

			for (int i = 0; i < selectedItems.length; i++) {
				Object o = selectedItems[i].getData();
				if (o instanceof TM1Object) {
					tm1objects.add((TM1Object) o);
				}
			}

			DirectoryDialog directorydialog = new DirectoryDialog(shell, SWT.OPEN);
			directorydialog.open();
			String exportBaseDirectory = directorydialog.getFilterPath();

			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			String datetimeString = sdf.format(date);

			String exportTempDirectoryString = exportBaseDirectory + "//export_" + datetimeString;
			File exportTempDirectory = new File(exportTempDirectoryString);
			if (!exportTempDirectory.exists()) {
				exportTempDirectory.mkdirs();
			}
			
			int exportOkCount = 0;
			int exportFailedCount = 0;

			for (int i = 0; i < tm1objects.size(); i++) {
				TM1Object exportObject = (TM1Object) tm1objects.get(i);
				if (exportObject instanceof TM1Dimension) {
					TM1Dimension exportDimension = (TM1Dimension)exportObject;
					if (exportDimension.writeDimensionToFile(exportTempDirectory.toString())) {
						exportOkCount++;
					} else {
						exportFailedCount++;
					}
				}
				if (exportObject instanceof TM1Cube) {
					TM1Cube exportcube = (TM1Cube)exportObject;
					if (exportcube.writeCubeToFile(exportTempDirectory.toString())) {
						exportOkCount++;
					} else {
						exportFailedCount++;
					}
				}
				if (exportObject instanceof TM1Chore) {
					TM1Chore exportChore = (TM1Chore)exportObject;
					if (exportChore.writeChoreToFile(exportTempDirectory.toString())) {
						exportOkCount++;
					} else {
						exportFailedCount++;
					}
				}
				if (exportObject instanceof TM1Process) {
					TM1Process exportProcess = (TM1Process)exportObject;
					if (exportProcess.writeProcessToFile(exportTempDirectory.toString())) {
						exportOkCount++;
					} else {
						exportFailedCount++;
					}
				}

			}
			
			FileOutputStream zipWriter = new FileOutputStream(exportTempDirectoryString + ".zip");
			ZipOutputStream zip = new ZipOutputStream(zipWriter);
			String dimensionTempExportDirString = exportTempDirectory.getAbsolutePath() + "//dim";
			File dimensionTempExportDir = new File(dimensionTempExportDirString);
			String cubeTempExportDirString = exportTempDirectory.getAbsolutePath() + "//cub";
			File cubeTempExportDir = new File(cubeTempExportDirString);
			String processTempExportDirString = exportTempDirectory.getAbsolutePath() + "//pro";
			File processTempExportDir = new File(processTempExportDirString);
			String choreTempExportDirString = exportTempDirectory.getAbsolutePath() + "//cho";
			File choreTempExportDir = new File(choreTempExportDirString);
			
			if (dimensionTempExportDir.exists()){
				ZipUtils.addFolderToZip("", dimensionTempExportDirString, zip);
			}
			
			if (cubeTempExportDir.exists()){
				ZipUtils.addFolderToZip("", cubeTempExportDirString, zip);
			}
			
			if (processTempExportDir.exists()){
				ZipUtils.addFolderToZip("", processTempExportDirString, zip);
			}
			
			if (choreTempExportDir.exists()){
				ZipUtils.addFolderToZip("", choreTempExportDirString, zip);
			}
			
			zip.close();
			zipWriter.close();
			recursiveDelete(exportTempDirectory);
			
			
			if (exportFailedCount == 0) {
				infoMessage("Export " + exportOkCount + " objects");
			} else {
				String errMessage = "";
				errMessage = errMessage.concat("Exported " + exportOkCount + " objects\n\n");
				errMessage = errMessage.concat("Failed to export " + exportFailedCount + " objects\n");
				errorMessage(errMessage);
			}

		} catch (Exception ex) {
			exception(ex);
		}
	}

	public void disconnectServer()  {
		//display.timerExec(-1, threadMonitorTimer);
		//display.timerExec(-1, messageLogRefreshTimer);
		try {
			tm1server.unauthenticate();
			this.dispose();
		} catch (URISyntaxException | IOException | JSONException | TM1RestException ex) {
			if (ex instanceof TM1RestException){
				MessageBox m = new MessageBox(shell, SWT.ERROR);
				m.setMessage("Failed to Disconnect");
				m.setText(ex.toString());
				m.open();
			}
		}
	}

	public void openTransactionQueryViewer() {
		TransactionLogViewer transactionlog_dialog = new TransactionLogViewer(shell, tm1server);
		transactionlog_dialog.open();
	}
	
	public void openSandboxViewer() {
		SandboxViewer sandboxviewer = new SandboxViewer(shell, tm1server);
		sandboxviewer.open();
	}

	private void ruleTargetCube() {
		try {
			TM1Cube cube = (TM1Cube) explorerTree.getSelection()[0].getData();
			List<SearchResult> r = cube.getRuleTargetCubesList();
			if (r.size() == 0) {
				infoMessage("No results found");
			} else {
				searchResult.open(r);
			}
		} catch (TM1RestException ex){
			exception(ex);
		}
	}

	private void ruleSourceCube() {
		try {
			TM1Cube cube = (TM1Cube) explorerTree.getSelection()[0].getData();
			List<SearchResult> r = cube.getRuleSourceCubesList();
			if (r.size() == 0) {
				infoMessage("No results found");
			} else {
				searchResult.open(r);
			}
		} catch (TM1RestException | URISyntaxException | IOException | JSONException ex){
			exception(ex);
		}
	}

	private void searchParentProcesses() {
		try {
			TM1Process process = (TM1Process) explorerTree.getSelection()[0].getData();
			List<SearchResult> r;
			r = process.findParentProcesses();
			if (r.size() == 0) {
				infoMessage("No results found");
			} else {
				try {
					searchResult.open(r);
				} catch (TM1RestException ex){
					exception(ex);
				}
			}
		} catch (TM1RestException | URISyntaxException | IOException | JSONException ex) {
			exception(ex);
		}

	}

	public void goTo(TM1Object tm1object) throws TM1RestException, ClientProtocolException, URISyntaxException, IOException, JSONException {
		if (tm1object instanceof TM1Cube || tm1object instanceof TM1Dimension || tm1object instanceof TM1Process || tm1object instanceof TM1Chore) {
			if (tm1object.displayName.startsWith("}")) {
				if (!showControlObjects)
					showControlObjects = true;
				updateTM1ServerNode();
			}
		} else if (tm1object instanceof TM1View || tm1object instanceof TM1Hierarchy) {
			if (tm1object.displayName.startsWith("}") || tm1object.getParent().displayName.startsWith("}")) {
				if (!showControlObjects)
					showControlObjects = true;
				updateTM1ServerNode();
			}
		} else if (tm1object instanceof TM1Subset) {
			if (tm1object.displayName.startsWith("}") || tm1object.getParent().displayName.startsWith("}") || tm1object.getParent().getParent().displayName.startsWith("}")) {
				if (!showControlObjects)
					showControlObjects = true;
				updateTM1ServerNode();
			}
		}

		String objectName = tm1object.displayName;
		if (tm1object instanceof TM1Cube) {
			//TM1Cube cube = (TM1Cube) tm1object;

			for (int i = 0; i < cubeListNode.getItemCount(); i++) {
				TreeItem t = cubeListNode.getItem(i);
				if (t.getText().equals(objectName)) {
					updateCubeNode(t);
					t.setExpanded(true);
					explorerTree.setSelection(t);
					break;
				}
			}
		} else if (tm1object instanceof TM1View) {
			TM1View view = (TM1View) tm1object;
			TM1Cube cube = (TM1Cube) view.getParent();

			for (int i = 0; i < cubeListNode.getItemCount(); i++) {
				TreeItem t = cubeListNode.getItem(i);
				if (t.getText().equals(cube.displayName)) {
					updateCubeNode(t);
					t.setExpanded(true);
					TreeItem viewlist = t.getItem(0);
					for (int j = 0; j < viewlist.getItemCount(); j++) {
						TreeItem u = viewlist.getItem(j);
						if (u.getText().equals(view.displayName)) {
							explorerTree.setSelection(u);
							break;
						}
					}
					break;
				}
			}
		} else if (tm1object instanceof TM1Dimension) {
			//TM1Dimension dimension = (TM1Dimension) tm1object;

			for (int i = 0; i < dimensionListNode.getItemCount(); i++) {
				TreeItem t = dimensionListNode.getItem(i);
				if (t.getText().equals(objectName)) {
					updateDimensionNode(t);
					t.setExpanded(true);
					explorerTree.setSelection(t);
					break;
				}
			}
		} else if (tm1object instanceof TM1Hierarchy) {
			TM1Hierarchy hierarchy = (TM1Hierarchy) tm1object;
			//TM1Dimension dimension = (TM1Dimension) hierarchy.getParent();

			for (int i = 0; i < dimensionListNode.getItemCount(); i++) {
				TreeItem t = dimensionListNode.getItem(i);
				if (t.getText().equals(objectName)) {
					updateDimensionNode(t);
					t.setExpanded(true);
					TreeItem hierarchyList = t.getItem(0);
					for (int j = 0; j < hierarchyList.getItemCount(); j++) {
						TreeItem u = hierarchyList.getItem(j);
						if (u.getText().equals(objectName)) {
							explorerTree.setSelection(u);
							break;
						}
					}
					break;
				}
			}
		} else if (tm1object instanceof TM1Subset) {
			TM1Subset subset = (TM1Subset) tm1object;
			TM1Hierarchy hierarchy = (TM1Hierarchy) subset.getParent();
			TM1Dimension dimension = (TM1Dimension) hierarchy.getParent();

			for (int i = 0; i < dimensionListNode.getItemCount(); i++) {
				TreeItem t = dimensionListNode.getItem(i);
				if (t.getText().equals(dimension.displayName)) {
					updateDimensionNode(t);
					t.setExpanded(true);
					TreeItem hierarchyList = t.getItem(0);
					for (int j = 0; j < hierarchyList.getItemCount(); j++) {
						TreeItem u = hierarchyList.getItem(j);
						if (u.getText().equals(hierarchy.displayName)) {
							u.setExpanded(true);
							TreeItem subsetList = u.getItem(0);
							for (int k = 0; k < subsetList.getItemCount(); k++) {
								TreeItem v = subsetList.getItem(k);
								if (v.getText().equals(subset.displayName)) {
									explorerTree.setSelection(v);
									break;
								}
							}
							break;
						}
					}
					break;
				}
			}
		} else if (tm1object instanceof TM1Process) {
			for (int i = 0; i < processListNode.getItemCount(); i++) {
				TreeItem t = processListNode.getItem(i);
				if (t.getText().equals(objectName)) {
					explorerTree.setSelection(t);
					break;
				}
			}
		} else if (tm1object instanceof TM1Chore) {
			for (int i = 0; i < choreListNode.getItemCount(); i++) {
				TreeItem t = choreListNode.getItem(i);
				if (t.getText().equals(objectName)) {
					explorerTree.setSelection(t);
					break;
				}
			}

		} 
	}

	public void openSearch() {
		try {
			searchResult.open(null);
		} catch (TM1RestException ex) {
			exception(ex);
		}
	}

	public void goToNext() {

	}

	public void gotoPrevious() {

	}

	private void openProcessEditor(TM1Process process) {
		try {
			ProcessEditor editor = new ProcessEditor(this.getShell(), process);
		} catch (TM1RestException ex){
			exception(ex);
		}
	}

	private void openProcessEditor(TM1Server tm1server) {
		try {
			ProcessEditor editor = new ProcessEditor(this.getShell(), tm1server);
		} catch (TM1RestException ex){
			exception(ex);
		}
	}
	
	private void openExportImportEditor(){
		try {
			ExportImportEditor exportImportDialog = new ExportImportEditor(shell, tm1server);
			exportImportDialog.open();
		} catch (TM1RestException ex){
			exception(ex);
		}
	}

	private void openChoreEditor(TM1Chore chore) {
		ChoreEditor editor = new ChoreEditor(this.getShell(), chore);
	}

	private void openChoreEditor() {
		ChoreEditor editor = new ChoreEditor(this.getShell(), tm1server);
	}

	private void openBlobEditor(TM1Blob blob) {
		try {
			BlobEditor editor = new BlobEditor(this.getShell(), blob);
		} catch (TM1RestException | URISyntaxException | IOException ex){
			exception(ex);
		}
	}

	private void openBlobEditor() {
		try {
			BlobEditor editor = new BlobEditor(this.getShell(), tm1server);
		} catch (TM1RestException | URISyntaxException | IOException ex){
			exception(ex);
		}
	}


	private void openViewEditor(TM1Cube cube, TM1View view) {
		try {
			ViewEditor editor = new ViewEditor(this.getShell(), cube, view);
		} catch (TM1RestException | URISyntaxException | IOException | JSONException ex) {
			// TODO Auto-generated catch block
			exception(ex);
		}
	}

	private void openViewEditor(TM1Cube cube) {
		try {
			ViewEditor editor = new ViewEditor(this.getShell(), cube);
		} catch (TM1RestException | URISyntaxException | IOException | JSONException ex){
			exception(ex);
		}
	}

	private void openRuleEditor(TM1Cube cube) {
		try {
			RuleEditor editor = new RuleEditor(this.getShell(), cube);
		} catch (TM1RestException ex){
			exception(ex);
		}
	}
	
	private void createNewDimension(){
		NewDimension newDimensionPrompt = new NewDimension(this.getShell(), tm1server);
		if (newDimensionPrompt.open()){
			
		}
	}

	private void createNewHierarchy(TM1Dimension dimension){
		NewHierarchy newHierarchyPrompt = new NewHierarchy(this.getShell(), dimension);
		if (newHierarchyPrompt.open()){
			HierarchyEditor editor = new HierarchyEditor(this.getShell(), newHierarchyPrompt.getCreatedHierarchy());
		}
	}
	
	private void openHierarchyEditor(TM1Hierarchy hierarchy)  {
		HierarchyEditor editor = new HierarchyEditor(this.getShell(), hierarchy);
	}

	private void openHierarchyEditor(TM1Dimension dimension)  {
		HierarchyEditor editor = new HierarchyEditor(this.getShell(), dimension);
	}
	
	private void openSubsetEditor(TM1Subset subset)  {
		try {
			SubsetEditor editor = new SubsetEditor(this.getShell(), subset);
			editor.open();
		} catch (Exception ex){
			exception(ex);
		}
	}

	private void openSubsetEditor(TM1Hierarchy hierarchy) {
		try {
			SubsetEditor editor = new SubsetEditor(shell, hierarchy);
			editor.open();
		} catch (Exception ex){
			exception(ex);
		}
	}

	private void toggleControlObjects()  {
		showControlObjects = !showControlObjects;
		updateTM1ServerNode();

	}

	private void startKeepAlive()  {
		keepAliveTimer = new Runnable() {
			public void run() {
				display.timerExec(keepAliveInterval * 1000, this);
				try {
					tm1server.keepAlive();
				} catch (TM1RestException | URISyntaxException | IOException ex) {
					// TODO Auto-generated catch block
					exception(ex);
				}
			}
		};
		display.timerExec(keepAliveInterval * 1000, keepAliveTimer);
	}

	private void stopKeepAlive(){
		display.timerExec(-1, keepAliveTimer);
	}

	private void openLoggerConfigWindow()  {
		try {
			LoggingConfiguration loggerConfigWindow = new LoggingConfiguration(shell, tm1server);
		} catch (TM1RestException ex) {
			// TODO Auto-generated catch block
			exception(ex);
		}
	}

	private void openModelConfigWindow() {
		ServerConfiguration modelConfigWindow = new ServerConfiguration(shell, tm1server);
	}

	private void openCellSecurityReport() {
		SecurityReport securityReportWindow = new SecurityReport(shell, tm1server);
	}

	private void exception(Exception ex){
		ex.printStackTrace();
		if (ex instanceof TM1RestException){
			if (((TM1RestException)ex).getResponse() == 401){
				MessageBox m = new MessageBox(shell, SWT.OK | SWT.CANCEL);
				m.setText("Disconnected from " + tm1server.getName());
				m.setMessage("Click OK to Reconnect");
				int rc = m.open();
				if (rc == SWT.OK){
					this.reconnect();
				} else {
					this.dispose();
				}
			}
		} else {
			MessageBox m = new MessageBox(shell, SWT.ERROR);
			m.setText("Error");
			m.setMessage(ex.toString());
			m.open();
		}
	}
	
	public void setWrarchitectMainWindow(Wrarchitect wrarchitectMainWindow){
		this.wrarchitectMainWindow = wrarchitectMainWindow; 
	}
	
	public void recursiveDelete(File file) {
        //to end the recursive loop
        if (!file.exists()) return;

        //if directory, go inside and call recursively
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                //call recursively
                recursiveDelete(f);
            }
        }
        //call delete to delete files and empty directory
        
        try {
            Files.delete(file.toPath());
        } catch (NoSuchFileException x) {
            System.err.format("%s: no such" + " file or directory%n", file.toPath());
        } catch (DirectoryNotEmptyException x) {
            System.err.format("%s not empty%n", file.toPath());
        } catch (IOException x) {
            // File permission problems are caught here.
            System.err.println(x);
        }
        
        /*
        if (file.delete()){
            System.out.println("Deleted file/folder: "+file.getAbsolutePath());
        } else{
            System.out.println("Failed to delete file/folder: "+file.getAbsolutePath());
        }
        */
    }

}
