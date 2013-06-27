package com.sitewhere.server.asset.filesystem;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * XML binding for an asset property.
 * 
 * @author Derek Adams
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class FileSystemAssetProperty {

	@XmlAttribute
	private String name;

	@XmlAttribute
	private String value;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}