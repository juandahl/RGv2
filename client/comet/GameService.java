package edu.isistan.rolegame.client.comet;

import java.util.Date;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import edu.isistan.rolegame.shared.Game;
import edu.isistan.rolegame.shared.GamePlayer;
import edu.isistan.rolegame.shared.comm.ArgumentMessage;
import edu.isistan.rolegame.shared.comm.PlayerInformMessage;
import edu.isistan.rolegame.shared.comm.StatusMessage;

/**
 * This is the interface for the chat communicating with the chat server.
 * 
 * @author Richard Zschech
 */
@RemoteServiceRelativePath("gameService")
public interface GameService extends RemoteService {
	
	/**
	 * Obtiene el nombre de usuario logueado actualmente.
	 * 
	 * @return
	 * @throws ChatException
	 */
	public String getUsername() throws ChatException;
	
	public GamePlayer getGamePlayer() throws ChatException;
	
	/**
	 * Logue y configura una CometSession en el servidor.
	 * 
	 * @param player
	 * @throws ChatException
	 */
	public GamePlayer login(String name, String password, String locale) throws ChatException;
	
	/**
	 * Desloguea y destruye la CometSession en el servidor.
	 * @param player Jugador
	 * @param game Juego en el que estaba participando el jugador al momento del logout, sino estaba participando
	 * de ningún juego es <code>null</code>.
	 * @throws ChatException
	 */
	public void logout(GamePlayer player, Game game) throws ChatException;
	
	/**
	 * Envía un mensaje a "todos" los usuarios del servidor.
	 * 
	 * @param message
	 * @throws ChatException
	 */
	public void send(Game game, String message) throws ChatException;
	
	
	/**
	 * Envía un PlayerInformMessage al servidor.
	 * 
	 * @param message
	 * @throws ChatException
	 */
	public void send(Game game, PlayerInformMessage message) throws ChatException;
	
	/**
	 * Envia una actualización de estado a todos los usuarios.
	 * 
	 * @param status
	 * @throws ChatException
	 */
	public void setStatus(Game game, StatusMessage status) throws ChatException;
	
	/**
	 * Crea una nueva partida en el servidor.
	 */
	public Game createGame(Integer n, String locale) throws ChatException;
	
	/**
	 * Une al jugador <code>player</code> a la partida <code>game</code>.
	 * @param player Jugador que se está uniendo.
	 * @param game Partida.
	 * @return Game La partida o null si hubo un error
	 */
	public Game joinToGame(GamePlayer player, Game game) throws ChatException;
	
	/**
	 * Retorna al jugador <code>player</code> a la partida <code>game</code>.
	 * @param player Jugador que se está re-uniendo.
	 * @param game Partida.
	 */
	public void reJoinToGame(GamePlayer player, Game game) throws ChatException;
	
	/**
	 * El jugador <code>player</code> abandona el juego <code>game</code>.
	 * @param player Jugador que abandona la partida.
	 * @param game Partida.
	 */
	public void leaveGame(GamePlayer player, Game game) throws ChatException;
	
	/**
	 * Registra un nuevo juego
	 * @param username Nombre de usuario
	 * @param password Contraseña (encriptada)
	 * @param email Correo electrónico
	 * @param sex Sexo M o F
	 * @param birthday Fecha de nacimiento
	 */
	public void register(String username, String password, String email, char sex, Date birthday, Integer xp, String locale) throws ChatException;

	/**
	 * Envía una invitación para participar de una partida
	 * @param game Partida a participar
	 * @param player Jugador invitado
	 * @throws ChatException 
	 */
	public void sendInvite(Game game, GamePlayer player) throws ChatException;
	
	public void sendArgument(Game game, ArgumentMessage messsage) throws ChatException;
	/**
	 * Envía un argumento, actualmente de tipo CompArgumentMessage. Es el argumento completo de una persona.
	 */
}
