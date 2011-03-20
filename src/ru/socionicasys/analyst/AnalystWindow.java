package ru.socionicasys.analyst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.SwingWorker.StateValue;
import javax.swing.border.Border;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.*;
import javax.swing.text.JTextComponent.KeyBinding;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

@SuppressWarnings("serial")
public class AnalystWindow extends JFrame implements PropertyChangeListener {
	public static final String VERSION = "1.1-dev";

	private static final String EXTENSION = "htm";
	private static final String APPLICATION_NAME = "Информационный анализ";
	private static final Logger logger = LoggerFactory.getLogger(AnalystWindow.class);

	private final ADocument document;
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

	public AnalystWindow(String startupFilename) {
		super(String.format("%s - %s", APPLICATION_NAME, ADocument.DEFAULT_TITLE));

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
		textPane = new JTextPane();
		// Replace the built-in  behavior when the caret highlight
		// becomes invisible when focus moves to another component
		textPane.setCaret(
			new DefaultCaret() {
				@Override
				public void focusLost(FocusEvent e) {
				}
			}
		);
		// popup menu for the textPane
		popupMenu = new JPopupMenu();
		textPane.setComponentPopupMenu(popupMenu);
		textPane.setCaretPosition(0);
		textPane.setMinimumSize(new Dimension(400, 100));

		fileChooser = new JFileChooser();
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Файлы ." + EXTENSION, EXTENSION));
		document = new ADocument();
		textPane.setEditorKit(new AEditorKit());
		textPane.setDocument(document);

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
		navigateTree = new ATree(document);
		analysisTree = new BTree(document);
		document.addADocumentChangeListener(analysisTree);
		histogramTree = new CTree(document);
		document.addADocumentChangeListener(histogramTree);
		JTabbedPane navigateTabs = createTabPane();

		JSplitPane splitPaneH = new JSplitPane(
			JSplitPane.HORIZONTAL_SPLIT,
			navigateTabs, splitPaneV);
		splitPaneH.setOneTouchExpandable(true);

		//Add the control panels.
		controlsPane = new ControlsPane();
		controlsPane.bindToTextPane(textPane, document, commentField);

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

		textPane.setCaretPosition(0);

		//Start watching for undoable edits and caret changes.
		document.addUndoableEditListener(new MyUndoableEditListener());
		textPane.addCaretListener(status);

		pack();

		// load document passed in the command line
		if (startupFilename != null) {
			File file = new File(startupFilename);
			try {
				FileInputStream fis = new FileInputStream(file);
				try {
					ProgressWindow pw = new ProgressWindow(frame, document, "    Идет загрузка файла...   ");
					document.load(fis, pw);
					fileName = file.getAbsolutePath();
					status.setText("");
					frame.setTitle(String.format("%s - %s", APPLICATION_NAME, file.getName()));
				} catch (Exception e) {
					logger.error("Error loading file " + startupFilename, e);
				} finally {
					try {
						fis.close();
					} catch (IOException e) {
						logger.error("Error closing input stream", e);
					}
				}
			} catch (FileNotFoundException e) {
				logger.error("Unable to open file " + startupFilename, e);
			}
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

		JMenuItem load = new JMenuItem("Открыть");
		load.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					status.setText("Открытие документа...");
					if (saveConfirmation() == JOptionPane.CANCEL_OPTION) {
						return;
					}
					fileChooser.setDialogTitle("Открытие документа");
					int returnVal = fileChooser.showDialog(AnalystWindow.this, "Открыть");
					File file = null;
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						file = fileChooser.getSelectedFile();
					} else {
						return;
					}

					FileInputStream fis = new FileInputStream(file);
					ProgressWindow pw = new ProgressWindow(frame, document, "    Идет загрузка файла...   ");
					document.load(fis, pw);

					fileName = file.getAbsolutePath();
					// after loading the document scroll it to the beginning
					textPane.grabFocus();

					status.setText("");
					frame.setTitle(APPLICATION_NAME + " - " + file.getName());
				} catch (FileNotFoundException e) {
					logger.error("Error opening file " + fileName, e);
					JOptionPane.showOptionDialog(frame,
						"Ошибка открытия файла: " + fileName + "\n\n" + e.getMessage(),
						"Ошибка открытия файла",
						JOptionPane.OK_OPTION,
						JOptionPane.ERROR_MESSAGE,
						null,
						new Object[]{"Закрыть"},
						null);
				} catch (Exception e) {
					logger.error("Error setting model to view :: bad location", e);
				}
			}
		});

		JMenuItem append = new JMenuItem("Открыть и присоединить");
		append.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					fileChooser.setDialogTitle("Открыть и присоединить документ");
					int returnVal = fileChooser.showDialog(AnalystWindow.this, "Открыть и присоединить");
					File file = null;
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						file = fileChooser.getSelectedFile();
					}

					if (file != null) {
						FileInputStream fis = new FileInputStream(file);
						ProgressWindow pw = new ProgressWindow(frame, document, "    Идет загрузка файла...   ");
						document.append(fis, pw);
					}
					// after loading the document scroll it to the beginning
					JViewport viewport = (JViewport) textPane.getParent();
					String d = textPane.getText();
					Rectangle rect = textPane.modelToView(0);
					viewport.scrollRectToVisible(rect);
				} catch (FileNotFoundException e) {
					logger.error("Error opening file", fileName);
					JOptionPane.showOptionDialog(frame,
						"Ошибка открытия файла: " + fileName + "\n\n" + e.getMessage(),
						"Ошибка открытия файла",
						JOptionPane.OK_OPTION,
						JOptionPane.ERROR_MESSAGE,
						null,
						new Object[]{"Закрыть"},
						null);
				} catch (Exception e) {
					logger.error("Error setting model to view :: bad location");
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

		Action about = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent event) {
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
						"Версия: " + VERSION);

				JTextArea licText = new JTextArea(15, 40);
				info.setEditable(false);
				licText.setEditable(false);
				licText.setLineWrap(true);
				licText.setMargin(new Insets(3, 3, 3, 3));
				licText.setWrapStyleWord(true);
				licText.setAutoscrolls(false);

				licText.setText("ВНИМАНИЕ!!! Не удалось отрыть файл лицензии.\n\n" +
					"Согласно условий оригинальной лицензии GNU GPL, программное обеспечение должно поставляться вместе с текстом оригинальной лицензии.\n\n" +
					"Отсутствие такой лицензии может неправомерно ограничивать ваши права как пользователя. \n\n" +
					"Требуйте получение исходной лицензии от поставщика данного программного продукта.\n\n" +
					"Оригинальный текст GNU GPL на английском языке вы можете прочитать здесь: http://www.gnu.org/copyleft/gpl.html");

				InputStream is = AnalystWindow.class.getClassLoader().getResourceAsStream("license.txt");
				if (is != null) {
					BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
					try {
						StringBuilder license = new StringBuilder();
						String next = br.readLine();
						while (next != null) {
							license.append(next).append('\n');
							next = br.readLine();
						}
						licText.setText(license.toString());
					} catch (IOException e) {
						logger.error("Error opening license file", e);
					} finally {
						try {
							br.close();
						} catch (IOException e) {
							logger.error("Error closing BufferedReader", e);
						}
					}
				}

				JScrollPane licenseScrl = new JScrollPane(licText, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

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
						FileOutputStream fos = new FileOutputStream(file);
						ProgressWindow pw = new ProgressWindow(frame, document, "    Сохранение файла: ");
						LegacyHtmlWriter iow = new LegacyHtmlWriter(pw, document, fos);
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

	public CTree getAnalysisTree() {
		return histogramTree;
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
		document.initNew();
		initUndoManager();
		frame.setTitle(String.format("%s - %s", APPLICATION_NAME, document.getProperty(Document.TitleProperty)));
		fileName = "";
		makeNewDocument = false;
	}

	public void initUndoManager() {
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
			try {
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

				// Поскольку сохранение происходит асинхронно, поток закрывается в LegacyHtmlWriter
				@SuppressWarnings({"IOResourceOpenedButNotSafelyClosed"})
				FileOutputStream fos = new FileOutputStream(saveFile);
				ProgressWindow pw = new ProgressWindow(AnalystWindow.this, document, "    Сохранение файла: ");
				LegacyHtmlWriter backgroundWriter = new LegacyHtmlWriter(pw, document, fos);
				backgroundWriter.execute();
				frame.setTitle(String.format("%s - %s", APPLICATION_NAME, saveFile.getName()));
			} catch (HeadlessException ex) {
				logger.error("Somehow got stuck in a headless environment", ex);
			} catch (FileNotFoundException ex) {
				logger.error("Error writing document to file {}", fileName, ex);
				JOptionPane.showOptionDialog(frame,
					String.format("Ошибка сохранения файла: %s\n\n%s", fileName, ex.getMessage()),
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
