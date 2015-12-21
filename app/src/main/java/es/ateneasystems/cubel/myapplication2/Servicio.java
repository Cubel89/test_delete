package es.ateneasystems.cubel.myapplication2;

/**
 * Created by cubel on 15/10/15.
 */

import java.util.Timer;
        import java.util.TimerTask;

        import android.app.Service;
        import android.content.Intent;
        import android.os.Handler;
        import android.os.IBinder;
        import android.os.Message;

public class Servicio extends Service {

    private Timer temporizador = new Timer();
    private static final long INTERVALO_ACTUALIZACION = 30000; // En ms
    public static MainActivity UPDATE_LISTENER;
    private double cronometro = 0;
    private Handler handler;

    /**
     * Establece quien va ha recibir las actualizaciones del cronometro
     *
     * @param poiService
     */
    public static void setUpdateListener(MainActivity poiService) {
        UPDATE_LISTENER = poiService;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        iniciarCronometro();

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                //UPDATE_LISTENER.actualizarCronometro(cronometro);
            }
        };
    }

    @Override
    public void onDestroy() {
        pararCronometro();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    private void iniciarCronometro() {
        temporizador.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                cronometro += 1.00;
                handler.sendEmptyMessage(0);
            }
        }, 0, INTERVALO_ACTUALIZACION);
    }

    private void pararCronometro() {
        if (temporizador != null)
            temporizador.cancel();
    }

}