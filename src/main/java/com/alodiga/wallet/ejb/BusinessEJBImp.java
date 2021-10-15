package com.alodiga.wallet.ejb;

import java.util.List;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import org.apache.log4j.Logger;
import com.portal.business.commons.exceptions.GeneralException;
import com.portal.business.commons.exceptions.NullParameterException;
import com.portal.business.commons.exceptions.EmptyListException;
import com.alodiga.wallet.common.genericEJB.WalletContextInterceptor;
import com.alodiga.wallet.common.genericEJB.WalletLoggerInterceptor;
import com.alodiga.wallet.common.ejb.BusinessEJB;
import com.alodiga.wallet.common.ejb.BusinessEJBLocal;
import com.alodiga.wallet.common.genericEJB.AbstractWalletEJB;
import com.alodiga.wallet.common.utils.EjbConstants;
import com.portal.business.commons.data.BusinessData;
import com.portal.business.commons.models.Business;

@Interceptors({WalletLoggerInterceptor.class, WalletContextInterceptor.class})
@Stateless(name = EjbConstants.BUSINESS_EJB, mappedName = EjbConstants.BUSINESS_EJB)
@TransactionManagement(TransactionManagementType.BEAN)

public class BusinessEJBImp extends AbstractWalletEJB implements BusinessEJB, BusinessEJBLocal {

    private static final Logger logger = Logger.getLogger(BusinessEJBImp.class);    
    private BusinessData businessData = new BusinessData();

    @Override
    public Business getBusinessById(long id) throws NullParameterException, GeneralException {
        return businessData.getBusinessById(id);
    }

    @Override
    public Business getBusinessByCode(String code) throws NullParameterException, GeneralException {
        return businessData.getBusinessByCode(code);
    }

    @Override
    public Business getBusinessByIdentification(String identification) throws NullParameterException, GeneralException {
        return businessData.getBusinessByIdentification(identification);
    }

    @Override
    public Business getBusinessByEmail(String email) throws NullParameterException, GeneralException {
        return businessData.getBusinessByEmail(email);
    }

    @Override
    public Business getBusinessByPhone(String phone) throws NullParameterException, GeneralException {
        return businessData.getBusinessByPhone(phone);
    }

    @Override
    public Business getBusinessByLogin(String login) throws NullParameterException, GeneralException {
        return businessData.getBusinessByLogin(login);
    }

    @Override
    public List<Business> getAll() throws EmptyListException, GeneralException {
        return businessData.getBusinessList();
    }	
    
}
