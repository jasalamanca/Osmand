package net.osmand.binary;

public abstract class BinaryIndexPart {

	String name;
	int length;
	int filePointer;

	public int getLength() {
		return length;
	}

	public int getFilePointer() {
		return filePointer;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}
