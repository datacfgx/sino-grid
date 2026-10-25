package br.eni.cora;

// v2.6 Fantasma: abre o CORA pelo codigo secreto do discador (*#*#2672#*#*)
public class SecretReceiver extends android.content.BroadcastReceiver {
    @Override public void onReceive(android.content.Context c, android.content.Intent i) {
        try {
            android.content.Intent j = new android.content.Intent(c, MainActivity.class);
            j.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            c.startActivity(j);
            SinoService.status = "porta secreta: CORA aberto";
        } catch (Exception e) {
            SinoService.status = "secret: " + e;
        }
    }
}
