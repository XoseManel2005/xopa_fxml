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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.Pair;
import javafx.util.StringConverter;

public class ControllerViewTripsTypes implements Initializable{

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
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Guardar el ResourceBundle en una variable de clase
        this.resource = resources;
        
     // Configuración del cmbStatus
        ObservableList<Category> categoryList = FXCollections.observableArrayList(TripType.Category.values());
        
        // Configurar el CellFactory para mostrar los items en el dropdown
        cmbCategory.setCellFactory((Callback<ListView<Category>, ListCell<Category>>) new Callback<ListView<Category>, ListCell<Category>>() {
            @Override
            public ListCell<Category> call(ListView<Category> param) {
                return new ListCell<Category>() {
                	@Override
                	protected void updateItem(Category item, boolean empty) {
                	    super.updateItem(item, empty);
                	    if (empty) {
                	        setText(resource.getString("text.Category.ALL"));
                	    } else {
                	        setText(resource.getString("text.Category." + item.name()));
                	    }
                	}
                };
            }
        });
        
        // Configurar el StringConverter para mostrar el item seleccionado
        cmbCategory.setConverter(new StringConverter<Category>() {
            @Override
            public String toString(Category category) {
            	if (category==null) {
					return resource.getString("text.Category.ALL");
				}
                return category == null ? null : 
                    resource.getString("text.Category." + category.name());
            }

            @Override
            public Category fromString(String string) {
                return null;
            }
        });

        
        // Establecer los items del ComboBox
        cmbCategory.setItems(categoryList);
        
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
	    final ServiceQueryTripsTypes queryTripsTypes = new ServiceQueryTripsTypes(
	        categoryArray, priceFrom, priceTo, placesFrom, placesTo, durationTo, durationFrom
	    );
	    
	    HboxTripsTypes.setSpacing(10);
	    
	    // Configurar el manejador de éxito de la consulta
	    queryTripsTypes.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
	        @Override
	        public void handle(WorkerStateEvent t) {
	            ObservableList<TripType> tripTypes = FXCollections.observableArrayList(queryTripsTypes.getValue());
	            HboxTripsTypes.getChildren().clear();
	                    
	            for (TripType trypType : tripTypes) {
	                ListView<TripType> listView = createListViewForType(trypType);
	                ObservableList<TripType> filteredItems = FXCollections.observableArrayList(
	                        tripTypes.filtered(item -> item.getTitle().equals(trypType.getTitle()))
	                );
	                listView.setItems(filteredItems);
	                HboxTripsTypes.getChildren().add(listView);
	            }
	        }
	    });
	    
	    // Configurar el manejador de fallo de la consulta
	    queryTripsTypes.setOnFailed(new OnFailedEventHandler(
	        ResourceManager.getInstance().getText("error.viewTripTypes.web.service")
	    ));
	    
	    // Iniciar la consulta
	    queryTripsTypes.start();
	}
	// Método auxiliar para crear ListViews con configuración estándar
	private ListView<TripType> createListViewForType(TripType tripType) {
	    ListView<TripType> listView = new ListView<>();
	    listView.setPrefHeight(300); // Altura predeterminada
	    listView.setPrefWidth(200); // Ancho predeterminada
	    
	    // Agregar título al ListView
	    Label label = new Label("Viajes de tipo: " + tripType.getTitle());
	    VBox container = new VBox(label, listView);
	    container.setAlignment(Pos.CENTER);
	    return listView;
	}

	// Método auxiliar para validar campos numéricos con valor por defecto
	private Double validarCampoNumerico(String texto, double valorPorDefecto) {
	    try {
	        return texto == null || texto.trim().isEmpty() ? valorPorDefecto : Double.parseDouble(texto);
	    } catch (NumberFormatException e) {
	        return valorPorDefecto;
	    }
	}

	// Método auxiliar para validar campos enteros con valor por defecto
	private Integer validarCampoEntero(String texto, int valorPorDefecto) {
	    try {
	        return texto == null || texto.trim().isEmpty() ? valorPorDefecto : Integer.parseInt(texto);
	    } catch (NumberFormatException e) {
	        return valorPorDefecto;
	    }
	}
}
