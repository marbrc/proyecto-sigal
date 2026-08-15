package mx.utng.util;

import java.io.ByteArrayInputStream;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;

/**
 * Ayuda a mostrar la foto de perfil (guardada como bytes en
 * tb_usuario.FotoPerfil) en los distintos círculos de avatar que
 * tiene SIGAL: la tarjeta de "Mi cuenta", el chip de la topbar y
 * el panel desplegable de cuenta.
 *
 * Todas esas pantallas dibujan el ícono por defecto (una silueta)
 * con formas vectoriales (Group con Circle/Rectangle) y, encima,
 * un ImageView oculto listo para la foto real. Este helper solo
 * decide cuál de los dos se ve.
 */
public final class AvatarUtil {

    private AvatarUtil() {
    }

    /**
     * Si fotoBytes trae datos, los muestra en imgView (recortado en
     * círculo) y esconde el ícono por defecto. Si viene null/vacío,
     * hace lo contrario: deja el ícono por defecto visible.
     *
     * @param imgView   ImageView reservado para la foto (puede estar en managed=false / visible=false en el FXML)
     * @param iconoPorDefecto nodo con el ícono vectorial de silueta (Group, Label, etc.); puede ser null si esa pantalla no tiene uno
     * @param fotoBytes bytes de la imagen (JPG/PNG) tal como vienen de la base de datos, o null
     */
    public static void aplicar(ImageView imgView, Node iconoPorDefecto, byte[] fotoBytes) {
        if (imgView == null) {
            return;
        }

        if (fotoBytes != null && fotoBytes.length > 0) {
            Image imagen = new Image(new ByteArrayInputStream(fotoBytes));
            imgView.setImage(imagen);
            imgView.setVisible(true);
            imgView.setManaged(true);
            recortarEnCirculo(imgView);
            if (iconoPorDefecto != null) {
                iconoPorDefecto.setVisible(false);
            }
        } else {
            imgView.setImage(null);
            imgView.setVisible(false);
            imgView.setManaged(false);
            if (iconoPorDefecto != null) {
                iconoPorDefecto.setVisible(true);
            }
        }
    }

    /** Le pone un clip circular al ImageView, usando la mitad de su fitWidth/fitHeight como radio. */
    private static void recortarEnCirculo(ImageView imgView) {
        double ancho = imgView.getFitWidth();
        double alto = imgView.getFitHeight();
        double radio = Math.min(ancho, alto) / 2.0;

        Circle clip = new Circle(ancho / 2.0, alto / 2.0, radio);
        imgView.setClip(clip);
    }
}
