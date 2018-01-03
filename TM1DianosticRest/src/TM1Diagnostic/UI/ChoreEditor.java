package TM1Diagnostic.UI;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.OrderedJSONObject;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Combo;

import TM1Diagnostic.ChoreTask;
import TM1Diagnostic.REST.TM1Chore;
import TM1Diagnostic.REST.TM1Process;
import TM1Diagnostic.REST.TM1RestException;
import TM1Diagnostic.REST.TM1Server;

import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

public class ChoreEditor {

	static private Image SAVE;
	static private Image SAVEAS;
	static private Image EXECUTE;

	protected Shell shell;
	protected Display display;

	private Table processesTable;
	private Table choreProcessTable;
	private Button activeButton;
	private Combo commitModeCombo;
	private DateTime startDateCalendar;
	private DateTime startTimeLocal;

	private TM1Chore chore;
	private TM1Server tm1server;

	private boolean refreshOnClose;
	private Text daysFrequencyText;
	private Text secondsFrequencyText;
	private Text minutesFrequencyText;
	private Text hoursFrequencyText;

	private Menu choreProcessMenu;

	private boolean pendingSave;
	private boolean newChore;

	/**
	 * @wbp.parser.constructor
	 */
	public ChoreEditor(Shell parent, TM1Chore chore) {
		this.chore = chore;
		tm1server = chore.tm1server;
		shell = new Shell(parent, SWT.RESIZE | SWT.MAX | SWT.MIN);
		//shell.setSize(689, 448);
		shell.setText("Chore Editor - " + chore.name);
		newChore = false;
		shell.setLayout(new GridLayout(1, false));
		display = shell.getDisplay();
		createContents();
		shell.layout();
		shell.open();
	}

	public ChoreEditor(Shell parent, TM1Server tm1server) {
		this.tm1server = tm1server;
		shell = new Shell(parent, SWT.RESIZE | SWT.MAX | SWT.MIN);
		//shell.setSize(689, 448);
		shell.setText("Chore Editor - *New Chore");
		newChore = true;
		shell.setLayout(new GridLayout(1, false));
		display = shell.getDisplay();
		createContents();
		shell.layout();
		shell.open();
	}

	private void createContents(){	
		pendingSave = false;
		
		shell.setSize(1055, 680);

		Device device = Display.getCurrent();
		SAVE = new Image(device, ".//images//action_save.gif");
		SAVEAS = new Image(device, ".//images//action_save_as.gif");
		EXECUTE = new Image(device, ".//images//action_run.gif");

		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		composite.setLayout(new GridLayout(3, true));

		Composite processButtonComposite = new Composite(composite, SWT.NONE);
		processButtonComposite.setLayout(new GridLayout(4, false));
		processButtonComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));

		Label lblNewLabel_3 = new Label(processButtonComposite, SWT.NONE);
		lblNewLabel_3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_3.setText("Commit Mode");

		commitModeCombo = new Combo(processButtonComposite, SWT.NONE);
		commitModeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		commitModeCombo.add("Single");
		commitModeCombo.add("Multiple");

		Label lblNewLabel_1 = new Label(processButtonComposite, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));

		activeButton = new Button(processButtonComposite, SWT.CHECK);
		activeButton.setText("Active");

		Composite availableProcessesComposite = new Composite(composite, SWT.NONE);
		availableProcessesComposite.setLayout(new GridLayout(1, false));
		availableProcessesComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		processesTable = new Table(availableProcessesComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		processesTable.setHeaderVisible(true);
		processesTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		processesTable.setBounds(0, 0, 89, 51);
		processesTable.setLinesVisible(true);

		final List<Item> dragSourceItems = new ArrayList<Item>();
		Transfer[] types = new Transfer[] { TextTransfer.getInstance() };

		DragSource processesTableSource = new DragSource(processesTable, DND.DROP_MOVE);
		processesTableSource.setTransfer(types);
		processesTableSource.addDragListener(new DragSourceAdapter() {

			public void dragStart(DragSourceEvent event) {
				TableItem[] selection = processesTable.getSelection();
				if (selection.length > 0) {
					event.doit = true;
					dragSourceItems.clear();
					for (int i = selection.length - 1; i >= 0; i--) {
						dragSourceItems.add(selection[i]);
					}
					// System.out.println("-> " + dragSourceItems.size() +
					// " items to drag");
				} else {
					event.doit = false;
				}
			}

			public void dragSetData(DragSourceEvent event) {
				event.data = dragSourceItems.get(0).getText();
			}
		});

		TableColumn tblclmnNewColumn = new TableColumn(processesTable, SWT.NONE);
		tblclmnNewColumn.setWidth(257);
		tblclmnNewColumn.setText("Available");

		Composite choreProcesses = new Composite(composite, SWT.NONE);
		choreProcesses.setLayout(new GridLayout(1, false));
		choreProcesses.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		choreProcessTable = new Table(choreProcesses, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		choreProcessTable.setHeaderVisible(true);
		choreProcessTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		choreProcessTable.setLinesVisible(true);

		DragSource choreProcessTableSource = new DragSource(choreProcessTable, DND.DROP_MOVE);
		choreProcessTableSource.setTransfer(types);
		choreProcessTableSource.addDragListener(new DragSourceAdapter() {

			public void dragStart(DragSourceEvent event) {
				TableItem[] selection = choreProcessTable.getSelection();
				if (selection.length > 0) {
					event.doit = true;
					dragSourceItems.clear();
					for (int i = selection.length - 1; i >= 0; i--) {
						dragSourceItems.add(selection[i]);
					}
					// System.out.println("-> " + dragSourceItems.size() +
					// " items to drag");
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

		DropTarget choreProcessTableTarget = new DropTarget(choreProcessTable, DND.DROP_COPY | DND.DROP_MOVE);
		choreProcessTableTarget.setTransfer(types);
		choreProcessTableTarget.addDropListener(new DropTargetAdapter() {

			public void dragOver(DropTargetEvent event) {
				// System.out.println("Drag over on chore table");
				event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
				if (event.item != null) {
					Display display = shell.getDisplay();
					TableItem item = (TableItem) event.item;
					Point pt = display.map(null, choreProcessTable, event.x, event.y);
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

				// System.out.println("Drop onto on chore table");
				if (event.item != null) {
					// System.out.println("Drop onto on item");
					TableItem item = (TableItem) event.item;
					Display display = shell.getDisplay();
					Point pt = display.map(null, choreProcessTable, event.x, event.y);
					Rectangle bounds = item.getBounds();

					TableItem[] tableItems = choreProcessTable.getItems();
					int index = 0;
					for (int i = 0; i < tableItems.length; i++) {
						if (tableItems[i] == item) {
							index = i;
							break;
						}
					}
					if (pt.y < bounds.y + bounds.height / 3) {
						for (int i = 0; i < dragSourceItems.size(); i++) {
							TableItem newItem = new TableItem(choreProcessTable, SWT.NONE, index);
							newItem.setText(dragSourceItems.get(i).getText());
						}

					} else if (pt.y > bounds.y + 2 * bounds.height / 3) {
						for (int i = 0; i < dragSourceItems.size(); i++) {
							TableItem newItem = new TableItem(choreProcessTable, SWT.NONE, index + 1);
							newItem.setText(dragSourceItems.get(i).getText());
						}
					} else {
						for (int i = 0; i < dragSourceItems.size(); i++) {
							TableItem newItem = new TableItem(choreProcessTable, SWT.NONE);
							newItem.setText(dragSourceItems.get(i).getText());
						}
					}
				} else {
					// System.out.println("Drop onto on empty");
					for (int i = 0; i < dragSourceItems.size(); i++) {
						TableItem newItem = new TableItem(choreProcessTable, SWT.NONE);
						newItem.setText(dragSourceItems.get(i).getText());
					}
				}
				table.redraw();

			}
		});

		TableColumn tblclmnNewColumn_1 = new TableColumn(choreProcessTable, SWT.NONE);
		tblclmnNewColumn_1.setWidth(182);
		tblclmnNewColumn_1.setText("Selected");

		TableColumn tblclmnNewColumn_2 = new TableColumn(choreProcessTable, SWT.NONE);
		tblclmnNewColumn_2.setWidth(100);
		tblclmnNewColumn_2.setText("Parameter");

		choreProcessMenu = new Menu(choreProcessTable);
		choreProcessTable.setMenu(choreProcessMenu);

		choreProcessMenu.addMenuListener(new MenuAdapter() {
			@Override
			public void menuShown(org.eclipse.swt.events.MenuEvent arg0) {
				MenuItem[] items = choreProcessMenu.getItems();
				for (int i = 0; i < items.length; i++) {
					items[i].dispose();
				}

				TableItem[] selectedSubsetItems = choreProcessTable.getSelection();

				MenuItem setParametersMenuItem = new MenuItem(choreProcessMenu, SWT.NONE);
				setParametersMenuItem.setText("Set Parameters");
				setParametersMenuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						TableItem selectedItems = choreProcessTable.getSelection()[0];
						ChoreTask task = (ChoreTask)selectedItems.getData();
						//process.readProcessFromServer();
						UI_ChoreProcessParameters processParametersDialog = new UI_ChoreProcessParameters(shell, task.getProcess());
						if (processParametersDialog.open()) {

						}
					}
				});

				if (choreProcessTable.getSelectionCount() != 1) {
					setParametersMenuItem.setEnabled(false);
				}

			}
		});

		Composite composite_2 = new Composite(composite, SWT.NONE);
		composite_2.setLayout(new GridLayout(1, false));
		composite_2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));

		Label lblNewLabel_4 = new Label(composite_2, SWT.NONE);
		lblNewLabel_4.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_4.setText("Chore Start Time");

		startDateCalendar = new DateTime(composite_2, SWT.BORDER | SWT.CALENDAR);
		startDateCalendar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblNewLabel = new Label(composite_2, SWT.NONE);
		lblNewLabel.setText("Local Time");

		startTimeLocal = new DateTime(composite_2, SWT.BORDER | SWT.TIME);
		startTimeLocal.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		startTimeLocal.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent event) {
			}
		      
			public void focusLost(FocusEvent event) {
				MessageBox m = new MessageBox(shell, SWT.ERROR);
				m.setText("Focus lost");
				m.open();
			}
		});

		Label lblNewLabel_2 = new Label(composite_2, SWT.NONE);
		lblNewLabel_2.setText("GMT Time");

		DateTime startTimeGMT = new DateTime(composite_2, SWT.BORDER | SWT.TIME);
		startTimeGMT.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		startTimeGMT.setEnabled(false);

		Composite composite_1 = new Composite(composite_2, SWT.NONE);
		composite_1.setLayout(new GridLayout(2, false));
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));

		Label lblNewLabel_5 = new Label(composite_1, SWT.NONE);
		lblNewLabel_5.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1));
		lblNewLabel_5.setText("Chore Frequency");

		Label lblNewLabel_6 = new Label(composite_1, SWT.NONE);
		lblNewLabel_6.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_6.setText("Days");

		daysFrequencyText = new Text(composite_1, SWT.BORDER);
		daysFrequencyText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblNewLabel_7 = new Label(composite_1, SWT.NONE);
		lblNewLabel_7.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_7.setText("Hours");

		hoursFrequencyText = new Text(composite_1, SWT.BORDER);
		hoursFrequencyText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblNewLabel_8 = new Label(composite_1, SWT.NONE);
		lblNewLabel_8.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_8.setText("Minutes");

		minutesFrequencyText = new Text(composite_1, SWT.BORDER);
		minutesFrequencyText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblNewLabel_9 = new Label(composite_1, SWT.NONE);
		lblNewLabel_9.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_9.setText("Seconds");

		secondsFrequencyText = new Text(composite_1, SWT.BORDER);
		secondsFrequencyText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Composite composite_3 = new Composite(shell, SWT.NONE);
		composite_3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		composite_3.setLayout(new GridLayout(2, false));
		
		Button saveButton = new Button(composite_3, SWT.NONE);
		GridData gd_saveButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_saveButton.widthHint = 120;
		saveButton.setLayoutData(gd_saveButton);
		saveButton.setText("Save");
		
		Button closeButton = new Button(composite_3, SWT.NONE);
		GridData gd_closeButton = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_closeButton.widthHint = 120;
		closeButton.setLayoutData(gd_closeButton);
		closeButton.setBounds(0, 0, 90, 30);
		closeButton.setText("Close");

		refreshProcessTable();

		if (chore == null) {
			commitModeCombo.select(0);
			activeButton.setSelection(false);
			daysFrequencyText.setText("0");
			hoursFrequencyText.setText("0");
			minutesFrequencyText.setText("0");
			secondsFrequencyText.setText("0");
		} else {
			try {
				chore.readChoreTasksFromServer();
			} catch (TM1RestException | URISyntaxException | IOException | JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if (chore.executionMode.equals("SingleCommit")) {
				commitModeCombo.select(0);
			} else {
				commitModeCombo.select(1);
			}
			activeButton.setSelection(chore.active);

			String frequency = chore.frequency;
			int dtIndex = frequency.indexOf("DT");
			int fDays = Integer.parseInt(frequency.substring(1, dtIndex));
			daysFrequencyText.setText(Integer.toString(fDays));
			int hIndex = frequency.indexOf("H");
			int fHours = Integer.parseInt(frequency.substring(dtIndex + 2, hIndex));
			hoursFrequencyText.setText(Integer.toString(fHours));
			int mIndex = frequency.indexOf("M");
			int fMinutes = Integer.parseInt(frequency.substring(hIndex + 1, mIndex));
			minutesFrequencyText.setText(Integer.toString(fMinutes));
			int sIndex = frequency.indexOf("S");
			int fSeconds = Integer.parseInt(frequency.substring(mIndex + 1, sIndex));
			secondsFrequencyText.setText(Integer.toString(fSeconds));

			// 2017-02-20T18:00:30Z
			String sDateTime = chore.startDateTime.replaceAll("Z$", "+0000");

			SimpleDateFormat sdfgmt;
			SimpleDateFormat sdfmad;
			if (sDateTime.length() == 24) {
				sdfgmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
				sdfmad = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
			} else {
				sdfgmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
				sdfmad = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
			}

			sdfgmt.setTimeZone(TimeZone.getTimeZone("GMT"));
			sdfmad.setTimeZone(TimeZone.getTimeZone("EST"));

			Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
			Calendar callocal = new GregorianCalendar(TimeZone.getTimeZone("EST"));

			try {
				cal.setTime(sdfgmt.parse(sDateTime));
				callocal.setTime(sdfmad.parse(sDateTime));

				// System.out.println("String : " + sDateTime);
				startDateCalendar.setDay(cal.get(cal.DATE));
				startDateCalendar.setMonth(cal.get(cal.MONTH));
				startDateCalendar.setYear(cal.get(cal.YEAR));

				startTimeGMT.setTime(cal.get(cal.HOUR), cal.get(cal.MINUTE), cal.get(cal.SECOND));
				startTimeLocal.setTime(callocal.get(callocal.HOUR), callocal.get(callocal.MINUTE), callocal.get(callocal.SECOND));

			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			refreshChoreProcessTable();
		}


	}

	private void refreshProcessTable() {
		try {

			processesTable.removeAll();
			String request = "Processes";
			String query = "$select=Name";
			tm1server.get(request, query);
			OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
			JSONArray jprocesses = jresponse.getJSONArray("value");
			for (int i = 0; i < jprocesses.length(); i++) {
				OrderedJSONObject jprocess = (OrderedJSONObject) jprocesses.getJSONObject(i);
				TableItem t = new TableItem(processesTable, SWT.NONE);
				t.setText(jprocess.getString("Name"));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void refreshChoreProcessTable() {
		try {
			choreProcessTable.removeAll();
			for (int i = 0; i < chore.choreTaskCount(); i++) {
				TableItem t = new TableItem(choreProcessTable, SWT.NONE);
				ChoreTask task = chore.getChoreTask(i);
				t.setText(task.getProcess().name);
				t.setData(task);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void updateChoreFromUI(){

		if (commitModeCombo.getSelectionIndex() == 0) {
			chore.executionMode = "SingleCommit";
		} else {
			chore.executionMode = "MultipleCommit";
		}

		chore.active = activeButton.getSelection();

		String fDays =  daysFrequencyText.getText();
		String fHours = hoursFrequencyText.getText();
		String fMinutes = minutesFrequencyText.getText();
		String fSeconds = secondsFrequencyText.getText();

		//P1DT00H00M00S
		String frequency = "P" + fDays + "DT" + fHours + "M" + fMinutes + "S";

		// 2017-02-20T18:00:30Z
		String startYear = Integer.toString(startDateCalendar.getYear());
		String startMonth = Integer.toString(startDateCalendar.getMonth());
		String startDay = Integer.toString(startDateCalendar.getDay());

		String startHours = Integer.toString(startTimeLocal.getHours());
		String startMinutes = Integer.toString(startTimeLocal.getMinutes());
		String startSeconds = Integer.toString(startTimeLocal.getSeconds());

		String gmtStartDate = startYear + "-" + startMonth + "-" + startDay + "T" + startHours + ":" + startMinutes + ":" + startSeconds + "Z";
		chore.startDateTime = gmtStartDate;

		chore.clearChoreTasks();
		for (int i=0; i<choreProcessTable.getItemCount(); i++){
			ChoreTask choreTask = (ChoreTask)choreProcessTable.getItem(i).getData();
			chore.addChoreTask(choreTask);
		}


	}

	public void infoMessage(String message) {
		MessageBox m = new MessageBox(shell, SWT.ICON_INFORMATION);
		m.setMessage(message);
		m.open();
	}

	public void errorMessage(String errMessage) {
		MessageBox m = new MessageBox(shell, SWT.ICON_ERROR);
		m.setMessage(errMessage);
		m.open();
	}

	public boolean save(){

		return true;
	}

	public boolean saveAs(){

		return true;
	}

	public boolean pendingSave(){
		return pendingSave;
	}
}
