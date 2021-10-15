package com.alodiga.wallet.ejb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.apache.log4j.Logger;

import com.alodiga.wallet.common.ejb.PreferencesEJB;
import com.alodiga.wallet.common.ejb.PreferencesEJBLocal;
import com.alodiga.wallet.common.exception.EmptyListException;
import com.alodiga.wallet.common.exception.GeneralException;
import com.alodiga.wallet.common.exception.NullParameterException;
import com.alodiga.wallet.common.exception.RegisterNotFoundException;
import com.alodiga.wallet.common.genericEJB.AbstractWalletEJB;
import com.alodiga.wallet.common.genericEJB.EJBRequest;
import com.alodiga.wallet.common.genericEJB.WalletContextInterceptor;
import com.alodiga.wallet.common.genericEJB.WalletLoggerInterceptor;
import com.alodiga.wallet.common.model.DocumentTypeEnum;
import com.alodiga.wallet.common.model.Preference;
import com.alodiga.wallet.common.model.PreferenceClassification;
import com.alodiga.wallet.common.model.PreferenceControl;
import com.alodiga.wallet.common.model.PreferenceField;
import com.alodiga.wallet.common.model.PreferenceType;
import com.alodiga.wallet.common.model.PreferenceValue;
import com.alodiga.wallet.common.model.PreferenceFieldData;
import com.alodiga.wallet.common.model.TransactionType;
import com.alodiga.wallet.common.utils.EjbConstants;
import com.alodiga.wallet.common.utils.QueryConstants;
import javax.persistence.NoResultException;

@Interceptors({WalletLoggerInterceptor.class, WalletContextInterceptor.class})
@Stateless(name = EjbConstants.PREFERENCES_EJB, mappedName = EjbConstants.PREFERENCES_EJB)
@TransactionManagement(TransactionManagementType.BEAN)
public class PreferencesEJBImp extends AbstractWalletEJB implements PreferencesEJB, PreferencesEJBLocal {

    private static final Logger logger = Logger.getLogger(PreferencesEJBImp.class);

    
    public PreferenceField deletePreferenceField(EJBRequest request) throws GeneralException, NullParameterException {
        return null;
    }

    
    public PreferenceType deletePreferenceType(EJBRequest request) throws GeneralException, NullParameterException {
        return null;
    }

    public PreferenceValue deletePreferenceValue(EJBRequest request) throws GeneralException, NullParameterException {
        return null;
    }

    private PreferenceValue getLastPreferenceValueByPreferenceField(Long preferenceFieldId, Long classificationId) throws GeneralException, NullParameterException, EmptyListException {

    	PreferenceValue preferenceValue = null;
        Query query = null;
        try {
        	 query = createQuery("SELECT p FROM PreferenceValue p WHERE p.preferenceFieldId.id=?1 and p.preferenceClassficationId.id= ?2 and p.productId is null and p.transactionTypeId is null and p.preferenceValueParentId is null and p.bussinessId is null");
             query.setParameter("1", preferenceFieldId);
             query.setParameter("2", classificationId);
             preferenceValue = (PreferenceValue) query.setHint("toplink.refresh", "true").getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return preferenceValue;
    }

    public List<Preference> getPreference(EJBRequest request) throws GeneralException, RegisterNotFoundException, NullParameterException, EmptyListException {
        return (List<Preference>) listEntities(Preference.class, request, logger, getMethodName());
    }
    
    @Override
    public Preference savePreference(Preference preference) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (preference== null) {
            throw new NullParameterException("preference", null);
        }
        return (Preference) saveEntity(preference);
    }


    public Map<Long, String> getLastPreferenceValues(EJBRequest request) throws GeneralException, RegisterNotFoundException, NullParameterException, EmptyListException {
        Map<Long, String> currentValues = new HashMap<Long, String>();
        Map<String, Object> params = request.getParams();
        List<PreferenceField> fields = this.getPreferenceFields(request);
        for (PreferenceField field : fields) {
            PreferenceValue pv = getLastPreferenceValueByPreferenceField(field.getId(), Long.valueOf("" + params.get("classificationId")));
            if (pv != null) {
                currentValues.put(field.getId(), pv.getValue());
            }
        }
        return currentValues;
    }

    public List<PreferenceField> getPreferenceFields(EJBRequest request) throws GeneralException, EmptyListException, NullParameterException {
        return (List<PreferenceField>) listEntities(PreferenceField.class, request, logger, getMethodName());
    }
    
    public List<PreferenceField> getPreferenceFieldsByCode(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<PreferenceField> preferenceFieldList = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_CODE)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_CODE), null);
        }       
        preferenceFieldList = (List<PreferenceField>) getNamedQueryResult(PreferenceField.class, QueryConstants.CODE_EXIST_IN_BD_PREFERENCE_FIELD, request, getMethodName(), logger, "preferenceFieldList");
        return preferenceFieldList;
    }
    
    public List<PreferenceField> searchPreferenceField(String name) throws EmptyListException, GeneralException, NullParameterException {
        List<PreferenceField> preferenceFieldList = null;
        if (name == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "name"), null);
        }
        try {
            StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM preference_field p ");
            sqlBuilder.append("WHERE p.preferenceId IN (SELECT p.id FROM preference p WHERE p.name LIKE '").append(name).append("%')");
            Query query = entityManager.createNativeQuery(sqlBuilder.toString(), PreferenceField.class);
            preferenceFieldList = query.setHint("toplink.refresh", "true").getResultList();

        } catch (NoResultException ex) {
            throw new EmptyListException("No distributions found");
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        return preferenceFieldList;
    }
    
    public List<PreferenceType> getPreferenceTypes(EJBRequest request) throws GeneralException, RegisterNotFoundException, NullParameterException, EmptyListException {
        return (List<PreferenceType>) listEntities(PreferenceType.class, request, logger, getMethodName());
    }

    
    public List<PreferenceValue> getPreferenceValues(EJBRequest request) throws GeneralException, RegisterNotFoundException, NullParameterException, EmptyListException {
        return (List<PreferenceValue>) listEntities(PreferenceValue.class, request, logger, getMethodName());
    }

    
    public List<PreferenceValue> getPreferenceValuesByClassificationIdAndFieldId(Long classificationId, Long fieldId) throws GeneralException, RegisterNotFoundException, NullParameterException, EmptyListException {
        List<PreferenceValue> preferenceValues = new ArrayList<PreferenceValue>();

        Query query = null;
        try {
            query = createQuery("SELECT p FROM PreferenceValue p WHERE p.preferenceField.id=?1 AND p.preferenceClassficationId.id= ?2");
            query.setParameter("1", fieldId);
            query.setParameter("2", classificationId);
            preferenceValues = query.setHint("toplink.refresh", "true").getResultList();

        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (preferenceValues.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return preferenceValues;
    }

    public PreferenceValue loadActivePreferenceValuesByClassificationIdAndFieldId(Long classificationId, Long fieldId) throws GeneralException, RegisterNotFoundException, NullParameterException {

        PreferenceValue preferenceValue = null;
        try {
            Query query = null;
            query = createQuery("SELECT p FROM PreferenceValue p WHERE p.preferenceFieldId.id=?1 and p.preferenceClassficationId.id= ?2 and p.productId is null and p.transactionTypeId is null and p.preferenceValueParentId is null and p.bussinessId is null");
            query.setParameter("1", fieldId);
            query.setParameter("2", classificationId);
            preferenceValue = (PreferenceValue) query.setHint("toplink.refresh", "true").getSingleResult();

        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (preferenceValue == null) {
            throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_REGISTER_NOT_FOUND_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return preferenceValue;
    }

    
    public PreferenceField loadPreferenceField(EJBRequest request) throws GeneralException, RegisterNotFoundException, NullParameterException {
        return (PreferenceField) loadEntity(PreferenceField.class, request, logger, getMethodName());
    }

    
    public PreferenceType loadPreferenceType(EJBRequest request) throws GeneralException, RegisterNotFoundException, NullParameterException {
        return (PreferenceType) loadEntity(PreferenceType.class, request, logger, getMethodName());
    }

    
    public PreferenceValue loadPreferenceValue(EJBRequest request) throws GeneralException, RegisterNotFoundException, NullParameterException {
        return (PreferenceValue) loadEntity(PreferenceValue.class, request, logger, getMethodName());
    }

    
    public PreferenceField savePreferenceField(EJBRequest request) throws GeneralException, NullParameterException {
        return (PreferenceField) saveEntity(request, logger, getMethodName());
    }

    
    public PreferenceType savePreferenceType(EJBRequest request) throws GeneralException, NullParameterException {
        return (PreferenceType) saveEntity(request, logger, getMethodName());
    }
    
    @Override
    public PreferenceType savePreferencesType(PreferenceType preferenceType) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (preferenceType== null) {
            throw new NullParameterException("preferenceType", null);
        }
        return (PreferenceType) saveEntity(preferenceType);
    }
    
    public List<PreferenceType> getPreferenceTypeByType(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<PreferenceType> preferenceType = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_TYPE)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_TYPE), null);
        }
        preferenceType = (List<PreferenceType>) getNamedQueryResult(PreferenceType.class, QueryConstants.PREFERENCE_TYPE_BY_TYPE, request, getMethodName(), logger, "preferenceType");
        return preferenceType;
    }
    
    public PreferenceValue savePreferenceValue(PreferenceValue preferenceValue) throws GeneralException, NullParameterException {
        if (preferenceValue == null) {
            throw new NullParameterException("preferenceValue", null);
        }
        return (PreferenceValue) saveEntity(preferenceValue);
    }

    
    public List<PreferenceValue> savePreferenceValues(List<PreferenceValue> preferenceValues,List<PreferenceControl> preferenceControls) throws GeneralException, NullParameterException {
        List<PreferenceValue> returnValues = new ArrayList<PreferenceValue>();
        for (PreferenceValue pv : preferenceValues) {
             returnValues.add((PreferenceValue) saveEntity(pv, logger, getMethodName())); //Guardo o Actualizo la preferencia
        }
        for (PreferenceControl pc : preferenceControls) {
            saveEntity(pc, logger, getMethodName()); //Guardo la auditoria del preference_value que se guardo o modifico
        }

        return returnValues;
    }
    
    public List<PreferenceClassification> getPreferenceClassifications(EJBRequest request) throws GeneralException, RegisterNotFoundException, NullParameterException, EmptyListException{
        return (List<PreferenceClassification>) listEntities(PreferenceClassification.class, request, logger, getMethodName());
    }
    
    public PreferenceClassification loadPreferenceClassification(EJBRequest request) throws GeneralException, RegisterNotFoundException, NullParameterException{
    	 return (PreferenceClassification) loadEntity(PreferenceClassification.class, request, logger, getMethodName());
    }
    
    public List<PreferenceValue> getPreferenceValuesGroupByBussinessId(EJBRequest request) throws GeneralException, NullParameterException, EmptyListException{
    	 List<PreferenceValue> preferenceValues = new ArrayList<PreferenceValue>();
    	 Map<String, Object> params = request.getParams();
         Query query = null;
         try {
            StringBuilder sqlBuilder = new StringBuilder("SELECT p FROM PreferenceValue p WHERE p.bussinessId is not null");
            if (params.containsKey(QueryConstants.PARAM_BUSSINESS_ID)) {
	            sqlBuilder.append(" AND p.bussinessId=").append(params.get(QueryConstants.PARAM_BUSSINESS_ID));
	        }
            if (params.containsKey(QueryConstants.PARAM_TRANSACTION_TYPE_ID)) {
 	            sqlBuilder.append(" AND p.transactionTypeId.id=").append(params.get(QueryConstants.PARAM_TRANSACTION_TYPE_ID));
 	        }
 	        if (params.containsKey(QueryConstants.PARAM_PRODUCT_ID)) {
 	            sqlBuilder.append(" AND p.productId.id=").append(params.get(QueryConstants.PARAM_PRODUCT_ID));
 	        }
 	        sqlBuilder.append(" GROUP BY p.productId.id,p.transactionTypeId.id, p.preferenceClassficationId.id,p.bussinessId");
 	        query = createQuery(sqlBuilder.toString());
 	        if (request.getLimit() != null && request.getLimit() > 0) {
               query.setMaxResults(request.getLimit());
            }
            preferenceValues = query.setHint("toplink.refresh", "true").getResultList();

         } catch (Exception e) {
             throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
         }
         if (preferenceValues.isEmpty()) {
             throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
         }
         return preferenceValues;
    }
    

    public List<TransactionType> getTransactionTypes(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<TransactionType>) listEntities(TransactionType.class, request, logger, getMethodName());
    }
    
    public TransactionType saveTransactionType(EJBRequest request) throws GeneralException, NullParameterException {
        return (TransactionType) saveEntity(request, logger, getMethodName());
    }
    
    public List<TransactionType> getTransactionTypeByCode(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<TransactionType> transactionTypeList = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_CODE)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_CODE), null);
        }       
        transactionTypeList = (List<TransactionType>) getNamedQueryResult(TransactionType.class, QueryConstants.CODE_EXIST_IN_BD_TRANSACTION_TYPE, request, getMethodName(), logger, "transactionTypeList");
        return transactionTypeList;
    }
    
    public List<TransactionType> searchTransactionType(String name) throws EmptyListException, GeneralException, NullParameterException {
        List<TransactionType> transactionTypeList = null;
        if (name == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "name"), null);
        }
        try {
            StringBuilder sqlBuilder = new StringBuilder("SELECT DISTINCT t FROM TransactionType t ");
            sqlBuilder.append("WHERE t.code LIKE '").append(name).append("%'");

            Query query = entityManager.createQuery(sqlBuilder.toString());
            transactionTypeList= query.setHint("toplink.refresh", "true").getResultList();

        } catch (NoResultException ex) {
            throw new EmptyListException("No distributions found");
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        return transactionTypeList;
    }
    
   
    public List<PreferenceValue> getPreferenceValuesByParam(Long classificationId, Long productId, Long transactionTypeId, Long bussinessId) throws GeneralException, NullParameterException, EmptyListException {
        List<PreferenceValue> preferenceValues = new ArrayList<PreferenceValue>();

        Query query = null;
        try {
            query = createQuery("SELECT p FROM PreferenceValue p WHERE p.preferenceClassficationId.id= ?1 AND p.productId.id= ?2 AND p.transactionTypeId.id= ?3 AND p.bussinessId= ?4");
            query.setParameter("1", classificationId);
            query.setParameter("2", productId);
            query.setParameter("3", transactionTypeId);
            query.setParameter("4", bussinessId);
            preferenceValues = query.setHint("toplink.refresh", "true").getResultList();

        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (preferenceValues.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return preferenceValues;
    }
    
    public List<PreferenceValue> getPreferenceValuesByClassificationIdAndBussinessId(Long classificationId, Long bussinessId) throws GeneralException, RegisterNotFoundException, NullParameterException, EmptyListException {
        List<PreferenceValue> preferenceValues = new ArrayList<PreferenceValue>();
        Query query = null;
        try {
            query = createQuery("SELECT p FROM PreferenceValue p WHERE p.preferenceClassficationId.id=?1 AND p.bussinessId=?2");
            query.setParameter("1", classificationId);
            query.setParameter("2", bussinessId);
            preferenceValues = query.setHint("toplink.refresh", "true").getResultList();

        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (preferenceValues.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return preferenceValues;
    }
    
    public boolean validatePreferencesValues(Long classificationId,Long productId,Long transactionTypeId, Long bussinessId)throws GeneralException, NullParameterException, EmptyListException {
    	if (bussinessId == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "bussinessId"), null);
        }
		boolean valid = false;
		List<PreferenceValue> preferenceValues = new ArrayList<PreferenceValue>();
		Query query = null;
		try {
			query = createQuery("SELECT p FROM PreferenceValue p WHERE p.preferenceClassficationId.id=?1 AND p.productId.id=?2 AND p.transactionTypeId.id=?3 AND p.bussinessId=?4");
			query.setParameter("1", classificationId);
			query.setParameter("2", productId);
			query.setParameter("3", transactionTypeId);
			query.setParameter("4", bussinessId);
			preferenceValues = query.setHint("toplink.refresh", "true").getResultList();
		} catch (Exception e) {
			throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(),	getMethodName(), e.getMessage()), null);
		}
		if (preferenceValues.isEmpty()) {
			valid = true;
		}
		return valid;
    }
    
    public Map<Long, String> getLastPreferenceValuesByBusiness(EJBRequest request) throws GeneralException, RegisterNotFoundException, NullParameterException, EmptyListException {
        Map<Long, String> currentValues = new HashMap<Long, String>();
        Map<String, Object> params = request.getParams();
        List<PreferenceField> fields = this.getPreferenceFields(request);
        for (PreferenceField field : fields) {
            PreferenceValue pv = getLastPreferenceValueByPreferenceFieldByBusiness(field.getId(), Long.valueOf("" + params.get("classificationId")), Long.valueOf("" + params.get("productId")), Long.valueOf("" + params.get("transactionTypeId")), Long.valueOf("" + params.get("bussinessId")));
            if (pv != null) {
                currentValues.put(field.getId(), pv.getValue());
            }
        }
        return currentValues;
    }
    
    private PreferenceValue getLastPreferenceValueByPreferenceFieldByBusiness(Long preferenceFieldId, Long classificationId, Long productId,Long transactionTypeId, Long bussinessId ) throws GeneralException, NullParameterException, EmptyListException {

    	PreferenceValue preferenceValue = null;
        Query query = null;
        try {
        	 query = createQuery("SELECT p FROM PreferenceValue p WHERE p.preferenceFieldId.id=?1 and p.preferenceClassficationId.id= ?2 and p.productId.id=?3 AND p.transactionTypeId.id=?4 AND p.bussinessId=?5");
             query.setParameter("1", preferenceFieldId);
             query.setParameter("2", classificationId);
             query.setParameter("3", productId);
             query.setParameter("4", transactionTypeId);
             query.setParameter("5", bussinessId);
           
             preferenceValue = (PreferenceValue) query.setHint("toplink.refresh", "true").getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return preferenceValue;
    }
    

    public List<PreferenceField> getPreferenceFieldsByPreferenceId(Long preferenceId) throws GeneralException, RegisterNotFoundException, NullParameterException, EmptyListException{
    	  List<PreferenceField> preferenceFields = new ArrayList<PreferenceField>();
          Query query = null;
          try {
              query = createQuery("SELECT p FROM PreferenceField p WHERE p.preferenceId.id=?1");
              query.setParameter("1", preferenceId);
              preferenceFields = query.setHint("toplink.refresh", "true").getResultList();

          } catch (Exception e) {
              throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
          }
          if (preferenceFields.isEmpty()) {
              throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
          }
          return preferenceFields;
    }


	@Override
	public PreferenceValue getPreferenceValuesByParamAndBussiness(Long classificationId, Long productId,Long transactionTypeId, Long bussinessId, Long preferenceFieldId) throws GeneralException, NullParameterException, RegisterNotFoundException {
		List<PreferenceValue> preferenceValues = new ArrayList<PreferenceValue>();

	        Query query = null;
	        try {
	            query = createQuery("SELECT p FROM PreferenceValue p WHERE p.preferenceClassficationId.id= ?1 AND p.productId.id= ?2 AND p.transactionTypeId.id= ?3 AND p.bussinessId= ?4 AND p.preferenceFieldId.id=?5");
	            query.setParameter("1", classificationId);
	            query.setParameter("2", productId);
	            query.setParameter("3", transactionTypeId);
	            query.setParameter("4", bussinessId);
	            query.setParameter("5", preferenceFieldId);
	            preferenceValues = query.setHint("toplink.refresh", "true").getResultList();

	        } catch (Exception e) {
	            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
	        }
	        
	        if (preferenceValues.isEmpty()) {
	        	 throw new RegisterNotFoundException("No PreferenceValue found");
	        }
	        return preferenceValues.get(0);
	}


    @Override
    public List<PreferenceFieldData> getPreferenceFieldDataByPreference(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<PreferenceFieldData> preferenceFieldDataList = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_PREFERENCE_FIELD_ID)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_PREFERENCE_FIELD_ID), null);
        }
        preferenceFieldDataList = (List<PreferenceFieldData>) getNamedQueryResult(PreferenceFieldData.class, QueryConstants.PREFERENCE_FIELD_DATA_BY_PREFERENCE, request, getMethodName(), logger, "preferenceFieldDataList");
        return preferenceFieldDataList;
    }
    
    public PreferenceFieldData savePreferenceFieldData(PreferenceFieldData preferenceFieldData) throws GeneralException, NullParameterException {
        if (preferenceFieldData== null) {
            throw new NullParameterException("preferenceFieldData", null);
        }
        return (PreferenceFieldData) saveEntity(preferenceFieldData);
    }

}
