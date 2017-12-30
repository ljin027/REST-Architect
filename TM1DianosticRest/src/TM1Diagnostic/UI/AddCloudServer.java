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

public class AddCloudServer {

	protected Shell shlAddTmAdmin;
	
	
	
	private Text cloudHostNameText;
	private Text tm1ModelNameText;
	
	private String cloudHostName;
	private String tm1ModelName;
	
	private boolean refreshOnClose;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			AddCloudServer window = new AddCloudServer();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean open() {
		refreshOnClose = false;
		Display display = Display.getDefault();
		createContents();
		shlAddTmAdmin.open();
		shlAddTmAdmin.layout();
		while (!shlAddTmAdmin.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return refreshOnClose;
	}

	protected void createContents() {
		shlAddTmAdmin = new Shell();
		shlAddTmAdmin.setSize(534, 177);
		shlAddTmAdmin.setText("Add IBM Cloud TM1 Server");
		shlAddTmAdmin.setLayout(new GridLayout(1, false));
		
		Composite composite = new Composite(shlAddTmAdmin, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Label lblNewLabel = new Label(composite, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("IBM Cloud Hostname");
		
				cloudHostNameText = new Text(composite, SWT.BORDER);
				cloudHostNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		

		Label lblNewLabel_1 = new Label(composite, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_1.setText("TM1 Server Name");
		
		tm1ModelNameText = new Text(composite, SWT.BORDER);
		tm1ModelNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		

		Button addButton = new Button(shlAddTmAdmin, SWT.NONE);
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				cloudHostName = cloudHostNameText.getText();
				tm1ModelName = tm1ModelNameText.getText();
				refreshOnClose = true;
				shlAddTmAdmin.close();
			}
		});
		addButton.setText("Add");
		
	}
	
	
	
	public String getCloudHostName(){
		return cloudHostName;
	}
	
	public String getTM1ModelName(){
		return tm1ModelName;
	}
}
