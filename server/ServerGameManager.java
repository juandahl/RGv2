package edu.isistan.rolegame.server;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.scb.gwt.web.server.i18n.GWTI18N;

import edu.isistan.rolegame.client.comet.ChatException;
import edu.isistan.rolegame.server.comet.GameServiceImpl;
import edu.isistan.rolegame.server.resources.DAOFactory;
import edu.isistan.rolegame.server.resources.MyDAOFactory;
import edu.isistan.rolegame.shared.Game;
import edu.isistan.rolegame.shared.GamePlayer;
import edu.isistan.rolegame.shared.PlayerState;
import edu.isistan.rolegame.shared.Role;
import edu.isistan.rolegame.shared.Round;
import edu.isistan.rolegame.shared.comm.ArgumentMessage;
import edu.isistan.rolegame.shared.comm.InformFinishGameStatusMessage;
import edu.isistan.rolegame.shared.comm.InformFinishRoundStatusMessage;
import edu.isistan.rolegame.shared.comm.InformGeneralRoundResultStatusMessage;
import edu.isistan.rolegame.shared.comm.InformGoodRoundResult;
import edu.isistan.rolegame.shared.comm.InformPlayerDecisionStatusMessage;
import edu.isistan.rolegame.shared.comm.InformRevGeneralRoundResultStatusMessage;
import edu.isistan.rolegame.shared.comm.InformRoundSelection;
import edu.isistan.rolegame.shared.comm.InformRoundsFinalResultStatusMessage;
import edu.isistan.rolegame.shared.comm.InformStartRoundStatusMessage;
import edu.isistan.rolegame.shared.comm.LockGameStatusMessage;
import edu.isistan.rolegame.shared.comm.PlayerInformMessage;
import edu.isistan.rolegame.shared.comm.StartBadRoundStatusMessage;
import edu.isistan.rolegame.shared.comm.StartDocRoundStatusMessage;
import edu.isistan.rolegame.shared.comm.StartGameStatusMessage;
import edu.isistan.rolegame.shared.comm.StartGeneralRoundStatusMessage;
import edu.isistan.rolegame.shared.comm.StartGoodRoundStatusMessage;
import edu.isistan.rolegame.shared.comm.UserMessage;
import edu.isistan.rolegame.shared.ext.RolegameConstants;
import edu.isistan.rolegame.shared.ext.RolegameMessages;


public class ServerGameManager {

	private int gamestate;
	private long countstate; // Flag para saber si el TimerTask está en el estado en que debería ejecutarse
	private GameServiceImpl impl;
	private int milsec;
	private long milsecgral;
	
	private Role constantsRole;
	
	public static RolegameConstants constants; // = GWTI18N.create(RolegameConstants.class);
	public static RolegameMessages messages; // = GWTI18N.create(RolegameMessages.class);

	// Datos generales de la partida.
	private Game game;
	// Mapea cada jugador de la partida con un rol.
	private ConcurrentHashMap<GamePlayer, String> players;
	// Jugadores habilitados para el intercambio de mensajes según el estado de la partida
	private Vector<GamePlayer> enabled_players;	
	// Jugadores que han sido eliminados de la partida por la misma dinámica del juego
	private Vector<GamePlayer> eliminated_players;
	// Mapea cada rol con la cantidad requerida para comenzar la partida.
	private ConcurrentHashMap<String, Integer> nRoles;
	// Mapea cada rol con la cantidad de jugadores registrados hasta el momento.
	// Tambien se utiliza, luego de comenzada la partida, para saber cuantos jugadores de cada rol quedan.
	private ConcurrentHashMap<String, Integer> countRoles;
	// Vector donde se almacenan los PlayerInformMessage, para su posterior procesamiento (dependiendo de la ronda)
	private Vector<PlayerInformMessage> informs;
	// Timer utilizado para coordinar y regular las rondas.
	private Timer timer;
	// Almacena al jugador seleccionado para abandonar la partida (ronda BAD).
	private GamePlayer selectedBad;
	// Almacena al jugador seleccionado por GOOD para chequear si es BAD (ronda GOOD).
	private GamePlayer selectedGood;
	// Almacena al jugador salvado por el DOC (ronda DOC).
	private GamePlayer selectedDoc;
	// Almacena al jugador seleccionado para abandonar la partida (ronda GENERAL).
	private GamePlayer selectedGeneral;
	//DAOFactory utilizado para persistir los objetos.
	private DAOFactory dao;
	// Ronda actual
	private Round currentRound; 
	// Contador de empates consecutivos
	private int drawCount;
	// Indica que una mayoría fue alcanzada y por lo tanto no todos los jugadores votaron.
	private boolean majority;

	private static final int MAX_DRAWS = 3;

	public ServerGameManager(Game game, GameServiceImpl impl, String locale) {
		players = new ConcurrentHashMap<GamePlayer, String>();
		this.game = game;
		this.impl = impl;

		// Calcula las cantidad de jugadores para cada rol
		int nBad = Math.round((float)game.getnPlayers() / 4);
		int nGood = Math.round((float)game.getnPlayers() / 8);
		int nDoc = 1;
		int nPeople = Math.round((float)game.getnPlayers() / 2);
		if (nBad + nGood + nDoc + nPeople < game.getnPlayers())
			nBad++;
		else
			if ((nBad + nGood + nDoc + nPeople > game.getnPlayers()))
				nPeople--;
		
		constantsRole = new RoleServer(locale);
		
		try {
			constants = GWTI18N.create(RolegameConstants.class, locale);
			messages = GWTI18N.create(RolegameMessages.class, locale);
		}
		catch (IOException ioe) {System.out.println("cataplum servermanager");}


		nRoles = new ConcurrentHashMap<String, Integer>();
		nRoles.put(constants.badRole(), nBad);
		nRoles.put(constants.goodRole(), nGood);
		nRoles.put(constants.docRole(), nDoc);
		nRoles.put(constants.peopleRole(), nPeople);

		// Inicializa los contadores.
		countRoles = new ConcurrentHashMap<String, Integer>();
		countRoles.put(constants.badRole(), new Integer(0));
		countRoles.put(constants.goodRole(), new Integer(0));
		countRoles.put(constants.docRole(), new Integer(0));
		countRoles.put(constants.peopleRole(), new Integer(0));


		// Inicializa el estado de la partida
		gamestate = 0;
		countstate = 0;

		drawCount = 0;
		majority = false;

		// Setea los milisegundos que durarán las rondas particulares (BAD, GOOD, DOC)
		this.milsec = 90000;

		// Setea los milisegundos que durarán las rondas generales
		this.milsecgral = 300000;

		// Inicializa el timer
		timer = new Timer();

		// Inicializa los jugadores seleccionados
		selectedBad = null;
		selectedGood = null;
		selectedDoc = null;
		selectedGeneral = null;

		// Inicializa los vectores auxiliares
		eliminated_players = new Vector<GamePlayer>();

		// Inicializa el DAO
		dao = new MyDAOFactory();

		// Persiste la ronda de espera inicial, antes de que el juego de comienzo
		currentRound =  dao.getGameDAO().addRound(this.game, constants.initialRole());
	}

	public boolean newPlayer(GamePlayer player) {
		// Controla que no se supere el número de jugadores definido para la partida
		if (players.size() == game.getnPlayers())
			return false;
		// Determinar rol
		String role = getRole(Math.random());
		players.put(player, role);
		countRoles.put(role, countRoles.get(role) + 1);
		// Persiste la Participación del jugador en la partida
		dao.getParticipationDAO().addParticipation(player, game, role);
		return true;
	}

	/**
	 * Determina si la partida está completa
	 * @return True si la partida está completa
	 */
	public boolean isFull() {
		return players.size() == game.getnPlayers();
	}

	// Determina un rol al azar en base a los disponibles
	private String getRole(double rnd) {
		if (rnd < 0.25)
			if (countRoles.get(constants.badRole()).equals(nRoles.get(constants.badRole())))
				return getRole(rnd + 0.25);
			else
				return constants.badRole();
		else
			if (rnd < 0.5)
				if (countRoles.get(constants.goodRole()).equals(nRoles.get(constants.goodRole())))
					return getRole(rnd + 0.25);
				else
					return constants.goodRole();
			else
				if (rnd < 0.75)
					if (countRoles.get(constants.docRole()).equals(nRoles.get(constants.docRole())))
						return getRole(rnd + 0.25);
					else
						return constants.docRole();
				else // rnd >= 0.75
					if (countRoles.get(constants.peopleRole()).equals(nRoles.get(constants.peopleRole())))
						return getRole(0);
					else
						return constants.peopleRole();	
	}

	public String getRole(GamePlayer player) {
		return players.get(getPlayerByName(player.getName()));
	}

	public PlayerState getPlayerState(GamePlayer player) {
		PlayerState pstate = new PlayerState();
		player = getPlayerByName(player.getName());

		pstate.setEliminated(eliminated_players.contains(player));
		boolean enabled = getEnabledPlayers().contains(player);
		pstate.setCanVote(enabled);
		pstate.setLocked(!enabled);
		pstate.setRole(getRole(player));

		return pstate;
	}

	public boolean removePlayer(GamePlayer player) {
		// Si ya empezo la partida, reemplazo jugador por bot, 
		// sino lo quito y sigo esperando por nuevos jugadores
		if (gamestate == 0) {
			String role = getRole(player);
			//players.remove(player);
			players.remove(getPlayerByName(player.getName()));
			countRoles.put(role, new Integer(countRoles.get(role) - 1));
			// Elimina la participación persistida
			dao.getParticipationDAO().removeParticipation(player, game);	
		}
		else
			if (eliminated_players.contains(player)) {
				// El jugador fue eliminado por lo tanto no participa más del juego
				players.remove(getPlayerByName(player.getName()));
			}
			else
				// Técnicamente no debería nunca entrar en esta alternativa, puesto que si el jugador
				// no es un jugador eliminado y el juego ya ha comenzado, el tablero no debería dar la chance
				// de cerrar. (Solo puede salir por logout)
				players.remove(getPlayerByName(player.getName()));
		return true;
	}

	public void updateState() {
		switch (gamestate) {
		case 0: // Registro y comienzo
			if (game.getnPlayers() == players.size()) {
				gamestate = 1;
				countstate++;
				StartGameStatusMessage status;
				
				try {
					// Envia a cada jugador el mensaje indicando el rol asignado
					for(Map.Entry<GamePlayer, String> map : players.entrySet()) {
						if (map.getValue().equals(constants.peopleRole())) // Para el rol PEOPLE no se informa que jugadores poseen el mismo rol
							status = new StartGameStatusMessage(map.getValue(), constantsRole.getRoleName(map.getValue()), constantsRole.getMessageRole(map.getValue()), new Vector<GamePlayer>());
						else // Para GOOD, BAD y DOC se informa quién más posee el mismo rol	
							status = new StartGameStatusMessage(map.getValue(), constantsRole.getRoleName(map.getValue()), constantsRole.getMessageRole(map.getValue()), getPlayersByRole(map.getValue()));
						impl.setStatus(map.getKey(), status);
					}
				}
				catch (ChatException e) {/* TODO */ };
			}
			else
				break;
		case 1: // Comienzo de la ronda BAD
			try {
				// Informa que comienza la ronda (a todos)
				InformStartRoundStatusMessage inform = new InformStartRoundStatusMessage(constants.badRoundStartMessage()); //BAD_ROUND_START_MESSAGE);
				impl.setStatus(getAllPlayers(), inform);
				// Persiste la ronda
				currentRound = dao.getGameDAO().addRound(game, constants.badRole());
				// Envia a los BAD el comienzo de su ronda, y al resto Lock
				StartBadRoundStatusMessage bad = new StartBadRoundStatusMessage();
				LockGameStatusMessage lock = new LockGameStatusMessage();
				for(Map.Entry<GamePlayer, String> map : players.entrySet()) {
					if (map.getValue().equals(constants.badRole()))
						impl.setStatus(map.getKey(), bad);
					else						
						impl.setStatus(map.getKey(), lock);
				}

				countstate++;
				// Agrega las acciones al Timer que controlará la ronda
				timer.schedule(new PreviousFinishTimerTask(countstate, this, impl, constants.generalPreviousFinishMessage()), (long)(milsec * 0.75));
				timer.schedule(new FinishRoundTimerTask(countstate, 3, this, impl, constants.generalRoundFinishMessage()), getTimeRound(constants.badRole()));

				// Pasa al siguiente estado donde se controla el final de la partida
				informs = new Vector<PlayerInformMessage>();
				gamestate = 2;

			}
			catch (ChatException e) { /* TODO */}
			break;
		case 2: // Controla el final de la ronda BAD
			//if (informs.size() == nRoles.get(constants.badRole()).intValue()) {
			if (informs.size() == countRoles.get(constants.badRole()).intValue()) { // Controla sobre los jugadores del rol no eliminados
				gamestate = 3;
				countstate++;
			}
			else	
				break;
		case 3: // Finaliza la ronda BAD y Determina el resultado
			try {
				// Determina quién ha sido elegido para ser capturado.
				Vector<GamePlayer> selectedPlayers = getPlayerSelected(informs, countRoles.get(constants.badRole()));
				if (selectedPlayers.size() > 1) {
					// Se registró un empate en la votación, por lo cual se debe volver a votar
					// Envía a TODOS, start general round con la info del EMPATE
					StartGeneralRoundStatusMessage startdraw = new StartGeneralRoundStatusMessage(constants.drawMessage(), selectedPlayers);
					impl.setStatus(getPlayersByRole(constants.badRole()), startdraw);

					countstate++;
					// Agrega las acciones al Timer que controlará la ronda
					timer.schedule(new PreviousFinishTimerTask(countstate, this, impl, constants.generalPreviousFinishMessage()), (long)(milsec * 0.3));
					timer.schedule(new FinishRoundTimerTask(countstate, 3, this, impl, constants.generalRoundFinishMessage()), (long)(getTimeRound(constants.badRole()) * 0.4)); // Más corta

					// Pasa al estado donde se controla el final de la ronda
					informs = new Vector<PlayerInformMessage>();
					gamestate = 2;

					break;
				}
				else {
					// Hubo un único jugador seleccionado (mayor cantidad de votos)
					selectedBad = selectedPlayers.firstElement();
					// Persiste el resultado de la ronda
					dao.getGameDAO().setRoundResult(currentRound, selectedBad);

					// Informa a los BAD del resultado de la votación
					InformRoundSelection selection = new InformRoundSelection(messages.badRoundSelection(selectedBad.getName()), selectedBad); 
					//constants2.BAD_ROUND_SELECTION.replace(constants2.CONSTANT_PLAYER_NAME,selectedBad.getName()),selectedBad);
					impl.setStatus(getPlayersByRole(constants.badRole()), selection);

					// Informa el final de la ronda (a todos)
					InformFinishRoundStatusMessage inform = new InformFinishRoundStatusMessage(constants.badRoundFinishMessage());

					impl.setStatus(getAllPlayers(), inform);
					gamestate = 4;
					countstate++;
				}
			}
			catch (ChatException e) {}
		case 4: // Comienza la ronda GOOD
			try {
				// Informa que comienza la ronda (a todos)
				InformStartRoundStatusMessage informGOOD = new InformStartRoundStatusMessage(constants.goodRoundStartMessage()); //2.GOOD_ROUND_START_MESSAGE);
				impl.setStatus(getAllPlayers(), informGOOD);
				// Persiste la ronda
				currentRound = dao.getGameDAO().addRound(game, constants.goodRole());
				// Envia a los GOOD el comienzo de su ronda, y al resto Lock
				StartGoodRoundStatusMessage good = new StartGoodRoundStatusMessage();
				LockGameStatusMessage lock = new LockGameStatusMessage();
				for(Map.Entry<GamePlayer, String> map : players.entrySet()) {
					if (map.getValue().equals(constants.goodRole()))
						impl.setStatus(map.getKey(), good);
					else						
						impl.setStatus(map.getKey(), lock);
				}

				countstate++;
				// Agrega las acciones al Timer que controlará la ronda
				timer.schedule(new PreviousFinishTimerTask(countstate, this, impl, constants.generalPreviousFinishMessage()), (long)(milsec * 0.75));
				timer.schedule(new FinishRoundTimerTask(countstate, 6, this, impl, constants.generalRoundFinishMessage()), getTimeRound(constants.goodRole()));

				// Pasa al siguiente estado donde se controla el final de la partida
				informs = new Vector<PlayerInformMessage>(); // Limpia los mensajes de votación
				gamestate = 5;
			}
			catch (ChatException e) {/* TODO */}
		case 5: // Controla el final de la ronda GOOD
			if (informs.size() == countRoles.get(constants.goodRole()).intValue()) { //nRoles.get(constants.goodRole()).intValue()) {
				gamestate = 6;
				countstate++;
			}
			else	
				break;
		case 6: // Finaliza la ronda GOOD y determina el resultado
			try {
				// Chequea que aún queden GOODs en el juego, sino informa el fin de la ronda
				// NOTA: la ronda debe ser "realizada" de igual forma para evitar evidenciar que no quedan jugadores de este rol.
				if (countRoles.get(constants.goodRole()).intValue() > 0) {
					// Determina quién ha sido elegido para chequear si es un Dragon
					Vector<GamePlayer> selectedPlayers = getPlayerSelected(informs, countRoles.get(constants.goodRole()));
					if (selectedPlayers.size() > 1) {
						// Se registró un empate en la votación, por lo cual se debe volver a votar
						// Envía a TODOS, start general round con la info del EMPATE
						StartGeneralRoundStatusMessage startdraw = new StartGeneralRoundStatusMessage(constants.drawMessage(), selectedPlayers);
						impl.setStatus(getPlayersByRole(constants.goodRole()), startdraw);

						countstate++;
						// Agrega las acciones al Timer que controlará la ronda
						timer.schedule(new PreviousFinishTimerTask(countstate, this, impl, constants.generalPreviousFinishMessage()), (long)(milsec * 0.3));
						timer.schedule(new FinishRoundTimerTask(countstate, 6, this, impl, constants.generalRoundFinishMessage()), (long)(getTimeRound(constants.goodRole()) * 0.4));

						// Pasa al estado donde se controla el final de la ronda
						informs = new Vector<PlayerInformMessage>();
						gamestate = 5;
						break;
					}
					else {
						// Hubo un único jugador seleccionado (mayor cantidad de votos)
						selectedGood = selectedPlayers.firstElement();
						// Persiste el resultado de la ronda
						dao.getGameDAO().setRoundResult(currentRound, selectedGood);

						// Informa a los GOOD del resultado de la votación
						InformRoundSelection selection = new InformRoundSelection(messages.goodRoundSelection(selectedGood.getName()), selectedGood);//constants2.GOOD_ROUND_SELECTION.replace(constants2.CONSTANT_PLAYER_NAME,selectedGood.getName()),
								
						impl.setStatus(getPlayersByRole(constants.goodRole()), selection);

						// Determina y envía el resultado de la consulta de GOOD
						String msgGoodResult;
						boolean isBad;
						if (players.get(selectedGood).equals(constants.badRole()))  {// Consulta positiva - NUll?
							isBad = true;
							msgGoodResult = messages.trueInformGoodRoundResult(selectedGood.getName());
						}
						else {
							isBad = false;
							msgGoodResult = messages.falseInformGoodRoundResult(selectedGood.getName());
						}

						//msgGoodResult = msgGoodResult.replaceAll(constants2.CONSTANT_PLAYER_NAME, selectedGood.getName());

						InformGoodRoundResult goodresult = new InformGoodRoundResult(msgGoodResult, selectedGood, isBad);
						impl.setStatus(getPlayersByRole(constants.goodRole()), goodresult);
					}
				}
				// Informa el final de la ronda (a todos)
				InformFinishRoundStatusMessage inform = new InformFinishRoundStatusMessage(constants.goodRoundFinishMessage());

				impl.setStatus(getAllPlayers(), inform);
				gamestate = 7;
				countstate++;
			}
			catch (ChatException e) {}
		case 7:// Comienzo de la ronda DOC
			try {
				// Informa que comienza la ronda (a todos)
				InformStartRoundStatusMessage inform = new InformStartRoundStatusMessage(constants.docRoundStartMessage());
				impl.setStatus(getAllPlayers(), inform);
				// Persiste la ronda
				currentRound = dao.getGameDAO().addRound(game, constants.docRole());
				// Envia a los DOC el comienzo de su ronda, y al resto Lock
				StartDocRoundStatusMessage doc = new StartDocRoundStatusMessage();
				LockGameStatusMessage lock = new LockGameStatusMessage();
				for(Map.Entry<GamePlayer, String> map : players.entrySet()) {
					if (map.getValue().equals(constants.docRole()))
						impl.setStatus(map.getKey(), doc);
					else						
						impl.setStatus(map.getKey(), lock);
				}

				countstate++;
				// Agrega las acciones al Timer que controlará la ronda
				timer.schedule(new PreviousFinishTimerTask(countstate, this, impl, constants.generalPreviousFinishMessage()), (long)(getTimeRound(constants.docRole()) * 0.75));
				timer.schedule(new FinishRoundTimerTask(countstate, 9, this, impl, constants.generalRoundFinishMessage()), (long)(getTimeRound(constants.docRole())));

				// Pasa al siguiente estado donde se controla el final de la partida
				informs = new Vector<PlayerInformMessage>();
				gamestate = 8;

			}
			catch (ChatException e) { /* TODO */}
			break;
		case 8: // Controla el final de la ronda DOC
			if (informs.size() == countRoles.get(constants.docRole()).intValue()) { // nRoles.get(constants.docRole()).intValue()) {
				gamestate = 9;
				countstate++;
			}
			else
				break;
		case 9: // Finaliza la ronda DOC y Determina el resultado
			try {
				// Chequea que el DOC esté aún en el juego.
				if (countRoles.get(constants.docRole()).intValue() > 0) {
					// Determina quién ha sido elegido para ser capturado.
					// Como hay un único DOC no hay empate
					selectedDoc = getPlayerSelected(informs, countRoles.get(constants.docRole())).firstElement();

					// Persiste el resultado de la ronda
					dao.getGameDAO().setRoundResult(currentRound, selectedDoc);

					// Informa a los BAD del resultado de la votación
					InformRoundSelection selection = new InformRoundSelection(messages.docRoundSelection(selectedDoc.getName()),selectedDoc);//constants2.DOC_ROUND_SELECTION.replace(constants2.CONSTANT_PLAYER_NAME,selectedDoc.getName()),
							
					impl.setStatus(getPlayersByRole(constants.docRole()), selection);

				}
				else
					selectedDoc = null;
				// Informa el final de la ronda (a todos)
				InformFinishRoundStatusMessage inform = new InformFinishRoundStatusMessage(constants.docRoundFinishMessage());

				impl.setStatus(getAllPlayers(), inform);
				gamestate = 10;
				countstate++;
			}
			catch (ChatException e) {}
		case 10: // Informa del resultado de la ronda (quien fue capturado) - Efectiviza la captura
			try {
				// Comprueba si hay un jugador capturado o fue salvado por el DOC
				boolean isCaptured;
				String msgFinalResult;
				if (selectedDoc != null && selectedDoc.equals(selectedBad)) { // El jugador fue salvado por el Doc
					isCaptured = false;
					msgFinalResult = messages.falseFinalResult();
				}
				else { // El jugador salvado no es quien fue capturado
					isCaptured = true;
					msgFinalResult = messages.trueFinalResult(selectedBad.getName());
				}
				// Reemplaza el jugador capturado en la frase
				//msgFinalResult = msgFinalResult.replaceAll(constants2.CONSTANT_PLAYER_NAME, selectedBad.getName());
				// Envía el informe a todos los jugadores
				InformRoundsFinalResultStatusMessage finalresult = new InformRoundsFinalResultStatusMessage(msgFinalResult, isCaptured, selectedBad);
				impl.setStatus(getAllPlayers(), finalresult);
				// Realiza la baja del jugador capturado
				if (isCaptured) {
					// Pone al jugador en el vector de jugadores eliminados
					eliminated_players.addElement(selectedBad);
					// Determina el rol del jugador capturado
					String rolecaptured = players.get(selectedBad);
					// Disminuye la cantidad de jugadores de su rol en la partida
					countRoles.put(rolecaptured,countRoles.get(rolecaptured) - 1);
					// Persiste la captura/eliminación
					dao.getGameDAO().setPlayerEliminated(game, selectedBad, constants.badRole());
					// Chequea el fin de la partida (a favor de los BAD)
					if (countRoles.get(constants.goodRole()) + countRoles.get(constants.docRole()) + countRoles.get(constants.peopleRole()) == 0) {
						gamestate = 11;
						countstate++;
					}
					else 
						// Chequea si por un autovoto se autoeliminaron todos los dragones
						if (countRoles.get(constants.badRole()) == 0) {
							gamestate = 20;
							timer.schedule(new TimerTask() {

								@Override
								public void run() {
									updateState(); ///// mmmmm.. revisar esto
								}
							}, 0);
							break;
						}
						else {
							gamestate = 12; // Se continúa con la ronda general
							timer.schedule(new TimerTask() {

								@Override
								public void run() {
									updateState(); ///// mmmmm.. revisar esto
								}
							}, 0);
							break;
						}
				}
				else {
					gamestate = 12; 
					countstate++;
					timer.schedule(new TimerTask() {

						@Override
						public void run() {
							updateState(); ///// mmmmm.. revisar esto
						}
					}, 0);
					break;
				}
			}
			catch (ChatException e) {}
		case 11: // Final de la partida, ganaron los BAD
			try {
				// Persiste el resultado de la partida
				dao.getGameDAO().setFinal(game, constants.badRole());
				// Informa el final de la partida
				InformFinishGameStatusMessage finishgame = new InformFinishGameStatusMessage(constants.winnerBadMessage(), true, getPlayerRolesMessages());
				impl.setStatus(getAllPlayers(), finishgame);
				gamestate = 99; // 99 no existe, es por las dudas q se vuelva a entrar a updatestate
				countstate++;
				impl.removeGame(this.game);
				timer.cancel();
				break;
			}
			catch (ChatException e) {}
		case 12: // Primera Ronda GENERAL ********************************************************************
			try {
				// Informa que comienza la ronda (a todos)
				InformStartRoundStatusMessage inform = new InformStartRoundStatusMessage(constants.generalRoundStartMessage());
				impl.setStatus(getAllPlayers(), inform);
				// Persiste la ronda
				currentRound = dao.getGameDAO().addRound(game, constants.peopleRole());
				// Envia a TODOS, start general round
				StartGeneralRoundStatusMessage startgral = new StartGeneralRoundStatusMessage();
				impl.setStatus(getAllPlayers(), startgral);

				countstate++;
				// Agrega las acciones al Timer que controlará la ronda
				timer.schedule(new PreviousFinishTimerTask(countstate, this, impl, constants.generalPreviousFinishMessage()), (long)(milsecgral * 0.75));
				timer.schedule(new FinishRoundTimerTask(countstate, 14, this, impl, constants.generalRoundFinishMessage()), milsecgral);

				// Pasa al siguiente estado donde se controla el final de la ronda
				informs = new Vector<PlayerInformMessage>();
				gamestate = 13;

			}
			catch (ChatException e) { /* TODO */}
			break;
		case 13: // Controla el final de la ronda GENERAL, 
			// Cuando la cantidad de mensajes recibidos es igual a la cantidad de jugadores aún en la partida	 
			if (informs.size() == game.getnPlayers() - eliminated_players.size() || (majority = majority(game.getnPlayers() - eliminated_players.size()))) {
				gamestate = 14;
				countstate++;
			}
			else	
				break;
		case 14: // Finaliza la primera ronda GENERAL
			try {
				// Determina quién ha sido elegido para ser eliminado del juego.
				Vector<GamePlayer> selectedPlayers = getPlayerSelected(informs, game.getnPlayers() - eliminated_players.size());
				if (selectedPlayers.size() > 1) {
					if (drawCount < MAX_DRAWS - 1) { // Chequea si se alcanzó el limite de empates
						// Se registró un empate en la votación, por lo cual se debe volver a votar
						// Envía a TODOS, start general round con la info del EMPATE
						StartGeneralRoundStatusMessage startgraldraw = new StartGeneralRoundStatusMessage(constants.drawMessage(), selectedPlayers);
						impl.setStatus(getAllPlayers(), startgraldraw);

						drawCount++; // Cuenta los empates consecutivos
						countstate++;
						// Agrega las acciones al Timer que controlará la ronda
						timer.schedule(new PreviousFinishTimerTask(countstate, this, impl, constants.generalPreviousFinishMessage()), (long)(milsecgral * 0.3));
						timer.schedule(new FinishRoundTimerTask(countstate, 14, this, impl, constants.generalRoundFinishMessage()), (long)(milsecgral * 0.4));

						// Pasa al estado donde se controla el final de la ronda
						informs = new Vector<PlayerInformMessage>();
						gamestate = 13;
						break;
					}
					else { // Se alcanzo el limite de empates
						// Informa del limite de empates.
						// Informa el final de la ronda (a todos)
						InformFinishRoundStatusMessage informdrawlimit = new InformFinishRoundStatusMessage(constants.drawRoundFinishMessage());
						impl.setStatus(getAllPlayers(), informdrawlimit);
						// Pasa directamente a una nueva ronda BAD
						gamestate = 1; // Se debe continuar con la ronda BAD
						drawCount = 0; // Inicializa nuevamente el contador de empates puesto que se alcanzó el limite
						countstate++;
						timer.schedule(new TimerTask() {
							@Override
							public void run() {
								updateState();
							}
						}, 0);
						break;
					}
				}
				else {
					// Hubo un único jugador seleccionado (mayor cantidad de votos)
					selectedGeneral = selectedPlayers.firstElement();
					// Persiste el resultado de la ronda
					dao.getGameDAO().setRoundResult(currentRound, selectedGeneral);

					String mjStr = ""; // Mensaje que advierte que se finalizó la votación por mayoría alcanzada.
					if (majority) {
						mjStr = constants.majorityMessage();
						majority = false;
					}
					
					// Informa el final de la ronda (a todos)
					InformFinishRoundStatusMessage inform = new InformFinishRoundStatusMessage(mjStr + constants.generalRoundFinishMessage());

					impl.setStatus(getAllPlayers(), inform);
					gamestate = 15;
					countstate++;
					drawCount = 0; // Inicializa nuevamente el contador de empates puesto que ya hubo una elección
				}
			}
			catch (ChatException e) {}
		case 15: // Informa el resultado de la primera ronda GENERAL
			try {
				InformGeneralRoundResultStatusMessage generalinform = new InformGeneralRoundResultStatusMessage(messages.generalRoundResult(selectedGeneral.getName()), selectedGeneral);//constants.GENERAL_ROUND_RESULT.replaceAll(constants2.CONSTANT_PLAYER_NAME, selectedGeneral.getName()), selectedGeneral);
				impl.setStatus(getAllPlayers(), generalinform);
				gamestate = 16;
				countstate++;
			}
			catch (ChatException e) {}
		case 16: // Inicia la ronda GENERAL de RÉPLICA
			//*********************************************************************+
			try {
				// Informa que comienza la ronda (a todos)
				InformStartRoundStatusMessage inform = new InformStartRoundStatusMessage(constants.revGeneralRoundStartMessage());
				impl.setStatus(getAllPlayers(), inform);
				// Persiste la ronda
				currentRound = dao.getGameDAO().addRound(game, constants.peopleRole() + "2");
				// Envía a TODOS, start general round (es el mismo msg para ambas rondas general)
				StartGeneralRoundStatusMessage startgral = new StartGeneralRoundStatusMessage();
				impl.setStatus(getAllPlayers(), startgral);

				countstate++;
				// Agrega las acciones al Timer que controlará la ronda
				timer.schedule(new PreviousFinishTimerTask(countstate, this, impl, constants.generalPreviousFinishMessage()), (long)(milsecgral * 0.75));
				timer.schedule(new FinishRoundTimerTask(countstate, 18, this, impl, constants.generalRoundFinishMessage()), milsecgral);

				// Pasa al siguiente estado donde se controla el final de la ronda
				informs = new Vector<PlayerInformMessage>();
				gamestate = 17;
			}
			catch (ChatException e) { /* TODO */}
			break;
		case 17: // Controla el final de la ronda GENERAL de RÉPLICA, 
			// Cuando la cantidad de mensajes recibidos es igual a la cantidad de jugadores aún en la partida o se alcanzó la mayoría
			
			if (informs.size() == game.getnPlayers() - eliminated_players.size() || (majority = majority(game.getnPlayers() - eliminated_players.size()))) {
				gamestate = 18;
				countstate++;
			}
			else	
				break;
		case 18: // Finaliza la ronda GENERAL de RÉPLICA
			try {
				// Determina quién ha sido elegido para ser eliminado del juego.
				Vector<GamePlayer> selectedPlayers = getPlayerSelected(informs, game.getnPlayers() - eliminated_players.size());
				if (selectedPlayers.size() > 1) {
					if (drawCount < MAX_DRAWS - 1) {
						// Se registró un empate en la votación, por lo cual se debe volver a votar
						// Envía a TODOS, start general round con la info del EMPATE
						StartGeneralRoundStatusMessage startgral = new StartGeneralRoundStatusMessage(constants.drawMessage(), selectedPlayers);
						impl.setStatus(getAllPlayers(), startgral);

						drawCount++; // Cuenta los empates consecutivos
						countstate++;
						// Agrega las acciones al Timer que controlará la ronda
						timer.schedule(new PreviousFinishTimerTask(countstate, this, impl, constants.generalPreviousFinishMessage()), (long)(milsecgral * 0.3));
						timer.schedule(new FinishRoundTimerTask(countstate, 18, this, impl, constants.generalRoundFinishMessage()), (long)(milsecgral * 0.4));

						// Pasa al estado donde se controla el final de la ronda
						informs = new Vector<PlayerInformMessage>();
						gamestate = 17;
						break;
					}
					else { // Se alcanzo el limite de empates
						// Informa del limite de empates.
						// Informa el final de la ronda (a todos)
						InformFinishRoundStatusMessage informdrawlimit = new InformFinishRoundStatusMessage(constants.drawRoundFinishMessage());
						impl.setStatus(getAllPlayers(), informdrawlimit);
						// Pasa directamente a una nueva ronda BAD
						gamestate = 1; // Se debe continuar con la ronda BAD
						drawCount = 0; // Inicializa nuevamente el contador de empates puesto que se alcanzó el limite
						countstate++;
						timer.schedule(new TimerTask() {
							@Override
							public void run() {
								updateState();
							}
						}, 0);
						break;
					}
				}
				else {
					// Hubo un único jugador seleccionado (mayor cantidad de votos)
					selectedGeneral = selectedPlayers.firstElement();
					// Persiste el resultado de la ronda
					dao.getGameDAO().setRoundResult(currentRound, selectedGeneral);
					
					String mjStr = ""; // Mensaje que advierte que se finalizó la votación por mayoría alcanzada.
					if (majority) {
						mjStr = constants.majorityMessage();
						majority = false;
					}
					// Informa el final de la ronda (a todos)
					InformFinishRoundStatusMessage inform = new InformFinishRoundStatusMessage(mjStr + constants.revGeneralRoundFinishMessage());

					impl.setStatus(getAllPlayers(), inform);
					gamestate = 19;
					countstate++;
					drawCount = 0; // Inicializa nuevamente el contador de empates puesto que ya hubo una elección
				}
			}
			catch (ChatException e) {}
		case 19: // Determina e informa el resultado de la ronda GENERAL de RÉPLICA (ex15)
			try {
				// Informa quién fue el jugador elegido para ser eliminado de la partida
				InformRevGeneralRoundResultStatusMessage generalinform = new InformRevGeneralRoundResultStatusMessage(messages.revGeneralRoundResult(selectedGeneral.getName()),selectedGeneral); //constants2.REV_GENERAL_ROUND_RESULT.replaceAll(constants2.CONSTANT_PLAYER_NAME, selectedGeneral.getName()), selectedGeneral);
				impl.setStatus(getAllPlayers(), generalinform);
				// Realiza la baja del jugador elegido
				// Pone al jugador en el vector de jugadores eliminados
				eliminated_players.addElement(selectedGeneral);
				// Determina el rol del jugador capturado
				String rolecaptured = players.get(selectedGeneral);
				// Disminuye la cantidad de jugadores de su rol en la partida
				countRoles.put(rolecaptured,countRoles.get(rolecaptured) - 1);
				// Persiste la captura/eliminación
				dao.getGameDAO().setPlayerEliminated(game, selectedGeneral, constants.peopleRole());
				// Chequea el fin de la partida (a favor de los BAD)
				if (countRoles.get(constants.goodRole()) + countRoles.get(constants.docRole()) + countRoles.get(constants.peopleRole()) == 0) {
					gamestate = 11; // Ganaron los BAD
					countstate++;
					timer.schedule(new TimerTask() {

						@Override
						public void run() {
							updateState(); ///// mmmmm.. revisar esto
						}
					}, 0);
					break;
				}
				else 
					// Chequea el fin de la partida a favor del resto
					if (countRoles.get(constants.badRole()) == 0) {
						gamestate = 20;
						countstate++;
					}
					else {
						gamestate = 1; // Se debe continuar con la ronda BAD
						countstate++;
						timer.schedule(new TimerTask() {

							@Override
							public void run() {
								updateState(); ///// mmmmm.. revisar esto
							}
						}, 0);
						break;
					}

			}
			catch (ChatException e) {}
		case 20: // Fin de la partida a favor de GOOD, DOC, PEOPLE (ex16)
			try {
				// Persiste el resultado de la partida
				dao.getGameDAO().setFinal(game, constants.peopleRole());
				// Informa el final de la partida
				InformFinishGameStatusMessage finishgame = new InformFinishGameStatusMessage(constants.winnerGoodMessage(), true, getPlayerRolesMessages());
				impl.setStatus(getAllPlayers(), finishgame);
				gamestate = 99; // 21 no existe, es por las dudas q se vuelva a entrar a updatestate
				countstate++;
				impl.removeGame(this.game);
				timer.cancel();
				break;
			}
			catch (ChatException e) {}
		default:
			break;
		}
	}

	/**
	 *  Metodo encargado de recibir los InformMessage de los jugadores, almacenarlos, informar
	 *  al resto de los jugadores (si corresponde) y actualizar el estado de la partida.
	 *  
	 * @param message <code>PlayerInformMessage</code> enviado por el jugador.
	 */
	public void receivePlayerInformMessage(PlayerInformMessage message) {
		// Almacena el mensaje recibido
		informs.addElement(message);
		// Informa al resto de los jugadores habilitados de la decisión
		String msgdec = messages.informDecisionMessage(message.getPlayer().getName(), message.getDecision());
		//msgdec = msgdec.replaceAll(constants.constantPlayerName(), message.getPlayer().getName()); // Reemplaza el nombre del emisor
		//msgdec = msgdec.replaceAll(constants.constantDecisionPlayer(), message.getDecision()); // Reemplaza el nombre del votado
		InformPlayerDecisionStatusMessage playerdec = new InformPlayerDecisionStatusMessage(msgdec, message.getPlayer(), getPlayerByName(message.getDecision())); // Construye el mensaje
		// Persiste el voto

		dao.getGameDAO().addVote(message.getPlayer(), getPlayerByName(message.getDecision()), game, currentRound, message.isAuto());

		try {
			// Envia el mensaje
			impl.setStatus(getEnabledPlayers(), playerdec);
		}
		catch (ChatException c) {}
		// Actualiza el estado (donde se chequea si la ronda finalizó)
		updateState();
	}

	// Determina para que grupo van los mensajes grupales dependiendo del estado de la partida
	public Vector<GamePlayer> getEnabledPlayers() {
		switch (gamestate) {
		case 0:
			return getAllPlayers();
		case 1:	
		case 2:
		case 3:
			return getPlayersByRole(constants.badRole());
		case 4:
		case 5:
		case 6:
			return getPlayersByRole(constants.goodRole());
		case 7:
		case 8:
		case 9:
			return getPlayersByRole(constants.docRole());
		case 10:
		case 11:
		case 12:
		case 13:
		case 14:
		case 15:
		case 16:
		case 17:
		case 18:
		case 19:
		case 20:
		case 99: // Para que al finalizar la partida puedan seguir chateando
			return getAllPlayers();
		default:
			break;
		}
		return new Vector<GamePlayer>();
	}

	public Round getCurrentRound() {
		return currentRound;
	}

	/**
	 * Devuelve <code>TRUE</code> si el juego ya ha comenzado.
	 * @return Verdadero si el juego ha comenzado.
	 */
	public boolean hasStarted() {
		return gamestate > 0;
	}	
	
	/**
	 * Devuelve <code>TRUE</code> si el juego ya ha finalizado.
	 * @return Verdadero si el juego ha comenzado.
	 */
	public boolean hasFinished() {
		return gamestate == 99;
	}

	private Vector<String> getPlayerRolesMessages() {
		Vector<String> msgs = new Vector<String>();
		for(Map.Entry<GamePlayer, String> map : players.entrySet())
			msgs.addElement(messages.finRoleInformMessage(map.getKey().getName(), constantsRole.getRoleName(map.getValue())));
			//msgs.addElement(constants2.FIN_ROLE_INFORM_MESSAGE.replace(constants2.CONSTANT_PLAYER_NAME, map.getKey().getName())
			//		.replace(constants2.CONSTANT_ROLE, constants2.getRoleName(map.getValue())));			
		return msgs;
	}

	// TODO ver si no es mas eficiente mantener cada vector por separado en vez de calcularlo cada vez
	private Vector<GamePlayer> getPlayersByRole(String role) {
		Vector<GamePlayer> vp = new Vector<GamePlayer>();
		for(Map.Entry<GamePlayer, String> map : players.entrySet())
			if (map.getValue().equals(role))
				vp.addElement(map.getKey());
		return vp;
	}

	private Vector<GamePlayer> getPlayersByRoles(Vector<String> roles) {
		Vector<GamePlayer> vp = new Vector<GamePlayer>();
		for(Map.Entry<GamePlayer, String> map : players.entrySet())
			if (roles.contains(map.getValue()))
				vp.addElement(map.getKey());
		return vp;
	}

	public Vector<GamePlayer> getAllPlayers() {
		Vector<GamePlayer> v = new Vector<GamePlayer>(); 
		v.addAll(players.keySet());
		return v;
	}

	public Vector<GamePlayer> getEliminatedPlayers() {
		return eliminated_players;
	}

	private Vector<GamePlayer> getNotEliminatedPlayers() {
		Vector<GamePlayer> vp = new Vector<GamePlayer>();
		for(Enumeration<GamePlayer> e = players.keys(); e.hasMoreElements();) {
			GamePlayer p = e.nextElement();
			if (!eliminated_players.contains(p))
				vp.addElement(p);
		}
		return vp;
	}

	/**
	 * Determina que Jugador ha sido seleccionado por decisión de la ronda.
	 * 
	 * @param informs Mensajes PlayerInformMessage enviados por los usuarios.
	 * @param n Cantidad de usuarios activos que participaron de la selección.
	 * @return Jugador seleccionado (P.e. Ronda BAD: jugador capturado) 
	 */
	private Vector<GamePlayer> getPlayerSelected(Vector<PlayerInformMessage> informs, int n) {
		// int[] counts = new int[nPlayers - eliminated_players.size()];
		// Contadores para cada jugador
		ConcurrentHashMap<GamePlayer, Integer> counts = new ConcurrentHashMap<GamePlayer, Integer>();
		// Vector de jugadores con máxima cantidad de votos
		Vector<GamePlayer> vmax = new Vector<GamePlayer>();
		// Máximo
		int max = -1;
		// Computa la cantida de votos para cada jugador
		for(Enumeration<PlayerInformMessage> e = informs.elements(); e.hasMoreElements();) {
			PlayerInformMessage inform = e.nextElement();
			GamePlayer player = getPlayerByName(inform.getDecision());
			Integer count = counts.get(player);

			if (count != null) {
				count++;
				counts.put(player, new Integer(count));
			}
			else {
				count = 1;
				counts.put(player, new Integer(count));
			}

			if (max == count) {
				if (!vmax.contains(player))
					vmax.addElement(player);
			}
			else
				if (max < count) {
					vmax.clear();
					vmax.addElement(player);
					max = count;
				}
		}

		// Determina quién fue seleccionado
		if (vmax.size() == 1) // Hubo un jugador con mayoría de votos
			return vmax;
		else
			if (vmax.size() == 0) { // No se eligió a nadie (y no se registraron votos).
				// Elige al azar entre los jugadores disponibles
				Vector<GamePlayer> notEliminated = getNotEliminatedPlayers();
				vmax.addElement(notEliminated.elementAt((int)(Math.random() * 100) % notEliminated.size()));
				return vmax;
			}
			else // Hay empate entre 2 o más jugadores.
				if (informs.size() == n) // Votaron todos los que debían votar
					return vmax; // Se devuelve el vector para que se desempate.
				else // No todos los que debían votar votaron
					return vmax; // Se devuelve el vector para que se desempate.
	}
	
	
	/**
	 * Determina si ya hay una mayoría en la votación irreversible con los votos que faltan
	 * @param n Cantidad de jugadores que están participando de la selección
	 * @return TRUE si ya hay una mayoria irreversible
	 */
	private boolean majority(int n) {
		// Contadores para cada jugador
		ConcurrentHashMap<GamePlayer, Integer> counts = new ConcurrentHashMap<GamePlayer, Integer>();
		// Vector de jugadores con máxima cantidad de votos
		Vector<GamePlayer> vmax = new Vector<GamePlayer>();
		// Máximo
		int max = -1;
		// Computa la cantida de votos para cada jugador
		for(Enumeration<PlayerInformMessage> e = informs.elements(); e.hasMoreElements();) {
			PlayerInformMessage inform = e.nextElement();
			GamePlayer player = getPlayerByName(inform.getDecision());
			Integer count = counts.get(player);

			if (count != null) {
				count++;
				counts.put(player, new Integer(count));
			}
			else {
				count = 1;
				counts.put(player, new Integer(count));
			}

			if (max == count) {
				if (!vmax.contains(player))
					vmax.addElement(player);
			}
			else
				if (max < count) {
					vmax.clear();
					vmax.addElement(player);
					max = count;
				}
		}
		
		if (vmax.size() > 1) // si hay hasta el momento un empate, no puede haber mayoría
			return false;
		
		boolean isMax = true;
		for(Enumeration<GamePlayer> ePlayers = players.keys(); ePlayers.hasMoreElements() && isMax;) {
			GamePlayer player = ePlayers.nextElement();
			Integer count = counts.get(player);
			if (count == null)
				count = 0; // No recibió ningún voto
			if (count.intValue() != max)
				isMax = (max - count.intValue()) > (n - informs.size());
		}
		return isMax;

	}

	public GamePlayer getPlayerByName(String name) {
		for(Map.Entry<GamePlayer, String> entry : players.entrySet())
			if (entry.getKey().getName().equals(name))
				return entry.getKey();

		return null;

	}

	/**
	 * Determina el tiempo que durará la ronda, en base a la cantidad de jugadores con el rol de la ronda.
	 * Cuando <code>role</code> es <code>null</code> representa la ronda general.
	 * @param role Rol de la ronda.
	 * @return Tiempo estimado de finalización.
	 */
	private long getTimeRound(String role) {
		if (role == null) // Ronda general
			return milsecgral;
		else
			if (countRoles.get(role) == 0) // No hay participantes del rol
				return (long)(milsec * 0.1);
			else
				return milsec;
	}

	public boolean persistUserMessage(UserMessage message) {
		return dao.getGameDAO().addUserMessage(message, game, currentRound);
	}
	
	public boolean persistArgument(ArgumentMessage message){
		return true;
	}

	protected long getCountState() {
		return countstate;
	}

	protected void setGameState(int state) {
		gamestate = state;
	}

}
