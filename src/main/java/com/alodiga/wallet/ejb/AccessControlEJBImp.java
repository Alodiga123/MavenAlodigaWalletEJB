package com.alodiga.wallet.ejb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.persistence.Query;
import org.apache.log4j.Logger;

import com.alodiga.wallet.common.ejb.AccessControlEJB;
import com.alodiga.wallet.common.ejb.AccessControlEJBLocal;
import com.alodiga.wallet.common.ejb.UserEJBLocal;
import com.alodiga.wallet.common.exception.DisabledUserException;
import com.alodiga.wallet.common.exception.EmptyListException;
import com.alodiga.wallet.common.exception.GeneralException;
import com.alodiga.wallet.common.exception.NullParameterException;
import com.alodiga.wallet.common.exception.RegisterNotFoundException;
import com.alodiga.wallet.common.genericEJB.AbstractWalletEJB;
import com.alodiga.wallet.common.genericEJB.EJBRequest;
import com.alodiga.wallet.common.genericEJB.WalletContextInterceptor;
import com.alodiga.wallet.common.genericEJB.WalletLoggerInterceptor;
import com.alodiga.wallet.common.model.Permission;
import com.alodiga.wallet.common.model.PermissionGroup;
import com.alodiga.wallet.common.model.PermissionHasProfile;
import com.alodiga.wallet.common.model.Profile;
import com.alodiga.wallet.common.model.ProfileData;
import com.alodiga.wallet.common.model.User;
import com.alodiga.wallet.common.utils.EjbConstants;
import com.alodiga.wallet.common.utils.QueryConstants;

import javax.persistence.NoResultException;


@Interceptors({WalletLoggerInterceptor.class, WalletContextInterceptor.class})
@Stateless(name = EjbConstants.ACCESS_CONTROL_EJB, mappedName = EjbConstants.ACCESS_CONTROL_EJB)
@TransactionManagement(TransactionManagementType.BEAN)
public class AccessControlEJBImp extends AbstractWalletEJB implements AccessControlEJB, AccessControlEJBLocal {

    private static final Logger logger = Logger.getLogger(AccessControlEJBImp.class);
    @EJB
    private UserEJBLocal userEJB;
    private static AccessControlEJB accessEjb = null;

    public void deletePermissionHasProfile(Long profileId) throws NullParameterException, GeneralException {
        if (profileId == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "profileId"), null);
        }
        Map<String, Long> params = new HashMap<String, Long>();
        params.put(QueryConstants.PARAM_PROFILE_ID, profileId);
        try {
            executeNameQuery(PermissionHasProfile.class, QueryConstants.DELETE_PERMISSION_HAS_PROFILE, params, getMethodName(), logger, "Profile", null, null);
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
    }

    public List<Profile> getParentsByProfile(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<Profile> profiles = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(QueryConstants.PARAM_PROFILE_ID)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), QueryConstants.PARAM_PROFILE_ID), null);
        }
        Query query = null;
        try {
            query = createQuery("SELECT php.parent FROM ProfileHasProfile php WHERE php.child.id = ?1 AND php.endingDate IS NULL");
            query.setParameter("1", params.get(QueryConstants.PARAM_PROFILE_ID));
            if (request.getLimit() != null && request.getLimit() > 0) {
                query.setMaxResults(request.getLimit());
            }
            profiles = query.setHint("toplink.refresh", "true").getResultList();

        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (profiles.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return profiles;
    }

    public List<Permission> getPermissions(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<Permission> permissions = (List<Permission>) listEntities(Permission.class, request, logger, getMethodName());
        return permissions;
    }
    
    public List<Permission> getPermissionOrderByAsc(EJBRequest request) throws EmptyListException, NullParameterException, GeneralException {
        List<Permission> permission = new ArrayList<Permission>();
        Query query = null;
        try {
            query = createQuery("SELECT p FROM Permission p ORDER BY p.action ASC");
            permission = query.setHint("toplink.refresh", "true").getResultList();
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (permission.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return permission;

    }

    public List<Profile> getProfiles(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<Profile> profiles = (List<Profile>) listEntities(Profile.class, request, logger, getMethodName());
        return profiles;
    }

    public Permission loadPermission(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        Permission permission = (Permission) loadEntity(Permission.class, request, logger, getMethodName());

        return permission;
    }

    public Profile loadProfile(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        Profile profile = (Profile) loadEntity(Profile.class, request, logger, getMethodName());

        return profile;
    }

    public void logginFailed(EJBRequest request) throws NullParameterException, GeneralException, RegisterNotFoundException {
        Object o = request.getParam();
        if (o == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), QueryConstants.PARAM_CUSTOMER_ID + " - " + QueryConstants.PARAM_DISTRIBUTOR_ID + " - " + QueryConstants.PARAM_USER_ID), null);
        }

    }

    public void logginSuccessful(EJBRequest request) throws NullParameterException, GeneralException, RegisterNotFoundException {
    }

    public Permission savePermission(EJBRequest request) throws NullParameterException, GeneralException {
        return (Permission) saveEntity(request, logger, getMethodName());
    }

    public Profile saveProfile(EJBRequest request) throws NullParameterException, GeneralException {
        return (Profile) saveEntity(request, logger, getMethodName());
    }

    public ProfileData saveProfileData(EJBRequest request) throws NullParameterException, GeneralException {
        return (ProfileData) saveEntity(request, logger, getMethodName());

    }

    public boolean validateLoginPreferences(EJBRequest request) throws NullParameterException, GeneralException, RegisterNotFoundException {
        Object o = request.getParam();
        if (o == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), QueryConstants.PARAM_CUSTOMER_ID + " - " + QueryConstants.PARAM_DISTRIBUTOR_ID + " - " + QueryConstants.PARAM_USER_ID), null);
        }
        return false;
    }

    public User validateUser(String login, String password) throws RegisterNotFoundException, NullParameterException, GeneralException, DisabledUserException {
        User user = null;


        if (login == null || login.equals("")) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), QueryConstants.PARAM_LOGIN), null);
        }
        if (password == null || password.equals("")) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), QueryConstants.PARAM_PASSWORD), null);
        }

        try {
            user = userEJB.loadUserByLogin(login);
            if (user != null && !user.getPassword().equals(password)) {
                user = null;
                throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName(), "user"), null);
            }

        } catch (NoResultException ex) {
            throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName(), "user"), ex);
        } catch (RegisterNotFoundException ex) {
            throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName(), "user"), ex);
        } catch (Exception ex) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName(), "user"), ex);
        }
        if (!user.getEnabled()) {
            throw new DisabledUserException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName(), "distributor enabled"), null);
        }

        return user;
    }


    public List<PermissionGroup> getPermissionGroups() throws EmptyListException, NullParameterException, GeneralException {
        List<PermissionGroup> permissionGroups = new ArrayList<PermissionGroup>();
        Query query = null;
        try {
            query = createQuery("SELECT pg FROM PermissionGroup pg");
            permissionGroups = query.setHint("toplink.refresh", "true").getResultList();
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (permissionGroups.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return permissionGroups;

    }

    public List<Permission> getPermissions() throws EmptyListException, NullParameterException, GeneralException {
        List<Permission> permissions = new ArrayList<Permission>();
        Query query = null;
        try {
            query = createQuery("SELECT p FROM Permission p WHERE p.enabled =1");
            permissions = query.setHint("toplink.refresh", "true").getResultList();
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (permissions.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return permissions;
    }

    public List<Permission> getPermissionByGroupId(Long groupId) throws EmptyListException, NullParameterException, GeneralException {
        if (groupId == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "groupId"), null);
        }
        List<Permission> permissions = new ArrayList<Permission>();
        Query query = null;
        try {
            query = createQuery("SELECT p FROM Permission p WHERE p.enabled =1 AND p.permissionGroup.id=?1");
            query.setParameter("1", groupId);
            permissions = query.setHint("toplink.refresh", "true").getResultList();
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (permissions.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return permissions;
    }

    public List<Permission> getPermissionByProfileId(Long profileId) throws EmptyListException, NullParameterException, GeneralException {
        if (profileId == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "groupId"), null);
        }
        List<Permission> permissions = new ArrayList<Permission>();
        Query query = null;
        try {
            query = createQuery("SELECT php.permission FROM PermissionHasProfile php WHERE php.profile.id = ?1");
            query.setParameter("1", profileId);
            permissions = query.setHint("toplink.refresh", "true").getResultList();
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (permissions.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return permissions;
    }

    public Permission loadPermissionById(Long permissionId) throws GeneralException, NullParameterException, RegisterNotFoundException {
        EJBRequest bRequest = new EJBRequest(permissionId);
        Permission permission = (Permission) loadEntity(Permission.class, bRequest, logger, getMethodName());
        return permission;

    }
    
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
    
    public List<User> getUsersWithParams(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<User> users = null;
        Map<String, Object> params = request.getParams();
	    StringBuilder sqlBuilder = new StringBuilder("SELECT s FROM User s WHERE s.enabled = 1");
	    if (params.containsKey(QueryConstants.PARAM_LOGIN)) {
	        sqlBuilder.append(" AND s.login=").append(params.get(QueryConstants.PARAM_LOGIN));
	    }

	    Query query = null;
	    try {
	        System.out.println("query:********"+sqlBuilder.toString());
	        query = createQuery(sqlBuilder.toString());
	        if (request.getLimit() != null && request.getLimit() > 0) {
	            query.setMaxResults(request.getLimit());
	        }
	        users = query.setHint("toplink.refresh", "true").getResultList();
	    } catch (Exception e) {
	        e.printStackTrace();
	        throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
	    }
	    if (users.isEmpty()) {
	        throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
	    }
        
        return users;
    }

}
