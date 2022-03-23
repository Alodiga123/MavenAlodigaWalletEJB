package com.alodiga.wallet.ejb;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import com.alodiga.wallet.common.ejb.UtilsEJB;
import com.alodiga.wallet.common.ejb.UtilsEJBLocal;
import com.alodiga.wallet.common.exception.DuplicateEntryException;
import com.alodiga.wallet.common.exception.EmptyListException;
import com.alodiga.wallet.common.exception.GeneralException;
import com.alodiga.wallet.common.exception.NullParameterException;
import com.alodiga.wallet.common.exception.RegisterNotFoundException;
import com.alodiga.wallet.common.genericEJB.AbstractWalletEJB;
import com.alodiga.wallet.common.genericEJB.EJBRequest;
import com.alodiga.wallet.common.genericEJB.WalletContextInterceptor;
import com.alodiga.wallet.common.genericEJB.WalletLoggerInterceptor;
import com.alodiga.wallet.common.model.AccountBank;
import com.alodiga.wallet.common.model.Bank;
import com.alodiga.wallet.common.model.BankOperation;
import com.alodiga.wallet.common.model.BankOperationMode;
import com.alodiga.wallet.common.model.BankOperationType;
import com.alodiga.wallet.common.model.AffiliationRequest;
import com.alodiga.wallet.common.model.BusinessCategory;
import com.alodiga.wallet.common.model.BusinessSubCategory;
import com.alodiga.wallet.common.model.BusinessType;
import com.alodiga.wallet.common.model.BusinessServiceType;
import com.alodiga.wallet.common.model.CalendarDays;
import com.alodiga.wallet.common.model.City;
import com.alodiga.wallet.common.model.CollectionType;
import com.alodiga.wallet.common.model.CollectionsRequest;
import com.alodiga.wallet.common.model.Commission;
import com.alodiga.wallet.common.model.CommissionItem;
import com.alodiga.wallet.common.model.Country;
import com.alodiga.wallet.common.model.County;
import com.alodiga.wallet.common.model.Currency;
import com.alodiga.wallet.common.model.DailyClosing;
import com.alodiga.wallet.common.model.DocumentType;
import com.alodiga.wallet.common.model.ExchangeRate;
import com.alodiga.wallet.common.model.Language;
import com.alodiga.wallet.common.model.OriginApplication;
import com.alodiga.wallet.common.model.Period;
import com.alodiga.wallet.common.model.PersonType;
import com.alodiga.wallet.common.model.PreferenceValue;
import com.alodiga.wallet.common.model.Product;
import com.alodiga.wallet.common.model.RequestHasCollectionRequest;
import com.alodiga.wallet.common.model.RequestType;
import com.alodiga.wallet.common.model.ReviewAffiliationRequest;
import com.alodiga.wallet.common.model.ReviewOfac;
import com.alodiga.wallet.common.model.ReviewType;
import com.alodiga.wallet.common.model.Sequences;
import com.alodiga.wallet.common.model.Sms;
import com.alodiga.wallet.common.model.State;
import com.alodiga.wallet.common.model.StatusCard;
import com.alodiga.wallet.common.model.StatusCardHasFinalState;
import com.alodiga.wallet.common.model.StatusBusinessAffiliationHasFinalState;
import com.alodiga.wallet.common.model.StatusRequest;
import com.alodiga.wallet.common.model.StatusTransactionApproveRequest;
import com.alodiga.wallet.common.model.Transaction;
import com.alodiga.wallet.common.model.TransactionApproveRequest;
import com.alodiga.wallet.common.model.TransactionSource;
import com.alodiga.wallet.common.model.TransactionType;
import com.alodiga.wallet.common.utils.Constants;
import com.alodiga.wallet.common.utils.EjbConstants;
import com.alodiga.wallet.common.utils.EjbUtils;
import com.alodiga.wallet.common.utils.QueryConstants;
import java.util.Calendar;
import java.util.logging.Level;

@Interceptors({WalletLoggerInterceptor.class, WalletContextInterceptor.class})
@Stateless(name = EjbConstants.UTILS_EJB, mappedName = EjbConstants.UTILS_EJB)
@TransactionManagement(TransactionManagementType.BEAN)

public class UtilsEJBImp extends AbstractWalletEJB implements UtilsEJB, UtilsEJBLocal {

    private static final Logger logger = Logger.getLogger(UtilsEJBImp.class);

    public List<City> getCitiesByCounty(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<City> cities = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(QueryConstants.PARAM_COUNTY_ID)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), QueryConstants.PARAM_COUNTY_ID), null);
        }
        cities = (List<City>) getNamedQueryResult(UtilsEJB.class, QueryConstants.CITIES_BY_COUNTY, request, getMethodName(), logger, "cities");
        return cities;
    }

    public List<City> getCitiesByState(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<City> cities = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(QueryConstants.PARAM_STATE_ID)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "id"), null);
        }

        cities = (List<City>) getNamedQueryResult(UtilsEJB.class, QueryConstants.CITIES_BY_STATE, request, getMethodName(), logger, "cities");

        return cities;
    }

    public City loadCity(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        City city = (City) loadEntity(City.class, request, logger, getMethodName());
        return city;
    }

    //County
    public List<County> getCountiesByState(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<County> counties = null;
        Map<String, Object> params = request.getParams();

        if (!params.containsKey(QueryConstants.PARAM_STATE_ID)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), QueryConstants.PARAM_STATE_ID), null);
        }
        counties = (List<County>) getNamedQueryResult(UtilsEJB.class, QueryConstants.COUNTIES_BY_STATE, request, getMethodName(), logger, "counties");
        return counties;
    }

    public County loadCounty(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        County county = (County) loadEntity(County.class, request, logger, getMethodName());
        return county;
    }

    //Country
    public List<Country> getCountries(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<Country> countries = (List<Country>) listEntities(Country.class, request, logger, getMethodName());
        return countries;
    }

    public List<Country> getCountries() throws EmptyListException, GeneralException, NullParameterException {
        List<Country> countries = null;
        Query query = null;
        try {
            query = createQuery("SELECT c FROM Country c ORDER BY c.name");
            countries = query.setHint("toplink.refresh", "true").getResultList();
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (countries.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return countries;
    }

    public Country loadCountry(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        Country country = (Country) loadEntity(Country.class, request, logger, getMethodName());
        return country;
    }

    public Country loadCountryByName(String name) throws RegisterNotFoundException, NullParameterException, GeneralException {
        List<Country> list = new ArrayList<Country>();
        Country country = new Country();

        try {
            if (name == null) {
                throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "name"), null);
            }
            StringBuilder sqlBuilder = new StringBuilder("SELECT DISTINCT ct.country FROM CountryTranslation ct ");
            sqlBuilder.append("WHERE ct.alias LIKE '").append(name).append("'")//Problema con el caso FRANCE y GUYANA FRANCESA
                    .append(" OR ct.country.alternativeName1 LIKE '").append(name).append("'").append(" OR ct.country.alternativeName2 LIKE '%").append(name).append("'").append(" OR ct.country.alternativeName3 LIKE '%").append(name).append("'");
            //country = (Country) createQuery(sqlBuilder.toString()).setHint("toplink.refresh", "true").getSingleResult();
            list = createQuery(sqlBuilder.toString()).setHint("toplink.refresh", "true").getResultList();
            if (list.isEmpty()) {
                System.out.println("name: " + name);
                throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_REGISTER_NOT_FOUND_EXCEPTION, Country.class.getSimpleName(), "loadCountryByName", Country.class.getSimpleName(), null), null);
            } else {
                country = list.get(0);
            }
        } catch (RegisterNotFoundException ex) {
            System.out.println("name: " + name);
            throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_REGISTER_NOT_FOUND_EXCEPTION, Country.class.getSimpleName(), "loadCountryByName", Country.class.getSimpleName(), null), ex);
        } catch (Exception ex) {
            System.out.println("name: " + name);
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), ex.getMessage()), ex);
        }

        return country;
    }

    public Country loadCountryByShortName(String referenceCode) throws RegisterNotFoundException, NullParameterException, GeneralException {
        Country country = new Country();
        try {
            if (referenceCode == null) {
                throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "referenceCode"), null);
            }

            Query query = createQuery("SELECT c FROM Country c WHERE c.shortName = ?1");
            query.setParameter("1", referenceCode);
            country = (Country) query.setHint("toplink.refresh", "true").getSingleResult();
        } catch (NoResultException ex) {
            System.out.println("shortName: " + referenceCode);
            throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_REGISTER_NOT_FOUND_EXCEPTION, Country.class.getSimpleName(), "loadCountryByShortName", Country.class.getSimpleName(), null), ex);
        } catch (Exception ex) {
            System.out.println("shortName: " + referenceCode);
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), ex.getMessage()), ex);
        }
        return country;
    }

    public Country searchCountry(String name) throws RegisterNotFoundException, NullParameterException, GeneralException {
        Country country = new Country();

        try {
            if (name == null) {
                throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "name"), null);
            }
            StringBuilder sqlBuilder = new StringBuilder("SELECT DISTINCT ct.country FROM CountryTranslation ct ");
            sqlBuilder.append("WHERE ct.country.name LIKE '").append(name).append("'").append(" OR ct.alias LIKE '").append(name).append("'")//Problema con el caso FRANCE y GUYANA FRANCESA
                    .append(" OR ct.country.alternativeName1 LIKE '%").append(name).append("%'").append(" OR ct.country.alternativeName2 LIKE '%").append(name).append("'").append(" OR ct.country.alternativeName3 LIKE '%").append(name).append("'").append(" OR ct.country.shortName LIKE '").append(name).append("'");
            country = (Country) createQuery(sqlBuilder.toString()).setHint("toplink.refresh", "true").getSingleResult();
        } catch (NoResultException ex) {
            System.out.println("name: " + name);
            throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_REGISTER_NOT_FOUND_EXCEPTION, Country.class.getSimpleName(), "loadCountryByName", Country.class.getSimpleName(), null), ex);
        } catch (Exception ex) {
            System.out.println("name: " + name);
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), ex.getMessage()), ex);
        }

        return country;
    }

    public Country saveCountry(Country country) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (country == null) {
            throw new NullParameterException("country", null);
        }
        return (Country) saveEntity(country);
    }

    public List<Country> getSearchCountry(String name) throws EmptyListException, GeneralException, NullParameterException {
        List<Country> countryList = null;
        if (name == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "name"), null);
        }
        try {
            StringBuilder sqlBuilder = new StringBuilder("SELECT DISTINCT c FROM Country c ");
            sqlBuilder.append("WHERE c.name LIKE '").append(name).append("%'");

            Query query = entityManager.createQuery(sqlBuilder.toString());
            countryList = query.setHint("toplink.refresh", "true").getResultList();

        } catch (NoResultException ex) {
            throw new EmptyListException("No distributions found");
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        return countryList;
    }

    public List<Country> getValidateCountryByCode(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<Country> countryList = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_CODE)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_CODE), null);
        }
        countryList = (List<Country>) getNamedQueryResult(Country.class, QueryConstants.CODE_EXIST_IN_BD_COUNTRY, request, getMethodName(), logger, "countryList");
        return countryList;
    }

    public List<Country> getValidateCountryByName(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<Country> countryList = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_NAME)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_NAME), null);
        }
        countryList = (List<Country>) getNamedQueryResult(Country.class, QueryConstants.NAME_EXIST_IN_BD_COUNTRY, request, getMethodName(), logger, "countryList");
        return countryList;
    }

    //Language
    public List<Language> getLanguages() throws EmptyListException, GeneralException, NullParameterException {
        EJBRequest request = new EJBRequest();
        List<Language> languages = (List<Language>) listEntities(Language.class, request, logger, getMethodName());
        return languages;
    }

    public Language loadLanguage(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        Language language = (Language) loadEntity(Language.class, request, logger, getMethodName());
        return language;
    }

    //Period
    public List<Period> getPeriods() throws EmptyListException, GeneralException, NullParameterException {
        EJBRequest request = new EJBRequest();
        List<Period> periods = (List<Period>) listEntities(Period.class, request, logger, getMethodName());

        return periods;
    }

    public Period loadPeriod(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        Period period = (Period) loadEntity(Period.class, request, logger, getMethodName());
        return period;
    }

    public Period loadperiod(Period period) throws RegisterNotFoundException, NullParameterException, GeneralException {
        Period periods = (Period) loadEntity(Period.class, period, logger, getMethodName());
        return periods;
    }

    //State
    public List<State> getStateByCountry(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<State> states = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(QueryConstants.PARAM_COUNTRY_ID)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), QueryConstants.PARAM_COUNTRY_ID), null);
        }
        states = (List<State>) getNamedQueryResult(UtilsEJB.class, QueryConstants.STATES_BY_COUNTRY, request, getMethodName(), logger, "states");
        return states;
    }

    public State loadState(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        State state = (State) loadEntity(State.class, request, logger, getMethodName());
        return state;
    }

    //Currency
    public List<Currency> getCurrency(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<Currency>) listEntities(Currency.class, request, logger, getMethodName());
    }

    public List<Currency> getCurrencies() throws EmptyListException, GeneralException, NullParameterException {
        EJBRequest request = new EJBRequest();
        List<Currency> currencies = (List<Currency>) listEntities(Currency.class, request, logger, getMethodName());

        return currencies;
    }

    @Override
    public List<Currency> getSearchCurrency(String name) throws EmptyListException, GeneralException, NullParameterException {
        List<Currency> currencyList = null;
        if (name == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "name"), null);
        }
        try {
            StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM currency c ");
            sqlBuilder.append("WHERE c.name LIKE '%").append(name).append("%'");
            Query query = entityManager.createNativeQuery(sqlBuilder.toString(), Currency.class);
            currencyList = query.setHint("toplink.refresh", "true").getResultList();

        } catch (NoResultException ex) {
            throw new EmptyListException("No distributions found");
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        return currencyList;
    }

    @Override
    public Currency loadCurrency(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        Currency currency = (Currency) loadEntity(Currency.class, request, logger, getMethodName());
        return currency;
    }

    @Override
    public Currency saveCurrency(Currency currency) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (currency == null) {
            throw new NullParameterException("currency", null);
        }
        return (Currency) saveEntity(currency);
    }

    //SMS
    public Sms saveSMS(EJBRequest request) throws NullParameterException, GeneralException {
        return (Sms) saveEntity(request, logger, getMethodName());
    }

    //Bank
    @Override
    public List<Bank> getBank(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<Bank>) listEntities(Bank.class, request, logger, getMethodName());
    }

    @Override
    public Bank loadBank(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        Bank bank = (Bank) loadEntity(Bank.class, request, logger, getMethodName());
        return bank;
    }

    @Override
    public Bank saveBank(Bank bank) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (bank == null) {
            throw new NullParameterException("bank", null);
        }
        return (Bank) saveEntity(bank);
    }

    @Override
    public List<Bank> getSearchBank(String name) throws EmptyListException, GeneralException, NullParameterException {
        List<Bank> bankyList = null;
        if (name == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "name"), null);
        }
        try {
            StringBuilder sqlBuilder = new StringBuilder("SELECT DISTINCT b FROM Bank b ");
            sqlBuilder.append("WHERE b.name LIKE '").append(name).append("%'");

            Query query = entityManager.createQuery(sqlBuilder.toString());
            bankyList = query.setHint("toplink.refresh", "true").getResultList();

        } catch (NoResultException ex) {
            throw new EmptyListException("No distributions found");
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        return bankyList;
    }

    @Override
    public List<Bank> searchBankByCountry(String name) throws EmptyListException, GeneralException, NullParameterException {
        List<Bank> bank = null;
        try {
            if (name == null) {
                throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "name"), null);
            }

            StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM bank b ");
            sqlBuilder.append("WHERE b.countryId IN (SELECT c.id FROM country c WHERE c.name LIKE '").append(name).append("%')");
            Query query = entityManager.createNativeQuery(sqlBuilder.toString(), Bank.class);
            bank = query.setHint("toplink.refresh", "true").getResultList();

        } catch (NoResultException ex) {
            throw new EmptyListException("No distributions found");
        } catch (Exception ex) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), ex.getMessage()), ex);
        }
        return bank;
    }
    
    public List<Bank> getBankByCountry(Long countryId) throws EmptyListException, GeneralException, NullParameterException {
        List<Bank> bankList = new ArrayList<Bank>();
        try {
            bankList = (List<Bank>) entityManager.createNamedQuery("Bank.findByCountryIdBank", Bank.class).setParameter("countryId", countryId).getResultList();
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (bankList.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return bankList;
    }

    //ExchangeRate
    public List<ExchangeRate> getExchangeRate(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<ExchangeRate>) listEntities(ExchangeRate.class, request, logger, getMethodName());
    }

    @Override
    public ExchangeRate loadExchangeRate(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        ExchangeRate exchangeRate = (ExchangeRate) loadEntity(ExchangeRate.class, request, logger, getMethodName());
        return exchangeRate;
    }

    @Override
    public ExchangeRate saveExchangeRate(ExchangeRate exchangeRate) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (exchangeRate == null) {
            throw new NullParameterException("exchangeRate", null);
        }
        return (ExchangeRate) saveEntity(exchangeRate);
    }

    public List<ExchangeRate> getExchangeRateByProductAndEndingDate(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<ExchangeRate> exchangeList = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_PRODUCT_ID)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_PRODUCT_ID), null);
        }
        exchangeList = (List<ExchangeRate>) getNamedQueryResult(ExchangeRate.class, QueryConstants.EXCHANGE_RATE_BY_PRODUCT, request, getMethodName(), logger, "exchangeRateByProduct");
        return exchangeList;
    }

    //Transaction
    @Override
    public List<Transaction> getTransaction(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<Transaction> transactions = (List<Transaction>) listEntities(Transaction.class, request, logger, getMethodName());
        return transactions;
    }

    public List<Transaction> getTransactionByDates(Date beginningDate, Date endingDate) throws RegisterNotFoundException, NullParameterException, GeneralException, EmptyListException {
        List<Transaction> transactionsList = new ArrayList<Transaction>();

        try {
            String pattern = "yyyy-MM-dd";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

            if (beginningDate == null || endingDate == null) {
                throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "beginningDate & endingDate"), null);
            }

            String strDate1 = (simpleDateFormat.format(beginningDate));
            String strDate2 = (simpleDateFormat.format(endingDate));

            StringBuilder sqlBuilder = new StringBuilder("select * from transaction t where t.creationDate BETWEEN '");
            sqlBuilder.append(strDate1);
            sqlBuilder.append("' AND '");
            sqlBuilder.append(strDate2);
            sqlBuilder.append("'");
            Query query = entityManager.createNativeQuery(sqlBuilder.toString(), Transaction.class);
            transactionsList = (List<Transaction>) query.setHint("toplink.refresh", "true").getResultList();

        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (transactionsList.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return transactionsList;
    }

    public List<Transaction> getTransactionByBeginningDate(Date beginningDate) throws EmptyListException, GeneralException, NullParameterException {
        List<Transaction> transactionsList = new ArrayList<Transaction>();
        try {
            String pattern = "yyyy-MM-dd";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

            if (beginningDate == null) {
                throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "beginningDate & endingDate"), null);
            }

            String strDate = (simpleDateFormat.format(beginningDate));

            StringBuilder sqlBuilder = new StringBuilder("select * from transaction t where t.creationDate like '");
            sqlBuilder.append(strDate);
            sqlBuilder.append("%'");
            Query query = entityManager.createNativeQuery(sqlBuilder.toString(), Transaction.class);
            transactionsList = (List<Transaction>) query.setHint("toplink.refresh", "true").getResultList();

        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (transactionsList.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return transactionsList;
    }

    public List<Transaction> getTransactionByBeginningDateAndOriginTransaccion(EJBRequest request) throws GeneralException, NullParameterException, EmptyListException {
        List<Transaction> transactionsList = new ArrayList<Transaction>();
        try {
            Map<String, Object> params = request.getParams();
            String pattern = "yyyy-MM-dd";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String strDate = (simpleDateFormat.format(EjbUtils.getBeginningDate((Date) params.get(QueryConstants.PARAM_BEGINNING_DATE))));

            StringBuilder sqlBuilder = new StringBuilder("SELECT * from transaction t WHERE t.creationDate LIKE '");
            sqlBuilder.append(strDate);
            sqlBuilder.append("%'");

            if (params.containsKey(QueryConstants.PARAM_STATUS_SOURCE_ID)) {
                sqlBuilder.append(" AND t.transactionSourceId = ").append(params.get(QueryConstants.PARAM_STATUS_SOURCE_ID));
            }

            Query query = entityManager.createNativeQuery(sqlBuilder.toString(), Transaction.class);
            transactionsList = (List<Transaction>) query.setHint("toplink.refresh", "true").getResultList();

        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (transactionsList.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }

        return transactionsList;
    }

    public List<Transaction> getTransactionByDatesAndOrigin(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException, EmptyListException {
        List<Transaction> transactionsList = new ArrayList<Transaction>();

        try {
            Map<String, Object> params = request.getParams();
            String pattern = "yyyy-MM-dd";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String strDate1 = (simpleDateFormat.format(EjbUtils.getBeginningDate((Date) params.get(QueryConstants.PARAM_BEGINNING_DATE))));
            String strDate2 = (simpleDateFormat.format(EjbUtils.getEndingDate((Date) params.get(QueryConstants.PARAM_ENDING_DATE))));

            StringBuilder sqlBuilder = new StringBuilder("select * from transaction t where t.creationDate BETWEEN '");
            sqlBuilder.append(strDate1);
            sqlBuilder.append("' and '");
            sqlBuilder.append(strDate2);
            sqlBuilder.append("'");

            if (params.containsKey(QueryConstants.PARAM_STATUS_SOURCE_ID)) {
                sqlBuilder.append(" AND t.transactionSourceId = ").append(params.get(QueryConstants.PARAM_STATUS_SOURCE_ID));
            }

            Query query = entityManager.createNativeQuery(sqlBuilder.toString(), Transaction.class);
            transactionsList = (List<Transaction>) query.setHint("toplink.refresh", "true").getResultList();

        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (transactionsList.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return transactionsList;
    }

    @Override
    public Transaction loadTransaction(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        Transaction transaction = (Transaction) loadEntity(Transaction.class, request, logger, getMethodName());
        return transaction;
    }

    @Override
    public Transaction saveTransaction(Transaction transaction) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (transaction == null) {
            throw new NullParameterException("transaction", null);
        }
        return (Transaction) saveEntity(transaction);
    }

    public List<BankOperation> getBankOperationsByParams(EJBRequest request) throws NullParameterException, GeneralException, EmptyListException {
        List<BankOperation> operations = new ArrayList<BankOperation>();

        Map<String, Object> params = request.getParams();

        StringBuilder sqlBuilder = new StringBuilder("SELECT o FROM BankOperation o WHERE o.transactionId.creationDate BETWEEN ?1 AND ?2");
        if (!params.containsKey(QueryConstants.PARAM_BEGINNING_DATE) || !params.containsKey(QueryConstants.PARAM_ENDING_DATE)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "beginningDate & endingDate"), null);
        }
        if (params.containsKey(QueryConstants.PARAM_BANK_OPERATION_TYPE_ID)) {
            sqlBuilder.append(" AND o.bankOperationTypeId.id=").append(params.get(QueryConstants.PARAM_BANK_OPERATION_TYPE_ID));
        }
        if (params.containsKey(QueryConstants.PARAM_BANK_OPERATION_MODE_ID)) {
            sqlBuilder.append(" AND o.bankOperationModeId.id=").append(params.get(QueryConstants.PARAM_BANK_OPERATION_MODE_ID));
        }
        if (params.containsKey(QueryConstants.PARAM_PRODUCT_ID)) {
            sqlBuilder.append(" AND o.productId.id=").append(params.get(QueryConstants.PARAM_PRODUCT_ID));
        }
        if (params.containsKey(QueryConstants.PARAM_BANK_ID)) {
            sqlBuilder.append(" AND o.bankId.id=").append(params.get(QueryConstants.PARAM_BANK_ID));
        }
        Query query = null;
        try {
            System.out.println("query:********" + sqlBuilder.toString());
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
    public List<BankOperationType> getBankOperationTypes(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<BankOperationType> operationTypes = (List<BankOperationType>) listEntities(BankOperationType.class, request, logger, getMethodName());
        return operationTypes;
    }

    @Override
    public List<BankOperationMode> getBankOperationModes(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<BankOperationMode> operationModes = (List<BankOperationMode>) listEntities(BankOperationMode.class, request, logger, getMethodName());
        return operationModes;
    }

    public List<BankOperation> getBankOperations(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<BankOperation> bankOperations = (List<BankOperation>) listEntities(BankOperation.class, request, logger, getMethodName());
        return bankOperations;
    }

    //Commission
    public List<Commission> getCommission(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<Commission>) listEntities(Commission.class, request, logger, getMethodName());
    }

    public List<Commission> getCommissionByProduct(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<Commission> commissionByProductList = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_PRODUCT_ID)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_PRODUCT_ID), null);
        }
        commissionByProductList = (List<Commission>) getNamedQueryResult(Commission.class, QueryConstants.COMMISSION_BY_PRODUCT, request, getMethodName(), logger, "commissionByProductList");
        return commissionByProductList;
    }

    public Commission loadCommission(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        Commission commission = (Commission) loadEntity(Commission.class, request, logger, getMethodName());
        return commission;
    }

    public Commission saveCommission(Commission commission) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (commission == null) {
            throw new NullParameterException("commission", null);
        }
        return (Commission) saveEntity(commission);
    }

    public List<Commission> getCommissionByProductAndTranssactionType(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<Commission> commissionByProductAndTranssactionType = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_PRODUCT_ID)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_PRODUCT_ID), null);
        }

        if (!params.containsKey(EjbConstants.TRANSACTION_TYPE_KEY)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.TRANSACTION_TYPE_KEY), null);
        }

        if (!params.containsKey(EjbConstants.PARAM_APPLICATION_COMISSION)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_APPLICATION_COMISSION), null);
        }

        commissionByProductAndTranssactionType = (List<Commission>) getNamedQueryResult(Commission.class, QueryConstants.COMMISSION_BY_TRANSACTIONTYPE_AND_PRODUCT, request, getMethodName(), logger, "comissionByTransactionTypeAndProduct");
        return commissionByProductAndTranssactionType;
    }

    public List<Commission> searchCommissionByProduct(String name) throws EmptyListException, GeneralException, NullParameterException {
        List<Commission> commission = null;
        try {
            if (name == null) {
                throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "name"), null);
            }

            StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM commission c ");
            sqlBuilder.append("WHERE c.productId IN (SELECT p.id FROM product p WHERE p.name LIKE '").append(name).append("%')");
            Query query = entityManager.createNativeQuery(sqlBuilder.toString(), Commission.class);
            commission = query.setHint("toplink.refresh", "true").getResultList();

        } catch (NoResultException ex) {
            throw new EmptyListException("No distributions found");
        } catch (Exception ex) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), ex.getMessage()), ex);
        }
        return commission;
    }
    
    public List<Commission> searchCommissionByTranssactionType(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<Commission> commission = null;
        Map<String, Object> params = request.getParams();
        try {
           
            StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM commission c ");
            sqlBuilder.append("WHERE c.productId = ").append(params.get(QueryConstants.PARAM_PRODUCT_ID));
            sqlBuilder.append(" AND c.transactionTypeId IN (SELECT t.id FROM transaction_type t WHERE t.value LIKE '").append(params.get(QueryConstants.PARAM_VALUE)).append("%')");
            Query query = entityManager.createNativeQuery(sqlBuilder.toString(), Commission.class);
            commission = query.setHint("toplink.refresh", "true").getResultList();

        } catch (NoResultException ex) {
            throw new EmptyListException("No distributions found");
        } catch (Exception ex) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), ex.getMessage()), ex);
        }
        return commission;
    }

    //TransactionType
    public List<TransactionType> getTransactionType(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<TransactionType>) listEntities(TransactionType.class, request, logger, getMethodName());
    }

    public TransactionType loadTransactionType(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        TransactionType transactionType = (TransactionType) loadEntity(TransactionType.class, request, logger, getMethodName());
        return transactionType;
    }

    public TransactionType saveTransactionType(TransactionType transactionType) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (transactionType == null) {
            throw new NullParameterException("transactionType", null);
        }
        return (TransactionType) saveEntity(transactionType);
    }

    //TransactionSource
    public List<TransactionSource> getTransactionSource(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<TransactionSource>) listEntities(TransactionSource.class, request, logger, getMethodName());
    }

    public List<CommissionItem> getCommissionItems(Long transactionId) throws EmptyListException, GeneralException, NullParameterException {
        List<CommissionItem> commissionItems = null;
        if (transactionId == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "transactionId"), null);
        }
        try {
            commissionItems = entityManager.createQuery("SELECT c FROM CommissionItem c WHERE c.transactionId.id='" + transactionId + "'").setHint("toplink.refresh", "true").getResultList();
        } catch (Exception e) {
            throw new GeneralException("Exception in method loadEnterprise: Exception text: " + e.getMessage(), e.getStackTrace());
        }
        if (commissionItems.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return commissionItems;
    }

    //CollectionType
    public List<CollectionType> getCollectionType(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<CollectionType>) listEntities(CollectionType.class, request, logger, getMethodName());

    }

    public List<CollectionType> getCollectionTypeByCountry(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<CollectionType> collectionTypeByCountry = null;
        collectionTypeByCountry = (List<CollectionType>) getNamedQueryResult(UtilsEJB.class, QueryConstants.COLLECTION_TYPE_BY_COUNTRY, request, getMethodName(), logger, "collectionTypeByCountry");
        return collectionTypeByCountry;
    }

    public List<CollectionType> getCollectionTypeByCountryByPersonType(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<CollectionType> collectionTypeByCountry = null;
        collectionTypeByCountry = (List<CollectionType>) getNamedQueryResult(UtilsEJB.class, QueryConstants.COLLECTION_TYPE_BY_COUNTRY_BY_PERSON_TYPE, request, getMethodName(), logger, "CollectionTypeByCountryByPersonType");
        return collectionTypeByCountry;
    }

    public CollectionType loadCollectionType(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        CollectionType collectionType = (CollectionType) loadEntity(CollectionType.class, request, logger, getMethodName());
        return collectionType;
    }

    public CollectionType saveCollectionType(CollectionType collectionType) throws NullParameterException, GeneralException {
        if (collectionType == null) {
            throw new NullParameterException("collectionType", null);
        }
        return (CollectionType) saveEntity(collectionType);
    }

    public CollectionType searchCollectionType(String description) throws RegisterNotFoundException, NullParameterException, GeneralException {
        CollectionType collectionType = new CollectionType();
        try {
            if (description == null) {
                throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "description"), null);
            }
            StringBuilder sqlBuilder = new StringBuilder("SELECT DISTINCT c FROM CollectionType c ");
            sqlBuilder.append("WHERE c.description LIKE '").append(description).append("'");
            collectionType = (CollectionType) createQuery(sqlBuilder.toString()).setHint("toplink.refresh", "true").getSingleResult();

        } catch (NoResultException ex) {
            throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_REGISTER_NOT_FOUND_EXCEPTION, CollectionType.class.getSimpleName(), "loadCollectionTypeByDescription", CollectionType.class.getSimpleName(), null), ex);
        } catch (Exception ex) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), ex.getMessage()), ex);
        }
        return collectionType;
    }

    public List<CollectionType> getSearchCollectionType(String name) throws EmptyListException, GeneralException, NullParameterException {
        List<CollectionType> collectionTypeList = null;
        if (name == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "name"), null);
        }
        try {
            StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM collection_type c ");
            sqlBuilder.append("WHERE c.countryId IN (SELECT c.id FROM country c WHERE c.name LIKE '").append(name).append("%')");
            Query query = entityManager.createNativeQuery(sqlBuilder.toString(), CollectionType.class);
            collectionTypeList = query.setHint("toplink.refresh", "true").getResultList();

        } catch (NoResultException ex) {
            throw new EmptyListException("No distributions found");
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        return collectionTypeList;
    }
    
    public List<CollectionType> getSearchCollectionTypeByOriginAplication(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<CollectionType> collectionTypeList = null;
        Map<String, Object> params = request.getParams();
        try {
            StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM collection_type ct ");
            sqlBuilder.append("WHERE ct.countryId IN");
            if (params.containsKey(QueryConstants.PARAM_COUNTRY_NAME)) {
                sqlBuilder.append("(SELECT c.id FROM country c WHERE c.name LIKE '").append(params.get(QueryConstants.PARAM_COUNTRY_NAME)).append("%')");
            }
            if (params.containsKey(QueryConstants.PARAM_ORIGIN_APPLICATION_ID)) {
                sqlBuilder.append("AND ct.personTypeId IN");
                sqlBuilder.append("(SELECT pt.id FROM alodigaWallet.person_type pt Where pt.originApplicationId= ").append(params.get(QueryConstants.PARAM_ORIGIN_APPLICATION_ID)).append(")");
            }
            System.out.println("sql " + sqlBuilder.toString());
            Query query = entityManager.createNativeQuery(sqlBuilder.toString(), CollectionType.class);
            collectionTypeList = query.setHint("toplink.refresh", "true").getResultList();

        } catch (NoResultException ex) {
            throw new EmptyListException("No distributions found");
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        return collectionTypeList;
    }

    //BusinessCategory
    public List<BusinessCategory> getBusinessCategory(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<BusinessCategory>) listEntities(BusinessCategory.class, request, logger, getMethodName());
    }

    public BusinessCategory loadBusinessCategory(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        BusinessCategory businessCategory = (BusinessCategory) loadEntity(BusinessCategory.class, request, logger, getMethodName());
        return businessCategory;
    }

    public BusinessCategory saveBusinessCategory(BusinessCategory businessCategory) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (businessCategory == null) {
            throw new NullParameterException("businessCategory", null);
        }
        return (BusinessCategory) saveEntity(businessCategory);
    }

    public List<BusinessCategory> getSearchBusinessCategory(String name) throws EmptyListException, GeneralException, NullParameterException {
        List<BusinessCategory> businessCategoryList = null;
        if (name == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "name"), null);
        }
        try {
            StringBuilder sqlBuilder = new StringBuilder("SELECT DISTINCT b FROM BusinessCategory b ");
            sqlBuilder.append("WHERE b.description LIKE '").append(name).append("%'");

            Query query = entityManager.createQuery(sqlBuilder.toString());
            businessCategoryList = query.setHint("toplink.refresh", "true").getResultList();

        } catch (NoResultException ex) {
            throw new EmptyListException("No distributions found");
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        return businessCategoryList;
    }

    public List<BusinessCategory> getValidateCodeMCC(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<BusinessCategory> businessCategoryList = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_MCC_CODE)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_MCC_CODE), null);
        }
        businessCategoryList = (List<BusinessCategory>) getNamedQueryResult(BusinessCategory.class, QueryConstants.CODEMCC_EXIST_IN_BD_BUSINESS_CATEGORY, request, getMethodName(), logger, "codeMCCExistInBdBusinessCategory");
        return businessCategoryList;
    }

    //BusinessSubCategory
    public List<BusinessSubCategory> getBusinessSubCategory(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<BusinessSubCategory>) listEntities(BusinessSubCategory.class, request, logger, getMethodName());
    }

    public List<BusinessSubCategory> getBusinessSubCategoryByCategory(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<BusinessSubCategory> businessSubCategoryList = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_BUSINESS_CATEGORY_ID)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_BUSINESS_CATEGORY_ID), null);
        }
        businessSubCategoryList = (List<BusinessSubCategory>) getNamedQueryResult(BusinessSubCategory.class, QueryConstants.BUSINESS_SUB_CATEGORY_BY_CATEGORY, request, getMethodName(), logger, "businessSubCategoryList");
        return businessSubCategoryList;
    }

    public BusinessSubCategory loadBusinessSubCategory(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        BusinessSubCategory businessSubCategory = (BusinessSubCategory) loadEntity(BusinessSubCategory.class, request, logger, getMethodName());
        return businessSubCategory;
    }

    public BusinessSubCategory saveBusinessSubCategory(BusinessSubCategory businessSubCategory) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (businessSubCategory == null) {
            throw new NullParameterException("businessSubCategory", null);
        }
        return (BusinessSubCategory) saveEntity(businessSubCategory);
    }

    public List<BusinessSubCategory> getSearchBusinessSubCategory(String name) throws EmptyListException, GeneralException, NullParameterException {
        List<BusinessSubCategory> businessSubCategoryList = null;
        if (name == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "name"), null);
        }
        try {
            StringBuilder sqlBuilder = new StringBuilder("SELECT DISTINCT b FROM BusinessSubCategory b ");
            sqlBuilder.append("WHERE b.description LIKE '").append(name).append("%'");

            Query query = entityManager.createQuery(sqlBuilder.toString());
            businessSubCategoryList = query.setHint("toplink.refresh", "true").getResultList();

        } catch (NoResultException ex) {
            throw new EmptyListException("No distributions found");
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        return businessSubCategoryList;
    }

    public List<BusinessSubCategory> getBusinessSubCategoryValidateCodeMCC(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<BusinessSubCategory> businessSubCategoryList = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_MCC_CODE)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_MCC_CODE), null);
        }
        businessSubCategoryList = (List<BusinessSubCategory>) getNamedQueryResult(BusinessCategory.class, QueryConstants.CODEMCC_EXIST_IN_BD_BUSINESS_SUB_CATEGORY, request, getMethodName(), logger, "codeMCCExistInBdBusinessSubCategory");
        return businessSubCategoryList;
    }

    //BusinessType
    public List<BusinessType> getBusinessType(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<BusinessType>) listEntities(BusinessType.class, request, logger, getMethodName());
    }

    public BusinessType saveBusinessType(BusinessType businessType) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (businessType == null) {
            throw new NullParameterException("businessType", null);
        }
        return (BusinessType) saveEntity(businessType);
    }

    public BusinessType saveNewBusinessType(BusinessType businessType) throws RegisterNotFoundException, NullParameterException, GeneralException, DuplicateEntryException {
        try {
            BusinessType businessTypeResponse = new BusinessType();
            businessTypeResponse = loadBussinesTypeByCode(businessType.getCode());
            if (businessTypeResponse != null) {
                throw new DuplicateEntryException("DuplicateEntryException");
            }
        } catch (RegisterNotFoundException e) {
            businessType = (BusinessType) saveEntity(businessType);
            return businessType;
        } catch (GeneralException e) {
            throw new GeneralException(e.getMessage());
        }
        return businessType;
    }

    public BusinessType loadBussinesTypeByCode(String referenceCode) throws RegisterNotFoundException, NullParameterException, GeneralException {
        BusinessType businessType = new BusinessType();
        try {
            if (referenceCode == null) {
                throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "referenceCode"), null);
            }

            Query query = createQuery("SELECT b FROM BusinessType b WHERE b.code = ?1");
            query.setParameter("1", referenceCode);
            businessType = (BusinessType) query.setHint("toplink.refresh", "true").getSingleResult();
        } catch (NoResultException ex) {
            System.out.println("code: " + referenceCode);
            throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_REGISTER_NOT_FOUND_EXCEPTION, Country.class.getSimpleName(), "loadCountryByShortName", Country.class.getSimpleName(), null), ex);
        } catch (Exception ex) {
            System.out.println("code: " + referenceCode);
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), ex.getMessage()), ex);
        }
        return businessType;
    }

    //BusinessServiceType
    public List<BusinessServiceType> getBusinessServiceType(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<BusinessServiceType>) listEntities(BusinessServiceType.class, request, logger, getMethodName());
    }

    public List<BusinessServiceType> getBusinessServiceTypeByBusinessType(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        List<BusinessServiceType> businessServiceList = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(QueryConstants.PARAM_BUSINESS_TYPE_ID)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), QueryConstants.PARAM_BUSINESS_TYPE_ID), null);
        }
        try {
            businessServiceList = (List<BusinessServiceType>) getNamedQueryResult(BusinessServiceType.class, QueryConstants.BUSINESS_SERVICE_TYPE_BY_BUSINESS_TYPE, request, getMethodName(), logger, "businessServiceList");
        } catch (EmptyListException e) {
            throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName(), "businessServiceList"), null);
        }
        return businessServiceList;
    }

    public BusinessServiceType saveBusinessServiceType(BusinessServiceType businessServiceType) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (businessServiceType == null) {
            throw new NullParameterException("businessServiceType", null);
        }
        return (BusinessServiceType) saveEntity(businessServiceType);
    }

    public List<BusinessServiceType> getBusinessServiceTypeValidateCode(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<BusinessServiceType> businessServiceTypeList = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_CODE)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_CODE), null);
        }
        businessServiceTypeList = (List<BusinessServiceType>) getNamedQueryResult(BusinessServiceType.class, QueryConstants.CODE_EXIST_IN_BD_BUSINESS_SERVICE_TYPE, request, getMethodName(), logger, "businessServiceTypeList");
        return businessServiceTypeList;
    }

    //StatusTransactionApproveRequest
    public List<StatusTransactionApproveRequest> getStatusTransactionApproveRequest(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<StatusTransactionApproveRequest>) listEntities(StatusTransactionApproveRequest.class, request, logger, getMethodName());
    }

    public List<StatusTransactionApproveRequest> getStatusTransactionApproveRequestPending(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<StatusTransactionApproveRequest> statusTransactionApproveRequestPendingList = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_CODE)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_CODE), null);
        }
        statusTransactionApproveRequestPendingList = (List<StatusTransactionApproveRequest>) getNamedQueryResult(StatusTransactionApproveRequest.class, QueryConstants.CODE_BY_STATUS, request, getMethodName(), logger, "statusTransactionApproveRequestPendingList");
        return statusTransactionApproveRequestPendingList;
    }

    public StatusTransactionApproveRequest loadStatusTransactionApproveRequest(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        StatusTransactionApproveRequest statusTransactionApproveRequest = (StatusTransactionApproveRequest) loadEntity(StatusTransactionApproveRequest.class, request, logger, getMethodName());
        return statusTransactionApproveRequest;
    }

    public StatusTransactionApproveRequest saveStatusTransactionApproveRequest(StatusTransactionApproveRequest statusTransactionApproveRequest) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (statusTransactionApproveRequest == null) {
            throw new NullParameterException("statusTransactionApproveRequest", null);
        }
        return (StatusTransactionApproveRequest) saveEntity(statusTransactionApproveRequest);
    }

    //BankOperation
    public List<BankOperation> getBankOperation(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<BankOperation>) listEntities(BankOperation.class, request, logger, getMethodName());
    }

    public BankOperation loadBankOperation(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        BankOperation bankOperation = (BankOperation) loadEntity(BankOperation.class, request, logger, getMethodName());
        return bankOperation;
    }

    public BankOperation saveBankOperation(BankOperation bankOperation) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (bankOperation == null) {
            throw new NullParameterException("bankOperation", null);
        }
        return (BankOperation) saveEntity(bankOperation);
    }

    //CollectionsRequest
    public List<CollectionsRequest> getCollectionsRequest(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<CollectionsRequest>) listEntities(CollectionsRequest.class, request, logger, getMethodName());
    }

    public List<CollectionsRequest> getCollectionsByPersonType(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<CollectionsRequest> collectionsRequestByPersonTypeList = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_PERSON_TYPE_ID)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_PERSON_TYPE_ID), null);
        }
        collectionsRequestByPersonTypeList = (List<CollectionsRequest>) getNamedQueryResult(CollectionsRequest.class, QueryConstants.COLLECTIONS_BY_PERSON_TYPE, request, getMethodName(), logger, "collectionsRequestByPersonTypeList");
        return collectionsRequestByPersonTypeList;
    }

    public List<CollectionsRequest> getCollectionsRequestByID(CollectionsRequest collectionsRequest) throws GeneralException, EmptyListException, NullParameterException {
        List<CollectionsRequest> collectionsRequestList = null;
        try {
            if (collectionsRequest == null) {
                throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "collectionsRequest"), null);
            }      //To change body of generated methods, choose Tools | Templates.

            StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM collections_request where collectionTypeId=");
            sqlBuilder.append(collectionsRequest.getCollectionTypeId().getId());
            sqlBuilder.append(" and personTypeId=");
            sqlBuilder.append(collectionsRequest.getPersonTypeId().getId());
            Query query = entityManager.createNativeQuery(sqlBuilder.toString(), CollectionsRequest.class);
            collectionsRequestList = (List<CollectionsRequest>) query.setHint("toplink.refresh", "true").getResultList();

        } catch (Exception ex) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), ex.getMessage()), ex);
        }
        return collectionsRequestList;
    }

    public CollectionsRequest loadCollectionsRequest(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        CollectionsRequest collectionsRequest = (CollectionsRequest) loadEntity(CollectionsRequest.class, request, logger, getMethodName());
        return collectionsRequest;
    }

    public CollectionsRequest saveCollectionsRequest(CollectionsRequest collectionsRequest) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (collectionsRequest == null) {
            throw new NullParameterException("collectionsRequest", null);
        }
        return (CollectionsRequest) saveEntity(collectionsRequest);
    }

    public List<CollectionsRequest> searchCollectionsRequestByCountry(String name) throws EmptyListException, GeneralException, NullParameterException {
        List<CollectionsRequest> collectionsRequest = null;
        try {
            if (name == null) {
                throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "name"), null);
            }

            StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM collections_request cr ");
            sqlBuilder.append("WHERE cr.collectionTypeId IN (SELECT c.id FROM collection_type c WHERE c.countryId ");
            sqlBuilder.append("IN (SELECT c.id FROM country c WHERE c.name LIKE '").append(name).append("%'))");
            Query query = entityManager.createNativeQuery(sqlBuilder.toString(), CollectionsRequest.class);
            collectionsRequest = query.setHint("toplink.refresh", "true").getResultList();

        } catch (NoResultException ex) {
            throw new EmptyListException("No distributions found");
        } catch (Exception ex) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), ex.getMessage()), ex);
        }
        return collectionsRequest;
    }

    @Override
    public List<StatusRequest> getStatusRequest(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<StatusRequest> statusBusinessAffiliationRequests = (List<StatusRequest>) listEntities(StatusRequest.class, request, logger, getMethodName());
        return statusBusinessAffiliationRequests;
    }

    @Override
    public StatusRequest loadStatusRequest(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        StatusRequest statusBusinessAffiliationRequest = (StatusRequest) loadEntity(StatusRequest.class, request, logger, getMethodName());
        return statusBusinessAffiliationRequest;
    }

    @Override
    public StatusRequest saveStatusRequest(StatusTransactionApproveRequest statusTransactionApproveRequest) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (statusTransactionApproveRequest == null) {
            throw new NullParameterException("statusTransactionApproveRequest", null);
        }
        return (StatusRequest) saveEntity(statusTransactionApproveRequest);
    }

    @Override
    public List<StatusBusinessAffiliationHasFinalState> getStatusBusinessAffiliationHasFinalState(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<StatusBusinessAffiliationHasFinalState> hasFinalStates = (List<StatusBusinessAffiliationHasFinalState>) listEntities(StatusBusinessAffiliationHasFinalState.class, request, logger, getMethodName());
        return hasFinalStates;
    }

    @Override
    public StatusBusinessAffiliationHasFinalState loadStatusBusinessAffiliationHasFinalState(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        StatusBusinessAffiliationHasFinalState hasFinalState = (StatusBusinessAffiliationHasFinalState) loadEntity(StatusBusinessAffiliationHasFinalState.class, request, logger, getMethodName());
        return hasFinalState;
    }

    @Override
    public StatusBusinessAffiliationHasFinalState saveStatusBusinessAffiliationHasFinalState(StatusBusinessAffiliationHasFinalState statusBusinessAffiliationHasFinalState) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (statusBusinessAffiliationHasFinalState == null) {
            throw new NullParameterException("statusBusinessAffiliationHasFinalState", null);
        }
        return (StatusBusinessAffiliationHasFinalState) saveEntity(statusBusinessAffiliationHasFinalState);
    }

    //BusinessAffiliationRequets
    @Override
    public List<AffiliationRequest> getAffiliationRequest(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<AffiliationRequest> affiliationRequests = new ArrayList<AffiliationRequest>();
        try {
            affiliationRequests = (List<AffiliationRequest>) entityManager.createNamedQuery("AffiliationRequest.findAll", AffiliationRequest.class).setHint("toplink.refresh", "true").getResultList();
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (affiliationRequests.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return affiliationRequests;
    }

    @Override
    public AffiliationRequest loadAffiliationRequest(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        AffiliationRequest affiliationRequest = (AffiliationRequest) loadEntity(AffiliationRequest.class, request, logger, getMethodName());
        return affiliationRequest;
    }

    @Override
    public AffiliationRequest saveAffiliationRequest(AffiliationRequest affiliationRequest) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (affiliationRequest == null) {
            throw new NullParameterException("businessAffiliationRequest", null);
        }
        return (AffiliationRequest) saveEntity(affiliationRequest);
    }

    @Override
    public List<AffiliationRequest> searchAffiliationRequestByParams(EJBRequest request) throws GeneralException, NullParameterException, EmptyListException {
        List<AffiliationRequest> operations = new ArrayList<AffiliationRequest>();

        Map<String, Object> params = request.getParams();
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM affiliation_request t WHERE t.requestTypeId=").append(params.get(QueryConstants.PARAM_REQUEST_TYPE));

        if (params.containsKey(QueryConstants.PARAM_NUMBER_REQUEST)) {
            sqlBuilder.append(" AND t.numberRequest LIKE '").append(params.get(QueryConstants.PARAM_NUMBER_REQUEST)).append("%'");
        }
        Query query = entityManager.createNativeQuery(sqlBuilder.toString(), AffiliationRequest.class);
        operations = (List<AffiliationRequest>) query.setHint("toplink.refresh", "true").getResultList();
        if (operations.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return operations;
    }

    @Override
    public List<AffiliationRequest> getAffiliationRequestByRequestByType(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<AffiliationRequest> affiliationRequestList = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_REQUEST_TYPE)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_REQUEST_TYPE), null);
        }
        affiliationRequestList = (List<AffiliationRequest>) getNamedQueryResult(AffiliationRequest.class, QueryConstants.AFFILIATION_REQUEST_BY_REQUEST_TYPE_ID, request, getMethodName(), logger, "affiliationRequestList");
        return affiliationRequestList;
    }

    @Override
    public List<AffiliationRequest> getAffiliationRequestByUserByType(EJBRequest request) throws GeneralException, NullParameterException, EmptyListException {
        List<AffiliationRequest> operations = new ArrayList<AffiliationRequest>();
        try {

            Map<String, Object> params = request.getParams();
            StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM affiliation_request t WHERE t.requestTypeId=").append(params.get(QueryConstants.PARAM_REQUEST_TYPE));

            if (params.containsKey(QueryConstants.PARAM_USER_REGISTER_ID)) {
                sqlBuilder.append(" AND t.userRegisterUnifiedId=").append(params.get(QueryConstants.PARAM_USER_REGISTER_ID));
            }

            if (params.containsKey(QueryConstants.PARAM_BUSINESS_PERSON_ID)) {
                sqlBuilder.append(" AND t.businessPersonId=").append(params.get(QueryConstants.PARAM_BUSINESS_PERSON_ID));
            }

            Query query = entityManager.createNativeQuery(sqlBuilder.toString(), AffiliationRequest.class);
            operations = (List<AffiliationRequest>) query.setHint("toplink.refresh", "true").getResultList();
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (operations.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return operations;
    }
    
    @Override
    public List<AffiliationRequest> getAffiliationRequestByPerson(EJBRequest request) throws GeneralException, NullParameterException, EmptyListException {
        List<AffiliationRequest> operations = new ArrayList<AffiliationRequest>();

            Map<String, Object> params = request.getParams();
            StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM affiliation_request t WHERE ");
            
            if (params.containsKey(QueryConstants.PARAM_USER_REGISTER_ID)) {
                sqlBuilder.append("t.userRegisterUnifiedId=").append(params.get(QueryConstants.PARAM_USER_REGISTER_ID)).append("");
            }
            
            if (params.containsKey(QueryConstants.PARAM_BUSINESS_PERSON_ID)) {
                sqlBuilder.append("t.businessPersonId=").append(params.get(QueryConstants.PARAM_BUSINESS_PERSON_ID)).append("");
            }
            
            Query query = entityManager.createNativeQuery(sqlBuilder.toString(), AffiliationRequest.class);
            operations = (List<AffiliationRequest>) query.setHint("toplink.refresh", "true").getResultList();
            if (operations.isEmpty()) {
                throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
            }
            return operations;
    }
    
    @Override
    public List<AffiliationRequest> getAffiliationRequestByLegalPerson(EJBRequest request) throws GeneralException, NullParameterException, EmptyListException {
        List<AffiliationRequest> affiliationRequestList = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_LEGAL_PERSON_ID)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_LEGAL_PERSON_ID), null);
        }
        affiliationRequestList = (List<AffiliationRequest>) getNamedQueryResult(AffiliationRequest.class, QueryConstants.AFFILIATION_REQUEST_BY_LEGAL_PERSON, request, getMethodName(), logger, "affiliationRequestList");
        return affiliationRequestList;
    }

    @Override
    public boolean validateStatusBusinessAffiliationHasFinalState(Integer statusId, Integer finalId) throws GeneralException, NullParameterException {
        if (statusId == null || finalId == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "statusId or finalId"), null);
        }
        boolean valid = false;
        List<PreferenceValue> preferenceValues = new ArrayList<PreferenceValue>();
        Query query = null;
        try {
            query = createQuery("SELECT s FROM StatusBusinessAffiliationHasFinalState s WHERE s.statusBusinessAffiliationRequetsId.id=?1 AND s.finalStateId.id=?2");
            query.setParameter("1", statusId);
            query.setParameter("2", finalId);
            preferenceValues = query.setHint("toplink.refresh", "true").getResultList();
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (preferenceValues.isEmpty()) {
            valid = true;
        }
        return valid;
    }

    //ReviewBusinessAffiliationRequest
    public List<ReviewAffiliationRequest> getReviewAffiliationRequest(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<ReviewAffiliationRequest>) listEntities(ReviewAffiliationRequest.class, request, logger, getMethodName());
    }

    public ReviewAffiliationRequest loadReviewAffiliationRequest(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        ReviewAffiliationRequest reviewBusinessAffiliationRequest = (ReviewAffiliationRequest) loadEntity(ReviewAffiliationRequest.class, request, logger, getMethodName());
        return reviewBusinessAffiliationRequest;
    }

    public List<ReviewAffiliationRequest> getReviewRequestByRequest(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<ReviewAffiliationRequest> reviewRequest = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_AFFILIATION_REQUEST)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_REQUEST_ID), null);
        }
        if (!params.containsKey(EjbConstants.PARAM_REVIEW_REQUEST_TYPE_ID)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_REVIEW_REQUEST_TYPE_ID), null);
        }
        reviewRequest = (List<ReviewAffiliationRequest>) getNamedQueryResult(ReviewAffiliationRequest.class, QueryConstants.REVIEW_REQUEST_BY_REQUEST, request, getMethodName(), logger, "reviewRequest");
        return reviewRequest;
    }

    public ReviewAffiliationRequest saveReviewAffiliationRequest(ReviewAffiliationRequest reviewAffiliationRequest) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (reviewAffiliationRequest == null) {
            throw new NullParameterException("reviewAffiliationRequest", null);
        }
        return (ReviewAffiliationRequest) saveEntity(reviewAffiliationRequest);
    }

    //RequestHasCollectionRequest
    @Override
    public List<RequestHasCollectionRequest> getRequestsHasCollectionsRequest(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<RequestHasCollectionRequest> requestHasCollectionsRequest = (List<RequestHasCollectionRequest>) listEntities(RequestHasCollectionRequest.class, request, logger, getMethodName());
        return requestHasCollectionsRequest;
    }

    @Override
    public RequestHasCollectionRequest loadRequestHasCollectionsRequest(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        RequestHasCollectionRequest requestHasCollectionsRequest = (RequestHasCollectionRequest) loadEntity(RequestHasCollectionRequest.class, request, logger, getMethodName());
        return requestHasCollectionsRequest;
    }

    @Override
    public RequestHasCollectionRequest saveRequestHasCollectionsRequest(RequestHasCollectionRequest requestHasCollectionsRequest) throws NullParameterException, GeneralException {
        if (requestHasCollectionsRequest == null) {
            throw new NullParameterException("requestHasCollectionsRequest", null);
        }
        return (RequestHasCollectionRequest) saveEntity(requestHasCollectionsRequest);
    }

    @Override
    public List<RequestHasCollectionRequest> getRequestsHasCollectionsRequestByRequestByCollectionRequest(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<RequestHasCollectionRequest> requestHasCollectionsRequestList = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_AFFILIATION_REQUEST)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_AFFILIATION_REQUEST), null);
        }
        if (!params.containsKey(EjbConstants.PARAM_COLLECTION_REQUEST_ID)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_COLLECTION_REQUEST_ID), null);
        }
        requestHasCollectionsRequestList = (List<RequestHasCollectionRequest>) getNamedQueryResult(RequestHasCollectionRequest.class, QueryConstants.REQUEST_HAS_COLLECTION_REQUEST_BY_REQUEST_BY_COLLECTION_REQUEST, request, getMethodName(), logger, "requestHasCollectionsRequestList");
        return requestHasCollectionsRequestList;
    }

    @Override
    public List<RequestHasCollectionRequest> getRequestsHasCollectionsRequestByBusinessAffiliationRequest(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<RequestHasCollectionRequest> requestHasCollectionsRequestList = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_AFFILIATION_REQUEST)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_AFFILIATION_REQUEST), null);
        }
        requestHasCollectionsRequestList = (List<RequestHasCollectionRequest>) getNamedQueryResult(RequestHasCollectionRequest.class, QueryConstants.REQUEST_HAS_COLLECTION_REQUEST_BY_AFFILIATON_REQUEST, request, getMethodName(), logger, "requestHasCollectionsRequestList");
        return requestHasCollectionsRequestList;
    }

    public List<RequestHasCollectionRequest> searchRequestHasCollectionRequest(EJBRequest request) throws GeneralException, NullParameterException, EmptyListException {
        List<RequestHasCollectionRequest> requestHasCollectionList = new ArrayList<RequestHasCollectionRequest>();

        Map<String, Object> params = request.getParams();
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM request_has_collection_request r WHERE r.affiliationRequestId =").append(params.get(QueryConstants.PARAM_AFFILIATION_REQUEST_ID));
        sqlBuilder.append(" AND r.collectionsRequestId IN (SELECT c.id FROM collections_request c WHERE");
        sqlBuilder.append(" c.collectionTypeId IN (SELECT c.id FROM collection_type c WHERE c.description LIKE '").append(params.get(QueryConstants.PARAM_NAME)).append("%'))");

        Query query = entityManager.createNativeQuery(sqlBuilder.toString(), AffiliationRequest.class);
        requestHasCollectionList = (List<RequestHasCollectionRequest>) query.setHint("toplink.refresh", "true").getResultList();
        if (requestHasCollectionList.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return requestHasCollectionList;
    }

    @Override
    public void updateBusinessAffiliationRequest(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<RequestHasCollectionRequest> requestHasCollectionsRequestList = new ArrayList<RequestHasCollectionRequest>();
        boolean complet = false;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_AFFILIATION_REQUEST)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_AFFILIATION_REQUEST), null);
        }
        Long businessAffiliationRequestId = (Long) params.get(EjbConstants.PARAM_AFFILIATION_REQUEST);
        try {
            requestHasCollectionsRequestList = (List<RequestHasCollectionRequest>) getNamedQueryResult(RequestHasCollectionRequest.class, QueryConstants.REQUEST_HAS_COLLECTION_REQUEST_BY_AFFILIATON_REQUEST_COMPLET, request, getMethodName(), logger, "requestHasCollectionsRequestList");
        } catch (EmptyListException e) {

        }
        if (requestHasCollectionsRequestList.isEmpty()) {
            params = new HashMap();
            params.put(Constants.PARAM_CODE, Constants.STATUS_BUSINESS_AFFILIATION_REQUEST_COMPLET);
            request.setParams(params);
            try {
                StatusRequest status = loadStatusBusinessAffiliationRequestByCode(request);
                request = new EJBRequest();
                request.setParam(businessAffiliationRequestId);
                AffiliationRequest businessAffiliationRequest = loadAffiliationRequest(request);
                businessAffiliationRequest.setUpdateDate(new Timestamp(new Date().getTime()));
                businessAffiliationRequest.setStatusRequestId(status);
                saveAffiliationRequest(businessAffiliationRequest);
            } catch (RegisterNotFoundException e) {

            }
        } else {
            try {
                requestHasCollectionsRequestList = new ArrayList<RequestHasCollectionRequest>();
                requestHasCollectionsRequestList = (List<RequestHasCollectionRequest>) getNamedQueryResult(RequestHasCollectionRequest.class, QueryConstants.REQUEST_HAS_COLLECTION_REQUEST_BY_AFFILIATON_REQUEST_INCOMPLET, request, getMethodName(), logger, "requestHasCollectionsRequestList");
            } catch (EmptyListException e) {

            }
            if (!requestHasCollectionsRequestList.isEmpty()) {
                params = new HashMap();
                params.put(Constants.PARAM_CODE, Constants.STATUS_BUSINESS_AFFILIATION_REQUEST_INCOMPLET);
                request.setParams(params);
                try {
                    StatusRequest status = loadStatusBusinessAffiliationRequestByCode(request);
                    request = new EJBRequest();
                    request.setParam(businessAffiliationRequestId);
                    AffiliationRequest businessAffiliationRequest = loadAffiliationRequest(request);
                    businessAffiliationRequest.setStatusRequestId(status);
                    businessAffiliationRequest.setUpdateDate(new Timestamp(new Date().getTime()));
                    saveAffiliationRequest(businessAffiliationRequest);
                } catch (RegisterNotFoundException e) {

                }
            }
        }
    }

    @Override
    public StatusRequest loadStatusBusinessAffiliationRequestByCode(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException, EmptyListException {
        List<StatusRequest> status = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_CODE)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_CODE), null);
        }
        status = (List<StatusRequest>) getNamedQueryResult(StatusRequest.class, QueryConstants.STATUS_BUSINESS_AFFILIATON_REQUEST_BY_CODE, request, getMethodName(), logger, "status");
        return status.get(0);
    }

    //StatusCard
    public List<StatusCard> getStatusCard(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<StatusCard>) listEntities(StatusCard.class, request, logger, getMethodName());
    }

    public StatusCard saveStatusCard(StatusCard statusCard) throws NullParameterException, GeneralException {
        if (statusCard == null) {
            throw new NullParameterException("statusCard", null);
        }
        return (StatusCard) saveEntity(statusCard);
    }

    //StatusCardHasFinalState
    public List<StatusCardHasFinalState> getStatusCardHasFinalState(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<StatusCardHasFinalState>) listEntities(StatusCardHasFinalState.class, request, logger, getMethodName());
    }

    @Override
    public List<StatusCardHasFinalState> getStatusCardById(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<StatusCardHasFinalState> statusCardHasFinalStates = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_STATUS_ID)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_STATUS_ID), null);
        }
        statusCardHasFinalStates = (List<StatusCardHasFinalState>) getNamedQueryResult(UtilsEJB.class, QueryConstants.CARDSTATUS_BY_ID, request, getMethodName(), logger, "zipZones");
        return statusCardHasFinalStates;
    }

    public boolean validateStatusCardHasFinalState(Integer statusId, Integer finalId) throws GeneralException, NullParameterException {
        if (statusId == null || finalId == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "statusId or finalId"), null);
        }
        boolean valid = false;
        List<PreferenceValue> preferenceValues = new ArrayList<PreferenceValue>();
        Query query = null;
        try {
            query = createQuery("SELECT s FROM StatusCardHasFinalState s WHERE s.statusCardId.id=?1 AND s.statusCardFinalStateId.id=?2");
            query.setParameter("1", statusId);
            query.setParameter("2", finalId);
            preferenceValues = query.setHint("toplink.refresh", "true").getResultList();
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (preferenceValues.isEmpty()) {
            valid = true;
        }
        return valid;
    }

    public StatusCardHasFinalState saveStatusCardHasFinalState(StatusCardHasFinalState statusCardHasFinalState) throws NullParameterException, GeneralException {
        if (statusCardHasFinalState == null) {
            throw new NullParameterException("StatusCardHasFinalState", null);
        }
        return (StatusCardHasFinalState) saveEntity(statusCardHasFinalState);
    }

    //PersonType
    public PersonType savePersonType(PersonType personType) throws NullParameterException, GeneralException {
        if (personType == null) {
            throw new NullParameterException("personType", null);
        }
        return (PersonType) saveEntity(personType);
    }

    @Override
    public List<PersonType> getPersonType(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<PersonType>) listEntities(PersonType.class, request, logger, getMethodName());
    }

    @Override
    public List<PersonType> getPersonTypeByCountryByIndNaturalPerson(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<PersonType> personTypes = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_COUNTRY_ID)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_COUNTRY_ID), null);
        }
        if (!params.containsKey(EjbConstants.PARAM_ORIGIN_APPLICATION_ID)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_ORIGIN_APPLICATION_ID), null);
        }
        if (!params.containsKey(EjbConstants.PARAM_IND_NATURAL_PERSON)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_IND_NATURAL_PERSON), null);
        }
        personTypes = (List<PersonType>) getNamedQueryResult(PersonType.class, QueryConstants.PERSON_TYPE_BY_COUNTRY_BY_IND_NATURAL_PERSON, request, getMethodName(), logger, "personTypes");
        return personTypes;
    }

    @Override
    public List<PersonType> getPersonTypeByCountryByOriginApplication(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<PersonType> personTypes = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_COUNTRY_ID)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_COUNTRY_ID), null);
        }
        if (!params.containsKey(EjbConstants.PARAM_ORIGIN_APPLICATION_ID)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_ORIGIN_APPLICATION_ID), null);
        }

        personTypes = (List<PersonType>) getNamedQueryResult(PersonType.class, QueryConstants.PERSON_TYPE_BY_COUNTRY_BY_ORIGIN_APPLICATION, request, getMethodName(), logger, "personTypeByCountryByOriginApplication");
        return personTypes;
    }

    public List<PersonType> getSearchPersonTypeByCountry(String name) throws EmptyListException, GeneralException, NullParameterException {
        List<PersonType> personType = null;
        try {
            if (name == null) {
                throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "name"), null);
            }

            StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM person_type p ");
            sqlBuilder.append("WHERE p.countryId IN (SELECT c.id FROM country c WHERE c.name LIKE '").append(name).append("%')");
            Query query = entityManager.createNativeQuery(sqlBuilder.toString(), PersonType.class);
            personType = query.setHint("toplink.refresh", "true").getResultList();

        } catch (NoResultException ex) {
            throw new EmptyListException("No distributions found");
        } catch (Exception ex) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), ex.getMessage()), ex);
        }
        return personType;
    }

    //ReviewOfac
    public List<ReviewOfac> getReviewOfac(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<ReviewOfac>) listEntities(ReviewOfac.class, request, logger, getMethodName());
    }

    public List<ReviewOfac> getReviewOfacByRequest(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<ReviewOfac> reviewOfacList = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_PERSON_ID)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_PERSON_ID), null);
        }
        if (!params.containsKey(EjbConstants.PARAM_AFFILIATION_REQUEST)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_AFFILIATION_REQUEST), null);
        }
        reviewOfacList = (List<ReviewOfac>) getNamedQueryResult(ReviewOfac.class, QueryConstants.REVIEW_OFAC_BY_REQUEST, request, getMethodName(), logger, "reviewOfacList");
        return reviewOfacList;
    }

    public ReviewOfac loadReviewOfac(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        ReviewOfac reviewOfac = (ReviewOfac) loadEntity(ReviewOfac.class, request, logger, getMethodName());
        return reviewOfac;
    }

    public ReviewOfac saveReviewOfac(ReviewOfac reviewOfac) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (reviewOfac == null) {
            throw new NullParameterException("reviewOfac", null);
        }
        return (ReviewOfac) saveEntity(reviewOfac);
    }

    //ReviewType
    public List<ReviewType> getReviewType(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<ReviewType>) listEntities(ReviewType.class, request, logger, getMethodName());
    }

    public ReviewType loadReviewType(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        ReviewType reviewType = (ReviewType) loadEntity(ReviewType.class, request, logger, getMethodName());
        return reviewType;
    }

    public ReviewType saveReviewType(ReviewType reviewType) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (reviewType == null) {
            throw new NullParameterException("reviewType", null);
        }
        return (ReviewType) saveEntity(reviewType);
    }

    //OriginApplication
    public List<OriginApplication> getOriginApplications(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<OriginApplication> originApplications = (List<OriginApplication>) listEntities(OriginApplication.class, request, logger, getMethodName());
        return originApplications;
    }

    @Override
    public OriginApplication loadOriginApplicationByCode(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException, EmptyListException {
        List<OriginApplication> status = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_CODE)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_CODE), null);
        }
        status = (List<OriginApplication>) getNamedQueryResult(OriginApplication.class, "OriginApplication.findByCode", request, getMethodName(), logger, "status");
        return status.get(0);
    }

    @Override
    public Integer getDocumentTypeByCode(String code) throws RegisterNotFoundException, NullParameterException, GeneralException, EmptyListException {
        DocumentType documentType = new DocumentType();
        try {
            if (code == null) {
                throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "code"), null);
            }

            Query query = createQuery("SELECT d FROM DocumentType d WHERE d.acronym = ?1");
            query.setParameter("1", code);
            documentType = (DocumentType) query.setHint("toplink.refresh", "true").getSingleResult();
        } catch (NoResultException ex) {
            throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_REGISTER_NOT_FOUND_EXCEPTION, DocumentType.class.getSimpleName(), "getDocumentTypeByCode", DocumentType.class.getSimpleName(), null), ex);
        } catch (Exception ex) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), ex.getMessage()), ex);
        }
        return documentType.getId();
    }

    //Sequences
    @Override
    public List<Sequences> getSequences(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<Sequences> sequences = (List<Sequences>) listEntities(Sequences.class, request, logger, getMethodName());
        return sequences;

    }

    @Override
    public Sequences loadSequences(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        Sequences sequence = (Sequences) loadEntity(Sequences.class, request, logger, getMethodName());
        return sequence;
    }

    @Override
    public Sequences saveSequences(Sequences sequence) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (sequence == null) {
            throw new NullParameterException("sequence", null);
        }
        return (Sequences) saveEntity(sequence);
    }

    @Override
    public List<Sequences> getSequencesByDocumentType(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<Sequences> sequence = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_DOCUMENT_TYPE_ID)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_DOCUMENT_TYPE_ID), null);
        }
        sequence = (List<Sequences>) getNamedQueryResult(UtilsEJB.class, QueryConstants.SEQUENCES_BY_DOCUMENT_TYPE, request, getMethodName(), logger, "sequence");
        return sequence;
    }

    @Override
    public String generateNumberSequence(List<Sequences> sequence, int originApplication) throws GeneralException, RegisterNotFoundException, NullParameterException {
        int numberSequence = 0;
        String prefixNumberSequence = "";
        String acronym = "";
        for (Sequences s : sequence) {
            if (s.getOriginApplicationId().getId() == originApplication) {
                if (s.getCurrentValue() > 1) {
                    numberSequence = s.getCurrentValue();
                } else {
                    numberSequence = s.getInitialValue();
                }
                acronym = s.getDocumentTypeId().getAcronym();
                s.setCurrentValue(s.getCurrentValue() + 1);
                saveSequences(s);
            }
        }
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        switch (originApplication) {
            case Constants.ORIGIN_APPLICATION_ADMIN_ID:
                prefixNumberSequence = "ADW-";
                break;
            default:
                break;
        }
        prefixNumberSequence = prefixNumberSequence.concat(acronym);
        String suffixNumberSequence = "-";
        suffixNumberSequence = suffixNumberSequence.concat(String.valueOf(year));
        String numberSequenceDoc = prefixNumberSequence;
        numberSequenceDoc = numberSequenceDoc.concat("-");
        numberSequenceDoc = numberSequenceDoc.concat(String.valueOf(numberSequence));
        numberSequenceDoc = numberSequenceDoc.concat(suffixNumberSequence);
        return numberSequenceDoc;
    }

    public Country saveNewCountry(Country country) throws RegisterNotFoundException, GeneralException, NullParameterException, DuplicateEntryException {
        try {

            Country countryResponse = new Country();
            countryResponse = loadCountryByShortName(country.getShortName());
            if (countryResponse != null) {
                throw new DuplicateEntryException("DuplicateEntryException");
            }
        } catch (RegisterNotFoundException e) {
            country = (Country) saveEntity(country);
            return country;
        } catch (GeneralException e) {
            throw new GeneralException(e.getMessage());
        }
        return country;
    }

    //Calendar Days
    public List<CalendarDays> getCalendarDays(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<CalendarDays> calendarDays = (List<CalendarDays>) listEntities(CalendarDays.class, request, logger, getMethodName());
        return calendarDays;
    }

    @Override
    public CalendarDays saveCalendarDays(CalendarDays calendarDays) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (calendarDays == null) {
            throw new NullParameterException("calendarDays", null);
        }
        return (CalendarDays) saveEntity(calendarDays);
    }

    @Override
    public ExchangeRate getExchangeRateByBeginningDate(Product productId, Date creationDate) throws EmptyListException, GeneralException, NullParameterException, RegisterNotFoundException {
        ExchangeRate exchangeRate = new ExchangeRate();
        try {

            if (productId == null) {
                throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "productId"), null);
            }
            if (creationDate == null) {
                throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "creationDate"), null);
            }
            Query query = createQuery("SELECT e FROM ExchangeRate e WHERE e.productId = ?1 AND e.beginningDate = ?2 AND e.endingDate IS NOT NULL ");

            query.setParameter("1", productId);
            query.setParameter("2", creationDate);
            exchangeRate = (ExchangeRate) query.setHint("toplink.refresh", "true").getSingleResult();
        } catch (NoResultException ex) {
            Query query;
            try {
                query = createQuery("SELECT e FROM ExchangeRate e WHERE e.productId = ?1 AND e.beginningDate = ?2 AND e.endingDate IS NULL ");
                query.setParameter("1", productId);
                query.setParameter("2", creationDate);
                exchangeRate = (ExchangeRate) query.setHint("toplink.refresh", "true").getSingleResult();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_REGISTER_NOT_FOUND_EXCEPTION, ExchangeRate.class.getSimpleName(), "getExchangeRateByBeginningDate", ExchangeRate.class.getSimpleName(), null), ex);
            }

        } catch (RegisterNotFoundException ex) {
            System.out.println("ex: " + ex);
            throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_REGISTER_NOT_FOUND_EXCEPTION, ExchangeRate.class.getSimpleName(), "getExchangeRateByBeginningDate", ExchangeRate.class.getSimpleName(), null), ex);
        } catch (Exception ex) {
            System.out.println("ex: " + ex);
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), ex.getMessage()), ex);
        }

        return exchangeRate;
    }

    public Long loadTransactionTypeById(Long transactionTypeId) throws RegisterNotFoundException, NullParameterException, GeneralException {
        TransactionType transactionType = new TransactionType();
        try {
            if (transactionTypeId == null) {
                throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "transactionTypeId"), null);
            }

            Query query = createQuery("SELECT t FROM TransactionType t WHERE t.id =?1");
            query.setParameter("1", transactionTypeId);
            transactionType = (TransactionType) query.setHint("toplink.refresh", "true").getSingleResult();
        } catch (NoResultException ex) {
            throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_REGISTER_NOT_FOUND_EXCEPTION, TransactionType.class.getSimpleName(), "loadTransactionTypeById", TransactionType.class.getSimpleName(), null), ex);
        } catch (Exception ex) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), ex.getMessage()), ex);
        }
        return transactionType.getId();
    }

    public List<CalendarDays> searchCalendarDays(String name) throws EmptyListException, GeneralException, NullParameterException {
        List<CalendarDays> calendarDays = null;
        try {
            if (name == null) {
                throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "name"), null);
            }

            StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM calendar_days cd ");
            sqlBuilder.append("WHERE cd.countryId IN (SELECT c.id FROM country c WHERE c.name LIKE '").append(name).append("%')");
            Query query = entityManager.createNativeQuery(sqlBuilder.toString(), CalendarDays.class);
            calendarDays = query.setHint("toplink.refresh", "true").getResultList();

        } catch (NoResultException ex) {
            throw new EmptyListException("No distributions found");
        } catch (Exception ex) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), ex.getMessage()), ex);
        }
        return calendarDays;
    }

    @Override
    public List<CalendarDays> getCalendarDaysByCountryAndDate(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<CalendarDays> calendarDaysList = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_HOLI_DAY_DATE)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_HOLI_DAY_DATE), null);
        }

        if (!params.containsKey(EjbConstants.PARAM_COUNTRY_ID)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_COUNTRY_ID), null);
        }
        calendarDaysList = (List<CalendarDays>) getNamedQueryResult(CalendarDays.class, QueryConstants.HOLI_DAY_EXIST_IN_BD_CALENDAR_DAYS, request, getMethodName(), logger, "calendarDaysList");
        return calendarDaysList;
    }

    //RequestType
    public List<RequestType> getRequestType(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<RequestType> requestType = (List<RequestType>) listEntities(RequestType.class, request, logger, getMethodName());
        return requestType;

    }

    @Override
    public RequestType loadRequestTypeByCode(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException, EmptyListException {
        List<RequestType> requestTypes = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_CODE)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_CODE), null);
        }
        requestTypes = (List<RequestType>) getNamedQueryResult(RequestType.class, "RequestType.findByCode", request, getMethodName(), logger, "code");
        return requestTypes.get(0);
    }

    @Override
    public Long haveAffiliationRequestByUser(Long userId) throws GeneralException, NullParameterException {
        StringBuilder sqlBuilder = new StringBuilder("SELECT COUNT(ar.id) FROM affiliation_request ar WHERE ar.userRegisterUnifiedId = ?1");
        Query query = entityManager.createNativeQuery(sqlBuilder.toString());
        query.setParameter("1", userId);
        List result = (List) query.setHint("toplink.refresh", "true").getResultList();
        return result.get(0) != null ? (Long) result.get(0) : 0l;
    }

    @Override
    public Long haveReviewOFACByPerson(Long personId) throws GeneralException, NullParameterException {
        StringBuilder sqlBuilder = new StringBuilder("SELECT COUNT(r.id) FROM review_ofac r WHERE r.personId = ?1");
        Query query = entityManager.createNativeQuery(sqlBuilder.toString());
        query.setParameter("1", personId);
        List result = (List) query.setHint("toplink.refresh", "true").getResultList();
        return result.get(0) != null ? (Long) result.get(0) : 0l;
    }

    @Override
    public Long validateAccountBankExistsBD(Long userId, Long bankId, String accountNumber) throws GeneralException, NullParameterException {
        StringBuilder sqlBuilder = new StringBuilder("SELECT COUNT(a.id) FROM account_bank a WHERE a.bankId = ?1 AND a.unifiedRegistryId = ?2 AND a.accountNumber = ?3");
        Query query = entityManager.createNativeQuery(sqlBuilder.toString());
        query.setParameter("1", bankId);
        query.setParameter("2", userId);
        query.setParameter("3", accountNumber);        
        List result = (List) query.setHint("toplink.refresh", "true").getResultList();
        return result.get(0) != null ? (Long) result.get(0) : 0l;
    }

    @Override
    public List<AccountBank> getAccountBankByBankByUser(Long bankId, Long userId) throws EmptyListException, GeneralException, NullParameterException {
        List<AccountBank> accountBankList = new ArrayList<AccountBank>();
        try {
            accountBankList = (List<AccountBank>) entityManager.createNamedQuery("AccountBank.accountBankByBankByUser", AccountBank.class).setParameter("bankId", bankId).setParameter("unifiedRegistryId", userId).getResultList();
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (accountBankList.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return accountBankList;
    }

    @Override
    public List<AffiliationRequest> getAffiliationRequestByNaturalPerson(EJBRequest request) throws GeneralException, NullParameterException, EmptyListException {
        List<AffiliationRequest> affiliationRequestList = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_PERSON_ID)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_PERSON_ID), null);
        }
        affiliationRequestList = (List<AffiliationRequest>) getNamedQueryResult(AffiliationRequest.class, QueryConstants.AFFILIATION_REQUEST_BY_PERSON, request, getMethodName(), logger, "affiliationRequestList");
        return affiliationRequestList;
    }
    
    public AffiliationRequest getAffiliationRequestByNumberRequest(String code) throws RegisterNotFoundException, NullParameterException, GeneralException, EmptyListException {
        AffiliationRequest affiliationRequest = new AffiliationRequest();
        try {
            if (code == null) {
                throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "code"), null);
            }
            
            StringBuilder sqlBuilder = new StringBuilder("SELECT ar FROM AffiliationRequest ar WHERE ar.numberRequest LIKE '");
            sqlBuilder.append(code).append("'");
            affiliationRequest = (AffiliationRequest) createQuery(sqlBuilder.toString()).setHint("toplink.refresh", "true").getSingleResult();
 
        } catch (NoResultException ex) {
            throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_REGISTER_NOT_FOUND_EXCEPTION, AffiliationRequest.class.getSimpleName(), "getAffiliationRequestByNumberRequest", AffiliationRequest.class.getSimpleName(), null), ex);
        } catch (Exception ex) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), ex.getMessage()), ex);
        }
        return affiliationRequest;
    }

}
