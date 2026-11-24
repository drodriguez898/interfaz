-- Ejecutar en SQL*Plus con el usuario dueño del esquema RegistroHogar.
-- Conserva las filas existentes y admite volver a ejecutarse.
SET SERVEROUTPUT ON
DECLARE
  PROCEDURE agregar_columna(p_tabla VARCHAR2, p_columna VARCHAR2, p_definicion VARCHAR2) IS
    cantidad NUMBER;
  BEGIN
    SELECT COUNT(*) INTO cantidad
      FROM USER_TAB_COLUMNS WHERE TABLE_NAME = p_tabla AND COLUMN_NAME = p_columna;
    IF cantidad = 0 THEN
      EXECUTE IMMEDIATE 'ALTER TABLE ' || p_tabla || ' ADD (' || p_columna || ' ' || p_definicion || ')';
    END IF;
  END;
BEGIN
  agregar_columna('COORDINADOR', 'CONTRASENA', 'VARCHAR2(255)');
  agregar_columna('COORDINADOR', 'ROL_ACCESO', 'VARCHAR2(20)');
  agregar_columna('ENCUESTADOR', 'CONTRASENA', 'VARCHAR2(255)');
  agregar_columna('ENCUESTADO', 'DOCUMENTO', 'VARCHAR2(20)');
  agregar_columna('ENCUESTADO', 'CONTRASENA', 'VARCHAR2(255)');
  agregar_columna('ENCUESTA', 'IDADMINISTRADOR',
    'NUMBER CONSTRAINT FK_ENCUESTA_ADMINISTRADOR REFERENCES COORDINADOR (IDCOORDINADOR)');
  DBMS_OUTPUT.PUT_LINE('Esquema actualizado para los tres roles.');
END;
/
