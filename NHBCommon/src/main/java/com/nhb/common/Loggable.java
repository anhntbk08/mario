package com.nhb.common;

import org.slf4j.Logger;

public interface Loggable {

	Logger getLogger();

	Logger getLogger(String name);
}
