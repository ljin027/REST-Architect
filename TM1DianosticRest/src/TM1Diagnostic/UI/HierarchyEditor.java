package TM1Diagnostic.UI;

import org.apache.wink.json4j.JSONException;
import org.eclipse.swt.widgets.Composite;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.MenuEvent;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;

import TM1Diagnostic.REST.TM1Dimension;
import TM1Diagnostic.REST.TM1Element;
import TM1Diagnostic.REST.TM1Hierarchy;
import TM1Diagnostic.REST.TM1RestException;
import TM1Diagnostic.REST.TM1Server;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TreeColumn;

public class HierarchyEditor {

	protected Shell shell;
	protected Display display;

	static public Image ICON_CONSOLIDATED;
	static public Image ICON_STRING;
	static public Image ICON_NUMERIC;
	static public Image ICON_SUBSETALL;
	static public Image ICON_DELETE;
	static public Image ICON_KEEP;

	static public String NUMERIC = "Numeric";
	static public String STRING = "String";
	static public String CONSOLIDATED = "Consolidated";

	private TM1Server tm1server;
	private TM1Hierarchy hierarchy;
	private TM1Dimension dimension;

	protected boolean refreshTreeOnClose;
	// protected Shell shlHierarchyEditor;

	private Tree hierarchyTree;
	private List<TM1Element> selectedHierarchyElements;
	private List<TM1Element> elementDNDList;
	private List<TM1Element> elementCopyPaste;

	private Combo aliasCombo;

	private ServerExplorerComposite explorer;
	private boolean pendingSave;

	/**
	 * @wbp.parser.constructor
	 */
	public HierarchyEditor(Shell parent, TM1Hierarchy hierarchy) {
		this.hierarchy = hierarchy;
		this.dimension = hierarchy.dimension;
		shell = new Shell(parent, SWT.RESIZE | SWT.MAX | SWT.MIN);
		shell.setSize(883, 506);
		shell.setText("Hierarchy Editor - " + dimension.displayName + "/" + hierarchy.displayName);
		shell.setLayout(new GridLayout(1, false));
		display = shell.getDisplay();
		createContents();
		shell.layout();
		shell.open();
	}

	public HierarchyEditor(Shell parent, TM1Dimension dimension) {
		this.dimension = dimension;
		shell = new Shell(parent, SWT.RESIZE | SWT.MAX | SWT.MIN);
		shell.setSize(689, 448);
		shell.setText("Hierarchy Editor - *New Hierarchy");
		shell.setLayout(new GridLayout(1, false));
		display = shell.getDisplay();
		createContents();
		shell.layout();
		shell.open();
	}

	public HierarchyEditor(Shell parent, TM1Server tm1server) {
		this.tm1server = tm1server;
		shell = new Shell(parent, SWT.RESIZE | SWT.MAX | SWT.MIN);
		shell.setSize(689, 448);
		shell.setText("Hierarchy Editor - *New Dimension");
		shell.setLayout(new GridLayout(1, false));
		display = shell.getDisplay();
		createContents();
		shell.layout();
		shell.open();
	}

	private void createContents() {

		selectedHierarchyElements = new ArrayList<TM1Element>();
		elementDNDList = new ArrayList<TM1Element>();
		elementCopyPaste = new ArrayList<TM1Element>();
		pendingSave = false;

		onOpen();
	}

	private void onOpen() {

		ICON_CONSOLIDATED = new Image(display, ".\\images\\consolidation.gif");
		ICON_NUMERIC = new Image(display, ".\\images\\numeric.gif");
		ICON_STRING = new Image(display, ".\\images\\string.gif");
		ICON_SUBSETALL = new Image(display, ".\\images\\subsetAll.gif");
		ICON_DELETE = new Image(display, ".\\images\\deleteElements.gif");
		ICON_KEEP = new Image(display, ".\\images\\keepElements.gif");

		// setLayout(new GridLayout(1, false));

		Composite buttons_pane = new Composite(shell, SWT.NONE);
		buttons_pane.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		buttons_pane.setLayout(new GridLayout(4, false));

		Label lblNewLabel = new Label(buttons_pane, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("Hierarchy");

		Combo combo = new Combo(buttons_pane, SWT.NONE);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		if (hierarchy != null) {
			TM1Dimension dimension = (TM1Dimension) hierarchy.getParent();
			for (int i = 0; i < dimension.hierarchyCount(); i++) {
				combo.add(dimension.getHeirarchy(i).displayName);
			}
			combo.setText(hierarchy.displayName);
		}

		Button expand_button = new Button(buttons_pane, SWT.NONE);
		expand_button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				for (int i = 0; i < hierarchyTree.getItemCount(); i++) {
					TreeItem t = hierarchyTree.getItem(i);
					t.setExpanded(true);
					expandNode(t);
				}
			}
		});
		expand_button.setText("Expand");

		Button collapse_button = new Button(buttons_pane, SWT.NONE);
		collapse_button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				for (int i = 0; i < hierarchyTree.getItemCount(); i++) {
					TreeItem t = hierarchyTree.getItem(i);
					t.setExpanded(false);
					expandNode(t);
				}
			}
		});
		collapse_button.setText("Collapse");

		Label alias_label = new Label(buttons_pane, SWT.NONE);
		alias_label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		alias_label.setText("Alias");

		aliasCombo = new Combo(buttons_pane, SWT.NONE);
		aliasCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		aliasCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				// System.out.println("Set alias to " + aliasCombo.getText());
				refreshHierarchyTreeElementAliases();
			}
		});

		try {
			hierarchy.readElementAliasesFromServer();
			hierarchy.readHierarchyFromServer();
		} catch (TM1RestException | URISyntaxException | IOException | JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		for (int i = 0; i < hierarchy.aliasCount(); i++) {
			aliasCombo.add(hierarchy.getAlias(i));
		}

		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		composite.setLayout(new FillLayout(SWT.HORIZONTAL));

		hierarchyTree = new Tree(composite, SWT.BORDER | SWT.MULTI);
		hierarchyTree.setHeaderVisible(true);

		Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
		DragSource source = new DragSource(hierarchyTree, DND.DROP_MOVE);
		source.setTransfer(types);

		final List<TreeItem> dragSourceItems = new ArrayList<TreeItem>();

		source.addDragListener(new DragSourceAdapter() {
			public void dragStart(DragSourceEvent event) {
				TreeItem[] selection = hierarchyTree.getSelection();
				if (selection.length > 0 && selection[0].getItemCount() >= 0) {
					event.doit = true;
					dragSourceItems.clear();
					elementDNDList.clear();
					for (int i = selection.length - 1; i >= 0; i--) {
						dragSourceItems.add(selection[i]);
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
						TreeItem t = dragSourceItems.get(i);
						t.dispose();
						t = null;
					}
				}
			}
		});

		DropTarget target = new DropTarget(hierarchyTree, DND.DROP_MOVE);
		target.setTransfer(types);
		target.addDropListener(new DropTargetAdapter() {
			public void dragOver(DropTargetEvent event) {
				event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
				if (event.item != null) {
					TreeItem item = (TreeItem) event.item;
					Display display = explorer.getDisplay();
					Point pt = display.map(null, hierarchyTree, event.x, event.y);
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
				if (event.data == null) {
					event.detail = DND.DROP_NONE;
					return;
				}
				if (event.item == null) {

					// This is the case where the element is dropped
					// in white space below the hierarchy being displayed

					for (int i = 0; i < elementDNDList.size(); i++) {
						TM1Element element = elementDNDList.get(i);
						try {
							hierarchy.moveElementBefore(element, null);
						} catch (TM1RestException | URISyntaxException | IOException | JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} else {

					// This is the case where the element is dropped
					// on either another element, or between elements

					TreeItem item = (TreeItem) event.item;
					Display display = explorer.getDisplay();
					Point pt = display.map(null, hierarchyTree, event.x, event.y);
					Rectangle bounds = item.getBounds();
					TreeItem parentItem = item.getParentItem();
					if (parentItem != null) {
						TM1Element parentElement = (TM1Element) parentItem.getData();

						// System.out.println("Dropped element at Child Level");
						TreeItem[] items = parentItem.getItems();
						int index = 0;
						for (int i = 0; i < items.length; i++) {
							if (items[i] == item) {
								index = i;
								break;
							}
						}
						if (pt.y < bounds.y + bounds.height / 3) {
							// System.out.println("---> 1");
							for (int i = 0; i < elementDNDList.size(); i++) {
								TM1Element element = elementDNDList.get(i);
								TM1Element beforeElement;
								if (index > parentItem.getItemCount()) {
									beforeElement = null;
								} else {
									beforeElement = (TM1Element) parentItem.getItem(index).getData();
								}
								try {
									parentElement.setComponentOnServer(element, beforeElement);
								} catch (JSONException | TM1RestException | URISyntaxException | IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						} else if (pt.y > bounds.y + 2 * bounds.height / 3) {
							// System.out.println("---> 2");
							for (int i = 0; i < elementDNDList.size(); i++) {
								TM1Element element = elementDNDList.get(i);
								TM1Element beforeElement;
								if (index + 1 >= parentItem.getItemCount()) {
									beforeElement = null;
								} else {
									beforeElement = (TM1Element) parentItem.getItem(index + 1).getData();
								}
								try {
									parentElement.setComponentOnServer(element, beforeElement);
								} catch (JSONException | TM1RestException | URISyntaxException | IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

							}

						} else {

							// System.out.println("Dropped onto item");
							for (int i = 0; i < elementDNDList.size(); i++) {
								TM1Element element = elementDNDList.get(i);
								// TreeItem newItem = new TreeItem(item,
								// SWT.NONE);
								TM1Element subParentElement = (TM1Element) item.getData();

								try {
									subParentElement.setComponentOnServer(element, null);
								} catch (JSONException | TM1RestException | URISyntaxException | IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

							}
						}

					} else {
						// System.out.println("Dropped Element At Top Level");
						TreeItem[] items = hierarchyTree.getItems();
						int index = 0;
						for (int i = 0; i < items.length; i++) {
							if (items[i] == item) {
								index = i;
								break;
							}
						}
						if (pt.y < bounds.y + bounds.height / 3) {
							System.out.println("---> 1");
							for (int i = 0; i < elementDNDList.size(); i++) {
								TM1Element element = elementDNDList.get(i);

								TM1Element beforeElement = (TM1Element) hierarchyTree.getItem(index).getData();
								try {
									hierarchy.moveElementBefore(element, beforeElement);
								} catch (TM1RestException | URISyntaxException | IOException | JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

							}
						} else if (pt.y > bounds.y + 2 * bounds.height / 3) {

							// System.out.println("---> 2");
							for (int i = 0; i < elementDNDList.size(); i++) {
								TM1Element element = elementDNDList.get(i);
								TM1Element beforeElement;
								if (index + 1 >= hierarchyTree.getItemCount()) {
									beforeElement = null;
								} else {
									beforeElement = (TM1Element) hierarchyTree.getItem(index + 1).getData();
								}
								try {
									hierarchy.moveElementBefore(element, beforeElement);
								} catch (TM1RestException | URISyntaxException | IOException | JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

							}
						} else {

							// System.out.println("Dropped onto Item");
							for (int i = 0; i < elementDNDList.size(); i++) {
								TM1Element element = elementDNDList.get(i);
								TM1Element subParentElement = (TM1Element) item.getData();
								try {
									subParentElement.setComponentOnServer(element, null);
								} catch (JSONException | TM1RestException | URISyntaxException | IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					}

				}
				try {
					hierarchy.readHierarchyFromServer();
				} catch (TM1RestException | URISyntaxException | IOException | JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				refreshHierarchyTree();
			}

		});

		final Menu hierarchyTreeMenu = new Menu(hierarchyTree);
		hierarchyTree.setMenu(hierarchyTreeMenu);

		TreeColumn hierachyTreeColumn1 = new TreeColumn(hierarchyTree, SWT.NONE);
		hierachyTreeColumn1.setWidth(334);
		hierachyTreeColumn1.setText("Name");

		TreeColumn trclmnNewColumn = new TreeColumn(hierarchyTree, SWT.NONE);
		trclmnNewColumn.setWidth(100);
		trclmnNewColumn.setText("Type");

		TreeColumn hierachyTreeColumn2 = new TreeColumn(hierarchyTree, SWT.NONE);
		hierachyTreeColumn2.setWidth(100);
		hierachyTreeColumn2.setText("Index");

		TreeColumn hierachyTreeColumn3 = new TreeColumn(hierarchyTree, SWT.NONE);
		hierachyTreeColumn3.setWidth(100);
		hierachyTreeColumn3.setText("Level");

		TreeColumn hierarchyTreeColumn4 = new TreeColumn(hierarchyTree, SWT.NONE);
		hierarchyTreeColumn4.setWidth(100);
		hierarchyTreeColumn4.setText("Weight");

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

		hierarchyTreeMenu.addMenuListener(new MenuAdapter() {
			@Override
			public void menuShown(org.eclipse.swt.events.MenuEvent arg0) {
				if (hierarchyTree.getSelectionCount() > 0) {

					MenuItem[] items = hierarchyTreeMenu.getItems();
					for (int i = 0; i < items.length; i++) {
						items[i].dispose();
					}

					selectedHierarchyElements.clear();
					for (int i = 0; i < hierarchyTree.getSelectionCount(); i++) {
						TreeItem t = hierarchyTree.getItem(i);
						selectedHierarchyElements.add((TM1Element) t.getData());

					}

					MenuItem deleteMenuItem = new MenuItem(hierarchyTreeMenu, SWT.NONE);
					deleteMenuItem.setText("Delete");
					deleteMenuItem.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent event) {
							TreeItem[] selectedItems = hierarchyTree.getSelection();
							deleteElements(selectedItems);
						}
					});

					MenuItem deleteWithChildreanMenuItem = new MenuItem(hierarchyTreeMenu, SWT.NONE);
					deleteWithChildreanMenuItem.setText("Delete Element and Children");
					deleteWithChildreanMenuItem.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent event) {
							TreeItem[] selectedItems = hierarchyTree.getSelection();
							deleteElementWithChildren(selectedItems);
						}
					});

					new MenuItem(hierarchyTreeMenu, SWT.SEPARATOR);

					MenuItem cutMenuItem = new MenuItem(hierarchyTreeMenu, SWT.NONE);
					cutMenuItem.setText("Cut");
					cutMenuItem.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent event) {
							elementCopyPaste.clear();
							for (int i = 0; i < hierarchyTree.getSelectionCount(); i++) {
								TreeItem t = hierarchyTree.getSelection()[i];
								elementCopyPaste.add((TM1Element) t.getData());
								// System.out.println("Cut " + ((TM1Element)
								// t.getData()).displayName);
							}
						}
					});

					new MenuItem(hierarchyTreeMenu, SWT.SEPARATOR);

					MenuItem pasteAboveMenuItem = new MenuItem(hierarchyTreeMenu, SWT.NONE);
					pasteAboveMenuItem.setText("Paste Above");
					if (selectedHierarchyElements.size() != 1 || elementCopyPaste.size() == 0) {
						pasteAboveMenuItem.setEnabled(false);
					}
					pasteAboveMenuItem.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent event) {

							TreeItem pasteAboveItem = hierarchyTree.getSelection()[0];
							TM1Element beforeElement = (TM1Element) pasteAboveItem.getData();
							if (pasteAboveItem.getParentItem() != null) {
								TreeItem parentElementItem = pasteAboveItem.getParentItem();
								TM1Element parentElement = (TM1Element) parentElementItem.getData();
								for (int i = 0; i < elementCopyPaste.size(); i++) {
									TM1Element element = elementCopyPaste.get(i);
									try {
										parentElement.setComponentOnServer(element, beforeElement);
									} catch (JSONException | TM1RestException | URISyntaxException | IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							} else {
								for (int i = 0; i < elementCopyPaste.size(); i++) {
									TM1Element element = elementCopyPaste.get(i);
									try {
										hierarchy.moveElementBefore(element, beforeElement);
									} catch (TM1RestException | URISyntaxException | IOException | JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}
							try {
								hierarchy.readHierarchyFromServer();
							} catch (TM1RestException | URISyntaxException | IOException | JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							refreshHierarchyTree();
						}

					});

					MenuItem pasteBelowMenuItem = new MenuItem(hierarchyTreeMenu, SWT.NONE);
					pasteBelowMenuItem.setText("Paste Below");
					if (selectedHierarchyElements.size() != 1 || elementCopyPaste.size() == 0) {
						pasteBelowMenuItem.setEnabled(false);
					}

					MenuItem pasteAsChildMenuItem = new MenuItem(hierarchyTreeMenu, SWT.NONE);
					pasteAsChildMenuItem.setText("Paste as Child");
					if (selectedHierarchyElements.size() != 1 || elementCopyPaste.size() == 0) {
						pasteAsChildMenuItem.setEnabled(false);
					}

					new MenuItem(hierarchyTreeMenu, SWT.SEPARATOR);

					MenuItem insertSiblingMenuItem = new MenuItem(hierarchyTreeMenu, SWT.NONE);
					insertSiblingMenuItem.setText("Insert Sibling");
					insertSiblingMenuItem.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent event) {
							TreeItem selectedItem = hierarchyTree.getSelection()[0];
							TreeItem parentItem = selectedItem.getParentItem();
							AddElement addElementDialog;
							if (parentItem == null) {
								TM1Element selectedElement = (TM1Element) selectedItem.getData();
								addElementDialog = new AddElement(shell, hierarchy, null, selectedElement);
							} else {
								TM1Element selectedElement = (TM1Element) selectedItem.getData();
								TM1Element parentElement = (TM1Element) parentItem.getData();
								addElementDialog = new AddElement(shell, hierarchy, parentElement, selectedElement);
							}

							if (addElementDialog.open()) {
								try {
									hierarchy.readHierarchyFromServer();
								} catch (TM1RestException | URISyntaxException | IOException | JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								refreshHierarchyTree();
							}
						}
					});
					if (selectedHierarchyElements.size() == 0) {
						insertSiblingMenuItem.setEnabled(false);
					}

					MenuItem insertChildMenuItem = new MenuItem(hierarchyTreeMenu, SWT.NONE);
					insertChildMenuItem.setText("Insert Child");
					insertChildMenuItem.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent event) {
							TreeItem parentItem = hierarchyTree.getSelection()[0];
							TM1Element parentElement = (TM1Element) parentItem.getData();
							AddElement addElementDialog = new AddElement(explorer.getShell(), hierarchy, parentElement, null);
							if (addElementDialog.open()) {
								// refresh hierarchy tree
								try {
									hierarchy.readHierarchyFromServer();
								} catch (TM1RestException | URISyntaxException | IOException | JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								refreshHierarchyTree();
							}
						}
					});
					if (selectedHierarchyElements.size() == 0) {
						insertChildMenuItem.setEnabled(false);
					}

				}
			}
		});

		refreshHierarchyTree();

		for (int i = 0; i < hierarchyTree.getItemCount(); i++) {
			TreeItem t = hierarchyTree.getItem(i);
			t.setExpanded(true);
			expandNode(t);
		}
	}

	public void setRefreshTreeOnClose(boolean refreshTreeOnClose) {
		this.refreshTreeOnClose = refreshTreeOnClose;
	}

	private void refreshHierarchyTree() {
		hierarchyTree.removeAll();

		for (int i = 0; i < hierarchy.getRootElementCount(); i++) {
			TM1Element rootElement = hierarchy.getRootElement(i);
			rootElement.setAlias(aliasCombo.getText());

			rootElement.setAlias(aliasCombo.getText());
			TreeItem rootElementTreeItem = new TreeItem(hierarchyTree, SWT.NONE);
			rootElementTreeItem.setText(0, rootElement.alias);
			rootElementTreeItem.setText(1, rootElement.elementType);
			rootElementTreeItem.setText(2, Integer.toString(rootElement.index));
			rootElementTreeItem.setText(3, Integer.toString(rootElement.level));
			rootElementTreeItem.setText(4, Integer.toString(rootElement.weight));

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

		for (int i = 0; i < hierarchyTree.getItemCount(); i++) {
			TreeItem rootItem = hierarchyTree.getItem(i);
			expandNode(rootItem);
		}
	}

	private void refreshHierarchyTreeItem(TreeItem parentTreeItem) {
		TM1Element element = (TM1Element) parentTreeItem.getData();
		for (int i = 0; i < element.childElementCount(); i++) {
			TM1Element childElement = element.childElement(i);
			childElement.setAlias(aliasCombo.getText());
			TreeItem childElementTreeItem = new TreeItem(parentTreeItem, SWT.NONE);
			childElementTreeItem.setText(0, childElement.alias);
			childElementTreeItem.setText(1, childElement.elementType);
			childElementTreeItem.setText(2, Integer.toString(childElement.index));
			childElementTreeItem.setText(3, Integer.toString(childElement.level));
			childElementTreeItem.setText(4, Integer.toString(childElement.weight));
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

	private void refreshHierarchyTreeElementAliases() {
		for (int i = 0; i < hierarchyTree.getItemCount(); i++) {
			TreeItem treeItem = hierarchyTree.getItem(i);
			TM1Element element = (TM1Element) treeItem.getData();
			element.setAlias(aliasCombo.getText());
			treeItem.setText(0, element.alias);
			if (treeItem.getItemCount() > 0) {
				refreshHierarchyItemElementAliases(treeItem);
			}
		}
	}

	private void refreshHierarchyItemElementAliases(TreeItem treeItem) {
		for (int i = 0; i < treeItem.getItemCount(); i++) {
			TreeItem childTreeItem = treeItem.getItem(i);
			TM1Element element = (TM1Element) childTreeItem.getData();
			element.setAlias(aliasCombo.getText());
			childTreeItem.setText(0, element.alias);
			if (childTreeItem.getItemCount() > 0) {
				refreshHierarchyItemElementAliases(childTreeItem);
			}
		}
	}

	private void expandNode(TreeItem t) {
		t.setExpanded(true);
		for (int i = 0; i < t.getItemCount(); i++) {
			TreeItem child = t.getItem(i);
			child.setExpanded(true);
			expandNode(child);
		}
	}

	private void collapseNode(TreeItem t) {
		t.setExpanded(false);
		for (int i = 0; i < t.getItemCount(); i++) {
			TreeItem child = t.getItem(i);
			child.setExpanded(false);
			collapseNode(child);
		}
	}

	private void updateHierarchyFromUI() {
		hierarchy.clearElements();
		for (int i = 0; i < hierarchyTree.getItemCount(); i++) {
			TreeItem rootItem = hierarchyTree.getItem(i);
			String elemName = rootItem.getText(0);
			String elemType = rootItem.getText(1);
			int elemIndex = Integer.parseInt(rootItem.getText(2));
			int elemWeight = Integer.parseInt(rootItem.getText(3));
			int elemLevel = Integer.parseInt(rootItem.getText(4));

			TM1Element rootElement = new TM1Element(elemName, hierarchy.getServer(), hierarchy, elemType, elemIndex, elemWeight, elemLevel);
			if (rootItem.getItemCount() > 0) {
				updateHierarchyFromUI(rootItem, rootElement);
			}
			hierarchy.addRootElement(rootElement);
		}
	}

	private void updateHierarchyFromUI(TreeItem treeItem, TM1Element element) {
		for (int i = 0; i < treeItem.getItemCount(); i++) {
			TreeItem childItem = treeItem.getItem(i);
			String elemName = childItem.getText(0);
			String elemType = childItem.getText(1);
			int elemIndex = Integer.parseInt(childItem.getText(2));
			int elemWeight = Integer.parseInt(childItem.getText(3));
			int elemLevel = Integer.parseInt(childItem.getText(4));

			TM1Element childElement = new TM1Element(elemName, hierarchy.getServer(), hierarchy, elemType, elemIndex, elemWeight, elemLevel);
			if (childItem.getItemCount() > 0) {
				updateHierarchyFromUI(childItem, childElement);
			}
			element.addChildElement(childElement);
		}
	}

	private void deleteElements(TreeItem[] selectedItems) {
		for (int i = 0; i < selectedItems.length; i++) {
			TreeItem t = selectedItems[i];
			TM1Element selectedElement = (TM1Element) t.getData();
			try {
				selectedElement.removeElement();
			} catch (TM1RestException | URISyntaxException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			if (hierarchy.readHierarchyFromServer()) {
				refreshHierarchyTree();
			}
		} catch (TM1RestException | URISyntaxException | IOException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void deleteElementWithChildren(TreeItem[] selectedItems) {
		for (int i = 0; i < selectedItems.length; i++) {
			TreeItem t = selectedItems[i];
			TM1Element selectedElement = (TM1Element) t.getData();
			try {
				selectedElement.removeElementWithChildern();
			} catch (TM1RestException | URISyntaxException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			if (hierarchy.readHierarchyFromServer()) {
				refreshHierarchyTree();
			}
		} catch (TM1RestException | URISyntaxException | IOException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean save() {
		updateHierarchyFromUI();
		try {
			hierarchy.writeToServer();
		} catch (JSONException | TM1RestException | URISyntaxException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	public boolean saveAs() {
		UI_NamePrompt namePrompt = new UI_NamePrompt(explorer.getShell(), "Hierarchy Name", "");
		if (namePrompt.open()) {
			try {
				hierarchy.saveHierarchyAs(namePrompt.getobjectname());
			} catch (JSONException | TM1RestException | URISyntaxException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			refreshTreeOnClose = true;
		}
		return true;
	}

	public boolean pendingSave() {
		return pendingSave;
	}

}
