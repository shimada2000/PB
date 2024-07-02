package mapd.solvers.Interface;

/**
 * IPrioritizable
 */
public interface IPriority {
    public double priority();
    public boolean isPrior(IPriority iPriority);
    public void updatePriority();
}