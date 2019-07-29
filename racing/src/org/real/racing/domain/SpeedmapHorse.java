package org.real.racing.domain;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "horse")
@XmlAccessorType(XmlAccessType.FIELD)
public class SpeedmapHorse {
	
	@XmlAttribute
	private Integer number;
	@XmlAttribute
	private String name;
	@XmlAttribute
	private Integer barrier;
	@XmlAttribute(name = "effective-barrier")
	private Integer effectiveBarrier;
	@XmlAttribute
	private Integer forward;
	@XmlAttribute
	private Integer wide;
	@XmlAttribute(name = "forward-saved")
	private Integer savedForward;
	@XmlAttribute(name = "wide-saved")
	private Integer savedWide;
	@XmlAttribute
	private Boolean scratched;
	
	
	public SpeedmapHorse() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Integer getNumber() {
		return number;
	}
	public void setNumber(Integer number) {
		this.number = number;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getBarrier() {
		return barrier;
	}
	public void setBarrier(Integer barrier) {
		this.barrier = barrier;
	}
	public Integer getEffectiveBarrier() {
		return effectiveBarrier;
	}
	public void setEffectiveBarrier(Integer effectiveBarrier) {
		this.effectiveBarrier = effectiveBarrier;
	}
	public Integer getForward() {
		return forward;
	}
	public void setForward(Integer forward) {
		this.forward = forward;
	}
	public Integer getWide() {
		return wide;
	}
	public void setWide(Integer wide) {
		this.wide = wide;
	}
	public Integer getSavedForward() {
		return savedForward;
	}
	public void setSavedForward(Integer savedForward) {
		this.savedForward = savedForward;
	}
	public Integer getSavedWide() {
		return savedWide;
	}
	public void setSavedWide(Integer savedWide) {
		this.savedWide = savedWide;
	}
	public Boolean getScratched() {
		return scratched;
	}
	public void setScratched(Boolean scratched) {
		this.scratched = scratched;
	}
	@Override
	public String toString() {
		return "SpeedmapHorse [number=" + number + ", name=" + name + ", barrier=" + barrier + ", effectiveBarrier="
				+ effectiveBarrier + ", forward=" + forward + ", wide=" + wide + ", savedForward=" + savedForward
				+ ", savedWide=" + savedWide + ", scratched=" + scratched + "]\n";
	}

}
