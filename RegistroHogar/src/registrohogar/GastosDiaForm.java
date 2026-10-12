package registrohogar;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

public class GastosDiaForm extends JFrame {

    private static final Color COLOR_HEADER = new Color(46, 63, 80);
    private static final Color COLOR_BG = new Color(238, 240, 245);
    private static final Color COLOR_CARD_BG = Color.WHITE;
    private static final Color COLOR_BORDER = new Color(215, 220, 228);
    private static final Color COLOR_TEXT_DARK = new Color(30, 41, 55);
    private static final Color COLOR_TEXT_MUTED = new Color(130, 140, 155);

    private String nombreHogar;
    private int dia;
    
    // Mapa que almacena la lista de gastos independientemente para cada día
    private static Map<Integer, List<String[]>> mapaGastosPorDia = new HashMap<>();
    
    private DetalleHogarForm detalleHogarForm;
    private JPanel panelGastos;
    private JLabel lblTotal;
    private JLabel lblHeaderSub;

    public GastosDiaForm() {
        this("Hogar 45091", 1, null);
    }

    public GastosDiaForm(String nombreHogar, int dia) {
        this(nombreHogar, dia, null);
    }

    public GastosDiaForm(String nombreHogar, int dia, DetalleHogarForm detalleHogarForm) {
        this.nombreHogar = nombreHogar;
        this.dia = dia;
        this.detalleHogarForm = detalleHogarForm;
        initComponents();
        cargarGastos();
    }

    public static List<String[]> getListaGastosDelDia(int dia) {
        return mapaGastosPorDia.getOrDefault(dia, new ArrayList<>());
    }

    public static void agregarGastoAManuscrito(int dia, String articulo, String cantidad, String unidad, String adquisicion, String categoria, String valor, String frecuencia, String tipoGasto) {
        mapaGastosPorDia.putIfAbsent(dia, new ArrayList<>());
        mapaGastosPorDia.get(dia).add(new String[]{articulo, cantidad, unidad, adquisicion, categoria, valor, frecuencia, tipoGasto});
    }

    public static void actualizarGasto(int dia, int index, String articulo, String cantidad, String unidad, String adquisicion, String categoria, String valor, String frecuencia, String tipoGasto) {
        List<String[]> lista = mapaGastosPorDia.get(dia);
        if (lista != null && index >= 0 && index < lista.size()) {
            lista.set(index, new String[]{articulo, cantidad, unidad, adquisicion, categoria, valor, frecuencia, tipoGasto});
        }
    }

    public static void eliminarGasto(int dia, int index) {
        List<String[]> lista = mapaGastosPorDia.get(dia);
        if (lista != null && index >= 0 && index < lista.size()) {
            lista.remove(index);
        }
    }

    private void initComponents() {
        setTitle("Gastos del día " + dia);
        setSize(360, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(COLOR_BG);

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(COLOR_HEADER);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel lblTitulo = new JLabel("Gastos del día " + dia);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(Color.WHITE);

        lblHeaderSub = new JLabel(nombreHogar + " · 0 gastos");
        lblHeaderSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblHeaderSub.setForeground(new Color(185, 195, 205));

        headerPanel.add(lblTitulo);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        headerPanel.add(lblHeaderSub);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel bodyPanel = new JPanel();
        bodyPanel.setBackground(COLOR_BG);
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBorder(new EmptyBorder(10, 12, 10, 12));

        panelGastos = new JPanel();
        panelGastos.setOpaque(false);
        panelGastos.setLayout(new BoxLayout(panelGastos, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(panelGastos);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        bodyPanel.add(scroll);

        JPanel panelTotal = new JPanel(new BorderLayout());
        panelTotal.setOpaque(false);
        panelTotal.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        JLabel lblTotalTitulo = new JLabel("Total registrado (COP)");
        lblTotalTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTotalTitulo.setForeground(COLOR_TEXT_MUTED);

        lblTotal = new JLabel("0");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotal.setForeground(COLOR_TEXT_DARK);

        panelTotal.add(lblTotalTitulo, BorderLayout.WEST);
        panelTotal.add(lblTotal, BorderLayout.EAST);

        bodyPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        bodyPanel.add(panelTotal);
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton btnAgregarGasto = new RoundedButton("Agregar gasto", COLOR_HEADER, Color.WHITE);
        JButton btnCerrarDia = new RoundedButton("Cerrar día " + dia, new Color(150, 160, 172), Color.WHITE);

        btnAgregarGasto.addActionListener(e -> {
            NuevoGastoForm form = new NuevoGastoForm(nombreHogar, dia);
            form.setVisible(true);
            this.dispose();
        });

        btnCerrarDia.addActionListener(e -> {
            CerrarDiaForm formCierre = new CerrarDiaForm(nombreHogar, dia, detalleHogarForm);
            formCierre.setVisible(true);
            this.dispose();
        });

        bodyPanel.add(btnAgregarGasto);
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        bodyPanel.add(btnCerrarDia);

        JLabel lblNota = new JLabel("Completa los gastos para poder cerrar el día.");
        lblNota.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblNota.setForeground(COLOR_TEXT_MUTED);
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        bodyPanel.add(lblNota);

        mainPanel.add(bodyPanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    public void cargarGastos() {
        panelGastos.removeAll();
        long totalCOP = 0;

        List<String[]> listaDia = getListaGastosDelDia(dia);

        for (int i = 0; i < listaDia.size(); i++) {
            String[] g = listaDia.get(i);
            String art = g[0];
            String cant = g[1] + " " + g[2];
            String adq = g[3];
            String cat = g[4].contains("·") ? g[4].split("·")[0].trim() : g[4];
            long val = 0;
            try { val = Long.parseLong(g[5]); } catch (Exception e) {}
            totalCOP += val;

            int indexActual = i;
            JPanel card = crearTarjetaGasto(art, cant + " · " + adq + " · " + cat, val);
            
            card.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    DetalleGastoForm detalle = new DetalleGastoForm(GastosDiaForm.this, nombreHogar, dia, indexActual, g);
                    detalle.setVisible(true);
                }
            });

            panelGastos.add(card);
            panelGastos.add(Box.createRigidArea(new Dimension(0, 6)));
        }

        NumberFormat nf = NumberFormat.getInstance(new Locale("es", "CO"));
        lblTotal.setText(nf.format(totalCOP));
        lblHeaderSub.setText(nombreHogar + " · " + listaDia.size() + " gasto" + (listaDia.size() != 1 ? "s" : ""));

        panelGastos.revalidate();
        panelGastos.repaint();
    }

    private JPanel crearTarjetaGasto(String titulo, String subtitulo, long valor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblTit = new JLabel(titulo);
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTit.setForeground(COLOR_TEXT_DARK);

        JLabel lblSub = new JLabel(subtitulo);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblSub.setForeground(COLOR_TEXT_MUTED);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(lblTit);
        left.add(Box.createRigidArea(new Dimension(0, 2)));
        left.add(lblSub);

        NumberFormat nf = NumberFormat.getInstance(new Locale("es", "CO"));
        JLabel lblVal = new JLabel(nf.format(valor));
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblVal.setForeground(COLOR_TEXT_DARK);

        card.add(left, BorderLayout.WEST);
        card.add(lblVal, BorderLayout.EAST);
        return card;
    }

    private class RoundedButton extends JButton {
        public RoundedButton(String text, Color bg, Color fg) {
            super(text);
            setOpaque(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(fg);
            setBackground(bg);
            setPreferredSize(new Dimension(0, 36));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            super.paintComponent(g2);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GastosDiaForm().setVisible(true));
    }
}