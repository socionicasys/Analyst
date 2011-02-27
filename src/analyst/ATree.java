/**
 * 
 */
package analyst;

import java.awt.Dimension;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.tree.*;

import analyst.ADocument.ASection;
/**
 * @author Виктор
 *
 */
public class ATree extends JTree implements  
											ADocumentChangeListener
											
											{
	
	static final int MAX_PRESENTATION_CHARS  = 100;
	ADocument aDoc;
	DefaultMutableTreeNode rootNode;
	DefaultTreeModel treeModel;
	TreePath path; 
	JumpCounter jc;

	
	DefaultMutableTreeNode aspectNode = new DefaultMutableTreeNode("Функции");
		DefaultMutableTreeNode aspectLNode= new DefaultMutableTreeNode("БЛ");
			DefaultMutableTreeNode aspectLSignNode= new DefaultMutableTreeNode("Знаки");
				DefaultMutableTreeNode aspectLSignPlusNode= new EndTreeNode(" + ");
				DefaultMutableTreeNode aspectLSignMinusNode= new EndTreeNode(" - ");
			DefaultMutableTreeNode aspectLDimensionNode= new DefaultMutableTreeNode("Размерности");
				DefaultMutableTreeNode aspectLDimensionD1Node= new EndTreeNode("Ex");
				DefaultMutableTreeNode aspectLDimensionD2Node= new EndTreeNode("Nm");
				DefaultMutableTreeNode aspectLDimensionD3Node= new EndTreeNode("St");
				DefaultMutableTreeNode aspectLDimensionD4Node= new EndTreeNode("Tm");
				DefaultMutableTreeNode aspectLDimensionOdnomernostNode= new EndTreeNode("Одномерность");
				DefaultMutableTreeNode aspectLDimensionIndividualnostNode= new EndTreeNode("Индивидуальность");
				DefaultMutableTreeNode aspectLDimensionMalomernostNode= new EndTreeNode("Маломерность");
				DefaultMutableTreeNode aspectLDimensionMnogomernostNode= new EndTreeNode("Многомерность");
			DefaultMutableTreeNode aspectLVMNode= new DefaultMutableTreeNode("Ментал/Витал");
				DefaultMutableTreeNode aspectLVMMentalNode= new EndTreeNode("Ментал");
				DefaultMutableTreeNode aspectLVMVitalNode= new EndTreeNode("Витал");

				DefaultMutableTreeNode aspectPNode= new DefaultMutableTreeNode("ЧЛ");
				DefaultMutableTreeNode aspectPSignNode= new DefaultMutableTreeNode("Знаки");
					DefaultMutableTreeNode aspectPSignPlusNode= new EndTreeNode(" + ");
					DefaultMutableTreeNode aspectPSignMinusNode= new EndTreeNode(" - ");
				DefaultMutableTreeNode aspectPDimensionNode= new DefaultMutableTreeNode("Размерности");
					DefaultMutableTreeNode aspectPDimensionD1Node= new EndTreeNode("Ex");
					DefaultMutableTreeNode aspectPDimensionD2Node= new EndTreeNode("Nm");
					DefaultMutableTreeNode aspectPDimensionD3Node= new EndTreeNode("St");
					DefaultMutableTreeNode aspectPDimensionD4Node= new EndTreeNode("Tm");
					DefaultMutableTreeNode aspectPDimensionOdnomernostNode= new EndTreeNode("Одномерность");
					DefaultMutableTreeNode aspectPDimensionIndividualnostNode= new EndTreeNode("Индивидуальность");
					DefaultMutableTreeNode aspectPDimensionMalomernostNode= new EndTreeNode("Маломерность");
					DefaultMutableTreeNode aspectPDimensionMnogomernostNode= new EndTreeNode("Многомерность");
				DefaultMutableTreeNode aspectPVMNode= new DefaultMutableTreeNode("Ментал/Витал");
					DefaultMutableTreeNode aspectPVMMentalNode= new EndTreeNode("Ментал");
					DefaultMutableTreeNode aspectPVMVitalNode= new EndTreeNode("Витал");
					
					DefaultMutableTreeNode aspectRNode= new DefaultMutableTreeNode("БЭ");
					DefaultMutableTreeNode aspectRSignNode= new DefaultMutableTreeNode("Знаки");
						DefaultMutableTreeNode aspectRSignPlusNode= new EndTreeNode(" + ");
						DefaultMutableTreeNode aspectRSignMinusNode= new EndTreeNode(" - ");
					DefaultMutableTreeNode aspectRDimensionNode= new DefaultMutableTreeNode("Размерности");
						DefaultMutableTreeNode aspectRDimensionD1Node= new EndTreeNode("Ex");
						DefaultMutableTreeNode aspectRDimensionD2Node= new EndTreeNode("Nm");
						DefaultMutableTreeNode aspectRDimensionD3Node= new EndTreeNode("St");
						DefaultMutableTreeNode aspectRDimensionD4Node= new EndTreeNode("Tm");
						DefaultMutableTreeNode aspectRDimensionOdnomernostNode= new EndTreeNode("Одномерность");
						DefaultMutableTreeNode aspectRDimensionIndividualnostNode= new EndTreeNode("Индивидуальность");
						DefaultMutableTreeNode aspectRDimensionMalomernostNode= new EndTreeNode("Маломерность");
						DefaultMutableTreeNode aspectRDimensionMnogomernostNode= new EndTreeNode("Многомерность");
					DefaultMutableTreeNode aspectRVMNode= new DefaultMutableTreeNode("Ментал/Витал");
						DefaultMutableTreeNode aspectRVMMentalNode= new EndTreeNode("Ментал");
						DefaultMutableTreeNode aspectRVMVitalNode= new EndTreeNode("Витал");				

						DefaultMutableTreeNode aspectENode= new DefaultMutableTreeNode("ЧЭ");
						DefaultMutableTreeNode aspectESignNode= new DefaultMutableTreeNode("Знаки");
							DefaultMutableTreeNode aspectESignPlusNode= new EndTreeNode(" + ");
							DefaultMutableTreeNode aspectESignMinusNode= new EndTreeNode(" - ");
						DefaultMutableTreeNode aspectEDimensionNode= new DefaultMutableTreeNode("Размерности");
							DefaultMutableTreeNode aspectEDimensionD1Node= new EndTreeNode("Ex");
							DefaultMutableTreeNode aspectEDimensionD2Node= new EndTreeNode("Nm");
							DefaultMutableTreeNode aspectEDimensionD3Node= new EndTreeNode("St");
							DefaultMutableTreeNode aspectEDimensionD4Node= new EndTreeNode("Tm");
							DefaultMutableTreeNode aspectEDimensionOdnomernostNode= new EndTreeNode("Одномерность");
							DefaultMutableTreeNode aspectEDimensionIndividualnostNode= new EndTreeNode("Индивидуальность");
							DefaultMutableTreeNode aspectEDimensionMalomernostNode= new EndTreeNode("Маломерность");
							DefaultMutableTreeNode aspectEDimensionMnogomernostNode= new EndTreeNode("Многомерность");
						DefaultMutableTreeNode aspectEVMNode= new DefaultMutableTreeNode("Ментал/Витал");
							DefaultMutableTreeNode aspectEVMMentalNode= new EndTreeNode("Ментал");
							DefaultMutableTreeNode aspectEVMVitalNode= new EndTreeNode("Витал");			
							
							DefaultMutableTreeNode aspectSNode= new DefaultMutableTreeNode("БС");
							DefaultMutableTreeNode aspectSSignNode= new DefaultMutableTreeNode("Знаки");
								DefaultMutableTreeNode aspectSSignPlusNode= new EndTreeNode(" + ");
								DefaultMutableTreeNode aspectSSignMinusNode= new EndTreeNode(" - ");
							DefaultMutableTreeNode aspectSDimensionNode= new DefaultMutableTreeNode("Размерности");
								DefaultMutableTreeNode aspectSDimensionD1Node= new EndTreeNode("Ex");
								DefaultMutableTreeNode aspectSDimensionD2Node= new EndTreeNode("Nm");
								DefaultMutableTreeNode aspectSDimensionD3Node= new EndTreeNode("St");
								DefaultMutableTreeNode aspectSDimensionD4Node= new EndTreeNode("Tm");
								DefaultMutableTreeNode aspectSDimensionOdnomernostNode= new EndTreeNode("Одномерность");
								DefaultMutableTreeNode aspectSDimensionIndividualnostNode= new EndTreeNode("Индивидуальность");
								DefaultMutableTreeNode aspectSDimensionMalomernostNode= new EndTreeNode("Маломерность");
								DefaultMutableTreeNode aspectSDimensionMnogomernostNode= new EndTreeNode("Многомерность");
							DefaultMutableTreeNode aspectSVMNode= new DefaultMutableTreeNode("Ментал/Витал");
								DefaultMutableTreeNode aspectSVMMentalNode= new EndTreeNode("Ментал");
								DefaultMutableTreeNode aspectSVMVitalNode= new EndTreeNode("Витал");							

								DefaultMutableTreeNode aspectFNode= new DefaultMutableTreeNode("ЧС");
								DefaultMutableTreeNode aspectFSignNode= new DefaultMutableTreeNode("Знаки");
									DefaultMutableTreeNode aspectFSignPlusNode= new EndTreeNode(" + ");
									DefaultMutableTreeNode aspectFSignMinusNode= new EndTreeNode(" - ");
								DefaultMutableTreeNode aspectFDimensionNode= new DefaultMutableTreeNode("Размерности");
									DefaultMutableTreeNode aspectFDimensionD1Node= new EndTreeNode("Ex");
									DefaultMutableTreeNode aspectFDimensionD2Node= new EndTreeNode("Nm");
									DefaultMutableTreeNode aspectFDimensionD3Node= new EndTreeNode("St");
									DefaultMutableTreeNode aspectFDimensionD4Node= new EndTreeNode("Tm");
									DefaultMutableTreeNode aspectFDimensionOdnomernostNode= new EndTreeNode("Одномерность");
									DefaultMutableTreeNode aspectFDimensionIndividualnostNode= new EndTreeNode("Индивидуальность");
									DefaultMutableTreeNode aspectFDimensionMalomernostNode= new EndTreeNode("Маломерность");
									DefaultMutableTreeNode aspectFDimensionMnogomernostNode= new EndTreeNode("Многомерность");
								DefaultMutableTreeNode aspectFVMNode= new DefaultMutableTreeNode("Ментал/Витал");
									DefaultMutableTreeNode aspectFVMMentalNode= new EndTreeNode("Ментал");
									DefaultMutableTreeNode aspectFVMVitalNode= new EndTreeNode("Витал");
					
									DefaultMutableTreeNode aspectTNode= new DefaultMutableTreeNode("БИ");
									DefaultMutableTreeNode aspectTSignNode= new DefaultMutableTreeNode("Знаки");
										DefaultMutableTreeNode aspectTSignPlusNode= new EndTreeNode(" + ");
										DefaultMutableTreeNode aspectTSignMinusNode= new EndTreeNode(" - ");
									DefaultMutableTreeNode aspectTDimensionNode= new DefaultMutableTreeNode("Размерности");
										DefaultMutableTreeNode aspectTDimensionD1Node= new EndTreeNode("Ex");
										DefaultMutableTreeNode aspectTDimensionD2Node= new EndTreeNode("Nm");
										DefaultMutableTreeNode aspectTDimensionD3Node= new EndTreeNode("St");
										DefaultMutableTreeNode aspectTDimensionD4Node= new EndTreeNode("Tm");
										DefaultMutableTreeNode aspectTDimensionOdnomernostNode= new EndTreeNode("Одномерность");
										DefaultMutableTreeNode aspectTDimensionIndividualnostNode= new EndTreeNode("Индивидуальность");
										DefaultMutableTreeNode aspectTDimensionMalomernostNode= new EndTreeNode("Маломерность");
										DefaultMutableTreeNode aspectTDimensionMnogomernostNode= new EndTreeNode("Многомерность");
									DefaultMutableTreeNode aspectTVMNode= new DefaultMutableTreeNode("Ментал/Витал");
										DefaultMutableTreeNode aspectTVMMentalNode= new EndTreeNode("Ментал");
										DefaultMutableTreeNode aspectTVMVitalNode= new EndTreeNode("Витал");									
				
										DefaultMutableTreeNode aspectINode= new DefaultMutableTreeNode("ЧИ");
										DefaultMutableTreeNode aspectISignNode= new DefaultMutableTreeNode("Знаки");
											DefaultMutableTreeNode aspectISignPlusNode= new EndTreeNode(" + ");
											DefaultMutableTreeNode aspectISignMinusNode= new EndTreeNode(" - ");
										DefaultMutableTreeNode aspectIDimensionNode= new DefaultMutableTreeNode("Размерности");
											DefaultMutableTreeNode aspectIDimensionD1Node= new EndTreeNode("Ex");
											DefaultMutableTreeNode aspectIDimensionD2Node= new EndTreeNode("Nm");
											DefaultMutableTreeNode aspectIDimensionD3Node= new EndTreeNode("St");
											DefaultMutableTreeNode aspectIDimensionD4Node= new EndTreeNode("Tm");
											DefaultMutableTreeNode aspectIDimensionOdnomernostNode= new EndTreeNode("Одномерность");
											DefaultMutableTreeNode aspectIDimensionIndividualnostNode= new EndTreeNode("Индивидуальность");
											DefaultMutableTreeNode aspectIDimensionMalomernostNode= new EndTreeNode("Маломерность");
											DefaultMutableTreeNode aspectIDimensionMnogomernostNode= new EndTreeNode("Многомерность");
										DefaultMutableTreeNode aspectIVMNode= new DefaultMutableTreeNode("Ментал/Витал");
											DefaultMutableTreeNode aspectIVMMentalNode= new EndTreeNode("Ментал");
											DefaultMutableTreeNode aspectIVMVitalNode= new EndTreeNode("Витал");										
										
	DefaultMutableTreeNode dimensionNode = new DefaultMutableTreeNode("Размерности");
		DefaultMutableTreeNode dimensionD1Node = new DefaultMutableTreeNode("Опыт");
			DefaultMutableTreeNode dimensionD1LNode = new EndTreeNode("БЛ");
			DefaultMutableTreeNode dimensionD1PNode = new EndTreeNode("ЧЛ");
			DefaultMutableTreeNode dimensionD1RNode = new EndTreeNode("БЭ");
			DefaultMutableTreeNode dimensionD1ENode = new EndTreeNode("ЧЭ");
			DefaultMutableTreeNode dimensionD1SNode = new EndTreeNode("БС");
			DefaultMutableTreeNode dimensionD1FNode = new EndTreeNode("ЧС");
			DefaultMutableTreeNode dimensionD1TNode = new EndTreeNode("БИ");
			DefaultMutableTreeNode dimensionD1INode = new EndTreeNode("ЧИ");
		DefaultMutableTreeNode dimensionD2Node = new DefaultMutableTreeNode("Норма");
			DefaultMutableTreeNode dimensionD2LNode = new EndTreeNode("БЛ");
			DefaultMutableTreeNode dimensionD2PNode = new EndTreeNode("ЧЛ");
			DefaultMutableTreeNode dimensionD2RNode = new EndTreeNode("БЭ");
			DefaultMutableTreeNode dimensionD2ENode = new EndTreeNode("ЧЭ");
			DefaultMutableTreeNode dimensionD2SNode = new EndTreeNode("БС");
			DefaultMutableTreeNode dimensionD2FNode = new EndTreeNode("ЧС");
			DefaultMutableTreeNode dimensionD2TNode = new EndTreeNode("БИ");
			DefaultMutableTreeNode dimensionD2INode = new EndTreeNode("ЧИ");
		
		DefaultMutableTreeNode dimensionD3Node = new DefaultMutableTreeNode("Ситуация");
			DefaultMutableTreeNode dimensionD3LNode = new EndTreeNode("БЛ");
			DefaultMutableTreeNode dimensionD3PNode = new EndTreeNode("ЧЛ");
			DefaultMutableTreeNode dimensionD3RNode = new EndTreeNode("БЭ");
			DefaultMutableTreeNode dimensionD3ENode = new EndTreeNode("ЧЭ");
			DefaultMutableTreeNode dimensionD3SNode = new EndTreeNode("БС");
			DefaultMutableTreeNode dimensionD3FNode = new EndTreeNode("ЧС");
			DefaultMutableTreeNode dimensionD3TNode = new EndTreeNode("БИ");
			DefaultMutableTreeNode dimensionD3INode = new EndTreeNode("ЧИ");
			
		DefaultMutableTreeNode dimensionD4Node = new DefaultMutableTreeNode("Время");
			DefaultMutableTreeNode dimensionD4LNode = new EndTreeNode("БЛ");
			DefaultMutableTreeNode dimensionD4PNode = new EndTreeNode("ЧЛ");
			DefaultMutableTreeNode dimensionD4RNode = new EndTreeNode("БЭ");
			DefaultMutableTreeNode dimensionD4ENode = new EndTreeNode("ЧЭ");
			DefaultMutableTreeNode dimensionD4SNode = new EndTreeNode("БС");
			DefaultMutableTreeNode dimensionD4FNode = new EndTreeNode("ЧС");
			DefaultMutableTreeNode dimensionD4TNode = new EndTreeNode("БИ");
			DefaultMutableTreeNode dimensionD4INode = new EndTreeNode("ЧИ");
			
DefaultMutableTreeNode dimensionMaloNode = new DefaultMutableTreeNode("Маломерность");
			DefaultMutableTreeNode dimensionMaloLNode = new EndTreeNode("БЛ");
			DefaultMutableTreeNode dimensionMaloPNode = new EndTreeNode("ЧЛ");
			DefaultMutableTreeNode dimensionMaloRNode = new EndTreeNode("БЭ");
			DefaultMutableTreeNode dimensionMaloENode = new EndTreeNode("ЧЭ");
			DefaultMutableTreeNode dimensionMaloSNode = new EndTreeNode("БС");
			DefaultMutableTreeNode dimensionMaloFNode = new EndTreeNode("ЧС");
			DefaultMutableTreeNode dimensionMaloTNode = new EndTreeNode("БИ");
			DefaultMutableTreeNode dimensionMaloINode = new EndTreeNode("ЧИ");
			
DefaultMutableTreeNode dimensionMnogoNode = new DefaultMutableTreeNode("Многомерность");
			DefaultMutableTreeNode dimensionMnogoLNode = new EndTreeNode("БЛ");
			DefaultMutableTreeNode dimensionMnogoPNode = new EndTreeNode("ЧЛ");
			DefaultMutableTreeNode dimensionMnogoRNode = new EndTreeNode("БЭ");
			DefaultMutableTreeNode dimensionMnogoENode = new EndTreeNode("ЧЭ");
			DefaultMutableTreeNode dimensionMnogoSNode = new EndTreeNode("БС");
			DefaultMutableTreeNode dimensionMnogoFNode = new EndTreeNode("ЧС");
			DefaultMutableTreeNode dimensionMnogoTNode = new EndTreeNode("БИ");
			DefaultMutableTreeNode dimensionMnogoINode = new EndTreeNode("ЧИ");			

DefaultMutableTreeNode dimensionOdnoNode = new DefaultMutableTreeNode("Одномерность");
			DefaultMutableTreeNode dimensionOdnoLNode = new EndTreeNode("БЛ");
			DefaultMutableTreeNode dimensionOdnoPNode = new EndTreeNode("ЧЛ");
			DefaultMutableTreeNode dimensionOdnoRNode = new EndTreeNode("БЭ");
			DefaultMutableTreeNode dimensionOdnoENode = new EndTreeNode("ЧЭ");
			DefaultMutableTreeNode dimensionOdnoSNode = new EndTreeNode("БС");
			DefaultMutableTreeNode dimensionOdnoFNode = new EndTreeNode("ЧС");
			DefaultMutableTreeNode dimensionOdnoTNode = new EndTreeNode("БИ");
			DefaultMutableTreeNode dimensionOdnoINode = new EndTreeNode("ЧИ");			

DefaultMutableTreeNode dimensionIndiNode = new DefaultMutableTreeNode("Индивидуальность");
			DefaultMutableTreeNode dimensionIndiLNode = new EndTreeNode("БЛ");
			DefaultMutableTreeNode dimensionIndiPNode = new EndTreeNode("ЧЛ");
			DefaultMutableTreeNode dimensionIndiRNode = new EndTreeNode("БЭ");
			DefaultMutableTreeNode dimensionIndiENode = new EndTreeNode("ЧЭ");
			DefaultMutableTreeNode dimensionIndiSNode = new EndTreeNode("БС");
			DefaultMutableTreeNode dimensionIndiFNode = new EndTreeNode("ЧС");
			DefaultMutableTreeNode dimensionIndiTNode = new EndTreeNode("БИ");
			DefaultMutableTreeNode dimensionIndiINode = new EndTreeNode("ЧИ");			

	DefaultMutableTreeNode blockNode = new DefaultMutableTreeNode("Блоки");
		DefaultMutableTreeNode blockSuperegoNode = new EndTreeNode("Супер-ЭГО");
		DefaultMutableTreeNode blockSuperidNode = new EndTreeNode("Супер-ИД");		
		DefaultMutableTreeNode blockLINode = new EndTreeNode("БЛ-ЧИ");
		DefaultMutableTreeNode blockLFNode = new EndTreeNode("БЛ-ЧС");
		DefaultMutableTreeNode blockPTNode = new EndTreeNode("ЧЛ-БИ");
		DefaultMutableTreeNode blockPSNode = new EndTreeNode("ЧЛ-БС");
		DefaultMutableTreeNode blockRINode = new EndTreeNode("БЭ-ЧИ");
		DefaultMutableTreeNode blockRFNode = new EndTreeNode("БЭ-ЧС");
		DefaultMutableTreeNode blockETNode = new EndTreeNode("ЧЭ-БИ");
		DefaultMutableTreeNode blockESNode = new EndTreeNode("ЧЭ-БС");
		DefaultMutableTreeNode blockSPNode = new EndTreeNode("БС-ЧЛ");
		DefaultMutableTreeNode blockSENode = new EndTreeNode("БС-ЧЭ");
		DefaultMutableTreeNode blockFLNode = new EndTreeNode("ЧС-БЛ");
		DefaultMutableTreeNode blockFRNode = new EndTreeNode("ЧС-БЭ");
		DefaultMutableTreeNode blockTPNode = new EndTreeNode("БИ-ЧЛ");
		DefaultMutableTreeNode blockTENode = new EndTreeNode("БИ-ЧЭ");
		DefaultMutableTreeNode blockILNode = new EndTreeNode("ЧИ-БЛ");
		DefaultMutableTreeNode blockIRNode = new EndTreeNode("ЧИ-БЭ");
		
	DefaultMutableTreeNode signNode = new DefaultMutableTreeNode("Знаки");
		DefaultMutableTreeNode signPlusNode = new DefaultMutableTreeNode(" + ");
			DefaultMutableTreeNode signPlusLNode = new EndTreeNode("БЛ");
			DefaultMutableTreeNode signPlusPNode = new EndTreeNode("ЧЛ");
			DefaultMutableTreeNode signPlusRNode = new EndTreeNode("БЭ");
			DefaultMutableTreeNode signPlusENode = new EndTreeNode("ЧЭ");
			DefaultMutableTreeNode signPlusSNode = new EndTreeNode("БС");
			DefaultMutableTreeNode signPlusFNode = new EndTreeNode("ЧС");
			DefaultMutableTreeNode signPlusTNode = new EndTreeNode("БИ");
			DefaultMutableTreeNode signPlusINode = new EndTreeNode("ЧИ");
		DefaultMutableTreeNode signMinusNode = new DefaultMutableTreeNode(" - ");
			DefaultMutableTreeNode signMinusLNode = new EndTreeNode("БЛ");
			DefaultMutableTreeNode signMinusPNode = new EndTreeNode("ЧЛ");
			DefaultMutableTreeNode signMinusRNode = new EndTreeNode("БЭ");
			DefaultMutableTreeNode signMinusENode = new EndTreeNode("ЧЭ");
			DefaultMutableTreeNode signMinusSNode = new EndTreeNode("БС");
			DefaultMutableTreeNode signMinusFNode = new EndTreeNode("ЧС");
			DefaultMutableTreeNode signMinusTNode = new EndTreeNode("БИ");
			DefaultMutableTreeNode signMinusINode = new EndTreeNode("ЧИ");

	DefaultMutableTreeNode doubtNode = new EndTreeNode("Непонятные места");
	DefaultMutableTreeNode jumpNode = new EndTreeNode("Переводы");
		
	

	
private class EndTreeNode extends DefaultMutableTreeNode {

public EndTreeNode(Object o){
	super(o);
	
}

	public String toString(){
		return "["+ getChildCount() +"] "+ super.toString();
	} 
}	
	

	/**
	 * 
	 */
	public ATree(ADocument doc) {
		super();
        rootNode = new DefaultMutableTreeNode(doc.getProperty(Document.TitleProperty));
        treeModel = new DefaultTreeModel(rootNode);
        this.setModel(treeModel);
		this.aDoc = doc;
		doc.addADocumentChangeListener(this);
		jc = new JumpCounter();
		init();
		
	}



	
	protected void init(){
		
	   getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
//	   addTreeSelectionListener(this);
	   setEditable(false);
	   toggleClickCount = 1;
//     treeModel = new DefaultTreeModel(rootNode);
	   makeTreeStructure();
	   updateTree();
//	   addTreeSelectionListener(this);
	   

	}
	
	private void updateTree(){
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
				   quoteLength = Math.min(sectionLength, MAX_PRESENTATION_CHARS);
				   
				   data =  aDataMap.get(section);
				   aspect = data.getAspect();
				   secondAspect = data.getSecondAspect();
				   modifier = data.getModifier();
				   
				   dimension = data.getDimension();
				   sign = data.getSign();
				   comment = data.getComment();
				   mv = data.getMV();
				   quote = aDoc.getText(sectionOffset, quoteLength);
				   
				   
				   if (aspect!=null && aspect.equals(AData.L )){
					   if(sign!=null && sign.equals(AData.PLUS)){
	 			    		aspectLSignPlusNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					        signPlusLNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					   		}
	 			    		else if (sign!=null && sign.equals(AData.MINUS)){ 
	 			    		aspectLSignMinusNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					        signMinusLNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		} 
					   if(dimension!=null && dimension.equals(AData.MALOMERNOST)){
						    aspectLDimensionMalomernostNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
						    dimensionMaloLNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					   		}
	 			    		else if (dimension!=null && dimension.equals(AData.MNOGOMERNOST)){
	 			    		aspectLDimensionMnogomernostNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		dimensionMnogoLNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		} else	
					   		if(dimension!=null && dimension.equals(AData.ODNOMERNOST)){
						    aspectLDimensionOdnomernostNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
						    dimensionOdnoLNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					   		}
	 			    		else if (dimension!=null && dimension.equals(AData.INDIVIDUALNOST)){
	 			    		aspectLDimensionIndividualnostNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		dimensionIndiLNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		} 						   
	 			    		else if (dimension!=null && dimension.equals(AData.D1)){ 
	 			    		aspectLDimensionD1Node.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		dimensionD1LNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		//dimensionMaloLNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		}
	 			    		else if (dimension!=null && dimension.equals(AData.D2)){ 
		 			    	aspectLDimensionD2Node.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
		 			    	dimensionD2LNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
		 			    	//dimensionMaloLNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		}		
	 			    		else if (dimension!=null && dimension.equals(AData.D3)){ 
			 			    aspectLDimensionD3Node.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			 			    dimensionD3LNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			 			    dimensionMnogoLNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
		 			    	}	
	 			    		else if (dimension!=null && dimension.equals(AData.D4)){ 
				 			aspectLDimensionD4Node.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
				 			dimensionD4LNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
				 			dimensionMnogoLNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			 			    }						   
					   if(mv!=null && mv.equals(AData.MENTAL)){
						    aspectLVMMentalNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					   		}
	 			    		else if (mv!=null && mv.equals(AData.VITAL)){
	 			    		aspectLVMVitalNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		} 	

				   } //aspect L
				   
				   	//aspect P	
				   if (aspect!=null && aspect.equals(AData.P )){
					   if(sign!=null && sign.equals(AData.PLUS)){
	 			    		aspectPSignPlusNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					        signPlusPNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					   		}
	 			    		else if (sign!=null && sign.equals(AData.MINUS)){ 
	 			    		aspectPSignMinusNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					        signMinusPNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		} 
					   if(dimension!=null && dimension.equals(AData.MALOMERNOST)){
						   	aspectPDimensionMalomernostNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
						    dimensionMaloPNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					   		}
	 			    		else if (dimension!=null && dimension.equals(AData.MNOGOMERNOST)){
	 			    		aspectPDimensionMnogomernostNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		dimensionMnogoPNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		} else	
					   		if(dimension!=null && dimension.equals(AData.ODNOMERNOST)){
						   	aspectPDimensionOdnomernostNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
						    dimensionOdnoPNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					   		}
	 			    		else if (dimension!=null && dimension.equals(AData.INDIVIDUALNOST)){
	 			    		aspectPDimensionIndividualnostNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		dimensionIndiPNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		} 		 			    		else if (dimension!=null && dimension.equals(AData.D1)){ 
	 			    		aspectPDimensionD1Node.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		dimensionD1PNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		//dimensionMaloPNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		}
	 			    		else if (dimension!=null && dimension.equals(AData.D2)){ 
		 			    	aspectPDimensionD2Node.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
		 			    	dimensionD2PNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
		 			    	//dimensionMaloPNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		}		
	 			    		else if (dimension!=null && dimension.equals(AData.D3)){ 
			 			    aspectPDimensionD3Node.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			 			    dimensionD3PNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			 			    dimensionMnogoPNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
		 			    	}	
	 			    		else if (dimension!=null && dimension.equals(AData.D4)){ 
				 			aspectPDimensionD4Node.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
				 			dimensionD4PNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
				 			dimensionMnogoPNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			 			    }						   
					   if(mv!=null && mv.equals(AData.MENTAL)){
						    aspectPVMMentalNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					   		}
	 			    		else if (mv!=null && mv.equals(AData.VITAL)){
	 			    		aspectPVMVitalNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		} 	

				   }//aspect P	 

				   	//aspect R	
				   if (aspect!=null && aspect.equals(AData.R )){
					   if(sign!=null && sign.equals(AData.PLUS)){
	 			    		aspectRSignPlusNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					        signPlusRNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					   		}
	 			    		else if (sign!=null && sign.equals(AData.MINUS)){ 
	 			    		aspectRSignMinusNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					        signMinusRNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		} 
					   if(dimension!=null && dimension.equals(AData.MALOMERNOST)){
						    aspectRDimensionMalomernostNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
						    dimensionMaloRNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					   		}
	 			    		else if (dimension!=null && dimension.equals(AData.MNOGOMERNOST)){
	 			    		aspectRDimensionMnogomernostNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		dimensionMnogoRNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		} 	else
					   		if(dimension!=null && dimension.equals(AData.ODNOMERNOST)){
						    aspectRDimensionOdnomernostNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
						    dimensionOdnoRNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					   		}
	 			    		else if (dimension!=null && dimension.equals(AData.INDIVIDUALNOST)){
	 			    		aspectRDimensionIndividualnostNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		dimensionIndiRNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		} 		 			    		else if (dimension!=null && dimension.equals(AData.D1)){ 
	 			    		aspectRDimensionD1Node.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		dimensionD1RNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		//dimensionMaloRNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		}
	 			    		else if (dimension!=null && dimension.equals(AData.D2)){ 
		 			    	aspectRDimensionD2Node.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
		 			    	dimensionD2RNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
		 			    	//dimensionMaloRNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		}		
	 			    		else if (dimension!=null && dimension.equals(AData.D3)){ 
			 			    aspectRDimensionD3Node.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			 			    dimensionD3RNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			 			    dimensionMnogoRNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
		 			    	}	
	 			    		else if (dimension!=null && dimension.equals(AData.D4)){ 
				 			aspectRDimensionD4Node.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
				 			dimensionD4RNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
				 			dimensionMnogoRNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			 			    }						   
					   if(mv!=null && mv.equals(AData.MENTAL)){
						    aspectRVMMentalNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					   		}
	 			    		else if (mv!=null && mv.equals(AData.VITAL)){
	 			    		aspectRVMVitalNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		} 	
				   }//aspect R	
				   
				   	//aspect E	
				   if (aspect!=null && aspect.equals(AData.E )){
					   if(sign!=null && sign.equals(AData.PLUS)){
	 			    		aspectESignPlusNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					        signPlusENode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					   		}
	 			    		else if (sign!=null && sign.equals(AData.MINUS)){ 
	 			    		aspectESignMinusNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					        signMinusENode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		} 
					   if(dimension!=null && dimension.equals(AData.MALOMERNOST)){
						    aspectEDimensionMalomernostNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
						    dimensionMaloENode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					   		}
	 			    		else if (dimension!=null && dimension.equals(AData.MNOGOMERNOST)){
	 			    		aspectEDimensionMnogomernostNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		dimensionMnogoENode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		} 	else
					   		if(dimension!=null && dimension.equals(AData.ODNOMERNOST)){
						    aspectEDimensionOdnomernostNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
						    dimensionOdnoENode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					   		}
	 			    		else if (dimension!=null && dimension.equals(AData.INDIVIDUALNOST)){
	 			    		aspectEDimensionIndividualnostNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		dimensionIndiENode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		} 	
	 			    		else if (dimension!=null && dimension.equals(AData.D1)){ 
	 			    		aspectEDimensionD1Node.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		dimensionD1ENode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		dimensionMaloENode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		}
	 			    		else if (dimension!=null && dimension.equals(AData.D2)){ 
		 			    	aspectEDimensionD2Node.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
		 			    	dimensionD2ENode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
		 			    	//dimensionMaloENode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		}		
	 			    		else if (dimension!=null && dimension.equals(AData.D3)){ 
			 			    aspectEDimensionD3Node.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			 			    dimensionD3ENode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			 			    //dimensionMnogoENode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
		 			    	}	
	 			    		else if (dimension!=null && dimension.equals(AData.D4)){ 
				 			aspectEDimensionD4Node.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
				 			dimensionD4ENode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
				 			dimensionMnogoENode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			 			    }						   
					   if(mv!=null && mv.equals(AData.MENTAL)){
						    aspectEVMMentalNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					   		}
	 			    		else if (mv!=null && mv.equals(AData.VITAL)){
	 			    		aspectEVMVitalNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		} 
				   }//aspect E	 
				   
				   	//aspect S	
				   if (aspect!=null && aspect.equals(AData.S )){
					   if(sign!=null && sign.equals(AData.PLUS)){
	 			    		aspectSSignPlusNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					        signPlusSNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					   		}
	 			    		else if (sign!=null && sign.equals(AData.MINUS)){ 
	 			    		aspectSSignMinusNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					        signMinusSNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		} 
					   if(dimension!=null && dimension.equals(AData.MALOMERNOST)){
						    aspectSDimensionMalomernostNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
						    dimensionMaloSNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					   		}
	 			    		else if (dimension!=null && dimension.equals(AData.MNOGOMERNOST)){
	 			    		aspectSDimensionMnogomernostNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		dimensionMnogoSNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		} else	
					   		if(dimension!=null && dimension.equals(AData.ODNOMERNOST)){
						    aspectSDimensionOdnomernostNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
						    dimensionOdnoSNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					   		}
	 			    		else if (dimension!=null && dimension.equals(AData.INDIVIDUALNOST)){
	 			    		aspectSDimensionIndividualnostNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		dimensionIndiSNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		} 	
	 		 			    		else if (dimension!=null && dimension.equals(AData.D1)){ 
	 			    		aspectSDimensionD1Node.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		dimensionD1SNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		//dimensionMaloSNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		}
	 			    		else if (dimension!=null && dimension.equals(AData.D2)){ 
		 			    	aspectSDimensionD2Node.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
		 			    	dimensionD2SNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
		 			    	//dimensionMaloSNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		}		
	 			    		else if (dimension!=null && dimension.equals(AData.D3)){ 
			 			    aspectSDimensionD3Node.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			 			    dimensionD3SNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			 			    dimensionMnogoSNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
		 			    	}	
	 			    		else if (dimension!=null && dimension.equals(AData.D4)){ 
				 			aspectSDimensionD4Node.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
				 			dimensionD4SNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
				 			dimensionMnogoSNode.add    (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			 			    }						   
					   if(mv!=null && mv.equals(AData.MENTAL)){
						    aspectSVMMentalNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					   		}
	 			    		else if (mv!=null && mv.equals(AData.VITAL)){
	 			    		aspectSVMVitalNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		} 	

				   }//aspect S	 
				   
				   	//aspect F	
				   if (aspect!=null && aspect.equals(AData.F )){
					   if(sign!=null && sign.equals(AData.PLUS)){
	 			    		aspectFSignPlusNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					        signPlusFNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					   		}
	 			    		else if (sign!=null && sign.equals(AData.MINUS)){ 
	 			    		aspectFSignMinusNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					        signMinusFNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		} 
					   if(dimension!=null && dimension.equals(AData.MALOMERNOST)){
						    aspectFDimensionMalomernostNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
						    dimensionMaloFNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					   		}
	 			    		else if (dimension!=null && dimension.equals(AData.MNOGOMERNOST)){
	 			    		aspectFDimensionMnogomernostNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		dimensionMnogoFNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		} else
					   		if(dimension!=null && dimension.equals(AData.ODNOMERNOST)){
						    aspectFDimensionOdnomernostNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
						    dimensionOdnoFNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					   		}
	 			    		else if (dimension!=null && dimension.equals(AData.INDIVIDUALNOST)){
	 			    		aspectFDimensionIndividualnostNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		dimensionIndiFNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		} 	

	 			    		else if (dimension!=null && dimension.equals(AData.D1)){ 
	 			    		aspectFDimensionD1Node.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		dimensionD1FNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		//dimensionMaloFNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		}
	 			    		else if (dimension!=null && dimension.equals(AData.D2)){ 
		 			    	aspectFDimensionD2Node.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
		 			    	dimensionD2FNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
		 			    	//dimensionMaloFNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		}		
	 			    		else if (dimension!=null && dimension.equals(AData.D3)){ 
			 			    aspectFDimensionD3Node.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			 			    dimensionD3FNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			 			    dimensionMnogoFNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
		 			    	}	
	 			    		else if (dimension!=null && dimension.equals(AData.D4)){ 
				 			aspectFDimensionD4Node.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
				 			dimensionD4FNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
				 			dimensionMnogoFNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			 			    }						   
					   if(mv!=null && mv.equals(AData.MENTAL)){
						    aspectFVMMentalNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					   		}
	 			    		else if (mv!=null && mv.equals(AData.VITAL)){
	 			    		aspectFVMVitalNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		} 	

				   }//aspect F	 
				   
				   	//aspect T	
				   if (aspect!=null && aspect.equals(AData.T )){
					   if(sign!=null && sign.equals(AData.PLUS)){
	 			    		aspectTSignPlusNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					        signPlusTNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					   		}
	 			    		else if (sign!=null && sign.equals(AData.MINUS)){ 
	 			    		aspectTSignMinusNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					        signMinusTNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		} 
					   if(dimension!=null && dimension.equals(AData.MALOMERNOST)){
						    aspectTDimensionMalomernostNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
						    dimensionMaloTNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					   		}
	 			    		else if (dimension!=null && dimension.equals(AData.MNOGOMERNOST)){
	 			    		aspectTDimensionMnogomernostNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		dimensionMnogoTNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		} 	else
	 			    		if(dimension!=null && dimension.equals(AData.ODNOMERNOST)){
						    aspectTDimensionOdnomernostNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
						    dimensionOdnoTNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					   		}
	 			    		else if (dimension!=null && dimension.equals(AData.INDIVIDUALNOST)){
	 			    		aspectTDimensionIndividualnostNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		dimensionIndiTNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		} 	
	 	 			    		else if (dimension!=null && dimension.equals(AData.D1)){ 
	 			    		aspectTDimensionD1Node.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		dimensionD1TNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		//dimensionMaloTNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		}
	 			    		else if (dimension!=null && dimension.equals(AData.D2)){ 
		 			    	aspectTDimensionD2Node.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
		 			    	dimensionD2TNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
		 			    	//dimensionMaloTNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		}		
	 			    		else if (dimension!=null && dimension.equals(AData.D3)){ 
			 			    aspectTDimensionD3Node.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			 			    dimensionD3TNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			 			    dimensionMnogoTNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
		 			    	}	
	 			    		else if (dimension!=null && dimension.equals(AData.D4)){ 
				 			aspectTDimensionD4Node.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
				 			dimensionD4TNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
				 			dimensionMnogoTNode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			 			    }						   
					   if(mv!=null && mv.equals(AData.MENTAL)){
						    aspectTVMMentalNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					   		}
	 			    		else if (mv!=null && mv.equals(AData.VITAL)){
	 			    		aspectTVMVitalNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		} 	

				   }//aspect T	 
				   
				   	//aspect I	
				   if (aspect!=null && aspect.equals(AData.I )){
					   if(sign!=null && sign.equals(AData.PLUS)){
	 			    		aspectISignPlusNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					        signPlusINode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					   		}
	 			    		else if (sign!=null && sign.equals(AData.MINUS)){ 
	 			    		aspectISignMinusNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					        signMinusINode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		} 
					   if(dimension!=null && dimension.equals(AData.MALOMERNOST)){
						    aspectIDimensionMalomernostNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
						    dimensionMaloINode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					   		}
	 			    		else if (dimension!=null && dimension.equals(AData.MNOGOMERNOST)){
	 			    		aspectIDimensionMnogomernostNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		dimensionMnogoINode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		}
					   		if(dimension!=null && dimension.equals(AData.ODNOMERNOST)){
						    aspectIDimensionOdnomernostNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
						    dimensionOdnoINode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					   		}
	 			    		else if (dimension!=null && dimension.equals(AData.INDIVIDUALNOST)){
	 			    		aspectIDimensionIndividualnostNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		dimensionIndiINode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		} 	
	 			    		else if (dimension!=null && dimension.equals(AData.D1)){ 
	 			    		aspectIDimensionD1Node.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		dimensionD1INode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		//dimensionMaloINode.add    (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		}
	 			    		else if (dimension!=null && dimension.equals(AData.D2)){ 
		 			    	aspectIDimensionD2Node.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
		 			    	dimensionD2INode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
		 			    	//dimensionMaloINode.add    (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		}		
	 			    		else if (dimension!=null && dimension.equals(AData.D3)){ 
			 			    aspectIDimensionD3Node.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			 			    dimensionD3INode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			 			    dimensionMnogoINode.add   (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
		 			    	}	
	 			    		else if (dimension!=null && dimension.equals(AData.D4)){ 
				 			aspectIDimensionD4Node.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
				 			dimensionD4INode.add      (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
				 			dimensionMnogoINode.add   (  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
			 			    }						   
					   if(mv!=null && mv.equals(AData.MENTAL)){
						    aspectIVMMentalNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
					   		}
	 			    		else if (mv!=null && mv.equals(AData.VITAL)){
	 			    		aspectIVMVitalNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
	 			    		} 	

				   }//aspect I	 
				   
				   // blocks	
				   if(mv!=null && mv.equals(AData.SUPEREGO)){
					    blockSuperegoNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
				   		}	
				   
				   if(mv!=null && mv.equals(AData.SUPERID)){
					    blockSuperidNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
				   		}						   
				   if (modifier != null && modifier.equals(AData.BLOCK) && aspect !=null && secondAspect!=null ){
						   if(aspect.equals(AData.L) && secondAspect.equals(AData.I)){
							    blockLINode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
						   		}  
						   if(aspect.equals(AData.L) && secondAspect.equals(AData.F)){
							   blockLFNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
						   		}
						   if(aspect.equals(AData.P) && secondAspect.equals(AData.T)){
							   blockPTNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
						   		}
						   if(aspect.equals(AData.P) && secondAspect.equals(AData.S)){
							   blockPSNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
						   		}
						   if(aspect.equals(AData.R) && secondAspect.equals(AData.I)){
							   blockRINode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
						   		}
						   if(aspect.equals(AData.R) && secondAspect.equals(AData.F)){
							   blockRFNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
						   		}
						   if(aspect.equals(AData.E) && secondAspect.equals(AData.T)){
							   blockETNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
						   		}
						   if(aspect.equals(AData.E) && secondAspect.equals(AData.S)){
							   blockESNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
						   		}
						   if(aspect.equals(AData.S) && secondAspect.equals(AData.P)){
							   blockSPNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
						   		}
						   if(aspect.equals(AData.S) && secondAspect.equals(AData.E)){
							   blockSENode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
						   		}
						   if(aspect.equals(AData.F) && secondAspect.equals(AData.L)){
							   blockFLNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
						   		}
						   if(aspect.equals(AData.F) && secondAspect.equals(AData.R)){
							   blockFRNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
						   		}
						   if(aspect.equals(AData.T) && secondAspect.equals(AData.P)){
							   blockTPNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
						   		}
						   if(aspect.equals(AData.T) && secondAspect.equals(AData.E)){
							   blockTENode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
						   		}
						   if(aspect.equals(AData.I) && secondAspect.equals(AData.L)){
							   blockILNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
						   		}
						   if(aspect.equals(AData.I) && secondAspect.equals(AData.R)){
							   blockIRNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +quote + "..."), false));
						   		} 
				   }
				   //end blocks	
				   
				   
				    // doubt
				  if (aspect!=null && aspect.equals(AData.DOUBT )){ 
					  doubtNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." +comment + "..."), false));
				  }  
				  
				    // jumps
				  if (modifier!=null && modifier.equals(AData.JUMP )){ 
					  jumpNode.add(  new DefaultMutableTreeNode(new EndNodeObject(sectionOffset , " Перевод " +aspect+ " -> "+secondAspect), false));
					  jc.addJump(secondAspect, aspect);
				  }  
			}
		} catch (BadLocationException e) {
			System.out.println("Exception in ATree.updateTree() :");
			e.printStackTrace();
		}
		treeModel.reload();
		if (path!=null) {
			if(path.getLastPathComponent() instanceof DefaultMutableTreeNode 
					&& ((DefaultMutableTreeNode)path.getLastPathComponent()).isLeaf())expandPath(path.getParentPath());
			else  expandPath(path);
		}
	}
	
	
	private void removeAllChildren() {
		
		aspectLSignPlusNode.removeAllChildren();
		aspectLSignMinusNode.removeAllChildren();
		aspectLDimensionD1Node.removeAllChildren();		
	    aspectLDimensionD2Node.removeAllChildren();
        aspectLDimensionD3Node.removeAllChildren();
        aspectLDimensionD4Node.removeAllChildren();
        aspectLDimensionMalomernostNode.removeAllChildren();
        aspectLDimensionMnogomernostNode.removeAllChildren();
        aspectLDimensionOdnomernostNode.removeAllChildren();
        aspectLDimensionIndividualnostNode.removeAllChildren();
        aspectLVMMentalNode.removeAllChildren();
        aspectLVMVitalNode.removeAllChildren();
        aspectPSignPlusNode.removeAllChildren(); 
        aspectPSignMinusNode.removeAllChildren();
        aspectPDimensionD1Node.removeAllChildren();
        aspectPDimensionD2Node.removeAllChildren();
        aspectPDimensionD3Node.removeAllChildren();
        aspectPDimensionD4Node.removeAllChildren();
        aspectPDimensionMalomernostNode.removeAllChildren(); 
        aspectPDimensionMnogomernostNode.removeAllChildren();
        aspectPDimensionOdnomernostNode.removeAllChildren(); 
        aspectPDimensionIndividualnostNode.removeAllChildren();
        aspectPVMMentalNode.removeAllChildren(); 
        aspectPVMVitalNode.removeAllChildren();
        aspectRSignPlusNode.removeAllChildren(); 
        aspectRSignMinusNode.removeAllChildren();
        aspectRDimensionD1Node.removeAllChildren();
        aspectRDimensionD2Node.removeAllChildren();
        aspectRDimensionD3Node.removeAllChildren();
        aspectRDimensionD4Node.removeAllChildren();
        aspectRDimensionMalomernostNode.removeAllChildren();
        aspectRDimensionMnogomernostNode.removeAllChildren();
        aspectRDimensionOdnomernostNode.removeAllChildren();
        aspectRDimensionIndividualnostNode.removeAllChildren();
        aspectRVMMentalNode.removeAllChildren();
        aspectRVMVitalNode.removeAllChildren(); 
        aspectESignPlusNode.removeAllChildren();
        aspectESignMinusNode.removeAllChildren();
        aspectEDimensionD1Node.removeAllChildren(); 
        aspectEDimensionD2Node.removeAllChildren();
        aspectEDimensionD3Node.removeAllChildren();
        aspectEDimensionD4Node.removeAllChildren();
        aspectEDimensionMalomernostNode.removeAllChildren();
        aspectEDimensionMnogomernostNode.removeAllChildren();
        aspectEDimensionOdnomernostNode.removeAllChildren();
        aspectEDimensionIndividualnostNode.removeAllChildren();
        aspectEVMMentalNode.removeAllChildren();
        aspectEVMVitalNode.removeAllChildren();
        aspectSSignPlusNode.removeAllChildren();
        aspectSSignMinusNode.removeAllChildren();	
        aspectSDimensionD1Node.removeAllChildren();	
        aspectSDimensionD2Node.removeAllChildren();	
        aspectSDimensionD3Node.removeAllChildren();	
        aspectSDimensionD4Node.removeAllChildren();	
        aspectSDimensionMalomernostNode.removeAllChildren();	
        aspectSDimensionMnogomernostNode.removeAllChildren();	
        aspectSDimensionOdnomernostNode.removeAllChildren();	
        aspectSDimensionIndividualnostNode.removeAllChildren();	
       aspectSVMMentalNode.removeAllChildren(); 	
       aspectSVMVitalNode.removeAllChildren();  	
        aspectFSignPlusNode.removeAllChildren();	
        aspectFSignMinusNode.removeAllChildren();	
        aspectFDimensionD1Node.removeAllChildren();	
        aspectFDimensionD2Node.removeAllChildren();	
        aspectFDimensionD3Node.removeAllChildren();	
        aspectFDimensionD4Node.removeAllChildren();	
        aspectFDimensionMalomernostNode.removeAllChildren();	
        aspectFDimensionMnogomernostNode.removeAllChildren();	
        aspectFDimensionOdnomernostNode.removeAllChildren();	
        aspectFDimensionIndividualnostNode.removeAllChildren();	
        aspectFVMMentalNode.removeAllChildren();	
        aspectFVMVitalNode.removeAllChildren();	
        aspectTSignPlusNode.removeAllChildren();	
        aspectTSignMinusNode.removeAllChildren();	
        aspectTDimensionD1Node.removeAllChildren();
        aspectTDimensionD2Node.removeAllChildren();
        aspectTDimensionD3Node.removeAllChildren();
        aspectTDimensionD4Node.removeAllChildren();
        aspectTDimensionMalomernostNode.removeAllChildren(); 	
        aspectTDimensionMnogomernostNode.removeAllChildren();	
        aspectTDimensionOdnomernostNode.removeAllChildren(); 	
        aspectTDimensionIndividualnostNode.removeAllChildren();	        
        aspectTVMMentalNode.removeAllChildren();	
        aspectTVMVitalNode.removeAllChildren(); 	
        aspectISignPlusNode.removeAllChildren();	
        aspectISignMinusNode.removeAllChildren();	
        aspectIDimensionD1Node.removeAllChildren();
        aspectIDimensionD2Node.removeAllChildren();
        aspectIDimensionD3Node.removeAllChildren();
        aspectIDimensionD4Node.removeAllChildren();
        aspectIDimensionMalomernostNode.removeAllChildren();
        aspectIDimensionMnogomernostNode.removeAllChildren();
        aspectIDimensionOdnomernostNode.removeAllChildren();
        aspectIDimensionIndividualnostNode.removeAllChildren();
        aspectIVMMentalNode.removeAllChildren();
        aspectIVMVitalNode.removeAllChildren();
        	
        	    dimensionD1LNode.removeAllChildren(); 
        		dimensionD1PNode.removeAllChildren(); 
        		dimensionD1RNode.removeAllChildren(); 
        		dimensionD1ENode.removeAllChildren(); 
        		dimensionD1SNode.removeAllChildren(); 
        		dimensionD1FNode.removeAllChildren(); 
        		dimensionD1TNode.removeAllChildren(); 
        		dimensionD1INode.removeAllChildren(); 
        	
        		dimensionD2LNode.removeAllChildren(); 
        		dimensionD2PNode.removeAllChildren(); 
        		dimensionD2RNode.removeAllChildren(); 
        		dimensionD2ENode.removeAllChildren(); 
        		dimensionD2SNode.removeAllChildren(); 
        		dimensionD2FNode.removeAllChildren(); 
        		dimensionD2TNode.removeAllChildren(); 
        		dimensionD2INode.removeAllChildren(); 
        		                   
        	
        		dimensionD3LNode.removeAllChildren(); 
        		dimensionD3PNode.removeAllChildren(); 
        		dimensionD3RNode.removeAllChildren(); 
        		dimensionD3ENode.removeAllChildren(); 
        		dimensionD3SNode.removeAllChildren(); 
        		dimensionD3FNode.removeAllChildren(); 
        		dimensionD3TNode.removeAllChildren(); 
        		dimensionD3INode.removeAllChildren(); 
        		                   
      
        		dimensionD4LNode.removeAllChildren(); 
        		dimensionD4PNode.removeAllChildren(); 
        		dimensionD4RNode.removeAllChildren(); 
        		dimensionD4ENode.removeAllChildren(); 
        		dimensionD4SNode.removeAllChildren(); 
        		dimensionD4FNode.removeAllChildren(); 
        		dimensionD4TNode.removeAllChildren(); 
        		dimensionD4INode.removeAllChildren(); 
        		                   
        	
        		dimensionMaloLNode.removeAllChildren(); 
        		dimensionMaloPNode.removeAllChildren(); 
        		dimensionMaloRNode.removeAllChildren(); 
        		dimensionMaloENode.removeAllChildren(); 
        		dimensionMaloSNode.removeAllChildren(); 
        		dimensionMaloFNode.removeAllChildren(); 
        		dimensionMaloTNode.removeAllChildren(); 
        		dimensionMaloINode.removeAllChildren(); 
        		                   
        	
        		dimensionMnogoLNode.removeAllChildren();
        		dimensionMnogoPNode.removeAllChildren();
        		dimensionMnogoRNode.removeAllChildren();
        		dimensionMnogoENode.removeAllChildren();
        		dimensionMnogoSNode.removeAllChildren();
        		dimensionMnogoFNode.removeAllChildren();
        		dimensionMnogoTNode.removeAllChildren();
        		dimensionMnogoINode.removeAllChildren();	
        		
        		
           		dimensionIndiLNode.removeAllChildren();
        		dimensionIndiPNode.removeAllChildren();
        		dimensionIndiRNode.removeAllChildren();
        		dimensionIndiENode.removeAllChildren();
        		dimensionIndiSNode.removeAllChildren();
        		dimensionIndiFNode.removeAllChildren();
        		dimensionIndiTNode.removeAllChildren();
        		dimensionIndiINode.removeAllChildren();	

           		dimensionOdnoLNode.removeAllChildren();
        		dimensionOdnoPNode.removeAllChildren();
        		dimensionOdnoRNode.removeAllChildren();
        		dimensionOdnoENode.removeAllChildren();
        		dimensionOdnoSNode.removeAllChildren();
        		dimensionOdnoFNode.removeAllChildren();
        		dimensionOdnoTNode.removeAllChildren();
        		dimensionOdnoINode.removeAllChildren();	
        	
        		blockSuperegoNode.removeAllChildren(); 
        		blockSuperidNode.removeAllChildren(); 
        		blockLINode.removeAllChildren(); 
        		blockLFNode.removeAllChildren(); 
        		blockPTNode.removeAllChildren(); 
        		blockPSNode.removeAllChildren(); 
        		blockRINode.removeAllChildren(); 
        		blockRFNode.removeAllChildren(); 
        		blockETNode.removeAllChildren(); 
        		blockESNode.removeAllChildren(); 
        		blockSPNode.removeAllChildren(); 
        		blockSENode.removeAllChildren(); 
        		blockFLNode.removeAllChildren(); 
        		blockFRNode.removeAllChildren(); 
        		blockTPNode.removeAllChildren(); 
        		blockTENode.removeAllChildren(); 
        		blockILNode.removeAllChildren(); 
        		blockIRNode.removeAllChildren(); 
        
                signPlusLNode.removeAllChildren();
                signPlusPNode.removeAllChildren();
                signPlusRNode.removeAllChildren();
                signPlusENode.removeAllChildren();
                signPlusSNode.removeAllChildren();
                signPlusFNode.removeAllChildren();
                signPlusTNode.removeAllChildren();
                signPlusINode.removeAllChildren();
        
                signMinusLNode.removeAllChildren(); 
                signMinusPNode.removeAllChildren(); 
                signMinusRNode.removeAllChildren(); 
                signMinusENode.removeAllChildren(); 
                signMinusSNode.removeAllChildren(); 
                signMinusFNode.removeAllChildren(); 
                signMinusTNode.removeAllChildren(); 
                signMinusINode.removeAllChildren(); 
        
                doubtNode.removeAllChildren();
                jumpNode.removeAllChildren();
                
                jc.clear();
	}

	public JScrollPane getContainer(){
		JScrollPane sp = new JScrollPane(this);
		sp.setPreferredSize(new Dimension (200,500));
		return  sp;
	}
	
   private void makeTreeStructure(){

	   rootNode.add(aspectNode);
	   
	   aspectNode.add(aspectLNode);
	   	aspectLNode.add(aspectLSignNode);
	   		aspectLSignNode.add(aspectLSignPlusNode);
	   		aspectLSignNode.add(aspectLSignMinusNode);
	   	aspectLNode.add(aspectLDimensionNode);
	   		aspectLDimensionNode.add(aspectLDimensionD1Node);
	   		aspectLDimensionNode.add(aspectLDimensionD2Node);
	   		aspectLDimensionNode.add(aspectLDimensionD3Node);
	   		aspectLDimensionNode.add(aspectLDimensionD4Node);
	   		aspectLDimensionNode.add(aspectLDimensionMalomernostNode );
	   		aspectLDimensionNode.add(aspectLDimensionMnogomernostNode);
	   		aspectLDimensionNode.add(aspectLDimensionOdnomernostNode );
	   		aspectLDimensionNode.add(aspectLDimensionIndividualnostNode);
	   	aspectLNode.add(aspectLVMNode);
	   		aspectLVMNode.add(aspectLVMMentalNode);
	   		aspectLVMNode.add(aspectLVMVitalNode);

	   aspectNode.add(aspectPNode);
			   	aspectPNode.add(aspectPSignNode);
		   		aspectPSignNode.add(aspectPSignPlusNode);
		   		aspectPSignNode.add(aspectPSignMinusNode);
		   aspectPNode.add(aspectPDimensionNode);
		   		aspectPDimensionNode.add(aspectPDimensionD1Node);
		   		aspectPDimensionNode.add(aspectPDimensionD2Node);
		   		aspectPDimensionNode.add(aspectPDimensionD3Node);
		   		aspectPDimensionNode.add(aspectPDimensionD4Node);
		   		aspectPDimensionNode.add(aspectPDimensionMalomernostNode );
		   		aspectPDimensionNode.add(aspectPDimensionMnogomernostNode);
		   		aspectPDimensionNode.add(aspectPDimensionOdnomernostNode );
		   		aspectPDimensionNode.add(aspectPDimensionIndividualnostNode);
		   aspectPNode.add(aspectPVMNode);
		   		aspectPVMNode.add(aspectPVMMentalNode);
		   		aspectPVMNode.add(aspectPVMVitalNode);	   
	   
	   aspectNode.add(aspectRNode);
		   	aspectRNode.add(aspectRSignNode);
	   		aspectRSignNode.add(aspectRSignPlusNode);
	   		aspectRSignNode.add(aspectRSignMinusNode);
	   	aspectRNode.add(aspectRDimensionNode);
	   		aspectRDimensionNode.add(aspectRDimensionD1Node);
	   		aspectRDimensionNode.add(aspectRDimensionD2Node);
	   		aspectRDimensionNode.add(aspectRDimensionD3Node);
	   		aspectRDimensionNode.add(aspectRDimensionD4Node);
	   		aspectRDimensionNode.add(aspectRDimensionMalomernostNode );
	   		aspectRDimensionNode.add(aspectRDimensionMnogomernostNode);
	   		aspectRDimensionNode.add(aspectRDimensionOdnomernostNode );
	   		aspectRDimensionNode.add(aspectRDimensionIndividualnostNode);
	   	aspectRNode.add(aspectRVMNode);
	   		aspectRVMNode.add(aspectRVMMentalNode);
	   		aspectRVMNode.add(aspectRVMVitalNode);	   
	   
	   aspectNode.add(aspectENode);
		   	aspectENode.add(aspectESignNode);
	   		aspectESignNode.add(aspectESignPlusNode);
	   		aspectESignNode.add(aspectESignMinusNode);
	   	aspectENode.add(aspectEDimensionNode);
	   		aspectEDimensionNode.add(aspectEDimensionD1Node);
	   		aspectEDimensionNode.add(aspectEDimensionD2Node);
	   		aspectEDimensionNode.add(aspectEDimensionD3Node);
	   		aspectEDimensionNode.add(aspectEDimensionD4Node);
	   		aspectEDimensionNode.add(aspectEDimensionMalomernostNode );
	   		aspectEDimensionNode.add(aspectEDimensionMnogomernostNode);
	   		aspectEDimensionNode.add(aspectEDimensionOdnomernostNode );
	   		aspectEDimensionNode.add(aspectEDimensionIndividualnostNode);
	   	aspectENode.add(aspectEVMNode);
	   		aspectEVMNode.add(aspectEVMMentalNode);
	   		aspectEVMNode.add(aspectEVMVitalNode);	   
	   
	   aspectNode.add(aspectSNode);
		   	aspectSNode.add(aspectSSignNode);
	   		aspectSSignNode.add(aspectSSignPlusNode);
	   		aspectSSignNode.add(aspectSSignMinusNode);
	   	aspectSNode.add(aspectSDimensionNode);
	   		aspectSDimensionNode.add(aspectSDimensionD1Node);
	   		aspectSDimensionNode.add(aspectSDimensionD2Node);
	   		aspectSDimensionNode.add(aspectSDimensionD3Node);
	   		aspectSDimensionNode.add(aspectSDimensionD4Node);
	   		aspectSDimensionNode.add(aspectSDimensionMalomernostNode );
	   		aspectSDimensionNode.add(aspectSDimensionMnogomernostNode);
	   		aspectSDimensionNode.add(aspectSDimensionOdnomernostNode );
	   		aspectSDimensionNode.add(aspectSDimensionIndividualnostNode);
	   	aspectSNode.add(aspectSVMNode);
	   		aspectSVMNode.add(aspectSVMMentalNode);
	   		aspectSVMNode.add(aspectSVMVitalNode);	   
		   
	   aspectNode.add(aspectFNode);
		   	aspectFNode.add(aspectFSignNode);
	   		aspectFSignNode.add(aspectFSignPlusNode);
	   		aspectFSignNode.add(aspectFSignMinusNode);
	   	aspectFNode.add(aspectFDimensionNode);
	   		aspectFDimensionNode.add(aspectFDimensionD1Node);
	   		aspectFDimensionNode.add(aspectFDimensionD2Node);
	   		aspectFDimensionNode.add(aspectFDimensionD3Node);
	   		aspectFDimensionNode.add(aspectFDimensionD4Node);
	   		aspectFDimensionNode.add(aspectFDimensionMalomernostNode );
	   		aspectFDimensionNode.add(aspectFDimensionMnogomernostNode);
	   		aspectFDimensionNode.add(aspectFDimensionOdnomernostNode );
	   		aspectFDimensionNode.add(aspectFDimensionIndividualnostNode);
	   	aspectFNode.add(aspectFVMNode);
	   		aspectFVMNode.add(aspectFVMMentalNode);
	   		aspectFVMNode.add(aspectFVMVitalNode);
	   
	   aspectNode.add(aspectTNode);
		   	aspectTNode.add(aspectTSignNode);
	   		aspectTSignNode.add(aspectTSignPlusNode);
	   		aspectTSignNode.add(aspectTSignMinusNode);
	   	aspectTNode.add(aspectTDimensionNode);
	   		aspectTDimensionNode.add(aspectTDimensionD1Node);
	   		aspectTDimensionNode.add(aspectTDimensionD2Node);
	   		aspectTDimensionNode.add(aspectTDimensionD3Node);
	   		aspectTDimensionNode.add(aspectTDimensionD4Node);
	   		aspectTDimensionNode.add(aspectTDimensionMalomernostNode );
	   		aspectTDimensionNode.add(aspectTDimensionMnogomernostNode);
	   		aspectTDimensionNode.add(aspectTDimensionOdnomernostNode );
	   		aspectTDimensionNode.add(aspectTDimensionIndividualnostNode);
	   	aspectTNode.add(aspectTVMNode);
	   		aspectTVMNode.add(aspectTVMMentalNode);
	   		aspectTVMNode.add(aspectTVMVitalNode); 
	   
	   aspectNode.add(aspectINode);
		   	aspectINode.add(aspectISignNode);
	   		aspectISignNode.add(aspectISignPlusNode);
	   		aspectISignNode.add(aspectISignMinusNode);
	   	aspectINode.add(aspectIDimensionNode);
	   		aspectIDimensionNode.add(aspectIDimensionD1Node);
	   		aspectIDimensionNode.add(aspectIDimensionD2Node);
	   		aspectIDimensionNode.add(aspectIDimensionD3Node);
	   		aspectIDimensionNode.add(aspectIDimensionD4Node);
	   		aspectIDimensionNode.add(aspectIDimensionMalomernostNode );
	   		aspectIDimensionNode.add(aspectIDimensionMnogomernostNode);
	   		aspectIDimensionNode.add(aspectIDimensionOdnomernostNode );
	   		aspectIDimensionNode.add(aspectIDimensionIndividualnostNode);
	   	aspectINode.add(aspectIVMNode);
	   		aspectIVMNode.add(aspectIVMMentalNode);
	   		aspectIVMNode.add(aspectIVMVitalNode);

	   rootNode.add(dimensionNode); 


	   		dimensionNode.add(dimensionD1Node);
	   			dimensionD1Node.add(dimensionD1LNode);
	   			dimensionD1Node.add(dimensionD1PNode);
	   			dimensionD1Node.add(dimensionD1RNode);
	   			dimensionD1Node.add(dimensionD1ENode);
	   			dimensionD1Node.add(dimensionD1SNode);
	   			dimensionD1Node.add(dimensionD1FNode);
	   			dimensionD1Node.add(dimensionD1TNode);
	   			dimensionD1Node.add(dimensionD1INode);
	   		dimensionNode.add(dimensionD2Node);
		   		dimensionD2Node.add(dimensionD2LNode);
	   			dimensionD2Node.add(dimensionD2PNode);
	   			dimensionD2Node.add(dimensionD2RNode);
	   			dimensionD2Node.add(dimensionD2ENode);
	   			dimensionD2Node.add(dimensionD2SNode);
	   			dimensionD2Node.add(dimensionD2FNode);
	   			dimensionD2Node.add(dimensionD2TNode);
	   			dimensionD2Node.add(dimensionD2INode);
	   		dimensionNode.add(dimensionD3Node);
		   		dimensionD3Node.add(dimensionD3LNode);
	   			dimensionD3Node.add(dimensionD3PNode);
	   			dimensionD3Node.add(dimensionD3RNode);
	   			dimensionD3Node.add(dimensionD3ENode);
	   			dimensionD3Node.add(dimensionD3SNode);
	   			dimensionD3Node.add(dimensionD3FNode);
	   			dimensionD3Node.add(dimensionD3TNode);
	   			dimensionD3Node.add(dimensionD3INode);
	   		dimensionNode.add(dimensionD4Node);
		   		dimensionD4Node.add(dimensionD4LNode);
	   			dimensionD4Node.add(dimensionD4PNode);
	   			dimensionD4Node.add(dimensionD4RNode);
	   			dimensionD4Node.add(dimensionD4ENode);
	   			dimensionD4Node.add(dimensionD4SNode);
	   			dimensionD4Node.add(dimensionD4FNode);
	   			dimensionD4Node.add(dimensionD4TNode);
	   			dimensionD4Node.add(dimensionD4INode);
		   	dimensionNode.add(dimensionMaloNode);
	   			dimensionMaloNode.add(dimensionMaloLNode);
	   			dimensionMaloNode.add(dimensionMaloPNode);
	   			dimensionMaloNode.add(dimensionMaloRNode);
	   			dimensionMaloNode.add(dimensionMaloENode);
	   			dimensionMaloNode.add(dimensionMaloSNode);
	   			dimensionMaloNode.add(dimensionMaloFNode);
	   			dimensionMaloNode.add(dimensionMaloTNode);
	   			dimensionMaloNode.add(dimensionMaloINode);
	   		dimensionNode.add(dimensionMnogoNode);
	   			dimensionMnogoNode.add(dimensionMnogoLNode);
	   			dimensionMnogoNode.add(dimensionMnogoPNode);
	   			dimensionMnogoNode.add(dimensionMnogoRNode);
	   			dimensionMnogoNode.add(dimensionMnogoENode);
	   			dimensionMnogoNode.add(dimensionMnogoSNode);
	   			dimensionMnogoNode.add(dimensionMnogoFNode);
	   			dimensionMnogoNode.add(dimensionMnogoTNode);
	   			dimensionMnogoNode.add(dimensionMnogoINode);	
			dimensionNode.add(dimensionOdnoNode);
	   			dimensionOdnoNode.add(dimensionOdnoLNode);
	   			dimensionOdnoNode.add(dimensionOdnoPNode);
	   			dimensionOdnoNode.add(dimensionOdnoRNode);
	   			dimensionOdnoNode.add(dimensionOdnoENode);
	   			dimensionOdnoNode.add(dimensionOdnoSNode);
	   			dimensionOdnoNode.add(dimensionOdnoFNode);
	   			dimensionOdnoNode.add(dimensionOdnoTNode);
	   			dimensionOdnoNode.add(dimensionOdnoINode);
			dimensionNode.add(dimensionIndiNode);
	   			dimensionIndiNode.add(dimensionIndiLNode);
	   			dimensionIndiNode.add(dimensionIndiPNode);
	   			dimensionIndiNode.add(dimensionIndiRNode);
	   			dimensionIndiNode.add(dimensionIndiENode);
	   			dimensionIndiNode.add(dimensionIndiSNode);
	   			dimensionIndiNode.add(dimensionIndiFNode);
	   			dimensionIndiNode.add(dimensionIndiTNode);
	   			dimensionIndiNode.add(dimensionIndiINode);	
		   
	   rootNode.add(blockNode); 
	   	blockNode.add(blockSuperegoNode);
		blockNode.add(blockSuperidNode);
		blockNode.add(blockLINode );
		blockNode.add(blockLFNode );
		blockNode.add(blockPTNode );
		blockNode.add(blockPSNode );
		blockNode.add(blockRINode );
		blockNode.add(blockRFNode );
		blockNode.add(blockETNode );
		blockNode.add(blockESNode );
		blockNode.add(blockSPNode );
		blockNode.add(blockSENode );
		blockNode.add(blockFLNode );
		blockNode.add(blockFRNode );
		blockNode.add(blockTPNode );
		blockNode.add(blockTENode );
		blockNode.add(blockILNode );
		blockNode.add(blockIRNode );

		
		rootNode.add(signNode); 
			signNode.add(signPlusNode);
			 	signPlusNode.add(signPlusLNode);
			 	signPlusNode.add(signPlusRNode);
			 	signPlusNode.add(signPlusSNode);
			 	signPlusNode.add(signPlusTNode);
			 	signPlusNode.add(signPlusPNode);			 	
			 	signPlusNode.add(signPlusENode);			 	
			 	signPlusNode.add(signPlusFNode);			 	
			 	signPlusNode.add(signPlusINode);
			signNode.add(signMinusNode);
				signMinusNode.add(signMinusLNode);
				signMinusNode.add(signMinusRNode);
				signMinusNode.add(signMinusSNode);
				signMinusNode.add(signMinusTNode);
			 	signMinusNode.add(signMinusPNode);			 	
			 	signMinusNode.add(signMinusENode);			 	
			 	signMinusNode.add(signMinusFNode);			 	
			 	signMinusNode.add(signMinusINode);
	   
	   rootNode.add(doubtNode); 
	   rootNode.add(jumpNode);	   

   } // generateTree();



@Override
public void aDocumentChanged(ADocument doc) {
	updateTree();
	
}


public String getReport(){
String report = "";	

if (!aDoc.getADataMap().isEmpty()){

report = 
		"<br/>" +
		"<h2> Выявленные параметры функций ИМ: </h2>" +
//		"<br/>" +
		"Каждый из отмеченных экспертом фрагментов текста представляет собой анализ аспектного содержания фрагмента и <br/>" +
		"параметров обработки этой информации. <br/>" +
		"Приведенная ниже таблица иллюстрирует распределение ответов типируемого по параметрам модели А.<br/><br/>" +
		"<table title=\"function analysis\" border=2 width=\"80%\">" +
		"<tr>" 															+ "\n" +
		"	<th width=\"20%\">  </th>"					+ "\n" +
		"	<th width=\"10%\"> БЛ </th>"  					+ "\n" +
		"	<th width=\"10%\"> ЧЛ </th>"  					+ "\n" +
		"	<th width=\"10%\"> БЭ </th>"  					+ "\n" +
		"	<th width=\"10%\"> ЧЭ </th>"  					+ "\n" +
		"	<th width=\"10%\"> БС </th>"  					+ "\n" +
		"	<th width=\"10%\"> ЧС </th>"  					+ "\n" +
		"	<th width=\"10%\"> БИ </th>"  					+ "\n" +
		"	<th width=\"10%\"> ЧИ </th>"  					+ "\n" +
		"</tr>" 														+ "\n"  +
		"<tr>" 															+ "\n" +		
		"	<td style=\"font-weight:bold\"> Ментал 				</td>"	+ "\n" +	
		"		<td align=\"center\">" + aspectLVMMentalNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\">" + aspectPVMMentalNode.getChildCount()+ " </td>"  + "\n" +		
		"		<td align=\"center\">" + aspectRVMMentalNode.getChildCount()+ " </td>"  + "\n" +		
		"		<td align=\"center\" >" + aspectEVMMentalNode.getChildCount()+ " </td>"  + "\n" +		
		"		<td align=\"center\" >" + aspectSVMMentalNode.getChildCount()+ " </td>"  + "\n" +		
		"		<td align=\"center\" >" + aspectFVMMentalNode.getChildCount()+ " </td>"  + "\n" +		
		"		<td align=\"center\" >" + aspectTVMMentalNode.getChildCount()+ " </td>"  + "\n" +		
		"		<td align=\"center\" >" + aspectIVMMentalNode.getChildCount()+ " </td>"  + "\n" +		"</tr>" 														+ "\n"  +		
		"</tr>" 														+ "\n"  +		
		"<tr>" 															+ "\n" +		
		"	<td style=\"font-weight:bold\"> Витал					</td>"	+ "\n" +			
		"		<td align=\"center\" >" + aspectLVMVitalNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectPVMVitalNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectRVMVitalNode.getChildCount()+ " </td>"  + "\n" +		
		"		<td align=\"center\" >" + aspectEVMVitalNode.getChildCount()+ " </td>"  + "\n" +		
		"		<td align=\"center\" >" + aspectSVMVitalNode.getChildCount()+ " </td>"  + "\n" +		
		"		<td align=\"center\" >" + aspectFVMVitalNode.getChildCount()+ " </td>"  + "\n" +		
		"		<td align=\"center\" >" + aspectTVMVitalNode.getChildCount()+ " </td>"  + "\n" +		
		"		<td align=\"center\" >" + aspectIVMVitalNode.getChildCount()+ " </td>"  + "\n" +		
		"</tr>" 														+ "\n"  +		
		"<tr>" 															+ "\n" +		
		"	<td style=\"font-weight:bold\"> Знак \"+\" 			</td>"	+ "\n" +			
		"		<td align=\"center\" >" + aspectLSignPlusNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectPSignPlusNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectRSignPlusNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectESignPlusNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectSSignPlusNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectFSignPlusNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectTSignPlusNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectISignPlusNode.getChildCount()+ " </td>"  + "\n" +
		"</tr>" 														+ "\n"  +		
		"<tr>" 															+ "\n" +		
		"	<td style=\"font-weight:bold\"> Знак \"-\" 			</td>"	+ "\n" +			
		"		<td align=\"center\" >" + aspectLSignMinusNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectPSignMinusNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectRSignMinusNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectESignMinusNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectSSignMinusNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectFSignMinusNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectTSignMinusNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectISignMinusNode.getChildCount()+ " </td>"  + "\n" +
		"</tr>" 														+ "\n"  +		
		"<tr>" 															+ "\n" +		
		"	<td style=\"font-weight:bold\"> Ex 					</td>"	+ "\n" +			
		"		<td align=\"center\" >" + aspectLDimensionD1Node.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectPDimensionD1Node.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectRDimensionD1Node.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectEDimensionD1Node.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectSDimensionD1Node.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectFDimensionD1Node.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectTDimensionD1Node.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectIDimensionD1Node.getChildCount()+ " </td>"  + "\n" +
		"</tr>" 														+ "\n"  +		
		"<tr>" 															+ "\n" +		
		"	<td style=\"font-weight:bold\"> Nm 					</td>"	+ "\n" +			
		"		<td align=\"center\" >" + aspectLDimensionD2Node.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectPDimensionD2Node.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectRDimensionD2Node.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectEDimensionD2Node.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectSDimensionD2Node.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectFDimensionD2Node.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectTDimensionD2Node.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectIDimensionD2Node.getChildCount()+ " </td>"  + "\n" +
		"</tr>" 														+ "\n"  +		
		"<tr>" 															+ "\n" +		
		"	<td style=\"font-weight:bold\"> St 					</td>"	+ "\n" +			
		"		<td align=\"center\" >" + aspectLDimensionD3Node.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectPDimensionD3Node.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectRDimensionD3Node.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectEDimensionD3Node.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectSDimensionD3Node.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectFDimensionD3Node.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectTDimensionD3Node.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectIDimensionD3Node.getChildCount()+ " </td>"  + "\n" +
		"</tr>" 														+ "\n"  +		
		"<tr>" 															+ "\n" +		
		"	<td style=\"font-weight:bold\"> Tm 					</td>"	+ "\n" +			
		"		<td align=\"center\" >" + aspectLDimensionD4Node.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectPDimensionD4Node.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectRDimensionD4Node.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectEDimensionD4Node.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectSDimensionD4Node.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectFDimensionD4Node.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectTDimensionD4Node.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectIDimensionD4Node.getChildCount()+ " </td>"  + "\n" +
		"</tr>" 														+ "\n"  +		
		"<tr>" 															+ "\n" +		
		"	<td style=\"font-weight:bold\"> Одномерность 			</td>"	+ "\n" +			
		"		<td align=\"center\" >" + aspectLDimensionOdnomernostNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectPDimensionOdnomernostNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectRDimensionOdnomernostNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectEDimensionOdnomernostNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectSDimensionOdnomernostNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectFDimensionOdnomernostNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectTDimensionOdnomernostNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectIDimensionOdnomernostNode.getChildCount()+ " </td>"  + "\n" +
		"</tr>" 														+ "\n"  +		
		"<tr>" 															+ "\n" +		
		"	<td style=\"font-weight:bold\"> Маломерность 			</td>"	+ "\n" +			
		"		<td align=\"center\" >" + aspectLDimensionMalomernostNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectPDimensionMalomernostNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectRDimensionMalomernostNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectEDimensionMalomernostNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectSDimensionMalomernostNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectFDimensionMalomernostNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectTDimensionMalomernostNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectIDimensionMalomernostNode.getChildCount()+ " </td>"  + "\n" +

		"</tr>" 														+ "\n"  +		
		"<tr>" 															+ "\n" +		
		"	<td style=\"font-weight:bold\"> Многомерность 			</td>"	+ "\n" +			
		"		<td align=\"center\" >" + aspectLDimensionMnogomernostNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectPDimensionMnogomernostNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectRDimensionMnogomernostNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectEDimensionMnogomernostNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectSDimensionMnogomernostNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectFDimensionMnogomernostNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectTDimensionMnogomernostNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectIDimensionMnogomernostNode.getChildCount()+ " </td>"  + "\n" +

		"</tr>" 														+ "\n"  +		
		"<tr>" 															+ "\n" +		
		"	<td style=\"font-weight:bold\"> Индивидуальность 		</td>"	+ "\n" +			
		"		<td align=\"center\" >" + aspectLDimensionIndividualnostNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectPDimensionIndividualnostNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectRDimensionIndividualnostNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectEDimensionIndividualnostNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectSDimensionIndividualnostNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectFDimensionIndividualnostNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectTDimensionIndividualnostNode.getChildCount()+ " </td>"  + "\n" +
		"		<td align=\"center\" >" + aspectIDimensionIndividualnostNode.getChildCount()+ " </td>"  + "\n" +

		"</tr>" 																	   + "\n" +		
		"</table>";

//Переводы
if (!jc.isEmpty()){
	
	report +=
			"<br/>" +
			"<h2> Переводы управления </h2>" +
			//"<br/>" +
			"Это наблюдаемый перевод ответа из одного аспекта в другой. <br/>" +
			"Перевод осуществляется: <br/>" +
			"1) из  менее мерной функции в более мерную функцию (внутри блока или кольца); <br/>" +
			"2) из витала в ментал.	<br/><br/>" +
			"<table title=\"jumps\" border=2 width=\"80%\">" +
			"<tr>" 															+ "\n" +
			"	<th width=\"20%\">Переводы <br/>из функции "+'\u25ba'+"<br/>в функцию <br/> "+'\u25bc'+" </th>"		+ "\n" +
			"	<th width=\"10%\"> БЛ </th>"  					+ "\n" +
			"	<th width=\"10%\"> ЧЛ </th>"  					+ "\n" +
			"	<th width=\"10%\"> БЭ </th>"  					+ "\n" +
			"	<th width=\"10%\"> ЧЭ </th>"  					+ "\n" +
			"	<th width=\"10%\"> БС </th>"  					+ "\n" +
			"	<th width=\"10%\"> ЧС </th>"  					+ "\n" +
			"	<th width=\"10%\"> БИ </th>"  					+ "\n" +
			"	<th width=\"10%\"> ЧИ </th>"  					+ "\n" +
			"</tr>" 														+ "\n"  +
			"<tr>" 															+ "\n" +		
			"	<td style=\"font-weight:bold\"> БЛ </td>"	+ "\n" +	
			"		<td align=\"center\">" + "X" + " </td>"  + "\n" +
			"		<td align=\"center\">" + jc.getJumpCount(AData.L, AData.P) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.L, AData.R) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.L, AData.E) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.L, AData.S) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.L, AData.F) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.L, AData.T) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.L, AData.I) + " </td>"  + "\n" +		
			"</tr>" 																	   + "\n"  +		
			"<tr>" 																		   + "\n" +		
			"	<td style=\"font-weight:bold\"> ЧЛ </td>"	+ "\n" +			
			"		<td align=\"center\">" + jc.getJumpCount(AData.P, AData.L) + " </td>"  + "\n" +
			"		<td align=\"center\">" + "X"  + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.P, AData.R) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.P, AData.E) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.P, AData.S) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.P, AData.F) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.P, AData.T) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.P, AData.I) + " </td>"  + "\n" +		
			"</tr>" 														+ "\n"  +		
			"<tr>" 															+ "\n" +		
			"	<td style=\"font-weight:bold\"> БЭ </td>"	+ "\n" +			
			"		<td align=\"center\">" + jc.getJumpCount(AData.R, AData.L) + " </td>"  + "\n" +
			"		<td align=\"center\">" + jc.getJumpCount(AData.R, AData.P) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + "X"  + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.R, AData.E) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.R, AData.S) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.R, AData.F) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.R, AData.T) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.R, AData.I) + " </td>"  + "\n" +		
		"</tr>" 														+ "\n"  +		
			"<tr>" 															+ "\n" +		
			"	<td style=\"font-weight:bold\"> ЧЭ </td>"	+ "\n" +			
			"		<td align=\"center\">" + jc.getJumpCount(AData.E, AData.L) + " </td>"  + "\n" +
			"		<td align=\"center\">" + jc.getJumpCount(AData.E, AData.P) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.E, AData.R) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + "X"  + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.E, AData.S) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.E, AData.F) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.E, AData.T) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.E, AData.I) + " </td>"  + "\n" +		
			"</tr>" 														+ "\n"  +		
			"<tr>" 															+ "\n" +		
			"	<td style=\"font-weight:bold\"> БС </td>"	+ "\n" +			
			"		<td align=\"center\">" + jc.getJumpCount(AData.S, AData.L) + " </td>"  + "\n" +
			"		<td align=\"center\">" + jc.getJumpCount(AData.S, AData.P) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.S, AData.R) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.S, AData.E) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + "X"  + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.S, AData.F) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.S, AData.T) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.S, AData.I) + " </td>"  + "\n" +		
			"</tr>" 														+ "\n"  +		
			"<tr>" 															+ "\n" +		
			"	<td style=\"font-weight:bold\"> ЧС </td>"	+ "\n" +			
			"		<td align=\"center\">" + jc.getJumpCount(AData.F, AData.L) + " </td>"  + "\n" +
			"		<td align=\"center\">" + jc.getJumpCount(AData.F, AData.P) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.F, AData.R) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.F, AData.E) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.F, AData.S) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + "X"  + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.F, AData.T) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.F, AData.I) + " </td>"  + "\n" +		
			"</tr>" 														+ "\n"  +		
			"<tr>" 															+ "\n" +		
			"	<td style=\"font-weight:bold\"> БИ </td>"	+ "\n" +			
			"		<td align=\"center\">" + jc.getJumpCount(AData.T, AData.L) + " </td>"  + "\n" +
			"		<td align=\"center\">" + jc.getJumpCount(AData.T, AData.P) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.T, AData.R) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.T, AData.E) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.T, AData.S) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.T, AData.F) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + "X"  + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.P, AData.I) + " </td>"  + "\n" +		
			"</tr>" 														+ "\n"  +		
			"<tr>" 															+ "\n" +		
			"	<td style=\"font-weight:bold\"> ЧИ </td>"	+ "\n" +			
			"		<td align=\"center\">" + jc.getJumpCount(AData.I, AData.L) + " </td>"  + "\n" +
			"		<td align=\"center\">" + jc.getJumpCount(AData.I, AData.P)  + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.I, AData.R) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.I, AData.E) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.I, AData.S) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.I, AData.F) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + jc.getJumpCount(AData.I, AData.T) + " </td>"  + "\n" +		
			"		<td align=\"center\">" + "X"  + " </td>"  + "\n" +		
			"</tr>" 														+ "\n"  +	
			"</table>";
}
} // end if !ADataMap.isEmpty() 
 else {
	 report = 
			"<br/>" +
			"<h2> В документе отсутствует анализ </h2>"+
	 		"<br/>";
 }

			
return report;	
}




}
