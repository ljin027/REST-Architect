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

import org.apache.http.client.ClientProtocolException;
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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import TM1Diagnostic.TransferSpec;

import TM1Diagnostic.ZipUtils;
import TM1Diagnostic.REST.TM1Blob;
import TM1Diagnostic.REST.TM1Chore;
import TM1Diagnostic.REST.TM1Cube;
import TM1Diagnostic.REST.TM1Dimension;
import TM1Diagnostic.REST.TM1Element;
import TM1Diagnostic.REST.TM1Folder;
import TM1Diagnostic.REST.TM1Hierarchy;
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
import org.eclipse.swt.events.DragDetectListener;
import org.eclipse.swt.events.DragDetectEvent;

public class TransferEditor extends Dialog {

	final int EDITABLECOLUMN = 1;

	static private int IMPORT = 1;
	static private int EXPORT = 2;
	static private int TRANSFER = 3;

	protected boolean refreshServerTreeOnExit;
	protected Shell shlExport;

	private Tree transferSourceTree;
	private Tree transferTargetTree;

	private Button transferButton;
	private Combo sourceCombo;
	private Combo targetCombo;
	private Button controlObjectsCheckButton;

	private TM1Server tm1server;
	private List<TreeItem> export_items;
	private File importFile;
	private File exportFile;
	private boolean showControlObjects;

	private List<Object> transferObjects;

	private TransferSpec transferSpec;

	static private Image FOLDERICON;

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

	public TransferEditor(Shell parent, TM1Server tm1server) {
		super(parent, SWT.DIALOG_TRIM);
		setText("SWT Dialog");
		this.tm1server = tm1server;
		export_items = new ArrayList<TreeItem>();
		transferObjects = new ArrayList<Object>();
		transferSpec = new TransferSpec();
	}

	public boolean open() throws TM1RestException {
		refreshServerTreeOnExit = false;
		showControlObjects = false;
		createContents();
		shlExport.open();
		shlExport.layout();
		Display display = getParent().getDisplay();
		while (!shlExport.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return refreshServerTreeOnExit;
	}

	private void createContents() throws TM1RestException {
		shlExport = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.MAX | SWT.RESIZE | SWT.APPLICATION_MODAL);
		shlExport.setSize(918, 519);
		shlExport.setText("Transfer Manager");
		shlExport.setLayout(new GridLayout(1, false));

		Display display = shlExport.getDisplay();

		FOLDERICON = new Image(display, ".\\images\\icon_folder.gif");
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

		Composite composite_2 = new Composite(shlExport, SWT.NONE);
		composite_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		composite_2.setLayout(new GridLayout(3, false));

		Label label1 = new Label(composite_2, SWT.NONE);
		label1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label1.setText("Source");

		sourceCombo = new Combo(composite_2, SWT.READ_ONLY);
		sourceCombo.add("Select File");
		sourceCombo.add("Model: " + tm1server.getName());
		sourceCombo.setText("");
		sourceCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String s = (String) sourceCombo.getItem(sourceCombo.getSelectionIndex());
				if (s.equals("Select File")) {
					// Configure to transfer from files to model
					transferSpec = new TransferSpec();
					FileDialog filedialog = new FileDialog(shlExport, SWT.SINGLE);
					filedialog.setFilterExtensions(new String[] { "*.*" });
					if (filedialog.open() != null) {
						String filename = filedialog.getFilterPath() + "//" + filedialog.getFileName();
						importFile = new File(filename);
						sourceCombo.add(importFile.getAbsolutePath());
						sourceCombo.setText(importFile.getAbsolutePath());
						transferSpec.readSourceZipFile(importFile.getAbsolutePath());

						transferSpec.setImport(importFile, tm1server);

						targetCombo.setEnabled(false);
						targetCombo.removeAll();
						targetCombo.add("Model: " + tm1server.getName());
						targetCombo.setText("Model: " + tm1server.getName());
						updateFileImportTree();
					}
				} else if (s.equals("Model: " + tm1server.getName())) {
					// Configure to transfer from model to file or model
					transferSpec.setSourceModel(tm1server);
					sourceCombo.setEnabled(false);
					targetCombo.setEnabled(true);
					targetCombo.removeAll();
					targetCombo.add("Select Directory");
					targetCombo.add("Select Model");

					updateTM1ServerExportTree();
				}
			}
		});
		sourceCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		controlObjectsCheckButton = new Button(composite_2, SWT.CHECK);
		controlObjectsCheckButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				showControlObjects = controlObjectsCheckButton.getSelection();

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
				if (targetCombo.getText().equals("Select Directory")) {
					FileDialog filedialog = new FileDialog(shlExport, SWT.OPEN);
					String[] filterExt = { "*.zip" };
					filedialog.setFilterExtensions(filterExt);
					filedialog.open();
					String exportDirectory = filedialog.getFilterPath();
					String exportFileName = filedialog.getFileName();
					File exportFile = new File(exportDirectory + "//" + exportFileName + ".zip");

					System.out.println("Export file is " + exportFile.getAbsolutePath());

					targetCombo.removeAll();
					targetCombo.add("File: " + exportFile.getAbsolutePath());
					targetCombo.setText("File: " + exportFile.getAbsolutePath());
					transferSpec.setExport(tm1server, exportFile);

				} else if (targetCombo.getText().equals("Select Model")) {
					System.out.println("Select model for transfer");

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

		transferSourceTree = new Tree(sashForm, SWT.BORDER | SWT.MULTI);

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
					TreeItem treeItem = (TreeItem) event.item;
					try {
						checkSourceItem(treeItem);
					} catch (TM1RestException | URISyntaxException | IOException | JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		});

		Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
		DragSource source = new DragSource(transferSourceTree, DND.DROP_MOVE);
		source.setTransfer(types);

		TreeColumn trclmnNewColumn = new TreeColumn(transferSourceTree, SWT.NONE);
		trclmnNewColumn.setText("Source Object");

		transferObjects = new ArrayList<Object>();

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
						if (selection[i].getData() instanceof TM1Dimension) {
							transferObjects.add((Object) selection[i].getData());
						} else if (selection[i].getData() instanceof TM1Cube) {
							transferObjects.add((Object) selection[i].getData());
						} else if (selection[i].getData() instanceof TM1Process) {
							transferObjects.add((Object) selection[i].getData());
						} else if (selection[i].getData() instanceof TM1Chore) {
							transferObjects.add((Object) selection[i].getData());
						} else if (selection[i].getData() instanceof String) {
							String objectType = (String)selection[i].getData();
							//System.out.println("Transfer string " + objectType);
							if (objectType.equals("Cubes")) {
								for (int j=0; j<selection[i].getItemCount(); j++) {
									TreeItem processTreeItem = selection[i].getItem(j);
									transferObjects.add((Object) processTreeItem.getData());
								}
							}
							if (objectType.equals("Dimensions")) {
								for (int j=0; j<selection[i].getItemCount(); j++) {
									TreeItem processTreeItem = selection[i].getItem(j);
									transferObjects.add((Object) processTreeItem.getData());
								}
							}
							if (objectType.equals("Processes")) {
								for (int j=0; j<selection[i].getItemCount(); j++) {
									TreeItem processTreeItem = selection[i].getItem(j);
									transferObjects.add((Object) processTreeItem.getData());
								}
							}
							if (objectType.equals("Chores")) {
								for (int j=0; j<selection[i].getItemCount(); j++) {
									TreeItem processTreeItem = selection[i].getItem(j);
									transferObjects.add((Object) processTreeItem.getData());
								}
							}
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
					/*
					 * for (int i = 0; i < dragSourceItems.size(); i++) { TreeItem t =
					 * dragSourceItems.get(i); t.dispose(); t = null; }
					 */
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

		DropTarget dropTarget = new DropTarget(transferTargetTree, DND.DROP_MOVE);
		dropTarget.setTransfer(types);

		dropTarget.addDropListener(new DropTargetAdapter() {

			public void dragOver(DropTargetEvent event) {
				// System.out.println("Drag over target tree");
				event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
				if (event.item != null) {
					Display display = shlExport.getDisplay();
				}
			}

			public void drop(DropTargetEvent event) {
				try {
					DropTarget target = (DropTarget) event.widget;
					for (int i = 0; i < transferObjects.size(); i++) {
						System.out.println("Adding object " + i + " out of " + transferObjects.size());
						Object obj = transferObjects.get(i);
						if (obj instanceof TM1Dimension) {
							TM1Dimension dimension = (TM1Dimension) obj;
							transferSpec.addDimension(dimension);
							//System.out.println("Dimension " + dimension.name);
						}
						if (obj instanceof TM1Cube) {
							TM1Cube cube = (TM1Cube) obj;
							transferSpec.addCube(cube);
							//System.out.println("Cube " + cube.name);
						}
						if (obj instanceof TM1Process) {
							TM1Process process = (TM1Process) obj;
							transferSpec.addProcess(process);
							//System.out.println("Process " + process.name);
						}
						if (obj instanceof TM1Chore) {
							TM1Chore chore = (TM1Chore) obj;
							transferSpec.addChore(chore);
							//System.out.println("Chore " + chore.name);
						}
					}
					updateTargetTree();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		TreeColumn trclmnNewColumn_1 = new TreeColumn(transferTargetTree, SWT.NONE);
		trclmnNewColumn_1.setWidth(169);
		trclmnNewColumn_1.setText("Target Object");

		TreeColumn trclmnNewColumn_2 = new TreeColumn(transferTargetTree, SWT.NONE);
		trclmnNewColumn_2.setWidth(171);
		trclmnNewColumn_2.setText("Options");
		sashForm.setWeights(new int[] { 337, 540 });

		Composite composite_1 = new Composite(shlExport, SWT.NONE);
		composite_1.setLayout(new GridLayout(2, false));
		composite_1.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, false, 1, 1));

		Button cancelButton = new Button(composite_1, SWT.NONE);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (transferButton.getEnabled()) {
					refreshServerTreeOnExit = false;
				} else {
					refreshServerTreeOnExit = true;
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

	}

	private void checkSourceItem(TreeItem checkedTreeItem) throws ClientProtocolException, TM1RestException, URISyntaxException, IOException, JSONException {
		if (checkedTreeItem.getData() instanceof Object) {
			// transferSpec.addModelObject((Object) checkedTreeItem.getData());
		}
		for (int i = 0; i < checkedTreeItem.getItemCount(); i++) {
			TreeItem treeItemChild = checkedTreeItem.getItem(i);
			treeItemChild.setChecked(true);
			checkSourceItem(treeItemChild);
		}
	}

	public void expand(TreeItem t) {
		t.setExpanded(true);
		for (int i = 0; i < t.getItemCount(); i++) {
			expand(t.getItem(i));
		}
	}

	public void updateTargetTree() {
		try {
			System.out.println("Updating target tree");
			transferTargetTree.removeAll();

			if (transferSpec.getCubeCount() > 0) {
				TreeItem cubesnode = new TreeItem(transferTargetTree, SWT.NONE);
				cubesnode.setText("Cubes");
				cubesnode.setImage(CUBEICON);
				cubesnode.setData("Cubes");
				
				for (int i = 0; i < transferSpec.getCubeCount(); i++) {
					TM1Cube cube = transferSpec.getCube(i);
					TreeItem cubenode = new TreeItem(cubesnode, SWT.NONE);
					cubenode.setText(cube.name);
					cubenode.setImage(CUBEICON);
					cubenode.setData(cube);

					cube.readCubeViewsFromServer();
					
					TreeItem viewsnode = new TreeItem(cubenode, SWT.NONE);
					viewsnode.setText("Views");
					viewsnode.setData("Views");
					viewsnode.setImage(VIEWICON);
					for (int j = 0; j < cube.viewCount(); j++) {
						TM1View view = cube.getview(j);
						TreeItem viewnode = new TreeItem(viewsnode, SWT.NONE);
						viewnode.setImage(VIEWICON);
						viewnode.setText(view.name);
						viewnode.setData(view);
					}
					if (cube.checkServerForRules()) {
						TreeItem rulenode = new TreeItem(cubenode, SWT.NONE);
						rulenode.setText("Rules");
						rulenode.setData("Rules");
						rulenode.setImage(RULEICON);
					}
				}
				cubesnode.setExpanded(true);
			}

			if (transferSpec.getDimensionCount() > 0) {
				TreeItem dimsnode = new TreeItem(transferTargetTree, SWT.NONE);
				dimsnode.setText("Dimensions");
				dimsnode.setImage(DIMICON);
				dimsnode.setData("Dimensions");
				for (int i = 0; i < transferSpec.getDimensionCount(); i++) {
					TM1Dimension dimension = transferSpec.getDimension(i);
					String dimensionname = dimension.name;
					TreeItem dimensionnode = new TreeItem(dimsnode, SWT.NONE);
					dimensionnode.setImage(DIMICON);
					dimensionnode.setData(dimension);
					dimensionnode.setText(dimensionname);
				}
				dimsnode.setExpanded(true);
			}

			if (transferSpec.getProcessCount() > 0) {
				TreeItem processesnode = new TreeItem(transferTargetTree, SWT.NONE);
				processesnode.setText("Processes");
				processesnode.setImage(PROICON);
				processesnode.setData("Processes");
				for (int i = 0; i < transferSpec.getProcessCount(); i++) {
					TM1Process process = transferSpec.getProcess(i);
					String processname = process.name;
					TreeItem processnode = new TreeItem(processesnode, SWT.NONE);
					processnode.setText(processname);
					processnode.setImage(PROICON);
					processnode.setData(process);
				}
				processesnode.setExpanded(true);
			}

			if (transferSpec.getChoreCount() > 0) {
				TreeItem choresnode = new TreeItem(transferTargetTree, SWT.NONE);
				choresnode.setText("Chores");
				choresnode.setImage(CHOICON);
				choresnode.setData("Chores");
				for (int i = 0; i < transferSpec.getChoreCount(); i++) {
					TM1Chore chore = transferSpec.getChore(i);
					String chorename = chore.name;
					TreeItem chorenode = new TreeItem(choresnode, SWT.NONE);
					chorenode.setText(chorename);
					chorenode.setImage(CHOICON);
					chorenode.setData(chore);
				}
				choresnode.setExpanded(true);
			}

			for (TreeColumn tc : transferTargetTree.getColumns())
				tc.pack();
			transferTargetTree.redraw();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void updateTM1ServerExportTree() {
		try {
			transferSourceTree.removeAll();

			if (tm1server.cubeCount() > 0) {
				TreeItem cubesnode = new TreeItem(transferSourceTree, SWT.NONE);
				cubesnode.setText("Cubes");
				cubesnode.setImage(CUBEICON);
				cubesnode.setData("Cubes");
				int count = tm1server.cubeCount();
				for (int i = 0; i < count; i++) {
					TM1Cube cube = tm1server.getCube(i);
					if (!showControlObjects && cube.name.startsWith("}")) {
					} else {
						TreeItem cubenode = new TreeItem(cubesnode, SWT.NONE);
						cubenode.setText(cube.name);
						cubenode.setImage(CUBEICON);
						cubenode.setData(cube);

						cube.readCubeDimensionsFromServer();
						TreeItem cubeDimensiosNode = new TreeItem(cubenode, SWT.NONE);
						cubeDimensiosNode.setText("Dimensions");
						cubeDimensiosNode.setData("Dimensions");
						cubeDimensiosNode.setImage(DIMICON);
						for (int j = 0; j < cube.dimensionCount(); j++) {
							TM1Dimension dimension = cube.getDimension(j);
							TreeItem dimensionNode = new TreeItem(cubeDimensiosNode, SWT.NONE);
							dimensionNode.setImage(DIMICON);
							dimensionNode.setText(dimension.name);
							dimensionNode.setData(dimension);
						}
						cube.readCubeViewsFromServer();
						TreeItem viewsnode = new TreeItem(cubenode, SWT.NONE);
						viewsnode.setText("Views");
						viewsnode.setData("Views");
						viewsnode.setImage(VIEWICON);
						for (int j = 0; j < cube.viewCount(); j++) {
							TM1View view = cube.getview(j);
							TreeItem viewnode = new TreeItem(viewsnode, SWT.NONE);
							viewnode.setImage(VIEWICON);
							viewnode.setText(view.name);
							viewnode.setData(view);
						}

						if (cube.checkServerForRules()) {
							TreeItem rulenode = new TreeItem(cubenode, SWT.NONE);
							rulenode.setText("Rules");
							rulenode.setData("Rules");
							rulenode.setImage(RULEICON);
						}
					}
				}
				cubesnode.setExpanded(true);
			}

			if (tm1server.dimensionCount() > 0) {
				TreeItem dimensionsnode = new TreeItem(transferSourceTree, SWT.NONE);
				dimensionsnode.setText("Dimensions");
				dimensionsnode.setImage(DIMICON);
				dimensionsnode.setData("Dimensions");
				for (int i = 0; i < tm1server.dimensionCount(); i++) {
					TM1Dimension dimension = tm1server.getDimension(i);
					String dimensionname = dimension.name;
					if (!showControlObjects && dimensionname.startsWith("}")) {
					} else {
						TreeItem dimensionnode = new TreeItem(dimensionsnode, SWT.NONE);
						dimensionnode.setImage(DIMICON);
						dimensionnode.setData(dimension);
						dimensionnode.setText(dimensionname);
					}
				}
				dimensionsnode.setExpanded(true);
			}

			if (tm1server.processCount() > 0) {
				TreeItem processesnode = new TreeItem(transferSourceTree, SWT.NONE);
				processesnode.setText("Processes");
				processesnode.setImage(PROICON);
				processesnode.setData("Processes");
				for (int i = 0; i < tm1server.processCount(); i++) {
					TM1Process process = tm1server.getProcess(i);
					String processname = process.name;
					if (!showControlObjects && processname.startsWith("}")) {
					} else {
						TreeItem processnode = new TreeItem(processesnode, SWT.NONE);
						processnode.setText(processname);
						processnode.setImage(PROICON);
						processnode.setData(process);
					}
				}
				processesnode.setExpanded(true);
			}

			if (tm1server.choreCount() > 0) {
				TreeItem choresnode = new TreeItem(transferSourceTree, SWT.NONE);
				choresnode.setText("Chores");
				choresnode.setImage(CHOICON);
				choresnode.setData("Chores");
				for (int i = 0; i < tm1server.choreCount(); i++) {
					TM1Chore chore = tm1server.getChore(i);
					String chorename = chore.name;
					TreeItem chorenode = new TreeItem(choresnode, SWT.NONE);
					chorenode.setText(chorename);
					chorenode.setImage(CHOICON);
					chorenode.setData(chore);
				}
				choresnode.setExpanded(true);
			}

			for (TreeColumn tc : transferSourceTree.getColumns()) tc.pack();
			transferSourceTree.redraw();
		} catch (Exception ex) {
			ex.printStackTrace();
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
				String hierarchyname = hierarchy.name;
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
			String subsetname = subset.name;
			TreeItem subsetnode = new TreeItem(subsetListNode, SWT.NONE);
			subsetnode.setText(subsetname);
			subsetnode.setImage(SUBICON);
			subsetnode.setData(subset);
		}

		int subsetcount = hierarchy.subsetCount();
		for (int i = 0; i < subsetcount; i++) {
			TM1Subset subset = hierarchy.getSubset(i);
			String subsetname = subset.name;
			TreeItem subsetnode = new TreeItem(subsetListNode, SWT.NONE);
			subsetnode.setText(subsetname);
			subsetnode.setImage(PRIVSUBICON);
			subsetnode.setData(subset);
		}

		subsetListNode.setText("Subsets");
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
				String blobname = blob.name;
				TreeItem blobnode = new TreeItem(blobsnode, SWT.NONE);
				blobnode.setText(blobname);
				blobnode.setImage(CHOICON);
				blobnode.setData(blob);
			}
		} catch (URISyntaxException | IOException | TM1RestException | JSONException ex) {
			ex.printStackTrace();
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
				String foldername = folder.name;
				TreeItem appnode = new TreeItem(applicationsnode, SWT.NONE);
				appnode.setText(foldername);
				appnode.setImage(FOLDERICON);
				appnode.setData(folder);
				updateApplicationNode(appnode);
			}
		} catch (URISyntaxException | IOException | TM1RestException | JSONException ex) {
			ex.printStackTrace();
		}
	}

	public void updateApplicationNode(TreeItem applicationNode) {
		applicationNode.removeAll();
		TM1Folder tm1folder = (TM1Folder) applicationNode.getData();
		for (int i = 0; i < tm1folder.getFolderCount(); i++) {
			TM1Folder folder = tm1folder.getFolder(i);
			TreeItem childNode = new TreeItem(applicationNode, SWT.NONE);
			childNode.setText(folder.name);
			childNode.setImage(FOLDERICON);
			childNode.setData(folder);
			updateApplicationNode(childNode);
		}
		for (int i = 0; i < tm1folder.getReferenceCount(); i++) {
			TM1ObjectReference reference = tm1folder.getReference(i);
			TreeItem childNode = new TreeItem(applicationNode, SWT.NONE);
			childNode.setText(reference.name);
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

	/*
	 * public void getselectedobjects(TreeItem t) { int childcount =
	 * t.getItemCount(); if (t.getData() instanceof TM1Object) { TM1Object
	 * export_object = (TM1Object) t.getData(); if (t.getChecked()) {
	 * export_items.add(t); } } for (int i = 0; i < childcount; i++) { TreeItem
	 * child = t.getItem(i); getselectedobjects(child); } }
	 */

	private void setSourceTreeColumnSize() {
		int columnCount = transferSourceTree.getColumnCount();
		if (columnCount == 0)
			return;
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

	private void updateFileImportTree() {

		if (transferSpec.getCubeCount() > 0) {
			TreeItem cubesParentNode = new TreeItem(transferSourceTree, SWT.NONE);
			cubesParentNode.setText("Cubes");
			cubesParentNode.setImage(CUBEICON);
			cubesParentNode.setChecked(true);
			for (int i = 0; i < transferSpec.getCubeCount(); i++) {
				TM1Cube cube = transferSpec.getCube(i);
				TreeItem cubeNode = new TreeItem(cubesParentNode, SWT.NONE);
				cubeNode.setText(cube.name);
				cubeNode.setImage(CUBEICON);
				cubeNode.setData(cube);
				cubeNode.setChecked(true);
			}
			cubesParentNode.setExpanded(true);
		}

		if (transferSpec.getDimensionCount() > 0) {
			TreeItem dimParentNode = new TreeItem(transferSourceTree, SWT.NONE);
			dimParentNode.setText("Dimensions");
			dimParentNode.setImage(DIMICON);
			dimParentNode.setChecked(true);
			for (int i = 0; i < transferSpec.getDimensionCount(); i++) {
				TM1Dimension dimension = transferSpec.getDimension(i);
				TreeItem dimNode = new TreeItem(dimParentNode, SWT.NONE);
				dimNode.setText(dimension.name);
				dimNode.setImage(DIMICON);
				dimNode.setData(dimension);
				dimNode.setChecked(true);
				for (int j = 0; j < dimension.hierarchyCount(); j++) {
					TM1Hierarchy hierarchy = dimension.getHeirarchy(j);
					TreeItem hierarchyNode = new TreeItem(dimNode, SWT.NONE);
					hierarchyNode.setText(hierarchy.name);
					hierarchyNode.setImage(HIERICON);
					hierarchyNode.setData(hierarchy);
					hierarchyNode.setChecked(true);
					/*
					 * for (int k = 0; k < hierarchy.getSubsetCount(); k++) { TransferSubset subset
					 * = hierarchy.getSubset(k); TreeItem subsetNode = new TreeItem(hierarchyNode,
					 * SWT.NONE); subsetNode.setText(subset.name); subsetNode.setImage(SUBICON);
					 * subsetNode.setData(subset); }
					 */
				}
			}
			dimParentNode.setExpanded(true);
		}

		if (transferSpec.getProcessCount() > 0) {
			TreeItem processParentNode = new TreeItem(transferSourceTree, SWT.NONE);
			processParentNode.setText("Processes");
			processParentNode.setImage(PROICON);
			processParentNode.setChecked(true);
			for (int i = 0; i < transferSpec.getProcessCount(); i++) {
				TM1Process process = transferSpec.getProcess(i);
				TreeItem processNode = new TreeItem(processParentNode, SWT.NONE);
				processNode.setText(process.name);
				processNode.setImage(PROICON);
				processNode.setData(process);
				processNode.setChecked(true);
			}
			processParentNode.setExpanded(true);
		}

		if (transferSpec.getChoreCount() > 0) {
			TreeItem choreParentNode = new TreeItem(transferSourceTree, SWT.NONE);
			choreParentNode.setText("Chores");
			choreParentNode.setImage(CHOICON);
			choreParentNode.setChecked(true);
			for (int i = 0; i < transferSpec.getChoreCount(); i++) {
				TM1Chore chore = transferSpec.getChore(i);
				TreeItem choreNode = new TreeItem(choreParentNode, SWT.NONE);
				choreNode.setText(chore.name);
				choreNode.setImage(CHOICON);
				choreNode.setData(chore);
				choreParentNode.setChecked(true);
			}
			choreParentNode.setExpanded(true);
		}

	}

	private void runTransfer() {
		try {
			transferSpec.runTransfer();
			if (transferSpec.getTransferType() == TransferSpec.IMPORT) {
				this.refreshServerTreeOnExit = true;
				shlExport.close();
			} else if (transferSpec.getTransferType() == TransferSpec.EXPORT) {
				shlExport.close();
			}
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			MessageBox m = new MessageBox(this.shlExport, SWT.ERROR);
			m.setText("Error");
			m.setMessage(ex.toString());
			m.open();
		}
	}

}
