package presentacion;

import negocio.DatosEditables;
import negocio.Rol;
import persistencia.GastoDAO;
import persistencia.SesionActual;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;
import java.sql.SQLException;

public class DetalleGastoForm extends JFrame {

    private static final Color COLOR_HEADER = EstiloInterfaz.TINTA;
    private static final Color COLOR_BG = EstiloInterfaz.FONDO;
    private static final Color COLOR_CARD_BG = Color.WHITE;
    private static final Color COLOR_BORDER = new Color(215, 220, 228);
    private static final Color COLOR_TEXT_DARK = new Color(30, 41, 55);
    private static final Color COLOR_TEXT_MUTED = new Color(130, 140, 155);
    private static final Color COLOR_TAG_BG = new Color(220, 245, 230);
    private static final Color COLOR_TAG_TEXT = new Color(35, 130, 70);

    private String nombreHogar;
    private int dia;
    private int indiceGasto;
    private String[] datosGasto;
    private GastosDiaForm ventanaPadre;
    private DatosEditables original;

    public DetalleGastoForm(GastosDiaForm ventanaPadre, String nombreHogar, int dia, int indiceGasto, String[] datosGasto) {
        this.ventanaPadre = ventanaPadre;
        this.nombreHogar = nombreHogar;
        this.dia = dia;
        this.indiceGasto = indiceGasto;
        this.datosGasto = datosGasto;
        initComponents();
    }

    private void initComponents() {
        setTitle("Detalle del gasto");
        EstiloInterfaz.tamanoTableta(this);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent evento) {
                volver();
            }
        });

        JPanel principal = new JPanel(new BorderLayout());
        principal.setBackground(COLOR_BG);
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(COLOR_HEADER);
        cabecera.add(EstiloInterfaz.encabezado("Detalle del gasto", nombreHogar + " · Día " + dia),
                BorderLayout.CENTER);
        JPanel navegacion = EstiloInterfaz.filaBotones();
        navegacion.setBorder(new EmptyBorder(25, 0, 15, 22));
        JButton regresar = EstiloInterfaz.boton("Volver", false);
        regresar.addActionListener(e -> volver());
        navegacion.add(regresar);
        cabecera.add(navegacion, BorderLayout.EAST);
        principal.add(cabecera, BorderLayout.NORTH);

        NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("es-CO"));
        String valor = datosGasto[5] == null ? "—" : "$ " + nf.format(new BigDecimal(datosGasto[5]));
        JPanel informacion = EstiloInterfaz.tarjeta(new BorderLayout(0, 16));
        informacion.add(EstiloInterfaz.titulo("Información del gasto"), BorderLayout.NORTH);
        JPanel datos = new JPanel(new GridLayout(0, 2, 25, 16));
        datos.setOpaque(false);
        agregarCampoDetalle(datos, "Artículo", datosGasto[0]);
        agregarCampoDetalle(datos, "Categoría COICOP", datosGasto[4]);
        agregarCampoDetalle(datos, "Cantidad", datosGasto[1]);
        agregarCampoDetalle(datos, "Unidad", datosGasto[2]);
        agregarCampoDetalle(datos, "Forma de adquisición", datosGasto[3]);
        agregarCampoDetalle(datos, "Valor", valor);
        agregarCampoDetalle(datos, "Frecuencia de compra", datosGasto.length > 6 ? datosGasto[6] : "");
        agregarCampoDetalle(datos, "Tipo de gasto", datosGasto.length > 7 ? datosGasto[7] : "");
        agregarCampoDetalle(datos, "ID del gasto", datosGasto[8]);
        informacion.add(datos, BorderLayout.CENTER);

        JPanel contenido = new JPanel(new BorderLayout(0, 14));
        contenido.setBackground(COLOR_BG);
        contenido.setBorder(new EmptyBorder(20, 24, 20, 24));
        JScrollPane scroll = new JScrollPane(informacion);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        contenido.add(scroll, BorderLayout.CENTER);
        if (ventanaPadre != null && SesionActual.es(Rol.JEFE_HOGAR)) {
            JPanel acciones = EstiloInterfaz.filaBotones();
            agregarAcciones(acciones);
            contenido.add(acciones, BorderLayout.SOUTH);
        }
        principal.add(contenido, BorderLayout.CENTER);
        setContentPane(principal);
    }

    private void volver() {
        if (ventanaPadre != null && ventanaPadre.isDisplayable()) {
            ventanaPadre.cargarGastos();
            ventanaPadre.setVisible(true);
        } else {
            new GastosDiaForm(nombreHogar, dia).setVisible(true);
        }
        dispose();
    }

    private void agregarAcciones(JPanel panel) {
        int hogar = ventanaPadre.getIdHogar();
        int gasto = Integer.parseInt(datosGasto[8]);
        try {
            String motivo = GastoDAO.motivoSoloLectura(hogar, dia);
            if (!motivo.isEmpty()) {
                panel.add(EstiloInterfaz.nota(motivo));
                return;
            }
            original = GastoDAO.cargarEdicionJefe(hogar, dia, gasto);
            String articuloId = original.articuloId() == null ? null : original.articuloId().toString();
            if (datosGasto.length > 9 && !Objects.equals(datosGasto[9], articuloId)) {
                panel.add(EstiloInterfaz.nota("El gasto cambió. Vuelve a abrirlo."));
                return;
            }
        } catch (SQLException ex) {
            panel.add(EstiloInterfaz.nota("Solo lectura. Vuelve a abrir el gasto."));
            return;
        }

        JButton editar = EstiloInterfaz.boton("Editar gasto", true);
        JButton eliminar = EstiloInterfaz.boton("Eliminar gasto", false);
        editar.addActionListener(e -> {
            try {
                String motivo = GastoDAO.motivoSoloLectura(hogar, dia);
                if (!motivo.isEmpty()) {
                    JOptionPane.showMessageDialog(this, motivo);
                    return;
                }
                NuevoGastoForm form = new NuevoGastoForm(nombreHogar, dia, indiceGasto, datosGasto);
                form.setEdicion(gasto, original);
                form.setVentanaGastos(ventanaPadre);
                form.setVisible(true);
                dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Editar gasto", JOptionPane.ERROR_MESSAGE);
            }
        });

        eliminar.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "¿Eliminar este gasto?", "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                GastoDAO.eliminar(hogar, dia, gasto, original);
                ventanaPadre.cargarGastos();
                ventanaPadre.setVisible(true);
                dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Eliminar gasto", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(editar);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(eliminar);
    }

    private void agregarCampoDetalle(JPanel contenedor, String titulo, String valor) {
        JPanel campo = new JPanel(new BorderLayout(0, 5));
        campo.setOpaque(false);
        JLabel etiqueta = EstiloInterfaz.nota(titulo);
        JLabel texto = new JLabel(valor == null || valor.isBlank() ? "—" : valor);
        texto.setFont(EstiloInterfaz.fuente(Font.BOLD, 14));
        texto.setForeground(COLOR_TEXT_DARK);
        campo.add(etiqueta, BorderLayout.NORTH);
        campo.add(texto, BorderLayout.CENTER);
        contenedor.add(campo);
    }
}
