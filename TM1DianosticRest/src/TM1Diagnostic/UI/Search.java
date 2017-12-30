package TM1Diagnostic.UI;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Button;

import TM1Diagnostic.REST.TM1Server;

public class Search extends Dialog {

	protected Object result;
	protected Shell shlSearch;
	private Text text;
	
	private TM1Server tm1server;

	public Search(Shell parent, TM1Server tm1server) {
		super(parent, SWT.DIALOG_TRIM);
		this.tm1server = tm1server;
	}

	public Object open() {
		createContents();
		shlSearch.open();
		shlSearch.layout();
		Display display = getParent().getDisplay();
		while (!shlSearch.isDisposed()) {
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
		shlSearch = new Shell(getParent(), getStyle());
		shlSearch.setSize(674, 252);
		shlSearch.setText("Search");
		shlSearch.setLayout(new GridLayout(2, false));
		
		Composite composite = new Composite(shlSearch, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Label lblNewLabel = new Label(composite, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("Object Type");
		
		Combo objectTypeCombo = new Combo(composite, SWT.NONE);
		objectTypeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		objectTypeCombo.add("All");
		objectTypeCombo.add("Cubes");
		objectTypeCombo.add("Views");
		objectTypeCombo.add("Dimensions");
		objectTypeCombo.add("Hierarchies");
		objectTypeCombo.add("Subsets");
		objectTypeCombo.add("Processes");
		objectTypeCombo.add("Chores");
		
		
		Label lblNewLabel_1 = new Label(composite, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_1.setText("Name");
		
		text = new Text(composite, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Composite buttonsComposite = new Composite(shlSearch, SWT.NONE);
		buttonsComposite.setLayout(new GridLayout(1, false));
		buttonsComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, true, 1, 1));
		
		Button findNextButton = new Button(buttonsComposite, SWT.NONE);
		findNextButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
		findNextButton.setText("Find Next");
		
		Button btnNewButton = new Button(buttonsComposite, SWT.NONE);
		btnNewButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
		btnNewButton.setText("Find Previous");

	}

}
