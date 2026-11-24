package presentacion;

import negocio.DatosHogar;
import negocio.DatosJefe;
import negocio.Rol;
import persistencia.HogarDAO;
import persistencia.SesionActual;

import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.IntConsumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

final class RegistroHogarForm extends JPanel {
    private final JTextField familia = new JTextField(24);
    private final JTextField direccion = new JTextField(24);
    private final JTextField personas = new JTextField(8);
    private final JTextField fecha = new JTextField(LocalDate.now().toString(), 10);
    private final JComboBox<String> departamento = new JComboBox<>(new String[]{
        "Seleccione el departamento", "Amazonas", "Antioquia", "Arauca", "Atlántico",
        "Bogotá D. C.", "Bolívar", "Boyacá", "Caldas", "Caquetá", "Casanare", "Cauca", "Cesar",
        "Chocó", "Córdoba", "Cundinamarca", "Guainía", "Guaviare", "Huila", "La Guajira",
        "Magdalena", "Meta", "Nariño", "Norte de Santander", "Putumayo", "Quindío",
        "Risaralda", "San Andrés, Providencia y Santa Catalina", "Santander", "Sucre",
        "Tolima", "Valle del Cauca", "Vaupés", "Vichada"
    });
    private static final String[] CODIGOS = {"", "91", "05", "81", "08", "11", "13",
        "15", "17", "18", "85", "19", "20", "27", "23", "25", "94", "95", "41", "44",
        "47", "50", "52", "54", "86", "63", "66", "88", "68", "70", "73", "76", "97", "99"};
    private final JComboBox<String> municipio = new JComboBox<>();
    private final JTextField nombreJefe = new JTextField(24);
    private final JTextField documento = new JTextField(14);
    private final JTextField edad = new JTextField(5);
    private final JPasswordField clave = new JPasswordField(24);
    private final JPasswordField confirmar = new JPasswordField(24);
    private final JButton guardar = EstiloInterfaz.boton("Guardar hogar y cuenta", true);

    static void mostrar(JFrame ventana, int administrador, IntConsumer alGuardar) throws SQLException {
        SesionActual.exigirUsuario(Rol.ADMINISTRADOR, administrador);
        JDialog dialogo = new JDialog(ventana, "Registrar hogar", true);
        dialogo.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        RegistroHogarForm contenido = new RegistroHogarForm(administrador, hogar -> {
            dialogo.dispose();
            alGuardar.accept(hogar);
        }, dialogo::dispose);
        dialogo.setContentPane(contenido);
        dialogo.getRootPane().setDefaultButton(contenido.guardar);
        dialogo.pack();
        EstiloInterfaz.tamanoTableta(dialogo);
        dialogo.setLocationRelativeTo(ventana);
        dialogo.setVisible(true);
    }

    RegistroHogarForm(int administrador, IntConsumer alGuardar, Runnable cancelar) {
        super(new BorderLayout());
        setBackground(EstiloInterfaz.FONDO);
        add(EstiloInterfaz.encabezado("Registrar hogar", "El jefe de hogar podrá iniciar sesión al guardar."),
                BorderLayout.NORTH);

        municipio.setEditable(true);
        departamento.addActionListener(e -> sugerirMunicipios());
        sugerirMunicipios();
        JPanel datosHogar = EstiloInterfaz.tarjeta(new GridLayout(0, 1, 0, 9));
        datosHogar.add(EstiloInterfaz.titulo("Hogar"));
        datosHogar.add(EstiloInterfaz.campo("Nombre de la familia", familia));
        datosHogar.add(EstiloInterfaz.campo("Dirección", direccion));
        datosHogar.add(EstiloInterfaz.campo("Departamento", departamento));
        datosHogar.add(EstiloInterfaz.campo("Municipio", municipio));
        datosHogar.add(juntos(EstiloInterfaz.campo("Personas", personas),
                EstiloInterfaz.campo("Inicio · AAAA-MM-DD", fecha)));

        JPanel datosJefe = EstiloInterfaz.tarjeta(new GridLayout(0, 1, 0, 9));
        datosJefe.add(EstiloInterfaz.titulo("Jefe de hogar"));
        datosJefe.add(EstiloInterfaz.campo("Nombre completo", nombreJefe));
        datosJefe.add(juntos(EstiloInterfaz.campo("Documento", documento),
                EstiloInterfaz.campo("Edad", edad)));
        datosJefe.add(EstiloInterfaz.campo("Contraseña · mínimo 8 caracteres", clave));
        datosJefe.add(EstiloInterfaz.campo("Confirmar contraseña", confirmar));

        JPanel columnas = new JPanel(new GridLayout(1, 2, 16, 0));
        columnas.setBackground(EstiloInterfaz.FONDO);
        columnas.setBorder(new EmptyBorder(18, 22, 18, 22));
        columnas.add(datosHogar);
        columnas.add(datosJefe);
        JScrollPane scroll = new JScrollPane(columnas);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        add(scroll, BorderLayout.CENTER);

        JPanel acciones = EstiloInterfaz.filaBotones();
        acciones.setBorder(new EmptyBorder(0, 16, 16, 14));
        JButton volver = EstiloInterfaz.boton("Cancelar", false);
        acciones.add(volver);
        acciones.add(guardar);
        add(acciones, BorderLayout.SOUTH);

        volver.addActionListener(e -> cancelar.run());
        guardar.addActionListener(e -> guardar(administrador, alGuardar));
    }

    private JPanel juntos(JComponent primero, JComponent segundo) {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));
        panel.setOpaque(false);
        panel.add(primero);
        panel.add(segundo);
        return panel;
    }

    private void sugerirMunicipios() {
        int indice = departamento.getSelectedIndex();
        List<String> sugerencias = new ArrayList<>();
        if (indice > 0) {
            InputStream fuente = RegistroHogarForm.class.getResourceAsStream("municipios.csv");
            if (fuente == null) {
                JOptionPane.showMessageDialog(this, "No se encontró el listado de municipios. "
                        + "Escribe el municipio manualmente.");
            } else {
                try (BufferedReader lector = new BufferedReader(new InputStreamReader(fuente, StandardCharsets.UTF_8))) {
                    String linea;
                    while ((linea = lector.readLine()) != null) {
                        String[] partes = linea.split(";", 2);
                        if (partes.length == 2 && partes[0].equals(CODIGOS[indice])) {
                            sugerencias.add(partes[1]);
                        }
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "No se pudo leer el listado de municipios. "
                            + "Puedes escribirlo manualmente.");
                }
            }
        }
        sugerencias.sort(java.text.Collator.getInstance(new java.util.Locale("es", "CO")));
        municipio.setModel(new DefaultComboBoxModel<>(sugerencias.toArray(String[]::new)));
        municipio.getEditor().setItem(indice > 0 && "11".equals(CODIGOS[indice]) ? "Bogotá, D.C." : "");
    }

    private void guardar(int administrador, IntConsumer alGuardar) {
        char[] secreto = clave.getPassword();
        char[] confirmacion = confirmar.getPassword();
        int id;
        try {
            if (!Arrays.equals(secreto, confirmacion)) {
                throw new IllegalArgumentException("Las contraseñas no coinciden.");
            }
            int cantidad;
            int anos;
            try {
                cantidad = Integer.parseInt(personas.getText().trim());
                anos = Integer.parseInt(edad.getText().trim());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Ingrese la cantidad de personas y la edad en números.");
            }
            LocalDate inicio;
            try {
                inicio = LocalDate.parse(fecha.getText().trim());
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("Ingrese la fecha con el formato AAAA-MM-DD.");
            }

            String region = departamento.getSelectedIndex() > 0 ? (String) departamento.getSelectedItem() : "";
            Object ciudad = municipio.getEditor().getItem();
            DatosHogar hogar = new DatosHogar(familia.getText().trim(), direccion.getText().trim(), region,
                    ciudad == null ? "" : ciudad.toString().trim(), inicio, cantidad);
            DatosJefe jefe = new DatosJefe(nombreJefe.getText().trim(), anos, documento.getText().trim());
            id = HogarDAO.guardarHogar(administrador, hogar, jefe, new String(secreto));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo guardar el hogar",
                    JOptionPane.ERROR_MESSAGE);
            return;
        } finally {
            Arrays.fill(secreto, '\0');
            Arrays.fill(confirmacion, '\0');
        }
        clave.setText("");
        confirmar.setText("");
        alGuardar.accept(id);
    }
}
