package TM1Diagnostic;

import java.io.IOException;
import java.net.URISyntaxException;

import org.eclipse.swt.widgets.Display;

import TM1Diagnostic.REST.TM1Chore;
import TM1Diagnostic.REST.TM1RestException;
import TM1Diagnostic.UI.ServerExplorerComposite;

public class ChoreWorker extends Thread {

	private TM1Chore chore;
	private ServerExplorerComposite parentUI;
	private boolean notify;

	public ChoreWorker(TM1Chore chore, ServerExplorerComposite parentUI, boolean notify){
		this.parentUI = parentUI;
		this.chore = chore;
		this.notify = notify;
	}

	@Override
	public void run() {
		try {
			chore.execute();
			Display d = Display.getDefault();
			d.asyncExec( new Runnable() {
				@Override
				public void run() {
					if (notify){
						parentUI.infoMessage("Chore '" + chore.displayName + "'" + " completed succesfully");
					} 
				}
			});
		} catch (TM1RestException | URISyntaxException | IOException e) {
			// TODO Auto-generated catch block
			if (notify){
				parentUI.infoMessage("Chore '" + chore.displayName + "'" + " failed");
			} 
			e.printStackTrace();
		}

	}
}

