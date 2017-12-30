package TM1Diagnostic.UI;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class AddLocalAdminServer {

	protected Shell shell;
	private Text adminServerNameText;
	private Text adminServerPortText;
	private String adminServerName;
	private String adminServerPort;
	private boolean refreshOnClose;

	AddLocalAdminServer(Shell parent) {
		shell = new Shell(parent, SWT.DIALOG_TRIM);
		shell.setSize(500, 175);
		shell.setText("Add Local Admin Server");
		createContents();
		shell.layout();
		shell.open();
	}

	public boolean open() {
		refreshOnClose = false;
		Display display = Display.getDefault();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return refreshOnClose;
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	protected void createContents() {
		shell.setLayout(new GridLayout(1, false));
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Label lblNewLabel = new Label(composite, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("Hostname");
		
		adminServerNameText = new Text(composite, SWT.BORDER);
		adminServerNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblNewLabel_1 = new Label(composite, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_1.setText("Port");
		
		adminServerPortText = new Text(composite, SWT.BORDER);
		adminServerPortText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				
		Button addButton = new Button(shell, SWT.NONE);
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				adminServerName = adminServerNameText.getText();
				adminServerPort = adminServerPortText.getText();
				refreshOnClose = true;
				shell.close();
			}
		});
		addButton.setText("Add");
	}
	
	public String getAdminServerName(){
		return adminServerName;
	}
	
	public String getAdminServerPort(){
		return adminServerPort;
	}
}
