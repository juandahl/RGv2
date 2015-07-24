package edu.isistan.rolegame.shared.comm;

import edu.isistan.rolegame.client.ClientGameManager;
import edu.isistan.rolegame.shared.GamePlayer;

public abstract class ArgumentMessage extends GameMessage /*implements Serializable*/{
	//private String tipo;
	protected GamePlayer player;
	protected Long id;
	private static final long serialVersionUID = 10L;
	
	public ArgumentMessage(GamePlayer player) {
		this.player=player;
	}

	/*public String getNombre() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}*/

	public GamePlayer getPlayer() {
		return player;
	}

	public void setPlayer(GamePlayer player) {
		this.player = player;
	}
	
	public abstract String toString();

	public void showEffect(ClientGameManager client) {
		UserMessage msg = new UserMessage(UserMessage.PLAYER_GAME_MESSAGE,toString(),player);
		client.getBoard().postMessage(msg);
	}
}
