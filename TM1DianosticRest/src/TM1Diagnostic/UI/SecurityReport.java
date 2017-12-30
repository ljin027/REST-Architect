package TM1Diagnostic.UI;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;
import org.apache.wink.json4j.OrderedJSONObject;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.TableColumn;

import TM1Diagnostic.REST.TM1Cell;
import TM1Diagnostic.REST.TM1Cube;
import TM1Diagnostic.REST.TM1Dimension;
import TM1Diagnostic.REST.TM1Element;
import TM1Diagnostic.REST.TM1Hierarchy;
import TM1Diagnostic.REST.TM1RestException;
import TM1Diagnostic.REST.TM1Server;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.custom.SashForm;

public class SecurityReport {

	protected Display display;
	protected Shell shell;

	private TM1Server tm1server;
	private TM1Cube cube;
	private String user;
	private String[] elementNames;

	private Combo clientsCombo;
	private Combo cubesCombo;
	private Table dimensionsTable;
	private StyledText securityReportText;

	/**
	 * @wbp.parser.constructor
	 */

	public SecurityReport(Shell parent, TM1Server tm1server, TM1Cube cube, String[] elementNames) {
		shell = new Shell(parent, SWT.RESIZE | SWT.MAX | SWT.MIN);
		display = shell.getDisplay();
		this.tm1server = tm1server;
		this.cube = cube;
		this.elementNames = elementNames;
		shell.setSize(993, 582);
		shell.setText("Security Report");
		createContents();
		shell.layout();
		shell.open();
	}

	public SecurityReport(Shell parent, TM1Server tm1server, TM1Cube cube, String user) {
		shell = new Shell(parent, SWT.RESIZE | SWT.MAX | SWT.MIN);
		display = shell.getDisplay();
		this.tm1server = tm1server;
		this.cube = cube;
		shell.setSize(993, 582);
		shell.setText("Security Report");
		createContents();
		shell.layout();
		shell.open();
	}

	public SecurityReport(Shell parent, TM1Server tm1server, TM1Cube cube) {
		shell = new Shell(parent, SWT.RESIZE | SWT.MAX | SWT.MIN);
		display = shell.getDisplay();
		this.tm1server = tm1server;
		this.cube = cube;
		shell.setSize(993, 582);
		shell.setText("Security Report");
		createContents();
		shell.layout();
		shell.open();
	}

	public SecurityReport(Shell parent, TM1Server tm1server) {
		shell = new Shell(parent, SWT.RESIZE | SWT.MAX | SWT.MIN);
		display = shell.getDisplay();
		this.tm1server = tm1server;
		shell.setSize(993, 582);
		shell.setText("Security Report");
		createContents();
		shell.layout();
		shell.open();
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell.setLayout(new GridLayout(1, false));

		SashForm sashForm = new SashForm(shell, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		Composite composite = new Composite(sashForm, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));

		Label clientLabel = new Label(composite, SWT.NONE);
		clientLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));
		clientLabel.setText("Client");

		clientsCombo = new Combo(composite, SWT.NONE);
		clientsCombo.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		Label cubeLabel = new Label(composite, SWT.NONE);
		cubeLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));
		cubeLabel.setBounds(0, 0, 70, 20);
		cubeLabel.setText("Cube");

		cubesCombo = new Combo(composite, SWT.NONE);
		readCubesFromServer();

		dimensionsTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
		dimensionsTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		dimensionsTable.setHeaderVisible(true);
		dimensionsTable.setLinesVisible(true);

		dimensionsTable.addListener(SWT.MouseDoubleClick, new Listener() {
			public void handleEvent(Event event) {
				try {
					if (dimensionsTable.getSelectionCount() == 0) {
						// Nothing is selected so do nothing
					} else {
						String dimensionName = dimensionsTable.getSelection()[0].getText(0);
						String elementName = dimensionsTable.getSelection()[0].getText(1);
						TM1Dimension dimension = new TM1Dimension(dimensionName, tm1server);
						SubsetEditor editor = new SubsetEditor(shell, dimension, elementName);
						editor.open();
						String selectedElement = editor.getSelectedElement();
						dimensionsTable.getSelection()[0].setText(1, selectedElement);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		TableColumn tblclmnNewColumn = new TableColumn(dimensionsTable, SWT.NONE);
		tblclmnNewColumn.setWidth(100);
		tblclmnNewColumn.setText("Dimension");

		TableColumn tblclmnNewColumn_1 = new TableColumn(dimensionsTable, SWT.NONE);
		tblclmnNewColumn_1.setWidth(100);
		tblclmnNewColumn_1.setText("Element");

		if (cube != null) {
			cubesCombo.setText(cube.displayName);
			readDimensions();
		} else {
			cubesCombo.select(0);
			readDimensions();
		}

		Button btnNewButton = new Button(composite, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				generateSecurityReport();
			}
		});
		btnNewButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		btnNewButton.setText("Run Security Report");
		cubesCombo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent event0) {
				readDimensions();
			}
		});

		cubesCombo.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		Composite composite_1 = new Composite(sashForm, SWT.NONE);
		composite_1.setLayout(new GridLayout(1, false));

		securityReportText = new StyledText(composite_1, SWT.BORDER | SWT.V_SCROLL);
		securityReportText.setAlwaysShowScrollBars(false);
		securityReportText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		sashForm.setWeights(new int[] { 1, 1 });

		readClientsFromServer();
		clientsCombo.select(0);
		readCubesFromServer();
	}

	public boolean readClientsFromServer() {
		try {
			String request = "Dimensions('}Clients')/DefaultHierarchy/Elements";
			String query = "$select=Attributes";
			tm1server.get(request, query);
			OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
			JSONArray jclients = jresponse.getJSONArray("value");
			for (int i = 0; i < jclients.length(); i++) {
				OrderedJSONObject client = (OrderedJSONObject) jclients.getJSONObject(i);
				OrderedJSONObject att = (OrderedJSONObject) client.getJSONObject("Attributes");
				clientsCombo.add(att.getString("}TM1_DefaultDisplayValue"));
			}
			return true;

		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public boolean readCubesFromServer() {
		try {
			String request = "Cubes";
			tm1server.get(request);
			OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
			JSONArray jcubes = jresponse.getJSONArray("value");
			for (int i = 0; i < jcubes.length(); i++) {
				OrderedJSONObject cube = (OrderedJSONObject) jcubes.getJSONObject(i);
				cubesCombo.add(cube.getString("Name"));
			}
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public boolean readDimensions() {
		try {
			dimensionsTable.removeAll();
			String cubeName = cubesCombo.getText();
			String request = "Cubes('" + cubeName + "')/Dimensions";
			tm1server.get(request);
			OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
			JSONArray jDimensions = jresponse.getJSONArray("value");
			TM1Dimension dimension;
			String dimensionName;
			for (int i = 0; i < jDimensions.length(); i++) {
				OrderedJSONObject dimensionJSON = (OrderedJSONObject) jDimensions.getJSONObject(i);
				TableItem t = new TableItem(dimensionsTable, i);
				dimensionName = dimensionJSON.getString("Name");
				t.setText(0, dimensionName);
				dimension = new TM1Dimension(dimensionName, tm1server);
				TM1Hierarchy hierarchy = dimension.getDefaultHierarchy();
				hierarchy.readDefaultElementFromServer();
				if (elementNames != null) {
					t.setText(1, elementNames[i]);
				} else {
					TM1Element element = hierarchy.getDefaultElement();
					t.setText(1, element.displayName);
				}
			}
			resizeColumns();
			return true;

		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	private void resizeColumns() {
		for (int i = 0, n = dimensionsTable.getColumnCount(); i < n; i++)
			dimensionsTable.getColumn(i).pack();
	}

	private void generateSecurityReport() {
		try {
			securityReportText.setText("");
			String client = clientsCombo.getText();
			String cube = cubesCombo.getText();

			// List Group Memberships
			StyleRange header1Sytle = new StyleRange();
			header1Sytle.start = securityReportText.getCharCount();
			String header1 = "1 - Groups Memberships for User " + client + "\n";
			header1Sytle.length = header1.length();
			header1Sytle.fontStyle = SWT.BOLD;
			securityReportText.append(header1);
			securityReportText.setStyleRange(header1Sytle);
			List<String> groups = getGroups(client);
			for (int i = 0; i < groups.size(); i++) {
				securityReportText.append(groups.get(i) + "\n");
			}
			securityReportText.append("\n");

			// List Cube Security
			String leastCubeSecurity = "NONE";
			// securityReportText.append("Cube Security by Group:\n");
			StyleRange header2Sytle = new StyleRange();
			header2Sytle.start = securityReportText.getCharCount();
			String header2 = "2 - Cube Security\n";
			header2Sytle.length = header2.length();
			header2Sytle.fontStyle = SWT.BOLD;
			securityReportText.append(header2);
			securityReportText.setStyleRange(header2Sytle);

			for (int i = 0; i < groups.size(); i++) {
				String group = groups.get(i);
				String cubeSecurity = getCubeSecurity(group, cube);
				if (cubeSecurity.isEmpty())
					cubeSecurity = "NONE";
				if (cubeSecurity.equals("READ") & leastCubeSecurity.equals("NONE"))
					leastCubeSecurity = "READ";
				if (cubeSecurity.equals("WRITE") && (leastCubeSecurity.equals("READ") || leastCubeSecurity.equals("NONE")))
					leastCubeSecurity = "WRITE";
				securityReportText.append(group + " has " + cubeSecurity + " on Cube " + cube + "\n");
			}
			StyleRange cubeSecurityStyle = new StyleRange();
			cubeSecurityStyle.start = securityReportText.getCharCount();
			cubeSecurityStyle.length = leastCubeSecurity.length();
			if (leastCubeSecurity.equals("NONE"))
				cubeSecurityStyle.foreground = display.getSystemColor(SWT.COLOR_RED);
			if (leastCubeSecurity.equals("READ"))
				cubeSecurityStyle.foreground = display.getSystemColor(SWT.COLOR_YELLOW);
			if (leastCubeSecurity.equals("WRITE"))
				cubeSecurityStyle.foreground = display.getSystemColor(SWT.COLOR_GREEN);
			securityReportText.append(leastCubeSecurity + " access on cube " + cube + "\n");
			securityReportText.setStyleRange(cubeSecurityStyle);
			securityReportText.append("\n");

			// List Dimension Security
			// securityReportText.append("Dimension Security by Group:\n");
			StyleRange header3Sytle = new StyleRange();
			header3Sytle.start = securityReportText.getCharCount();
			String header3 = "3 - Dimension Security\n";
			header3Sytle.length = header3.length();
			header3Sytle.fontStyle = SWT.BOLD;
			securityReportText.append(header3);
			securityReportText.setStyleRange(header3Sytle);
			for (int i = 0; i < dimensionsTable.getItemCount(); i++) {
				String dimensionName = dimensionsTable.getItem(i).getText(0);
				String leastDimensionSecurity = "NONE";
				for (int j = 0; j < groups.size(); j++) {
					String groupName = groups.get(j);
					String dimensionSecurity = getDimensionSecurity(groupName, dimensionName);
					if (dimensionSecurity.isEmpty())
						dimensionSecurity = "NONE";
					if (dimensionSecurity.equals("READ") & leastDimensionSecurity.equals("NONE"))
						leastDimensionSecurity = "READ";
					if (dimensionSecurity.equals("WRITE") && (leastDimensionSecurity.equals("READ") || leastDimensionSecurity.equals("NONE")))
						leastDimensionSecurity = "WRITE";
					securityReportText.append(groupName + " has " + dimensionSecurity + " on Dimensions " + dimensionName + "\n");
				}
				StyleRange dimensionSecurityStyle = new StyleRange();
				dimensionSecurityStyle.start = securityReportText.getCharCount();
				dimensionSecurityStyle.length = leastDimensionSecurity.length();
				if (leastDimensionSecurity.equals("NONE"))
					dimensionSecurityStyle.foreground = display.getSystemColor(SWT.COLOR_RED);
				if (leastDimensionSecurity.equals("READ"))
					dimensionSecurityStyle.foreground = display.getSystemColor(SWT.COLOR_YELLOW);
				if (leastDimensionSecurity.equals("WRITE"))
					dimensionSecurityStyle.foreground = display.getSystemColor(SWT.COLOR_GREEN);
				securityReportText.append(leastDimensionSecurity + " access on dimension " + dimensionName + "\n");
				securityReportText.setStyleRange(dimensionSecurityStyle);
				securityReportText.append("\n");
			}

			// Element Security
			// securityReportText.append("Element Security by Group:\n");
			StyleRange header4Sytle = new StyleRange();
			header4Sytle.start = securityReportText.getCharCount();
			String header4 = "4 - Element Security\n";
			header4Sytle.length = header4.length();
			header4Sytle.fontStyle = SWT.BOLD;
			securityReportText.append(header4);
			securityReportText.setStyleRange(header4Sytle);
			for (int i = 0; i < dimensionsTable.getItemCount(); i++) {
				String dimensionName = dimensionsTable.getItem(i).getText(0);
				String elementName = dimensionsTable.getItem(i).getText(1);
				if (checkServerForElementSecurity(dimensionName)) {
					String leastElementSecurity = "NONE";
					for (int j = 0; j < groups.size(); j++) {
						String groupName = groups.get(j);
						String elementSecurity = getElementSecurity(groupName, dimensionName, elementName);
						if (elementSecurity.isEmpty())
							elementSecurity = "NONE";
						if (elementSecurity.equals("READ") & leastElementSecurity.equals("NONE"))
							leastElementSecurity = "READ";
						if (elementSecurity.equals("WRITE") && (leastElementSecurity.equals("READ") || leastElementSecurity.equals("NONE")))
							leastElementSecurity = "WRITE";
						securityReportText.append(groupName + " has " + elementSecurity + " on element " + dimensionName + ":" + elementName + "\n");
					}
					StyleRange elementSecurityStyle = new StyleRange();
					elementSecurityStyle.start = securityReportText.getCharCount();
					elementSecurityStyle.length = leastElementSecurity.length();
					if (leastElementSecurity.equals("NONE"))
						elementSecurityStyle.foreground = display.getSystemColor(SWT.COLOR_RED);
					if (leastElementSecurity.equals("READ"))
						elementSecurityStyle.foreground = display.getSystemColor(SWT.COLOR_YELLOW);
					if (leastElementSecurity.equals("WRITE"))
						elementSecurityStyle.foreground = display.getSystemColor(SWT.COLOR_GREEN);
					securityReportText.append(leastElementSecurity + " access on element " + dimensionName + ":" + elementName + "\n");
					securityReportText.setStyleRange(elementSecurityStyle);
				} else {
					StyleRange noElementSecurityStyle = new StyleRange();
					String noElementSecurity = "No element security on dimension " + dimensionName + "\n";
					noElementSecurityStyle.start = securityReportText.getCharCount();
					noElementSecurityStyle.length = noElementSecurity.length();
					noElementSecurityStyle.foreground = display.getSystemColor(SWT.COLOR_GREEN);
					securityReportText.append(noElementSecurity);
					securityReportText.setStyleRange(noElementSecurityStyle);
				}
				securityReportText.append("\n");
			}

			// Cell Security
			// securityReportText.append("Cell Security by Group:\n");
			StyleRange header5Sytle = new StyleRange();
			header5Sytle.start = securityReportText.getCharCount();
			String header5 = "5 - Cell Security\n";
			header5Sytle.length = header5.length();
			header5Sytle.fontStyle = SWT.BOLD;
			securityReportText.append(header5);
			securityReportText.setStyleRange(header5Sytle);
			if (checkServerForCellSecurity(cube)) {
				String leastCellSecurity = "NONE";
				for (int j = 0; j < groups.size(); j++) {
					String groupName = groups.get(j);
					String cellSecurity = getCellSecurity(groupName, cube);
					if (cellSecurity.isEmpty())
						cellSecurity = "NONE";
					if (cellSecurity.equals("READ") & leastCellSecurity.equals("NONE"))
						leastCellSecurity = "READ";
					if (cellSecurity.equals("WRITE") && (leastCellSecurity.equals("READ") || leastCellSecurity.equals("NONE")))
						leastCellSecurity = "WRITE";
					securityReportText.append(groupName + " has " + cellSecurity + " on cube " + cube + "\n");
				}
				StyleRange cellSecurityStyle = new StyleRange();
				cellSecurityStyle.start = securityReportText.getCharCount();
				cellSecurityStyle.length = leastCellSecurity.length();
				if (leastCellSecurity.equals("NONE"))
					cellSecurityStyle.foreground = display.getSystemColor(SWT.COLOR_RED);
				if (leastCellSecurity.equals("READ"))
					cellSecurityStyle.foreground = display.getSystemColor(SWT.COLOR_YELLOW);
				if (leastCellSecurity.equals("WRITE"))
					cellSecurityStyle.foreground = display.getSystemColor(SWT.COLOR_GREEN);
				securityReportText.append(leastCellSecurity + " access on cube " + cube + "\n");
				securityReportText.setStyleRange(cellSecurityStyle);
			} else {
				StyleRange noCellSecurityStyle = new StyleRange();
				String noCellSecurity = "No cell security on cube " + cube + "\n";
				noCellSecurityStyle.start = securityReportText.getCharCount();
				noCellSecurityStyle.length = noCellSecurity.length();
				noCellSecurityStyle.foreground = display.getSystemColor(SWT.COLOR_GREEN);
				securityReportText.append(noCellSecurity);
				securityReportText.setStyleRange(noCellSecurityStyle);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private List<String> getGroups(String client) {
		List<String> groups = new ArrayList<String>();
		try {
			String request = "ExecuteMDXSetExpression";
			String query = "$expand=Tuples($expand=Members($select=Name))";
			OrderedJSONObject payload = new OrderedJSONObject();
			String mdxQuery = "{FILTER( {TM1FILTERBYLEVEL( {TM1SUBSETALL( [}Groups] )}, 0)}, [}ClientGroups].([}Clients].[" + client + "]) <> '' )}";
			payload.put("MDX", mdxQuery);
			tm1server.post(request, query, payload);
			OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
			JSONArray tuplesJSON = jresponse.getJSONArray("Tuples");
			for (int i = 0; i < tuplesJSON.size(); i++) {
				JSONObject tupleJSON = tuplesJSON.getJSONObject(i);
				JSONArray membersJSON = tupleJSON.getJSONArray("Members");
				for (int j = 0; j < membersJSON.size(); j++) {
					JSONObject memberJSON = membersJSON.getJSONObject(j);
					String groupName = memberJSON.getString("Name");
					groups.add(groupName);
				}
			}
			return groups;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return groups;
		}
	}

	private String getCubeSecurity(String group, String cubeName) {
		String cubeSecurity = "";
		try {
			String request = "ExecuteMDX";
			String query = "$expand=Cells";
			OrderedJSONObject payload = new OrderedJSONObject();
			String mdxQuery = "SELECT {[}Groups].[" + group + "]} on 0, {[}Cubes].[" + cubeName + "]} ON 1  FROM [}CubeSecurity]";
			payload.put("MDX", mdxQuery);
			tm1server.post(request, query, payload);
			OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
			JSONArray cellsJSON = jresponse.getJSONArray("Cells");
			JSONObject cellJSON = cellsJSON.getJSONObject(0);
			cubeSecurity = cellJSON.getString("Value");
			return cubeSecurity;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return cubeSecurity;
		}
	}

	private String getDimensionSecurity(String group, String dimensionName) {
		String dimensionSecurity = "";
		try {
			String request = "ExecuteMDX";
			String query = "$expand=Cells";
			OrderedJSONObject payload = new OrderedJSONObject();
			String mdxQuery = "SELECT {[}Groups].[" + group + "]} on 0, {[}Dimensions].[" + dimensionName + "]} ON 1  FROM [}DimensionSecurity]";
			payload.put("MDX", mdxQuery);
			tm1server.post(request, query, payload);
			OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
			JSONArray cellsJSON = jresponse.getJSONArray("Cells");
			JSONObject cellJSON = cellsJSON.getJSONObject(0);
			dimensionSecurity = cellJSON.getString("Value");
			return dimensionSecurity;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return dimensionSecurity;
		}
	}

	private String getElementSecurity(String group, String dimensionName, String elementName) {
		String elementSecurity = "";
		try {
			String request = "ExecuteMDX";
			String query = "$expand=Cells";
			OrderedJSONObject payload = new OrderedJSONObject();
			String mdxQuery = "SELECT {[}Groups].[" + group + "]} on 0, {[" + dimensionName + "].[" + elementName + "]} ON 1 FROM [}ElementSecurity_" + dimensionName + "]";
			payload.put("MDX", mdxQuery);
			tm1server.post(request, query, payload);
			OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
			JSONArray cellsJSON = jresponse.getJSONArray("Cells");
			JSONObject cellJSON = cellsJSON.getJSONObject(0);
			elementSecurity = cellJSON.getString("Value");
			return elementSecurity;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return elementSecurity;
		}
	}

	private String getCellSecurity(String group, String cubeName) {
		String cellSecurity = "";
		List<String> cellSecurityDimensions = new ArrayList<String>();
		try {
			String request = "Cubes('}CellSecurity_" + cubeName + "')/Dimensions";
			String query = "$select=Name";
			tm1server.get(request, query);
			JSONObject jresponse = new JSONObject(tm1server.response);
			JSONArray dimensionsJSON = jresponse.getJSONArray("value");
			for (int i = 0; i < dimensionsJSON.size(); i++) {
				JSONObject dimensionJSON = dimensionsJSON.getJSONObject(i);
				cellSecurityDimensions.add(dimensionJSON.getString("Name"));
			}
			request = "ExecuteMDX";
			query = "$expand=Cells";
			OrderedJSONObject payload = new OrderedJSONObject();
			// "SELECT {[}Groups].[BUDGET REVIEWER]} * {[plan_department].[200]} ON 0 FROM [}CellSecurity_plan_BudgetPlan]"
			String mdx = "SELECT {[}Groups].[" + group + "]}";
			for (int i = 0; i < cellSecurityDimensions.size(); i++) {
				String lookupDimensioName = cellSecurityDimensions.get(i);
				String elementNameLookup = "";
				for (int j = 0; j < dimensionsTable.getItemCount(); j++) {
					String tableRowDimensioName = dimensionsTable.getItem(j).getText(0);
					if (lookupDimensioName.equals(tableRowDimensioName)) {
						elementNameLookup = dimensionsTable.getItem(j).getText(1);
						mdx = mdx + " * {[" + lookupDimensioName + "].[" + elementNameLookup + "]}";
					}
				}
			}
			mdx = mdx + " ON 0 FROM [}CellSecurity_" + cubeName + "]";
			payload.put("MDX", mdx);
			tm1server.post(request, query, payload);
			jresponse = new OrderedJSONObject(tm1server.response);
			JSONArray cellsJSON = jresponse.getJSONArray("Cells");
			JSONObject cellJSON = cellsJSON.getJSONObject(0);
			cellSecurity = cellJSON.getString("Value");
			return cellSecurity;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return cellSecurity;
		}
	}

	public boolean checkServerForElementSecurity(String dimensionName) throws ClientProtocolException, URISyntaxException, IOException, TM1RestException {
		TM1Dimension dimension = new TM1Dimension(dimensionName, tm1server);
		return dimension.checkServerForElementSecurity();
	}

	public boolean checkServerForCellSecurity(String cubeName) throws ClientProtocolException, TM1RestException, URISyntaxException, IOException {
		TM1Cube cube = new TM1Cube(cubeName, tm1server);
		return cube.checkServerForCellSecurity();
	}

}
