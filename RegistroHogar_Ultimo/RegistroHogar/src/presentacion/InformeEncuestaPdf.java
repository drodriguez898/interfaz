package presentacion;

import negocio.DatosInformeEncuesta;
import negocio.GastoRevision;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

final class InformeEncuestaPdf implements AutoCloseable {
    private static final Locale COLOMBIA = Locale.forLanguageTag("es-CO");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", COLOMBIA);
    private static final PDRectangle PAPEL = PDRectangle.LETTER;
    private static final float MARGEN = 44;
    private static final float ANCHO = PAPEL.getWidth() - 2 * MARGEN;
    private static final float FIN_TABLA = PAPEL.getHeight() - 65;
    private static final float ALTO_LINEA = 12;
    private static final float[] COLUMNAS = {43, 209, 76, 84, 112};
    private static final Color TINTA = new Color(25, 47, 64);
    private static final Color TEXTO_SECUNDARIO = new Color(69, 92, 107);
    private static final Color LINEA = new Color(203, 218, 226);
    private static final Color ACENTO = new Color(0, 105, 97);
    private static final Color FONDO = new Color(242, 247, 249);
    private static final Color FONDO_CABECERA_TABLA = new Color(224, 235, 241);

    private final PDDocument documento;
    private final PDFont normal;
    private final PDFont negrita;
    private final NumberFormat dinero = NumberFormat.getNumberInstance(COLOMBIA);
    private final NumberFormat cantidades = NumberFormat.getNumberInstance(COLOMBIA);
    private PDPageContentStream pagina;
    private float cursor;

    private InformeEncuestaPdf(PDDocument documento) throws IOException {
        this.documento = documento;
        normal = fuente("LiberationSans-Regular.ttf");
        negrita = fuente("LiberationSans-Bold.ttf");
        dinero.setMinimumFractionDigits(2);
        dinero.setMaximumFractionDigits(2);
        cantidades.setMaximumFractionDigits(8);
    }

    static void guardar(DatosInformeEncuesta datos, Path archivo) throws IOException {
        try (PDDocument documento = new PDDocument()) {
            documento.getDocumentInformation().setTitle("Informe de encuesta " + datos.encuesta());
            documento.getDocumentInformation().setSubject("Registro de hogares");
            documento.getDocumentCatalog().setLanguage("es-CO");
            try (InformeEncuestaPdf informe = new InformeEncuestaPdf(documento)) {
                informe.dibujar(datos);
                informe.close();
                informe.agregarPie(datos.encuesta());
            }
            documento.save(archivo.toFile());
        }
    }

    private PDFont fuente(String nombre) throws IOException {
        String ruta = "/presentacion/fuentes/" + nombre;
        try (InputStream archivo = InformeEncuestaPdf.class.getResourceAsStream(ruta)) {
            if (archivo == null) {
                throw new IOException("No se encontró la fuente del informe.");
            }
            return PDType0Font.load(documento, archivo);
        }
    }

    private void dibujar(DatosInformeEncuesta datos) throws IOException {
        nuevaPagina(datos, true);
        resumen(datos);
        seccion("Detalle de gastos");
        cabeceraTabla();
        if (datos.gastos().isEmpty()) {
            texto(pagina, normal, 10, TINTA, MARGEN + 9, cursor + 17, "Sin gastos registrados.");
            return;
        }

        int indice = 0;
        for (GastoRevision gasto : datos.gastos()) {
            List<String> articulo = envolver(gasto.articulo(), normal, 10, COLUMNAS[1] - 18);
            List<String> unidad = envolver(gasto.unidad(), normal, 10, COLUMNAS[3] - 18);
            float alto = 12 + ALTO_LINEA * Math.max(articulo.size(), unidad.size());
            if (cursor + alto > FIN_TABLA) {
                nuevaPagina(datos, false);
                cabeceraTabla();
            }
            fila(gasto, articulo, unidad, alto, indice++);
        }
    }

    private void nuevaPagina(DatosInformeEncuesta datos, boolean primera) throws IOException {
        close();
        PDPage nueva = new PDPage(PAPEL);
        documento.addPage(nueva);
        pagina = new PDPageContentStream(documento, nueva);
        String titulo = "Informe de encuesta " + datos.encuesta();
        if (primera) {
            rectangulo(MARGEN, 39, ANCHO, 80, TINTA);
            texto(pagina, negrita, 23, Color.WHITE, MARGEN + 17, 74, titulo);
            textoAjustado(normal, 12, new Color(194, 219, 229), MARGEN + 17, 100, ANCHO - 34,
                    limpio(datos.familia()) + " · Encuesta cerrada");
            cursor = 144;
        } else {
            texto(pagina, negrita, 16, TINTA, MARGEN, 52, titulo);
            texto(pagina, normal, 10, TEXTO_SECUNDARIO, MARGEN, 69, "Detalle de gastos · Continuación");
            cursor = 84;
        }
    }

    private void resumen(DatosInformeEncuesta datos) throws IOException {
        BigDecimal total = datos.gastos().stream().map(GastoRevision::valor)
                .filter(valor -> valor != null).reduce(BigDecimal.ZERO, BigDecimal::add);
        seccion("Resumen");
        float anchoTarjeta = (ANCHO - 20) / 3;
        tarjeta(MARGEN, anchoTarjeta, "Días cerrados", datos.diasCerrados() + " de 14");
        tarjeta(MARGEN + anchoTarjeta + 10, anchoTarjeta, "Gastos registrados", Integer.toString(datos.gastos().size()));
        tarjeta(MARGEN + 2 * (anchoTarjeta + 10), anchoTarjeta, "Total de gastos", dinero.format(total) + " COP");
        cursor += 75;

        metadatos("Hogar", datos.familia(), "Ubicación", datos.ubicacion());
        metadatos("Encuestador actual", datos.encuestador(), "Administrador del cierre", datos.responsable());
        metadatos("Fecha de transmisión", fecha(datos.transmision()), "Fecha de cierre", fecha(datos.cierre()));
        cursor += 15;
    }

    private void seccion(String titulo) throws IOException {
        texto(pagina, negrita, 15, TINTA, MARGEN, cursor, titulo);
        cursor += 11;
    }

    private void tarjeta(float x, float ancho, String etiqueta, String valor) throws IOException {
        rectangulo(x, cursor, ancho, 56, FONDO);
        texto(pagina, normal, 10, TEXTO_SECUNDARIO, x + 11, cursor + 18, etiqueta);
        textoAjustado(negrita, 19, ACENTO, x + 11, cursor + 41, ancho - 22, valor);
    }

    private void metadatos(String tituloIzq, String valorIzq, String tituloDer, String valorDer) throws IOException {
        float ancho = (ANCHO - 15) / 2;
        float derecha = MARGEN + ancho + 15;
        List<String> izquierda = envolver(valorIzq, negrita, 11, ancho - 5);
        List<String> derechaLineas = envolver(valorDer, negrita, 11, ancho - 5);
        texto(pagina, normal, 10, TEXTO_SECUNDARIO, MARGEN, cursor + 1, tituloIzq);
        texto(pagina, normal, 10, TEXTO_SECUNDARIO, derecha, cursor + 1, tituloDer);
        escribirLineas(izquierda, negrita, 11, MARGEN, cursor + 16);
        escribirLineas(derechaLineas, negrita, 11, derecha, cursor + 16);
        cursor += 20 + ALTO_LINEA * Math.max(izquierda.size(), derechaLineas.size());
    }

    private void cabeceraTabla() throws IOException {
        rectangulo(MARGEN, cursor, ANCHO, 26, FONDO_CABECERA_TABLA);
        String[] titulos = {"Día", "Artículo", "Cantidad", "Unidad", "Valor (COP)"};
        float x = MARGEN;
        for (int i = 0; i < titulos.length; i++) {
            if (i == 2 || i == 4) {
                textoDerecha(negrita, 10, x + COLUMNAS[i] - 9, cursor + 17, titulos[i]);
            } else {
                texto(pagina, negrita, 10, TINTA, x + 9, cursor + 17, titulos[i]);
            }
            x += COLUMNAS[i];
        }
        cursor += 26;
    }

    private void fila(GastoRevision gasto, List<String> articulo, List<String> unidad, float alto, int indice)
            throws IOException {
        if (indice % 2 == 1) {
            rectangulo(MARGEN, cursor, ANCHO, alto, FONDO);
        }
        float y = cursor + 15;
        float xArticulo = MARGEN + COLUMNAS[0];
        float xCantidad = xArticulo + COLUMNAS[1];
        float xUnidad = xCantidad + COLUMNAS[2];
        texto(pagina, normal, 10, TINTA, MARGEN + 9, y, Integer.toString(gasto.dia()));
        escribirLineas(articulo, normal, 10, xArticulo + 9, y);
        textoDerecha(normal, 10, xUnidad - 9, y, gasto.cantidad() == null ? "-" : cantidades.format(gasto.cantidad()));
        escribirLineas(unidad, normal, 10, xUnidad + 9, y);
        textoDerecha(normal, 10, MARGEN + ANCHO - 9, y, gasto.valor() == null ? "-" : dinero.format(gasto.valor()));
        cursor += alto;
        rectangulo(MARGEN, cursor - 0.35f, ANCHO, 0.35f, LINEA);
    }

    private void escribirLineas(List<String> lineas, PDFont fuente, float tamano, float x, float y) throws IOException {
        for (String linea : lineas) {
            texto(pagina, fuente, tamano, TINTA, x, y, linea);
            y += ALTO_LINEA;
        }
    }

    private void textoDerecha(PDFont fuente, float tamano, float derecha, float y, String valor) throws IOException {
        texto(pagina, fuente, tamano, TINTA, derecha - ancho(fuente, tamano, valor), y, valor);
    }

    private void textoAjustado(PDFont fuente, float tamano, Color color, float x, float y, float maximo, String valor)
            throws IOException {
        String visible = textoCompatible(fuente, limpio(valor));
        float ancho = ancho(fuente, tamano, visible);
        float ajustado = ancho > maximo ? tamano * maximo / ancho : tamano;
        texto(pagina, fuente, ajustado, color, x, y, visible);
    }

    private List<String> envolver(String valor, PDFont fuente, float tamano, float maximo) throws IOException {
        String pendiente = textoCompatible(fuente, limpio(valor));
        List<String> lineas = new ArrayList<>();
        while (!pendiente.isEmpty()) {
            if (ancho(fuente, tamano, pendiente) <= maximo) {
                lineas.add(pendiente);
                break;
            }
            int corte = 0;
            int espacio = -1;
            for (int posicion = 0; posicion < pendiente.length();) {
                int siguiente = posicion + Character.charCount(pendiente.codePointAt(posicion));
                if (ancho(fuente, tamano, pendiente.substring(0, siguiente)) > maximo) {
                    break;
                }
                if (pendiente.charAt(posicion) == ' ') {
                    espacio = posicion;
                }
                corte = siguiente;
                posicion = siguiente;
            }
            int fin = espacio > 0 ? espacio : corte == 0 ? pendiente.offsetByCodePoints(0, 1) : corte;
            lineas.add(pendiente.substring(0, fin).stripTrailing());
            pendiente = pendiente.substring(fin).stripLeading();
        }
        return lineas;
    }

    private static String limpio(String texto) {
        return texto == null || texto.isBlank() ? "-" : texto.strip().replaceAll("\\s+", " ");
    }

    private static String textoCompatible(PDFont fuente, String valor) throws IOException {
        try {
            fuente.getStringWidth(valor);
            return valor;
        } catch (IllegalArgumentException ex) {
            StringBuilder disponible = new StringBuilder();
            for (int punto : valor.codePoints().toArray()) {
                String caracter = Character.toString(punto);
                try {
                    fuente.getStringWidth(caracter);
                    disponible.append(caracter);
                } catch (IllegalArgumentException sinGlifo) {
                    disponible.append('?');
                }
            }
            return disponible.toString();
        }
    }

    private static float ancho(PDFont fuente, float tamano, String valor) throws IOException {
        return fuente.getStringWidth(valor) * tamano / 1000;
    }

    private static String fecha(LocalDateTime valor) {
        return valor == null ? "Sin fecha registrada" : FECHA.format(valor);
    }

    private static void texto(PDPageContentStream destino, PDFont fuente, float tamano, Color color,
            float x, float superior, String valor) throws IOException {
        destino.beginText();
        destino.setFont(fuente, tamano);
        destino.setNonStrokingColor(color);
        destino.newLineAtOffset(x, PAPEL.getHeight() - superior);
        destino.showText(valor);
        destino.endText();
    }

    private void rectangulo(float x, float superior, float ancho, float alto, Color color) throws IOException {
        pagina.setNonStrokingColor(color);
        pagina.addRect(x, PAPEL.getHeight() - superior - alto, ancho, alto);
        pagina.fill();
    }

    private void agregarPie(int encuesta) throws IOException {
        int total = documento.getNumberOfPages();
        for (int i = 0; i < total; i++) {
            try (PDPageContentStream pie = new PDPageContentStream(documento, documento.getPage(i),
                    PDPageContentStream.AppendMode.APPEND, true, true)) {
                pie.setStrokingColor(LINEA);
                pie.moveTo(MARGEN, 46);
                pie.lineTo(MARGEN + ANCHO, 46);
                pie.stroke();
                texto(pie, normal, 9, TEXTO_SECUNDARIO, MARGEN, PAPEL.getHeight() - 30,
                        "Registro de hogares · Encuesta " + encuesta);
                String numero = "Página " + (i + 1) + " de " + total;
                texto(pie, normal, 9, TEXTO_SECUNDARIO, MARGEN + ANCHO - ancho(normal, 9, numero),
                        PAPEL.getHeight() - 30, numero);
            }
        }
    }

    @Override public void close() throws IOException {
        if (pagina != null) {
            PDPageContentStream actual = pagina;
            pagina = null;
            actual.close();
        }
    }
}
