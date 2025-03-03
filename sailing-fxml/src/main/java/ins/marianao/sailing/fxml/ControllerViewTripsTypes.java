package ins.marianao.sailing.fxml;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cat.institutmarianao.sailing.ws.model.User;
import cat.institutmarianao.sailing.ws.model.Trip;
import cat.institutmarianao.sailing.ws.model.TripType;
import cat.institutmarianao.sailing.ws.model.Trip.Status;
import cat.institutmarianao.sailing.ws.model.TripType.Category;
import cat.institutmarianao.sailing.ws.model.User.Role;
import ins.marianao.sailing.fxml.exception.OnFailedEventHandler;
import ins.marianao.sailing.fxml.manager.ResourceManager;
import ins.marianao.sailing.fxml.services.ServiceQueryTripsTypes;
import ins.marianao.sailing.fxml.utils.Formatters;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.Pair;
import javafx.util.StringConverter;

public class ControllerViewTripsTypes implements Initializable {
	@FXML
	private ComboBox<Category> cmbCategory;
	@FXML
	private TextField tfDurationFrom;
	@FXML
	private TextField tfDurationTo;
	@FXML
	private TextField tfPlacesFrom;
	@FXML
	private TextField tfPlacesTo;
	@FXML
	private TextField tfPriceFrom;
	@FXML
	private TextField tfPriceTo;
	@FXML
	private BorderPane viewTripsDirectory;
	@FXML
	private HBox HboxTripsTypes;

	private ResourceBundle resource;
	private GridPane gridPane;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.resource = resources;

		gridPane = new GridPane();
		gridPane.setHgap(10);
		gridPane.setVgap(10);
		gridPane.setPadding(new Insets(10));

		for (int i = 0; i < 4; i++) {
		    ColumnConstraints cc = new ColumnConstraints();
		    cc.setPercentWidth(25.0);
		    cc.setFillWidth(true);
		    cc.setHgrow(Priority.ALWAYS);
		    gridPane.getColumnConstraints().add(cc);
		}

		HboxTripsTypes.getChildren().clear();
		HboxTripsTypes.getChildren().add(gridPane);

		this.tfDurationFrom.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				reloadTripsTypes();
			}
		});

		this.tfDurationTo.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				reloadTripsTypes();
			}
		});

		this.tfPlacesFrom.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				reloadTripsTypes();
			}
		});

		this.tfPlacesTo.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				reloadTripsTypes();
			}
		});

		this.tfPriceFrom.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				reloadTripsTypes();
			}
		});

		this.tfPriceTo.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				reloadTripsTypes();
			}
		});

		ObservableList<TripType.Category> categoryList = FXCollections.observableArrayList(TripType.Category.GROUP,
				TripType.Category.PRIVATE);

		TripType.Category allCategory = null;

		ObservableList<TripType.Category> categoryListWithAll = FXCollections.observableArrayList();
		categoryListWithAll.add(allCategory);
		categoryListWithAll.addAll(categoryList);

		cmbCategory.setItems(categoryListWithAll);
		
		cmbCategory.setCellFactory(new Callback<ListView<TripType.Category>, ListCell<TripType.Category>>() {
			@Override
			public ListCell<TripType.Category> call(ListView<TripType.Category> param) {
				return new ListCell<TripType.Category>() {
					@Override
					protected void updateItem(TripType.Category item, boolean empty) {
						super.updateItem(item, empty);
						if (empty || item == null) {
							setText(resource.getString("text.Category.ALL"));
						} else {
							setText(resource.getString("text.Category." + item.name()));
						}
					}
				};
			}
		});

		cmbCategory.setConverter(new StringConverter<TripType.Category>() {
			@Override
			public String toString(TripType.Category category) {
				if (category == null) {
					return resource.getString("text.Category.ALL"); 
				}
				return resource.getString("text.Category." + category.name());
			}

			@Override
			public TripType.Category fromString(String string) {
				if (string.equals(resource.getString("text.Category.ALL"))) {
				}
				return categoryList.stream()
						.filter(cat -> resource.getString("text.Category." + cat.name()).equals(string)).findFirst()
						.orElse(null);
			}
		});


		cmbCategory.valueProperty().addListener((observable, oldValue, newValue) -> {
			reloadTripsTypes();
		});

		reloadTripsTypes();
	}

	private void reloadTripsTypes() {
		Category category = cmbCategory.getValue();

		Double priceFrom = validarCampoNumerico(tfPriceFrom.getText(), 0.0);
		Double priceTo = validarCampoNumerico(tfPriceTo.getText(), Double.MAX_VALUE);
		Integer placesFrom = validarCampoEntero(tfPlacesFrom.getText(), 0);
		Integer placesTo = validarCampoEntero(tfPlacesTo.getText(), Integer.MAX_VALUE);
		Integer durationFrom = validarCampoEntero(tfDurationFrom.getText(), 0);
		Integer durationTo = validarCampoEntero(tfDurationTo.getText(), Integer.MAX_VALUE);
		
		Category[] categoryArray = category != null ? new Category[] { category } : null;

		// Crear el servicio de consulta de viajes
		final ServiceQueryTripsTypes queryTripsTypes = new ServiceQueryTripsTypes(categoryArray, priceFrom, priceTo,
				placesFrom, placesTo, durationTo, durationFrom);
		queryTripsTypes.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent t) {
				ObservableList<TripType> tripTypes = FXCollections.observableArrayList(queryTripsTypes.getValue());
				gridPane.getChildren().clear();
				int rowIndex = 0;
				for (TripType tripType : tripTypes) {
					VBox vBoxContainer = createListViewForType(tripType);
					vBoxContainer.setMaxWidth(Double.MAX_VALUE);
					vBoxContainer.setFillWidth(true);
					int columnIndex = rowIndex % 4;
					int row = rowIndex / 4;
					gridPane.add(vBoxContainer, columnIndex, row);
					GridPane.setHgrow(vBoxContainer, Priority.ALWAYS);

					rowIndex++;
				}
			}
		});

		queryTripsTypes.setOnFailed(
				new OnFailedEventHandler(ResourceManager.getInstance().getText("error.viewTripTypes.web.service")));

		// Iniciar la consulta
		queryTripsTypes.start();
	}

	private VBox createListViewForType(TripType tripType) {
	    ListView<String> listView = new ListView<>();
	    ObservableList<String> tripDetails = FXCollections.observableArrayList();
	    tripDetails.add(tripType.getCategory().toString());
	    tripDetails.add(tripType.getTitle());
	    tripDetails.add("Max Places: " + tripType.getMaxPlaces());
	    tripDetails.add("Price: " + tripType.getPrice());
	    tripDetails.add("Duration: " + tripType.getDuration());
	    tripDetails.add("Departures \n ----------------------------- \n " + tripType.getDepartures());
	    listView.setItems(tripDetails);

	    listView.setMinHeight(200); 
	    listView.setPrefHeight(tripDetails.size() * 24);

	    Label label = new Label(tripType.getTitle());

	    VBox container = new VBox(label, listView);
	    container.setSpacing(10);
	    container.setStyle("-fx-border-color: grey; -fx-border-width: 2; -fx-padding: 10; -fx-border-radius: 10");
	    container.setAlignment(Pos.CENTER);

	    if (ResourceManager.getInstance().getCurrentUser() != null) {
	        Button moreInfoButton = new Button("RESERVE");
	        moreInfoButton.setOnAction(e -> {
	            ResourceManager.getInstance().getMenuController().openReserveTrip(tripType);
	        });
	        container.getChildren().add(moreInfoButton);
	    }

	    return container;
	}


	private Double validarCampoNumerico(String texto, double valorPorDefecto) {
		try {
			return texto == null || texto.trim().isEmpty() ? valorPorDefecto : Double.parseDouble(texto);
		} catch (NumberFormatException e) {
			return valorPorDefecto;
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