package ins.marianao.sailing.fxml;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang3.exception.ExceptionUtils;

import cat.institutmarianao.sailing.ws.model.Action;
import cat.institutmarianao.sailing.ws.model.Trip;
import cat.institutmarianao.sailing.ws.model.TripType;
import cat.institutmarianao.sailing.ws.model.Trip.Status;
import cat.institutmarianao.sailing.ws.model.TripType.Category;
import cat.institutmarianao.sailing.ws.model.User;
import ins.marianao.sailing.fxml.exception.OnFailedEventHandler;
import ins.marianao.sailing.fxml.manager.ResourceManager;
import ins.marianao.sailing.fxml.services.ServiceQueryTrips;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class ControllerViewTripsClient implements Initializable {

    @FXML private ComboBox<Category> cmbCategory;
    @FXML private ComboBox<Status> cmbStatus;
    @FXML private DatePicker dpFrom;
    @FXML private DatePicker dpTo;
    @FXML private TableView<Trip> tripsTable;
    @FXML private TableColumn<Trip, Number> colIndex;
    @FXML private TableColumn<Trip, String> colCategory;
    @FXML private TableColumn<Trip, String> colTitle;
    @FXML private TableColumn<Trip, Integer> colMax;
    @FXML private TableColumn<Trip, Integer> colBooked;
    @FXML private TableColumn<Trip, String> colStatus;
    @FXML private TableColumn<Trip, String> colDate;
    @FXML private TableColumn<Trip, String> colDeparture;
    @FXML
	private TableColumn<Trip, String> colComents;
    @FXML private TableColumn<Trip, Boolean> colCancel;
    
    private ResourceBundle resource;
    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resource = resources;
        this.currentUser = ResourceManager.getInstance().getCurrentUser();
        
        // Inicializar ComboBox
        cmbCategory.setItems(FXCollections.observableArrayList(Category.values()));
        cmbStatus.setItems(FXCollections.observableArrayList(Status.values()));

        cmbCategory.valueProperty().addListener((obs, oldVal, newVal) -> reloadTrips());
        cmbStatus.valueProperty().addListener((obs, oldVal, newVal) -> reloadTrips());
        dpFrom.valueProperty().addListener((obs, oldVal, newVal) -> reloadTrips());
        dpTo.valueProperty().addListener((obs, oldVal, newVal) -> reloadTrips());
        
        ObservableList<TripType.Category> categoryList = FXCollections.observableArrayList(
            TripType.Category.GROUP, 
            TripType.Category.PRIVATE
        );

        TripType.Category allCategory = null; //all será null para no tneer filtros

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
                        .filter(cat -> resource.getString("text.Category." + cat.name()).equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });
        
        cmbCategory.valueProperty().addListener((observable, oldValue, newValue) -> {
            reloadTrips();
        });
        
        Status allStatus = null; // "ALL" será representado como null
        ObservableList<Status> statusListWithAll = FXCollections.observableArrayList();
        statusListWithAll.add(allStatus);
        statusListWithAll.addAll(FXCollections.observableArrayList(Trip.Status.values()));

        // Asignar la lista al ComboBox
        cmbStatus.setItems(statusListWithAll);

        // Configurar el CellFactory para mostrar los items en el dropdown
        cmbStatus.setCellFactory(new Callback<ListView<Status>, ListCell<Status>>() {
            @Override
            public ListCell<Status> call(ListView<Status> param) {
                return new ListCell<Status>() {
                    @Override
                    protected void updateItem(Status item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(resource.getString("text.Status.ALL")); // Mostrar "ALL" si es null
                        } else {
                            setText(resource.getString("text.Status." + item.name()));
                        }
                    }
                };
            }
        });

        // Configurar el StringConverter para manejar "ALL"
        cmbStatus.setConverter(new StringConverter<Status>() {
            @Override
            public String toString(Status status) {
                if (status == null) {
                    return resource.getString("text.Status.ALL"); // Mostrar "ALL" si es null
                }
                return resource.getString("text.Status." + status.name());
            }

            @Override
            public Status fromString(String string) {
                if (string.equals(resource.getString("text.Status.ALL"))) {
                    return null; // Representamos "ALL" como null
                }
                return Arrays.stream(Trip.Status.values())
                        .filter(st -> resource.getString("text.Status." + st.name()).equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });
        
        cmbStatus.valueProperty().addListener((observable, oldValue, newValue) -> {
            reloadTrips();
        });

        // Configurar las columnas de la tabla
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
        SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");

        colCategory.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getDeparture().getTripType().getCategory().name())
        );
        colTitle.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getDeparture().getTripType().getTitle())
        );
        colMax.setCellValueFactory(cellData ->
            new SimpleIntegerProperty(cellData.getValue().getDeparture().getTripType().getMaxPlaces()).asObject()
        );
        colBooked.setCellValueFactory(cellData ->
            new SimpleIntegerProperty(cellData.getValue().getDeparture().getBookedPlaces()).asObject()
        );
        colStatus.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getStatus().toString())
        );
        colDate.setCellValueFactory(cellData ->
            new SimpleStringProperty(sdf.format(cellData.getValue().getDeparture().getDate()))
        );
        colDeparture.setCellValueFactory(cellData ->
            new SimpleStringProperty(sdf2.format(cellData.getValue().getDeparture().getDeparture()))
        );
        colIndex.setCellValueFactory(cellData ->
            new SimpleLongProperty(tripsTable.getItems().indexOf(cellData.getValue()) + 1)
        );
        
        this.colComents.setMinWidth(100);
		this.colComents.setMaxWidth(Double.MAX_VALUE);
		this.colComents.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Trip, String>, ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(TableColumn.CellDataFeatures<Trip, String> trip) {
						List<Action> tripComent = trip.getValue().getTracking();
						for (Action tripComent1 : tripComent) {
							System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAction de trip" + tripComent1.getInfo());
							if (tripComent1.getInfo()!=null && !tripComent1.getInfo().trim().isEmpty()) {
								return new SimpleStringProperty(tripComent1.getInfo());
							}
						}
						return null;
					}
				});

        // Configurar columna Cancel (ya definida)
        this.colCancel.setMinWidth(50);
        this.colCancel.setMaxWidth(70);
        this.colCancel.setCellValueFactory(cell ->
            new SimpleBooleanProperty(false)
        );
        this.colCancel.setCellFactory(column -> new TableCell<Trip, Boolean>() {
            private final Button button = new Button();

            {
                ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("resources/cancel.png")));
                imageView.setFitWidth(16);
                imageView.setFitHeight(16);
                button.setGraphic(imageView);
                button.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

                button.setOnAction(event -> {
                    Trip trip = getTableView().getItems().get(getIndex());
                    
                            try {
                                ResourceManager.getInstance().getMenuController().openTripCancel(trip);
                            } catch (Exception e) {
                                ControllerMenu.showError(resource.getString("error.viewUsers.delete"), e.getMessage(), ExceptionUtils.getStackTrace(e));
                            }
                        
                 
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Trip trip = getTableRow().getItem();
                    if (trip.getStatus() == Status.DONE || trip.getStatus() == Status.CANCELLED) {
                        setGraphic(null);
                    } else {
                        setGraphic(button);
                    }
                }
            }
        });

        this.tripsTable.setEditable(true);
        this.tripsTable.getSelectionModel().setCellSelectionEnabled(true);

        // Finalmente, recargar los viajes
        reloadTrips();
    }

    private void reloadTrips() {
        Category category = cmbCategory.getValue();
        Status status = cmbStatus.getValue();
        LocalDate fromDate = dpFrom.getValue();
        LocalDate toDate = dpTo.getValue();

        Date fromDateConverted = fromDate != null ? Date.from(fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null;
        Date toDateConverted = toDate != null ? Date.from(toDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()) : null;

        final ServiceQueryTrips queryTrips = new ServiceQueryTrips(
                status != null ? new Status[]{status} : new Status[0],
                category != null ? new Category[]{category} : new Category[0],
                currentUser.getUsername(),
                fromDateConverted,
                toDateConverted
        );

        queryTrips.setOnSucceeded(event -> {
            tripsTable.getItems().clear();
            List<Trip> resultTrips = queryTrips.getValue();
            if (resultTrips != null) {
                tripsTable.setItems(FXCollections.observableArrayList(resultTrips));
            }
        });

        queryTrips.setOnFailed(new OnFailedEventHandler(ResourceManager.getInstance().getText("error.viewTrips.web.service")));
        queryTrips.start();
    }
}
