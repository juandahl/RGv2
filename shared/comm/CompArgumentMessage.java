package edu.isistan.rolegame.shared.comm;

import java.io.Serializable;
import java.util.Vector;

import com.google.gwt.core.client.GWT;

import edu.isistan.rolegame.shared.GamePlayer;
import edu.isistan.rolegame.shared.ext.RolegameMessages;

public class CompArgumentMessage extends ArgumentMessage implements
		Serializable {
	private Vector<ArgumentMessage> components;
	private static final long serialVersionUID = -1045198721445121549L;
	private RolegameMessages messages = GWT.create(RolegameMessages.class);

	public CompArgumentMessage(){
		super();
		components = null;
	}
	
	public CompArgumentMessage(GamePlayer player) {
		super(player);
		components = new Vector<ArgumentMessage>();
	}

	public void addElement(ArgumentMessage element) {
		components.add(element);
	}

	public String toString() {
		String arg = new String();
		
		if (components.size()>2){
			//CLAIM
			arg+=components.get(0); //yes or no
			arg+=" "+messages.claimText()+" "+components.get(1); //player selected
			
			//data
			arg+=" "+messages.causeText()+" '"+components.get(2)+"'"; //previous message selected
			if (components.size()>3 && !components.get(3).toString().isEmpty()) //there is a warranty
				arg+=" "+messages.warrantyText()+" "+components.get(3);
			System.out.println("Previously: "+arg);
			arg+=extraDataText(); //in case there is more data
		}
		return arg;
	}
	
	private String extraDataText(){
		String extraTxt = new String();
		System.out.println("Tamaño: "+components.size());
		for (int i=4; i<components.size();i++){ //extra data
			if ((i%2)==1 && !components.get(i).toString().isEmpty()) //there is warranty for the data
				extraTxt+=" "+messages.warrantyText()+" "+components.get(i);
			else
				if (!components.get(i).toString().isEmpty())
					extraTxt+=" "+messages.extraCause()+" '"+components.get(i)+"'";
		}
		System.out.println(extraTxt);
		return extraTxt;
	}
}
