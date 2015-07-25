package edu.isistan.rolegame.shared.comm;

import java.util.Date;

import edu.isistan.rolegame.client.ClientGameManager;
import edu.isistan.rolegame.shared.Game;
import edu.isistan.rolegame.shared.GamePlayer;
import edu.isistan.rolegame.shared.Round;

public abstract class ArgumentMessage extends GameMessage /*
														 * implements
														 * Serializable
														 */{
	// private String tipo;
	protected GamePlayer player;
	protected Long id;
	private Game game;
	private Round round;
	private Date date;
	private static final long serialVersionUID = 10L;

	public ArgumentMessage(){
		player = null;
	}
	
	public ArgumentMessage(GamePlayer player) {
		this.player = player;
	}

	/*
	 * public String getNombre() { return tipo; }
	 * 
	 * public void setTipo(String tipo) { this.tipo = tipo; }
	 */

	public abstract String toString();

	public void showEffect(ClientGameManager client) {
		UserMessage msg = new UserMessage(UserMessage.PLAYER_GAME_MESSAGE,
				toString(), player);
		client.getBoard().postMessage(msg);
	}

	public GamePlayer getPlayer() {
		return player;
	}

	public void setPlayer(GamePlayer player) {
		this.player = player;
	}

	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = game;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Round getRound() {
		return round;
	}

	public void setRound(Round round) {
		this.round = round;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

}
