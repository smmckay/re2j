package us.abbies.b.re2jit;

public class AnchoredMatchingShort extends AbstractBenchmark {
    public AnchoredMatchingShort() {
        super(".bc(d|e).*", "abcdefghijklmnopqrstuvwxyz", true);
    }
}
