package TM1Diagnostic.UI;

import org.apache.wink.json4j.OrderedJSONObject;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import TM1Diagnostic.REST.TM1Dimension;
import TM1Diagnostic.REST.TM1Element;
import TM1Diagnostic.REST.TM1Hierarchy;
import TM1Diagnostic.REST.TM1Server;

import org.eclipse.swt.widgets.TableColumn;

public class AddElement extends Dialog {

	protected Shell shlAddElements;
	private Text dimensionText;
	private Text parentElementText;
	private Text newElementNameText;
	private Table table;

	private TM1Dimension dimension;
	private TM1Hierarchy hierarchy;
	private TM1Element parentElement;
	private TM1Element beforeElement;

	private Button numericRadioButton;
	private Button stringRadioButton;
	private Button consolidatedRadioButton;

	private boolean refreshOnClose;
	private Text elementWeightText;

	public AddElement(Shell parent, TM1Hierarchy hierarchy, TM1Element element, TM1Element beforeElement) {
		super(parent, SWT.DIALOG_TRIM);
		this.hierarchy = hierarchy;
		this.dimension = hierarchy.dimension;
		this.parentElement = element;
		this.beforeElement = beforeElement;
	}

	public boolean open() {
		refreshOnClose = false;
		Display display = Display.getDefault();
		createContents();
		shlAddElements.open();
		shlAddElements.layout();
		while (!shlAddElements.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return refreshOnClose;
	}

	protected void createContents() {
		shlAddElements = new Shell();
		shlAddElements.setSize(684, 432);
		shlAddElements.setText("Add Elements");
		shlAddElements.setLayout(new GridLayout(1, false));
		
		Composite composite = new Composite(shlAddElements, SWT.NONE);
		composite.setLayout(new GridLayout(5, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Label lblNewLabel = new Label(composite, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("Dimension");
		
		dimensionText = new Text(composite, SWT.BORDER);
		dimensionText.setEditable(false);
		dimensionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		dimensionText.setText(hierarchy.dimension.name + ":" + hierarchy.name);
		
		Composite composite_1 = new Composite(composite, SWT.NONE);
		composite_1.setLayout(new GridLayout(1, false));
		composite_1.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false, 1, 5));
		
		Button addButton = new Button(composite_1, SWT.NONE);
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				addNextElement();
			}
		});
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		addButton.setText("Add");
		
		Button replaceButton = new Button(composite_1, SWT.NONE);
		replaceButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		replaceButton.setText("Replace");
		
		Label lblNewLabel_1 = new Label(composite, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_1.setText("Parent Element");
		
		parentElementText = new Text(composite, SWT.BORDER);
		parentElementText.setEditable(false);
		parentElementText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		if (parentElement == null){
			parentElementText.setText("<root>");
		} else {
			parentElementText.setText(parentElement.name);
		}
		
		Label lblNewLabel_2 = new Label(composite, SWT.NONE);
		lblNewLabel_2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_2.setText("Name");
		
		newElementNameText = new Text(composite, SWT.BORDER);
		newElementNameText.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				addNextElement();
			}
		});
		newElementNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		Label lblNewLabel_4 = new Label(composite, SWT.NONE);
		lblNewLabel_4.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_4.setText("Weight");
		
		elementWeightText = new Text(composite, SWT.BORDER);
		elementWeightText.setText("1");
		elementWeightText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		Label lblNewLabel_3 = new Label(composite, SWT.NONE);
		lblNewLabel_3.setText("Element Type");
		
		numericRadioButton = new Button(composite, SWT.RADIO);
		numericRadioButton.setText("Numeric");
		numericRadioButton.setSelection(true);
		
		stringRadioButton = new Button(composite, SWT.RADIO);
		stringRadioButton.setText("String");
		
		consolidatedRadioButton = new Button(composite, SWT.RADIO);
		consolidatedRadioButton.setText("Consolidated");
		
		table = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
		GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true, 5, 1);
		gd_table.widthHint = 448;
		table.setLayoutData(gd_table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		TableColumn column1 = new TableColumn(table, SWT.NONE);
		column1.setWidth(263);
		column1.setText("Name");
		
		TableColumn column2 = new TableColumn(table, SWT.NONE);
		column2.setWidth(143);
		column2.setText("Type");
		
		TableColumn tblclmnNewColumn = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn.setWidth(151);
		tblclmnNewColumn.setText("Weight");
		
		TableColumn column4 = new TableColumn(table, SWT.NONE);
		column4.setWidth(100);
		column4.setText("Parent Element");
		
		Composite okCancelComposite = new Composite(shlAddElements, SWT.NONE);
		okCancelComposite.setLayout(new GridLayout(2, false));
		okCancelComposite.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, false, false, 1, 1));
		
		Button okButton = new Button(okCancelComposite, SWT.NONE);
		okButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				addElementsToHierarchy();
				refreshOnClose = true;
				shlAddElements.close();
			}
		});
		GridData gd_okButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_okButton.widthHint = 140;
		okButton.setLayoutData(gd_okButton);
		okButton.setText("Ok");
		
		Button cancelButton = new Button(okCancelComposite, SWT.NONE);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				shlAddElements.close();
			}
		});
		GridData gd_cancelButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_cancelButton.widthHint = 140;
		cancelButton.setLayoutData(gd_cancelButton);
		cancelButton.setText("Cancel");

	}

	/*
	 * { "Element" : {"Name":"1.2"} , "Before@odata.bind" :
	 * "Dimensions('Stuart')/Hierarchies('Stuart')/Elements('1.3')" }
	 */
	private void addElementsToHierarchy() {
		try {
			if (parentElementText.getText().equals("<root>")) {
				String request = dimension.entity + '/' + hierarchy.entity + "/tm1.SetElement";
				for (int i = 0; i < table.getItemCount(); i++) {
					TableItem t = table.getItem(i);
					OrderedJSONObject payload = new OrderedJSONObject();
					OrderedJSONObject newElementJSON = new OrderedJSONObject();
					newElementJSON.put("Name", t.getText(0));
					newElementJSON.put("Type", t.getText(1));
					TM1Server tm1server = hierarchy.tm1server;
					payload.put("Element", newElementJSON);
					if (beforeElement != null) {
						payload.put("Before@odata.bind", dimension.entity + '/' + hierarchy.entity + '/' + beforeElement.entity);
					}
					//System.out.println("SetElement Payload " + payload);
					tm1server.post(request, payload);
					refreshOnClose = true;
				}
			} else {
				String request = dimension.entity + '/' + hierarchy.entity + '/' + parentElement.entity + "/tm1.SetComponent";
				for (int i = 0; i < table.getItemCount(); i++) {
					TableItem t = table.getItem(i);
					OrderedJSONObject payload = new OrderedJSONObject();
					OrderedJSONObject newElementJSON = new OrderedJSONObject();
					newElementJSON.put("Name", t.getText(0));
					newElementJSON.put("Type", t.getText(1));
					// newElementJSON.put("Weight", t.getText(2));
					TM1Server tm1server = hierarchy.tm1server;
					payload.put("Element", newElementJSON);
					if (beforeElement != null) {
						payload.put("Before@odata.bind", dimension.entity + '/' + hierarchy.entity + '/' + beforeElement.entity);
					}
					//System.out.println("SetComponent Payload " + payload);
					tm1server.post(request, payload);
					refreshOnClose = true;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public void infoMessage(String message) {
		MessageBox m = new MessageBox(shlAddElements, SWT.ICON_INFORMATION);
		m.setMessage(message);
		m.open();
	}

	public void errorMessage(String errMessage) {
		MessageBox m = new MessageBox(shlAddElements, SWT.ICON_ERROR);
		m.setMessage(errMessage);
	}

	public void addNextElement() {
		TableItem newElementItem = new TableItem(table, SWT.NONE);

		String newElementName = newElementNameText.getText();
		newElementItem.setText(0, newElementName);
		if (numericRadioButton.getSelection()) {
			newElementItem.setText(1, "Numeric");
		} else if (stringRadioButton.getSelection()) {
			newElementItem.setText(1, "String");
		} else if (consolidatedRadioButton.getSelection()) {
			newElementItem.setText(1, "Consolidated");
		}
		String elementWeight = elementWeightText.getText();
		newElementItem.setText(2, elementWeight);
		newElementItem.setText(3, parentElementText.getText());
		
		newElementNameText.setText("");
	}
}
