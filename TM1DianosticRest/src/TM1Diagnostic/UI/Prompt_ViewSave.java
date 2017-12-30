package TM1Diagnostic.UI;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;

public class Prompt_ViewSave extends Dialog {

	protected boolean result;
	protected Shell shlSaveView;
	private Text viewNameText;
	private boolean isPrivate;
	private String viewName;


	public Prompt_ViewSave(Shell parent, int style) {
		super(parent, style);
		
	}


	public boolean open() {
		createContents();
		result = false;
		isPrivate = true;
		shlSaveView.open();
		shlSaveView.layout();
		Display display = getParent().getDisplay();
		while (!shlSaveView.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	private void createContents() {
		shlSaveView = new Shell(getParent(), getStyle());
		shlSaveView.setSize(483, 196);
		shlSaveView.setText("Save View");
		shlSaveView.setLayout(new GridLayout(1, false));
		
		Composite composite_2 = new Composite(shlSaveView, SWT.NONE);
		composite_2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		composite_2.setLayout(new GridLayout(2, false));
		
		Label lblNewLabel = new Label(composite_2, SWT.NONE);
		lblNewLabel.setText("View Name");
		
		viewNameText = new Text(composite_2, SWT.BORDER);
		viewNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				viewName = viewNameText.getText(); 
			}
		});
		viewNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Composite composite_1 = new Composite(shlSaveView, SWT.NONE);
		composite_1.setLayout(new GridLayout(2, false));
		composite_1.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
		
		Button btnRadioButton = new Button(composite_1, SWT.RADIO);
		btnRadioButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				isPrivate = false;
			}
		});
		GridData gd_btnRadioButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnRadioButton.widthHint = 120;
		btnRadioButton.setLayoutData(gd_btnRadioButton);
		btnRadioButton.setText("Public");
		
		Button btnRadioButton_1 = new Button(composite_1, SWT.RADIO);
		btnRadioButton_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				isPrivate = true;
			}
		});
		btnRadioButton_1.setSelection(true);
		GridData gd_btnRadioButton_1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnRadioButton_1.widthHint = 120;
		btnRadioButton_1.setLayoutData(gd_btnRadioButton_1);
		btnRadioButton_1.setText("Private");
		
		Composite composite = new Composite(shlSaveView, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, true, true, 1, 1));
		
		Button btnNewButton = new Button(composite, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				result = true;
				shlSaveView.close();
			}
		});
		GridData gd_btnNewButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnNewButton.widthHint = 140;
		btnNewButton.setLayoutData(gd_btnNewButton);
		btnNewButton.setText("Save");
		
		Button btnNewButton_1 = new Button(composite, SWT.NONE);
		btnNewButton_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				result = false;
				shlSaveView.close();
			}
		});
		GridData gd_btnNewButton_1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnNewButton_1.widthHint = 140;
		btnNewButton_1.setLayoutData(gd_btnNewButton_1);
		btnNewButton_1.setText("Cancel");

	}
	
	
	public String getViewName(){
		return viewName;
	}
	
	public boolean getIsPrivate(){
		return isPrivate;
	}
}
