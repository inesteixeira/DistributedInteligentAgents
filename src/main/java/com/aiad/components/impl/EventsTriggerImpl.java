package com.aiad.components.impl;

import com.aiad.Vehicle;
import com.aiad.components.TrackingAgendaEventListener;
import com.aiad.components.api.DroolsCache;
import com.aiad.components.api.EventsTrigger;
import com.aiad.utils.Constants;
import org.kie.api.runtime.KieSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;


public class EventsTriggerImpl implements EventsTrigger {



    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    @Override
    public Object getTriggeredEvents(Object payload, String rule) {

        DroolsCache droolsCache = new DroolsCacheImpl();
        try {
            droolsCache.initCache();
        } catch (IOException e) {
            e.printStackTrace();
        }

        KieSession kieSession = droolsCache.getSession(rule);

        if (kieSession == null) {
            throw new IllegalStateException(Constants.ErrorMessage_KnowledgeBase_KieSessionNull);
        }
        TrackingAgendaEventListener rulesFired = new TrackingAgendaEventListener();
        kieSession.addEventListener(rulesFired);

        Object objectWithPrice = payload;

        //logger.info("START kieSession.fireAllRules()");
        //logger.info(objectWithPrice.toString());
        kieSession.insert(objectWithPrice);
        kieSession.fireAllRules();

        //logger.info("Rules fired:" + rulesFired.matchsToString());
        //logger.info("STOP kieSession.fireAllRules()");

        kieSession.dispose();

        return objectWithPrice;
    }
}
