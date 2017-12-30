package TM1Diagnostic.UI;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;
import org.apache.wink.json4j.OrderedJSONObject;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

public class RuleTracer {

	protected Display display;
	protected Shell shlTraceRules;

	private TM1Server tm1server;
	private TM1Cube cube;
	private String user;
	private String[] elementNames;
	private Text valueText;
	private Table rulesTable;
	private Group grpRules;
	private Group grpRuleComponents;
	private TableColumn tblclmnNewColumn;
	private Tree componentTree;
	private TreeColumn valueTreeColumn;
	private TreeColumn typeTreeColumn;
	private TreeColumn ruleTreeColumn;
	private TreeColumn cellTreeColumn;

	/**
	 * @wbp.parser.constructor
	 */

	public RuleTracer(Shell parent, TM1Server tm1server, TM1Cube cube, String[] elementNames){
		shlTraceRules = new Shell(parent, SWT.RESIZE | SWT.MAX | SWT.MIN);
		display = shlTraceRules.getDisplay();
		this.tm1server = tm1server;
		this.cube = cube;
		this.elementNames = elementNames;
		shlTraceRules.setSize(993, 582);
		shlTraceRules.setText("Trace Rules");
		createContents();
		shlTraceRules.layout();
		shlTraceRules.open();
	}

	public RuleTracer(Shell parent, TM1Server tm1server, TM1Cube cube, String user){
		shlTraceRules = new Shell(parent, SWT.RESIZE | SWT.MAX | SWT.MIN);
		display = shlTraceRules.getDisplay();
		this.tm1server = tm1server;
		this.cube = cube;
		shlTraceRules.setSize(993, 582);
		shlTraceRules.setText("Security Report");
		createContents();
		shlTraceRules.layout();
		shlTraceRules.open();
	}

	public RuleTracer(Shell parent, TM1Server tm1server, TM1Cube cube){
		shlTraceRules = new Shell(parent, SWT.RESIZE | SWT.MAX | SWT.MIN);
		display = shlTraceRules.getDisplay();
		this.tm1server = tm1server;
		this.cube = cube;
		shlTraceRules.setSize(993, 582);
		shlTraceRules.setText("Security Report");
		createContents();
		shlTraceRules.layout();
		shlTraceRules.open();
	}

	public RuleTracer(Shell parent, TM1Server tm1server){
		shlTraceRules = new Shell(parent, SWT.RESIZE | SWT.MAX | SWT.MIN);
		display = shlTraceRules.getDisplay();
		this.tm1server = tm1server;
		shlTraceRules.setSize(993, 582);
		shlTraceRules.setText("Security Report");
		createContents();
		shlTraceRules.layout();
		shlTraceRules.open();
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlTraceRules.setLayout(new GridLayout(1, false));

		Composite composite = new Composite(shlTraceRules, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		grpRules = new Group(composite, SWT.NONE);
		grpRules.setText("Rules");
		grpRules.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		grpRules.setLayout(new GridLayout(2, false));

		rulesTable = new Table(grpRules, SWT.BORDER | SWT.FULL_SELECTION);
		rulesTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		rulesTable.setLinesVisible(true);

		tblclmnNewColumn = new TableColumn(rulesTable, SWT.NONE);
		tblclmnNewColumn.setWidth(100);
		tblclmnNewColumn.setText("New Column");

		grpRuleComponents = new Group(composite, SWT.NONE);
		grpRuleComponents.setText("Rule Components");
		grpRuleComponents.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		grpRuleComponents.setLayout(new GridLayout(1, false));

		componentTree = new Tree(grpRuleComponents, SWT.BORDER);
		componentTree.setHeaderVisible(true);
		componentTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		valueTreeColumn = new TreeColumn(componentTree, SWT.NONE);
		valueTreeColumn.setWidth(100);
		valueTreeColumn.setText("Value");

		typeTreeColumn = new TreeColumn(componentTree, SWT.NONE);
		typeTreeColumn.setWidth(100);
		typeTreeColumn.setText("Type");

		ruleTreeColumn = new TreeColumn(componentTree, SWT.NONE);
		ruleTreeColumn.setWidth(100);
		ruleTreeColumn.setText("Rule");

		cellTreeColumn = new TreeColumn(componentTree, SWT.NONE);
		cellTreeColumn.setWidth(100);
		cellTreeColumn.setText("Cell");

		Label valueLabel = new Label(composite, SWT.NONE);
		valueLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		valueLabel.setText("Cell Value:");

		valueText = new Text(composite, SWT.BORDER);
		valueText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		readRuleTrace();		
	}

	/*
	 * 
{ "Tuple@odata.bind": [
	"Dimensions('Letters')/Hierarchies('Letters')/Elements('A')",
	"Dimensions('Lines')/Hierarchies('Lines')/Elements('1')"
	]
}
	 *
	 */

	private void readRuleTrace(){
		try {
			String request = cube.getEntity() + "/tm1.TraceCellCalculation";
			String query = "$expand=Tuple,Components/Tuple";
			OrderedJSONObject payload = new OrderedJSONObject();
			JSONArray jElements = new JSONArray();
			for (int i=0; i<elementNames.length; i++){
				jElements.put(elementNames[i]);
			}
			payload.put("Tuple@odata.bind", jElements);
			tm1server.post(request, query, payload);


			OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
			int value = jresponse.getInt("Value");
			valueText.setText(String.valueOf(value));

			JSONArray statmentJSON = jresponse.getJSONArray("Statements");
			for (int i=0; i<statmentJSON.size(); i++){
				//ruleText.append(statmentJSON.getString(i) + "\n");
				String ruleStatement = statmentJSON.getString(i) + "\n";
				TableItem t = new TableItem(rulesTable, SWT.NONE);
				t.setText(0, ruleStatement);
			}

			JSONArray componentsJSON = jresponse.getJSONArray("Components");
			for (int i=0; i<componentsJSON.size(); i++){

				TreeItem t = new TreeItem(componentTree, SWT.NONE);
				JSONObject componentJSON = componentsJSON.getJSONObject(i);
				String compValue = componentJSON.getString("Value");
				String compType = componentJSON.getString("Type");
				String compStatement = componentJSON.getString("Statements");
				t.setText(0, compValue);
				t.setText(1, compType);
				JSONArray ruleStatementJSON = componentJSON.getJSONArray("Statements");
				String ruleStatement = "";
				for (int j=0; j<ruleStatementJSON.size(); j++){
					ruleStatement = ruleStatement + ruleStatementJSON.getString(j) + " ";
				}
				t.setText(2, ruleStatement);

				JSONArray childComponentsJSON = componentJSON.getJSONArray("Components");
				for (int j=0; j<childComponentsJSON.size(); j++){
					readChildComponent(childComponentsJSON.getJSONObject(j), t);
				}
				if (!componentJSON.isNull("Tuple")){
					JSONArray tuplesJSON = componentJSON.getJSONArray("Tuple");
					String tupleString = "";
					for (int k=0; k<tuplesJSON.size(); k++){
						JSONObject elementJSON = tuplesJSON.getJSONObject(k);
						String elementName = elementJSON.getString("Name");
						tupleString = tupleString + elementName + " ";
					}
					System.out.println("Tuple " + tupleString);
					t.setText(3, tupleString);
				}
			}

			for (TableColumn tc : rulesTable.getColumns()) tc.pack();
			for (TreeColumn tc : componentTree.getColumns()) tc.pack();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void readChildComponent(JSONObject componentJSON, TreeItem parentItem) throws JSONException{
		//System.out.println(componentsJSON.toString());

		TreeItem t = new TreeItem(parentItem, SWT.NONE);
		String compValue = componentJSON.getString("Value");
		String compType = componentJSON.getString("Type");
		t.setText(0, compValue);
		t.setText(1, compType);
		JSONArray ruleStatementJSON = componentJSON.getJSONArray("Statements");
		String ruleStatement = "";
		for (int i=0; i<ruleStatementJSON.size(); i++){
			ruleStatement = ruleStatement + ruleStatementJSON.getString(i) + " ";
		}
		t.setText(2, ruleStatement);

		JSONArray childComponentsJSON = componentJSON.getJSONArray("Components");
		for (int j=0; j<childComponentsJSON.size(); j++){
			readChildComponent(childComponentsJSON.getJSONObject(j), t);
		}
		if (!componentJSON.isNull("Tuple")){
			JSONArray tuplesJSON = componentJSON.getJSONArray("Tuple");
			String tupleString = "";
			for (int i=0; i<tuplesJSON.size(); i++){
				JSONObject elementJSON = tuplesJSON.getJSONObject(i);
				String elementName = elementJSON.getString("Name");
				tupleString = tupleString + elementName + " ";
			}
			System.out.println("Tuple " + tupleString);
			t.setText(3, tupleString);
		}

	}


}
