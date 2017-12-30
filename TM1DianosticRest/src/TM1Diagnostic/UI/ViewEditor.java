package TM1Diagnostic.UI;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.wink.json4j.JSONException;
import org.eclipse.swt.widgets.Control;
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
//import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.layout.FillLayout;

import TM1Diagnostic.CubeViewerPosition;
import TM1Diagnostic.REST.TM1Cell;
import TM1Diagnostic.REST.TM1Cube;
import TM1Diagnostic.REST.TM1Dimension;
import TM1Diagnostic.REST.TM1Hierarchy;
import TM1Diagnostic.REST.TM1RestException;
import TM1Diagnostic.REST.TM1Subset;
import TM1Diagnostic.REST.TM1View;
import TM1Diagnostic.REST.TM1ViewAxes;

import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;

public class ViewEditor {

	protected Object result;
	public Display display;
	protected Shell shell;

	private String[] viewlist;

	static public int ROW = 0;
	static public int COLUMN = 1;
	static public int FILTER = 2;

	//private static Image recalc_icon;
	private static Image RESIZE_ICON;

	private static Color HEADER;
	//private static Color HEADER_EMPTY;
	private static Color RULE_CELL;
	private static Color CONSOLIDATED_CELL;
	private static Color UPDATED_CELL;

	private Table viewtable;

	private Button rowSuppressButton;
	private Button columnSuppressButton;

	private TM1Cube cube;
	private TM1View view;
	private Label viewInfoLabel;
	private Combo sandbox_combo;

	private Menu menuTable;
	private MenuItem mntmTraceRules;

	private Group columns_group;
	private Group rows_group;
	private Group titlesGroup;

	private int columncount;
	private int columnheadercount;
	private int totalcolumncount;

	private int rowcount;
	private int rowheadercount;
	private int totalrowcount;

	private int selectedrow;
	private int selectedcolumn;

	private boolean pendingSave;

	private Table rowsTable;
	private Table columnsTable;
	private Table titlesTable;
	private boolean suppressheader;

	public ViewEditor(Shell parent, TM1Cube cube, TM1View view) throws TM1RestException, ClientProtocolException, URISyntaxException, IOException, JSONException {
		this.cube = cube;
		this.view = view;
		shell = new Shell(parent, SWT.RESIZE | SWT.MAX | SWT.MIN);
		shell.setSize(860, 600);
		shell.setText("Cubeviewer - " + cube.displayName + "/" + view.displayName);
		display = shell.getDisplay();
		createContents();
		shell.layout();
		shell.open();
	}

	/**
	 * @throws IOException 
	 * @throws URISyntaxException 
	 * @throws ClientProtocolException 
	 * @throws JSONException 
	 * @wbp.parser.constructor
	 */
	public ViewEditor(Shell parent, TM1Cube cube) throws TM1RestException, ClientProtocolException, URISyntaxException, IOException, JSONException {
		this.cube = cube;
		shell = new Shell(parent, SWT.RESIZE | SWT.MAX | SWT.MIN);
		shell.setSize(860, 600);
		shell.setText("Cubeviewer - *New View");
		display = shell.getDisplay();
		createContents();
		shell.layout();
		shell.open();
	}


	/**
	 * Create contents of the dialog.
	 * @throws IOException 
	 * @throws URISyntaxException 
	 * @throws ClientProtocolException 
	 * @throws JSONException 
	 */
	private void createContents() throws TM1RestException, ClientProtocolException, URISyntaxException, IOException, JSONException {

		if (view != null) {
		} else {
			if (!cube.checkServerForDefaultView()) {
				cube.createDefaultPrivateView();
				view = new TM1View("Default", cube, cube.getServer(), TM1View.PRIVATE);
			} else {
				view = new TM1View("Default", cube, cube.getServer(), TM1View.PRIVATE);
			}
		}

		Device device = Display.getCurrent();

		HEADER = device.getSystemColor(SWT.COLOR_TITLE_BACKGROUND);
		RULE_CELL = new Color(device, 165, 80, 100);
		CONSOLIDATED_CELL = new Color(device, 185, 185, 185);
		UPDATED_CELL = new Color(device, 10, 150, 30);

		RESIZE_ICON = new Image(device, ".\\images\\icon_table.gif");
		shell.setLayout(new GridLayout(1, false));


		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		composite.setLayout(new GridLayout(8, false));

		Label lblNewLabel = new Label(composite, SWT.NONE);
		lblNewLabel.setAlignment(SWT.RIGHT);
		lblNewLabel.setText("View");

		Combo combo = new Combo(composite, SWT.NONE);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		viewlist = new String[cube.viewCount()];
		for (int i = 0; i < cube.viewCount(); i++) {
			viewlist[i] = cube.getview(i).displayName;
		}
		combo.setItems(viewlist);
		if (view != null) {
			combo.setText(view.displayName);
		} else {
			combo.setText("New view");
		}

		rowSuppressButton = new Button(composite, SWT.CHECK);
		rowSuppressButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					view.setRowSuppression(rowSuppressButton.getSelection());
				} catch (JSONException | TM1RestException | URISyntaxException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		rowSuppressButton.setText("Row Suppress");

		columnSuppressButton = new Button(composite, SWT.CHECK);
		columnSuppressButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					view.setColumnSuppression(columnSuppressButton.getSelection());
				} catch (JSONException | TM1RestException | URISyntaxException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		columnSuppressButton.setText("Column Suppress");

		Button resizeColumnsButton = new Button(composite, SWT.NONE);
		resizeColumnsButton.setToolTipText("Auto Resize Columns");
		resizeColumnsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				resizeColumns();
			}
		});
		resizeColumnsButton.setImage(RESIZE_ICON);

		Button recalcbutton = new Button(composite, SWT.NONE);
		recalcbutton.setToolTipText("Recalculate View");
		recalcbutton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		recalcbutton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					view.writeCellValueUpdatesToServer();
					updateViewFromUI();
					view.updateViewToServer();
					refreshViewHeaders();
					refreshView();
				} catch (TM1RestException | URISyntaxException | IOException | JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		Button btnCheckButton = new Button(composite, SWT.CHECK);
		btnCheckButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnCheckButton.setText("Auto-Recalc");

		Label lblNewLabel_2 = new Label(composite, SWT.NONE);
		lblNewLabel_2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_2.setText("Sandbox");

		sandbox_combo = new Combo(composite, SWT.NONE);
		sandbox_combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 6, 1));

		Button btnNewButton = new Button(composite, SWT.NONE);
		btnNewButton.setEnabled(false);
		btnNewButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnNewButton.setText("Commit");

		Composite dimension_order = new Composite(shell, SWT.NONE);
		dimension_order.setLayout(new GridLayout(5, false));
		dimension_order.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		rows_group = new Group(dimension_order, SWT.NONE);
		rows_group.setLayout(new FillLayout(SWT.HORIZONTAL));
		rows_group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		rows_group.setText("Rows");

		rowsTable = new Table(rows_group, SWT.BORDER | SWT.FULL_SELECTION);
		rowsTable.addListener(SWT.Resize, new Listener() {

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

		Label label = new Label(dimension_order, SWT.SEPARATOR | SWT.VERTICAL);

		columns_group = new Group(dimension_order, SWT.NONE);
		columns_group.setLayout(new FillLayout(SWT.VERTICAL));
		columns_group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		columns_group.setText("Columns");

		columnsTable = new Table(columns_group, SWT.BORDER | SWT.FULL_SELECTION);
		columnsTable.addListener(SWT.Resize, new Listener() {

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

		Label label_1 = new Label(dimension_order, SWT.SEPARATOR | SWT.VERTICAL);

		titlesGroup = new Group(dimension_order, SWT.NONE);
		titlesGroup.setLayout(new FillLayout(SWT.VERTICAL));
		titlesGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		titlesGroup.setText("Filter");

		titlesTable = new Table(titlesGroup, SWT.BORDER | SWT.FULL_SELECTION);
		titlesTable.addListener(SWT.Resize, new Listener() {

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

		rowsTable.addListener(SWT.MouseDoubleClick, new Listener() {
			public void handleEvent(Event event) {
				try {
					if (rowsTable.getSelectionCount() == 0) {
						// Nothing is selected so do nothing
					} else {

						CubeViewerPosition cpos = (CubeViewerPosition) rowsTable.getSelection()[0].getData();
						TM1Subset subset = cpos.subset;
						subset.readElementListFromServer();
						SubsetEditor editor = new SubsetEditor(shell, subset);
						editor.open();
					}
				} catch (TM1RestException | URISyntaxException | IOException | JSONException e){
					// fill in
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		columnsTable.addListener(SWT.MouseDoubleClick, new Listener() {
			public void handleEvent(Event event) {
				if (columnsTable.getSelectionCount() > 0) {
					try {
						CubeViewerPosition cpos = (CubeViewerPosition) columnsTable.getSelection()[0].getData();
						TM1Subset subset = cpos.subset;
						subset.readElementListFromServer();
						SubsetEditor editor = new SubsetEditor(shell, subset);
						editor.open();
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});

		titlesTable.addListener(SWT.MouseDoubleClick, new Listener() {
			public void handleEvent(Event event) {
				if (titlesTable.getSelectionCount() > 0) {
					try {
						CubeViewerPosition cpos = (CubeViewerPosition) titlesTable.getSelection()[0].getData();
						TM1Subset subset = cpos.subset;
						subset.readElementListFromServer();
						SubsetEditor editor = new SubsetEditor(shell, subset);
						editor.open();
					} catch (Exception e1){
						e1.printStackTrace();
					}
				}
			}
		});

		final List<CubeViewerPosition> transferDimensionPositions = new ArrayList<CubeViewerPosition>();
		Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
		DragSource rowSource = new DragSource(rowsTable, DND.DROP_MOVE);
		rowSource.setTransfer(types);

		rowSource.addDragListener(new DragSourceAdapter() {
			public void dragSetData(DragSourceEvent event) {
				// Get the selected items in the drag source
				DragSource ds = (DragSource) event.widget;
				Table table = (Table) ds.getControl();
				TableItem selection = table.getSelection()[0];
				StringBuffer buff = new StringBuffer();
				buff.append(selection.getText());
				event.data = buff.toString();

				if (selection.getData() instanceof CubeViewerPosition) {
					//System.out.println("Dragged item is CubeViewerPosition");
					transferDimensionPositions.add((CubeViewerPosition) selection.getData());
				} else {
					//System.out.println("Dragged item was not CubeViewerPosition");
				}
			}

			public void dragFinished(DragSourceEvent event) {
				if (event.detail == DND.DROP_MOVE) {
					//DragSource ds = (DragSource) event.widget;
					//Table table = (Table) ds.getControl();
					rowsTable.remove(rowsTable.getSelectionIndex());
				}
			}
		});

		DragSource columnSource = new DragSource(columnsTable, DND.DROP_MOVE);
		columnSource.setTransfer(types);
		columnSource.addDragListener(new DragSourceAdapter() {
			public void dragSetData(DragSourceEvent event) {
				// Get the selected items in the drag source
				DragSource ds = (DragSource) event.widget;
				Table table = (Table) ds.getControl();
				TableItem selection = table.getSelection()[0];
				StringBuffer buff = new StringBuffer();
				buff.append(selection.getText());
				event.data = buff.toString();
				if (selection.getData() instanceof CubeViewerPosition) {
					//System.out.println("Dragged item is CubeViewerPosition");
					transferDimensionPositions.add((CubeViewerPosition) selection.getData());
				} else {
					//System.out.println("Dragged item was not CubeViewerPosition");
				}
			}

			public void dragFinished(DragSourceEvent event) {
				// If a move operation has been performed, remove the data
				// from the source
				if (event.detail == DND.DROP_MOVE) {
					DragSource ds = (DragSource) event.widget;
					columnsTable.remove(columnsTable.getSelectionIndex());
				}
			}
		});

		DragSource titleSource = new DragSource(titlesTable, DND.DROP_MOVE);
		titleSource.setTransfer(types);
		titleSource.addDragListener(new DragSourceAdapter() {
			public void dragSetData(DragSourceEvent event) {
				// Get the selected items in the drag source
				DragSource ds = (DragSource) event.widget;
				Table table = (Table) ds.getControl();
				TableItem selection = table.getSelection()[0];
				StringBuffer buff = new StringBuffer();
				buff.append(selection.getText());
				event.data = buff.toString();

				if (selection.getData() instanceof CubeViewerPosition) {
					//System.out.println("Dragged item is CubeViewerPosition");
					transferDimensionPositions.add((CubeViewerPosition) selection.getData());
				} else {
					//System.out.println("Dragged item was not CubeViewerPosition");
				}
			}

			public void dragFinished(DragSourceEvent event) {
				// If a move operation has been performed, remove the data
				// from the source
				if (event.detail == DND.DROP_MOVE) {
					//DragSource ds = (DragSource) event.widget;
					//Table table = (Table) ds.getControl();
					titlesTable.remove(titlesTable.getSelectionIndex());
				}
			}
		});

		DropTarget rowsTarget = new DropTarget(rowsTable, DND.DROP_MOVE);
		rowsTarget.setTransfer(types);

		TableColumn tblclmnNewColumn = new TableColumn(rowsTable, SWT.NONE);
		tblclmnNewColumn.setWidth(100);
		//tblclmnNewColumn.setText("New Column");

		TableColumn tblclmnNewColumn_1 = new TableColumn(rowsTable, SWT.NONE);
		tblclmnNewColumn_1.setWidth(100);
		//tblclmnNewColumn_1.setText("New Column");
		rowsTarget.addDropListener(new DropTargetAdapter() {

			public void dragEnter(DropTargetEvent event) {
				if (event.detail == DND.DROP_DEFAULT) {
					event.detail = (event.operations & DND.DROP_COPY) != 0 ? DND.DROP_COPY : DND.DROP_NONE;
				}
				// Allow dropping text only
				for (int i = 0, n = event.dataTypes.length; i < n; i++) {
					if (TextTransfer.getInstance().isSupportedType(event.dataTypes[i])) {
						event.currentDataType = event.dataTypes[i];
					}
				}
			}

			public void dragOver(DropTargetEvent event) {
				// System.out.println("dragOver ");
				event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
				if (event.item != null) {
					TableItem item = (TableItem) event.item;
					Point pt = display.map(null, rowsTable, event.x, event.y);
					Rectangle bounds = item.getBounds();
					if (pt.y < bounds.y + bounds.height / 3) {
						event.feedback |= DND.FEEDBACK_INSERT_BEFORE;
					} else if (pt.y > bounds.y + 2 * bounds.height / 3) {
						event.feedback |= DND.FEEDBACK_INSERT_AFTER;
					} else {
						event.feedback |= DND.FEEDBACK_SELECT;
					}
				}
			}

			public void drop(DropTargetEvent event) {
				if (TextTransfer.getInstance().isSupportedType(event.currentDataType)) {
					// Get the dropped data
					DropTarget target = (DropTarget) event.widget;
					//String data = (String) event.data;
					Table table = (Table) target.getControl();
					TableItem newItem;
					if (event.item != null) {
						TableItem item = (TableItem) event.item;
						//Display display = shlCubeViewer.getDisplay();
						Point pt = display.map(null, rowsTable, event.x, event.y);
						Rectangle bounds = item.getBounds();

						TableItem[] tableItems = rowsTable.getItems();
						int index = 0;
						for (int i = 0; i < tableItems.length; i++) {
							if (tableItems[i] == item) {
								index = i;
								break;
							}
						}

						if (pt.y < bounds.y + bounds.height / 3) {
							newItem = new TableItem(rowsTable, SWT.NONE, index);
						} else if (pt.y > bounds.y + 2 * bounds.height / 3) {
							newItem = new TableItem(rowsTable, SWT.NONE, index + 1);
						} else {
							newItem = new TableItem(rowsTable, SWT.NONE);
						}
					} else {
						newItem = new TableItem(rowsTable, SWT.NONE);
					}
					CubeViewerPosition cubeViewerPosition = transferDimensionPositions.get(0);
					newItem.setText(0, cubeViewerPosition.dimensionName());
					newItem.setText(1, cubeViewerPosition.subsetName());
					newItem.setData(cubeViewerPosition);
					for (TableColumn tc : rowsTable.getColumns()) tc.pack();
					table.redraw();
					transferDimensionPositions.clear();
				}
			}
		});

		DropTarget columnsTarget = new DropTarget(columnsTable, DND.DROP_MOVE);
		columnsTarget.setTransfer(types);

		TableColumn tblclmnNewColumn_2 = new TableColumn(columnsTable, SWT.NONE);
		tblclmnNewColumn_2.setWidth(100);
		tblclmnNewColumn_2.setText("New Column");

		TableColumn tblclmnNewColumn_3 = new TableColumn(columnsTable, SWT.NONE);
		tblclmnNewColumn_3.setWidth(100);
		tblclmnNewColumn_3.setText("New Column");
		columnsTarget.addDropListener(new DropTargetAdapter() {
			public void dragEnter(DropTargetEvent event) {
				if (event.detail == DND.DROP_DEFAULT) {
					event.detail = (event.operations & DND.DROP_COPY) != 0 ? DND.DROP_COPY : DND.DROP_NONE;
				}

				// Allow dropping text only
				for (int i = 0, n = event.dataTypes.length; i < n; i++) {
					if (TextTransfer.getInstance().isSupportedType(event.dataTypes[i])) {
						event.currentDataType = event.dataTypes[i];
					}
				}
			}

			public void dragOver(DropTargetEvent event) {
				// System.out.println("dragOver ");
				event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
				if (event.item != null) {

					TableItem item = (TableItem) event.item;
					Point pt = display.map(null, rowsTable, event.x, event.y);
					Rectangle bounds = item.getBounds();
					if (pt.y < bounds.y + bounds.height / 3) {
						event.feedback |= DND.FEEDBACK_INSERT_BEFORE;
					} else if (pt.y > bounds.y + 2 * bounds.height / 3) {
						event.feedback |= DND.FEEDBACK_INSERT_AFTER;
					} else {
						event.feedback |= DND.FEEDBACK_SELECT;
					}
				}
			}

			public void drop(DropTargetEvent event) {
				if (TextTransfer.getInstance().isSupportedType(event.currentDataType)) {
					// Get the dropped data
					DropTarget target = (DropTarget) event.widget;
					//String data = (String) event.data;
					Table table = (Table) target.getControl();
					TableItem newItem;
					if (event.item != null) {
						TableItem item = (TableItem) event.item;
						//Display display = shlCubeViewer.getDisplay();
						Point pt = display.map(null, columnsTable, event.x, event.y);
						Rectangle bounds = item.getBounds();

						TableItem[] tableItems = columnsTable.getItems();
						int index = 0;
						for (int i = 0; i < tableItems.length; i++) {
							if (tableItems[i] == item) {
								index = i;
								break;
							}
						}
						if (pt.y < bounds.y + bounds.height / 3) {
							newItem = new TableItem(columnsTable, SWT.NONE, index);
						} else if (pt.y > bounds.y + 2 * bounds.height / 3) {
							newItem = new TableItem(columnsTable, SWT.NONE, index + 1);
						} else {
							newItem = new TableItem(columnsTable, SWT.NONE);
						}
					} else {
						newItem = new TableItem(columnsTable, SWT.NONE);
					}
					CubeViewerPosition cubeViewerPosition = transferDimensionPositions.get(0);
					newItem.setText(0, cubeViewerPosition.dimensionName());
					newItem.setText(1, cubeViewerPosition.subsetName());
					newItem.setData(cubeViewerPosition);
					for (TableColumn tc : columnsTable.getColumns()) tc.pack();
					table.redraw();
					transferDimensionPositions.clear();
				}
			}
		});

		DropTarget titlesTarget = new DropTarget(titlesTable, DND.DROP_MOVE);
		titlesTarget.setTransfer(types);

		TableColumn tblclmnNewColumn_4 = new TableColumn(titlesTable, SWT.NONE);
		tblclmnNewColumn_4.setWidth(100);
		tblclmnNewColumn_4.setText("New Column");

		TableColumn tblclmnNewColumn_5 = new TableColumn(titlesTable, SWT.NONE);
		tblclmnNewColumn_5.setWidth(100);
		tblclmnNewColumn_5.setText("New Column");
		titlesTarget.addDropListener(new DropTargetAdapter() {
			public void dragEnter(DropTargetEvent event) {
				if (event.detail == DND.DROP_DEFAULT) {
					event.detail = (event.operations & DND.DROP_COPY) != 0 ? DND.DROP_COPY : DND.DROP_NONE;
				}

				// Allow dropping text only
				for (int i = 0, n = event.dataTypes.length; i < n; i++) {
					if (TextTransfer.getInstance().isSupportedType(event.dataTypes[i])) {
						event.currentDataType = event.dataTypes[i];
					}
				}
			}

			public void dragOver(DropTargetEvent event) {
				// System.out.println("dragOver ");
				event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
				if (event.item != null) {
					Display display = shell.getDisplay();
					TableItem item = (TableItem) event.item;
					Point pt = display.map(null, titlesTable, event.x, event.y);
					Rectangle bounds = item.getBounds();
					if (pt.y < bounds.y + bounds.height / 3) {
						event.feedback |= DND.FEEDBACK_INSERT_BEFORE;
					} else if (pt.y > bounds.y + 2 * bounds.height / 3) {
						event.feedback |= DND.FEEDBACK_INSERT_AFTER;
					} else {
						event.feedback |= DND.FEEDBACK_SELECT;
					}
				}
			}

			public void drop(DropTargetEvent event) {
				try {
					if (TextTransfer.getInstance().isSupportedType(event.currentDataType)) {
						// Get the dropped data
						DropTarget target = (DropTarget) event.widget;
						//String data = (String) event.data;
						Table table = (Table) target.getControl();
						TableItem newItem;
						if (event.item != null) {
							TableItem item = (TableItem) event.item;
							//Display display = shlCubeViewer.getDisplay();
							Point pt = display.map(null, titlesTable, event.x, event.y);
							Rectangle bounds = item.getBounds();

							TableItem[] tableItems = titlesTable.getItems();
							int index = 0;
							for (int i = 0; i < tableItems.length; i++) {
								if (tableItems[i] == item) {
									index = i;
									break;
								}
							}
							if (pt.y < bounds.y + bounds.height / 3) {
								newItem = new TableItem(titlesTable, SWT.NONE, index);
							} else if (pt.y > bounds.y + 2 * bounds.height / 3) {
								newItem = new TableItem(titlesTable, SWT.NONE, index + 1);
							} else {
								newItem = new TableItem(titlesTable, SWT.NONE);
							}
						} else {
							newItem = new TableItem(titlesTable, SWT.NONE);
						}
						CubeViewerPosition cubeViewerPosition = transferDimensionPositions.get(0);
						newItem.setText(0, cubeViewerPosition.dimensionName());
						newItem.setText(1, cubeViewerPosition.elementName());
						newItem.setData(cubeViewerPosition);
						for (TableColumn tc : titlesTable.getColumns()) tc.pack();
						table.redraw();
						transferDimensionPositions.clear();
					}
				} catch (Exception e1){
					e1.printStackTrace();
				}
			}
		});

		Composite composite_1 = new Composite(shell, SWT.NONE);
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		composite_1.setLayout(new FillLayout(SWT.HORIZONTAL));

		viewtable = new Table(composite_1, SWT.NONE | SWT.FULL_SELECTION); //tableViewer.getTable();
		viewtable.setHeaderVisible(true);
		viewtable.setLinesVisible(true);

		final TableEditor editor = new TableEditor(viewtable);
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		editor.minimumWidth = 50;

		// Create context menu
		menuTable = new Menu(viewtable);
		menuTable.addMenuListener(new MenuAdapter() {
			@Override
			public void menuShown(MenuEvent e) {
				showCellMenu();
			}
		});
		viewtable.setMenu(menuTable);

		MenuItem cellid_menuitem = new MenuItem(menuTable, SWT.NONE);
		cellid_menuitem.setText("Cell ID");
		cellid_menuitem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				// System.out.println(selectedrow + " " + selectedcolumn);
				findCellLocation(selectedrow, selectedcolumn);
			}
		});

		MenuItem ordinal_menuitem = new MenuItem(menuTable, SWT.NONE);
		ordinal_menuitem.setText("Edit Status");
		ordinal_menuitem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				showEditStatus(get_ordinal(selectedrow, selectedcolumn));
			}
		});

		MenuItem mntmSecurityReport = new MenuItem(menuTable, SWT.NONE);
		mntmSecurityReport.setText("Security Report");
		mntmSecurityReport.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				securityReport(selectedrow, selectedcolumn);
			}
		});
		

		mntmTraceRules = new MenuItem(menuTable, SWT.NONE);
		mntmTraceRules.setText("Trace Rules");
		mntmTraceRules.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				traceRules(selectedrow, selectedcolumn);
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

		Menu menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);

		MenuItem mntmFile = new MenuItem(menu, SWT.CASCADE);
		mntmFile.setText("File");

		Menu menu_1 = new Menu(mntmFile);
		mntmFile.setMenu(menu_1);

		MenuItem mntmSave = new MenuItem(menu_1, SWT.NONE);
		mntmSave.setText("Save");

		MenuItem mntmSaveAs = new MenuItem(menu_1, SWT.NONE);
		mntmSaveAs.setText("Save As");

		MenuItem mntmClose = new MenuItem(menu_1, SWT.NONE);
		mntmClose.setText("Close");

		if (view != null) {
			view.readSandboxesFromServer();
			set_sandboxes();
			view.readSuppressionFromServer();
			set_suppressions();
			refreshViewHeaders();
			refreshView();
		}

	}


	public void infoMessage(String message) {
		MessageBox m = new MessageBox(shell, SWT.ICON_INFORMATION);
		m.setMessage(message);
		m.open();
	}

	public void errorMessage(String code, String message) {
		MessageBox m = new MessageBox(shell, SWT.ERROR);
		m.setMessage("Error code " + code + "\n" + message);
		m.open();
	}

	public void set_suppressions() {
		rowSuppressButton.setSelection(view.suppressEmptyRows);
		columnSuppressButton.setSelection(view.suppressEmptyColumns);
	}

	public void showEditStatus(TM1Cell tm1cell) {
		if (tm1cell.rulederived) {
			infoMessage("Rule derived");
		} else if (tm1cell.consolidated) {
			infoMessage("Consolidated");
		} else {
			infoMessage("Editable");
		}
	}
	
	public boolean isRuleDerived(TM1Cell tm1cell){
		if (tm1cell.rulederived) return true;
		return false;
	}

	public TM1Cell get_ordinal(int row, int column) {
		int ordinal = ((row - rowheadercount) * (columncount)) + (column - columnheadercount);
		return view.getCell(ordinal);

	}

	public void securityReport(int row, int column){
		int dimensionCount = cube.dimensionCount();
		String[] elementNames = new String[dimensionCount];
		for (int i=0; i<dimensionCount; i++){
			TM1Dimension dimension = cube.getDimension(i);
			elementNames[i] = findElementAtCellLocation(row, column, dimension.displayName);
		}
		SecurityReport securityReportWindow = new SecurityReport(shell, cube.getServer(), cube, elementNames);
	}
	
	private void traceRules(int row, int column){
		int dimensionCount = cube.dimensionCount();
		String[] elementNames = new String[dimensionCount];
		for (int i=0; i<dimensionCount; i++){
			TM1Dimension dimension = cube.getDimension(i);
			elementNames[i] = findElementEntityAtCellLocation(row, column, dimension.displayName);
		}
		RuleTracer ruleTrace = new RuleTracer(shell, cube.getServer(), cube, elementNames);
	}

	public String findElementAtCellLocation(int row, int column, String dimensionName){
		String elementName = "";
		TM1ViewAxes columnsaxes = view.getColumnAxes();
		TM1ViewAxes rowsaxes = view.getrowaxes();
		for (int i = 0; i < rowheadercount; i++) {
			//String colheader = columnsaxes.tuples.get(column - columnheadercount).members.get(i).name;
			if(view.getColumnDimensionPosition(i).dimension.displayName.equals(dimensionName)){
				elementName = columnsaxes.tuples.get(column - columnheadercount).members.get(i).name;
			}
		}
		for (int i = 0; i < columnheadercount; i++) {
			//String rowheader = rowsaxes.tuples.get(row - rowheadercount).members.get(i).name;
			if(view.getRowDimensionPosition(i).dimension.displayName.equals(dimensionName)){
				elementName = rowsaxes.tuples.get(row - rowheadercount).members.get(i).name;
			}
		}
		for (int i = 0; i < view.getfilterscount(); i++) {
			//intersections = intersections.concat(view.getTitleDimensionPosition(i).dimension.displayName + ":" + view.getTitleDimensionPosition(i).element.displayName + "\n");
			if (view.getTitleDimensionPosition(i).dimension.displayName.equals(dimensionName)) {
				elementName = view.getTitleDimensionPosition(i).element.displayName;
			}
		}
		return elementName;
	}
	
	public String findElementEntityAtCellLocation(int row, int column, String dimensionName){
		String elementName = "";
		TM1ViewAxes columnsaxes = view.getColumnAxes();
		TM1ViewAxes rowsaxes = view.getrowaxes();
		for (int i = 0; i < rowheadercount; i++) {
			//String colheader = columnsaxes.tuples.get(column - columnheadercount).members.get(i).name;
			if(view.getColumnDimensionPosition(i).dimension.displayName.equals(dimensionName)){
				elementName = "Dimensions('" + dimensionName + "')/" + "Hierarchies('" + dimensionName + "')/" + "Elements('" + columnsaxes.tuples.get(column - columnheadercount).members.get(i).name + "')";
			}
		}
		for (int i = 0; i < columnheadercount; i++) {
			//String rowheader = rowsaxes.tuples.get(row - rowheadercount).members.get(i).name;
			if(view.getRowDimensionPosition(i).dimension.displayName.equals(dimensionName)){
				elementName = "Dimensions('" + dimensionName + "')/" + "Hierarchies('" + dimensionName + "')/" + "Elements('" + rowsaxes.tuples.get(row - rowheadercount).members.get(i).name + "')";
			}
		}
		for (int i = 0; i < view.getfilterscount(); i++) {
			//intersections = intersections.concat(view.getTitleDimensionPosition(i).dimension.displayName + ":" + view.getTitleDimensionPosition(i).element.displayName + "\n");
			if (view.getTitleDimensionPosition(i).dimension.displayName.equals(dimensionName)) {
				elementName = "Dimensions('" + dimensionName + "')/" + "Hierarchies('" + dimensionName + "')/" + "Elements('" + view.getTitleDimensionPosition(i).element.displayName + "')";
			}
		}
		return elementName;
	}
	

	public void findCellLocation(int row, int column) {
		String intersections = "";
		TM1ViewAxes columnsaxes = view.getColumnAxes();
		TM1ViewAxes rowsaxes = view.getrowaxes();
		for (int i = 0; i < rowheadercount; i++) {
			String colheader = columnsaxes.tuples.get(column - columnheadercount).members.get(i).name;
			intersections = intersections.concat(view.getColumnDimensionPosition(i).dimension.displayName + ":" + colheader + "\n");
		}
		for (int i = 0; i < columnheadercount; i++) {
			String rowheader = rowsaxes.tuples.get(row - rowheadercount).members.get(i).name;
			intersections = intersections.concat(view.getRowDimensionPosition(i).dimension.displayName + ":" + rowheader + "\n");
		}
		for (int i = 0; i < view.getfilterscount(); i++) {
			intersections = intersections.concat(view.getTitleDimensionPosition(i).dimension.displayName + ":" + view.getTitleDimensionPosition(i).element.displayName + "\n");
		}
		infoMessage(intersections);
	}

	public void set_sandboxes() {
		for (int i = 0; i < view.sandboxCount(); i++) {
			sandbox_combo.add(view.getSandbox(i).name);
			if (view.getSandbox(i).isactive) {
				sandbox_combo.select(i);
			}
		}
	}

	public void refreshViewHeaders() {
		try {
			view.readAxesFromServer();
			rowsTable.removeAll();
			for (int i = 0; i < view.getrowscount(); i++) {
				TableItem rowTableItem = new TableItem(rowsTable, SWT.NONE);
				CubeViewerPosition cubeViewerPosition = view.getRowDimensionPosition(i);
				rowTableItem.setText(0, cubeViewerPosition.dimensionName());
				rowTableItem.setText(1, cubeViewerPosition.subsetName());
				rowTableItem.setData(view.getRowDimensionPosition(i));
			}
			for (TableColumn tc : rowsTable.getColumns()) tc.pack();

			columnsTable.removeAll();
			for (int i = 0; i < view.columnCount(); i++) {
				TableItem columnTableItem = new TableItem(columnsTable, SWT.NONE);
				CubeViewerPosition cubeViewerPosition = view.getColumnDimensionPosition(i);
				columnTableItem.setText(0, cubeViewerPosition.dimensionName());
				columnTableItem.setText(1, cubeViewerPosition.subsetName());
				columnTableItem.setData(view.getColumnDimensionPosition(i));
			}
			for (TableColumn tc : columnsTable.getColumns()) tc.pack();

			titlesTable.removeAll();
			for (int i = 0; i < view.getfilterscount(); i++) {
				TableItem titleTableItem = new TableItem(titlesTable, SWT.NONE);
				CubeViewerPosition cubeViewerPosition = view.getTitleDimensionPosition(i);
				titleTableItem.setText(0, cubeViewerPosition.dimensionName());
				titleTableItem.setText(1, cubeViewerPosition.elementName());

				titleTableItem.setData(view.getTitleDimensionPosition(i));
			}
			for (TableColumn tc : titlesTable.getColumns()) tc.pack();

		} catch (JSONException | TM1RestException | URISyntaxException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void refreshView() {
		try {
			view.recalculate();
			viewtable.setRedraw(false);
			viewtable.clearAll();
			while (viewtable.getItemCount() > 0) {
				viewtable.getItem(0).dispose();
			}
			while (viewtable.getColumnCount() > 0) {
				viewtable.getColumns()[0].dispose();
			}
			TM1ViewAxes columnsaxes = view.getColumnAxes();
			TM1ViewAxes rowsaxes = view.getrowaxes();

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
				column.setResizable(true);
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
					if (suppressheader) {
						if (colheader.equals(lastcolheader)) {
							row.setText(j, "");
						} else {
							if (membertype.equals("Consolidated")) {
								row.setText(j, "-- " + colheader);
							} else {
								row.setText(j, colheader);
							}
							lastcolheader = colheader;
						}
					} else {
						if (membertype.equals("Consolidated")) {
							row.setText(j, "-- " + colheader);
						} else {
							row.setText(j, colheader);
						}
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
					if (suppressheader) {
						if (rowheader.equals(lastrowheader)) {
							row.setText(i, "");
						} else {
							if (membertype.equals("Consolidated")) {
								row.setText(i, "-- " + rowheader);
							} else {
								row.setText(i, rowheader);
							}
							lastrowheader = rowheader;
						}
					} else {
						if (membertype.equals("Consolidated")) {
							row.setText(i, "-- " + rowheader);
						} else {
							row.setText(i, rowheader);
						}
					}
					row.setBackground(i, HEADER);
				}
			}

			if (view.cellCount() == 0) {
				infoMessage("No values");
			} else {

				int ordinal = 0;
				for (int i = rowheadercount; i < totalrowcount; i++) {
					TableItem row = viewtable.getItem(i);
					for (int j = columnheadercount; j < totalcolumncount; j++) {
						TM1Cell cell = view.getCell(ordinal);
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
			resizeColumns();

		} catch (JSONException | TM1RestException | URISyntaxException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void updateViewFromUI() {
		view.clearRows();
		view.clearColumns();
		view.clearTitles();

		for (int i = 0; i < rowsTable.getItemCount(); i++) {
			TableItem t = rowsTable.getItem(i);
			CubeViewerPosition cubeViewerPosition = (CubeViewerPosition) t.getData();
			view.setRowDimensionPosition(cubeViewerPosition);
		}

		for (int i = 0; i < columnsTable.getItemCount(); i++) {
			TableItem t = columnsTable.getItem(i);
			CubeViewerPosition cubeViewerPosition = (CubeViewerPosition) t.getData();
			view.setColumnDimensionPosition(cubeViewerPosition);
		}

		for (int i = 0; i < titlesTable.getItemCount(); i++) {
			TableItem t = titlesTable.getItem(i);
			CubeViewerPosition cubeViewerPosition = (CubeViewerPosition) t.getData();
			view.setTitleDimensionPosition(cubeViewerPosition);
		}
	}

	private void resizeColumns(){
		for (TableColumn tc : viewtable.getColumns()) tc.pack();
	}

	public void save(){
		try {
			updateViewFromUI();
			view.updateViewToServer();
		} catch (TM1RestException | URISyntaxException | IOException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void saveAs(){
		try {
			updateViewFromUI();
			Prompt_ViewSave viewNamePrompt = new Prompt_ViewSave(shell, SWT.DIALOG_TRIM);
			if (viewNamePrompt.open()){
				view.writeViewToServer(viewNamePrompt.getViewName(), viewNamePrompt.getIsPrivate());
			}
		} catch (JSONException | TM1RestException | URISyntaxException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean pendingSave(){
		return pendingSave;
	}

	public void openSubsetEditor() throws TM1RestException {

	}
	
	private void showCellMenu(){
		// Create menu item
		if (isRuleDerived(get_ordinal(selectedrow, selectedcolumn))){
			mntmTraceRules.setEnabled(true);
		} else {
			mntmTraceRules.setEnabled(false);
		}
	}
}
