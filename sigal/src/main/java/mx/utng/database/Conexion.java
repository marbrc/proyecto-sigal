package mx.utng.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    private static final String URL = "jdbc:mysql://localhost:3306/db_sigal"; //3306 puerto de mar 
    private static final String USER = "root";
    private static final String PASSWORD = ""; // sin contraseña para mar

    public static Connection conectar() {

        try {

            Connection conexion = DriverManager.getConnection(URL, USER, PASSWORD);

            System.out.println("✅ Conexión exitosa a db_sigal");

            return conexion;

        } catch (SQLException e) {

            System.out.println("❌ Error al conectar con la base de datos");
            e.printStackTrace();

            return null;

        }

    }

}