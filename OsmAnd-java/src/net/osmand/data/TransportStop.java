package net.osmand.data;

public class TransportStop extends MapObject {
	private int[] referencesToRoutes = null;
	
	public TransportStop(){
	}
	
	public int[] getReferencesToRoutes() {
		return referencesToRoutes;
	}
	
	public void setReferencesToRoutes(int[] referencesToRoutes) {
		this.referencesToRoutes = referencesToRoutes;
	}

}
