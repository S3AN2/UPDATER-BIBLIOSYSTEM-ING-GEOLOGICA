package driwai.updater.ui;
//Permitir que cualquier componente visual (como UpdateWindow)
// pueda recibir actualizaciones del proceso sin depender directamente de la implementación interna.
public interface UIUpdater {
    void updateProgress(int percent, String message);
    void updateStatus(String message);
    void close();
}
