package edu.isistan.rolegame.shared.ext;

import com.google.gwt.i18n.client.Constants;



public interface RolegameConstants extends Constants {
	@DefaultStringValue("badrole")
	String badRole();
	@DefaultStringValue("goodrole")
	String goodRole();
	@DefaultStringValue("docrole")
	String docRole();
	@DefaultStringValue("peoplerole")
	String peopleRole();
	@DefaultStringValue("initialrole")
	String initialRole();
	@DefaultStringValue("realbadrole")
	String realBadRole();
	@DefaultStringValue("realgoodrole")
	String realGoodRole();
	@DefaultStringValue("realdocrole")
	String realDocRole();
	@DefaultStringValue("realpeoplerole")
	String realPeopleRole();
	@DefaultStringValue("badstartmessage")
	String badStartMessage();
	@DefaultStringValue("goodstartmessage")
	String goodStartMessage();
	@DefaultStringValue("docstartmessage")
	String docStartMessage();
	@DefaultStringValue("peoplestartmessage")
	String peopleStartMessage();
	@DefaultStringValue("badroundstartmessage")
	String badRoundStartMessage();
	@DefaultStringValue("goodroundstartmessage")
	String goodRoundStartMessage();
	@DefaultStringValue("docroundstartmessage")
	String docRoundStartMessage();
	@DefaultStringValue("generalroundstartmessage")
	String generalRoundStartMessage();
	@DefaultStringValue("revgeneralroundstartmessage")
	String revGeneralRoundStartMessage();
	@DefaultStringValue("badroundfinishmessage")
	String badRoundFinishMessage();
	@DefaultStringValue("goodroundfinishmessage")
	String goodRoundFinishMessage();
	@DefaultStringValue("docroundfinishmessage")
	String docRoundFinishMessage();
	@DefaultStringValue("generalroundfinishmessage")
	String generalRoundFinishMessage();
	@DefaultStringValue("revgeneralroundfinishmessage")
	String revGeneralRoundFinishMessage();
	@DefaultStringValue("drawroundfinishmessage")
	String drawRoundFinishMessage();
	@DefaultStringValue("constantplayername")
	String constantPlayerName();
	@DefaultStringValue("constantdecisionplayer")
	String constantDecisionPlayer();
	@DefaultStringValue("constantrole")
	String constantRole();
	@DefaultStringValue("informdecisionmessage")
	String informDecisionMessage();
	@DefaultStringValue("generalpreviousfinishmessage")
	String generalPreviousFinishMessage();
	@DefaultStringValue("generaltimeout")
	String generalTimeout();
	@DefaultStringValue("finroleinformmessage")
	String finroleinformMessage();
	@DefaultStringValue("drawmessage")
	String drawMessage();
	@DefaultStringValue("majoritymessage")
	String majorityMessage();
	@DefaultStringValue("badroundselection")
	String badRoundSelection();
	@DefaultStringValue("docroundselection")
	String docRoundSelection();
	@DefaultStringValue("goodroundselection")
	String goodRoundSelection();
	@DefaultStringValue("trueinformgoodroundresult")
	String trueInformGoodRoundResult();
	@DefaultStringValue("falseinformgoodroundresult")
	String falseInformGoodRoundResult();
	@DefaultStringValue("truefinalresult")
	String trueFinalResult();
	@DefaultStringValue("falsefinalresult")
	String falseFinalResult();
	@DefaultStringValue("generalroundresult")
	String generalRoundResult();
	@DefaultStringValue("revgeneralroundresult")
	String revGeneralRoundResult();
	@DefaultStringValue("winnerbadmessage")
	String winnerBadMessage();
	@DefaultStringValue("winnergoodmessage")
	String winnerGoodMessage();
	// GameBoard
	@DefaultStringValue("titlegameboard")
	String titleGameBoard();
	@DefaultStringValue("voteButton")
	String voteButton();
	@DefaultStringValue("boardLabel")
	String boardLabel();
	@DefaultStringValue("playerList")
	String playerList();
	@DefaultStringValue("sendButton")
	String sendButton();
	@DefaultStringValue("titleGameConsole")
	String titleGameConsole();
	@DefaultStringValue("availableGames")
	String availableGames();
	@DefaultStringValue("playersOnline")
	String playersOnline();
	@DefaultStringValue("joinButton")
	String joinButton();
	@DefaultStringValue("inviteButton")
	String inviteButton();
	@DefaultStringValue("numberPlayers")
	String numberPlayers();
	@DefaultStringValue("createButton")
	String createButton();
	@DefaultStringValue("rulesButton")
	String rulesButton();
	@DefaultStringValue("alertDialog")
	String alertDialog();
	// AgentDialog
	@DefaultStringValue("hideButton")
	String hideButton();
	// InviteDialog
	@DefaultStringValue("joinButtonShort")
	String joinButtonShort();
	@DefaultStringValue("ignoreButton")
	String ignoreButton();
	// LoginDialog
	@DefaultStringValue("loginTitle")
	String loginTitle();
	@DefaultStringValue("welcomeMessage")
	String welcomeMessage();
	@DefaultStringValue("userLabel")
	String userLabel();
	@DefaultStringValue("passwordLabel")
	String passwordLabel();
	@DefaultStringValue("loginButton")
	String loginButton();
	@DefaultStringValue("registerButton")
	String registerButton();
	@DefaultStringValue("registrationTitle")
	String registrationTitle();
	@DefaultStringValue("usernameLabel")
	String usernameLabel();
	@DefaultStringValue("emaillabel")
	String emailLabel();
	@DefaultStringValue("passRegLabel")
	String passRegLabel();
	@DefaultStringValue("passRepLabel")
	String passRepLabel();
	@DefaultStringValue("sexLabel")
	String sexLabel();
	@DefaultStringValue("birthLabel")
	String birthLabel();
	// RulesDialog
	@DefaultStringValue("rulesTitle")
	String rulesTitle();
}

/*public interface RolegameConstants extends Constants {
String badRole();
String goodRole();
String docRole();
String peopleRole();
String inicialRole();
String badStartMessage();
String goodStartMessage();
String docStartMessage();
String peopleStartMessage();
String badRoundStartMessage();
String goodRoundStartMessage();
String docRoundStartMessage();
String generalRoundStartMessage();
String revGeneralRoundStartMessage();
String badRoundFinishMessage();
String goodRoundFinishMessage();
String docRoundFinishMessage();
String generalRoundFinishMessage();
String revGeneralRoundFinishMessage();
String drawRoundFinishMessage();
String constantPlayerName();
String constantDecisionPlayer();
String constantRole();
String informDecisionMessage();
String generalPreviousFinishMessage();
String generalTimeout();
String finroleinformMessage();
String drawMessage();
String majorityMessage();
String badRoundSelection();
String docRoundSelection();
String goodRoundSelection();
String trueInformGoodRoundResult();
String falseInformGoodRoundResult();
String trueFinalResult();
String falseFinalResult();
String generalRoundResult();
String revGeneralRoundResult();
String winnerBadMessage();
String winnerGoodMessage();*/