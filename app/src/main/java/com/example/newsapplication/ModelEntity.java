package es.upm.hcid.pui.assignment;

import java.util.Enumeration;
import java.util.Hashtable;

import org.json.simple.JSONObject;

import es.upm.hcid.pui.assignment.exceptions.ServerCommunicationError;

public abstract class ModelEntity {
	protected int id;
	protected ModelManager mm;
	
	/**
	 * 
	 * @param mm
	 */
	public ModelEntity(ModelManager mm){
		this.mm=mm;
	}
	
	/**
	 * 
	 * @return the object id (-1) if not saved
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * 
	 * @return hashtable of attributes of the entity (without id)
	 */
	protected abstract Hashtable<String,String> getAttributes();
	
	/**
	 * save the entity in remote server
	 * @throws ServerCommunicationError
	 */
	public void save() throws ServerCommunicationError{
		int id = mm.save(this);
		this.id = id;
	}
	
	/**
	 * delete the entity from remote server 
	 * @throws ServerCommunicationError
	 */
	public void delete() throws ServerCommunicationError{
		mm.delete(this);
	}
	
	/**
	 * 
	 * @return json object of the entity
	 */
	@SuppressWarnings("unchecked")
	public JSONObject toJSON(){
		JSONObject jsonArticle = new JSONObject();
		if(getId()>0)
			jsonArticle.put("id", getId());
	
		Hashtable<String,String> res = getAttributes();
		Enumeration<String> keys = res.keys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			jsonArticle.put(key, JSONObject.escape(res.get((key))));
		}
		return jsonArticle;
	}
}
