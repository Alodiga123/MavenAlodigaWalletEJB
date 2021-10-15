package com.alodiga.wallet.ejb;

import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import org.apache.log4j.Logger;

import com.alodiga.wallet.common.ejb.AuditoryEJB;
import com.alodiga.wallet.common.ejb.AuditoryEJBLocal;
import com.alodiga.wallet.common.exception.EmptyListException;
import com.alodiga.wallet.common.exception.GeneralException;
import com.alodiga.wallet.common.exception.NoResultException;
import com.alodiga.wallet.common.exception.NullParameterException;
import com.alodiga.wallet.common.exception.RegisterNotFoundException;
import com.alodiga.wallet.common.genericEJB.AbstractWalletEJB;
import com.alodiga.wallet.common.genericEJB.EJBRequest;
import com.alodiga.wallet.common.genericEJB.WalletContextInterceptor;
import com.alodiga.wallet.common.genericEJB.WalletLoggerInterceptor;
import com.alodiga.wallet.common.model.Audit;
import com.alodiga.wallet.common.model.AuditAction;
import com.alodiga.wallet.common.model.Event;
import com.alodiga.wallet.common.utils.EjbConstants;
import com.alodiga.wallet.common.utils.EjbUtils;
import com.alodiga.wallet.common.utils.GeneralUtils;
import com.alodiga.wallet.common.utils.QueryConstants;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import javax.interceptor.Interceptors;
import javax.persistence.Query;

@Interceptors({WalletLoggerInterceptor.class, WalletContextInterceptor.class})
@Stateless(name = EjbConstants.AUDITORY_EJB, mappedName = EjbConstants.AUDITORY_EJB)
@TransactionManagement(TransactionManagementType.BEAN)
public class AuditoryEJBImp extends AbstractWalletEJB implements AuditoryEJB, AuditoryEJBLocal {

    private static final Logger logger = Logger.getLogger(AuditoryEJBImp.class);


    public List<Audit> getLastAudits(EJBRequest request) throws GeneralException, RegisterNotFoundException, NullParameterException, EmptyListException {
        List<Audit> audits = null;
        String nameQuery = null;
        nameQuery = QueryConstants.LOAD_LAST_AUDITS;
        audits = (List<Audit>) getNamedQueryResult(Audit.class, nameQuery, request, getMethodName(), logger, "audits");
        return audits;

    }

    public List<Audit> getAudits(EJBRequest request) throws GeneralException, RegisterNotFoundException, NullParameterException, EmptyListException {
        return (List<Audit>) listEntities(Audit.class, request, logger, getMethodName());
    }

    public Audit loadAudit(EJBRequest request) throws GeneralException, RegisterNotFoundException, NullParameterException {
        return (Audit) loadEntity(Audit.class, request, logger, getMethodName());
    }

    public Audit saveAudit(EJBRequest request) throws GeneralException, NullParameterException {
        return (Audit) saveEntity(request, logger, getMethodName());
    }

    public List<Audit> searchAudit(EJBRequest request) throws GeneralException, NullParameterException, EmptyListException {
        List<Audit> audits = new ArrayList<Audit>();
        Map<String, Object> params = request.getParams();
        String sql = "SELECT a FROM Audit a WHERE a.creationDate BETWEEN :date1 AND :date2";

        if (!params.containsKey(QueryConstants.PARAM_BEGINNING_DATE) || !params.containsKey(QueryConstants.PARAM_ENDING_DATE)) {
            throw new NullParameterException( sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "beginningDate & endingDate"), null);
        }
        if (params.containsKey(QueryConstants.PARAM_RESPONSIBLE_ID)) {
            sql += " AND a.responsibleId LIKE '%" + params.get(QueryConstants.PARAM_RESPONSIBLE_ID) + "%'";
        }
        if (params.containsKey(QueryConstants.PARAM_RESPONSIBLE_TYPE)) {
            sql += " AND a.responsibleType LIKE '%" + params.get(QueryConstants.PARAM_RESPONSIBLE_TYPE) + "%'";
        }
        if (params.containsKey(QueryConstants.PARAM_TABLE_NAME)) {
            sql += " AND a.tableName LIKE '%" + params.get(QueryConstants.PARAM_TABLE_NAME) + "%'";
        }
        if (params.containsKey(QueryConstants.PARAM_REMOTE_IP)) {
            sql += " AND a.remoteIp LIKE '%" + params.get(QueryConstants.PARAM_REMOTE_IP) + "%'";
        }
        if (params.containsKey(QueryConstants.PARAM_EVENT_ID)) {
            sql += " AND a.event.id =" + params.get(QueryConstants.PARAM_EVENT_ID);
        }
        Query query = null;
        try {
            query = createQuery(sql);
            query.setParameter("date1", EjbUtils.getBeginningDate((Date) params.get(QueryConstants.PARAM_BEGINNING_DATE)));
            query.setParameter("date2", EjbUtils.getEndingDate((Date) params.get(QueryConstants.PARAM_ENDING_DATE)));

            if (request.getLimit() != null && request.getLimit() > 0) {
                query.setMaxResults(request.getLimit());
            }
            audits = query.setHint("toplink.refresh", "true").getResultList();

        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (audits.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return audits;

    }
    public AuditAction deleteAuditAction(Long actionId) throws GeneralException, NullParameterException {
        return null;
    }
    
    public List<AuditAction> getAuditAction(EJBRequest request) throws GeneralException, RegisterNotFoundException, NullParameterException, EmptyListException {
        return (List<AuditAction>) listEntities(AuditAction.class, request, logger, getMethodName());
    }

    public List<AuditAction> getAuditActions(Date beginningDate, Date endingDate) throws GeneralException, RegisterNotFoundException, NullParameterException, EmptyListException {
        List<AuditAction> audits = new ArrayList<AuditAction>();
        if (beginningDate == null || beginningDate == null) {
            throw new NullParameterException( sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "beginningDate & endingDate"), null);
        }
        Query query = null;
        try {
            query = createQuery("SELECT a FROM AuditAction a WHERE a.date BETWEEN :date1 AND :date2");
            query.setParameter("date1", GeneralUtils.getBeginningDate(beginningDate));
            query.setParameter("date2", GeneralUtils.getEndingDate(endingDate));
            audits = query.setHint("toplink.refresh", "true").getResultList();
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (audits.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return audits;
    }


     public List<AuditAction> searchAuditAction(String login, String userName, Long permissionId, Date beginningDate, Date endingDate) throws GeneralException, NullParameterException, EmptyListException {
        List<AuditAction> audits = new ArrayList<AuditAction>();

        if (beginningDate == null || endingDate == null) {
            throw new NullParameterException( sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "beginningDate & endingDate"), null);
        }
        StringBuilder sqlBuilder = new StringBuilder("SELECT a FROM AuditAction a WHERE a.date BETWEEN :date1 AND :date2");

        if (login != null) {
            sqlBuilder.append(" AND a.user.login=").append(login);
        }
        if (userName != null && !userName.equals("")) {
            sqlBuilder.append(" AND a.user.firstName=").append(userName);
        }
        if (permissionId != null) {
            sqlBuilder.append(" AND a.permission.id=").append(permissionId);
        }
        Query query = null;
        try {
            
            query = createQuery(sqlBuilder.toString());
            query.setParameter("date1", GeneralUtils.getBeginningDate(beginningDate));
            query.setParameter("date2", GeneralUtils.getEndingDate(endingDate));
            audits = query.setHint("toplink.refresh", "true").getResultList();
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (audits.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return audits;
    }
     
     public List<AuditAction> searchAuditActions(EJBRequest request) throws GeneralException, NullParameterException, EmptyListException {
        List<AuditAction> audits = new ArrayList<AuditAction>();
        try {
            Map<String, Object> params = request.getParams();
            String pattern = "yyyy-MM-dd";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String strDate1 = (simpleDateFormat.format(EjbUtils.getBeginningDate((Date) params.get(QueryConstants.PARAM_BEGINNING_DATE))));
            String strDate2 = (simpleDateFormat.format(EjbUtils.getEndingDate((Date) params.get(QueryConstants.PARAM_ENDING_DATE))));
            
            StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM audit_action a WHERE a.userId =").append(params.get(QueryConstants.PARAM_USER_ID));
            sqlBuilder.append(" and a.date BETWEEN '");
            sqlBuilder.append(strDate1);
            sqlBuilder.append("' and '");
            sqlBuilder.append(strDate2);
            sqlBuilder.append("'");
            
            if (params.containsKey(QueryConstants.PARAM_PERMISSION_ID)) {
            sqlBuilder.append(" and a.permissionId = ").append(params.get(QueryConstants.PARAM_PERMISSION_ID));
            } 
            
            Query query = entityManager.createNativeQuery(sqlBuilder.toString(), AuditAction.class);
            audits = (List<AuditAction>) query.setHint("toplink.refresh", "true").getResultList();
        
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (audits.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return audits;
    }
     

    public AuditAction saveAuditAction(AuditAction action) throws GeneralException, NullParameterException {
        return (AuditAction) saveEntity(action);
    }

    public List<AuditAction> getAuditActionsByUserId(Long userId, Date beginningDate, Date endingDate) throws GeneralException, RegisterNotFoundException, NullParameterException, EmptyListException {
        List<AuditAction> audits = new ArrayList<AuditAction>();

        if (userId == null) {
            throw new NullParameterException( sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "userId"), null);
        } else if (beginningDate == null || beginningDate == null) {
            throw new NullParameterException( sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "beginningDate & endingDate"), null);
        }
        Query query = null;
        try {
            query = createQuery("SELECT a FROM AuditAction a WHERE a.date BETWEEN :date1 AND :date2 AND a.user.id=?1");
            query.setParameter("1", userId);
            query.setParameter("date1", GeneralUtils.getBeginningDate(beginningDate));
            query.setParameter("date2", GeneralUtils.getEndingDate(endingDate));
            audits = query.setHint("toplink.refresh", "true").getResultList();
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (audits.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return audits;
    }
      
    public Event loadEvent(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
    	Event event = (Event) loadEntity(Event.class, request, logger, getMethodName());
        return event;
    }


}
