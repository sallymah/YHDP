package tw.com.hyweb.svc.yhdp.batch.framework.cursorThread;

import java.util.HashMap;
import java.util.Hashtable;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.svc.yhdp.batch.framework.cursorThread.JobRunnerFactory;

public class ImplementJobRunner implements JobRunnerFactory{

	@Override
	public Runnable create(BatchJob job, Hashtable<String, Long> resultMap,
			String batchDate) {
		// TODO Auto-generated method stub
		return new JobRunner(job, resultMap, batchDate);
	}
}
