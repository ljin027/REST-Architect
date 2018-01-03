package TM1Diagnostic.UI;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.wink.json4j.JSONException;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Table;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.layout.FillLayout;

import TM1Diagnostic.CubeViewerPosition;
import TM1Diagnostic.REST.TM1Cell;
import TM1Diagnostic.REST.TM1Cube;
import TM1Diagnostic.REST.TM1Dimension;
import TM1Diagnostic.REST.TM1Element;
import TM1Diagnostic.REST.TM1Hierarchy;
import TM1Diagnostic.REST.TM1MDXView;
import TM1Diagnostic.REST.TM1RestException;
import TM1Diagnostic.REST.TM1Subset;
import TM1Diagnostic.REST.TM1View;
import TM1Diagnostic.REST.TM1ViewAxes;
import TM1Diagnostic.REST.TM1ViewBuilder;

import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.custom.StyledText;

public class UI_CubeMDXViewer extends Dialog {

	private String[] viewlist;

	static public int ROW = 0;
	static public int COLUMN = 1;
	static public int FILTER = 2;

	private static Image recalc_icon;
	private static Color HEADER;
	private static Color HEADER_EMPTY;
	private static Color RULE_CELL;
	private static Color CONSOLIDATED_CELL;
	private static Color UPDATED_CELL;

	protected Shell shell;
	private Table viewtable;
	private TableViewer tableViewer;
	private StyledText mdxText;
	private Label viewInfoLabel;

	private TM1Cube cube;
	private TM1MDXView mdxview;

	private int columncount;
	private int columnheadercount;
	private int totalcolumncount;

	private int rowcount;
	private int rowheadercount;
	private int totalrowcount;

	private int selectedrow;
	private int selectedcolumn;

	private Menu menuTable;

	/**
	 * @wbp.parser.constructor
	 */
	public UI_CubeMDXViewer(Shell parent, TM1Cube cube) {
		super(parent, SWT.DIALOG_TRIM);
		this.cube = cube;
		mdxview = new TM1MDXView(cube.name, cube.getServer());

	}

	public void open() {
		createContents();
		shell.open();
		shell.layout();

		Display display = shell.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell(getParent(), SWT.SHELL_TRIM | SWT.BORDER);
		shell.setSize(1001, 657);
		shell.setLayout(new GridLayout(1, false));

		Device device = Display.getCurrent();

		HEADER = shell.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND);
		HEADER_EMPTY = shell.getDisplay().getSystemColor(SWT.COLOR_WHITE);
		RULE_CELL = new Color(device, 165, 80, 100);
		CONSOLIDATED_CELL = new Color(device, 185, 185, 185);
		UPDATED_CELL = new Color(device, 10, 150, 30);

		recalc_icon = new Image(shell.getDisplay(), ".\\images\\icon_recalculate.gif");

		Menu menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);

		MenuItem file_menuitem = new MenuItem(menu, SWT.CASCADE);
		file_menuitem.setText("File");

		Menu file_menu = new Menu(file_menuitem);
		file_menuitem.setMenu(file_menu);

		MenuItem save_menuitem = new MenuItem(file_menu, SWT.NONE);
		save_menuitem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (true) {
					message_box("Error saving view", SWT.ERROR);
				}
			}
		});
		save_menuitem.setEnabled(false);
		save_menuitem.setText("Save");

		MenuItem saveas_menuitem = new MenuItem(file_menu, SWT.NONE);
		saveas_menuitem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (true) {
					message_box("Error saving view", SWT.ERROR);
				}
			}
		});
		saveas_menuitem.setEnabled(false);
		saveas_menuitem.setText("Save As");

		MenuItem close_menuitem = new MenuItem(file_menu, SWT.NONE);
		close_menuitem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				shell.close();
			}
		});
		close_menuitem.setText("Close");

		MenuItem options_menuitem = new MenuItem(menu, SWT.CASCADE);
		options_menuitem.setText("Options");

		Menu options_menu = new Menu(options_menuitem);
		options_menuitem.setMenu(options_menu);

		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_composite.heightHint = 206;
		composite.setLayoutData(gd_composite);

		mdxText = new StyledText(composite, SWT.V_SCROLL | SWT.BORDER);
		GridData gd_mdxText = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_mdxText.heightHint = 104;
		mdxText.setLayoutData(gd_mdxText);

		Button btnNewButton = new Button(composite, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					mdxview.setQuery(mdxText.getText());
					mdxview.executeMDXView();
					refreshView();
				} catch (Exception ex){

				}
			}
		});
		btnNewButton.setBounds(0, 0, 90, 30);
		btnNewButton.setText("Execute MDX");

		Composite composite_1 = new Composite(shell, SWT.NONE);
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		composite_1.setLayout(new FillLayout(SWT.HORIZONTAL));

		tableViewer = new TableViewer(composite_1, SWT.NONE | SWT.FULL_SELECTION);
		viewtable = tableViewer.getTable();
		viewtable.setLinesVisible(true);

		final TableEditor editor = new TableEditor(viewtable);
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		editor.minimumWidth = 50;

		// Create context menu
		menuTable = new Menu(viewtable);
		viewtable.setMenu(menuTable);

		// Create menu item
		MenuItem cellid_menuitem = new MenuItem(menuTable, SWT.NONE);
		cellid_menuitem.setText("Cell ID");
		cellid_menuitem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {

			}
		});

		MenuItem ordinal_menuitem = new MenuItem(menuTable, SWT.NONE);
		ordinal_menuitem.setText("Edit Status");
		ordinal_menuitem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {

			}
		});

		viewtable.addListener(SWT.MouseDown, new Listener() {
			@Override
			public void handleEvent(Event e) {
				Point pt = new Point(e.x, e.y);
				TableItem item = viewtable.getItem(pt);
				if (item == null)
					return;
				for (int i = 0; i < viewtable.getColumnCount(); i++) {
					Rectangle rect = item.getBounds(i);
					if (rect.contains(pt)) {
						selectedrow = viewtable.indexOf(item);
						selectedcolumn = i;
						if (i >= columnheadercount && selectedrow >= rowheadercount) {
							if (e.button == 3) {
								// Right click menu on all cells
								menuTable.setVisible(true);
							} else {
								// Clean up any previous editor control
								Control oldEditor = editor.getEditor();
								if (oldEditor != null)
									oldEditor.dispose();
								TM1Cell cell = get_ordinal(selectedrow, selectedcolumn);
								if (!cell.consolidated && !cell.rulederived) {
									Text newEditor = new Text(viewtable, SWT.NONE);
									newEditor.setText(item.getText(selectedcolumn));
									newEditor.addModifyListener(new ModifyListener() {
										public void modifyText(ModifyEvent e) {
											Text text = (Text) editor.getEditor();
											editor.getItem().setText(selectedcolumn, text.getText());
											editor.getItem().setForeground(selectedcolumn, UPDATED_CELL);
											TM1Cell cell = get_ordinal(selectedrow, selectedcolumn);
											cell.updated = true;
											cell.value = text.getText();
										}
									});
									newEditor.addListener(SWT.MouseDown, new Listener() {
										@Override
										public void handleEvent(Event e2) {
											if (e2.button == 3) {
												menuTable.setVisible(true);
											}
										}
									});
									newEditor.selectAll();
									newEditor.setFocus();
									editor.setEditor(newEditor, item, selectedcolumn);
								}
							}
						}
					}
				}
			}
		});

		Composite footer = new Composite(shell, SWT.NONE);
		footer.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 2, 1));
		footer.setLayout(new FillLayout(SWT.HORIZONTAL));

		viewInfoLabel = new Label(footer, SWT.NONE);


		try {
			mdxText.setText(defaultMDXQuery());
		} catch (TM1RestException | URISyntaxException | IOException | JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	// SELECT 
	//   [}Cubes].members ON COLUMNS, 
	//   [}CubeProperties].members ON ROWS 
	//   FROM [}CubeProperties]

	public String defaultMDXQuery() throws TM1RestException, ClientProtocolException, URISyntaxException, IOException, JSONException {
		String mdx = "SELECT \n";
		cube.readCubeDimensionsFromServer();

		int dimcount = cube.dimensionCount();
		TM1Dimension dimension;
		TM1Hierarchy hierarchy;

		dimension = cube.getDimension(0);
		hierarchy = dimension.getDefaultHierarchy();

		String column = "[" + dimension.name + "].members ON COLUMNS, \n";
		mdx = mdx.concat(column);

		dimension = cube.getDimension(1);
		hierarchy = dimension.getDefaultHierarchy();

		String row = "[" + dimension.name + "].members ON ROWS \nFROM [" + cube.name + "] \n";
		mdx = mdx.concat(row);

		if (dimcount > 2){
			mdx = mdx.concat("WHERE ( \n");
			for (int i=2; i<dimcount; i++){
				dimension = cube.getDimension(i);
				hierarchy = dimension.getDefaultHierarchy();
				TM1Element defaultElement;
				if (hierarchy.readDefaultElementFromServer()){
					defaultElement = hierarchy.getDefaultElement();
					String filter = "[" + dimension.name + "].[" + defaultElement.name + "]";
					String postFilter = " )";
					if (i != dimcount - 1){
						postFilter = ", \n";
					}
					mdx = mdx.concat(filter);
					mdx = mdx.concat(postFilter);

				}
			}
		}
		return mdx;

	}

	public void message_box(String message, int type) {
		MessageBox m = new MessageBox(shell, type);
		m.setMessage(message);
		m.open();
	}

	public void showEditStatus(TM1Cell tm1cell) {
		if (tm1cell.rulederived) {
			message_box("Cell is rule derived", SWT.NONE);
		} else if (tm1cell.consolidated) {
			message_box("Cell is consolidated", SWT.NONE);
		} else {
			message_box("Cell is editable", SWT.NONE);
		}
	}

	public TM1Cell get_ordinal(int row, int column) {
		int ordinal = ((row - rowheadercount) * (columncount)) + (column - columnheadercount);
		return mdxview.getCell(ordinal);
	}

	public void findCellLocation(int row, int column) {
		String intersections = "";
		TM1ViewAxes columnsaxes = mdxview.getColumnAxes();
		TM1ViewAxes rowsaxes = mdxview.getrowaxes();
		for (int i = 0; i < rowheadercount; i++) {
			String colheader = columnsaxes.tuples.get(column - columnheadercount).members.get(i).name;
			intersections = intersections.concat(mdxview.getcolumn(i).dimension.name + ":" + colheader + "\n");
		}
		for (int i = 0; i < columnheadercount; i++) {
			String rowheader = rowsaxes.tuples.get(row - rowheadercount).members.get(i).name;
			intersections = intersections.concat(mdxview.getrow(i).dimension.name + ":" + rowheader + "\n");
		}
		for (int i = 0; i < mdxview.getfilterscount(); i++) {
			intersections = intersections.concat(mdxview.getfilter(i).dimension.name + ":" + mdxview.getfilter(i).element.name + "\n");
		}
		message_box(intersections, SWT.NONE);
	}

	public void refreshView() {

		viewtable.setRedraw(false);
		viewtable.clearAll();
		while (viewtable.getItemCount() > 0) {
			viewtable.getItem(0).dispose();
		}
		while (viewtable.getColumnCount() > 0) {
			viewtable.getColumns()[0].dispose();
		}

		TM1ViewAxes columnsaxes = mdxview.getColumnAxes();
		TM1ViewAxes rowsaxes = mdxview.getrowaxes();

		columncount = columnsaxes.tuples.size();
		if (rowsaxes.tuples.size() > 0) {
			columnheadercount = rowsaxes.tuples.get(0).members.size();
		} else {
			columnheadercount = 0;
		}
		totalcolumncount = columncount + columnheadercount;

		rowcount = rowsaxes.tuples.size();
		if (columnsaxes.tuples.size() > 0) {
			rowheadercount = columnsaxes.tuples.get(0).members.size();
		} else {
			rowheadercount = 0;
		}
		totalrowcount = rowcount + rowheadercount;

		// Create all columns
		for (int i = 0; i < totalcolumncount; i++) {
			TableColumn column = new TableColumn(viewtable, SWT.BORDER);
			column.setWidth(100);
		}

		// Create all rows
		for (int i = 0; i < totalrowcount; i++) {
			TableItem row = new TableItem(viewtable, SWT.BORDER);
		}

		for (int i = 0; i < rowheadercount; i++) {
			for (int j = 0; j < columnheadercount; j++) {
				TableItem row = viewtable.getItem(i);
				row.setBackground(j, HEADER);
			}
		}

		String lastcolheader = "";
		for (int i = 0; i < rowheadercount; i++) {
			TableItem row = viewtable.getItem(i);
			for (int j = columnheadercount; j < totalcolumncount; j++) {
				String colheader = columnsaxes.tuples.get(j - columnheadercount).members.get(i).name;
				String membertype = columnsaxes.tuples.get(j - columnheadercount).members.get(i).type;
				if (membertype.equals("Consolidated")) {
					row.setText(j, "-- " + colheader);
				} else {
					row.setText(j, colheader);
				}
				row.setBackground(j, HEADER);
			}
		}

		for (int i = 0; i < columnheadercount; i++) {
			String lastrowheader = "";
			for (int j = rowheadercount; j < totalrowcount; j++) {
				TableItem row = viewtable.getItem(j);
				String rowheader = rowsaxes.tuples.get(j - rowheadercount).members.get(i).name;
				String membertype = rowsaxes.tuples.get(j - rowheadercount).members.get(i).type;
				if (membertype.equals("Consolidated")) {
					row.setText(i, "-- " + rowheader);
				} else {
					row.setText(i, rowheader);
				}
				row.setBackground(i, HEADER);
			}
		}

		if (mdxview.cellCount() == 0) {
			message_box("No values", SWT.NONE);
		} else {
			//message_box("Cell count " + mdxview.cellCount(), SWT.NONE);
			int ordinal = 0;
			for (int i = rowheadercount; i < totalrowcount; i++) {
				TableItem row = viewtable.getItem(i);
				for (int j = columnheadercount; j < totalcolumncount; j++) {
					TM1Cell cell = mdxview.getCell(ordinal);
					//System.out.println(cell.formattedvalue);
					row.setText(j, (String) cell.formattedvalue);
					if (cell.consolidated) {
						row.setBackground(j, CONSOLIDATED_CELL);
					}
					if (cell.rulederived) {
						row.setBackground(j, RULE_CELL);
					}
					ordinal++;
				}
			}
		}

		viewInfoLabel.setText("Rows " + (totalrowcount - rowheadercount) + "   Columns " + (totalcolumncount - columnheadercount));
		viewtable.setRedraw(true);

	}
}
