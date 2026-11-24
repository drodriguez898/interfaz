package negocio;


public record CorreccionGasto(String articulo, String cantidad, String unidad, String adquisicion,
        String valor, String frecuencia, String tipo, String lugar, String nota) {}
