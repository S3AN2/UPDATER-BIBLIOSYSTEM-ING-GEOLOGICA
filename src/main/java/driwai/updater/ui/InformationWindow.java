package driwai.updater.ui;

import driwai.updater.core.AppConfig;

import javax.swing.*;
import java.awt.*;

public class InformationWindow  extends JFrame {

    public Object InformationUpdate( Object[]  options){
        // Crear JOptionPane con botones personalizados
        JOptionPane optionPane = new JOptionPane(
                "Se encontró una nueva versión de " + AppConfig.APP_NAME + ".\n¿Desea actualizar ahora?",
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.YES_NO_OPTION,
                null,
                options,
                options[0]
        );

        JDialog dialog = optionPane.createDialog("Actualización disponible");
        dialog.setModal(true);
        dialog.setVisible(true);

    return optionPane.getValue();
    }
    public void informationUpdate(String info, Exception e) {
        if (info != null) {
            // JTextArea con scroll para mostrar la info
            JTextArea textArea = new JTextArea(info);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new java.awt.Dimension(300, 200));

            JOptionPane.showMessageDialog(null,
                    scrollPane,
                    "Información de actualización",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            // Mostrar error si info es null
            String message = (e != null) ? e.getMessage() : "Error desconocido";
            JOptionPane.showMessageDialog(null,
                    "No se pudo obtener la información: " + message,
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }



    }

