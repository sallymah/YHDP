package tw.com.hyweb.core.cp.common.impfiles.ImpPersoFeedback.JobExecutor;

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
