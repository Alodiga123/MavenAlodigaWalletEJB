package com.alodiga.wallet.ejb;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

import org.apache.log4j.Logger;

import com.alodiga.wallet.common.ejb.TransactionEJBLocal;
import com.alodiga.wallet.common.ejb.TransactionTimerEJB;
import com.alodiga.wallet.common.ejb.TransactionTimerEJBLocal;
import com.alodiga.wallet.common.genericEJB.AbstractWalletEJB;
import com.alodiga.wallet.common.genericEJB.EJBRequest;
import com.alodiga.wallet.common.genericEJB.WalletContextInterceptor;
import com.alodiga.wallet.common.genericEJB.WalletLoggerInterceptor;
import com.alodiga.wallet.common.manager.PreferenceManager;
import com.alodiga.wallet.common.model.PreferenceFieldEnum;
import com.alodiga.wallet.common.utils.EjbConstants;

@Interceptors({WalletLoggerInterceptor.class, WalletContextInterceptor.class})
@Stateless(name = EjbConstants.TRANSACTION_TIMER_EJB, mappedName = EjbConstants.TRANSACTION_TIMER_EJB)
@TransactionManagement(TransactionManagementType.BEAN)
public class TransactionTimerEJBImp extends AbstractWalletEJB implements TransactionTimerEJB, TransactionTimerEJBLocal {

    private static final Logger logger = Logger.getLogger(TransactionTimerEJBImp.class);

    @EJB
    private TransactionEJBLocal transactionEJBLocal;
    @Resource
    private SessionContext ctx;
    Calendar initialExpiration;
    private Long timeoutInterval = 0L;


    private void cancelTimers() {
        try {
            if (ctx.getTimerService() != null) {
                Collection<Timer> timers = ctx.getTimerService().getTimers();
                if (timers != null) {
                    for (Timer timer : timers) {
                        timer.cancel();
                    }
                }
            }
        } catch (Exception e) {
            //
        }
    }



    private void createTimer() {
        ctx.getTimerService().createTimer(initialExpiration.getTime(), timeoutInterval, EjbConstants.TRANSACTION_TIMER_EJB);
}

    @Timeout
    public void execute(Timer timer) {
        try {
            logger.info("[TransactionTimerEJB] Ejecutando");
            System.out.println("[TransactionTimerEJB] Ejecutando");
            executeBilling();
            stop();
            start();

        } catch (Exception e) {
            logger.error("Error", e);
        }
    }


    private void executeBilling() throws Exception {

        try {
        	EJBRequest request = new EJBRequest();
        	transactionEJBLocal.closingDailyTransactionWallet(new Date());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void forceExecution() throws Exception {
        logger.info("Ejecuta forceExecution!!!!!!!!");
        //System.out.println("Ejecuta forceExecution!!!!!!!!");
    }

    public void forceTimeout() throws Exception {
        logger.info("[TransactionTimerEJB] Forzando timeout para dentro de 1 minuto");
        //System.out.println("[TransactionTimerEJB] Forzando timeout para dentro de 1 minuto");
        cancelTimers();
        setTimeoutInterval();
        initialExpiration = Calendar.getInstance();
        initialExpiration.add(Calendar.MINUTE, 1);
        createTimer();
    }

    public Date getNextExecutionDate() {
        if (ctx.getTimerService() != null) {
            Collection<Timer> timers = ctx.getTimerService().getTimers();
            if (timers != null) {
                for (Timer timer : timers) {
                    return timer.getNextTimeout();
                }
            }
        }

        return null;
    }

    public void restart() throws Exception {
        stop();
        start();
        logger.info("[TransactionTimerEJB] Reiniciado");
        //System.out.println("[TransactionTimerEJB] Reiniciado");
    }

    private void setTimeoutInterval() throws Exception {
    	PreferenceManager pManager = PreferenceManager.getInstance();
    	String time = pManager.getPreferencesValueByClassificationIdAndPreferenceId(1L,PreferenceFieldEnum.WALLET_CLOSING_TIME.getId());
    	String[] parts = time.split(":");
        initialExpiration = Calendar.getInstance();
        initialExpiration.set(Calendar.HOUR, Integer.parseInt(parts[0]));//Media entre zona horaria de California Y Florida - EN CA 12 am en FL seria las 4 am.
        initialExpiration.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
        initialExpiration.set(Calendar.SECOND, Integer.parseInt(parts[2]));;
        initialExpiration.set(Calendar.MILLISECOND, 0);
        initialExpiration.set(Calendar.AM_PM, Calendar.PM);
        Long secondsInDay = 86400L;
        //secondsInDay = secondsInDay * 15;//Cada quince dias
        initialExpiration.add(Calendar.DAY_OF_MONTH, 0);//El timer comienza un dia despues que se inicializa.
        timeoutInterval = secondsInDay * 1000L;//Milisegundos
    }

    @SuppressWarnings("unchecked")
    public void start() throws Exception {
        setTimeoutInterval();
        createTimer();
        logger.info("[TransactionTimerEJB] Iniciado");
        //System.out.println("TransactionTimerEJB] Iniciado");
    }

    @SuppressWarnings("unchecked")
    public void stop() throws Exception {
        cancelTimers();
        logger.info("[TransactionTimerEJB] Detenido");
        //System.out.println("[TransactionTimerEJB] Detenido");
    }

    public Long getTimeoutInterval() {
        return timeoutInterval;
    }
        
    
}
