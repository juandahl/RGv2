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
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.smartgwt.client.widgets.Dialog;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.CloseClientEvent;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

import edu.isistan.rolegame.client.ClientGameManager;
import edu.isistan.rolegame.shared.GamePlayer;
import edu.isistan.rolegame.shared.ext.RolegameConstants;
import edu.isistan.rolegame.shared.ext.RolegameMessages;

public class ArgumentDialog extends Dialog implements AfterAlertDialogExecution{

	private ClientGameManager gameman;
	private Vector<GamePlayer> players;

	private ListBox yn_box; // yes or no option
	private TextBox claim_text; 
	private ListBox player_box; // list of players to vote or acuse
	private VLayout dataLay;
	private Vector<Widget> dataCol; //vector para poder luego mas facil pasar los datos a String
	private Vector<String> argsPlayerSel; //arguments loaded for the player selected, set by ClientGameManager

	private RolegameMessages messages = GWT.create(RolegameMessages.class);
	private RolegameConstants constants = GWT.create(RolegameConstants.class);
	private static final int extraButtons=2;
	private static final int dataPosition=2; //comienza en 0 la num. en layout

	public ArgumentDialog(ClientGameManager gamemanager, Vector<GamePlayer> players) {
		// set GENERAL ui
		this.gameman = gamemanager;
		this.players = players;
		setTitle(constants.argumentTitle());
		setSize("1000px", "300px");
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

		claim_text = new TextBox();
		claim_text.setText(messages.claimText());
		claim_text.setWidth("140px");
		claim_text.setStyleName("argLabel");
		claim.addMember(claim_text);

		player_box = new ListBox();
		player_box.setWidth("100px");
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

		// set DATA/WARRANY section
		dataLay = new VLayout(); // data-warranty layout for multiple items
		dataLay.setHeight("100px");
		dataLay.addMember(getDataInterface());

		HLayout addLay = new HLayout();
		PushButton addButton = new PushButton();
		addButton.setText(constants.plus());
		addButton.setWidth("10px");
		addButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				dataLay.addMember(getDataInterface(), (dataCol.size()/2)-(extraButtons-1));
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
				if (sendArgument())
					destroy();
				else hide();
			}
		});
		dataLay.addMember(sendButton);

		content.addMember(dataLay);

		gameman.loadArguments(player_box.getValue(player_box.getSelectedIndex())); //debe hacerse aca por que sino no hay ningun ListBox disponible
		   																   //cuando se cargaron los nombres de los jugadores																		
		addItem(content);
	}

	private HLayout getDataInterface() {	//cada vez que se toca el boton de Añadir, se crea una nueva interfaz de Data/Warranty
		HLayout ui = new HLayout();
		ui.setWidth("300px");
		ui.setMargin(7);

		//texto conectivo entre claim y data. "ya que" 
		Label causeTxt = new Label();
		if (dataCol.size()==0)
			causeTxt.setText(messages.causeText());
		else
			causeTxt.setText(messages.extraCause());
		causeTxt.setWidth("60px");
		causeTxt.setStyleName("argLabel");
		ui.addMember(causeTxt);
		
		//opciones para ingresar argumento previo de player o un texto cualquiera
		ui.addMember(getOptionsInterface());

		//lista donde se cargan los argumentos del player elegido
		ListBox data_box = new ListBox();
		data_box.setStyleName("dataBox");
		data_box.setWidth("225px");
		ui.addMember(data_box);
		dataCol.add(data_box);

		//texto conectivo entre data y warranty
		Label henceTxt = new Label();
		henceTxt.setText(messages.warrantyText());
		henceTxt.setWidth("65px");
		henceTxt.setStyleName("argLabel");
		ui.addMember(henceTxt);

		//espacio para poner Warranty
		TextArea warranty = new TextArea();
		warranty.setWidth("225px");
		warranty.setHeight("40px");
		warranty.setStyleName("warrantyTxtArea");
		ui.addMember(warranty);
		dataCol.add(warranty);

		//llamado para popular de información el listado de argumentos del player elegido
		loadArguments();

		return ui;
	}

	private VLayout getOptionsInterface(){		//interfaz para seleccionar si se pone 
												//el listado de argumentos previos o un texto a escribir por el usuario
		VLayout options = new VLayout();
		//primer botton: dijo
		RadioButton rd1 = new RadioButton(Integer.toString(dataCol.size()/2),constants.radioOpt1());
		rd1.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				ListBox data_box = new ListBox();
				updateDataInterface(data_box, (RadioButton)event.getSource());
			}
		});
		rd1.setHeight("10px");
		rd1.setValue(true);
		options.addMember(rd1);

		//segundo boton: [otro]
		RadioButton rd2 = new RadioButton(Integer.toString(dataCol.size()/2),constants.radioOpt2());
		rd2.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				TextBox user_text = new TextBox();
				updateDataInterface(user_text, (RadioButton)event.getSource());
			}
		});
		rd2.setHeight("10px");
		options.setWidth("70px");
		options.addMember(rd2);
		
		return options;
	}
	
	private void updateDataInterface(Widget newWidget,RadioButton button){
		int position = Integer.parseInt(button.getName()); //numero de Data-Warranty en la layout cuya opcion es seleccionada

		//quitar el widget existente en la posicion previamente
		HLayout dataWar = (HLayout)dataLay.getMember(position);
		dataWar.removeMember(dataWar.getMember(dataPosition));
		dataCol.remove(position*2);
		
		//añadir nuevo widget
		newWidget.setWidth("225px");
		newWidget.setHeight("20px");
		dataWar.addMember(newWidget,dataPosition);
		dataCol.add(position*2,newWidget);
	}
	
	private void setPlayerNames() {		//cargamos la lista de players con los jugadores actuales
		for (GamePlayer p: players) {
			player_box.addItem(p.getName());
		}
	}
	
	private void loadArguments(){	//metodo llamado por el otro loadArguments() de la clase,
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
	
	public void loadArguments(Vector<String> arguments) {	//metodo activado por ClientGameManager al
		//conseguir onSucess() del llamado a Comet
		argsPlayerSel = arguments;
		loadArguments();
	}
	
	public boolean sendArgument(){			//conseguir los datos a enviar y pasarlos al ClientGameManager
		Vector<String> argument = getArgumentTxt();
		if (argument!=null){
			gameman.sendArgument(argument);
			return true;
		}
		else return false;
	}
	
	private Vector<String> getArgumentTxt(){	//transformar el contenido de la interfaz
												//en un vector de texto de las distintas partes
		Vector<String> argument = new Vector<String>();
		argument = getDataTexts();
		if (argument!=null){
			argument.add(0,getClaimText());
			return argument;
		}
		else return null;
	}
	
	private String getClaimText(){			//transformar la parte de Claim a texto
		String claim = new String(); 
		claim = yn_box.getValue(yn_box.getSelectedIndex())
				+ " "
				+ claim_text.getText()
				+ " "
				+ player_box.getValue(player_box.getSelectedIndex());
				
		return claim;
	}
	
	private Vector<String> getDataTexts(){		//por cada seccion Data/Warranty creada,
												//armar el texto correspondiente
		Vector<String> datas = new Vector<String>();
		
		String dataW = new String();
		boolean first_data = true;
		
		for (Widget w: dataCol){
			if (w instanceof TextArea){ //es warranty
				TextArea ta = (TextArea)w;
				if (!ta.getText().isEmpty()){
					dataW += " " + messages.warrantyText()
							+ " " + ta.getText();
				}
				datas.add(dataW);

				dataW = new String();
				first_data= false;
			}
			else{ //no es warranty, sino area. que puede ser dada en dos formas
				if (first_data){
					if (w instanceof ListBox){ //ya que dijo
						ListBox lb = (ListBox)w;
						if (lb.getSelectedIndex()<0){
							showAlert();
							return null;
						}
						dataW = " " + messages.causeText() + " " + constants.radioOpt1()
								+ " '" + lb.getValue(lb.getSelectedIndex()) + "'";
					}
					if (w instanceof TextBox){ //ya que otro
						TextBox tb = (TextBox)w;
						if (tb.getValue().isEmpty()){
							showAlert();
							return null;
						}
						dataW = " " + messages.causeText()
								+ " '" + tb.getValue() + "'";
					}
				}
				else
				{
					if (w instanceof ListBox){ //ya que dijo
						ListBox lb = (ListBox)w;
						if (lb.getSelectedIndex()<0){
							showAlert();
							return null;
						}
						dataW = " " + messages.extraCause() + " " + constants.radioOpt1()
								+ " '" + lb.getValue(lb.getSelectedIndex()) + "'";
					}
					if (w instanceof TextBox){ //ya que otro
						TextBox tb = (TextBox)w;
						if (tb.getValue().isEmpty()){
							showAlert();
							return null;
						}
						dataW = " " + messages.extraCause() +
								" '" + tb.getValue() + "'";
					}
				}	
			}
		}
		return datas;
	}
	
	private void showAlert(){
		AlertDialog alert = new AlertDialog(constants.argAlertTitle(),messages.argAlertText(),false,this);
		alert.show();
	}
	
	@Override
	public void afterExecution() {
		this.show();
	}
}
