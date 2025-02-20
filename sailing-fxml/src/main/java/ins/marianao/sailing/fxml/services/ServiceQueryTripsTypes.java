package ins.marianao.sailing.fxml.services;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.MissingResourceException;
import java.util.stream.Collectors;

import cat.institutmarianao.sailing.ws.model.Trip;
import cat.institutmarianao.sailing.ws.model.Trip.Status;
import cat.institutmarianao.sailing.ws.model.TripType;
import cat.institutmarianao.sailing.ws.model.TripType.Category;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import ins.marianao.sailing.fxml.manager.ResourceManager;

/**
 * Servicio para consultar tipos de viajes con filtros específicos
 */
public class ServiceQueryTripsTypes extends ServiceQueryBase<TripType> {

    private Category[] categories;
    private Double priceFrom;
    private Double priceTo;
    private Integer placesFrom;
    private Integer placesTo;
    private Integer durationTo;
    private Integer durationFrom;

    private static final String PATH_QUERY_ALL = "/triptypes/find/all";

    public ServiceQueryTripsTypes(Category[] categories, Double priceFrom, Double priceTo, Integer placesFrom,
            Integer placesTo, Integer durationTo, Integer durationFrom) {
        super();
        this.categories = categories;
        this.priceFrom = priceFrom;
        this.priceTo = priceTo;
        this.placesFrom = placesFrom;
        this.placesTo = placesTo;
        this.durationTo = durationTo;
        this.durationFrom = durationFrom;
    }

    @Override
    protected List<TripType> customCall() throws Exception {
        Client client = ResourceManager.getInstance().getWebClient();
        WebTarget webTarget = client.target(ResourceManager.getInstance().getParam("web.service.host.url"))
                .path(PATH_QUERY_ALL);

        // Validar precio
        if (priceFrom != null && priceTo != null && priceFrom > priceTo) {
            throw new IllegalArgumentException("El precio desde no puede ser menor que el precio hasta");
        }

        // Validar plazas
        if (placesFrom != null && placesTo != null && placesFrom > placesTo) {
            throw new IllegalArgumentException("Las plazas desde no pueden ser menores que las plazas hasta");
        }

        // Validar duración
        if (durationFrom != null && durationTo != null && durationFrom > durationTo) {
            throw new IllegalArgumentException("La duración desde no puede ser menor que la duración hasta");
        }

        if (this.categories != null && this.categories.length > 0) {
            String categoriesStr = Arrays.stream(categories).map(Category::name).collect(Collectors.joining(","));
            webTarget = webTarget.queryParam("category", categoriesStr);
        }

        Invocation.Builder invocationBuilder = ResourceManager.getInstance().getAuthRequestBuilder(webTarget, true);

        try {
            Response response = invocationBuilder.get();
            if (response.getStatus() == 200) {
                return response.readEntity(new GenericType<List<TripType>>() {});
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