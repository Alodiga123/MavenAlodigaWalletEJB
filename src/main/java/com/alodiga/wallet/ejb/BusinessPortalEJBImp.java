package com.alodiga.wallet.ejb;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import com.alodiga.wallet.common.ejb.BusinessPortalEJB;
import com.alodiga.wallet.common.ejb.UtilsEJB;
import com.alodiga.wallet.common.ejb.UtilsEJBLocal;
import com.alodiga.wallet.common.enumeraciones.DocumentTypeE;
import com.alodiga.wallet.common.enumeraciones.PersonClassificationE;
import com.alodiga.wallet.common.enumeraciones.RequestTypeE;
import com.alodiga.wallet.common.exception.EmptyListException;
import com.alodiga.wallet.common.exception.GeneralException;
import com.alodiga.wallet.common.exception.NullParameterException;
import com.alodiga.wallet.common.exception.RegisterNotFoundException;
import com.alodiga.wallet.common.genericEJB.AbstractWalletEJB;
import com.alodiga.wallet.common.genericEJB.EJBRequest;
import com.alodiga.wallet.common.genericEJB.WalletContextInterceptor;
import com.alodiga.wallet.common.genericEJB.WalletLoggerInterceptor;
import com.alodiga.wallet.common.model.AccountBank;
import com.alodiga.wallet.common.model.AccountTypeBank;
import com.alodiga.wallet.common.model.Address;
import com.alodiga.wallet.common.model.AddressType;
import com.alodiga.wallet.common.model.Bank;
import com.alodiga.wallet.common.model.AffiliationRequest;
import com.alodiga.wallet.common.model.BusinessCategory;
import com.alodiga.wallet.common.model.City;
import com.alodiga.wallet.common.model.CivilStatus;
import com.alodiga.wallet.common.model.CollectionType;
import com.alodiga.wallet.common.model.CollectionsRequest;
import com.alodiga.wallet.common.model.Country;
import com.alodiga.wallet.common.model.Currency;
import com.alodiga.wallet.common.model.DocumentType;
import com.alodiga.wallet.common.model.DocumentsPersonType;
import com.alodiga.wallet.common.model.EdificationType;
import com.alodiga.wallet.common.model.LegalPerson;
import com.alodiga.wallet.common.model.LegalRepresentative;
import com.alodiga.wallet.common.model.NaturalPerson;
import com.alodiga.wallet.common.model.OriginApplication;
import com.alodiga.wallet.common.model.Person;
import com.alodiga.wallet.common.model.PersonClassification;
import com.alodiga.wallet.common.model.PersonHasAddress;
import com.alodiga.wallet.common.model.PersonType;
import com.alodiga.wallet.common.model.PhonePerson;
import com.alodiga.wallet.common.model.PhoneType;
import com.alodiga.wallet.common.model.PreferenceValue;
import com.alodiga.wallet.common.model.Profession;
import com.alodiga.wallet.common.model.RequestHasCollectionRequest;
import com.alodiga.wallet.common.model.RequestType;
import com.alodiga.wallet.common.model.Sequences;
import com.alodiga.wallet.common.model.State;
import com.alodiga.wallet.common.model.StatusAccountBank;
import com.alodiga.wallet.common.model.StatusApplicant;
import com.alodiga.wallet.common.model.StatusRequest;
import com.alodiga.wallet.common.model.StreetType;
import com.alodiga.wallet.common.utils.Constants;
import com.alodiga.wallet.common.utils.EjbConstants;
import com.alodiga.wallet.common.utils.QueryConstants;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Interceptors({WalletLoggerInterceptor.class, WalletContextInterceptor.class})
@Stateless(name = EjbConstants.BUSINESS_PORTAL_EJB, mappedName = EjbConstants.BUSINESS_PORTAL_EJB)
@TransactionManagement(TransactionManagementType.BEAN)

public class BusinessPortalEJBImp extends AbstractWalletEJB implements BusinessPortalEJB {

    private static final Logger logger = Logger.getLogger(BusinessPortalEJBImp.class);
    @EJB
    private UtilsEJBLocal utilsEJB;
    

    @Override
    public List<PersonType> getPersonTypesBycountryId(Long countryId) throws EmptyListException, GeneralException, NullParameterException {
        if (countryId == null) {
            throw new NullParameterException("countryId", null);
        }
        List<PersonType> personTypes = new ArrayList<PersonType>();
        try {
            personTypes = (List<PersonType>) entityManager.createNamedQuery("PersonType.findBycountryId", PersonType.class).setParameter("countryId", countryId).getResultList();
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (personTypes.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return personTypes;
    }

    @Override
    public List<DocumentsPersonType> getDocumentPersonTypesBypersonTypeId(Long personTypeId) throws EmptyListException, GeneralException, NullParameterException {
        if (personTypeId == null) {
            throw new NullParameterException("personTypeId", null);
        }
        List<DocumentsPersonType> documentsPersonTypes = new ArrayList<DocumentsPersonType>();
        try {
            documentsPersonTypes = (List<DocumentsPersonType>) entityManager.createNamedQuery("DocumentsPersonType.findBypersonTypeId", DocumentsPersonType.class).setParameter("personTypeId", personTypeId).getResultList();
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (documentsPersonTypes.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return documentsPersonTypes;
    }

    @Override
    public List<CollectionType> getCollectionTypesBycountryId(Long countryId) throws EmptyListException, GeneralException, NullParameterException {
        if (countryId == null) {
            throw new NullParameterException("countryId", null);
        }
        List<CollectionType> collectionTypes = new ArrayList<CollectionType>();
        try {
            collectionTypes = (List<CollectionType>) entityManager.createNamedQuery("CollectionType.findBycountryId", CollectionType.class).setParameter("countryId", countryId).getResultList();
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (collectionTypes.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return collectionTypes;
    }

    @SuppressWarnings("unchecked")
    @Override
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

    @Override
    public List<State> getStatesByCountryId(Long countryId) throws EmptyListException, GeneralException, NullParameterException {
        if (countryId == null) {
            throw new NullParameterException("countryId", null);
        }
        List<State> states = new ArrayList<State>();
        try {
            states = (List<State>) entityManager.createNamedQuery("State.findBycountryId", State.class).setParameter("countryId", countryId).getResultList();
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (states.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return states;
    }

    @Override
    public List<City> getCitiesByStateId(Long stateId) throws EmptyListException, GeneralException, NullParameterException {
        if (stateId == null) {
            throw new NullParameterException("stateId", null);
        }
        List<City> citys = new ArrayList<City>();
        try {
            citys = (List<City>) entityManager.createNamedQuery("City.findBystateId", City.class).setParameter("stateId", stateId).getResultList();
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (citys.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return citys;
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
            case Constants.ORIGIN_APPLICATION_APP_ALODIGA_WALLET_ID:
                prefixNumberSequence = "APP-";
                break;
            case Constants.ORIGIN_APPLICATION_ADMIN_WALLET_ID:
                prefixNumberSequence = "ADM-";
                break;
            case Constants.ORIGIN_APPLICATION_PORTAL_WEB_ID:
                prefixNumberSequence = "PBW";
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

    @Override
    public List<CollectionsRequest> getCollectionRequestsBycollectionTypeId(Long collectionTypeId)
        throws EmptyListException, GeneralException, NullParameterException {
        if (collectionTypeId == null) {
                throw new NullParameterException("collectionTypeId", null);
        }
        List<CollectionsRequest> collectionsRequests = new ArrayList<CollectionsRequest>();
        try {
            collectionsRequests = (List<CollectionsRequest>) entityManager.createNamedQuery("CollectionsRequest.findBycollectionTypeId", CollectionsRequest.class)
            .setParameter("collectionTypeId", collectionTypeId).getResultList();
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(),getMethodName(), e.getMessage()), null);
        }
        if (collectionsRequests.isEmpty()) {
                throw new EmptyListException(logger,sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return collectionsRequests;
    }

    @Override
    public List<CollectionsRequest> getCollectionRequestsByPersonTypeId(Long personTypeId)
        throws EmptyListException, GeneralException, NullParameterException {
        if (personTypeId == null) {
            throw new NullParameterException("personTypeId", null);
        }
        List<CollectionsRequest> collectionsRequests = new ArrayList<CollectionsRequest>();
        try {
            collectionsRequests = (List<CollectionsRequest>) entityManager.createNamedQuery(QueryConstants.COLLECTIONS_BY_PERSON_TYPE, CollectionsRequest.class)
                                   .setParameter("personTypeId", personTypeId).getResultList();
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(),getMethodName(), e.getMessage()), null);
        }
        if (collectionsRequests.isEmpty()) {
            throw new EmptyListException(logger,sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return collectionsRequests;
    }
    
    @Override
    public Sequences saveSequences(Sequences sequence) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (sequence == null) {
            throw new NullParameterException("sequence", null);
        }
        return (Sequences) saveEntity(sequence);
    }
    
    public Person createPerson(Person person, PersonClassification personClassification) {
        person.setCreateDate(new Timestamp(new Date().getTime()));
        if (person.getEmail() != null) {
            person.setEmail(person.getEmail());
        } else {
            person.setEmail(null);
        }
        person.setPersonTypeId(person.getPersonTypeId());
        person.setPersonClassificationId(personClassification);
        if (person.getWebSite() != null) {
            person.setWebSite(person.getWebSite());
        } else {
            person.setWebSite(null);
        }
        person.setCountryId(person.getCountryId());
        return person;
    }

    @Override
    public AffiliationRequest saveNaturalPersonAffiliationRequest(Person person, NaturalPerson naturalPerson, RequestType requestType, PhonePerson phonePerson, Address address) throws NullParameterException, GeneralException {
        AffiliationRequest affiliationRequest = new AffiliationRequest();
        PersonClassification personClassification = null;
        OriginApplication originApplication = null;
        String acronym = null;
        String numberSequence = "";
        String requestTypeBusiness = "";
        EJBRequest request = new EJBRequest();
        Map<String, Object> params = new HashMap<String, Object>();
        
        try {
            requestTypeBusiness = RequestTypeE.SOAFNE.getRequestTypeCode();
            if (requestType.getCode().equals(requestTypeBusiness)) {
                //Se obtiene el PersonClassification del Solicitante de Negocio Persona Natural
                String personClassificationCode = PersonClassificationE.NABUAP.getPersonClassificationCode();
                personClassification = (PersonClassification) entityManager.createNamedQuery(QueryConstants.PERSON_CLASSIFICATION_BY_CODE, PersonClassification.class).setParameter(Constants.PARAM_CODE, personClassificationCode).getSingleResult(); 
                
                //Se guarda el objeto person asociado al solicitante de negocio
                person = (Person) saveEntity(createPerson(person, personClassification));
                
                //Se asocia el negocio a la solicitud de afiliación
                affiliationRequest.setBusinessPersonId(person);
            } else {
                //Se obtiene el PersonClassification del Solicitante Usuario de la Billetera
                String personClassificationCode = PersonClassificationE.REUNUS.getPersonClassificationCode();
                personClassification = (PersonClassification) entityManager.createNamedQuery(QueryConstants.PERSON_CLASSIFICATION_BY_CODE, PersonClassification.class).setParameter(Constants.PARAM_CODE, personClassificationCode).getSingleResult();
                
                //Se guarda el objeto person asociado al solicitante usuario de la billetera
                person = (Person) saveEntity(createPerson(person, personClassification));
                
                //Se asocia el usuario de la billetera a la solicitud de afiliación
                affiliationRequest.setUserRegisterUnifiedId(person);
            } 

            //Se guarda la información en BD del solicitante
            //1. Datos personales del solicitante
            naturalPerson.setPersonId(person);
            naturalPerson.setDocumentsPersonTypeId(naturalPerson.getDocumentsPersonTypeId());
            naturalPerson.setIdentificationNumber(naturalPerson.getIdentificationNumber());
            if (naturalPerson.getIdentificactionNumberOld() != null) {
                naturalPerson.setIdentificactionNumberOld(naturalPerson.getIdentificactionNumberOld());
            } else {
                naturalPerson.setIdentificactionNumberOld(null);
            }
            naturalPerson.setDueDateDocumentIdentification(naturalPerson.getDueDateDocumentIdentification());
            naturalPerson.setFirstName(naturalPerson.getFirstName());
            naturalPerson.setLastName(naturalPerson.getLastName());
            if (naturalPerson.getMarriedLastName() != null) {
                naturalPerson.setMarriedLastName(naturalPerson.getMarriedLastName());
            } else {
                naturalPerson.setMarriedLastName(null);
            }
            naturalPerson.setGender(naturalPerson.getGender());
            naturalPerson.setPlaceBirth(naturalPerson.getPlaceBirth());
            naturalPerson.setDateBirth(naturalPerson.getDateBirth());
            naturalPerson.setCivilStatusId(naturalPerson.getCivilStatusId());
            if (naturalPerson.getProfessionId() != null) {
                naturalPerson.setProfessionId(naturalPerson.getProfessionId());
            } else {
                naturalPerson.setProfessionId(null);
            }
            naturalPerson.setCreateDate(new Timestamp(new Date().getTime()));
            saveEntity(naturalPerson);
            
            //2. Teléfono Móvil del Solicitante
            phonePerson.setCountryId(phonePerson.getCountryId());
            phonePerson.setCountryCode(phonePerson.getCountryCode());
            phonePerson.setAreaCode(phonePerson.getAreaCode());
            phonePerson.setNumberPhone(phonePerson.getNumberPhone());
            phonePerson.setPersonId(person);
            phonePerson.setPhoneTypeId(phonePerson.getPhoneTypeId());
            if (phonePerson.getExtensionPhoneNumber() != null) {
                phonePerson.setExtensionPhoneNumber(phonePerson.getExtensionPhoneNumber());
            } else {
                phonePerson.setExtensionPhoneNumber(null);
            }
            phonePerson.setIndMainPhone(phonePerson.getIndMainPhone());
            phonePerson.setCreateDate(new Timestamp(new Date().getTime()));
            saveEntity(phonePerson);
            
            //3. Dirección del solicitante
            address.setCountryId(address.getCountryId());
            address.setCityId(address.getCityId());
            if (address.getCountyId() != null) {
                address.setCountyId(address.getCountyId());
            } else {
                address.setCountyId(null);
            }
            if (address.getZipCode() != null) {
                address.setZipCode(address.getZipCode());
            } else {
                address.setZipCode(null);
            }
            if (address.getStreetTypeId() != null) {
                address.setStreetTypeId(address.getStreetTypeId());
            } else {
                address.setStreetTypeId(null);
            }
            if (address.getNameStreet() != null) {
                address.setNameStreet(address.getNameStreet());
            } else {
                address.setNameStreet(null);
            }
            address.setEdificationTypeId(address.getEdificationTypeId());
            if (address.getNameEdification() != null) {
                address.setNameEdification(address.getNameEdification());
            } else {
                address.setNameEdification(null);
            }
            if (address.getTower() != null) {
                address.setTower(address.getTower());
            } else {
                address.setTower(null);
            }
            if (address.getFloor() != null) {
                address.setFloor(address.getFloor());
            } else {
                address.setFloor(null);
            }
            if (address.getUrbanization() != null) {
                address.setUrbanization(address.getUrbanization());
            } else {
                address.setUrbanization(null);
            }
            address.setAddressLine1("calle:" + address.getNameStreet() + "," + "Urbanizacion: " + address.getUrbanization() + "," + "Edificio:" + address.getNameEdification() + "," + "Piso:" + address.getFloor() + "");
            address.setAddressLine2("Pais:" + address.getCountryId().getName() + "," + "Ciudad:" + address.getCityId().getName() + "," + "Codigo Postal:" + address.getZipCode() + "");
            address.setAddressTypeId(address.getAddressTypeId());
            address.setIndMainAddress(address.getIndMainAddress());
            address = (Address) saveEntity(address);
            
            //4. Se asocia la dirección al solicitante
            PersonHasAddress personHasAddress = new PersonHasAddress();
            personHasAddress.setAddressId(address);
            personHasAddress.setPersonId(person);
            personHasAddress.setCreateDate(new Timestamp(new Date().getTime()));
            saveEntity(personHasAddress);
            
            //Se obtiene la aplicación según el tipo de solicitud         
            if (requestType.getCode().equals(requestTypeBusiness)) {
                params.put(Constants.PARAM_CODE, Constants.ORIGIN_APPLICATION_PORTAL_NEGOCIOS_CODE);
                request.setParams(params);
                originApplication = utilsEJB.loadOriginApplicationByCode(request);
            	acronym = DocumentTypeE.BUAFRQ.getDocumentTypeAcronym();
            }else {
                params.put(Constants.PARAM_CODE, Constants.ORIGIN_APPLICATION_APP_CODE);
                request.setParams(params);
                originApplication = utilsEJB.loadOriginApplicationByCode(request);
            	acronym = DocumentTypeE.USREAR.getDocumentTypeAcronym();
            }
            
            //Se obtiene el tipo de documento y la secuencia asociada a la solicitud
            Integer documentType = utilsEJB.getDocumentTypeByCode(acronym);
            params = new HashMap<String, Object>();
            params.put(EjbConstants.PARAM_DOCUMENT_TYPE_ID, documentType);
            request = new EJBRequest();
            request.setParams(params);
            List<Sequences> sequences = getSequencesByDocumentType(documentType);
            numberSequence = generateNumberSequence(sequences, originApplication.getId());
            
            //Se obtiene el estatus pendiente de la solicitud de afiliación
            params = new HashMap<String, Object>();
            params.put(Constants.PARAM_CODE, Constants.STATUS_BUSINESS_AFFILIATION_REQUEST_PENDING);
            request = new EJBRequest();
            request.setParams(params);            
            StatusRequest status = utilsEJB.loadStatusBusinessAffiliationRequestByCode(request);

            //Se guarda la solicitud en la BD
            affiliationRequest.setNumberRequest(numberSequence);
            affiliationRequest.setCreateDate(new Timestamp(new Date().getTime()));
            affiliationRequest.setDateRequest(new Date());            
            affiliationRequest.setStatusRequestId(status);
            affiliationRequest.setRequestTypeId(requestType);
            affiliationRequest = (AffiliationRequest) saveEntity(affiliationRequest);
            
        } catch (Exception e) {
        	e.printStackTrace();
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        return affiliationRequest;
    }

    @Override
    public RequestHasCollectionRequest saveRequestHasCollectionsRequest(RequestHasCollectionRequest requestHasCollectionsRequest) throws NullParameterException, GeneralException {
        if (requestHasCollectionsRequest == null) {
            throw new NullParameterException("requestHasCollectionsRequest", null);
        }
        return (RequestHasCollectionRequest) saveEntity(requestHasCollectionsRequest);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<CivilStatus> getCivilStatus(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<CivilStatus>) listEntities(CivilStatus.class, request, logger, getMethodName());
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Profession> getProfession(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<Profession>) listEntities(Profession.class, request, logger, getMethodName());
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<PhoneType> getPhoneType(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<PhoneType>) listEntities(PhoneType.class, request, logger, getMethodName());
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<AddressType> getAddressType(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<AddressType>) listEntities(AddressType.class, request, logger, getMethodName());
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<EdificationType> getEdificationType(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<EdificationType>) listEntities(EdificationType.class, request, logger, getMethodName());
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<StreetType> getStreetType(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<StreetType>) listEntities(StreetType.class, request, logger, getMethodName());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<Bank> getBanks(EJBRequest request)throws EmptyListException, GeneralException, NullParameterException{
        return (List<Bank>) listEntities(Bank.class, request, logger, getMethodName());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<AccountTypeBank> getAccountTypeBanks(EJBRequest request)throws EmptyListException, GeneralException, NullParameterException{
        return (List<AccountTypeBank>) listEntities(AccountTypeBank.class, request, logger, getMethodName());
    }
    
    @Override
    public AccountBank saveAccountBank(AccountBank accountBank) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (accountBank == null) {
            throw new NullParameterException("accountBank", null);
        }
        return (AccountBank) saveEntity(accountBank);
    }
	
    @Override
    public List<PreferenceValue> getDiscountRateByBusiness(Long businessId, Long productId, Long transactionTypeId) throws EmptyListException, GeneralException, NullParameterException {
        List<PreferenceValue> preferenceValue = new ArrayList<PreferenceValue>();
        String discountRateCode = Constants.DISCOUNT_RATE_CODE;

        if (businessId == null) {
            throw new NullParameterException("businessId", null);
        }
        if (productId == null) {
            throw new NullParameterException("productId", null);
        }
        if (transactionTypeId == null) {
            throw new NullParameterException("transactionTypeId", null);
        }
        try {
            StringBuilder sqlBuilder = new StringBuilder("SELECT pv.* FROM preference_value pv INNER JOIN preference_field pf ");
            sqlBuilder.append("on pv.preferenceFieldId = pf.id ");
            sqlBuilder.append("WHERE pv.bussinessId = ").append(businessId);
            sqlBuilder.append(" AND pv.productId = ").append(productId);
            sqlBuilder.append(" AND pv.transactionTypeId = ").append(transactionTypeId);
            sqlBuilder.append(" AND pf.code = '").append(discountRateCode).append("' AND pv.enabled = true");
            Query query = entityManager.createNativeQuery(sqlBuilder.toString(), PreferenceValue.class);
            preferenceValue = query.setHint("toplink.refresh", "true").getResultList();
        } catch (NoResultException ex) {
                throw new EmptyListException("No distributions found");
        } catch (Exception e) {
                throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(),getMethodName(), e.getMessage()), null);
        }
        return preferenceValue;
    }

    @Override
    public List<PreferenceValue> getDiscountRateByBusinessAndValidityDate(Long businessId, Date validityDate, Long productId, Long transactionTypeId) throws EmptyListException, GeneralException, NullParameterException {
        List<PreferenceValue> preferenceValue = new ArrayList<PreferenceValue>();
        String discountRateCode = Constants.DISCOUNT_RATE_CODE;
        
        if (businessId == null) {
            throw new NullParameterException("businessId", null);
        }
        if (validityDate == null) {
            throw new NullParameterException("validityDate", null);
        }
                if (productId == null) {
            throw new NullParameterException("productId", null);
        }
        if (transactionTypeId == null) {
            throw new NullParameterException("transactionTypeId", null);
        }
        String pattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String preferenceValidityDate = simpleDateFormat.format(validityDate);
        try {
            StringBuilder sqlBuilder = new StringBuilder("SELECT pv.* FROM preference_value pv INNER JOIN preference_field pf ");
            sqlBuilder.append("on pv.preferenceFieldId = pf.id ");
            sqlBuilder.append("WHERE pv.bussinessId = ").append(businessId);
            sqlBuilder.append(" AND pv.productId = ").append(productId);
            sqlBuilder.append(" AND pv.transactionTypeId = ").append(transactionTypeId);
            sqlBuilder.append(" AND pf.code = '").append(discountRateCode);
            sqlBuilder.append("' AND pv.beginningDate <= '").append(preferenceValidityDate).append("'");
            sqlBuilder.append(" AND pv.endingDate >= '").append(preferenceValidityDate).append("'");
            Query query = entityManager.createNativeQuery(sqlBuilder.toString(), PreferenceValue.class);
            preferenceValue = query.setHint("toplink.refresh", "true").getResultList();
        } catch (NoResultException ex) {
            throw new EmptyListException("No distributions found");
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(),getMethodName(), e.getMessage()), null);
        }
        return preferenceValue;
    }

    @Override
    public AccountBank loadAccountBankById(Long id)
        throws RegisterNotFoundException, NullParameterException, GeneralException {
        EJBRequest request = new EJBRequest();
        request.setParam(id);
        AccountBank accountBank = (AccountBank) loadEntity(AccountBank.class, request, logger, getMethodName());
        return accountBank;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Currency> getCurrencies(EJBRequest request)throws EmptyListException, GeneralException, NullParameterException{
        return (List<Currency>) listEntities(Currency.class, request, logger, getMethodName());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<StatusAccountBank> getStatusAccountBanks(EJBRequest request)throws EmptyListException, GeneralException, NullParameterException{
        return (List<StatusAccountBank>) listEntities(StatusAccountBank.class, request, logger, getMethodName());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<BusinessCategory> getBusinessCategories(EJBRequest request)throws EmptyListException, GeneralException, NullParameterException{
        return (List<BusinessCategory>) listEntities(BusinessCategory.class, request, logger, getMethodName());
    }
    
    @Override
    public AccountTypeBank loadAccountTypeBankById(Integer id)
        throws RegisterNotFoundException, NullParameterException, GeneralException {
        EJBRequest request = new EJBRequest();
        request.setParam(id);
        AccountTypeBank accountTypeBank = (AccountTypeBank) loadEntity(AccountTypeBank.class, request, logger,
        getMethodName());
        return accountTypeBank;
    }

    @Override
    public AffiliationRequest loadAffiliationRequestById(Long id)
        throws RegisterNotFoundException, NullParameterException, GeneralException {
        EJBRequest request = new EJBRequest();
        request.setParam(id);
        AffiliationRequest affiliationRequest = (AffiliationRequest) loadEntity(AffiliationRequest.class, request,logger, getMethodName());
        entityManager.refresh(affiliationRequest);
        return affiliationRequest;
    }

    @Override
    public Bank loadBankById(Long id) throws RegisterNotFoundException, NullParameterException, GeneralException {
        EJBRequest request = new EJBRequest();
        request.setParam(id);
        Bank bank = (Bank) loadEntity(Bank.class, request, logger, getMethodName());
        return bank;
    }

    @Override
    public BusinessCategory loadBusinessCategoryById(Integer id)
        throws RegisterNotFoundException, NullParameterException, GeneralException {
        EJBRequest request = new EJBRequest();
        request.setParam(id);
        BusinessCategory category = (BusinessCategory) loadEntity(BusinessCategory.class, request, logger,
        getMethodName());
        return category;
    }

    @Override
    public Currency loadCurrencyById(Long id)
        throws RegisterNotFoundException, NullParameterException, GeneralException {
        EJBRequest request = new EJBRequest();
        request.setParam(id);
        Currency currency = (Currency) loadEntity(Currency.class, request, logger, getMethodName());
        return currency;
    }

    @Override
    public DocumentType loadDocumentTypeById(Integer id)
        throws RegisterNotFoundException, NullParameterException, GeneralException {
        EJBRequest request = new EJBRequest();
        request.setParam(id);
        DocumentType documentType = (DocumentType) loadEntity(DocumentType.class, request, logger, getMethodName());
        return documentType;
    }

    @Override
    public OriginApplication loadOriginApplicationById(Integer id)
        throws RegisterNotFoundException, NullParameterException, GeneralException {
        EJBRequest request = new EJBRequest();
        request.setParam(id);
        OriginApplication originApplication = (OriginApplication) loadEntity(OriginApplication.class, request, logger,
        getMethodName());
        return originApplication;
    }

    @Override
    public PhoneType loadPhoneTypeById(Integer id)
        throws RegisterNotFoundException, NullParameterException, GeneralException {
        EJBRequest request = new EJBRequest();
        request.setParam(id);
        PhoneType phoneType = (PhoneType) loadEntity(PhoneType.class, request, logger, getMethodName());
        return phoneType;
    }

    @Override
    public RequestType loadRequestTypeByCode(String code)
        throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (code == null) {
            throw new NullParameterException("code", null);
        }
        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<RequestType> cq = cb.createQuery(RequestType.class);
            Root<RequestType> from = cq.from(RequestType.class);
            cq.select(from);
            cq.where(cb.equal(from.get("code"), code));
            return entityManager.createQuery(cq).setMaxResults(1).getSingleResult();
        } catch (NoResultException e) {
            throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_NORESULTEXCEPTION,
                                this.getClass(), getMethodName(), e.getMessage()), null);
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(),getMethodName(), e.getMessage()), null);
        }
    }

    @Override
    public StatusAccountBank loadStatusAccountBankById(Integer id)
        throws RegisterNotFoundException, NullParameterException, GeneralException {
        EJBRequest request = new EJBRequest();
        request.setParam(id);
        StatusAccountBank currency = (StatusAccountBank) loadEntity(StatusAccountBank.class, request, logger,
                        getMethodName());
        return currency;
    }

    @Override
    public StatusApplicant loadStatusApplicantById(Integer id)
        throws RegisterNotFoundException, NullParameterException, GeneralException {
        EJBRequest request = new EJBRequest();
        request.setParam(id);
        StatusApplicant statusApplicant = (StatusApplicant) loadEntity(StatusApplicant.class, request, logger,
        getMethodName());
        return statusApplicant;
    }

    
    @Override
    public List<Sequences> getSequencesByDocumentType(Integer documentTypeId)
        throws EmptyListException, GeneralException, NullParameterException {
        if (documentTypeId == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(),getMethodName(), EjbConstants.PARAM_DOCUMENT_TYPE_ID), null);
        }
        List<Sequences> sequence = null;
        EJBRequest request = new EJBRequest();
        Map<String, Object> params = new HashMap();
        params.put(EjbConstants.PARAM_DOCUMENT_TYPE_ID, documentTypeId);
        request.setParams(params);
        sequence = (List<Sequences>) getNamedQueryResult(UtilsEJB.class, QueryConstants.SEQUENCES_BY_DOCUMENT_TYPE,
                        request, getMethodName(), logger, "sequence");
        return sequence;
    }

    @Override
    public AffiliationRequest saveLegalPersonAffiliationRequest(Person person, LegalPerson legalPerson,
                RequestType requestType, PhonePerson phonePerson, Address address, LegalRepresentative legalRepresentative)
                throws NullParameterException, GeneralException {
        
        if (person == null || legalPerson == null || legalRepresentative == null || requestType == null
                        || phonePerson == null || address == null) {
                throw new NullParameterException(EjbConstants.ERR_NULL_PARAMETER);
        }
        AffiliationRequest affiliatinRequest = new AffiliationRequest();
        try {
            if (requestType.getCode().equals(RequestTypeE.SOAFNE.getRequestTypeCode())) {
                // Se obtiene la Clasificacion del Solicitante Juridico
                String personClassificationCode = PersonClassificationE.LEBUAP.getPersonClassificationCode();
                PersonClassification personClassification = (PersonClassification) entityManager
                                .createNamedQuery(QueryConstants.PERSON_CLASSIFICATION_BY_CODE, PersonClassification.class)
                                .setParameter(Constants.PARAM_CODE, personClassificationCode).getSingleResult();

                // Se guarda el objeto person en la BD
                person.setCreateDate(new Date());
                person.setPersonClassificationId(personClassification);
                person = (Person) saveEntity(person);

                // Se guarda el objeto LegalPerson en la BD
                legalPerson.setPersonId(person);
                legalPerson.setCreateDate(new Date());
                saveEntity(legalPerson);

                // TODO buscar el representante legal a ver si ya existe, cambiar la clasificacion por alguna que exista
                Person legalRepresentativePerson = new Person();
                legalRepresentativePerson.setPersonTypeId(legalRepresentative.getDocumentsPersonTypeId().getPersonTypeId());
                PersonClassification representativePersonClassification = (PersonClassification) entityManager
                        .createNamedQuery(QueryConstants.PERSON_CLASSIFICATION_BY_CODE, PersonClassification.class)
                        .setParameter(Constants.PARAM_CODE, PersonClassificationE.LEGREP.getPersonClassificationCode()).getSingleResult();
                
                legalRepresentativePerson.setPersonClassificationId(representativePersonClassification);
                legalRepresentativePerson.setCountryId(person.getCountryId());
                legalRepresentativePerson.setCreateDate(new Date());
                saveEntity(legalRepresentativePerson);
                
                legalRepresentative.setCreateDate(new Date());
                legalRepresentative.setPersonId(legalRepresentativePerson);
                saveEntity(legalRepresentative);
                
                //Asocia el representante legal al person relacionado
                legalRepresentativePerson.setLegalRepresentative(legalRepresentative);
                saveEntity(legalRepresentativePerson);

                legalPerson.setLegalRepresentativeId(legalRepresentative);
                saveEntity(legalPerson);

            } else {
                // TODO implementar otro tipo de requestType
                return null;
            }
            // Se guarda el objeto PhonePerson en la BD

            phonePerson.setPersonId(person);
            phonePerson.setCreateDate(new Date());
            saveEntity(phonePerson);

            // Guardo Address
            address.setAddressLine1(
                            "Calle:" + address.getNameStreet() + "," + "Urbanizacion: " + address.getUrbanization() + ","
                                            + "Edificio:" + address.getNameEdification() + "," + "Piso:" + address.getFloor() + "");
            address.setAddressLine2("Pais:" + address.getCountryId().getName() + "," + "Ciudad:"
                            + address.getCityId().getName() + "," + "Codigo Postal:" + address.getZipCode() + "");
            address = (Address) saveEntity(address);

            // Guardo Person_has_addres
            PersonHasAddress personHasAddress = new PersonHasAddress();
            personHasAddress.setAddressId(address);
            personHasAddress.setPersonId(person);
            personHasAddress.setCreateDate(new Date());
            saveEntity(personHasAddress);

            // Guardar la Solicitud de Afiliacion
            Map<String, Object> params = new HashMap<String, Object>();
            if (requestType.getCode().equals(RequestTypeE.SOAFNE.getRequestTypeCode())) {
                    params.put(Constants.PARAM_CODE, Constants.ORIGIN_APPLICATION_PORTAL_NEGOCIOS_CODE);
            }
            EJBRequest request = new EJBRequest();
            request.setParams(params);
            OriginApplication originApplication = utilsEJB.loadOriginApplicationByCode(request);

            params = new HashMap<String, Object>();
            String acronym = null;
            if (requestType.getCode().equals(RequestTypeE.SOAFNE.getRequestTypeCode())) {
                    acronym = DocumentTypeE.BUAFRQ.getDocumentTypeAcronym();
                    affiliatinRequest.setBusinessPersonId(person);
            }

            Integer documentType = utilsEJB.getDocumentTypeByCode(acronym);
            List<Sequences> sequences = getSequencesByDocumentType(documentType);
            String numberSequence = generateNumberSequence(sequences, originApplication.getId());
            affiliatinRequest.setCreateDate(new Timestamp(new Date().getTime()));
            affiliatinRequest.setDateRequest(new Date());
            params = new HashMap<String, Object>();
            params.put(Constants.PARAM_CODE, Constants.STATUS_BUSINESS_AFFILIATION_REQUEST_PENDING);
            request = new EJBRequest();
            request.setParams(params);
            affiliatinRequest.setNumberRequest(numberSequence);
            StatusRequest status = utilsEJB.loadStatusBusinessAffiliationRequestByCode(request);
            affiliatinRequest.setStatusRequestId(status);
            affiliatinRequest.setRequestTypeId(requestType);
            affiliatinRequest = (AffiliationRequest) saveEntity(affiliatinRequest);
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(),getMethodName(), e.getMessage()), null);
        }
        return affiliatinRequest;
    }

    public List<AccountBank> getAccountBanksByBusiness(Long businessId)
        throws EmptyListException, GeneralException, NullParameterException {
        if (businessId == null) {
                throw new NullParameterException("businessId", null);
        }
        List<AccountBank> accountBanks = null;
        try {
                CriteriaBuilder cb = entityManager.getCriteriaBuilder();
                CriteriaQuery<AccountBank> cq = cb.createQuery(AccountBank.class);
                Root<AccountBank> from = cq.from(AccountBank.class);
                cq.select(from);
                cq.where(cb.equal(from.get("businessId"), businessId));
                accountBanks = entityManager.createQuery(cq).getResultList();
        } catch (Exception e) {
                e.printStackTrace();
                throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(),
                                getMethodName(), e.getMessage()), null);
        }
        if (accountBanks == null || accountBanks.isEmpty()) {
                throw new EmptyListException(logger,
                                sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return accountBanks;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<StatusApplicant> getStatusApplicant(EJBRequest request)
        throws EmptyListException, GeneralException, NullParameterException {
        return (List<StatusApplicant>) listEntities(StatusApplicant.class, request, logger, getMethodName());
    }

}