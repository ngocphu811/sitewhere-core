package com.sitewhere.server.asset.filesystem;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Object used to marshal an XML file on the filesystem so it can be used to define a list of hardware assets.
 * 
 * @author Derek Adams
 */
@XmlRootElement(name = "hardware-assets")
@XmlAccessorType(XmlAccessType.FIELD)
public class FileSystemHardwareAssets {

	@XmlElement(name = "hardware-asset")
	private List<FileSystemHardwareAsset> hardwareAssets = new ArrayList<FileSystemHardwareAsset>();

	public List<FileSystemHardwareAsset> getHardwareAssets() {
		return hardwareAssets;
	}

	public void setHardwareAssets(List<FileSystemHardwareAsset> hardwareAssets) {
		this.hardwareAssets = hardwareAssets;
	}
}