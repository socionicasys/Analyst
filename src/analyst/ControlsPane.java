/**
 * 
 */
package analyst;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.tree.DefaultMutableTreeNode;



import analyst.ADocument.ASection;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;


/**
 * @author Виктор
 *
 */
public class ControlsPane extends JToolBar  implements 	CaretListener, 
														ADataChangeListener, 
														ChangeListener, 
														TreeSelectionListener {
	
	AspectPanel aspectPanel;
	SignPanel signPanel;
	MVPanel mvPanel;
	DimensionPanel dimensionPanel;
	JTextPane textPane;
	ADocument aDoc = null;
	Vector <ADataChangeListener> aDataListeners;
	ADocument.ASection currentASection = null;
	JTextArea commentField;
	private Object oldTreeObject = null;
	
	
	//constructor
	public ControlsPane(){
        super("Панель разметки", JToolBar.VERTICAL);
        
        //setOrientation(JToolBar.VERTICAL);
		aDataListeners = new Vector <ADataChangeListener>();
		signPanel = new SignPanel();
		mvPanel = new MVPanel();        
        dimensionPanel = new DimensionPanel();      
        aspectPanel = new AspectPanel();
        
        aspectPanel.addAspectSelectionListener(signPanel);
        aspectPanel.addAspectSelectionListener(mvPanel);
        aspectPanel.addAspectSelectionListener(dimensionPanel); 
       
                
 //       JPanel controlsPane = new JPanel();
 //     setMinimumSize(new Dimension (150,600));
        Panel dummy = new Panel();
        JPanel container = new JPanel();
        container. setMinimumSize(new Dimension (200,500));
        GridBagLayout gbLayout = new GridBagLayout();
        GridBagConstraints gbc =  new GridBagConstraints();
        JScrollPane scrl = new JScrollPane(container);
        //scrl.setMinimumSize(new Dimension((int)container.getMinimumSize().getWidth()+ 20,
        //									(int)container.getMinimumSize().getHeight()+20));
        
        scrl.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
     
        gbc.gridwidth = GridBagConstraints.REMAINDER ;
        gbc.insets = new Insets(2,2,2,2);
        container.add(aspectPanel); //,gbc);
        container.add(signPanel) ; //,gbc);
        container.add(dimensionPanel); //,gbc);
        container.add(mvPanel); //,gbc);  
        //gbc.weighty = 1.0;
        //container.add(dummy); //,gbc);
        
        //add(scrl);
        
        add(container);
 
        mvPanel.setPanelEnabled(false);    
        dimensionPanel.setPanelEnabled(false);		
        signPanel.setPanelEnabled(false);	
		
        aspectPanel.setPanelEnabled(false);
        
	}// constructor ControlsPane()
	
    public interface AspectSelectionListener {
        
    	public void setPanelEnabled(boolean enabled);
    }//interface
 
        private class MVPanel extends JPanel implements ActionListener, AspectSelectionListener {

        private JRadioButton vitalButton; 
        private JRadioButton mentalButton; 
        private JRadioButton superidButton; 
        private JRadioButton superegoButton; 
        private ButtonGroup mvButtonGroup;
        private JButton clearMVSelection;
        
  
        public MVPanel(){
        	super();
        	vitalButton = new JRadioButton("Витал");	
        	mentalButton = new JRadioButton("Ментал");	
        	superidButton = new JRadioButton("Супер-ИД");	
        	superegoButton = new JRadioButton("Супер-ЭГО");	
        	
        	vitalButton.addActionListener(this); 	vitalButton.setActionCommand(AData.VITAL);
        	mentalButton.addActionListener(this);	mentalButton.setActionCommand(AData.MENTAL);
        	superidButton.addActionListener(this);	superidButton.setActionCommand(AData.SUPERID);
        	superegoButton.addActionListener(this);	superegoButton.setActionCommand(AData.SUPEREGO);       	
        	
        	mvButtonGroup = new ButtonGroup();
        	mvButtonGroup.clearSelection();
        	clearMVSelection = new JButton("Очистить");
        	     	
        	mvButtonGroup.add(vitalButton);		
        	mvButtonGroup.add(mentalButton);
        	mvButtonGroup.add(superidButton);		
        	mvButtonGroup.add(superegoButton);
        	
        	mvButtonGroup.clearSelection();
        	clearMVSelection.addActionListener(this);
        	
        	Panel pp1 = new Panel(); 
        	Panel pp2 = new Panel(); 
        	Panel pp = new Panel(); 
        	//setMinimumSize(new Dimension(100,110));
        	pp.setMinimumSize(new Dimension(200,120));
        	//pp.setPreferredSize(new Dimension(100,100));
        	setMinimumSize(new Dimension(200,120));
        	setMaximumSize(new Dimension(200,120));
        	
        pp1.setLayout(new BoxLayout(pp1,BoxLayout.Y_AXIS));
        pp2.setLayout(new BoxLayout(pp2,BoxLayout.Y_AXIS));
        pp1.add(vitalButton);
        pp1.add(mentalButton);
        pp2.add(superidButton);
        pp2.add(superegoButton);
        //pp.add(new JPanel());
        pp.setLayout(new BoxLayout(pp,BoxLayout.X_AXIS));
        
        pp.add(pp1);
        pp.add(pp2);
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); 
        
        add(pp);
        add(clearMVSelection);
        setBorder(new TitledBorder ("Ментал/Витал"));
  
        } //constructor MVPanel
        
        public void actionPerformed(ActionEvent e){
        	if (e.getSource().equals(clearMVSelection)){ 
        		mvButtonGroup.clearSelection();
        		clearMVSelection.setEnabled(false);
        	} else {
        		clearMVSelection.setEnabled(true);
        	}
        	
        	fireADataChanged();
        }   
        public void setPanelEnabled(boolean enabled){
        	if (!enabled){
        		mvButtonGroup.clearSelection();
        		clearMVSelection.setEnabled(false);
        	}
        	vitalButton.setEnabled(enabled);	
        	mentalButton.setEnabled(enabled);
        	superegoButton.setEnabled(enabled);
        	superidButton.setEnabled(enabled);
        }
        
        public String getMVSelection(){
        	ButtonModel bm = mvButtonGroup.getSelection();
        	if (bm == null) return null;
        	return bm.getActionCommand();	 
        }

		public void setMV(String mv) {
			if (mv == null) mvButtonGroup.clearSelection();
			else 
			if(mv.equals(AData.MENTAL)) mvButtonGroup.setSelected(mentalButton.getModel(), true);
			else
			if(mv.equals(AData.VITAL)) mvButtonGroup.setSelected(vitalButton.getModel(), true);
			else
			if(mv.equals(AData.SUPEREGO)) mvButtonGroup.setSelected(superegoButton.getModel(), true);
			else
			if(mv.equals(AData.SUPERID)) mvButtonGroup.setSelected(superidButton.getModel(), true);
			
			if (mvButtonGroup.getSelection()!=null) clearMVSelection.setEnabled(true);
			 else  clearMVSelection.setEnabled(false);
		}
        
        
        } //class MVPanel
        
        private class SignPanel extends JPanel implements ActionListener, AspectSelectionListener {

            private JRadioButton plusButton; 
            private JRadioButton minusButton; 
            private ButtonGroup signButtonGroup;
            private JButton clearSignSelection;
            
      
            public SignPanel(){
            	super();
            	            	
            	plusButton = new JRadioButton("+");	
            	minusButton = new JRadioButton("-");	
            	plusButton.addActionListener(this); 	plusButton.setActionCommand(AData.PLUS);
            	minusButton.addActionListener(this);	minusButton.setActionCommand(AData.MINUS);
            	signButtonGroup = new ButtonGroup();
            	signButtonGroup.clearSelection();
            	clearSignSelection = new JButton("Очистить");
            	     	
            	signButtonGroup.add(plusButton);		
            	signButtonGroup.add(minusButton);	
            	signButtonGroup.clearSelection();
            	clearSignSelection.addActionListener(this);
            	
            	Panel pp = new Panel(); 
            	//pp.setMinimumSize(new Dimension(100,50));
            	pp.setMaximumSize(new Dimension(100,50));
            	pp.setPreferredSize(new Dimension(100,50));
            	
            	setMinimumSize(new Dimension(200,80));
            	setPreferredSize(new Dimension(200,80));
            	setMaximumSize(new Dimension(200,80));
            	
            pp.setLayout(new BoxLayout(pp,BoxLayout.Y_AXIS));
            pp.add(plusButton);
            pp.add(minusButton);
            
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS)); 
            
            add(pp);
            add(clearSignSelection);
            setBorder(new TitledBorder("Знак"));
           
      
            } //constructor MVPanel
            
            public void actionPerformed(ActionEvent e){
            	if (e.getSource().equals(clearSignSelection)){ 
            		signButtonGroup.clearSelection();
            		clearSignSelection.setEnabled(false);
            	} else {
            		clearSignSelection.setEnabled(true);
            	}
            	fireADataChanged();
            	
            }   
            public void setPanelEnabled(boolean enabled){
            	if (!enabled){
            		signButtonGroup.clearSelection();
            		clearSignSelection.setEnabled(false);
            	}
            	plusButton.setEnabled(enabled);	
            	minusButton.setEnabled(enabled);
            }
            
            public String getSignSelection(){
            	ButtonModel bm = signButtonGroup.getSelection();
            	if (bm == null) return null;
            	return bm.getActionCommand();	 
            }

			public void setSign(String sign) {
				if (sign == null) signButtonGroup.clearSelection();
				else
				if(sign.equals(AData.PLUS)) signButtonGroup.setSelected(plusButton.getModel(), true);
				else
				if(sign.equals(AData.MINUS)) signButtonGroup.setSelected(minusButton.getModel(), true);
				
				if (signButtonGroup.getSelection()!=null) clearSignSelection.setEnabled(true);
				else clearSignSelection.setEnabled(false);
			}
            
            
            } //class SignPanel       
        
        private class AspectPanel extends JPanel implements ActionListener, ItemListener {

            private JRadioButton l,p,i,t,s,f,r,e,d; 
            private JRadioButton l2,p2,i2,t2,s2,f2,r2,e2,d2; 
            private JRadioButton aspect, block, jump;
            private ButtonGroup aspectGroup, secondAspectGroup, controlGroup; 
            private JButton clearAspectSelection;
            private Vector <AspectSelectionListener> actionListeners = new Vector <AspectSelectionListener>();
            boolean aspectPanelEnabled= false;
            
        public AspectPanel (){
        super();
        l = new JRadioButton("БЛ"); l.addItemListener(this); 	l.setActionCommand(AData.L);
        p = new JRadioButton("ЧЛ"); p.addItemListener(this);	p.setActionCommand(AData.P);
        i = new JRadioButton("ЧИ"); i.addItemListener(this); 	i.setActionCommand(AData.I);
        t = new JRadioButton("БИ"); t.addItemListener(this);	t.setActionCommand(AData.T);
        s = new JRadioButton("БС"); s.addItemListener(this);	s.setActionCommand(AData.S);
        f = new JRadioButton("ЧС"); f.addItemListener(this);	f.setActionCommand(AData.F);
        r = new JRadioButton("БЭ"); r.addItemListener(this);	r.setActionCommand(AData.R);
        e = new JRadioButton("ЧЭ"); e.addItemListener(this);	e.setActionCommand(AData.E);
        
        l2 = new JRadioButton("БЛ"); l2.addItemListener(this);  l2.setActionCommand(AData.L);
        p2 = new JRadioButton("ЧЛ"); p2.addItemListener(this);	p2.setActionCommand(AData.P);
        i2 = new JRadioButton("ЧИ"); i2.addItemListener(this);  i2.setActionCommand(AData.I);
        t2 = new JRadioButton("БИ"); t2.addItemListener(this);	t2.setActionCommand(AData.T);
        s2 = new JRadioButton("БС"); s2.addItemListener(this);	s2.setActionCommand(AData.S);
        f2 = new JRadioButton("ЧС"); f2.addItemListener(this);	f2.setActionCommand(AData.F);
        r2 = new JRadioButton("БЭ"); r2.addItemListener(this);	r2.setActionCommand(AData.R);
        e2 = new JRadioButton("ЧЭ"); e2.addItemListener(this);	e2.setActionCommand(AData.E);
                                                                                             
        aspect 	= new JRadioButton("Аспект"); 	aspect.addItemListener(this);	aspect.setActionCommand("aspect");
        block 	= new JRadioButton("Блок");		block.addItemListener(this);	block.setActionCommand("block");
        jump 	= new JRadioButton("Перевод"); 	jump.addItemListener(this);		jump.setActionCommand("jump");
        
        d = new JRadioButton("???");  d.getModel().addItemListener(this); 	d.setActionCommand(AData.DOUBT);
        
        aspectGroup = new ButtonGroup();
        aspectGroup.add(l);	l.addActionListener(this);
        aspectGroup.add(p);	p.addActionListener(this);
        aspectGroup.add(r);	r.addActionListener(this);
        aspectGroup.add(e);	e.addActionListener(this);
        aspectGroup.add(s);	s.addActionListener(this);
        aspectGroup.add(f);	f.addActionListener(this);
        aspectGroup.add(t);	t.addActionListener(this);
        aspectGroup.add(i);	i.addActionListener(this);
        aspectGroup.add(d);	d.addActionListener(this);
        
        secondAspectGroup = new ButtonGroup();
        secondAspectGroup.add(l2);	l2.addActionListener(this);
        secondAspectGroup.add(p2);	p2.addActionListener(this);
        secondAspectGroup.add(r2);	r2.addActionListener(this);
        secondAspectGroup.add(e2);	e2.addActionListener(this);
        secondAspectGroup.add(s2);	s2.addActionListener(this);
        secondAspectGroup.add(f2);	f2.addActionListener(this);
        secondAspectGroup.add(t2);	t2.addActionListener(this);
        secondAspectGroup.add(i2);	i2.addActionListener(this);
     
     
        controlGroup = new ButtonGroup();
        controlGroup.add(aspect);	aspect.addActionListener(this);
        controlGroup.add(block);	block.addActionListener(this);
        controlGroup.add(jump);	jump.addActionListener(this);
       
        clearAspectSelection  = new JButton("Очистить");
        
       setMinimumSize(new Dimension(200, 270)); 
       setMaximumSize(new Dimension(200, 270));
       Panel pAspect = new Panel(); 
       pAspect.setLayout(new BoxLayout(pAspect, BoxLayout.Y_AXIS));
       pAspect.setPreferredSize(new Dimension(50, 200));
       pAspect.setMinimumSize(new Dimension(50, 200));
      
       pAspect.add(l); 
       pAspect.add(p); 
       pAspect.add(r); 
       pAspect.add(e); 
       pAspect.add(s); 
       pAspect.add(f); 
       pAspect.add(t); 
       pAspect.add(i); 
       //pAspect.add(d); 
       
       Panel pAspect2 = new Panel(); 
       pAspect2.setLayout(new BoxLayout(pAspect2, BoxLayout.Y_AXIS));
       pAspect2.setPreferredSize(new Dimension(50, 200));
       pAspect2.setMinimumSize(new Dimension(50, 200));
       pAspect2.add(l2); 
       pAspect2.add(p2); 
       pAspect2.add(r2); 
       pAspect2.add(e2); 
       pAspect2.add(s2); 
       pAspect2.add(f2); 
       pAspect2.add(t2); 
       pAspect2.add(i2); 
     
       Panel pControl = new Panel(); 
       pControl.setLayout(new BoxLayout(pControl, BoxLayout.X_AXIS));
       pControl.setPreferredSize(new Dimension(50, 40));
       pControl.setMinimumSize(new Dimension(50, 40));
       pControl.add(aspect); 
       pControl.add(block); 
       pControl.add(jump); 
      
       Panel pA = new Panel(); 
       pA.setLayout(new BoxLayout(pA, BoxLayout.X_AXIS));
       
       pA.add(pAspect);
       pA.add(pAspect2);
       
       Panel pB = new Panel(); 
       pB.setLayout(new BoxLayout(pB, BoxLayout.Y_AXIS));
       
       pB.add (new Panel());
       pB.add(clearAspectSelection);
       pB.add (new Panel());
       pB.add(d);
       pB.add(new Panel());
       
       setLayout(new BorderLayout());
       add(pControl,BorderLayout.NORTH);
       add(pA, BorderLayout.WEST);
       add(pB, BorderLayout.EAST);
      
       setBorder(new TitledBorder ("Аспект/Блок"));

       aspectGroup.clearSelection();
       secondAspectGroup.clearSelection();
       controlGroup.clearSelection();
       
 
       clearAspectSelection.addActionListener(this);
       clearAspectSelection.setEnabled(false);  
      
        }    //constructor
        
        public void addAspectSelectionListener(AspectSelectionListener asl){
        actionListeners.add(asl);
        informListeners(isAspectSelected());
        }
        
        protected void informListeners(boolean selected){
        	for (int i=0; i<actionListeners.size(); i++){
        		actionListeners.get(i).setPanelEnabled(selected);
        	}
        	
    		if (selected) {if(!commentField.isEditable()) commentField.setEditable(true);}
			else 
				if (commentField!=null){commentField.setText(""); 
				    commentField.setEditable(false);
			}

        }
        
        public void actionPerformed(ActionEvent ev){
            Object source = ev.getSource();
        	if (source.equals(d) ||
        		source.equals(l) ||
        		source.equals(p) ||
        		source.equals(r) ||
        		source.equals(e) ||
        		source.equals(s) || 
        		source.equals(f) ||
        		source.equals(t) ||
        		source.equals(i) 
        							)secondAspectGroup.clearSelection(); 
        	
         	if (source.equals(clearAspectSelection)) { 
        		aspectGroup.clearSelection();
        		secondAspectGroup.clearSelection();
        		setSecondAspectGroupEnabled(false);
        		controlGroup.setSelected(aspect.getModel(), true);        		
        		clearAspectSelection.setEnabled(false);
        		informListeners(false);
        		}else 
        			if (source.equals(jump)){
        			  secondAspectGroup.clearSelection();
        			  setSecondAspectGroupEnabled(false);
               		}else 
            			if (source.equals(aspect)){
            			  secondAspectGroup.clearSelection();
            			  setSecondAspectGroupEnabled(false);
            			  //aspectGroup.clearSelection();
            			  setAspectGroupEnabled(true);
                   		}else 
                			if (source.equals(block)){
                			  secondAspectGroup.clearSelection();
                			  setSecondAspectGroupEnabled(false);
                			  
                			 // setPanelEnabled(true);
            		}        	
        			else{
        			clearAspectSelection.setEnabled(true);
        			}
        	if (block.isSelected()&& block.isEnabled()){
        	  ButtonModel bm = aspectGroup.getSelection();
  			  if (bm!=null) setSecondAspectForBlock(bm.getActionCommand());
        	}
        	
        	if (jump.isSelected()&& jump.isEnabled()){
          	  ButtonModel bm = aspectGroup.getSelection();
    			  if (bm!=null) setSecondAspectForJump(bm.getActionCommand());
          	}
        	
        	 fireADataChanged();		        	
        }
        
  
		private void setSecondAspectGroupEnabled(boolean enabled) {
			
			l2.setEnabled(enabled);	
        	p2.setEnabled(enabled);
        	s2.setEnabled(enabled);
        	f2.setEnabled(enabled);
        	i2.setEnabled(enabled);
        	t2.setEnabled(enabled);
        	e2.setEnabled(enabled);
        	r2.setEnabled(enabled);

			
		}
        
       private void setSecondAspectForBlock(String firstAspect) {
    	
    	if (firstAspect!= null){   
    	setSecondAspectGroupEnabled(false);
             	
       	if (firstAspect.equals(AData.L)){
       		f2.setEnabled(true);
       		i2.setEnabled(true);
       	} else
       		if(firstAspect.equals(AData.P)) {
       			s2.setEnabled(true); 
       			t2.setEnabled(true);
       		} else
		  		if(firstAspect.equals(AData.R)) {
		   			f2.setEnabled(true); 
		   			i2.setEnabled(true);
	       		} else
			  		if(firstAspect.equals(AData.E)) {
			   			s2.setEnabled(true); 
			   			t2.setEnabled(true);
		       		} else
				  		if(firstAspect.equals(AData.S)) {
				   			p2.setEnabled(true); 
				   			e2.setEnabled(true);
			       		} else
					  		if(firstAspect.equals(AData.F)) {
					   			l2.setEnabled(true); 
					   			r2.setEnabled(true);
				       		} else
						  		if(firstAspect.equals(AData.T)) {
						   			p2.setEnabled(true); 
						   			e2.setEnabled(true);
					       		} else
							  		if(firstAspect.equals(AData.I)) {
							   			l2.setEnabled(true); 
							   			r2.setEnabled(true);
							   		}
    	}
		}    //
       
       private void setSecondAspectForJump(String firstAspect) {
       	
       	if (firstAspect!= null){   
       	//setSecondAspectGroupEnabled(false);
       	setSecondAspectGroupEnabled(true);
       	
          	if (firstAspect.equals(AData.L)){
          		l2.setEnabled(false);
          	} else
          		if(firstAspect.equals(AData.P)) {
              		p2.setEnabled(false);
          		} else
   		  		if(firstAspect.equals(AData.R)) {
	          		r2.setEnabled(false);
   	       		} else
   			  		if(firstAspect.equals(AData.E)) {
	   	          		e2.setEnabled(false);
   		       		} else
   				  		if(firstAspect.equals(AData.S)) {
   				   			s2.setEnabled(false);
   			       		} else
   					  		if(firstAspect.equals(AData.F)) {
   					   			f2.setEnabled(false);
   				       		} else
   						  		if(firstAspect.equals(AData.T)) {
   						   			t2.setEnabled(false);
   					       		} else
   							  		if(firstAspect.equals(AData.I)) {
    							   			i2.setEnabled(false);
   							   		}
       	}
   		}    //

		public boolean isAspectSelected(){
        	if (aspectGroup.getSelection()!= null)
        			//&& !aspectGroup.getSelection().getActionCommand().equals(AData.DOUBT)) 
        			return true;
        	return 	false;
        }
        
        
        public void setPanelEnabled(boolean enabled){
        	aspectPanelEnabled = enabled;
        	if (!enabled){
        		aspectGroup.clearSelection();
        		secondAspectGroup.clearSelection();
        		controlGroup.clearSelection();
        		setAspectGroupEnabled(false);
        		setSecondAspectGroupEnabled(false);
        		setControlGroupEnabled(false);
        		clearAspectSelection.setEnabled(false);
        		informListeners(enabled);
        	} else {
        		 setControlGroupEnabled(true);
        		 controlGroup.setSelected(aspect.getModel(), true);
        		 aspectGroup.clearSelection();
        		 setAspectGroupEnabled(true);        		 
        	}
       }

        private void setAspectGroupEnabled(boolean enabled){
           	l.setEnabled(enabled);	
        	p.setEnabled(enabled);
        	s.setEnabled(enabled);
        	f.setEnabled(enabled);
        	i.setEnabled(enabled);
        	t.setEnabled(enabled);
        	e.setEnabled(enabled);
        	r.setEnabled(enabled);
        	d.setEnabled(enabled);
        }
        
        private void setControlGroupEnabled(boolean enabled){
        	aspect.setEnabled(enabled);
        	block.setEnabled(enabled);
        	jump.setEnabled(enabled);
        }        
        
        public String getAspectSelection(){
        	String res = "";
        	ButtonModel bma =  aspectGroup.getSelection();
        	ButtonModel bma2 = secondAspectGroup.getSelection();      	
         	
        	if (bma == null) return null;
        	
        	res+=bma.getActionCommand();
        	
        	if (bma2!=null){
		        	if (block.isSelected()) res += AData.BLOCK_TOKEN+bma2.getActionCommand();
		        		else
		        			if (jump.isSelected()) res += AData.JUMP_TOKEN+bma2.getActionCommand();
		        	}
         return res;	        	
        }
        
		@Override
		public void itemStateChanged(ItemEvent arg0) {
		//извещение от поля комментария	
		 informListeners(isAspectSelected());			
		}

		public void setAspect(AData data) {
			if (data == null) return;
			String aspect = data.getAspect();
			
			if (aspect == null){//setAspectGroupEnabled(true);
								aspectGroup.clearSelection(); secondAspectGroup.clearSelection();
								this.aspect.getModel().setSelected(true);
								}
			else
			if(aspect.equals(AData.L)) {l.getModel().setSelected(true); }
			else
			if(aspect.equals(AData.P)) {p.getModel().setSelected(true); }
			else
			if(aspect.equals(AData.R)) {r.getModel().setSelected(true); }
			else
			if(aspect.equals(AData.E)) {e.getModel().setSelected(true); }
			else
			if(aspect.equals(AData.S)) {s.getModel().setSelected(true); }
			else
			if(aspect.equals(AData.F)) {f.getModel().setSelected(true); }
			else
			if(aspect.equals(AData.T)) {t.getModel().setSelected(true); }
			else
			if(aspect.equals(AData.I)) {i.getModel().setSelected(true); }
			else
			if(aspect.equals(AData.DOUBT)) {d.getModel().setSelected(true); }
			
			
			String modifier = data.getModifier();
			String secondAspect = data.getSecondAspect();
			
			if (modifier!=null){
				if (modifier.equals(AData.BLOCK)){ 	block.getModel().setSelected(true);
													setSecondAspectForBlock(aspect);
												 }
					else 
						if (modifier.equals(AData.JUMP)) {	jump.getModel().setSelected(true);
															setSecondAspectForJump(aspect);
														 }
						
	
			
				
		
					if (secondAspect == null) {secondAspectGroup.clearSelection();}
					else
					if(secondAspect.equals(AData.L)) {l2.getModel().setSelected(true); }
					else
					if(secondAspect.equals(AData.P)) {p2.getModel().setSelected(true); }
					else
					if(secondAspect.equals(AData.R)) {r2.getModel().setSelected(true); }
					else
					if(secondAspect.equals(AData.E)) {e2.getModel().setSelected(true); }
					else
					if(secondAspect.equals(AData.S)) {s2.getModel().setSelected(true); }
					else
					if(secondAspect.equals(AData.F)) {f2.getModel().setSelected(true); }
					else
					if(secondAspect.equals(AData.T)) {t2.getModel().setSelected(true); }
					else
					if(secondAspect.equals(AData.I)) {i2.getModel().setSelected(true); }
			
			}
        			
			informListeners(isAspectSelected());
			clearAspectSelection.setEnabled(isAspectSelected());
	        
		}	
        } //class AspectPanel
        
        private class DimensionPanel extends JPanel implements ActionListener,  AspectSelectionListener {

            private JRadioButton d1,d2,d3,d4,malo,mnogo,odno,indi; 
            private ButtonGroup dimensionGroup;
            private JButton clearDimensionSelection;
        
        public DimensionPanel (){
        super();	
        d1 = new JRadioButton("Ex");		d1.setActionCommand(AData.D1);
        d2 = new JRadioButton("Nm");		d2.setActionCommand(AData.D2);
        d3 = new JRadioButton("St");		d3.setActionCommand(AData.D3);
        d4 = new JRadioButton("Tm");		d4.setActionCommand(AData.D4);
        odno = new JRadioButton("Одномерность");		odno.setActionCommand(AData.ODNOMERNOST);
        malo = new JRadioButton("Маломерность");		malo.setActionCommand(AData.MALOMERNOST);
        mnogo = new JRadioButton("Многомерность");		mnogo.setActionCommand(AData.MNOGOMERNOST);
        indi = new JRadioButton("Индивидуальность");	indi.setActionCommand(AData.INDIVIDUALNOST);
        
        dimensionGroup=new ButtonGroup();
        dimensionGroup.add(d1);	
        dimensionGroup.add(d2);
        dimensionGroup.add(d3);
        dimensionGroup.add(d4);
        dimensionGroup.add(odno);
        dimensionGroup.add(malo);
        dimensionGroup.add(mnogo);
        dimensionGroup.add(indi);
        dimensionGroup.clearSelection();
        clearDimensionSelection = new JButton("Очистить");      
        clearDimensionSelection.addActionListener(this);
        
 //       setLayout(new GridLayout(8,2));
       Panel p = new Panel();
       Panel p1 = new Panel();
       Panel p2 = new Panel();
       
       p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
       p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));       
       p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));
       
       //p.setPreferredSize(new Dimension(100,170));
       //p.setMaximumSize(new Dimension(100,170));
       setMinimumSize(new Dimension(200,170));
       setMaximumSize(new Dimension(200,170));
     
       p1.add(d1);		d1.addActionListener(this);
       p1.add(d2);  	d2.addActionListener(this);
       p1.add(d3); 		d3.addActionListener(this);
       p1.add(d4); 		d4.addActionListener(this);
       p2.add(indi); 	indi.addActionListener(this);
       p2.add(odno); 	odno.addActionListener(this);
       p2.add(malo); 	malo.addActionListener(this);
       p2.add(mnogo); 	mnogo.addActionListener(this);
       
       
       p.add(p1);
       p.add(p2);
       
       setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); 
       add(p);
       add(clearDimensionSelection);
       setBorder(new TitledBorder ("Размерность"));
      
        }    //constructor
        
        public void actionPerformed(ActionEvent e){
        	Object source = e.getSource();
        	if (source.equals(clearDimensionSelection)){ 
        		dimensionGroup.clearSelection();
        		clearDimensionSelection.setEnabled(false);
        	} else {
        		clearDimensionSelection.setEnabled(true);
     		}
        	fireADataChanged();
        }
         
        
       


		public void setPanelEnabled(boolean enabled){
        	if (!enabled) {
        		dimensionGroup.clearSelection();
        		clearDimensionSelection.setEnabled(false);
        	}
        	
        	d1.setEnabled(enabled);	
        	d2.setEnabled(enabled);
        	d3.setEnabled(enabled);
        	d4.setEnabled(enabled); 
        	odno.setEnabled(enabled);
        	mnogo.setEnabled(enabled);   
        	malo.setEnabled(enabled); 
        	indi.setEnabled(enabled); 
        	}

        public String getDimensionSelection(){
        	ButtonModel bm = dimensionGroup.getSelection();
        	if (bm == null) return null;
        	return bm.getActionCommand();	 
        }

		public void setDimension(String dimension) {
			if (dimension == null) dimensionGroup.clearSelection();
			else
			if(dimension.equals(AData.D1)) d1.setSelected(true);
			else
			if(dimension.equals(AData.D2)) d2.setSelected(true);
			else
			if(dimension.equals(AData.D3)) d3.setSelected(true);
			else
			if(dimension.equals(AData.D4)) d4.setSelected(true);
			else
			if(dimension.equals(AData.ODNOMERNOST)) odno.setSelected(true);
			else
			if(dimension.equals(AData.MALOMERNOST)) malo.setSelected(true);
			else
			if(dimension.equals(AData.MNOGOMERNOST)) mnogo.setSelected(true);
			else
				if(dimension.equals(AData.INDIVIDUALNOST)) indi.setSelected(true);

			if (dimensionGroup.getSelection()!=null) clearDimensionSelection.setEnabled(true);
				else clearDimensionSelection.setEnabled(false);
		}
        	
        }//class DimensionPanel
        
public void bindToTextPane(JTextPane textComponent, ADocument adoc, JTextArea commentField){
	this.textPane=textComponent;
	this.aDoc=adoc;
	this.commentField=commentField; 
	}
        
  public AData getAData(){
	AData adata=null;  
	try{
	 adata = AData.parceAData(	aspectPanel.getAspectSelection()+AData.SEPARATOR+
			 					signPanel.getSignSelection()+AData.SEPARATOR+
			 					dimensionPanel.getDimensionSelection()+AData.SEPARATOR+
			 					mvPanel.getMVSelection()+AData.SEPARATOR
			 					);
	 if(adata!=null) adata.setComment(commentField.getText());	 

	}  catch (AData.ADataException e) {
			//System.out.println("Exception in ControlsPane.gerData():");	
			//e.printStackTrace();		
	        }
	return adata;
  }


@Override
public void caretUpdate (CaretEvent e) {

	int dot = e.getDot();
	int mark = e.getMark();
	int begin = Math.min(dot, mark);
//	int length = Math.abs(dot-mark); 



	//try to find current ASection to edit
	
	currentASection = null;
	
	// if found mark the section with caret
	if((dot==mark) && (aDoc.getASection(begin)!=null)) {
		currentASection = aDoc.getASection(begin);
		dot = currentASection.getStartOffset();
		mark = currentASection.getEndOffset();	
		
		Caret c  = textPane.getCaret();
		textPane.removeCaretListener(this);
		
		
		c.setDot(mark);
		c.moveDot(dot);
		
		textPane.addCaretListener(this);
		commentField.getCaret().removeChangeListener(this);
		
		aspectPanel.setPanelEnabled(false);
		AData data = aDoc.getAData(currentASection);
    	aspectPanel.setPanelEnabled(true);
    	setContols(data);
    	
		commentField.getCaret().addChangeListener(this);
	}
	else
	if (dot!=mark){
		    currentASection = aDoc.getASectionThatStartsAt(begin);
		    if (currentASection!=null){ 
		    	AData data = aDoc.getAData(currentASection);
		    	setContols(data);
		    	dot = currentASection.getStartOffset();
				mark = currentASection.getEndOffset();	
				
				Caret c  = textPane.getCaret();
				textPane.removeCaretListener(this);
				
				c.setDot(mark);
				c.moveDot(dot);
				
				textPane.addCaretListener(this);
				setContols(data);
		    } else aspectPanel.setPanelEnabled(false);
			
			aspectPanel.setPanelEnabled(true);

	} else {aspectPanel.setPanelEnabled(false); }
	


}

protected void setContols(AData data){
	
	removeADataListener(this);
	
	if (data != null){

		aspectPanel.setAspect(data);
		dimensionPanel.setDimension(data.getDimension());
		mvPanel.setMV(data.getMV());
		signPanel.setSign(data.getSign());
		
		    //need this not to receive notification
			{
		//	commentField.getCaret().removeChangeListener(this);
			commentField.setText(data.getComment());
		//	commentField.getCaret().addChangeListener(this);
			}
		
	}	else {
		aspectPanel.setAspect(null);
		dimensionPanel.setDimension(null);
		mvPanel.setMV(null);
		signPanel.setSign(null);
		commentField.setText(null);
		
		}
	
	addADataListener(this);
} //setContols()

protected void addADataListener(ADataChangeListener l){
  aDataListeners.add(l);	
}

protected void removeADataListener(ADataChangeListener l){
	  aDataListeners. remove(l);	
	}

private void fireADataChanged() {
	if (aDataListeners.isEmpty())return;

	
	Caret caret = textPane.getCaret();
	//Document doc = textPane.getDocument();
	
	int dot = caret.getDot();
	int mark = caret.getMark();
	int begin = Math.min(dot, mark);
	int end = Math.max(dot, mark);
 
	AData d = getAData();
	
	for(int i=0; i<aDataListeners.size();i++){
		aDataListeners.get(i).aDataChanged( begin,  end, d);
	}	
}	

@Override
public void aDataChanged(int start, int end, AData data) {
	
	//currentASection = aDoc.getASectionThatStartsAt(start);
	if ((data == null) && (currentASection!=null)){
													aDoc.startCompoundEdit();
													aDoc.removeASection(currentASection); 
													aDoc.endCompoundEdit(null);
													currentASection = null;
													}
	 else
	    if ((data != null) && (currentASection!=null)){
	    											aDoc.startCompoundEdit();
	    											aDoc.updateASection(currentASection, data); 
	    											aDoc.endCompoundEdit(null);}
	    else 
	    	 if ((data != null) && (currentASection==null)){
	   		 
	    		 try {
					    currentASection = aDoc.new ASection( aDoc.createPosition(start), aDoc.createPosition(end));
					    currentASection.setAttributes(aDoc.defaultSectionAttributes);
					    aDoc.startCompoundEdit();
					    aDoc.addASection(currentASection, data);
					    aDoc.endCompoundEdit(null);
				 } catch (BadLocationException e) {
					System.out.println("Exception in ControlsPane.aDataChanged():2");
					e.printStackTrace();
				   }
			}
} // aDataChanged()	 

@Override
public void stateChanged(ChangeEvent e) {
	// Event from the comment field - just update changes to the document
  if ( aspectPanel.isAspectSelected())
							fireADataChanged();
}

@Override
public void valueChanged(TreeSelectionEvent e) {
	
	Object obj = ((DefaultMutableTreeNode)(e.getPath().getLastPathComponent())). getUserObject();
	
	if (obj.equals(oldTreeObject)) {
		//oldTreeObject=null;
		return;
	}
		else oldTreeObject = obj;
	
	String quote =null;
	int index=0;
	
	if (obj instanceof EndNodeObject ){
		quote	= ((EndNodeObject)obj).getString();
		index	 =((EndNodeObject)obj).getOffset();	
		
	}
	//String quote = e.getPath().getLastPathComponent().toString();
	else{	
		quote =  obj.toString();
		if (quote!=null && quote.startsWith("#") ){
			String indexStr = quote.substring(1,quote.indexOf("::") ) ;
			index = Integer.parseInt(indexStr);
		}
	}
		//////////////test for text positioning in scroll pane////////////////////////
		JViewport viewport = (JViewport) textPane.getParent();
		
		currentASection =	  aDoc.getASectionThatStartsAt(index);
		if (currentASection!=null) {
			int offset =  currentASection.getMiddleOffset();
			int start  =  currentASection.getStartOffset();
			int end    =  currentASection.getEndOffset();
			
			Rectangle rect;
			
			
			textPane.removeCaretListener(this);
			textPane.getCaret().setDot(end);
			textPane.getCaret().moveDot(start);			
			textPane.addCaretListener(this);
			
			commentField.getCaret().removeChangeListener(this);
			
			aspectPanel.setPanelEnabled(false);
			AData data = aDoc.getAData(currentASection);
	    	aspectPanel.setPanelEnabled(true);
	    	setContols(data);
	    	
			commentField.getCaret().addChangeListener(this);

//			textPane.getCaret().setDot(offset);			
			
			try {
				rect = textPane.modelToView(offset);
				//System.out.println("offset = "+offset+" rectangle = "+rect);
				viewport.scrollRectToVisible(rect);
				textPane.grabFocus();
				
			} catch (BadLocationException e1) {
				//
				System.out.println(" error setting model to view :: bad location");
			}
			
		} else ; //System.out.println(" Не найдена секция");
	   		////////////////////////////////////////////	
	
		
	

}
    
   public void update(){
	 if (currentASection!=null) { 
	 AData data = aDoc.getADataMap().get(currentASection);	 
	 setContols(data);  
	 }
   } 

}//class ControlsPane
