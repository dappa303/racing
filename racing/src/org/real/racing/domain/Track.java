package org.real.racing.domain;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import org.bson.types.ObjectId;

@Entity("tracks")
public class Track {
	
	@Id
    private ObjectId id;
	private String name;
    private String rcom;
    private String rnet;
    private String code;
    private String state;
    private boolean full;
	
    public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getRcom() {
		return rcom;
	}
	public void setRcom(String rcom) {
		this.rcom = rcom;
	}
	public String getRnet() {
		return rnet;
	}
	public void setRnet(String rnet) {
		this.rnet = rnet;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public boolean isFull() {
		return full;
	}
	public void setFull(boolean full) {
		this.full = full;
	}

    

}
