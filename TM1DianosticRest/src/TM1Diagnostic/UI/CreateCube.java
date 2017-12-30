package TM1Diagnostic.UI;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.OrderedJSONObject;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import TM1Diagnostic.REST.TM1Dimension;
import TM1Diagnostic.REST.TM1RestException;
import TM1Diagnostic.REST.TM1Server;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class CreateCube extends Dialog {

	static private Image CUBEICON;

	protected Shell shlCreateCube;
	private Table allDimensionsTable;
	private Table cubeDimensionsTable;
	private Text cubeNameText;

	private boolean refreshOnClose;

	private TM1Server tm1server;

	public CreateCube(Shell parent, TM1Server tm1server) {
		super(parent, SWT.DIALOG_TRIM);
		this.tm1server = tm1server;
	}

	public boolean open() throws TM1RestException, ClientProtocolException, URISyntaxException, IOException {
		refreshOnClose = false;
		Display display = Display.getDefault();
		createContents();
		shlCreateCube.open();
		shlCreateCube.layout();
		while (!shlCreateCube.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return refreshOnClose;
	}

	protected void createContents() throws TM1RestException, ClientProtocolException, URISyntaxException, IOException {
		shlCreateCube = new Shell();
		shlCreateCube.setSize(907, 475);
		shlCreateCube.setText("Create Cube");
		shlCreateCube.setLayout(new GridLayout(2, true));

		CUBEICON = new Image(shlCreateCube.getDisplay(), ".\\images\\icon_cube.gif");
		shlCreateCube.setImage(CUBEICON);


		Composite topComposite = new Composite(shlCreateCube, SWT.NONE);
		topComposite.setLayout(new GridLayout(2, false));
		topComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		Label cubeNameLabel = new Label(topComposite, SWT.NONE);
		cubeNameLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		cubeNameLabel.setText("Cube Name");

		cubeNameText = new Text(topComposite, SWT.BORDER);
		cubeNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				String newCubeName = cubeNameText.getText();
				if (newCubeName.indexOf('\\') != -1) {
					cubeNameText.setText(newCubeName.substring(0, newCubeName.length() - 1));
					errorMessage("Invalid character '\\'");
				}
				if (newCubeName.indexOf('/') != -1) {
					cubeNameText.setText(newCubeName.substring(0, newCubeName.length() - 1));
					errorMessage("Invalid character '/'");
				}
				if (newCubeName.indexOf(':') != -1) {
					cubeNameText.setText(newCubeName.substring(0, newCubeName.length() - 1));
					errorMessage("Invalid character ':'");
				}
				if (newCubeName.indexOf('?') != -1) {
					cubeNameText.setText(newCubeName.substring(0, newCubeName.length() - 1));
					errorMessage("Invalid character '?'");
				}
				if (newCubeName.indexOf('\"') != -1) {
					cubeNameText.setText(newCubeName.substring(0, newCubeName.length() - 1));
					errorMessage("Invalid character '\"'");
				}
				if (newCubeName.indexOf('<') != -1) {
					cubeNameText.setText(newCubeName.substring(0, newCubeName.length() - 1));
					errorMessage("Invalid character '<'");
				}
				if (newCubeName.indexOf('>') != -1) {
					cubeNameText.setText(newCubeName.substring(0, newCubeName.length() - 1));
					errorMessage("Invalid character '>'");
				}
				if (newCubeName.indexOf('|') != -1) {
					cubeNameText.setText(newCubeName.substring(0, newCubeName.length() - 1));
					errorMessage("Invalid character '|'");
				}
				if (newCubeName.indexOf('\'') != -1) {
					cubeNameText.setText(newCubeName.substring(0, newCubeName.length() - 1));
					errorMessage("Invalid character '\''");
				}
				if (newCubeName.indexOf(';') != -1) {
					cubeNameText.setText(newCubeName.substring(0, newCubeName.length() - 1));
					errorMessage("Invalid character ';'");
				}
				if (newCubeName.indexOf(',') != -1) {
					cubeNameText.setText(newCubeName.substring(0, newCubeName.length() - 1));
					errorMessage("Invalid character ','");
				}

			}
		});
		cubeNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Composite dimensionsComposite = new Composite(shlCreateCube, SWT.NONE);
		dimensionsComposite.setLayout(new GridLayout(1, false));
		dimensionsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		allDimensionsTable = new Table(dimensionsComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		allDimensionsTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		allDimensionsTable.setHeaderVisible(true);
		allDimensionsTable.setLinesVisible(true);

		final List<TableItem> dragSourceItems = new ArrayList<TableItem>();
		Transfer[] types = new Transfer[] { TextTransfer.getInstance() };

		DragSource dimensionTableSource = new DragSource(allDimensionsTable, DND.DROP_MOVE);
		dimensionTableSource.setTransfer(types);

		dimensionTableSource.addDragListener(new DragSourceAdapter() {
			public void dragStart(DragSourceEvent event) {
				TableItem[] selection = allDimensionsTable.getSelection();
				if (selection.length > 0) {
					event.doit = true;
					dragSourceItems.clear();
					for (int i = selection.length - 1; i >= 0; i--) {
						dragSourceItems.add(selection[i]);
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
						TableItem t = dragSourceItems.get(i);
						t.dispose();
						t = null;
					}
				}
			}
		});

		Composite cubeDimensionsComposite = new Composite(shlCreateCube, SWT.NONE);
		cubeDimensionsComposite.setLayout(new GridLayout(1, false));
		cubeDimensionsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		cubeDimensionsTable = new Table(cubeDimensionsComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		cubeDimensionsTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		cubeDimensionsTable.setHeaderVisible(true);
		cubeDimensionsTable.setLinesVisible(true);

		DragSource cubeDimensionTableSource = new DragSource(cubeDimensionsTable, DND.DROP_MOVE);
		cubeDimensionTableSource.setTransfer(types);

		cubeDimensionTableSource.addDragListener(new DragSourceAdapter() {
			public void dragStart(DragSourceEvent event) {
				System.out.println("Drag source from cube table");
				TableItem[] selection = cubeDimensionsTable.getSelection();
				if (selection.length > 0) {
					event.doit = true;
					dragSourceItems.clear();
					for (int i = selection.length - 1; i >= 0; i--) {
						dragSourceItems.add(selection[i]);
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
						TableItem t = dragSourceItems.get(i);
						t.dispose();
						t = null;
					}
				}
			}
		});

		DropTarget subsetTableTarget = new DropTarget(cubeDimensionsTable, DND.DROP_COPY | DND.DROP_MOVE);
		subsetTableTarget.setTransfer(types);
		subsetTableTarget.addDropListener(new DropTargetAdapter() {

			public void dragOver(DropTargetEvent event) {
				// System.out.println("Drag Over on Subset Table ");
				event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
				if (event.item != null) {
					Display display = shlCreateCube.getDisplay();
					TableItem item = (TableItem) event.item;
					Point pt = display.map(null, cubeDimensionsTable, event.x, event.y);
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
				DropTarget target = (DropTarget) event.widget;
				String data = (String) event.data;
				Table table = (Table) target.getControl();

				if (event.item != null) {
					TableItem item = (TableItem) event.item;
					Display display = shlCreateCube.getDisplay();
					Point pt = display.map(null, cubeDimensionsTable, event.x, event.y);
					Rectangle bounds = item.getBounds();

					TableItem[] tableItems = cubeDimensionsTable.getItems();
					int index = 0;
					for (int i = 0; i < tableItems.length; i++) {
						if (tableItems[i] == item) {
							index = i;
							break;
						}
					}
					if (pt.y < bounds.y + bounds.height / 3) {
						for (int i = 0; i < dragSourceItems.size(); i++) {
							String transferDimName = dragSourceItems.get(i).getText();
							TableItem newItem = new TableItem(cubeDimensionsTable, SWT.NONE, index);
							newItem.setText(transferDimName);
						}
					} else if (pt.y > bounds.y + 2 * bounds.height / 3) {
						for (int i = 0; i < dragSourceItems.size(); i++) {
							String transferDimName = dragSourceItems.get(i).getText();
							TableItem newItem = new TableItem(cubeDimensionsTable, SWT.NONE, index + 1);
							newItem.setText(transferDimName);
						}
					} else {
						for (int i = 0; i < dragSourceItems.size(); i++) {
							String transferDimName = dragSourceItems.get(i).getText();
							TableItem newItem = new TableItem(cubeDimensionsTable, SWT.NONE);
							newItem.setText(transferDimName);
						}
					}
				} else {
					for (int i = 0; i < dragSourceItems.size(); i++) {
						String transferDimName = dragSourceItems.get(i).getText();
						TableItem newItem = new TableItem(cubeDimensionsTable, SWT.NONE);
						newItem.setText(transferDimName);
					}
				}
				table.redraw();

			}
		});

		Composite bottomComposite = new Composite(shlCreateCube, SWT.NONE);
		bottomComposite.setLayout(new GridLayout(2, false));
		bottomComposite.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, true, false, 2, 1));

		Button createCubeButton = new Button(bottomComposite, SWT.PUSH);
		createCubeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				createNewCube();
				refreshOnClose = true;
				shlCreateCube.close();
			}
		});
		GridData gd_createCubeButton = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_createCubeButton.widthHint = 150;
		gd_createCubeButton.minimumWidth = 200;
		createCubeButton.setLayoutData(gd_createCubeButton);
		createCubeButton.setBounds(0, 0, 90, 30);
		createCubeButton.setText("Create Cube");

		Button cancelButton = new Button(bottomComposite, SWT.PUSH);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				shlCreateCube.close();
			}
		});
		GridData gd_cancelButton = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_cancelButton.widthHint = 150;
		cancelButton.setLayoutData(gd_cancelButton);
		cancelButton.setBounds(0, 0, 90, 30);
		cancelButton.setText("Cancel");

		refreshDimensionTable();
	}

	private void refreshDimensionTable() throws TM1RestException, ClientProtocolException, URISyntaxException, IOException {
		allDimensionsTable.removeAll();
		String request = "Dimensions";
		tm1server.get(request);
		try {
			OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
			JSONArray jdimensions = jresponse.getJSONArray("value");
			for (int i = 0; i < jdimensions.length(); i++) {
				OrderedJSONObject jdimension = (OrderedJSONObject) jdimensions.getJSONObject(i);
				TableItem t = new TableItem(allDimensionsTable, SWT.NONE);
				t.setText(jdimension.getString("Name"));
			}
		} catch (Exception ex) {
			ex.printStackTrace();

		}
	}

	private void createNewCube() {
		try {
			String newCubeName = cubeNameText.getText();
			if (newCubeName.isEmpty()) {
				errorMessage("Cube name required");
				return;
			}

			String request = "Cubes";
			OrderedJSONObject payload = new OrderedJSONObject();
			payload.put("Name", newCubeName);

			JSONArray jdimensions = new JSONArray();
			for (int i = 0; i < cubeDimensionsTable.getItemCount(); i++) {
				TableItem t = cubeDimensionsTable.getItem(i);
				jdimensions.put("Dimensions('" + t.getText() + "')");
			}
			payload.put("Dimensions@odata.bind", jdimensions);
			tm1server.post(request, payload);
			infoMessage("Cube " + newCubeName + " created");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void infoMessage(String message) {
		MessageBox m = new MessageBox(shlCreateCube, SWT.ICON_INFORMATION);
		m.setMessage(message);
		m.open();
	}

	public void errorMessage(String errMessage) {
		MessageBox m = new MessageBox(shlCreateCube, SWT.ICON_ERROR);
		m.setMessage(errMessage);
		m.open();
	}

	public void errorMessage(String code, String message) {
		MessageBox m = new MessageBox(shlCreateCube, SWT.ICON_ERROR);
		m.setMessage("Error code " + code + "\n" + message);
		m.open();
	}

}
