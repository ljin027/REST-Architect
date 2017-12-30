package TM1Diagnostic.UI;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.EventObject;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;


import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.swt.widgets.Text;

import TM1Diagnostic.FunctionsMenuContent;
import TM1Diagnostic.ProcessParameter;
import TM1Diagnostic.ProcessWorker;
import TM1Diagnostic.REST.TM1Process;

public class UI_ChoreProcessParameters extends Dialog {

	final int EDITABLECOLUMN = 2;
	
	protected boolean result;
	protected Shell shlProcessParameters;
	private TM1Process process;
	private Table table;
	private Composite parentUI;
	
	private boolean refreshOnClose;
	

	public UI_ChoreProcessParameters(Shell parent, TM1Process process, Composite parentUI) {
		super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		setText("SWT Dialog");
		this.process = process;
		this.parentUI = parentUI;
	}

	public UI_ChoreProcessParameters(Shell parent, TM1Process process) {
		super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		setText("SWT Dialog");
		this.process = process;
		this.parentUI = null;
	}

	
	public boolean open() {
		createContents();
		refreshOnClose = false;
		shlProcessParameters.open();
		shlProcessParameters.layout();
		Display display = getParent().getDisplay();
		while (!shlProcessParameters.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return refreshOnClose;
	}

	private boolean fillInParametersTable(){
		table.clearAll();
		for (int i=0; i<process.parametercount(); i++){
			ProcessParameter p = process.getparameter(i);
			TableItem t = new TableItem(table, SWT.NONE);
			t.setText(0, p.getname());
			if(p.gettype() == ProcessParameter.NUMERIC){
				t.setText(1, "Numeric");
			} else {
				t.setText(1, "String");
			}
			t.setText(2, p.getvalue());
			t.setText(3, p.getprompt());
		}
		return true;
	}

	private void createContents() {
		shlProcessParameters = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
		shlProcessParameters.setSize(859, 374);
		shlProcessParameters.setText("Chore Process Parameters");
		shlProcessParameters.setLayout(new GridLayout(1, false));
		
		Composite composite_2 = new Composite(shlProcessParameters, SWT.NONE);
		composite_2.setLayout(new FillLayout(SWT.HORIZONTAL));
		composite_2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		table = new Table(composite_2, SWT.BORDER | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		
		table.addSelectionListener(new SelectionAdapter() {
	        @Override
	        public void widgetSelected(SelectionEvent e) {
	            final TableEditor editor = new TableEditor(table);               
	            editor.horizontalAlignment = SWT.LEFT;
	            editor.grabHorizontal = true;
	            editor.minimumWidth = 50;
	            Control oldEditor = editor.getEditor();
	            if (oldEditor != null) oldEditor.dispose();                
	            TableItem item = (TableItem) e.item;
	            if (item == null) return;
	            Text newEditor = new Text(table, SWT.NONE);
	            newEditor.setText(item.getText(EDITABLECOLUMN));
	            newEditor.addModifyListener(new ModifyListener() {
	                public void modifyText(ModifyEvent me) {
	                    Text text = (Text) editor.getEditor();
	                    editor.getItem().setText(EDITABLECOLUMN, text.getText());
	                }
	            });         
	            newEditor.selectAll();
	            newEditor.setFocus();           
	            editor.setEditor(newEditor, item, EDITABLECOLUMN);      
	        }
	    });     
		
		
		TableColumn tblclmnNewColumn = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn.setWidth(193);
		tblclmnNewColumn.setText("Name");
		
		TableColumn tblclmnNewColumn_3 = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn_3.setWidth(66);
		tblclmnNewColumn_3.setText("Type");
		
		TableColumn tblclmnNewColumn_1 = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn_1.setWidth(324);
		tblclmnNewColumn_1.setText("Value");
		
		TableColumn tblclmnNewColumn_2 = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn_2.setWidth(212);
		tblclmnNewColumn_2.setText("Description");
		
		Composite composite_1 = new Composite(shlProcessParameters, SWT.NONE);
		composite_1.setLayout(new FillLayout(SWT.HORIZONTAL));
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		Button saveButton = new Button(composite_1, SWT.NONE);
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				refreshOnClose = true;
				shlProcessParameters.close();
				//
			}
		});
		saveButton.setText("Save");
		
		Button cancelButton = new Button(composite_1, SWT.NONE);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				shlProcessParameters.close();
			}
		});
		cancelButton.setBounds(0, 0, 90, 30);
		cancelButton.setText("Cancel");
		
		fillInParametersTable();

	}
}
