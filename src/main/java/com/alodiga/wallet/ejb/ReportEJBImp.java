package com.alodiga.wallet.ejb;

import java.util.List;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import org.apache.log4j.Logger;
import com.alodiga.wallet.common.ejb.ReportEJB;
import com.alodiga.wallet.common.ejb.ReportEJBLocal;
import com.alodiga.wallet.common.exception.EmptyListException;
import com.alodiga.wallet.common.exception.GeneralException;
import com.alodiga.wallet.common.exception.NullParameterException;
import com.alodiga.wallet.common.exception.RegisterNotFoundException;
import com.alodiga.wallet.common.genericEJB.AbstractWalletEJB;
import com.alodiga.wallet.common.genericEJB.EJBRequest;
import com.alodiga.wallet.common.genericEJB.WalletContextInterceptor;
import com.alodiga.wallet.common.genericEJB.WalletLoggerInterceptor;
import com.alodiga.wallet.common.model.ParameterType;
import com.alodiga.wallet.common.model.Report;
import com.alodiga.wallet.common.model.ReportHasProfile;
import com.alodiga.wallet.common.model.ReportParameter;
import com.alodiga.wallet.common.model.ReportType;
import com.alodiga.wallet.common.model.Transaction;
import com.alodiga.wallet.common.model.User;
import com.alodiga.wallet.common.utils.EjbConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.Query;
import com.alodiga.wallet.common.utils.QueryConstants;
import javax.persistence.NoResultException;

@Interceptors({WalletLoggerInterceptor.class, WalletContextInterceptor.class})
@Stateless(name = EjbConstants.REPORT_EJB, mappedName = EjbConstants.REPORT_EJB)
@TransactionManagement(TransactionManagementType.BEAN)
public class ReportEJBImp extends AbstractWalletEJB implements ReportEJB, ReportEJBLocal {
    private static final Logger logger = Logger.getLogger(ReportEJBImp.class);
    
    //Report
    public void deleteProfileReports(EJBRequest request) throws NullParameterException, GeneralException {
        Object param = request.getParam();
        if (param == null || !(param instanceof Long)) {
            throw new NullParameterException( sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "profileId"), null);
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("reportId", (Long) param);
        try {
            executeNameQuery(ReportHasProfile.class, QueryConstants.DELETE_REPORT_PROFILE, map, getMethodName(), logger, "ReportProfile", null, null);
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
    }

    public void deleteReportParameter(EJBRequest request) throws NullParameterException, GeneralException {
        Object param = request.getParam();
        if (param == null || !(param instanceof Long)) {
            throw new NullParameterException( sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "reportId"), null);
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("reportId", (Long) param);
        try {
            executeNameQuery(ReportParameter.class, QueryConstants.DELETE_REPORT_PARAMETER, map, getMethodName(), logger, "ReportParameter", null, null);
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
    }

    public Report enableProduct(EJBRequest request) throws GeneralException, NullParameterException, RegisterNotFoundException {
        return (Report) saveEntity(request, logger, getMethodName());
    }

    public List<ParameterType> getParameterType(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<ParameterType> parameterType = (List<ParameterType>) listEntities(ParameterType.class, request, logger, getMethodName());
        return parameterType;
    }

    public List<Report> getReport(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<Report> reports = (List<Report>) listEntities(Report.class, request, logger, getMethodName());

        return reports;
    }

    public List<ReportHasProfile> getReportByProfile(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<ReportHasProfile> reportHasProfiles = null;
        Map<String, Object> params = request.getParams();

        if (!params.containsKey("profileId")) {
            throw new NullParameterException( sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "profileId"), null);
        }
        reportHasProfiles = (List<ReportHasProfile>) getNamedQueryResult(ReportEJB.class, QueryConstants.REPORT_BY_PROFILE, request, getMethodName(), logger, "reports");

        return reportHasProfiles;
    }

    public List<Report> getReportByReportTypeId(Long reportTypeId, User currentUser) throws NullParameterException, GeneralException, EmptyListException {
        Long profileId = currentUser.getCurrentProfile().getId();
        if (reportTypeId == null) {
            throw new NullParameterException( sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "reportTypeId"), null);
        } else if (currentUser == null) {
            throw new NullParameterException( sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "currentUser"), null);
        }
        List<Report> reports = new ArrayList<Report>();
        String sql = "SELECT rhp.reportId FROM ReportHasProfile rhp WHERE rhp.profileId.id= ?1 AND rhp.reportId.reportTypeId.id= ?2";
        try {
            Query query = createQuery(sql);
            query.setParameter("1", profileId);
            query.setParameter("2", reportTypeId);
            reports = query.setHint("toplink.refresh", "true").getResultList();
        } catch (Exception ex) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), ex.getMessage()), null);
        }
        if (reports.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return reports;
    }

    public List<ReportParameter> getReportParameter(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<ReportParameter> reportParameters = (List<ReportParameter>) listEntities(ReportParameter.class, request, logger, getMethodName());

        return reportParameters;
    }

    public List<ReportType> getReportTypes(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<ReportType> reportTypes = (List<ReportType>) listEntities(ReportType.class, request, logger, getMethodName());
        return reportTypes;
    }

    public ParameterType loadParameterType(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        ParameterType parameterType = (ParameterType) loadEntity(ParameterType.class, request, logger, getMethodName());

        return parameterType;
    }

    public Report loadReport(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        Report report = (Report) loadEntity(Report.class, request, logger, getMethodName());

        return report;
    }

    public ReportParameter loadReportParameter(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        ReportParameter reportParameter = (ReportParameter) loadEntity(ReportParameter.class, request, logger, getMethodName());
        return reportParameter;
    }

    public List<String> runReport(EJBRequest request) throws NullParameterException, GeneralException, EmptyListException {
        List<String> reports = new ArrayList<String>();

        Map<String, Object> params = request.getParams();
        String sql = (String) params.get(QueryConstants.PARAM_SQL);
        if (!params.containsKey(QueryConstants.PARAM_SQL)) {
            throw new NullParameterException( sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), QueryConstants.PARAM_PROFILE_ID), null);
        }
        try {
            reports = entityManager.createNativeQuery(sql).setHint("toplink.refresh", "true").getResultList();
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (reports.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }

        return reports;
    }

    public Report saveReport(EJBRequest request) throws NullParameterException, GeneralException {
        return (Report) saveEntity(request, logger, getMethodName());
    }
    
    public List<Report> searchReport(String name) throws EmptyListException, GeneralException, NullParameterException {
        List<Report> reportList = null;
        if (name == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "name"), null);
        }
        try {
            StringBuilder sqlBuilder = new StringBuilder("SELECT DISTINCT r FROM Report r ");
            sqlBuilder.append("WHERE r.name LIKE '").append(name).append("%'");

            Query query = entityManager.createQuery(sqlBuilder.toString());
            reportList = query.setHint("toplink.refresh", "true").getResultList();

        } catch (NoResultException ex) {
            throw new EmptyListException("No distributions found");
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        return reportList;
    }
}
