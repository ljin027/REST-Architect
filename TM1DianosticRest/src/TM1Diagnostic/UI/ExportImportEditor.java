package TM1Diagnostic.UI;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.OrderedJSONObject;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

import TM1Diagnostic.TransferChore;
import TM1Diagnostic.TransferCube;
import TM1Diagnostic.TransferDimension;
import TM1Diagnostic.TransferHierarchy;
import TM1Diagnostic.TransferProcess;
import TM1Diagnostic.TransferSpec;
import TM1Diagnostic.TransferSubset;
import TM1Diagnostic.ZipUtils;
import TM1Diagnostic.REST.FileTransferHelper;
import TM1Diagnostic.REST.TM1Blob;
import TM1Diagnostic.REST.TM1Chore;
import TM1Diagnostic.REST.TM1Cube;
import TM1Diagnostic.REST.TM1Dimension;
import TM1Diagnostic.REST.TM1Folder;
import TM1Diagnostic.REST.TM1Hierarchy;
import TM1Diagnostic.REST.TM1Object;
import TM1Diagnostic.REST.TM1ObjectReference;
import TM1Diagnostic.REST.TM1Process;
import TM1Diagnostic.REST.TM1RestException;
import TM1Diagnostic.REST.TM1Server;
import TM1Diagnostic.REST.TM1Subset;
import TM1Diagnostic.REST.TM1View;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

public class ExportImportEditor extends Dialog {

	final int EDITABLECOLUMN = 1;

	protected boolean result;
	protected Shell shlExport;

	private Tree transferSourceTree;
	private Tree transferTargetTree;

	private Button transferButton;
	private Combo sourceCombo;
	private Combo targetCombo;
	private Button controlObjectsCheckButton;

	private Object source;
	private Object target;

	private TM1Server tm1server;
	private List<TreeItem> export_items;
	private File importFile;
	private File exportDirectory;
	private boolean showControlObjects;

	private List<TM1Object> transferObjects;

	private FileTransferHelper fileTransferHelper;
	
	private TransferSpec transferSpec;

	static private Image FOLDERICON;
	static private Image ADMINICON;
	static private Image CONNECTEDICON;
	static private Image DISCONNECTEDICON;
	static private Image APPICON;
	static private Image DIMICON;
	static private Image CUBEICON;
	static private Image PROICON;
	static private Image CHOICON;
	static private Image VIEWICON;
	static private Image SUBICON;
	static private Image HIERICON;
	static private Image RULEICON;
	static private Image EXCELICON;
	static private Image PRIVVIEWICON;
	static private Image PRIVSUBICON;
	static private Image BLOBICON;
	static private Image SECURITY_ICON;

	static private Image SAVE;
	static private Image SAVEAS;
	static private Image EXECUTE;

	static public int SERVER = 0;
	static public int CUBE = 1;
	static public int DIMENSION = 2;
	static public int PROCESS = 3;
	static public int CHORE = 4;
	static public int VIEW = 5;
	static public int HIERARCHY = 6;
	static public int SUBSET = 7;
	static public int ELEMENT = 8;
	static public int APPLICATION = 9;
	static public int CELL = 10;
	static public int BLOB = 11;
	static public int FOLDER = 12;
	static public int REFERENCE = 13;
	static public int DOCUMENT = 14;

	public ExportImportEditor(Shell parent, TM1Server tm1server) {
		super(parent, SWT.DIALOG_TRIM);
		setText("SWT Dialog");
		this.tm1server = tm1server;
		export_items = new ArrayList<TreeItem>();
		transferObjects = new ArrayList<TM1Object>();
		fileTransferHelper = new FileTransferHelper(tm1server);
		transferSpec = new TransferSpec(tm1server);
	}

	public boolean open() throws TM1RestException {
		result = false;
		showControlObjects = false;
		source = tm1server;

		createContents();
		shlExport.open();
		shlExport.layout();
		Display display = getParent().getDisplay();
		while (!shlExport.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	private void createContents() throws TM1RestException {
		shlExport = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.MAX | SWT.RESIZE | SWT.APPLICATION_MODAL);
		shlExport.setSize(918, 519);
		shlExport.setText("Transfer Manager");
		shlExport.setLayout(new GridLayout(1, false));

		Display display = shlExport.getDisplay();

		SAVE = new Image(display, ".\\images\\action_save.gif");
		SAVEAS = new Image(display, ".\\images\\action_save_as.gif");
		EXECUTE = new Image(display, ".\\images\\action_run.gif");

		FOLDERICON = new Image(display, ".\\images\\icon_folder.gif");
		ADMINICON = new Image(display, ".\\images\\icon_admin.gif");
		CONNECTEDICON = new Image(display, ".\\images\\icon_connected.gif");
		DISCONNECTEDICON = new Image(display, ".\\images\\icon_disconnected.gif");
		APPICON = new Image(display, ".\\images\\icon_app.gif");
		DIMICON = new Image(display, ".\\images\\icon_dimension.gif");
		CUBEICON = new Image(display, ".\\images\\icon_cube.gif");
		PROICON = new Image(display, ".\\images\\icon_process.gif");
		CHOICON = new Image(display, ".\\images\\icon_chore.gif");
		VIEWICON = new Image(display, ".\\images\\icon_view.gif");
		SUBICON = new Image(display, ".\\images\\icon_subset.gif");
		HIERICON = new Image(display, ".\\images\\icon_hierarchy.gif");
		RULEICON = new Image(display, ".\\images\\icon_rules.gif");
		EXCELICON = new Image(display, ".\\images\\icon_excel.gif");
		PRIVVIEWICON = new Image(display, ".\\images\\icon_views_private.gif");
		PRIVSUBICON = new Image(display, ".\\images\\icon_subset.gif");
		BLOBICON = new Image(display, ".\\images\\icon_table.gif");
		SECURITY_ICON = new Image(display, ".\\images\\key.gif");

		Composite composite_2 = new Composite(shlExport, SWT.NONE);
		composite_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		composite_2.setLayout(new GridLayout(3, false));

		Label label1 = new Label(composite_2, SWT.NONE);
		label1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label1.setText("Source");

		sourceCombo = new Combo(composite_2, SWT.READ_ONLY);
		sourceCombo.add("File");
		sourceCombo.add("TM1 Server: " + tm1server.getName());
		sourceCombo.setText("");
		sourceCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String s = (String) sourceCombo.getItem(sourceCombo.getSelectionIndex());
				if (s.equals("File")) {
					// Configure to import from File
					if (selectImportFile()) {
						targetCombo.setEnabled(false);
						targetCombo.removeAll();
						targetCombo.add("TM1 Server: " + tm1server.getName());
						targetCombo.setText("TM1 Server: " + tm1server.getName());
					}
				} else if (s.equals("TM1 Server: " + tm1server.getName())) {
					// Configure to export from TM1 Model
					sourceCombo.setEnabled(false);

					targetCombo.setEnabled(true);
					targetCombo.removeAll();
					targetCombo.add("Directory");
					targetCombo.setText("");

					// updateTM1ServerExportTree();
				}
			}
		});
		sourceCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		controlObjectsCheckButton = new Button(composite_2, SWT.CHECK);
		controlObjectsCheckButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				showControlObjects = controlObjectsCheckButton.getSelection();
				if (source instanceof TM1Server) {
					// updateTM1ServerExportTree();
				} else {

				}
			}
		});
		controlObjectsCheckButton.setText("Show Control Objects");

		Label targetLabel = new Label(composite_2, SWT.NONE);
		targetLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));
		targetLabel.setText("Target");

		targetCombo = new Combo(composite_2, SWT.READ_ONLY);
		targetCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (targetCombo.getText().equals("Directory")) {
					if (selectExportDirectory()) {
						target = exportDirectory;
						fileTransferHelper.setExportDirectory(exportDirectory);
						targetCombo.removeAll();
						targetCombo.add("Directory: " + exportDirectory.getAbsolutePath());
						targetCombo.add("Directory");
						targetCombo.select(0);
						// targetCombo.setEnabled(false);
						// updateFileExportTree();
					}
				}
			}
		});
		targetCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(composite_2, SWT.NONE);

		Composite composite = new Composite(shlExport, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		SashForm sashForm = new SashForm(composite, SWT.NONE);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		transferSourceTree = new Tree(sashForm, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		transferSourceTree.setHeaderVisible(true);
		transferSourceTree.addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(Event event) {
				setSourceTreeColumnSize();

				// for (TreeColumn tc : transferSourceTree.getColumns())
				// tc.pack();
				// transferSourceTree.redraw();
			}
		});

		transferSourceTree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (event.detail == SWT.CHECK) {
					TreeItem t = (TreeItem) event.item;
					if (t.getChecked()) {
						t.setChecked(true);
						toggleselection(t, true);
					} else {
						t.setChecked(false);
						toggleselection(t, false);
					}
				}
			}
		});

		transferSourceTree.addTreeListener(new TreeListener() {

			public void treeExpanded(TreeEvent event) {
				if (source instanceof TM1Server) {
					TreeItem node = (TreeItem) event.item;
					Object o = node.getData();
					if (o instanceof TM1Cube) {
						TM1Cube cube = (TM1Cube) o;
						cube.expandedInExplorerTree = true;
						updateCubeNode(node);
					} else if (o instanceof TM1Dimension) {
						TM1Dimension dimension = (TM1Dimension) o;
						dimension.expandedInExplorerTree = true;
						// updateDimensionNode(node);
					} else if (o instanceof TM1Hierarchy) {
						TM1Hierarchy hierarchy = (TM1Hierarchy) o;
						hierarchy.expandedInExplorerTree = true;
					} else if (o instanceof String) {
						String nodename = (String) o;
						if (nodename.equals("Applications")) {
							tm1server.expandapplications();
						} else if (nodename.equals("Cubes")) {
							tm1server.expandcubes();
						} else if (nodename.equals("Dimensions")) {
							if (node.getParentItem() == null) {
								tm1server.expanddimensions();
							} else if (node.getParentItem().getData() instanceof TM1Cube) {
								TM1Cube cube = (TM1Cube) node.getParentItem().getData();
								cube.dimensionsExpandedInServerExplorer = true;
							}
						} else if (nodename.equals("Processes")) {
							tm1server.expandprocesses();
						} else if (nodename.equals("Chores")) {
							tm1server.expandchores();
						} else if (nodename.equals("Hierarchies")) {
							TreeItem dimensionNode = node.getParentItem();
							TM1Dimension dimension = (TM1Dimension) dimensionNode.getData();
							dimension.heirarchiesExpandedInServerExplorer = true;
						} else if (nodename.equals("Subsets")) {
							TreeItem hierarchyNode = node.getParentItem();
							TM1Hierarchy hierarchy = (TM1Hierarchy) hierarchyNode.getData();
							hierarchy.subsetsExpandedInServerExplorer = true;
						} else if (nodename.equals("Views")) {
							TreeItem cubeNode = node.getParentItem();
							TM1Cube cube = (TM1Cube) cubeNode.getData();
							cube.viewsExpandedInServerExplorer = true;
						} else {
							//

						}
					}
				}
				setSourceTreeColumnSize();
			}

			public void treeCollapsed(TreeEvent event) {
				if (source instanceof TM1Server) {
					TreeItem node = (TreeItem) event.item;
					Object o = node.getData();
					if (o instanceof TM1Cube) {
						TM1Cube cube = (TM1Cube) o;
						cube.expandedInExplorerTree = false;
						// updateCubeNode(node);
					} else if (o instanceof TM1Dimension) {
						TM1Dimension dimension = (TM1Dimension) o;
						dimension.expandedInExplorerTree = false;
						// updateDimensionNode(node);
					} else if (o instanceof TM1Hierarchy) {
						TM1Hierarchy hierarchy = (TM1Hierarchy) o;
						hierarchy.expandedInExplorerTree = false;
						// updateDimensionNode(node);
					} else if (node.getData() instanceof String) {
						String nodename = (String) node.getData();
						if (nodename.equals("Applications")) {
							tm1server.collapseapplications();
						} else if (nodename.equals("Cubes")) {
							tm1server.collapsecubes();
						} else if (nodename.equals("Dimensions")) {
							if (node.getParentItem() == null) {
								tm1server.expanddimensions();
							} else if (node.getParentItem().getData() instanceof TM1Cube) {
								TM1Cube cube = (TM1Cube) node.getParentItem().getData();
								cube.dimensionsExpandedInServerExplorer = false;
							}
						} else if (nodename.equals("Hierarchies")) {
							TreeItem dimensionNode = node.getParentItem();
							TM1Dimension dimension = (TM1Dimension) dimensionNode.getData();
							dimension.heirarchiesExpandedInServerExplorer = false;
						} else if (nodename.equals("Processes")) {
							tm1server.collapseprocesses();
						} else if (nodename.equals("Chores")) {
							tm1server.collapsechores();
						} else if (nodename.equals("Hierarchies")) {
							TreeItem dimensionNode = node.getParentItem();
							TM1Dimension dimension = (TM1Dimension) dimensionNode.getData();
							dimension.heirarchiesExpandedInServerExplorer = false;
						} else if (nodename.equals("Subsets")) {
							TreeItem hierarchyNode = node.getParentItem();
							TM1Hierarchy hierarchy = (TM1Hierarchy) hierarchyNode.getData();
							hierarchy.subsetsExpandedInServerExplorer = false;
						} else if (nodename.equals("Views")) {
							TreeItem cubeNode = node.getParentItem();
							TM1Cube cube = (TM1Cube) cubeNode.getData();
							cube.viewsExpandedInServerExplorer = false;
						} else {

						}
					}
				}

			}
		});

		Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
		DragSource source = new DragSource(transferSourceTree, DND.DROP_MOVE);
		source.setTransfer(types);

		TreeColumn trclmnNewColumn = new TreeColumn(transferSourceTree, SWT.NONE);
		// trclmnNewColumn.setWidth(200);
		trclmnNewColumn.setText("Source Object");

		transferObjects = new ArrayList<TM1Object>();

		final List<TreeItem> dragSourceItems = new ArrayList<TreeItem>();

		source.addDragListener(new DragSourceAdapter() {
			public void dragStart(DragSourceEvent event) {
				TreeItem[] selection = transferSourceTree.getSelection();
				if (selection.length > 0 && selection[0].getItemCount() >= 0) {
					event.doit = true;
					dragSourceItems.clear();
					transferObjects.clear();
					for (int i = selection.length - 1; i >= 0; i--) {
						dragSourceItems.add(selection[i]);
						if (selection[i].getData() instanceof TM1Object) {
							transferObjects.add((TM1Object) selection[i].getData());
						} else if (selection[i].getData() instanceof String) {

						} else {

						}
					}
				} else {
					event.doit = false;
				}
			}

			public void dragSetData(DragSourceEvent event) {
				event.data = dragSourceItems.get(0).getText();
			}

			public void dragFinished(DragSourceEvent event) {
				if (event.detail == DND.DROP_MOVE) {
					// for (int i = 0; i < dragSourceItems.size(); i++) {
					// TreeItem t = dragSourceItems.get(i);
					// t.dispose();
					// t = null;
					// }
				}
			}
		});

		transferTargetTree = new Tree(sashForm, SWT.BORDER);
		transferTargetTree.setHeaderVisible(true);

		transferTargetTree.addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(Event event) {
				Tree tree = (Tree) event.widget;
				int columnCount = tree.getColumnCount();
				if (columnCount == 0)
					return;
				Rectangle area = tree.getClientArea();
				int totalAreaWdith = area.width;
				int lineWidth = tree.getGridLineWidth();
				int totalGridLineWidth = (columnCount - 1) * lineWidth;
				int totalColumnWidth = 0;
				for (TreeColumn column : tree.getColumns()) {
					totalColumnWidth = totalColumnWidth + column.getWidth();
				}
				int diff = totalAreaWdith - (totalColumnWidth + totalGridLineWidth);
				TreeColumn lastCol = tree.getColumns()[columnCount - 1];
				lastCol.setWidth(diff + lastCol.getWidth());

			}
		});

		transferTargetTree.addTreeListener(new TreeListener() {

			public void treeExpanded(TreeEvent event) {
				if (target instanceof File) {
					TreeItem node = (TreeItem) event.item;
					Object o = node.getData();
					if (o instanceof TM1Server) {
						TM1Server tm1server = (TM1Server) o;
						tm1server.expandserver();
					} else if (o instanceof TM1Cube) {
						TM1Cube cube = (TM1Cube) o;
						cube.expandedInExplorerTree = true;
						updateCubeNode(node);
					} else if (o instanceof TM1Dimension) {
						TM1Dimension dimension = (TM1Dimension) o;
						dimension.expandedInExplorerTree = true;
						// updateDimensionNode(node);
					} else if (o instanceof TM1Hierarchy) {
						TM1Hierarchy hierarchy = (TM1Hierarchy) o;
						hierarchy.expandedInExplorerTree = true;
					} else if (o instanceof TM1Subset) {
					} else if (o instanceof TM1View) {
					} else if (o instanceof String) {
						String nodename = (String) o;
						if (nodename.equals("Applications")) {
							// fileTransferHelper.expandapplications = true;
						} else if (nodename.equals("Cubes")) {
							fileTransferHelper.cubesexpanded = true;
						} else if (nodename.equals("Dimensions")) {
							if (node.getParentItem() == null) {
								fileTransferHelper.dimensionsexpanded = true;
							} else if (node.getParentItem().getData() instanceof TM1Cube) {
								TM1Cube cube = (TM1Cube) node.getParentItem().getData();
								cube.dimensionsExpandedInServerExplorer = true;
							}
						} else if (nodename.equals("Processes")) {
							fileTransferHelper.processesexpanded = true;
						} else if (nodename.equals("Chores")) {
							fileTransferHelper.processesexpanded = true;
						} else if (nodename.equals("Hierarchies")) {
							TreeItem dimensionNode = node.getParentItem();
							TM1Dimension dimension = (TM1Dimension) dimensionNode.getData();
							dimension.heirarchiesExpandedInServerExplorer = true;
						} else if (nodename.equals("Subsets")) {
							TreeItem hierarchyNode = node.getParentItem();
							TM1Hierarchy hierarchy = (TM1Hierarchy) hierarchyNode.getData();
							hierarchy.subsetsExpandedInServerExplorer = true;
						} else if (nodename.equals("Views")) {
							TreeItem cubeNode = node.getParentItem();
							TM1Cube cube = (TM1Cube) cubeNode.getData();
							cube.viewsExpandedInServerExplorer = true;
						} else {
							//

						}
					}
				}
			}

			public void treeCollapsed(TreeEvent event) {
				if (target instanceof File) {
					TreeItem node = (TreeItem) event.item;
					Object o = node.getData();
					if (o instanceof TM1Cube) {
						TM1Cube cube = (TM1Cube) o;
						cube.expandedInExplorerTree = false;
					} else if (o instanceof TM1Dimension) {
						TM1Dimension dimension = (TM1Dimension) o;
						dimension.expandedInExplorerTree = false;
					} else if (o instanceof TM1Hierarchy) {
						TM1Hierarchy hierarchy = (TM1Hierarchy) o;
						hierarchy.expandedInExplorerTree = false;
					} else if (node.getData() instanceof String) {
						String nodename = (String) node.getData();
						if (nodename.equals("Applications")) {
							// fileTransferHelper.collapseapplications();
						} else if (nodename.equals("Cubes")) {
							fileTransferHelper.cubesexpanded = false;
						} else if (nodename.equals("Dimensions")) {
							if (node.getParentItem() == null) {
								fileTransferHelper.dimensionsexpanded = false;
							} else if (node.getParentItem().getData() instanceof TM1Cube) {
								TM1Cube cube = (TM1Cube) node.getParentItem().getData();
								cube.dimensionsExpandedInServerExplorer = false;
							}
						} else if (nodename.equals("Hierarchies")) {
							TreeItem dimensionNode = node.getParentItem();
							TM1Dimension dimension = (TM1Dimension) dimensionNode.getData();
							dimension.heirarchiesExpandedInServerExplorer = false;
						} else if (nodename.equals("Processes")) {
							fileTransferHelper.processesexpanded = false;
						} else if (nodename.equals("Chores")) {
							fileTransferHelper.choresexpanded = false;
						} else if (nodename.equals("Hierarchies")) {
							TreeItem dimensionNode = node.getParentItem();
							TM1Dimension dimension = (TM1Dimension) dimensionNode.getData();
							dimension.heirarchiesExpandedInServerExplorer = false;
						} else if (nodename.equals("Subsets")) {
							TreeItem hierarchyNode = node.getParentItem();
							TM1Hierarchy hierarchy = (TM1Hierarchy) hierarchyNode.getData();
							hierarchy.subsetsExpandedInServerExplorer = false;
						} else if (nodename.equals("Views")) {
							TreeItem cubeNode = node.getParentItem();
							TM1Cube cube = (TM1Cube) cubeNode.getData();
							cube.viewsExpandedInServerExplorer = false;
						} else {

						}
					}
				}

			}
		});

		DropTarget dropTarget = new DropTarget(transferTargetTree, DND.DROP_MOVE);
		dropTarget.setTransfer(types);

		TreeColumn trclmnNewColumn_1 = new TreeColumn(transferTargetTree, SWT.NONE);
		trclmnNewColumn_1.setWidth(169);
		trclmnNewColumn_1.setText("Target Object");

		TreeColumn trclmnNewColumn_2 = new TreeColumn(transferTargetTree, SWT.NONE);
		trclmnNewColumn_2.setWidth(171);
		trclmnNewColumn_2.setText("Options");
		sashForm.setWeights(new int[] { 337, 540 });
		dropTarget.addDropListener(new DropTargetAdapter() {
			public void dragOver(DropTargetEvent event) {
				event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
				if (event.item != null) {
					event.feedback |= DND.FEEDBACK_SELECT;
				}
			}

			public void drop(DropTargetEvent event) {
				if (event.data == null) {
					event.detail = DND.DROP_NONE;
					return;
				}
				for (int i = 0; i < transferObjects.size(); i++) {
					TM1Object tm1object = transferObjects.get(i);
					if (tm1object instanceof TM1Process) {
						TM1Process process = (TM1Process) tm1object;
						fileTransferHelper.addProcess(process);
					} else if (tm1object instanceof TM1Chore) {
						TM1Chore chore = (TM1Chore) tm1object;
						fileTransferHelper.addChore(chore);
					} else if (tm1object instanceof TM1Dimension) {
						// TM1Dimension dimension = (TM1Dimension) tm1object;
						// fileTransferHelper.addDimension(dimension);
					} else if (tm1object instanceof TM1Cube) {
						try {
							TM1Cube cube = (TM1Cube) tm1object;
							fileTransferHelper.addCube(cube);
						} catch (TM1RestException | URISyntaxException | IOException | JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

					try {
						updateFileExportTree();
					} catch (TM1RestException e) {
						// TODO Auto-generated catch block
					}
				}
			}
		});

		Composite composite_1 = new Composite(shlExport, SWT.NONE);
		composite_1.setLayout(new GridLayout(2, false));
		composite_1.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, false, 1, 1));

		Button cancelButton = new Button(composite_1, SWT.NONE);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (transferButton.getEnabled()) {
					result = false;
				} else {
					result = true;
				}
				shlExport.close();
			}
		});
		GridData gd_cancelButton = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_cancelButton.widthHint = 140;
		cancelButton.setLayoutData(gd_cancelButton);
		cancelButton.setText("Cancel");

		transferButton = new Button(composite_1, SWT.NONE);
		transferButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				runTransfer();
				
			}
		});
		GridData gd_transferButton = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_transferButton.widthHint = 140;
		transferButton.setLayoutData(gd_transferButton);
		transferButton.setText("Transfer");

		// updateTM1ServerExportTree();
	}

	public void expand(TreeItem t) {
		t.setExpanded(true);
		for (int i = 0; i < t.getItemCount(); i++) {
			expand(t.getItem(i));
		}
	}

	public void updateTM1ServerExportTree() throws TM1RestException {
		// serverNode.removeAll();
		transferSourceTree.removeAll();

		// TreeItem appnode = new TreeItem(transferSourceTree, SWT.NONE);
		// appnode.setText("Applications");
		// appnode.setImage(APPICON);
		// updateApplicationsListNode(appnode);

		TreeItem cubesnode = new TreeItem(transferSourceTree, SWT.NONE);
		cubesnode.setText("Cubes");
		cubesnode.setImage(CUBEICON);
		cubesnode.setData("Cubes");
		updateCubeListNode(cubesnode);

		TreeItem dimsnode = new TreeItem(transferSourceTree, SWT.NONE);
		dimsnode.setText("Dimensions");
		dimsnode.setImage(DIMICON);
		dimsnode.setData("Dimensions");
		updateDimensionListNode(dimsnode);

		TreeItem processesnode = new TreeItem(transferSourceTree, SWT.NONE);
		processesnode.setText("Processes");
		processesnode.setImage(PROICON);
		processesnode.setData("Processes");
		updateProcessListNode(processesnode);

		TreeItem choresnode = new TreeItem(transferSourceTree, SWT.NONE);
		choresnode.setText("Chores");
		choresnode.setImage(CHOICON);
		choresnode.setData("Chores");
		updateChoreListNode(choresnode);

		// TreeItem blobsnode = new TreeItem(transferSourceTree, SWT.NONE);
		// blobsnode.setText("Blobs");
		// blobsnode.setImage(FOLDERICON);
		// blobsnode.setData("Blobs");
		// updateBlobListNode(blobsnode);

		if (tm1server.cubesexpanded()) {
			cubesnode.setExpanded(true);
		}
		if (tm1server.dimensionsexpanded()) {
			dimsnode.setExpanded(true);
		}
		if (tm1server.processesexpanded()) {
			processesnode.setExpanded(true);
		}
		if (tm1server.choresexpanded()) {
			choresnode.setExpanded(true);
		}

		for (TreeColumn tc : transferSourceTree.getColumns())
			tc.pack();
		transferSourceTree.redraw();
	}

	public void updateCubeListNode(TreeItem cubesnode) {
		try {
			cubesnode.removeAll();
			tm1server.readCubesFromServer();
			int count = tm1server.cubeCount();
			for (int i = 0; i < count; i++) {
				TM1Cube cube = tm1server.getCube(i);
				if (!showControlObjects && cube.displayName.startsWith("}")) {
				} else {
					TreeItem cubenode = new TreeItem(cubesnode, SWT.NONE);
					cubenode.setText(cube.displayName);
					cubenode.setImage(CUBEICON);
					cubenode.setData(cube);
					if (cube.expandedInExplorerTree) {
						updateCubeNode(cubenode);
						cubenode.setExpanded(true);
					} else {
						TreeItem cubeChildNode = new TreeItem(cubenode, SWT.NONE);
						cubeChildNode.setText("");
					}
				}
			}
			cubesnode.setText("Cubes");
		} catch (URISyntaxException | IOException | TM1RestException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updateCubeNode(TreeItem cubenode) {
		try {
			if ((cubenode.getData() instanceof TM1Cube)) {
				TM1Cube cube = (TM1Cube) cubenode.getData();
				cubenode.removeAll();
				cube.readCubeViewsFromServer();
				TreeItem viewsnode = new TreeItem(cubenode, SWT.NONE);
				viewsnode.setText("Views");
				viewsnode.setData("Views");
				viewsnode.setImage(VIEWICON);
				updateViewListNode(viewsnode);
				if (cube.viewsExpandedInServerExplorer) {
					viewsnode.setExpanded(true);
				}

				if (cube.checkServerForRules()) {
					TreeItem rulenode = new TreeItem(cubenode, SWT.NONE);
					rulenode.setText("Rules");
					rulenode.setData("Rules");
					rulenode.setImage(RULEICON);
				}
			}
		} catch (TM1RestException | URISyntaxException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updateViewListNode(TreeItem viewsnode) {
		TreeItem cubenode = viewsnode.getParentItem();
		TM1Cube cube = (TM1Cube) cubenode.getData();
		viewsnode.removeAll();

		int privateViewCount = cube.privateViewCount();
		for (int i = 0; i < privateViewCount; i++) {
			TM1View view = cube.getPrivateView(i);
			TreeItem viewnode = new TreeItem(viewsnode, SWT.NONE);
			viewnode.setImage(PRIVVIEWICON);
			viewnode.setText(view.displayName + " (Private)");
			viewnode.setData(view);
		}

		int viewcount = cube.viewCount();
		for (int i = 0; i < viewcount; i++) {
			TM1View view = cube.getview(i);
			TreeItem viewnode = new TreeItem(viewsnode, SWT.NONE);
			viewnode.setImage(VIEWICON);
			viewnode.setText(view.displayName);
			viewnode.setData(view);
		}
		viewsnode.setText("Views");
	}

	public void updateDimensionListNode(TreeItem dimsnode) {
		try {
			dimsnode.removeAll();
			if (tm1server.readDimensionsFromServer()) {
				int count = tm1server.dimensionCount();
				for (int i = 0; i < count; i++) {
					TM1Dimension dimension = tm1server.getDimension(i);
					String dimensionname = dimension.displayName;
					if (!showControlObjects && dimensionname.startsWith("}")) {
					} else {
						TreeItem dimensionnode = new TreeItem(dimsnode, SWT.NONE);
						dimensionnode.setImage(DIMICON);
						dimensionnode.setData(dimension);
						dimensionnode.setText(dimensionname);

						if (dimension.expandedInExplorerTree) {
							updateDimensionNode(dimensionnode);
							dimensionnode.setExpanded(true);
						} else {
							TreeItem dimensionChildNode = new TreeItem(dimensionnode, SWT.NONE);
							dimensionChildNode.setText("");
						}
					}
				}
				dimsnode.setText("Dimensions");
			} else {
				checkForReconnect(dimsnode);
			}
		} catch (TM1RestException | URISyntaxException | IOException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updateDimensionNode(TreeItem dimnode) throws TM1RestException {
		if (!(dimnode.getData() instanceof TM1Dimension))
			return;

		TM1Dimension dimension = (TM1Dimension) dimnode.getData();
		dimnode.removeAll();

		TreeItem hiarachynode = new TreeItem(dimnode, SWT.NONE);
		hiarachynode.setImage(HIERICON);
		hiarachynode.setText("Hierarchies");
		hiarachynode.setData("Hierarchies");
		updateHierarchyListNode(hiarachynode);

		if (dimension.heirarchiesExpandedInServerExplorer) {
			hiarachynode.setExpanded(true);
		}

	}

	public void infoMessage(String message) {
		MessageBox m = new MessageBox(shlExport, SWT.ICON_INFORMATION);
		m.setMessage(message);
		m.open();
	}

	public void errorMessage(String errMessage) {
		MessageBox m = new MessageBox(shlExport, SWT.ICON_ERROR);
		m.setMessage(errMessage);
		m.open();
	}

	public void checkForReconnect(TreeItem treenode) {
		TreeItem serverNode = getServerNode(treenode);
		TM1Server tm1server = (TM1Server) serverNode.getData();
		if (!tm1server.isAuthenticated()) {
			infoMessage("Disconnected from " + tm1server.getName());
		}
	}

	public TreeItem getServerNode(TreeItem treenode) {
		if ((treenode.getData() instanceof TM1Server)) {
			return treenode;
		} else {
			return getServerNode(treenode.getParentItem());
		}
	}

	public void updateHierarchyListNode(TreeItem hierarchiesnode) {
		try {
			TreeItem dimensionnode = hierarchiesnode.getParentItem();
			TM1Dimension dimension = (TM1Dimension) (dimensionnode.getData());
			hierarchiesnode.removeAll();

			dimension.readHierarchiesFromServer();
			int hierarchycount = dimension.hierarchyCount();
			hierarchiesnode.setText("Hierarchies");
			for (int i = 0; i < hierarchycount; i++) {
				TM1Hierarchy hierarchy = dimension.getHeirarchy(i);
				String hierarchyname = hierarchy.displayName;
				TreeItem hierarchynode = new TreeItem(hierarchiesnode, SWT.NONE);
				hierarchynode.setText(hierarchyname);
				hierarchynode.setImage(HIERICON);
				hierarchynode.setData(hierarchy);

				if (!hierarchy.readSubsetsFromServer()) {
					checkForReconnect(hierarchiesnode);
					return;
				}

				TreeItem subsetsnode = new TreeItem(hierarchynode, SWT.NONE);
				subsetsnode.setImage(SUBICON);
				subsetsnode.setData("Subsets");
				updateSubsetListNode(subsetsnode);
				if (hierarchy.subsetsExpandedInServerExplorer) {
					subsetsnode.setExpanded(true);
				}

				if (hierarchy.expandedInExplorerTree) {
					hierarchynode.setExpanded(true);
				}
			}
		} catch (TM1RestException | URISyntaxException | IOException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void updateSubsetListNode(TreeItem subsetListNode) {
		TM1Hierarchy hierarchy = (TM1Hierarchy) (subsetListNode.getParentItem().getData());
		subsetListNode.removeAll();

		int privateSubsetCount = hierarchy.privateSubsetCount();
		for (int i = 0; i < privateSubsetCount; i++) {
			TM1Subset subset = hierarchy.getPrivateSubset(i);
			String subsetname = subset.displayName;
			TreeItem subsetnode = new TreeItem(subsetListNode, SWT.NONE);
			subsetnode.setText(subsetname);
			subsetnode.setImage(SUBICON);
			subsetnode.setData(subset);
		}

		int subsetcount = hierarchy.subsetCount();
		for (int i = 0; i < subsetcount; i++) {
			TM1Subset subset = hierarchy.getSubset(i);
			String subsetname = subset.displayName;
			TreeItem subsetnode = new TreeItem(subsetListNode, SWT.NONE);
			subsetnode.setText(subsetname);
			subsetnode.setImage(PRIVSUBICON);
			subsetnode.setData(subset);
		}

		subsetListNode.setText("Subsets");
	}

	public void updateProcessListNode(TreeItem processListNode) throws TM1RestException {
		try {
			processListNode.removeAll();
			if (tm1server.readProcessesFromServer()) {
				int count = tm1server.processCount();
				for (int i = 0; i < count; i++) {
					TM1Process process = tm1server.getProcess(i);
					String processname = process.displayName;
					if (!showControlObjects && processname.startsWith("}")) {
					} else {
						TreeItem processnode = new TreeItem(processListNode, SWT.NONE);
						processnode.setText(processname);
						processnode.setImage(PROICON);
						processnode.setData(process);
					}
				}
				processListNode.setText("Procesess");
				return;
			} else {
				checkForReconnect(processListNode);
			}
		} catch (URISyntaxException | IOException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updateChoreListNode(TreeItem choreListNode) {
		try {
			choreListNode.removeAll();
			if (!tm1server.readChoresFromServer()) {
				checkForReconnect(choreListNode);
				return;
			}
			int chorecount = tm1server.choreCount();
			choreListNode.setText("Chores");
			for (int i = 0; i < chorecount; i++) {
				TM1Chore chore = tm1server.getChore(i);
				String chorename = chore.displayName;
				TreeItem chorenode = new TreeItem(choreListNode, SWT.NONE);
				chorenode.setText(chorename);
				chorenode.setImage(CHOICON);
				chorenode.setData(chore);
			}
		} catch (URISyntaxException | IOException | JSONException | TM1RestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updateBlobListNode(TreeItem blobsnode) {
		try {
			blobsnode.removeAll();
			if (!tm1server.readBlobsFromServer()) {
				checkForReconnect(blobsnode);
				return;
			}
			int blobcount = tm1server.getBlobCount();
			blobsnode.setText("Blobs");
			for (int i = 0; i < blobcount; i++) {
				TM1Blob blob = tm1server.getBlob(i);
				String blobname = blob.displayName;
				TreeItem blobnode = new TreeItem(blobsnode, SWT.NONE);
				blobnode.setText(blobname);
				blobnode.setImage(CHOICON);
				blobnode.setData(blob);
			}
		} catch (URISyntaxException | IOException | TM1RestException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updateApplicationsListNode(TreeItem applicationsnode) {
		try {
			applicationsnode.removeAll();
			if (!tm1server.readFoldersFromServer()) {
				checkForReconnect(applicationsnode);
				return;
			}
			int applicationcount = tm1server.getFoldersCount();
			applicationsnode.setText("Applications");
			for (int i = 0; i < applicationcount; i++) {
				TM1Folder folder = tm1server.getFolder(i);
				String foldername = folder.displayName;
				TreeItem appnode = new TreeItem(applicationsnode, SWT.NONE);
				appnode.setText(foldername);
				appnode.setImage(FOLDERICON);
				appnode.setData(folder);
				updateApplicationNode(appnode);
			}
		} catch (URISyntaxException | IOException | TM1RestException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updateApplicationNode(TreeItem applicationNode) {
		applicationNode.removeAll();
		TM1Folder tm1folder = (TM1Folder) applicationNode.getData();
		for (int i = 0; i < tm1folder.getFolderCount(); i++) {
			TM1Folder folder = tm1folder.getFolder(i);
			TreeItem childNode = new TreeItem(applicationNode, SWT.NONE);
			childNode.setText(folder.displayName);
			childNode.setImage(FOLDERICON);
			childNode.setData(folder);
			updateApplicationNode(childNode);
		}
		for (int i = 0; i < tm1folder.getReferenceCount(); i++) {
			TM1ObjectReference reference = tm1folder.getReference(i);
			TreeItem childNode = new TreeItem(applicationNode, SWT.NONE);
			childNode.setText(reference.displayName);
			if (reference.referenceType == CUBE) {
				childNode.setImage(CUBEICON);
			} else if (reference.referenceType == PROCESS) {
				childNode.setImage(PROICON);
			} else if (reference.referenceType == VIEW) {
				childNode.setImage(VIEWICON);
			} else if (reference.referenceType == DOCUMENT) {
				childNode.setImage(EXCELICON);
			}
			childNode.setData(reference);
		}
	}

	public void toggleselection(TreeItem t, boolean state) {
		int childcount = t.getItemCount();
		for (int i = 0; i < childcount; i++) {
			TreeItem child = t.getItem(i);
			child.setChecked(state);
			toggleselection(child, state);
		}
	}

	public void getselectedobjects(TreeItem t) {
		int childcount = t.getItemCount();
		if (t.getData() instanceof TM1Object) {
			TM1Object export_object = (TM1Object) t.getData();
			if (t.getChecked()) {
				export_items.add(t);
			}
		}
		for (int i = 0; i < childcount; i++) {
			TreeItem child = t.getItem(i);
			getselectedobjects(child);
		}
	}

	private boolean selectImportFile() {
		FileDialog filedialog = new FileDialog(shlExport, SWT.SINGLE);
		filedialog.setFilterExtensions(new String[] { "*.*" });
		if (filedialog.open() != null) {
			String filename = filedialog.getFilterPath() + "//" + filedialog.getFileName();
			importFile = new File(filename);
			sourceCombo.add(importFile.getAbsolutePath());
			sourceCombo.setText(importFile.getAbsolutePath());
			readSourceZipFile(importFile.getAbsolutePath());
			return true;
		}
		return false;
	}

	private boolean selectExportDirectory() {
		DirectoryDialog dirDialog = new DirectoryDialog(shlExport, SWT.SINGLE);
		if (dirDialog.open() != null) {
			String directoryName = dirDialog.getFilterPath();
			exportDirectory = new File(directoryName);
			return true;
		}
		return false;
	}

	private void readSourceZipFile(String importZipFile) {
		try {
			transferSourceTree.removeAll();
			String uniqueID = "Import_" + UUID.randomUUID().toString();
			String baseImportDirectory = ".//temp//" + uniqueID;
			ZipUtils.unzip(importZipFile, baseImportDirectory);

			File dimensionImportPath = new File(baseImportDirectory + "//dim");
			if (dimensionImportPath.exists()) {
				for (File dimensionEntry : dimensionImportPath.listFiles()) {
					if (dimensionEntry.isFile()) {
						//System.out.println("Found dimension file " + dimensionEntry.getName());
						String dimensionName = dimensionEntry.getName().substring(0, dimensionEntry.getName().lastIndexOf('.'));
						String dimensionDirectoryString = dimensionImportPath.getAbsolutePath() + "//" + dimensionName;
						//System.out.println("Checking for dimension directory " + dimensionDirectoryString);
						
						File dimensionDir = new File(dimensionDirectoryString);
						if (dimensionDir.exists()) {
							//System.out.println("Found dimension directory " + dimensionDir);
							FileReader fr = new FileReader(dimensionEntry);
							BufferedReader br = new BufferedReader(fr);
							OrderedJSONObject dimensionJSON = new OrderedJSONObject(br);
							TransferDimension transferDimension = new TransferDimension(dimensionName, dimensionJSON);
							transferSpec.addDimension(transferDimension);
							br.close();
							for (File hierarchyEntry : dimensionDir.listFiles()) {
								if (hierarchyEntry.isFile()){
									//System.out.println("Found hierarchy file " + hierarchyEntry.getName());
									String hierarchyName = hierarchyEntry.getName().substring(0, hierarchyEntry.getName().lastIndexOf('.'));
									String hiearchyDirectoryName = dimensionDir + "//" + hierarchyName;
									FileReader hierarchyFileReader = new FileReader(hierarchyEntry);
									BufferedReader hierarchyBufferedReader = new BufferedReader(hierarchyFileReader);
									OrderedJSONObject hierarchyJSON = new OrderedJSONObject(hierarchyBufferedReader);
									TransferHierarchy transferHierarchy = new TransferHierarchy(hierarchyName, dimensionName, hierarchyJSON);
									transferDimension.addHierarchy(transferHierarchy);
									hierarchyBufferedReader.close();
									File hierarchyDirectory = new File(hiearchyDirectoryName);
									if (hierarchyDirectory.exists()){
										//System.out.println("Found hierarchy directory " + hierarchyDirectory);
										for (File subsetEntry : hierarchyDirectory.listFiles()) {
											if (subsetEntry.isFile()){
												String subsetName = subsetEntry.getName().substring(0, subsetEntry.getName().lastIndexOf('.'));
												FileReader subsetFileReader = new FileReader(subsetEntry);
												BufferedReader subsetBufferedReader = new BufferedReader(subsetFileReader);
												OrderedJSONObject subsetJSON = new OrderedJSONObject(subsetBufferedReader);
												TransferSubset transferSubset = new TransferSubset(subsetName, hierarchyName, dimensionName, subsetJSON);
												transferHierarchy.addSubset(transferSubset);
												subsetBufferedReader.close();
												//System.out.println("Found subset file " + subsetEntry.getName());
											}
										}
									} else {
										System.out.println("Failed to find hierarchy directory " + hierarchyDirectory);
									}
								}
							}
						} else {
							System.out.println("Failed to find dimension directory " + dimensionDir);
						}
						//transferSpec.writeAll();
						
					} 
				}
			}
			
			File cubeDirectory = new File(baseImportDirectory + "//cub");
			if (cubeDirectory.exists()) {
				for (File cubeEntry : cubeDirectory.listFiles()) {
					if (cubeEntry.isFile() && cubeEntry.getName().endsWith(".cube")) {
						String cubeName = cubeEntry.getName().substring(0, cubeEntry.getName().lastIndexOf('.'));
						FileReader fr = new FileReader(cubeEntry);
						BufferedReader br = new BufferedReader(fr);
						OrderedJSONObject cubeJSON = new OrderedJSONObject(br);
						TransferCube transferCube = new TransferCube(cubeName, cubeJSON);
						transferSpec.addCube(transferCube);
						//System.out.println("Found cube file " + cubeEntry.getAbsolutePath());
					} else {
						//System.out.println("Filed to find cube file " + cubeEntry.getAbsolutePath());
					}
				}
			}
			
			File processDirectory = new File(baseImportDirectory + "//pro");
			if (processDirectory.exists()) {
				for (File processEntry : processDirectory.listFiles()) {
					if (processEntry.isFile() && processEntry.getName().endsWith(".pro")) {
						String processName = processEntry.getName().substring(0, processEntry.getName().lastIndexOf('.'));
						FileReader fr = new FileReader(processEntry);
						BufferedReader br = new BufferedReader(fr);
						OrderedJSONObject processJSON = new OrderedJSONObject(br);
						TransferProcess transferProcess = new TransferProcess(processName, processJSON);
						transferSpec.addProcess(transferProcess);
						//System.out.println("Found process file " + processEntry.getAbsolutePath());
					} else {
						//System.out.println("Failed to find process file " + processEntry.getAbsolutePath());
					}
				}
			}
			
			File choreDirectory = new File(baseImportDirectory + "//cho");
			if (choreDirectory.exists()) {
				for (File choreEntry : choreDirectory.listFiles()) {
					if (choreEntry.isFile() && choreEntry.getName().endsWith(".cho")) {
						String choreName = choreEntry.getName().substring(0, choreEntry.getName().lastIndexOf('.'));
						FileReader fr = new FileReader(choreEntry);
						BufferedReader br = new BufferedReader(fr);
						OrderedJSONObject choreJSON = new OrderedJSONObject(br);
						TransferChore transferChore = new TransferChore(choreName, choreJSON);
						transferSpec.addChore(transferChore);
						//System.out.println("Found chore file " + choreEntry.getName());
					}
				}
			}
			
			updateFileImportTree();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateFileExportTree() throws TM1RestException {
		transferTargetTree.removeAll();

		if (fileTransferHelper.cubeCount() > 0) {
			TreeItem cubesnode = new TreeItem(transferTargetTree, SWT.NONE);
			cubesnode.setText("Cubes");
			cubesnode.setImage(CUBEICON);
			cubesnode.setData("Cubes");
			updateCubeListTransferNode(cubesnode);
			if (fileTransferHelper.cubesexpanded) {
				cubesnode.setExpanded(true);
			}
		}

		if (fileTransferHelper.dimensionCount() > 0) {
			TreeItem dimsnode = new TreeItem(transferTargetTree, SWT.NONE);
			dimsnode.setText("Dimensions");
			dimsnode.setImage(DIMICON);
			dimsnode.setData("Dimensions");
			updateDimensionListTransferNode(dimsnode);
			if (fileTransferHelper.dimensionsexpanded) {
				dimsnode.setExpanded(true);
			}
		}

		if (fileTransferHelper.processCount() > 0) {
			TreeItem processesnode = new TreeItem(transferTargetTree, SWT.NONE);
			processesnode.setText("Processes");
			processesnode.setImage(PROICON);
			processesnode.setData("Processes");
			updateProcessListTransferNode(processesnode);
			if (fileTransferHelper.processesexpanded) {
				processesnode.setExpanded(true);
			}
		}

		if (fileTransferHelper.choreCount() > 0) {
			TreeItem choresnode = new TreeItem(transferTargetTree, SWT.NONE);
			choresnode.setText("Chores");
			choresnode.setImage(CHOICON);
			choresnode.setData("Chores");
			updateChoreListTransferNode(choresnode);
			if (fileTransferHelper.choresexpanded) {
				choresnode.setExpanded(true);
			}
		}

		for (TreeColumn tc : transferTargetTree.getColumns())
			tc.pack();
		transferTargetTree.redraw();

		// TreeItem blobsnode = new TreeItem(transferSourceTree, SWT.NONE);
		// blobsnode.setText("Blobs");
		// blobsnode.setImage(FOLDERICON);
		// blobsnode.setData("Blobs");
		// updateBlobListNode(blobsnode);
	}

	public void updateCubeListTransferNode(TreeItem cubesnode) {
		cubesnode.removeAll();
		int count = fileTransferHelper.cubeCount();
		for (int i = 0; i < count; i++) {
			TM1Cube cube = fileTransferHelper.getCube(i);
			TreeItem cubenode = new TreeItem(cubesnode, SWT.NONE);
			cubenode.setText(cube.displayName);
			cubenode.setImage(CUBEICON);
			cubenode.setData(cube);
			if (cube.expandedInExplorerTree) {
				updateCubeNode(cubenode);
				cubenode.setExpanded(true);
			} else {
				TreeItem cubeChildNode = new TreeItem(cubenode, SWT.NONE);
				cubeChildNode.setText("");
			}
		}
		cubesnode.setText("Cubes");
	}

	public void updateDimensionListTransferNode(TreeItem dimsnode) throws TM1RestException {
		dimsnode.removeAll();
		int count = fileTransferHelper.dimensionCount();
		for (int i = 0; i < count; i++) {
			TM1Dimension dimension = fileTransferHelper.getDimension(i);
			String dimensionname = dimension.displayName;
			TreeItem dimensionnode = new TreeItem(dimsnode, SWT.NONE);
			dimensionnode.setImage(DIMICON);
			dimensionnode.setData(dimension);
			dimensionnode.setText(dimensionname);

			if (dimension.expandedInExplorerTree) {
				updateDimensionNode(dimensionnode);
				dimensionnode.setExpanded(true);
			} else {
				TreeItem dimensionChildNode = new TreeItem(dimensionnode, SWT.NONE);
				dimensionChildNode.setText("");
			}
		}
		dimsnode.setText("Dimensions");
	}

	public void updateProcessListTransferNode(TreeItem processListNode) {
		processListNode.removeAll();
		int count = fileTransferHelper.processCount();
		for (int i = 0; i < count; i++) {
			TM1Process process = fileTransferHelper.getProcess(i);
			String processname = process.displayName;
			TreeItem processnode = new TreeItem(processListNode, SWT.NONE);
			processnode.setText(processname);
			processnode.setImage(PROICON);
			processnode.setData(process);
		}
		processListNode.setText("Procesess");
	}

	public void updateChoreListTransferNode(TreeItem choreListNode) {
		choreListNode.removeAll();
		int chorecount = fileTransferHelper.choreCount();
		choreListNode.setText("Chores");
		for (int i = 0; i < chorecount; i++) {
			TM1Chore chore = fileTransferHelper.getChore(i);
			String chorename = chore.displayName;
			TreeItem chorenode = new TreeItem(choreListNode, SWT.NONE);
			chorenode.setText(chorename);
			chorenode.setImage(CHOICON);
			chorenode.setData(chore);
		}
	}

	private void setSourceTreeColumnSize() {
		int columnCount = transferSourceTree.getColumnCount();
		if (columnCount == 0) return;
		Rectangle area = transferSourceTree.getClientArea();
		int totalAreaWdith = area.width;
		int lineWidth = transferSourceTree.getGridLineWidth();
		int totalGridLineWidth = (columnCount - 1) * lineWidth;
		int totalColumnWidth = 0;
		for (TreeColumn column : transferSourceTree.getColumns()) {
			totalColumnWidth = totalColumnWidth + column.getWidth();
		}
		int diff = totalAreaWdith - (totalColumnWidth + totalGridLineWidth);
		TreeColumn lastCol = transferSourceTree.getColumns()[columnCount - 1];
		lastCol.setWidth(diff + lastCol.getWidth());
	}
	
	private void updateFileImportTree(){
		
		if (transferSpec.getCubeCount() > 0){
			TreeItem cubesParentNode = new TreeItem(transferSourceTree, SWT.NONE);
			cubesParentNode.setText("Cubes");
			cubesParentNode.setImage(CUBEICON);
			for (int i=0; i<transferSpec.getCubeCount(); i++){
				TransferCube cube = transferSpec.getCube(i);
				TreeItem cubeNode = new TreeItem(cubesParentNode, SWT.NONE);
				cubeNode.setText(cube.name);
				cubeNode.setImage(CUBEICON);
				cubeNode.setData(cube);
			}
		}

		if (transferSpec.getDimensionCount() > 0){
			TreeItem dimParentNode = new TreeItem(transferSourceTree, SWT.NONE);
			dimParentNode.setText("Dimensions");
			dimParentNode.setImage(DIMICON);
			for (int i=0; i<transferSpec.getDimensionCount(); i++){
				TransferDimension dimension = transferSpec.getDimension(i);
				TreeItem dimNode = new TreeItem(dimParentNode, SWT.NONE);
				dimNode.setText(dimension.name);
				dimNode.setImage(DIMICON);
				dimNode.setData(dimension);
				for (int j=0; j<dimension.getHierarchyCount(); j++){
					TransferHierarchy hierarchy = dimension.getHierarchy(j);
					TreeItem hierarchyNode = new TreeItem(dimNode, SWT.NONE);
					hierarchyNode.setText(hierarchy.name);
					hierarchyNode.setImage(HIERICON);
					hierarchyNode.setData(hierarchy);
					for (int k=0; k<hierarchy.getSubsetCount(); k++){
						TransferSubset subset = hierarchy.getSubset(k);
						TreeItem subsetNode = new TreeItem(hierarchyNode, SWT.NONE);
						subsetNode.setText(subset.name);
						subsetNode.setImage(SUBICON);
						subsetNode.setData(subset);
					}
				}
			}
			dimParentNode.setChecked(true);
		}
		
		if (transferSpec.getProcessCount() > 0){
			TreeItem processParentNode = new TreeItem(transferSourceTree, SWT.NONE);
			processParentNode.setText("Processes");
			processParentNode.setImage(PROICON);
			for (int i=0; i<transferSpec.getProcessCount(); i++){
				TransferProcess process = transferSpec.getProcess(i);
				TreeItem processNode = new TreeItem(processParentNode, SWT.NONE);
				processNode.setText(process.name);
				processNode.setImage(PROICON);
				processNode.setData(process);
			}
		}
		
		if (transferSpec.getChoreCount() > 0){
			TreeItem choreParentNode = new TreeItem(transferSourceTree, SWT.NONE);
			choreParentNode.setText("Chores");
			choreParentNode.setImage(CHOICON);
			for (int i=0; i<transferSpec.getChoreCount(); i++){
				TransferChore chore = transferSpec.getChore(i);
				TreeItem choreNode = new TreeItem(choreParentNode, SWT.NONE);
				choreNode.setText(chore.name);
				choreNode.setImage(CHOICON);
				choreNode.setData(chore);
			}
		}

	}
	
	private void runTransfer(){
		try {
			transferSpec.runTransfer();
		} catch (TM1RestException | URISyntaxException | IOException ex) {
			// TODO Auto-generated catch block
			MessageBox m = new MessageBox(this.shlExport, SWT.ERROR);
			m.setText("Import Error");
			m.setMessage(ex.toString());
			m.open();
			
		}
	}

}
