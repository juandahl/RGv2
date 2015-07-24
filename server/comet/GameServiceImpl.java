package edu.isistan.rolegame.server.comet;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.HttpSession;

import net.zschech.gwt.comet.server.CometServlet;
import net.zschech.gwt.comet.server.CometSession;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.scb.gwt.web.server.i18n.GWTI18N;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.isistan.rolegame.client.comet.ChatException;
import edu.isistan.rolegame.client.comet.GameService;
import edu.isistan.rolegame.server.ServerGameManager;
import edu.isistan.rolegame.server.bot.BotSession;
import edu.isistan.rolegame.server.resources.DAOFactory;
import edu.isistan.rolegame.server.resources.MyDAOFactory;
import edu.isistan.rolegame.shared.Game;
import edu.isistan.rolegame.shared.GamePlayer;
import edu.isistan.rolegame.shared.GameSession;
import edu.isistan.rolegame.shared.comm.AddNewGameConsoleMessage;
import edu.isistan.rolegame.shared.comm.AddPlayerConsoleMessage;
import edu.isistan.rolegame.shared.comm.AddPlayerToGameConsoleMessage;
import edu.isistan.rolegame.shared.comm.ArgumentMessage;
import edu.isistan.rolegame.shared.comm.GameInviteConsoleMessage;
import edu.isistan.rolegame.shared.comm.GameMessage;
import edu.isistan.rolegame.shared.comm.InformAvailableGamesConsoleMessage;
import edu.isistan.rolegame.shared.comm.NewPlayerStatusMessage;
import edu.isistan.rolegame.shared.comm.PlayerInformMessage;
import edu.isistan.rolegame.shared.comm.RemoveGameConsoleMessage;
import edu.isistan.rolegame.shared.comm.RemovePlayerConsoleMessage;
import edu.isistan.rolegame.shared.comm.RemovePlayerFromGameConsoleMessage;
import edu.isistan.rolegame.shared.comm.RemovePlayerStatusMessage;
import edu.isistan.rolegame.shared.comm.StatusMessage;
import edu.isistan.rolegame.shared.comm.UserMessage;
import edu.isistan.rolegame.shared.ext.RolegameMessages;

/**
 * 
 * @author
 */
public class GameServiceImpl extends RemoteServiceServlet implements GameService {
	
	private static final long serialVersionUID = 1L;
	
	/** 
	 *  Administrador de la partida, controla los estados de la partida.
	 */
	//private ServerGameManager serverman;
	
	/**
	 * Mapea cada partida (idGame) con el correspondiente administrador de partidas.
	 */
	private ConcurrentMap<Integer, ServerGameManager> gamemans;
	/**
	 * Vector de partidas disponibles.
	 */
	private Vector<Game> games;
	
	/**
	 * Mapea a cada GamePlayer(nombre) a la CometSession utilizada para enrutar los mensajes.
	 */
	private ConcurrentMap<String, CometSession> users;
	
	/**
	 * Jugadores conectados al juego 
	 */
	private Vector<GamePlayer> players;
	/**
	 * Mapea un nombre de jugador con la partida que está disputando.
	 * 
	 */
	private ConcurrentMap<String, Game> playerInGame;
	 /**
	  * Mapea un nombre de jugador con una BotSession.
	  */
	private ConcurrentMap<String, BotSession> botsessions;
	/**
	 * Mapea un nombre de jugador con el juego en el cual fue reemplazado por un bot.
	 */
	private ConcurrentMap<String, Game> botgames;
	
	/**
	 * DAOFactory utilizado para persistir los objetos.
	 */
	private DAOFactory dao;
	
	private InvalidSessionChecker checker;
	
	// Lista de TAGS HTML válidos en los mensajes de los jugadores
	private Whitelist whitelist;
	
	public GameServiceImpl() {
		super();
		// Inicializa los ConcurrentHashMap
		users = new ConcurrentHashMap<String, CometSession>();
		players = new Vector<GamePlayer>();
		playerInGame = new ConcurrentHashMap<String, Game>();
		games = new Vector<Game>();
		gamemans = new ConcurrentHashMap<Integer, ServerGameManager>();
		botsessions = new ConcurrentHashMap<String, BotSession>();
		botgames = new ConcurrentHashMap<String, Game>();
		// Inicializa el DAOFactory para acceder a MySQL
		dao = new MyDAOFactory();
		// Configura la WhiteList
		whitelist = new Whitelist();
		whitelist.addTags("b","br","marquee","i");
		// Inicia el controlador de sesiones invalidas
		Timer timer = new Timer();
		checker = new InvalidSessionChecker(this);
		timer.schedule(checker, 600000, 600000);
	}
	
	@Override
	public String getUsername() throws ChatException {
		// Chequea si hay una sesión HTTP configurada.
		HttpSession httpSession = getThreadLocalRequest().getSession(false);
		if (httpSession == null) {
			return null;
		}
		
		// Devuelve el nombre de usuario.
		return (String) httpSession.getAttribute("username");
	}
	
	/**
	 * Devuelve el jugador 
	 */
	@Override
	public GamePlayer getGamePlayer() throws ChatException {
		// Chequea si hay una sesión HTTP configurada.
		HttpSession httpSession = getThreadLocalRequest().getSession(false);
		if (httpSession == null) {
			return null;
		}
		
		// Devuelve el jugador.
		return (GamePlayer) httpSession.getAttribute("game-player");
	}

	/**
	 * Registra al jugador <code>player</code> en la aplicación.
	 */
	@Override
	public GamePlayer login(String name, String password, String locale) throws ChatException {
		// Get or create the HTTP session for the browser
		HttpSession httpSession = getThreadLocalRequest().getSession();
		// Get or create the Comet session for the browser
		CometSession cometSession = CometServlet.getCometSession(httpSession);
		
		//Internalization: se crea un messages para cada usuario logueado
		RolegameMessages messages;
		try {
			messages = GWTI18N.create(RolegameMessages.class, locale);
		}
		catch (IOException ioe) {
			throw new ChatException("Error (internalization)");
		}
		
		// Valida el usuario y password
		GamePlayer player = dao.getPlayerDAO().validatePlayer(name, password);
		if (player == null)
			throw new ChatException(messages.errorLogin());
		
		// setup the mapping of user names to CometSessions
		if (users.putIfAbsent(player.getName(), cometSession) != null) {
			// Hay alguien conectado con ese nombre: puede ser un bot que lo está reemplazando
			if (!botgames.containsKey(player.getName())) {
				// Existe un jugador con ese nombre y no es un bot.
				// Ejecuta el chequeo de sesiones inválidas por si hubo un logout no registrado
				checkInvalidSessions();
				// Invalida la nueva sesión creada
				httpSession.invalidate();
				throw new ChatException(messages.errorPlayerLogged(player.getName()));
			}
			else {
				/*
				// Obtiene la BotSession
				BotSession botsession = botsessions.remove(player.getName());
				// Reemplaza BotSession por CommetSession
				// Avisa a la consola que hay un juego sin terminar
				Game game = botgames.remove(player.getName());
				// Obtiene el ServerGameManager
				ServerGameManager serverman = gamemans.get(game.getIdGame());
				// Envía el mensaje con la información de la partida
				InformPreviousGameConsoleMessage previous = new InformPreviousGameConsoleMessage(game,
						botsession.getUserMessages(), serverman.getAllPlayers(), 
						serverman.getEliminatedPlayers(), serverman.getPlayerState(player));

				setStatus(player, previous);
				*/
				
				/*for(Map.Entry<String, CometSession> map : users.entrySet()) {
					System.out.println("Player: " + map.getKey() + " Session: " + map.getValue() + "-" + map.getValue().isValid());
				}*/
				
				throw new ChatException(messages.errorBotLogged(player.getName()));

			}
		}
		else { // El usuario es nuevo, y no tiene ninguna partida empezada
			// Recuerda el usuario de la sesión
			httpSession.setAttribute("game-player", player);
			// Agrega el jugador al vector de jugadores.
			players.addElement(player);
		}
		
		// Persiste el login
		GameSession gamesession = dao.getPlayerDAO().login(player); 
		// Guarda la sesion
		httpSession.setAttribute("game-session", gamesession);
		// Guarda el messages (internalization)
		httpSession.setAttribute("messages-internalization", messages);
		
		// Informa al usuario recientemente conectado de las partidas disponibles
		InformAvailableGamesConsoleMessage inform = new InformAvailableGamesConsoleMessage(games);
		setStatus(player, inform);
		
		// Informa al nuevo usuario de los usuarios ya conectados
		for(Enumeration<GamePlayer> eplayers = players.elements(); eplayers.hasMoreElements();) {
			GamePlayer p = eplayers.nextElement();
			if (!p.equals(player)) { // No se informa él mismo, lo hace cuando informa a todos
				AddPlayerConsoleMessage existent = new AddPlayerConsoleMessage(p);
				enqueueMessage(cometSession, existent);
				//if (cometSession.isValid())
				//	cometSession.enqueue(existent);
			}
		}
		
		// Informa a las consolas (TODOS los usuarios) del nuevo jugador conectado
		AddPlayerConsoleMessage addplayer = new AddPlayerConsoleMessage(player);
		setStatus(players, addplayer);		
			
		// Devuelve el GamePlayer que acaba de iniciar sesión
		return player;
	}
	
	/**
	 * 
	 */
	@Override
	public void logout(GamePlayer player, Game game) throws ChatException {
		// check if there is a HTTP session setup.
		HttpSession httpSession = getThreadLocalRequest().getSession(false);
				
		if (httpSession == null) {
			throw new ChatException("No http session (logout)");
		}
		
		// Recupera el messages (internalization)
		RolegameMessages messages = (RolegameMessages)httpSession.getAttribute("messages-internalization");
		
		// check if there is a Comet session setup. In a larger application the HTTP session may have been
		// setup via other means.
		CometSession cometSession = CometServlet.getCometSession(httpSession, false);
		if (cometSession == null) {
			throw new ChatException(messages.errorNoSesion("no comet session (logout)"));
		}
		
		// check the user name parameter matches the HTTP sessions user name
		if (!player.equals(httpSession.getAttribute("game-player"))) {
			throw new ChatException(messages.errorNoSesion("(logout)"));
		}
		
		// Persiste el logout
		GameSession gamesession = (GameSession)httpSession.getAttribute("game-session");
		dao.getPlayerDAO().logout(gamesession);		

		if (game != null) { // El jugador estaba participando de una partida.
			//*leaveGame(player, game);
			// Obtiene el rol de jugador
			ServerGameManager serverman = gamemans.get(game.getIdGame());
			if (serverman.hasStarted() && !serverman.hasFinished()) {
				String role = serverman.getRole(player);
				// Crea el botsession
				BotSession botsession = new BotSession(this, player, game, role, serverman.getAllPlayers(), serverman.getPlayerState(player));

				// Reemplaza la commetSession por el bot
				users.replace(player.getName(), cometSession, botsession);
				cometSession.invalidate();
				// Almacena el usuario por si vuelve a la partida
				botgames.put(player.getName(), game);
				botsessions.put(player.getName(), botsession);
			}
			else { // Tvia no empezó o ya terminó el juego, entonces elimina al jugador normalmente
				// remove the mapping of user name to CometSession
				if (!serverman.hasFinished()) // Tvia no empezó
					leaveGame(player, game); // Sino ya fue eliminado el game
				users.remove(player.getName(), cometSession);
				players.remove(player);
			}
		}
		else {
			// remove the mapping of user name to CometSession
			//httpSession.invalidate();
			users.remove(player.getName(), cometSession);
			players.remove(player);
		}
		
		// Informa al resto de los usuarios de la desconexión
		RemovePlayerConsoleMessage remove = new RemovePlayerConsoleMessage(player);
		setStatus(players,remove);
		
		// Invalida la sesión
		try {
			httpSession.invalidate();
		}
		catch (IllegalStateException e) {System.out.println("La sesión ya fue invalidada.");}
		catch (Exception e) {System.out.println("Error al invalidar la sesión. " + e.getMessage());}
		
	}
	
	/**
	 * Envía un mensaje de jugador a todos los jugadores habilitados
	 */
	@Override
	public void send(Game game, String message) throws ChatException {
		// check if there is a HTTP session setup.
		HttpSession httpSession = getThreadLocalRequest().getSession(false);
		if (httpSession == null) {
			throw new ChatException();
		}
		
		// Recupera el messages (internalization)
		RolegameMessages messages = (RolegameMessages)httpSession.getAttribute("messages-internalization");
		
		// get the user name for the HTTP session.
		GamePlayer player = (GamePlayer) httpSession.getAttribute("game-player");
		if (player == null) {
			throw new ChatException(messages.errorNoSesion(""));
		}
		
		// Elimina el código HTML del mensaje, evita inyección de HTML
		message = Jsoup.clean(message, whitelist);
		
		// Crea el nuevo mensaje
		UserMessage userMessage = new UserMessage();
		userMessage.setSender(player);
		userMessage.setText(message);
		userMessage.setType(UserMessage.PLAYER_GAME_MESSAGE);
		
		// Obtiene el administrador de la partida.
		ServerGameManager serverman = gamemans.get(game.getIdGame());
		
		// Determina, según el estado del juego, a que set de Players enviar el mensaje
		Vector<GamePlayer> enabledplayers = serverman.getEnabledPlayers();
		
		for(Enumeration<GamePlayer> players = enabledplayers.elements(); players.hasMoreElements();) {
			GamePlayer p = players.nextElement();
			CometSession cometSession = users.get(p.getName());
			enqueueMessage(cometSession, userMessage);
			//if (cometSession != null && cometSession.isValid())
			//	cometSession.enqueue(userMessage);
		}
		
		// Persiste el mensaje
		serverman.persistUserMessage(userMessage);
	}

	@Override
	public void setStatus(Game game, StatusMessage status) throws ChatException {
		// Chequea si hay una sesión HTTP configurada.
		HttpSession httpSession = getThreadLocalRequest().getSession(false);
		if (httpSession == null) {
			throw new ChatException();
		}
		
		// Recupera el messages (internalization)
		RolegameMessages messages = (RolegameMessages)httpSession.getAttribute("messages-internalization");
		
		// get the user name for the HTTP session.
		GamePlayer player = (GamePlayer) httpSession.getAttribute("game-player");
		if (player == null) {
			throw new ChatException(messages.errorNoSesion("(setStatus-2)"));
		}
		
		// Obtiene el administrador de la partida.
		//System.out.println(games.size());
		ServerGameManager serverman = gamemans.get(game.getIdGame());
		
		// Determina, según el estado del juego, a que set de Players enviar el mensaje
		Vector<GamePlayer> enabledplayers = serverman.getEnabledPlayers();
				
		CometSession cometSession;
		for(Enumeration<GamePlayer> players = enabledplayers.elements(); players.hasMoreElements();) {
			cometSession = users.get(players.nextElement().getName());
			enqueueMessage(cometSession, status);
			//if (cometSession.isValid())
			//	cometSession.enqueue(status);
		}
	}
	
	/**
	 * Envía un mensaje del jugador al administrador de la partida.
	 * @param message Mensaje enviado.
	 */
	public void send(Game game, PlayerInformMessage message) throws ChatException {
		// Obtiene el administrador de la partida.
		ServerGameManager serverman = gamemans.get(game.getIdGame());
		// Delega el mensage a la partida
		serverman.receivePlayerInformMessage(message);
	}
	
	/**
	 * Envía el argumento enviado por un jugador.
	 */
	@Override
	public void send(Game game, ArgumentMessage message) throws ChatException {
		// check if there is a HTTP session setup.
		HttpSession httpSession = getThreadLocalRequest().getSession(false);
		if (httpSession == null) {
			throw new ChatException();
		}
		
		// Recupera el messages (internalization)
		RolegameMessages messages = (RolegameMessages)httpSession.getAttribute("messages-internalization");
		
		// get the user name for the HTTP session.
		GamePlayer player = (GamePlayer) httpSession.getAttribute("game-player");
		if (player == null) {
			throw new ChatException(messages.errorNoSesion(""));
		}
		
		/* Elimina el código HTML del mensaje, evita inyección de HTML
		message = Jsoup.clean(message, whitelist);*/
		
		// Obtiene el administrador de la partida.
		ServerGameManager serverman = gamemans.get(game.getIdGame());
		
		// Determina, según el estado del juego, a que set de Players enviar el argumento creado
		Vector<GamePlayer> enabledplayers = serverman.getEnabledPlayers();
		
		for(Enumeration<GamePlayer> players = enabledplayers.elements(); players.hasMoreElements();) {
			GamePlayer p = players.nextElement();
			CometSession cometSession = users.get(p.getName());
			enqueueMessage(cometSession, message);
			//if (cometSession != null && cometSession.isValid())
			//	cometSession.enqueue(userMessage);
		}
		
		// Persiste el mensaje
		serverman.persistArgument(message);
	}
	
	/**
	 *  Envia un mensaje de estado <code>message</code> a un único jugador <code>player</code>.
	 * @param player Jugador destino.
	 * @param message Mensaje enviado.
	 * @throws ChatException
	 */
	public void setStatus(GamePlayer player, StatusMessage message) throws ChatException {
		CometSession session = users.get(player.getName());
		enqueueMessage(session, message);
		//if (sesion.isValid())
		//	sesion.enqueue(message);
	}
	
	/**
	 * Envía un mensaje de estado <code>message</code> a un conjunto de jugadores <code>players</code>.
	 * @param players Vector de jugadores.
	 * @param message Mensaje Enviado.
	 * @throws ChatException
	 */
	public void setStatus(Vector<GamePlayer> players, StatusMessage message) throws ChatException {
		for(Enumeration<GamePlayer> e = players.elements(); e.hasMoreElements();) {
			CometSession session = users.get(e.nextElement().getName());
			enqueueMessage(session, message);
			//if (sesion.isValid())
			//	sesion.enqueue(message);
		}	
	}
	
	// Metodos propios de la partida
	
	@Override
	public Game createGame(Integer n, String locale) throws ChatException {
		// Chequea si hay una sesión HTTP configurada.
		HttpSession httpSession = getThreadLocalRequest().getSession(false);
		if (httpSession == null) {
			throw new ChatException("Error (setStatus-1)");
		}
		
		// Recupera el messages (internalization)
		RolegameMessages messages = (RolegameMessages)httpSession.getAttribute("messages-internalization");
		
		// Obtiene el usuario que creo la partida
		GamePlayer player = (GamePlayer) httpSession.getAttribute("game-player");
		if (player == null) {
			throw new ChatException(messages.errorNoSesion("(setStatus-2)"));
		}
		
		// Crea el la partida y la persiste
		Game game;
		if ((game = dao.getGameDAO().createGame(n, player)) != null) {
			// Crea el administrador de la partida
			ServerGameManager serverman = new ServerGameManager(game, this, locale);
			// Guarda la partida en el hashmap
			gamemans.putIfAbsent(game.getIdGame(), serverman);
			game.setCount(0);
			games.addElement(game);
			// sgames.put(game.getIdGame(), serverman);
			AddNewGameConsoleMessage message = new AddNewGameConsoleMessage(game);
			setStatus(players,message);
			return game;
		}
		else
			throw new ChatException(messages.errorCreateGame());
	}
	
	@Override
	public Game joinToGame(GamePlayer player, Game game) throws ChatException {
		// Chequea si existe una conexión HTTP configurada.
		HttpSession httpSession = getThreadLocalRequest().getSession(false);
		if (httpSession == null) {
			throw new ChatException("Error (jointToGame)");
		}
		
		// Recupera el messages (internalization)
		RolegameMessages messages = (RolegameMessages)httpSession.getAttribute("messages-internalization");
		
		// check if there is a Comet session setup. In a larger application the HTTP session may have been
		// setup via other means.
		CometSession cometSession = CometServlet.getCometSession(httpSession, false);
		if (cometSession == null) {
			throw new ChatException(messages.errorNoSesion("(no comet session)"));
		}
		
		// Obtiene el administrador de la partida.
		ServerGameManager serverman = gamemans.get(game.getIdGame());
		
		// Chequea que el juego no haya comenzado aún
		if (serverman.hasStarted())
			throw new ChatException(messages.errorGameStarted());
		
		// Conexión correcta
		// Informar a los usuarios de la partida de un nuevo jugador (no está agregado aún el jugador nuevo)
		NewPlayerStatusMessage stt = new NewPlayerStatusMessage(player);
		setStatus(game, stt);
		
			
		// Ingresa el jugador a la partida.
		if (!serverman.newPlayer(player))
			throw new ChatException(messages.errorNPlayers());	
	
		// Informar al nuevo usuario de los usuarios conectados (incluyendo a si mismo)
		// Obtiene los jugadores ya conectados a la partida
		Vector<GamePlayer> enabledplayers = serverman.getAllPlayers();
		for(Enumeration<GamePlayer> players = enabledplayers.elements(); players.hasMoreElements();) {
			NewPlayerStatusMessage existent = new NewPlayerStatusMessage(players.nextElement());
			enqueueMessage(cometSession, existent);
			//if (cometSession.isValid())
			//	cometSession.enqueue(existent);
		}
		
		// Actualiza el estado de la partida, para chequear si debe comenzar (desde acá, pq sino no se actualiza la lista de jugadores en el tablero)
		serverman.updateState();
		
		games.elementAt(games.indexOf(game)).addCount();
		
		// Setéa la partida en la que se une el jugador 
		playerInGame.put(player.getName(), game);
		
		// Informa a la consola que hay un nuevo jugador en la partida
		AddPlayerToGameConsoleMessage message = new AddPlayerToGameConsoleMessage(games.elementAt(games.indexOf(game)));
		setStatus(players, message);
		
		return game;
	}
	
	/**
	 * Remueve un juego de la consola porque finalizó, aunque aún no lo elimina del control dado que los usuarios pueden seguir intercambiando mensajes.
	 * El juego es realmente eliminado cuando no quedan jugadores en él.
	 * @param game Juego a ser eliminado.
	 * @throws ChatException
	 */
	public void removeGame(Game game) throws ChatException {

		// Elimina los Bots
		// Obtiene el administrador de la partida.
		ServerGameManager serverman = gamemans.get(game.getIdGame());
		// Obtiene los jugadores de la partida para chequear cuál es Bot
		Vector<GamePlayer> gplayers = serverman.getAllPlayers();
		GamePlayer bplayer;
		for(Enumeration<GamePlayer> e = gplayers.elements(); e.hasMoreElements();) {
			bplayer = e.nextElement();
			if (botsessions.remove(bplayer.getName()) != null)  {// Si hay una botsession activa para bplayer
				// Elimina el mapeo player-game (bot)
				botgames.remove(bplayer.getName());
				// Elimina la botsession
				users.remove(bplayer.getName());
				// Lo elimina de la lista de jugadores
				players.remove(bplayer);
			}
			// Quita el mapeo jugador-partida
			playerInGame.remove(bplayer.getName());
		}		
		// Envía el mensaje a la consola
		RemoveGameConsoleMessage remove = new RemoveGameConsoleMessage(game);
		setStatus(players, remove);
		
	}
	
	@Override
	public void leaveGame(GamePlayer player, Game game) throws ChatException {
		// Chequea si existe una conexión HTTP configurada.
		HttpSession httpSession = getThreadLocalRequest().getSession(false);
		if (httpSession == null) {
			throw new ChatException(("Error (no comet session)"));
		}
		
		// Recupera el messages (internalization)
		RolegameMessages messages = (RolegameMessages)httpSession.getAttribute("messages-internalization");
		
		// check if there is a Comet session setup. In a larger application the HTTP session may have been
		// setup via other means.
		CometSession cometSession = CometServlet.getCometSession(httpSession, false);
		if (cometSession == null) {
			throw new ChatException(messages.errorNoSesion("(no comet session)"));
		}
		
		// check the user name parameter matches the HTTP sessions user name
		if (!player.equals(httpSession.getAttribute("game-player"))) {
			throw new ChatException(messages.errorNoSesion("(no comet session)"));
		}
		
		games.elementAt(games.indexOf(game)).substractCount();
		
		playerInGame.remove(player.getName());
		
		// Obtiene el administrador de la partida.
		ServerGameManager serverman = gamemans.get(game.getIdGame());
		// Informa al administrador de la partida de la baja
		serverman.removePlayer(player); // Si ya empezó la partida, será reemplazado por un bot
		
		// Informar al resto de los jugadores de la baja en la partida (antes de cerrar la sesión).
		RemovePlayerStatusMessage stt = new RemovePlayerStatusMessage(player);
		setStatus(serverman.getAllPlayers(), stt);
		
		// Remueve la partida si no quedaron jugadores
		/*if (games.elementAt(games.indexOf(game)).getCount() == 0) {
			games.remove(game);
			gamemans.remove(game.getIdGame());
		}*/
		
		// Avisa a la consola de todos los jugadores de la baja de un jugador en la partida
		RemovePlayerFromGameConsoleMessage remove = new RemovePlayerFromGameConsoleMessage(games.elementAt(games.indexOf(game)));
		setStatus(players,remove);
	}

	@Override
	public void reJoinToGame(GamePlayer player, Game game) throws ChatException {
		
	}

	@Override
	public void register(String username, String password, String email, char sex, Date birthday, Integer xp, String locale) throws ChatException {
		//Internalization: se crea un messages para cada usuario logueado
		RolegameMessages messages;
		try {
			messages = GWTI18N.create(RolegameMessages.class, locale);
		}
		catch (IOException ioe) {
			throw new ChatException("Error (internalization)");
		}

		// Chequea que el nombre de usuario esté disponible
		if (!dao.getPlayerDAO().isNameAvailable(username))
			throw new ChatException(messages.errorNoAvUsername(username + System.currentTimeMillis()));
		// Da de alta el usuario
		dao.getPlayerDAO().createPlayer(username, password, email, sex, birthday, xp);		
		
	}

	@Override
	public void sendInvite(Game game, GamePlayer player) throws ChatException {
		// Chequea si existe una conexión HTTP configurada.
		HttpSession httpSession = getThreadLocalRequest().getSession(false);
		if (httpSession == null) {
			throw new ChatException("Error (sendInvite)");
		}
		
		// Recupera el messages (internalization)
		RolegameMessages messages = (RolegameMessages)httpSession.getAttribute("messages-internalization");
		
		
		// check if there is a Comet session setup. In a larger application the HTTP session may have been
		// setup via other means.
		CometSession cometSession = CometServlet.getCometSession(httpSession, false);
		if (cometSession == null) {
			throw new ChatException(messages.errorNoSesion("(sendInvite)"));
		}
		
		// Obtiene el administrador de la partida.
		ServerGameManager serverman = gamemans.get(game.getIdGame());
		// Chequéa que el juego esté disponible
		if (serverman == null)
			throw new ChatException(messages.errorGameFinish());
		// Chequéa que la partida no esté completa
		if (serverman.isFull())
			throw new ChatException(messages.errorNPlayers());
		// Chequéa que el jugador invitado no esté en otra partida
		if (playerInGame.containsKey(player.getName()))
			throw new ChatException(messages.errorPlayerOcup());
		
		// Obtiene el player de la session, q es quien envió la invitación
		GamePlayer sender = (GamePlayer)httpSession.getAttribute("game-player");
		if (sender.equals(player))
			throw new ChatException(messages.errorSelfInvitation());
			
		// Envía el mensaje de invitación al jugador invitado
		GameInviteConsoleMessage invite = new GameInviteConsoleMessage(game, sender);
		setStatus(player, invite);
	}
	
	private void enqueueMessage(CometSession session, GameMessage message) throws ChatException {
		if (session.isValid())
			session.enqueue(message);
		else
			forceLogout(session);
	}
	
	/**
	 * Busca sesiones invalidas, las elimina y habilita para un nuevo login.
	 * Avisa a las consolas del logout del jugado
	 */
	public void checkInvalidSessions() {
		try {
			for(Enumeration<GamePlayer> e = players.elements(); e.hasMoreElements();) {
				CometSession session = users.get(e.nextElement().getName());
				if (!session.isValid())
					forceLogout(session);
			}
		}
		catch (Exception e) {}
	}
	
	private void forceLogout(CometSession session) throws ChatException {
		// Obtiene el usuario de la session caida
		String name = getKeyByValue(users, session);
		if (name != null) {
			Game game = playerInGame.get(name);
			GamePlayer player = getPlayerByName(name);
			if (game != null) { // El jugador estaba participando de una partida.
				// Obtiene el rol de jugador
				ServerGameManager serverman = gamemans.get(game.getIdGame());
				if (serverman.hasStarted() && !serverman.hasFinished()) {
					String role = serverman.getRole(player);
					// Crea el botsession
					BotSession botsession = new BotSession(this, player, game, role, serverman.getAllPlayers(), serverman.getPlayerState(player));

					// Reemplaza la commetSession por el bot
					users.replace(player.getName(), session, botsession);
					session.invalidate();
					// Almacena el usuario por si vuelve a la partida
					botgames.put(player.getName(), game);
					botsessions.put(player.getName(), botsession);
				}
				else { // Tvia no empezó o ya terminó el juego, entonces elimina al jugador normalmente
					// remove the mapping of user name to CometSession
					if (!serverman.hasFinished()) // Tvia no empezó
						leaveGame(player, game); // Sino ya fue eliminado el game
					users.remove(player.getName(), session);
					players.remove(player);
				}
			}
			else {
				// remove the mapping of user name to CometSession
				//httpSession.invalidate();
				users.remove(player.getName(), session);
				players.remove(player);
			}
			
			// Informa al resto de los usuarios de la desconexión
			RemovePlayerConsoleMessage remove = new RemovePlayerConsoleMessage(player);
			setStatus(players,remove);
		}


	}
	
	/**
	 * Devuelve la clave dado un valor, se asume relación 1 a 1.
	 * @param map ConcurrentHashMap
	 * @param session valor
	 * @return clave, null si no existe el valor
	 */
	private String getKeyByValue(ConcurrentMap<String,CometSession> map, CometSession session) {
	    for (Entry<String, CometSession> entry : map.entrySet()) {
	        if (session.equals(entry.getValue())) {
	            return entry.getKey();
	        }
	    }
	    return null;
	}
	
	private GamePlayer getPlayerByName(String name) {
		GamePlayer player = null;
		boolean found = false;
		for(Enumeration<GamePlayer> ePlayers = players.elements(); ePlayers.hasMoreElements() && !found;) {
			player = ePlayers.nextElement();
			found = player.getName().equals(name);
		}
		if (found)
			return player;
		else
			return null;
	}
	
	
	
}