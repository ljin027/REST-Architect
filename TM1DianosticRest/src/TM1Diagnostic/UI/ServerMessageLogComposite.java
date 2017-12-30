package TM1Diagnostic.UI;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.OrderedJSONObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import TM1Diagnostic.REST.TM1RestException;
import TM1Diagnostic.REST.TM1Server;

public class ServerMessageLogComposite extends Composite {

	private TM1Server tm1server;
	private Display display;
	private Table serverMessageLogTable;
	private Text messageLogRowCountText;
	private int messageLogRowCount;
	private int messageLogRefresh;
	private Runnable messageLogRefreshTimer;
	private Button autoRefeshButton;
	private Text messageLogRefreshPeriodText;
	private Shell shell;

	public ServerMessageLogComposite(Composite parent, ServerExplorerComposite explorer, TM1Server tm1server) {
		super(parent, SWT.EMBEDDED);
		this.tm1server = tm1server;
		display = parent.getDisplay();
		onOpen();
	}

	private void onOpen(){
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		setLayout(new GridLayout(1, false));

		Composite composite_1 = new Composite(this, SWT.NONE);
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		composite_1.setLayout(new GridLayout(10, false));

		autoRefeshButton = new Button(composite_1, SWT.CHECK);
		autoRefeshButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) 
			{
				if (autoRefeshButton.getSelection()){
					display.timerExec(messageLogRefresh*1000, messageLogRefreshTimer);
				} else {
					display.timerExec(-1, messageLogRefreshTimer);
				}
			}
		});
		autoRefeshButton.setAlignment(SWT.CENTER);
		autoRefeshButton.setText("Automatic Refresh");

		Label label = new Label(composite_1, SWT.SEPARATOR | SWT.VERTICAL);
		GridData gd_label = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_label.heightHint = 20;
		label.setLayoutData(gd_label);

		Label lblNewLabel_1 = new Label(composite_1, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_1.setText("Row Count");

		messageLogRowCountText = new Text(composite_1, SWT.BORDER);
		messageLogRowCountText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				try {
					int ivalue = Integer.parseInt(messageLogRowCountText.getText());
					messageLogRowCount = ivalue;
				} catch (Exception ex) {
					errorMessage("Invalid value entered");
					messageLogRowCountText.setText(Integer.toString(messageLogRowCount));
					ex.printStackTrace();
				}
			}
		});
		messageLogRowCountText.setText("100");

		Label label_1 = new Label(composite_1, SWT.SEPARATOR | SWT.VERTICAL);
		GridData gd_label_1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_label_1.heightHint = 20;
		label_1.setLayoutData(gd_label_1);

		Label lblNewLabel_2 = new Label(composite_1, SWT.NONE);
		lblNewLabel_2.setText("Refresh Period");

		messageLogRefreshPeriodText = new Text(composite_1, SWT.BORDER);
		messageLogRefreshPeriodText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				try {
					int ivalue = Integer.parseInt(messageLogRefreshPeriodText.getText());
					messageLogRefresh = ivalue;
				} catch (Exception ex) {
					errorMessage("Invalid value entered");
					messageLogRefreshPeriodText.setText(Integer.toString(messageLogRowCount));
					ex.printStackTrace();
				}
			}
		});
		messageLogRefreshPeriodText.setText("60");

		Label lblNewLabel = new Label(composite_1, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Button refreshButton = new Button(composite_1, SWT.NONE);
		refreshButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				readMessageLogEntriesFromServer();
			}
		});
		GridData gd_refreshButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_refreshButton.widthHint = 120;
		refreshButton.setLayoutData(gd_refreshButton);
		refreshButton.setText("Refresh");

		Button download_button = new Button(composite_1, SWT.NONE);
		download_button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				FileDialog dialog = new FileDialog(shell, SWT.SAVE);
				dialog.setFilterNames(new String[] { "Log File (*.log)" });
				dialog.setFilterExtensions(new String[] { "*.log", "*.*" }); // Windows
				dialog.setFileName(tm1server.getName() +  "_tm1server.log");
				downloadLogFile(dialog.open(), 0);
			}
		});
		GridData gd_download_button = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_download_button.widthHint = 120;
		download_button.setLayoutData(gd_download_button);
		download_button.setText("Download");

		Composite composite = new Composite(this, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		serverMessageLogTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
		serverMessageLogTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		serverMessageLogTable.setHeaderVisible(true);
		serverMessageLogTable.setLinesVisible(true);

		TableColumn tblclmnNewColumn = new TableColumn(serverMessageLogTable, SWT.NONE);
		tblclmnNewColumn.setWidth(100);
		tblclmnNewColumn.setText("Thread");

		TableColumn tblclmnNewColumn_1 = new TableColumn(serverMessageLogTable, SWT.NONE);
		tblclmnNewColumn_1.setWidth(100);
		tblclmnNewColumn_1.setText("Time Stamp");

		TableColumn logger_column = new TableColumn(serverMessageLogTable, SWT.NONE);
		logger_column.setWidth(100);
		logger_column.setText("Logger");

		TableColumn tblclmnNewColumn_2 = new TableColumn(serverMessageLogTable, SWT.NONE);
		tblclmnNewColumn_2.setWidth(100);
		tblclmnNewColumn_2.setText("Level");

		TableColumn thread_column = new TableColumn(serverMessageLogTable, SWT.NONE);
		thread_column.setWidth(100);
		thread_column.setText("Message");

		serverMessageLogTable.addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(Event event) {
				Table table = (Table) event.widget;
				int columnCount = table.getColumnCount();
				if (columnCount == 0) return;
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


		messageLogRefreshTimer = new Runnable(){
			public void run(){
				display.timerExec(messageLogRefresh*1000, this);
				if (autoRefeshButton.getSelection()){
					readMessageLogEntriesFromServer();
				}
			}
		};
		display.timerExec(messageLogRefresh * 1000, messageLogRefreshTimer);
	}

	public void errorMessage(String message) {
		MessageBox m = new MessageBox(shell, SWT.ERROR);
		m.setMessage(message);
		m.open();
	}


	public void readMessageLogEntriesFromServer (){
		try {
			serverMessageLogTable.removeAll();
			String request = "MessageLogEntries/$count";
			String query = "";
			tm1server.get(request);
			int rowCount = Integer.parseInt(tm1server.response);
			int skip = rowCount - messageLogRowCount; 
			if (skip < 0) skip = 0;
			request = "MessageLogEntries";
			query = "$skip=" + skip;
			System.out.println("Skipping " + skip);
			//System.exit(-1);
			tm1server.get(request, query);
			OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
			JSONArray ja = jresponse.getJSONArray("value");
			for (int i=ja.length()-1; i>0; i--){
				String id = String.valueOf(ja.getJSONObject(i).getLong("ThreadID"));
				String timestamp = ja.getJSONObject(i).getString("TimeStamp");
				String logger = ja.getJSONObject(i).getString("Logger");
				String level = ja.getJSONObject(i).getString("Level");
				String message = ja.getJSONObject(i).getString("Message");
				TableItem tableItem = new TableItem(serverMessageLogTable, SWT.NONE);
				String[] tableItemText = { id, timestamp, logger, level, message }; 
				tableItem.setText(tableItemText);
			}
			for (TableColumn tc : serverMessageLogTable.getColumns()) tc.pack();
			serverMessageLogTable.redraw();




		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void downloadLogFile (String filename, int rows) {
		try {
			//logviewer_table.removeAll();
			String request = "MessageLogEntries/$count";
			tm1server.get(request);
			int skiprows = 0;
			if (rows != 0){
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
			for (int i=0; i<jlines.length(); i++){
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


		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void setTM1Server(TM1Server tm1server){
		this.tm1server = tm1server;
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
