package com.aiad;


import jade.core.Profile;
import jade.core.ProfileImpl;

import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;


public class Main {

	public static void main(String[] args) {
		// Get a hold on JADE runtime
		Runtime rt = Runtime.instance();

		// Exit the JVM when there are no more containers around
		rt.setCloseVM(true);
		System.out.print("runtime created\n");

		// Create a default profile
		Profile profile = new ProfileImpl(null, 1200, null);
		System.out.print("profile created\n");

		System.out.println("Launching a whole in-process platform..."+profile);
		AgentContainer mainContainer = rt.createMainContainer(profile);

		// now set the default Profile to start a container
		ProfileImpl pContainer = new ProfileImpl(null, 1200, null);
		System.out.println("Launching the agent container ..."+pContainer);

		AgentContainer cont = rt.createAgentContainer(pContainer);
		System.out.println("Launching the agent container after ..."+pContainer);

		try {
			AgentController agency = mainContainer.createNewAgent("agency",
					"com.aiad.agents.AgencyAgent", new Object[0]);

			AgentController client = mainContainer.createNewAgent("client",
					"com.aiad.agents.ClientAgent"
					, new String[]{
							"Ines",
							"Health",
							"23",
							"Medium Risk", // High Medium or Low risk patient
							"true", // IF TRUE => OBESE ELSE => ANOREXIC
							"false",
							"10",
							"20"});
			AgentController broker1 = mainContainer.createNewAgent("Fred",
					"com.aiad.agents.BrokerAgent"
					, new String[]{
							"Fred",
							"Vehicle|Health",
							"0.8",
							"false",
							"0"});
			AgentController broker2 = mainContainer.createNewAgent("John",
					"com.aiad.agents.BrokerAgent"
					, new String[]{
							"John",
							"Health",
							"0.8",
							"true",
							"25"});
			AgentController broker3 = mainContainer.createNewAgent("Mary",
					"com.aiad.agents.BrokerAgent"
					, new String[]{
							"Mary",
							"Vehicle",
							"0.65",
							"true",
							"8"});
			agency.start();
			client.start();
			broker1.start();
			broker2.start();
			broker3.start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}


	}
	
}
