package it.prova.gestionesatelliti.exceptions;

public class AlreadyLaunchedSatelliteException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AlreadyLaunchedSatelliteException(String m) {
		super(m);
	}
}
