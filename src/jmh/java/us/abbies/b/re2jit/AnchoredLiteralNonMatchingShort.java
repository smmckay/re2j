package us.abbies.b.re2jit;

public class AnchoredLiteralNonMatchingShort extends AbstractBenchmark {
    public AnchoredLiteralNonMatchingShort() {
        super("zbc(d|e).*", "abcdefghijklmnopqrstuvwxyz", false);
    }
}
