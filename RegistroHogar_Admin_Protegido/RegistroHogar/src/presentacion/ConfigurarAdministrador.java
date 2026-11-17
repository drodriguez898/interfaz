package presentacion;

import persistencia.RolesDAO;

import java.awt.GridLayout;
import java.util.Arrays;
import javax.swing.*;

/** Alta inicial o recuperación de una cuenta por el dueño del esquema de Oracle. */
public final class ConfigurarAdministrador {
    private ConfigurarAdministrador() {}

    public static void mostrarFormulario() {
        JTextField documento = new JTextField(24);
        JTextField nombre = new JTextField(24);
        JTextField correo = new JTextField(24);
        JPasswordField claveOracle = new JPasswordField(24);
        JPasswordField clave = new JPasswordField(24);
        JPasswordField confirmar = new JPasswordField(24);

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("Contraseña del usuario de Oracle configurado"));
        panel.add(claveOracle);
        panel.add(new JLabel("Documento del administrador"));
        panel.add(documento);
        panel.add(new JLabel("Nombre completo (para una cuenta nueva)"));
        panel.add(nombre);
        panel.add(new JLabel("Correo institucional (opcional)"));
        panel.add(correo);
        panel.add(new JLabel("Contraseña de acceso a la aplicación"));
        panel.add(clave);
        panel.add(new JLabel("Confirmar contraseña de la aplicación"));
        panel.add(confirmar);

        while (JOptionPane.showConfirmDialog(null, panel, "Configurar acceso de administrador",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            char[] secretoOracle = claveOracle.getPassword();
            char[] secreto = clave.getPassword();
            char[] confirmacion = confirmar.getPassword();
            try {
                if (!Arrays.equals(secreto, confirmacion)) {
                    throw new IllegalArgumentException("Las contraseñas de la aplicación no coinciden.");
                }
                RolesDAO.configurarAdministrador(new String(secretoOracle), documento.getText().trim(),
                        nombre.getText().trim(), correo.getText().trim(), new String(secreto));
                JOptionPane.showMessageDialog(null, "Cuenta lista. Ingrese con su documento y contraseña.");
                return;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(),
                        "No se pudo configurar el administrador", JOptionPane.ERROR_MESSAGE);
            } finally {
                Arrays.fill(secretoOracle, '\0');
                Arrays.fill(secreto, '\0');
                Arrays.fill(confirmacion, '\0');
                claveOracle.setText("");
                clave.setText("");
                confirmar.setText("");
            }
        }
    }
}
