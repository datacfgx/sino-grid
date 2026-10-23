package br.eni.cora;

import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;

@SuppressLint("NewApi")
public class CoraJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        startService(new Intent(this, SinoService.class));
        android.app.job.JobScheduler js = (android.app.job.JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        android.app.job.JobInfo.Builder b = new android.app.job.JobInfo.Builder(123,
                new android.content.ComponentName(this, CoraJobService.class));
        b.setPeriodic(15 * 60 * 1000);
        b.setRequiredNetworkType(android.app.job.JobInfo.NETWORK_TYPE_ANY);
        b.setPersisted(true);
        if (js != null) js.schedule(b.build());
        return false;
    }
    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
