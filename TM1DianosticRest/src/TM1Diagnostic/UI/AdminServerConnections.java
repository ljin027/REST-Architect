package TM1Diagnostic.UI;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Composite;

//import org.eclipse.jface.dialogs.Dialog;

public class AdminServerConnections extends Dialog {

	private Shell shell;
	
	private Table localAdminHostsTable;
	private Table ibmCloudTable;

	private Menu localAdminHostTableMenu;
	private Menu ibmCloudTableMenu;

	private String path = ".//connections//";

	private boolean refreshOnClose;

	public AdminServerConnections(Shell parent) {
		super(parent, SWT.DIALOG_TRIM | SWT.RESIZE);
	}

	/**
	 * Open the window.
	 */
	public boolean open() {
		createContents();
		shell.open();
		shell.layout();

		Display display = shell.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		return refreshOnClose;
	}


	protected void createContents() {

		shell = new Shell(getParent(), SWT.SHELL_TRIM | SWT.BORDER);
		shell.setSize(1001, 657);
		shell.setLayout(new GridLayout(1, false));

		shell.setText("TM1 Admin Server Connections");

		Group admin_group = new Group(shell, SWT.NONE);
		admin_group.setLayout(new GridLayout(1, false));
		admin_group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		admin_group.setText("Local TM1 Admin Hosts");

		localAdminHostsTable = new Table(admin_group, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
		localAdminHostsTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		localAdminHostsTable.setLinesVisible(true);
		localAdminHostsTable.setHeaderVisible(true);
		localAdminHostsTable.addListener(SWT.Resize, new Listener() {

			@Override
			public void handleEvent(Event event) {
				Table table = (Table) event.widget;
				
				int columnCount = table.getColumnCount();
				if (columnCount == 0)
					return;
				Rectangle area = table.getClientArea();
				int totalAreaWdith = area.width;
				int lineWidth = table.getGridLineWidth();
				int totalGridLineWidth = (columnCount - 1) * lineWidth;
				int totalColumnWidth = 0;
				for (TableColumn column : table.getColumns()) {
					totalColumnWidth = totalColumnWidth + column.getWidth();
				}
				int diff = totalAreaWdith - (totalColumnWidth + totalGridLineWidth);
				TableColumn lastCol = table.getColumns()[columnCount - 1];
				lastCol.setWidth(diff + lastCol.getWidth());

			}
		});
		
		TableColumn adminhostname_column = new TableColumn(localAdminHostsTable, SWT.NONE);
		adminhostname_column.setWidth(372);
		adminhostname_column.setText("Server");

		TableColumn port_column = new TableColumn(localAdminHostsTable, SWT.NONE);
		port_column.setWidth(318);
		port_column.setText("Port");

		localAdminHostTableMenu = new Menu(localAdminHostsTable);
		localAdminHostsTable.setMenu(localAdminHostTableMenu);
		
		Composite composite = new Composite(admin_group, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false, 1, 1));
		composite.setLayout(new GridLayout(2, false));
		
		Button addLocalButton = new Button(composite, SWT.NONE);
		addLocalButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addLocalAdmin();
			}
		});
		GridData gd_addLocalButton = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_addLocalButton.widthHint = 100;
		gd_addLocalButton.minimumWidth = 140;
		addLocalButton.setLayoutData(gd_addLocalButton);
		addLocalButton.setText("New ");
		
		Button removeLocalButton = new Button(composite, SWT.NONE);
		removeLocalButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (localAdminHostsTable.getSelectionCount() > 0){
					removeLocalAdmin();
				} else {
					MessageBox m = new MessageBox(shell, SWT.ERROR);
					m.setText("Error");
					m.setMessage("No TM1 Admin server selected");
					m.open();
				}
			}
		});
		GridData gd_removeLocalButton = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_removeLocalButton.widthHint = 100;
		removeLocalButton.setLayoutData(gd_removeLocalButton);
		removeLocalButton.setText("Remove");

		localAdminHostTableMenu.addMenuListener(new MenuAdapter() {
			public void menuShown(MenuEvent e) {
				MenuItem[] items = localAdminHostTableMenu.getItems();
				for (int i = 0; i < items.length; i++) {
					items[i].dispose();
				}

				MenuItem mntmAddAdminServer = new MenuItem(localAdminHostTableMenu, SWT.NONE);
				mntmAddAdminServer.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent arg0) {
						addLocalAdmin();
					}
				});
				mntmAddAdminServer.setText("Add Admin Server");

				MenuItem mntmRemoveAdminServer = new MenuItem(localAdminHostTableMenu, SWT.NONE);
				mntmRemoveAdminServer.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						removeLocalAdmin();
					}
				});
				mntmRemoveAdminServer.setText("Remove Admin Server");
				if (localAdminHostsTable.getSelectionCount() == 0) {
					mntmRemoveAdminServer.setEnabled(false);
				} else {
					mntmRemoveAdminServer.setEnabled(true);
				}

			}
		});

		Group cloud_group = new Group(shell, SWT.NONE);
		cloud_group.setText("IBM Cloud TM1 Servers");
		cloud_group.setLayout(new GridLayout(1, false));
		cloud_group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		new Label(cloud_group, SWT.NONE);

		ibmCloudTable = new Table(cloud_group, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
		ibmCloudTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		ibmCloudTable.setHeaderVisible(true);
		ibmCloudTable.setLinesVisible(true);
		
		ibmCloudTable.addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(Event event) {
				Table table = (Table) event.widget;
				int columnCount = table.getColumnCount();
				if (columnCount == 0)
					return;
				Rectangle area = table.getClientArea();
				int totalAreaWdith = area.width;
				int lineWidth = table.getGridLineWidth();
				int totalGridLineWidth = (columnCount - 1) * lineWidth;
				int totalColumnWidth = 0;
				for (TableColumn column : table.getColumns()) {
					totalColumnWidth = totalColumnWidth + column.getWidth();
				}
				int diff = totalAreaWdith - (totalColumnWidth + totalGridLineWidth);
				TableColumn lastCol = table.getColumns()[columnCount - 1];
				lastCol.setWidth(diff + lastCol.getWidth());
			}
		});

		ibmCloudTableMenu = new Menu(ibmCloudTable);
		ibmCloudTable.setMenu(ibmCloudTableMenu);
		ibmCloudTableMenu.addMenuListener(new MenuAdapter() {
			public void menuShown(MenuEvent e) {
				MenuItem[] items = ibmCloudTableMenu.getItems();
				for (int i = 0; i < items.length; i++) {
					items[i].dispose();
				}

				MenuItem mntmAddAdminServer = new MenuItem(ibmCloudTableMenu, SWT.NONE);
				mntmAddAdminServer.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent arg0) {
						addCloudServer();
					}
				});
				mntmAddAdminServer.setText("Add Cloud TM1 Server");

				MenuItem mntmRemoveAdminServer = new MenuItem(ibmCloudTableMenu, SWT.NONE);
				mntmRemoveAdminServer.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						removeCloudServer();
					}
				});
				mntmRemoveAdminServer.setText("Remove Cloud TM1 Server");
				if (ibmCloudTable.getSelectionCount() == 0) {
					mntmRemoveAdminServer.setEnabled(false);
				} else {
					mntmRemoveAdminServer.setEnabled(true);
				}

			}
		});
		
		TableColumn ibmCloudTableColumn1 = new TableColumn(ibmCloudTable, SWT.NONE);
		ibmCloudTableColumn1.setWidth(475);
		ibmCloudTableColumn1.setText("URL");

		TableColumn ibmCloudTableColumn2 = new TableColumn(ibmCloudTable, SWT.NONE);
		ibmCloudTableColumn2.setWidth(172);
		ibmCloudTableColumn2.setText("Model Name");
		
		Composite composite_1 = new Composite(cloud_group, SWT.NONE);
		composite_1.setLayout(new GridLayout(2, false));
		composite_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		
		Button newCloudButton = new Button(composite_1, SWT.NONE);
		newCloudButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addCloudServer();
			}
		});
		GridData gd_newCloudButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_newCloudButton.widthHint = 100;
		newCloudButton.setLayoutData(gd_newCloudButton);
		newCloudButton.setText("New");
		
		Button removeCloudButton = new Button(composite_1, SWT.NONE);
		removeCloudButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				removeCloudServer();
			}
		});
		GridData gd_removeCloudButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_removeCloudButton.widthHint = 100;
		removeCloudButton.setLayoutData(gd_removeCloudButton);
		removeCloudButton.setText("Remove");

		Composite buttonsComposite = new Composite(shell, SWT.NONE);
		buttonsComposite.setLayout(new GridLayout(1, false));
		buttonsComposite.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, false, false, 1, 1));

		Button saveButton = new Button(buttonsComposite, SWT.NONE);
		GridData gd_saveButton = new GridData(SWT.RIGHT, SWT.BOTTOM, false, false, 1, 1);
		gd_saveButton.widthHint = 150;
		saveButton.setLayoutData(gd_saveButton);
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				writeLocalAdminServersToFile();
				writeCloudServersToFile();
				refreshOnClose = true;
				shell.close();
			}
		});
		saveButton.setText("Close");
		readAdminServersFromFile();
		readIBMCloudServersFromFile();
	}

	public boolean readAdminServersFromFile() {
		String serverfilename = path + "adminhosts";
		try {
			FileReader fr = new FileReader(serverfilename);
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();
			while (line != null) {
				String[] tokens = line.split(":");
				TableItem t = new TableItem(localAdminHostsTable, SWT.NONE);
				t.setText(0, tokens[0]);
				t.setText(1, tokens[1]);
				line = br.readLine();
			}
			for (TableColumn tc : localAdminHostsTable.getColumns()) tc.pack();
			localAdminHostsTable.redraw();
			br.close();
			fr.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean readIBMCloudServersFromFile() {
		String serverfilename = path + "ibmcloudservers";
		try {
			FileReader fr = new FileReader(serverfilename);
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();
			while (line != null) {
				String[] tokens = line.split(":");
				TableItem t = new TableItem(ibmCloudTable, SWT.NONE);
				t.setText(0, tokens[0]);
				t.setText(1, tokens[1]);
				line = br.readLine();
			}
			for (TableColumn tc : ibmCloudTable.getColumns()) tc.pack();
			ibmCloudTable.redraw();
			br.close();
			fr.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean writeLocalAdminServersToFile() {
		try {
			FileWriter fw = new FileWriter(path + "adminhosts");
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter pw = new PrintWriter(bw);
			for (int i = 0; i < localAdminHostsTable.getItemCount(); i++) {
				TableItem t = localAdminHostsTable.getItem(i);
				pw.println(t.getText(0) + ":" + t.getText(1));
			}
			pw.close();
			bw.close();
			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean writeCloudServersToFile() {
		try {
			FileWriter fw = new FileWriter(path + "ibmcloudservers");
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter pw = new PrintWriter(bw);
			for (int i = 0; i < ibmCloudTable.getItemCount(); i++) {
				TableItem tableItem = ibmCloudTable.getItem(i);
				pw.println(tableItem.getText(0) + ":" + tableItem.getText(1));
			}
			pw.close();
			bw.close();
			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	private void addLocalAdmin(){
		AddLocalAdminServer addLocalAdminServerDialog = new AddLocalAdminServer(shell);
		if (addLocalAdminServerDialog.open()){
			String adminServerHostName = addLocalAdminServerDialog.getAdminServerName();
			String adminServerPort = addLocalAdminServerDialog.getAdminServerPort();
			System.out.println("xxx " + adminServerHostName);
			TableItem row = new TableItem(localAdminHostsTable, SWT.NONE);
			row.setText(0, adminServerHostName);
			row.setText(1, adminServerPort);
			for (TableColumn tc : localAdminHostsTable.getColumns()) tc.pack();
			localAdminHostsTable.redraw();
			writeLocalAdminServersToFile();
		}
	}
	
	private void removeLocalAdmin(){
		TableItem t = localAdminHostsTable.getSelection()[0];
		t.dispose();
		writeLocalAdminServersToFile();
	}
	
	private void addCloudServer(){
		AddCloudServer addCloudServerDialog = new AddCloudServer();
		if (addCloudServerDialog.open()){
			String adminServerHostName = addCloudServerDialog.getCloudHostName();
			String adminServerPort = addCloudServerDialog.getTM1ModelName();
			TableItem row = new TableItem(ibmCloudTable, SWT.NONE);
			row.setText(0, adminServerHostName);
			row.setText(1, adminServerPort);
			for (TableColumn tc : ibmCloudTable.getColumns()) tc.pack();
			ibmCloudTable.redraw();
		}
		writeCloudServersToFile();
	}

	private void removeCloudServer(){
		TableItem t = ibmCloudTable.getSelection()[0];
		t.dispose();
		writeCloudServersToFile();
	}
	
}
