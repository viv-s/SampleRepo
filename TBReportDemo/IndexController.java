package org.fstrf.actg.TBOutcomeReport.controllers;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.commons.io.IOUtils;
import org.fstrf.actg.TBOutcomeReport.dao.ReportDao;
import org.fstrf.actg.TBOutcomeReport.model.ReportInputForm;
import org.fstrf.actg.TBOutcomeReport.reporting.ReportUtil;
import org.fstrf.actg.TBOutcomeReport.service.ReportService;
import org.fstrf.actg.TBOutcomeReport.util.Constants;
import org.fstrf.actg.TBOutcomeReport.util.LogUtil;
import org.fstrf.actg.TBOutcomeReport.util.TBPatid;
import org.fstrf.actg.TBOutcomeReport.validation.ReportInputValidator;
import org.fstrf.common.crystal.CrystalReport;
import org.fstrf.security.util.SecurityContextWebAppUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class IndexController {

	private static final Logger log = LogUtil.getLogger(IndexController.class);

    @Autowired
    private ReportDao reportDao;

    @Autowired
    private ReportUtil reportUtil;

    @Autowired
    private ReportInputValidator validator;

    @InitBinder("reportInput")
    private void initBinder(WebDataBinder binder) {
    	binder.registerCustomEditor( String.class, new StringTrimmerEditor( true ));
        binder.setValidator(validator);
    }


    @RequestMapping(value= "/index", method=RequestMethod.GET)
    public ModelAndView getIndexPage(HttpServletRequest request, HttpSession session, ModelMap model) {
        log.info("Application accessed by user - {}", SecurityContextWebAppUtil.getUsername());
        model.addAttribute("reportInput", new ReportInputForm());
        session.setAttribute("projectName", SecurityContextWebAppUtil.getProjectName());

        try {
        	 if(!validator.isValidUser(SecurityContextWebAppUtil.getUsername())) {
             	model.addAttribute("invalidUser", true);
             	return new ModelAndView("errors", model);
             }

             List<Integer> patids = reportDao.getPatids();
             model.addAttribute("patids", patids);

             List<Integer> institutions = reportDao.getInstitutions();
             model.addAttribute("institutions", institutions);

             Map<Integer, String> dispositions = reportDao.getDispositions();
             model.addAttribute("dispositions", dispositions);

             Map<Integer, String> medHistory = reportDao.getMedHistory();
             model.addAttribute("medHistory", medHistory);

             Map<Integer, String> adverseEvents = reportDao.getAdverseEvents();
             model.addAttribute("adverseEvents", adverseEvents);

             Map<String, String> concomMeds = reportDao.getConcomMeds();
             model.addAttribute("concomMeds", concomMeds);

             Map<String, String> tbTreatmentType = reportDao.getConcomMedsTB();
             model.addAttribute("tbTreatment", tbTreatmentType);

             return new ModelAndView("index");

        } catch (Exception e) {
        	log.error("Error while processing index page.", e);
        	model.addAttribute("reportError", "Error occured while loading page.");
        	return new ModelAndView("errors");
        }
    }

    @RequestMapping(value="/getReport", params="generateReport", method=RequestMethod.POST)
    public ModelAndView getReport(@Valid @ModelAttribute("reportInput") ReportInputForm reportInput,
    		BindingResult result,
    		ModelMap model,
    		HttpServletResponse response) {

        if(result.hasErrors()) {
            return new ModelAndView("errors", model);
        }
        else {
            model.addAttribute("patid", reportInput.getPatid());
            model.addAttribute("institution", reportInput.getInstitution());
            model.addAttribute("tbEventFromDate", reportInput.getTbEventFromDate());
            model.addAttribute("tbEventToDate", reportInput.getTbEventToDate());
            model.addAttribute("disposition", reportInput.getDisposition());
            model.addAttribute("patientType", reportInput.getPatientType());
            model.addAttribute("studyStatus", reportInput.getStudyStatus());
            model.addAttribute("deadPids", reportInput.getDeadPids());
            model.addAttribute("ageRange", reportInput.getAgeRange());
            model.addAttribute("patidDisplay", reportInput.getPatidDisplay());
            model.addAttribute("medHistory", reportInput.getMedHistory());
            model.addAttribute("adverseEvent", reportInput.getAdverseEvent());
            model.addAttribute("concomMeds", reportInput.getConcomMeds());

            return new ModelAndView("displayReport", model);
        }
    }

    @RequestMapping(value="/getReport", params="generateWorksheet", method=RequestMethod.POST)
    public ModelAndView generateWorksheet(@Valid @ModelAttribute("reportInput") ReportInputForm reportInput,
    		BindingResult result,
    		ModelMap model,
    		HttpServletResponse response) {
    	if(result.hasErrors()) {
            return new ModelAndView("errors", model);
        } else {
        	model.addAttribute("patid", reportInput.getPatid());
            model.addAttribute("institution", reportInput.getInstitution());
            model.addAttribute("tbEventFromDate", reportInput.getTbEventFromDate());
            model.addAttribute("tbEventToDate", reportInput.getTbEventToDate());
            model.addAttribute("disposition", reportInput.getDisposition());
            model.addAttribute("patientType", reportInput.getPatientType());
            model.addAttribute("studyStatus", reportInput.getStudyStatus());
            model.addAttribute("deadPids", reportInput.getDeadPids());
            model.addAttribute("ageRange", reportInput.getAgeRange());
            model.addAttribute("patidDisplay", reportInput.getPatidDisplay());
            model.addAttribute("medHistory", reportInput.getMedHistory());
            model.addAttribute("adverseEvent", reportInput.getAdverseEvent());
            model.addAttribute("concomMeds", reportInput.getConcomMeds());

            return new ModelAndView("displayWorksheet", model);
        }
    }

    @RequestMapping(value="/displayReport", method=RequestMethod.GET)
    public ModelAndView displayReport( @RequestParam("study") String study,
    		@RequestParam("patidList") String patidList,
            @RequestParam("institutionList") String institutionList,
            @RequestParam("tbEventFromDate") String tbEventFromDate,
            @RequestParam("tbEventToDate") String tbEventToDate,
            @RequestParam("disposition") String disposition,
            @RequestParam("patientType") int patientType,
            @RequestParam("studyStatus") int studyStatus,
            @RequestParam("deadPids") int deadPids,
            @RequestParam("ageRange") String ageRange,
            @RequestParam("patidDisplay") int patidDisplay,
            @RequestParam("medHistory") int medHistory,
            @RequestParam("adverseEvent") int adverseEvent,
            @RequestParam("concomMeds") String concomMeds,
            HttpServletResponse response) {

        Map<String, Object> parameters = new HashMap<String, Object>();
        InputStream stream = null;
        ServletOutputStream out = null;
        String report = "TBOutcomeReport.rpt";
        ModelAndView modelAndView = new ModelAndView("errors");

        Random randomGenerator = new Random();
        int randomNum = randomGenerator.nextInt(9999) + 1;
		
        // The code values for the various filter criteria are set in index.jsp
        log.info("Setting input criteria to report");
        parameters.put("username", SecurityContextWebAppUtil.getUsername());
        parameters.put("patidList", patidList);
        parameters.put("institutionList", institutionList);
        parameters.put("age", ageRange);

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
        String currentDate = sdf.format(new Date());

        String tbEventStartDate = (tbEventFromDate.equals("")) ? Constants.DEFAULT_START_DATE : tbEventFromDate;
        String tbEventEndDate = (tbEventToDate.equals("")) ? currentDate : tbEventToDate;
        parameters.put("startDate", tbEventStartDate);
        parameters.put("endDate", tbEventEndDate);

    /* if(tbEventFromDate.equals("")) {
        	parameters.put("startDate", Constants.DEFAULT_START_DATE);
        } else {
        	parameters.put("startDate", tbEventFromDate);
        }
        if(tbEventToDate.equals("")) {
        	parameters.put("endDate", currentDate);
        } else {
        	parameters.put("endDate", tbEventToDate);
        }
        */
        // get patids based on TB event dates. if dates are not specified then report will run without limiting patids on TB event dates.
        try {
        	// random number is inserted into table and acts like session id
        	reportDao.populateTBEventPatids(tbEventStartDate, tbEventEndDate, randomNum);
        } catch (Exception dae) {
    		modelAndView.addObject("reportError",
                    "Error occured while generating the report.");
            return modelAndView;
    	}

        parameters.put("pidType", patientType);
        parameters.put("studyStatus", studyStatus);
        parameters.put("dispos", disposition);
        // get disposition description
        if(disposition.equals("-2")) {
        	parameters.put("disposDescr", "Blank Disposition");
        } else {
        	// TODO: Get value if DM needs the input value on the report. Set to temp value for now.
        	parameters.put("disposDescr", "Test");
        }
        parameters.put("mhHLTCode", medHistory);
        if(medHistory == -1) {
        	parameters.put("medHistoryDescr", "All events");
        } else {
        	parameters.put("medHistoryDescr", reportDao.getHLTDescr(medHistory));
        }
        parameters.put("aeCode", adverseEvent);
        if(adverseEvent == -1) {
        	parameters.put("adverseEventDescr", "All events");
        } else {
        	parameters.put("adverseEventDescr", reportDao.getHLTDescr(adverseEvent));
        }
        parameters.put("concomMed", concomMeds);
        if(concomMeds.equals("")) {
        	parameters.put("concomMedsDescr", "All");
        } else {
        	parameters.put("concomMedsDescr", concomMeds);
        }
        parameters.put("deadPids", deadPids);
        parameters.put("patidDisplay", patidDisplay);
        parameters.put("sessionid", randomNum);

        CrystalReport crystalReport = null;
        try {
        	log.info("Calling Crystal lib - "+sdfTest.format(Calendar.getInstance().getTime()));
            crystalReport = reportUtil.setupReport(report, parameters);
            log.info("After Crystal lib - "+sdfTest.format(Calendar.getInstance().getTime()));
            stream = crystalReport.exportToInputStream(CrystalReport.PDF);
            log.info("After  exportToInputStream - "+sdfTest.format(Calendar.getInstance().getTime()));
            reportDao.clearTBEventData(randomNum);
            response.setContentType("application/pdf");
            response.addHeader("content-disposition", "inline; filename=TBOutcomeReport.pdf");
            out = response.getOutputStream();
            IOUtils.copy(stream, out);
            log.info("After IOUtils copy - "+sdfTest.format(Calendar.getInstance().getTime()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            modelAndView.addObject("reportError",
                    "Error occured while generating the report.");
            return modelAndView;
        }
        finally {
            ReportService.tryClose(stream);
            ReportService.tryFlush(out);
            ReportService.tryClose(out);
            ReportUtil.tryClose(crystalReport);
        }
        return null;
    }

    @RequestMapping(value="/displayWorksheet", method=RequestMethod.GET)
    public ModelAndView displayWorksheet( HttpServletRequest request, @RequestParam("study") String study,
    		@RequestParam("patid") String patid,
            @RequestParam("institution") String institution,
            @RequestParam("tbEventFromDate") String tbEventFromDate,
            @RequestParam("tbEventToDate") String tbEventToDate,
            @RequestParam("disposition") int disposition,
            @RequestParam("patientType") int patientType,
            @RequestParam("studyStatus") int studyStatus,
            @RequestParam("deadPids") int deadPids,
            @RequestParam("ageRange") String ageRange,
            @RequestParam("patidDisplay") int patidDisplay,
            @RequestParam("medHistory") int medHistory,
            @RequestParam("adverseEvent") int adverseEvent,
            @RequestParam("concomMeds") String concomMeds,
            HttpServletResponse response) {
    	log.info("In display report");

        ServletOutputStream out = null;
        ModelAndView modelAndView = new ModelAndView("errors");

        response.setContentType("application/pdf");
        response.addHeader("content-disposition", "inline; filename=TBOutcomeWorksheet.pdf");

        try {
			out = response.getOutputStream();
			String[] patidStr = patid.split(",");
			List<Integer> pidList = new ArrayList<Integer>();
			for(String pid : patidStr) {
				pidList.add(Integer.parseInt(pid));
			}

			String[] instStr = institution.split(",");
			List<Integer> instList = new ArrayList<Integer>();
			for(String inst : instStr) {
				instList.add(Integer.parseInt(inst));
			}

			List<TBPatid> tbPatids = reportDao.getTBPatids(pidList, instList, tbEventFromDate, tbEventToDate, disposition,
	        		patientType, studyStatus, deadPids, ageRange, patidDisplay, medHistory, adverseEvent, concomMeds);

	        reportUtil.generateWorksheet(tbPatids, patidDisplay, request, out);

		} catch (Exception e) {
			 log.error(e.getMessage(), e);
	            modelAndView.addObject("reportError",
	                    "Error occured while generating the worksheet.");
	            return modelAndView;
		} finally {
			   ReportService.tryFlush(out);
	           ReportService.tryClose(out);
		}

        return null;
    }

}
