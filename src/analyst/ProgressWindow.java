/**
 * 
 */
package analyst;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.EventListener;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * @author Виктор
 *
 */
public class ProgressWindow extends JProgressBar implements PropertyChangeListener {
    
//	JProgressBar bar;
	private JDialog dialog;
    private Analyst an;
    private JLabel label;

//	private String message;
	static int minValue = 0;
	static int maxValue = 100;
	private int value=0;
	private Vector <ActionListener> listeners  = null;
	
	
	
	public ProgressWindow(Frame frame, String message){
		super(JProgressBar.HORIZONTAL, minValue, maxValue);
		this.an =(Analyst)frame;
		setMaximumSize(new Dimension(300,30));
		//setMinimumSize(new Dimension(300,30));
		setPreferredSize(new Dimension(300,30));
		
		JPanel p = new JPanel(new BorderLayout());
		JPanel pp = new JPanel(new BorderLayout());
		//p.setMinimumSize(new Dimension(300,200));
		//p.setLayout();
		label = new JLabel(message);
		p.add(label,BorderLayout.WEST);
		pp.setLayout(new BoxLayout(pp, BoxLayout.Y_AXIS));
		
		pp.add(new JPanel());
		pp.add(this);
		pp.add(new JPanel());
		
		p.add(pp, BorderLayout.CENTER);
		
		//p.setOpaque(true);
		
		
		
		dialog=new JDialog((Frame)null,"Подождите пожалуйста, выполняется операция...", false);
		dialog.setContentPane(p);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setSize(new Dimension(500, 100));
        dialog.setLocationRelativeTo(frame);
        dialog.setAlwaysOnTop(true);
        dialog.setResizable(false);
        dialog.setVisible(true);
        
		}


public void close(){
	dialog.setVisible(false);
	dialog.dispose();
	an.aDoc.fireADocumentChanged();
}

public Analyst getAnalyst(){
	return an;
}

public void addUserInterruptListener(ActionListener al){
	if (listeners==null ||(listeners!=null && listeners.isEmpty()) ) listeners  = new Vector<ActionListener>();
	listeners.add(al);
}

private void fireUserInterrupt(){
	
	if (listeners!=null)
		for (int i=0; i<listeners.size();i++){
			
			listeners.get(i).actionPerformed(new ActionEvent((Object)this, 0, "User Interrupt"));
		}
	
}

	@Override
	public void propertyChange(PropertyChangeEvent pce) {
		String propName = pce.getPropertyName();
		
		if (propName.equals("progress")){
			int value = ((Integer)pce.getNewValue()).intValue();
			if (value >= minValue && value <= maxValue){ 
				setValue(value);
				label.setText("      Выполнено :" + value + "%");
								
			}
			else{
				//close();
			}
		}
		if (propName.equals("status") ){
			int value = ((Integer)pce.getNewValue()).intValue();
			if (value > minValue && value <= maxValue){ 
				close();
								
			}
			
		}
	}

	
}