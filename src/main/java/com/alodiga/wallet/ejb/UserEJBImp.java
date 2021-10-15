package com.alodiga.wallet.ejb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import org.apache.log4j.Logger;
import com.alodiga.wallet.common.ejb.UserEJB;
import com.alodiga.wallet.common.ejb.UserEJBLocal;
import com.alodiga.wallet.common.exception.EmptyListException;
import com.alodiga.wallet.common.exception.GeneralException;
import com.alodiga.wallet.common.exception.NullParameterException;
import com.alodiga.wallet.common.exception.RegisterNotFoundException;
import com.alodiga.wallet.common.genericEJB.AbstractWalletEJB;
import com.alodiga.wallet.common.genericEJB.EJBRequest;
import com.alodiga.wallet.common.genericEJB.WalletContextInterceptor;
import com.alodiga.wallet.common.genericEJB.WalletLoggerInterceptor;
import com.alodiga.wallet.common.model.Profile;
import com.alodiga.wallet.common.model.User;
import com.alodiga.wallet.common.utils.EjbConstants;
import com.alodiga.wallet.common.utils.QueryConstants;

@Interceptors({WalletLoggerInterceptor.class, WalletContextInterceptor.class})
@Stateless(name = EjbConstants.USER_EJB, mappedName = EjbConstants.USER_EJB)
@TransactionManagement(TransactionManagementType.BEAN)
public class UserEJBImp extends AbstractWalletEJB implements UserEJB, UserEJBLocal {

    private static final Logger logger = Logger.getLogger(UserEJBImp.class);

    //User
    public List<User> getUsers(EJBRequest request) throws EmptyListException, GeneralException {

        List<User> users = null;
        try {
            users = (List<User>) createQuery("SELECT u FROM User u").setHint("toplink.refresh", "true").getResultList();
        } catch (Exception ex) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), ex.getMessage()), null);
        }
        if (users.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return users;
    }

    @Override
    public List<User> validatePassword(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<User> userList = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_CURRENT_PASSWORD)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_CURRENT_PASSWORD), null);
        }
        if (!params.containsKey(EjbConstants.PARAM_USER_ID)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_USER_ID), null);
        }
        userList = (List<User>) getNamedQueryResult(User.class, QueryConstants.VALIDATE_PASSWORD, request, getMethodName(), logger, "userList");
        return userList;
    }
    
//    @Override
//    public List<User> getValidatePassword(User user) throws GeneralException, EmptyListException, NullParameterException {
//        List<User> bankHasProductList = null;
//        try {
//            if (bankHasProduct == null) {
//                throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "bankHasProduct"), null);
//            }      //To change body of generated methods, choose Tools | Templates.
//
//            StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM bank_has_product where productId=");
//            sqlBuilder.append(bankHasProduct.getProductId().getId());
//            sqlBuilder.append(" and bankId=");
//            sqlBuilder.append(bankHasProduct.getBankId().getId());
//            Query query = entityManager.createNativeQuery(sqlBuilder.toString(), BankHasProduct.class);
//            bankHasProductList = (List<BankHasProduct>) query.setHint("toplink.refresh", "true").getResultList();
//
//        } catch (Exception ex) {
//            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), ex.getMessage()), ex);
//        }
//        return bankHasProductList;
//    }

    public List<User> getUserTopUpNotification() throws EmptyListException, GeneralException {
        List<User> users = null;
        try {
            Query query = entityManager.createQuery("SELECT u FROM User u WHERE u.receiveTopUpNotification = TRUE");
            users = query.setHint("toplink.refresh", "true").getResultList();
        } catch (NoResultException ex) {
            throw new EmptyListException("No user found for TopUp Notifications");
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        return users;
    }

    public User loadUser(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        User user = (User) loadEntity(User.class, request, logger, getMethodName());

        return user;
    }

    public User loadUserByLogin(String login) throws RegisterNotFoundException, NullParameterException, GeneralException {
        User user = null;
        if (login == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "login"), null);
        }
        try {

            Query query = createQuery("SELECT u FROM User u WHERE u.login =?1 AND u.enabled=TRUE");
            query.setParameter("1", login);
            user = (User) query.getSingleResult();

        } catch (NoResultException ex) {
            throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), ex);
        } catch (Exception ex) {
            ex.getMessage();
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), ex.getMessage()), ex);
        }
        return user;
    }

    public User loadUserByEmail(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {

        List<User> users = null;
        Map<String, Object> params = request.getParams();

        if (!params.containsKey(QueryConstants.PARAM_EMAIL)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), QueryConstants.PARAM_EMAIL), null);
        }

        try {
            users = (List<User>) getNamedQueryResult(User.class, "User.loadUserByEmail", request, getMethodName(), logger, "User");
        } catch (EmptyListException e) {
            throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName(), "user"), null);
        }

        return users.get(0);
    }

    public User loadUserByLogin(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        List<User> users = null;
        Map<String, Object> params = request.getParams();

        if (!params.containsKey(QueryConstants.PARAM_LOGIN)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), QueryConstants.PARAM_LOGIN), null);
        }
        try {
            users = (List<User>) getNamedQueryResult(User.class, "User.loadUserByLogin", request, getMethodName(), logger, "User");
        } catch (EmptyListException e) {
            throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName(), "user"), null);
        }
        return users.get(0);
    }

    public User saveUser(EJBRequest request) throws NullParameterException, GeneralException {
        return (User) saveEntity(request, logger, getMethodName());
    }

    public User saveUser(User user) throws NullParameterException, GeneralException {
        if (user == null) {
            throw new NullParameterException("user", null);
        }
        return (User) saveEntity(user);
    }

    public boolean validateExistingUser(EJBRequest request) throws NullParameterException, GeneralException {
        boolean valid = true;
        Map<String, Object> params = request.getParams();
        if (params.containsKey(QueryConstants.PARAM_LOGIN)) {
            try {
                loadUserByLogin(request);
            } catch (RegisterNotFoundException ex) {
                valid = false;
            } catch (NullParameterException ex) {
                throw new NullParameterException(ex.getMessage());
            } catch (GeneralException ex) {
                throw new GeneralException(ex.getMessage());
            }
        } else if (params.containsKey(QueryConstants.PARAM_EMAIL)) {
            try {
                loadUserByEmail(request);
            } catch (RegisterNotFoundException ex) {
                valid = false;
            } catch (NullParameterException ex) {
                throw new NullParameterException(ex.getMessage());
            } catch (GeneralException ex) {
                throw new GeneralException(ex.getMessage());
            }
        } else {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), QueryConstants.PARAM_LOGIN), null);
        }
        return valid;
    }

    public void updateUserNotifications(String ids) throws NullParameterException, GeneralException {
        if (ids == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "ids"), null);
        }

        try {
            Query queryDisable = entityManager.createQuery("UPDATE User u SET u.receiveTopUpNotification = FALSE");
            EntityTransaction transaction = entityManager.getTransaction();
            try {
                transaction.begin();
                queryDisable.executeUpdate();
                if (!ids.equals("")) {
                    Query queryEnable = entityManager.createQuery("UPDATE User u SET u.receiveTopUpNotification = TRUE WHERE u.id IN (" + ids + ")");
                    queryEnable.executeUpdate();
                }
                transaction.commit();
            } catch (Exception ex) {
                ex.printStackTrace();
                try {
                    if (transaction.isActive()) {
                        transaction.rollback();
                    }
                } catch (Exception ex1) {
                    ex1.printStackTrace();
                }
            }

        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }

    }
    
    @Override
    public List<User> getUserByLogin(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<User> userList = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_LOGIN)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_LOGIN), null);
        }       
        userList = (List<User>) getNamedQueryResult(User.class, QueryConstants.LOGIN_EXIST_IN_BD, request, getMethodName(), logger, "userList");
        return userList;
    }
    
    @Override
    public List<User> getValidateEmployee(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<User> userList = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_EMPLOYEE)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_EMPLOYEE), null);
        }       
        userList = (List<User>) getNamedQueryResult(User.class, QueryConstants.EMPLOYEE_EXIST_IN_BD, request, getMethodName(), logger, "userList");
        return userList;
    }
   
    //Profile
    public List<Profile> getProfiles() throws EmptyListException, GeneralException {

        List<Profile> profiles = new ArrayList<Profile>();
        Query query = null;
        try {
            query = createQuery("SELECT p FROM Profile p WHERE p.enabled = 1");
            profiles = query.setHint("toplink.refresh", "true").getResultList();
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (profiles.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return profiles;
    }

}
