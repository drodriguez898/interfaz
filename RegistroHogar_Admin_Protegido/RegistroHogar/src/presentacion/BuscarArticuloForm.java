package presentacion;

import negocio.Articulo;
import negocio.RegistroArticulo;
import persistencia.ArticuloDAO;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class BuscarArticuloForm extends JFrame {

    private static final Color COLOR_HEADER = EstiloInterfaz.TINTA;
    private static final Color COLOR_BG = EstiloInterfaz.FONDO;
    private static final Color COLOR_BORDER = new Color(215, 220, 228);
    private static final Color COLOR_TEXT_DARK = new Color(30, 41, 55);
    private static final Color COLOR_TEXT_MUTED = new Color(100, 110, 125);

    private final NuevoGastoForm formularioPadre;
    private final ArticuloDAO articuloDAO = new ArticuloDAO();
    private final JTextField txtBuscador = new JTextField();
    private final JPanel panelResultados = new JPanel();
    private final JButton btnManual = EstiloInterfaz.boton("Registrar producto nuevo", true);
    private final JLabel lblResultados = new JLabel("Cargando catálogo…");

    private List<Articulo> catalogo = List.of();

    public BuscarArticuloForm(NuevoGastoForm formularioPadre, String nombreHogar, int dia) {
        this.formularioPadre = formularioPadre;
        initComponents(nombreHogar, dia);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowOpened(java.awt.event.WindowEvent evento) {
                cargarCatalogo();
            }
        });
    }

    private void initComponents(String nombreHogar, int dia) {
        setTitle("Buscar artículo");
        EstiloInterfaz.tamanoTableta(this);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent evento) {
                volverAlGasto();
            }
        });
        setLocationRelativeTo(null);

        JPanel principal = new JPanel(new BorderLayout());
        principal.setBackground(COLOR_BG);

        JPanel encabezado = new JPanel();
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));
        encabezado.setBackground(COLOR_HEADER);
        encabezado.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel titulo = new JLabel("Buscar artículo");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(EstiloInterfaz.fuente(Font.BOLD, 18));

        JLabel subtitulo = new JLabel(nombreHogar + " · Día " + dia + " · Sección D");
        subtitulo.setForeground(new Color(185, 195, 205));
        subtitulo.setFont(EstiloInterfaz.fuente(Font.PLAIN, 13));

        JButton btnVolver = EstiloInterfaz.boton("Volver", false);
        btnVolver.addActionListener(evento -> volverAlGasto());

        encabezado.add(titulo);
        encabezado.add(subtitulo);
        encabezado.add(Box.createVerticalStrut(8));
        encabezado.add(btnVolver);
        principal.add(encabezado, BorderLayout.NORTH);

        JPanel cuerpo = new JPanel();
        cuerpo.setLayout(new BoxLayout(cuerpo, BoxLayout.Y_AXIS));
        cuerpo.setBackground(COLOR_BG);
        cuerpo.setBorder(new EmptyBorder(10, 12, 10, 12));

        txtBuscador.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtBuscador.setPreferredSize(new Dimension(0, 36));
        txtBuscador.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtBuscador.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_HEADER, 1, true),
                new EmptyBorder(0, 10, 0, 10)
        ));
        txtBuscador.setEnabled(false);

        txtBuscador.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent evento) {
                filtrar();
            }

            @Override
            public void removeUpdate(DocumentEvent evento) {
                filtrar();
            }

            @Override
            public void changedUpdate(DocumentEvent evento) {
                filtrar();
            }
        });

        lblResultados.setForeground(COLOR_TEXT_MUTED);
        lblResultados.setFont(EstiloInterfaz.fuente(Font.PLAIN, 13));
        lblResultados.setAlignmentX(Component.LEFT_ALIGNMENT);

        panelResultados.setLayout(new BoxLayout(panelResultados, BoxLayout.Y_AXIS));
        panelResultados.setBackground(COLOR_BG);

        JScrollPane scroll = new JScrollPane(panelResultados);
        scroll.setBorder(null);
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnManual.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnManual.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnManual.setEnabled(false);
        btnManual.addActionListener(evento -> registrarProducto());

        cuerpo.add(txtBuscador);
        cuerpo.add(Box.createVerticalStrut(8));
        cuerpo.add(lblResultados);
        cuerpo.add(Box.createVerticalStrut(6));
        cuerpo.add(scroll);
        cuerpo.add(Box.createVerticalStrut(10));
        cuerpo.add(btnManual);

        principal.add(cuerpo, BorderLayout.CENTER);
        setContentPane(principal);
    }

    private void volverAlGasto() {
        if (formularioPadre != null && formularioPadre.isDisplayable()) {
            formularioPadre.setVisible(true);
        }
        dispose();
    }

    private void cargarCatalogo() {
        new SwingWorker<List<Articulo>, Void>() {
            @Override
            protected List<Articulo> doInBackground() throws SQLException {
                return articuloDAO.listar();
            }

            @Override
            protected void done() {
                if (!isDisplayable()) {
                    return;
                }

                try {
                    catalogo = get();
                    txtBuscador.setEnabled(true);
                    btnManual.setEnabled(true);
                    filtrar();
                } catch (InterruptedException | ExecutionException error) {
                    lblResultados.setText("Catálogo no disponible");
                    mostrarError("No se pudo cargar el catálogo", error);
                }
            }
        }.execute();
    }

    private void filtrar() {
        panelResultados.removeAll();
        String filtro = txtBuscador.getText().trim().toLowerCase(Locale.ROOT);

        for (Articulo articulo : catalogo) {
            if (articulo.nombre().toLowerCase(Locale.ROOT).contains(filtro)) {
                agregarResultado(articulo);
            }
        }

        lblResultados.setText(
                panelResultados.getComponentCount() == 0
                        ? "Sin resultados"
                        : "Resultados"
        );
        panelResultados.revalidate();
        panelResultados.repaint();
    }

    private void agregarResultado(Articulo articulo) {
        String categoria = articulo.categoria();
        String etiqueta = categoria.isEmpty()
                ? articulo.nombre()
                : articulo.nombre() + " · " + categoria;

        JButton resultado = new JButton();
        resultado.putClientProperty("html.disable", Boolean.TRUE);
        resultado.setText(etiqueta);
        resultado.setHorizontalAlignment(SwingConstants.LEFT);
        resultado.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultado.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        resultado.setBackground(Color.WHITE);
        resultado.setForeground(COLOR_TEXT_DARK);
        resultado.setFont(EstiloInterfaz.fuente(Font.PLAIN, 13));
        resultado.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        resultado.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        resultado.addActionListener(evento -> seleccionar(articulo));

        panelResultados.add(resultado);
        panelResultados.add(Box.createVerticalStrut(6));
    }

    private void registrarProducto() {
        String entrada = JOptionPane.showInputDialog(
                this, "Nombre del producto", txtBuscador.getText().trim()
        );

        if (entrada == null) {
            return;
        }

        String nombre = entrada.trim();

        if (nombre.isEmpty() || nombre.length() > 200) {
            JOptionPane.showMessageDialog(this, "Escribe el nombre del producto con máximo 200 caracteres.");
            return;
        }

        btnManual.setEnabled(false);

        new SwingWorker<RegistroArticulo, Void>() {
            @Override
            protected RegistroArticulo doInBackground() throws SQLException {
                return articuloDAO.registrar(nombre);
            }

            @Override
            protected void done() {
                if (!isDisplayable()) {
                    return;
                }

                try {
                    RegistroArticulo registro = get();

                    if (registro.existia()) {
                        JOptionPane.showMessageDialog(
                                BuscarArticuloForm.this,
                                "El producto ya estaba en el catálogo. Se seleccionará el existente."
                        );
                    }

                    seleccionar(registro.articulo());
                } catch (InterruptedException | ExecutionException error) {
                    mostrarError("No se pudo registrar el producto", error);
                    btnManual.setEnabled(true);
                }
            }
        }.execute();
    }

    private void seleccionar(Articulo articulo) {
        if (formularioPadre != null && formularioPadre.isDisplayable()) {
            formularioPadre.setArticuloSeleccionado(
                    articulo.nombre(), articulo.categoria()
            );
            formularioPadre.setVisible(true);
        }
        dispose();
    }

    private void mostrarError(String mensaje, Exception error) {
        Throwable causa = error.getCause() == null ? error : error.getCause();

        JOptionPane.showMessageDialog(
                this,
                mensaje + ".\n" + causa.getMessage(),
                "Catálogo de artículos",
                JOptionPane.ERROR_MESSAGE
        );
    }
}