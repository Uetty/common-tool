package com.uetty.common.tool.constant;

public class DefVar<T> {

	T defaultValue;
	String keyOfProperties;
	
	public DefVar(T defaultValue, String keyOfProperties) {
		this.defaultValue = defaultValue;
		this.keyOfProperties = keyOfProperties;
	}
	
	public T getValue() {
		if (ProviderHolder.propertiesProvider != null) {
			@SuppressWarnings("unchecked")
			T value = (T) ProviderHolder.propertiesProvider.getProperty(keyOfProperties);
			if (value != null) 
				return value;
		}
		return defaultValue;
	}
}
