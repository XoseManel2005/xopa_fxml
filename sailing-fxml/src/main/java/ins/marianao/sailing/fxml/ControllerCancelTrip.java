package ins.marianao.sailing.fxml;

import java.awt.Window.Type;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang3.exception.ExceptionUtils;

import cat.institutmarianao.sailing.ws.model.Action;
import cat.institutmarianao.sailing.ws.model.Client;
import cat.institutmarianao.sailing.ws.model.Departure;
import cat.institutmarianao.sailing.ws.model.Rescheduling;
import cat.institutmarianao.sailing.ws.model.Trip;
import cat.institutmarianao.sailing.ws.model.TripType;
import cat.institutmarianao.sailing.ws.model.User;
import ch.qos.logback.core.joran.conditional.IfAction;
import cat.institutmarianao.sailing.ws.model.TripType.Category;
import ins.marianao.sailing.fxml.manager.ResourceManager;
import ins.marianao.sailing.fxml.services.ServiceSaveTrip;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Pair;

public class ControllerCancelTrip {

	@FXML
	private Button btnCancel;

	@FXML
	private Button btnSubmit;

	@FXML
	private TextField tfReason;

	@FXML
	private BorderPane viewTripsDirectory;

	Trip trip = null;

	public void loadTrip(Trip trip) {
		this.trip = trip;
		System.out.println("Trip recibido correctamente" + trip);
	}

	@FXML
	void cancel(ActionEvent event) {

		boolean result = ControllerMenu.showConfirm(
				ResourceManager.getInstance().getText("fxml.text.viewTrips.cancel.title"),
				ResourceManager.getInstance().getText("fxml.text.viewTrips.cancel.text"));
		if (result) {
			if (result) {
				((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
			}
		}
	}

	@FXML
	void submit(ActionEvent event) {
	    String reason = this.tfReason.getText().trim();
	    
	   
	    
	    // Crear Action con todos los campos obligatorios
	    Action newAction = Rescheduling.builder()
	        .type(Action.Type.valueOf(Action.CANCELLATION))
	        .idTrip(trip.getId())
	        .date(new Date())
	        .reason(reason)
	        .performer(ResourceManager.getInstance().getCurrentUser())
	        .trip(trip)
	        .build();
	    
	    ServiceSaveTrip addAction;
	    try {
	        addAction = new ServiceSaveTrip(newAction);
	        
	        // Manejar el éxito
	        addAction.setOnSucceeded(e -> {
	            ControllerMenu.showInfo("Canceled", "El trip se ha cancelado.");
	            ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
	        });
	        
	        // Manejar el fallo
	        addAction.setOnFailed(e -> {
	            Throwable exception = addAction.getException();
	            System.out.println(newAction);
	            System.out.println(addAction.toString());

	            String errorMessage = (exception != null) ? exception.getMessage() : "Error desconocido";
	            ControllerMenu.showError("Error en el registro", errorMessage, "");
	        });
	        
	        // Iniciar el servicio
	        addAction.start();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

}
