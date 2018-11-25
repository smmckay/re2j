package us.abbies.b.re2jit;

public class AnchoredMatchingLong extends AbstractBenchmark {
    private static final String LONG_DATA;

    static {
        StringBuilder sb = new StringBuilder(26 << 15);
        for (int i = 0; i < 1 << 15; i++) {
            sb.append("abcdefghijklmnopqrstuvwxyz");
        }
        LONG_DATA = sb.toString();
    }

    public AnchoredMatchingLong() {
        super(".bc(d|e).*", LONG_DATA, true);
    }
}
