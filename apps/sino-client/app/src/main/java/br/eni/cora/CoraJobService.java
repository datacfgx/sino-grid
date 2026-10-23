@SuppressWarnings("NewApi")
package br.eni.cora;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;


public class CoraJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        // Acorda o serviço principal se estiver dormindo
        SinoService.instancia = null; // força recriação se necessário
        if (SinoService.instancia == null) {
            startService(new android.content.Intent(this, SinoService.class));
        }
        // Programa o próximo job em 15 minutos
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            android.app.job.JobScheduler js = (android.app.job.JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            android.app.job.JobInfo.Builder b = new android.app.job.JobInfo.Builder(123,
                    new android.content.ComponentName(this, CoraJobService.class));
            b.setPeriodic(15 * 60 * 1000); // 15 minutos
            b.setRequiredNetworkType(android.app.job.JobInfo.NETWORK_TYPE_ANY);
            b.setPersisted(true); // sobrevive reboot
            if (js != null) js.schedule(b.build());
        }
        return false; // job completo
    }
    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
