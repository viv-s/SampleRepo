package org.fstrf.common.starrs.service;

import java.util.List;
import java.util.Map;

/**
 * @author shingorn
 * Interface to get data for the enrollment status page
 */
public interface EnrollStatusService
{
    public Map<String, String> getSummPageLabels(String study, int step) throws Exception;
    
    public Map<String, Boolean> getDisplayStatus(String study, int step) throws Exception;
    
    public String getStepName(String study, int step, String version) throws Exception;
    
    public String getInstName(String inst) throws Exception;
    
    public int getSidDisplayType(String study, int step) throws Exception;
    
    public int getPidDisplayType(String study, int step) throws Exception;

    public String getVersionName(String study, String version) throws Exception;
    
    public List<String> getEnrollmentSummUsers(String inst, String study, int grpc) throws Exception;
    
    public String getFilePath(String type);
    
    public String getMailEncryptionType();
    
    public int getGrpc(String inst, String study) throws Exception;
}
