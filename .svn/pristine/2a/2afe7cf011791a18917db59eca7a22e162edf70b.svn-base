package tw.com.hyweb.core.yhdp.batch.framework;

/**
 */
public class TestBatch extends AbstractBatchBasic {
    public void process(String[] argv) throws Exception {
        throw new Exception("just test!");
    }

    public static void main(String[] args) {
        TestBatch tb = new TestBatch();
        try {
            tb.run(args);
        }
        catch (Exception ignore) {
            System.exit(-1);
        }
        System.exit(0);
    }
}
