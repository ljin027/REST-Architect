package TM1Diagnostic.UI;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.wink.json4j.JSONException;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.win32.OS;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;

import TM1Diagnostic.SearchResult;
import TM1Diagnostic.REST.TM1Chore;
import TM1Diagnostic.REST.TM1Cube;
import TM1Diagnostic.REST.TM1Dimension;
import TM1Diagnostic.REST.TM1Hierarchy;
import TM1Diagnostic.REST.TM1Object;
import TM1Diagnostic.REST.TM1Process;
import TM1Diagnostic.REST.TM1RestException;
import TM1Diagnostic.REST.TM1Server;
import TM1Diagnostic.REST.TM1Subset;
import TM1Diagnostic.REST.TM1View;

import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Button;

public class UI_SearchResult extends Dialog {

	protected Shell shlSearchResult;
	private Table searchResultTable;
	private ServerExplorerComposite parentComposite;
	private TM1Server tm1server;
	public boolean isOpen;
	
	private int options;
	
	private Button allTypesCheckButton;
	private Button cubesCheckButton;
	private Button viewsCheckButton;
	private Button dimensionsCheckButton;
	private Button hierarchiesCheckButton;
	private Button subsetsCheckButton;
	private Button processesCheckButton;
	private Button choresCheckButton;
	

	private MenuItem goToMenuItem;

	private List<SearchResult> results;
	private Text searchTermText;
	private Button controlObjectCheckButton;
	private Button regexCheckButton;
	private Composite composite_2;
	private Label lblNewLabel_1;

	public UI_SearchResult(Shell parent, ServerExplorerComposite parentComposite, TM1Server tm1server) {
		super(parent, SWT.DIALOG_TRIM);
		this.parentComposite = parentComposite;
		this.tm1server = tm1server;
		options = 0;
		isOpen = false;
	}

	public void open(List<SearchResult> results) throws TM1RestException {
		if (isOpen) {
			this.results = results;
			updateResultsTable();
			return;
		}
		;
		isOpen = true;
		Display display = Display.getDefault();
		createContents();
		this.results = results;
		updateResultsTable();
		shlSearchResult.open();
		shlSearchResult.layout();
		while (!shlSearchResult.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		isOpen = false;
	}
	
	protected void createContents() throws TM1RestException {
		shlSearchResult = new Shell();
		shlSearchResult.setSize(816, 493);
		shlSearchResult.setText("Search Result");
		shlSearchResult.setLayout(new GridLayout(1, false));

		long handle = shlSearchResult.handle;
		Point location = shlSearchResult.getLocation();
		Point dimension = shlSearchResult.getSize();
		OS.SetWindowPos(handle, true ? OS.HWND_TOPMOST : OS.HWND_NOTOPMOST, location.x, location.y, dimension.x, dimension.y, 0);
		
		Composite composite_1 = new Composite(shlSearchResult, SWT.NONE);
		composite_1.setLayout(new GridLayout(2, false));
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
		Label lblNewLabel = new Label(composite_1, SWT.NONE);
		lblNewLabel.setText("Search term");
		
		searchTermText = new Text(composite_1, SWT.BORDER);
		searchTermText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Group grpTypeFilter = new Group(composite_1, SWT.NONE);
		grpTypeFilter.setText("Type Filter");
		grpTypeFilter.setLayout(new GridLayout(8, false));
		grpTypeFilter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		allTypesCheckButton = new Button(grpTypeFilter, SWT.CHECK);
		allTypesCheckButton.setEnabled(false);
		allTypesCheckButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (allTypesCheckButton.getSelection()){
					cubesCheckButton.setSelection(true);
					viewsCheckButton.setSelection(true);
					dimensionsCheckButton.setSelection(true);
					hierarchiesCheckButton.setSelection(true);
					subsetsCheckButton.setSelection(true);
					processesCheckButton.setSelection(true);
					choresCheckButton.setSelection(true);
				} else {
					cubesCheckButton.setSelection(false);
					viewsCheckButton.setSelection(false);
					dimensionsCheckButton.setSelection(false);
					hierarchiesCheckButton.setSelection(false);
					subsetsCheckButton.setSelection(false);
					processesCheckButton.setSelection(false);
					choresCheckButton.setSelection(false);
				}

			}
		});
		allTypesCheckButton.setText("All");
		
		cubesCheckButton = new Button(grpTypeFilter, SWT.CHECK);
		cubesCheckButton.setEnabled(false);
		cubesCheckButton.setText("Cubes");
		
		viewsCheckButton = new Button(grpTypeFilter, SWT.CHECK);
		viewsCheckButton.setEnabled(false);
		viewsCheckButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
			}
		});
		viewsCheckButton.setText("Views");
		
		dimensionsCheckButton = new Button(grpTypeFilter, SWT.CHECK);
		dimensionsCheckButton.setEnabled(false);
		dimensionsCheckButton.setText("Dimensions");
		
		hierarchiesCheckButton = new Button(grpTypeFilter, SWT.CHECK);
		hierarchiesCheckButton.setEnabled(false);
		hierarchiesCheckButton.setText("Hierarchies");
		
		subsetsCheckButton = new Button(grpTypeFilter, SWT.CHECK);
		subsetsCheckButton.setEnabled(false);
		subsetsCheckButton.setText("Subsets");
		
		processesCheckButton = new Button(grpTypeFilter, SWT.CHECK);
		processesCheckButton.setEnabled(false);
		processesCheckButton.setText("Processes");
		
		choresCheckButton = new Button(grpTypeFilter, SWT.CHECK);
		choresCheckButton.setEnabled(false);
		choresCheckButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
			}
		});
		choresCheckButton.setText("Chores");
		
		composite_2 = new Composite(composite_1, SWT.NONE);
		composite_2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		composite_2.setLayout(new GridLayout(4, false));
		
		Button searchButton = new Button(composite_2, SWT.NONE);
		GridData gd_searchButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_searchButton.widthHint = 140;
		searchButton.setLayoutData(gd_searchButton);
		searchButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					runSearch();
				} catch (TM1RestException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
		});
		searchButton.setText("Search");
		//searchButton = button;
		searchButton.setText("Search");
		
		lblNewLabel_1 = new Label(composite_2, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		
		regexCheckButton = new Button(composite_2, SWT.CHECK);
		regexCheckButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		regexCheckButton.setText("Regex");
		
		controlObjectCheckButton = new Button(composite_2, SWT.CHECK);
		controlObjectCheckButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		controlObjectCheckButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
			}
		});
		controlObjectCheckButton.setText("Search Control Objects");

		Composite composite = new Composite(shlSearchResult, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		searchResultTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
		searchResultTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
			}
		});
		searchResultTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		searchResultTable.setHeaderVisible(true);
		searchResultTable.setLinesVisible(true);

		TableColumn tblclmnNewColumn = new TableColumn(searchResultTable, SWT.NONE);
		tblclmnNewColumn.setWidth(100);
		tblclmnNewColumn.setText("Type");

		TableColumn tblclmnNewColumn_1 = new TableColumn(searchResultTable, SWT.NONE);
		tblclmnNewColumn_1.setWidth(100);
		tblclmnNewColumn_1.setText("Name");

		Menu menu = new Menu(searchResultTable);
		menu.addMenuListener(new MenuAdapter() {
			@Override
			public void menuShown(MenuEvent arg0) {
				if (searchResultTable.getSelectionCount() != 1) {
					goToMenuItem.setEnabled(false);
				}
			}
		});
		searchResultTable.setMenu(menu);

		goToMenuItem = new MenuItem(menu, SWT.NONE);
		goToMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					clickGoTo();
				} catch (URISyntaxException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});
		goToMenuItem.setText("Go To");
		
		TableColumn tblclmnDetails = new TableColumn(searchResultTable, SWT.NONE);
		tblclmnDetails.setWidth(100);
		tblclmnDetails.setText("Details");
	}

	private void clickGoTo() throws ClientProtocolException, URISyntaxException, IOException, JSONException {
		TableItem t = searchResultTable.getSelection()[0];
		try {
			parentComposite.goTo((TM1Object)t.getData());
		} catch (TM1RestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updateSearchResults(List<SearchResult> results) {
		this.results = results;
		updateResultsTable();
	}

	public void updateResultsTable() {
		searchResultTable.removeAll();
		if (results == null) return;
		if (results.size() == 0){
			infoMessage("No results found");
		}
		for (int i = 0; i < results.size(); i++) {
			SearchResult result = results.get(i);
			TM1Object o = result.tm1object;
			TableItem t = new TableItem(searchResultTable, SWT.NONE);
			if (o instanceof TM1Cube) {
				t.setText(0, "Cube");
				t.setText(1, o.displayName);
				t.setText(2, result.details);
			} else if (o instanceof TM1View) {
				t.setText(0, "View");
				t.setText(1, o.displayName);
				t.setText(2, result.details);
			} else if (o instanceof TM1Dimension) {
				t.setText(0, "Dimension");
				t.setText(1, o.displayName);
				t.setText(2, result.details);
			} else if (o instanceof TM1Hierarchy) {
				t.setText(0, "Hierarchy");
				t.setText(1, o.displayName);
				t.setText(2, result.details);
			} else if (o instanceof TM1Subset) {
				t.setText(0, "Subset");
				t.setText(1, o.displayName);
				t.setText(2, result.details);
			} else if (o instanceof TM1Process) {
				t.setText(0, "Process");
				t.setText(1, o.displayName);
				t.setText(2, result.details);
			} else if (o instanceof TM1Chore) {
				t.setText(0, "Chore");
				t.setText(1, o.displayName);
				t.setText(2, result.details);
			}
			t.setData(o);
		}
		for (TableColumn tc : searchResultTable.getColumns()) tc.pack();
	}


	private void runSearch() throws TM1RestException {
		try {
			String searchTerm = searchTermText.getText();
			results = tm1server.search(searchTerm, controlObjectCheckButton.getSelection(), regexCheckButton.getSelection());
			updateResultsTable();
		} catch (JSONException | URISyntaxException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public void infoMessage(String message) {
		MessageBox m = new MessageBox(shlSearchResult, SWT.ICON_INFORMATION);
		m.setMessage(message);
		m.open();
	}
	
}

