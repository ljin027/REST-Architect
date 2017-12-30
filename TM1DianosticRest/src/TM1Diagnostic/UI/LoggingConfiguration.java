package TM1Diagnostic.UI;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.OrderedJSONObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import TM1Diagnostic.REST.TM1RestException;
import TM1Diagnostic.REST.TM1Server;

import org.eclipse.swt.layout.GridLayout;

public class LoggingConfiguration {

	protected Shell shell;
	protected Display display;

	private TM1Server tm1server;
	private Tree loggersTree;
	private TreeItem selectedTreeItem;

	final int EDITABLECOLUMN = 1;

	public LoggingConfiguration(Shell parent, TM1Server tm1server) throws TM1RestException {
		this.tm1server = tm1server;
		shell = new Shell(parent, SWT.RESIZE | SWT.MAX | SWT.MIN);
		shell.setSize(689, 448);
		shell.setText("Logging Configuration");
		shell.setLayout(new GridLayout(1, false));
		display = shell.getDisplay();
		createContents();
		shell.layout();
		shell.open();
	}

	private void createContents() throws TM1RestException {

		loggersTree = new Tree(shell, SWT.BORDER | SWT.FULL_SELECTION);
		loggersTree.setHeaderVisible(true);
		loggersTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		TreeColumn loggername_column = new TreeColumn(loggersTree, SWT.NONE);
		loggername_column.setWidth(253);
		loggername_column.setText("Name");

		TreeColumn loggerlevel_column = new TreeColumn(loggersTree, SWT.NONE);
		loggerlevel_column.setWidth(188);
		loggerlevel_column.setText("Level");

		loggersTree.addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(Event event) {
				Tree tree = (Tree) event.widget;
				int columnCount = tree.getColumnCount();
				if (columnCount == 0) return;
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

		loggersTree.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				final TreeEditor editor = new TreeEditor(loggersTree);
				editor.horizontalAlignment = SWT.LEFT;
				editor.grabHorizontal = true;
				editor.minimumWidth = 50;
				Control oldEditor = editor.getEditor();
				if (oldEditor != null)
					oldEditor.dispose();

				TreeItem item = (TreeItem) e.item;
				selectedTreeItem = item;
				if (item == null)
					return;
				Combo newEditor = new Combo(loggersTree, SWT.NONE);
				newEditor.add("Fatal");
				newEditor.add("Error");
				newEditor.add("Warning");
				newEditor.add("Info");
				newEditor.add("Debug");
				newEditor.add("Unknown");
				newEditor.add("Off");


				newEditor.setText(item.getText(EDITABLECOLUMN));

				newEditor.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent me) {
						Combo combo = (Combo)editor.getEditor();
						editor.getItem().setText(EDITABLECOLUMN, combo.getText());
						patchlogger(selectedTreeItem.getText(0), selectedTreeItem.getText(1));
					}
				});
				editor.setEditor(newEditor, item, EDITABLECOLUMN);

			}
		});
		readLoggersFromServer();
	}

	private void setChildLoggers(TreeItem selectedTreeItem, String loggerLevel) {
		for (int i=0; i<selectedTreeItem.getItemCount(); i++){
			TreeItem childTreeItem = selectedTreeItem.getItem(i);
			if(patchlogger(childTreeItem.getText(0), loggerLevel)){
				childTreeItem.setText(1, loggerLevel);
			}
			if(childTreeItem.getItemCount() > 0){
				setChildLoggers(childTreeItem, loggerLevel);
			}
		}
	}

	public TreeItem treeContains(Tree t, String s) {
		for (int i = 0; i < t.getItemCount(); i++) {
			TreeItem node = t.getItem(i);
			if (s.startsWith(node.getText())) {
				return treeContains(node, s);
			}
		}
		return null;
	}

	public TreeItem treeContains(TreeItem t, String s) {
		for (int i = 0; i < t.getItemCount(); i++) {
			String searchnodename = t.getItem(i).getText();
			String p = s.substring(s.lastIndexOf('.'), s.length());
			if (p.equals(searchnodename)) {
				return t.getItem(i);
			}
			if (s.startsWith(searchnodename)) {
				return treeContains(t.getItem(i), s);
			}
		}
		return t;
	}

	public boolean patchlogger(String logger, String level) {
		String request = "Loggers('" + logger + "')";
		try {
			OrderedJSONObject payload = new OrderedJSONObject();
			payload.put("Level", level);
			//System.out.println("Logger update payload " + payload.toString());
			tm1server.patch(request, payload);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	public void setTM1Server(TM1Server tm1server){
		this.tm1server = tm1server;
	}

	public void readLoggersFromServer() {
		try {
			loggersTree.removeAll();
			String request = "Loggers";
			tm1server.get(request);
			OrderedJSONObject jresponse = new OrderedJSONObject(tm1server.response);
			JSONArray json_loggers = jresponse.getJSONArray("value");
			for (int i = 0; i < json_loggers.length(); i++) {
				OrderedJSONObject json_logger = (OrderedJSONObject) json_loggers.getJSONObject(i);
				String logger_name = json_logger.getString("Name");
				String logger_level = json_logger.getString("Level");
				TreeItem t = treeContains(loggersTree, logger_name);
				if (t != null) {
					TreeItem n = new TreeItem(t, SWT.NONE);
					n.setText(0, logger_name);
					n.setText(1, logger_level);

				} else {
					TreeItem n = new TreeItem(loggersTree, SWT.NONE);
					n.setText(0, logger_name);
					n.setText(1, logger_level);
				}
			}
			if (loggersTree.getItemCount() > 0) {
				loggersTree.getItem(0).setExpanded(true);
			}
			for (TreeColumn tc : loggersTree.getColumns()) tc.pack();
			loggersTree.redraw();
		} catch (URISyntaxException | IOException | JSONException | TM1RestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
