package negocio;

import java.math.BigDecimal;

public record DatosEditables(int diaId, Integer articuloId, BigDecimal cantidad, String unidad,
        String adquisicion, BigDecimal valor, String frecuencia, String tipo, String lugar) {}
