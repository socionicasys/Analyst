package analyst;
/*
 * TextComponentDemo.java requires one additional file:
 *   DocumentSizeFilter.java
 */

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Dictionary;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.undo.*;

import analyst.ADocument.ASection;


@SuppressWarnings("serial")

public class Analyst extends JFrame implements WindowListener, PropertyChangeListener {
	public final static String version = "1.03";
	public JTextPane textPane;
	private AbstractDocument doc;
	ADocument aDoc;
	static final int MAX_CHARACTERS = 10000000;
	private JTextArea commentField;
	private JTabbedPane navigateTabs;
	private ControlsPane controlsPane;
	private StatusLabel status;
	private ATree navigateTree;
	private BTree analisysTree;
	private CTree hystogramTree;
	private JFileChooser fc;
	private JOptionPane optionPane;
	private JFrame frame = this;
	private String fileName = "";
	private JPopupMenu popupMenu;

	private boolean genetateReport = false;
	JProgressBar progress;
	private boolean programExit = false;
	private boolean makeNewDocument = false;
	private final static String extension = "htm";


	static String applicationName = "Информационный анализ";

	String newline = "\n";

	HashMap<Object, Action> actions;

	//undo helpers
	protected static UndoAction undoAction = null;
	protected static RedoAction redoAction = null;
	protected static UndoManager undo = new UndoManager();


	public Analyst(String arg0) {
		super(applicationName + " - " + ADocument.DEFAULT_TITLE);

		addWindowListener(this);
		setMinimumSize(new Dimension(600, 400));
		setPreferredSize(new Dimension(1000, 700));
		//Create the text pane and configure it.
		textPane = new JTextPane();
		// Replace the built-in  behavior when the caret highlight
		// becomes invisible when focus moves to another component
		textPane.setCaret(
			new DefaultCaret() {
				public void focusLost(FocusEvent e) {
				}

				;
			}
		);
		// popup menu for the textPane
		popupMenu = new JPopupMenu();

		textPane.setCaretPosition(0);
		//textPane.setMargin(new Insets(5,5,5,5));
		textPane.setMinimumSize(new Dimension(400, 100));

		// binding the popup menu for textPane
		Toolkit.getDefaultToolkit().getSystemEventQueue().push(new MyEventQueue());

		fc = new JFileChooser();
		fc.addChoosableFileFilter(new FileNameExtensionFilter("Файлы ." + extension, extension));
		optionPane = new JOptionPane();
		aDoc = new ADocument();
		textPane.setEditorKit(new AEditorKit());
		textPane.setDocument(aDoc);

		if (aDoc instanceof AbstractDocument) {
			doc = (AbstractDocument) aDoc;
			doc.setDocumentFilter(new DocumentSizeFilter(MAX_CHARACTERS));
		} else {
			System.err.println("Text pane's document isn't an AbstractDocument!");
			System.exit(-1);
		}
		JScrollPane scrollPane = new JScrollPane(textPane);
		scrollPane.setPreferredSize(new Dimension(600, 500));
		scrollPane.setMinimumSize(new Dimension(400, 250));

		//Create the text area for the status log and configure it.
		commentField = new JTextArea(5, 30);
		commentField.setEditable(false);
		commentField.setLineWrap(true);
		commentField.setWrapStyleWord(true);
		commentField.setMaximumSize(new Dimension(400, 30));

		JScrollPane scrollPaneForComment = new JScrollPane(commentField);
		scrollPaneForComment.setMinimumSize(new Dimension(400, 30));
		scrollPaneForComment.setMaximumSize(new Dimension(400, 30));
		scrollPaneForComment.setPreferredSize(new Dimension(400, 30));

		//Create a split pane for the change log and the text area.
		JSplitPane splitPaneV = new JSplitPane(
			JSplitPane.VERTICAL_SPLIT,
			scrollPane, scrollPaneForComment);
		splitPaneV.setOneTouchExpandable(false);

		//Create the status area.
		JPanel statusPane = new JPanel(new BorderLayout()); // GridLayout(1, 2));
		status =
			new StatusLabel("Откройте сохраненный документ или вставтьте анализируемый текст в центральное окно");
		progress = new JProgressBar(0, 100);
		progress.setSize(new Dimension(300, 30));

		progress.setVisible(false);
		statusPane.add(status, BorderLayout.WEST);
		statusPane.add(progress, BorderLayout.CENTER);

		// Create tabbed navigation pane

		navigateTree = new ATree(aDoc);
		analisysTree = new BTree(aDoc);
		hystogramTree = new CTree(aDoc);
		navigateTabs = createTabPane();

		JSplitPane splitPaneH = new JSplitPane(
			JSplitPane.HORIZONTAL_SPLIT,
			navigateTabs, splitPaneV);
		splitPaneH.setOneTouchExpandable(true);

		//Add the control panels.
		controlsPane = new ControlsPane();
		controlsPane.bindToTextPane(textPane, aDoc, commentField);

		textPane.addCaretListener(controlsPane);
		controlsPane.addADataListener(controlsPane);
		controlsPane.addADataListener(status);
		navigateTree.addTreeSelectionListener(controlsPane);
		analisysTree.addTreeSelectionListener(controlsPane);

		commentField.getCaret().addChangeListener((ChangeListener) controlsPane);

		JScrollPane scrollPaneControls = new JScrollPane(controlsPane);
		scrollPaneControls.setMinimumSize(new Dimension(300, 500));
		scrollPaneControls.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		//getContentPane().add(navigateTabs, BorderLayout.WEST);
		JPanel allButToolbar = new JPanel();

		getContentPane().add(splitPaneH, BorderLayout.CENTER);
		getContentPane().add(statusPane, BorderLayout.SOUTH);

		controlsPane.setMargin(new Insets(1, 1, 1, 1));
		controlsPane.setBorderPainted(true);

		//getContentPane().setLayout(new BorderLayout());
		//getContentPane().add(allButToolbar, BorderLayout.CENTER);
		getContentPane().add(controlsPane, BorderLayout.EAST);

		//Set up the menu bar.
		actions = createActionTable(textPane);
		JMenu fileMenu = createFileMenu();
		JMenu editMenu = createEditMenu();
		JMenu styleMenu = createStyleMenu();
		JMenu settingsMenu = createSettingsMenu();
		JMenu infoMenu = createInfoMenu();
		JMenuBar mb = new JMenuBar();

		mb.add(fileMenu);
		mb.add(editMenu);
		mb.add(styleMenu);
		mb.add(settingsMenu);
		mb.add(infoMenu);
		setJMenuBar(mb);

		//Add some key bindings.
		addBindings();

		textPane.setCaretPosition(0);

		//Start watching for undoable edits and caret changes.
		doc.addUndoableEditListener(new MyUndoableEditListener());
		textPane.addCaretListener(status);
		doc.addDocumentListener(new MyDocumentListener());

		// load document passed in the command line
		if (arg0 != null) {
			File file = new File(arg0);
			FileInputStream fis;
			try {
				fis = new FileInputStream(file);

				ProgressWindow pw = new ProgressWindow(frame, "    Идет загрузка файла...   ");
				aDoc.load(fis, pw);

				fileName = file.getAbsolutePath();

				status.setText("");
				frame.setTitle(applicationName + " - " + file.getName());
			} catch (Exception e1) {
				System.out.println("Ошибка при загрузке файла: " + arg0);
				e1.printStackTrace();
			}
		}
	}

	private JTabbedPane createTabPane() {
		JTabbedPane navigateTabs = new JTabbedPane();

		navigateTabs.addTab("Навигация", navigateTree.getContainer());
		navigateTabs.addTab("Анализ", analisysTree.getContainer());
		navigateTabs.addTab("График", hystogramTree.getContainer());
		//navigateTabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		navigateTabs.setMinimumSize(new Dimension(200, 400));
		navigateTabs.setPreferredSize(new Dimension(300, 400));
		return navigateTabs;
	}

	protected JMenu createFileMenu() {
		JMenu menu = new JMenu("Файл");

		//Undo and redo are actions of our own creation.

		menu.addSeparator();

		JMenuItem newDocumnet = new JMenuItem("Создать новый документ");
		newDocumnet.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int option = saveConfirmation();
				if (option == JOptionPane.YES_OPTION) {
					makeNewDocument = true;
				} else if (option == JOptionPane.NO_OPTION) {
					initNewDocument();
				}
			}
		});

		JMenuItem exit = new JMenuItem("Выход");
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				programExit = true;
				int option = saveConfirmation();
				if (option == JOptionPane.CANCEL_OPTION) programExit = false;
				else if (option == JOptionPane.YES_OPTION) ;
				else if (option == JOptionPane.NO_OPTION) System.exit(0);
			}
		});

		JMenuItem save = new JMenuItem("Сохранить");
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

				try {
					File file = null;
					if (fileName.length() == 0) {
						fc.setDialogTitle("Сохранение документа");
						int returnVal = fc.showDialog(Analyst.this, "Сохранить");
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							file = fc.getSelectedFile();
							fileName = file.getAbsolutePath();
							if (!fileName.endsWith("." + extension)) fileName += "." + extension;
							file = new File(fileName);

							if (file.exists()) {
								Object[] options = {"Да", "Нет"};
								if (JOptionPane.showOptionDialog(frame,
									"Такой файл существует!\n\nХотите перезаписать этот файл?", "Предупреждение!!!",
									JOptionPane.YES_NO_OPTION,
									JOptionPane.QUESTION_MESSAGE,
									null,
									options, null) ==
									JOptionPane.NO_OPTION) return;
							}
						}
					} else {
						file = new File(fileName);
					}

					if (file != null) {
						FileOutputStream fos = new FileOutputStream(file);
						ProgressWindow pw = new ProgressWindow(frame, "    Сохранение файла: ");
						IOWorker iow = new IOWorker(pw, aDoc, fos);
						iow.execute();
						frame.setTitle(applicationName + " - " + file.getName());
					}
				} catch (Exception e) {
					//
					System.out.println("Error writing document to file: ");
					e.printStackTrace();
					JOptionPane.showOptionDialog(frame,
						"Ошибка сохранения файла: " + fileName + "\n\n" + e.getMessage(),
						"Ошибка сохранения файла",
						JOptionPane.OK_OPTION,
						JOptionPane.ERROR_MESSAGE,
						null,
						new Object[]{"Закрыть"},
						null);
				}
			}
		});

		JMenuItem saveAs = new JMenuItem("Сохранить как...");
		saveAs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

				try {
					File file = null;
					fc.setDialogTitle("Сохранение документа под новым именем");

					int returnVal = fc.showDialog(Analyst.this, "Сохранить как...");
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						file = fc.getSelectedFile();
						fileName = file.getAbsolutePath();
						if (!fileName.endsWith("." + extension)) fileName += "." + extension;
						file = new File(fileName);
					} else {
						return;
					}

					if (file.exists()) {
						Object[] options = {"Да", "Нет"};
						if (JOptionPane.showOptionDialog(frame,
							"Такой файл существует!\n\nХотите перезаписать этот файл?", "Предупреждение!!!",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE,
							null,
							options, null) ==
							JOptionPane.NO_OPTION) return;
					}

					FileOutputStream fos = new FileOutputStream(file);
					ProgressWindow pw = new ProgressWindow(frame, "    Сохранение файла: ");
					IOWorker iow = new IOWorker(pw, aDoc, fos);
					iow.execute();

					frame.setTitle(applicationName + " - " + file.getName());
				} catch (Exception e) {
					//
					System.out.println("Error writing document to file: ");
					e.printStackTrace();
					JOptionPane.showOptionDialog(frame,
						"Ошибка сохранения файла: " + fileName + "\n\n" + e.getMessage(),
						"Ошибка сохранения файла",
						JOptionPane.OK_OPTION,
						JOptionPane.ERROR_MESSAGE,
						null,
						new Object[]{"Закрыть"},
						null);
				}
			}
		});

		JMenuItem load = new JMenuItem("Открыть");
		load.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

				try {

					status.setText("Открытие документа...");
					if (saveConfirmation() == JOptionPane.CANCEL_OPTION) return;
					fc.setDialogTitle("Открытие документа");
					int returnVal = fc.showDialog(Analyst.this, "Открыть");
					File file = null;
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						file = fc.getSelectedFile();
					} else {
						return;
					}

					FileInputStream fis = new FileInputStream(file);
					ProgressWindow pw = new ProgressWindow(frame, "    Идет загрузка файла...   ");
					//IOWorker lw = new IOWorker(pw, aDoc, fis);
					aDoc.load(fis, pw);

					fileName = file.getAbsolutePath();
					// after loading the document scroll it to the beginning
					textPane.grabFocus();

					status.setText("");
					frame.setTitle(applicationName + " - " + file.getName());
				} catch (FileNotFoundException e) {
					//
					System.out.println("Error opening  file");
					e.printStackTrace();
					JOptionPane.showOptionDialog(frame,
						"Ошибка открытия файла: " + fileName + "\n\n" + e.getMessage(),
						"Ошибка открытия файла",
						JOptionPane.OK_OPTION,
						JOptionPane.ERROR_MESSAGE,
						null,
						new Object[]{"Закрыть"},
						null);
				} catch (Exception e) {
					//
					System.out.println(" Error setting model to view :: bad location");
					e.printStackTrace();
				}
			}
		});

		JMenuItem append = new JMenuItem("Открыть и присоединить");
		append.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

				try {
					fc.setDialogTitle("Открыть и присоединить документ");
					int returnVal = fc.showDialog(Analyst.this, "Открыть и присоединить");
					File file = null;
					if (returnVal == JFileChooser.APPROVE_OPTION)
						file = fc.getSelectedFile();

					if (file != null) {
						FileInputStream fis = new FileInputStream(file);
						ProgressWindow pw = new ProgressWindow(frame, "    Идет загрузка файла...   ");
						aDoc.append(fis, pw);
					}
					// after loading the document scroll it to the beginning
					JViewport viewport = (JViewport) textPane.getParent();
					String d = textPane.getText();
					Rectangle rect = textPane.modelToView(0);
					// viewport.validate();
					// while (!viewport.isValid()){Thread.sleep(1000);};
					viewport.scrollRectToVisible(rect);

					//textPane.requestFocus();

				} catch (FileNotFoundException e) {
					//
					System.out.println("Error opening  file");
					e.printStackTrace();
					System.out.println("Error opening  file");
					e.printStackTrace();
					JOptionPane.showOptionDialog(frame,
						"Ошибка открытия файла: " + fileName + "\n\n" + e.getMessage(),
						"Ошибка открытия файла",
						JOptionPane.OK_OPTION,
						JOptionPane.ERROR_MESSAGE,
						null,
						new Object[]{"Закрыть"},
						null);
				} catch (Exception e) {
					//
					System.out.println(" Error setting model to view :: bad location");
					e.printStackTrace();
				}
			}
		});

		menu.add(newDocumnet);
		menu.addSeparator();
		menu.add(load);
		menu.add(append);
		menu.addSeparator();
		menu.add(save);
		menu.add(saveAs);
		menu.addSeparator();
		menu.add(exit);

		menu.addSeparator();

		return menu;
	}

	//This listens for and reports caret movements.
	protected class StatusLabel extends JLabel
		implements CaretListener, ADataChangeListener {
		public StatusLabel(String label) {
			super(label);
		}

		//Might not be invoked from the event dispatch thread.
		public void caretUpdate(CaretEvent e) {

			ASection s;
			s = aDoc.getASectionThatStartsAt(textPane.getCaretPosition());
			if (textPane.getText().length() <= 0)
				setText("Откройте сохраненный документ или вставтьте анализируемый текст в центральное окно");
			else if (s != null) {
				setText(aDoc.getAData(s).toString());
			} else if (e.getDot() == e.getMark())
				setText("Выделите область текста чтобы начать анализ...");
			else setText("Выберите аспект  и параметры обработки, используя панель справа...");
		} //caretUpdate()


		@Override
		public void aDataChanged(int start, int end, AData data) {

			if (data != null) setText(data.toString());
			else
				setText(" ");
			return;
		} //aDataChanged()
	} //class StatusLabel

	//This one listens for edits that can be undone.
	protected class MyUndoableEditListener
		implements UndoableEditListener {
		public void undoableEditHappened(UndoableEditEvent e) {
			//Remember the edit and update the menus.

			undo.addEdit(e.getEdit());
			undoAction.updateUndoState();
			redoAction.updateRedoState();

			if (e.getEdit().getPresentationName().contains("deletion")) {
				if (docDeleteConfirmation() == JOptionPane.NO_OPTION) {
					undo.undo();
				}
			}
		}
	}

	//And this one listens for any changes to the document.
	protected class MyDocumentListener
		implements DocumentListener {
		public void insertUpdate(DocumentEvent e) {
			displayEditInfo(e);
		}

		public void removeUpdate(DocumentEvent e) {
			displayEditInfo(e);
		}

		public void changedUpdate(DocumentEvent e) {
			displayEditInfo(e);
		}

		private void displayEditInfo(DocumentEvent e) {

		}
	}

	//Add a couple of emacs key bindings for navigation.
	protected void addBindings() {

		final JTextComponent.KeyBinding[] defaultBindings = {
			new JTextComponent.KeyBinding(
				KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK),
				AEditorKit.copyAction),
			new JTextComponent.KeyBinding(
				KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK),
				AEditorKit.pasteAction),
			new JTextComponent.KeyBinding(
				KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK),
				AEditorKit.cutAction),
		};
		final Action[] defaultActions = {
			new AEditorKit.CopyAction(textPane),
			new AEditorKit.PasteAction(textPane),
			new AEditorKit.CutAction(textPane),
		};

		Keymap k = textPane.getKeymap();
		JTextComponent.loadKeymap(k, defaultBindings, defaultActions);
	}

	//Create the edit menu.
	protected JMenu createEditMenu() {
		InputMap inputMap = textPane.getInputMap();
		Keymap keyMap = textPane.getKeymap();
		KeyStroke key = null;
		JMenuItem menuItem = null;

		JMenu menu = new JMenu("Редактирование");

		//Undo and redo are actions of our own creation.
		undoAction = new UndoAction();
		undoAction.putValue(Action.NAME, "Отменить действие");
		menuItem = new JMenuItem(undoAction);
		key = KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.CTRL_MASK);
		menuItem.setAccelerator(key);
		menu.add(menuItem);

//        menu.add(undoAction);

		redoAction = new RedoAction();
		redoAction.putValue(Action.NAME, "Вернуть действие");
		menuItem = new JMenuItem(redoAction);
		key = KeyStroke.getKeyStroke(KeyEvent.VK_Y, Event.CTRL_MASK);
		menuItem.setAccelerator(key);
		menu.add(menuItem);
//        menu.add(redoAction);

		menu.addSeparator();

		//Get the actions and stick them in the menu.
		Action a = new AEditorKit.CutAction(textPane); //getActionByName(DefaultEditorKit.cutAction);
		a.putValue(Action.NAME, "Вырезать");
		menuItem = new JMenuItem(a);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.CTRL_MASK));
		menu.add(menuItem);
		popupMenu.add(a);
		a = new AEditorKit.CopyAction(textPane); //getActionByName(DefaultEditorKit.copyAction);
		a.putValue(Action.NAME, "Копировать");
		menuItem = new JMenuItem(a);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK));

		menu.add(menuItem);
		popupMenu.add(a);
		a = new AEditorKit.PasteAction(textPane); //getActionByName(DefaultEditorKit.pasteAction);
		a.putValue(Action.NAME, "Вставить");
		menuItem = new JMenuItem(a);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Event.CTRL_MASK));
		menu.add(menuItem);

		popupMenu.add(a);
		menu.addSeparator();
		a = getActionByName(DefaultEditorKit.selectAllAction);
		a.putValue(Action.NAME, "Выделить всё");
		menuItem = new JMenuItem(a);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.CTRL_MASK));
		menu.add(menuItem);
		popupMenu.add(a);

		menu.addSeparator();
		popupMenu.addSeparator();

		a = new SearchAction((JTextComponent) textPane, aDoc);
		a.putValue(Action.NAME, "Поиск");
		menuItem = new JMenuItem(a);
		menuItem.setAction(a);
		key = KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK);
		menuItem.setAccelerator(key);
		menuItem.setAction(a);
		menu.add(menuItem);
		popupMenu.add(a);

		return menu;
	}

	//Create the style menu.
	protected JMenu createStyleMenu() {
		JMenu menu = new JMenu("Стиль");

		JMenuItem menuItem = new JMenuItem();

		InputMap keyMap = menu.getInputMap();

		Action action = new StyledEditorKit.BoldAction();
		action.putValue(Action.NAME, "Вопрос");
		menuItem.setAction(action);
		KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_B, Event.CTRL_MASK);
		menuItem.setAccelerator(key);
		menu.add(menuItem);

		action = new StyledEditorKit.ItalicAction();
		action.putValue(Action.NAME, "Цитата");
		menuItem = new JMenuItem();
		menuItem.setAction(action);
		key = KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.CTRL_MASK);
		menuItem.setAccelerator(key);
		menu.add(menuItem);

		return menu;
	}

	// infoMenu
	protected JMenu createInfoMenu() {
		JMenu menu = new JMenu("Информация");

		Action docProperties = new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				JTextArea titleField = new JTextArea(1, 30);
				titleField.setLineWrap(true);
				JTextArea expertField = new JTextArea(4, 30);
				expertField.setLineWrap(true);
				//expertField.setWrapStyleWord(true);
				JTextArea clientField = new JTextArea(1, 30);
				clientField.setLineWrap(true);
				JTextArea dateField = new JTextArea(1, 30);
				dateField.setLineWrap(true);
				JTextArea commentArea = new JTextArea(5, 30);
				commentArea.setLineWrap(true);

				JLabel lt = new JLabel("Название:");
				lt.setPreferredSize(new Dimension(100, 40));
				lt.setMaximumSize(new Dimension(100, 40));
				JLabel le = new JLabel("Эксперт(ы):");
				le.setPreferredSize(new Dimension(100, 40));
				le.setMaximumSize(new Dimension(100, 40));
				JLabel lc = new JLabel("Типируемый:");
				lc.setPreferredSize(new Dimension(100, 40));
				lc.setMaximumSize(new Dimension(100, 40));
				JLabel ld = new JLabel("Дата:");
				ld.setPreferredSize(new Dimension(100, 40));
				ld.setMaximumSize(new Dimension(100, 40));
				JLabel lcm = new JLabel("Примечание:");
				lcm.setPreferredSize(new Dimension(100, 40));
				lcm.setMaximumSize(new Dimension(100, 40));

				Panel pt = new Panel();
				Panel pe = new Panel();
				Panel pc = new Panel();
				Panel pd = new Panel();
				Panel ppc = new Panel();
				Panel panel = new Panel();
				//BoxLayout layout  = new BoxLayout (panel,BoxLayout.Y_AXIS);
				panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

				pt.add(lt);
				pt.setMinimumSize(new Dimension(500, 40));
				pt.add(new JScrollPane(titleField, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
				pe.add(le);
				pe.setMinimumSize(new Dimension(500, 50));
				pe.add(new JScrollPane(expertField, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
				pc.add(lc);
				pc.setMinimumSize(new Dimension(500, 40));
				pc.add(new JScrollPane(clientField, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
				pd.add(ld);
				pd.setMinimumSize(new Dimension(500, 40));
				pd.add(new JScrollPane(dateField, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
				ppc.add(lcm);
				ppc.setMinimumSize(new Dimension(500, 70));
				ppc.add(new JScrollPane(commentArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));

				String title = (String) aDoc.getProperty(ADocument.TitleProperty);
				String expert = (String) aDoc.getProperty(ADocument.ExpertProperty);
				String client = (String) aDoc.getProperty(ADocument.ClientProperty);
				String date = (String) aDoc.getProperty(ADocument.DateProperty);
				String comment = (String) aDoc.getProperty(ADocument.CommentProperty);

				panel.add(pt);
				panel.add(pe);
				panel.add(pc);
				panel.add(pd);
				panel.add(ppc);
				//panel.add(new Panel());

				if (title != null) titleField.setText(title);
				if (expert != null)
					expertField.setText(expert);
				if (client != null) clientField.setText(client);
				if (date != null) dateField.setText(date);
				if (comment != null) commentArea.setText(comment);

				if (JOptionPane.showOptionDialog(frame,
					panel,
					"Информация о документе",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.INFORMATION_MESSAGE,
					null,
					new Object[]{"Принять", "Отмена"},
					null
				) == JOptionPane.YES_OPTION) {

					title = titleField.getText();
					expert = expertField.getText();
					client = clientField.getText();
					date = dateField.getText();
					comment = commentArea.getText();

					Dictionary<Object, Object> properties = aDoc.getDocumentProperties();

					properties.put(ADocument.TitleProperty, title);
					properties.put(ADocument.ClientProperty, client);
					properties.put(ADocument.ExpertProperty, expert);
					properties.put(ADocument.DateProperty, date);
					properties.put(ADocument.CommentProperty, comment);
					aDoc.fireADocumentChanged();
				}
				;
			}
		};

		Action about = new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				JPanel panel = new JPanel();
				panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
				JTextArea info = new JTextArea(6, 40);
				info.setEditable(false);
				info.setBackground(panel.getBackground());
				info.setText(
					"Программа \"Информационный анализ\"\n" +
						"\n" +
						"© Школа системной соционики, Киев, 2010 г.\n" +
						"http://www.socionicasys.ru\n" +
						"Версия: " + version);

				JTextArea licText = new JTextArea(15, 40);
				info.setEditable(false);
				licText.setEditable(false);
				licText.setLineWrap(true);
				licText.setMargin(new Insets(3, 3, 3, 3));
				licText.setWrapStyleWord(true);
				licText.setAutoscrolls(false);

				String license = "ВНИМАНИЕ!!! Не удалось отрыть файл лицензии.\n\n" +
					"Согласно условий оригинальной лицензии GNU GPL, программное обеспечение должно поставляться вместе с текстом оригинальной лицензии.\n\n" +
					"Отсутствие такой лицензии может неправомерно ограничивать ваши права как пользователя. \n\n" +
					"Требуйте получение исходной лицензии от поставщика данного программного продукта.\n\n" +
					"Оригинальный текст GNU GPL на английском языке вы можете прочитать здесь: http://www.gnu.org/copyleft/gpl.html";

				InputStream is = Analyst.class.getClassLoader().getResourceAsStream("analyst/license.txt");
				InputStreamReader isr = null;
				if (is != null) {
					BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
					license = "";
					String next;
					try {
						next = br.readLine();

						while (next != null) {
							license += next + "\n";
							next = br.readLine();
						}
					} catch (IOException e) {
						System.out.println("Ошибка при открытии файла лицензии");
						e.printStackTrace();
					}
				}

				licText.setText(license);

				JScrollPane licenseScrl = new JScrollPane(licText, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

				licText.getCaret().setDot(0);
				licText.insert("", 0);

				Border border = BorderFactory.createTitledBorder("Лицензия:");
				licenseScrl.setBorder(border);

				panel.add(info);
				panel.add(licenseScrl);

				JOptionPane.showOptionDialog(frame,
					panel,
					"О программе",
					JOptionPane.INFORMATION_MESSAGE,
					JOptionPane.PLAIN_MESSAGE,
					null,
					new Object[]{"Закрыть"},
					null
				);
			}
		};

		docProperties.putValue(Action.NAME, "Свойства документа");
		about.putValue(Action.NAME, "О программе");
		menu.add(docProperties);
		menu.add(about);
		return menu;
	}

	protected JMenu createSettingsMenu() {

		JMenu menu = new JMenu("Установки");

		//Settings.
		JCheckBox reportCheckbox = new JCheckBox("Генерировать отчет при сохранении");
		reportCheckbox.setSelected(genetateReport);

		reportCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				genetateReport = ((JCheckBox) ae.getSource()).isSelected();
			}
		});

		menu.add(reportCheckbox);

		return menu;
	}

	protected SimpleAttributeSet[] initAttributes(int length) {
		//Hard-code some attributes.
		SimpleAttributeSet[] attrs = new SimpleAttributeSet[length];

		attrs[0] = new SimpleAttributeSet();
		StyleConstants.setFontFamily(attrs[0], "Tahoma");
		StyleConstants.setFontSize(attrs[0], 16);
		StyleConstants.setForeground(attrs[0], Color.BLUE);
		//     StyleConstants.setFirstLineIndent(attrs[0], 10);

		attrs[1] = new SimpleAttributeSet(attrs[0]);
		StyleConstants.setBold(attrs[1], true);
		StyleConstants.setForeground(attrs[0], Color.black);
		attrs[1].addAttribute("AData", "R+3V");

		attrs[2] = new SimpleAttributeSet(attrs[0]);
		StyleConstants.setItalic(attrs[2], true);

		attrs[3] = new SimpleAttributeSet(attrs[0]);
		StyleConstants.setFontSize(attrs[3], 20);

		attrs[4] = new SimpleAttributeSet(attrs[0]);
		StyleConstants.setFontSize(attrs[4], 12);

		attrs[5] = new SimpleAttributeSet(attrs[0]);
		StyleConstants.setForeground(attrs[5], Color.red);

		return attrs;
	}

	//The following two methods allow us to find an
	//action provided by the editor kit by its name.
	private HashMap<Object, Action> createActionTable(JTextComponent textComponent) {
		HashMap<Object, Action> actions = new HashMap<Object, Action>();
		Action[] actionsArray = textComponent.getActions();
		for (int i = 0; i < actionsArray.length; i++) {
			Action a = actionsArray[i];
			actions.put(a.getValue(Action.NAME), a);
			//System.out.println(a.getValue(Action.NAME));
		}
		return actions;
	}

	private Action getActionByName(String name) {
		return actions.get(name);
	}

	class UndoAction extends AbstractAction {
		public UndoAction() {
			super("Undo");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			try {
				undo.undo();
			} catch (CannotUndoException ex) {
				System.out.println("Unable to undo: " + ex.getMessage());
				ex.printStackTrace();
			}
			controlsPane.update();
			updateUndoState();
			redoAction.updateRedoState();
		}

		protected void updateUndoState() {
			if (undo.canUndo()) {
				setEnabled(true);
				putValue(Action.NAME, getRussianUndoName(undo.getUndoPresentationName()));
			} else {
				setEnabled(false);
				putValue(Action.NAME, "Отменить действие");
			}
		}

		private Object getRussianUndoName(String undoPresentationName) {
			if (undoPresentationName.contains("deletion")) return "Отменить удаление";
			if (undoPresentationName.contains("style")) return "Отменить";
			if (undoPresentationName.contains("addition")) return "Отменить ввод";
			return undoPresentationName;
		}
	}

	class RedoAction extends AbstractAction {
		public RedoAction() {
			super("Redo");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			try {
				undo.redo();
			} catch (CannotRedoException ex) {
				System.out.println("Unable to redo: " + ex);
				ex.printStackTrace();
			}
			controlsPane.update();
			updateRedoState();
			undoAction.updateUndoState();
		}

		protected void updateRedoState() {
			if (undo.canRedo()) {
				setEnabled(true);
				putValue(Action.NAME, getRussianRedoName(undo.getRedoPresentationName()));
			} else {
				setEnabled(false);
				putValue(Action.NAME, "Вернуть действие");
			}
		}

		private Object getRussianRedoName(String redoPresentationName) {
			if (redoPresentationName.contains("deletion")) return "Вернуть удаление";
			if (redoPresentationName.contains("style")) return "Вернуть";
			if (redoPresentationName.contains("addition")) return "Вернуть ввод";
			return redoPresentationName;
		}
	}

	/**
	 * Create the GUI and show it.  For thread safety,
	 * this method should be invoked from the
	 * event dispatch thread.
	 *
	 * @param arg0
	 */
	private static void createAndShowGUI(String arg0) {
		//Create and set up the window.
		final Analyst frame = new Analyst(arg0);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		//Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	//The standard main method.
	public static void main(String[] args) {
		//Schedule a job for the event dispatch thread:
		//creating and showing this application's GUI.

		String s = null;
		if (args != null && args.length > 0) s = args[0];
		final String arg0 = s;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				//Turn off metal's use of bold fonts
				UIManager.put("swing.boldMetal", Boolean.FALSE);
				createAndShowGUI(arg0);
			}
		});
	}

	@Override
	public void windowActivated(WindowEvent arg0) {

	}

	@Override
	public void windowClosed(WindowEvent arg0) {

	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		programExit = true;
		int option = saveConfirmation();
		if (option == JOptionPane.CANCEL_OPTION) programExit = false;
		else if (option == JOptionPane.YES_OPTION) ;
		else if (option == JOptionPane.NO_OPTION) System.exit(0);
	}


	@Override
	public void windowDeactivated(WindowEvent arg0) {

	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {

	}

	@Override
	public void windowIconified(WindowEvent arg0) {

	}

	@Override
	public void windowOpened(WindowEvent arg0) {

	}


	private int saveConfirmation() {
		int choice = JOptionPane.NO_OPTION;
		// if existing document is not empty
		if (aDoc.getLength() > 0) {
			choice = JOptionPane.showOptionDialog(frame,
				"Текущий документ не пустой.\n\nСохранить текущий документ?",
				"Требуется подтверждение",
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.ERROR_MESSAGE,
				null,
				new Object[]{"Сохранить", "Не сохранять", "Отмена"},
				null);
			if (choice == JOptionPane.YES_OPTION) {
				File file = null;

				if (fileName == null || (fileName != null && fileName.length() == 0)) {
					boolean cancel = false;
					boolean overwrite = false;
					int returnVal = 0;
					int option = 0;

					while (!(cancel || overwrite)) {
						fc.setDialogTitle("Сохранение документа");
						returnVal = fc.showDialog(Analyst.this, "Сохранить");
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							file = fc.getSelectedFile();

							fileName = file.getAbsolutePath();
							if (file.exists()) {
								Object[] options = {"Да", "Нет"};
								option = JOptionPane.showOptionDialog(frame,
									"Такой файл существует!\n\nХотите перезаписать этот файл?", "Предупреждение!!!",
									JOptionPane.YES_NO_OPTION,
									JOptionPane.QUESTION_MESSAGE,
									null,
									options, null);
								if (option == JOptionPane.YES_OPTION) overwrite = true;
							}	//if file doesn't exist
							else cancel = true;
						} else if (returnVal == JFileChooser.CANCEL_OPTION) cancel = true;
					} //end while
				}

				if (fileName != null & fileName.length() > 0) {
					try {

						file = new File(fileName);

						if (file != null) {
							FileOutputStream fos = new FileOutputStream(file);
							ProgressWindow pw = new ProgressWindow(frame, "    Сохранение файла: ");
							IOWorker iow = new IOWorker(pw, aDoc, fos);
							iow.execute();
						}
					} catch (Exception e) {
						//
						System.out.println("Error writing document to file: ");
						e.printStackTrace();
						JOptionPane.showOptionDialog(frame,
							"Ошибка сохранения файла: " + fileName + "\n\n" + e.getMessage(),
							"Ошибка сохранения файла",
							JOptionPane.OK_OPTION,
							JOptionPane.ERROR_MESSAGE,
							null,
							new Object[]{"Закрыть"},
							null);
					}
				}
			}
		}
		return choice;
	}// end save confifmation

	public int docDeleteConfirmation() {
		int choice = JOptionPane.YES_OPTION;
		int offset = textPane.getCaret().getMark();
		int dot = textPane.getCaret().getDot();
		if (aDoc.getASectionThatStartsAt(offset) != null)
			choice = JOptionPane.showOptionDialog(frame,
				"Вы собираетесь удалить размеченный фрагмент документа\n\nВы действительно хотите удалить фрагмент?",
				"Подтверждение удаления",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.INFORMATION_MESSAGE,
				null,
				new Object[]{"Удалить", "Не удалять"},
				"Не удалять");

		if (choice == JOptionPane.YES_OPTION) aDoc.removeCleanup(Math.min(offset, dot), Math.max(offset, dot));
		else {
			choice = JOptionPane.NO_OPTION;
		}
		return choice;
	}

	public class MyEventQueue extends EventQueue {
		protected void dispatchEvent(AWTEvent event) {
			super.dispatchEvent(event);

			// interested only in mouseevents
			if (!(event instanceof MouseEvent))
				return;

			MouseEvent me = (MouseEvent) event;

			// interested only in popuptriggers
			if (!me.isPopupTrigger())
				return;

			// me.getComponent(...) retunrs the heavy weight component on which event occured
			Component comp = SwingUtilities.getDeepestComponentAt(me.getComponent(), me.getX(), me.getY());

			// interested only in textcomponents
			if (!(comp instanceof JTextPane))
				return;

			// no popup shown by user code
			if (MenuSelectionManager.defaultManager().getSelectedPath().length > 0)
				return;

			// create popup menu and show

			Point pt = SwingUtilities.convertPoint(me.getComponent(), me.getPoint(), textPane);
			popupMenu.show(textPane, pt.x, pt.y);
		}
	}

	public Frame getFrame() {
		return frame;
	}

	public ATree getNavigeTree() {
		return navigateTree;
	}

	public CTree getAnalisysTree() {
		return hystogramTree;
	}

	public boolean getGenerateReport() {
		return genetateReport;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

		if (programExit)
			if (evt.getPropertyName().equals("state") && (evt.getNewValue()).toString().equals("DONE")) {
				System.exit(0);
			}
		if (makeNewDocument)
			if (evt.getPropertyName().equals("state") && (evt.getNewValue()).toString().equals("DONE")) {
				initNewDocument();
			}
	}

	private void initNewDocument() {
		aDoc.initNew();
		frame.setTitle(applicationName + " - " + (String) aDoc.getProperty((Document.TitleProperty)));
		fileName = "";
		makeNewDocument = false;
	}

	public static void initUndoManager() {
		undo.discardAllEdits();
		if (undoAction != null) undoAction.updateUndoState();
		if (redoAction != null) redoAction.updateRedoState();
	}
}//end class Analyst

