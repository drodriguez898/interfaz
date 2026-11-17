-- Ejecutar en SQL*Plus con el mismo usuario y servicio que utiliza el programa.
-- Este archivo solo consulta los datos.
SET LINESIZE 150
SET PAGESIZE 40
COLUMN TABLA FORMAT A16
COLUMN USO FORMAT A66
COLUMN ESTADO FORMAT A9

SELECT tabla, cantidad, CASE WHEN cantidad = 0 THEN 'VACIA' ELSE 'CON DATOS' END AS estado, uso
FROM (
    SELECT 1 AS orden, 'COORDINADOR' AS tabla, COUNT(*) AS cantidad,
           'Cuentas de administrador (solo ROL_ACCESO = ADMINISTRADOR)' AS uso
    FROM COORDINADOR WHERE ROL_ACCESO = 'ADMINISTRADOR'
    UNION ALL SELECT 2, 'ENCUESTADOR', COUNT(*), 'Se registra desde Usuarios' FROM ENCUESTADOR
    UNION ALL SELECT 3, 'HOGAR', COUNT(*), 'Se registra desde Hogares' FROM HOGAR
    UNION ALL SELECT 4, 'ENCUESTADO', COUNT(*), 'Personas del hogar y cuentas de jefes' FROM ENCUESTADO
    UNION ALL SELECT 5, 'ARTICULO', COUNT(*), 'Catalogo para seleccionar al registrar gastos' FROM ARTICULO
    UNION ALL SELECT 6, 'ENCUESTA', COUNT(*), 'Se genera al abrir el ciclo mediante el primer gasto o cierre' FROM ENCUESTA
    UNION ALL SELECT 7, 'DIACICLO', COUNT(*), 'Se genera al guardar un gasto o cerrar un dia' FROM DIACICLO
    UNION ALL SELECT 8, 'GASTO', COUNT(*), 'Se llena al registrar gastos' FROM GASTO
    UNION ALL SELECT 9, 'VISITA', COUNT(*), 'Se llena al registrar visitas' FROM VISITA
    UNION ALL SELECT 10, 'OBSERVACION', COUNT(*), 'Puede quedar vacia hasta que haya correcciones' FROM OBSERVACION
)
ORDER BY orden;

SELECT COUNT(*) AS hogares_sin_encuestador FROM HOGAR WHERE IDENCUESTADOR IS NULL;

SELECT COUNT(*) AS hogares_sin_cuenta_jefe
FROM HOGAR h
WHERE NOT EXISTS (
    SELECT 1 FROM ENCUESTADO e
    WHERE e.IDHOGAR = h.IDHOGAR AND e.DOCUMENTO IS NOT NULL AND e.CONTRASENA IS NOT NULL
      AND UPPER(TRIM(CAST(e.ESENCARGADOGASTOS AS VARCHAR2(10)))) IN ('S', 'SI', 'SÍ', '1')
);
