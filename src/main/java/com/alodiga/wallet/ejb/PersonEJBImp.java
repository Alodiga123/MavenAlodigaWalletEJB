package com.alodiga.wallet.ejb;

import com.alodiga.wallet.common.ejb.PersonEJB;
import com.alodiga.wallet.common.ejb.PersonEJBLocal;
import com.alodiga.wallet.common.ejb.UtilsEJB;
import com.alodiga.wallet.common.enumeraciones.PersonClassificationE;
import com.alodiga.wallet.common.enumeraciones.StatusApplicantE;
import java.util.List;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import org.apache.log4j.Logger;
import com.alodiga.wallet.common.exception.EmptyListException;
import com.alodiga.wallet.common.exception.GeneralException;
import com.alodiga.wallet.common.exception.NullParameterException;
import com.alodiga.wallet.common.exception.RegisterNotFoundException;
import com.alodiga.wallet.common.genericEJB.AbstractWalletEJB;
import com.alodiga.wallet.common.genericEJB.EJBRequest;
import com.alodiga.wallet.common.genericEJB.WalletContextInterceptor;
import com.alodiga.wallet.common.genericEJB.WalletLoggerInterceptor;
import com.alodiga.wallet.common.model.AddressType;
import com.alodiga.wallet.common.model.CivilStatus;
import com.alodiga.wallet.common.model.ComercialAgency;
import com.alodiga.wallet.common.model.DocumentsPersonType;
import com.alodiga.wallet.common.model.EdificationType;
import com.alodiga.wallet.common.model.EmployedPosition;
import com.alodiga.wallet.common.model.Employee;
import com.alodiga.wallet.common.model.LegalPerson;
import com.alodiga.wallet.common.model.LegalRepresentative;
import com.alodiga.wallet.common.model.NaturalPerson;
import com.alodiga.wallet.common.model.PasswordChangeRequest;
import com.alodiga.wallet.common.model.Person;
import com.alodiga.wallet.common.model.PersonClassification;
import com.alodiga.wallet.common.model.PersonHasAddress;
import com.alodiga.wallet.common.model.PersonType;
import com.alodiga.wallet.common.model.PhonePerson;
import com.alodiga.wallet.common.model.PhoneType;
import com.alodiga.wallet.common.model.Profession;
import com.alodiga.wallet.common.model.StatusApplicant;
import com.alodiga.wallet.common.model.StreetType;
import com.alodiga.wallet.common.utils.Constants;
import com.alodiga.wallet.common.utils.EjbConstants;
import com.alodiga.wallet.common.utils.EjbUtils;
import com.alodiga.wallet.common.utils.QueryConstants;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import javax.persistence.NoResultException;
import javax.persistence.Query;

@Interceptors({WalletLoggerInterceptor.class, WalletContextInterceptor.class})
@Stateless(name = EjbConstants.PERSON_EJB, mappedName = EjbConstants.PERSON_EJB)
@TransactionManagement(TransactionManagementType.BEAN)
public class PersonEJBImp extends AbstractWalletEJB implements PersonEJB, PersonEJBLocal {

    private static final Logger logger = Logger.getLogger(PersonEJBImp.class);

    //DocumentPersonType
    public List<DocumentsPersonType> getDocumentsPersonType(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<DocumentsPersonType>) listEntities(DocumentsPersonType.class, request, logger, getMethodName());
    }

    public DocumentsPersonType loadDocumentsPersonType(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        DocumentsPersonType documentsPersonType = (DocumentsPersonType) loadEntity(DocumentsPersonType.class, request, logger, getMethodName());
        return documentsPersonType;
    }

    public DocumentsPersonType saveDocumentsPersonType(DocumentsPersonType documentsPersonType) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (documentsPersonType == null) {
            throw new NullParameterException("documentsPersonType", null);
        }
        return (DocumentsPersonType) saveEntity(documentsPersonType);
    }

    @Override
    public List<DocumentsPersonType> getDocumentsPersonByCountry(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<DocumentsPersonType> documentsPersonType = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_COUNTRY_ID)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_COUNTRY_ID), null);
        }
        if (!params.containsKey(EjbConstants.PARAM_IND_NATURAL_PERSON)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_IND_NATURAL_PERSON), null);
        }
        if (!params.containsKey(EjbConstants.PARAM_ORIGIN_APPLICATION_ID)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_ORIGIN_APPLICATION_ID), null);
        }
        documentsPersonType = (List<DocumentsPersonType>) getNamedQueryResult(DocumentsPersonType.class, QueryConstants.DOCUMENTS_BY_COUNTRY, request, getMethodName(), logger, "documentsPersonType");
        return documentsPersonType;
    }

    public List<DocumentsPersonType> searchDocumentsPersonTypeByCountry(String name) throws EmptyListException, GeneralException, NullParameterException {
        List<DocumentsPersonType> documentsPersonType = null;
        try {
            if (name == null) {
                throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "name"), null);
            }

            StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM documents_person_type cp ");
            sqlBuilder.append("WHERE cp.personTypeId IN (SELECT p.id FROM person_type p WHERE p.countryId ");
            sqlBuilder.append("IN (SELECT c.id FROM country c WHERE c.name LIKE '").append(name).append("%'))");
            Query query = entityManager.createNativeQuery(sqlBuilder.toString(), DocumentsPersonType.class);
            documentsPersonType = query.setHint("toplink.refresh", "true").getResultList();

        } catch (NoResultException ex) {
            throw new EmptyListException("No distributions found");
        } catch (Exception ex) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), ex.getMessage()), ex);
        }
        return documentsPersonType;
    }

    //PersonType
    @Override
    public List<PersonType> getPersonTypes(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<PersonType> personTypes = (List<PersonType>) listEntities(PersonType.class, request, logger, getMethodName());
        return personTypes;
    }

    @Override
    public PersonType loadPersonType(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        PersonType personTypes = (PersonType) loadEntity(PersonType.class, request, logger, getMethodName());
        return personTypes;
    }

    @Override
    public PersonType savePersonType(PersonType personType) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (personType == null) {
            throw new NullParameterException("personType", null);
        }
        return (PersonType) saveEntity(personType);
    }

    @Override
    public List<PersonType> getPersonTypeByCountry(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<PersonType> personTypes = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_COUNTRY_ID)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_COUNTRY_ID), null);
        }
//        if (!params.containsKey(EjbConstants.PARAM_ORIGIN_APPLICATION_ID)) {
//            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_ORIGIN_APPLICATION_ID), null);
//        }
        personTypes = (List<PersonType>) getNamedQueryResult(PersonType.class, QueryConstants.PERSON_TYPE_BY_COUNTRY_BY_ORIGIN_APPLICATION_PORTAL, request, getMethodName(), logger, "personTypes");
        return personTypes;
    }

    //Tabla de PhonePerson
    public List<PhonePerson> getPhonePerson(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<PhonePerson>) listEntities(PhonePerson.class, request, logger, getMethodName());
    }

    @Override
    public List<PhonePerson> getPhoneByPerson(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<PhonePerson> phonePersonList = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_PERSON_ID)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_PERSON_ID), null);
        }
        phonePersonList = (List<PhonePerson>) getNamedQueryResult(PhonePerson.class, QueryConstants.PHONES_BY_PERSON, request, getMethodName(), logger, "phonePersonList");
        return phonePersonList;
    }

    public PhonePerson loadPhonePerson(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        PhonePerson phonePerson = (PhonePerson) loadEntity(PhonePerson.class, request, logger, getMethodName());
        return phonePerson;
    }

    public PhonePerson savePhonePerson(PhonePerson phonePerson) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (phonePerson == null) {
            throw new NullParameterException("phonePerson", null);
        }
        return (PhonePerson) saveEntity(phonePerson);
    }

    public List<PhonePerson> getValidateMainPhone(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<PhonePerson> phonePersonList = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_PERSON_ID)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_PERSON_ID), null);
        }
        phonePersonList = (List<PhonePerson>) getNamedQueryResult(PhonePerson.class, QueryConstants.PHONES_BY_MAIN, request, getMethodName(), logger, "phonePersonList");
        return phonePersonList;
    }

    //PersonHasAddress
    @Override
    public List<PersonHasAddress> getPersonHasAddresses(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<PersonHasAddress>) listEntities(PersonHasAddress.class, request, logger, getMethodName());
    }

    @Override
    public List<PersonHasAddress> getPersonHasAddressesByPerson(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<PersonHasAddress> personHasAddressByPerson = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_PERSON_ID)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_PERSON_ID), null);
        }
        personHasAddressByPerson = (List<PersonHasAddress>) getNamedQueryResult(UtilsEJB.class, QueryConstants.PERSON_HAS_ADDRESS_BY_PERSON, request, getMethodName(), logger, "personHasAddressByPerson");
        return personHasAddressByPerson;
    }

    @Override
    public PersonHasAddress loadPersonHasAddress(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        PersonHasAddress personHasAddress = (PersonHasAddress) loadEntity(PersonHasAddress.class, request, logger, getMethodName());
        return personHasAddress;
    }

    @Override
    public PersonHasAddress savePersonHasAddress(PersonHasAddress personHasAddress) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (personHasAddress == null) {
            throw new NullParameterException("personHasAddress", null);
        }
        return (PersonHasAddress) saveEntity(personHasAddress);
    }

    //LegalRepresentative
    public List<LegalRepresentative> getLegalRepresentative(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<LegalRepresentative>) listEntities(LegalRepresentative.class, request, logger, getMethodName());
    }

    public LegalRepresentative loadLegalRepresentative(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        LegalRepresentative legalRepresentative = (LegalRepresentative) loadEntity(LegalRepresentative.class, request, logger, getMethodName());
        return legalRepresentative;
    }

    public LegalRepresentative saveLegalRepresentative(LegalRepresentative legalRepresentative) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (legalRepresentative == null) {
            throw new NullParameterException("legalRepresentative", null);
        }
        return (LegalRepresentative) saveEntity(legalRepresentative);
    }

    //Person
    public List<Person> getPerson(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<Person>) listEntities(Person.class, request, logger, getMethodName());
    }

    public Person loadPerson(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        Person person = (Person) loadEntity(Person.class, request, logger, getMethodName());
        return person;
    }

    public Person savePerson(Person person) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (person == null) {
            throw new NullParameterException("person", null);
        }
        return (Person) saveEntity(person);
    }

    @Override
    public List<Person> getPersonRegisterUnified(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<Person> personList = null;

        Map<String, Object> params = request.getParams();
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM person p WHERE p.personClassificationId = " + PersonClassificationE.REUNUS.getId() + "");
        Query query = entityManager.createNativeQuery(sqlBuilder.toString(), Person.class);
        personList = (List<Person>) query.setHint("toplink.refresh", "true").getResultList();
        if (personList.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }

        return personList;
    }

    public List<Person> getPersonBusinessApplicant(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<Person> personList = null;

        Map<String, Object> params = request.getParams();
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM person p WHERE p.personClassificationId = " + PersonClassificationE.NABUAP.getId() + "");
        sqlBuilder.append(" OR p.personClassificationId= " + PersonClassificationE.LEBUAP.getId() + "");
        sqlBuilder.append(" OR p.personClassificationId= " + PersonClassificationE.LEGREP.getId() + "");
        Query query = entityManager.createNativeQuery(sqlBuilder.toString(), Person.class);
        personList = (List<Person>) query.setHint("toplink.refresh", "true").getResultList();
        if (personList.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }

        return personList;
    }

    @Override
    public List<Person> getPersonByPersonClassificationId(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<Person> personList = null;

        Map<String, Object> params = request.getParams();
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM person p WHERE p.id IN ");
        if (params.containsKey(QueryConstants.PARAM_FIRST_NAME)) {
            sqlBuilder.append("(SELECT n.personId FROM natural_person n WHERE n.firstName LIKE '").append(params.get(QueryConstants.PARAM_FIRST_NAME)).append("%')");;
        }
        sqlBuilder.append(" AND p.personClassificationId = " + PersonClassificationE.REUNUS.getId() + "");

        Query query = entityManager.createNativeQuery(sqlBuilder.toString(), Person.class);
        personList = (List<Person>) query.setHint("toplink.refresh", "true").getResultList();
        if (personList.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }

        return personList;
    }

    public Person getPersonByEmail(String email) throws EmptyListException, GeneralException, NullParameterException {
        Person person = new Person();
        try {
            if (email == null) {
                throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "referenceCode"), null);
            }
            Query query = createQuery("SELECT p FROM Person p WHERE p.email = ?1");
            query.setParameter("1", email);
            person = (Person) query.setHint("toplink.refresh", "true").getSingleResult();
            
        } catch (Exception ex) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), ex.getMessage()), ex);
        }       

        return person;
    }

    

    @Override
    public List<Person> searchBusinessApplicantByStatusApplicantAndNumber(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<Person> personList = null;
        Map<String, Object> params = request.getParams();

        StringBuilder sqlBuilder = new StringBuilder("SELECT DISTINCT p.* FROM person p ");
        sqlBuilder.append("join legal_person lp on p.id = lp.personId ");
        sqlBuilder.append("left join legal_representative lr on p.id = lr.personId ");
        sqlBuilder.append("left join natural_person np on p.id = np.personId ");
        sqlBuilder.append("join affiliation_request ar on p.id = ar.businessPersonId WHERE ");
        sqlBuilder.append("p.personClassificationId = " + PersonClassificationE.LEBUAP.getId() + "");
        sqlBuilder.append(" OR p.personClassificationId = " + PersonClassificationE.NABUAP.getId() + "");
        sqlBuilder.append(" OR p.personClassificationId = " + PersonClassificationE.LEGREP.getId() + "");
        if (params.containsKey(QueryConstants.PARAM_STATUS_APPLICANT_ID)) {
            sqlBuilder.append(" AND lp.statusApplicantId =").append(params.get(QueryConstants.PARAM_STATUS_APPLICANT_ID));
            sqlBuilder.append(" OR lr.statusApplicantId =").append(params.get(QueryConstants.PARAM_STATUS_APPLICANT_ID));
            sqlBuilder.append(" OR np.statusApplicantId =").append(params.get(QueryConstants.PARAM_STATUS_APPLICANT_ID));
        }
        if (params.containsKey(QueryConstants.PARAM_NUMBER_REQUEST)) {
            sqlBuilder.append(" AND ar.numberRequest LIKE '").append(params.get(QueryConstants.PARAM_NUMBER_REQUEST)).append("%'");
        }

        Query query = entityManager.createNativeQuery(sqlBuilder.toString(), Person.class);
        personList = (List<Person>) query.setHint("toplink.refresh", "true").getResultList();
        if (personList.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }

        return personList;
    }
    
    @Override
    public Person searchPersonByLegalPersonId(Long legalPersonId)  throws EmptyListException, GeneralException, NullParameterException {
        List<Person> personList = null;
        if (legalPersonId==null) {
        	 throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "legalPersonId"), null);
        }	
        StringBuilder sqlBuilder = new StringBuilder("SELECT DISTINCT p.* FROM alodigaWallet.person p left join alodigaWallet.legal_representative lr ");
        sqlBuilder.append("on p.id = lr.personId left join alodigaWallet.legal_person lp on lr.id = lp.legalRepresentativeId ");
        sqlBuilder.append(" WHERE p.personClassificationId = " + PersonClassificationE.LEGREP.getId() + "");
        sqlBuilder.append(" AND lp.id = " +legalPersonId + "");

        Query query = entityManager.createNativeQuery(sqlBuilder.toString(), Person.class);
        personList = (List<Person>) query.setHint("toplink.refresh", "true").getResultList();
        if (personList.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }

        return personList.get(0);
    }

    @Override
    public List<Person> searchRegisterUnifiedByStatusApplicantAndNumber(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<Person> personList = null;
        Map<String, Object> params = request.getParams();
        String pattern = "yyyy-MM-dd";
        String strDate1 = "";
        String strDate2 = "";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        
        if (params.containsKey(QueryConstants.PARAM_BEGINNING_DATE)) {
            strDate1 = (simpleDateFormat.format(EjbUtils.getBeginningDate((Date) params.get(QueryConstants.PARAM_BEGINNING_DATE))));
        }
        
        if (params.containsKey(QueryConstants.PARAM_ENDING_DATE)) {
            strDate2 = (simpleDateFormat.format(EjbUtils.getEndingDate((Date) params.get(QueryConstants.PARAM_ENDING_DATE))));
        }
        
        StringBuilder sqlBuilder = new StringBuilder("SELECT DISTINCT p.* FROM person p ");
        sqlBuilder.append("join natural_person np on p.id = np.personId ");
        sqlBuilder.append("join affiliation_request ar on p.id = ar.userRegisterUnifiedId WHERE ");
        sqlBuilder.append("p.personClassificationId = " + PersonClassificationE.REUNUS.getId() + "");
        if (params.containsKey(QueryConstants.PARAM_STATUS_APPLICANT_ID)) {
            sqlBuilder.append(" AND np.statusApplicantId =").append(params.get(QueryConstants.PARAM_STATUS_APPLICANT_ID));
        }
        if (params.containsKey(QueryConstants.PARAM_NUMBER_REQUEST)) {
            sqlBuilder.append(" AND ar.numberRequest LIKE '").append(params.get(QueryConstants.PARAM_NUMBER_REQUEST)).append("%'");
        }
        
        if (!strDate1.isEmpty() & !strDate2.isEmpty()) {
            sqlBuilder.append(" AND ar.dateRequest BETWEEN '");
            sqlBuilder.append(strDate1);
            sqlBuilder.append("' and '");
            sqlBuilder.append(strDate2);
            sqlBuilder.append("'");
        }
        
        if(!strDate1.isEmpty() & strDate2.isEmpty()){
           sqlBuilder.append(" AND ar.dateRequest = '");
           sqlBuilder.append(strDate1);
           sqlBuilder.append("'");
        }
        
        Query query = entityManager.createNativeQuery(sqlBuilder.toString(), Person.class);
        personList = (List<Person>) query.setHint("toplink.refresh", "true").getResultList();
        if (personList.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }

        return personList;
    }

    public List<Person> searchPersonByLegalPersonAndLegalRepresentative(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<Person> personList = null;

        Map<String, Object> params = request.getParams();

        if (!params.containsKey(EjbConstants.PARAM_PERSON_ID)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_PERSON_ID), null);
        }

        if (!params.containsKey(EjbConstants.PARAM_AFFILIATION_REQUEST)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_AFFILIATION_REQUEST), null);
        }

        if (!params.containsKey(EjbConstants.PARAM_NAME)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_NAME), null);
        }
        try {
            StringBuilder sqlBuilder = new StringBuilder("SELECT DISTINCT u.* ");
            sqlBuilder.append("FROM (SELECT p.*, lp.businessName as name ");
            sqlBuilder.append("FROM person p join legal_person lp on p.id = lp.personId and lp.personId = ");
            sqlBuilder.append(params.get(EjbConstants.PARAM_PERSON_ID));
            sqlBuilder.append(" join affiliation_request ar on p.id = ar.businessPersonId and ar.id = ");
            sqlBuilder.append(params.get(EjbConstants.PARAM_AFFILIATION_REQUEST));
            sqlBuilder.append(" union SELECT p.*, concat(lr.firstNames, ' ', lr.lastNames) as name ");
            sqlBuilder.append("FROM person p join legal_representative lr on p.id = lr.personId join legal_person lp on ");
            sqlBuilder.append("lr.id = lp.legalRepresentativeId) u where u.name like '");
            sqlBuilder.append(params.get(EjbConstants.PARAM_NAME));
            sqlBuilder.append("%'");
            Query query = entityManager.createNativeQuery(sqlBuilder.toString(), Person.class);
            personList = (List<Person>) query.setHint("toplink.refresh", "true").getResultList();

        } catch (Exception ex) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), ex.getMessage()), ex);
        }
        return personList;
    }

    //Natural Person
    public List<NaturalPerson> getNaturalPerson(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<NaturalPerson>) listEntities(NaturalPerson.class, request, logger, getMethodName());
    }

    public NaturalPerson loadNaturalPerson(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        NaturalPerson naturalPerson = (NaturalPerson) loadEntity(NaturalPerson.class, request, logger, getMethodName());
        return naturalPerson;
    }

    public NaturalPerson saveNaturalPerson(NaturalPerson naturalPerson) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (naturalPerson == null) {
            throw new NullParameterException("naturalPerson", null);
        }
        return (NaturalPerson) saveEntity(naturalPerson);
    }
    
    @Override
    public List<NaturalPerson> getNaturalPersonByPerson(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<NaturalPerson> naturalPersonList = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_PERSON_ID)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_PERSON_ID), null);
        }
        naturalPersonList = (List<NaturalPerson>) getNamedQueryResult(NaturalPerson.class, QueryConstants.NATURAL_PERSON_BY_PERSON, request, getMethodName(), logger, "naturalPersonList");
        return naturalPersonList;
    }

    //Legal Person
    public List<LegalPerson> getLegalPerson(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<LegalPerson>) listEntities(LegalPerson.class, request, logger, getMethodName());
    }

    public List<LegalPerson> getLegalPersonByLegalRepresentative(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<LegalPerson> legalPersonByRepresentative = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_LEGAL_REPRESENTATIVE_ID)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_LEGAL_REPRESENTATIVE_ID), null);
        }
        legalPersonByRepresentative = (List<LegalPerson>) getNamedQueryResult(LegalPerson.class, QueryConstants.LEGAL_PERSON_BY_LEGAL_REPRESENTATIVE, request, getMethodName(), logger, "legalPersonByRepresentative");
        return legalPersonByRepresentative;
    }

    public List<LegalPerson> getLegalPersonByPerson(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<LegalPerson> legalPersonByPerson = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(EjbConstants.PARAM_PERSON_ID)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), EjbConstants.PARAM_PERSON_ID), null);
        }
        legalPersonByPerson = (List<LegalPerson>) getNamedQueryResult(LegalPerson.class, QueryConstants.LEGAL_PERSON_BY_PERSON, request, getMethodName(), logger, "legalPersonByPerson");
        return legalPersonByPerson;
    }

    public LegalPerson loadLegalPerson(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        LegalPerson legalPerson = (LegalPerson) loadEntity(LegalPerson.class, request, logger, getMethodName());
        return legalPerson;
    }

    public LegalPerson saveLegalPerson(LegalPerson legalPerson) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (legalPerson == null) {
            throw new NullParameterException("legalPerson", null);
        }
        return (LegalPerson) saveEntity(legalPerson);
    }

    //StatusApplicant
    public List<StatusApplicant> getStatusApplicant(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<StatusApplicant>) listEntities(StatusApplicant.class, request, logger, getMethodName());
    }

    public List<StatusApplicant> getStatusApplicantOFAC(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<StatusApplicant> statusApplicantList = null;

        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM status_applicant sa ");
        sqlBuilder.append("WHERE sa.code= '").append("" + StatusApplicantE.LISNOK.getStatusApplicantCode() + "").append("'");
        sqlBuilder.append(" OR sa.code= '").append("" + StatusApplicantE.LISNEG.getStatusApplicantCode() + "").append("'");
        Query query = entityManager.createNativeQuery(sqlBuilder.toString(), StatusApplicant.class);
        statusApplicantList = (List<StatusApplicant>) query.setHint("toplink.refresh", "true").getResultList();
        if (statusApplicantList.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }

        return statusApplicantList;
    }

    public StatusApplicant loadStatusApplicant(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        StatusApplicant statusApplicant = (StatusApplicant) loadEntity(StatusApplicant.class, request, logger, getMethodName());
        return statusApplicant;
    }

    public StatusApplicant saveStatusApplicant(StatusApplicant statusApplicant) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (statusApplicant == null) {
            throw new NullParameterException("statusApplicant", null);
        }
        return (StatusApplicant) saveEntity(statusApplicant);
    }

    @SuppressWarnings("unchecked")
    //PasswordChangeRequest
    public List<PasswordChangeRequest> getPasswordChangeRequest(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<PasswordChangeRequest>) listEntities(PasswordChangeRequest.class, request, logger, getMethodName());
    }

    public PasswordChangeRequest loadPasswordChangeRequest(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        PasswordChangeRequest passwordChangeRequest = (PasswordChangeRequest) loadEntity(PasswordChangeRequest.class, request, logger, getMethodName());
        return passwordChangeRequest;
    }

    public PasswordChangeRequest savePasswordChangeRequest(PasswordChangeRequest passwordChangeRequest) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (passwordChangeRequest == null) {
            throw new NullParameterException("passwordChangeRequest", null);
        }
        return (PasswordChangeRequest) saveEntity(passwordChangeRequest);
    }

    public List<PasswordChangeRequest> getSearchPasswordChangeRequest(String name) throws EmptyListException, GeneralException, NullParameterException {
        List<PasswordChangeRequest> passwordChangeRequestList = null;
        if (name == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "name"), null);
        }
        try {
            StringBuilder sqlBuilder = new StringBuilder("SELECT DISTINCT p FROM PasswordChangeRequest p ");
            sqlBuilder.append("WHERE p.requestNumber LIKE '").append(name).append("%'");

            Query query = entityManager.createQuery(sqlBuilder.toString());
            passwordChangeRequestList = query.setHint("toplink.refresh", "true").getResultList();

        } catch (NoResultException ex) {
            throw new EmptyListException("No distributions found");
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        return passwordChangeRequestList;
    }

    @Override
    public List<Employee> getEmployee(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<Employee> employee = (List<Employee>) listEntities(Employee.class, request, logger, getMethodName());
        return employee;

    }

    @Override
    public Employee saveEmployee(Employee employee) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (employee == null) {
            throw new NullParameterException("employee", null);
        }
        return (Employee) saveEntity(employee);
    }

    public List<Employee> searchEmployee(String name) throws EmptyListException, GeneralException, NullParameterException {
        List<Employee> employeeList = null;
        if (name == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "name"), null);
        }
        try {
            StringBuilder sqlBuilder = new StringBuilder("SELECT DISTINCT e FROM Employee e ");
            sqlBuilder.append("WHERE e.firstNames LIKE '").append(name).append("%'");

            Query query = entityManager.createQuery(sqlBuilder.toString());
            employeeList = query.setHint("toplink.refresh", "true").getResultList();

        } catch (NoResultException ex) {
            throw new EmptyListException("No distributions found");
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        return employeeList;
    }

    @Override
    public PersonClassification loadPersonClassification(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        PersonClassification personClassification = (PersonClassification) loadEntity(PersonClassification.class, request, logger, getMethodName());
        return personClassification;
    }

    @Override
    public CivilStatus loadCivilStatus(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        CivilStatus civilStatus = (CivilStatus) loadEntity(CivilStatus.class, request, logger, getMethodName());
        return civilStatus;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<CivilStatus> getCivilStatus(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<CivilStatus>) listEntities(CivilStatus.class, request, logger, getMethodName());
    }

    @Override
    public CivilStatus saveCivilStatus(CivilStatus civilStatus) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (civilStatus == null) {
            throw new NullParameterException("civilStatus", null);
        }
        return (CivilStatus) saveEntity(civilStatus);
    }

    @Override
    public Profession loadProfession(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        Profession profession = (Profession) loadEntity(Profession.class, request, logger, getMethodName());
        return profession;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Profession> getProfession(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<Profession>) listEntities(Profession.class, request, logger, getMethodName());
    }

    @Override
    public Profession saveProfession(Profession profession) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (profession == null) {
            throw new NullParameterException("profession", null);
        }
        return (Profession) saveEntity(profession);
    }

    @Override
    public PhoneType loadPhoneType(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        PhoneType phoneType = (PhoneType) loadEntity(PhoneType.class, request, logger, getMethodName());
        return phoneType;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<PhoneType> getPhoneType(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<PhoneType>) listEntities(PhoneType.class, request, logger, getMethodName());
    }

    @Override
    public PhoneType savePhoneType(PhoneType phoneType) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (phoneType == null) {
            throw new NullParameterException("phoneType", null);
        }
        return (PhoneType) saveEntity(phoneType);
    }

    @Override
    public AddressType loadAddressType(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        AddressType addressType = (AddressType) loadEntity(AddressType.class, request, logger, getMethodName());
        return addressType;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<AddressType> getAddressType(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<AddressType>) listEntities(AddressType.class, request, logger, getMethodName());
    }

    @Override
    public AddressType saveAddressType(AddressType addressType) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (addressType == null) {
            throw new NullParameterException("addressType", null);
        }
        return (AddressType) saveEntity(addressType);
    }

    @Override
    public EdificationType loadEdificationType(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        EdificationType edificationType = (EdificationType) loadEntity(EdificationType.class, request, logger, getMethodName());
        return edificationType;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<EdificationType> getEdificationType(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<EdificationType>) listEntities(EdificationType.class, request, logger, getMethodName());
    }

    @Override
    public EdificationType saveEdificationType(EdificationType edificationType) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (edificationType == null) {
            throw new NullParameterException("edificationType", null);
        }
        return (EdificationType) saveEntity(edificationType);
    }

    @Override
    public StreetType loadStreetType(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        StreetType streetType = (StreetType) loadEntity(StreetType.class, request, logger, getMethodName());
        return streetType;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<StreetType> getStreetType(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        return (List<StreetType>) listEntities(StreetType.class, request, logger, getMethodName());
    }

    @Override
    public StreetType saveStreetType(StreetType streetType) throws RegisterNotFoundException, NullParameterException, GeneralException {
        if (streetType == null) {
            throw new NullParameterException("streetType", null);
        }
        return (StreetType) saveEntity(streetType);
    }

    //ComercialAgency
    @Override
    public List<ComercialAgency> getComercialAgency(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<ComercialAgency> comercialAgency = (List<ComercialAgency>) listEntities(ComercialAgency.class, request, logger, getMethodName());
        return comercialAgency;
    }

    @Override
    public ComercialAgency loadComercialAgency(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        ComercialAgency comercialAgency = (ComercialAgency) loadEntity(ComercialAgency.class, request, logger, getMethodName());
        return comercialAgency;
    }

    //EmployedPosition
    @Override
    public List<EmployedPosition> getEmployedPosition(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<EmployedPosition> employedPosition = (List<EmployedPosition>) listEntities(EmployedPosition.class, request, logger, getMethodName());
        return employedPosition;
    }

}
