package TM1Diagnostic.UI;

import org.apache.wink.json4j.JSONException;
import org.eclipse.swt.widgets.Composite;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;




//import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Point;

import TM1Diagnostic.FunctionList;
import TM1Diagnostic.FunctionList.Function;
import TM1Diagnostic.FunctionsMenuContent;
import TM1Diagnostic.FunctionsMenuContent.FunctionMenuOption;
import TM1Diagnostic.REST.TM1Cube;
import TM1Diagnostic.REST.TM1RestException;
import TM1Diagnostic.REST.TM1Server;

import org.eclipse.swt.widgets.Button;

public class RuleEditor {

	protected Shell shell;
	protected boolean result;

	public Display display;
	private ServerExplorerComposite serverExplorer;

	private TM1Server tm1server;
	private TM1Cube cube;
	private StyledText rules_text;
	private Menu rules_menu;
	private Color found_color;
	private List<StyleRange> rules_style_ranges;
	private FunctionsMenuContent functionmenucontent;
	private FunctionList functionlist;
	private MenuItem preferences_menuitem;
	private UI_RulesFind finddialog;

	private boolean pendingSave;
	private Menu menu;
	private MenuItem menuFile;
	private MenuItem mntmFileSave;

	public RuleEditor(Shell parent, TM1Cube cube) throws TM1RestException {
		this.cube = cube;
		this.tm1server = cube.getServer();

		shell = new Shell(parent, SWT.RESIZE | SWT.MAX | SWT.MIN);
		shell.setSize(689, 448);
		shell.setText("Rule Editor -> " + cube.name);
		shell.setLayout(new GridLayout(1, false));

		display = shell.getDisplay();

		createContents();
		shell.layout();
		shell.open();
	}

	private void createContents() {

		functionlist = new FunctionList(".\\data\\rules.dat");		
		functionmenucontent = new FunctionsMenuContent(".\\data\\rules.dat");

		found_color = display.getSystemColor(SWT.COLOR_GREEN);

		Composite composite_1 = new Composite(shell, SWT.BORDER);
		composite_1.setLayout(new FillLayout(SWT.HORIZONTAL));
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		rules_text = new StyledText(composite_1, SWT.BORDER | SWT.V_SCROLL);
		rules_style_ranges = new ArrayList<StyleRange>();
		rules_menu = new Menu(rules_text);
		rules_text.setMenu(rules_menu);

		menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);

		MenuItem mntmFile = new MenuItem(menu, SWT.CASCADE);
		mntmFile.setText("File");

		Menu menu_1 = new Menu(mntmFile);
		mntmFile.setMenu(menu_1);

		MenuItem mntmSave = new MenuItem(menu_1, SWT.NONE);
		mntmSave.setText("Save");

		MenuItem mntmExit = new MenuItem(menu_1, SWT.NONE);
		mntmExit.setText("Exit");

		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false, 1, 1));

		Button btnNewButton = new Button(composite, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				save();
			}
		});
		btnNewButton.setText("Save");


		Button closeButton = new Button(composite, SWT.NONE);
		closeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				close();
			}
		});
		closeButton.setBounds(0, 0, 90, 30);
		closeButton.setText("Close");



		rules_menu.addMenuListener(new MenuAdapter() {
			public void menuShown(MenuEvent e) {
				MenuItem[] items = rules_menu.getItems();
				for (int i = 0; i < items.length; i++) {
					items[i].dispose();
				}

				MenuItem cubesmenuitem = new MenuItem(rules_menu, SWT.CASCADE);
				cubesmenuitem.setText("DB(cube, !dim1, 1dim2...)");
				Menu cubesMenu = new Menu(cubesmenuitem);
				cubesmenuitem.setMenu(cubesMenu);

				for (int i=0; i<tm1server.cubeCount(); i++){
					String cubename = tm1server.getCube(i).name;
					if (!cubename.startsWith("}")){
						MenuItem cube_menuitem = new MenuItem(cubesMenu, SWT.NONE);
						cube_menuitem.setText(cubename);
						cube_menuitem.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent event) {
								MenuItem item = (MenuItem)event.getSource();
								String s = "DB('" + item.getText() + "'";
								for (int j=0; j<cube.dimensionCount(); j++){
									s = s.concat(",!" + cube.getDimension(j).name);
								}
								s = s.concat(")");
								rules_text.insert(s);
							}
						});
					} 
					if (cubename.startsWith("}") && preferences_menuitem.getSelection()){
						MenuItem cube_menuitem = new MenuItem(cubesMenu, SWT.NONE);
						cube_menuitem.setText(cubename);
						cube_menuitem.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent event) {
								MenuItem item = (MenuItem)event.getSource();
								String s = "DB('" + item.getText() + "'";
								for (int j=0; j<cube.dimensionCount(); j++){
									s = s.concat(",!" + cube.getDimension(j).name);
								}
								s = s.concat(")");
								rules_text.insert(s);
							}
						});
					}
				}

				MenuItem dimensions_menuitem = new MenuItem(rules_menu, SWT.CASCADE);
				dimensions_menuitem.setText("Dimensions");
				Menu dimension_menu = new Menu(dimensions_menuitem);
				dimensions_menuitem.setMenu(dimension_menu);

				for (int i=0; i<cube.dimensionCount(); i++){
					String dimensionname = cube.getDimension(i).name;
					if (!dimensionname.startsWith("}")){
						MenuItem dimension_menuitem = new MenuItem(dimension_menu, SWT.NONE);
						dimension_menuitem.setText(dimensionname);
						dimension_menuitem.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent event) {
								MenuItem item = (MenuItem)event.getSource();
								rules_text.insert("!" + item.getText());
							}
						});
					}
					if (!dimensionname.startsWith("}") && preferences_menuitem.getSelection()){
						MenuItem dimension_menuitem = new MenuItem(dimension_menu, SWT.NONE);
						dimension_menuitem.setText(dimensionname);
						dimension_menuitem.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent event) {
								MenuItem item = (MenuItem)event.getSource();
								rules_text.insert(item.getText());
							}
						});
					}
				}

				MenuItem if_menuitem = new MenuItem(rules_menu, SWT.CASCADE);
				if_menuitem.setText("IF");
				if_menuitem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						MenuItem item = (MenuItem)event.getSource();
						rules_text.insert("If(<%Condition%>,<%True%>,<%False%>)");
					}
				});


				MenuItem functions_menuitem = new MenuItem(rules_menu, SWT.CASCADE);
				functions_menuitem.setText("Functions");
				Menu functions_menu = new Menu(functions_menuitem);
				functions_menuitem.setMenu(functions_menu);

				for (int i=0; i<functionmenucontent.getcount(); i++){
					MenuItem function_group_menuitem = new MenuItem(functions_menu, SWT.CASCADE);
					function_group_menuitem.setText(functionmenucontent.getmenugroup(i).getname());

					Menu function_group_menu = new Menu(function_group_menuitem);
					function_group_menuitem.setMenu(function_group_menu);

					for (int j=0; j<functionmenucontent.getmenugroup(i).getcount(); j++){
						MenuItem function_menuitem = new MenuItem(function_group_menu, SWT.NONE);
						FunctionMenuOption function = functionmenucontent.getmenugroup(i).getfunctionmenuoption(j);
						function_menuitem.setText(function.getname());
						function_menuitem.setData(function);
						function_menuitem.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent event) {
								MenuItem item = (MenuItem)event.getSource();
								FunctionMenuOption function = (FunctionMenuOption)item.getData();
								rules_text.insert(function.getsyntax());
							}
						});

					}
				}
			}
		});

		rules_text.addMouseTrackListener(new MouseTrackListener(){

			@Override
			public void mouseEnter(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExit(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseHover(MouseEvent event) {
				Point p = new Point(event.x, event.y);
				try {
					int offset = rules_text.getOffsetAtLocation(p);
					//System.out.println("offset: " + offset);
					wordlookup(offset, p);
				} catch (IllegalArgumentException ex){

				}

			}

		});



		rules_text.addLineStyleListener(new LineStyleListener()
		{
			public void lineGetStyle(LineStyleEvent e)
			{
				e.bulletIndex = rules_text.getLineAtOffset(e.lineOffset);
				StyleRange bulletstyle = new StyleRange();
				bulletstyle.metrics = new GlyphMetrics(0, 0, Integer.toString(rules_text.getLineCount()+1).length()*12);
				e.bullet = new Bullet(ST.BULLET_NUMBER,bulletstyle);
				e.styles = (StyleRange[])rules_style_ranges.toArray(new StyleRange[0]);
			}
		});

		rules_text.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				rules_style_ranges.clear();
				for (int i=0; i<rules_text.getLineCount(); i++){
					String line = rules_text.getLine(i);
					int line_start_index = rules_text.getOffsetAtLine(i);
					if (line.startsWith("#")){
						rules_style_ranges.add(new StyleRange(line_start_index, line.length(), display.getSystemColor(SWT.COLOR_BLUE), null));
					}
				}	
				rules_text.redraw();
			}
		});

		try {
			if(cube.checkServerForRules()){
				if(cube.readRulesFromServer()){
					rules_text.setText(cube.getrules());			
				}
			} 
		} catch (Exception ex){

		}
	}


	public void wordlookup(int offset, Point p){
		try {
			char char_at_offset = rules_text.getText().charAt(offset);
			char[] ignore_chars = new char[] { '\'', ',', '(', ')', '[', ']', '=', '>', ';', ':', '@', '<' };
			if (new String(ignore_chars).indexOf(char_at_offset) == -1){
				//System.out.println("Char " + char_at_offset);
				int leftpos = offset--;
				char goleft = rules_text.getText().charAt(leftpos);
				while (new String(ignore_chars).indexOf(goleft) == -1){
					leftpos--;
					if (leftpos <= 0){
						leftpos = 0;
						break;
					}
					goleft = rules_text.getText().charAt(leftpos);
				}
				int rightpos = offset++;
				char goright = rules_text.getText().charAt(rightpos);
				while (new String(ignore_chars).indexOf(goright) == -1){
					rightpos++;
					if (rightpos >= rules_text.getText().length()){
						rightpos = rules_text.getText().length();
						break;
					}
					goright = rules_text.getText().charAt(rightpos);
				}
				String word = rules_text.getText(leftpos+1, rightpos-1);
				if (goright == '\'' && goleft == '\'' ){
					if (rules_text.getText().charAt(rightpos+1) == ':'){
						ToolTip tip = new ToolTip(shell, SWT.ICON_INFORMATION);
						tip.setMessage("Dimension name: " + word);
						tip.setVisible(true);
					} else if (rules_text.getText().charAt(leftpos-1) == ':'){
						ToolTip tip = new ToolTip(shell, SWT.ICON_INFORMATION);
						tip.setMessage("Element name: " + word);
						tip.setVisible(true);
					} else {
						ToolTip tip = new ToolTip(shell, SWT.ICON_INFORMATION);
						tip.setMessage("Element name: " + word);
						tip.setVisible(true);
					}
				} else if (word.startsWith("!")){
					ToolTip tip = new ToolTip(shell, SWT.ICON_INFORMATION);
					tip.setMessage("Dimension name: " + word);
					tip.setVisible(true);
				} else {
					if (functionlist.contains(word) != -1){
						Function f = functionlist.getfunction(functionlist.contains(word));
						ToolTip tip = new ToolTip(shell, SWT.BALLOON);
						tip.setMessage("Function: " + f.getname() + "\nSyntax: " + f.getsyntax() + "\n" + f.getdocurl());
						tip.setVisible(true);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void findnext(String searchterm){
		rules_style_ranges.clear();
		int start = rules_text.getCaretOffset();
		String searcharea = rules_text.getText(start, rules_text.getText().length()-1);
		int found = searcharea.indexOf(searchterm);
		if (found == -1){
		} else {
			rules_text.setCaretOffset(start + found + searchterm.length());
			StyleRange styleRange = new StyleRange();
			styleRange.start = start + found;
			styleRange.length = searchterm.length();
			styleRange.background = found_color;
			rules_style_ranges.add(styleRange);
			rules_text.showSelection();
			rules_text.redraw();	
		}
	}

	public void findprevious(String searchterm){
		rules_style_ranges.clear();
		int start = 0;
		int end = rules_text.getCaretOffset();
		String searcharea = rules_text.getText(start, end);
		int found = searcharea.lastIndexOf(searchterm);
		if (found == -1){
		} else {
			rules_text.setCaretOffset(found);
			StyleRange styleRange = new StyleRange();
			styleRange.start = start + found;
			styleRange.length = searchterm.length();
			styleRange.background = found_color;
			rules_style_ranges.add(styleRange);
			rules_text.showSelection();
			rules_text.redraw();	
		}
	}

	public void infoMessage(String message) {
		MessageBox m = new MessageBox(shell, SWT.ICON_INFORMATION);
		m.setMessage(message);
		m.open();
	}


	public void errorMessage(String message) {
		MessageBox m = new MessageBox(shell, SWT.ICON_ERROR);
		m.setMessage(message);
		m.open();
	}

	private void openfinddialog(){
		if (finddialog == null || finddialog.shlFind.isDisposed()){
			finddialog = new UI_RulesFind(shell);
			finddialog.open();
		} else {
			finddialog.shlFind.forceActive();
		}
	}

	public void save(){
		try {
			cube.writeRulesToServer(rules_text.getText());
		} catch (JSONException | TM1RestException | URISyntaxException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public void close(){
		shell.close();
	}

	public boolean pendingSave(){
		return pendingSave;
	}
}
