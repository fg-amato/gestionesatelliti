package it.prova.gestionesatelliti.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.criteria.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.prova.gestionesatelliti.exceptions.AlreadyLaunchedSatelliteException;
import it.prova.gestionesatelliti.exceptions.IllegalSatelliteStateException;
import it.prova.gestionesatelliti.model.Satellite;
import it.prova.gestionesatelliti.model.StatoSatellite;
import it.prova.gestionesatelliti.repository.SatelliteRepository;

@Service
public class SatelliteServiceImpl implements SatelliteService {

	@Autowired
	private SatelliteRepository repository;

	@Override
	@Transactional(readOnly = true)
	public List<Satellite> listAllElements() {
		return (List<Satellite>) repository.findAll();

	}

	@Override
	@Transactional(readOnly = true)
	public Satellite caricaSingoloElemento(Long id) {
		return repository.findById(id).orElse(null);
	}

	@Override
	@Transactional
	public void aggiorna(Satellite satelliteInstance) {
		repository.save(satelliteInstance);
	}

	@Override
	@Transactional
	public void inserisciNuovo(Satellite satelliteInstance) {
		repository.save(satelliteInstance);
	}

	@Override
	@Transactional
	public void rimuovi(Satellite satelliteInstance) {

		if (satelliteInstance.getDataLancio() == null) {
			repository.deleteById(satelliteInstance.getId());
			return;
		}

		if (satelliteInstance.getStato() == StatoSatellite.DISATTIVATO
				&& satelliteInstance.getDataRientro().before(new Date())) {
			repository.deleteById(satelliteInstance.getId());
			return;
		}

		throw new IllegalSatelliteStateException("Non è consentito eliminare il satellite..");
	}

	@Override
	@Transactional(readOnly = true)
	public List<Satellite> findByExample(Satellite example) {
		Specification<Satellite> specificationCriteria = (root, query, cb) -> {

			List<Predicate> predicates = new ArrayList<Predicate>();

			if (StringUtils.isNotEmpty(example.getCodice()))
				predicates.add(cb.like(cb.upper(root.get("codice")), "%" + example.getCodice().toUpperCase() + "%"));

			if (StringUtils.isNotEmpty(example.getDenominazione()))
				predicates.add(cb.like(cb.upper(root.get("denominazione")), "%" + example.getCodice().toUpperCase() + "%"));

			

			if (example.getStato() != null)
				predicates.add(cb.equal(root.get("stato"), example.getStato()));

			if (example.getDataLancio() != null)
				predicates.add(cb.greaterThanOrEqualTo(root.get("dataLancio"), example.getDataLancio()));
			
			if (example.getDataRientro() != null)
				predicates.add(cb.greaterThanOrEqualTo(root.get("dataRientro"), example.getDataRientro()));

			return cb.and(predicates.toArray(new Predicate[predicates.size()]));
		};

		return repository.findAll(specificationCriteria);
	}

	@Override
	@Transactional
	public void rimuoviById(Long id) {
		repository.deleteById(id);
	}

	@Override
	@Transactional
	public void lancia(Long id) {
		Satellite toLaunch = this.caricaSingoloElemento(id);
		if (toLaunch == null) {
			throw new RuntimeException("Input non valido");
		}

		if (toLaunch.getDataLancio() != null) {
			throw new AlreadyLaunchedSatelliteException("Il satellite ha già una data di lancio");
		}
		toLaunch.setDataLancio(new Date());
		toLaunch.setStato(StatoSatellite.IN_MOVIMENTO);

		this.aggiorna(toLaunch);
	}

	@Override
	@Transactional
	public void rientra(Long id) {
		Satellite toRientr = this.caricaSingoloElemento(id);
		if (toRientr == null) {
			throw new RuntimeException("Input non valido");
		}

		if (toRientr.getDataRientro() != null) {
			throw new AlreadyLaunchedSatelliteException("Il satellite ha già una data di rientro");
		}
		toRientr.setDataRientro(new Date());
		toRientr.setStato(StatoSatellite.DISATTIVATO);

		this.aggiorna(toRientr);
	}

}
