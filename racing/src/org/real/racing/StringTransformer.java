package org.real.racing;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import java.io.StringReader;
import java.io.StringWriter; 

public class StringTransformer {
	
	public String transform(String input, String xslFile ){
		TransformerFactory tFactory = TransformerFactory.newInstance();
		StringWriter writer = new StringWriter();
		try{
			Transformer transformer = tFactory.newTransformer(new StreamSource(xslFile));
			transformer.transform(new StreamSource(new StringReader(input)) , new StreamResult(writer));
		}
		catch(TransformerException e){
			return null;
		}
		return writer.toString();
	}
}
