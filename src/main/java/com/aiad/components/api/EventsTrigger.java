package com.aiad.components.api;

import com.aiad.Vehicle;

public interface EventsTrigger {

    Object getTriggeredEvents(Object payload, String rule);
}
