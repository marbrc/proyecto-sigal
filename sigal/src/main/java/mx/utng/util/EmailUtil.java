package mx.utng.util;
 
import java.util.Properties;
 
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
 
 
public class EmailUtil {
 
    private static final String CORREO_ORIGEN = "sigal.utng@gmail.com"; // creé una cuenta de correo e hice la verificacion en dos pasos para que me dejará arrpjar la contraseña aleatoria
    private static final String CONTRASENA_APP = "v v i s o v d x d q i t j r q p";
 
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
 
    public static void enviarCodigoRecuperacion(String correoDestino, String nombreUsuario, String codigo)
            throws MessagingException {
 
        enviarCorreoConCodigo(
                correoDestino,
                "SIGAL · Código de verificación",
                construirHtml(nombreUsuario, codigo,
                        "Recibimos una solicitud para restablecer tu contraseña. Usa el siguiente código para continuar:")
        );
    }
 
    /** Igual que enviarCodigoRecuperacion, pero con el texto adecuado para confirmar una cuenta nueva. */
    public static void enviarCodigoRegistro(String correoDestino, String nombreUsuario, String codigo)
            throws MessagingException {
 
        enviarCorreoConCodigo(
                correoDestino,
                "SIGAL · Verifica tu cuenta",
                construirHtml(nombreUsuario, codigo,
                        "Estás a un paso de crear tu cuenta en SIGAL. Usa el siguiente código para verificar tu correo:")
        );
    }
 
    private static void enviarCorreoConCodigo(String correoDestino, String asunto, String htmlContenido)
            throws MessagingException {
 
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
 
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(CORREO_ORIGEN, CONTRASENA_APP);
            }
        });
 
        Message mensaje = new MimeMessage(session);
        mensaje.setFrom(new InternetAddress(CORREO_ORIGEN, false));
        mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(correoDestino));
        mensaje.setSubject(asunto);
        mensaje.setContent(htmlContenido, "text/html; charset=UTF-8");
 
        Transport.send(mensaje);
    }
 
    private static String construirHtml(String nombreUsuario, String codigo, String textoIntro) {
 
        String nombreSeguro = (nombreUsuario == null || nombreUsuario.isBlank()) ? "usuario" : nombreUsuario;
 
        return "<div style=\"font-family:'Segoe UI',Arial,sans-serif;background:#050814;padding:40px 0;\">"
             + "  <div style=\"max-width:420px;margin:0 auto;background:#070c22;border:1px solid rgba(130,180,255,0.4);border-radius:18px;padding:36px;\">"
             + "    <h2 style=\"color:#ffffff;margin:0 0 6px;\">SIGAL</h2>"
             + "    <p style=\"color:#93a0c4;margin:0 0 26px;font-size:13px;\">Sistema de Gestión y Asignación de Laboratorios</p>"
             + "    <p style=\"color:#e4e9f7;font-size:14px;\">Hola " + nombreSeguro + ",</p>"
             + "    <p style=\"color:#c7cfe8;font-size:14px;\">" + textoIntro + "</p>"
             + "    <div style=\"text-align:center;margin:28px 0;\">"
             + "      <span style=\"display:inline-block;background:linear-gradient(to right,#2f6bff,#7b2ff7);color:#fff;font-size:28px;letter-spacing:8px;font-weight:bold;padding:14px 26px;border-radius:12px;\">" + codigo + "</span>"
             + "    </div>"
             + "    <p style=\"color:#93a0c4;font-size:12.5px;\">Este código vence en 10 minutos. Si tú no solicitaste esto, ignora este mensaje.</p>"
             + "    <p style=\"color:#5a6690;font-size:11px;margin-top:30px;\">© 2026 SIGAL — Universidad Tecnológica del Norte de Guanajuato</p>"
             + "  </div>"
             + "</div>";
    }
}
 