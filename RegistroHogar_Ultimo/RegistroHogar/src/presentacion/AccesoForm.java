package presentacion;

import negocio.Usuario;
import persistencia.RolesDAO;
import persistencia.SesionActual;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class AccesoForm extends JFrame {
    public AccesoForm() {
        setTitle("Registro de hogares - Acceso");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel base = new JPanel(new BorderLayout());
        base.setBackground(EstiloInterfaz.FONDO);
        base.add(EstiloInterfaz.encabezado("Registro de hogares", "Inicio de sesión"), BorderLayout.NORTH);

        JTextField documento = new JTextField(24);
        JPasswordField clave = new JPasswordField(24);
        JButton ingresar = EstiloInterfaz.boton("Iniciar sesión", true);

        JPanel campos = EstiloInterfaz.tarjeta(new GridLayout(0, 1, 0, 11));
        campos.add(EstiloInterfaz.campo("Documento", documento));
        campos.add(EstiloInterfaz.campo("Contraseña", clave));
        campos.add(ingresar);
        JPanel cuerpo = new JPanel(new java.awt.GridBagLayout());
        cuerpo.setOpaque(false);
        cuerpo.setBorder(new EmptyBorder(21, 27, 25, 27));
        campos.setPreferredSize(new java.awt.Dimension(430, 255));
        cuerpo.add(campos);
        base.add(cuerpo);
        add(base);
        EstiloInterfaz.tamanoTableta(this);
        getRootPane().setDefaultButton(ingresar);

        ingresar.addActionListener(e -> {
            char[] secreto = clave.getPassword();
            try {
                String numero = documento.getText().trim();
                if (numero.isEmpty() || secreto.length == 0) {
                    JOptionPane.showMessageDialog(this, "Ingrese el documento y la contraseña.");
                    return;
                }
                Usuario usuario = RolesDAO.entrar(numero, new String(secreto));
                if (usuario == null) {
                    JOptionPane.showMessageDialog(this, "Documento o contraseña incorrectos.");
                    return;
                }
                JFrame ventana = switch (usuario.getRol()) {
                    case ADMINISTRADOR -> new AdministradorForm(usuario.getId(), usuario.getNombre());
                    case ENCUESTADOR -> new MisHogaresForm(usuario.getId(), usuario.getNombre());
                    case JEFE_HOGAR -> new JefeHogarForm(usuario.getId(), usuario.getHogar(), usuario.getNombre());
                };
                ventana.setVisible(true);
                dispose();
            } catch (Exception ex) {
                SesionActual.salir();
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "No se pudo iniciar sesión", JOptionPane.ERROR_MESSAGE);
            } finally {
                Arrays.fill(secreto, '\0');
                clave.setText("");
            }
        });

    }

    public static void abrir() {
        EstiloInterfaz.instalarTipografia();
        new AccesoForm().setVisible(true);
    }
}
