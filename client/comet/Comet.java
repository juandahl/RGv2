package edu.isistan.rolegame.client.comet;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import net.zschech.gwt.comet.client.CometClient;
import net.zschech.gwt.comet.client.CometListener;
import net.zschech.gwt.comet.client.CometSerializer;
import net.zschech.gwt.comet.client.SerialTypes;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.isistan.rolegame.client.ClientGameManager;
import edu.isistan.rolegame.shared.Game;
import edu.isistan.rolegame.shared.GamePlayer;
import edu.isistan.rolegame.shared.comm.ArgumentMessage;
import edu.isistan.rolegame.shared.comm.GameMessage;
import edu.isistan.rolegame.shared.comm.PlayerInformMessage;
import edu.isistan.rolegame.shared.comm.StatusMessage;

public class Comet {
	
	private GameServiceAsync gameService;
	private CometClient cometClient;
	
	private ClientGameManager gameman;
	
	@SerialTypes( { GameMessage.class, PlayerInformMessage.class })
	public static abstract class ChatCometSerializer extends CometSerializer {
	}
	
	public Comet(ClientGameManager gameman) {
		this.gameman = gameman;
		gameService = GWT.create(GameService.class);
	}
	
	public void login(String name, String password) {
		gameService.login(name, password, LocaleInfo.getCurrentLocale().getLocaleName(), new AsyncCallback<GamePlayer>() {
			
			@Override
			public void onSuccess(GamePlayer player) {
				gameman.setOwner(player);
				loggedOn(player);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				gameman.logginError(((ChatException)caught).getMessage());
			}
		});
	}

	public void logout(GamePlayer player, Game game) {
		gameService.logout(player, game, new AsyncCallback<Void>() {
			
			@Override
			public void onSuccess(Void result) {
				//cometClient.stop(); Da error
				//showLogonDialog();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				//cometClient.stop();
				//output(caught.toString(), "red");
			}
		});
	}

	public void loggedOn(GamePlayer player) {

		CometSerializer serializer = GWT.create(ChatCometSerializer.class);
		cometClient = new CometClient(GWT.getModuleBaseURL() + "comet", serializer, new CometListener() {
			public void onConnected(int heartbeat) {
				gameman.onConnected(heartbeat);
			}
			
			public void onDisconnected() {
				gameman.onDisconnected();
			}
			
			public void onError(Throwable exception, boolean connected) {
				gameman.onError(exception, connected);
			}
			
			public void onHeartbeat() {
				gameman.onHeartbeat();
			}
			
			public void onRefresh() {
				gameman.onRefresh();
			}
			
			public void onMessage(List<? extends Serializable> messages) {
				gameman.onMessage(messages);
			}
		});
		cometClient.start();
	}
	
	public void sendMessage(Game game, String message) {
		gameService.send(game, message, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
			}
			
			@Override
			public void onFailure(Throwable caught) {
			}
		});
	}
	
	public void sendPlayerInformMessage(Game game, PlayerInformMessage message) {
		gameService.send(game, message, new AsyncCallback<Void>() {
			
			@Override
			public void onSuccess(Void result) {		
			}
			
			@Override
			public void onFailure(Throwable caught) {	
			}
		});
	}
	
	public void sendArgument (Game game, ArgumentMessage message){
		gameService.send(game, message, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result){
			}
			
			@Override
			public void onFailure(Throwable caught){
			}
		});
	}
	
	public void setStatus(Game game, StatusMessage status) {
		gameService.setStatus(game, status, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
			}
			
			@Override
			public void onFailure(Throwable caught) {
			}
		});
	}
	
	public void createGame(Integer n, String locale) {
		gameService.createGame(n, locale, new AsyncCallback<Game>() {
			@Override
			public void onSuccess(Game game) {
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// TODO informar error
			}
		});
	}
	
	public void joinToGame(GamePlayer player, Game game) {
		gameService.joinToGame(player, game, new AsyncCallback<Game>() {
			
			@Override
			public void onSuccess(Game result) {
				gameman.joinToGameSuccessful(result);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				gameman.joinToGameError(caught.getMessage());
			}
		});
	}
	
	public void reJoinToGame(GamePlayer player, Game game) {
		gameService.reJoinToGame(player, game, new AsyncCallback<Void>() {
			
			@Override
			public void onSuccess(Void result) {
				
				
			}
			
			@Override
			public void onFailure(Throwable caught) {
			
				
			}
		});
	}
	
	public void leaveGame(GamePlayer player, Game game) {
		gameService.leaveGame(player, game, new AsyncCallback<Void>() {
			
			@Override
			public void onSuccess(Void result) {
				
			}
			
			@Override
			public void onFailure(Throwable caught) {
				
			}
		});
	}
	
	public void register(String username, String password, String email, char sex, Date birthday, Integer xp) {
		gameService.register(username, password, email, sex, birthday, xp,  LocaleInfo.getCurrentLocale().getLocaleName(), new AsyncCallback<Void>() {
			
			@Override
			public void onSuccess(Void result) {
				gameman.registrationSuccessful();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				gameman.registrationError(caught.getMessage());
				
			}
		});
	}

	public void sendInvite(Game game, GamePlayer player) {
		gameService.sendInvite(game, player, new AsyncCallback<Void>() {

			@Override
			public void onSuccess(Void result) {	
			}
			
			@Override
			public void onFailure(Throwable caught) {
				gameman.inviteError(caught.getMessage());
			}
		});
		
	}

}
