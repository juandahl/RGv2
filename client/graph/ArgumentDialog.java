package edu.isistan.rolegame.client.graph;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextArea;
import com.smartgwt.client.widgets.Dialog;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.CloseClientEvent;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

import edu.isistan.rolegame.client.ClientGameManager;
import edu.isistan.rolegame.shared.ext.RolegameConstants;
import edu.isistan.rolegame.shared.ext.RolegameMessages;

public class ArgumentDialog extends Dialog {

	private ClientGameManager gameman;
	private ListBox yn_box; // yes or no option
	private ListBox player_box; // list of players to vote or acuse
	private Label text; // text for the argument
	private VLayout dataLay;
	private int datos;
	private boolean first_data;

	private RolegameMessages messages = GWT.create(RolegameMessages.class);
	private RolegameConstants constants = GWT.create(RolegameConstants.class);

	public ArgumentDialog(/*ClientGameManager gameman*/){
		//set general ui
		//this.gameman = gameman;
		setTitle(constants.argumentTitle());
		setSize("980px","300px");
		addCloseClickHandler(new CloseClickHandler() {
			public void onCloseClick(CloseClientEvent event){
				//CODIGO PARA PREGUNTAR AL USUARIO SI EFECTIVAMENTE QUIERE CERRAR
				destroy();
			}
		});
		HLayout content = new HLayout();
		content.setWidth("910px");
		datos=0;
		first_data=true;
		
		//set claim section of the ui
		HLayout claim = new HLayout();
		claim.setWidth("300px");
		
		yn_box = new ListBox();
		yn_box.addItem(constants.claimOpt1());
		yn_box.addItem(constants.claimOpt2());
		yn_box.setWidth("45px");
		claim.addMember(yn_box);
		
		text = new Label();
		text.setText(messages.claimText());	//Esto podría cambiar en un futuro si el texto se hace mas versatl
		text.setWidth("140px");
		text.setStyleName("argLabel");
		claim.addMember(text);
		
		player_box = new ListBox();
		player_box.addItem("A");
		player_box.addItem("B"); //AB TESTING DE PLAYER, LUEGO CAMBIAR
		claim.addMember(player_box);
		claim.setMargin(7);
		
		content.addMember(claim);
		
		//set data/warranty section
		dataLay = new VLayout(); //data-warranty layout for multiple items
		dataLay.setHeight("100px");
		dataLay.addMember(getDataInterface());
		first_data=false;
		datos++;

		HLayout addLay = new HLayout();
		PushButton addButton = new PushButton();
		addButton.setText("+");
		addButton.setWidth("10px");
		addButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				dataLay.addMember(getDataInterface(), datos);
				datos++;
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
				// TODO Auto-generated method stub
				destroy();
			}
		});
		dataLay.addMember(sendButton);

		content.addMember(dataLay);
		
		addItem(content);
	}

	private HLayout getDataInterface() {
		HLayout ui = new HLayout();
		ui.setWidth("300px");
		ui.setMargin(7);

		Label causeTxt = new Label();
		if (first_data)
			causeTxt.setText(messages.causeText());
		else
			causeTxt.setText(messages.extraCause());
		causeTxt.setWidth("70px");
		causeTxt.setStyleName("argLabel");
		ui.addMember(causeTxt);

		ListBox data_box = new ListBox();
		data_box.addItem("Aca un texto de prueba que puede extenderse del ancho minimo");
		data_box.setStyleName("dataBox");
		data_box.setWidth("250px");
		ui.addMember(data_box);

		Label henceTxt = new Label();
		henceTxt.setText(messages.warrantyText());
		henceTxt.setWidth("65px");
		henceTxt.setStyleName("argLabel");
		ui.addMember(henceTxt);

		TextArea warranty = new TextArea();
		warranty.setWidth("225px");
		warranty.setHeight("40px");
		warranty.setStyleName("warrantyTxtArea");
		ui.addMember(warranty);

		return ui;
	}

}
