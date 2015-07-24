package edu.isistan.rolegame.shared.comm;

import java.io.Serializable;

import edu.isistan.rolegame.shared.GamePlayer;

public class SimpleArgumentMessage extends ArgumentElemMessage implements Serializable{
	
	private String message;
	private static final long serialVersionUID = -1045198721445121548L;
	// participante que realiza el argumento

	public SimpleArgumentMessage(String message, GamePlayer player) {
		super(player);
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage (String message){
		this.message = message;
	}
	
	public String toString(){
		return message;
	}
	
}
