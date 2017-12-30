 package TM1Diagnostic.UI;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;

import TM1Diagnostic.TransactionQuery;
import TM1Diagnostic.REST.TM1Server;

import org.apache.wink.json4j.OrderedJSONObject;
import org.apache.wink.json4j.JSONArray;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.DateTime;

public class TransactionLogViewer extends Dialog {

	protected Object result;
	protected Shell shlTransactionLogViewer;
	private TM1Server tm1server;
	private Display display;
	private Table transactionTable;
	private Label transactionRowCountLabel;

	private Combo cubeCombo;
	private Combo clientCombo;

	private String cubeFilter;
	private String userFilter;

	private String startDateFilter;
	private String startTimeFilter;

	private String endDateFilter;
	private String endTimeFilter;

	private DateTime fromDate;
	private DateTime toDate;
	private DateTime fromTime;
	private DateTime toTime;

	public TransactionLogViewer(Shell parent, TM1Server server) {
		super(parent, SWT.DIALOG_TRIM);
		tm1server = server;
		setText("SWT Dialog");

		cubeFilter = "*";
		userFilter = "*";
		startDateFilter = "2000-01-01";
		startTimeFilter = "00:00:00";
		endDateFilter = "2100-01-01";
		endTimeFilter = "00:00:00";

	}

	public Object open() {
		display = getParent().getDisplay();
		createContents();
		shlTransactionLogViewer.open();
		shlTransactionLogViewer.layout();

		while (!shlTransactionLogViewer.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlTransactionLogViewer = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.MAX | SWT.RESIZE | SWT.APPLICATION_MODAL);
		shlTransactionLogViewer.setSize(936, 682);
		shlTransactionLogViewer.setText("Transaction Log");
		shlTransactionLogViewer.setLayout(new GridLayout(1, false));

		Composite composite_2 = new Composite(shlTransactionLogViewer, SWT.NONE);
		composite_2.setLayout(new GridLayout(3, false));
		composite_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblNewLabel = new Label(composite_2, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("Cube");

		cubeCombo = new Combo(composite_2, SWT.READ_ONLY);
		cubeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		cubeCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				cubeFilter = cubeCombo.getText();
			}
		});

		Label clientLabel = new Label(composite_2, SWT.NONE);
		clientLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		clientLabel.setText("Client");

		clientCombo = new Combo(composite_2, SWT.READ_ONLY);
		clientCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		clientCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				userFilter = clientCombo.getText();
			}
		});

		Label lblNewLabel_1 = new Label(composite_2, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_1.setText("From Date");

		fromDate = new DateTime(composite_2, SWT.BORDER);
		fromDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		fromDate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					String from_string = fromDate.getYear() + "-" + fromDate.getMonth() + "-" + fromDate.getDay();
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
					Date from_dt = df.parse(from_string);
					startDateFilter = from_dt.toString();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		fromDate.setDate(2000, 01, 01);

		fromTime = new DateTime(composite_2, SWT.BORDER | SWT.TIME);
		fromTime.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					String from_t_string = fromTime.getHours() + ":" + fromTime.getMinutes() + ":" + fromTime.getSeconds();
					SimpleDateFormat tf = new SimpleDateFormat("hh:mm:ss");
					Date from_tt = tf.parse(from_t_string);
					startTimeFilter = tf.format(from_tt);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		fromTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		fromTime.setTime(0, 0, 0);

		Label lblNewLabel_2 = new Label(composite_2, SWT.NONE);
		lblNewLabel_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_2.setText("To Date");

		toDate = new DateTime(composite_2, SWT.BORDER);
		toDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		toDate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					String to_string = toDate.getYear() + "-" + toDate.getMonth() + "-" + toDate.getDay();
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
					Date to_dt = df.parse(to_string);
					endDateFilter = df.format(to_dt);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		toDate.setDate(2100, 01, 01);

		toTime = new DateTime(composite_2, SWT.BORDER | SWT.TIME);
		toTime.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					String to_t_string = toTime.getHours() + ":" + toTime.getMinutes() + ":" + toTime.getSeconds();
					SimpleDateFormat tf = new SimpleDateFormat("hh:mm:ss");
					Date to_tt = tf.parse(to_t_string);
					startTimeFilter = tf.format(to_tt);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		toTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		toTime.setTime(0, 0, 0);

		Composite composite = new Composite(shlTransactionLogViewer, SWT.NONE);
		composite.setSize(688, 376);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		transactionTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		transactionTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		transactionTable.setHeaderVisible(true);
		transactionTable.setLinesVisible(true);

		transactionTable.addListener(SWT.Resize, new Listener() {
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

		TableColumn changeSetColumn = new TableColumn(transactionTable, SWT.NONE);
		changeSetColumn.setWidth(115);
		changeSetColumn.setText("Change Set ID");

		TableColumn timestampColumn = new TableColumn(transactionTable, SWT.NONE);
		timestampColumn.setWidth(100);
		timestampColumn.setText("Timestamp");

		TableColumn replicationTimeColumn = new TableColumn(transactionTable, SWT.NONE);
		replicationTimeColumn.setWidth(122);
		replicationTimeColumn.setText("Replication Time");

		TableColumn clientColumn = new TableColumn(transactionTable, SWT.NONE);
		clientColumn.setWidth(100);
		clientColumn.setText("Client");

		TableColumn cubeColumn = new TableColumn(transactionTable, SWT.NONE);
		cubeColumn.setWidth(100);
		cubeColumn.setText("Cube");

		TableColumn oldValueColumn = new TableColumn(transactionTable, SWT.NONE);
		oldValueColumn.setWidth(100);
		oldValueColumn.setText("Old Value");

		TableColumn newValueColumn = new TableColumn(transactionTable, SWT.NONE);
		newValueColumn.setWidth(100);
		newValueColumn.setText("New Value");

		TableColumn keyColumn = new TableColumn(transactionTable, SWT.NONE);
		keyColumn.setWidth(100);
		keyColumn.setText("Key 1");

		transactionRowCountLabel = new Label(shlTransactionLogViewer, SWT.NONE);
		transactionRowCountLabel.setText("Row Count:");
		transactionRowCountLabel.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1));

		Composite composite_1 = new Composite(shlTransactionLogViewer, SWT.NONE);
		composite_1.setLayout(new GridLayout(4, false));
		composite_1.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, true, false, 1, 1));
		composite_1.setBounds(0, 0, 64, 64);

		Button btnNewButton_2 = new Button(composite_1, SWT.NONE);
		btnNewButton_2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				downloadTransactions();
			}
		});
		GridData gd_btnNewButton_2 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnNewButton_2.widthHint = 140;
		btnNewButton_2.setLayoutData(gd_btnNewButton_2);
		btnNewButton_2.setText("Download");

		Button countButton = new Button(composite_1, SWT.NONE);
		countButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				transactionRowCount();
			}
		});
		GridData gd_countButton = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
		gd_countButton.widthHint = 140;
		countButton.setLayoutData(gd_countButton);
		countButton.setText("Count");

		Button btnNewButton_1 = new Button(composite_1, SWT.NONE);
		GridData gd_btnNewButton_1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnNewButton_1.widthHint = 140;
		btnNewButton_1.setLayoutData(gd_btnNewButton_1);
		btnNewButton_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				TransactionLogQuery queryDialog = new TransactionLogQuery(shlTransactionLogViewer, tm1server);
				readTransactionsFromServer();
			}
		});
		btnNewButton_1.setText("Query");

		Button btnNewButton = new Button(composite_1, SWT.NONE);
		GridData gd_btnNewButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnNewButton.widthHint = 140;
		btnNewButton.setLayoutData(gd_btnNewButton);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				shlTransactionLogViewer.close();
			}
		});
		btnNewButton.setText("Close");

		pullclients();
		pullcubes();
	}

	public void readTransactionsFromServer() {
		try {
			transactionRowCount();

			transactionTable.removeAll();
			String request = "TransactionLogEntries";
			String startDateTimeFilter = startDateFilter + "T" + startTimeFilter + "Z";
			String endDateTimeFilter = endDateFilter + "T" + endTimeFilter + "Z";
			String query = "$filter=TimeStamp gt " + startDateTimeFilter + " and TimeStamp lt " + endDateTimeFilter;
			if (!userFilter.equals("*")) {
				query = query + " and User eq '" + userFilter + "'";
			}
			if (!cubeFilter.equals("*")) {
				query = query + " and Cube eq '" + cubeFilter + "'";
			}

			tm1server.get(request, query);
				OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
				JSONArray transactions = jresponse.getJSONArray("value");
				for (int i = 0; i < transactions.length(); i++) {
					OrderedJSONObject transactionJSON = (OrderedJSONObject) transactions.getJSONObject(i);

					TableItem tableItem = new TableItem(transactionTable, SWT.NONE);

					String ChangeSetID = transactionJSON.getString("ChangeSetID");
					tableItem.setText(0, ChangeSetID);

					String LogTime = transactionJSON.getString("TimeStamp");
					tableItem.setText(1, LogTime);

					String ReplicationTime = "";
					if (!transactionJSON.isNull("ReplicationTime")) {
						ReplicationTime = transactionJSON.getString("ReplicationTime");
					}
					tableItem.setText(2, ReplicationTime);

					String User = "";
					if (!transactionJSON.isNull("User")) {
						User = transactionJSON.getString("User");
					}
					tableItem.setText(3, User);

					String Cube = "";
					if (!transactionJSON.isNull("Cube")) {
						Cube = transactionJSON.getString("Cube");
					}
					tableItem.setText(4, Cube);

					String OldValue = "";
					if (!transactionJSON.isNull("OldValue")) {
						OldValue = transactionJSON.getString("OldValue");
					}
					tableItem.setText(5, OldValue);

					String NewValue = "";
					if (!transactionJSON.isNull("NewValue")) {
						NewValue = transactionJSON.getString("NewValue");
					}
					tableItem.setText(6, NewValue);

					if (!transactionJSON.isNull("Tuple")) {
						JSONArray TupleJSON = transactionJSON.getJSONArray("Tuple");
						for (int j = 0; j < TupleJSON.length(); j++) {
							if (j + 7 > transactionTable.getColumnCount()) {
								TableColumn tc = new TableColumn(transactionTable, SWT.NONE);
								tc.setText("Key " + j);
							}
							tableItem.setText(7 + j, TupleJSON.get(j).toString());
						}
					}

				}
			for (TableColumn tc : transactionTable.getColumns())
				tc.pack();
			transactionTable.redraw();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public boolean pullclients() {
		try {
			clientCombo.add("*");
			clientCombo.select(0);

			String request = "Dimensions('}Clients')/DefaultHierarchy/Elements";
			String query = "$select=Attributes";
			tm1server.get(request, query);
				OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
				JSONArray jclients = jresponse.getJSONArray("value");
				for (int i = 0; i < jclients.length(); i++) {
					OrderedJSONObject client = (OrderedJSONObject) jclients.getJSONObject(i);
					OrderedJSONObject att = (OrderedJSONObject) client.getJSONObject("Attributes");
					clientCombo.add(att.getString("}TM1_DefaultDisplayValue"));
				}
				return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public boolean pullcubes() {
		try {
			cubeCombo.add("*");
			cubeCombo.select(0);
			String request = "Cubes";
			tm1server.get(request);
				OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
				JSONArray jcubes = jresponse.getJSONArray("value");
				for (int i = 0; i < jcubes.length(); i++) {
					OrderedJSONObject cube = (OrderedJSONObject) jcubes.getJSONObject(i);
					cubeCombo.add(cube.getString("Name"));
				}
				return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	protected DateTime getDateTime() {
		return fromDate;
	}

	protected DateTime getDateTime_1() {
		return toDate;
	}

	private long transactionRowCount() {
		try {
			String request = "TransactionLogEntries/$count";
			String startDateTimeFilter = startDateFilter + "T" + startTimeFilter + "Z";
			String endDateTimeFilter = endDateFilter + "T" + endTimeFilter + "Z";
			String query = "$filter=TimeStamp gt " + startDateTimeFilter + " and TimeStamp lt " + endDateTimeFilter;
			if (!userFilter.equals("*")) {
				query = query + " and User eq '" + userFilter + "'";
			}
			if (!cubeFilter.equals("*")) {
				query = query + " and Cube eq '" + cubeFilter + "'";
			}

			tm1server.get(request, query);
			long rowCount = Long.parseLong(tm1server.response);
			transactionRowCountLabel.setText("Row count: " + rowCount);
			return rowCount;
		} catch (Exception ex) {
			ex.printStackTrace();
			return 0;
		}
	}

	private void downloadTransactions() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String datetimeString = sdf.format(date);

		DirectoryDialog directorydialog = new DirectoryDialog(shlTransactionLogViewer, SWT.OPEN);
		if (directorydialog.open() != null) {
			String directory = directorydialog.getFilterPath();
			try {
				File f = new File(directory + "//tm1s." + datetimeString);
				FileWriter fw = new FileWriter(f, false);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter pw = new PrintWriter(bw);
				for (int i = 0; i < transactionTable.getItemCount(); i++) {
					TableItem t = transactionTable.getItem(i);
					pw.println(t.getText(0) + ", " + t.getText(1) + ", " + t.getText(2) + ", " + t.getText(3) + ", " + t.getText(4) + ", " + t.getText(5) + ", " + t.getText(6) + ", " + t.getText(7));
				}
				pw.close();
				bw.close();
				fw.close();
				infoMessage("Downloaded " + f.getAbsolutePath());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void infoMessage(String message) {
		MessageBox m = new MessageBox(shlTransactionLogViewer, SWT.ICON_INFORMATION);
		m.setMessage(message);
		m.open();
	}

	public void errorMessage(String errMessage) {
		MessageBox m = new MessageBox(shlTransactionLogViewer, SWT.ICON_ERROR);
		m.setMessage(errMessage);
		m.open();
	}

}
