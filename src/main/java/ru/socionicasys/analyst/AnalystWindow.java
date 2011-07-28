package ru.socionicasys.analyst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.SwingWorker.StateValue;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.*;
import javax.swing.text.JTextComponent.KeyBinding;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

@SuppressWarnings("serial")
public class AnalystWindow extends JFrame implements PropertyChangeListener {
	private static final String EXTENSION = "htm";
	private static final Logger logger = LoggerFactory.getLogger(AnalystWindow.class);

	private final DocumentHolder documentHolder;
	private final JTextPane textPane;
	private final ControlsPane controlsPane;
	private final StatusLabel status;
	private final ATree navigateTree;
	private final BTree analysisTree;
	private final CTree histogramTree;
	private final JFileChooser fileChooser;
	private final JPopupMenu popupMenu;
	private final AnalystWindow frame = this;

	private String fileName = "";

	private boolean generateReport = false;
	private boolean programExit = false;
	private boolean makeNewDocument = false;

	private final Map<Object, Action> actions = new HashMap<Object, Action>();

	//undo helpers
	private final UndoAction undoAction = new UndoAction();
	private final RedoAction redoAction = new RedoAction();
	private final UndoManager undo = new UndoManager();

	public AnalystWindow() {
		super(String.format("%s - %s", VersionInfo.getApplicationName(), ADocument.DEFAULT_TITLE));

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onWindowClosing();
			}
		});
		setMinimumSize(new Dimension(600, 400));
		setPreferredSize(new Dimension(1000, 700));
		//Create the text pane and configure it.
		documentHolder = new DocumentHolder(new ADocument());
		textPane = new JTextPane(documentHolder.getModel());
		// Replace the built-in  behavior when the caret highlight
		// becomes invisible when focus moves to another component
		textPane.setCaret(new HighlightCaret());
		textPane.setNavigationFilter(new BlockNavigationFilter(documentHolder));
		// popup menu for the textPane
		popupMenu = new JPopupMenu();
		textPane.setComponentPopupMenu(popupMenu);
		textPane.setCaretPosition(0);
		textPane.setMinimumSize(new Dimension(400, 100));

		fileChooser = new JFileChooser();
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Файлы ." + EXTENSION, EXTENSION));

		JScrollPane scrollPane = new JScrollPane(textPane);
		scrollPane.setPreferredSize(new Dimension(600, 500));
		scrollPane.setMinimumSize(new Dimension(400, 250));

		//Create the text area for the status log and configure it.
		JTextArea commentField = new JTextArea(5, 30);
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
		JPanel statusPane = new JPanel(new BorderLayout());
		status =
			new StatusLabel("Откройте сохраненный документ или вставтьте анализируемый текст в центральное окно");
		JProgressBar progress = new JProgressBar(0, 100);
		progress.setSize(new Dimension(300, 30));

		progress.setVisible(false);
		statusPane.add(status, BorderLayout.WEST);
		statusPane.add(progress, BorderLayout.CENTER);

		// Create tabbed navigation pane
		navigateTree = new ATree(documentHolder);
		analysisTree = new BTree(documentHolder.getModel());
		documentHolder.addADocumentChangeListener(analysisTree);
		histogramTree = new CTree(documentHolder);
		documentHolder.addADocumentChangeListener(histogramTree);
		JTabbedPane navigateTabs = createTabPane();

		JSplitPane splitPaneH = new JSplitPane(
			JSplitPane.HORIZONTAL_SPLIT,
			navigateTabs, splitPaneV);
		splitPaneH.setOneTouchExpandable(true);

		//Add the control panels.
		controlsPane = new ControlsPane(textPane, documentHolder, commentField);

		textPane.addCaretListener(controlsPane);
		controlsPane.addADataListener(controlsPane);
		controlsPane.addADataListener(status);
		navigateTree.addTreeSelectionListener(controlsPane);
		analysisTree.addTreeSelectionListener(controlsPane);
		commentField.getCaret().addChangeListener(controlsPane);

		JScrollPane scrollPaneControls = new JScrollPane(controlsPane);
		scrollPaneControls.setMinimumSize(new Dimension(300, 500));
		scrollPaneControls.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		getContentPane().add(splitPaneH, BorderLayout.CENTER);
		getContentPane().add(statusPane, BorderLayout.SOUTH);

		controlsPane.setMargin(new Insets(1, 1, 1, 1));
		controlsPane.setBorderPainted(true);

		getContentPane().add(controlsPane, BorderLayout.EAST);

		//Set up the menu bar.
		for (Action a : textPane.getActions()) {
			actions.put(a.getValue(Action.NAME), a);
		}
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

		//Start watching for undoable edits and caret changes.
		documentHolder.addUndoableEditListener(new MyUndoableEditListener());
		textPane.addCaretListener(status);

		pack();
	}

	public void openFile(String filename, boolean append) throws FileNotFoundException {
		openFile(new File(filename), append);
	}

	public void openFile(File file, boolean append) throws FileNotFoundException {
		try {
			final LegacyHtmlReader worker = new LegacyHtmlReader(file);
			worker.getPropertyChangeSupport().addPropertyChangeListener("state",
					new DocumentLoadListener(append));
			worker.addPropertyChangeListener(new ProgressWindow(this, "    Идет загрузка файла...   "));
			worker.execute();

			fileName = file.getAbsolutePath();
			textPane.grabFocus();
			status.setText("");
			setTitle(String.format("%s - %s", VersionInfo.getApplicationName(), file.getName()));
		} catch (Exception e) {
			logger.error("Error loading file {}", file.getAbsolutePath(), e);
		}
	}

	private JTabbedPane createTabPane() {
		JTabbedPane navigateTabs = new JTabbedPane();
		navigateTabs.addTab("Навигация", navigateTree.getContainer());
		navigateTabs.addTab("Анализ", analysisTree.getContainer());
		navigateTabs.addTab("График", histogramTree.getContainer());
		navigateTabs.setMinimumSize(new Dimension(200, 400));
		navigateTabs.setPreferredSize(new Dimension(300, 400));
		return navigateTabs;
	}

	private JMenu createFileMenu() {
		JMenu menu = new JMenu("Файл");

		JMenuItem newDocumnet = new JMenuItem("Создать новый документ");
		newDocumnet.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				switch (saveConfirmation()) {
				case JOptionPane.YES_OPTION:
					makeNewDocument = true;
					break;

				case JOptionPane.NO_OPTION:
					initNewDocument();
					break;
				}
			}
		});

		JMenuItem exit = new JMenuItem("Выход");
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				programExit = true;
				switch (saveConfirmation()) {
				case JOptionPane.CANCEL_OPTION:
					programExit = false;
					break;

				case JOptionPane.NO_OPTION:
					System.exit(0);
					break;
				}
			}
		});

		JMenuItem save = new JMenuItem(new SaveAction(false));
		JMenuItem saveAs = new JMenuItem(new SaveAction(true));

		JMenuItem load = new JMenuItem(new OpenAction(false));
		JMenuItem append = new JMenuItem(new OpenAction(true));

		menu.add(newDocumnet);
		menu.addSeparator();
		menu.add(load);
		menu.add(append);
		menu.addSeparator();
		menu.add(save);
		menu.add(saveAs);
		menu.addSeparator();
		menu.add(exit);

		return menu;
	}

	//This listens for and reports caret movements.
	private class StatusLabel extends JLabel implements CaretListener, ADataChangeListener {
		public StatusLabel(String label) {
			super(label);
		}

		//Might not be invoked from the event dispatch thread.
		@Override
		public void caretUpdate(CaretEvent e) {
			ADocument document = documentHolder.getModel();
			ASection section = document.getASectionThatStartsAt(textPane.getCaretPosition());
			if (textPane.getText().length() <= 0) {
				setText("Откройте сохраненный документ или вставтьте анализируемый текст в центральное окно");
			} else if (section != null) {
				setText(document.getAData(section).toString());
			} else if (e.getDot() == e.getMark()) {
				setText("Выделите область текста чтобы начать анализ...");
			} else {
				setText("Выберите аспект  и параметры обработки, используя панель справа...");
			}
		}

		@Override
		public void aDataChanged(int start, int end, AData data) {
			if (data != null) {
				setText(data.toString());
			}
			else {
				setText(" ");
			}
		}
	}

	//This one listens for edits that can be undone.
	private class MyUndoableEditListener implements UndoableEditListener {
		@Override
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

	//Add a couple of emacs key bindings for navigation.
	private void addBindings() {
		final KeyBinding[] defaultBindings = {
			new KeyBinding(
				KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK),
				DefaultEditorKit.copyAction),
			new KeyBinding(
				KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK),
				DefaultEditorKit.pasteAction),
			new KeyBinding(
				KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK),
				DefaultEditorKit.cutAction),
		};
		final Action[] defaultActions = {
			new AEditorKit.CopyAction(textPane),
			new AEditorKit.PasteAction(textPane),
			new AEditorKit.CutAction(textPane),
		};

		JTextComponent.loadKeymap(textPane.getKeymap(), defaultBindings, defaultActions);
	}

	//Create the edit menu.
	private JMenu createEditMenu() {
		JMenu menu = new JMenu("Редактирование");

		//Undo and redo are actions of our own creation.
		undoAction.putValue(Action.NAME, "Отменить действие");
		JMenuItem menuItem = new JMenuItem(undoAction);
		KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.CTRL_MASK);
		menuItem.setAccelerator(key);
		menu.add(menuItem);

		redoAction.putValue(Action.NAME, "Вернуть действие");
		menuItem = new JMenuItem(redoAction);
		key = KeyStroke.getKeyStroke(KeyEvent.VK_Y, Event.CTRL_MASK);
		menuItem.setAccelerator(key);
		menu.add(menuItem);

		menu.addSeparator();

		//Get the actions and stick them in the menu.
		Action a = new AEditorKit.CutAction(textPane);
		a.putValue(Action.NAME, "Вырезать");
		menuItem = new JMenuItem(a);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.CTRL_MASK));
		menu.add(menuItem);
		popupMenu.add(a);
		a = new AEditorKit.CopyAction(textPane);
		a.putValue(Action.NAME, "Копировать");
		menuItem = new JMenuItem(a);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK));

		menu.add(menuItem);
		popupMenu.add(a);
		a = new AEditorKit.PasteAction(textPane);
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

		a = new SearchAction(textPane);
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
	private JMenu createStyleMenu() {
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
	private JMenu createInfoMenu() {
		JMenu menu = new JMenu("Информация");

		Action docProperties = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JTextArea titleField = new JTextArea(1, 30);
				titleField.setLineWrap(true);
				JTextArea expertField = new JTextArea(4, 30);
				expertField.setLineWrap(true);
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
				panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

				pt.add(lt);
				pt.setMinimumSize(new Dimension(500, 40));
				pt.add(new JScrollPane(titleField, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));
				pe.add(le);
				pe.setMinimumSize(new Dimension(500, 50));
				pe.add(new JScrollPane(expertField, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));
				pc.add(lc);
				pc.setMinimumSize(new Dimension(500, 40));
				pc.add(new JScrollPane(clientField, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));
				pd.add(ld);
				pd.setMinimumSize(new Dimension(500, 40));
				pd.add(new JScrollPane(dateField, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));
				ppc.add(lcm);
				ppc.setMinimumSize(new Dimension(500, 70));
				ppc.add(new JScrollPane(commentArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));

				ADocument document = documentHolder.getModel();
				String title = (String) document.getProperty(Document.TitleProperty);
				String expert = (String) document.getProperty(ADocument.ExpertProperty);
				String client = (String) document.getProperty(ADocument.ClientProperty);
				String date = (String) document.getProperty(ADocument.DateProperty);
				String comment = (String) document.getProperty(ADocument.CommentProperty);

				panel.add(pt);
				panel.add(pe);
				panel.add(pc);
				panel.add(pd);
				panel.add(ppc);

				if (title != null) {
					titleField.setText(title);
				}
				if (expert != null) {
					expertField.setText(expert);
				}
				if (client != null) {
					clientField.setText(client);
				}
				if (date != null) {
					dateField.setText(date);
				}
				if (comment != null) {
					commentArea.setText(comment);
				}

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

					Dictionary<Object, Object> properties = document.getDocumentProperties();

					properties.put(ADocument.TitleProperty, title);
					properties.put(ADocument.ClientProperty, client);
					properties.put(ADocument.ExpertProperty, expert);
					properties.put(ADocument.DateProperty, date);
					properties.put(ADocument.CommentProperty, comment);
					document.fireADocumentChanged();
				}
			}
		};

		docProperties.putValue(Action.NAME, "Свойства документа");
		menu.add(docProperties);
		menu.add(new AboutAction(this));
		return menu;
	}

	private JMenu createSettingsMenu() {
		JMenu menu = new JMenu("Установки");

		//Settings.
		final JCheckBox reportCheckbox = new JCheckBox("Генерировать отчет при сохранении");
		reportCheckbox.setSelected(generateReport);
		reportCheckbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				generateReport = reportCheckbox.isSelected();
			}
		});

		menu.add(reportCheckbox);

		return menu;
	}

	private Action getActionByName(String name) {
		return actions.get(name);
	}

	private class UndoAction extends AbstractAction {
		private final Logger logger = LoggerFactory.getLogger(UndoAction.class);

		public UndoAction() {
			super("Undo");
			setEnabled(false);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				undo.undo();
			} catch (CannotUndoException ex) {
				logger.error("Unable to undo", ex);
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
			if (undoPresentationName.contains("deletion")) {
				return "Отменить удаление";
			}
			if (undoPresentationName.contains("style")) {
				return "Отменить";
			}
			if (undoPresentationName.contains("addition")) {
				return "Отменить ввод";
			}
			return undoPresentationName;
		}
	}

	private class RedoAction extends AbstractAction {
		private final Logger logger = LoggerFactory.getLogger(RedoAction.class);

		public RedoAction() {
			super("Redo");
			setEnabled(false);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				undo.redo();
			} catch (CannotRedoException ex) {
				logger.error("Unable to redo", ex);
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
			if (redoPresentationName.contains("deletion")) {
				return "Вернуть удаление";
			}
			if (redoPresentationName.contains("style")) {
				return "Вернуть";
			}
			if (redoPresentationName.contains("addition")) {
				return "Вернуть ввод";
			}
			return redoPresentationName;
		}
	}

	private void onWindowClosing() {
		programExit = true;
		int option = saveConfirmation();
		if (option == JOptionPane.CANCEL_OPTION) {
			programExit = false;
		}
		else if (option == JOptionPane.NO_OPTION) {
			System.exit(0);
		}
	}

	private int saveConfirmation() {
		int choice = JOptionPane.NO_OPTION;
		// if existing document is not empty
		ADocument document = documentHolder.getModel();
		if (document.getLength() > 0) {
			choice = JOptionPane.showOptionDialog(frame,
				"Текущий документ не пустой.\n\nСохранить текущий документ?",
				"Требуется подтверждение",
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.ERROR_MESSAGE,
				null,
				new Object[]{"Сохранить", "Не сохранять", "Отмена"},
				null);
			if (choice == JOptionPane.YES_OPTION) {
				File file;
				if (fileName == null || fileName.length() == 0) {
					boolean cancel = false;
					boolean overwrite = false;
					while (!(cancel || overwrite)) {
						fileChooser.setDialogTitle("Сохранение документа");
						int returnVal = fileChooser.showDialog(AnalystWindow.this, "Сохранить");
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							file = fileChooser.getSelectedFile();
							fileName = file.getAbsolutePath();
							if (file.exists()) {
								Object[] options = {"Да", "Нет"};
								int option = JOptionPane.showOptionDialog(frame,
									"Такой файл существует!\n\nХотите перезаписать этот файл?", "Предупреждение!!!",
									JOptionPane.YES_NO_OPTION,
									JOptionPane.QUESTION_MESSAGE,
									null,
									options, null);
								if (option == JOptionPane.YES_OPTION) {
									overwrite = true;
								}
							}	//if file doesn't exist
							else {
								cancel = true;
							}
						} else if (returnVal == JFileChooser.CANCEL_OPTION) {
							cancel = true;
						}
					}
				}

				if (fileName != null && fileName.length() > 0) {
					try {
						file = new File(fileName);
						ProgressWindow pw = new ProgressWindow(frame, "    Сохранение файла: ");
						LegacyHtmlWriter iow = new LegacyHtmlWriter(this, document, file);
						iow.addPropertyChangeListener(pw);
						iow.addPropertyChangeListener(this);
						iow.execute();
					} catch (Exception e) {
						logger.error("Error writing document to file" + fileName, e);
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
		ADocument document = documentHolder.getModel();
		if (document.getASectionThatStartsAt(offset) != null) {
			choice = JOptionPane.showOptionDialog(frame,
				"Вы собираетесь удалить размеченный фрагмент документа\n\nВы действительно хотите удалить фрагмент?",
				"Подтверждение удаления",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.INFORMATION_MESSAGE,
				null,
				new Object[]{"Удалить", "Не удалять"},
				"Не удалять");
		}
		if (choice == JOptionPane.YES_OPTION) {
			document.removeCleanup(Math.min(offset, dot), Math.max(offset, dot));
		} else {
			choice = JOptionPane.NO_OPTION;
		}
		return choice;
	}

	public ATree getNavigeTree() {
		return navigateTree;
	}

	public boolean getGenerateReport() {
		return generateReport;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ("state".equals(evt.getPropertyName())) {
			StateValue state = (StateValue) evt.getNewValue();
			if (state == StateValue.DONE) {
				if (programExit) {
					System.exit(0);
				}
				if (makeNewDocument) {
					initNewDocument();
				}
			}
		}
	}

	private void initNewDocument() {
		ADocument document = documentHolder.getModel();
		document.initNew();
		initUndoManager();
		frame.setTitle(String.format("%s - %s", VersionInfo.getApplicationName(), document.getProperty(Document.TitleProperty)));
		fileName = "";
		makeNewDocument = false;
	}

	private void initUndoManager() {
		undo.discardAllEdits();
		if (undoAction != null) {
			undoAction.updateUndoState();
		}
		if (redoAction != null) {
			redoAction.updateRedoState();
		}
	}

	private class SaveAction extends AbstractAction {
		private final boolean saveAs;

		public SaveAction(boolean saveAs) {
			super(saveAs ? "Сохранить как..." : "Сохранить");
			this.saveAs = saveAs;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			File saveFile;
			if (saveAs || fileName.isEmpty()) {
				// Если у документа еще нет привязки к имени файла, или выбран пункт «Сохранить как…»,
				// нужно показать диалог сохранения файла
				fileChooser.setDialogTitle(saveAs ? "Сохранение документа под новым именем" : "Сохранение документа");
				int saveResult = fileChooser.showDialog(AnalystWindow.this, saveAs ? "Сохранить как..." : "Сохранить");
				if (saveResult != JFileChooser.APPROVE_OPTION) {
					return;
				}

				fileName = fileChooser.getSelectedFile().getAbsolutePath();
				if (!fileName.endsWith('.' + EXTENSION)) {
					fileName += '.' + EXTENSION;
				}
				saveFile = new File(fileName);

				// Подтверждение замены файла
				if (saveFile.exists()) {
					Object[] options = {"Да", "Нет"};
					int replaceResult = JOptionPane.showOptionDialog(AnalystWindow.this,
						"Такой файл существует!\n\nХотите перезаписать этот файл?", "Предупреждение!!!",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						options,
						null
					);
					if (replaceResult == JOptionPane.NO_OPTION) {
						return;
					}
				}
			} else {
				// Если документ уже связан с именем файла
				saveFile = new File(fileName);
			}

			ADocument document = documentHolder.getModel();
			ProgressWindow pw = new ProgressWindow(AnalystWindow.this, "    Сохранение файла: ");
			LegacyHtmlWriter backgroundWriter = new LegacyHtmlWriter(AnalystWindow.this, document, saveFile);
			backgroundWriter.addPropertyChangeListener(pw);
			backgroundWriter.addPropertyChangeListener(AnalystWindow.this);
			backgroundWriter.execute();
			frame.setTitle(String.format("%s - %s", VersionInfo.getApplicationName(), saveFile.getName()));
		}
	}

	private class OpenAction extends AbstractAction {
		private final boolean append;

		public OpenAction(boolean append) {
			super(append ? "Открыть и присоединить..." : "Открыть...");
			this.append = append;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				if (!append) {
					status.setText("Открытие документа...");
					if (saveConfirmation() == JOptionPane.CANCEL_OPTION) {
						return;
					}
				}
				fileChooser.setDialogTitle(append ? "Открыть и присоединить документ" : "Открытие документа");
				int openResult = fileChooser.showDialog(AnalystWindow.this, append ? "Открыть и присоединить" : "Открыть");
				if (openResult == JFileChooser.APPROVE_OPTION) {
					openFile(fileChooser.getSelectedFile(), append);
				}
			} catch (FileNotFoundException ex) {
				JOptionPane.showOptionDialog(AnalystWindow.this,
					String.format("Ошибка открытия файла: %s\n\n%s", fileName, ex.getMessage()),
					"Ошибка открытия файла",
					JOptionPane.OK_OPTION,
					JOptionPane.ERROR_MESSAGE,
					null,
					new Object[]{"Закрыть"},
					null);
			}
		}
	}

	/**
	 * Класс, слушающий состояние загрузки документа. Помещает документ в главное окно по окончанию загрузки.
	 */
	private class DocumentLoadListener implements PropertyChangeListener {
		private final boolean append;

		private DocumentLoadListener(boolean append) {
			this.append = append;
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			StateValue state = (StateValue) evt.getNewValue();
			if (state == StateValue.DONE) {
				try {
					LegacyHtmlReader worker = (LegacyHtmlReader) evt.getSource();
					ADocument document = worker.get();
					if (append) {
						documentHolder.getModel().appendDocument(document);
					} else {
						documentHolder.setModel(document);
						textPane.setDocument(document);
					}
					initUndoManager();
				} catch (InterruptedException e) {
					logger.info("Document loading interrupted", e);
				} catch (ExecutionException e) {
					logger.error("Error while loading document", e.getCause());
				}
			}
		}
	}
}