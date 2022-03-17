package it.prova.gestionesatelliti.exceptions;

public class IllegalSatelliteStateException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public IllegalSatelliteStateException(String m) {
		super(m);
	}
}
