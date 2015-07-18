package edu.isistan.rolegame.client.graph;

import java.util.Enumeration;
import java.util.Vector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.ClosingHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.CloseClientEvent;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

import edu.isistan.rolegame.client.ClientGameManager;
import edu.isistan.rolegame.shared.Game;
import edu.isistan.rolegame.shared.GamePlayer;
import edu.isistan.rolegame.shared.PlayerState;
import edu.isistan.rolegame.shared.Role;
import edu.isistan.rolegame.shared.RoleClient;
import edu.isistan.rolegame.shared.comm.UserMessage;
import edu.isistan.rolegame.shared.ext.RolegameConstants;
import edu.isistan.rolegame.shared.ext.RolegameMessages;


public class GameBoard extends Window implements GeneralGameBoard {

	private Tree treeMainChat;
	private ListBox lstPlayers;
	private TextBox txtMessage;
	private Button btnSend;
	private Button btnAddArg;
	private ScrollPanel scrollPanelTree;
	private ScrollPanel scrollPanelList;
	
	private ClientGameManager gameman;
	// Vector de jugadores elegibles (que aún no fueron eliminados del juego)
	private Vector<GamePlayer> players;
	private VLayout vLayout;
	private Button btnVote;
	private GamePlayer player;
	private boolean wasEliminated;
	private boolean isGameStarted; // false si aún no comenzó
	private boolean isGameFinished;
	private HLayout hLayout_1;
	private Label lblNewLabel;
	private Label lblJugadores;
	private int timercount;
	
	private GeneralAgentDialog agent;
	private Game game;
	
	// Internalization
	private RolegameConstants constants = GWT.create(RolegameConstants.class);
	private RolegameMessages messages = GWT.create(RolegameMessages.class);

	
	public GameBoard(ClientGameManager man, Game game) {
		super();
		this.gameman = man;
		timercount = 0;
		this.game = game;
		
		setAutoCenter(true);
		setShowMinimizeButton(false);
		setShowResizer(false);
		setShowHeaderIcon(false);
		setShowCloseButton(true);
		setAutoSize(true);
		setTitle(constants.titleGameBoard());
		resizeTo(350, 200);
		setCanDragReposition(false);
		centerInPage();
		
		VLayout layout = new VLayout();
		
		HLayout hLayout = new HLayout();
		hLayout.setHeight("350px");
		
		scrollPanelTree = new ScrollPanel();
		hLayout.addMember(scrollPanelTree);
		scrollPanelTree.setSize("635px", "351px");
		
		// Toma el cierre de la ventana, como el Back to Previous Page.
		com.google.gwt.user.client.Window.addWindowClosingHandler(new ClosingHandler() {
			@Override
			public void onWindowClosing(ClosingEvent event) {
				gameman.logout();
			}
		});
		
		addCloseClickHandler(new CloseClickHandler() {
			@Override
			public void onCloseClick(CloseClientEvent event) {
				gameman.leaveGame(isGameFinished);
				hide();
				agent.hide();
			}
		});
		
		treeMainChat = new Tree();
		treeMainChat.setSize("635px", "350px");
		treeMainChat.setStyleName("tree-unlock");
		treeMainChat.setTabIndex(2);
		
		scrollPanelTree.setWidget(treeMainChat);
		
		vLayout = new VLayout();
		vLayout.setWidth("140px");
		
		scrollPanelList = new ScrollPanel();
		vLayout.addMember(scrollPanelList);
		scrollPanelList.setSize("140px", "316px");
		
		lstPlayers = new ListBox();
		lstPlayers.setStyleName("list-style");
		lstPlayers.setVisibleItemCount(10);
		lstPlayers.setSize("140px", "315px");
		lstPlayers.setTabIndex(3);
		scrollPanelList.setWidget(lstPlayers);
		
		btnVote = new Button(constants.voteButton());
		btnVote.setStyleName("sendButton");
		btnVote.setSize("140px", "34px");
		btnVote.setVisible(false); // Lo mostramos solo cuando hay q votar
		btnVote.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				clickVote();
			}
		});
		btnVote.setTabIndex(4);
		
		hLayout_1 = new HLayout();
		hLayout_1.setSize("775px", "1px");
		
		lblNewLabel = new Label(constants.boardLabel());
		lblNewLabel.setStyleName("label-style");
		hLayout_1.addMember(lblNewLabel);
		lblNewLabel.setSize("635px", "18px");
		
		lblJugadores = new Label(constants.playerList());
		lblJugadores.setStyleName("label-style");
		hLayout_1.addMember(lblJugadores);
		layout.addMember(hLayout_1);
		vLayout.addMember(btnVote);
		hLayout.addMember(vLayout);
		layout.addMember(hLayout);
		addItem(layout);
		
		FlexTable flexTable = new FlexTable();
		addItem(flexTable);
		flexTable.setSize("777px", "29px");
		
		txtMessage = new TextBox();
		flexTable.setWidget(0, 0, txtMessage);
		txtMessage.setSize("678px", "25px");
		txtMessage.setFocus(true);
		txtMessage.addKeyDownHandler(new KeyDownHandler() {
			
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
					sendMessage();
			}
		});
		txtMessage.setTabIndex(0);
		
		btnSend = new Button(constants.sendButton());
		btnSend.setStyleName("sendButton");
		flexTable.setWidget(0, 2, btnSend);
		btnSend.setWidth("78px");
		flexTable.getFlexCellFormatter().setColSpan(0, 0, 2);
		btnSend.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				sendMessage();
			}
		});
		btnSend.setTabIndex(2);
		players =  new Vector<GamePlayer>();

		btnAddArg = new Button(constants.addArgButton());
		btnAddArg.setStyleName("addArgButton");
		flexTable.setWidget(0,1,btnAddArg);
		btnSend.setWidth("150px");
		flexTable.getFlexCellFormatter().setColSpan(0,0,4);
		btnAddArg.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				addArgument();
			}
		});
		btnAddArg.setTabIndex(1);

		
		wasEliminated = false;
		isGameStarted = false;
		isGameFinished = false;
		
		player = man.getOwner();
		
		agent = new AgentDialog(this);
		
	}
	
	@Override
	public void setClientGameManager(ClientGameManager gameman) {
		this.gameman = gameman;
	}

	@Override
	public boolean postMessage(UserMessage msg) {
		// Define el estilo según el tipo de mensaje
		String styleName;
		switch (msg.getType()) {
		case UserMessage.ERROR_GAME_MESSAGE:
			styleName = "error-msg";
			break;
		case UserMessage.MASTER_GAME_MESSAGE:
			styleName = "master-msg";
			break;
		case UserMessage.PLAYER_GAME_MESSAGE:
			styleName = "player-msg";
			// Resalta la aparición del nombre propio en mensajes de usuario
			msg.setText(msg.getText().replaceAll(player.getName(), "<b>" + player.getName() + "</b>"));
			break;
		case UserMessage.INFORM_GAME_MESSAGE:
			styleName = "inform-msg";
			break;
		default:
			styleName = "error-msg";
			break;
		}
		String sendername;
		if (msg.getSender() == null)
			sendername = "";
		else
			sendername = msg.getSender().getName() + ": ";
		
		
		
		TreeItem node = new TreeItem(sendername + msg.getText());
		node.addStyleName(styleName);
		node.setWidth("600px");

		if (msg.getType() == UserMessage.MASTER_GAME_MESSAGE) {
			// Si el mensaje es del master retardo su visualización para simular secuencialidad
			DelayTimer timer = new DelayTimer(node);
			timercount++; // Se usa para determinar el retraso que debe tener el mensaje de acuerdo a la cantidad de mensajes retrasados en espera de ser publicados
			timer.schedule(1000 * timercount);
		}
		else { // Si no es del MASTER lo muestro inmediatamente
			treeMainChat.addItem(node);
			// Mantiene el arbol de chat en el último mensaje agregado
			scrollPanelTree.scrollToBottom();
		}

		return true;
	}

	@Override
	public void addPlayer(GamePlayer player) {
		players.addElement(player);
		lstPlayers.addItem(player.getName());
		// Calcula cuantos jugadores faltan ingresar al juego para que éste comience
		int n = game.getnPlayers() - players.size();
		// Postea el mensaje de aviso de nuevo jugador
		postMessage(new UserMessage(UserMessage.INFORM_GAME_MESSAGE, messages.joinToGameMessage(player.getName(), n), null));
		//lstPlayers.getElement().getElementsByTagName(player.getName()).getItem(0).setClassName("player-eliminated");
		// Resalta a si mismo en la lista.
		if (gameman.getOwner().equals(player)) {
			int i = players.indexOf(player);
			if (i > -1)
				lstPlayers.getElement().getElementsByTagName("option").getItem(i).setClassName("player-self");
			agent.informWaitingPlayers();
		}
	}

	@Override
	public void removePlayer(GamePlayer player) {
		int i = players.indexOf(player);
		if (i > -1) { // El jugador no ha sido eliminado del juego (el juego no ha comenzado aún)
			players.remove(player);
			lstPlayers.removeItem(i);
			// Calcula cuantos jugadores faltan ingresar al juego para que éste comience
			int n = game.getnPlayers() - players.size();
			// Postea el mensaje de aviso de baja de jugador
			postMessage(new UserMessage(UserMessage.INFORM_GAME_MESSAGE, messages.desertGameMessage(player.getName(), n), null));
		}
		else {
			postMessage(new UserMessage(UserMessage.INFORM_GAME_MESSAGE, messages.leaveGameMessage(player.getName()), null));
		}
	}

	@Override
	public boolean sendMessage() {
		gameman.sendMessage(txtMessage.getText());
		txtMessage.setText("");
		txtMessage.setFocus(true);
		return true;
	}

	@Override
	public void startGame() {
		postMessage(new UserMessage(UserMessage.INFORM_GAME_MESSAGE, messages.startGameMessage(), null));
		isGameStarted = true;
		this.setShowCloseButton(false);
	}
	
	@Override
	public void informRoleGame(String role, String rolename, String msg, Vector<GamePlayer> allies) {
		// No se puede usar Skins pq necesita recargar la página
		this.setTitle(getTitle() + ": " + rolename);
		postMessage(new UserMessage(UserMessage.MASTER_GAME_MESSAGE, msg, null));
		// Se visualiza el rol asignado con la imagen del agente
		agent.setRole(role);
		agent.informStartGame();
		// Resalta los "aliados" (jugadores del mismo rol) en la lista de usuarios.
		for(Enumeration<GamePlayer> e = allies.elements(); e.hasMoreElements();) {
			GamePlayer p = e.nextElement();
			if (!p.equals(player)) { // Si no es el mismo jugador
				Element element = getElementFromList(p);
				if (element != null) // Resalta el aliado
					element.setClassName("player-ally");
			}
		}
	}
	
	/**
	 * Devuelve el elemento de la lista lstPlayers correspondiente al jugador <code>player</code>
	 * @param player Jugador buscado
	 * @return Elemento correspondiente a <code>player</code>, null si no existe
	 */
	private Element getElementFromList(GamePlayer player) {
		int j;
		for(j = 0; j < lstPlayers.getItemCount() && !lstPlayers.getItemText(j).equals(player.getName()); j++);
	
		if (j < lstPlayers.getItemCount())
			return lstPlayers.getElement().getElementsByTagName("option").getItem(j);
		else
			return null;
	}
	
	@Override
	public void lockGame() {
		txtMessage.setEnabled(false);
		btnSend.setEnabled(false);
		if (!wasEliminated)
			agent.informOtherRound();
		treeMainChat.setStyleName("tree-lock");	
		scrollPanelTree.setStyleName("tree-lock");
	}

	@Override
	public void unlockGame() {
		txtMessage.setEnabled(true);
		btnSend.setEnabled(true);
		if (!wasEliminated)
			agent.informVotingInstruction();
		treeMainChat.setStyleName("tree-unlock");
		scrollPanelTree.setStyleName("tree-unlock");
	}

	@Override
	public void informStartRound(String msg) {
		postMessage(new UserMessage(UserMessage.MASTER_GAME_MESSAGE, msg, null));
		
	}
	
	@Override
	public void informPreviousFinishRound(String msg) {
		postMessage(new UserMessage(UserMessage.INFORM_GAME_MESSAGE, msg, null));
		// TODO indicar graficamente y sonoramente la urgencia 
	}

	@Override
	public void informFinishRound(String msg) {
		postMessage(new UserMessage(UserMessage.MASTER_GAME_MESSAGE, msg, null));
	}

	@Override
	public void enabledVotation(boolean b) {
		if (!wasEliminated) {
			btnVote.setVisible(b);
			btnVote.setEnabled(b);
		}
	}
	
	@Override
	public void informGoodRoundResult(String msg, GamePlayer player, boolean b) {
		// TODO El boolean determinará la animación a realizar
		postMessage(new UserMessage(UserMessage.MASTER_GAME_MESSAGE, msg, null));
	}

	private void clickVote() {
		if (lstPlayers.getSelectedIndex() > -1) { // Se seleccionó un jugador
			String playersel = lstPlayers.getItemText(lstPlayers.getSelectedIndex());
			if (getEnabledPlayer(playersel) != null) { // Es un jugador habilitado
				if (gameman.sendPlayerInformMessage(playersel))
					// Evita votos dobles
					btnVote.setEnabled(false);
				else
					postMessage(new UserMessage(UserMessage.ERROR_GAME_MESSAGE, messages.errorConexion(), null));
			}
			else
				postMessage(new UserMessage(UserMessage.ERROR_GAME_MESSAGE, messages.errorPlayerEliminated(), null));
		}
		else
			postMessage(new UserMessage(UserMessage.ERROR_GAME_MESSAGE, messages.errorNoPlayerSelect(), null));
	}
	
	/**
	 * Devuelve un <code>GamePlayer</code> que corresponde con el nombre pasado como parámetro.
	 * 
	 * @param name Nombre del jugador buscado.
	 * @return Devuelve el jugador buscado, o <code>null</code>
	 */
	private GamePlayer getEnabledPlayer(String name) {
		GamePlayer player;
		for(Enumeration<GamePlayer> e = players.elements(); e.hasMoreElements();) {
			player = e.nextElement();
			if (player.getName().equals(name))
				return player;
		}
		return null;
	}

	@Override
	public void informRoundsFinalResult(String msg, GamePlayer player, boolean isCaptured) {
		// TODO El boolean determinará la animación a realizar
		postMessage(new UserMessage(UserMessage.MASTER_GAME_MESSAGE, msg, null));
	}

	@Override
	public void informFinishGame(String msg, boolean winBad, Vector<String> rolemessages) {
		postMessage(new UserMessage(UserMessage.MASTER_GAME_MESSAGE, msg, null));
		btnVote.setVisible(false);
		// TODO dar la posibilidad de reiniciar la partida
		
		// Revela los roles de cada jugador
		postMessage(new UserMessage(UserMessage.MASTER_GAME_MESSAGE, messages.informationGame(), null));
		for(Enumeration<String> e = rolemessages.elements(); e.hasMoreElements();)
			postMessage(new UserMessage(UserMessage.MASTER_GAME_MESSAGE, " - " + e.nextElement(), null));
		
		// Habilita la salida de la partida
		this.setShowCloseButton(true);
		isGameFinished = true;
		agent.informFinishGame();
		// Habilita el chat
		btnSend.setVisible(true);
		btnSend.setEnabled(true);
		txtMessage.setVisible(true);
		txtMessage.setEnabled(true);
	}

	@Override
	public void setPlayer(GamePlayer player) {
		this.player = player;
		this.setTitle(player.getName());
	}

	@Override
	public void informGeneralRoundResult(String msg, GamePlayer player) {
		postMessage(new UserMessage(UserMessage.MASTER_GAME_MESSAGE, msg, null));
	}
	
	@Override
	public void informRevGeneralRoundResult(String msg, GamePlayer player) {
		postMessage(new UserMessage(UserMessage.MASTER_GAME_MESSAGE, msg, null));
	}

	@Override
	public void informPlayerDecision(String msg, GamePlayer player, GamePlayer voted) {
		// TODO Utilizar el dato de a quien se vota para mostrar alguna estadistica de como va la votación.
		postMessage(new UserMessage(UserMessage.INFORM_GAME_MESSAGE, msg, null));
		
	}

	@Override
	public void playerWasEliminated() {
		// Hace no visibles los componentes q le permiten participar del juego
		btnVote.setVisible(false);
		btnSend.setVisible(false);
		txtMessage.setVisible(false);
		// Informa la eliminación via agente.
		agent.informElimination();
		// Habilita el botón cerrar para abandonar la partida.
		this.setShowCloseButton(true);
		// Setea el flag de eliminación
		wasEliminated = true;
	}

	@Override
	public void informPlayerEliminated(GamePlayer player) {
		int i = players.indexOf(player);
		if (i > -1) {
			// Elimina al jugador de la lista de jugadores habilitados
			players.remove(player);
			// Marca al jugador como elimina, busca por nombre para marcar correctamente la eliminación
			int j;
			for(j = 0; j < lstPlayers.getItemCount() && !lstPlayers.getItemText(j).equals(player.getName()); j++);
			
			if (j < lstPlayers.getItemCount())
				lstPlayers.getElement().getElementsByTagName("option").getItem(j).setClassName("player-eliminated");
		}
		// TODO problema al identificar eliminados
		if (this.player.equals(player))
			playerWasEliminated(); //TODO deberia ser invocado por un mensaje aparte, enviado desde servermanager?
	}
	
	@Override
	public void informRoundSelection(String msg, GamePlayer player) {
		postMessage(new UserMessage(UserMessage.INFORM_GAME_MESSAGE, msg, null));
	}

	@Override
	public void setPreviousState(Vector<GamePlayer> players, Vector<GamePlayer> eliminated, PlayerState pstate) {
		GamePlayer newplayer;
		for(Enumeration<GamePlayer> ep = players.elements(); ep.hasMoreElements();) {
			newplayer = ep.nextElement();
			this.players.addElement(newplayer);
			lstPlayers.addItem(newplayer.getName());
			// Resalta a si mismo en la lista.
			if (gameman.getOwner().equals(newplayer)) {
				int i = this.players.indexOf(newplayer);
				if (i > -1)
					lstPlayers.getElement().getElementsByTagName("option").getItem(i).setClassName("player-self");
			}
			if (eliminated.contains(newplayer)) {
				informPlayerEliminated(newplayer);
			}
		}
		
		Role constants = new RoleClient();
		this.setTitle(getTitle() + ": " + constants.getRoleName(pstate.getRole()));
		btnVote.setEnabled(pstate.isCanVote());
	}

	@Override
	public void informDraw(String msg, Vector<GamePlayer> draws) {
		postMessage(new UserMessage(UserMessage.INFORM_GAME_MESSAGE, msg, null));
		for(Enumeration<GamePlayer> e = draws.elements(); e.hasMoreElements();) {
			postMessage(new UserMessage(UserMessage.INFORM_GAME_MESSAGE, "  - " + e.nextElement().getName(), null));
			// TODO habilitar la votación solo entre los que empataron
		}
	}

	@Override
	public void informTimeOut(String message) {
		postMessage(new UserMessage(UserMessage.INFORM_GAME_MESSAGE, message, null));
	}
	
	/**
	 * Clase utilizada para retardar los mensajes del MASTER con el objetivo de simular
	 * la secuencialidad  de los mismos
	 */
	private class DelayTimer extends Timer {
		private TreeItem node;
		
		public DelayTimer(TreeItem node) {
			super();
			this.node = node;
		}
		
		@Override
		public void run() {
			timercount--;
			treeMainChat.addItem(node);
			// Mantiene el arbol de chat en el último mensaje agregado
			scrollPanelTree.scrollToBottom();
		}
		
	}
	
	@Override
	public void show() {
		super.show();
		txtMessage.setFocus(true);
		agent.inicializePosition(agent.RIGHT_POSITION);
	}
	
	@Override
	public void hide() {
		super.hide();
		agent.hide();
	}

	private void addArgument(){
		
	}

}
