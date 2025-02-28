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
	private GridPane gridPane; // Variable para mantener el gridPane

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Guardar el ResourceBundle en una variable de clase
		this.resource = resources;

		// Crear el gridPane una sola vez
		gridPane = new GridPane();
		gridPane.setHgap(10);
		gridPane.setVgap(10);
		gridPane.setPadding(new Insets(10));

		// Configurar las columnas para que tengan el mismo ancho
		ColumnConstraints columnConstraints = new ColumnConstraints();
		columnConstraints.setPercentWidth(25.0);
		columnConstraints.setFillWidth(true);
		columnConstraints.setHgrow(Priority.ALWAYS);

		// Agregar las columnas al grid
		for (int i = 0; i < 4; i++) {
			gridPane.getColumnConstraints().add(columnConstraints);
		}

		// Reemplazar el HBox existente con el GridPane
		HboxTripsTypes.getChildren().clear();
		HboxTripsTypes.getChildren().add(gridPane);

		// Configuración de los listeners para los campos numéricos
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

		TripType.Category allCategory = null; // all será null para no tneer filtros

		// Crear la lista con ALL al inicio
		ObservableList<TripType.Category> categoryListWithAll = FXCollections.observableArrayList();
		categoryListWithAll.add(allCategory);
		categoryListWithAll.addAll(categoryList);

		// Asignar la lista al ComboBox
		cmbCategory.setItems(categoryListWithAll);
		
		// Configurar la visualización en el ComboBox
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

		// Configurar el StringConverter para manejar "ALL"
		cmbCategory.setConverter(new StringConverter<TripType.Category>() {
			@Override
			public String toString(TripType.Category category) {
				if (category == null) {
					return resource.getString("text.Category.ALL"); // Mostrar "ALL" si es null
				}
				return resource.getString("text.Category." + category.name());
			}

			@Override
			public TripType.Category fromString(String string) {
				if (string.equals(resource.getString("text.Category.ALL"))) {
					return null; // Representamos "ALL" como null
				}
				return categoryList.stream()
						.filter(cat -> resource.getString("text.Category." + cat.name()).equals(string)).findFirst()
						.orElse(null);
			}
		});


		// Agregar listener para recargar los viajes cuando cambia el estado
		cmbCategory.valueProperty().addListener((observable, oldValue, newValue) -> {
			reloadTripsTypes();
		});

		reloadTripsTypes();
	}

	private void reloadTripsTypes() {
		// Obtener los valores seleccionados de los ComboBox y DatePicker
		Category category = cmbCategory.getValue();

		// Validar campos numéricos con valores por defecto
		Double priceFrom = validarCampoNumerico(tfPriceFrom.getText(), 0.0);
		Double priceTo = validarCampoNumerico(tfPriceTo.getText(), Double.MAX_VALUE);
		Integer placesFrom = validarCampoEntero(tfPlacesFrom.getText(), 0);
		Integer placesTo = validarCampoEntero(tfPlacesFrom.getText(), Integer.MAX_VALUE);
		Integer durationFrom = validarCampoEntero(tfDurationFrom.getText(), 0);
		Integer durationTo = validarCampoEntero(tfDurationFrom.getText(), Integer.MAX_VALUE);

		// Convertir el estado y la categoría a arrays si es necesario
		Category[] categoryArray = category != null ? new Category[] { category } : null;

		// Crear el servicio de consulta de viajes con los parámetros de búsqueda
		final ServiceQueryTripsTypes queryTripsTypes = new ServiceQueryTripsTypes(categoryArray, priceFrom, priceTo,
				placesFrom, placesTo, durationTo, durationFrom);

		// Configurar el manejador de éxito de la consulta
		queryTripsTypes.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent t) {
				ObservableList<TripType> tripTypes = FXCollections.observableArrayList(queryTripsTypes.getValue());

				// Limpiar el gridPane existente
				gridPane.getChildren().clear();

				int rowIndex = 0;

				for (TripType tripType : tripTypes) {
					VBox vBoxContainer = createListViewForType(tripType);

					// Configurar el contenedor para que se adapte al espacio disponible
					vBoxContainer.setMaxWidth(Double.MAX_VALUE);
					vBoxContainer.setFillWidth(true);

					// Calcular la columna (0-3)
					int columnIndex = rowIndex % 4;

					// Calcular la fila (cada 4 elementos)
					int row = rowIndex / 4;

					// Agregar el contenedor al grid
					gridPane.add(vBoxContainer, columnIndex, row);

					// Configurar el crecimiento horizontal
					GridPane.setHgrow(vBoxContainer, Priority.ALWAYS);

					rowIndex++;
				}
			}
		});

		// Configurar el manejador de fallo de la consulta
		queryTripsTypes.setOnFailed(
				new OnFailedEventHandler(ResourceManager.getInstance().getText("error.viewTripTypes.web.service")));

		// Iniciar la consulta
		queryTripsTypes.start();
	}

	private VBox createListViewForType(TripType tripType) {
		ListView<String> listView = new ListView<>();

		// Agregar elementos a la lista con detalles del TripType
		ObservableList<String> tripDetails = FXCollections.observableArrayList();
		tripDetails.add(tripType.getCategory().toString());
		tripDetails.add(tripType.getTitle());
		tripDetails.add("Max Places: " + tripType.getMaxPlaces());
		tripDetails.add("Price: " + tripType.getPrice());
		tripDetails.add("Duration: " + tripType.getDuration());
		tripDetails.add("Departures \n ----------------------------- \n " + tripType.getDepartures());

		listView.setItems(tripDetails);

		Label label = new Label(tripType.getTitle());

		VBox container = new VBox(label, listView);
		container.setStyle("-fx-border-color: grey; -fx-border-width: 2; -fx-padding: 10; -fx-border-radius: 10");
		container.setAlignment(Pos.CENTER);

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