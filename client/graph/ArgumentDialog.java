package edu.isistan.rolegame.client.graph;

import java.util.Vector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.smartgwt.client.widgets.Dialog;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.CloseClientEvent;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

import edu.isistan.rolegame.client.ClientGameManager;
import edu.isistan.rolegame.shared.GamePlayer;
import edu.isistan.rolegame.shared.comm.ArgumentMessage;
import edu.isistan.rolegame.shared.ext.RolegameConstants;
import edu.isistan.rolegame.shared.ext.RolegameMessages;

public class ArgumentDialog extends Dialog {

	private ClientGameManager gameman;
	private Vector<GamePlayer> players;

	private ListBox yn_box; // yes or no option
	private ListBox player_box; // list of players to vote or acuse
	private Label text; // text for the argument
	private VLayout dataLay;
	private Vector<Widget> dataCol; //vector para poder luego mas facil pasar los datos a String
	private Vector<String> argsPlayerSel; //arguments loaded for the player selected, set by ClientGameManager

	private RolegameMessages messages = GWT.create(RolegameMessages.class);
	private RolegameConstants constants = GWT.create(RolegameConstants.class);

	public ArgumentDialog(ClientGameManager gamemanager, Vector<GamePlayer> players) {
		// set GENERAL ui
		this.gameman = gamemanager;
		this.players = players;
		setTitle(constants.argumentTitle());
		setSize("980px", "300px");
		addCloseClickHandler(new CloseClickHandler() {
			public void onCloseClick(CloseClientEvent event) {
				destroy();
			}
		});
		HLayout content = new HLayout();
		content.setWidth("910px");
		dataCol = new Vector<Widget>();
		argsPlayerSel = null;

		// set CLAIM section of the ui
		HLayout claim = new HLayout();
		claim.setWidth("270px");

		yn_box = new ListBox();
		yn_box.addItem(constants.claimOpt1());
		yn_box.addItem(constants.claimOpt2());
		yn_box.setWidth("42px");
		claim.addMember(yn_box);

		text = new Label();
		text.setText(messages.claimText()); // Esto podría cambiar en un futuro
											// si el texto se hace mas versatl
		text.setWidth("120px");
		text.setStyleName("argLabel");
		claim.addMember(text);

		player_box = new ListBox();
		player_box.setWidth("70px");
		player_box.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				//llamo a gameman para que pida mediante Comet los argumentos del nuevo jugador seleccionado
				gameman.loadArguments(player_box.getValue(player_box.getSelectedIndex()));		
			}
		});
		setPlayerNames();
		claim.addMember(player_box);
		claim.setMargin(7);

		content.addMember(claim);

		// set data/warranty section
		dataLay = new VLayout(); // data-warranty layout for multiple items
		dataLay.setHeight("100px");
		dataLay.addMember(getDataInterface());

		HLayout addLay = new HLayout();
		PushButton addButton = new PushButton();
		addButton.setText("+");
		addButton.setWidth("10px");
		addButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				dataLay.addMember(getDataInterface(), dataCol.size()/2);
			}
		});
		addLay.addMember(addButton);

		Label addMsg = new Label(constants.addMsg());
		addMsg.setStyleName("addLabel");
		addLay.addMember(addMsg);
		addLay.setMargin(7);

		dataLay.addMember(addLay);

		PushButton sendButton = new PushButton(constants.sendButton());
		sendButton.setWidth("37px");
		sendButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				sendArgument();
				destroy();
			}
		});
		dataLay.addMember(sendButton);

		content.addMember(dataLay);

		gameman.loadArguments(player_box.getValue(player_box.getSelectedIndex())); //debe hacerse aca por que sino no hay ningun ListBox disponible
		   																   //cuando se cargaron los nombres de los jugadores																		
		addItem(content);
	}

	private HLayout getDataInterface() {
		HLayout ui = new HLayout();
		ui.setWidth("300px");
		ui.setMargin(7);

		Label causeTxt = new Label();
		if (dataCol.size()==0)
			causeTxt.setText(messages.causeText());
		else
			causeTxt.setText(messages.extraCause());
		causeTxt.setWidth("85px");
		causeTxt.setStyleName("argLabel");
		ui.addMember(causeTxt);

		ListBox data_box = new ListBox();
		data_box.setStyleName("dataBox");
		data_box.setWidth("250px");
		ui.addMember(data_box);
		dataCol.add(data_box);

		Label henceTxt = new Label();
		henceTxt.setText(messages.warrantyText());
		henceTxt.setWidth("65px");
		henceTxt.setStyleName("argLabel");
		ui.addMember(henceTxt);

		TextArea warranty = new TextArea();
		warranty.setWidth("250px");
		warranty.setHeight("40px");
		warranty.setStyleName("warrantyTxtArea");
		ui.addMember(warranty);
		dataCol.add(warranty);

		loadArguments(player_box.getValue(player_box.getSelectedIndex()));
		
		return ui;
	}

	private void setPlayerNames() {
		for (GamePlayer p: players) {
			player_box.addItem(p.getName());
		}
	}
	
	private Vector<String> getArgumentTxt(){
		Vector<String> argument = new Vector<String>();

		argument.add(getClaimText());
		argument.addAll(getDataTexts());
		
		return argument;
	}
	
	public boolean sendArgument(){
		Vector<String> argument = getArgumentTxt();
		gameman.sendArgument(argument);
		return true;
	}

	private void loadArguments(String player){	//metodo llamado por el otro loadArguments() de la clase,
												//cuando se obtuvieron los argumentos pedidos al server.
		
		if (argsPlayerSel!=null){
			
			for (Widget w: dataCol){
				if (w instanceof ListBox){
					((ListBox) w).clear();
					for (String arg: argsPlayerSel)
						((ListBox)w).addItem(arg);
				}
			}
		}
	}
	
	private String getClaimText(){
		String claim = new String(); 
		claim = yn_box.getValue(yn_box.getSelectedIndex())
				+ " "
				+ messages.claimText()
				+ " "
				+ player_box.getValue(player_box.getSelectedIndex());
				
		System.out.println(claim);
		return claim;
	}
	
	private Vector<String> getDataTexts(){
		Vector<String> datas = new Vector<String>();
		
		String dataW = new String();
		boolean first_data = true;
		
		for (Widget w: dataCol){
			if (w instanceof ListBox){ //add data
				ListBox lb = (ListBox)w;
				if (first_data)
					dataW = " " + messages.causeText()
						+ " '" + lb.getValue(lb.getSelectedIndex()) + "'";
				else
					dataW = " " + messages.extraCause()
					+ " '" + lb.getValue(lb.getSelectedIndex()) + "'";
			}
			else{ //add warranty
				if (w instanceof TextArea){
					TextArea ta = (TextArea)w;
					if (!ta.getText().isEmpty()){
						dataW += " " + messages.warrantyText()
								+ " " + ta.getText();
					}
					datas.add(dataW);
					dataW = new String();
					first_data= false;
				}
			}
		}
		return datas;
	}

	public void loadArguments(Vector<String> arguments) {	//metodo activado por ClientGameManager al
															//conseguir onSucess() del llamado a Comet
		argsPlayerSel = arguments;
		loadArguments(player_box.getItemText(player_box.getSelectedIndex()));
	}
}
