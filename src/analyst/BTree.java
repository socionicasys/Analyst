/**ypeypeype
 * 
 */
package analyst;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import analyst.ADocument.ASection;


/**
 * @author Виктор
 *
 */
@SuppressWarnings("serial")

public class BTree  extends JTree implements  
									ADocumentChangeListener 
														{

	ADocument aDoc;
	DefaultMutableTreeNode rootNode;
	DefaultTreeModel treeModel;
	TreePath path; 
	
	
	 private  DefaultMutableTreeNode matchNode = new DefaultMutableTreeNode("Соответствие");
	 	private  DefaultMutableTreeNode ileMatchNode =  	new EndTreeNode(SocionicsType.ILE); 
	 	private  DefaultMutableTreeNode seiMatchNode =  	new EndTreeNode(SocionicsType.SEI);
	 	private  DefaultMutableTreeNode eseMatchNode =  	new EndTreeNode(SocionicsType.ESE);
	 	private  DefaultMutableTreeNode liiMatchNode =  	new EndTreeNode(SocionicsType.LII);
	 	private  DefaultMutableTreeNode eieMatchNode =  	new EndTreeNode(SocionicsType.EIE);
	 	private  DefaultMutableTreeNode lsiMatchNode =  	new EndTreeNode(SocionicsType.LSI);
	 	private  DefaultMutableTreeNode sleMatchNode =  	new EndTreeNode(SocionicsType.SLE);
	 	private  DefaultMutableTreeNode ieiMatchNode =  	new EndTreeNode(SocionicsType.IEI);
	 	private  DefaultMutableTreeNode seeMatchNode =  	new EndTreeNode(SocionicsType.SEE);
	 	private  DefaultMutableTreeNode iliMatchNode =  	new EndTreeNode(SocionicsType.ILI);
	 	private  DefaultMutableTreeNode lieMatchNode =  	new EndTreeNode(SocionicsType.LIE);
	 	private  DefaultMutableTreeNode esiMatchNode =  	new EndTreeNode(SocionicsType.ESI);
	 	private  DefaultMutableTreeNode lseMatchNode =  	new EndTreeNode(SocionicsType.LSE);
	 	private  DefaultMutableTreeNode eiiMatchNode =  	new EndTreeNode(SocionicsType.EII);
	 	private  DefaultMutableTreeNode ieeMatchNode =  	new EndTreeNode(SocionicsType.IEE);
	 	private  DefaultMutableTreeNode sliMatchNode =  	new EndTreeNode(SocionicsType.SLI);
	 	
	 private  DefaultMutableTreeNode noMatchNode = new DefaultMutableTreeNode("Несоответствие");
	 	private  DefaultMutableTreeNode ileNoMatchNode =  	new EndTreeNode(SocionicsType.ILE);
	 	private  DefaultMutableTreeNode seiNoMatchNode =  	new EndTreeNode(SocionicsType.SEI);
	 	private  DefaultMutableTreeNode eseNoMatchNode =  	new EndTreeNode(SocionicsType.ESE);
	 	private  DefaultMutableTreeNode liiNoMatchNode =  	new EndTreeNode(SocionicsType.LII);
	 	private  DefaultMutableTreeNode eieNoMatchNode =  	new EndTreeNode(SocionicsType.EIE);
	 	private  DefaultMutableTreeNode lsiNoMatchNode =  	new EndTreeNode(SocionicsType.LSI);
	 	private  DefaultMutableTreeNode sleNoMatchNode =  	new EndTreeNode(SocionicsType.SLE);
	 	private  DefaultMutableTreeNode ieiNoMatchNode =  	new EndTreeNode(SocionicsType.IEI);
	 	private  DefaultMutableTreeNode seeNoMatchNode =  	new EndTreeNode(SocionicsType.SEE);
	 	private  DefaultMutableTreeNode iliNoMatchNode =  	new EndTreeNode(SocionicsType.ILI);
	 	private  DefaultMutableTreeNode lieNoMatchNode =  	new EndTreeNode(SocionicsType.LIE);
	 	private  DefaultMutableTreeNode esiNoMatchNode =  	new EndTreeNode(SocionicsType.ESI);
	 	private  DefaultMutableTreeNode lseNoMatchNode =  	new EndTreeNode(SocionicsType.LSE);
	 	private  DefaultMutableTreeNode eiiNoMatchNode =  	new EndTreeNode(SocionicsType.EII);
	 	private  DefaultMutableTreeNode ieeNoMatchNode =  	new EndTreeNode(SocionicsType.IEE);
	 	private  DefaultMutableTreeNode sliNoMatchNode =  	new EndTreeNode(SocionicsType.SLI);
	 		  

	 
	 
	 
	 
	 private class EndTreeNode extends DefaultMutableTreeNode {

		 public EndTreeNode(Object o){
		 	super(o);
		 	
		 }

		 	public String toString(){
		 		return "["+ getChildCount() +"] "+ super.toString();
		 	} 
		 }	

	 private class Counter  {
		   
		 private Vector <SocionicsType> counter;  
		     
		 public Counter(){
			counter = new Vector <SocionicsType>();
			counter.add(SocionicsType.ILE);
			counter.add(SocionicsType.SEI);
			counter.add(SocionicsType.ESE);
			counter.add(SocionicsType.LII);
			counter.add(SocionicsType.EIE);
			counter.add(SocionicsType.LSI);
			counter.add(SocionicsType.SLE);
			counter.add(SocionicsType.IEI);
			counter.add(SocionicsType.SEE);
			counter.add(SocionicsType.ILI);
			counter.add(SocionicsType.LIE);
			counter.add(SocionicsType.ESI);
			counter.add(SocionicsType.LSE);
			counter.add(SocionicsType.EII);
			counter.add(SocionicsType.IEE);
			counter.add(SocionicsType.SLI);			                   
		} 
		 
		public void exclude (SocionicsType type) {
			counter.remove(type);
		} 
		 
		public Vector <SocionicsType> getExcludedTypes()	{
			Vector <SocionicsType> vec = new Vector<SocionicsType>();
			
			if (!counter.contains(SocionicsType.ILE)) vec.add(SocionicsType.ILE);
			if (!counter.contains(SocionicsType.SEI)) vec.add(SocionicsType.SEI);
			if (!counter.contains(SocionicsType.ESE)) vec.add(SocionicsType.ESE);
			if (!counter.contains(SocionicsType.LII)) vec.add(SocionicsType.LII);
			if (!counter.contains(SocionicsType.EIE)) vec.add(SocionicsType.EIE);
			if (!counter.contains(SocionicsType.LSI)) vec.add(SocionicsType.LSI);
			if (!counter.contains(SocionicsType.SLE)) vec.add(SocionicsType.SLE);
			if (!counter.contains(SocionicsType.IEI)) vec.add(SocionicsType.IEI);
			if (!counter.contains(SocionicsType.SEE)) vec.add(SocionicsType.SEE);
			if (!counter.contains(SocionicsType.ILI)) vec.add(SocionicsType.ILI);
			if (!counter.contains(SocionicsType.LIE)) vec.add(SocionicsType.LIE);
			if (!counter.contains(SocionicsType.ESI)) vec.add(SocionicsType.ESI);
			if (!counter.contains(SocionicsType.LSE)) vec.add(SocionicsType.LSE);
			if (!counter.contains(SocionicsType.EII)) vec.add(SocionicsType.EII);
			if (!counter.contains(SocionicsType.IEE)) vec.add(SocionicsType.IEE);
			if (!counter.contains(SocionicsType.SLI)) vec.add(SocionicsType.SLI);
	
		return vec;	
		}	
		
		
		
		public Vector <SocionicsType> getMatchTypes()	{

		return counter;		
		}	
		
	 }//end class Counter
	 

		 	
	public BTree(ADocument doc) {
		super();
		rootNode  = new DefaultMutableTreeNode(doc.getProperty(Document.TitleProperty));
		treeModel = new DefaultTreeModel(rootNode);
		setModel(treeModel);
		this.aDoc = doc;
		doc.addADocumentChangeListener(this);
		init();
		
	}
	
	
	private void init() {
		   getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		   setEditable(false);
		   toggleClickCount = 1;
		   makeTreeStructure();
		   updateTree();
	   
	}


	private void updateTree() {
//temporary return		
	//	if(true)return; 
		
		
			if (aDoc==null) return;
			
			rootNode.setUserObject(aDoc.getProperty(ADocument.TitleProperty));
			TreePath newPath = getSelectionPath();
			if (newPath != null )path = newPath;
					 
	 
			//Analyze document structure and update tree nodes
			try {
				HashMap <ASection, AData> aDataMap = aDoc.getADataMap();
				
	        	removeAllChildren();	

				Set <ASection> set =  aDataMap.keySet();
				Iterator <ASection> it = set.iterator();
				ASection section = null;
				int sectionOffset = 0;
				int sectionLength = 0;
				int quoteLength   = 0;
				AData data = null;
				String aspect, secondAspect;
				String modifier;
				String dimension;
				String sign;
				String comment;
				String mv;
				String quote;
				
				while (it.hasNext()){
					   section = it.next();
					   
					   sectionOffset = section.getStartOffset();
					   sectionLength = Math.abs(section.getEndOffset()- sectionOffset);
					   quoteLength = Math.min(sectionLength, ATree.MAX_PRESENTATION_CHARS);
					   
					   data =  aDataMap.get(section);
					   aspect = data.getAspect();
					   secondAspect = data.getSecondAspect();
					   modifier = data.getModifier();
					   
					   dimension = data.getDimension();
					   sign = data.getSign();
					   comment = data.getComment();
					   mv = data.getMV();
					   quote = aDoc.getText(sectionOffset, quoteLength);
					   
					   //Отладка
					   //if (!aspect.equals(AData.L)) continue;
					   if (aspect.equals(AData.DOUBT)) continue;
					   if (modifier!=null && modifier.equals(AData.JUMP))  continue;
					   

					   Counter counter = new Counter();

					   
					   if (aspect != null){ 
						   
						   
					   if(!SocionicsType.matches(SocionicsType.ILE, aspect, secondAspect, sign, dimension, mv))
						   counter.exclude(SocionicsType.ILE);
					   if(!SocionicsType.matches(SocionicsType.SEI, aspect, secondAspect, sign, dimension, mv))
						   counter.exclude(SocionicsType.SEI);
					   if(!SocionicsType.matches(SocionicsType.ESE, aspect, secondAspect, sign, dimension, mv))
						   counter.exclude(SocionicsType.ESE);
					   if(!SocionicsType.matches(SocionicsType.LII, aspect, secondAspect, sign, dimension, mv))
						   counter.exclude(SocionicsType.LII);
					   if(!SocionicsType.matches(SocionicsType.EIE, aspect, secondAspect, sign, dimension, mv))
						   counter.exclude(SocionicsType.EIE);
					   if(!SocionicsType.matches(SocionicsType.LSI, aspect, secondAspect, sign, dimension, mv))
						   counter.exclude(SocionicsType.LSI);
					   if(!SocionicsType.matches(SocionicsType.SLE, aspect, secondAspect, sign, dimension, mv))
						   counter.exclude(SocionicsType.SLE);
					   if(!SocionicsType.matches(SocionicsType.IEI, aspect, secondAspect, sign, dimension, mv))
						   counter.exclude(SocionicsType.IEI);
					   if(!SocionicsType.matches(SocionicsType.SEE, aspect, secondAspect, sign, dimension, mv))
						   counter.exclude(SocionicsType.SEE);
					   if(!SocionicsType.matches(SocionicsType.ILI, aspect, secondAspect, sign, dimension, mv))
						   counter.exclude(SocionicsType.ILI);
					   if(!SocionicsType.matches(SocionicsType.LIE, aspect, secondAspect, sign, dimension, mv))
						   counter.exclude(SocionicsType.LIE);
					   if(!SocionicsType.matches(SocionicsType.ESI, aspect, secondAspect, sign, dimension, mv))
						   counter.exclude(SocionicsType.ESI);
					   if(!SocionicsType.matches(SocionicsType.LSE, aspect, secondAspect, sign, dimension, mv))
						   counter.exclude(SocionicsType.LSE);
					   if(!SocionicsType.matches(SocionicsType.EII, aspect, secondAspect, sign, dimension, mv))
						   counter.exclude(SocionicsType.EII);
					   if(!SocionicsType.matches(SocionicsType.IEE, aspect, secondAspect, sign, dimension, mv))
						   counter.exclude(SocionicsType.IEE);
					   if(!SocionicsType.matches(SocionicsType.SLI, aspect, secondAspect, sign, dimension, mv))
						   counter.exclude(SocionicsType.SLI);
						   

					   }// if (aspect != null)
					   
				 
		    Vector<SocionicsType> c = counter.getMatchTypes();
			if (c.contains(SocionicsType.ILE))  ileMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			else								ileNoMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			if (c.contains(SocionicsType.SEI))	seiMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			else								seiNoMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false)); 
			if (c.contains(SocionicsType.ESE)) 	eseMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			else								eseNoMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			if (c.contains(SocionicsType.LII)) 	liiMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			else								liiNoMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			if (c.contains(SocionicsType.EIE)) 	eieMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			else								eieNoMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			if (c.contains(SocionicsType.LSI)) 	lsiMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			else								lsiNoMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			if (c.contains(SocionicsType.SLE)) 	sleMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			else								sleNoMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			if (c.contains(SocionicsType.IEI))	ieiMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			else								ieiNoMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false)); 
			if (c.contains(SocionicsType.SEE)) 	seeMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			else								seeNoMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			if (c.contains(SocionicsType.ILI)) 	iliMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			else								iliNoMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			if (c.contains(SocionicsType.LIE)) 	lieMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			else								lieNoMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			if (c.contains(SocionicsType.ESI))	esiMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			else								esiNoMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false)); 
			if (c.contains(SocionicsType.LSE))	lseMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			else								lseNoMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false)); 
			if (c.contains(SocionicsType.EII))	eiiMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			else								eiiNoMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false)); 
			if (c.contains(SocionicsType.IEE)) 	ieeMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			else								ieeNoMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			if (c.contains(SocionicsType.SLI)) 	sliMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			else								sliNoMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					   
					
				} //end While()
			} catch (BadLocationException e) {
				System.out.println("Exception in BTree.updateTree() :");
				e.printStackTrace();
			}
			
			treeModel.reload();

			if (path!=null) {
				if(path.getLastPathComponent() instanceof DefaultMutableTreeNode 
						&& ((DefaultMutableTreeNode)path.getLastPathComponent()).isLeaf())expandPath(path.getParentPath());
				else  expandPath(path);
			}
		}
		
		
	


	private void makeTreeStructure() {
		
		rootNode.add(matchNode);
		rootNode.add(noMatchNode);
		
		matchNode.add(ileMatchNode);
		matchNode.add(seiMatchNode);
		matchNode.add(eseMatchNode);
		matchNode.add(liiMatchNode);
		matchNode.add(eieMatchNode);
		matchNode.add(lsiMatchNode);
		matchNode.add(sleMatchNode);
		matchNode.add(ieiMatchNode);
		matchNode.add(seeMatchNode);
		matchNode.add(iliMatchNode);
		matchNode.add(lieMatchNode);
		matchNode.add(esiMatchNode);
		matchNode.add(lseMatchNode);
		matchNode.add(eiiMatchNode);
		matchNode.add(ieeMatchNode);
		matchNode.add(sliMatchNode);
		
		noMatchNode.add(ileNoMatchNode);
		noMatchNode.add(seiNoMatchNode);
		noMatchNode.add(eseNoMatchNode);
		noMatchNode.add(liiNoMatchNode);
		noMatchNode.add(eieNoMatchNode);
		noMatchNode.add(lsiNoMatchNode);
		noMatchNode.add(sleNoMatchNode);
		noMatchNode.add(ieiNoMatchNode);
		noMatchNode.add(seeNoMatchNode);
		noMatchNode.add(iliNoMatchNode);
		noMatchNode.add(lieNoMatchNode);
		noMatchNode.add(esiNoMatchNode);
		noMatchNode.add(lseNoMatchNode);
		noMatchNode.add(eiiNoMatchNode);
		noMatchNode.add(ieeNoMatchNode);
		noMatchNode.add(sliNoMatchNode);
		
		
	}

	public JScrollPane getContainer(){
		JScrollPane sp = new JScrollPane(this);
		sp.setPreferredSize(new Dimension (200,500));
		return  sp;
	}

	@Override
	public void aDocumentChanged(ADocument doc) {
		updateTree();		
	}
	
	private void removeAllChildren() {
		
		ileMatchNode.removeAllChildren();
		seiMatchNode.removeAllChildren();
	    eseMatchNode.removeAllChildren();
	    liiMatchNode.removeAllChildren();
        eieMatchNode.removeAllChildren();
        lsiMatchNode.removeAllChildren();
        sleMatchNode.removeAllChildren();        ieiMatchNode.removeAllChildren();        seeMatchNode.removeAllChildren();        iliMatchNode.removeAllChildren();        lieMatchNode.removeAllChildren();        esiMatchNode.removeAllChildren();        lseMatchNode.removeAllChildren();        eiiMatchNode.removeAllChildren();        ieeMatchNode.removeAllChildren();        sliMatchNode.removeAllChildren();
        
        
        ileNoMatchNode.removeAllChildren();
        seiNoMatchNode.removeAllChildren();
        eseNoMatchNode.removeAllChildren();
        liiNoMatchNode.removeAllChildren();
        eieNoMatchNode.removeAllChildren();
        lsiNoMatchNode.removeAllChildren();
        sleNoMatchNode.removeAllChildren();
        ieiNoMatchNode.removeAllChildren();
        seeNoMatchNode.removeAllChildren();
        iliNoMatchNode.removeAllChildren();
        lieNoMatchNode.removeAllChildren();
        esiNoMatchNode.removeAllChildren();
        lseNoMatchNode.removeAllChildren();
        eiiNoMatchNode.removeAllChildren();
        ieeNoMatchNode.removeAllChildren();
        sliNoMatchNode.removeAllChildren();
        
     }
	

}