package control;

import javax.swing.SwingUtilities;
import presentacion.AccesoForm;
import presentacion.ConfigurarAdministrador;

public final class Main {
    private Main() {}

    public static void main(String[] args) {
        boolean configurar = args.length == 1 && "--configurar-admin".equals(args[0]);
        SwingUtilities.invokeLater(configurar ? ConfigurarAdministrador::mostrarFormulario : AccesoForm::abrir);
    }
}
