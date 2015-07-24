package edu.isistan.rolegame.shared.comm;

import java.io.Serializable;
import java.util.Vector;
import edu.isistan.rolegame.shared.GamePlayer;

public class CompArgumentMessage extends ArgumentElemMessage implements Serializable{
	private Vector<ArgumentElemMessage> components;
	private static final long serialVersionUID = -1045198721445121549L;

	
	public CompArgumentMessage(GamePlayer player) {
		super(player);
		components = new Vector<>();
	}

	
	public void addElement(ArgumentElemMessage element){
		components.add(element);
	}
	
	public String toString() {
		String arg = new String();
		for (ArgumentElemMessage a: components)
			arg += a.toString();
		return arg;
	}
	
	
}
