package ins.marianao.sailing.fxml;

import java.awt.Window.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import ins.marianao.sailing.fxml.services.ServiceSaveAction;
import ins.marianao.sailing.fxml.services.ServiceSaveTrip;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Pair;

public class ControllerReserveTrip {

	@FXML
	private Button btnCancel;

	@FXML
	private Button btnSubmit;

	@FXML
	private ComboBox<String> cbDeparture;
	
	@FXML
	private DatePicker dpDate;

	@FXML
	private TextField tfPlaces;

	@FXML
	private BorderPane viewTripsDirectory;

	TripType trip = null;

	public void loadTripType(TripType trip) {
		this.trip = trip;
		System.out.println("Trip recibido correctamente" + trip);

		// Configuración del cmbStatus
		ObservableList<String> departures = FXCollections.observableArrayList();
		String departuresString = trip.getDepartures();
		if (departuresString != null && !departuresString.isEmpty()) {
			String[] departuresArray = departuresString.split(";");
			departures.addAll(Arrays.asList(departuresArray));
			cbDeparture.setItems(departures);
		}

	}

	@FXML
	void cancel(ActionEvent event) {

		boolean result = ControllerMenu.showConfirm(
				ResourceManager.getInstance().getText("fxml.text.viewUsers.delete.title"),
				ResourceManager.getInstance().getText("fxml.text.viewUsers.delete.text"));
		if (result) {
			if (result) {
				((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
			}
		}
	}

	Date departureTime = null;
	@FXML
	void confirm(ActionEvent event) {
		Integer places = validarCampoEntero(tfPlaces.getText(), Integer.MAX_VALUE);
		String departure = this.cbDeparture.getValue();
		LocalDate dateLocal = dpDate.getValue();

		// Validaciones existentes
		if (dateLocal == null) {
		    ControllerMenu.showError("Error", "La fecha no puede estar vacia.");
		    return;
		} else if (dateLocal.isBefore(LocalDate.now())) {
		    ControllerMenu.showError("Error", "La fecha no puede ser anterior a la fecha actual");
		    return;
		}

		// Formatear la fecha 
		Date formatedDate = Date.from(dateLocal.atStartOfDay(ZoneId.systemDefault()).toInstant());

		Date departureDate = null;
		if (departure != null && !departure.isEmpty()) {
		    if (departure.length() > 2 && Character.isDigit(departure.charAt(0))) {
		        int indexOfColon = departure.indexOf(':');
		        if (indexOfColon == 1) {
		            departure = "0" + departure;
		        }
		    }
		    LocalTime time = LocalTime.parse(departure);
		    LocalDateTime departureDateTime = LocalDateTime.of(dateLocal, time);
		    departureDate = Date.from(departureDateTime.atZone(ZoneId.systemDefault()).toInstant());
		}
		Departure newDeparture;
		if (departureDate==null) {
			newDeparture = Departure.builder().tripType(trip).date(formatedDate).departure(formatedDate).build();
		}else {
			newDeparture = Departure.builder().tripType(trip).date(formatedDate).departure(departureDate).build();
		}
		
		
		Trip newTrip = Trip.builder().client((Client) ResourceManager.getInstance().getCurrentUser()).departure(newDeparture).places(places).build();

		

		ServiceSaveTrip addTrip;
		try {
			addTrip = new ServiceSaveTrip(newTrip);
			addTrip.setOnSucceeded(e -> {
	            ControllerMenu.showInfo("Reserved", "trip has been reserved.");
	            ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
	        });
	        
			addTrip.setOnFailed(e -> {
	            Throwable exception = addTrip.getException();
	            String errorMessage = (exception != null) ? exception.getMessage() : "Error desconocido";
	            ControllerMenu.showError("Error en el registro", errorMessage, "");
	        });
			addTrip.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private Integer validarCampoEntero(String texto, int valorPorDefecto) {
		try {
			return texto == null || texto.trim().isEmpty() ? valorPorDefecto : Integer.parseInt(texto);
		} catch (NumberFormatException e) {
			return valorPorDefecto;
		}
	}

}
