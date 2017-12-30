package TM1Diagnostic.UI;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.OrderedJSONObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import TM1Diagnostic.REST.TM1RestException;
import TM1Diagnostic.REST.TM1Server;

public class ThreadMonitorComposite extends Composite {

	protected Shell shlHttpTmtop;
	private Display display;


	private Table threadMonitorTable;
	private Menu threadMonitorTableMenu;
	private Runnable threadMonitorTimer;
	private Text threadMontiorRefreshSecondsText;
	private int threadMonitorRefresh = 10;
	private Button threadMonitorActiveButton;
	private Button threadMonitorFilterIdleCheckButton;

	private TM1Server tm1server;

	public ThreadMonitorComposite(Composite parent, ServerExplorerComposite explorer, TM1Server tm1server) throws TM1RestException {
		super(parent, SWT.EMBEDDED);
		this.tm1server = tm1server;
		display = parent.getDisplay();
		onOpen();
	}


	private void onOpen() throws TM1RestException {
		setLayout(new GridLayout(1, false));

		Composite topRowComposite = new Composite(this, SWT.NONE);
		topRowComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		topRowComposite.setSize(1225, 36);
		topRowComposite.setLayout(new GridLayout(6, false));

		Label refreshLabel = new Label(topRowComposite, SWT.NONE);
		GridData gd_refreshLabel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_refreshLabel.widthHint = 81;
		refreshLabel.setLayoutData(gd_refreshLabel);
		refreshLabel.setText("Refresh");

		threadMontiorRefreshSecondsText = new Text(topRowComposite, SWT.BORDER);
		GridData gd_text = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_text.widthHint = 60;
		threadMontiorRefreshSecondsText.setLayoutData(gd_text);
		threadMontiorRefreshSecondsText.setText("10");

		threadMontiorRefreshSecondsText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				try {
					int val = Integer.parseInt(threadMontiorRefreshSecondsText.getText());
					if (val < 1 || val > 60) {
						MessageBox dialog = new MessageBox(shlHttpTmtop, SWT.OK);
						dialog.setText("Enter a value between 1-60");
						dialog.open();
						threadMontiorRefreshSecondsText.setText(String.valueOf(threadMonitorRefresh));
					} else {
						threadMonitorRefresh = val;
					}
				} catch (Exception e) {
					MessageBox dialog = new MessageBox(shlHttpTmtop, SWT.OK);
					dialog.setMessage("Enter a value between 1-60");
					dialog.open();
				}
			}
		});

		threadMonitorFilterIdleCheckButton = new Button(topRowComposite, SWT.CHECK);
		threadMonitorFilterIdleCheckButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {

			}
		});
		threadMonitorFilterIdleCheckButton.setText("Filter Idle");

		Label lblNewLabel = new Label(topRowComposite, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Button btnCheckButton = new Button(topRowComposite, SWT.CHECK);
		btnCheckButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		btnCheckButton.setText("Write Log");

		threadMonitorActiveButton = new Button(topRowComposite, SWT.CHECK);
		threadMonitorActiveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (threadMonitorActiveButton.getSelection()) {
					readThreadsFromServer();
					display.timerExec(threadMonitorRefresh * 1000, threadMonitorTimer);
				} else {
					display.timerExec(-1, threadMonitorTimer);
				} 
			}
		});
		threadMonitorActiveButton.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, true, 1, 1));
		threadMonitorActiveButton.setText("Active");

		Composite bottomRow = new Composite(this, SWT.NONE);
		bottomRow.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, true, true, 1, 1));
		bottomRow.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		bottomRow.setLayout(new FillLayout(SWT.VERTICAL));

		threadMonitorTable = new Table(bottomRow, SWT.BORDER | SWT.FULL_SELECTION);
		threadMonitorTable.setHeaderVisible(true);
		threadMonitorTable.setLinesVisible(true);

		TableColumn col1 = new TableColumn(threadMonitorTable, SWT.NONE);
		col1.setText("Thread");

		TableColumn col2 = new TableColumn(threadMonitorTable, SWT.NONE);
		col2.setText("Type");

		TableColumn col3 = new TableColumn(threadMonitorTable, SWT.NONE);
		col3.setText("Name");

		TableColumn col4 = new TableColumn(threadMonitorTable, SWT.NONE);
		col4.setText("Context");

		TableColumn col5 = new TableColumn(threadMonitorTable, SWT.NONE);
		col5.setText("State");

		TableColumn col6 = new TableColumn(threadMonitorTable, SWT.NONE);
		col6.setText("Function");

		TableColumn col7 = new TableColumn(threadMonitorTable, SWT.NONE);
		col7.setText("Object Type");

		TableColumn col8 = new TableColumn(threadMonitorTable, SWT.NONE);
		col8.setText("Object Name");

		TableColumn col9 = new TableColumn(threadMonitorTable, SWT.NONE);
		col9.setText("RLocks");

		TableColumn col10 = new TableColumn(threadMonitorTable, SWT.NONE);
		col10.setText("IXLocks");

		TableColumn col11 = new TableColumn(threadMonitorTable, SWT.NONE);
		col11.setText("WLocks");

		TableColumn col12 = new TableColumn(threadMonitorTable, SWT.NONE);
		col12.setText("Elapsed");

		TableColumn col13 = new TableColumn(threadMonitorTable, SWT.NONE);
		col13.setText("Wait");

		threadMonitorTable.addListener(SWT.Resize, new Listener() {
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

		threadMonitorTableMenu = new Menu(threadMonitorTable);
		threadMonitorTable.setMenu(threadMonitorTableMenu);

		threadMonitorTableMenu.addMenuListener(new MenuAdapter() {
			public void menuShown(MenuEvent e) {
				MenuItem[] items = threadMonitorTableMenu.getItems();
				for (int i = 0; i < items.length; i++) {
					items[i].dispose();
				}

				try {
					TableItem selectedrow = threadMonitorTable.getSelection()[0];
					MenuItem mntmCancelThread = new MenuItem(threadMonitorTableMenu, SWT.NONE);
					if (!selectedrow.getText(4).toLowerCase().equals("run")) {
						mntmCancelThread.setEnabled(false);
					}
					mntmCancelThread.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent event) {
							TableItem selectedrow = threadMonitorTable.getSelection()[0];
							long threadid = Long.parseLong(selectedrow.getText(0));
							cancelthread(threadid);
						}
					});
					mntmCancelThread.setText("Cancel Thread");
				} catch (Exception ex) {
					System.out.println("Clicked on something that isn't a row!");
				}

			}
		});

		threadMonitorTimer = new Runnable() {
			public void run() {
				display.timerExec(threadMonitorRefresh * 1000, this);
				readThreadsFromServer();
			}
		};

	}

	public void setTM1Server(TM1Server tm1server) {
		this.tm1server = tm1server;
	}

	public void readThreadsFromServer() {
		String request = "Threads";
		try {
			tm1server.get(request);
			threadMonitorTable.setRedraw(false);
			OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
			JSONArray threadJSONArray = jresponse.getJSONArray("value");
			threadMonitorTable.removeAll();

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

				if (line.has("ID")) {
					threadId = String.valueOf(line.getLong("ID"));
				} else if (line.has("ThreadID")) {
					threadId = String.valueOf(line.getLong("ThreadID"));
				}
				if (line.has("Type")) {
					type = line.getString("Type");
				}
				if (line.has("Name")) {
					name = line.getString("Name");
				}
				if (line.has("Context")) {
					context = line.getString("Context");
				}
				if (line.has("State")) {
					state = line.getString("State");
				}
				if (line.has("Function")) {
					function = line.getString("Function");
				}
				if (line.has("ObjectType")) {
					objectType = line.getString("ObjectType");
				}
				if (line.has("ObjectName")) {
					objectName = line.getString("ObjectName");
				}

				if (line.has("RLocks")) {
					rLocks = Integer.toString(line.getInt("RLocks"));
				}
				if (line.has("IXLocks")) {
					ixLocks = Integer.toString(line.getInt("IXLocks"));
				}
				if (line.has("WLocks")) {
					wLocks = Integer.toString(line.getInt("WLocks"));
				}

				elapsed = String.valueOf(countSeconds(line.getString("ElapsedTime")));
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
			for (TableColumn tc : threadMonitorTable.getColumns())
				tc.pack();
			threadMonitorTable.setRedraw(true);
		} catch (TM1RestException | URISyntaxException | IOException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void cancelthread(long threadid) {
		String request = "Threads(" + threadid + ")/tm1.CancelOperation";
		try {
			OrderedJSONObject payload = new OrderedJSONObject();
			tm1server.post(request, payload);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public int countSeconds(String s) {
		int secs = Integer.parseInt(s.substring(10, 12));
		int mins = Integer.parseInt(s.substring(7, 9));
		int hours = Integer.parseInt(s.substring(4, 6));
		int days = Integer.parseInt(s.substring(1, 2));
		return (days * 86400) + (hours * 3600) + (mins * 60) + secs;
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public void stopThreadMonitor() {
		display.timerExec(-1, threadMonitorTimer);
	}

	public Button getFilterIdleCheckButton() {
		return threadMonitorFilterIdleCheckButton;
	}
}
