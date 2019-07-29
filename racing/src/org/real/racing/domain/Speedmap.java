package org.real.racing.domain;

import java.util.ArrayList;
import java.util.Date;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.bson.types.ObjectId;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;

@Entity("speedmaps")
@XmlRootElement(name = "speedmaps")
@XmlAccessorType(XmlAccessType.FIELD)
public class Speedmap {
	
	@Id
	private ObjectId id;
	
	@XmlJavaTypeAdapter(DateAdapter.class)
	@XmlAttribute(name = "date")
	private Date date;
	
	@XmlAttribute(name = "track")
	private String track;
	
	@XmlElement(name = "race")
	private ArrayList<SpeedmapRace> races;
	
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getTrack() {
		return track;
	}
	public void setTrack(String track) {
		this.track = track;
	}
	public ArrayList<SpeedmapRace> getRaces() {
		return races;
	}
	public void setRaces(ArrayList<SpeedmapRace> races) {
		this.races = races;
	}
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	@Override
	public String toString() {
		return "Speedmap [date=" + date + ", track=" + track + ", races=" + races + "]\n";
	}



}
