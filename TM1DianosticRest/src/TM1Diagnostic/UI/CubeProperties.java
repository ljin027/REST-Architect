package TM1Diagnostic.UI;


import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.OrderedJSONObject;
import org.apache.wink.json4j.JSONArray;

import TM1Diagnostic.REST.TM1Chore;
import TM1Diagnostic.REST.TM1Cube;
import TM1Diagnostic.REST.TM1Server;

import org.eclipse.swt.widgets.Table;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.layout.FillLayout;

public class CubeProperties extends Dialog {

	protected boolean result;
	protected Shell shlCubeProperties;
	private TM1Cube cube;
	private TM1Server tm1server;
	private Table cubeproperty_table;
	private TableItem selecteditem;

	final int EDITABLECOLUMN = 1;


	public CubeProperties(Shell parent, TM1Cube cube) {
		super(parent, SWT.DIALOG_TRIM);
		setText("SWT Dialog");
		this.cube = cube;
	}

	public boolean open() {
		createContents();
		shlCubeProperties.open();
		shlCubeProperties.layout();
		Display display = getParent().getDisplay();
		while (!shlCubeProperties.isDisposed()) {
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
		shlCubeProperties = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.RESIZE);
		shlCubeProperties.setSize(653, 537);
		shlCubeProperties.setText("Cube Properties - " + cube.displayName);
		shlCubeProperties.setLayout(new FillLayout(SWT.HORIZONTAL));

		TableViewer tableViewer = new TableViewer(shlCubeProperties, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
		cubeproperty_table = tableViewer.getTable();
		cubeproperty_table.setHeaderVisible(true);

		cubeproperty_table.addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(Event event) {
				Table table = (Table)event.widget;
				int columnCount = table.getColumnCount();
				if(columnCount == 0) return;
				Rectangle area = table.getClientArea();
				int totalAreaWdith = area.width;
				int lineWidth = table.getGridLineWidth();
				int totalGridLineWidth = (columnCount-1)*lineWidth; 
				int totalColumnWidth = 0;
				for(TableColumn column: table.getColumns())
				{
					totalColumnWidth = totalColumnWidth+column.getWidth();
				}
				int diff = totalAreaWdith-(totalColumnWidth+totalGridLineWidth);
				TableColumn lastCol = table.getColumns()[columnCount-1];
				lastCol.setWidth(diff+lastCol.getWidth());
			}
		});

		cubeproperty_table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final TableEditor editor = new TableEditor(cubeproperty_table);               
				editor.horizontalAlignment = SWT.LEFT;
				editor.grabHorizontal = true;
				editor.minimumWidth = 50;
				Control oldEditor = editor.getEditor();
				if (oldEditor != null) oldEditor.dispose();                

				// Identify the selected row
				selecteditem = (TableItem) e.item;
				if (selecteditem == null) return;
				if (selecteditem.getText(0).equals("LOGGING") || selecteditem.getText(0).equals("DEMANDLOAD") || selecteditem.getText(0).equals("RULE_STATS") ){
					Combo newEditor = new Combo(cubeproperty_table, SWT.NONE);
					newEditor.add("");
					newEditor.add("YES");
					newEditor.add("NO");
					newEditor.setText(selecteditem.getText(EDITABLECOLUMN));
					newEditor.addModifyListener(new ModifyListener() {
						public void modifyText(ModifyEvent me) {
							if (me.getSource() instanceof Combo){
								update_cubeproperty(selecteditem.getText(0), ((Combo)me.getSource()).getText());
							}
						}
					});         
					newEditor.setFocus();           
					editor.setEditor(newEditor, selecteditem, EDITABLECOLUMN);      
				} else if (selecteditem.getText(0).equals("DATARESERVATIONMODE")){
					Combo newEditor = new Combo(cubeproperty_table, SWT.NONE);
					newEditor.add("");
					newEditor.add("ALLOWED");
					newEditor.add("REQUIRED");
					newEditor.add("REQUIREDSHARED");
					newEditor.setText(selecteditem.getText(EDITABLECOLUMN));
					newEditor.addModifyListener(new ModifyListener() {
						public void modifyText(ModifyEvent me) {
							if (me.getSource() instanceof Combo){
								update_cubeproperty(selecteditem.getText(0), ((Combo)me.getSource()).getText());
							}
						}
					});         
					newEditor.setFocus();           
					editor.setEditor(newEditor, selecteditem, EDITABLECOLUMN);   	            	
				}else if (selecteditem.getText(0).equals("MEASURES_DIMENSION")){
					Combo newEditor = new Combo(cubeproperty_table, SWT.NONE);
					for (int i=0; i<cube.dimensionCount(); i++){
						newEditor.add(cube.getDimension(i).displayName);
					}
					newEditor.setText(selecteditem.getText(EDITABLECOLUMN));
					newEditor.addModifyListener(new ModifyListener() {
						public void modifyText(ModifyEvent me) {
							if (me.getSource() instanceof Combo){
								update_cubeproperty(selecteditem.getText(0), ((Combo)me.getSource()).getText());
							}
						}
					});         
					newEditor.setFocus();           
					editor.setEditor(newEditor, selecteditem, EDITABLECOLUMN);      
				} else if (selecteditem.getText(0).equals("TIME_DIMENSION")){
					Combo newEditor = new Combo(cubeproperty_table, SWT.NONE);
					for (int i=0; i<cube.dimensionCount(); i++){
						newEditor.add(cube.getDimension(i).displayName);
					}
					newEditor.setText(selecteditem.getText(EDITABLECOLUMN));
					newEditor.addModifyListener(new ModifyListener() {
						public void modifyText(ModifyEvent me) {
							if (me.getSource() instanceof Combo){
								update_cubeproperty(selecteditem.getText(0), ((Combo)me.getSource()).getText());
							}
						}
					});         
					newEditor.setFocus();           
					editor.setEditor(newEditor, selecteditem, EDITABLECOLUMN);      
				} else{
					//Text newEditor = new Text(cubeproperty_table, SWT.NONE);
					//newEditor.setText(selecteditem.getText(EDITABLECOLUMN));
					//newEditor.setEnabled(false);
					/*newEditor.addModifyListener(new ModifyListener() {
		                public void modifyText(ModifyEvent me) {
		                    Text text = (Text) editor.getEditor();
		                    editor.getItem().setText(EDITABLECOLUMN, text.getText());
		                }
		            });*/         
					//newEditor.selectAll();
					//newEditor.setFocus();           
					//editor.setEditor(newEditor, selecteditem, EDITABLECOLUMN);      
				}
			}
		});     


		TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnNewColumn = tableViewerColumn.getColumn();
		tblclmnNewColumn.setWidth(219);
		tblclmnNewColumn.setText("Property");

		TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnNewColumn_1 = tableViewerColumn_1.getColumn();
		tblclmnNewColumn_1.setWidth(290);
		tblclmnNewColumn_1.setText("Value");

		pull_property_names();
		readPropertValuesFromServer();
	}


	private boolean update_cubeproperty(String property, String value){
		/*
		 * {"Cells":[
			{"Tuple@odata.bind": [
        	"Dimensions('}Cubes')/Hierarchies('}Cubes')/Elements('Sales')",
        	"Dimensions('}CubeProperties')/Hierarchies('}CubeProperties')/Elements('Logging')"]
			}],
    		"Value":"YES"
			}
		 */
		try {
			String request = "Cubes('%7DCubeProperties')/tm1.Update";
			String s_payload = "{\"Cells\":[{\"Tuple@odata.bind\":[\"Dimensions('}Cubes')/Hierarchies('}Cubes')/Elements('" + cube.displayName + "')\",\"Dimensions('}CubeProperties')/Hierarchies('}CubeProperties')/Elements('" + property + "')\"]}],\"Value\":\"" + value + "\"}";

			OrderedJSONObject payload = new OrderedJSONObject();
			JSONArray cells = new JSONArray();
			OrderedJSONObject tuplebind = new OrderedJSONObject();
			JSONArray tuples = new JSONArray();
			tuples.put("Dimensions('}Cubes')/Hierarchies('}Cubes')/Elements('" + cube.displayName + "')");
			tuples.put("Dimensions('}CubeProperties')/Hierarchies('}CubeProperties')/Elements('" + property + "')");
			tuplebind.put("Tuple@odata.bind", tuples);
			payload.put("Cells", cells);
			payload.put("Value", value);
			tm1server.post(request, payload);
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	private boolean pull_property_names(){
		try {
			String request = "Dimensions('%7DCubeProperties')/Hierarchies('%7DCubeProperties')/Elements";
			String query = "$select=Name";
			tm1server.get(request, query);
			OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
			JSONArray properties = jresponse.getJSONArray("value");
			for (int i=0; i<properties.length(); i++){
				String property_name = properties.getJSONObject(i).getString("Name");
				TableItem t = new TableItem(cubeproperty_table, SWT.None);
				t.setText(0, property_name);
			}
			return true;			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	private boolean readPropertValuesFromServer(){
		try {
			String request = "ExecuteMDX";
			String query = "$expand=Cells";
			OrderedJSONObject payload = new OrderedJSONObject();
			String mdxQuery = "SELECT {[}Cubes].[}Cubes].[" + cube.displayName + "]} on 0, ([}CubeProperties].members) on 1 FROM [}CubeProperties]";
			payload.put("MDX", mdxQuery);
			tm1server.post(request, query, payload);
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

}
