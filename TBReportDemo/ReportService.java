package org.fstrf.actg.TBOutcomeReport.service;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.Map;

import org.fstrf.actg.TBOutcomeReport.controllers.IndexController;
import org.fstrf.actg.TBOutcomeReport.dao.ReportDao;
import org.fstrf.actg.TBOutcomeReport.dao.ValidationDao;
import org.fstrf.actg.TBOutcomeReport.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

	private static final Logger log = LogUtil.getLogger(ReportService.class);

    @Autowired
    private ValidationDao validationDao;

    @Autowired
    private ReportDao reportDao;

    public boolean isInternal(String username) throws Exception {
        return validationDao.isInternal(username);
    }

    public boolean isValidPatidStudy(String username, String patidList, String study) {
        return validationDao.isValidPatidStudy(username, patidList, study);
    }

    public boolean isValidStudySite(String username, String study, String siteList) {
        return validationDao.isValidStudySite(username, study, siteList);
    }

    public Map<Integer, String> getDispositions() throws Exception {
    	return reportDao.getDispositions();
    }

    public Map<Integer, String> getMedicalHistory() throws Exception {
    	return reportDao.getMedHistory();
    }

    public Map<Integer, String> getAdverseEvents() throws Exception {
    	return reportDao.getAdverseEvents();
    }

    public Map<String, String> getConcomMeds() throws Exception {
    	return reportDao.getConcomMeds();
    }

    public Map<String, String> getTBTreatmentTypes() throws Exception {
    	return reportDao.getConcomMedsTB();
    }


    public static void tryClose(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                log.warn("could not close " + c, e);
            }
        }
    }

    public static void tryFlush(Flushable f) {
        if (f != null) {
            try {
                f.flush();
            } catch (IOException e) {
                log.warn("could not flush " + f, e);
            }
        }
    }

}