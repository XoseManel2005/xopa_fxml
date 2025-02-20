package ins.marianao.sailing.fxml.services;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.ResponseProcessingException;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import ins.marianao.sailing.fxml.manager.ResourceManager;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public abstract class ServiceSaveBase<T> extends Service<T>{

	public static final String POST = "POST"; //add
	public static final String PUT = "PUT"; //update

	public enum Method {
		POST, PUT
	}
	protected T entity;
	protected Class<T> entityType;
	protected String[] path;
	protected Method method;
	protected boolean auth;
	
	public ServiceSaveBase(T entity, Class<T> entityType, String[] path, Method method, boolean auth) throws Exception {
		if (entity == null) throw new Exception(ResourceManager.getInstance().getText("error.service.entity.null"));
		if (path == null || path.length == 0)  throw new Exception(ResourceManager.getInstance().getText("error.service.path.null")); 
		this.entity = entity;
		this.entityType = entityType;
		this.path = path;
		this.method = method;
		this.auth = auth;
	}

	@Override
	protected Task<T> createTask() {
		
		return new Task<T>() {
			@Override
			protected T call() throws Exception {
				Client client = ResourceManager.getInstance().getWebClient();

				WebTarget webTarget = client.target(ResourceManager.getInstance().getParam("web.service.host.url"));
				
				for (String part : path) {
					webTarget = webTarget.path(part);
				}
				
				Invocation.Builder invocationBuilder = auth?ResourceManager.getInstance().getAuthRequestBuilder(webTarget, true):webTarget.request(MediaType.APPLICATION_JSON);

				try {
					Response response = null;
					
					if (Method.POST.equals(method)) response = invocationBuilder.post(Entity.entity(entity, MediaType.APPLICATION_JSON)); 
					else  response = invocationBuilder.put(Entity.entity(entity, MediaType.APPLICATION_JSON)); 

					ResourceManager.getInstance().checkResponseErrors(response);

					//entity = response.readEntity(T.class);
					entity = (T) response.readEntity(entityType);
					
				} catch (ResponseProcessingException e) {
					e.printStackTrace();
					throw new Exception(ResourceManager.getInstance().getText("error.service.response.processing")+" "+e.getMessage());
				} catch (ProcessingException e) {
					e.printStackTrace();
					throw new Exception(ResourceManager.getInstance().getText("error.service.processing")+" "+e.getMessage());
				} catch (Exception e) {
					e.printStackTrace();
					throw e;
				}

				return entity;
			}
		};
	}
	
}
