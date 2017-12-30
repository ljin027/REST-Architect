package TM1Diagnostic.UI;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;

import TM1Diagnostic.Credential;
import TM1Diagnostic.REST.TM1Server;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;

public class CredentialsPrompt extends Dialog {

	protected boolean result;
	protected Shell shlConnectionProperties;
	private Text user_text;
	private Text password_text;
	private Text namespace_text;
	private Button connect_button;
	TM1Server tm1server;
	private Credential credential;

	private String path = ".//connections//";
	
	/**
	 * @wbp.parser.constructor
	 */
	public CredentialsPrompt(Shell parent, int style, TM1Server server) {
		super(parent, style);
		setText("Set Credentials");
		this.tm1server = server;
	}
	
	public CredentialsPrompt(Shell parent, TM1Server server) {
		super(parent, SWT.DIALOG_TRIM);
		setText("Set Credentials");
		this.tm1server = server;
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public boolean open() {
		createContents();
		shlConnectionProperties.open();
		shlConnectionProperties.layout();
		Display display = getParent().getDisplay();
		while (!shlConnectionProperties.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlConnectionProperties = new Shell(getParent(), getStyle());
		shlConnectionProperties.setSize(483, 188);
		shlConnectionProperties.setText("TM1 Server Credentials");
		shlConnectionProperties.setLayout(new GridLayout(1, false));
		
		Composite composite = new Composite(shlConnectionProperties, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		composite.setLayout(new GridLayout(2, false));
		
		Label namelabel = new Label(composite, SWT.NONE);
		namelabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		namelabel.setText("User Name");
		
		user_text = new Text(composite, SWT.BORDER);
		user_text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				if (user_text.equals("")){
					connect_button.setEnabled(false);
				}
			}
		});
		user_text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblNewLabel_1 = new Label(composite, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_1.setText("Password");
		
		password_text = new Text(composite, SWT.BORDER);
		password_text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		if (tm1server.getsecuritymode().equals("CAM")){
			Label lblNewLabel_2 = new Label(composite, SWT.NONE);
			lblNewLabel_2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			lblNewLabel_2.setText("Namespace");
			
			namespace_text = new Text(composite, SWT.BORDER);
			namespace_text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		} else {
			
		}
		
		Composite composite_1 = new Composite(shlConnectionProperties, SWT.NONE);
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1));
		composite_1.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		connect_button = new Button(composite_1, SWT.NONE);
		connect_button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (tm1server.getsecuritymode().equals("CAM")){
					credential = new Credential(user_text.getText(), password_text.getText(), namespace_text.getText());
					//tm1server.camauthenticate(user_text.getText(), password_text.getText(), namespace_text.getText());
				} else if (tm1server.getsecuritymode().equals("BASIC")){
					credential = new Credential(user_text.getText(), password_text.getText());
					//tm1server.basicauthenticate(user_text.getText(), password_text.getText());
				} else {
					// WTF
				}
				result = true;
				shlConnectionProperties.close();
			}
		});
		connect_button.setText("Ok");
		
		Button cancel_button = new Button(composite_1, SWT.NONE);
		cancel_button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				result = false;
				shlConnectionProperties.close();
			}
		});
		cancel_button.setText("Cancel");
	}
	
	public Credential getcredential(){
		return credential;
	}
	


}
