package com.aiad.utils;

public class Constants {

    // Info KieKnowledgeBaseFactory
    public static final String InfoMessage_KieKnowledgeBaseFactory_getKieSession_Start = "KieKnowledgeBaseFactory - Start getKieSession()";
    public static final String InfoMessage_KieKnowledgeBaseFactory_getKieSession_End = "KieKnowledgeBaseFactory - End getKieSession()";
    public static final String InfoMessage_KieKnowledgeBaseFactory_loadKnowlegdeBaseFromInputStream_Start = "KieKnowledgeBaseFactory - Start loadKnowlegdeBaseFromInputStream()";
    public static final String InfoMessage_KieKnowledgeBaseFactory_loadKnowlegdeBaseFromInputStream_End = "KieKnowledgeBaseFactory - End loadKnowlegdeBaseFromInputStream()";
    public static final String InfoMessage_KieKnowledgeBaseFactory_getRuleSetArtifact_Start = "KieKnowledgeBaseFactory - Start getRuleSetArtifact()";
    public static final String InfoMessage_KieKnowledgeBaseFactory_getRuleSetArtifact_End = "KieKnowledgeBaseFactory - End getRuleSetArtifact()";
    public static final String InfoMessage_KieKnowledgeBaseFactory_processTechnicalRules_Start = "KieKnowledgeBaseFactory - Start processTechnicalRules()";
    public static final String InfoMessage_KieKnowledgeBaseFactory_processTechnicalRules_End = "KieKnowledgeBaseFactory - End processTechnicalRules()";
    public static final String InfoMessage_KieKnowledgeBaseFactory_fetchRules_Start = "KieKnowledgeBaseFactory - Start fetchRules()";
    public static final String InfoMessage_KieKnowledgeBaseFactory_fetchRules_End = "KieKnowledgeBaseFactory - End fetchRules()";

    // Debug Messages KieKnowledgeBaseFactory
    public static final String DebugMessage_KieKnowledgeBaseFactory_GetS3Artifact = "KieKnowledgeBaseFactory - Fetching S3 artifact: %s";
    public static final String DebugMessage_KieKnowledgeBaseFactory_KieBasePathNotFound = "KieKnowledgeBaseFactory - KieBase at %s does not exist.";
    public static final String DebugMessage_KieKnowledgeBaseFactory_policyFilePath = "KieKnowledgeBaseFactory - Target Policy FilePath: ";
    public static final String DebugMessage_KieKnowledgeBaseFactory_kiebaseFilePath = "KieKnowledgeBaseFactory - KieBase FilePath: ";
    public static final String DebugMessage_KieKnowledgeBaseFactory_LoadedRuleSet = "KieKnowledgeBaseFactory - Rule Set is loaded.";
    public static final String DebugMessage_KieKnowledgeBaseFactory_LoadingKnowledgeBase = "KieKnowledgeBaseFactory - Loading up the knowledge base.";
    public static final String DebugMessage_KieKnowledgeBaseFactory_LoadedKnowledgeBase = "KieKnowledgeBaseFactory - Knowledge base is loaded.";
    public static final String DebugMessage_KieKnowledgeBaseFactory_LoadingModuleRelease = "KieKnowledgeBaseFactory - Loading Module ReleaseId: ";
    public static final String DebugMessage_KieKnowledgeBaseFactory_LoadedModuleRelease = "KieKnowledgeBaseFactory - Module ReleaseId is loaded.";
    public static final String DebugMessage_KieKnowledgeBaseFactory_LoadingFireAllRules = "KieKnowledgeBaseFactory - FireAllRules Ready.";
    public static final String DebugMessage_KieKnowledgeBaseFactory_LoadedFireAllRules = "KieKnowledgeBaseFactory - FireAllRules: ";
    public static final String DebugMessage_KieKnowledgeBaseFactory_FecthingRulesStart = "KieKnowledgeBaseFactory - Fetching Rules is starting.";
    public static final String DebugMessage_KieKnowledgeBaseFactory_FecthingRulesEnd = "KieKnowledgeBaseFactory - Fetching Rules is complete.";

    public static final String ErrorMessage_KnowledgeBase_KieSessionNull = "KieSession is null.";

    public static final String droolsCachePolicies = "vehicle,house,health";
}
