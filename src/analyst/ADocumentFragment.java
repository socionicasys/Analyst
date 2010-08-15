package analyst;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import javax.swing.text.AttributeSet;

import analyst.ADocument.ASection;

public class ADocumentFragment implements Transferable, Serializable{
	
	public static String MIME_TYPE =  "application/x-java-serialized-object; class=analyst.ADocumentFragment";
	
	private String text;
	private HashMap <DocSection, AttributeSet> styleMap;
	private HashMap <DocSection, AData> aDataMap;

	public ADocumentFragment(){}
	
	public ADocumentFragment(	String text, 
								HashMap <DocSection, AttributeSet> styleMap, 
								HashMap <DocSection, AData> aDataMap) {
		
		this.text =text;
		this.styleMap = styleMap;
		this.aDataMap = aDataMap;
	}
	
	public String getText() {return text;}
	public HashMap <DocSection, AttributeSet>getStyleMap() {return styleMap;}
	public HashMap <DocSection, AData> getaDataMap() {return aDataMap;}

	@Override
	public Object getTransferData(DataFlavor arg0)
			throws UnsupportedFlavorException, IOException {
			if(!arg0.getMimeType().equals(MIME_TYPE)) return null;
			return this;

	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		DataFlavor df = null;
		try {
			df = new DataFlavor(MIME_TYPE);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (df !=null)return new DataFlavor[] {df};
		else return null;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor arg0) {
		if(arg0.getMimeType().equals(MIME_TYPE)) return true;
		else return false;
	}

}
