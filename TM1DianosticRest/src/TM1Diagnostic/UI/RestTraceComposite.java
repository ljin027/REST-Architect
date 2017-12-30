package TM1Diagnostic.UI;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.OrderedJSONObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import TM1Diagnostic.REST.TM1Server;
import org.eclipse.swt.custom.StyledText;


public class RestTraceComposite extends Composite {

	protected Shell shell;
	private Display display;
	private Runnable threadMonitorTimer;
	private int threadMonitorRefresh = 1;
	
	public StyledText httpTraceStyledText;
	
	private TM1Server tm1server;

	public RestTraceComposite(Composite parent, ServerExplorerComposite explorer, TM1Server tm1server) {
		super(parent, SWT.EMBEDDED);
		this.tm1server = tm1server;
		display = parent.getDisplay();
		onOpen();
	}
	
	
	private void onOpen(){
		setLayout(new GridLayout(1, false));

		Composite composite = new Composite(this, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, true, true, 1, 1));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		composite.setLayout(new FillLayout(SWT.VERTICAL));
		
		httpTraceStyledText = new StyledText(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);

	}
	
	public void setTM1Server(TM1Server tm1server) {
		this.tm1server = tm1server;
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public void stopThreadMonitor() {
		display.timerExec(-1, threadMonitorTimer);
	}

}
