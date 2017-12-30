package TM1Diagnostic.UI;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;

public class ClientProperties {

	protected Shell shell;
	private Text keystoreFileText;
	private Text keystorePassText;

	/**
	 * Launch the application.
	 * @param args
	 */
	ClientProperties(Shell parent){
		shell = new Shell(parent, SWT.RESIZE | SWT.MAX | SWT.MIN);
		shell.setSize(656, 199);
		shell.setText("Properties");
	}
	
	/**
	 * Open the window.
	 */
	public boolean open() {
		Display display = Display.getDefault();
		createContents();
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
		shell.setLayout(new GridLayout(1, false));

		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Group sslGroup = new Group(composite, SWT.NONE);
		sslGroup.setText("SSL Settings");
		sslGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		sslGroup.setLayout(new GridLayout(2, false));
		
		Label lblNewLabel = new Label(sslGroup, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("TrustStore File");
		
		keystoreFileText = new Text(sslGroup, SWT.BORDER);
		keystoreFileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblNewLabel_1 = new Label(sslGroup, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_1.setText("TrustStore Password");
		
		keystorePassText = new Text(sslGroup, SWT.BORDER);
		keystorePassText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Composite composite_1 = new Composite(composite, SWT.NONE);
		composite_1.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, false, 1, 1));
		composite_1.setLayout(new GridLayout(2, false));
		
		Button btnNewButton = new Button(composite_1, SWT.NONE);
		GridData gd_btnNewButton = new GridData(SWT.FILL, SWT.BOTTOM, false, false, 1, 1);
		gd_btnNewButton.widthHint = 120;
		btnNewButton.setLayoutData(gd_btnNewButton);
		btnNewButton.setText("Apply");
		
		Button btnNewButton_1 = new Button(composite_1, SWT.NONE);
		GridData gd_btnNewButton_1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnNewButton_1.widthHint = 120;
		btnNewButton_1.setLayoutData(gd_btnNewButton_1);
		btnNewButton_1.setText("Cancel");

		
		readConfigurationFromFile();
	}
	
	
	public boolean readConfigurationFromFile() {
		String configFileName = ".//config//config";
		try {
			FileReader fr = new FileReader(configFileName);
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();
			while (line != null) {
				String[] tokens = line.split(":");
				String parameter = tokens[0];
				String value = tokens[1];
				if (parameter.equals("keystoreFile")){
					keystoreFileText.setText(value);
				} else if (parameter.equals("keystorePass")){
					keystorePassText.setText(value);
				}
				
				line = br.readLine();
			}
			br.close();
			fr.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}
	
	
	public boolean writeLocalAdminServersToFile() {
		try {
			String configFileName = ".//config//config";
			FileWriter fw = new FileWriter(configFileName);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter pw = new PrintWriter(bw);

			//
			
			pw.close();
			bw.close();
			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

}
