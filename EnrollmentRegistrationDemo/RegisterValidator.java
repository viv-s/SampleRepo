package org.fstrf.common.starrs.validator;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.fstrf.common.starrs.dao.UserDao;
import org.fstrf.common.starrs.model.RegisterForm;
import org.fstrf.common.starrs.util.CommonUtil;
import org.fstrf.common.starrs.util.Constants;
import org.fstrf.common.starrs.util.ErrorConstants;
import org.fstrf.common.starrs.util.ErrorHandler;
import org.fstrf.common.starrs.util.LogUtil;
import org.fstrf.security.util.SecurityContextWebAppUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class RegisterValidator implements Validator {   
    
    @Autowired
    UserDao userDao; 
    
    @Autowired
    CommonUtil commonUtil;
    
    @Autowired
    ErrorHandler errorHandler;
    
    private static final Logger log = LogUtil.getLogger(RegisterValidator.class);

    @Override
    public boolean supports(Class<?> clazz) {       
        return RegisterForm.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        RegisterForm reg = (RegisterForm) target;
        String study = reg.getStudy();
        String version = reg.getVersion();
        int step = reg.getStep();
        boolean isScreeningError = false;
        try {
            //check if screening study
            if(step == Constants.SCREENING_STEP) {               
                // get the screening study 
                study = userDao.getScreeningStudy(reg.getSite(), reg.getStudy()); // assign screening study as the main study for all business logic going forward   
                if(study == "") {
                    isScreeningError = true;
                    // only get the error message, email is handled in the dao method above
                    errorHandler = commonUtil.getErrorMessage(commonUtil.getErrCode(), Constants.ALL_STUDIES, Constants.ALL_STEPS); 
                    String error = formatErrorMsg(errorHandler.getMessage(), commonUtil.getErrCode(), commonUtil.getSubCode());
                    errors.reject("study", error);
                }                    
                // get screening checksum               
                version = userDao.getScreeningChksum(study); // set checksum in version field to screening checksum
                if(version == "") {
                    isScreeningError = true;
                    // only get the error message, email is handled in the dao method above
                    errorHandler = commonUtil.getErrorMessage(commonUtil.getErrCode(), study, Constants.ALL_STEPS);
                    String error = formatErrorMsg(errorHandler.getMessage(), commonUtil.getErrCode(), commonUtil.getSubCode());
                    errors.reject("version", error);
                }
                   
                step = 1; // set screening step to 1            
            }
            
            // check required fields. Institution, study, version and step are required fields. Other fields are dynamic.
       
            if(reg.getSite() == null || reg.getSite() == "") {
                errorHandler = commonUtil.handleError(ErrorConstants.SITE_NOT_FOUND, ErrorConstants.BLANK_INST_SUB,
                        Constants.ALL_STUDIES, Constants.ALL_STEPS, new StringBuffer());
                String error = formatErrorMsg(errorHandler.getMessage(), ErrorConstants.SITE_NOT_FOUND, ErrorConstants.BLANK_INST_SUB);
                errors.reject("site", error);
            }
            if(!isScreeningError) {
                if(study == null || study == "") {
                    errorHandler = commonUtil.handleError(ErrorConstants.STUDY_NOT_FOUND, ErrorConstants.BLANK_STUDY_SUB, 
                            Constants.ALL_STUDIES, Constants.ALL_STEPS, new StringBuffer());                  
                    String error = formatErrorMsg(errorHandler.getMessage(), ErrorConstants.STUDY_NOT_FOUND, ErrorConstants.BLANK_STUDY_SUB);
                    errors.reject("study", error);
                }
            }
            // Logic to handle dynamic fields on the form: First check if the previous fields have been answered so that fields that are not shown are not validated.
            // If yes, it means the dynamic field is displayed on the page. check the form element that needs to be validated. 
            
            // if inst and study are not blank then check version
            if(reg.getSite().length() > 0 && study.length() > 0) {
                if(!isScreeningError) {
                    if((version == null || version == "")) { // check for blank checksum
                        errorHandler = commonUtil.handleError(ErrorConstants.VERSION_NOT_FOUND, ErrorConstants.BLANK_VERSION_SUB,
                                study, Constants.ALL_STEPS, new StringBuffer());
                        String error = formatErrorMsg(errorHandler.getMessage(), ErrorConstants.VERSION_NOT_FOUND, ErrorConstants.BLANK_VERSION_SUB);
                        errors.reject("version", error);
                    } else { // check for valid checksum
                        List<String> checksums = userDao.getValidChecksums(study);
                        // the value of version field dropdown is checksum. check if valid checksum list contains the version (checksum) selected in the dropdown
                        if(!checksums.contains(version)) {
                            errorHandler = commonUtil.getErrorMessage(commonUtil.getErrCode(), study, Constants.ALL_STEPS);
                            String error = formatErrorMsg(errorHandler.getMessage(), commonUtil.getErrCode(), commonUtil.getSubCode());
                            errors.rejectValue("version", error);
                        }
                    } 
                }
                // check user has access to the institution and the study 
                String username = SecurityContextWebAppUtil.getUsername();
                
                // check if user has access to specific site or all sites
                boolean isValidUserSite = userDao.checkUserSite(reg.getSite(), username); 
                if(isValidUserSite) {
                    boolean isSiteActive = userDao.checkSiteActive(reg.getSite());
                    if(isSiteActive) {
                        boolean userStudyAccess = userDao.checkUserStudyAccess(study, username);
                        if(userStudyAccess) {
                            boolean isValidUserProtocol = userDao.checkStudyStatus(reg.getSite(), study);
                            if(!isValidUserProtocol) {
                                errorHandler = commonUtil.getErrorMessage(ErrorConstants.INVALID_STUDY, study, Constants.ALL_STEPS);
                                String error = formatErrorMsg(errorHandler.getMessage(), ErrorConstants.INVALID_STUDY, ErrorConstants.STUDY_NOT_ACTIVE_SUB);
                                errors.reject("study", error);
                            }
                        } else {
                            // user does not have access to the study
                            errorHandler = commonUtil.getErrorMessage(ErrorConstants.INVALID_STUDY, study, Constants.ALL_STEPS);
                            String error = formatErrorMsg(errorHandler.getMessage(), ErrorConstants.INVALID_STUDY, ErrorConstants.USER_NO_STUDY_ACCESS_SUB);
                            errors.reject("study", error);
                        }                       
                    } else {
                        errorHandler = commonUtil.getErrorMessage(ErrorConstants.INVALID_SITE, Constants.ALL_STUDIES, Constants.ALL_STEPS);
                        String error = formatErrorMsg(errorHandler.getMessage(), ErrorConstants.SITE_NOT_FOUND, ErrorConstants.USER_NO_SITE_ACCESS_SUB);
                        errors.rejectValue("site", error);
                    }                    
                } else {
                    errorHandler = commonUtil.getErrorMessage(ErrorConstants.INVALID_SITE, Constants.ALL_STUDIES, Constants.ALL_STEPS);
                    String error = formatErrorMsg(errorHandler.getMessage(), ErrorConstants.INVALID_SITE, ErrorConstants.USER_NO_SITE_ACCESS_SUB);
                    errors.rejectValue("site", error);
                }
                
            }
            
            // inst, study, version are not blank then check step
            if(reg.getSite().length() > 0 && study.length() > 0 && version.length() > 0) {
                if(step == Constants.BLANK_STEP) {
                    errorHandler = commonUtil.handleError(ErrorConstants.STEP_NOT_FOUND, ErrorConstants.BLANK_STEP_SUB,
                            study, Constants.ALL_STEPS, new StringBuffer());
                    String error = formatErrorMsg(errorHandler.getMessage(), ErrorConstants.STEP_NOT_FOUND, ErrorConstants.BLANK_STEP_SUB);
                    errors.reject("step", error);
                } else { // check for valid step 
                    List<Integer> steps = userDao.getValidSteps(study);
                    if(!steps.contains(step)) {
                        errorHandler = commonUtil.getErrorMessage(ErrorConstants.STEP_NOT_FOUND, study, Constants.ALL_STEPS);
                        String error = formatErrorMsg(errorHandler.getMessage(), ErrorConstants.STEP_NOT_FOUND, ErrorConstants.STEP_NOT_FOUND_SUB2);
                        errors.reject("step", error);
                    }
                }
            }
			
			String isScrNumDisplayed = userDao.isScrNumDisplayed(study, step, version);
                if(isScrNumDisplayed != null) {
                    String leadingChar = userDao.getScrNumChar(study);
                    if(leadingChar != null) {
                        String userScrNum = reg.getScrNum().replaceAll("\\s", "");
                        String userScrNumChar = userScrNum.substring(0, leadingChar.length()); // beginning of string - get protocol set characters
                        String userScrNumDigits = userScrNum.substring(leadingChar.length(), userScrNum.length()); // later part of string - get numbers
                        
                        if(NumberUtils.isDigits(userScrNumDigits)) {
                            boolean isValidScrNum = userDao.checkValidScrNum(study, Integer.parseInt(userScrNumDigits), userScrNumChar);
                            if(isValidScrNum) {
                                // check for the site
                                boolean isValidScrNumSite = userDao.checkSiteScrNum(study, step, Integer.parseInt(userScrNumDigits), userScrNumChar, reg.getSite());
                                if(!isValidScrNumSite) {
                                    errorHandler = commonUtil.getErrorMessage(ErrorConstants.INVALID_SCRNUM, study, step);
                                    String error = formatErrorMsg(errorHandler.getMessage(), ErrorConstants.INVALID_SCRNUM, ErrorConstants.SCRNUM_INVALID_INST_SUB);
                                    errors.reject("scrNum", error);
                                }
                                
                            } else { // if screening number is not valid (no row found in prsscr) then check status and print status related error message, else it is simply not a valid screening number
                                int scrStatus = userDao.checkScrNumStatus(study, Integer.parseInt(userScrNumDigits), userScrNumChar);
                                StringBuffer params = new StringBuffer();
                                switch(scrStatus) {
                                    case 2:
                                        params.append("Status code 2 found in prsscr for study = "+study+
                                                " spid="+Integer.parseInt(userScrNumDigits)+" pchar="+userScrNumChar+Constants.LINE_BREAK);
                                        errorHandler = commonUtil.handleError(ErrorConstants.INVALID_SCRNUM, ErrorConstants.SCRNUM_ENROLLED_SUB, study, step, params);
                                        String error2 = formatErrorMsg(errorHandler.getMessage(), ErrorConstants.INVALID_SCRNUM, ErrorConstants.SCRNUM_ENROLLED_SUB);
                                        errors.reject("scrNum", error2);
                                    break;                        
                                    case 3:
                                        params.append("Status code 3 found in prsscr for study = "+study+
                                                " spid="+Integer.parseInt(userScrNumDigits)+" pchar="+userScrNumChar+Constants.LINE_BREAK);
                                        errorHandler = commonUtil.handleError(ErrorConstants.INVALID_SCRNUM, ErrorConstants.SCRNUM_NO_ENROL_SUB, study, step, params);
                                        String error3 = formatErrorMsg(errorHandler.getMessage(), ErrorConstants.INVALID_SCRNUM, ErrorConstants.SCRNUM_ENROLLED_SUB);
                                        errors.reject("scrNum", error3);
                                    break;
                                    case 4: 
                                        params.append("Status code 4 found in prsscr for study = "+study+
                                                " spid="+Integer.parseInt(userScrNumDigits)+" pchar="+userScrNumChar+Constants.LINE_BREAK);
                                        errorHandler = commonUtil.handleError(ErrorConstants.INVALID_SCRNUM, ErrorConstants.SCRNUM_EXPIRED_SUB, study, step, params);
                                        String error4 = formatErrorMsg(errorHandler.getMessage(), ErrorConstants.INVALID_SCRNUM, ErrorConstants.SCRNUM_EXPIRED_SUB);
                                        errors.reject("scrNum", error4);
                                    break;
                                    case 5: 
                                        params.append("Status code 5 found in prsscr for study = "+study+
                                                " spid="+Integer.parseInt(userScrNumDigits)+" pchar="+userScrNumChar+Constants.LINE_BREAK);
                                        errorHandler = commonUtil.handleError(ErrorConstants.INVALID_SCRNUM, ErrorConstants.SCRNUM_NOT_SELECTED_SUB, study, step, params);
                                        String error5 = formatErrorMsg(errorHandler.getMessage(), ErrorConstants.INVALID_SCRNUM, ErrorConstants.SCRNUM_NOT_SELECTED_SUB);
                                        errors.reject("scrNum", error5);
                                    break;
                                    case 6: 
                                        params.append("Status code 6 found in prsscr for study = "+study+
                                                " spid="+Integer.parseInt(userScrNumDigits)+" pchar="+userScrNumChar+Constants.LINE_BREAK);
                                        errorHandler = commonUtil.handleError(ErrorConstants.INVALID_SCRNUM, ErrorConstants.SCRNUM_NOT_PROCEED_SUB, study, step, params);
                                        String error6 = formatErrorMsg(errorHandler.getMessage(), ErrorConstants.INVALID_SCRNUM, ErrorConstants.SCRNUM_NOT_PROCEED_SUB);
                                        errors.reject("scrNum", error6);
                                    break;
                                    default:
                                        params.append("No row found in prsscr for study = "+study+
                                                " spid="+Integer.parseInt(userScrNumDigits)+" pchar="+userScrNumChar+Constants.LINE_BREAK);
                                        errorHandler = commonUtil.handleError(ErrorConstants.INVALID_SCRNUM, ErrorConstants.SCRNUM_NOT_FOUND_SUB, study, step, params);
                                        String error7 = formatErrorMsg(errorHandler.getMessage(), ErrorConstants.INVALID_SCRNUM, ErrorConstants.SCRNUM_NOT_FOUND_SUB);
                                        errors.reject("scrNum", error7);
                                    break;
                                }
                            }                        
                        } else {
                            errorHandler = commonUtil.handleError(ErrorConstants.INVALID_SCRNUM, ErrorConstants.SCRNUM_INVALID_DIGITS_SUB, study, step, new StringBuffer());
                            String error = formatErrorMsg(errorHandler.getMessage(), ErrorConstants.INVALID_SCRNUM, ErrorConstants.SCRNUM_INVALID_DIGITS_SUB);
                            errors.reject("scrNum", error);
                        }
                    } else {
                        errorHandler = commonUtil.getErrorMessage(ErrorConstants.INVALID_SCRNUM, study, step);
                        String error = formatErrorMsg(errorHandler.getMessage(), ErrorConstants.INVALID_SCRNUM, ErrorConstants.SCRNUM_PREFIX_NOT_EXISTS_SUB);
                        errors.reject("scrNum", error);
                     }
                                   
                }
			
		} catch (Exception e) { // handle global error for target object. in controller, check for global error and display error page to user.
            log.error("Error in validating Getting Started form", e);
            StringBuffer params = new StringBuffer();
            params.append("Error validating Getting Started page data. This could be due to a program bug or database query failure. Contact programming team.");
            try
            {
                errorHandler = commonUtil.handleError(ErrorConstants.GEN_ERROR, ErrorConstants.GEN_ERROR_SUB, study, step, params);
                String error = formatErrorMsg(errorHandler.getMessage(), ErrorConstants.GEN_ERROR, ErrorConstants.GEN_ERROR_SUB);
                errors.reject("", error);
            } catch (Exception e1)
            {
                log.error("Getting Started page validation failure - Error in getting error message and sending email. ", e1);
                errors.reject("", "Error in processing the data.");
            }
         }
	}
}