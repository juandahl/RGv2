package edu.isistan.rolegame.shared.comm;

import java.io.Serializable;
import java.util.Vector;
import edu.isistan.rolegame.shared.GamePlayer;

public class CompArgumentMessage extends ArgumentMessage implements Serializable{
	private Vector<ArgumentMessage> components;
	private static final long serialVersionUID = -1045198721445121549L;

	
	public CompArgumentMessage(GamePlayer player) {
		super(player);
		components = new Vector<>();
	}

	
	public void addElement(ArgumentMessage element){
		components.add(element);
	}
	
	public String toString() {
		String arg = new String();
		for (ArgumentMessage a: components)
			arg += a.toString();
		return arg;
	}
	
	
}
