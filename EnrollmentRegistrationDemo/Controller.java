package org.fstrf.common.starrs.controller;

import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.fstrf.common.starrs.model.DemographicForm;
import org.fstrf.common.starrs.model.RegisterForm;
import org.fstrf.common.starrs.model.RegisterStart;
import org.fstrf.common.starrs.model.RequiredInfo;
import org.fstrf.common.starrs.model.SummaryForm;
import org.fstrf.common.starrs.randoBridge.GetTrackNumber;
import org.fstrf.common.starrs.randoBridge.IrandoBridge.IPrsResult;
import org.fstrf.common.starrs.randoBridge.RandoBridge;
import org.fstrf.common.starrs.service.ChecklistService;
import org.fstrf.common.starrs.service.EnrollStatusService;
import org.fstrf.common.starrs.service.ReqInfoService;
import org.fstrf.common.starrs.service.UserService;
import org.fstrf.common.starrs.util.CommonUtil;
import org.fstrf.common.starrs.util.Constants;
import org.fstrf.common.starrs.util.ContextDataSource;
import org.fstrf.common.starrs.util.ControllerHelper;
import org.fstrf.common.starrs.util.EnrollmentBlocker;
import org.fstrf.common.starrs.util.ErrorConstants;
import org.fstrf.common.starrs.util.ErrorHandler;
import org.fstrf.common.starrs.util.FormData;
import org.fstrf.common.starrs.util.LogUtil;
import org.fstrf.common.starrs.util.Queries;
import org.fstrf.common.starrs.util.SessionInfo;
import org.fstrf.common.starrs.validator.DemographicValidator;
import org.fstrf.common.starrs.validator.RegisterValidator;
import org.fstrf.common.webformrep.WebFormRep;
import org.fstrf.common.webformrep.WebFormRepComponentFactory;
import org.fstrf.common.webformrep.WebFormRepPageSingleContainerPage;
import org.fstrf.common.webformrep.WebFormRepProvider;
import org.fstrf.common.webformrep.comp.store.HttpServletRequestFormValueSource;
import org.fstrf.security.util.SecurityContextWebAppUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;


@Controller
@SessionAttributes({"regForm", "demogForm"})
public class StarrsController
{
    private static final Logger log = LogUtil.getLogger(StarrsController.class);
    
    // self note: use Resource and Autowired annotations to let Spring configure and manage the beans. 
    // Resource and Autowired are similar. Autowired is the newer annotation.
    @Resource
    private UserService userService;
    
    @Autowired
    private ReqInfoService reqInfoService;
    
    @Autowired
    private ChecklistService checklistService;
    
    @Autowired
    private RegisterValidator registerValidator;
    
    @Autowired
    private DemographicValidator demographicValidator;
    
    @Resource
    private WebFormRepComponentFactory webFormRepComponentFactory;
    
    @Autowired
    private WebFormRepProvider webFormRepProvider;
    
    @Autowired
    private ControllerHelper controllerHelper;
    
   @Autowired
   private SessionInfo sessionInfo;
   
   @Autowired
   private CommonUtil commonUtil;
   
   @Autowired
   private ContextDataSource contextDataSource;
   
   @Autowired
   ErrorHandler errorHandler;
   
   @Autowired
   EnrollStatusService enrollStatusService;
   
   @Autowired
   EnrollmentBlocker enrollmentBlocker;
@RequestMapping(value="/submitRegister", method = RequestMethod.POST)
    public String submitRegister(Model model, @ModelAttribute("regForm") RegisterForm regForm, 
            BindingResult result, HttpServletRequest request,  HttpSession session,
            @RequestParam(required=false, value="subBtnRegister") String subBtnRegister,
            @RequestParam(required=false, value="blankChklist") String blankChklist,
            @RequestParam(required=false, value="filledChklist") String filledChklist) throws Exception {        
     
        String username = SecurityContextWebAppUtil.getUsername();
        log.info("Submitting Geting Started page info");
        
        if(session.getAttribute("gettingStartedPage") == null) {
            return "loggedOut";
        }
        
        
        if(subBtnRegister != null) {
            log.info("Validating Getting Started page data...");
            registerValidator.validate(regForm, result);
        }
        if(result.hasErrors()) {
            log.info("Validation error on Getting Started page");
            model.addAttribute("isError", true);           
            controllerHelper.reloadGettingStartedPage(model, regForm, username);
            return "startRegistration";
        } else {            
            // get demographic details for next page (required information page)
            log.info("Getting Started page validations passed.");
            String study = regForm.getStudy();
            String version = regForm.getVersion();
            int step = regForm.getStep();
            
            // Set the pid check digit to be used by the rando bridge later
            int pidChk = userService.getTypeOfChKDigit(regForm.getSite(), regForm.getStudy(), regForm.getStep(), Constants.PID);
            if(pidChk != Constants.ACTG_GEN_CHKDIG) { // If check digit is entered by the user
                if(regForm.getPatid().length() > 1) { // If pid field is displayed on the screen and user enters valid pid with check digit 
                    regForm.setPidChkDig(regForm.getPatid().substring(regForm.getPatid().length() - 1));
                } else { // If pid is not displayed on the screen 
                    regForm.setPidChkDig("");
                }                
            }
            
            if(regForm.getStep() == Constants.SCREENING_STEP) {
                regForm.setcStudy(regForm.getStudy());     // store clinical study 
                regForm.setcVersion(regForm.getVersion());  // store clinical version
                study = userService.getScreeningStudy(regForm.getSite(), regForm.getStudy()); // assign screening study as the main study for all business logic going forward   
                version = userService.getScreeningChksum(study);
                step = 1; // set screening step to 1
                regForm.setStudy(study);
                regForm.setVersion(version);
                regForm.setStep(step);
            }
            
            // Load form fields in session. These values are used to determine duplicate enrollments and 
            // loading of blank/previously filled checklist
            if(session.getAttribute("sessSite") == null) {
                controllerHelper.setFormFieldsinSession(regForm, session);
            }
            
            // check if child pids were updated in the validation process (due to pidchk = 5) with check digit. If yes, then replace numerical 
            // child pid values with numerical + checkdigit values
            if(regForm.getcPidWithChkDigit() != null && !regForm.getcPidWithChkDigit().isEmpty()) {
                regForm.setcPatid(regForm.getcPidWithChkDigit());
            }
            
            // Check if review page is needed for the study
            boolean isSkipReview = checklistService.skipReviewPage(regForm.getStudy());
            String checklistBtnText = "";
            if(isSkipReview) {
                sessionInfo.setShowReviewLink(false);
                model.addAttribute("nextChklistStep", "Submit");
                checklistBtnText = "Submit";
            } else {
                sessionInfo.setShowReviewLink(true);
                model.addAttribute("nextChklistStep", "Continue");
                checklistBtnText = "Continue";
            }
            
            RequiredInfo reqInfo = reqInfoService.getDemogConfig(regForm.getSite(), study, step);
            // if no record found in demogrs table for the site/study then go to demographics page and show error.
            // if demographic details not required for study then skip demographics page ('Required information' screen)
            // and go the checklist          
            if((reqInfo != null
                && reqInfo.getEthnicityConfig() == Constants.DEMOG_NOT_REQUIRED
                && reqInfo.getRaceConfig() == Constants.DEMOG_NOT_REQUIRED
                && reqInfo.getNetworkConfig() == Constants.DEMOG_NOT_REQUIRED
                && reqInfo.getIv() == Constants.DEMOG_NOT_REQUIRED) || reqInfo == null) {
                
                String nextPage = "checklist";
                log.info("Demographics not needed. Setting checklist page");                
                
                if(subBtnRegister != null) {
                    log.info("Verifying if core and non-core fields were updated");
                // Check if checklist is saved in session first. If study, version and step from the previous enrollment 
                // were not changed then load the previously filled out checklist, else, load new checklist
                Map<String, Object> idToValueMap = (Map<String, Object>) session.getAttribute("sessionChecklistMap");
                if(idToValueMap != null
                        && controllerHelper.areCoreFieldsSame(regForm, session)
                        && controllerHelper.areNonCoreFieldsSame(regForm, session)) {
                    
                    // load previous filled out checklist if previous enrollment failed, else display duplicate enrollment message
                    // Duplicate enrollment - check if user has entered the program for the first time or clicked the back button                
                    int stopEnroll = sessionInfo.getStopEnroll();
                    if(stopEnroll != Constants.NEW_ENROLLMENT) {
                        // if user came back to the page check prsresult for enrollment status
                        int enrollStatus = commonUtil.getEnrollmentStatus(sessionInfo.getTrackingNum());
                        sessionInfo.setStopEnroll(enrollStatus);
                        if(enrollStatus == Constants.ENROLL_STOP) {
                            controllerHelper.reloadGettingStartedPage(model, regForm, username);
                            model.addAttribute("stopEnrollErr", "error");
                            model.addAttribute("dupEnrollMsg", commonUtil.getDuplicateEnrollMsg(ErrorConstants.DUPLICATE_ENROLL, regForm.getStudy()));
                            model.addAttribute("newEnrollBtn", "Start new enrollment");                           
                         
                            return "startRegistration"; 
                        } else { // Failed previous enrollment and no change to core and non-core fields, load previous checklist
                         // generate tracking number and go to checklist
                            return navigateToChecklist(model, regForm, request,
                                    session, username);                             
                        }
                    } else { // New enrollment 
                        return navigateToChecklist(model, regForm, request,
                                session, username);  
                    }                
                    
                } else if(idToValueMap != null && !controllerHelper.areCoreFieldsSame(regForm, session)) {   // Core fields changed so load new checklist
                    // load new checklist
                    study = study.trim();
                    return getTrackNumberAndChecklistPage(model, regForm, session, username, study,
                            version, checklistBtnText);
                } else if (idToValueMap != null && !controllerHelper.areNonCoreFieldsSame(regForm, session)) {  // Non core fields changed 
                    // Check if checklist was answered
                    if(controllerHelper.isChecklistEmpty(idToValueMap) || (Boolean)session.getAttribute("checklistVisited") == false) { // Checklist was not answered - load blank checklist
                        study = study.trim();
                        return getTrackNumberAndChecklistPage(model, regForm, session,
                                username, study, version, checklistBtnText);
                    } else { // Change made to non core fields. Check if previous enrollment was successful, if it was then display new checklist
                        int enrollStatus = commonUtil.getEnrollmentStatus(sessionInfo.getTrackingNum());
                        sessionInfo.setStopEnroll(enrollStatus);
                        if(enrollStatus == Constants.ENROLL_STOP) {
                            if((Boolean)session.getAttribute("checklistVisited")) {  // if user visits the checklist page 
                                if(!controllerHelper.isChecklistEmpty(idToValueMap)) { // checklist not empty                                    
                                 
                                    // display warning popup to user
                                    model.addAttribute("warnChecklist", "show");
                                    String warnMsg = userService.checklistWarnMsg();
                                    model.addAttribute("warnChecklistMsg", warnMsg);
                                    controllerHelper.reloadGettingStartedPage(model, regForm, username);
                                    controllerHelper.setFormFieldsinSession(regForm, session);
                                    
                                    return "startRegistration";                            
                                  
                                } else { // checklist was empty
                                    return loadNewChecklist(model, regForm,
                                            session, study, version,
                                            checklistBtnText);
                                }
                            } else { // if user clicks Back from summary page and comes to Getting Started and clicks Continue after changing non core field then show blank checklist
                                study = study.trim();
                                return getTrackNumberAndChecklistPage(model, regForm, session, username, study, version, checklistBtnText);
                            }
                           
                        } else { // If changes were made to the non-core fields and there is at least 1 response in the checklist and previous enrollment attempt failed
                            // then display warning message to user
                            model.addAttribute("warnChecklist", "show");
                            String warnMsg = userService.checklistWarnMsg();
                            model.addAttribute("warnChecklistMsg", warnMsg);
                            controllerHelper.reloadGettingStartedPage(model, regForm, username);
                            controllerHelper.setFormFieldsinSession(regForm, session);
                            
                            return "startRegistration"; 
                        }
                       
                    }                                    
                } else {   //When user logs in for the first time and blank checklist page should be loaded.
                    // generate tracking number and go to checklist
                    study = study.trim();
                    return getTrackNumberAndChecklistPage(model, regForm, session, username,
                            study, version, checklistBtnText);
                }
                } else if(blankChklist != null) { // only load new checklist because at this point user had previously been to the checklist and tracking number exists.
                    return loadNewChecklist(model, regForm, session, study, version, checklistBtnText);
                } else { // only navigate to old checklist because at this point user had previously been to the checklist and tracking number exists.
                    return navChecklist(model, request, session);    
                    
                }
             } else { //  Go to demographics
                 log.info("Loading demographics page");
                 // Need to add this to the model here when demographics page is called.
                 // If "demogForm" is not added to the model, Spring's session attribute annotation 
                 // throws an error in the submitDemog method below (HttpSessionRequiredException).
                 model.addAttribute("demogForm", new DemographicForm());    
                 controllerHelper.loadDemographics(model, regForm, username);
                 
                 // New enrollment. Initialize the tracking number
                 // sessionInfo.setTrackingNum(Constants.BLANK_TRACKING);
                 // sessionInfo.setNewTrackNumObtained(false);
                                  
                 controllerHelper.getMenu(model, sessionInfo.getUsername());
                 controllerHelper.setRequiredInfoPageLinks(model);
                 
                 session.setAttribute("demographicsPage", "demographicsPage");
                 return "reqInformation";
                 
             }  
        }
    }
	
	 /**
     * Method to navigate to the previously loaded checklist page with generation of new tracking number 
     * @param model
     * @param regForm
     * @param request
     * @param session
     * @param username
     * @return String name of view to be loaded
     * @throws Exception
     * @throws SQLException
     */
    private String navigateToChecklist(Model model, RegisterForm regForm,
            HttpServletRequest request, HttpSession session, String username)
            throws Exception, SQLException
    {
        log.info("Navigate to checklist");
        String bridgeUsername = sessionInfo.getLastName()+"."+sessionInfo.getFirstName();
        String study = regForm.getStudy().trim();
        String trackNum = new GetTrackNumber(contextDataSource.getContextDataSource(), bridgeUsername,
                study, String.valueOf(regForm.getStep()), regForm.getSite()).buildTrackNo(); 
        sessionInfo.setTrackingNum(trackNum); 
        int isRecInserted = commonUtil.insertEnrollStatus(trackNum, Constants.NEW_ENROLLMENT);
        if(isRecInserted < 1) {
            controllerHelper.reloadGettingStartedPage(model, regForm, username);
            model.addAttribute("isError", true);
            model.addAttribute("dupEnrollError", true);
            
            StringBuffer params = new StringBuffer();
            params.append("Error insert enrollment status records in enrollStatus table for track number "+trackNum+Constants.LINE_BREAK);
            errorHandler = commonUtil.handleError(ErrorConstants.GEN_ERROR, ErrorConstants.ENROLL_STATUS_INSERT_SUB, regForm.getStudy(), regForm.getStep(), params);
            String dupErrorMsg = errorHandler.getMessage()+" [Error "+ErrorConstants.GEN_ERROR+"-"+ErrorConstants.ENROLL_STATUS_INSERT_SUB+"]";
            model.addAttribute("dupErrorMsg", dupErrorMsg);
        
            return "startRegistration"; 
        }
                   // Needed to know if user visited the checlist page. This is useful when user clicks back button to go to the firs page after a failed enrollment
                  //and tries to enroll again. If user visits the checklist then load that state of the checklist else load blank checklist
        sessionInfo.setShowReqInfoLink(false);                           
        controllerHelper.setChecklistPageLinks(model);
        session.setAttribute("checklistVisited", true);  
        controllerHelper.setFormFieldsinSession(regForm, session);
        
        session.setAttribute("checklistPage", "checklistPage");
        return navChecklist(model, request, session);
    }
}