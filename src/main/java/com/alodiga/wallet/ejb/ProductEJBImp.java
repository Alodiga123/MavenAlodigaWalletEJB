package com.alodiga.wallet.ejb;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import org.apache.log4j.Logger;
import com.alodiga.wallet.common.ejb.ProductEJB;
import com.alodiga.wallet.common.ejb.ProductEJBLocal;
import com.alodiga.wallet.common.ejb.UtilsEJB;
import com.alodiga.wallet.common.ejb.UtilsEJBLocal;
import com.alodiga.wallet.common.enumeraciones.DocumentTypeE;
import com.alodiga.wallet.common.exception.EmptyListException;
import com.alodiga.wallet.common.exception.GeneralException;
import com.alodiga.wallet.common.exception.NegativeBalanceException;
import com.alodiga.wallet.common.exception.NullParameterException;
import com.alodiga.wallet.common.exception.RegisterNotFoundException;
import com.alodiga.wallet.common.genericEJB.AbstractWalletEJB;
import com.alodiga.wallet.common.genericEJB.EJBRequest;
import com.alodiga.wallet.common.genericEJB.WalletContextInterceptor;
import com.alodiga.wallet.common.genericEJB.WalletLoggerInterceptor;
import com.alodiga.wallet.common.model.BalanceHistory;
import com.alodiga.wallet.common.model.BankHasProduct;
import com.alodiga.wallet.common.model.Category;
import com.alodiga.wallet.common.model.CommissionItem;
import com.alodiga.wallet.common.model.DocumentTypeEnum;
import com.alodiga.wallet.common.model.Period;
import com.alodiga.wallet.common.model.Product;
import com.alodiga.wallet.common.model.ProductData;
import com.alodiga.wallet.common.model.Provider;
import com.alodiga.wallet.common.model.StatusTransactionApproveRequest;
import com.alodiga.wallet.common.enumeraciones.StatusTransactionApproveRequestE;
import com.alodiga.wallet.common.enumeraciones.TransactionSourceE;
import com.alodiga.wallet.common.enumeraciones.TransactionTypeE;
import com.alodiga.wallet.common.model.TransactionApproveRequest;
import com.alodiga.wallet.common.utils.Constants;
import com.alodiga.wallet.common.utils.EJBServiceLocator;
import com.alodiga.wallet.common.utils.EjbConstants;
import com.alodiga.wallet.common.utils.EjbUtils;
import com.alodiga.wallet.common.utils.QueryConstants;
import com.alodiga.wallet.common.utils.SendMailTherad;
import com.alodiga.wallet.common.utils.SendSmsThread;

@Interceptors({WalletLoggerInterceptor.class, WalletContextInterceptor.class})
@Stateless(name = EjbConstants.PRODUCT_EJB, mappedName = EjbConstants.PRODUCT_EJB)
@TransactionManagement(TransactionManagementType.BEAN)
public class ProductEJBImp extends AbstractWalletEJB implements ProductEJB, ProductEJBLocal {

    private static final Logger logger = Logger.getLogger(ProductEJBImp.class);
    @EJB
    private UtilsEJBLocal utilsEJB;
    private ProductEJB productEJB;

    //Category
    public List<Category> getCategories(EJBRequest request) throws GeneralException, EmptyListException, NullParameterException {
        return (List<Category>) listEntities(Category.class, request, logger, getMethodName());
    }

    public Category deleteCategory(EJBRequest request) throws GeneralException, NullParameterException {
        return null;
    }

    public Category loadCategory(EJBRequest request) throws GeneralException, RegisterNotFoundException, NullParameterException {

        return (Category) loadEntity(Category.class, request, logger, getMethodName());
    }

    public Category saveCategory(EJBRequest request) throws GeneralException, NullParameterException {
        return (Category) saveEntity(request, logger, getMethodName());
    }

    //Product
    public List<Product> getProducts(EJBRequest request) throws GeneralException, EmptyListException, NullParameterException {
        return (List<Product>) listEntities(Product.class, request, logger, getMethodName());
    }

    public List<Product> filterProducts(EJBRequest request) throws GeneralException, EmptyListException, NullParameterException {
        if (request == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "request"), null);
        }

        Map<String, Object> params = request.getParams();
        if (params == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "params"), null);
        }
        Boolean isFilter = true;
        Map orderField = new HashMap();
        orderField.put(Product.NAME, QueryConstants.ORDER_DESC);
        return (List<Product>) createSearchQuery(Product.class, request, orderField, logger, getMethodName(), "customers", isFilter);
    }

    public List<Product> getProductsByEnterprise(Long enterpriseId) throws GeneralException, EmptyListException, NullParameterException {
        List<Product> products = null;

        if (enterpriseId == null || enterpriseId.equals("")) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "enterpriseId"), null);
        }
        //
        Query query = null;
        try {
            query = createQuery("SELECT p FROM Product p WHERE p.enterprise.id = ?1");
            query.setParameter("1", enterpriseId);
            products = query.setHint("toplink.refresh", "true").getResultList();
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (products.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return products;
    }

    public Product loadProduct(EJBRequest request) throws GeneralException, RegisterNotFoundException, NullParameterException {
        return (Product) loadEntity(Product.class, request, logger, getMethodName());
    }

    public Product loadProductById(Long productId) throws GeneralException, RegisterNotFoundException, NullParameterException {
        if (productId == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "productId"), null);
        }
        Product product = new Product();
        try {
            Query query = createQuery("SELECT p FROM Product p WHERE p.id = ?1");
            query.setParameter("1", productId);
            product = (Product) query.getSingleResult();
        } catch (Exception ex) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), ex.getMessage()), null);
        }
        if (product == null) {
            throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return product;
    }

    public Product enableProduct(EJBRequest request) throws GeneralException, NullParameterException, RegisterNotFoundException {
        return (Product) saveEntity(request, logger, getMethodName());
    }

    public Product deleteProduct(EJBRequest request) throws GeneralException, NullParameterException {
        return null;
    }

    public void deleteProductHasProvider(Long productId) throws NullParameterException, GeneralException {
        if (productId == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "productId or providerId"), null);
        }
        try {
            EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();
            StringBuilder sqlBuilder = new StringBuilder("DELETE FROM ProductHasProvider php WHERE php.product.id=?1");
            Query query = createQuery(sqlBuilder.toString());
            query.setParameter("1", productId);
            query.executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
    }

    public Product saveProduct(EJBRequest request) throws GeneralException, NullParameterException {
        return (Product) saveEntity(request, logger, getMethodName());
    }

    //promotion
    public void deletePromotionTypeHasPromotion(EJBRequest request) throws NullParameterException, GeneralException {
        Object param = request.getParam();
        if (param == null || !(param instanceof Long)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "promotionId"), null);
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("promotionId", (Long) param);
        try {
            //executeNameQuery(PromotionTypeHasPromotion.class, QueryConstants.DELETE_PROMOTION_TYPE_HAS_PROMOTION, map, getMethodName(), logger, "PromotionTypeHasPromotion", null, null);
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
    }

    //provider
    public Provider deleteProvider(EJBRequest request) throws GeneralException, NullParameterException {
        return null;
    }

    public List<Provider> getProviders(EJBRequest request) throws GeneralException, EmptyListException, NullParameterException {

        return (List<Provider>) listEntities(Provider.class, request, logger, getMethodName());
    }

    public List<Provider> getSMSProviders(EJBRequest request) throws GeneralException, EmptyListException, NullParameterException {

        List<Provider> providers = null;
        //
        Query query = null;
        try {
            query = createQuery("SELECT p FROM Provider p WHERE p.isSMSProvider=1 AND p.enabled=1");

            if (request.getLimit() != null && request.getLimit() > 0) {
                query.setMaxResults(request.getLimit());
            } else {
                query.setMaxResults(20);
            }

            providers = query.setHint("toplink.refresh", "true").getResultList();

        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (providers.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return providers;
    }

    public Provider loadProvider(EJBRequest request) throws GeneralException, RegisterNotFoundException, NullParameterException {

        return (Provider) loadEntity(Provider.class, request, logger, getMethodName());
    }

    public Provider saveProvider(EJBRequest request) throws GeneralException, NullParameterException {

        return (Provider) saveEntity(request, logger, getMethodName());
    }

    //PinFree
    public Boolean deletePinFree(EJBRequest request) throws GeneralException, NullParameterException {
        return (Boolean) removeEntity(request, logger, getMethodName());
    }

    //
    public Float getPercentDiscount(Long levelId, Long productId) throws GeneralException, NullParameterException {
        if (levelId == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "levelId"), null);
        }
        if (productId == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "productId"), null);
        }
        Float discount = 0F;
        try {
            Query query = createQuery("SELECT lhp FROM LevelHasProduct lhp WHERE lhp.endingDate IS NULL AND lhp.level.id=?1 AND lhp.product.id=?2 ");
            query.setParameter("1", levelId);
            query.setParameter("2", productId);
//            discount = ((LevelHasProduct) query.getSingleResult()).getDiscountPercent();
        } catch (Exception ex) {
            ex.getMessage();
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), ex.getMessage()), null);
        }
        return discount;
    }

    //ProductData
    public ProductData saveProductData(EJBRequest request) throws GeneralException, NullParameterException {
        return (ProductData) saveEntity(request, logger, getMethodName());
    }

    //Period
    public List<Period> getPeriods(EJBRequest request) throws GeneralException, EmptyListException, NullParameterException {
        return (List<Period>) listEntities(Period.class, request, logger, getMethodName());
    }

    //BankHasProduct
    @Override
    public List<BankHasProduct> getBankHasProduct(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<BankHasProduct> bankHasProductList = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_PRODUCT_ID)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_PRODUCT_ID), null);
        }
        bankHasProductList = (List<BankHasProduct>) getNamedQueryResult(BankHasProduct.class, QueryConstants.BANK_BY_PRODUCT, request, getMethodName(), logger, "bankHasProductList");
        return bankHasProductList;
    }

    @Override
    public BankHasProduct saveBankHasProduct(BankHasProduct bankHasProduct) throws RegisterNotFoundException, NullParameterException, GeneralException {
        BankHasProduct _bankHasProduct = null;
        if (bankHasProduct == null) {
            throw new NullParameterException("bankHasProduct", null);
        }

        _bankHasProduct = (BankHasProduct) saveEntity(bankHasProduct, logger, getMethodName());

        return _bankHasProduct;
    }

    @Override
    public List<BankHasProduct> getBankHasProductByID(BankHasProduct bankHasProduct) throws GeneralException, EmptyListException, NullParameterException {
        List<BankHasProduct> bankHasProductList = null;
        try {
            if (bankHasProduct == null) {
                throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "bankHasProduct"), null);
            }  

            StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM bank_has_product where productId=");
            sqlBuilder.append(bankHasProduct.getProductId().getId());
            sqlBuilder.append(" and bankId=");
            sqlBuilder.append(bankHasProduct.getBankId().getId());
            Query query = entityManager.createNativeQuery(sqlBuilder.toString(), BankHasProduct.class);
            bankHasProductList = (List<BankHasProduct>) query.setHint("toplink.refresh", "true").getResultList();

        } catch (Exception ex) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), ex.getMessage()), ex);
        }
        return bankHasProductList;
    }

    @Override
    public List<TransactionApproveRequest> getTransactionApproveRequestByParams(EJBRequest request) throws GeneralException, NullParameterException, EmptyListException {
        List<TransactionApproveRequest> operations = new ArrayList<TransactionApproveRequest>();
        List<TransactionApproveRequest> results = new ArrayList<TransactionApproveRequest>();
        TransactionApproveRequest transactionApproveRequestParam = null;
        Map<String, Object> params = request.getParams();
            StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM transaction_approve_request t WHERE t.createDate BETWEEN ?1 AND ?2 and t.requestNumber like '%"+DocumentTypeEnum.MRAR.getDocumentType()+"%'");
            if (!params.containsKey(QueryConstants.PARAM_BEGINNING_DATE) || !params.containsKey(QueryConstants.PARAM_ENDING_DATE)) {
                throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "beginningDate & endingDate"), null);
            }
            if (params.containsKey(QueryConstants.PARAM_STATUS_TRANSACTION_APPROVE_REQUEST_ID)) {
                sqlBuilder.append(" AND t.statusTransactionApproveRequestId=").append(params.get(QueryConstants.PARAM_STATUS_TRANSACTION_APPROVE_REQUEST_ID));
            }
            if (params.containsKey(QueryConstants.PARAM_PRODUCT_ID)) {
                sqlBuilder.append(" AND t.productId=").append(params.get(QueryConstants.PARAM_PRODUCT_ID));
            }
            if (params.containsKey(QueryConstants.PARAM_REQUEST_NUMBER)) {
                sqlBuilder.append(" AND t.requestNumber='").append(params.get(QueryConstants.PARAM_REQUEST_NUMBER)).append("'");
            }
            if (params.containsKey(QueryConstants.PARAM_TRANSACTION_APPROVE_REQUEST)) {
                transactionApproveRequestParam = (TransactionApproveRequest) params.get(QueryConstants.PARAM_TRANSACTION_APPROVE_REQUEST);
            }            
            Query query = null;
            try {
                System.out.println("query:********"+sqlBuilder.toString());
                query = entityManager.createNativeQuery(sqlBuilder.toString(), TransactionApproveRequest.class);
                query.setParameter("1", EjbUtils.getBeginningDate((Date) params.get(QueryConstants.PARAM_BEGINNING_DATE)));
                query.setParameter("2", EjbUtils.getEndingDate((Date) params.get(QueryConstants.PARAM_ENDING_DATE)));
                if (request.getLimit() != null && request.getLimit() > 0) {
                    query.setMaxResults(request.getLimit());
                }
                results = (List<TransactionApproveRequest>) query.setHint("toplink.refresh", "true").getResultList();
                for (TransactionApproveRequest transactionApproveRequest : results) {
                    if (transactionApproveRequestParam != null && transactionApproveRequestParam.getId().longValue() == transactionApproveRequest.getId().longValue()) {
                        operations.add(transactionApproveRequestParam);
                    } else {
                        operations.add(transactionApproveRequest);
                    }                    
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
            }
            if (operations.isEmpty()) {
                throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
            }
            return operations;
    }
    

    public List<TransactionApproveRequest> searchTransactionApproveRequestByParamsMWAR(EJBRequest request) throws GeneralException, NullParameterException, EmptyListException {
        List<TransactionApproveRequest> operations = new ArrayList<TransactionApproveRequest>();

        Map<String, Object> params = request.getParams();

	        StringBuilder sqlBuilder = new StringBuilder("SELECT t FROM TransactionApproveRequest t WHERE t.createDate BETWEEN ?1 AND ?2 and t.requestNumber like '%"+DocumentTypeE.MWAR.getDocumentTypeAcronym()+"%'");
	        if (!params.containsKey(QueryConstants.PARAM_BEGINNING_DATE) || !params.containsKey(QueryConstants.PARAM_ENDING_DATE)) {
	            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "beginningDate & endingDate"), null);
	        }
	        if (params.containsKey(QueryConstants.PARAM_STATUS_TRANSACTION_APPROVE_REQUEST_ID)) {
	            sqlBuilder.append(" AND t.statusTransactionApproveRequestId.id=").append(params.get(QueryConstants.PARAM_STATUS_TRANSACTION_APPROVE_REQUEST_ID));
	        }
	        if (params.containsKey(QueryConstants.PARAM_PRODUCT_ID)) {
	            sqlBuilder.append(" AND t.productId.id=").append(params.get(QueryConstants.PARAM_PRODUCT_ID));
	        }
	        if (params.containsKey(QueryConstants.PARAM_REQUEST_NUMBER)) {
	            sqlBuilder.append(" AND t.requestNumber='").append(params.get(QueryConstants.PARAM_REQUEST_NUMBER)).append("'");
	        }
	        Query query = null;
	        try {
	            System.out.println("query:********"+sqlBuilder.toString());
	            query = createQuery(sqlBuilder.toString());
	            query.setParameter("1", EjbUtils.getBeginningDate((Date) params.get(QueryConstants.PARAM_BEGINNING_DATE)));
	            query.setParameter("2", EjbUtils.getEndingDate((Date) params.get(QueryConstants.PARAM_ENDING_DATE)));
	            if (request.getLimit() != null && request.getLimit() > 0) {
	                query.setMaxResults(request.getLimit());
	            }
	            operations = query.setHint("toplink.refresh", "true").getResultList();
	        } catch (Exception e) {
	            e.printStackTrace();
	            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
	        }
	        if (operations.isEmpty()) {
	            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
	        }
	        return operations;
	}


    @Override
    public TransactionApproveRequest loadTransactionApproveRequest(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        TransactionApproveRequest transactionApproveRequest = (TransactionApproveRequest) loadEntity(TransactionApproveRequest.class, request, logger, getMethodName());
        return transactionApproveRequest;
    }

    @Override
    public TransactionApproveRequest saveTransactionApproveRequest(TransactionApproveRequest transactionApproveRequest) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (transactionApproveRequest == null) {
            throw new NullParameterException("transactionApproveRequest", null);
        }
        return (TransactionApproveRequest) saveEntity(transactionApproveRequest);
    }

    @Override
    public List<StatusTransactionApproveRequest> getStatusTransactionApproveRequests(EJBRequest request) throws GeneralException, EmptyListException, NullParameterException {
        List<StatusTransactionApproveRequest> statusTransactionApproveRequests = (List<StatusTransactionApproveRequest>) listEntities(StatusTransactionApproveRequest.class, request, logger, getMethodName());
        return statusTransactionApproveRequests;
    }

    @Override
    public StatusTransactionApproveRequest loadStatusTransactionApproveRequestbyCode(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        List<StatusTransactionApproveRequest> statuses = null;
        Map<String, Object> params = request.getParams();

        if (!params.containsKey(QueryConstants.PARAM_CODE)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), QueryConstants.PARAM_CODE), null);
        }

        try {
            statuses = (List<StatusTransactionApproveRequest>) getNamedQueryResult(StatusTransactionApproveRequest.class, QueryConstants.CODE_BY_STATUS, request, getMethodName(), logger, "User");
        } catch (EmptyListException e) {
            throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName(), "user"), null);
        }

        return statuses.get(0);
    }

    @Override
    public TransactionApproveRequest updateTransactionApproveRequest(TransactionApproveRequest transactionApproveRequest, String movilPhone) throws RegisterNotFoundException, NullParameterException, GeneralException, NegativeBalanceException {
        BalanceHistory balancehistory = null;
        Float totalTransactionAmount;
        productEJB = (ProductEJB) EJBServiceLocator.getInstance().get(EjbConstants.PRODUCT_EJB);
        if (transactionApproveRequest == null) {
            throw new NullParameterException("transactionApproveRequest", null);
        } 
        if (transactionApproveRequest.getIndApproveRequest()) {
                try {
                    List<CommissionItem> commissionItems = utilsEJB.getCommissionItems(transactionApproveRequest.getTransactionId().getId());
                    if (!commissionItems.isEmpty()) {
                        totalTransactionAmount = calculateTotalTransactionAmount(commissionItems.get(0),transactionApproveRequest.getTransactionId().getAmount());
                        transactionApproveRequest = saveTransactionApproveRequest(transactionApproveRequest);
                        if (transactionApproveRequest.getTransactionId().getTransactionTypeId().getCode().equals(TransactionTypeE.MANREC.getTransactionTypeCode())) {
                            if (transactionApproveRequest.getTransactionId().getTransactionSourceId().getCode().equals(TransactionSourceE.APPBIL.getTransactionSourceCode())) {
                                balancehistory = createBalanceHistory(transactionApproveRequest.getUnifiedRegistryUserId().longValue(),null,transactionApproveRequest.getProductId(),totalTransactionAmount,transactionApproveRequest.getTransactionId().getTransactionTypeId().getId().intValue());
                            } else {
                                balancehistory = createBalanceHistory(null,transactionApproveRequest.getBusinessId().longValue(),transactionApproveRequest.getProductId(),totalTransactionAmount,transactionApproveRequest.getTransactionId().getTransactionTypeId().getId().intValue());
                            } 
                            balancehistory.setTransactionId(transactionApproveRequest.getTransactionId());
                            saveBalanceHistory(balancehistory);
                        }                                                
                        try {
                            if (transactionApproveRequest.getTransactionId().getTransactionTypeId().getCode().equals(TransactionTypeE.MANREC.getTransactionTypeCode())) {
                                SendSmsThread sendMailTherad = new SendSmsThread(movilPhone,totalTransactionAmount,transactionApproveRequest.getRequestNumber(),
                                Constants.SEND_TYPE_SMS_RECHARGE,transactionApproveRequest.getUnifiedRegistryUserId().longValue(),entityManager);
                                sendMailTherad.run();
                            } else {
                                if (transactionApproveRequest.getTransactionId().getTransactionSourceId().getCode().equals(TransactionSourceE.APPBIL.getTransactionSourceCode())) {
                                    SendSmsThread sendMailTherad = new SendSmsThread(movilPhone,totalTransactionAmount,transactionApproveRequest.getRequestNumber(),
                                    Constants.SEND_TYPE_SMS_WITHDRAWALS,transactionApproveRequest.getUnifiedRegistryUserId().longValue(),entityManager);
                                    sendMailTherad.run();
                                } else {
                                    SendSmsThread sendMailTherad = new SendSmsThread(movilPhone,totalTransactionAmount,transactionApproveRequest.getRequestNumber(),
                                    Constants.SEND_TYPE_SMS_WITHDRAWALS,transactionApproveRequest.getBusinessId().longValue(),entityManager);
                                    sendMailTherad.run();
                                }                               
                            }                            
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }  catch (RegisterNotFoundException e) {
                    e.printStackTrace();
                    throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName(), "transactionApproveRequest"), null);
                } catch (EmptyListException e) {
                    e.printStackTrace();
                    throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName(), "transactionApproveRequest"), null);
                } catch (NegativeBalanceException e) {
                    throw new NegativeBalanceException("Current amount can not be negative");
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
                }
        } else {
                try {
                    saveTransactionApproveRequest(transactionApproveRequest);
                } catch (RegisterNotFoundException e) {
                    e.printStackTrace();
                    throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName(), "transactionApproveRequest"), null);
                }  catch (Exception e) {
                    e.printStackTrace();
                    throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
                }
        }
        return transactionApproveRequest;
    }

    private BalanceHistory createBalanceHistory(Long userId, Long businessId, Product productId, float totalTransactionAmount, int transactionType) throws GeneralException, NullParameterException, NegativeBalanceException, RegisterNotFoundException {
        BalanceHistory currentBalanceHistory = loadLastBalanceHistoryByUserId(userId, businessId, productId.getId());
        float currentAmount = currentBalanceHistory != null ? currentBalanceHistory.getCurrentAmount() : 0f;
        BalanceHistory balanceHistory = new BalanceHistory();
        if (userId != null) {
            balanceHistory.setUserId(userId);
        } else {
            balanceHistory.setBusinessId(businessId);
        }
        balanceHistory.setDate(new Timestamp(new java.util.Date().getTime()));
        balanceHistory.setOldAmount(currentAmount);
        balanceHistory.setVersion(currentBalanceHistory!=null?currentBalanceHistory.getId():null);
        balanceHistory.setProductId(productId);
        float newCurrentAmount = 0.0f;
        switch (transactionType) {
            case 6: //incrementar el saldo
                newCurrentAmount = currentAmount + totalTransactionAmount;//SUMO AL MONTO ACTUAL (EL DESTINO)
                break;
        }
        if (newCurrentAmount < 0) {
            throw new NegativeBalanceException("Current amount can not be negative");
        }
        balanceHistory.setCurrentAmount(newCurrentAmount);
        return balanceHistory;
    }

    public BalanceHistory loadLastBalanceHistoryByUserId(Long userId, Long businessId, Long productId) throws GeneralException, RegisterNotFoundException, NullParameterException {
        BalanceHistory balanceHistory = null;
        StringBuilder sqlBuilder = new StringBuilder("SELECT b FROM BalanceHistory b WHERE b.date = :maxDate AND b.productId.id= "+ productId);
        if (userId != null) {
            sqlBuilder.append(" AND b.userId = " + userId);
        }
        if (businessId != null) {
            sqlBuilder.append(" AND b.businessId = " + businessId);
        }        
        try {
            Date maxDate = (Date) entityManager.createQuery("SELECT MAX(b.date) FROM BalanceHistory b WHERE b.userId = " + userId + " and b.productId.id= "+ productId).getSingleResult();
            Query query = entityManager.createQuery(sqlBuilder.toString());
            query.setParameter("maxDate", maxDate);
            List result = (List) query.setHint("toplink.refresh", "true").getResultList();
            if (!result.isEmpty()) {
                balanceHistory = ((BalanceHistory) result.get(0));
            }
        } catch (NoResultException ex) {
            throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_REGISTER_NOT_FOUND_EXCEPTION, this.getClass(), getMethodName(), "BalanceHistory"), null);
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), "BalanceHistory"), null);
        }
        return balanceHistory;
    }
    
	private float calculateTotalTransactionAmount(CommissionItem commisionItem, float amountTransaction) {
		float totalTransactionAmount = 0;
		switch (commisionItem.getCommissionId().getIndApplicationCommission()) {
		case 1:
			totalTransactionAmount = amountTransaction - commisionItem.getAmount();
			break;
		case 2:
			totalTransactionAmount = amountTransaction;
			break;
		default:
			totalTransactionAmount = amountTransaction - commisionItem.getAmount();
            break;
		}
		return totalTransactionAmount;
	}
	
	@Override
	public BalanceHistory saveBalanceHistory(BalanceHistory balancehistory) throws GeneralException, NullParameterException {
		if (balancehistory == null) {
	            throw new NullParameterException("balancehistory", null);
	    }
		return (BalanceHistory) saveEntity(balancehistory);
    }

    @Override
    public List<TransactionApproveRequest> getTransactionApproveRequest(EJBRequest request) throws GeneralException, EmptyListException, NullParameterException {
        return (List<TransactionApproveRequest>) listEntities(TransactionApproveRequest.class, request, logger, getMethodName());
    }
    
    @Override
    public List<TransactionApproveRequest> getTransactionApproveRequestByStatus(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<TransactionApproveRequest> transactionApproveRequestByStatusList = null;
        Map<String, Object> params = request.getParams();
        
        if (!params.containsKey(EjbConstants.PARAM_REQUEST_NUMBER)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_REQUEST_NUMBER), null);
        }
        transactionApproveRequestByStatusList = (List<TransactionApproveRequest>) getNamedQueryResult(TransactionApproveRequest.class, QueryConstants.TRANSACTION_APPROVE_REQUEST_BY_STATUS, request, getMethodName(), logger, "transactionApproveRequestByStatusList");
        return transactionApproveRequestByStatusList;
    }

}
