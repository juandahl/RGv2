package edu.isistan.rolegame.client.graph;

import java.util.Vector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
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
	private RolegameMessages messages = GWT.create(RolegameMessages.class);
	private RolegameConstants constants = GWT.create(RolegameConstants.class);
	private static final int dataPosition=2; //comienza en 0 la num. en layout
	
	private ListBox yn_box; // yes or no option
	private TextBox claim_text; 
	private ListBox player_box; // list of players to vote or acuse
	private TextBox reservation;
	private ListBox qualifiers;
	
	private VLayout dataLayout;
	private Vector<Widget> dataCol; //vector para poder luego mas facil pasar los datos a String
	private Vector<TextArea> warranties;
	private Vector<TextBox> backings;
	private Vector<RadioButton> optionButtons;
	
	private Vector<String> argsPlayerSel;   //arguments loaded for the player selected, set by ClientGameManager
											//stored like this, so we dont have to ask the server for every new data part that is added

	public ArgumentDialog(ClientGameManager gamemanager, Vector<GamePlayer> players) {
		// set GENERAL ui
		this.gameman = gamemanager;
		this.players = players;
		setTitle(constants.argumentTitle());
		setSize("1010px", "300px");
		addCloseClickHandler(new CloseClickHandler() {
			public void onCloseClick(CloseClientEvent event) {
				destroy();
			}
		});
		VLayout allContent = new VLayout();
		allContent.setWidth("910px");
		allContent.setMargin(15);
		dataCol = new Vector<Widget>(); //dataCollection. Los widgets involucrados en la seccion Data/Warranty
		warranties = new Vector<TextArea>();
		backings = new Vector<TextBox>();
		optionButtons = new Vector<RadioButton>();
		argsPlayerSel = null;			

		/**
		 * set CLAIM ui
		 */
		HLayout claim = new HLayout();
		claim.setWidth("270px");
		
//		qualifier = new CheckBox(constants.qualifierLabel());
//		qualifier.setStyleName("labelDisabled");
//		qualifier.addClickHandler(new ClickHandler() {			
//			@Override
//			public void onClick(ClickEvent event) {
//				if (qualifier.getValue())
//					qualifier.setStyleName("labelEnabled");
//				else qualifier.setStyleName("labelDisabled");	
//			}
//		});
//		claim.addMember(qualifierBox);
		
		qualifiers = new ListBox();
		qualifiers.addItem("");
		qualifiers.addItem(constants.qualifier1());
		qualifiers.addItem(constants.qualifier2());
		qualifiers.addItem(constants.qualifier3());
		claim.addMember(qualifiers);

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
		
		Label reservLabel = new Label(constants.reservLabel());
		reservLabel.setStyleName("textLabel");
		claim.addMember(reservLabel);
		
		reservation = new TextBox();
		reservation.setWidth("250px");
		claim.addMember(reservation);

		allContent.addMember(claim);

		// set DATA/WARRANY section
		dataLayout = new VLayout(); // data-warranty layout for multiple items
		dataLayout.setHeight("100px");
		dataLayout.addMember(getDataInterface());
		dataLayout.setStyleName("dataLayout");

		HLayout addLay = new HLayout();
		PushButton addButton = new PushButton();
		addButton.setText(constants.plus());
		addButton.setWidth("10px");
		addButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				dataLayout.addMember(getDataInterface(), (dataCol.size()-1));
			}
		});
		addLay.addMember(addButton);

		Label addMsg = new Label(constants.addMsg());
		addMsg.setStyleName("textLabel");
		addLay.addMember(addMsg);
		addLay.setMargin(7);
		dataLayout.addMember(addLay);

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
		dataLayout.addMember(sendButton);
		allContent.addMember(dataLayout);

		gameman.loadArguments(player_box.getValue(player_box.getSelectedIndex())); //debe hacerse aca por que sino no hay ningun ListBox disponible
		   																   //cuando se cargaron los nombres de los jugadores																		
		addItem(allContent);
	}

	private HLayout getDataInterface() {	//cada vez que se toca el boton de Añadir, se crea una nueva interfaz de Data/Warranty
		HLayout ui = new HLayout();
		ui.setWidth("300px");
		ui.setMargin(7);

		//texto conectivo entre claim y data. "ya que" 
		Label causeTxt = new Label();
		if (dataCol.size()==0)
			causeTxt.setText(constants.causeText());
		else
			causeTxt.setText(constants.extraCause());
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
		Label henceTxt = new Label(constants.warrantyText());
		henceTxt.setWidth("65px");
		henceTxt.setStyleName("argLabel");
		ui.addMember(henceTxt);
		
		//espacio para poner Warranty
		TextArea warranty = new TextArea();
		warranty.setWidth("225px");
		warranty.setHeight("40px");
		warranty.setStyleName("warrantyTxtArea");
		ui.addMember(warranty);
		warranties.add(warranty);
		
		//texto conectivo entre warranty y backing
		Label backTxt = new Label(constants.warrantyLabel());
		backTxt.setWidth("60px");
		backTxt.setStyleName("textLabel");
		ui.addMember(backTxt);
		
		//espacio para Backing
		TextBox backing = new TextBox();
		backing.setWidth("225px");
		backing.setHeight("20px");
		ui.addMember(backing);
		backings.add(backing);
		
		//boton de borrado del elemento agregado
		if (dataCol.size()>1){
			Button delete = new Button();
			delete.setText(constants.minus());
			delete.setStyleName(Integer.toString(dataCol.size()));	//para en un futuro saber que numero de boton esta siendo accionado
			delete.setWidth("10px");
			delete.addClickHandler(new ClickHandler() {	
				@Override
				public void onClick(ClickEvent event) {
					Button source = (Button)event.getSource();
					deleteData(Integer.parseInt(source.getStyleName()));
				}
			});
			ui.addMember(delete);
		}

		//llamado para popular de información el listado de argumentos del player elegido
		loadArguments();

		return ui;
	}

	private VLayout getOptionsInterface(){		//interfaz para seleccionar si se pone 
												//el listado de argumentos previos o un texto a escribir por el usuario
		VLayout options = new VLayout();
		//primer botton: dijo
		RadioButton rd1 = new RadioButton(Integer.toString(dataCol.size()),constants.radioOpt1());
		rd1.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				ListBox data_box = new ListBox();
				updateDataInterface(data_box, (RadioButton)event.getSource());
			}
		});
		rd1.setHeight("10px");
		rd1.setValue(true);
		optionButtons.addElement(rd1);
		options.addMember(rd1);

		//segundo boton: [otro]
		RadioButton rd2 = new RadioButton(Integer.toString(dataCol.size()),constants.radioOpt2());
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
		optionButtons.addElement(rd2);
		
		return options;
	}
	
	private void updateDataInterface(Widget newWidget,RadioButton button){		//cambiar la lista de argumentos previos por
																				//la linea de texto o viceversa
		
		int position = Integer.parseInt(button.getName()); //numero de Data-Warranty en la layout cuya opcion es seleccionada
		//quitar el widget existente en la posicion previamente
		HLayout dataWar = (HLayout)dataLayout.getMember(position);
		dataWar.removeMember(dataWar.getMember(dataPosition));
		dataCol.remove(position);
		
		//añadir nuevo widget
		newWidget.setWidth("225px");
		newWidget.setHeight("20px");
		dataWar.addMember(newWidget,dataPosition);
		dataCol.add(position,newWidget);
		
		loadArguments();
	}
	
	private void deleteData(int dataNumber){
		int index = dataNumber-1; //dado que los vectores comienzan en posicion 0
									//y el numero que yo paso es el tamaño del vector, el cual comienza en 1
		
		optionButtons.remove(index);
		optionButtons.remove(index+1);
		dataCol.remove(index);
		backings.remove(index);
		warranties.remove(index);
		
		for(int i=index*2;i<optionButtons.size();i++){
			RadioButton button = optionButtons.elementAt(i);
			button.setName(
					Integer.toString(Integer.parseInt(button.getName())-1));
		}
		
		HLayout data = (HLayout)dataLayout.getMember(index);
		dataLayout.removeChild(data);
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
			String claim = getClaimText();
			if (claim!=null){
				argument.add(0,getClaimText());
				return argument;
			}
			else return null;
		}
		else return null;
	}
	
	private String getClaimText(){			//transformar la parte de Claim a texto
		if (claim_text.getText().isEmpty()){
			showAlert();
			return null;
		}else{
			String claim = new String(); 
			//if (qualifierBox.getValue())
				claim = qualifiers.getItemText(qualifiers.getSelectedIndex());
			claim += " " + yn_box.getValue(yn_box.getSelectedIndex())
					+ " " + claim_text.getText()
					+ " " + player_box.getValue(player_box.getSelectedIndex());
			if (!reservation.getText().isEmpty())
				claim += " " + constants.reservLabel() + " " + reservation.getText();
			return claim;
		}
	}
	
	private Vector<String> getDataTexts(){		//por cada seccion Data/Warranty creada,
												//armar el texto correspondiente
		Vector<String> datas = new Vector<String>();
		
		String dataW = new String();
		boolean first_data = true;
		
		if (dataCol.size()==warranties.size()
				&& warranties.size()==backings.size()){		//control de que se agrego todo correctamente y funciona bien
			for (int i=0; i<dataCol.size(); i++){		//recorro los dos vectores : data y warranties, en paralelo
				//armar string del dato
				Widget w = dataCol.elementAt(i);
				if (first_data)
					dataW = " " + constants.causeText();
				else dataW = " " + constants.extraCause();
				if (w instanceof ListBox){
					ListBox lb = (ListBox)w;
					if (lb.getSelectedIndex()<0){
						showAlert();
						return null;
					}
					else dataW += " " + constants.radioOpt1() +
								" '" + lb.getValue(lb.getSelectedIndex()) + "'";
				} else{
					if (w instanceof TextBox){
						TextBox tb = (TextBox)w;
						if (tb.getValue().isEmpty()){
							showAlert();
							return null;
						} else dataW += " " + tb.getValue();
					}
				}
				//armar string de warranty
				String warrant = warranties.elementAt(i).getText();
				if (!warrant.isEmpty()){
					dataW += " " + constants.warrantyText()
							+ " " + warrant;
					//armar string de backing, solo si se incluyo una warranty
					String backing = backings.elementAt(i).getText();
					if (!backing.isEmpty())
						dataW += " " + constants.warrantyLabel() + " " + backing;
				}
				
				datas.add(dataW);
				dataW = new String();
				first_data = false;
			}
		}
		return datas;
	}
	
	private void showAlert(){	//mostrar dialogo de alerta cuando falta completar Dato
		AlertDialog alert = new AlertDialog(messages.argAlertTitle(),messages.argAlertText(),false,this);
		alert.show();
	}
	
	@Override
	public void afterExecution() {		//metodo ejecutado luego de mostrar una alerta
		this.show();
	}
}
