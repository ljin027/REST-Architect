package TM1Diagnostic.UI;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.wink.json4j.JSONException;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import TM1Diagnostic.REST.TM1Dimension;
import TM1Diagnostic.REST.TM1Element;
import TM1Diagnostic.REST.TM1Hierarchy;
import TM1Diagnostic.REST.TM1RestException;
import TM1Diagnostic.REST.TM1Subset;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.layout.FillLayout;

public class SubsetEditor {

	static public Image ICON_CONSOLIDATED;
	static public Image ICON_STRING;
	static public Image ICON_NUMERIC;
	static public Image ICON_SUBSETALL;
	static public Image ICON_DELETE;
	static public Image ICON_KEEP;

	static public String NUMERIC = "Numeric";
	static public String STRING = "String";
	static public String CONSOLIDATED = "Consolidated";

	public Menu menuBar, fileMenu, helpMenu;
	public MenuItem fileMenuHeader, helpMenuHeader;
	public MenuItem fileExitItem, fileSaveItem, helpGetHelpItem;

	private Shell parent;
	private Shell shell;

	private TM1Dimension dimension;
	private TM1Hierarchy hierarchy;
	private TM1Subset subset;
	private String selectedElement;

	protected boolean result;
	//private boolean refreshOnClose;
	private Tree hierarchyTree;
	private Table subsetTable;

	private boolean aliasEnabled;

	private Combo subsetSelectCombo;
	private Combo aliasCombo;

	private TreeColumn hierarchyTreeColumn1;
	private TreeColumn hierarchyTreeColumn2;
	private TreeColumn hierarchyTreeColumn3;

	private TableColumn subsetTableColumn1;
	private TableColumn subsetTableColumn2;
	private TableColumn subsetTableColumn3;

	private Menu subsetTableMenu;
	private Menu hierarchyTreeMenu;

	private List<TM1Element> selectedElements;
	private List<TM1Element> elementDNDList;

	private boolean singleSelect;
	private String selectedElementName;

	private Composite hierarchyComposite;
	private SashForm sashForm;
	private Button btnAlias;
	private Menu menu;
	private Composite composite;

	/**
	 * @throws Exception 
	 * @wbp.parser.constructor
	 */

	SubsetEditor(Shell parent, TM1Subset subset) throws Exception {
		this.subset = subset;
		this.hierarchy = subset.getHierarchy();
		this.dimension = hierarchy.dimension;
		selectedElements = new ArrayList<TM1Element>();
		elementDNDList = new ArrayList<TM1Element>();
		shell = new Shell(parent, SWT.RESIZE | SWT.MAX | SWT.MIN);
		shell.setSize(689, 448);
		//System.out.println("Update Subset Mode");
		shell.setText("Subset Editor - " + hierarchy.getParent().displayName + "/" + hierarchy.displayName + "/" + subset.displayName);
		createContents();
		shell.layout();
	}

	SubsetEditor(Shell parent, TM1Hierarchy hierarchy) throws Exception {
		this.hierarchy = hierarchy;
		this.dimension = hierarchy.dimension;
		selectedElements = new ArrayList<TM1Element>();
		elementDNDList = new ArrayList<TM1Element>();
		shell = new Shell(parent, SWT.RESIZE | SWT.MAX | SWT.MIN);
		shell.setSize(689, 448);
		shell.setText("Subset Editor - *New Subset");
		//System.out.println("New Subset Mode");
		createContents();
		//shell.layout();
	}

	SubsetEditor(Shell parent, TM1Dimension dimension, String selectedElement) throws Exception {
		this.dimension = dimension;
		hierarchy = dimension.getDefaultHierarchy();
		subset = hierarchy.readSubsetAllFromServer();
		this.selectedElement = selectedElement;
		singleSelect = true;
		selectedElements = new ArrayList<TM1Element>();
		elementDNDList = new ArrayList<TM1Element>();
		this.parent = parent;
		shell = new Shell(parent, SWT.RESIZE | SWT.MAX | SWT.MIN | SWT.PRIMARY_MODAL);
		shell.setSize(689, 448);
		shell.setText("Subset Editor - *New Subset");
		//System.out.println("Single Select Mode");
		createContents();
		//shell.layout();
	}


	private void createContents() throws Exception{
		shell.setLayout(new GridLayout(1, false));

		ICON_CONSOLIDATED = new Image(shell.getDisplay(), ".\\images\\consolidation.gif");
		ICON_NUMERIC = new Image(shell.getDisplay(), ".\\images\\numeric.gif");
		ICON_STRING = new Image(shell.getDisplay(), ".\\images\\string.gif");
		ICON_SUBSETALL = new Image(shell.getDisplay(), ".\\images\\subsetAll.gif");
		ICON_DELETE = new Image(shell.getDisplay(), ".\\images\\deleteElements.gif");
		ICON_KEEP = new Image(shell.getDisplay(), ".\\images\\keepElements.gif");

		Composite buttonComposite = new Composite(shell, SWT.BORDER);
		buttonComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		buttonComposite.setLayout(new GridLayout(4, false));
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));


		Label lblNewLabel = new Label(buttonComposite, SWT.NONE);
		lblNewLabel.setText("Subset");

		subsetSelectCombo = new Combo(buttonComposite, SWT.READ_ONLY);
		subsetSelectCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		subsetSelectCombo.removeAll();
		subsetSelectCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				try {
					subset = (TM1Subset) subsetSelectCombo.getData(subsetSelectCombo.getItem(subsetSelectCombo.getSelectionIndex()));
					subset.readElementListFromServer();
					refreshSubsetTable();
				} catch (TM1RestException | URISyntaxException | IOException | JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		Button remove_button = new Button(buttonComposite, SWT.NONE);
		remove_button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				removeElementsFromSubset();
			}
		});
		remove_button.setText("Remove");
		remove_button.setToolTipText("Remove Elements");
		remove_button.setImage(ICON_DELETE);

		Button keep_button = new Button(buttonComposite, SWT.NONE);
		keep_button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				keepElementsFromSubset();
			}
		});
		keep_button.setText("Keep");
		keep_button.setToolTipText("Keep Elements");
		keep_button.setImage(ICON_KEEP);

		btnAlias = new Button(buttonComposite, SWT.NONE);
		btnAlias.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				toggleAlias();
			}
		});

		btnAlias.setText("Alias");

		aliasCombo = new Combo(buttonComposite, SWT.READ_ONLY);
		aliasCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		aliasCombo.removeAll();
		aliasCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				subset.setAlias(aliasCombo.getText());
				refreshElementAliases();
			}
		});
		new Label(buttonComposite, SWT.NONE);
		new Label(buttonComposite, SWT.NONE);

		sashForm = new SashForm(shell, SWT.NONE);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		hierarchyComposite = new Composite(sashForm, SWT.NONE);
		hierarchyComposite.setLayout(new GridLayout(2, false));

		Composite subsetComposite = new Composite(sashForm, SWT.NONE);
		subsetComposite.setLayout(new GridLayout(3, false));

		hierarchyTree = new Tree(hierarchyComposite, SWT.BORDER | SWT.MULTI);
		hierarchyTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		hierarchyTree.setLinesVisible(true);
		hierarchyTree.setHeaderVisible(true);
		hierarchyTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 2));


		subsetTable = new Table(subsetComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		subsetTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		subsetTable.setHeaderVisible(true);
		subsetTable.setLinesVisible(true);


		try {
			hierarchy.readElementAliasesFromServer();
			for (int i = 0; i < hierarchy.aliasCount(); i++) {
				aliasCombo.add(hierarchy.getAlias(i));
				if (hierarchy.getAlias(i).equals(subset.alias)) {
					aliasCombo.select(i);
				}
			}

		} catch (TM1RestException | URISyntaxException | IOException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//aliasCombo.add("");

		try {
			subset.readSubsetAliasFromServer();
			if (subset.alias.isEmpty()){
				//System.out.println("Alias is disabled for subset " + subset.displayName);
				aliasEnabled = false;
			} else {
				//System.out.println("Alias set to " + subset.alias + " for subset " + subset.displayName);
				aliasEnabled = true;
			}

		} catch (TM1RestException | URISyntaxException | IOException | JSONException e2) {
			// TODO Auto-generated catch block
			if (e2 instanceof TM1RestException && ((TM1RestException)e2).getErrorCode() == 401){
				throw e2;
			} else {
				e2.printStackTrace();
			}
		}

		try {
			hierarchy.readSubsetsFromServer();
		} catch (JSONException | TM1RestException | URISyntaxException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if (subset.displayName.equals("")) {
			subsetSelectCombo.add("<no subset>");
			subsetSelectCombo.setData("<no subset>", subset);
			subsetSelectCombo.select(0);
		}
		for (int i = 0; i < hierarchy.subsetCount(); i++) {
			subsetSelectCombo.add(hierarchy.getSubset(i).displayName);
			subsetSelectCombo.setData(hierarchy.getSubset(i).displayName, hierarchy.getSubset(i));
			if (subset.displayName.equals(hierarchy.getSubset(i).displayName)) {
				subsetSelectCombo.select(i);
			}
		}


		final List<Item> dragSourceItems = new ArrayList<Item>();
		Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
		DragSource hierarchyTreeSource = new DragSource(hierarchyTree, DND.DROP_MOVE);
		hierarchyTreeSource.setTransfer(types);
		hierarchyTreeSource.addDragListener(new DragSourceAdapter() {

			public void dragStart(DragSourceEvent event) {
				// System.out.println("Drag Started from hierarchy Tree");
				TreeItem[] selection = hierarchyTree.getSelection();
				if (selection.length > 0 && selection[0].getItemCount() >= 0) {
					event.doit = true;
					dragSourceItems.clear();
					elementDNDList.clear();
					for (int i = selection.length - 1; i >= 0; i--) {
						dragSourceItems.add(selection[i]);
						// System.out.println("Added " + ((TM1Element)
						// selection[i].getData()).displayName);
						elementDNDList.add((TM1Element) selection[i].getData());
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
				}
			}
		});

		hierarchyTreeColumn1 = new TreeColumn(hierarchyTree, SWT.NONE);
		hierarchyTreeColumn1.setWidth(181);
		hierarchyTreeColumn1.setText("Name");

		hierarchyTreeColumn2 = new TreeColumn(hierarchyTree, SWT.NONE);
		hierarchyTreeColumn2.setWidth(100);
		hierarchyTreeColumn2.setText("Index");

		hierarchyTreeColumn3 = new TreeColumn(hierarchyTree, SWT.NONE);
		hierarchyTreeColumn3.setWidth(100);
		hierarchyTreeColumn3.setText("Level");

		hierarchyTreeMenu = new Menu(hierarchyTree);
		hierarchyTree.setMenu(hierarchyTreeMenu);

		TreeColumn hierarchyTreeColumn4 = new TreeColumn(hierarchyTree, SWT.NONE);
		hierarchyTreeColumn4.setWidth(100);
		hierarchyTreeColumn4.setText("Weight");

		DragSource subsetTableSource = new DragSource(subsetTable, DND.DROP_MOVE);
		subsetTableSource.setTransfer(types);
		subsetTableSource.addDragListener(new DragSourceAdapter() {

			public void dragStart(DragSourceEvent event) {
				TableItem[] selection = subsetTable.getSelection();
				if (selection.length > 0) {
					event.doit = true;
					dragSourceItems.clear();
					elementDNDList.clear();
					for (int i = selection.length - 1; i >= 0; i--) {
						dragSourceItems.add(selection[i]);
						// System.out.println("Added " + ((TM1Element)
						// selection[i].getData()).displayName);
						elementDNDList.add((TM1Element) selection[i].getData());
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
					for (int i = 0; i < dragSourceItems.size(); i++) {
						if (dragSourceItems.get(i) instanceof TableItem) {
							TableItem t = (TableItem) dragSourceItems.get(i);
							t.dispose();
							t = null;
						}
					}
				}
			}
		});

		DropTarget subsetTableTarget = new DropTarget(subsetTable, DND.DROP_COPY | DND.DROP_MOVE);
		subsetTableTarget.setTransfer(types);
		subsetTableTarget.addDropListener(new DropTargetAdapter() {

			public void dragOver(DropTargetEvent event) {
				// System.out.println("Drag Over on Subset Table ");
				event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
				if (event.item != null) {
					Display display = shell.getDisplay();
					TableItem item = (TableItem) event.item;
					Point pt = display.map(null, subsetTable, event.x, event.y);
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
				// System.out.println("Drag Over on Subset Table ");
				// System.out.println("Dropping onto subset table");
				// Get the dropped data
				DropTarget target = (DropTarget) event.widget;
				String data = (String) event.data;
				Table table = (Table) target.getControl();

				if (event.item != null) {
					TableItem item = (TableItem) event.item;
					TM1Element dropElement = (TM1Element)item.getData();
					Display display = shell.getDisplay();
					Point pt = display.map(null, subsetTable, event.x, event.y);
					Rectangle bounds = item.getBounds();

					TableItem[] tableItems = subsetTable.getItems();
					int index = 0;
					for (int i = 0; i < tableItems.length; i++) {
						if (tableItems[i] == item) {
							index = i;
							break;
						}
					}
					if (pt.y < bounds.y + bounds.height / 3) {

						// Elements inserts before an element

						for (int i = 0; i < elementDNDList.size(); i++) {
							TM1Element element = elementDNDList.get(i);
							TableItem newItem = new TableItem(subsetTable, SWT.NONE, index);
							if (aliasEnabled){
								newItem.setText(0, element.alias);
							} else {
								newItem.setText(0, element.displayName);
							}
							newItem.setText(1, Integer.toString(element.index));
							newItem.setText(2, Integer.toString(element.level));
							newItem.setData(element);
							if (element.elementType.equals(CONSOLIDATED)) {
								newItem.setImage(ICON_CONSOLIDATED);
							} else if (element.elementType.equals(NUMERIC)) {
								newItem.setImage(ICON_NUMERIC);
							} else if (element.elementType.equals(STRING)) {
								newItem.setImage(ICON_STRING);
							}
							System.out.println(element.displayName + " insert before " + dropElement.displayName);
							try {
								subset.insertBeforeElement(element, dropElement);
							} catch (TM1RestException | URISyntaxException | IOException | JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

					} else if (pt.y > bounds.y + 2 * bounds.height / 3) {

						// elements inserted after some element

						for (int i = 0; i < elementDNDList.size(); i++) {
							TM1Element element = elementDNDList.get(i);
							TableItem newItem = new TableItem(subsetTable, SWT.NONE, index + 1);
							if (aliasEnabled){
								newItem.setText(0, element.alias);
							} else {
								newItem.setText(0, element.displayName);
							}
							newItem.setText(1, Integer.toString(element.index));
							newItem.setText(2, Integer.toString(element.level));
							newItem.setData(element);
							if (element.elementType.equals(CONSOLIDATED)) {
								newItem.setImage(ICON_CONSOLIDATED);
							} else if (element.elementType.equals(NUMERIC)) {
								newItem.setImage(ICON_NUMERIC);
							} else if (element.elementType.equals(STRING)) {
								newItem.setImage(ICON_STRING);
							}

							System.out.println(element.displayName + " insert after " + dropElement.displayName);
							try {
								subset.insertAfterElement(element, dropElement);
							} catch (JSONException | TM1RestException | URISyntaxException | IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					} else {


						for (int i = 0; i < elementDNDList.size(); i++) {
							TM1Element element = elementDNDList.get(i);
							TableItem newItem = new TableItem(subsetTable, SWT.NONE);
							if (aliasEnabled){
								newItem.setText(0, element.alias);
							} else {
								newItem.setText(0, element.displayName);
							}
							newItem.setText(1, Integer.toString(element.index));
							newItem.setText(2, Integer.toString(element.level));
							newItem.setData(element);
							if (element.elementType.equals(CONSOLIDATED)) {
								newItem.setImage(ICON_CONSOLIDATED);
							} else if (element.elementType.equals(NUMERIC)) {
								newItem.setImage(ICON_NUMERIC);
							} else if (element.elementType.equals(STRING)) {
								newItem.setImage(ICON_STRING);
							}

							System.out.println(element.displayName + " insert after " + dropElement.displayName);
							try {
								subset.insertAfterElement(element, dropElement);
							} catch (JSONException | TM1RestException | URISyntaxException | IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				} else {

					// Elements inserted below all other elements

					for (int i = 0; i < elementDNDList.size(); i++) {
						TM1Element element = elementDNDList.get(i);
						TableItem newItem = new TableItem(subsetTable, SWT.NONE);
						if (aliasEnabled){
							newItem.setText(0, element.alias);
						} else {
							newItem.setText(0, element.displayName);
						}
						newItem.setText(1, Integer.toString(element.index));
						newItem.setText(2, Integer.toString(element.level));
						newItem.setData(element);
						if (element.elementType.equals(CONSOLIDATED)) {
							newItem.setImage(ICON_CONSOLIDATED);
						} else if (element.elementType.equals(NUMERIC)) {
							newItem.setImage(ICON_NUMERIC);
						} else if (element.elementType.equals(STRING)) {
							newItem.setImage(ICON_STRING);
						}

						System.out.println(element.displayName + " inserted at end of subset");
						try {
							subset.insertElement(element);
						} catch (JSONException | TM1RestException | URISyntaxException | IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				table.redraw();

			}
		});

		subsetTableColumn1 = new TableColumn(subsetTable, SWT.NONE);
		subsetTableColumn1.setWidth(208);
		subsetTableColumn1.setText("Name");

		subsetTableColumn2 = new TableColumn(subsetTable, SWT.NONE);
		subsetTableColumn2.setWidth(100);
		subsetTableColumn2.setText("Index");

		subsetTableColumn3 = new TableColumn(subsetTable, SWT.NONE);
		subsetTableColumn3.setWidth(100);
		subsetTableColumn3.setText("Level");

		subsetTableMenu = new Menu(subsetTable);
		subsetTable.setMenu(subsetTableMenu);
		subsetTableMenu.addMenuListener(new MenuAdapter() {
			@Override
			public void menuShown(org.eclipse.swt.events.MenuEvent arg0) {
				MenuItem[] items = subsetTableMenu.getItems();
				for (int i = 0; i < items.length; i++) {
					items[i].dispose();
				}

				TableItem[] selectedSubsetItems = subsetTable.getSelection();

				MenuItem cutMenuItem = new MenuItem(subsetTableMenu, SWT.NONE);
				cutMenuItem.setText("Cut");
				cutMenuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						TableItem[] selectedItems = subsetTable.getSelection();
						selectedElements.clear();
						for (int i = 0; i < selectedItems.length; i++) {
							selectedElements.add((TM1Element) subsetTable.getSelection()[i].getData());
						}
						subsetTable.remove(subsetTable.getSelectionIndices());
					}
				});
				if (selectedSubsetItems.length == 0) {
					cutMenuItem.setEnabled(false);
				}

				MenuItem copyMenuItem = new MenuItem(subsetTableMenu, SWT.NONE);
				copyMenuItem.setText("Copy");
				copyMenuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						TableItem[] selectedItems = subsetTable.getSelection();
						selectedElements.clear();
						for (int i = 0; i < selectedItems.length; i++) {
							selectedElements.add((TM1Element) subsetTable.getSelection()[i].getData());
						}
					}
				});
				if (selectedSubsetItems.length == 0) {
					copyMenuItem.setEnabled(false);
				}

				new MenuItem(subsetTableMenu, SWT.SEPARATOR);

				MenuItem pasteMenuItem = new MenuItem(subsetTableMenu, SWT.NONE);
				pasteMenuItem.setText("Paste");
				if (selectedSubsetItems.length > 0 || selectedElements.size() == 0) {
					pasteMenuItem.setEnabled(false);
				}
				pasteMenuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						for (int i = 0; i < selectedElements.size(); i++) {
							TM1Element elementToAdd = selectedElements.get(i);
							TableItem elementToAddTableItem = new TableItem(subsetTable, SWT.NONE);
							elementToAddTableItem.setText(0, elementToAdd.alias);
							elementToAddTableItem.setText(1, Integer.toString(elementToAdd.index));
							elementToAddTableItem.setText(2, Integer.toString(elementToAdd.level));
							if (elementToAdd.isConsolidated()) {
								elementToAddTableItem.setImage(ICON_CONSOLIDATED);
							} else if (elementToAdd.isNumberic()) {
								elementToAddTableItem.setImage(ICON_NUMERIC);
							} else if (elementToAdd.isString()) {
								elementToAddTableItem.setImage(ICON_STRING);
							}
						}

					}
				});

				MenuItem pasteAboveMenuItem = new MenuItem(subsetTableMenu, SWT.NONE);
				pasteAboveMenuItem.setText("Paste Above");
				if (selectedSubsetItems.length != 1 || selectedElements.size() > 0) {
					pasteAboveMenuItem.setEnabled(false);
				}
				pasteAboveMenuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						int[] selectedIndexes = subsetTable.getSelectionIndices();
						int indexOfLastSelectedItem = selectedIndexes[0];
						for (int i = 0; i < selectedElements.size(); i++) {
							TM1Element elementToAdd = selectedElements.get(i);
							TableItem elementToAddTableItem = new TableItem(subsetTable, SWT.NONE, indexOfLastSelectedItem);
							elementToAddTableItem.setText(0, elementToAdd.alias);
							elementToAddTableItem.setText(1, Integer.toString(elementToAdd.index));
							elementToAddTableItem.setText(2, Integer.toString(elementToAdd.level));
							if (elementToAdd.isConsolidated()) {
								elementToAddTableItem.setImage(ICON_CONSOLIDATED);
							} else if (elementToAdd.isNumberic()) {
								elementToAddTableItem.setImage(ICON_NUMERIC);
							} else if (elementToAdd.isString()) {
								elementToAddTableItem.setImage(ICON_STRING);
							}
						}

					}
				});

				MenuItem pasteBelowMenuItem = new MenuItem(subsetTableMenu, SWT.NONE);
				pasteBelowMenuItem.setText("Paste Below");
				if (selectedSubsetItems.length != 1 || selectedElements.size() > 0) {
					pasteBelowMenuItem.setEnabled(false);
				}
				pasteBelowMenuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						int[] selectedIndexes = subsetTable.getSelectionIndices();
						int indexOfLastSelectedItem = selectedIndexes[selectedIndexes.length - 1];
						for (int i = 0; i < selectedElements.size(); i++) {
							TM1Element elementToAdd = selectedElements.get(i);
							TableItem elementToAddTableItem = new TableItem(subsetTable, SWT.NONE, indexOfLastSelectedItem + 1);
							elementToAddTableItem.setText(0, elementToAdd.alias);
							elementToAddTableItem.setText(1, Integer.toString(elementToAdd.index));
							elementToAddTableItem.setText(2, Integer.toString(elementToAdd.level));
							if (elementToAdd.isConsolidated()) {
								elementToAddTableItem.setImage(ICON_CONSOLIDATED);
							} else if (elementToAdd.isNumberic()) {
								elementToAddTableItem.setImage(ICON_NUMERIC);
							} else if (elementToAdd.isString()) {
								elementToAddTableItem.setImage(ICON_STRING);
							}
						}
					}
				});
			}
		});
		hierarchyTreeMenu.addMenuListener(new MenuAdapter() {
			@Override
			public void menuShown(org.eclipse.swt.events.MenuEvent arg0) {
				if (hierarchyTree.getSelectionCount() > 0) {

					MenuItem[] items = hierarchyTreeMenu.getItems();
					for (int i = 0; i < items.length; i++) {
						items[i].dispose();
					}

					TableItem[] selectedItems = subsetTable.getSelection();
					selectedElements.clear();
					for (int i = 0; i < selectedItems.length; i++) {
						selectedElements.add((TM1Element) hierarchyTree.getSelection()[i].getData());
					}

					MenuItem copyMenuItem = new MenuItem(hierarchyTreeMenu, SWT.NONE);
					copyMenuItem.setText("Copy");
					copyMenuItem.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent event) {
							TreeItem[] selectedHierarchyTreeItems = hierarchyTree.getSelection();
							// System.out.println("Length: " +
							// selectedHierarchyTreeItems.length);
							selectedElements.clear();
							for (int i = 0; i < selectedHierarchyTreeItems.length; i++) {
								selectedElements.add((TM1Element) hierarchyTree.getSelection()[i].getData());
							}
						}
					});
				}
			}
		});
		sashForm.setWeights(new int[] { 1, 1 });

		menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);

		MenuItem mntmFile = new MenuItem(menu, SWT.CASCADE);
		mntmFile.setText("File");

		Menu menu_1 = new Menu(mntmFile);
		mntmFile.setMenu(menu_1);

		MenuItem mntmSaveAs = new MenuItem(menu_1, SWT.NONE);
		mntmSaveAs.setText("Save As");

		MenuItem mntmClose = new MenuItem(menu_1, SWT.NONE);
		mntmClose.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				closeSubsetEditor();
			}
		});
		mntmClose.setText("Close");

		composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, true, false, 1, 1));

		Button closeButton = new Button(composite, SWT.NONE);
		closeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				closeSubsetEditor();
			}
		});
		closeButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		closeButton.setText("Close");

		try {
			hierarchy.readHierarchyFromServer();
			refreshHierarchyTree();
			for (int i = 0; i < hierarchyTree.getItemCount(); i++) {
				TreeItem treeItem = hierarchyTree.getItem(i);
				treeItem.setExpanded(true);
				expand_node(treeItem);
			}
		} catch (TM1RestException | URISyntaxException | IOException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		// subset.readElementListFromServer();
		if (subset.displayName.equals("")){
			subsetTableSetAll();
		} else {
			refreshSubsetTable();
		}
		setSelectedElement();

		//shell.open();
	}

	public String open() {
		result = false;
		shell.open();
		shell.layout();
		Display display = shell.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return selectedElementName;
	}

	public String getSelectedElement(){
		return selectedElementName;
	}

	private void refreshHierarchyTree() {
		hierarchyTree.removeAll();

		for (int i = 0; i < hierarchy.getRootElementCount(); i++) {
			TM1Element rootElement = hierarchy.getRootElement(i);
			rootElement.setAlias(subset.alias);
			TreeItem rootElementTreeItem = new TreeItem(hierarchyTree, SWT.NONE);
			rootElementTreeItem.setText(0, rootElement.alias);
			rootElementTreeItem.setText(1, Integer.toString(rootElement.index));
			rootElementTreeItem.setText(2, Integer.toString(rootElement.level));
			rootElementTreeItem.setText(3, Integer.toString(rootElement.weight));
			rootElementTreeItem.setData(rootElement);
			if (rootElement.isConsolidated()) {
				rootElementTreeItem.setImage(ICON_CONSOLIDATED);
				refreshHierarchyTreeItem(rootElementTreeItem);
			} else if (rootElement.isNumberic()) {
				rootElementTreeItem.setImage(ICON_NUMERIC);
			} else if (rootElement.isString()) {
				rootElementTreeItem.setImage(ICON_STRING);
			}
		}
	}

	private void refreshHierarchyTreeItem(TreeItem parentTreeItem) {
		TM1Element element = (TM1Element) parentTreeItem.getData();
		for (int i = 0; i < element.childElementCount(); i++) {
			TM1Element childElement = element.childElement(i);
			childElement.setAlias(subset.alias);
			TreeItem childElementTreeItem = new TreeItem(parentTreeItem, SWT.NONE);
			childElementTreeItem.setText(0, childElement.alias);
			childElementTreeItem.setText(1, Integer.toString(childElement.index));
			childElementTreeItem.setText(2, Integer.toString(childElement.level));
			childElementTreeItem.setText(3, Integer.toString(childElement.weight));
			childElementTreeItem.setData(childElement);
			if (childElement.isConsolidated()) {
				childElementTreeItem.setImage(ICON_CONSOLIDATED);
				refreshHierarchyTreeItem(childElementTreeItem);
			} else if (childElement.isNumberic()) {
				childElementTreeItem.setImage(ICON_NUMERIC);
			} else if (childElement.isString()) {
				childElementTreeItem.setImage(ICON_STRING);
			}
		}
	}

	private void refreshSubsetTable() {
		subsetTable.removeAll();

		for (int i = 0; i < subset.elementCount(); i++) {
			TM1Element element = subset.getElement(i);
			element.setAlias(subset.alias);
			TableItem elementTableItem = new TableItem(subsetTable, SWT.NONE);
			elementTableItem.setText(0, element.alias);
			elementTableItem.setText(1, Integer.toString(element.index));
			elementTableItem.setText(2, Integer.toString(element.level));
			elementTableItem.setData(element);
			if (element.isConsolidated()) {
				elementTableItem.setImage(ICON_CONSOLIDATED);
			} else if (element.isNumberic()) {
				elementTableItem.setImage(ICON_NUMERIC);
			} else if (element.isString()) {
				elementTableItem.setImage(ICON_STRING);
			}
		}
	}

	private void subsetTableSetAll() {
		subsetTable.removeAll();
		for (int i = 0; i < hierarchy.getRootElementCount(); i++) {
			TM1Element rootElement = hierarchy.getRootElement(i);
			rootElement.setAlias(subset.alias);
			TableItem rootElementTableItem = new TableItem(subsetTable, SWT.NONE);
			rootElementTableItem.setText(0, rootElement.alias);
			rootElementTableItem.setText(1, Integer.toString(rootElement.index));
			rootElementTableItem.setText(2, Integer.toString(rootElement.level));
			rootElementTableItem.setText(3, Integer.toString(rootElement.weight));
			rootElementTableItem.setData(rootElement);
			if (rootElement.isConsolidated()) {
				rootElementTableItem.setImage(ICON_CONSOLIDATED);
				subsetTableSetAllChild(rootElementTableItem);
			} else if (rootElement.isNumberic()) {
				rootElementTableItem.setImage(ICON_NUMERIC);
			} else if (rootElement.isString()) {
				rootElementTableItem.setImage(ICON_STRING);
			}
		}
	}

	private void subsetTableSetAllChild(TableItem parentTableItem) {
		TM1Element element = (TM1Element) parentTableItem.getData();
		for (int i = 0; i < element.childElementCount(); i++) {
			TM1Element childElement = element.childElement(i);
			childElement.setAlias(subset.alias);
			TableItem childElementTableItem = new TableItem(subsetTable, SWT.NONE);
			childElementTableItem.setText(0, childElement.alias);
			childElementTableItem.setText(1, Integer.toString(childElement.index));
			childElementTableItem.setText(2, Integer.toString(childElement.level));
			childElementTableItem.setText(3, Integer.toString(childElement.weight));
			childElementTableItem.setData(childElement);
			if (childElement.isConsolidated()) {
				childElementTableItem.setImage(ICON_CONSOLIDATED);
				subsetTableSetAllChild(childElementTableItem);
			} else if (childElement.isNumberic()) {
				childElementTableItem.setImage(ICON_NUMERIC);
			} else if (childElement.isString()) {
				childElementTableItem.setImage(ICON_STRING);
			}
		}
	}

	private void setSelectedElement(){
		//System.out.println("Selecting element out of " + subsetTable.getItemCount());
		//System.out.println("Selected element is " + selectedElement);
		for (int i=0; i<subsetTable.getItemCount(); i++){
			TableItem t = subsetTable.getItem(i);
			//System.out.println("Compare to " + t.getText());
			if (t.getText().equals(selectedElement)){
				subsetTable.setSelection(i);
			}
		}
	}

	private void expand_node(TreeItem t) {
		for (int i = 0; i < t.getItemCount(); i++) {
			TreeItem child = t.getItem(i);
			child.setExpanded(true);
			expand_node(child);
		}
	}

	private void collapse_node(TreeItem t) {
		for (int i = 0; i < t.getItemCount(); i++) {
			TreeItem child = t.getItem(i);
			child.setExpanded(false);
			expand_node(child);
		}
	}

	private void updateSubsetFromUI() {
		subset.removeAllElements();
		for (int i = 0; i < subsetTable.getItemCount(); i++) {
			TM1Element element = (TM1Element) subsetTable.getItem(i).getData();
			subset.addElement(element);
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

	public void refreshElementAliases() {
		for (int i = 0; i < subsetTable.getItemCount(); i++) {
			TableItem t = subsetTable.getItem(i);
			TM1Element element = (TM1Element) t.getData();
			//System.out.println("Table Element " + element.displayName + " setting to alias " + subset.alias);
			element.setAlias(subset.alias);
			//System.out.println("Alias set to " + element.alias);
			if (aliasEnabled){
				t.setText(element.alias);
			} else {
				t.setText(element.displayName);
			}
		}

		for (int i = 0; i < hierarchyTree.getItemCount(); i++) {
			TreeItem t = hierarchyTree.getItem(i);
			TM1Element element = (TM1Element) t.getData();
			element.setAlias(subset.alias);
			if (aliasEnabled){
				t.setText(element.alias);
			} else {
				t.setText(element.displayName);
			}
			if (t.getItemCount() > 0) {
				refreshElementAliasesChildren(t);
			}

		}
	}

	public void refreshElementAliasesChildren(TreeItem t) {
		for (int i = 0; i < t.getItemCount(); i++) {
			TreeItem child = t.getItem(i);
			TM1Element element = (TM1Element) child.getData();
			element.setAlias(subset.alias);
			if (aliasEnabled){
				child.setText(element.alias);
			} else {
				child.setText(element.displayName);
			}
			if (child.getItemCount() > 0) {
				refreshElementAliasesChildren(child);
			}
		}
	}

	public boolean removeElementsFromSubset(){
		TableItem[] selectedForRemove = subsetTable.getSelection();
		for (int i=0; i<selectedForRemove.length; i++){
			try {
				subset.removeElement((TM1Element)selectedForRemove[i].getData());
			} catch (TM1RestException | URISyntaxException | IOException | JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		subsetTable.remove(subsetTable.getSelectionIndices());
		return true;
	}

	public boolean keepElementsFromSubset(){
		for (int i=subsetTable.getItemCount()-1; i>=0; i--){
			if (!subsetTable.isSelected(i)){
				subsetTable.remove(i);
			}
		}
		return true;
	}

	public void toggleAlias(){
		aliasEnabled = !aliasEnabled;
		this.refreshElementAliases();
	}

	public void closeSubsetEditor(){
		if (singleSelect){
			if (subsetTable.getSelectionCount() != 1){
				MessageBox messageBox = new MessageBox(shell, SWT.ERROR);
				messageBox.setText("Warning");
				messageBox.setMessage("Must select single element");
				messageBox.open();
			} else {
				selectedElementName = subsetTable.getSelection()[0].getText(0);
				shell.close();
			}
		} else {
			shell.close();
		}
	}
}
