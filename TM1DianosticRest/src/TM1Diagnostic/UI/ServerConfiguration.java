package TM1Diagnostic.UI;

import java.util.Iterator;

import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.OrderedJSONObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import TM1Diagnostic.REST.TM1Server;

import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Button;

public class ServerConfiguration {

	protected Display display;
	protected Shell shell;
	
	private static Color HEADER;

	private TM1Server tm1server;

	// Administration
	
	private Text serverNameText;
	private Text adminHostText;
	private Text databaseDirectoryText;
	private Combo disableSandboxingCombo;
	private Combo runningInBackgroundCombo;
	private Combo performanceMonitorOnCombo;
	private Combo perfMonActiveCombo;
	
	// Network

	private Combo ipVersionCombo;
	private Text ipAddressV4Text;
	private Text ipAddressV6Text;

	
	// CAPI

	private Text portNumberText;
	private Text clientMessagePortNumberText;
	private Text messageCompressionText;
	private Combo progressMessageCombo;
	private Text clientVersionMaximumText;
	private Text clientVersionMinimumText;
	private Text clientVersionPrecisionText;
	private Text idleConnectionTimeOutSecondsText;

	
	// HTTP/REST API

	private Text httpPortNumberText;
	private Text HTTPSessionTimeoutMinutesText;

	
	// CAM

	private Combo camUseSSLCombo;
	private Text clientCAMURIText;
	private Text serverCAMURIText;
	private Text camPortalVariableFileText;
	private Text clientPingCAMPassportText;
	private Text serverCAMURIRetryAttemptsText;
	private Combo createNewCAMClientsCombo;

	
	// LDAP

	private Combo ldapEnabledCombo;
	private Text ldapHostText;
	private Text ldapPortText;
	private Text ldapSearchBaseText;
	private Combo useServerAccountCombo;
	private Text verifyCertServerNameText;
	private Combo verifyServerSSLCertCombo;
	private Combo skipSSLCertVerificationCombo;
	private Combo skipSSLCRLVerificationCombo;
	private Text wellKnownUserNameText;
	private Text passwordFileText;
	private Text passwordKeyFileText;
	private Text searchBaseText;
	private Text SearchFieldText;
	
	// Server Logging
	
	private Combo serverLogEnabled;
	private Text logReleaseLineCountText;
	private Text loggingDirectoryText;
	
	// Authentication

	private Combo securityModeCombo;
	private Combo securityPackageNameCombo;
	private Text servicePrincipalNameText;
	
	// SSL

	private Combo useSSLCombo;
	private Combo supportPreTLSv12ClientsCombo;
	private Text certificateIDText;
	private Text certAuthorityText;
	private Text certRevocationFileText;
	private Text clientExportServerKeyIDText;
	private Text keyFileLabelText;
	private Text keyStashFileText;
	private Text keyLabelText;
	private Text tlsCipherListText;
	private Text fipsOperationModeText;
	
	
	// Audit
	
	private Combo auditEnabledCombo;
	private Text auditLogUpdateIntervalText;
	private Text auditLogMaxFileSizeText;
	private Text auditLogMaxQueryMemoryText;
	
	// Modelling
	
	private Combo defaultMeasuresDimensionCombo;
	private Combo userDefinedCalculationsCombo;
	private Combo enableNewHierarchyCreationCombo;
	
	// Spreading
	
	private Text spreadingPrecisionText;
	private Combo proportionSpreadToZeroCellsCombo;
	
	private Text startupChoresText;
	
	// Startup
	
	private Combo persistentFeedersCombo;
	private Text MaximumCubeLoadThreadsText;
	private Combo skipLoadingAliasesCombo;
	
	// Rules
	private Combo allowSeparateNandCRulesCombo;
	private Combo automaticallyAddCubeDependenciesCombo;
	private Combo rulesOverwriteCellsOnLoadCombo;
	private Combo forceReevaluationOfFeedersForFedCellsOnDataChangeCombo;
	
	// TI
	
	private Text cognosTM1InterfacePathText;
	private Combo useExcelSerialDateCombo;
	private Text maximumTILockObjectsText;
	private Combo enableTIDebuggingCombo;

	// MTQ
	
	private Text mtqUseAllThreadsText;
	private Text mtqNumberOfThreadsToUseText;
	private Combo mtqSingleCellConsolidationCombo;
	private Combo mtqImmediateCheckForSplitCombo;
	private Text mtqOperationProgressCheckSkipLoopSizeText;
	
	// Memory
	
	private Text maximumViewSizeMBText;
	private Combo applyMaximumViewSizeToEntireTransactionCombo;
	private Text maximumUserSandboxSizeMBText;
	private Text MaximumMemoryForSubsetUndoKBText;
	private Combo lockPagesInMemoryLabelCombo;
	
	
	private Text distributedOutputDirText;
	private Text languageText;
	private Text maximumLoginAttemptsText;
	
	private String temp;

	public ServerConfiguration(Shell parent, TM1Server tm1server) {
		shell = new Shell(parent);
		display = shell.getDisplay();
		this.tm1server = tm1server;
		
		this.tm1server = tm1server;
		shell = new Shell(parent, SWT.RESIZE | SWT.MAX | SWT.MIN);
		shell.setSize(757, 511);
		shell.setText("Server Configuration");
		shell.setLayout(new GridLayout(1, false));
		createContent();

		shell.layout();
		shell.open();

	}
		
	private void createContent(){

		HEADER = display.getSystemColor(SWT.COLOR_TITLE_BACKGROUND);

		ScrolledComposite ScrolledComposite = new ScrolledComposite(shell, SWT.V_SCROLL);
		ScrolledComposite.setLayout(new GridLayout(1, false));
		ScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite composite = new Composite(ScrolledComposite, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Label adminLabel = new Label(composite, SWT.NONE);
		adminLabel.setAlignment(SWT.CENTER);
		adminLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
		adminLabel.setText("Administration");
		adminLabel.setBackground(HEADER);
		
		Label ServerNameLabel = new Label(composite, SWT.NONE);
		ServerNameLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		ServerNameLabel.setText("ServerName");
		
		serverNameText = new Text(composite, SWT.BORDER);
		//serverNameText.setEnabled(false);
		serverNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label adminHostLabel = new Label(composite, SWT.NONE);
		adminHostLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		adminHostLabel.setText("AdminHost");
		
		adminHostText = new Text(composite, SWT.BORDER);
		//adminHostText.setEnabled(false);
		adminHostText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label databaseDirectoryLabel = new Label(composite, SWT.NONE);
		databaseDirectoryLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		databaseDirectoryLabel.setText("DataBaseDirectory");
		
		databaseDirectoryText = new Text(composite, SWT.BORDER);
		databaseDirectoryText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		//databaseDirectoryText.setEnabled(false);
		
		Label lblNewLabel_16 = new Label(composite, SWT.NONE);
		lblNewLabel_16.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_16.setText("Language");
		
		languageText = new Text(composite, SWT.BORDER);
		languageText.setEnabled(false);
		languageText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblDisablesandbox = new Label(composite, SWT.NONE);
		lblDisablesandbox.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDisablesandbox.setText("DisableSandbox");
		
		disableSandboxingCombo = new Combo(composite, SWT.NONE);
		disableSandboxingCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		disableSandboxingCombo.add("true");
		disableSandboxingCombo.add("false");
		//disableSandboxingCombo.setEnabled(false);

		Label lblStartupchores = new Label(composite, SWT.NONE);
		lblStartupchores.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblStartupchores.setText("StartupChores");
		
		startupChoresText = new Text(composite, SWT.BORDER);
		startupChoresText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label runningInBackgroundLabel = new Label(composite, SWT.NONE);
		runningInBackgroundLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		runningInBackgroundLabel.setText("RunningInBackground");
		
		runningInBackgroundCombo = new Combo(composite, SWT.BORDER);
		runningInBackgroundCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label PerformanceMonitorOnLabel = new Label(composite, SWT.NONE);
		PerformanceMonitorOnLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		PerformanceMonitorOnLabel.setText("PerformanceMonitorOn");
		
		performanceMonitorOnCombo = new Combo(composite, SWT.NONE);
		performanceMonitorOnCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		performanceMonitorOnCombo.add("true");
		performanceMonitorOnCombo.add("false");
		
		Label PerfMonActiveLabel = new Label(composite, SWT.NONE);
		PerfMonActiveLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		PerfMonActiveLabel.setText("PerfMonActive");
		
		perfMonActiveCombo = new Combo(composite, SWT.NONE);
		perfMonActiveCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		perfMonActiveCombo.add("true");
		perfMonActiveCombo.add("false");

		Label DistributedOutputDir = new Label(composite, SWT.NONE);
		DistributedOutputDir.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		DistributedOutputDir.setText("DistributedOutputDir");
		
		distributedOutputDirText = new Text(composite, SWT.BORDER);
		distributedOutputDirText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		
		// CAPI

		Label CAPILabel = new Label(composite, SWT.NONE);
		CAPILabel.setAlignment(SWT.CENTER);
		CAPILabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
		CAPILabel.setText("CAPI");
		CAPILabel.setBackground(HEADER);
		
		Label lblPortnumber = new Label(composite, SWT.NONE);
		lblPortnumber.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPortnumber.setText("PortNumber");
		
		portNumberText = new Text(composite, SWT.BORDER);
		//portNumberText.setEnabled(false);
		portNumberText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblNewLabel_15 = new Label(composite, SWT.NONE);
		lblNewLabel_15.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_15.setText("ClientMessagePortNumber");
		
		clientMessagePortNumberText = new Text(composite, SWT.BORDER);
		//clientMessagePortNumberText.setEnabled(false);
		clientMessagePortNumberText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label MessageCompressionLabel = new Label(composite, SWT.NONE);
		MessageCompressionLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		MessageCompressionLabel.setText("MessageCompression");
		
		messageCompressionText = new Text(composite, SWT.BORDER);
		//messageCompressionText.setEnabled(false);
		messageCompressionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label ProgressMessageLabel = new Label(composite, SWT.NONE);
		ProgressMessageLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		ProgressMessageLabel.setText("ProgressMessage");
		
		progressMessageCombo = new Combo(composite, SWT.BORDER);
		//progressMessageCombo.setEnabled(false);
		progressMessageCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		progressMessageCombo.add("true");
		progressMessageCombo.add("false");
		

		Label ClientVersionMaximumLabel = new Label(composite, SWT.NONE);
		ClientVersionMaximumLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		ClientVersionMaximumLabel.setText("ClientVersionMaximum");
		
		clientVersionMaximumText = new Text(composite, SWT.BORDER);
		//clientVersionMaximumText.setEnabled(false);
		clientVersionMaximumText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label ClientVersionMinimumLabel = new Label(composite, SWT.NONE);
		ClientVersionMinimumLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		ClientVersionMinimumLabel.setText("ClientVersionMinimum");
		
		clientVersionMinimumText = new Text(composite, SWT.BORDER);
		//clientVersionMinimumText.setEnabled(false);
		clientVersionMinimumText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label ClientVersionPrecisionLabel = new Label(composite, SWT.NONE);
		ClientVersionPrecisionLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		ClientVersionPrecisionLabel.setText("ClientVersionPrecision");
		
		clientVersionPrecisionText = new Text(composite, SWT.BORDER);
		//clientVersionPrecisionText.setEnabled(false);
		clientVersionPrecisionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	
		Label lblNewLabel_17 = new Label(composite, SWT.NONE);
		lblNewLabel_17.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_17.setText("IdleConnectionTimeOutSeconds");
		
		idleConnectionTimeOutSecondsText = new Text(composite, SWT.BORDER);
		idleConnectionTimeOutSecondsText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label restAPILabel = new Label(composite, SWT.NONE);
		restAPILabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
		restAPILabel.setText("REST API");
		restAPILabel.setBackground(HEADER);
		
		Label lblHttpportnumber = new Label(composite, SWT.NONE);
		lblHttpportnumber.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		lblHttpportnumber.setText("HTTPPortNumber");
		
		httpPortNumberText = new Text(composite, SWT.BORDER);
		//httpPortNumberText.setEnabled(false);
		httpPortNumberText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label SessionTimeout = new Label(composite, SWT.NONE);
		SessionTimeout.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		SessionTimeout.setText("HTTPSessionTimeoutMinutes");
		
		HTTPSessionTimeoutMinutesText = new Text(composite, SWT.BORDER);
		HTTPSessionTimeoutMinutesText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		// Authentication

		Label lblAuthentication = new Label(composite, SWT.NONE);
		lblAuthentication.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
		lblAuthentication.setText("Authentication");
		lblAuthentication.setBackground(HEADER);
		
		Label lblNewLabel_1 = new Label(composite, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_1.setText("IntergratedSecurityMode");
	
		securityModeCombo = new Combo(composite, SWT.NONE);
		securityModeCombo.add("Basic");
		securityModeCombo.add("CAM");

		Label lblSecuritypackagename = new Label(composite, SWT.NONE);
		lblSecuritypackagename.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblSecuritypackagename.setText("SecurityPackageName");
		
		securityPackageNameCombo = new Combo(composite, SWT.NONE);
		//securityPackageNameCombo.setEnabled(false);
		securityPackageNameCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		securityPackageNameCombo.add("Kerberos");
		securityPackageNameCombo.add("NTLM");
		securityPackageNameCombo.add("Negotiate");
		
		
		Label lblServiceprincipalname = new Label(composite, SWT.NONE);
		lblServiceprincipalname.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblServiceprincipalname.setText("ServicePrincipalName");
		
		servicePrincipalNameText = new Text(composite, SWT.BORDER);
		//servicePrincipalNameText.setEnabled(false);
		servicePrincipalNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblMaximumloginattempts = new Label(composite, SWT.NONE);
		lblMaximumloginattempts.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblMaximumloginattempts.setText("MaximumLoginAttempts");
		
		maximumLoginAttemptsText = new Text(composite, SWT.BORDER);
		maximumLoginAttemptsText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		// SSL
		
		Label SSLLabel = new Label(composite, SWT.NONE);
		SSLLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
		SSLLabel.setText("SSL");
		SSLLabel.setBackground(HEADER);
	
		
		Label lblUsessl = new Label(composite, SWT.NONE);
		lblUsessl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblUsessl.setText("UseSSL");
		
		useSSLCombo = new Combo(composite, SWT.NONE);
		useSSLCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		//useSSLCombo.setEnabled(false);
		useSSLCombo.add("false");
		useSSLCombo.add("true");
		
		Label lblSupportpretlsvclients = new Label(composite, SWT.NONE);
		lblSupportpretlsvclients.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblSupportpretlsvclients.setText("SupportPreTLSv12Clients");
		
		supportPreTLSv12ClientsCombo = new Combo(composite, SWT.NONE);
		supportPreTLSv12ClientsCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		//supportPreTLSv12ClientsCombo.setEnabled(false);
	
		Label CertificateIDLabel = new Label(composite, SWT.NONE);
		CertificateIDLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		CertificateIDLabel.setText("CertificateID");
		
		certificateIDText = new Text(composite, SWT.NONE);
		certificateIDText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		//certificateIDText.setEnabled(false);
		
		Label certAuthorityLabel = new Label(composite, SWT.NONE);
		certAuthorityLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		certAuthorityLabel.setText("CertAuthority");
		
		certAuthorityText = new Text(composite, SWT.NONE);
		certAuthorityText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		//certAuthorityText.setEnabled(false);
		
		Label certRevocationFileLabel = new Label(composite, SWT.NONE);
		certRevocationFileLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		certRevocationFileLabel.setText("CertRevocationFile");
		
		certRevocationFileText = new Text(composite, SWT.NONE);
		certRevocationFileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		//certRevocationFileText.setEnabled(false);
		
		Label ClientExportServerKeyIDLabel = new Label(composite, SWT.NONE);
		ClientExportServerKeyIDLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		ClientExportServerKeyIDLabel.setText("ClientExportServerKeyID");
		
		clientExportServerKeyIDText = new Text(composite, SWT.NONE);
		clientExportServerKeyIDText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		//clientExportServerKeyIDText.setEnabled(false);

		Label keyFileLabel = new Label(composite, SWT.NONE);
		keyFileLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		keyFileLabel.setText("KeyFile");
		
		keyFileLabelText = new Text(composite, SWT.NONE);
		keyFileLabelText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		//keyFileLabelText.setEnabled(false);
		
		Label keyStashFileLabel = new Label(composite, SWT.NONE);
		keyStashFileLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		keyStashFileLabel.setText("KeyStashFile");
		
		keyStashFileText = new Text(composite, SWT.NONE);
		keyStashFileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		//keyStashFileText.setEnabled(false);

		Label keyLabelLabel = new Label(composite, SWT.NONE);
		keyLabelLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		keyLabelLabel.setText("keyLabel");
		
		keyLabelText = new Text(composite, SWT.NONE);
		keyLabelText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		//keyLabelText.setEnabled(false);
		
		Label tlsCipherListLabel = new Label(composite, SWT.NONE);
		tlsCipherListLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		tlsCipherListLabel.setText("TLSCipherList");
		
		tlsCipherListText = new Text(composite, SWT.NONE);
		tlsCipherListText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		//tlsCipherListText.setEnabled(false);
	
		Label fipsOperationModeLabel = new Label(composite, SWT.NONE);
		fipsOperationModeLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		fipsOperationModeLabel.setText("FIPSOperationMode");
		
		fipsOperationModeText = new Text(composite, SWT.NONE);
		fipsOperationModeText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		//fipsOperationModeText.setEnabled(false);
		

		
		// Logging
		
		Label loggingHeader = new Label(composite, SWT.NONE);
		loggingHeader.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
		loggingHeader.setText("Logging");
		loggingHeader.setBackground(HEADER);
		
		Label loggingDirectoryLabel = new Label(composite, SWT.NONE);
		loggingDirectoryLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		loggingDirectoryLabel.setText("LoggingDirectory");
		
		loggingDirectoryText = new Text(composite, SWT.BORDER);
		loggingDirectoryText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		//loggingDirectoryText.setEnabled(false);

		Label enableLoggingLabel = new Label(composite, SWT.NONE);
		enableLoggingLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		enableLoggingLabel.setText("ServerLogging");
		
		serverLogEnabled = new Combo(composite, SWT.BORDER);
		serverLogEnabled.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		serverLogEnabled.add("false");
		serverLogEnabled.add("true");
		//serverLogEnabled.setEnabled(false);
		
		Label LogReleaseLineCountLabel = new Label(composite, SWT.NONE);
		LogReleaseLineCountLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		LogReleaseLineCountLabel.setText("LogReleaseLineCount");
		
		logReleaseLineCountText = new Text(composite, SWT.BORDER);
		logReleaseLineCountText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		//logReleaseLineCountText.setEnabled(false);
		
		// Audit
		
		Label auditHeader = new Label(composite, SWT.NONE);
		auditHeader.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
		auditHeader.setText("Audit");
		auditHeader.setBackground(HEADER);
		
		Label auditEnabledLabel = new Label(composite, SWT.NONE);
		auditEnabledLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		auditEnabledLabel.setText("AuditLogOn");
		
		auditEnabledCombo = new Combo(composite, SWT.NONE);
		auditEnabledCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		auditEnabledCombo.add("true");
		auditEnabledCombo.add("false");
		//auditEnabledCombo.setEnabled(false);
	
		Label AuditLogUpdateIntervalLabel = new Label(composite, SWT.NONE);
		AuditLogUpdateIntervalLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		AuditLogUpdateIntervalLabel.setText("AuditLogUpdateInterval");
	
		auditLogUpdateIntervalText = new Text(composite, SWT.NONE);
		auditLogUpdateIntervalText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		//auditLogUpdateIntervalText.setEnabled(false);
		
		Label AuditLogMaxFileSizeLabel = new Label(composite, SWT.NONE);
		AuditLogMaxFileSizeLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		AuditLogMaxFileSizeLabel.setText("AuditLogMaxFileSize");
		
		auditLogMaxFileSizeText = new Text(composite, SWT.NONE);
		auditLogMaxFileSizeText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		//auditLogMaxFileSizeText.setEnabled(false);
		
		Label AuditLogMaxQueryMemoryLabel = new Label(composite, SWT.NONE);
		AuditLogMaxQueryMemoryLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		AuditLogMaxQueryMemoryLabel.setText("AuditLogMaxQueryMemory");
		
		auditLogMaxQueryMemoryText = new Text(composite, SWT.NONE);
		auditLogMaxQueryMemoryText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		//auditLogMaxQueryMemoryText.setEnabled(false);
		
		// Rules
		
		Label lblNewLabel_19 = new Label(composite, SWT.NONE);
		lblNewLabel_19.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
		lblNewLabel_19.setText("Rules");
		lblNewLabel_19.setBackground(HEADER);
		
		Label AllowSeparateNandCRulesLabel = new Label(composite, SWT.NONE);
		AllowSeparateNandCRulesLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		AllowSeparateNandCRulesLabel.setText("AllowSeparateNandCRules");
		
		allowSeparateNandCRulesCombo = new Combo(composite, SWT.NONE);
		allowSeparateNandCRulesCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		allowSeparateNandCRulesCombo.add("true");
		allowSeparateNandCRulesCombo.add("false");
		//allowSeparateNandCRulesCombo.setEnabled(false);
		
		Label AutomaticallyAddCubeDependenciesLabel = new Label(composite, SWT.NONE);
		AutomaticallyAddCubeDependenciesLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		AutomaticallyAddCubeDependenciesLabel.setText("AutomaticallyAddCubeDependencies");
		
		automaticallyAddCubeDependenciesCombo = new Combo(composite, SWT.NONE);
		automaticallyAddCubeDependenciesCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		automaticallyAddCubeDependenciesCombo.add("true");
		automaticallyAddCubeDependenciesCombo.add("false");
		//automaticallyAddCubeDependenciesCombo.setEnabled(false);
		
		Label lblNewLabel_14 = new Label(composite, SWT.NONE);
		lblNewLabel_14.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_14.setText("ForceReevaluationOfFeeders");
		
		rulesOverwriteCellsOnLoadCombo = new Combo(composite, SWT.NONE);
		rulesOverwriteCellsOnLoadCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		rulesOverwriteCellsOnLoadCombo.add("true");
		rulesOverwriteCellsOnLoadCombo.add("false");
		//rulesOverwriteCellsOnLoadCombo.setEnabled(false);
		
		Label lblNewLabel_22 = new Label(composite, SWT.NONE);
		lblNewLabel_22.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_22.setText("RulesOverwriteCellsOnLoad");
		
		forceReevaluationOfFeedersForFedCellsOnDataChangeCombo = new Combo(composite, SWT.NONE);
		forceReevaluationOfFeedersForFedCellsOnDataChangeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		forceReevaluationOfFeedersForFedCellsOnDataChangeCombo.add("true");
		forceReevaluationOfFeedersForFedCellsOnDataChangeCombo.add("false");
		//forceReevaluationOfFeedersForFedCellsOnDataChangeCombo.setEnabled(false);
		
		// Modelling
		
		Label lblNewLabel_20 = new Label(composite, SWT.NONE);
		lblNewLabel_20.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
		lblNewLabel_20.setText("Modelling");
		lblNewLabel_20.setBackground(HEADER);
		
		Label lblNewLabel_25 = new Label(composite, SWT.NONE);
		lblNewLabel_25.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_25.setText("DefaultMeasuresDimension");
		
		defaultMeasuresDimensionCombo = new Combo(composite, SWT.NONE);
		defaultMeasuresDimensionCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		defaultMeasuresDimensionCombo.add("true");
		defaultMeasuresDimensionCombo.add("false");
		//defaultMeasuresDimensionCombo.setEnabled(false);
		
		
		Label lblNewLabel_27 = new Label(composite, SWT.NONE);
		lblNewLabel_27.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_27.setText("EnableNewHierarchyCreation");
		
		enableNewHierarchyCreationCombo = new Combo(composite, SWT.NONE);
		enableNewHierarchyCreationCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		enableNewHierarchyCreationCombo.add("true");
		enableNewHierarchyCreationCombo.add("false");
		//enableNewHierarchyCreationCombo.setEnabled(false);
		
		Label lblNewLabel_26 = new Label(composite, SWT.NONE);
		lblNewLabel_26.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_26.setText("UserDefinedCalculations");
		
		userDefinedCalculationsCombo = new Combo(composite, SWT.NONE);
		userDefinedCalculationsCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		userDefinedCalculationsCombo.add("true");
		userDefinedCalculationsCombo.add("false");
		
		Label SpreadingPrecisionLabel = new Label(composite, SWT.NONE);
		SpreadingPrecisionLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		SpreadingPrecisionLabel.setText("SpreadingPrecision");
		
		spreadingPrecisionText = new Text(composite, SWT.BORDER);
		spreadingPrecisionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label ProportionSpreadToZeroCellsLabel = new Label(composite, SWT.NONE);
		ProportionSpreadToZeroCellsLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		ProportionSpreadToZeroCellsLabel.setText("ProportionSpreadToZeroCells");
		
		proportionSpreadToZeroCellsCombo = new Combo(composite, SWT.NONE);
		proportionSpreadToZeroCellsCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		// TI Processes
		
		Label lblNewLabel_28 = new Label(composite, SWT.NONE);
		lblNewLabel_28.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
		lblNewLabel_28.setText("TI Processes");
		lblNewLabel_28.setBackground(HEADER);
		
		Label lblNewLabel_29 = new Label(composite, SWT.NONE);
		lblNewLabel_29.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_29.setText("CognosTM1InterfacePath");
		
		cognosTM1InterfacePathText = new Text(composite, SWT.BORDER);
		cognosTM1InterfacePathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblNewLabel_30 = new Label(composite, SWT.NONE);
		lblNewLabel_30.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_30.setText("UseExcelSerialDate");
		
		useExcelSerialDateCombo = new Combo(composite, SWT.NONE);
		useExcelSerialDateCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		useExcelSerialDateCombo.setText("false");
		useExcelSerialDateCombo.setText("true");
		
		Label lblNewLabel_31 = new Label(composite, SWT.NONE);
		lblNewLabel_31.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_31.setText("MaximumTILockObjects");
		
		maximumTILockObjectsText = new Text(composite, SWT.BORDER);
		maximumTILockObjectsText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblNewLabel_32 = new Label(composite, SWT.NONE);
		lblNewLabel_32.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_32.setText("EnableTIDebugging");
		
		enableTIDebuggingCombo = new Combo(composite, SWT.NONE);
		enableTIDebuggingCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		enableTIDebuggingCombo.setText("false");
		enableTIDebuggingCombo.setText("true");
		
		// CAM
		
		Label lblNewLabel = new Label(composite, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
		lblNewLabel.setText("CAM");
		lblNewLabel.setBackground(HEADER);
		
		Label lblNewLabel_2 = new Label(composite, SWT.NONE);
		lblNewLabel_2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_2.setText("ClientCAMURI");
		
		clientCAMURIText = new Text(composite, SWT.BORDER);
		clientCAMURIText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblNewLabel_3 = new Label(composite, SWT.NONE);
		lblNewLabel_3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_3.setText("ServerCAMURI");
		
		serverCAMURIText = new Text(composite, SWT.BORDER);
		serverCAMURIText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label CreateNewCAMClientsLabel = new Label(composite, SWT.NONE);
		CreateNewCAMClientsLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		CreateNewCAMClientsLabel.setText("CreateNewCAMClients");
		
		createNewCAMClientsCombo = new Combo(composite, SWT.NONE);
		createNewCAMClientsCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		createNewCAMClientsCombo.add("true");
		createNewCAMClientsCombo.add("false");
		
		Label lblNewLabel_4 = new Label(composite, SWT.NONE);
		lblNewLabel_4.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_4.setText("CAMUseSSL");
		
		camUseSSLCombo = new Combo(composite, SWT.NONE);
		camUseSSLCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		camUseSSLCombo.add("true");
		camUseSSLCombo.add("false");
		
		Label lblNewLabel_5 = new Label(composite, SWT.NONE);
		lblNewLabel_5.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_5.setText("ClientPingCAMPassport");
		
		clientPingCAMPassportText = new Text(composite, SWT.BORDER);
		clientPingCAMPassportText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		//portalVariableFileText
		Label CAMPortalVariableFileLabel = new Label(composite, SWT.NONE);
		CAMPortalVariableFileLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		CAMPortalVariableFileLabel.setText("CAMPortalVariableFile");
		
		camPortalVariableFileText = new Text(composite, SWT.BORDER);
		camPortalVariableFileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblNewLabel_10 = new Label(composite, SWT.NONE);
		lblNewLabel_10.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_10.setText("ServerCAMURIRetryAttempts");
		
		serverCAMURIRetryAttemptsText = new Text(composite, SWT.BORDER);
		serverCAMURIRetryAttemptsText.setText("");
		serverCAMURIRetryAttemptsText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		// STARTUP
		
		Label lblNewLabel_13 = new Label(composite, SWT.NONE);
		lblNewLabel_13.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
		lblNewLabel_13.setText("Startup");
		lblNewLabel_13.setBackground(HEADER);
		
		Label lblPersistentfeeder = new Label(composite, SWT.NONE);
		lblPersistentfeeder.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPersistentfeeder.setText("PersistentFeeder");
		
		persistentFeedersCombo = new Combo(composite, SWT.NONE);
		persistentFeedersCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		persistentFeedersCombo.add("false");
		persistentFeedersCombo.add("true");
		
		Label lblMaximumcubeloadthreads = new Label(composite, SWT.NONE);
		lblMaximumcubeloadthreads.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblMaximumcubeloadthreads.setText("MaximumCubeLoadThreads");
		
		MaximumCubeLoadThreadsText = new Text(composite, SWT.BORDER);
		MaximumCubeLoadThreadsText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblNewLabel_24 = new Label(composite, SWT.NONE);
		lblNewLabel_24.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_24.setText("SkipLoadingAliases");
		
		skipLoadingAliasesCombo = new Combo(composite, SWT.NONE);
		skipLoadingAliasesCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		skipLoadingAliasesCombo.add("false");
		skipLoadingAliasesCombo.add("true");

		// NETWORK

		Label lblNewLabel_6 = new Label(composite, SWT.NONE);
		lblNewLabel_6.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		lblNewLabel_6.setText("Network");
		lblNewLabel_6.setBackground(HEADER);
		
		Label lblNewLabel_7 = new Label(composite, SWT.NONE);
		lblNewLabel_7.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_7.setText("IPVersion");
		
		ipVersionCombo = new Combo(composite, SWT.NONE);
		ipVersionCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		ipVersionCombo.add("IPV4");
		ipVersionCombo.add("IPV6");
		ipVersionCombo.add("Dual");
		
		Label lblNewLabel_8 = new Label(composite, SWT.NONE);
		lblNewLabel_8.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_8.setText("IPAddressV4");
		
		ipAddressV4Text = new Text(composite, SWT.BORDER);
		ipAddressV4Text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblNewLabel_9 = new Label(composite, SWT.NONE);
		lblNewLabel_9.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_9.setText("IPAddressV6");
		
		ipAddressV6Text = new Text(composite, SWT.BORDER);
		ipAddressV6Text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		// LDAP
		
		Label lblNewLabel_11 = new Label(composite, SWT.NONE);
		lblNewLabel_11.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
		lblNewLabel_11.setText("LDAP");
		lblNewLabel_11.setBackground(HEADER);
		
		Label ldapEnabledLabel = new Label(composite, SWT.NONE);
		ldapEnabledLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		ldapEnabledLabel.setText("Enabled");
		
		ldapEnabledCombo = new Combo(composite, SWT.BORDER);
		ldapEnabledCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		ldapEnabledCombo.add("true");
		ldapEnabledCombo.add("false");
		
		Label lblLdaphost = new Label(composite, SWT.NONE);
		lblLdaphost.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblLdaphost.setText("LDAPHost");
		
		ldapHostText = new Text(composite, SWT.BORDER);
		ldapHostText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblNewLabel_12 = new Label(composite, SWT.NONE);
		lblNewLabel_12.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_12.setText("LDAPPort");
		
		ldapPortText = new Text(composite, SWT.BORDER);
		ldapPortText.setText("");
		ldapPortText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblLdapsearchbase = new Label(composite, SWT.NONE);
		lblLdapsearchbase.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblLdapsearchbase.setText("LDAPSearchBase");
		
		ldapSearchBaseText = new Text(composite, SWT.BORDER);
		ldapSearchBaseText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label UseServerAccountLabel = new Label(composite, SWT.NONE);
		UseServerAccountLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		UseServerAccountLabel.setText("UseServerAccount");
		
		useServerAccountCombo = new Combo(composite, SWT.BORDER);
		useServerAccountCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		useServerAccountCombo.add("true");
		useServerAccountCombo.add("false");
		
		Label VerifyCertServerNameLabel = new Label(composite, SWT.NONE);
		VerifyCertServerNameLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		VerifyCertServerNameLabel.setText("VerifyCertServerName");
		
		verifyCertServerNameText = new Text(composite, SWT.BORDER);
		verifyCertServerNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label verifyServerSSLCertLabel = new Label(composite, SWT.NONE);
		verifyServerSSLCertLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		verifyServerSSLCertLabel.setText("VerifyServerSSLCert");
		
		verifyServerSSLCertCombo = new Combo(composite, SWT.BORDER);
		verifyServerSSLCertCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		verifyServerSSLCertCombo.add("true");
		verifyServerSSLCertCombo.add("false");
		
		Label SkipSSLCertVerificationLabel = new Label(composite, SWT.NONE);
		SkipSSLCertVerificationLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		SkipSSLCertVerificationLabel.setText("SkipSSLCertVerification");
		
		skipSSLCertVerificationCombo = new Combo(composite, SWT.BORDER);
		skipSSLCertVerificationCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		skipSSLCertVerificationCombo.add("true");
		skipSSLCertVerificationCombo.add("false");
		
		Label skipSSLCRLVerificationLabel = new Label(composite, SWT.NONE);
		skipSSLCRLVerificationLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		skipSSLCRLVerificationLabel.setText("SkipSSLCRLVerification");
		
		skipSSLCRLVerificationCombo = new Combo(composite, SWT.BORDER);
		skipSSLCRLVerificationCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		skipSSLCRLVerificationCombo.add("true");
		skipSSLCRLVerificationCombo.add("false");
		
		Label wellKnownUserNameLabel = new Label(composite, SWT.NONE);
		wellKnownUserNameLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		wellKnownUserNameLabel.setText("WellKnownUserName");
		
		wellKnownUserNameText = new Text(composite, SWT.BORDER);
		wellKnownUserNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label passwordFileLabel = new Label(composite, SWT.NONE);
		passwordFileLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		passwordFileLabel.setText("PasswordFile");
		
		passwordFileText = new Text(composite, SWT.BORDER);
		passwordFileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label passwordKeyFileLabel = new Label(composite, SWT.NONE);
		passwordKeyFileLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		passwordKeyFileLabel.setText("PasswordKeyFile");
		
		passwordKeyFileText = new Text(composite, SWT.BORDER);
		passwordKeyFileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		
		Label searchBaseLabel = new Label(composite, SWT.NONE);
		searchBaseLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		searchBaseLabel.setText("SearchBase");
		
		searchBaseText = new Text(composite, SWT.BORDER);
		searchBaseText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label SearchFieldLabel= new Label(composite, SWT.NONE);
		SearchFieldLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		SearchFieldLabel.setText("SearchField");
		
		SearchFieldText = new Text(composite, SWT.BORDER);
		SearchFieldText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		// Performance
		
	
		// Memory

		Label lblPerformance = new Label(composite, SWT.NONE);
		lblPerformance.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
		lblPerformance.setText("Memory");
		lblPerformance.setBackground(HEADER);
		
        Label MaximumViewSizeMBLabel = new Label(composite, SWT.NONE);
		MaximumViewSizeMBLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		MaximumViewSizeMBLabel.setText("MaximumViewSize");
		
		maximumViewSizeMBText = new Text(composite, SWT.BORDER);
		maximumViewSizeMBText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        Label ApplyMaximumViewSizeToEntireTransactionLabel = new Label(composite, SWT.NONE);
		ApplyMaximumViewSizeToEntireTransactionLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		ApplyMaximumViewSizeToEntireTransactionLabel.setText("ApplyMaximumViewSizeToEntireTransaction");
		
		applyMaximumViewSizeToEntireTransactionCombo = new Combo(composite, SWT.BORDER);
		applyMaximumViewSizeToEntireTransactionCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		applyMaximumViewSizeToEntireTransactionCombo.add("true");
		applyMaximumViewSizeToEntireTransactionCombo.add("false");
		
		
		Label MaximumUserSandboxSizeMBLabel = new Label(composite, SWT.NONE);
		MaximumUserSandboxSizeMBLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		MaximumUserSandboxSizeMBLabel.setText("MaximumUserSandboxSizeMB");
		
		maximumUserSandboxSizeMBText = new Text(composite, SWT.BORDER);
		maximumUserSandboxSizeMBText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		//MaximumMemoryForSubsetUndoKB
		Label MaximumMemoryForSubsetUndoKBLabel = new Label(composite, SWT.NONE);
		MaximumMemoryForSubsetUndoKBLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		MaximumMemoryForSubsetUndoKBLabel.setText("MaximumMemoryForSubsetUndoKB");
		
		MaximumMemoryForSubsetUndoKBText = new Text(composite, SWT.BORDER);
		MaximumMemoryForSubsetUndoKBText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		
		Label LockPagesInMemoryLabel = new Label(composite, SWT.NONE);
		LockPagesInMemoryLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		LockPagesInMemoryLabel.setText("LockPagesInMemory");
			
		lockPagesInMemoryLabelCombo = new Combo(composite, SWT.BORDER);
		lockPagesInMemoryLabelCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lockPagesInMemoryLabelCombo.add("true");
		lockPagesInMemoryLabelCombo.add("false");
		
		
		
		// MTQ

		Label lblMtq = new Label(composite, SWT.NONE);
		lblMtq.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
		lblMtq.setText("MTQ");
		lblMtq.setBackground(HEADER);
		
		Label mtqUseAllThreadsLabel = new Label(composite, SWT.NONE);
		mtqUseAllThreadsLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		mtqUseAllThreadsLabel.setText("UseAllThreads");
		
		mtqUseAllThreadsText = new Text(composite, SWT.BORDER);
		mtqUseAllThreadsText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label NumberOfThreadsToUseLabel = new Label(composite, SWT.NONE);
		NumberOfThreadsToUseLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		NumberOfThreadsToUseLabel.setText("NumberOfThreadsToUse");
		
		mtqNumberOfThreadsToUseText = new Text(composite, SWT.BORDER);
		mtqNumberOfThreadsToUseText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label mtqSingleCellConsolidationLabel = new Label(composite, SWT.NONE);
		mtqSingleCellConsolidationLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		mtqSingleCellConsolidationLabel.setText("SingleCellConsolidation");
		
		mtqSingleCellConsolidationCombo = new Combo(composite, SWT.BORDER);
		mtqSingleCellConsolidationCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		mtqSingleCellConsolidationCombo.add("true");
		mtqSingleCellConsolidationCombo.add("false");
		
		Label ImmediateCheckForSplitLabel = new Label(composite, SWT.NONE);
		ImmediateCheckForSplitLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		ImmediateCheckForSplitLabel.setText("ImmediateCheckForSplit");
		
		mtqImmediateCheckForSplitCombo = new Combo(composite, SWT.BORDER);
		mtqImmediateCheckForSplitCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		mtqImmediateCheckForSplitCombo.add("true");
		mtqImmediateCheckForSplitCombo.add("false");
		
		Label mtqOperationProgressCheckSkipLoopSizeLabel = new Label(composite, SWT.NONE);
		mtqOperationProgressCheckSkipLoopSizeLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		mtqOperationProgressCheckSkipLoopSizeLabel.setText("MTQ.OperationProgressCheckSkipLoopSize");
		
		mtqOperationProgressCheckSkipLoopSizeText = new Text(composite, SWT.BORDER);
		mtqOperationProgressCheckSkipLoopSizeText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		

		/*Label lblPerformanceMonitor = new Label(composite, SWT.NONE);
		lblPerformanceMonitor.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPerformanceMonitor.setText("PerformanceMonitorOn");
		
		performanceMonitorOnCombo = new Combo(composite, SWT.BORDER);
		performanceMonitorOnCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		performanceMonitorOnCombo.add("T");
		performanceMonitorOnCombo.add("F");
		*/
		
		
		ScrolledComposite.setContent(composite);
		ScrolledComposite.setExpandHorizontal(true);
		ScrolledComposite.setExpandVertical(true);
		ScrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		

		readServerConfigurationFromServer();
	}

	public boolean readServerConfigurationFromServer() {
		try {
			String request = "Configuration";
			tm1server.get(request);
				OrderedJSONObject configJSON = new OrderedJSONObject(tm1server.response);
				String version = configJSON.getString("ProductVersion");
				if (version.substring(0, 6).equals("10.2.2")) {
					//System.out.println("Detected 10.2.2");
					request = "Configuration";
					tm1server.get(request);
						String ServerName = configJSON.getString("ServerName");
						serverNameText.setText(ServerName);
						String AdminHost = configJSON.getString("AdminHost");
						adminHostText.setText(AdminHost);
						String DataBaseDirectory = configJSON.getString("DataBaseDirectory");
						databaseDirectoryText.setText(DataBaseDirectory);
						String PortNumber = Integer.toString(configJSON.getInt("PortNumber"));
						portNumberText.setText(PortNumber);
						String ClientMessagePortNumber = Integer.toString(configJSON.getInt("ClientMessagePortNumber"));
						clientMessagePortNumberText.setText(ClientMessagePortNumber);						
						String HTTPPortNumber = Integer.toString(configJSON.getInt("HTTPPortNumber"));
						httpPortNumberText.setText(HTTPPortNumber);
						String IntegratedSecurityMode = String.valueOf(configJSON.getBoolean("IntegratedSecurityMode"));
						securityModeCombo.setText(IntegratedSecurityMode);
						//String SecurityMode = configJSON.getString("SecurityMode");
						//String PrincipalName = configJSON.getString("PrincipalName");
						//String SecurityPackageName = configJSON.getString("SecurityPackageName");
						String ClientCAMURI = configJSON.getString("ClientCAMURI");
						clientCAMURIText.setText(ClientCAMURI);
						String ServerCAMURI = configJSON.getString("ServerCAMURI");
						serverCAMURIText.setText(ServerCAMURI);
						//String WebCAMURI = configJSON.getString("WebCAMURI");
						String ClientPingCAMPassport = Integer.toString(configJSON.getInt("ClientPingCAMPassport"));
						clientPingCAMPassportText.setText(ClientPingCAMPassport);
						String DistributedOutputDir = configJSON.getString("DistributedOutputDir");
						distributedOutputDirText.setText(DistributedOutputDir);
						String DisableSandboxing = String.valueOf(configJSON.getBoolean("DisableSandboxing"));
						disableSandboxingCombo.setText(DisableSandboxing);
						//String JobQueuing = String.valueOf(configJSON.getBoolean("JobQueuing"));
						String ForceReevaluationOfFeedersForFedCellsOnDataChange = String.valueOf(configJSON.getBoolean("ForceReevaluationOfFeedersForFedCellsOnDataChange"));
						this.forceReevaluationOfFeedersForFedCellsOnDataChangeCombo.setText(ForceReevaluationOfFeedersForFedCellsOnDataChange);
						//String UnicodeUpperLowerCase = String.valueOf(configJSON.getBoolean("UnicodeUpperLowerCase"));

				} else if (version.substring(0, 2).equals("11")) {
					//System.out.println("Detected 11");
					request = "ActiveConfiguration";
					tm1server.get(request);

						// Access
						
						configJSON = new OrderedJSONObject(tm1server.response);
						OrderedJSONObject AccessJSON = (OrderedJSONObject) configJSON.getJSONObject("Access");
						
						// Network
						
						OrderedJSONObject NetworkJSON = (OrderedJSONObject) AccessJSON.getJSONObject("Network");
						String IPAddress = "";
						if (!NetworkJSON.isNull("IPAddress")) IPAddress = NetworkJSON.getString("IPAddress");
						this.ipAddressV4Text.setText(IPAddress);
						
						String IPVersion = "IPv4";
						if (!NetworkJSON.isNull("IPVersion")) {
							IPVersion = NetworkJSON.getString("IPVersion");
							this.ipVersionCombo.setText(IPVersion);
						}
						
						String IdleConnectionTimeOut = "";
						if (!NetworkJSON.isNull("IdleConnectionTimeOut")) IdleConnectionTimeOut = NetworkJSON.getString("IdleConnectionTimeOut");
						idleConnectionTimeOutSecondsText.setText(IdleConnectionTimeOut);

						// Authentication
						
						OrderedJSONObject AuthenticationJSON = (OrderedJSONObject) AccessJSON.getJSONObject("Authentication");

						String SecurityPackageName = AuthenticationJSON.getString("SecurityPackageName");
						this.securityPackageNameCombo.setText(SecurityPackageName);

						String ServicePrincipalName = "";
						if (!AuthenticationJSON.isNull("ServicePrincipalName")) AuthenticationJSON.getString("ServicePrincipalName");
						this.servicePrincipalNameText.setText(ServicePrincipalName);
						
						String IntegratedSecurityMode = AuthenticationJSON.getString("IntegratedSecurityMode");
						this.securityModeCombo.setText(IntegratedSecurityMode);
						securityModeCombo.addModifyListener(new ModifyListener() {
							@Override
							 public void modifyText(ModifyEvent e) {
								updateConfig("Access", "Authentication", "IntegratedSecurityMode", securityModeCombo.getText());
							}
						});
						securityModeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						
						String MaximumLoginAttempts = Integer.toString(AuthenticationJSON.getInt("MaximumLoginAttempts"));
						maximumLoginAttemptsText.setText(MaximumLoginAttempts);
						maximumLoginAttemptsText.addFocusListener(new FocusListener() {
					        @Override
					        public void focusLost(FocusEvent e) {
					            if (!temp.equals(maximumLoginAttemptsText.getText())) {
					            	updateConfig("Access", "Authentication", "MaximumLoginAttempts", maximumLoginAttemptsText.getText());
					            } 
					        }
					        @Override
					        public void focusGained(FocusEvent e) {
					            temp = maximumLoginAttemptsText.getText();
					        }
					    });

						
						// SSL

						OrderedJSONObject SSLJSON = (OrderedJSONObject) AccessJSON.getJSONObject("SSL");

						String EnableJSON = String.valueOf(SSLJSON.getBoolean("Enable"));
						this.useSSLCombo.setText(EnableJSON);

						String CertificateID = "";
						if (!SSLJSON.isNull("CertificateID")) CertificateID = SSLJSON.getString("CertificateID");
						this.certificateIDText.setText(CertificateID);

						String CertAuthority = "";
						if (!SSLJSON.isNull("CertAuthority")) CertAuthority = SSLJSON.getString("CertAuthority");
						this.certAuthorityText.setText(CertAuthority);

						String CertRevocationFile = "";
						if (!SSLJSON.isNull("CertRevocationFile")) CertRevocationFile = SSLJSON.getString("CertRevocationFile");
						this.certRevocationFileText.setText(CertRevocationFile);
						
						String ClientExportServerKeyID = "";
						if (!SSLJSON.isNull("ClientExportServerKeyID")) ClientExportServerKeyID = SSLJSON.getString("ClientExportServerKeyID");
						this.clientExportServerKeyIDText.setText(ClientExportServerKeyID);
						
						String KeyFile = "";
						if (!SSLJSON.isNull("KeyFile")) KeyFile = SSLJSON.getString("KeyFile");
						this.keyFileLabelText.setText(KeyFile);

						
						String KeyStashFile = ""; 
						if (!SSLJSON.isNull("KeyStashFile")) KeyStashFile = SSLJSON.getString("KeyStashFile");
						this.keyStashFileText.setText(KeyStashFile);

						String KeyLabel = "";
						if (!SSLJSON.isNull("KeyLabel")) KeyLabel = SSLJSON.getString("KeyLabel");
						this.keyLabelText.setText(KeyLabel);

						String TLSCipherList = "";
						if (!SSLJSON.isNull("TLSCipherList")) TLSCipherList = SSLJSON.getString("TLSCipherList");
						this.tlsCipherListText.setText(TLSCipherList);
						
						String FIPSOperationMode = "";
						if (!SSLJSON.isNull("FIPSOperationModeJSON")) FIPSOperationMode = SSLJSON.getString("FIPSOperationModeJSON");
						this.fipsOperationModeText.setText(FIPSOperationMode);
						
						//String NIST_SP800_131A_MODE = String.valueOf(SSLJSON.getBoolean("NIST_SP800_131A_MODE"));

						// CAM
						
						OrderedJSONObject CAMJSON = (OrderedJSONObject) AccessJSON.getJSONObject("CAM");

						String CAMUseSSL = "";
						if (!CAMJSON.isNull("CAMUseSSL")) CAMUseSSL = CAMJSON.getString("CAMUseSSL");
						camUseSSLCombo.setText(CAMUseSSL);
						
						String ClientURI = "";
						if (!CAMJSON.isNull("ClientURI")) ClientURI = CAMJSON.getString("ClientURI");
						clientCAMURIText.setText(ClientURI);
						clientCAMURIText.addFocusListener(new FocusListener() {
					        @Override
					        public void focusLost(FocusEvent e) {
					            if (!temp.equals(clientCAMURIText.getText())) {
					            	updateConfig("Access", "CAM", "ClientURI", clientCAMURIText.getText());
					            } 
					        }
					        @Override
					        public void focusGained(FocusEvent e) {
					            temp = clientCAMURIText.getText();
					        }
					    });

						JSONArray ServerURIsJSONArray = CAMJSON.getJSONArray("ServerURIs");
						for (int i=0; i<ServerURIsJSONArray.size(); i++){
							String ServerCAMURI = ServerURIsJSONArray.getString(i);
							this.serverCAMURIText.append(ServerCAMURI);
						}
						
						serverCAMURIText.addFocusListener(new FocusListener() {
					        @Override
					        public void focusLost(FocusEvent e) {
					            if (!temp.equals(serverCAMURIText.getText())) {
					            	updateConfig("Access", "CAM", "ServerCAMURI", serverCAMURIText.getText());
					            } 
					        }
					        @Override
					        public void focusGained(FocusEvent e) {
					            temp = serverCAMURIText.getText();
					        }
					    });
						
						String PortalVariableFile = ""; 
						if (!CAMJSON.isNull("PortalVariableFile")) PortalVariableFile = CAMJSON.getString("PortalVariableFile");
						camPortalVariableFileText.setText("PortalVariableFile");
						
						String ClientPingCAMPassport = "";
						if (!CAMJSON.isNull("ClientPingCAMPassport")) ClientPingCAMPassport = CAMJSON.getString("ClientPingCAMPassport");
						clientPingCAMPassportText.setText(ClientPingCAMPassport);

						String ServerCAMURIRetryAttempts = "";
						if (!CAMJSON.isNull("ServerCAMURIRetryAttempts")) ServerCAMURIRetryAttempts = Integer.toString(CAMJSON.getInt("ServerCAMURIRetryAttempts"));
						serverCAMURIRetryAttemptsText.setText(ServerCAMURIRetryAttempts);

						String CreateNewCAMClients = "";
						if (!CAMJSON.isNull("CreateNewCAMClients")) CreateNewCAMClients = String.valueOf(CAMJSON.getBoolean("CreateNewCAMClients"));
						createNewCAMClientsCombo.setText(CreateNewCAMClients);
						
						// LDAP

						OrderedJSONObject LDAPJSON = (OrderedJSONObject) AccessJSON.getJSONObject("LDAP");

						String LDAPEnable = "";
						if (!LDAPJSON.isNull("Enable")) LDAPEnable = String.valueOf(LDAPJSON.getBoolean("Enable"));
						this.ldapEnabledCombo.setText(LDAPEnable);
						
						String LDAPHost = "";
						if (!LDAPJSON.isNull("Host")) LDAPHost = LDAPJSON.getString("Host");
						this.ldapHostText.setText(LDAPHost);
						
						String LDAPPort = "";
						if (!LDAPJSON.isNull("Port")) {
							LDAPPort = Integer.toString(LDAPJSON.getInt("Port"));
							this.ldapPortText.setText(LDAPPort);
						}

						String UseServerAccount = "";
						if (!LDAPJSON.isNull("UseServerAccount")) LDAPHost = LDAPJSON.getString("UseServerAccount");
						useServerAccountCombo.setText(UseServerAccount);
						
						JSONArray VerifyCertServerNameJSONArray = new JSONArray();
						if (!LDAPJSON.isNull("VerifyCertServerName")){
							LDAPJSON.getJSONArray("VerifyCertServerName");
						}

						String VerifyServerSSLCert = "";
						if (!LDAPJSON.isNull("VerifyServerSSLCert")) VerifyServerSSLCert = LDAPJSON.getString("VerifyServerSSLCert");
						this.verifyServerSSLCertCombo.setText(VerifyServerSSLCert);
						
						String SkipSSLCertVerification = "";
						if (!LDAPJSON.isNull("SkipSSLCertVerification")) SkipSSLCertVerification = LDAPJSON.getString("SkipSSLCertVerification");
						this.skipSSLCertVerificationCombo.setText(SkipSSLCertVerification);
						
						String WellKnownUserName = "";
						if (!LDAPJSON.isNull("WellKnownUserName")) WellKnownUserName = LDAPJSON.getString("WellKnownUserName");
						this.wellKnownUserNameText.setText(WellKnownUserName);
						
						String PasswordFile = "";
						if (!LDAPJSON.isNull("PasswordFile")) PasswordFile = LDAPJSON.getString("PasswordFile");
						this.passwordFileText.setText(PasswordFile);
						
						String PasswordKeyFile = "";
						if (!LDAPJSON.isNull("PasswordKeyFile")) PasswordKeyFile = LDAPJSON.getString("PasswordKeyFile");
						this.passwordKeyFileText.setText(PasswordKeyFile);
						
						String SearchBase = "";
						if (!LDAPJSON.isNull("SearchBase")) SearchBase = LDAPJSON.getString("SearchBase");
						this.searchBaseText.setText(SearchBase);
						
						String SearchField = "";
						if (!LDAPJSON.isNull("SearchField")) SearchField = LDAPJSON.getString("SearchField");
						this.SearchFieldText.setText(SearchField);

						
						// CAPI
						
						OrderedJSONObject CAPIJSON = (OrderedJSONObject) AccessJSON.getJSONObject("CAPI");

						String CAPIPort = Integer.toString(CAPIJSON.getInt("Port"));
						this.portNumberText.setText(CAPIPort);

						String ClientMessagePort = "";
						if (!CAPIJSON.isNull("ClientMessagePort")) {
							ClientMessagePort = Integer.toString(CAPIJSON.getInt("ClientMessagePort"));
							clientMessagePortNumberText.setText(ClientMessagePort);
						}
						
						String MessageCompression = "";
						if (!CAPIJSON.isNull("MessageCompression")) MessageCompression = CAPIJSON.getString("MessageCompression");
						this.messageCompressionText.setText(MessageCompression);

						String ProgressMessage = "";
						if (!CAPIJSON.isNull("ProgressMessage")) ProgressMessage = Boolean.toString(CAPIJSON.getBoolean("ProgressMessage"));
						this.progressMessageCombo.setText(ProgressMessage);

						String ClientVersionMaximum = "";
						if (!CAPIJSON.isNull("ClientVersionMaximum")) ClientVersionMaximum = CAPIJSON.getString("ClientVersionMaximum");
						this.clientVersionMaximumText.setText(ClientVersionMaximum);

						String ClientVersionMinimum = "";
						if (!CAPIJSON.isNull("ClientVersionMinimum")) ClientVersionMinimum = CAPIJSON.getString("ClientVersionMinimum");
						this.clientVersionMinimumText.setText(ClientVersionMinimum);
						
						String ClientVersionPrecision = "";
						if (!CAPIJSON.isNull("ClientVersionPrecision"))  ClientVersionPrecision = CAPIJSON.getString("ClientVersionPrecision");
						this.clientVersionPrecisionText.setText(ClientVersionPrecision);

						
						
						
						// HTTP/REST API
						
						OrderedJSONObject HTTPJSON = (OrderedJSONObject) AccessJSON.getJSONObject("HTTP");

						String HTTPPort = ""; 
						if (!LDAPJSON.isNull("Port")) HTTPPort = Integer.toString(HTTPJSON.getInt("Port"));
						this.httpPortNumberText.setText(HTTPPort);

						String SessionTimeout = "";
						if (!LDAPJSON.isNull("SessionTimeout")) {
							SessionTimeout = HTTPJSON.getString("SessionTimeout");
							HTTPSessionTimeoutMinutesText.setText(SessionTimeout);
						} else {
							HTTPSessionTimeoutMinutesText.setText("20");
						}
						
						
						// Administration

						OrderedJSONObject AdministrationJSON = (OrderedJSONObject) configJSON.getJSONObject("Administration");

						String ServerName = "";
						if (!AdministrationJSON.isNull("ServerName")) ServerName = AdministrationJSON.getString("ServerName");
						serverNameText.setText(ServerName);

						String AdminHost = "";
						if (!AdministrationJSON.isNull("AdminHost")) AdminHost = AdministrationJSON.getString("AdminHost");
						adminHostText.setText(AdminHost);
						
						String Language = "";
						if (!AdministrationJSON.isNull("Language")) Language = AdministrationJSON.getString("Language");
						languageText.setText(Language);
						
						String DataBaseDirectory = "";
						if (!AdministrationJSON.isNull("DataBaseDirectory")) DataBaseDirectory = AdministrationJSON.getString("DataBaseDirectory");
						databaseDirectoryText.setText(DataBaseDirectory);
						
						String DisableSandboxing = "";
						if (!AdministrationJSON.isNull("DisableSandboxing")) DisableSandboxing = String.valueOf(AdministrationJSON.getBoolean("DisableSandboxing"));
						disableSandboxingCombo.setText(DisableSandboxing);
						
						String RunningInBackground = "";
						if (!AdministrationJSON.isNull("RunningInBackground")) RunningInBackground = String.valueOf(AdministrationJSON.getBoolean("RunningInBackground"));

						String StartupChores = "";
						if (!AdministrationJSON.isNull("StartupChores")) {
							StartupChores = AdministrationJSON.getString("StartupChores");
							this.startupChoresText.setText(DisableSandboxing);
						}
						
						String PerformanceMonitorOn = "false";
						if (!AdministrationJSON.isNull("PerformanceMonitorOn")) {
							PerformanceMonitorOn = String.valueOf(AdministrationJSON.getBoolean("PerformanceMonitorOn"));
							performanceMonitorOnCombo.setText(PerformanceMonitorOn);
						}
						
						String PerfMonActive = "false";
						if (!AdministrationJSON.isNull("PerfMonActive")) {
							PerfMonActive = String.valueOf(AdministrationJSON.getBoolean("PerfMonActive"));
							this.perfMonActiveCombo.setText(PerfMonActive);
						}

						// Clients
						
						OrderedJSONObject Clients = (OrderedJSONObject) AdministrationJSON.getJSONObject("Clients");

						String PerfMonPasswordMinimumLengthActive = "";
						if (!AdministrationJSON.isNull("PasswordMinimumLength"))
							PerfMonPasswordMinimumLengthActive = Integer.toString(AdministrationJSON.getInt("PasswordMinimumLength"));

						String ClientPropertiesSyncInterval = "";
						if (!AdministrationJSON.isNull("ClientPropertiesSyncInterval"))
							PerfMonPasswordMinimumLengthActive = Integer.toString(AdministrationJSON.getInt("ClientPropertiesSyncInterval"));

						String RetainNonCAMGroupMembership = "";
						if (!AdministrationJSON.isNull("RetainNonCAMGroupMembership"))
							RetainNonCAMGroupMembership = String.valueOf(AdministrationJSON.getBoolean("RetainNonCAMGroupMembership"));

						// Logging
						
						OrderedJSONObject DebugLogJSON = (OrderedJSONObject) AdministrationJSON.getJSONObject("DebugLog");

						String LoggingDirectory = "";
						if (!DebugLogJSON.isNull("LoggingDirectory")) LoggingDirectory = DebugLogJSON.getString("LoggingDirectory");
						loggingDirectoryText.setText(LoggingDirectory);

						OrderedJSONObject ServerLogJSON = (OrderedJSONObject) AdministrationJSON.getJSONObject("ServerLog");

						String EnabledServerLogging = "";
						if (!ServerLogJSON.isNull("Enable")) EnabledServerLogging = ServerLogJSON.getString("Enable");
						serverLogEnabled.setText(EnabledServerLogging);
			
						String LogReleaseLineCount = "";
						if (!ServerLogJSON.isNull("LogReleaseLineCount")) LogReleaseLineCount = ServerLogJSON.getString("LogReleaseLineCount");
						logReleaseLineCountText.setText(LogReleaseLineCount);
						
						
						// Auditing
						
						OrderedJSONObject AuditLogJSON = (OrderedJSONObject) AdministrationJSON.getJSONObject("AuditLog");
						
						String AuditEnable = String.valueOf(AuditLogJSON.getBoolean("Enable"));
						this.auditEnabledCombo.setText(AuditEnable);

						String UpdateInterval = "";
						if (!AuditLogJSON.isNull("UpdateInterval")) UpdateInterval = AuditLogJSON.getString("UpdateInterval");
						auditLogUpdateIntervalText.setText(UpdateInterval);
						
						String MaxFileSizeKilobytes = "";
						if (!AuditLogJSON.isNull("MaxFileSizeKilobytes")) MaxFileSizeKilobytes = Integer.toString(AuditLogJSON.getInt("MaxFileSizeKilobytes"));
						auditLogMaxFileSizeText.setText(MaxFileSizeKilobytes);
						
						String MaxQueryMemoryKilobytes = "";
						if (!AuditLogJSON.isNull("MaxQueryMemoryKilobytes")) 	MaxFileSizeKilobytes = Integer.toString(AuditLogJSON.getInt("MaxQueryMemoryKilobytes"));
						auditLogMaxQueryMemoryText.setText(MaxQueryMemoryKilobytes);
						
						
						// Modelling
						
						OrderedJSONObject ModellingJSON = (OrderedJSONObject) configJSON.getJSONObject("Modelling");
						
						String DefaultMeasuresDimension = "false";
						if (!ModellingJSON.isNull("DefaultMeasuresDimension")) {
							DefaultMeasuresDimension = Boolean.toString(ModellingJSON.getBoolean("DefaultMeasuresDimension"));
							this.defaultMeasuresDimensionCombo.setText(DefaultMeasuresDimension);
						}
						
						String UserDefinedCalculations = "false";
						if (!ModellingJSON.isNull("UserDefinedCalculations")) {
							DefaultMeasuresDimension = Boolean.toString(ModellingJSON.getBoolean("UserDefinedCalculations"));
							this.userDefinedCalculationsCombo.setText(UserDefinedCalculations);
						}
						
						String EnableNewHierarchyCreation = "false";
						if (!ModellingJSON.isNull("EnableNewHierarchyCreation")) {
							EnableNewHierarchyCreation = Boolean.toString(ModellingJSON.getBoolean("EnableNewHierarchyCreation"));
							this.enableNewHierarchyCreationCombo.setText(EnableNewHierarchyCreation);
						}

						// Spreading

						OrderedJSONObject SpreadingJSON = (OrderedJSONObject) ModellingJSON.getJSONObject("Spreading");
						
						String SpreadingPrecision = "";
						if (!SpreadingJSON.isNull("SpreadingPrecision")) {
							SpreadingPrecision = SpreadingJSON.getString("SpreadingPrecision");
							spreadingPrecisionText.setText(SpreadingPrecision);
						}
						
						String ProportionSpreadToZeroCells = "false";
						if (!SpreadingJSON.isNull("ProportionSpreadToZeroCells")) {
							ProportionSpreadToZeroCells = Boolean.toString(SpreadingJSON.getBoolean("ProportionSpreadToZeroCells"));
							this.proportionSpreadToZeroCellsCombo.setText(ProportionSpreadToZeroCells);
						}
						
						// STARTUP
						
						OrderedJSONObject StartupJSON = (OrderedJSONObject) ModellingJSON.getJSONObject("Startup");
						
						String PersistentFeeders = "";
						if (!StartupJSON.isNull("PersistentFeeders")) {
							PersistentFeeders = StartupJSON.getString("PersistentFeeders");
							persistentFeedersCombo.setText(PersistentFeeders);
						} else {
							persistentFeedersCombo.setText("false");
						}
						
						String SkipLoadingAliases = "";
						if (!StartupJSON.isNull("SkipLoadingAliases")) {
							SkipLoadingAliases = StartupJSON.getString("SkipLoadingAliases");
							skipLoadingAliasesCombo.setText(SkipLoadingAliases);
						} else {
							skipLoadingAliasesCombo.setText("false");
						}
						
						String MaximumCubeLoadThreads = "";
						if (!StartupJSON.isNull("MaximumCubeLoadThreads")) {
							MaximumCubeLoadThreads = StartupJSON.getString("MaximumCubeLoadThreads");
							MaximumCubeLoadThreadsText.setText(MaximumCubeLoadThreads);
						} else{
							MaximumCubeLoadThreadsText.setText("");
						}

						// RULES
						
						OrderedJSONObject RulesJSON = (OrderedJSONObject) ModellingJSON.getJSONObject("Rules");
	
						String AllowSeparateNandCRules = "false";
						if (!RulesJSON.isNull("AllowSeparateNandCRules")) AllowSeparateNandCRules = RulesJSON.getString("AllowSeparateNandCRules");
						this.allowSeparateNandCRulesCombo.setText(AllowSeparateNandCRules);
						
						String AutomaticallyAddCubeDependencies = "false";
						if (!RulesJSON.isNull("AutomaticallyAddCubeDependencies")) AutomaticallyAddCubeDependencies = RulesJSON.getString("AutomaticallyAddCubeDependencies");
						this.automaticallyAddCubeDependenciesCombo.setText(AutomaticallyAddCubeDependencies);

						String ForceReevaluationOfFeedersForFedCellsOnDataChange = "false";
						if (!RulesJSON.isNull("ForceReevaluationOfFeedersForFedCellsOnDataChange")){
							ForceReevaluationOfFeedersForFedCellsOnDataChange = RulesJSON.getString("ForceReevaluationOfFeedersForFedCellsOnDataChange");
							this.forceReevaluationOfFeedersForFedCellsOnDataChangeCombo.setText(ForceReevaluationOfFeedersForFedCellsOnDataChange);
						} else {
							this.forceReevaluationOfFeedersForFedCellsOnDataChangeCombo.setText("false");

						}

						String RulesOverwriteCellsOnLoad = "false";
						if (!RulesJSON.isNull("RulesOverwriteCellsOnLoad")) RulesOverwriteCellsOnLoad = RulesJSON.getString("RulesOverwriteCellsOnLoad");
						this.rulesOverwriteCellsOnLoadCombo.setText(RulesOverwriteCellsOnLoad);
						
						// TI Processes

						OrderedJSONObject TIJSON = (OrderedJSONObject) ModellingJSON.getJSONObject("TI");
						
						String CognosTM1InterfacePath = "";
						if (!TIJSON.isNull("CognosTM1InterfacePath")) CognosTM1InterfacePath = TIJSON.getString("CognosTM1InterfacePath");
						cognosTM1InterfacePathText.setText(CognosTM1InterfacePath);
						
						String UseExcelSerialDate = "false";
						if (!TIJSON.isNull("UseExcelSerialDate")) UseExcelSerialDate = TIJSON.getString("UseExcelSerialDate");
						useExcelSerialDateCombo.setText(UseExcelSerialDate);

						String MaximumTILockObjects = "";
						if (!TIJSON.isNull("MaximumTILockObjects")) MaximumTILockObjects = TIJSON.getString("MaximumTILockObjects");
						maximumTILockObjectsText.setText(MaximumTILockObjects);

						String EnableTIDebugging = "false";
						if (!TIJSON.isNull("EnableTIDebugging")) {
							EnableTIDebugging = TIJSON.getString("EnableTIDebugging");
							enableTIDebuggingCombo.setText(EnableTIDebugging);
						} else {
							enableTIDebuggingCombo.setText("false");
						}
						
						
						// Peformance
						OrderedJSONObject PerformanceJSON = (OrderedJSONObject) configJSON.getJSONObject("Performance");

						// Memory
						OrderedJSONObject MemoryJSON = (OrderedJSONObject) PerformanceJSON.getJSONObject("Memory");
						
						String MaximumViewSizeMB = "500";
						if (!MemoryJSON.isNull("MaximumViewSizeMB")) {
							MaximumViewSizeMB = MemoryJSON.getString("MaximumViewSizeMB");
							maximumViewSizeMBText.setText(MaximumViewSizeMB);
						}
						
						String ApplyMaximumViewSizeToEntireTransaction = "";
						if (!MemoryJSON.isNull("ApplyMaximumViewSizeToEntireTransaction")) ApplyMaximumViewSizeToEntireTransaction = MemoryJSON.getString("ApplyMaximumViewSizeToEntireTransaction");
						this.applyMaximumViewSizeToEntireTransactionCombo.setText(ApplyMaximumViewSizeToEntireTransaction);
						
						String MaximumUserSandboxSizeMB = "";
						if (!MemoryJSON.isNull("MaximumUserSandboxSizeMB")) MaximumUserSandboxSizeMB = MemoryJSON.getString("MaximumUserSandboxSizeMB");
						this.maximumUserSandboxSizeMBText.setText(MaximumUserSandboxSizeMB);
						
						String MaximumMemoryForSubsetUndoKB = "";
						if (!MemoryJSON.isNull("MaximumMemoryForSubsetUndoKB")) MaximumMemoryForSubsetUndoKB = MemoryJSON.getString("MaximumMemoryForSubsetUndoKB");
						this.MaximumMemoryForSubsetUndoKBText.setText(MaximumMemoryForSubsetUndoKB);
						
						String LockPagesInMemory = "";
						if (!MemoryJSON.isNull("LockPagesInMemory")) MaximumUserSandboxSizeMB = MemoryJSON.getString("LockPagesInMemory");
						this.lockPagesInMemoryLabelCombo.setText(LockPagesInMemory);
						
						// MTQ
						OrderedJSONObject MTQJSON = (OrderedJSONObject) PerformanceJSON.getJSONObject("MTQ");

						String UseAllThreads = "";
						if (!MTQJSON.isNull("UseAllThreads")) UseAllThreads = MTQJSON.getString("UseAllThreads");
						this.mtqUseAllThreadsText.setText(UseAllThreads);
						
						String NumberOfThreadsToUse = "";
						if (!MTQJSON.isNull("NumberOfThreadsToUse")) NumberOfThreadsToUse = MTQJSON.getString("NumberOfThreadsToUse");
						mtqNumberOfThreadsToUseText.setText(NumberOfThreadsToUse);
						
						String SingleCellConsolidation = "";
						if (!MTQJSON.isNull("SingleCellConsolidation")) SingleCellConsolidation = MTQJSON.getString("SingleCellConsolidation");
						this.mtqSingleCellConsolidationCombo.setText(SingleCellConsolidation);
						
						String ImmediateCheckForSplit = "";
						if (!MTQJSON.isNull("ImmediateCheckForSplit")) ImmediateCheckForSplit = MTQJSON.getString("ImmediateCheckForSplit");
						this.mtqSingleCellConsolidationCombo.setText(ImmediateCheckForSplit);
						
						String OperationProgressCheckSkipLoopSize = "";
						if (!MTQJSON.isNull("OperationProgressCheckSkipLoopSize")) OperationProgressCheckSkipLoopSize = MTQJSON.getString("OperationProgressCheckSkipLoopSize");
						this.mtqOperationProgressCheckSkipLoopSizeText.setText(OperationProgressCheckSkipLoopSize);
						
					}
				return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	private void updateConfig(String l1, String l2, String l3, String parameterValue){
		try {
			if (l3 != null){
				OrderedJSONObject l3p = new OrderedJSONObject();
				l3p.put(l3, parameterValue);
				OrderedJSONObject l2p = new OrderedJSONObject();
				l2p.put(l2, l3p);
				OrderedJSONObject l1p = new OrderedJSONObject();
				l1p.put(l1, l2p);
				tm1server.patch("StaticConfiguration", l1p);
			} else {
					if (l2 != null){
					OrderedJSONObject l2p = new OrderedJSONObject();
					l2p.put(l2, parameterValue);
					OrderedJSONObject l1p = new OrderedJSONObject();
					l1p.put(l1, l2p);
					tm1server.patch("StaticConfiguration", l1p);
				} else {
					OrderedJSONObject l1p = new OrderedJSONObject();
					l1p.put(l1, parameterValue);
					tm1server.patch("StaticConfiguration", l1p);
				}
				
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
