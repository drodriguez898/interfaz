-- Ejecutar como dueño de las tablas en la base ya configurada.
-- Otorga al rol administrador la eliminación de hogares y sus dependencias.
-- El trigger permite al usuario Oracle administrador borrar gastos al
-- eliminar un hogar, también cuando el día o la encuesta están cerrados.
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK

GRANT DELETE ON HOGAR TO RH_ADMINISTRADOR;
GRANT DELETE ON ENCUESTADO TO RH_ADMINISTRADOR;
GRANT DELETE ON ENCUESTA TO RH_ADMINISTRADOR;
GRANT DELETE ON DIACICLO TO RH_ADMINISTRADOR;
GRANT DELETE ON GASTO TO RH_ADMINISTRADOR;
GRANT DELETE ON OBSERVACION TO RH_ADMINISTRADOR;
GRANT SELECT, DELETE ON VISITA TO RH_ADMINISTRADOR;

CREATE OR REPLACE TRIGGER RH_GASTO_EDITABLE
BEFORE INSERT OR UPDATE OR DELETE ON GASTO
FOR EACH ROW
DECLARE
  v_dia DIACICLO.IDDIA%TYPE;
  v_encuesta ENCUESTA.IDENCUESTA%TYPE;
  v_estado_encuesta ENCUESTA.ESTADO%TYPE;
  v_estado_dia DIACICLO.ESTADO%TYPE;
  v_cuenta VARCHAR2(128) := SYS_CONTEXT('USERENV','SESSION_USER');
BEGIN
  IF UPDATING AND (:NEW.IDDIA IS NULL OR :OLD.IDDIA IS NULL OR :NEW.IDDIA <> :OLD.IDDIA
                   OR :NEW.IDGASTO IS NULL OR :NEW.IDGASTO <> :OLD.IDGASTO) THEN
    RAISE_APPLICATION_ERROR(-20011, 'El gasto no puede cambiar de identificador ni de dia.');
  END IF;
  IF DELETING THEN v_dia := :OLD.IDDIA; ELSE v_dia := :NEW.IDDIA; END IF;

  SELECT IDENCUESTA INTO v_encuesta FROM DIACICLO WHERE IDDIA = v_dia;
  SELECT UPPER(TRIM(ESTADO)) INTO v_estado_encuesta
    FROM ENCUESTA WHERE IDENCUESTA = v_encuesta FOR UPDATE;
  SELECT UPPER(TRIM(ESTADO)) INTO v_estado_dia
    FROM DIACICLO WHERE IDDIA = v_dia AND IDENCUESTA = v_encuesta FOR UPDATE;

  IF NOT (DELETING AND v_cuenta = 'RH_APP_ADMIN')
     AND (v_estado_encuesta = 'CERRADO'
          OR ((v_estado_encuesta = 'TRANSMITIDO' OR v_estado_dia = 'CERRADO')
              AND (NOT UPDATING OR v_cuenta <> 'RH_APP_ADMIN'))) THEN
    RAISE_APPLICATION_ERROR(-20012, 'El gasto pertenece a un dia o encuesta de solo lectura.');
  END IF;
EXCEPTION
  WHEN NO_DATA_FOUND THEN
    RAISE_APPLICATION_ERROR(-20013, 'El gasto debe pertenecer a un dia y una encuesta existentes.');
END;
/

DECLARE
  validos NUMBER;
BEGIN
  SELECT COUNT(*) INTO validos FROM USER_OBJECTS
  WHERE OBJECT_TYPE = 'TRIGGER' AND OBJECT_NAME = 'RH_GASTO_EDITABLE' AND STATUS = 'VALID';
  IF validos <> 1 THEN
    RAISE_APPLICATION_ERROR(-20001, 'El disparador no compiló. Consulta USER_ERRORS.');
  END IF;
END;
/
PROMPT Permisos para eliminar hogares instalados.
WHENEVER SQLERROR CONTINUE NONE
