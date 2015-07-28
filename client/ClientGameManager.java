package edu.isistan.rolegame.client;

import java.io.Serializable;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import com.google.gwt.core.client.GWT;

import edu.isistan.rolegame.client.comet.Comet;
import edu.isistan.rolegame.client.graph.ArgumentDialog;
import edu.isistan.rolegame.client.graph.GameBoard;
import edu.isistan.rolegame.client.graph.GamesConsole;
import edu.isistan.rolegame.client.graph.GeneralGameBoard;
import edu.isistan.rolegame.client.graph.GeneralGamesConsole;
import edu.isistan.rolegame.shared.Game;
import edu.isistan.rolegame.shared.GamePlayer;
import edu.isistan.rolegame.shared.PlayerState;
import edu.isistan.rolegame.shared.comm.ArgumentMessage;
import edu.isistan.rolegame.shared.comm.CompArgumentMessage;
import edu.isistan.rolegame.shared.comm.GameMessage;
import edu.isistan.rolegame.shared.comm.PlayerInformMessage;
import edu.isistan.rolegame.shared.comm.SimpleArgumentMessage;
import edu.isistan.rolegame.shared.comm.UserMessage;
import edu.isistan.rolegame.shared.ext.RolegameMessages;

public class ClientGameManager {
	
	private GeneralGamesConsole console;
	private GeneralGameBoard board;
	private Comet comet;
	private GamePlayer owner;
	private GamePlayer master;
	private ArgumentDialog lastArgDialog;
	
	private Game currentgame;
	
	//private RolegameConstants constants = GWT.create(RolegameConstants.class);
	private RolegameMessages messages; // Definimos aca pero creamos en el constructor para no romper cuando se crea BotClientGameManager
	
	public ClientGameManager() {
		master = new GamePlayer("MASTER");
		try {
			messages = GWT.create(RolegameMessages.class);
		}
		catch (Exception e) {e.printStackTrace();
			System.out.println("Salto la ficha");}
	}
	
	/**
	 * Habilita el inicio de Sesión
	 */
	public void login() {
		// TODO modificar el login
		console.showLogonDialog();
	}
	
	/**
	 * Efectiviza el inicio de sesión en el servidor
	 * @param name Nombre de usuario
	 * @param password Contraseña
	 */
	public void login(String name, String password) {
		try {
			// Encripta el password y envía el login
			comet.login(name, MD5.md5(password));
		}
		catch (Exception e) {logginError(messages.errorEncryption());}
	}
	
	public void logginError(String errormsg) {
		console.informLoginErrors(errormsg);
	}
	
	/**
	 * Efectiviza el logout, renueva la consola, el tablero y el comet por si fuera un logout explícito
	 */
	public void logout() {
		comet.logout(owner, currentgame);
		try {
			// Renueva la consola y el tablero con una instancia del mismo ya creado
			//console = console.getClass().getConstructor(ClientGameManager.class).newInstance(this);
			//board = board.getClass().getConstructor(ClientGameManager.class).newInstance(this);
			console = new GamesConsole(this);
			//console.setClientGameManager(this);
			board.hide();
			currentgame = null;
			//board = new GameBoard(this); No hace falta pq se crea cuando se une a la partida
			//board.setClientGameManager(this);
			// Crea una nueva instancia de Comet
			comet = new Comet(this);
		}
		catch (Exception e) {}
	}
	
	public boolean sendMessage(String text) {
		UserMessage msg = new UserMessage(UserMessage.PLAYER_GAME_MESSAGE, text, owner);
		return this.sendMessage(msg);
	}
	
	public boolean sendMessage(UserMessage msg) {
		// TODO enviar directamente el UserMessage
		if (currentgame != null) {
			comet.sendMessage(currentgame, msg.getText());
			return true;
		}
		else
			return false;
	}
	
	public boolean sendPlayerInformMessage(String name) {
		if (currentgame != null) {
				PlayerInformMessage message = new PlayerInformMessage(owner, name, false);	
				comet.sendPlayerInformMessage(currentgame, message);
				return true;
		}
		else
			return false; // No hay una partida habilitada
	}
	
	public boolean postMessage(UserMessage msg) {
		return board.postMessage(msg);
	}
	
	/**
	 * Solicita unirse a la partida <code>game</code>
	 * @param game Partida a la que se desea unir
	 */
	public void joinToGame(Game game) {
		// Chequea que no este participando de una partida.
		if (currentgame == null) {
			if (game.getCount() < game.getnPlayers()) { // Chequea si se alcanzó la cantidad de jugadores máxima
				board = new GameBoard(this, game);
				//((GameBoard)board).show();
				comet.joinToGame(owner, game);
				//currentgame = game; causa probleba con el error PlayerJoined
			}
			else
				joinToGameError(messages.errorNPlayers());
		}
		else  // Nota: evita que cuando llegan multiples invitaciones se una a más de una partida.
			joinToGameError(messages.errorPlayerJoined());
	}
	
	public void joinToGameSuccessful(Game game) {
		// Dado que no hubo errores asigna el game (28022012)
		currentgame = game;
		// Muestra el tablero
		board.show();
	}
	
	public void joinToGameError(String msg) {
		// Informa el error
		//currentgame = null; no se setea el currentgame hasta el Ok del comet
		console.showAlert(msg, true);
	}
	
	public boolean reJoinToGame(Game game, Vector<UserMessage> oldmessages,Vector<GamePlayer> players, Vector<GamePlayer> eliminated, PlayerState pstate) {
		if (currentgame == null) {
			board = new GameBoard(this, game);
			((GameBoard)board).show();
			for(Enumeration<UserMessage> em = oldmessages.elements(); em.hasMoreElements();) // Pasar a setPreviousState TODO
				board.postMessage(em.nextElement());
			board.setPreviousState(players, eliminated, pstate);
			currentgame = game;
			return true;
		}
		else
			return false;
	}
	
	public boolean leaveGame(boolean isFinished) {
		if (currentgame != null) {
			if (!isFinished) // No finalizó (puede haber comenzado o no)
				comet.leaveGame(owner, currentgame);
			currentgame = null;
			// TODO Avisar al Server para que elimine al GAME
			return true;
		}
		else 
			return false;
	}
	
	public void createGame(int nPlayers, String locale) {
		if (nPlayers >= 4) { // Minimo número de jugadores disponibles
			//Game game = new Game(Integer.valueOf((int)System.currentTimeMillis()), nPlayers);
			comet.createGame(new Integer(nPlayers), locale);
			//return game;
		}
		//else
			//return null;
	}
	
	/**
	 * Envía una invitación para participar de una partida
	 */
	public void sendInvite(Game game, GamePlayer player) {
		comet.sendInvite(game, player);
	}
	
	/** Registra un nuevo usuario
	 * 
	 * @return Código de error.
	 */
	public void register(String username, String password1, String password2, String email, char sex, Date birthday) {
		Vector<String> errors = new Vector<String>(); // No error
		// Chequea integridad de los datos
		if (username.isEmpty() || password1.isEmpty() || password2.isEmpty() || email.isEmpty())
			errors.addElement(messages.errorRegNoData()); // Dato faltante
		if (birthday == null)
			errors.addElement(messages.errorRegBirthday()); // Error de fecha
		if (!password1.equals(password2))
			errors.addElement(messages.errorRegPass()); // Password no corresponde
		//if (password1.length() < 6)
		//	errors.addElement(" - La contrase\u00F1a debe tener al menos 6 caracteres."); // Password demasiado corto
		if (!email.matches("([a-zA-Z_.0-9])*@(\\w+\\.)(\\w+)(\\.\\w+)*"))
			errors.addElement(messages.errorRegEmail()); // Email incorrecto
		// Encripta el password antes de registrar al usuario
		try {
			password1 = MD5.md5(password1);
		}
		catch (Exception e) {errors.addElement(messages.errorEncryption());}
		
		if (errors.isEmpty())
			comet.register(username, password1, email, sex, birthday, 0);
		else
			registrationError(errors);
		
	}
	
	public void registrationSuccessful() {
		// Confirma la registración
		console.showAlert(messages.successfulLoginMessage(), true);
	}
	
	public void registrationError(String msg) {
		console.informRegistrationErrors(msg);
	}
	
	public void registrationError(Vector<String> errors) {
		String msg = messages.errorsReg();
		for (Enumeration<String> e = errors.elements(); e.hasMoreElements();)
			msg = msg + e.nextElement() + " ";
		registrationError(msg);
	}
	
	public void inviteError(String message) {
		console.showAlert(message, true);
		
	}
	
	public boolean sendArgument(Vector<String> arg_components){
		CompArgumentMessage argument = new CompArgumentMessage(owner); 
		for (String s: arg_components){
			argument.addElement(new SimpleArgumentMessage(s, owner));
		}
		return this.sendArgument(argument);
	}
	
	public boolean sendArgument(ArgumentMessage argument){
		if (currentgame != null){
			comet.sendArgument(currentgame, argument);
			return true;
		}
		else
			return false;
	}
	
	public boolean loadArguments(String player){
		if (currentgame != null){
			comet.loadArguments(currentgame ,player);
			return true;
		}
		else 
			return false;
	}
	
	public void loadArguments(Vector<ArgumentMessage>result){
		Vector<String>arguments = new Vector<String>();
		for (ArgumentMessage arg : result){
			arguments.add(arg.toString());
		}
		lastArgDialog.loadArguments(arguments);
	}
	
	// EVENTOS DE LA CONEXION CON EL SERVIDOR
	public void onConnected(int heartbeat) {
		//board.postMessage(new UserMessage(UserMessage.MASTER_GAME_MESSAGE, "CONECTADO", master));
	}
	
	public void onDisconnected() {
		//board.postMessage(new UserMessage(UserMessage.MASTER_GAME_MESSAGE, "DESCONECTADO", master));
	}
	
	public void onError(Throwable exception, boolean connected) {
		// TODO Manejar error de conexión
		if (board != null) {
			board.postMessage(new UserMessage(UserMessage.ERROR_GAME_MESSAGE, messages.errorConexion() + exception, master));
			exception.printStackTrace();
		}
	}
	
	public void onHeartbeat() {
	}
	
	public void onRefresh() {
		//board.postMessage(new UserMessage(UserMessage.MASTER_GAME_MESSAGE, "refresh", master));
	}
	
	public void onMessage(List<? extends Serializable> messages) {
		for (Serializable message : messages) {
			GameMessage msg = (GameMessage)message;
			msg.showEffect(this);
		}
	}
	
	// GETTERS & SETTERS

	public GeneralGameBoard getBoard() {
		return board;
	}

	public void setBoard(GeneralGameBoard board) {
		this.board = board;
	}

	public GeneralGamesConsole getConsole() {
		return console;
	}

	public void setConsole(GeneralGamesConsole console) {
		this.console = console;
	}

	public Game getCurrentgame() {
		return currentgame;
	}

	public void setCurrentgame(Game currentgame) {
		this.currentgame = currentgame;
	}

	public Comet getComet() {
		return comet;
	}

	public void setComet(Comet comet) {
		this.comet = comet;
	}

	public GamePlayer getOwner() {
		return owner;
	}

	public void setOwner(GamePlayer owner) {
		this.owner = owner;
		// Indica que el login fue correcto, por lo tanto muestra la consola
		console.show();
		//((GameBoard)board).show();
	}
	
	public void setLastArgDialog(ArgumentDialog lastArgDialog) {
		this.lastArgDialog = lastArgDialog;
	}
	
	public ArgumentDialog getLastArgDialog() {
		return lastArgDialog;
	}


	

}
