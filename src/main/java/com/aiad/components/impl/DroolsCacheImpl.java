package com.aiad.components.impl;

import com.aiad.utils.Constants;
import com.aiad.components.api.DroolsCache;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.KieRepository;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;


public class DroolsCacheImpl implements DroolsCache {

    ConcurrentMap<String, KieBase> droolscache = new ConcurrentHashMap<String, KieBase>();

    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private static final KieServices KIE_SERVICES_INSTANCE = KieServices.get();


    @Override
    public KieSession getSession(String key) {
        //logger.info("Getting KieBase " + key);
        KieBase kieBase = droolscache.get(key);
        //logger.info("Got KieBase " + String.valueOf(kieBase != null));
        KieSession kieSession = null;

        if (kieBase != null) {

            kieSession = droolscache.get(key).newKieSession();

        }
        //logger.info("Got kieSession " + String.valueOf(kieSession != null));
        return kieSession;
    }

    @Override
    public void initCache() throws IOException {
        String droolsCachePolicies = Constants.droolsCachePolicies;
        List<String> policyList = Arrays.asList(droolsCachePolicies.split(","));
        this.readPoliciesFromFileSystem(policyList);

        //initKieSessions(policyList);


    }

    private void readPoliciesFromFileSystem(List<String> policyList) throws IOException {

        //logger.info(Constants.InfoMessage_KieKnowledgeBaseFactory_getKieSession_Start);

        for (String policy : policyList) {

            String fullFileName = policy + ".jar";
            //String fullFilePath = droolsCacheFilepath + "\\" + fullFileName;
            String fullFilePath = new File(".").getCanonicalPath() + "\\" + fullFileName;
            //String fullFilePath = new File(getClass().getClassLoader().getResource(droolsCacheFilepath + fullFileName).getFile()).getAbsolutePath();
            //File ruleSetFile = new File(getClass().getClassLoader().getResource("policies/events_1.0.0.jar").getFile());
            //logger.info("Loading KieBase from path " + fullFilePath);
            KieBase kieBase = loadKnowlegdeBaseFromFilePath(fullFilePath);
            //logger.info("Loaded KieBase " + String.valueOf(kieBase != null));
            if (kieBase != null) {
                //logger.info("key session policy " + policy);
                droolscache.put(policy, kieBase);

            }
        }
    }


    private KieBase loadKnowlegdeBaseFromFilePath(String filepath) {

        //logger.info(Constants.InfoMessage_KieKnowledgeBaseFactory_loadKnowlegdeBaseFromInputStream_Start);

        KieBase kiebase = null;



        Path path = Paths.get(filepath);

        File ruleSetFile = new File(filepath);

        if (ruleSetFile.exists()) {

            try (InputStream is = Files.newInputStream(path)) {

                // load up the knowledge base
                KieRepository kr = KIE_SERVICES_INSTANCE.getRepository();
                KieModule kModule = kr.addKieModule(KIE_SERVICES_INSTANCE.getResources().newInputStreamResource(is));

                //logger.info(Constants.DebugMessage_KieKnowledgeBaseFactory_LoadingModuleRelease + kModule.getReleaseId().toString());

                KieContainer kieContainer = KIE_SERVICES_INSTANCE.newKieContainer(kModule.getReleaseId());
                KieBaseConfiguration kbconf = KIE_SERVICES_INSTANCE.newKieBaseConfiguration();

                kiebase = kieContainer.newKieBase(kbconf);

                //logger.info(Constants.DebugMessage_KieKnowledgeBaseFactory_LoadedModuleRelease);
                //logger.info(Constants.InfoMessage_KieKnowledgeBaseFactory_loadKnowlegdeBaseFromInputStream_End);

            } catch (IOException e) {

                //logger.severe(e.getMessage());
            }
        }

        return kiebase;
    }
}