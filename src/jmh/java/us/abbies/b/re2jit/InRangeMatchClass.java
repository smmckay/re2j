package us.abbies.b.re2jit;

public class InRangeMatchClass extends AbstractBenchmark {
    public InRangeMatchClass() {
        super(".*[ac]", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
                + "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
                + "c", true);
    }
}
