package org.fstrf.actg.TBOutcomeReport.dao;

import java.text.ParseException;

import javax.sql.DataSource;

import org.fstrf.actg.TBOutcomeReport.util.LogUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

// ACTUAL TABLE NAMES HAVE BEEN REMOVED

@Repository
public class ValidationDao extends JdbcDaoSupport {

    @Autowired
    public ValidationDao(DataSource dataSource){
        setDataSource(dataSource);
    }

    @Autowired
    private ResourceLookup lookup;

    private static final Logger log = LogUtil.getLogger(ValidationDao.class);

    public boolean isInternal(String username) throws DataAccessException {
        String sql = "SELECT ANY(1) FROM TABLE1 WHERE uname = ?";
        Integer result = lookup.getJdbcTemplate().queryForObject(sql, Integer.class, username);
        return result > 0;
    }

    public boolean isValidPatidStudy(String username, String patidList, String study) {
        String valid = "SELECT ANY(1) FROM TABLE2 WHERE patid = ? AND study = ?";
        String[] patidArr = patidList.split(",");
        try {
        	 for(String patid : patidArr) {
             	Integer result = lookup.getJdbcTemplate().queryForObject(valid, Integer.class, patid, study);
             	 if(result == 0) {
                      return false;
                  }
        	 }
        } catch (DataAccessException dae) {
        	log.error("Error validating patid", dae);
        	return false;
        }
        return isInternal(username);
    }

    public boolean isValidStudySite(String username, String study, String siteList) {
    	String instCheck = "SELECT FIRST 1 1 FROM TABLE3 where study = ? AND instn = ?";
    	String[] instArr = siteList.split(",");
    	try {
    		for(String inst : instArr) {
        		int institution = Integer.parseInt(inst);
        		Integer result = lookup.getJdbcTemplate().queryForObject(instCheck, Integer.class, study, institution);
        		if(result != 1) {
        			return false;
        		}
        	}
    	} catch (DataAccessException | NumberFormatException e) {
    		log.error("Error validating the institution", e);
    		return false;
    	}

    	return isInternal(username);
    }

    public boolean isValidPatidStudySite(String username, String patidList, String study, String siteList) {
        String valid = "SELECT ANY(1) FROM TABLE3 WHERE patid = ? AND instn = ? AND study = ?";
        Integer result = lookup.getJdbcTemplate().queryForObject(valid, Integer.class, patid, site, study);
        if(result == 0){
            return false;
        }
        if(isInternal(username)){
            return true;
        }
        String access = "SELECT ANY(1) FROM TABLE1 AS a INNER JOIN TABLE2 AS i ON a.study = i.study AND a.patid = i.patid "
                + "WHERE a.iuser = ? AND a.patid = ? AND a.study = ? AND i.instn = ?";
        result = lookup.getJdbcTemplate().queryForObject(access, Integer.class, username, patid, study, site);
        return result > 0;
    }

}