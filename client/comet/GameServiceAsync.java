package edu.isistan.rolegame.client.comet;

import java.util.Date;
import java.util.Vector;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.isistan.rolegame.shared.Game;
import edu.isistan.rolegame.shared.GamePlayer;
import edu.isistan.rolegame.shared.comm.ArgumentMessage;
import edu.isistan.rolegame.shared.comm.PlayerInformMessage;
import edu.isistan.rolegame.shared.comm.StatusMessage;

public interface GameServiceAsync {
	public void getUsername(AsyncCallback<String> callback);
	
	public void getGamePlayer(AsyncCallback<GamePlayer> callback);
	
	public void login(String name, String password, String locale, AsyncCallback<GamePlayer> callback);
	
	public void logout(GamePlayer player, Game game, AsyncCallback<Void> callback);
	
	public void send(Game game, String message, AsyncCallback<Void> callback);
	
	public void send(Game game, PlayerInformMessage message, AsyncCallback<Void> callback);
	
	public void setStatus(Game game, StatusMessage status, AsyncCallback<Void> callback);
	
	public void createGame(Integer n, String locale, AsyncCallback<Game> callback);
	
	public void joinToGame(GamePlayer player, Game game, AsyncCallback<Game> callback);
	
	public void reJoinToGame(GamePlayer player, Game game, AsyncCallback<Void> callback);
	
	public void leaveGame(GamePlayer player, Game game, AsyncCallback<Void> callback);
	
	public void register(String username, String password, String email, char sex, Date birthday, Integer xp, String locale, AsyncCallback<Void> callback);

	public void sendInvite(Game game, GamePlayer player, AsyncCallback<Void> asyncCallback);
	
	public void send(Game game, ArgumentMessage message, AsyncCallback<Void> callback);

	public void loadArguments(Game game, String player,AsyncCallback<Vector<ArgumentMessage>> asyncCallback);
}	
