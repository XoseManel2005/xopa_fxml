package ins.marianao.sailing.fxml.services;

import cat.institutmarianao.sailing.ws.model.Action;
import cat.institutmarianao.sailing.ws.model.Rescheduling;
import cat.institutmarianao.sailing.ws.model.Trip;
import cat.institutmarianao.sailing.ws.model.User;
import ins.marianao.sailing.fxml.manager.ResourceManager;

public class ServiceSaveAction extends ServiceSaveBase<Action>{

	private static final String PATH_UPDATE= "/save/action";

    public ServiceSaveAction(Action newAction) throws Exception {
    	
        super(newAction, Action.class, new String[]{
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
