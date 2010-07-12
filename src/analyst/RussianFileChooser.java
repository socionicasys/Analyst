package analyst;

import java.awt.Component;
import java.awt.Container;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;

public class RussianFileChooser extends JFileChooser {

	
	public RussianFileChooser(){
		super();
		
		JFileChooser fc = new JFileChooser();
	
 	     for (int i=0; i<fc.getComponentCount(); i++)
 	     {   
 	    	 digComponents(fc.getComponent(i));
 	    	  }
 	    	 
 	}

		

	
	private void digComponents(Component c){
		if (! (c instanceof Container)) return;
		int childCount  = ((Container)c).getComponentCount();
		for (int i = 0; i<childCount; i++)
		{
			if (c instanceof Component)replaceSomeNames((Component)c);
			digComponents(((Container)c).getComponent(i));
		}
	}

	private void replaceSomeNames(Component c){

	//	if (c instanceof JLabel)
			System.out.println(c);//  && ((JLabel)c).getText().equals("Look In:")) ((JLabel)c).setText("Искать в:");
	//	if (c instanceof JLabel && ((JLabel)c).getText().equals("File Name:")) ((JLabel)c).setText("Имя файла:");
	//	if (c instanceof JLabel && ((JLabel)c).getText().equals("Files of Type:")) ((JLabel)c).setText("Тип файлов:");
	}
}
