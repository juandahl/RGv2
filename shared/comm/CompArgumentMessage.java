package edu.isistan.rolegame.shared.comm;

import java.io.Serializable;
import java.util.Vector;

import edu.isistan.rolegame.shared.GamePlayer;

public class CompArgumentMessage extends ArgumentMessage implements
		Serializable {
	private Vector<ArgumentMessage> components;
	private static final long serialVersionUID = -1045198721445121549L;
	//private RolegameMessages messages = GWT.create(RolegameMessages.class);

	public CompArgumentMessage(){
		super();
		components = new Vector<ArgumentMessage>();
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
		for (ArgumentMessage m: components){
			arg += m.toString() + " ";
		}
		return arg;
		
		/*if (components.size()>3){ //argument is "complete"
			//CLAIM
			arg+=components.get(0); //yes or no selected
			arg+=" "+messages.claimText()+" "+components.get(1); //player selected
			
			//DATA
			arg+= " " + messages.causeText() + " '" + components.get(2) + "'"; //previous message selected
			if (!components.get(3).toString().isEmpty()) //there is a warranty
				arg+=" "+messages.warrantyText()+" "+components.get(3);
			arg+=extraDataText(); //in case there is more data
		}
		return arg;*/
	}
	
	/*private String extraDataText(){
		return null;
		String extraTxt = new String();
		for (int i=4; i<components.size();i++){ //extra data
			if (!components.get(i).toString().isEmpty())
				if ((i%2)==1) //there is warranty for the data
					extraTxt+=" " + messages.warrantyText() + " " + components.get(i);
				else
					extraTxt+=" " + messages.extraCause() + " '" + components.get(i) + "'";
		}
		
		return extraTxt;*/
}
