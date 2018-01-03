package TM1Diagnostic.UI;

import org.apache.wink.json4j.JSONException;
import org.eclipse.swt.widgets.Composite;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;

import TM1Diagnostic.CubeViewerPosition;
import TM1Diagnostic.FunctionsMenuContent;
import TM1Diagnostic.FunctionsMenuContent.FunctionMenuOption;
import TM1Diagnostic.ProcessParameter;
import TM1Diagnostic.ProcessVariable;
import TM1Diagnostic.REST.TM1Cube;
import TM1Diagnostic.REST.TM1Dimension;
import TM1Diagnostic.REST.TM1Hierarchy;
import TM1Diagnostic.REST.TM1Process;
import TM1Diagnostic.REST.TM1RestException;
import TM1Diagnostic.REST.TM1Server;
import TM1Diagnostic.REST.TM1Subset;
import TM1Diagnostic.REST.TM1View;

import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Text;

public class ProcessEditor {

	protected Shell shell;

	static private Image SAVE;
	static private Image SAVEAS;
	static private Image EXECUTE;

	static final int VARIABLE_NAME_COLUMN = 1;
	static final int VARIABLE_TYPE_COLUMN = 2;

	static final int PARAMETER_NAME_COLUMN = 1;
	static final int PARAMETER_TYPE_COLUMN = 2;
	static final int PARAMETER_VALUE_COLUMN = 3;
	static final int PARAMETER_PROMPT_COLUMN = 4;

	static final int CHRARACTER = 0;
	static final int FIXEDWIDTH = 1;


	private TM1Process process;
	private TM1Server tm1server;
	private Display display;
	private Menu procedure_menu;

	private boolean newProcess;

	private Composite content_panel;
	private StackLayout content_layout;
	private Composite none_panel;
	private Composite text_panel;
	private Composite odbc_panel;
	private Composite odbo_panel;
	private Composite tm1view_panel;
	private Composite tm1subset_panel;
	private Composite bi_panel;

	private Button textButton;
	private Button odbcButton;
	private Button odboButton;
	private Button tm1ViewButton;
	private Button tm1SubsetButton;
	private Button cognosButton;
	private Button noneButton;

	private StyledText prologStyledText;
	private StyledText metadataStyledText;
	private StyledText dataStyledText;
	private StyledText epilogStyledText;

	private Combo cubeSelectCombo;
	private Combo viewSelectCombo;

	private Combo dimensionSelectCombo;
	private Combo subsetSelectCombo;

	private List<StyleRange> prolog_style_ranges;
	private List<StyleRange> metadata_style_ranges;
	private List<StyleRange> data_style_ranges;
	private List<StyleRange> epilog_style_ranges;

	private FunctionsMenuContent functionmenucontent;

	private MenuItem controlobjects_menuitem;

	private Text dataSourceNameForServer_text;
	private Text dataSourceNameForClientText;
	private Text asciiDecimalSeparatorText;
	private Combo asciiDelimiterType_combo;
	private Text asciiDelimiterChar_text;
	private Text asciiHeaderRecordsText;
	private Text asciiQuoteCharacterText;

	private Text userName_text;
	private Text password_text;
	private Button usesUnicode_button;
	private StyledText query_text;

	private Table parameters_table;
	private Table variablesTable;
	private Text thousandSeperatorText;

	private boolean refreshOnClose;
	private Table flatFilePreviewTable;
	private Table viewPreviewTable;
	private Table odbcPreviewTable;
	private Table subsetPreviewTable;

	private Color blue;

	private boolean pendingSave;

	/**
	 * @wbp.parser.constructor
	 */

	public ProcessEditor(Shell parent, TM1Process process) throws TM1RestException {
		this.process = process;
		this.tm1server = process.tm1server;
		newProcess = false;
		shell = new Shell(parent, SWT.RESIZE | SWT.MAX | SWT.MIN);
		//shell.setSize(689, 448);
		shell.setText("Process Editor - " + process.name);
		display = shell.getDisplay();
		createContents();
		shell.layout();
		shell.open();
	}

	public ProcessEditor(Shell parent, TM1Server server) throws TM1RestException {
		//this.process = process;
		this.tm1server = server;
		shell = new Shell(parent, SWT.RESIZE | SWT.MAX | SWT.MIN);
		//shell.setSize(689, 448);
		newProcess = true;
		shell.setText("Process Editor - *New Process");
		display = shell.getDisplay();
		createContents();
		shell.layout();
		shell.open();
	}


	protected void createContents() throws TM1RestException {
		shell = new Shell();
		shell.setSize(1080, 600);
		shell.setText("SWT Application");

		functionmenucontent = new FunctionsMenuContent(".\\data\\functions.dat");

		pendingSave = false;

		SAVE = new Image(display, ".\\images\\action_save.gif");
		SAVEAS = new Image(display, ".\\images\\action_save_as.gif");
		EXECUTE = new Image(display, ".\\images\\action_run.gif");
		shell.setLayout(new GridLayout(1, false));

		//setLayout(new GridLayout(1, false));

		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new FillLayout(SWT.HORIZONTAL));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		TabFolder tabFolder = new TabFolder(composite, SWT.NONE);

		TabItem tbtmNewItem = new TabItem(tabFolder, SWT.NONE);
		tbtmNewItem.setText("Data Source");

		Composite composite_2 = new Composite(tabFolder, SWT.NONE);
		tbtmNewItem.setControl(composite_2);
		composite_2.setLayout(new GridLayout(2, false));

		Group grpDataSourceType = new Group(composite_2, SWT.NONE);
		grpDataSourceType.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		grpDataSourceType.setText("Data Source Type");
		grpDataSourceType.setLayout(new FillLayout(SWT.VERTICAL));

		SelectionListener datasourcetype_selectionListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				Button button = ((Button) event.widget);
				if (button.getText().equals("None")) {
					content_layout.topControl = none_panel;
					content_panel.layout();
				} else if (button.getText().equals("Text")) {
					content_layout.topControl = text_panel;
					content_panel.layout();
				} else if (button.getText().equals("ODBC")) {
					content_layout.topControl = odbc_panel;
					content_panel.layout();
				} else if (button.getText().equals("ODBO")) {
					content_layout.topControl = odbo_panel;
					content_panel.layout();
				} else if (button.getText().equals("IBM Cognos BI")) {
					content_layout.topControl = bi_panel;
					content_panel.layout();
				} else if (button.getText().equals("TM1 View")) {
					content_layout.topControl = tm1view_panel;
					content_panel.layout();
				} else if (button.getText().equals("TM1 Subset")) {
					content_layout.topControl = tm1subset_panel;
					content_panel.layout();
				}

			};
		};

		noneButton = new Button(grpDataSourceType, SWT.RADIO);
		noneButton.setText("None");
		noneButton.addSelectionListener(datasourcetype_selectionListener);

		textButton = new Button(grpDataSourceType, SWT.RADIO);
		textButton.setText("Text");
		textButton.addSelectionListener(datasourcetype_selectionListener);

		odbcButton = new Button(grpDataSourceType, SWT.RADIO);
		odbcButton.setText("ODBC");
		odbcButton.addSelectionListener(datasourcetype_selectionListener);

		odboButton = new Button(grpDataSourceType, SWT.RADIO);
		odboButton.setText("ODBO");
		odboButton.addSelectionListener(datasourcetype_selectionListener);
		odboButton.setEnabled(false);

		tm1ViewButton = new Button(grpDataSourceType, SWT.RADIO);
		tm1ViewButton.setText("TM1 View");
		tm1ViewButton.addSelectionListener(datasourcetype_selectionListener);

		tm1SubsetButton = new Button(grpDataSourceType, SWT.RADIO);
		tm1SubsetButton.setText("TM1 Subset");
		tm1SubsetButton.addSelectionListener(datasourcetype_selectionListener);

		cognosButton = new Button(grpDataSourceType, SWT.RADIO);
		cognosButton.setText("IBM Cognos BI");
		cognosButton.addSelectionListener(datasourcetype_selectionListener);
		cognosButton.setEnabled(false);

		content_panel = new Composite(composite_2, SWT.NONE);
		content_panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		content_layout = new StackLayout();
		content_panel.setLayout(content_layout);

		none_panel = new Composite(content_panel, SWT.NONE);
		text_panel = new Composite(content_panel, SWT.NONE);
		text_panel.setLayout(new GridLayout(2, false));

		Label lblNewLabel = new Label(text_panel, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("Data Source Name on Server");

		dataSourceNameForServer_text = new Text(text_panel, SWT.BORDER);
		dataSourceNameForServer_text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblDataSourceName = new Label(text_panel, SWT.NONE);
		lblDataSourceName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDataSourceName.setText("Data Source Name on Client");

		dataSourceNameForClientText = new Text(text_panel, SWT.BORDER);
		dataSourceNameForClientText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblNewLabel_1 = new Label(text_panel, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_1.setText("Decimal Seperator");

		asciiDecimalSeparatorText = new Text(text_panel, SWT.BORDER);
		asciiDecimalSeparatorText.setText(".");
		asciiDecimalSeparatorText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblNewLabel_6 = new Label(text_panel, SWT.NONE);
		lblNewLabel_6.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_6.setText("Thousand Seperator");

		thousandSeperatorText = new Text(text_panel, SWT.BORDER);
		thousandSeperatorText.setText(",");
		thousandSeperatorText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblNewLabel_2 = new Label(text_panel, SWT.NONE);
		lblNewLabel_2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_2.setText("Delimiter Type");

		asciiDelimiterType_combo = new Combo(text_panel, SWT.NONE);
		asciiDelimiterType_combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		asciiDelimiterType_combo.add("Character");
		asciiDelimiterType_combo.add("FixedWidth");

		Label lblNewLabel_3 = new Label(text_panel, SWT.NONE);
		lblNewLabel_3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_3.setText("Delimiter Character");

		asciiDelimiterChar_text = new Text(text_panel, SWT.BORDER);
		asciiDelimiterChar_text.setText(",");
		asciiDelimiterChar_text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblNewLabel_4 = new Label(text_panel, SWT.NONE);
		lblNewLabel_4.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_4.setText("Number of Header Rows");

		asciiHeaderRecordsText = new Text(text_panel, SWT.BORDER);
		asciiHeaderRecordsText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				try {
					Integer.parseInt(asciiHeaderRecordsText.getText());
				} catch (NumberFormatException ex) {
					errorMessage("Invalid number of records");
					asciiHeaderRecordsText.setText("0");
				}
			}
		});
		asciiHeaderRecordsText.setText("0");
		asciiHeaderRecordsText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblNewLabel_5 = new Label(text_panel, SWT.NONE);
		lblNewLabel_5.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_5.setText("Quote Character");

		asciiQuoteCharacterText = new Text(text_panel, SWT.BORDER);
		asciiQuoteCharacterText.setText("\"");
		asciiQuoteCharacterText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Composite previewButton = new Composite(text_panel, SWT.NONE);
		previewButton.setLayout(new GridLayout(2, false));
		previewButton.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, true, false, 2, 1));

		Button btnNewButton = new Button(previewButton, SWT.NONE);
		GridData gd_btnNewButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnNewButton.widthHint = 140;
		btnNewButton.setLayoutData(gd_btnNewButton);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				previewFlatFile();
			}
		});
		btnNewButton.setText("Preview");

		Button setVariablesButton = new Button(previewButton, SWT.NONE);
		setVariablesButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setFlatFileVariables();
			}
		});
		GridData gd_setVariablesButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_setVariablesButton.widthHint = 140;
		setVariablesButton.setLayoutData(gd_setVariablesButton);
		setVariablesButton.setText("Set Variables");

		Composite composite_6 = new Composite(text_panel, SWT.NONE);
		composite_6.setLayout(new GridLayout(1, false));
		composite_6.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		flatFilePreviewTable = new Table(composite_6, SWT.BORDER | SWT.FULL_SELECTION);
		flatFilePreviewTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		flatFilePreviewTable.setHeaderVisible(true);
		flatFilePreviewTable.setLinesVisible(true);

		odbc_panel = new Composite(content_panel, SWT.NONE);
		odbc_panel.setLayout(new GridLayout(2, false));

		Label lblDataSourceUser = new Label(odbc_panel, SWT.NONE);
		lblDataSourceUser.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDataSourceUser.setText("Data Source User Name");

		userName_text = new Text(odbc_panel, SWT.BORDER);
		userName_text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblDataSourcePassword = new Label(odbc_panel, SWT.NONE);
		lblDataSourcePassword.setText("Data Source Password");

		password_text = new Text(odbc_panel, SWT.BORDER);
		password_text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		usesUnicode_button = new Button(odbc_panel, SWT.CHECK);
		usesUnicode_button.setText("Use Unicode");
		new Label(odbc_panel, SWT.NONE);

		Group grpSqlQuery = new Group(odbc_panel, SWT.NONE);
		grpSqlQuery.setText("SQL Query");
		grpSqlQuery.setLayout(new FillLayout(SWT.HORIZONTAL));
		grpSqlQuery.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		query_text = new StyledText(grpSqlQuery, SWT.BORDER);

		Composite composite_10 = new Composite(odbc_panel, SWT.NONE);
		composite_10.setLayout(new GridLayout(1, false));
		composite_10.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		Button odbcPreviewButton = new Button(composite_10, SWT.NONE);
		odbcPreviewButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		odbcPreviewButton.setText("Preview");

		Composite composite_11 = new Composite(odbc_panel, SWT.NONE);
		composite_11.setLayout(new GridLayout(1, false));
		composite_11.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		odbcPreviewTable = new Table(composite_11, SWT.BORDER | SWT.FULL_SELECTION);
		odbcPreviewTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		odbcPreviewTable.setHeaderVisible(true);
		odbcPreviewTable.setLinesVisible(true);
		odbo_panel = new Composite(content_panel, SWT.NONE);
		tm1view_panel = new Composite(content_panel, SWT.NONE);
		tm1view_panel.setLayout(new GridLayout(1, false));

		Composite composite_7 = new Composite(tm1view_panel, SWT.NONE);
		composite_7.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		composite_7.setLayout(new GridLayout(2, false));

		Label lblNewLabel_7 = new Label(composite_7, SWT.NONE);
		lblNewLabel_7.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_7.setText("Cube");

		cubeSelectCombo = new Combo(composite_7, SWT.READ_ONLY);
		cubeSelectCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cubeSelectCombo.removeAll();

		cubeSelectCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				TM1Cube cube = new TM1Cube(cubeSelectCombo.getItem(cubeSelectCombo.getSelectionIndex()), tm1server);
				try {
					cube.readCubeViewsFromServer();
				} catch (TM1RestException | URISyntaxException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				viewSelectCombo.removeAll();
				for (int i = 0; i < cube.viewCount(); i++) {
					TM1View view = cube.getview(i);
					viewSelectCombo.add(view.name);
				}
			}
		});

		Label viewSelectLabel = new Label(composite_7, SWT.NONE);
		viewSelectLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		viewSelectLabel.setText("View");

		viewSelectCombo = new Combo(composite_7, SWT.READ_ONLY);
		viewSelectCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		for (int i = 0; i < tm1server.cubeCount(); i++) {
			TM1Cube cube = tm1server.getCube(i);
			cubeSelectCombo.add(cube.name);
		}

		Composite composite_8 = new Composite(tm1view_panel, SWT.NONE);
		composite_8.setLayout(new GridLayout(2, false));
		composite_8.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false, 1, 1));

		Button viewSetVariablesButton = new Button(composite_8, SWT.NONE);
		GridData gd_viewSetVariablesButton = new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1);
		gd_viewSetVariablesButton.widthHint = 140;
		viewSetVariablesButton.setLayoutData(gd_viewSetVariablesButton);
		viewSetVariablesButton.setText("Set Variables");

		Button viewPreviewButton = new Button(composite_8, SWT.NONE);
		viewPreviewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				previewViewSource();
			}
		});
		GridData gd_viewPreviewButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_viewPreviewButton.widthHint = 140;
		viewPreviewButton.setLayoutData(gd_viewPreviewButton);
		viewPreviewButton.setText("Preview");

		Composite composite_9 = new Composite(tm1view_panel, SWT.NONE);
		composite_9.setLayout(new GridLayout(1, false));
		composite_9.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		viewPreviewTable = new Table(composite_9, SWT.BORDER | SWT.FULL_SELECTION);
		viewPreviewTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		viewPreviewTable.setHeaderVisible(true);
		viewPreviewTable.setLinesVisible(true);
		tm1subset_panel = new Composite(content_panel, SWT.NONE);
		tm1subset_panel.setLayout(new GridLayout(1, false));

		Composite composite_12 = new Composite(tm1subset_panel, SWT.NONE);
		composite_12.setLayout(new GridLayout(2, false));
		composite_12.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		Label dimensionSelectLabel = new Label(composite_12, SWT.NONE);
		dimensionSelectLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		dimensionSelectLabel.setText("Dimension");

		dimensionSelectCombo = new Combo(composite_12, SWT.READ_ONLY);
		dimensionSelectCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		dimensionSelectCombo.removeAll();
		try {
			tm1server.readDimensionsFromServer();
		} catch (URISyntaxException | IOException | JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for (int i = 0; i < tm1server.dimensionCount(); i++) {
			TM1Dimension dimension = tm1server.getDimension(i);
			dimensionSelectCombo.add(dimension.name);
		}

		Label subsetSelectLabel = new Label(composite_12, SWT.NONE);
		subsetSelectLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		subsetSelectLabel.setText("Subset");

		subsetSelectCombo = new Combo(composite_12, SWT.READ_ONLY);
		subsetSelectCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		dimensionSelectCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					TM1Dimension dimension = new TM1Dimension(dimensionSelectCombo.getItem(dimensionSelectCombo.getSelectionIndex()), tm1server);
					dimension.readHierarchiesFromServer();
					TM1Hierarchy hierarchy = dimension.getDefaultHierarchy();
					hierarchy.readSubsetsFromServer();
					subsetSelectCombo.removeAll();
					for (int i = 0; i < hierarchy.subsetCount(); i++) {
						TM1Subset subset = hierarchy.getSubset(i);
						subsetSelectCombo.add(subset.name);
					}
				} catch (Exception e) {

				}
			}
		});

		Composite composite_13 = new Composite(tm1subset_panel, SWT.NONE);
		composite_13.setLayout(new GridLayout(1, false));
		composite_13.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		Button subsetPreviewButton = new Button(composite_13, SWT.NONE);
		subsetPreviewButton.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, true, false, 1, 1));
		subsetPreviewButton.setText("Preview");

		Composite composite_14 = new Composite(tm1subset_panel, SWT.NONE);
		composite_14.setLayout(new GridLayout(1, false));
		composite_14.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		subsetPreviewTable = new Table(composite_14, SWT.BORDER | SWT.FULL_SELECTION);
		subsetPreviewTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		subsetPreviewTable.setHeaderVisible(true);
		subsetPreviewTable.setLinesVisible(true);
		bi_panel = new Composite(content_panel, SWT.NONE);

		TabItem tbtmNewItem_1 = new TabItem(tabFolder, SWT.NONE);
		tbtmNewItem_1.setText("Variables");

		Composite composite_4 = new Composite(tabFolder, SWT.NONE);
		tbtmNewItem_1.setControl(composite_4);
		composite_4.setLayout(new GridLayout(2, false));

		variablesTable = new Table(composite_4, SWT.BORDER);
		variablesTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				for (int i = 0; i < variablesTable.getItemCount(); i++) {
					variablesTable.getItem(i).setText("");
				}
				variablesTable.getItem(variablesTable.getSelectionIndex()).setText("*");
			}
		});
		variablesTable.setHeaderVisible(true);
		variablesTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		TableColumn tblclmnNewColumn_4 = new TableColumn(variablesTable, SWT.NONE);
		tblclmnNewColumn_4.setWidth(59);

		TableColumn variables_column_1 = new TableColumn(variablesTable, SWT.NONE);
		variables_column_1.setWidth(158);
		variables_column_1.setText("Name");

		TableColumn variables_column_2 = new TableColumn(variablesTable, SWT.NONE);
		variables_column_2.setWidth(175);
		variables_column_2.setText("Type");

		TableColumn variables_column_3 = new TableColumn(variablesTable, SWT.NONE);
		variables_column_3.setWidth(199);
		variables_column_3.setText("Formula");

		Composite composite_5 = new Composite(composite_4, SWT.NONE);
		composite_5.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		composite_5.setBounds(0, 0, 64, 64);
		composite_5.setLayout(new FillLayout(SWT.VERTICAL));

		Button addvariable_button = new Button(composite_5, SWT.NONE);
		addvariable_button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				TableItem item = new TableItem(variablesTable, SWT.NONE);

				TableEditor editor = new TableEditor(variablesTable);
				editor.horizontalAlignment = SWT.LEFT;
				editor.grabHorizontal = true;
				editor.minimumWidth = 50;
				Text variablenameeditor = new Text(variablesTable, SWT.NONE);
				variablenameeditor.setText("Variable " + variablesTable.getItemCount());
				editor.setEditor(variablenameeditor, item, VARIABLE_NAME_COLUMN);
				item.setText(VARIABLE_NAME_COLUMN, "Variable " + variablesTable.getItemCount());
				item.setData("e1", editor);

				editor = new TableEditor(variablesTable);
				editor.horizontalAlignment = SWT.LEFT;
				editor.grabHorizontal = true;
				editor.minimumWidth = 50;
				Combo variable_combo_editor = new Combo(variablesTable, SWT.NONE);
				variable_combo_editor.add("Numeric");
				variable_combo_editor.add("String");
				variable_combo_editor.select(0);
				editor.setEditor(variable_combo_editor, item, VARIABLE_TYPE_COLUMN);
				item.setData("e2", editor);
			}
		});
		addvariable_button.setText("Add");

		Button removevariable_button = new Button(composite_5, SWT.NONE);
		removevariable_button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (variablesTable.getSelectionCount() > 0) {
					TableItem t = variablesTable.getItem(variablesTable.getSelectionIndex());
					((TableEditor) t.getData("e1")).getEditor().dispose();
					((TableEditor) t.getData("e1")).dispose();
					((TableEditor) t.getData("e2")).getEditor().dispose();
					((TableEditor) t.getData("e2")).dispose();
					variablesTable.remove(variablesTable.getSelectionIndex());
				}
			}
		});
		removevariable_button.setText("Remove");

		new Label(composite_4, SWT.NONE);
		new Label(composite_4, SWT.NONE);

		TabItem tbtmNewItem_6 = new TabItem(tabFolder, SWT.NONE);
		tbtmNewItem_6.setText("Parameters");

		Composite composite_3 = new Composite(tabFolder, SWT.NONE);
		tbtmNewItem_6.setControl(composite_3);
		composite_3.setLayout(new GridLayout(2, false));

		parameters_table = new Table(composite_3, SWT.BORDER);
		parameters_table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		parameters_table.setHeaderVisible(true);
		parameters_table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				for (int i = 0; i < parameters_table.getItemCount(); i++) {
					parameters_table.getItem(i).setText("");
				}
				parameters_table.getItem(parameters_table.getSelectionIndex()).setText("*");
			}
		});

		TableColumn tblclmnNewColumn_5 = new TableColumn(parameters_table, SWT.NONE);
		tblclmnNewColumn_5.setWidth(48);

		TableColumn tblclmnNewColumn = new TableColumn(parameters_table, SWT.NONE);
		tblclmnNewColumn.setWidth(100);
		tblclmnNewColumn.setText("Name");

		TableColumn tblclmnNewColumn_1 = new TableColumn(parameters_table, SWT.NONE);
		tblclmnNewColumn_1.setWidth(100);
		tblclmnNewColumn_1.setText("Type");

		TableColumn tblclmnNewColumn_2 = new TableColumn(parameters_table, SWT.NONE);
		tblclmnNewColumn_2.setWidth(239);
		tblclmnNewColumn_2.setText("Default Value");

		TableColumn tblclmnNewColumn_3 = new TableColumn(parameters_table, SWT.NONE);
		tblclmnNewColumn_3.setWidth(100);
		tblclmnNewColumn_3.setText("Question");

		Composite composite_1 = new Composite(composite_3, SWT.NONE);
		composite_1.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));
		composite_1.setLayout(new GridLayout(1, false));

		Button addparameter_button = new Button(composite_1, SWT.NONE);
		addparameter_button.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
		addparameter_button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				TableItem item = new TableItem(parameters_table, SWT.NONE);

				TableEditor editor = new TableEditor(parameters_table);
				Text nameeditor = new Text(parameters_table, SWT.NONE);
				nameeditor.setText("Parameter " + parameters_table.getItemCount());
				editor.grabHorizontal = true;
				editor.setEditor(nameeditor, item, PARAMETER_NAME_COLUMN);
				item.setData("e1", editor);

				editor = new TableEditor(parameters_table);
				Combo typeeditor = new Combo(parameters_table, SWT.NONE);
				typeeditor.add("Numeric");
				typeeditor.add("String");
				typeeditor.select(0);
				editor.grabHorizontal = true;
				editor.setEditor(typeeditor, item, PARAMETER_TYPE_COLUMN);
				item.setData("e2", editor);

				editor = new TableEditor(parameters_table);
				Text valueeditor = new Text(parameters_table, SWT.NONE);
				editor.grabHorizontal = true;
				editor.setEditor(valueeditor, item, PARAMETER_VALUE_COLUMN);
				item.setData("e3", editor);

				editor = new TableEditor(parameters_table);
				Text prompteditor = new Text(parameters_table, SWT.NONE);
				editor.grabHorizontal = true;
				editor.setEditor(prompteditor, item, PARAMETER_PROMPT_COLUMN);
				item.setData("e4", editor);
			}
		});
		addparameter_button.setText("Add");

		Button removeparameter_button = new Button(composite_1, SWT.NONE);
		removeparameter_button.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
		removeparameter_button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (parameters_table.getSelectionCount() > 0) {
					TableItem t = parameters_table.getItem(parameters_table.getSelectionIndex());
					((TableEditor) t.getData("e1")).getEditor().dispose();
					((TableEditor) t.getData("e1")).dispose();
					((TableEditor) t.getData("e2")).getEditor().dispose();
					((TableEditor) t.getData("e2")).dispose();
					((TableEditor) t.getData("e3")).getEditor().dispose();
					((TableEditor) t.getData("e3")).dispose();
					((TableEditor) t.getData("e4")).getEditor().dispose();
					((TableEditor) t.getData("e4")).dispose();
					parameters_table.remove(parameters_table.getSelectionIndex());
				}
			}
		});
		removeparameter_button.setText("Remove");

		TabItem tbtmNewItem_2 = new TabItem(tabFolder, SWT.NONE);
		tbtmNewItem_2.setText("Prolog");

		prolog_style_ranges = new ArrayList<StyleRange>();
		metadata_style_ranges = new ArrayList<StyleRange>();
		data_style_ranges = new ArrayList<StyleRange>();
		epilog_style_ranges = new ArrayList<StyleRange>();

		prologStyledText = new StyledText(tabFolder, SWT.BORDER | SWT.V_SCROLL);
		prologStyledText.setAlwaysShowScrollBars(false);
		tbtmNewItem_2.setControl(prologStyledText);

		procedure_menu = new Menu(prologStyledText);
		prologStyledText.setMenu(procedure_menu);

		procedure_menu.addMenuListener(new MenuAdapter() {
			public void menuShown(MenuEvent e) {
				MenuItem[] items = procedure_menu.getItems();
				for (int i = 0; i < items.length; i++) {
					items[i].dispose();
				}
				MenuItem cubesmenuitem = new MenuItem(procedure_menu, SWT.CASCADE);
				cubesmenuitem.setText("Cubes");
				Menu cubesMenu = new Menu(procedure_menu);
				cubesmenuitem.setMenu(cubesMenu);

				for (int i = 0; i < tm1server.cubeCount(); i++) {
					String cubename = tm1server.getCube(i).name;
					MenuItem cube_menuitem = new MenuItem(cubesMenu, SWT.NONE);
					cube_menuitem.setText(cubename);
					cube_menuitem.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent event) {
							MenuItem item = (MenuItem) event.getSource();
							prologStyledText.insert(item.getText());
						}
					});

				}

				MenuItem dimensions_menuitem = new MenuItem(procedure_menu, SWT.CASCADE);
				dimensions_menuitem.setText("Dimensions");

				Menu dimension_menu = new Menu(dimensions_menuitem);
				dimensions_menuitem.setMenu(dimension_menu);

				for (int i = 0; i < tm1server.dimensionCount(); i++) {
					String dimensionname = tm1server.getDimension(i).name;
					MenuItem dimension_menuitem = new MenuItem(dimension_menu, SWT.NONE);
					dimension_menuitem.setText(dimensionname);
					dimension_menuitem.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent event) {
							MenuItem item = (MenuItem) event.getSource();
							prologStyledText.insert(item.getText());
						}
					});
				}

				MenuItem if_menuitem = new MenuItem(procedure_menu, SWT.CASCADE);
				if_menuitem.setText("If");
				if_menuitem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						MenuItem item = (MenuItem) event.getSource();
						prologStyledText.insert("If(<%CONDITION%>);\n<%STATEMENT%>;\nEndIf;\n\n");
					}
				});

				MenuItem while_menuitem = new MenuItem(procedure_menu, SWT.CASCADE);
				while_menuitem.setText("While");
				while_menuitem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						MenuItem item = (MenuItem) event.getSource();
						prologStyledText.insert("While(<%CONDITION%>);\n<%STATEMENT%>;\nEnd;\n\n");
					}
				});

				MenuItem functions_menuitem = new MenuItem(procedure_menu, SWT.CASCADE);
				functions_menuitem.setText("Functions");
				Menu functions_menu = new Menu(functions_menuitem);
				functions_menuitem.setMenu(functions_menu);

				for (int i = 0; i < functionmenucontent.getcount(); i++) {
					MenuItem function_group_menuitem = new MenuItem(functions_menu, SWT.CASCADE);
					function_group_menuitem.setText(functionmenucontent.getmenugroup(i).getname());

					Menu function_group_menu = new Menu(function_group_menuitem);
					function_group_menuitem.setMenu(function_group_menu);

					for (int j = 0; j < functionmenucontent.getmenugroup(i).getcount(); j++) {
						MenuItem function_menuitem = new MenuItem(function_group_menu, SWT.NONE);
						FunctionMenuOption function = functionmenucontent.getmenugroup(i).getfunctionmenuoption(j);
						function_menuitem.setText(function.getname());
						function_menuitem.setData(function);
						function_menuitem.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent event) {
								MenuItem item = (MenuItem) event.getSource();
								FunctionMenuOption function = (FunctionMenuOption) item.getData();
								prologStyledText.insert(function.getsyntax());
							}
						});

					}
				}

				MenuItem mntmCreateSubset = new MenuItem(procedure_menu, SWT.CASCADE);
				mntmCreateSubset.setText("Create Subset");

				Menu menu_3 = new Menu(mntmCreateSubset);
				mntmCreateSubset.setMenu(menu_3);

				MenuItem mntmCreateView = new MenuItem(procedure_menu, SWT.CASCADE);
				mntmCreateView.setText("Create View");

				Menu menu_4 = new Menu(mntmCreateView);
				mntmCreateView.setMenu(menu_4);
			}
		});

		prologStyledText.addLineStyleListener(new LineStyleListener() {
			public void lineGetStyle(LineStyleEvent e) {
				e.bulletIndex = prologStyledText.getLineAtOffset(e.lineOffset);
				StyleRange bulletstyle = new StyleRange();
				bulletstyle.metrics = new GlyphMetrics(0, 0, Integer.toString(prologStyledText.getLineCount() + 1).length() * 12);
				e.bullet = new Bullet(ST.BULLET_NUMBER, bulletstyle);
				e.styles = (StyleRange[]) prolog_style_ranges.toArray(new StyleRange[0]);
			}
		});

		prologStyledText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				prolog_style_ranges.clear();
				for (int i = 0; i < prologStyledText.getLineCount(); i++) {
					String line = prologStyledText.getLine(i);
					int line_start_index = prologStyledText.getOffsetAtLine(i);
					if (line.startsWith("#")) {
						prolog_style_ranges.add(new StyleRange(line_start_index, line.length(), display.getSystemColor(SWT.COLOR_BLUE), null));
					}
				}
				prologStyledText.redraw();
			}
		});

		TabItem tbtmNewItem_3 = new TabItem(tabFolder, SWT.NONE);
		tbtmNewItem_3.setText("Metadata");

		metadataStyledText = new StyledText(tabFolder, SWT.BORDER | SWT.V_SCROLL);
		tbtmNewItem_3.setControl(metadataStyledText);
		metadataStyledText.setMenu(procedure_menu);

		metadataStyledText.addLineStyleListener(new LineStyleListener() {
			public void lineGetStyle(LineStyleEvent e) {
				e.bulletIndex = metadataStyledText.getLineAtOffset(e.lineOffset);
				StyleRange bulletstyle = new StyleRange();
				bulletstyle.metrics = new GlyphMetrics(0, 0, Integer.toString(metadataStyledText.getLineCount() + 1).length() * 12);
				e.bullet = new Bullet(ST.BULLET_NUMBER, bulletstyle);
				e.styles = (StyleRange[]) metadata_style_ranges.toArray(new StyleRange[0]);
			}
		});

		metadataStyledText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				metadata_style_ranges.clear();
				for (int i = 0; i < metadataStyledText.getLineCount(); i++) {
					String line = metadataStyledText.getLine(i);
					int line_start_index = metadataStyledText.getOffsetAtLine(i);
					if (line.startsWith("#")) {
						metadata_style_ranges.add(new StyleRange(line_start_index, line.length(), display.getSystemColor(SWT.COLOR_BLUE), null));
					}
				}
				metadataStyledText.redraw();
			}
		});

		TabItem tbtmNewItem_4 = new TabItem(tabFolder, SWT.NONE);
		tbtmNewItem_4.setText("Data");

		dataStyledText = new StyledText(tabFolder, SWT.BORDER | SWT.V_SCROLL);
		tbtmNewItem_4.setControl(dataStyledText);
		dataStyledText.setMenu(procedure_menu);

		dataStyledText.addLineStyleListener(new LineStyleListener() {
			public void lineGetStyle(LineStyleEvent e) {
				e.bulletIndex = dataStyledText.getLineAtOffset(e.lineOffset);
				StyleRange bulletstyle = new StyleRange();
				bulletstyle.metrics = new GlyphMetrics(0, 0, Integer.toString(dataStyledText.getLineCount() + 1).length() * 12);
				e.bullet = new Bullet(ST.BULLET_NUMBER, bulletstyle);
				e.styles = (StyleRange[]) data_style_ranges.toArray(new StyleRange[0]);
			}
		});

		dataStyledText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				data_style_ranges.clear();
				for (int i = 0; i < dataStyledText.getLineCount(); i++) {
					String line = dataStyledText.getLine(i);
					int line_start_index = dataStyledText.getOffsetAtLine(i);
					if (line.startsWith("#")) {
						data_style_ranges.add(new StyleRange(line_start_index, line.length(), display.getSystemColor(SWT.COLOR_BLUE), null));
					}
				}
				dataStyledText.redraw();
			}
		});

		TabItem tbtmNewItem_5 = new TabItem(tabFolder, SWT.NONE);
		tbtmNewItem_5.setText("Epilog");

		epilogStyledText = new StyledText(tabFolder, SWT.BORDER | SWT.V_SCROLL);
		tbtmNewItem_5.setControl(epilogStyledText);
		epilogStyledText.setMenu(procedure_menu);

		Menu menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);

		MenuItem fileMenuItem = new MenuItem(menu, SWT.CASCADE);
		fileMenuItem.setText("File");

		Menu fileMenu = new Menu(fileMenuItem);
		fileMenuItem.setMenu(fileMenu);

		MenuItem save_menuitem = new MenuItem(fileMenu, SWT.NONE);
		save_menuitem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				updateProcessFromUI();
				try {
					process.updateOnServer(process.name);
					message_box("Processed saved", SWT.OK);
				} catch (JSONException | TM1RestException | URISyntaxException | IOException e) {
					// TODO Auto-generated catch block
					errorMessage(tm1server.getErrorMessage());
					e.printStackTrace();
				}
			}
		});
		save_menuitem.setText("Save");

		MenuItem saveas_menuitem = new MenuItem(fileMenu, SWT.NONE);
		saveas_menuitem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				updateProcessFromUI();
				UI_NamePrompt nameprompt = new UI_NamePrompt(shell, "New Process Name", "");
				if (nameprompt.open()) {
					try {
						process.createOnServer(nameprompt.getobjectname());
						refreshOnClose = true;
						message_box("Processed saved", SWT.OK);
					} catch (JSONException | TM1RestException | URISyntaxException | IOException e) {
						message_box("Error saving process", SWT.ERROR);
						e.printStackTrace();
					}
				}
			}
		});
		saveas_menuitem.setText("Save As");

		MenuItem close_menuitem = new MenuItem(fileMenu, SWT.NONE);
		close_menuitem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				refreshOnClose = true;
				shell.close();
			}
		});
		close_menuitem.setText("Close");

		epilogStyledText.addLineStyleListener(new LineStyleListener() {
			public void lineGetStyle(LineStyleEvent e) {
				e.bulletIndex = epilogStyledText.getLineAtOffset(e.lineOffset);
				StyleRange bulletstyle = new StyleRange();
				bulletstyle.metrics = new GlyphMetrics(0, 0, Integer.toString(epilogStyledText.getLineCount() + 1).length() * 12);
				e.bullet = new Bullet(ST.BULLET_NUMBER, bulletstyle);
				e.styles = (StyleRange[]) epilog_style_ranges.toArray(new StyleRange[0]);
			}
		});

		epilogStyledText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				epilog_style_ranges.clear();
				for (int i = 0; i < epilogStyledText.getLineCount(); i++) {
					String line = epilogStyledText.getLine(i);
					int line_start_index = epilogStyledText.getOffsetAtLine(i);
					if (line.startsWith("#")) {
						epilog_style_ranges.add(new StyleRange(line_start_index, line.length(), display.getSystemColor(SWT.COLOR_BLUE), null));
					}
				}
				epilogStyledText.redraw();
			}
		});

		if (process == null) {
			// defaults for new process
			noneButton.setSelection(true);
			process = new TM1Process("", tm1server);
		} else {
			try {
				process.readProcessFromServer();
				updateUIFromServer();
			} catch (URISyntaxException | IOException | JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	private void updateUIFromServer() {
		try {
			// Fill in Data Source
			String type = process.getdatasourcetype();
			if (type.equals("None")) {
				noneButton.setSelection(true);
				content_layout.topControl = none_panel;
				content_panel.layout();
			} else if (type.equals("ASCII")) {
				textButton.setSelection(true);
				asciiDecimalSeparatorText.setText(process.getasciiDecimalSeparator());
				asciiDelimiterChar_text.setText(process.getasciiDelimiterChar());

				if (process.getasciiDelimiterType().equals("Character")) {
					asciiDelimiterType_combo.select(0);
				} else if (process.getasciiDelimiterType().equals("FixedWidth")) {
					asciiDelimiterType_combo.select(1);
				} else {
					asciiDelimiterType_combo.select(0);
				}
				asciiHeaderRecordsText.setText(String.valueOf(process.getasciiHeaderRecords()));
				asciiQuoteCharacterText.setText(process.getasciiQuoteCharacter());
				dataSourceNameForClientText.setText(process.getdataSourceNameForClient());
				dataSourceNameForServer_text.setText(process.getdataSourceNameForServer());
				content_layout.topControl = text_panel;
				content_panel.layout();
			} else if (type.equals("ODBC")) {
				odbcButton.setSelection(true);
				password_text.setText(process.getpassword());
				query_text.setText(process.getquery());
				userName_text.setText(process.getuserName());
				boolean useunicode = process.usesUnicode();
				if (useunicode) {
					usesUnicode_button.setSelection(true);
				} else {
					usesUnicode_button.setSelection(false);
				}
				content_layout.topControl = odbc_panel;
				content_panel.layout();
			} else if (type.equals("ODBO")) {
				odboButton.setSelection(true);
				content_layout.topControl = odbc_panel;
				content_panel.layout();
			} else if (type.equals("TM1CubeView")) {
				tm1ViewButton.setSelection(true);
				content_layout.topControl = tm1view_panel;
				content_panel.layout();

				cubeSelectCombo.setText(process.getdataSourceNameForServer());
				TM1Cube cube = new TM1Cube(process.getdataSourceNameForServer(), tm1server);
				try {
					cube.readCubeViewsFromServer();
				} catch (URISyntaxException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				viewSelectCombo.removeAll();
				for (int i = 0; i < cube.viewCount(); i++) {
					TM1View view = cube.getview(i);
					viewSelectCombo.add(view.name);
				}
				viewSelectCombo.setText(process.getViewSource());

			} else if (type.equals("TM1DimensionSubset")) {
				tm1SubsetButton.setSelection(true);
				content_layout.topControl = tm1subset_panel;
				content_panel.layout();
				TM1Dimension dimension = new TM1Dimension(process.getdataSourceNameForServer(), tm1server);
				dimension.readHierarchiesFromServer();
				TM1Hierarchy hierarchy = dimension.getDefaultHierarchy();
				dimensionSelectCombo.setText(process.getdataSourceNameForServer());
				subsetSelectCombo.removeAll();
				for (int i = 0; i < hierarchy.subsetCount(); i++) {
					TM1Subset subset = hierarchy.getSubset(i);
					subsetSelectCombo.add(subset.name);
				}
				subsetSelectCombo.setText(process.getSubsetSource());

			} else if (type.equals("BI")) {
				cognosButton.setSelection(true);
				content_layout.topControl = bi_panel;
				content_panel.layout();
			} else {
				noneButton.setSelection(true);
				content_layout.topControl = none_panel;
				content_panel.layout();
			}

			// Fill in Parameters Table
			parameters_table.clearAll();
			for (int i = 0; i < process.parametercount(); i++) {
				ProcessParameter parameter = process.getparameter(i);
				TableItem item = new TableItem(parameters_table, SWT.NONE);

				TableEditor editor = new TableEditor(parameters_table);
				Text nameeditor = new Text(parameters_table, SWT.NONE);
				nameeditor.setText(parameter.getname());
				editor.grabHorizontal = true;
				editor.setEditor(nameeditor, item, PARAMETER_NAME_COLUMN);
				item.setData("e1", editor);

				editor = new TableEditor(parameters_table);
				Combo typeeditor = new Combo(parameters_table, SWT.NONE);
				typeeditor.add("Numeric");
				typeeditor.add("String");
				typeeditor.select(parameter.gettype());
				editor.grabHorizontal = true;
				editor.setEditor(typeeditor, item, PARAMETER_TYPE_COLUMN);
				item.setData("e2", editor);

				editor = new TableEditor(parameters_table);
				Text valueeditor = new Text(parameters_table, SWT.NONE);
				valueeditor.setText(parameter.getvalue());
				editor.grabHorizontal = true;
				editor.setEditor(valueeditor, item, PARAMETER_VALUE_COLUMN);
				item.setData("e3", editor);

				editor = new TableEditor(parameters_table);
				Text prompteditor = new Text(parameters_table, SWT.NONE);
				prompteditor.setText(parameter.getprompt());
				editor.grabHorizontal = true;
				editor.setEditor(prompteditor, item, PARAMETER_PROMPT_COLUMN);
				item.setData("e4", editor);
			}

			// Fill in Variables Table
			variablesTable.clearAll();
			for (int i = 0; i < process.variable_count(); i++) {
				ProcessVariable variable = process.getvariable(i);
				TableItem item = new TableItem(variablesTable, SWT.NONE);

				TableEditor editor = new TableEditor(variablesTable);
				editor.horizontalAlignment = SWT.LEFT;
				editor.grabHorizontal = true;
				editor.minimumWidth = 50;
				Text variablenameeditor = new Text(variablesTable, SWT.NONE);
				variablenameeditor.setText(variable.getname());
				editor.setEditor(variablenameeditor, item, VARIABLE_NAME_COLUMN);
				item.setText(VARIABLE_NAME_COLUMN, variable.getname());
				item.setData("e1", editor);

				editor = new TableEditor(variablesTable);
				editor.horizontalAlignment = SWT.LEFT;
				editor.grabHorizontal = true;
				editor.minimumWidth = 50;
				Combo variable_combo_editor = new Combo(variablesTable, SWT.NONE);
				variable_combo_editor.add("Numeric");
				variable_combo_editor.add("String");
				variable_combo_editor.setText(variable.gettype());
				editor.setEditor(variable_combo_editor, item, VARIABLE_TYPE_COLUMN);
				item.setData("e2", editor);
			}

			// Fill in Prolog Procedure
			String full_prolog = process.getprocess_prolog();
			int linestart = 0;
			for (int i = 0; i < full_prolog.length(); i++) {
				char c = full_prolog.charAt(i);
				if (c == '\n') {
					linestart++;
				} else if (c == '\r') {
					String line = full_prolog.substring(linestart, i);
					prologStyledText.append(line);
					linestart = i++;
				}
			}
			prologStyledText.append(full_prolog.substring(linestart, full_prolog.length()));

			// Fill in Metadata Procedure
			String full_metadata = process.getprocess_metadata();
			linestart = 0;
			for (int i = 0; i < full_metadata.length(); i++) {
				char c = full_metadata.charAt(i);
				if (c == '\n') {
					linestart++;
				} else if (c == '\r') {
					String line = full_metadata.substring(linestart, i);
					metadataStyledText.append(line);
					linestart = i++;
				}
			}
			metadataStyledText.append(full_metadata.substring(linestart, full_metadata.length()));

			// Fill in Metadata Procedure
			String full_data = process.getprocess_data();
			linestart = 0;
			for (int i = 0; i < full_data.length(); i++) {
				char c = full_data.charAt(i);
				if (c == '\n') {
					linestart++;
				} else if (c == '\r') {
					String line = full_data.substring(linestart, i);
					dataStyledText.append(line);
					linestart = i++;
				}
			}
			dataStyledText.append(full_data.substring(linestart, full_data.length()));

			// Fill in Epilog Procedure
			String full_epilog = process.getprocess_prolog();
			linestart = 0;
			for (int i = 0; i < full_epilog.length(); i++) {
				char c = full_epilog.charAt(i);
				if (c == '\n') {
					linestart++;
				} else if (c == '\r') {
					String line = full_epilog.substring(linestart, i);
					epilogStyledText.append(line);
					linestart = i++;
				}
			}
			epilogStyledText.append(full_epilog.substring(linestart, full_epilog.length()));

		} catch (Exception ex){

		}
	}

	public void updateProcessFromUI() {
		// Fill in Data Source
		String type = process.getdatasourcetype();
		if (noneButton.getSelection()) {
			process.setdatasourcetype("None");
		} else if (textButton.getSelection()) {
			// System.out.println("Set type to ascii");
			process.setdatasourcetype("ASCII");
			process.setasciiDecimalSeparator(asciiDecimalSeparatorText.getText());
			process.setasciiDelimiterChar(asciiDelimiterChar_text.getText());
			if (asciiDelimiterType_combo.getSelectionIndex() == 0) {
				process.setasciiDelimiterType("Character");
			} else if (asciiDelimiterType_combo.getSelectionIndex() == 1) {
				process.setasciiDelimiterType("FixedWidth");
			} else {
				process.setasciiDelimiterType("Character");
			}
			process.setasciiHeaderRecords(Integer.parseInt(asciiHeaderRecordsText.getText()));
			process.setasciiQuoteCharacter(asciiQuoteCharacterText.getText());
			process.setdataSourceNameForClient(dataSourceNameForClientText.getText());
			process.setdataSourceNameForServer(dataSourceNameForServer_text.getText());
		} else if (odbcButton.getSelection()) {
			System.out.println("Set type to ODBC");
			process.setdatasourcetype("ODBC");
			process.setpassword(password_text.getText());
			process.setquery(query_text.getText());
			process.setuserName(userName_text.getText());
			usesUnicode_button.setSelection(process.usesUnicode());
			content_layout.topControl = odbc_panel;
			content_panel.layout();
		} else if (odboButton.getSelection()) {
			process.setdatasourcetype("ODBC");
		} else if (tm1ViewButton.getSelection()) {
			process.setdatasourcetype("TM1CubeView");
		} else if (tm1ViewButton.getSelection()) {
			process.setdatasourcetype("TM1CubeView");
		} else if (cognosButton.getSelection()) {
			process.setdatasourcetype("BI");
		} else {
			process.setdatasourcetype("None");
		}

		process.clear_parameters();
		for (int i = 0; i < parameters_table.getItemCount(); i++) {
			TableItem item = parameters_table.getItem(i);
			String ppname = ((Text) ((TableEditor) item.getData("e1")).getEditor()).getText();
			System.out.println("Type index: " + ((Combo) ((TableEditor) item.getData("e2")).getEditor()).getSelectionIndex());
			int pptype = ((Combo) ((TableEditor) item.getData("e2")).getEditor()).getSelectionIndex();
			System.out.println("Update " + pptype);
			String ppvalue = ((Text) ((TableEditor) item.getData("e3")).getEditor()).getText();
			String ppprompt = ((Text) ((TableEditor) item.getData("e4")).getEditor()).getText();
			ProcessParameter parameter = new ProcessParameter(ppname, pptype, ppvalue, ppprompt);
			process.addparameter(parameter);
		}

		process.clear_variables();
		for (int i = 0; i < variablesTable.getItemCount(); i++) {
			TableItem item = variablesTable.getItem(i);
			String vvname = ((Text) ((TableEditor) item.getData("e1")).getEditor()).getText();
			String vvtype = ((Combo) ((TableEditor) item.getData("e2")).getEditor()).getText();

			ProcessVariable variable = new ProcessVariable(vvname, vvtype, i + 1);
			process.addvariable(variable);
		}

		process.set_prolog(prologStyledText.getText());
		process.set_metadata(metadataStyledText.getText());
		process.set_data(dataStyledText.getText());
		process.set_epilog(epilogStyledText.getText());
	}

	public void updateVariablesTable() {
		variablesTable.clearAll();
		for (int i = 0; i < process.variable_count(); i++) {
			ProcessVariable variable = process.getvariable(i);
			TableItem item = new TableItem(variablesTable, SWT.NONE);

			TableEditor editor = new TableEditor(variablesTable);
			editor.horizontalAlignment = SWT.LEFT;
			editor.grabHorizontal = true;
			editor.minimumWidth = 50;
			Text variablenameeditor = new Text(variablesTable, SWT.NONE);
			variablenameeditor.setText(variable.getname());
			editor.setEditor(variablenameeditor, item, VARIABLE_NAME_COLUMN);
			item.setText(VARIABLE_NAME_COLUMN, variable.getname());
			item.setData("e1", editor);

			editor = new TableEditor(variablesTable);
			editor.horizontalAlignment = SWT.LEFT;
			editor.grabHorizontal = true;
			editor.minimumWidth = 50;
			Combo variable_combo_editor = new Combo(variablesTable, SWT.NONE);
			variable_combo_editor.add("Numeric");
			variable_combo_editor.add("String");
			variable_combo_editor.setText(variable.gettype());
			editor.setEditor(variable_combo_editor, item, VARIABLE_TYPE_COLUMN);
			item.setData("e2", editor);
		}
	}


	public void updateParametersTable() {
		parameters_table.clearAll();
		for (int i = 0; i < process.parametercount(); i++) {
			ProcessParameter parameter = process.getparameter(i);
			TableItem item = new TableItem(parameters_table, SWT.NONE);

			TableEditor editor = new TableEditor(parameters_table);
			Text nameeditor = new Text(parameters_table, SWT.NONE);
			nameeditor.setText(parameter.getname());
			editor.grabHorizontal = true;
			editor.setEditor(nameeditor, item, PARAMETER_NAME_COLUMN);
			item.setData("e1", editor);

			editor = new TableEditor(parameters_table);
			Combo typeeditor = new Combo(parameters_table, SWT.NONE);
			typeeditor.add("Numeric");
			typeeditor.add("String");
			typeeditor.select(parameter.gettype());
			editor.grabHorizontal = true;
			editor.setEditor(typeeditor, item, PARAMETER_TYPE_COLUMN);
			item.setData("e2", editor);

			editor = new TableEditor(parameters_table);
			Text valueeditor = new Text(parameters_table, SWT.NONE);
			valueeditor.setText(parameter.getvalue());
			editor.grabHorizontal = true;
			editor.setEditor(valueeditor, item, PARAMETER_VALUE_COLUMN);
			item.setData("e3", editor);

			editor = new TableEditor(parameters_table);
			Text prompteditor = new Text(parameters_table, SWT.NONE);
			prompteditor.setText(parameter.getprompt());
			editor.grabHorizontal = true;
			editor.setEditor(prompteditor, item, PARAMETER_PROMPT_COLUMN);
			item.setData("e4", editor);
		}
	}

	private void previewViewSource() {
		try {
			viewPreviewTable.removeAll();
			String cubeName = process.getdataSourceNameForServer();
			String viewName = process.getViewSource();

			TM1View view = new TM1View(viewName, new TM1Cube(cubeName, tm1server), tm1server, TM1View.PUBLIC);
			if (view.readAxesFromServer()) {
				for (int i = 0; i < view.getfilterscount(); i++) {
					TableColumn col = new TableColumn(viewPreviewTable, SWT.NONE);
					CubeViewerPosition cubeViewerPosition = view.getTitleDimensionPosition(i);
					col.setText(cubeViewerPosition.dimensionName());
					System.out.println(cubeViewerPosition.dimensionName());
				}
				for (int i = 0; i < view.getrowscount(); i++) {
					TableColumn col = new TableColumn(viewPreviewTable, SWT.NONE);
					CubeViewerPosition cubeViewerPosition = view.getRowDimensionPosition(i);
					col.setText(cubeViewerPosition.dimensionName());
					System.out.println(cubeViewerPosition.dimensionName());
				}
				for (int i = 0; i < view.columnCount(); i++) {
					TableColumn col = new TableColumn(viewPreviewTable, SWT.NONE);
					CubeViewerPosition cubeViewerPosition = view.getColumnDimensionPosition(i);
					col.setText(cubeViewerPosition.dimensionName());
					System.out.println(cubeViewerPosition.dimensionName());
				}
				for (TableColumn tc : viewPreviewTable.getColumns())
					tc.pack();
			}
		} catch (Exception ex){

		}
	}

	public void previewFlatFile() {
		flatFilePreviewTable.removeAll();
		File file = new File(dataSourceNameForClientText.getText());
		if (!file.exists()) {
			errorMessage("Unable to read file");
			return;
		}

		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String line = "";
			String recordDelimiter = ",";
			int previewCount = 0;
			while ((line = br.readLine()) != null && previewCount < 11) {
				// System.out.println("Line: " + line);

				TableItem tableItem = new TableItem(flatFilePreviewTable, SWT.NONE);

				String[] columns = line.split(recordDelimiter);
				for (int i = 0; i < columns.length; i++) {
					if (i >= flatFilePreviewTable.getColumnCount()) {
						TableColumn newColumn = new TableColumn(flatFilePreviewTable, SWT.NONE);
						newColumn.setText(columns[i]);
					}
					if (previewCount >= Integer.parseInt(asciiHeaderRecordsText.getText())) {
						tableItem.setText(i, columns[i]);
					} else {
						tableItem.dispose();
					}
				}

				previewCount++;
			}
			for (TableColumn tc : flatFilePreviewTable.getColumns())
				tc.pack();
			flatFilePreviewTable.redraw();
			br.close();
			fr.close();
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private void setFlatFileVariables() {

		System.out.println("Variables table row count " + variablesTable.getItemCount());

		for (int i = variablesTable.getItemCount() - 1; i >= 0; i--) {
			TableItem t = variablesTable.getItem(i);
			((TableEditor) t.getData("e1")).getEditor().dispose();
			((TableEditor) t.getData("e1")).dispose();
			((TableEditor) t.getData("e2")).getEditor().dispose();
			((TableEditor) t.getData("e2")).dispose();
			variablesTable.remove(i);
		}
		variablesTable.redraw();

		for (int i = 0; i < flatFilePreviewTable.getColumnCount(); i++) {
			String variableName = flatFilePreviewTable.getColumn(i).getText();
			TableItem newVariableItem = new TableItem(variablesTable, SWT.CHECK);
			// newVariableItem.setText(1, columnHeaderText);
			System.out.println("Variable " + variableName);
			boolean isString = false;
			for (int j = 0; j < flatFilePreviewTable.getItemCount(); j++) {
				TableItem tableItem = flatFilePreviewTable.getItem(j);
				String s = tableItem.getText(i);
				try {
					double x = Double.parseDouble(s);
				} catch (NumberFormatException e) {
					isString = true;
					break;
				}
			}

			String variableType = "";
			if (isString) {
				variableType = "String";
			} else {
				variableType = "Numeric";
			}

			TableEditor editor = new TableEditor(variablesTable);
			editor.horizontalAlignment = SWT.LEFT;
			editor.grabHorizontal = true;
			editor.minimumWidth = 50;
			Text variablenameeditor = new Text(variablesTable, SWT.NONE);
			variablenameeditor.setText(variableName);
			editor.setEditor(variablenameeditor, newVariableItem, VARIABLE_NAME_COLUMN);
			newVariableItem.setText(VARIABLE_NAME_COLUMN, variableName);
			newVariableItem.setData("e1", editor);

			editor = new TableEditor(variablesTable);
			editor.horizontalAlignment = SWT.LEFT;
			editor.grabHorizontal = true;
			editor.minimumWidth = 50;
			Combo variable_combo_editor = new Combo(variablesTable, SWT.NONE);
			variable_combo_editor.add("Numeric");
			variable_combo_editor.add("String");
			variable_combo_editor.setText(variableType);
			editor.setEditor(variable_combo_editor, newVariableItem, VARIABLE_TYPE_COLUMN);
			newVariableItem.setData("e2", editor);
		}
	}

	public boolean save(){
		return true;
	}

	public boolean saveAs(){

		return true;
	}

	public void executeProcess(){
		try {
			process.execute();
		} catch (JSONException | TM1RestException | URISyntaxException | IOException e) {
			e.printStackTrace();
		}
	}

	public boolean pendingSave(){
		return pendingSave;
	}

	public void message_box(String message, int type) {
		MessageBox m = new MessageBox(shell, type);
		m.setMessage(message);
		m.open();
	}

	public void errorMessage(String message) {
		MessageBox m = new MessageBox(shell, SWT.ERROR);
		m.setMessage(message);
		m.open();
	}

}
