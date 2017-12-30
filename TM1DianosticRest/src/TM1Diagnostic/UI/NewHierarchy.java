package TM1Diagnostic.UI;

import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.OrderedJSONObject;
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

import TM1Diagnostic.REST.TM1Dimension;
import TM1Diagnostic.REST.TM1Hierarchy;
import TM1Diagnostic.REST.TM1Server;

public class NewHierarchy extends Dialog {

	protected boolean result;
	protected Shell shlRenameProcess;
	private Text newObjectNameText;
	private Button okButton;
	private Button cancelButton;

	private TM1Dimension dimension;
	private TM1Hierarchy createdHierarchy;
	private TM1Server tm1server;

	private String name;
	private Label lblNewLabel;
	private Composite composite_1;

	public NewHierarchy(Shell parent, TM1Dimension dimension) {
		super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		this.dimension = dimension;
		this.tm1server = dimension.getServer();
		result = false;
	}

	public boolean open() {
		createContents();
		shlRenameProcess.open();
		shlRenameProcess.layout();
		Display display = getParent().getDisplay();
		shlRenameProcess.setText("Create Dimension");
		while (!shlRenameProcess.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	public String getobjectname() {
		return name;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlRenameProcess = new Shell(getParent(), getStyle());
		shlRenameProcess.setSize(554, 135);
		shlRenameProcess.setLayout(new GridLayout(1, false));

		composite_1 = new Composite(shlRenameProcess, SWT.NONE);
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		composite_1.setLayout(new GridLayout(2, false));

		lblNewLabel = new Label(composite_1, SWT.NONE);
		lblNewLabel.setText("Dimension Name");

		newObjectNameText = new Text(composite_1, SWT.BORDER);
		newObjectNameText.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1));

		Composite composite = new Composite(shlRenameProcess, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, true, false, 1, 1));
		composite.setLayout(new GridLayout(2, false));

		okButton = new Button(composite, SWT.NONE);
		GridData gd_okButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_okButton.widthHint = 120;
		okButton.setLayoutData(gd_okButton);
		okButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				createNewHierarchy();
			}
		});
		okButton.setText("Ok");

		cancelButton = new Button(composite, SWT.NONE);
		GridData gd_cancelButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_cancelButton.widthHint = 120;
		cancelButton.setLayoutData(gd_cancelButton);
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

	private void createNewHierarchy() {
		try {
			result = true;
			name = newObjectNameText.getText();
			String request = dimension.getEntity() + "/Hierarchies";
			OrderedJSONObject payload = new OrderedJSONObject();
			payload.put("Name", name);
			payload.put("UniqueName", "[" + name + "]");
			payload.put("Structure", 0);
			payload.put("Visible", true);
			tm1server.post(request, payload);

			createdHierarchy = new TM1Hierarchy(name, tm1server, dimension);
			
			shlRenameProcess.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public TM1Hierarchy getCreatedHierarchy(){
		return createdHierarchy;
	}
	
}
