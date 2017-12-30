package TM1Diagnostic.UI;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class UI_NamePrompt extends Dialog {

	protected boolean result;
	protected Shell shlRenameProcess;
	private Text newObjectNameText;
	private Button okButton;
	private Button cancelButton;
	private String defaultval;
	
	private String title;
	private String name;


	public UI_NamePrompt(Shell parent, String title, String defaultval) {
		super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		this.title = title;
		this.defaultval = defaultval;
		result = false;
	}
	
	
	public boolean open() {
		createContents();
		shlRenameProcess.open();
		shlRenameProcess.layout();
		Display display = getParent().getDisplay();
		shlRenameProcess.setText(title);
		while (!shlRenameProcess.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}
	
	public String getobjectname(){
		return name;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlRenameProcess = new Shell(getParent(), getStyle());
		shlRenameProcess.setSize(459, 177);
		shlRenameProcess.setLayout(new GridLayout(1, false));
		
		newObjectNameText = new Text(shlRenameProcess, SWT.BORDER);
		newObjectNameText.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1));
		
		Composite composite = new Composite(shlRenameProcess, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1));
		composite.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		okButton = new Button(composite, SWT.NONE);
		okButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				result = true;
				name = newObjectNameText.getText();
				shlRenameProcess.close();
			}
		});
		okButton.setText("Ok");
		
		cancelButton = new Button(composite, SWT.NONE);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				result = false;
				name = null;
				shlRenameProcess.close();
			}
		});
		cancelButton.setText("Cancel");

	}
}
