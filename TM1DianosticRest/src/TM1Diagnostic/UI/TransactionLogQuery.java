package TM1Diagnostic.UI;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import TM1Diagnostic.TransactionQuery;
import TM1Diagnostic.REST.TM1Server;

import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.OrderedJSONObject;
import org.apache.wink.json4j.JSONArray;

public class TransactionLogQuery extends Dialog {

	protected boolean result;
	protected Shell shlTransactionQuery;

	private TM1Server tm1server;

	private Combo cubecombo;
	private Combo clientcombo;
	private DateTime fromdate;
	private DateTime todate;

	public String cubeFilter;
	public String userFilter;
	public String startDateTimeFilter;
	public String endDateTimeFilter;

	public TransactionLogQuery(Shell parent, TM1Server tm1server) {
		super(parent, SWT.DIALOG_TRIM);
		setText("SWT Dialog");
		this.tm1server = tm1server;

		cubeFilter = "";
		userFilter = "";
		startDateTimeFilter = "";
		endDateTimeFilter = "";

	}

	public boolean open() {
		result = false;
		createContents();
		shlTransactionQuery.open();
		shlTransactionQuery.layout();
		Display display = getParent().getDisplay();
		while (!shlTransactionQuery.isDisposed()) {
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
		shlTransactionQuery = new Shell(getParent(), getStyle());
		shlTransactionQuery.setSize(574, 380);
		shlTransactionQuery.setText("Transaction Query");
		shlTransactionQuery.setLayout(new FillLayout(SWT.HORIZONTAL));

		Composite composite = new Composite(shlTransactionQuery, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		Composite composite_2 = new Composite(composite, SWT.NONE);
		composite_2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		composite_2.setLayout(new GridLayout(3, false));

		Label lblFrom = new Label(composite_2, SWT.RIGHT);
		lblFrom.setText("From");
		new Label(composite_2, SWT.NONE);

		Label lblTo = new Label(composite_2, SWT.NONE);
		lblTo.setText("To");

		fromdate = new DateTime(composite_2, SWT.CALENDAR | SWT.BORDER);
		fromdate.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
		fromdate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					String from_string = fromdate.getYear() + "-" + fromdate.getMonth() + "-" + fromdate.getDay();
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
					Date from_dt = df.parse(from_string);
					String from_dt_string = df.format(from_dt) + "T00:00:00Z";
					startDateTimeFilter = from_dt_string;
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		todate = new DateTime(composite_2, SWT.CALENDAR | SWT.BORDER);
		todate.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		todate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					String to_string = todate.getYear() + "-" + todate.getMonth() + "-" + todate.getDay();
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
					Date to_dt = df.parse(to_string);
					String to_dt_string = df.format(to_dt) + "T00:00:00Z";
					endDateTimeFilter = to_dt_string;
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		Label lblNewLabel = new Label(composite_2, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("Client");

		clientcombo = new Combo(composite_2, SWT.READ_ONLY);
		clientcombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		clientcombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				userFilter = clientcombo.getText();
			}
		});

		Label lblNewLabel_1 = new Label(composite_2, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_1.setText("Cube");

		cubecombo = new Combo(composite_2, SWT.READ_ONLY);
		cubecombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		cubecombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				cubeFilter = cubecombo.getText();
			}
		});

		Composite composite_1 = new Composite(composite, SWT.NONE);
		composite_1.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, true, false, 1, 1));
		composite_1.setLayout(new GridLayout(4, false));

		Button btnNewButton = new Button(composite_1, SWT.NONE);
		GridData gd_btnNewButton = new GridData(SWT.CENTER, SWT.CENTER, false, false, 3, 1);
		gd_btnNewButton.widthHint = 140;
		btnNewButton.setLayoutData(gd_btnNewButton);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					result = true;
					shlTransactionQuery.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		btnNewButton.setText("Search");

		Button cancelButton = new Button(composite_1, SWT.NONE);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				result = false;
				shlTransactionQuery.close();
			}
		});
		GridData gd_cancelButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_cancelButton.widthHint = 140;
		cancelButton.setLayoutData(gd_cancelButton);
		cancelButton.setText("Cancel");

		pullclients();
		pullcubes();
	}

	public boolean pullclients() {
		try {
			clientcombo.add("*");
			clientcombo.select(0);

			String request = "Dimensions('}Clients')/DefaultHierarchy/Elements";
			String query = "$select=Attributes";
			tm1server.get(request, query);
				OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
				JSONArray jclients = jresponse.getJSONArray("value");
				for (int i = 0; i < jclients.length(); i++) {
					OrderedJSONObject client = (OrderedJSONObject) jclients.getJSONObject(i);
					OrderedJSONObject att = (OrderedJSONObject) client.getJSONObject("Attributes");
					clientcombo.add(att.getString("}TM1_DefaultDisplayValue"));
				}
				return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public boolean pullcubes() {
		try {
			cubecombo.add("*");
			cubecombo.select(0);
			String request = "Cubes";
			tm1server.get(request);
				OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
				JSONArray jcubes = jresponse.getJSONArray("value");
				for (int i = 0; i < jcubes.length(); i++) {
					OrderedJSONObject cube = (OrderedJSONObject) jcubes.getJSONObject(i);
					cubecombo.add(cube.getString("Name"));
				}
				return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
}
