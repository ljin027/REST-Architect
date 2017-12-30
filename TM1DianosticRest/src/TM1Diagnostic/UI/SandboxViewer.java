package TM1Diagnostic.UI;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;


















import java.net.URISyntaxException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.wink.json4j.JSONException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

import TM1Diagnostic.REST.TM1Blob;
import TM1Diagnostic.REST.TM1RestException;
import TM1Diagnostic.REST.TM1Sandbox;
import TM1Diagnostic.REST.TM1Server;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.custom.StyledText;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;

public class SandboxViewer extends Dialog {

	protected Object result;
	protected Shell shell;
	
	private TM1Server tm1server;
	private Table sandbox_table;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public SandboxViewer(Shell parent, TM1Server tm1server) {
		super(parent, SWT.DIALOG_TRIM | SWT.MAX | SWT.RESIZE);
		this.tm1server = tm1server;
		setText("Sandboxes");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
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
		shell = new Shell(getParent(), getStyle());
		shell.setSize(780, 442);
		shell.setText(getText());
		shell.setLayout(new GridLayout(1, false));
		
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		sandbox_table = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
		sandbox_table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		sandbox_table.setHeaderVisible(true);
		sandbox_table.setLinesVisible(true);
		
		TableColumn tblclmnNewColumn = new TableColumn(sandbox_table, SWT.NONE);
		tblclmnNewColumn.setWidth(441);
		tblclmnNewColumn.setText("Name");
		
		TableColumn tblclmnNewColumn_1 = new TableColumn(sandbox_table, SWT.NONE);
		tblclmnNewColumn_1.setWidth(213);
		tblclmnNewColumn_1.setText("Active");
		
		try {
			tm1server.readSandboxesFromServer();
			display_sandboxes();
		} catch (TM1RestException | URISyntaxException | IOException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void display_sandboxes(){
		sandbox_table.removeAll();
		for (int i=0; i<tm1server.getSandboxCount(); i++){
			TM1Sandbox sandbox = tm1server.getSandbox(i);
			TableItem t = new TableItem(sandbox_table, SWT.NONE);
			t.setText(0, sandbox.name);
			if (sandbox.isactive){
				t.setText(1, "*");
			}
		}
	}
		
}
