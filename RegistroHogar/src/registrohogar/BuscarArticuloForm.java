package registrohogar;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BuscarArticuloForm extends JFrame {

    private static final Color COLOR_HEADER = new Color(46, 63, 80);
    private static final Color COLOR_BG = new Color(238, 240, 245);
    private static final Color COLOR_CARD_BG = Color.WHITE;
    private static final Color COLOR_BORDER = new Color(215, 220, 228);
    private static final Color COLOR_TEXT_DARK = new Color(30, 41, 55);
    private static final Color COLOR_TEXT_MUTED = new Color(130, 140, 155);

    private JTextField txtBuscador;
    private JPanel panelResultados;
    private NuevoGastoForm formularioPadre;

    // Catálogo base de artículos COICOP
    private static final String[][] CATALOGO_COICOP = {
        {"Pan blandito pequeño", "01.1.1.3 · Pan y cereales"},
        {"Pan de bono", "01.1.1.3 · Pan y cereales"},
        {"Pan tajado", "01.1.1.3 · Pan y cereales"},
        {"Pan integral", "01.1.1.3 · Pan y cereales"},
        {"Leche entera 1L", "01.1.4.1 · Leche fresca"},
        {"Hígado de res", "01.1.2.1 · Carne de res"},
        {"Arroz blanco", "01.1.1.1 · Arroz"},
        {"Aceite vegetal", "01.1.5.1 · Aceites comestibles"},
        {"Huevos AA", "01.1.4.7 · Huevos"},
        {"Café molido", "01.2.1.1 · Café"}
    };

    public BuscarArticuloForm(NuevoGastoForm formularioPadre, String nombreHogar, int dia) {
        this.formularioPadre = formularioPadre;
        initComponents(nombreHogar, dia);
    }

    private void initComponents(String nombreHogar, int dia) {
        setTitle("Buscar artículo");
        setSize(360, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(COLOR_BG);

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(COLOR_HEADER);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel lblTitulo = new JLabel("Buscar artículo");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(Color.WHITE);

        JLabel lblSubtitulo = new JLabel(nombreHogar + " · Día " + dia + " · Sección D");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSubtitulo.setForeground(new Color(185, 195, 205));

        headerPanel.add(lblTitulo);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        headerPanel.add(lblSubtitulo);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Cuerpo
        JPanel bodyPanel = new JPanel();
        bodyPanel.setBackground(COLOR_BG);
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBorder(new EmptyBorder(10, 12, 10, 12));

        // Caja de búsqueda
        txtBuscador = new JTextField();
        txtBuscador.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtBuscador.setPreferredSize(new Dimension(0, 36));
        txtBuscador.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtBuscador.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_HEADER, 1, true),
                new EmptyBorder(0, 10, 0, 10)
        ));

        JLabel lblResultados = new JLabel("Resultados");
        lblResultados.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblResultados.setForeground(COLOR_TEXT_MUTED);

        panelResultados = new JPanel();
        panelResultados.setOpaque(false);
        panelResultados.setLayout(new BoxLayout(panelResultados, BoxLayout.Y_AXIS));

        // Filtrado dinámico al escribir
        txtBuscador.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            public void changedUpdate(DocumentEvent e) { filtrar(); }
        });

        bodyPanel.add(txtBuscador);
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        bodyPanel.add(lblResultados);
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        JScrollPane scrollResultados = new JScrollPane(panelResultados);
        scrollResultados.setBorder(null);
        scrollResultados.setOpaque(false);
        scrollResultados.getViewport().setOpaque(false);
        bodyPanel.add(scrollResultados);

        // Botón agregar manual
        JLabel lblNoEncuentras = new JLabel("¿No encuentras el artículo?");
        lblNoEncuentras.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblNoEncuentras.setForeground(COLOR_TEXT_MUTED);

        JButton btnManual = new JButton("Agregar artículo manualmente");
        btnManual.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnManual.setForeground(COLOR_HEADER);
        btnManual.setBackground(Color.WHITE);
        btnManual.setBorder(new LineBorder(COLOR_HEADER, 1, true));
        btnManual.setFocusPainted(false);
        btnManual.setPreferredSize(new Dimension(0, 38));
        btnManual.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnManual.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnManual.addActionListener(e -> {
            String textoBusqueda = txtBuscador.getText().trim();
            if (formularioPadre != null) {
                formularioPadre.setArticuloSeleccionado(textoBusqueda.isEmpty() ? "Artículo personalizado" : textoBusqueda, "01.9.9.9 · Otro artículo");
            }
            this.dispose();
        });

        bodyPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        bodyPanel.add(lblNoEncuentras);
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        bodyPanel.add(btnManual);

        mainPanel.add(bodyPanel, BorderLayout.CENTER);
        add(mainPanel);

        filtrar(); // Cargar lista inicial
    }

    private void filtrar() {
        panelResultados.removeAll();
        String filtro = txtBuscador.getText().trim().toLowerCase();

        for (String[] item : CATALOGO_COICOP) {
            if (filtro.isEmpty() || item[0].toLowerCase().contains(filtro)) {
                agregarTarjetaResultado(item[0], item[1]);
            }
        }

        panelResultados.revalidate();
        panelResultados.repaint();
    }

    private void agregarTarjetaResultado(String nombre, String categoria) {
        JPanel card = new JPanel();
        card.setBackground(COLOR_CARD_BG);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblNombre = new JLabel(nombre);
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblNombre.setForeground(COLOR_TEXT_DARK);

        JLabel lblCat = new JLabel(categoria);
        lblCat.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblCat.setForeground(COLOR_TEXT_MUTED);

        card.add(lblNombre);
        card.add(Box.createRigidArea(new Dimension(0, 2)));
        card.add(lblCat);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (formularioPadre != null) {
                    formularioPadre.setArticuloSeleccionado(nombre, categoria);
                }
                dispose();
            }
        });

        panelResultados.add(card);
        panelResultados.add(Box.createRigidArea(new Dimension(0, 6)));
    }
}