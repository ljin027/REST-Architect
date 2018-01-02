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

import org.apache.http.client.ClientProtocolException;
import org.apache.wink.json4j.JSONException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import TM1Diagnostic.REST.TM1Blob;
import TM1Diagnostic.REST.TM1RestException;
import TM1Diagnostic.REST.TM1Server;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.custom.StyledText;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;


public class BlobEditor {

	protected Shell shell;
	protected Display display;

	protected boolean refreshTreeOnClose;
	private ServerExplorerComposite explorer;
	private StyledText blobContentText;
	private TM1Blob blob;
	private TM1Server tm1server;
	private boolean pendingSave;
	private Menu menu;
	private MenuItem mntmNewSubmenu;
	private Menu menu_1;
	private MenuItem mntmSave;
	private MenuItem mntmSaveAs;
	private MenuItem mntmExit;
	

	/**
	 * @wbp.parser.constructor
	 */

	public BlobEditor(Shell parent, TM1Blob blob) throws TM1RestException, ClientProtocolException, URISyntaxException, IOException {
		this.blob = blob;
		tm1server = blob.getServer();
		shell = new Shell(parent, SWT.RESIZE | SWT.MAX | SWT.MIN);
		//shell.setSize(689, 448);
		shell.setText("Blob Editor - " + blob.displayName);
		shell.setLayout(new GridLayout(1, false));
		display = shell.getDisplay();
		createContents();
		shell.layout();
		shell.open();
	}

	public BlobEditor(Shell parent, TM1Server tm1server) throws TM1RestException, ClientProtocolException, URISyntaxException, IOException {
		this.tm1server = tm1server;
		//blob = new TM1Blob(tm1server);
		shell = new Shell(parent, SWT.RESIZE | SWT.MAX | SWT.MIN);
		//shell.setSize(689, 448);
		shell.setText("Blob Editor - *New Blob");
		shell.setLayout(new GridLayout(1, false));
		display = shell.getDisplay();
		createContents();
		shell.layout();
		shell.open();
	}

	
	private void createContents() throws TM1RestException, ClientProtocolException, URISyntaxException, IOException {
		shell.setSize(1080, 600);
		
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		blobContentText = new StyledText(composite, SWT.BORDER);
		blobContentText.setAlwaysShowScrollBars(false);
		blobContentText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);
		
		mntmNewSubmenu = new MenuItem(menu, SWT.CASCADE);
		mntmNewSubmenu.setText("File");
		
		menu_1 = new Menu(mntmNewSubmenu);
		mntmNewSubmenu.setMenu(menu_1);
		
		mntmSave = new MenuItem(menu_1, SWT.NONE);
		mntmSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					save();
				} catch (TM1RestException | URISyntaxException | IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		mntmSave.setText("Save");
		
		mntmSaveAs = new MenuItem(menu_1, SWT.NONE);
		mntmSaveAs.setText("Save As");
		
		mntmExit = new MenuItem(menu_1, SWT.NONE);
		mntmExit.setText("Exit");

		if (blob == null){
			blob = new TM1Blob("", tm1server);
		} else {
			blob.readBlobContentFromServer();
			display_blob_content();
		}
	}

	private void display_blob_content() {
		// System.out.println("Blob content: " + blob.getContent());
		if (blob.isUTF8(blob.getBytes())) {
			if (blob.getContent().contains("<?xml")) {
				blobContentText.setText(toPrettyString(blob.getContent(), 5));
			} else {
				blobContentText.setText(blob.getContent());
			}
		} else {
			blobContentText.setText("XML/Text content not detected");
		}
	}

	public static String toPrettyString(String xml, int indent) {
		try {
			// Turn xml string into a document
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new ByteArrayInputStream(xml.getBytes("utf-8"))));
			document.normalize();
			XPath xPath = XPathFactory.newInstance().newXPath();
			NodeList nodeList = (NodeList) xPath.evaluate("//text()[normalize-space()='']", document, XPathConstants.NODESET);

			for (int i = 0; i < nodeList.getLength(); ++i) {
				Node node = nodeList.item(i);
				node.getParentNode().removeChild(node);
			}
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			transformerFactory.setAttribute("indent-number", indent);
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			// Return pretty print xml string
			StringWriter stringWriter = new StringWriter();
			transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
			return stringWriter.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void updateBlobFromUI(){
		blob.setContent(blobContentText.getText());
	}
	
	public void save() throws ClientProtocolException, TM1RestException, URISyntaxException, IOException{
		updateBlobFromUI();
		blob.writeToServer();
	}
	
	public void saveAs() throws ClientProtocolException, JSONException, TM1RestException, URISyntaxException, IOException{
		updateBlobFromUI();
		UI_NamePrompt namePrompt = new UI_NamePrompt(shell, "Blob Name", "");
		if (namePrompt.open()){
			blob.writeToServerAs(namePrompt.getobjectname());
			refreshTreeOnClose = true;
		}
	}
	
	public boolean pendingSave(){
		return pendingSave;
	}


}
