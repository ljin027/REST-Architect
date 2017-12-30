package TM1Diagnostic.UI;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;

import TM1Diagnostic.Credential;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class AddUser {

	protected Shell shell;
	private Text userText;
	private Text passwordText;
	private Text namespaceText;
	private boolean isCam;

	private List<Credential> credentials;

	/**
	 * @wbp.parser.entryPoint
	 */

	public AddUser(Shell parent, List<Credential> credentials, boolean isCam){
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		this.credentials = credentials;
		this.isCam = isCam;
		createContents();
		shell.layout();
	}

	/**
	 * Open the window.
	 */
	public boolean open() {
		Display display = shell.getDisplay();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return true;
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell.setSize(500, 200);
		shell.setText("Add Credentials");
		shell.setLayout(new GridLayout(1, false));

		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		composite.setLayout(new GridLayout(2, false));

		Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("User");

		userText = new Text(composite, SWT.BORDER);
		userText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label label_1 = new Label(composite, SWT.NONE);
		label_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label_1.setText("Password");

		passwordText = new Text(composite, SWT.BORDER | SWT.PASSWORD);
		passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		if (isCam){
			Label label_2 = new Label(composite, SWT.NONE);
			label_2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			label_2.setText("Namespace");

			namespaceText = new Text(composite, SWT.BORDER);
			namespaceText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		}

		Composite composite_1 = new Composite(shell, SWT.NONE);
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		composite_1.setLayout(new GridLayout(2, false));

		Button addButton = new Button(composite_1, SWT.NONE);
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addUserToList();
			}
		});
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		addButton.setText("Add");

		Button closeButton = new Button(composite_1, SWT.NONE);
		closeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}
		});
		closeButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		closeButton.setText("Close");

	}

	private void addUserToList(){
		Credential c;
		if (isCam){
			String username = userText.getText();
			String password = passwordText.getText();
			String namespace = namespaceText.getText();
			c = new Credential(username, password, namespace);
			if (!credentials.contains(c)){
				credentials.add(c);
			} else {
				MessageBox m = new MessageBox(shell, SWT.ERROR);
				m.setMessage("Credentials already stored");
				m.open();
			}
		} else {
			String username = userText.getText();
			String password = passwordText.getText();
			c = new Credential(username, password);
			if (!credentials.contains(c)){
				credentials.add(c);
			} else {
				MessageBox m = new MessageBox(shell, SWT.ERROR);
				m.setMessage("Credentials already stored");
				m.open();
			}
		}
		userText.setText("");
		passwordText.setText("");
	}


}
