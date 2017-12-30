package TM1Diagnostic.UI;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class UI_RulesFind extends Dialog {

	protected Object result;
	protected Shell shlFind;
	private Text find_text;
	
	//private Composite_RuleEditor parentComposite;

	public UI_RulesFind(Shell parent) {
		super(parent, SWT.DIALOG_TRIM);
		//this.parentComposite = parentComposite;
	}


	public Object open() {
		createContents();
		shlFind.open();
		shlFind.layout();
		Display display = getParent().getDisplay();
		while (!shlFind.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	private void createContents() {
		shlFind = new Shell(getParent(), getStyle());
		shlFind.setSize(448, 116);
		shlFind.setText("Find");
		shlFind.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		Composite composite = new Composite(shlFind, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		
		Label lblNewLabel = new Label(composite, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, true, 1, 1));
		lblNewLabel.setText("Find");
		
		find_text = new Text(composite, SWT.BORDER);
		find_text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
		
		Composite composite_1 = new Composite(composite, SWT.NONE);
		composite_1.setSize(432, 40);
		composite_1.setLayout(new FillLayout(SWT.HORIZONTAL));
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		Button btnNewButton = new Button(composite_1, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				//parentComposite.findprevious(find_text.getText());
			}
		});
		btnNewButton.setText("Previous");
		
		Button btnNewButton_2 = new Button(composite_1, SWT.NONE);
		btnNewButton_2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				//parentComposite.findnext(find_text.getText());
			}
		});
		btnNewButton_2.setText("Next");

	}
	
	public void close(){
		shlFind.dispose();
	}
	
}
