-- Ejecutar con un DBA en el mismo servicio que contiene las tablas existentes.
SET VERIFY OFF
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK

-- Las cuentas nuevas se crean bloqueadas y sin espacio para crear tablas.
-- Si ya existen, este paso conserva sus contraseñas y su estado.
DECLARE
  cantidad NUMBER;
  PROCEDURE crear_rol(nombre VARCHAR2) IS
  BEGIN
    SELECT COUNT(*) INTO cantidad FROM DBA_ROLES WHERE ROLE = nombre;
    IF cantidad = 0 THEN
      EXECUTE IMMEDIATE 'CREATE ROLE ' || nombre;
    END IF;
  END;
  PROCEDURE crear_cuenta(nombre VARCHAR2) IS
  BEGIN
    SELECT COUNT(*) INTO cantidad FROM DBA_USERS WHERE USERNAME = nombre;
    IF cantidad = 0 THEN
      EXECUTE IMMEDIATE 'CREATE USER ' || nombre || ' IDENTIFIED BY "Rh#'
        || SUBSTR(RAWTOHEX(SYS_GUID()), 1, 20) || '" ACCOUNT LOCK';
    END IF;
  END;
BEGIN
  crear_rol('RH_ADMINISTRADOR');
  crear_rol('RH_ENCUESTADOR');
  crear_rol('RH_JEFE_HOGAR');
  crear_cuenta('RH_APP_ACCESO');
  crear_cuenta('RH_APP_ADMIN');
  crear_cuenta('RH_APP_ENC');
  crear_cuenta('RH_APP_JEFE');
END;
/

GRANT CREATE SESSION TO RH_APP_ACCESO;
GRANT CREATE SESSION TO RH_APP_ADMIN;
GRANT RH_ADMINISTRADOR TO RH_APP_ADMIN;
GRANT CREATE SESSION TO RH_APP_ENC;
GRANT RH_ENCUESTADOR TO RH_APP_ENC;
GRANT CREATE SESSION TO RH_APP_JEFE;
GRANT RH_JEFE_HOGAR TO RH_APP_JEFE;

PROMPT Esquemas y roles creados. Establece las contrasenas en SQL*Plus:
PROMPT PASSWORD RH_APP_ACCESO
PROMPT PASSWORD RH_APP_ADMIN
PROMPT PASSWORD RH_APP_ENC
PROMPT PASSWORD RH_APP_JEFE
PROMPT Luego ejecuta 02_permisos_y_disparadores.sql como dueno de las tablas.
WHENEVER SQLERROR CONTINUE NONE
