package ins.marianao.sailing.fxml.services;

import cat.institutmarianao.sailing.ws.model.Trip;
import cat.institutmarianao.sailing.ws.model.User;
import ins.marianao.sailing.fxml.manager.ResourceManager;

public class ServiceSaveTrip extends ServiceSaveBase<Trip>{

	private static final String PATH_UPDATE= "/save";

    public ServiceSaveTrip(Trip newTrip) throws Exception {
    	
        super(newTrip, Trip.class, new String[]{
        		ServiceQueryTrips.PATH_QUERY_UPDATE, PATH_UPDATE
        		}, Method.POST, shouldRequireAuth());
    }
    
    private static boolean shouldRequireAuth() {
        User currentUser = ResourceManager.getInstance().getCurrentUser();

        if (currentUser == null) {
            return false;
        }

        if (currentUser.getRole() == User.Role.ADMIN || currentUser.getRole() == User.Role.CLIENT) {
            return true;
        }

        return false;
    }
}