package org.fstrf.randomization.rando.main;

import javax.sql.DataSource;

import org.fstrf.randomization.rando.service.EnrollmentCriteriaService;
import org.fstrf.randomization.rando.service.impl.EnrollmentCriteriaServiceImpl;
import org.fstrf.randomization.rando.service.impl.OutputServiceImpl;
import org.fstrf.randomization.rando.service.impl.InputServiceImpl;
import org.fstrf.randomization.rando.util.Constants;
import org.fstrf.randomization.rando.util.DataSourceUtil;
import org.fstrf.randomization.rando.util.ErrorHandler;
import org.fstrf.randomization.rando.util.LogUtil;
import org.fstrf.randomization.rando.util.Utility;
import org.slf4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/*
   This class is actually quite big. I have trimmed it and only included some main features.
*/

public class Enrollment {
	
    private static final Logger progLog = (Logger) LogUtil.getLogger("progLogger");
    private static final Logger rcLog = (Logger) LogUtil.getLogger("rcLogger");

    private DataSourceTransactionManager transactionManager;
    private TransactionTemplate transactionTemplate;
    private JdbcTemplate jdbcTemplate;
    
    public synchronized EnrollResponse enroll(final PatientDataWrapper patientData, final DataSource dataSource) {
        
        transactionManager = new DataSourceTransactionManager(dataSource);
        jdbcTemplate = new JdbcTemplate(transactionManager.getDataSource());
        transactionTemplate = new TransactionTemplate(transactionManager);
        
        final EnrollmentContainer enrollContainer = new EnrollmentContainer();
        final EnrollResponse enrollResponse = new EnrollResponse();

        transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                enrollContainer.setDatasource(dataSource);
                enrollContainer.setJdbcTemplate(jdbcTemplate);   
                enrollContainer.setErrorHandler(new ErrorHandler(jdbcTemplate));
                
                    try { 
                        
                        InputServiceImpl inputService = new InputServiceImpl(enrollContainer, patientData.getRegistrationData());
                        
                        if(inputService.checkInput()){
                            progLog.info("Input Module Successful");
                        } else {
                            Utility.setResponse(Constants.ENROLL_STATUS_FAIL, enrollContainer, enrollResponse);
                            status.isRollbackOnly();
                            return enrollResponse;
                        }
                        
                        EnrollmentCriteriaService enrollCriteriaService = new EnrollmentCriteriaServiceImpl(enrollContainer);
                        
                        // Check if pid needs to be generated. If pid is entered by user then check the pid
                        boolean isSuccessGeneratePid = enrollCriteriaService.generatePid();
                        if(!isSuccessGeneratePid) {                 
                            Utility.setResponse(Constants.ENROLL_STATUS_FAIL, enrollContainer, enrollResponse);
                            status.isRollbackOnly();
                            return enrollResponse;
                        }
                        
                        
                        // Is study-step open to accrual 
                        boolean isSuccessStudyAccrual = enrollCriteriaService.isStudyAccrualOpen();
                        if(!isSuccessStudyAccrual) {
                            Utility.setResponse(Constants.ENROLL_STATUS_FAIL, enrollContainer, enrollResponse); 
                            status.isRollbackOnly();
                            return enrollResponse;
                        }
                        
                        // rando - update_pinfo
                        boolean updatePinfo = enrollCriteriaService.updatePinfo();
                        if(!updatePinfo) {
                            Utility.setResponse(Constants.ENROLL_STATUS_FAIL, enrollContainer, enrollResponse);
                            status.isRollbackOnly();
                            return enrollResponse;
                        }   
                      
                        
                        OutputServiceImpl outputService = new OutputServiceImpl(enrollContainer);
                        
                        boolean outputModule = outputService.checkOutput();
                        if(!outputModule){
                            Utility.setResponse(Constants.ENROLL_STATUS_FAIL, enrollContainer, enrollResponse);
                            status.isRollbackOnly();
                            return enrollResponse;
                        }
                        
                        String message = outputService.successMessage(enrollContainer);
                        if(message == null){
                            Utility.setResponse(Constants.ENROLL_STATUS_FAIL, enrollContainer, enrollResponse);
                            status.isRollbackOnly();
                            return enrollResponse;
                        }
                        
                        //If it reaches this point then all tables have been loaded and enrollment is successful  
                        enrollResponse.setStatus(Constants.ENROLL_STATUS_SUCCESS);
                        enrollResponse.setMessage(message);
                    //    Utility.setResponse(Constants.ENROLL_STATUS_SUCCESS, enrollContainer, enrollResponse);
                        return enrollResponse;
                       
                } catch (RandoException re) {
                   
                    int errCode = re.getErrCode();
                    String sql = re.getSqlQuery();
                    String context = re.getContext();
                    enrollContainer.getErrorHandler().handleError(errCode, enrollContainer.getStudy().getStudy(), enrollContainer.getStudy().getStep().getStepNo(),context,sql,re);
                    rcLog.error("Error in enrollment. "+context+" Code: "+errCode +" Sql: "+sql);
                    progLog.error("Error in enrollment", re);
                    Utility.setResponse(Constants.ENROLL_STATUS_FAIL, enrollContainer, enrollResponse);
                    status.isRollbackOnly();
                    return enrollResponse;
                } catch (Exception e) {
                    Utility.setResponse(Constants.ENROLL_STATUS_FAIL, enrollContainer, enrollResponse);
                    status.isRollbackOnly();
                    return enrollResponse;
                }
            } 
        }); 
		
        return enrollResponse;       
    }
}
