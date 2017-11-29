package fr.sictiam.stela.acteservice.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
public class StampPosition {

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	private String uuid;
	private int x;
	private int y;

	public StampPosition() {
	}

	public StampPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public String getUuid() {
		return uuid;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
}
