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

public class ControllerReschedulingTrip {

	@FXML
	private Button btnCancel;

	@FXML
	private Button btnSubmit;

	@FXML
	private ComboBox<String> cbDeparture;

	@FXML
	private DatePicker dpDate;

	@FXML
	private TextField tfReason;

	@FXML
	private BorderPane viewTripsDirectory;

	Trip trip = null;

	public void loadTrip(Trip trip) {
		this.trip = trip;
		System.out.println("Trip recibido correctamente" + trip);
		System.out.println(trip.getDeparture().getTripType().getDepartures());
		System.out.println(trip.getDeparture().getTripType());

		// Configuración del cmbStatus
		ObservableList<String> departures = FXCollections.observableArrayList();
		String departuresString = trip.getDeparture().getTripType().getDepartures();
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
	void submit(ActionEvent event) {
		String reason = this.tfReason.getText();
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

		// Formatear fecha exactamente como espera el servidor
		Date formatedDate = Date.from(dateLocal.atStartOfDay(ZoneId.systemDefault()).toInstant());

		if (departure != null && !departure.isEmpty()) {
			if (departure.length() > 2 && Character.isDigit(departure.charAt(0))) {
				int indexOfColon = departure.indexOf(':');
				if (indexOfColon == 1) {
					departure = "0" + departure;
				}
			}
			LocalTime time = LocalTime.parse(departure);
			departureTime = java.sql.Time.valueOf(time);
		}

		Action newAction = Rescheduling.builder().type(Action.Type.valueOf(Action.RESCHEDULING)).idTrip(trip.getId())
				.reason(reason).date(new Date()) // Fecha actual
				.oldDate(trip.getDeparture().getDate()) // Fecha original
				.oldDeparture(trip.getDeparture().getDeparture()) // Hora original
				.performer(ResourceManager.getInstance().getCurrentUser())
				.trip(trip)
				.newDate(formatedDate)
				.newDeparture(departureTime) // Nueva hora formateada
				.build();
		

		ServiceSaveTrip addAction;
		try {
			addAction = new ServiceSaveTrip(newAction);
			addAction.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
