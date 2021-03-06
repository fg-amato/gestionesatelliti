package it.prova.gestionesatelliti.web.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.prova.gestionesatelliti.exceptions.AlreadyLaunchedSatelliteException;
import it.prova.gestionesatelliti.exceptions.IllegalSatelliteStateException;
import it.prova.gestionesatelliti.model.Satellite;
import it.prova.gestionesatelliti.service.SatelliteService;

@Controller
@RequestMapping(value = "/satellite")
public class SatelliteController {

	@Autowired
	private SatelliteService satelliteService;

	@GetMapping
	public ModelAndView listAll() {
		ModelAndView mv = new ModelAndView();
		List<Satellite> results = satelliteService.listAllElements();
		mv.addObject("satellite_list_attribute", results);
		mv.setViewName("satellite/list");
		return mv;
	}

	@GetMapping("/search")
	public String search() {
		return "satellite/search";
	}

	@PostMapping("/list")
	public String listByExample(Satellite example, ModelMap model) {
		List<Satellite> results = satelliteService.findByExample(example);
		model.addAttribute("satellite_list_attribute", results);
		return "satellite/list";
	}

	@GetMapping("/listDisattivatiMaInOrbita")
	public String listDisattivatiMaInOrbita(ModelMap model) {
		List<Satellite> results = satelliteService.findAllDisattivatiMaNonRientrati();
		model.addAttribute("satellite_list_attribute", results);
		return "satellite/list";
	}
	
	@GetMapping("/listFissiDaDieciAnni")
	public String listFissiDaDieciAnni(ModelMap model) {
		List<Satellite> results = satelliteService.findAllFissiPerAlmenoDieciAnni();
		model.addAttribute("satellite_list_attribute", results);
		return "satellite/list";
	}
	
	@GetMapping("/listLanciatiDaDueAnni")
	public String listLanciatiDaDueAnni(ModelMap model) {
		List<Satellite> results = satelliteService.findAllLanciatiDaAlmenoDueAnni();
		model.addAttribute("satellite_list_attribute", results);
		return "satellite/list";
	}

	@GetMapping("/insert")
	public String create(Model model) {
		model.addAttribute("insert_satellite_attr", new Satellite());
		return "satellite/insert";
	}

	@PostMapping("/save")
	public String save(@Valid @ModelAttribute("insert_satellite_attr") Satellite satellite, BindingResult result,
			RedirectAttributes redirectAttrs) {

		if (satellite.getDataLancio() != null && satellite.getDataRientro() != null
				&& satellite.getDataLancio().after(satellite.getDataRientro())) {
			result.rejectValue("dataLancio", "dataLancio.dataRientro.rangeInvalid");
			result.rejectValue("dataRientro", "dataLancio.dataRientro.rangeInvalid");
		}

		if (result.hasErrors())
			return "satellite/insert";

		satelliteService.inserisciNuovo(satellite);

		redirectAttrs.addFlashAttribute("successMessage", "Operazione eseguita correttamente");
		return "redirect:/satellite";
	}

	@GetMapping("/delete/{idSatellite}")
	public String delete(@PathVariable(required = true) Long idSatellite, Model model) {
		Satellite toDelete = satelliteService.caricaSingoloElemento(idSatellite);
		model.addAttribute("delete_satellite_attr", toDelete);
		return "satellite/delete";
	}

	@PostMapping("/remove")
	public String remove(@RequestParam(required = true) Long idSatellite, RedirectAttributes redirectAttrs) {

		try {
			Satellite toRemove = satelliteService.caricaSingoloElemento(idSatellite);
			satelliteService.rimuovi(toRemove);
		} catch (IllegalSatelliteStateException e) {
			redirectAttrs.addFlashAttribute("errorMessage", "Impossibile eliminare il satellite!");
			//redirectAttrs.addFlashAttribute("list_satellite_attr", satelliteService.listAllElements());
			return "redirect:/satellite";
		}

		redirectAttrs.addFlashAttribute("successMessage", "Operazione eseguita correttamente");
		return "redirect:/satellite";
	}

	@GetMapping("/show/{idSatellite}")
	public String show(@PathVariable(required = true) Long idSatellite, Model model) {
		model.addAttribute("show_satellite_attr", satelliteService.caricaSingoloElemento(idSatellite));
		return "satellite/show";
	}

	@GetMapping("/edit/{idSatellite}")
	public String edit(@PathVariable(required = true) Long idSatellite, Model model) {
		Satellite toUpdate = satelliteService.caricaSingoloElemento(idSatellite);
		model.addAttribute("update_satellite_attr", toUpdate);
		return "satellite/edit";
	}

	@PostMapping("/update")
	public String update(@Valid @ModelAttribute("update_satellite_attr") Satellite satellite, BindingResult result,
			RedirectAttributes redirectAttrs) {

		if (satellite.getDataLancio() != null && satellite.getDataRientro() != null
				&& satellite.getDataLancio().after(satellite.getDataRientro())) {
			result.rejectValue("dataLancio", "dataLancio.dataRientro.rangeInvalid");
			result.rejectValue("dataRientro", "dataLancio.dataRientro.rangeInvalid");
		}

		if (result.hasErrors())
			return "satellite/edit";

		satelliteService.aggiorna(satellite);

		redirectAttrs.addFlashAttribute("successMessage", "Operazione eseguita correttamente");
		return "redirect:/satellite";
	}

	@PostMapping("/lancia")
	public String lancia(@RequestParam(required = true) Long idSatellite, RedirectAttributes redirectAttrs) {

		try {
			satelliteService.lancia(idSatellite);
		} catch (AlreadyLaunchedSatelliteException e) {
			redirectAttrs.addFlashAttribute("errorMessage", "Impossibile eseguire il lancio del satellite!");
			redirectAttrs.addFlashAttribute("list_satellite_attr", satelliteService.listAllElements());
			return "redirect:/satellite";
		} catch (Exception e) {
			redirectAttrs.addFlashAttribute("errorMessage", "Qualcosa ?? andato storto!");
			//redirectAttrs.addFlashAttribute("list_satellite_attr", satelliteService.listAllElements());
			return "redirect:/satellite";
		}

		redirectAttrs.addFlashAttribute("successMessage", "Operazione eseguita correttamente");
		return "redirect:/satellite";
	}

	@PostMapping("/rientra")
	public String rientra(@RequestParam(required = true) Long idSatellite, RedirectAttributes redirectAttrs) {

		try {
			satelliteService.rientra(idSatellite);
		} catch (AlreadyLaunchedSatelliteException e) {
			redirectAttrs.addFlashAttribute("errorMessage", "Impossibile eseguire il rientro del satellite!");
			redirectAttrs.addFlashAttribute("list_satellite_attr", satelliteService.listAllElements());
			return "redirect:/satellite";
		} catch (Exception e) {
			redirectAttrs.addFlashAttribute("errorMessage", "Qualcosa ?? andato storto!");
			//redirectAttrs.addFlashAttribute("list_satellite_attr", satelliteService.listAllElements());
			return "redirect:/satellite";
		}

		redirectAttrs.addFlashAttribute("successMessage", "Operazione eseguita correttamente");
		return "redirect:/satellite";
	}
}
