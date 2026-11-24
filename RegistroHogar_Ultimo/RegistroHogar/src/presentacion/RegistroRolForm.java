package presentacion;

import persistencia.RolesDAO;

import java.awt.*;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

final class RegistroRolForm extends JDialog {
    RegistroRolForm(JFrame anterior, int idAdministrador, Runnable alGuardar) {
        super(anterior, "Crear encuestador", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JTextField documento = new JTextField(22);
        JTextField nombre = new JTextField(22);
        JPasswordField clave = new JPasswordField(22);
        JPasswordField confirmar = new JPasswordField(22);
        JButton guardar = EstiloInterfaz.boton("Guardar encuestador", true);
        JButton volver = EstiloInterfaz.boton("Cerrar", false);

        JPanel campos = EstiloInterfaz.tarjeta(new GridLayout(0, 1, 0, 8));
        campos.add(EstiloInterfaz.campo("Documento", documento));
        campos.add(EstiloInterfaz.campo("Nombre completo", nombre));
        campos.add(EstiloInterfaz.campo("Contraseña · mínimo 8 caracteres", clave));
        campos.add(EstiloInterfaz.campo("Confirmar contraseña", confirmar));
        JPanel acciones = EstiloInterfaz.filaBotones();
        acciones.add(volver);
        acciones.add(guardar);
        JPanel cuerpo = new JPanel(new BorderLayout(0, 14));
        cuerpo.setBackground(EstiloInterfaz.FONDO);
        cuerpo.setBorder(new EmptyBorder(17, 22, 17, 22));
        JPanel centro = new JPanel(new GridBagLayout());
        centro.setOpaque(false);
        campos.setPreferredSize(new Dimension(500, 360));
        centro.add(campos);
        JScrollPane scroll = new JScrollPane(centro);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        cuerpo.add(scroll, BorderLayout.CENTER);
        cuerpo.add(acciones, BorderLayout.SOUTH);
        add(EstiloInterfaz.encabezado("Crear encuestador", ""), BorderLayout.NORTH);
        add(cuerpo, BorderLayout.CENTER);

        volver.addActionListener(e -> dispose());
        guardar.addActionListener(e -> {
            char[] secreto = clave.getPassword();
            char[] confirmacion = confirmar.getPassword();
            try {
                if (!Arrays.equals(secreto, confirmacion)) {
                    throw new IllegalArgumentException("Las contraseñas no coinciden.");
                }
                RolesDAO.registrarEncuestador(idAdministrador, documento.getText().trim(),
                        nombre.getText().trim(), new String(secreto));
                alGuardar.run();
                JOptionPane.showMessageDialog(this, "Encuestador guardado.");
                documento.setText("");
                nombre.setText("");
                documento.requestFocusInWindow();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo registrar", JOptionPane.ERROR_MESSAGE);
            } finally {
                Arrays.fill(secreto, '\0');
                Arrays.fill(confirmacion, '\0');
                clave.setText("");
                confirmar.setText("");
            }
        });

        getRootPane().setDefaultButton(guardar);
        pack();
        EstiloInterfaz.tamanoTableta(this);
        setLocationRelativeTo(anterior);
    }
}
