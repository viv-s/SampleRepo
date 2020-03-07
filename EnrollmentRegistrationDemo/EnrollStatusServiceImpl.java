package org.fstrf.common.starrs.service;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.fstrf.common.starrs.dao.EnrollStatusDao;
import org.springframework.stereotype.Service;

@Service("enrollStatusService")
public class EnrollStatusServiceImpl implements EnrollStatusService
{
    @Resource(name = "enrollStatusDao")
    EnrollStatusDao enrollStatusDao;

    @Override
    public Map<String, String> getSummPageLabels(String study, int step)
            throws Exception
    {
        return enrollStatusDao.getSummPageLabels(study, step);
    }

    @Override
    public String getStepName(String study, int step, String chksum)
            throws Exception
    {
        return enrollStatusDao.getStepName(study, step, chksum);
    }

    @Override
    public String getInstName(String inst) throws Exception
    {
        return enrollStatusDao.getInstName(inst);
    }

    @Override
    public int getSidDisplayType(String study, int step) throws Exception
    {
        return enrollStatusDao.getSidDisplayType(study, step);
    }

    @Override
    public int getPidDisplayType(String study, int step) throws Exception
    {
        return enrollStatusDao.getPidDisplayType(study, step);
    }

    @Override
    public Map<String, Boolean> getDisplayStatus(String study, int step)
            throws Exception
    {
        return enrollStatusDao.getDisplayStatus(study, step);
    }

    @Override
    public String getVersionName(String study, String version) throws Exception
    {
       return enrollStatusDao.getVersionName(study, version); 
    }

    @Override
    public List<String> getEnrollmentSummUsers(String inst, String study, int grpc)
            throws Exception
    {
        return enrollStatusDao.getEnrollmentSummUsers(inst, study, grpc);
    }

    @Override
    public String getFilePath(String type)
    {        
        return enrollStatusDao.getFilePath(type);
    }

    @Override
    public String getMailEncryptionType()
    {
        return enrollStatusDao.getMailEncryptionType();
    }

    @Override
    public int getGrpc(String inst, String study) throws Exception
    {
        return enrollStatusDao.getGrpc(inst, study);
    }
    
    
    

}
