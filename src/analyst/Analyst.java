/*
 * Copyright (c) 1995 - 2008 Sun Microsystems, Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

package analyst;
/*
 * TextComponentDemo.java requires one additional file:
 *   DocumentSizeFilter.java
 */

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Dictionary;
import java.util.Locale;
import java.util.Vector;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.undo.*;

import analyst.ADocument.ASection;
import analyst.ControlsPane.AspectSelectionListener;


@SuppressWarnings("serial")

  public class Analyst extends JFrame implements WindowListener{
	JTextPane textPane;
    AbstractDocument doc;
    ADocument aDoc;   
    static final int MAX_CHARACTERS = 10000000;
    JTextArea commentField;
    JTabbedPane navigateTabs;
    ControlsPane controlsPane;
    StatusLabel caretListenerLabel;
    ATree navigateTree;
    BTree analisysTree;
    CTree hystogramTree;
    JFileChooser fc;
    JOptionPane optionPane;
    JFrame frame = this;
    String fileName = "";
    JPopupMenu popupMenu;
    
    
 static   String applicationName = "Информационный анализ";
 
    String newline = "\n";
    
    HashMap<Object, Action> actions;

    //undo helpers
    protected UndoAction undoAction;
    protected RedoAction redoAction;
    protected UndoManager undo = new UndoManager();

    public Analyst() {
        super(applicationName);
        addWindowListener(this);
        setMinimumSize(new Dimension(600,650));
        setPreferredSize(new Dimension(1000,650));
        //Create the text pane and configure it.
        textPane = new JTextPane();
        // Replace the built-in  behavior when the caret highlight
        // becomes invisible when focus moves to another component
        textPane.setCaret(
        	    new DefaultCaret(){
        			public void focusLost(FocusEvent e){};
        		}
        	);
        // popup menu for the textPane
        popupMenu = new JPopupMenu();
        
        textPane.setCaretPosition(0);
        textPane.setMargin(new Insets(5,5,5,5));
        textPane.setMinimumSize(new Dimension(400,400));
        
        // binding the popup menu for textPane
        Toolkit.getDefaultToolkit().getSystemEventQueue().push(new MyEventQueue()); 


        fc = new JFileChooser();
        fc.addChoosableFileFilter( new FileNameExtensionFilter("Файлы .htm", "htm"));
        optionPane = new JOptionPane();
        aDoc = new ADocument();
        textPane.setDocument(aDoc);
    
        if (aDoc instanceof AbstractDocument) {
            doc = (AbstractDocument)aDoc;
            doc.setDocumentFilter(new DocumentSizeFilter(MAX_CHARACTERS));
        } else {
            System.err.println("Text pane's document isn't an AbstractDocument!");
            System.exit(-1);
        }
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setPreferredSize(new Dimension(600, 500));
        scrollPane.setMinimumSize(new Dimension(400,400));
        
        //Create the text area for the status log and configure it.
        commentField = new JTextArea (5, 30) ;
        commentField.setEditable(false);
        commentField.setLineWrap(true);
        commentField.setWrapStyleWord(true);
        
        JScrollPane scrollPaneForComment = new JScrollPane(commentField);
        scrollPaneForComment.setMinimumSize(new Dimension(400,50));
 //       scrollPaneForComment.setPreferredSize(new Dimension(540,70));
        
        //Create a split pane for the change log and the text area.
        JSplitPane splitPaneV = new JSplitPane(
                                       JSplitPane.VERTICAL_SPLIT,
                                       scrollPane, scrollPaneForComment);
        splitPaneV.setOneTouchExpandable(false);
        
        

        //Create the status area.
        JPanel statusPane = new JPanel(new GridLayout(1, 1));
        caretListenerLabel =
                new StatusLabel("Откройте сохраненный документ или вставтьте анализируемый текст в центральное окно");
        statusPane.add(caretListenerLabel);
        // Create tabbed navigation pane

        navigateTree 	= new ATree (aDoc);
        analisysTree 	= new BTree (aDoc);
        hystogramTree	= new CTree (aDoc);
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
        controlsPane.addADataListener(caretListenerLabel);
        navigateTree.addTreeSelectionListener(controlsPane);
        analisysTree.addTreeSelectionListener(controlsPane);
        
        commentField.getCaret().addChangeListener((ChangeListener)controlsPane);

 
             
        //getContentPane().add(navigateTabs, BorderLayout.WEST);
        getContentPane().add(splitPaneH, BorderLayout.CENTER);
        getContentPane().add(statusPane, BorderLayout.PAGE_END);
        getContentPane().add(controlsPane, BorderLayout.EAST);
    
       
        
        //Set up the menu bar.
        actions=createActionTable(textPane);
        JMenu fileMenu = createFileMenu();
        JMenu editMenu = createEditMenu();
        JMenu styleMenu = createStyleMenu();
        JMenu infoMenu = createInfoMenu();
        JMenuBar mb = new JMenuBar();
        
        mb.add(fileMenu);
        mb.add(editMenu);
        mb.add(styleMenu);
        mb.add(infoMenu);
        setJMenuBar(mb);

        //Add some key bindings.
        addBindings();

        //Put the initial text into the text pane.
        initDocument();
        textPane.setCaretPosition(0);

        //Start watching for undoable edits and caret changes.
        doc.addUndoableEditListener(new MyUndoableEditListener());
        textPane.addCaretListener(caretListenerLabel);
        doc.addDocumentListener(new MyDocumentListener());
    }

    private JTabbedPane createTabPane() {
    	JTabbedPane navigateTabs = new  JTabbedPane();
    	
        navigateTabs.addTab("Навигация", navigateTree.getContainer());
    	navigateTabs.addTab("Анализ", analisysTree.getContainer());
    	navigateTabs.addTab("График", hystogramTree.getContainer());
    	//navigateTabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    	navigateTabs.setMinimumSize(new Dimension(200,400));
    	navigateTabs.setPreferredSize(new Dimension(300,400));
        return navigateTabs;
	}

	protected JMenu createFileMenu() {
    	JMenu menu = new JMenu("Файл");

        //Undo and redo are actions of our own creation.
       
        menu.addSeparator();
        
        JMenuItem newDocumnet = new JMenuItem("Создать новый документ");
        newDocumnet.addActionListener( new ActionListener()
        						{
									@Override
									public void actionPerformed(ActionEvent arg0) {
										if (saveConfirmation()!= JOptionPane.CANCEL_OPTION)	
																						aDoc.initNew();	
									}
        						
        						});
        
        JMenuItem exit = new JMenuItem("Выход");
        exit.addActionListener( new ActionListener()
        						{
									@Override
									public void actionPerformed(ActionEvent arg0) {
										if (saveConfirmation()!= JOptionPane.CANCEL_OPTION)	
																						System.exit(0);	
									}
        						
        						});
        
        
        JMenuItem save = new JMenuItem("Сохранить");
        save.addActionListener( new ActionListener()
        						{
								@Override
								public void actionPerformed(ActionEvent arg0) {
									
								    try {
								    	 File file = null;
								    	 if (fileName.length() == 0) {	
								    		 fc.setDialogTitle("Сохранение документа");
									    	 int returnVal = fc.showDialog(Analyst.this, "Сохранить");
									         if (returnVal == JFileChooser.APPROVE_OPTION) file = fc.getSelectedFile();
							       
									         }
									    	 else {
									    		 file = new File(fileName);
									    	 }
								    		 
								    	 if (file!=null){	 
									         	FileOutputStream fos = new FileOutputStream(file); 
												aDoc.save(fos);
								    	 }		

								        
										 } catch (IOException e) {
										// 
										System.out.println("Error writing document to file: ");
										e.printStackTrace();
										JOptionPane.showOptionDialog(frame, 
												"Ошибка сохранения файла: "+fileName + "\n\n" +e.getMessage(), 
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
        saveAs.addActionListener( new ActionListener()
        						{
								@Override
								public void actionPerformed(ActionEvent arg0) {
									
								    try {
								    	 	 File file = null;
									    	 fc.setDialogTitle("Сохранение документа под новым именем");
								    										    		 
								    		 int returnVal = fc.showDialog(Analyst.this, "Сохранить как...");
									         if (returnVal == JFileChooser.APPROVE_OPTION) file = fc.getSelectedFile();
									         
									         if (file.exists()){
									        	  Object[] options =  {"Да","Нет"};
									        	 if(JOptionPane.showOptionDialog(frame,
									                 "Такой файл существует!\n\nХотите перезаписать этот файл?", "Предупреждение!!!",
									                  JOptionPane.YES_NO_OPTION, 
									                  JOptionPane.QUESTION_MESSAGE,
									                  null, 
									                  options,null) ==
									                	  				JOptionPane.NO_OPTION) return;
									         }	
									   
								    		 
								    	 if (file!=null){	 
									         	FileOutputStream fos = new FileOutputStream(file); 
												aDoc.save(fos);
								    	 }		

								        
										 } catch (IOException e) {
										// 
										System.out.println("Error writing document to file: ");
										e.printStackTrace();
										JOptionPane.showOptionDialog(frame, 
												"Ошибка сохранения файла: "+fileName + "\n\n" +e.getMessage(), 
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
        load.addActionListener(new ActionListener()
		{
		@Override
		public void actionPerformed(ActionEvent arg0) {
		
		    try {
		    	 fc.setDialogTitle("Открытие документа");
		    	 if (saveConfirmation() == JOptionPane.CANCEL_OPTION) return;
		    	 int returnVal = fc.showDialog(Analyst.this, "Открыть");
		    	 File file = null;
		         if (returnVal == JFileChooser.APPROVE_OPTION) 
		        	 	file = fc.getSelectedFile();
		    		 
		        	 if (file!=null) {
		        		 FileInputStream fis = new FileInputStream(file);
						 aDoc.load(fis, false);
						 fileName = file.getAbsolutePath();
						 frame.setTitle(applicationName + " - "+ fileName);
						 
		        	 }
  				 // after loading the document scroll it to the beginning
					    JViewport viewport = (JViewport) textPane.getParent();
					    String d = textPane.getText();
					    Rectangle rect = textPane.modelToView(0);	
					   // viewport.validate();
					   // while (!viewport.isValid()){Thread.sleep(1000);};
					   	viewport.scrollRectToVisible(rect);
					  
						//textPane.requestFocus();
		       

			} catch (FileNotFoundException e  ) {
				//
				System.out.println("Error opening  file" );
				e.printStackTrace();
				JOptionPane.showOptionDialog(frame, 
						"Ошибка открытия файла: "+fileName + "\n\n" +e.getMessage(), 
						"Ошибка открытия файла", 
						JOptionPane.OK_OPTION, 
						JOptionPane.ERROR_MESSAGE, 
						null, 
						new Object[]{"Закрыть"}, 
						null);
			
			} 
			catch (Exception e) {
				//
				System.out.println(" Error setting model to view :: bad location");
				e.printStackTrace();
			}
		}
		
		});        
 
        JMenuItem append = new JMenuItem("Открыть и присоединить");
        append.addActionListener(new ActionListener()
		{
		@Override
		public void actionPerformed(ActionEvent arg0) {
		
		    try {    
		    	 fc.setDialogTitle("Открыть и присоединить документ");
		    	 int returnVal = fc.showDialog(Analyst.this, "Открыть и присоединить");
		    	 File file = null;
		         if (returnVal == JFileChooser.APPROVE_OPTION) 
		        	 	file = fc.getSelectedFile();
		    		 
		        	 if (file!=null) {
		        		 FileInputStream fis = new FileInputStream(file);
						 aDoc.load(fis, true);
						// frame.setTitle(applicationName + " - "+file.getName());
		        	 }
  				 // after loading the document scroll it to the beginning
					    JViewport viewport = (JViewport) textPane.getParent();
					    String d = textPane.getText();
					    Rectangle rect = textPane.modelToView(0);	
					   // viewport.validate();
					   // while (!viewport.isValid()){Thread.sleep(1000);};
					   	viewport.scrollRectToVisible(rect);
					  
						//textPane.requestFocus();
		       

			} catch (FileNotFoundException e  ) {
				//
				System.out.println("Error opening  file" );
				e.printStackTrace();
				System.out.println("Error opening  file" );
				e.printStackTrace();
				JOptionPane.showOptionDialog(frame, 
						"Ошибка открытия файла: "+fileName + "\n\n" +e.getMessage(), 
						"Ошибка открытия файла", 
						JOptionPane.OK_OPTION, 
						JOptionPane.ERROR_MESSAGE, 
						null, 
						new Object[]{"Закрыть"}, 
						null);
				
			} 
			catch (Exception e) {
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
        	s  = aDoc.getASectionThatStartsAt(textPane.getCaretPosition());
        	if (textPane.getText().length()<=0)setText("Откройте сохраненный документ или вставтьте анализируемый текст в центральное окно");
        	else
			if(s  !=null){
        		setText(aDoc.getAData(s).toString());}
        		else 
        			if (e.getDot()==e.getMark())
        			   setText("Выделите область текста чтобы начать анализ...");
        			   else   setText("Выберите аспект  и параметры обработки, используя панель справа...");
        	
   } //caretUpdate()
 
 /*
        protected void displayStructureInfo(final int dot,
                final int mark) {     
        	Element dre = aDoc.getDefaultRootElement();
        	Element ee = aDoc.getCharacterElement(dot); 
        	commentField.append("=====================================\n");
        	commentField.append("DOT = " + dot+"; MARK = "+ mark+ "\n");
        	commentField.append("DefaultRootElement = " +dre.getName()+ "\n");
        	if (ee!=null)commentField.append("CharacterElement at dot " + dot + " = " +ee.getName()+ "\n"); 
        	  else commentField.append("Element at dot " + dot + " = null\n");
        	
        	printElementInfo(ee);
       	
 /*       	for (int i=0; i<dre.getElementCount(); i++){
        		Element z =dre.getElement(i);
        		commentField.append("Element [" +i+ "] ="+z.getName()+ "\n");
        		commentField.append("Element [" +i+ "] ="+z.getName()+ "\n");
        		
        		AttributeSet as = z.getAttributes();
        		Enumeration names=null; 
        		if (as!=null)names = as.getAttributeNames();   
        		  else commentField.append("    No attributes for element " + i +"\n");
        		
        		while(names!=null && names.hasMoreElements()){
        		  String attr = as.getAttribute(names.nextElement()).toString(); 	
        		  commentField.append("    Attribute = "+ attr + "\n");
        		}// while
        	}//for i
  	
        }
 
        
        public void printElementInfo(Element e){
        	
        		Element z = e;
        		String elementName =z.getName(); 
        		commentField.append("___________________________________________\n");
        		commentField.append("Element "+elementName+  "; Parent = " + z.getParentElement()+"\n");
        		if (z.isLeaf())commentField.append("  ...isLeaf"); 
        		commentField.append("\n");
        		
        		AttributeSet as = z.getAttributes();
        		
        		Enumeration names=null;
        		
        		if (as!=null){names = as.getAttributeNames();   
        		}
        		  else commentField.append("    No attributes for element "+ elementName +"\n");
        		
        		while(names!=null && names.hasMoreElements()){
        		  Object attrName = names.nextElement();	
        		  String attr = as.getAttribute(attrName).toString(); 	
        		  commentField.append("    Attribute: " + attrName+ " = "+ attr + "\n");
        		}// while
        		if (z.getParentElement()!=null)printElementInfo(z.getParentElement());
        	
        }
 */       

		@Override
		public void aDataChanged(int start, int end, AData data) {
			
       	if (data!=null)  setText(data.toString());
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
	            
	            if (e.getEdit().getPresentationName().equals("deletion")){
	        		if (docDeleteConfirmation() ==JOptionPane.NO_OPTION){
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
        InputMap inputMap = textPane.getInputMap();

        //Ctrl-b to go backward one character
        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_B, Event.CTRL_MASK);
        inputMap.put(key, DefaultEditorKit.backwardAction);

        //Ctrl-f to go forward one character
        key = KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK);
        inputMap.put(key, DefaultEditorKit.forwardAction);

        //Ctrl-p to go up one line
        key = KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK);
        inputMap.put(key, DefaultEditorKit.upAction);

        //Ctrl-n to go down one line
        key = KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK);
        inputMap.put(key, DefaultEditorKit.downAction);
    }

    //Create the edit menu.
    protected JMenu createEditMenu() {
        JMenu menu = new JMenu("Редактирование");

        //Undo and redo are actions of our own creation.
        undoAction = new UndoAction();
        undoAction.putValue(Action.NAME, "Отменить действие");
        menu.add(undoAction);

        redoAction = new RedoAction();
        redoAction.putValue(Action.NAME, "Повторить действие");
        menu.add(redoAction);

        menu.addSeparator();
  

        //These actions come from the default editor kit.
        //Get the ones we want and stick them in the menu.
        Action a = getActionByName(DefaultEditorKit.cutAction);
        a.putValue(Action.NAME, "Вырезать");
        menu.add(a);
        popupMenu.add(a);
        a = getActionByName(DefaultEditorKit.copyAction);
        a.putValue(Action.NAME, "Копировать");
        menu.add(a);
        popupMenu.add(a);
        a = getActionByName(DefaultEditorKit.pasteAction);
        a.putValue(Action.NAME, "Вставить");
        menu.add(a);
        popupMenu.add(a);
        menu.addSeparator();
        a = getActionByName(DefaultEditorKit.selectAllAction);
        a.putValue(Action.NAME, "Выделить всё");
        menu.add(a);
        popupMenu.add(a);
        return menu;
    }

    //Create the style menu.
    protected JMenu createStyleMenu() {
        JMenu menu = new JMenu("Стиль");

        Action action = new StyledEditorKit.BoldAction();
        action.putValue(Action.NAME, "Жирный");
        menu.add(action);

        action = new StyledEditorKit.ItalicAction();
        action.putValue(Action.NAME, "Курсив");
        menu.add(action);

        action = new StyledEditorKit.UnderlineAction();
        action.putValue(Action.NAME, "Подчеркнутый");
        menu.add(action);

        menu.addSeparator();

        menu.add(new StyledEditorKit.FontSizeAction("12", 12));
        menu.add(new StyledEditorKit.FontSizeAction("14", 14));
        menu.add(new StyledEditorKit.FontSizeAction("18", 18));

        menu.addSeparator();

        menu.add(new StyledEditorKit.FontFamilyAction("Serif",
                                                      "Serif"));
        menu.add(new StyledEditorKit.FontFamilyAction("SansSerif",
                                                      "SansSerif"));

        menu.addSeparator();

        menu.add(new StyledEditorKit.ForegroundAction("Красный ",
                                                      Color.red));
        menu.add(new StyledEditorKit.ForegroundAction("Зеленый",
                                                      Color.green));
        menu.add(new StyledEditorKit.ForegroundAction("Синий",
                                                      Color.blue));
        menu.add(new StyledEditorKit.ForegroundAction("Черный",
                                                      Color.black));

        return menu;
    }
  
    // infoMenu
    protected JMenu createInfoMenu() {
        JMenu menu = new JMenu("Информация");

        Action docProperties = new AbstractAction (){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				JTextArea titleField 	= new JTextArea(1,30); 	titleField.setLineWrap(true);		
				JTextArea expertField 	= new JTextArea(2,30);	expertField.setLineWrap(true);
				JTextArea clientField 	= new JTextArea(1,30);	clientField.setLineWrap(true);
				JTextArea dateField 	= new JTextArea(1,30);	dateField.setLineWrap(true);
				JTextArea commentArea 	= new JTextArea(5,30);	commentArea.setLineWrap(true);
				
				JLabel lt = new JLabel("Название:" );  		lt.setPreferredSize(new Dimension(100, 40));
				JLabel le = new JLabel("Эксперт(ы):" ); 	le.setPreferredSize(new Dimension(100, 40));
				JLabel lc = new JLabel("Типируемый:" ); 	lc.setPreferredSize(new Dimension(100, 40));
				JLabel ld = new JLabel("Дата:" );  			ld.setPreferredSize(new Dimension(100, 40));
				JLabel lcm = new JLabel("Примечание:" );	lcm.setPreferredSize(new Dimension(100, 40));
				
				
				
				

				
				Panel pt = new Panel();
				Panel pe = new Panel(); 
				Panel pc = new Panel(); 
				Panel pd = new Panel(); 
				Panel ppc = new Panel(); 
                Panel panel = new Panel(); 
                //BoxLayout layout  = new BoxLayout (panel,BoxLayout.Y_AXIS);
                panel.setLayout(new BoxLayout (panel,BoxLayout.Y_AXIS));
                
               	pt.add(lt);
               	pt.setMinimumSize(new Dimension(500, 40));
				pt.add(new JScrollPane(titleField));
				pe.add(le);
				pe.setMinimumSize(new Dimension(500, 50));
				pe.add(new JScrollPane(expertField));
				pc.add(lc);
				pc.setMinimumSize(new Dimension(500, 40));
				pc.add(new JScrollPane(clientField));
				pd.add(ld);
				pd.setMinimumSize(new Dimension(500, 40));
				pd.add(new JScrollPane(dateField));
				ppc.add(lcm);
				ppc.setMinimumSize(new Dimension(500, 70));
				ppc.add(new JScrollPane(commentArea)); 
				
				String title	= (String) aDoc.getProperty(ADocument.TitleProperty);
				String expert 	= (String) aDoc.getProperty(ADocument.ExpertProperty);
				String client 	= (String) aDoc.getProperty(ADocument.ClientProperty);
				String date 	= (String) aDoc.getProperty(ADocument.DateProperty);
				String comment 	= (String) aDoc.getProperty(ADocument.CommentProperty);
				
				if (title	!=null) 	titleField.setText	(title	);
				if (expert 	!=null) 	expertField.setText	(expert );
				if (client 	!=null) 	clientField.setText	(client );
				if (date 	!=null) 	dateField.setText	(date 	);
				if (comment	!=null) 	commentArea.setText	(comment);
				
				panel.add(pt);
				panel.add(pe);
				panel.add(pc);
				panel.add(pd);
				panel.add(ppc);
				//panel.add(new Panel());
				
				if(JOptionPane.showOptionDialog(frame, 
						panel, 
						"Информация о документе", 
						JOptionPane.YES_NO_OPTION,
						JOptionPane.INFORMATION_MESSAGE,	
						null,
						new Object[]{"Принять", "Отмена"},
						null
							) == JOptionPane.YES_OPTION){
					
					    	title  	= titleField.getText(); 
					    	expert	= expertField.getText();
					    	client 	= clientField.getText();
					    	date	= dateField.getText();
					    	comment = commentArea.getText();
					    	
					    	Dictionary <Object, Object> properties = aDoc.getDocumentProperties();

					    	
					    	properties.put(ADocument.TitleProperty, 	title);
					    	properties.put(ADocument.ClientProperty, 	client);
					    	properties.put(ADocument.ExpertProperty, 	expert);
					    	properties.put(ADocument.DateProperty, 		date);
					    	properties.put(ADocument.CommentProperty, 	comment);
					    	aDoc.fireADocumentChanged();
					    
				}
							;			
				
			}
        }; 
        
        Action about = new AbstractAction(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				
			JOptionPane.showOptionDialog(frame, 
					"Программа \"Информационный Анализ\"\n"+
					"(с) Виктор Пятницкий 2010 г. \n\n"+
					"Школа системной соционики, Киев\n\n"+
					"Версия \"Альфа\"", "О программе", 
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

    protected void initDocument() {
       
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
                System.out.println("Unable to undo: " + ex);
                ex.printStackTrace();
            }
            updateUndoState();
            redoAction.updateRedoState();
        }

        protected void updateUndoState() {
            if (undo.canUndo()) {
                setEnabled(true);
                putValue(Action.NAME, undo.getUndoPresentationName());
            } else {
                setEnabled(false);
                putValue(Action.NAME, "Undo");
            }
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
            updateRedoState();
            undoAction.updateUndoState();
        }

        protected void updateRedoState() {
            if (undo.canRedo()) {
                setEnabled(true);
                putValue(Action.NAME, undo.getRedoPresentationName());
            } else {
                setEnabled(false);
                putValue(Action.NAME, "Redo");
            }
        }
    }

    /* @INFO  Class to hold the UI Mental - Vital controls
    */
  

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        final Analyst frame = new Analyst();
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);


        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    //The standard main method.
    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //Turn off metal's use of bold fonts
	        UIManager.put("swing.boldMetal", Boolean.FALSE);
		createAndShowGUI();
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
		if (saveConfirmation()!=JOptionPane.CANCEL_OPTION);	
		System.exit(0);
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
	
	

private int saveConfirmation(){
	int choice = JOptionPane.YES_OPTION;
	// if existing document is not empty
	if (aDoc.getLength()>0){
		choice = JOptionPane.showOptionDialog(frame, 
					"Текущий документ не пустой.\n\nСохранить текущий документ?", 
					"Требуется подтверждение", 
					JOptionPane.YES_NO_CANCEL_OPTION, 
					JOptionPane.ERROR_MESSAGE, 
					null, 
					new Object[]{"Сохранить", "Не сохранять", "Отмена"}, 
					null);
		if (choice == JOptionPane.YES_OPTION){	
			File file = null;
	    	
			if (fileName == null ||(fileName!=null && fileName.length() == 0)) {	
	    		 fc.setDialogTitle("Сохранение документа");
		    	 int returnVal = fc.showDialog(Analyst.this, "Сохранить");
		         if (returnVal == JFileChooser.APPROVE_OPTION) file = fc.getSelectedFile();
       
		         }
				

				if (fileName!= null & fileName.length()>0){
						    try {
						    	 
						    	 file =  new File(fileName);
						    		 
						    	 if (file!=null){	 
							         	FileOutputStream fos = new FileOutputStream(file); 
										aDoc.save(fos);
							    	 }		
				
								        
								 } catch (IOException e) {
								// 
								System.out.println("Error writing document to file: ");
								e.printStackTrace();
								JOptionPane.showOptionDialog(frame, 
										"Ошибка сохранения файла: "+fileName + "\n\n" +e.getMessage(), 
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

public int docDeleteConfirmation(){
	int choice = JOptionPane.YES_OPTION;
	int offset = textPane.getCaret().getMark();
	if (aDoc.getASectionThatStartsAt(offset)!=null)
	choice = JOptionPane.showOptionDialog(frame, 
			"Вы собираетесь удалить размеченный фрагмент документа\n\nВы действительно хотите удалить фрагмент?", 
			"Подтверждение удаления", 
			JOptionPane.YES_NO_OPTION, 
			JOptionPane.INFORMATION_MESSAGE, 
			null, 
			new Object[]{"Удалить", "Не удалять"}, 
			"Не удалять");
	
	if (choice == JOptionPane.YES_OPTION) aDoc.removeCleanup(offset);
		else {
			choice = JOptionPane.NO_OPTION;			
		}
	return choice;
}

public class MyEventQueue extends EventQueue{ 
    protected void dispatchEvent(AWTEvent event){ 
        super.dispatchEvent(event); 
 
        // interested only in mouseevents 
        if(!(event instanceof MouseEvent)) 
            return; 
 
        MouseEvent me = (MouseEvent)event; 
 
        // interested only in popuptriggers 
        if(!me.isPopupTrigger()) 
            return; 
 
        // me.getComponent(...) retunrs the heavy weight component on which event occured 
        Component comp = SwingUtilities.getDeepestComponentAt(me.getComponent(), me.getX(), me.getY()); 
       
        // interested only in textcomponents 
        if(!(comp instanceof JTextPane)) 
            return; 
 
        // no popup shown by user code 
        if(MenuSelectionManager.defaultManager().getSelectedPath().length>0) 
            return; 
 
        // create popup menu and show 
 
        Point pt = SwingUtilities.convertPoint(me.getComponent(), me.getPoint(), textPane);
        popupMenu.show(textPane, pt.x, pt.y);
    } 
}

}//end class Analyst

