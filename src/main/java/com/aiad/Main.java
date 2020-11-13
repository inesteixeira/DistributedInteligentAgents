package com.aiad;

import com.aiad.components.api.DroolsCache;
import com.aiad.components.api.EventsTrigger;
import com.aiad.components.impl.DroolsCacheImpl;
import com.aiad.components.impl.EventsTriggerImpl;
import jade.core.Profile;
import jade.core.ProfileImpl;

import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;

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
					"com.aiad.AgencyAgent", new Object[0]);

			AgentController client = mainContainer.createNewAgent("client",
					"com.aiad.ClientAgent"
					, new String[]{
							"Ines",
							"Vehicle",
							"23",
							"Car",
							"2001",
							"true",
							"10",
							"20"});
			AgentController broker1 = mainContainer.createNewAgent("broker1",
					"com.aiad.BrokerAgent"
					, new String[]{
							"Fred",
							"Vehicle|Health",
							"0.8",
							"false",
							"0"});
			AgentController broker2 = mainContainer.createNewAgent("broker2",
					"com.aiad.BrokerAgent"
					, new String[]{
							"John",
							"Health",
							"0.8",
							"true",
							"25"});
			AgentController broker3 = mainContainer.createNewAgent("broker3",
					"com.aiad.BrokerAgent"
					, new String[]{
							"Mary",
							"Vehicle",
							"0.65",
							"true",
							"20"});
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
