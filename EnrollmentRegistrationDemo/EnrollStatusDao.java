package org.fstrf.common.starrs.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.fstrf.common.starrs.util.CommonUtil;
import org.fstrf.common.starrs.util.Constants;
import org.fstrf.common.starrs.util.ContextDataSource;
import org.fstrf.common.starrs.util.LogUtil;
import org.fstrf.common.starrs.util.Queries;
import org.fstrf.common.starrs.util.SessionInfo;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository("enrollStatusDao")
public class EnrollStatusDao
{
    
    @Resource(name="contextJdbcTemplate")
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private ContextDataSource contextDataSource;
    
    @Autowired
    private CommonUtil commonUtil;
    
    @Autowired
    private SessionInfo sessionInfo;
    
    private static final Logger log = LogUtil.getLogger(EnrollStatusDao.class);
    
    public Map<String, String> getSummPageLabels(String study, int step) throws Exception {
        List<String> labels = Arrays.asList("SUCCESS", "FAIL", "PID", "CPID", "PROT", "VERSION", "CHECKLIST", "INST", "SID", "PRESCRIPTION");
        Map<String, String> labelMessageMap = new HashMap<String, String>();
        for(String param : labels) {
            try {
                String label = jdbcTemplate.queryForObject(Queries.ENROLL_STATUS_LABELS, String.class, study, step, param);
                labelMessageMap.put(param, label);
            } catch (IncorrectResultSizeDataAccessException i) {
                try {
                    String label = jdbcTemplate.queryForObject(Queries.ENROLL_STATUS_LABELS, String.class, study, Constants.ALL_STEPS, param);
                    labelMessageMap.put(param, label);
                } catch (IncorrectResultSizeDataAccessException i1) {
                    try {
                        String label = jdbcTemplate.queryForObject(Queries.ENROLL_STATUS_LABELS, String.class, Constants.ALL_STUDIES, Constants.ALL_STEPS, param);
                        labelMessageMap.put(param, label);
                    } catch (IncorrectResultSizeDataAccessException i2) {
                        labelMessageMap.put(param, ""); // blank label if no row found/more than 1 row found
                    } catch (DataAccessException dae2) {
                        log.error("Error retireving the labels for the confirmation page", dae2);
                        throw new Exception(dae2);
                    }
                } catch (DataAccessException dae1) {
                    log.error("Error retireving the labels for the confirmation page", dae1);
                    throw new Exception(dae1);
                }               
            } catch (DataAccessException dae) {
                log.error("Error retireving the labels for the confirmation page", dae);
                throw new Exception(dae);
            }
        }
        
        return labelMessageMap;
    }
    
    public String getStepName(String study, int step, String chksum) {    
        if(step != -1) {
            try {
                return jdbcTemplate.queryForObject(Queries.STEP_DESC, String.class, study, step, chksum);
            } catch (DataAccessException dae) {
                log.error("Error retrieving the step description for study "+study+" step "+step+" checksum "+chksum);                
                return "";
            }
           
        } else {
            try {
                return jdbcTemplate.queryForObject(Queries.SCREENING_SELECT_STUDY, String.class, study);
            } catch (DataAccessException dae) {
                try {
                    return jdbcTemplate.queryForObject(Queries.SCREENING_SELECT_STUDY, String.class, Constants.ALL_STUDIES);
                } catch (DataAccessException dae1) {
                    log.error("Error retrieving the step description for study "+study+" and all steps");   
                    return "";
                }
            }             
        }  
    }
    
    public int getSidDisplayType(String study, int step) throws Exception {
        try {
            return jdbcTemplate.queryForObject(Queries.SID_TYPE, Integer.class, study, step);
        } catch (IncorrectResultSizeDataAccessException i) {
            try {
                return jdbcTemplate.queryForObject(Queries.SID_TYPE, Integer.class, study, Constants.ALL_STEPS);
            } catch (IncorrectResultSizeDataAccessException i1) {
                return Constants.DEFAULT_SID_PID;
            } catch (DataAccessException dae1) {
                log.error("Error retrieving the display type of the sid for study "+study+ " and all steps", dae1);
                return Constants.NO_ROW;
            }            
        } catch (DataAccessException dae) {
            log.error("Error retrieving the display type of the sid for study "+study+ " and step "+step, dae);
            return Constants.NO_ROW;
        }
    }
    
    public int getPidDisplayType(String study, int step) throws Exception {
        try {
            return jdbcTemplate.queryForObject(Queries.PID_TYPE, Integer.class, study, step);
        } catch (IncorrectResultSizeDataAccessException i) {
            try {
                return jdbcTemplate.queryForObject(Queries.PID_TYPE, Integer.class, study, Constants.ALL_STEPS);
            } catch (IncorrectResultSizeDataAccessException i1) {
                return Constants.DEFAULT_SID_PID;              //sharing the default_sid value of -2
            } catch (DataAccessException dae1) {
                log.error("Error retrieving the display type of the pid for study "+study+ " and all steps", dae1);
                return Constants.NO_ROW;
            }            
        } catch (DataAccessException dae) {
            log.error("Error retrieving the display type of the pid for study "+study+ " and step "+step, dae);
            return Constants.NO_ROW;
        }
    }

    public Map<String, Boolean> getDisplayStatus(String study, int step) throws Exception {
        List<String> fields = Arrays.asList("PID", "CPID", "VERSION", "CHECKLIST", "SID", "PRESCRIPTION");
        Map<String, Boolean> fieldStatusMap = new HashMap<String, Boolean>();
        for(String param : fields) {
            try {
                int code = jdbcTemplate.queryForObject(Queries.DISPLAY_SUMMARY_FIELD, Integer.class, study, step, param);                
                boolean isDisp = (code == Constants.DO_NOT_DISPLAY) ? false : true ; // if dsply = 1 or 3 or 4 then show else don't show field
                fieldStatusMap.put(param, isDisp);
            } catch (IncorrectResultSizeDataAccessException i) {
                try {
                    int code = jdbcTemplate.queryForObject(Queries.DISPLAY_SUMMARY_FIELD, Integer.class, study, Constants.ALL_STEPS, param);
                    boolean isDisp = (code == Constants.DO_NOT_DISPLAY) ? false : true ;
                    fieldStatusMap.put(param, isDisp);
                } catch (IncorrectResultSizeDataAccessException i1) {
                    try {
                        int code = jdbcTemplate.queryForObject(Queries.DISPLAY_SUMMARY_FIELD, Integer.class, Constants.ALL_STUDIES, Constants.ALL_STEPS, param);
                        boolean isDisp = (code == Constants.DO_NOT_DISPLAY) ? false : true ;
                        fieldStatusMap.put(param, isDisp);
                    } catch (IncorrectResultSizeDataAccessException i2) {
                        fieldStatusMap.put(param, true); // default to true if no row found/more than 1 row found
                    } catch (DataAccessException dae2) {
                        log.error("Error retireving the field display flag for the confirmation page", dae2);
                        throw new Exception(dae2);
                    }
                } catch (DataAccessException dae1) {
                    log.error("Error retireving the field display flag for the confirmation page", dae1);
                    throw new Exception(dae1);
                }               
            } catch (DataAccessException dae) {
                log.error("Error retireving the field display flag for the confirmation page", dae);
                throw new Exception(dae);
            }
        }
        return fieldStatusMap;
    }

}
