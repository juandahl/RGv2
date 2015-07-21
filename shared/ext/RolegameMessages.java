package edu.isistan.rolegame.shared.ext;

import com.google.gwt.i18n.client.Messages;

public interface RolegameMessages extends Messages {
	
	@DefaultMessage("{0} ha votado a {1}.")
	String informDecisionMessage(String player, String voted);
	
	@DefaultMessage("El rol del jugador {0} era {1}.")
	String finRoleInformMessage(String player, String role);
	
	@DefaultMessage("Fin de la votación. Los Dragones han seleccionado a {0}.")
	String badRoundSelection(String player);
	
	@DefaultMessage("Fin de la votación. Los Hechiceros han seleccionado a {0}.")
	String docRoundSelection(String player);
	
	@DefaultMessage("Fin de la votación. Los Caballeros han seleccionado a {0}.")
	String goodRoundSelection(String player);
	
	@DefaultMessage("Se han detectado fuerzas malignas sobre {0}, es un DRAGON sin lugar a duda!")
	String trueInformGoodRoundResult(String player);
	
	@DefaultMessage("No hay razón para sospechar de {0}, no es un Dragón..")
	String falseInformGoodRoundResult(String player);
	
	@DefaultMessage("¡Las fuerzas oscuras se han impuesto en esta batalla! {0} ha sido capturado...")
	String trueFinalResult(String player);
	
	@DefaultMessage("¡Felicitaciones! El Gran Hechicero ha logrado frustrar el ataque dragón. No se han reportado capturas.")
	String falseFinalResult();
	 
	@DefaultMessage("Los ciudadanos han debatido y han señalado a {0} como presunto Dragón. Tiene derecho a réplica.")
	String generalRoundResult(String player);
	
	@DefaultMessage("¡Los ciudadanos han decidido finalmente que {0} abandone la comarca acusado de ser un Dragón!")
	String revGeneralRoundResult(String player);
	
	@DefaultMessage("joinToGameMessage {0} {1}")
	String joinToGameMessage(String player, int n);
	@DefaultMessage("desertGameMessage {0} {1}")
	String desertGameMessage(String player, int n);
	@DefaultMessage("leaveGameMessage {0}")
	String leaveGameMessage(String player);
	@DefaultMessage("startGameMessage")
	String startGameMessage();
	@DefaultMessage("errorConexion")
	String errorConexion();
	@DefaultMessage("errorPlayerEliminated")
	String errorPlayerEliminated();
	@DefaultMessage("errorNoPlayerSelect")
	String errorNoPlayerSelect();
	@DefaultMessage("informationGame")
	String informationGame();
	@DefaultMessage("createGameLabel {0}")
	String createGameLabel(int n);
	@DefaultMessage("Partida Nro. {0} creada por {1} (Jugadores unidos a la partida: {2} de {3} necesarios)")
	String infoGameMessage(int idGame, String name, int count, int n);
	@DefaultMessage("invitationMessage {0} {1} {2}")
	String invitationMessage(String player, String game, int nPlayers);
	@DefaultMessage("errorEncryption")
	String errorEncryption();
	@DefaultMessage("errorInvitation")
	String errorInvitation();
	@DefaultMessage("errorNPlayers")
	String errorNPlayers();
	@DefaultMessage("errorPlayerJoined")
	String errorPlayerJoined();
	@DefaultMessage("errorRegNoData")
	String errorRegNoData();
	@DefaultMessage("errorRegPass")
	String errorRegPass();
	@DefaultMessage("errorRegEmail")
	String errorRegEmail();
	@DefaultMessage("errorRegBirthday")
	String errorRegBirthday();
	@DefaultMessage("successfulLoginMessage")
	String successfulLoginMessage();
	@DefaultMessage("errorsReg")
	String errorsReg();
	@DefaultMessage("errorLogin")
	String errorLogin();
	@DefaultMessage("errorPlayerLogged {0}")
	String errorPlayerLogged(String player);
	@DefaultMessage("errorBotLogged {0}")
	String errorBotLogged(String player);
	@DefaultMessage("errorNoSesion {0}")
	String errorNoSesion(String error);
	@DefaultMessage("errorCreateGame")
	String errorCreateGame();
	@DefaultMessage("errorGameStarted")
	String errorGameStarted();
	@DefaultMessage("errorNoAvUsername {0}")
	String errorNoAvUsername(String player);
	@DefaultMessage("errorGameFinish")
	String errorGameFinish();
	@DefaultMessage("errorPlayerOcup")
	String errorPlayerOcup();
	@DefaultMessage("errorSelfInvitation")
	String errorSelfInvitation();
	@DefaultMessage("waitingPlayerAgent")
	String waitingPlayerAgent();
	@DefaultMessage("startGameAgent")
	String startGameAgent();
	@DefaultMessage("votingIntructionAgent")
	String votingIntructionAgent();
	@DefaultMessage("otherRoundAgent")
	String otherRoundAgent();
	@DefaultMessage("eliminationAgent")
	String eliminationAgent();
	@DefaultMessage("finishGameAgent")
	String finishGameAgent();
	@DefaultMessage("invitationSent")
	String invitationSent();
	
	@DefaultMessage("rulesText")
	String rulesHTML();
	
	@DefaultMessage("claimText")
	String claimText();

}
