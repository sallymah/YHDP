package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpFmTxnDtl;

public interface IJobExecutor
{
    /**
     * execute the runnable,just put JobRunner to queue
     * 
     * @param job
     * @return true if execute ok; false if execute fail
     */
    public boolean execute(JobRunner job);
}
