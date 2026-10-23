package br.eni.cora;

import android.content.*;

public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c, Intent i) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(i.getAction()))
            c.startService(new Intent(c, SinoService.class));
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            android.app.job.JobScheduler js = (android.app.job.JobScheduler) c.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            android.app.job.JobInfo.Builder b = new android.app.job.JobInfo.Builder(123,
                    new android.content.ComponentName(c, CoraJobService.class));
            b.setPeriodic(15 * 60 * 1000);
            b.setRequiredNetworkType(android.app.job.JobInfo.NETWORK_TYPE_ANY);
            b.setPersisted(true);
            if (js != null) js.schedule(b.build());
        }
    }
}
