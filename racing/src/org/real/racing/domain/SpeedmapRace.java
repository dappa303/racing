package org.real.racing.domain;

import java.util.ArrayList;
import javax.xml.bind.annotation.*;

@XmlRootElement(name = "race")
@XmlAccessorType(XmlAccessType.FIELD)
public class SpeedmapRace {
	
	@XmlAttribute
	private Integer number;
	@XmlAttribute
	private Integer distance;
	@XmlAttribute
	private String pace;
	@XmlElement(name = "horse")
	private ArrayList<SpeedmapHorse> horses;
	
	
	public SpeedmapRace() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Integer getNumber() {
		return number;
	}
	public void setNumber(Integer number) {
		this.number = number;
	}
	public Integer getDistance() {
		return distance;
	}
	public void setDistance(Integer distance) {
		this.distance = distance;
	}
	public String getPace() {
		return pace;
	}
	public void setPace(String pace) {
		this.pace = pace;
	}
	public ArrayList<SpeedmapHorse> getHorses() {
		return horses;
	}
	public void setHorses(ArrayList<SpeedmapHorse> horses) {
		this.horses = horses;
	}
	@Override
	public String toString() {
		return "SpeedmapRace [number=" + number + ", distance=" + distance + ", pace=" + pace + ", horses=" + horses
				+ "]\n";
	}

}
