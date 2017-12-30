package TM1Diagnostic;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.wink.json4j.JSONException;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import TM1Diagnostic.REST.TM1Chore;
import TM1Diagnostic.REST.TM1Process;
import TM1Diagnostic.REST.TM1RestException;
import TM1Diagnostic.UI.ServerExplorerComposite;
import TM1Diagnostic.UI.Wrarchitect;

public class ProcessWorker extends Thread {

	private TM1Process process;
	private ServerExplorerComposite parentUI;
	private boolean notify;

	public ProcessWorker(TM1Process process, ServerExplorerComposite parentUI, boolean notify){
		this.parentUI = parentUI;
		this.process = process;
		this.notify = notify;
	}

	@Override
	public void run() {
		try {
			process.execute();
			Display.getDefault().asyncExec( new Runnable() {
				public void run() {
					if (notify){
						parentUI.infoMessage("Process '" + process.displayName + "'" + " completed succesfully");
					} 
				}
			});			
		} catch (JSONException | TM1RestException | URISyntaxException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
}

