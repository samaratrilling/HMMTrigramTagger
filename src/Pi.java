
public class Pi {
	public int k;
	public String v;
	public double logProbability;
	public Pi backPointer;
	
	public Pi (double logProbability, Pi backPointer, String v, int k) {
		this.logProbability = logProbability;
		this.backPointer = backPointer;
		this.v = v;
		this.k = k;
	}
}
