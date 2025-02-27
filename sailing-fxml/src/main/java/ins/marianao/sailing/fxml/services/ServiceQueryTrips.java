package ins.marianao.sailing.fxml.services;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.MissingResourceException;
import java.util.stream.Collectors;

import cat.institutmarianao.sailing.ws.model.Trip;
import cat.institutmarianao.sailing.ws.model.Trip.Status;
import cat.institutmarianao.sailing.ws.model.TripType.Category;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import ins.marianao.sailing.fxml.manager.ResourceManager;

public class ServiceQueryTrips extends ServiceQueryBase<Trip> {

    private Status[] status;
    private Category[] categories;
    private String clientName;
    private Date from;
    private Date to;

    public static final String PATH_QUERY_ALL = "/trips/find/all"; // Asegúrate de que coincida con el servidor
    public static final String PATH_QUERY_UPDATE = "trips"; // Asegúrate de que coincida con el servidor

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ServiceQueryTrips(Status[] status, Category[] categories, String clientName, Date from, Date to) {
        super();
        this.status = status;
        this.categories = categories;
        this.clientName = clientName;
        this.from = from;
        this.to = to;
    }

    @Override
    protected List<Trip> customCall() throws Exception {
        Client client = ResourceManager.getInstance().getWebClient();
        WebTarget webTarget = client.target(ResourceManager.getInstance().getParam("web.service.host.url"))
                .path(PATH_QUERY_ALL);

        // Validar fechas
        if (from != null && to != null && from.after(to)) {
            throw new IllegalArgumentException("La fecha 'from' no puede ser posterior a 'to'");
        }

        // Parámetros como listas separadas por comas
        if (this.status != null && this.status.length > 0) {
            String statuses = Arrays.stream(status)
                    .map(Status::name)
                    .collect(Collectors.joining(","));
            webTarget = webTarget.queryParam("status", statuses);
        }

        if (this.categories != null && this.categories.length > 0) {
            String categoriesStr = Arrays.stream(categories)
                    .map(Category::name)
                    .collect(Collectors.joining(","));
            webTarget = webTarget.queryParam("category", categoriesStr);
        }
        
        if (this.clientName != null && !this.clientName.isBlank()) webTarget = webTarget.queryParam("client", clientName);

        if (this.from != null) {
            Date From = this.from;
            webTarget = webTarget.queryParam("from", 
                new SimpleDateFormat("yyyy-MM-dd").format(From));
        }
        if (this.to != null) {
            Date To = this.to;
            webTarget = webTarget.queryParam("to",
                new SimpleDateFormat("yyyy-MM-dd").format(To));
        }

        Invocation.Builder invocationBuilder = ResourceManager.getInstance().getAuthRequestBuilder(webTarget, true);

        try {
            Response response = invocationBuilder.get();
            if (response.getStatus() == 200) {
                return response.readEntity(new GenericType<List<Trip>>() {});
            } else {
                String errorDetails = response.readEntity(String.class);
                throw new Exception("Error " + response.getStatus() + ": " + errorDetails);
            }
        } catch (ProcessingException e) {
            handleException(e, "error.service.processing");
        } catch (Exception e) {
            handleException(e, "error.general");
        }
        return Collections.emptyList();
    }

    private void handleException(Exception e, String errorMessageKey) throws Exception {
        String errorMessage;
        try {
            errorMessage = ResourceManager.getInstance().getText(errorMessageKey);
        } catch (MissingResourceException mre) {
            errorMessage = "Error no especificado";
        }
        throw new Exception(errorMessage + " - Detalles: " + e.getMessage(), e);
    }
}