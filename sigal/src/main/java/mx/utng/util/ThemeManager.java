package mx.utng.util;
 
import javafx.scene.Scene;
 
/**
 * Controla el tema visual de las pantallas de Ajustes / Acerca del sistema
 * (oscuro, claro y azul original -el look por defecto de SIGAL-).
 *
 * Uso desde un controller:
 *   ThemeManager.setTema(ThemeManager.Tema.AZUL_ORIGINAL);
 *   ThemeManager.apply(cualquierNodo.getScene());
 */
public class ThemeManager {
 
    public enum Tema {
        OSCURO("/mx/utng/view/theme_dark.css"),
        CLARO("/mx/utng/view/theme_light.css"),
        AZUL_ORIGINAL("/mx/utng/view/theme_azul.css");
 
        private final String ruta;
 
        Tema(String ruta) {
            this.ruta = ruta;
        }
 
        public String getRuta() {
            return ruta;
        }
 
        /**
         * Convierte el valor guardado en tb_usuario.tema (ej. "OSCURO",
         * "claro", "azul_original") al enum correspondiente. Si viene
         * vacío, null, o con un valor que no reconoce, regresa el tema
         * por defecto (AZUL_ORIGINAL) en vez de tronar.
         */
        public static Tema desdeValorBD(String valor) {
            if (valor == null || valor.isBlank()) {
                return AZUL_ORIGINAL;
            }
            try {
                return Tema.valueOf(valor.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return AZUL_ORIGINAL;
            }
        }
 
        /** Valor listo para guardar en tb_usuario.tema. */
        public String getValorBD() {
            return this.name();
        }
    }
 
    // Tema con el que arranca la app: el azul original de SIGAL.
    private static Tema temaActual = Tema.AZUL_ORIGINAL;
 
    public static Tema getTema() {
        return temaActual;
    }
 
    public static void setTema(Tema tema) {
        temaActual = tema;
    }
 
    // --- Compatibilidad con el código anterior (booleano oscuro/claro) ---
    public static boolean isOscuro() {
        return temaActual == Tema.OSCURO;
    }
 
    public static void setOscuro(boolean valor) {
        temaActual = valor ? Tema.OSCURO : Tema.CLARO;
    }
 
    public static void apply(Scene scene) {
        if (scene == null) {
            return;
        }
        scene.getStylesheets().clear();
        scene.getStylesheets().add(
                ThemeManager.class.getResource(temaActual.getRuta()).toExternalForm());
    }
}