package TM1Diagnostic.UI;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

import org.apache.wink.json4j.JSONException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import TM1Diagnostic.Credential;
import TM1Diagnostic.REST.TM1Admin;
import TM1Diagnostic.REST.TM1RestException;
import TM1Diagnostic.REST.TM1Server;
import TM1Diagnostic.REST.TM1ServerStub;

public class ServerConnectorComposite extends Composite {

	private static Image ADMINICON;
	private static Image CONNECTEDICON;
	private static Image DISCONNECTEDICON;

	private Tree connectionsTree;
	private Display display;
	protected Shell shell;

	public boolean HTTPDEBUG;

	private TabFolder folders;
	// private TabItem activeTab;
	// private TabItem connectorTab;

	private Wrarchitect wrarchitectMainWindow;

	private List<CTabItem> tm1ServerTabs;
	private List<TM1Admin> adminservers;

	public ServerConnectorComposite(Composite parent) {
		super(parent, SWT.EMBEDDED);
		setLayout(new GridLayout(1, false));

		display = parent.getDisplay();
		shell = this.getShell();
		adminservers = new ArrayList<TM1Admin>();

		ADMINICON = new Image(display, ".\\images\\icon_admin.gif");
		CONNECTEDICON = new Image(display, ".\\images\\icon_connected.gif");
		DISCONNECTEDICON = new Image(display, ".\\images\\icon_disconnected.gif");

		Composite treeComposite = new Composite(this, SWT.NONE);
		treeComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		treeComposite.setLayout(new GridLayout(1, false));

		connectionsTree = new Tree(treeComposite, SWT.BORDER | SWT.MULTI);
		connectionsTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		connectionsTree.setBounds(0, 0, 89, 89);

		final Menu treemenu = new Menu(connectionsTree);
		connectionsTree.setMenu(treemenu);

		connectionsTree.addListener(SWT.MouseDoubleClick, new Listener() {
			public void handleEvent(Event event) {
				if (connectionsTree.getSelectionCount() == 0) {
					// Nothing is selected so do nothing
				} else if (connectionsTree.getSelection()[0].getData() instanceof TM1ServerStub) {
					TM1ServerStub tm1ServerStub = (TM1ServerStub) connectionsTree.getSelection()[0].getData();
					if (!tm1ServerStub.isAuthenticated) {
						connectToTM1ServerAs();
					}
				}
			}
		});

		connectionsTree.addTreeListener(new TreeListener() {
			public void treeExpanded(TreeEvent event) {
				TreeItem node = (TreeItem) event.item;
				if (node.getData() instanceof TM1Admin) {
					TM1Admin admin = (TM1Admin) node.getData();
					admin.expand();
					// System.out.println("Expanded admin node");
				}
			}

			public void treeCollapsed(TreeEvent event) {
				TreeItem node = (TreeItem) event.item;
				if (node.getData() instanceof TM1Admin) {
					TM1Admin admin = (TM1Admin) node.getData();
					admin.collapse();
					// System.out.println("Collapsed admin node");
				}
			}
		});

		treemenu.addMenuListener(new MenuAdapter() {
			public void menuShown(MenuEvent e) {
				MenuItem[] items = treemenu.getItems();
				for (int i = 0; i < items.length; i++) {
					items[i].dispose();
				}
				// Tree menu items for TM1 Admin Host
				if (connectionsTree.getSelectionCount() == 0) {
					// Nothing is selected so do nothing
				} else if (connectionsTree.getSelection()[0].getData() instanceof TM1Admin) {
					MenuItem adminRefreshMenuItem = new MenuItem(treemenu, SWT.NONE);
					adminRefreshMenuItem.setText("Refresh");
					adminRefreshMenuItem.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent event) {
							// TM1Admin adminserver = (TM1Admin) mainTree.getSelection()[0].getData();
							refreshTM1AdminHostTree(connectionsTree.getSelection()[0]);
						}
					});

					// Tree menu items for TM1 Servers
				} else if (connectionsTree.getSelection()[0].getData() instanceof TM1Server) {
					TM1Server tm1server = (TM1Server) connectionsTree.getSelection()[0].getData();

					MenuItem connectAsMenuItem = new MenuItem(treemenu, SWT.CASCADE);
					connectAsMenuItem.setText("Connect");
					if (!tm1server.isAuthenticated() && tm1server.isRestEnabled()) {
						connectAsMenuItem.setEnabled(true);
					} else {
						connectAsMenuItem.setEnabled(false);
					}
					connectAsMenuItem.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent event) {
							connectToTM1ServerAs();
						}
					});

					MenuItem disconnectMenuItem = new MenuItem(treemenu, SWT.NONE);
					disconnectMenuItem.setText("Disconnect");
					if (tm1server.isAuthenticated() && tm1server.isRestEnabled()) {
						disconnectMenuItem.setEnabled(true);
					} else {
						disconnectMenuItem.setEnabled(false);
					}
					disconnectMenuItem.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent event) {
							TM1Server tm1server = (TM1Server) (connectionsTree.getSelection()[0].getData());
							TreeItem adminnode = connectionsTree.getSelection()[0].getParentItem();
							try {
								if (tm1server.unauthenticate())
									refreshTM1AdminHostTree(adminnode);
							} catch (TM1RestException | URISyntaxException | IOException | JSONException e) {
								e.printStackTrace();
							}
						}
					});
				}
			}

		});

		displayInitAdminServerTree();

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
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

		for (int i = 0; i < adminservers.size(); i++) {
			TM1Admin adminserver = adminservers.get(i);
			TreeItem tm1admintreeitem = new TreeItem(connectionsTree, SWT.NONE);
			tm1admintreeitem.setText(adminserver.hostname);
			tm1admintreeitem.setImage(ADMINICON);
			tm1admintreeitem.setData(adminserver);

			for (int j = 0; j < adminserver.getservers().size(); j++) {
				TM1ServerStub tm1ServerStub = adminserver.getserver(j);
				TreeItem tm1serevrtreeitem = new TreeItem(tm1admintreeitem, SWT.NONE);
				if (tm1ServerStub.isRestEnabled()) {
					tm1serevrtreeitem.setText(tm1ServerStub.name);
				} else {
					tm1serevrtreeitem.setText(tm1ServerStub.name + " (HTTP Disabled)");
				}
				tm1serevrtreeitem.setImage(DISCONNECTEDICON);
				tm1serevrtreeitem.setData(tm1ServerStub);
			}
			tm1admintreeitem.setExpanded(true);
		}
	}

	public void refreshTM1AdminHostTree(TreeItem adminhostnode) {
		TM1Admin adminhost = (TM1Admin) adminhostnode.getData();
		adminhostnode.removeAll();
		adminhost.readTM1ServersFromServer();
		for (int i = 0; i < adminhost.getservers().size(); i++) {
			TM1ServerStub tm1ServerStub = adminhost.getserver(i);
			TreeItem tm1serevrtreeitem = new TreeItem(adminhostnode, SWT.NONE);
			if (tm1ServerStub.isRestEnabled()) {
				tm1serevrtreeitem.setText(tm1ServerStub.name);
			} else {
				tm1serevrtreeitem.setText(tm1ServerStub.name + " (HTTP Disabled)");
			}
			tm1serevrtreeitem.setData(tm1ServerStub);
			if (tm1ServerStub.isAuthenticated) {
				tm1serevrtreeitem.setImage(CONNECTEDICON);
			} else {
				tm1serevrtreeitem.setImage(DISCONNECTEDICON);
			}
		}
	}

	public void refreshFullExplorerHierarchy() {
		connectionsTree.removeAll();
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

		connectionsTree.removeAll();
		for (int i = 0; i < adminservers.size(); i++) {
			TM1Admin adminserver = adminservers.get(i);
			TreeItem tm1admintreeitem = new TreeItem(connectionsTree, SWT.NONE);
			tm1admintreeitem.setText(adminserver.hostname);
			tm1admintreeitem.setImage(ADMINICON);
			tm1admintreeitem.setData(adminserver);
			refreshTM1AdminHostTree(tm1admintreeitem);
			tm1admintreeitem.setExpanded(true);
			// if (adminserver.isExpanded()){
			// tm1admintreeitem.setExpanded(true);
			// }
		}

	}

	public void removeTM1ServerTree(TreeItem servernode) {
		servernode.removeAll();
	}

	public void expandNode(TreeItem node) {
		node.setExpanded(true);
		for (int i = 0; i < node.getItemCount(); i++) {
			expandNode(node.getItem(i));
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

	public void checkForReconnect(TreeItem serverNode) {
		TM1Server tm1server = (TM1Server) serverNode.getData();
		if (!tm1server.isAuthenticated()) {
			infoMessage("Disconnected from " + tm1server.getName());
			connectToTM1ServerAs();
			this.refreshTM1AdminHostTree(serverNode.getParentItem());
		}
	}

	public void connectToTM1ServerAs() {
		try {
			//System.out.println("=> connectToTM1ServerAs");
			TreeItem selectednode = connectionsTree.getSelection()[0];
			TM1ServerStub tm1ServerStub = (TM1ServerStub) (connectionsTree.getSelection()[0].getData());
			TM1Server tm1Server;
			if (tm1ServerStub.isCloud) {
				tm1Server = new TM1Server(tm1ServerStub.hostname, tm1ServerStub.name);
			} else {
				tm1Server = new TM1Server(tm1ServerStub.hostname, tm1ServerStub.name, tm1ServerStub.port, tm1ServerStub.useSSL);
			}
			tm1Server.readSecurityMode();
			//System.out.println("Read Security Mode OK");
			if (!tm1Server.isAuthenticated()) {
				//System.out.println("Not Authenticated Yet");
				while (!tm1Server.isAuthenticated()) {
					//System.out.println("Still Not Authenticated");
					ConnectAs dialog = new ConnectAs(shell, tm1Server);
					if (dialog.open()) {
						Credential c = dialog.getcredential();
						//System.out.println("Connecting as " + c.username);
						tm1Server.authenticate(c);

						TabItem serverExplorerTab = new TabItem(folders, SWT.CLOSE);
						serverExplorerTab.setText(tm1Server.getAdminHostName() + ":" + tm1Server.getName());
						ServerExplorerComposite serverExplorerComposite = new ServerExplorerComposite(folders, tm1Server);
						serverExplorerComposite.setWrarchitectMainWindow(wrarchitectMainWindow);
						serverExplorerTab.setControl(serverExplorerComposite);
						serverExplorerComposite.setLayout(new GridLayout(1, false));
						serverExplorerComposite.updateTM1ServerNode();
						selectednode.setText(tm1Server.getName() + " (" + tm1Server.displayuser() + ")");
						selectednode.setImage(CONNECTEDICON);
						folders.setSelection(folders.getItemCount() - 1);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			if (ex instanceof TM1RestException) {
				TM1RestException restException = (TM1RestException) ex;
				MessageBox m = new MessageBox(shell, SWT.ERROR | SWT.OK);
				m.setText("Error Connecting to TM1 Server");
				m.setMessage("Error: " + restException.getErrorCode());
				if (m.open() == SWT.OK) {
					connectToTM1ServerAs();
				}
			} else {
				MessageBox m = new MessageBox(shell, SWT.ERROR | SWT.OK);
				m.setText("Error");
				m.setMessage(ex.getMessage());
			}
		}
	}

	private void doConnection() {

	}

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

	public void setFolders(TabFolder folders) {
		this.folders = folders;
	}

	/*
	 * public void setActiveTab(TabItem activeTab){ this.activeTab = activeTab; }
	 * 
	 * public void setConnectorTab(TabItem connectorTab){ this.connectorTab =
	 * connectorTab; }
	 */

	public void setWrarchitectMainWindow(Wrarchitect wrarchitectMainWindow) {
		this.wrarchitectMainWindow = wrarchitectMainWindow;
	}

}
